/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.csr.gaiacontrol.activities;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.csr.gaia.library.GaiaError;
import com.csr.gaia.library.GaiaLink;
import com.csr.gaiacontrol.R;
import com.csr.gaiacontrol.fragments.ConnectionFragment;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Set;

/**
 * <p>This class is the connection activity for this application. It allows to display all devices in which the
 * application can connect.</p> <p>This activity extends ModelActivity.</p> <p>This activity implements the
 * IConnectionFragmentListener to allow the ConnectionFragment to communicate with this activity.</p>
 */

public class ConnectionActivity extends ModelActivity implements ConnectionFragment.IConnectionFragmentListener {

    /**
     * For the debug mode, the tag to display for logs.
     */
    private static final String TAG = "ConnectionActivity";

    /**
     * The fragment which controls the display where the user can select a device to control.
     */
    private ConnectionFragment mConnectionFragment;
    /**
     * The device to attempt to connect using GaiaLink.
     */
    private BluetoothDevice mDevice;
    /**
     * The profile to use to connect to a device.
     */
    private GaiaLink.Transport mTransport;
    /**
     * To know if the user is waiting for a connection.
     */
    private boolean mWaitingForConnection = false;
    /**
     * To manage exception when the app is already connected to a device: to stop when we tried more than a certain
     * number of attempts.
     */
    private int nbAttemptConnection = 0;
    /**
     * To know if the attempted connection is using SPP as the transport.
     */
    private boolean isAttemptingSPP = false;
    /**
     * To know if the attempted connection is using GAIA UUID as the transport.
     */
    private boolean iSAttemptingGAIAUUID = false;
    /**
     * The maximum number of attempts when a device is already connected.
     */
    private static final int NB_ATTEMPTS_CONNECTION_MAX = 2;

    /**
     * inherited from ModeActivity
     */
    public void onBluetoothEnabled() {
        updateListDevices();
    }

    /**
     * called from children fragment, inherited from children fragment's interface
     */
    public void start() {
        updateListDevices();
    }

    /**
     * called from children fragment to connect the selected device
     * @param device the target device to connect
     */
    @Override
    public void connect(BluetoothDevice device) {

        if (!isAttemptingSPP && !iSAttemptingGAIAUUID) {
            isAttemptingSPP = true;
            mTransport = GaiaLink.Transport.BT_SPP;
        }
        else if (isAttemptingSPP) {
            isAttemptingSPP = false;
            iSAttemptingGAIAUUID = true;
            mTransport = GaiaLink.Transport.BT_GAIA;
        }
        displayWaitingProcess(true);
        mDevice = device;

        if (mGaiaLink.isConnected()) {
            mWaitingForConnection = true;
            disconnectDevice();
        }
        else {
            mWaitingForConnection = false;
            connectDevice();
        }
    }

