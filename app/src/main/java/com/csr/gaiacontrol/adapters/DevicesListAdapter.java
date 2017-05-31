/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/
package com.csr.gaiacontrol.adapters;

import java.util.Set;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.csr.gaiacontrol.R;
import com.csr.gaiacontrol.views.DeviceViewHolder;

/**
 * This adapter allows to display a BluetoothDevice list in a RecyclerView.
 */
public class DevicesListAdapter extends RecyclerView.Adapter<DeviceViewHolder> implements DeviceViewHolder.IViewHolder {

    /**
     * The position when any item is selected.
     */
    private final int ITEM_NULL = -1;
    /**
     * The position for the item selected by the user.
     */
    private int mSelectedItem = ITEM_NULL;
    /**
     * The data list for this adapter.
     */
    private BluetoothDevice[] mListDevices;
    /**
     * The listener for all user interaction.
     */
    private final IListAdapterListener mListAdapterListener;
    /**
     * To know the state of the list: enabled or disabled.
     */
    private boolean mEnabled;

    @Override
    public void onClickItem(int position) {
        if (mEnabled) {
            if (mSelectedItem == position) {
                mSelectedItem = ITEM_NULL;
            } else {
                int previousItem = mSelectedItem;
                mSelectedItem = position;
                notifyItemChanged(previousItem);
            }
            notifyItemChanged(position);
            mListAdapterListener.onDeviceItemSelected();
        }
    }

    /**
     * The main constructor of this list to build a new instance for an adapter to a RecyclerView about a
     * BluetoothDevice list.
     *
     * @param listener
     *            The listener to use when the user interacts with the RecyclerView.
     */
    public DevicesListAdapter(IListAdapterListener listener) {
        this.mListAdapterListener = listener;
        this.mListDevices = new BluetoothDevice[0];
        mEnabled = true;
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_devices_item, parent, false);
        return new DeviceViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder holder, int position) {
        // we define the content of this view depending on the data set of this adapter.

        holder.mTextViewDeviceName.setText(mListDevices[position].getName());
        holder.mTextViewDeviceAddress.setText(mListDevices[position].getAddress());

        holder.itemView.setActivated(position == mSelectedItem);
        holder.itemView.setEnabled(mEnabled);
        if (mEnabled) {
            if (position == mSelectedItem) {
                holder.mImageViewTick.setVisibility(View.VISIBLE);
            } else {
                holder.mImageViewTick.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mListDevices.length;
    }

    /**
     * This method allows to define the data for the adapter.
     * 
     * @param listDevices
     *            The list of devices to put on the RecyclerView.
     */
    public void setListDevices(Set<BluetoothDevice> listDevices) {
        this.mListDevices = listDevices.toArray(new BluetoothDevice[listDevices.size()]);
        notifyDataSetChanged();
    }

    /**
     * This method allows to return the item selected by the user. If there is no selected item the method returns
     * "null".
     * 
     * @return the selected item by the user or null.
     */
    public BluetoothDevice getSelectedItem() {
        if (hasSelection())
            return this.mListDevices[mSelectedItem];
        else
            return null;
    }

    /**
     * This method allows to know if the the view has a selected item.
     * 
     * @return true if the view has a selected item and false if none of the items is selected.
     */
    public boolean hasSelection() {
        return mSelectedItem >= 0 && mSelectedItem < mListDevices.length;
    }

    /**
     * To define if views built in this adapter are enabled or disabled.
     *
     * @param enabled
     *              True if the views should be enabled, false otherwise.
     */
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
        notifyDataSetChanged();
    }

    /**
     * This interface allows the adapter to communicate with the element which controls the RecyclerView. Such as a
     * fragment or an activity.
     */
    public interface IListAdapterListener {
        /**
         * This method is called by the adapter when the user selects or deselects an item of the list.
         */
        void onDeviceItemSelected();
    }
}