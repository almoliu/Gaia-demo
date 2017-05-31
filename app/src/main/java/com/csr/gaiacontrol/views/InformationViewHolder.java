/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.csr.gaiacontrol.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.csr.gaiacontrol.R;

/**
 * This class allows to define the view for an item on a RecyclerView.
 */
public class InformationViewHolder extends RecyclerView.ViewHolder {
    /**
     * The text views to display information from a device.
     */
    public final TextView mTVInformationName, mTVInformationValue;

    /**
     * The constructor of this class to build this view.
     * @param v
     *          The inflated layout for this view.
     */
    public InformationViewHolder(View v) {
        super(v);
        mTVInformationName = (TextView) v.findViewById(R.id.tv_information_name);
        mTVInformationValue = (TextView) v.findViewById(R.id.tv_information_value);
    }
}