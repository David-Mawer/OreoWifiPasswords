package com.pithsoftware.wifipasswords.recycler;

import com.pithsoftware.wifipasswords.pojo.WifiEntry;

public interface ItemTouchHelperAdapter {

    boolean onItemMove(int fromPosition, int toPosition);

    WifiEntry onItemDismiss(int position);

}
