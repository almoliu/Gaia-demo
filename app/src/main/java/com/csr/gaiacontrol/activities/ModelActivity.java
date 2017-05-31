/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/
package com.csr.gaiacontrol.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;

import com.csr.gaia.library.Gaia;
import com.csr.gaia.library.GaiaLink;
import com.csr.gaiacontrol.implementations.BroadcastReceiverExtended;
import com.csr.gaiacontrol.utils.Consts;

/**
 * <p>This class is the abstract activity to extend for each activity on this application. This class implements all
 * instances needed on each activity and manages their life cycle depending on the activity one.</p>
 * <p>So the instances of the GaiaLink, the BroadcastReceiver for the Bluetooth state and the BluetoothAdapter are available.</p>
 */

@SuppressWarnings("deprecation")
public abstract class ModelActivity extends ActionBarActivity implements
        BroadcastReceiverExtended.BroadcastReceiverListener {

    /**
     * The code to use to the activity results about Bluetooth state.
     */
    private static final int REQUEST_ENABLE_BT = 1;
    /**
     * The Broadcast receiver we used to have information about the Bluetooth state on the device.
     */
    private BroadcastReceiver mBroadcastReceiver;

    /**
     * To know if we are using the application in the debug mode.
     */
    static final boolean DEBUG = Consts.DEBUG;
    /**
     * Instance of the object used to communicate with the GAIA device.
     */
    GaiaLink mGaiaLink;
    /**
     * The instance of the Bluetooth adapter used to retrieve paired Bluetooth devices.
     */
    BluetoothAdapter mBtAdapter;

    /**
     * When the application is informed that the Bluetooth is disabled, this method is called.
     */
    public void onBluetoothDisabled() {
        checkEnableBt();
    }

    // When the activity is created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.init();
    }

    // Callback activated after the user responds to the enable Bluetooth dialogue.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_ENABLE_BT: {
            if (resultCode == RESULT_OK) {
                onBluetoothEnabled();
            }
            break;
        }
        default:
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // When the activity is resumed.
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(mBroadcastReceiver, filter);

        // the object to communicate with GAIA devices
        mGaiaLink = GaiaLink.getInstance();
        mGaiaLink.setReceiveHandler(this.getGaiaHandler());
    }

    // When the activity is paused.
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    /**
     * To initialise objects used in this activity.
     */
    private void init() {
        // Bluetooth adapter
        mBtAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

        // Register for broadcasts on BluetoothAdapter state change so that we can tell if it has been turned off.
        mBroadcastReceiver = new BroadcastReceiverExtended(this);
    }

    /**
     * Display a dialog requesting Bluetooth to be enabled if it isn't already. Otherwise this method update the list to
     * the list view. The list view needs to be ready when this method is called.
     */
    private void checkEnableBt() {
        if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            onBluetoothEnabled();
        }
    }

    /**
     * To send a packet to the device.
     *
     * @param command
     *            the command to send to the device.
     * @param payload
     *            the additional information for the command.
     */
    void sendGaiaPacket(int command, int... payload) {
        mGaiaLink.sendCommand(Gaia.VENDOR_CSR, command, payload);
    }

    /**
     * To send a packet to the device.
     *
     * @param command
     *            the command to send to the device.
     * @param payload
     *            the additional information for the command.
     */
    @SuppressWarnings("SameParameterValue")
    void sendGaiaPacket(int command, byte... payload) {
        mGaiaLink.sendCommand(Gaia.VENDOR_CSR, command, payload);
    }

    /**
     * To send a packet to the device.
     *
     * @param command
     *            the command to send to the device.
     * @param payload
     *            the additional information for the command.
     */
    void sendGaiaPacket(int command, boolean payload) {
        mGaiaLink.sendCommand(Gaia.VENDOR_CSR, command, payload);
    }

    /**
     * To register for a notification on a GAIA Bluetooth device.
     *
     * @param eventID
     *          The event for which we want to be notified.
     */
    void registerNotification(Gaia.EventId eventID) {
        mGaiaLink.registerNotification(Gaia.VENDOR_CSR, eventID);
    }

    /**
     * To cancel a notification on a GAIA Bluetooth device.
     *
     * @param eventID
     *          The event for which we want to cancel the notification.
     */
    void cancelNotification(Gaia.EventId eventID) {
        mGaiaLink.cancelNotification(Gaia.VENDOR_CSR, eventID);
    }

    /**
     * To handle the message providing by the device using GAIA communication.
     */
    protected abstract Handler getGaiaHandler ();

    @Override
    public void onBluetoothEnabled() {
    }
}
