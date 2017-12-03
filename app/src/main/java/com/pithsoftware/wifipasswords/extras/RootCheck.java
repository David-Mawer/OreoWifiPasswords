package com.pithsoftware.wifipasswords.extras;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;


public class RootCheck {

    private static final String TAG = "ROOT";

    /***********************************************************************/
    //Root Check method
    //Credit: http://muzikant-android.blogspot.co.il/2011/02/how-to-get-root-access-and-execute.html
    /***********************************************************************/
    public static boolean canRunRootCommands() {
        boolean retval;
        Process suProcess;

        try {
            suProcess = Runtime.getRuntime().exec("su -c id");
            BufferedReader suCheckReader = new BufferedReader(new InputStreamReader(suProcess.getInputStream()));
            String currUid = suCheckReader.readLine();
            if (null == currUid) {
                retval = false;
                Log.d(TAG, "Can't get root access or denied by user");
            } else if (currUid.contains("uid=0")) {
                retval = true;
                Log.d(TAG, "Root access granted");
            } else {
                retval = false;
                Log.d(TAG, "Root access rejected: " + currUid);
            }
        } catch (Exception e) {
            // Can't get root !
            // Probably broken pipe exception on trying to write to output stream (os) after su failed, meaning that the device is not rooted

            retval = false;
            Log.d(TAG, "Root access rejected [" + e.getClass().getName() + "] : " + e.getMessage());
        }

        return retval;
    }

}
