package com.pithsoftware.wifipasswords.extras;

import android.util.Log;

import com.stericson.RootShell.RootShell;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class RootCheck {

    private static final String TAG = "ROOT";

    /***********************************************************************/
    //Root Check method
    //Credit: http://muzikant-android.blogspot.co.il/2011/02/how-to-get-root-access-and-execute.html
    /***********************************************************************/
    public static boolean canRunRootCommands() {
        boolean retval;

        try {
            if (!RootShell.isRootAvailable()) {
                retval = false;
                Log.d(TAG, "Can't get root access or denied by user");
            } else if (RootShell.isAccessGiven()) {
                retval = true;
                Log.d(TAG, "Root access granted");
            } else {
                retval = false;
                Log.d(TAG, "Root access rejected");
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
