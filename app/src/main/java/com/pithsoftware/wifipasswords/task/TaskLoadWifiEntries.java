package com.pithsoftware.wifipasswords.task;


import android.os.AsyncTask;
import android.util.Xml;

import com.pithsoftware.wifipasswords.database.PasswordDB;
import com.pithsoftware.wifipasswords.dialogs.CustomAlertDialogListener;
import com.pithsoftware.wifipasswords.extras.MyApplication;
import com.pithsoftware.wifipasswords.extras.RootCheck;
import com.pithsoftware.wifipasswords.pojo.WifiEntry;
import com.pithsoftware.wifipasswords.recycler.WifiListLoadedListener;
import com.stericson.RootShell.RootShell;
import com.stericson.RootShell.execution.Command;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import static com.stericson.RootShell.RootShell.commandWait;

/***********************************************************************/
//Copy wpa_supplicant.conf from /data/misc/wifi to sdcard/WifiPasswords

/***********************************************************************/
public class TaskLoadWifiEntries extends AsyncTask<String, Void, ArrayList<WifiEntry>> {

    WifiListLoadedListener mListListener;
    boolean mRootAccess = false;
    String mPath;
    String mFileName;
    CustomAlertDialogListener mDialogListener;
    boolean mResetDB;
    String[] mLocationList = {
            "/data/misc/wifi/wpa_supplicant.conf",
            "/data/wifi/bcm_supp.conf",
            "/data/misc/wifi/wpa.conf"
    };
    String[] mOreoLocationList = {
            "/data/misc/wifi/WifiConfigStore.xml"
    };
    boolean mManualLocation;

    final String BSSID = "bssid";
    final String SSID = "ssid";
    final String WPA_PSK = "psk";
    final String WEP_PSK = "wep_key0";
    final String ENTRY_START = "network={";
    final String ENTRY_END = "}";

    //Constructor for Manual Path
    public TaskLoadWifiEntries(String filePath, String fileName, boolean resetDB, WifiListLoadedListener listListener, CustomAlertDialogListener dialogListener) {
        mListListener = listListener;
        mPath = filePath;
        mFileName = fileName;
        mDialogListener = dialogListener;
        mResetDB = resetDB;
        mManualLocation = true;
        mRootAccess = RootCheck.canRunRootCommands();
    }

    //Constructor for Known Paths
    public TaskLoadWifiEntries(boolean resetDB, WifiListLoadedListener listListener, CustomAlertDialogListener dialogListener) {
        mListListener = listListener;
        mDialogListener = dialogListener;
        mResetDB = resetDB;
        mManualLocation = false;
        mRootAccess = RootCheck.canRunRootCommands();
    }


    @Override
    protected ArrayList<WifiEntry> doInBackground(String... params) {
        ArrayList<WifiEntry> result;
        mRootAccess = RootCheck.canRunRootCommands();
        if (android.os.Build.VERSION.SDK_INT >= 26) { // Hard-CODED: Oreo
            if (mManualLocation) {
                if (mFileName.endsWith(".xml")) {
                    result = readOreoFile(mPath + mFileName);
                    if (result == null) {
                        cancel(true);
                    }
                } else {
                    result = readFile();
                }
            } else if (!mRootAccess) {
                cancel(true);
                return null;
            } else {
                result = new ArrayList<>();
                for (String oreoLocation : mOreoLocationList) {
                    ArrayList<WifiEntry> currentFile = readOreoFile(oreoLocation);
                    if ((currentFile != null) && (!currentFile.isEmpty())) {
                        result.addAll(currentFile);
                    }
                }
            }
        } else {
            result = readFile();
        }

        return result;
    }


    @Override
    protected void onPostExecute(ArrayList<WifiEntry> wifiEntries) {

        //Insert Wifi Entries to database
        PasswordDB db = MyApplication.getWritableDatabase();

//        if (mResetDB) {
//            db.purgeDatabase();
//        }

        db.insertWifiEntries(wifiEntries, mResetDB, false); //keep Tags according to mResetDB

        //Update RecyclerView
        if (mListListener != null) {

            wifiEntries = new ArrayList<>(db.getAllWifiEntries(false)); //re-read list from database as it removes duplicates
            mListListener.onWifiListLoaded(wifiEntries, mResetDB ? wifiEntries.size() : PasswordDB.mNewEntriesOnLastInsert, mResetDB);
        }

        MyApplication.closeDatabase();
    }


    @Override
    protected void onCancelled() {
        super.onCancelled();

        //Show "No Root Access" error
        if (!mRootAccess) {
            if (mDialogListener != null) {
                mDialogListener.showRootErrorDialog();
            }
        }

    }


    /****************************************************/
    /****************** Helper Methods ******************/
    /****************************************************/