    // When the activity is created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        this.init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"on Resume() called...");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart() called...");
    }

    // When the activity is resumed.
    @Override
    protected void onResumeFragments() {
        Log.d(TAG,"onResumeFragments() called...");
        super.onResumeFragments();
        if (!mConnectionFragment.isVisible()) {
            FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, mConnectionFragment);
            fragmentTransaction.commit();
        }
    }

    /**
     * To initialise objects used in this activity.
     */
    private void init() {
        // fragments
        mConnectionFragment = ConnectionFragment.newInstance();
    }

    @Override
    protected Handler getGaiaHandler() {
        return new GaiaHandler(this);
    }

    /**
     * To update the list of the paired Bluetooth devices list.
     */
    private void updateListDevices() {
        if (mBtAdapter != null && mBtAdapter.isEnabled()) {
            mConnectionFragment.setListDevices(mBtAdapter.getBondedDevices());
        }
        else {
            Set<BluetoothDevice> emptySetDevices = Collections.emptySet();
            mConnectionFragment.setListDevices(emptySetDevices);
        }
    }

    /**
     * When the gaia connection is connected this method is called to start the next activity.
     */
    private void onConnect() {
        isAttemptingSPP = false;
        iSAttemptingGAIAUUID = false;
        this.finish();
    }

    /**
     * To start the connection process.
     */
    private void connectDevice() {
        displayWaitingProcess(true);
        mGaiaLink.connect(mDevice, mTransport);
    }

    /**
     * To start the disconnection process.
     */
    private void disconnectDevice() {
        mGaiaLink.disconnect();
    }

    /**
     * To manage errors information catching inside the handler and coming from the library.
     *
     * @param error
     *            The error coming from the library, formatting as a <code>GaiaError<</code>.
     */
    private void handleError(GaiaError error) {
        if (isAttemptingSPP) {
            connect(mDevice);
        }
        else {
            isAttemptingSPP = false;
            iSAttemptingGAIAUUID = false;

            switch (error.getType()) {
                case ILLEGAL_ARGUMENT:
                case DEVICE_UNKNOWN_ADDRESS:
                    makeToast(R.string.toast_try_another_device, Toast.LENGTH_LONG);
                    break;
                case UNSUPPORTED_TRANSPORT:
                case CONNECTION_FAILED:
                    makeToast(R.string.toast_connection_failed, Toast.LENGTH_LONG);
                    break;
                case ALREADY_CONNECTED:
                    if (nbAttemptConnection < NB_ATTEMPTS_CONNECTION_MAX) {
                        nbAttemptConnection++;
                        this.disconnectDevice();
                    } else {
                        nbAttemptConnection = 0;
                        makeToast(R.string.toast_connection_failed, Toast.LENGTH_LONG);
                    }
                    break;
                case BLUETOOTH_NOT_SUPPORTED:
                    // This case has already been tested in this activity by extending of ModelActivity.
                    break;
            }
        }
    }

    /**
     * To display a toast with the given text and the given length.
     *
     * @param idText
     *            The text id to display.
     * @param length
     *            The time the message should appear.
     */
    private void makeToast(int idText, @SuppressWarnings("SameParameterValue") int length) {
        Toast.makeText(this, idText, length).show();
    }

    /**
     * To show or hide the view to inform the user we are waiting an answer from a process before to allow him to act.
     *
     * @param waiting
     *              true to show the view, false to hide it.
     */
    private void displayWaitingProcess (boolean waiting) {
        mConnectionFragment.displayProgressBar(waiting);
    }

    /**
     * The class which allows to manage messages from Gaia devices.
     */
    private static class GaiaHandler extends Handler {

        /**
         * The reference to this activity.
         */
        final WeakReference<ConnectionActivity> mActivity;

        /**
         * The constructor for this activity.
         *
         * @param activity
         *              this activity.
         */
        public GaiaHandler(ConnectionActivity activity) {
            super();
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ConnectionActivity parentActivity = mActivity.get();
            parentActivity.displayWaitingProcess(false);
            String handleMessage = "Handle a message from Gaia: ";
            GaiaLink.Message message = GaiaLink.Message.valueOf(msg.what);
            if (message == null) {
                if (DEBUG)
                    Log.d(TAG, handleMessage + "NULL");
                return;
            }
            switch (message) {
                case CONNECTED:
                    if (DEBUG)
                        Log.d(TAG, handleMessage + "CONNECTED");
                    parentActivity.onConnect();
                    break;

                case DISCONNECTED:
                    if (DEBUG)
                        Log.d(TAG, handleMessage + "DISCONNECTED");
                    if (parentActivity.mWaitingForConnection) {
                        parentActivity.isAttemptingSPP = false;
                        parentActivity.iSAttemptingGAIAUUID = false;
                        parentActivity.connectDevice();
                        parentActivity.mWaitingForConnection = false;
                    }
                    break;

                case ERROR:
                    if (DEBUG)
                        Log.d(TAG, handleMessage + "ERROR");
                    GaiaError error = (GaiaError) msg.obj;
                    parentActivity.handleError(error);
                    break;
            }
        }
    }
}
