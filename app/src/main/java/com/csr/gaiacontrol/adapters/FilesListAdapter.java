/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/
package com.csr.gaiacontrol.adapters;

import java.io.File;
import java.util.Date;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.csr.gaiacontrol.R;
import com.csr.gaiacontrol.utils.Consts;
import com.csr.gaiacontrol.views.FileViewHolder;

/**
 * This adapter allows to display a File list in a RecyclerView.
 */
public class FilesListAdapter extends RecyclerView.Adapter<FileViewHolder> implements FileViewHolder.IViewHolder {

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
    private File[] mFilesList;
    /**
     * The listener for all user interactions.
     */
    private final IFilesListAdapterListener mListener;

    @Override
    public void onClickItem(int position) {
            if (mSelectedItem == position) {
                mSelectedItem = ITEM_NULL;
            } else {
                int previousItem = mSelectedItem;
                mSelectedItem = position;
                notifyItemChanged(previousItem);
            }
            notifyItemChanged(position);
            mListener.onDeviceItemSelected(hasSelection());
    }

    /**
     * The main constructor of this list to build a new instance for an adapter to a RecyclerView about a
     * BluetoothDevice list.
     *
     * @param listener
     *            The listener to use when the user interacts with the RecyclerView.
     */
    public FilesListAdapter(IFilesListAdapterListener listener) {
        this.mListener = listener;
        this.mFilesList = new File[0];
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_files_item, parent, false);
        return new FileViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
        // we define the content of this view depending on the data set of this adapter.
        holder.mTextViewFileName.setText(mFilesList[position].getName());
        String date = DateFormat.format(Consts.DATE_FORMAT, new Date(mFilesList[position].lastModified())).toString();
        holder.mTextViewFileLastModification.setText(date);
        long size = mFilesList[position].length() / 1024;
        String sizeText = size + Consts.UNIT_FILE_SIZE;
        holder.mTextViewFileSize.setText(sizeText);

        holder.itemView.setActivated(position == mSelectedItem);
        holder.mCheckBoxSelected.setChecked(position == mSelectedItem);
    }

    @Override
    public int getItemCount() {
        return mFilesList.length;
    }

    /**
     * This method allows to define the data for this adapter.
     *
     * @param filesList
     *            The list of files to display on the RecyclerView.
     */
    public void setFilesList(File[] filesList) {
        if (filesList != null) {
            this.mFilesList = filesList;
        }
        else {
            this.mFilesList = new File[0];
        }
        notifyDataSetChanged();
    }

    /**
     * This method allows to return the item selected by the user. If there is no selection the method returns null.
     *
     * @return the selected item by the user or null.
     */
    public File getSelectedItem() {
        if (hasSelection())
            return this.mFilesList[mSelectedItem];
        else
            return null;
    }

    /**
     * This method allows to know if this view has a selected item.
     *
     * @return true if the view has a selected item and false if none of the items is selected.
     */
    public boolean hasSelection() {
        return mSelectedItem >= 0 && mSelectedItem < mFilesList.length;
    }

    /**
     * This interface allows the adapter to communicate with the element which controls the RecyclerView. Such as a
     * fragment or an activity.
     */
    public interface IFilesListAdapterListener {
        /**
         * This method is called by the adapter when the user selects or deselects an item of the list.
         *
         * @param itemSelected
         *                  true if an item is selected, false otherwise.
         */
        void onDeviceItemSelected(boolean itemSelected);
    }
}