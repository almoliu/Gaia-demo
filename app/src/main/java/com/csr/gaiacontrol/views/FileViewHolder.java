/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.csr.gaiacontrol.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.csr.gaiacontrol.R;

/**
 * This class allows to define the view for an item on a RecyclerView.
 */
public class FileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    /**
     * The text views to display information from a device.
     */
    public final TextView mTextViewFileName, mTextViewFileLastModification, mTextViewFileSize;
    /**
     * The instance of the parent to interact with it as a listener.
     */
    private final IViewHolder mListener;
    /**
     * The image view when an user selects an item.
     */
    public final CheckBox mCheckBoxSelected;

    /**
     * The constructor of this class to build this view.
     * @param v
     *          The inflated layout for this view.
     * @param listener
     *          The instance of the parent to interact with it as a listener.
     */
    public FileViewHolder(View v, IViewHolder listener) {
        super(v);
        mTextViewFileName = (TextView) v.findViewById(R.id.tv_file_name);
        mTextViewFileLastModification = (TextView) v.findViewById(R.id.tv_file_last_modification);
        mTextViewFileSize = (TextView) v.findViewById(R.id.tv_file_size);
        mCheckBoxSelected = (CheckBox) v.findViewById(R.id.cb_item_selected);
        mListener = listener;
        mCheckBoxSelected.setOnClickListener(this);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        mListener.onClickItem(this.getAdapterPosition());
    }

    /**
     * The interface to allow this class to interact with its parent.
     */
    public interface IViewHolder {
        /**
         * This method is called when the user clicks on the main view of an item.
         *
         * @param position
         *              The position of the item in the list.
         */
        void onClickItem(int position);
    }
}