/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/
package com.csr.gaiacontrol.implementations;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This class allows to receive information from the system. We use it to have information about the Bluetooth state.
 */

public class BroadcastReceiverExtended extends BroadcastReceiver {

    /**
     * To have an access to the main activity from which this receiver is initiated.
     */
    private final BroadcastReceiverListener mListener;

    /**
     * The constructor of this class.
     *
     * @param listener
     *            the main activity from which the receiver is initiated.
     */
    public BroadcastReceiverExtended(BroadcastReceiverListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            if (state == BluetoothAdapter.STATE_OFF) {
                mListener.onBluetoothDisabled();
            }
            else if (state == BluetoothAdapter.STATE_ON) {
                mListener.onBluetoothEnabled();
            }
        }
    }

    /**
     * The interface used to communicate with this object.
     */
    public interface BroadcastReceiverListener {
        /**
         * When the application is informed that the Bluetooth is disabled, this method is called.
         */
        void onBluetoothDisabled();
        /**
         * When the application is informed that the Bluetooth is enabled, this method is called.
         */
        void onBluetoothEnabled();
    }

}
