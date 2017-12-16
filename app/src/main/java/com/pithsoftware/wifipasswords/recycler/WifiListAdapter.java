package com.pithsoftware.wifipasswords.recycler;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pithsoftware.wifipasswords.R;
import com.pithsoftware.wifipasswords.extras.MyApplication;
import com.pithsoftware.wifipasswords.pojo.WifiEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.MyViewHolder>
        implements ItemTouchHelperAdapter {

    private LayoutInflater layoutInflater;
    private List<WifiEntry> mListWifi;
    private ItemDragListener mDragListener;
    private boolean mShowDragHandler;
    private Context mContext;
    private SparseBooleanArray mSelectedItems = new SparseBooleanArray();
    private boolean mShowPublic = false;


    public WifiListAdapter(Context context, ItemDragListener dragListener) {
        layoutInflater = LayoutInflater.from(context);
        mContext = context;
        mDragListener = dragListener;
        mListWifi = new ArrayList<>();
        mShowDragHandler = false;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = layoutInflater.inflate(R.layout.wifi_entry_row, parent, false);

        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        WifiEntry currentEntry = mListWifi.get(position);

        holder.mTitle.setText(currentEntry.getTitle());
        holder.mCurrentInd.setChecked(currentEntry.getConnectedInd());
        holder.mPassword.setText(currentEntry.getPassword(false));
        holder.mTagText.setText(currentEntry.getTag());


        //Selected Background
        if (mSelectedItems.get(position, false)) {
            holder.mBackground.setBackgroundResource(R.color.colorHighlight);
        } else {
            if (PreferenceManager.getDefaultSharedPreferences(mContext)
                    .getBoolean(mContext.getString(R.string.pref_dark_theme_key), false)) {
                holder.mBackground.setBackground(ResourcesCompat.getDrawable(mContext.getResources(),
                        R.drawable.highlight_selected_dark, mContext.getTheme()));
            } else {
                holder.mBackground.setBackgroundResource(R.drawable.highlight_selected);
            }
        }

        //Drag Icon
        holder.mDragHandler.setOnTouchListener((v, event) -> {
            if (MotionEventCompat.getActionMasked(event) ==
                    MotionEvent.ACTION_DOWN) {
                mDragListener.onStartDrag(holder);
            }
            return false;
        });

        //Tag & Drag Handler Visibility
        toggleTagAndDrag(holder);
    }


    @Override
    public int getItemCount() {
        return mListWifi.size();
    }


    public void setWifiList(ArrayList<WifiEntry> listWifi, boolean showPublic) {
        mShowPublic = showPublic;
        mListWifi = listWifi;
        // Clean up the list
        validateAllEntries();
        notifyDataSetChanged();
    }

    public String getItemPassword(int position) {
        WifiEntry thisEntry = mListWifi.get(position);

        if (thisEntry != null) {
            return thisEntry.getPassword(true);
        }
        return "";
    }


    private void toggleTagAndDrag(MyViewHolder holder) {
        if (mShowDragHandler) {
            holder.mDragHandler.setVisibility(View.VISIBLE);
            holder.mDragHandler.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.simple_grow));
            holder.mTagText.setVisibility(View.GONE);

        } else {
            holder.mDragHandler.setVisibility(View.GONE);

            if (!holder.mTagText.getText().toString().replace(" ", "").isEmpty()) {
                holder.mTagText.setVisibility(View.VISIBLE);

            } else {
                holder.mTagText.setVisibility(View.GONE);
            }
        }
    }

    /*******************************************************/
    /************ Contextual Action Mode Methods ***********/
    /*******************************************************/

    public void toggleSelection(int position) {

        if (mSelectedItems.get(position, false)) {
            mSelectedItems.delete(position);
        } else {
            mSelectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    public void clearSelection() {

        mSelectedItems.clear();
        notifyDataSetChanged();
    }

    public ArrayList<Integer> getSelectedItems() {

        ArrayList<Integer> items = new ArrayList<>(mSelectedItems.size());

        for (int i = 0; i < mSelectedItems.size(); i++) {
            items.add(mSelectedItems.keyAt(i));
        }
        return items;
    }

    // Remove the entry if it's got a blank password, and "open networks" not checked.
    // tick the entry if it's the current connection.
    private int validateAllEntries() {
        int result = 0;
        String currentSSID = getWifiName();
        try {
            for (int i = mListWifi.size() - 1; i >= 0; i--) {
                WifiEntry thisEntry = mListWifi.get(i);
                if (!mShowPublic && thisEntry.getPassword(true).equals(MyApplication.NO_PASSWORD_TEXT)) {
                    removeItem(i);
                    result++;
                } else {
                    String title = thisEntry.getTitle();
                    thisEntry.setConnectedInd(title.equals(currentSSID));
                }
            }
        } catch (Exception error) {
            if (error != null) {

            }
        }
        return result;
    }

    private String getWifiName() {
        WifiManager manager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (manager.isWifiEnabled()) {
            WifiInfo wifiInfo = manager.getConnectionInfo();
            if (wifiInfo != null) {
                NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
                if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    String result = wifiInfo.getSSID();
                    if (Character.toString(result.charAt(0)).equals("\"")) {
                        result = result.substring(1, result.length() - 1);
                    }
                    return result;
                }
            }
        }
        return null;
    }


    /**********************************************/
    /************ Items Changes Methods ***********/
    /**********************************************/

    public WifiEntry removeItem(int position) {

        final WifiEntry entry = mListWifi.remove(position);

        if (mSelectedItems.get(position, false)) {
            mSelectedItems.delete(position);
        }

        notifyItemRemoved(position);
        return entry;
    }

    public void addItem(int position, WifiEntry entry) {

        mListWifi.add(position, entry);
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {

        final WifiEntry entry = mListWifi.remove(fromPosition);
        mListWifi.add(toPosition, entry);
        notifyItemMoved(fromPosition, toPosition);
    }


    /*********************************************/
    /************ Animate Search Query ***********/
    /*********************************************/

    public void animateTo(ArrayList<WifiEntry> listWifi) {
        //Order is important
        applyAndAnimateRemovals(listWifi);
        applyAndAnimateAdditions(listWifi);
        applyAndAnimateMovedItems(listWifi);
    }

    private void applyAndAnimateRemovals(ArrayList<WifiEntry> newListWifi) {

        for (int i = mListWifi.size() - 1; i >= 0; i--) {
            final WifiEntry entry = mListWifi.get(i);
            if (!newListWifi.contains(entry)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(ArrayList<WifiEntry> newListWifi) {

        for (int i = 0, count = newListWifi.size(); i < count; i++) {
            final WifiEntry entry = newListWifi.get(i);
            if (!mListWifi.contains(entry)) {
                addItem(i, entry);
            }
        }
    }

    private void applyAndAnimateMovedItems(ArrayList<WifiEntry> newListWifi) {

        for (int toPosition = newListWifi.size() - 1; toPosition >= 0; toPosition--) {
            final WifiEntry entry = newListWifi.get(toPosition);
            final int fromPosition = mListWifi.indexOf(entry);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    /*****************************************************/
    /************** ItemTouchHelper Methods***************/
    /*****************************************************/

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mListWifi, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mListWifi, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);

        return true;
    }

    @Override
    public WifiEntry onItemDismiss(int position) {

        return removeItem(position);
    }

    public void showDragHandler(boolean show) {
        mShowDragHandler = show;
        notifyDataSetChanged();
    }


    /*****************************************/
    /********** View Holder Sub-Class ********/
    /*****************************************/

    class MyViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.title_wifi)
        TextView mTitle;
        @Bind(R.id.current_wifi)
        CheckBox mCurrentInd;
        @Bind(R.id.password_wifi)
        TextView mPassword;
        @Bind(R.id.drag_handler)
        ImageView mDragHandler;
        @Bind(R.id.wifi_entry_layout)
        LinearLayout mBackground;
        @Bind(R.id.tag_wifi_text)
        TextView mTagText;

        MyViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