    private static ArrayList<String> executeForResult(String cmd) {
        final ArrayList<String> results = new ArrayList<>();
        Command command = new Command(3, false, cmd) {

            @Override
            public void commandOutput(int id, String line) {
                results.add(line);
                super.commandOutput(id, line);
            }
        };
        try {
            RootShell.getShell(true).add(command);
            commandWait(RootShell.getShell(true), command);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return results;
    }

    /****************** Oreo Helper Methods: BEGIN ******************/
    private ArrayList<WifiEntry> readNetworkList(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<WifiEntry> result = new ArrayList<>();
        parser.require(XmlPullParser.START_TAG, null, "NetworkList");
        boolean doLoop = true;
        while (doLoop) {
            parser.next();
            String tagName = parser.getName();
            if (tagName == null) {
                tagName = "";
            }
            doLoop = (!tagName.equalsIgnoreCase("NetworkList"));
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            if (tagName.equals("Network")) {
                WifiEntry newWifi = readNetworkEntry(parser);
                if (newWifi.getTitle().length() != 0) {
                    result.add(newWifi);
                }
            } else {
                skip(parser);
            }
        }
        return result;
    }

    // Parses a "Network" entry
    private WifiEntry readNetworkEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "Network");
        WifiEntry result = new WifiEntry("", MyApplication.NO_PASSWORD_TEXT);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tagName = parser.getName();
            // Starts by looking for the entry tag
            if (tagName.equals("WifiConfiguration")) {
                result = readWiFiConfig(parser, result);
//            } else if (tagName.equals("WifiEnterpriseConfiguration")) {
//                result.setTyp(WifiObject.TYP_ENTERPRISE);
            } else {
                skip(parser);
            }
        }
        return result;
    }

    // Parses a "WifiConfiguration" entry
    private WifiEntry readWiFiConfig(XmlPullParser parser, WifiEntry result) throws XmlPullParserException, IOException {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tagName = parser.getName();
            String name = parser.getAttributeValue(null, "name");
            if (name.equals("SSID") && !tagName.equalsIgnoreCase("null")) {
                result.setTitle(readTag(parser, tagName));
            } else if (name.equals("PreSharedKey") && !tagName.equalsIgnoreCase("null")) {
                String newPwd = readTag(parser, tagName);
                if (newPwd.length() > 0) {
                    result.setPassword(newPwd);
//                  result.setTyp(WifiObject.TYP_WPA);
                }
            } else if (name.equals("WEPKeys") && !tagName.equalsIgnoreCase("null")) {
                String newPwd = readTag(parser, tagName);
                if (newPwd.length() > 0) {
                    result.setPassword(readTag(parser, tagName));
//                  result.setTyp(WifiObject.TYP_WEP);
                }
            } else {
                skip(parser);
            }
        }
        return result;
    }

    // Return the text for a specified tag.
    private String readTag(XmlPullParser parser, String tagName) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, tagName);
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        parser.require(XmlPullParser.END_TAG, null, tagName);
        if (tagName.equalsIgnoreCase("string")
                && Character.toString(result.charAt(0)).equals("\"")) {
            result = result.substring(1, result.length() - 1);
        }
        return result;
    }

    // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
    // finds the matching END_TAG (as indicated by the value of "depth" being 0).
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private ArrayList<WifiEntry> readOreoFile(String configLocation) {
        ArrayList<WifiEntry> result = new ArrayList<>();
        try {
            if (RootShell.exists(configLocation)) {
                ArrayList<String> fileLines = executeForResult("su -c /system/bin/cat " + configLocation);
                if (fileLines == null) {
                    return null;
                }
                StringBuilder buffer = new StringBuilder();
                for (String thisLine : fileLines) {
                    buffer.append(thisLine).append("\n");
                }
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(new StringReader(buffer.toString()));
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    if (parser.getName().equalsIgnoreCase("NetworkList")) {
                        // Process the <Network> entries in the list
                        result.addAll(readNetworkList(parser));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        if (!result.isEmpty()) {
            return result;
        } else {
            return null;
        }
    }

    /****************** Oreo Helper Methods: END ******************/

    private ArrayList<WifiEntry> readFile() {

        ArrayList<WifiEntry> listWifi = new ArrayList<>();

        try {
            String configFileName = "";
            if (mManualLocation) {
                if (RootShell.exists(mPath + mFileName)) {
                    configFileName = mPath + mFileName;
                }
            } else {
                for (String testFile : mLocationList) {
                    if (RootShell.exists(testFile)) {
                        configFileName = testFile;
                        break;
                    }
                }
            }

            if (configFileName.length() > 0) {
                String title = "";
                String password = "";
                boolean processingEntry = false;
                ArrayList<String> fileLines = executeForResult("su -c /system/bin/cat " + configFileName);
                for (String line : fileLines) {
                    if (processingEntry) {
                        if (line.contains(ENTRY_END)) {
                            // Finished with this entry now - clean up the data and add it to the list.
                            if (password.equals("")) {
                                password = MyApplication.NO_PASSWORD_TEXT;
                            }

                            WifiEntry current = new WifiEntry(title, password);
                            listWifi.add(current);
                            // clear the current entry variables.
                            title = "";
                            password = "";
                            processingEntry = false;
                        } else {
                            // still processing the current entry; check for valid data.
                            if ((line.contains(SSID + "=") || line.contains(SSID + " =")) && !line.contains(BSSID)) {
                                title = line.replace(SSID, "").replace("=", "").replace("\"", "").replace(" ", "");
                            }
                            if (line.contains(WPA_PSK + "=") || line.contains(WPA_PSK + " =")) {
                                password = line.replace(WPA_PSK, "").replace("=", "").replace("\"", "").replace(" ", "");
                            } else if (line.contains(WEP_PSK + "=") || line.contains(WEP_PSK + " =")) {
                                password = line.replace(WEP_PSK, "").replace("=", "").replace("\"", "").replace(" ", "");
                            }
                        }
                    } else {
                        processingEntry = line.contains(ENTRY_START);
                    }
                }

            } else if (mRootAccess) {
                mDialogListener.showPathErrorDialog();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return listWifi;
    }

}
