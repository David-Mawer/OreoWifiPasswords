package com.pithsoftware.wifipasswords.dialogs;


import com.pithsoftware.wifipasswords.pojo.WifiEntry;

import java.util.ArrayList;

public interface InputDialogListener {

    void onSubmitAddDialog(String title, String password);

    void onSubmitTagDialog(String tag, ArrayList<WifiEntry> listWifi, ArrayList<Integer> positions);
}
