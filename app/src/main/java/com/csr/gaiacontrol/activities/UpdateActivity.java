/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/
package com.csr.gaiacontrol.activities;

import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.csr.gaia.library.Gaia;
import com.csr.gaia.library.GaiaError;
import com.csr.gaia.library.GaiaLink;
import com.csr.gaia.library.GaiaPacket;
import com.csr.gaiacontrol.R;
import com.csr.gaiacontrol.fragments.UpdateVMFragment;
import com.csr.gaiacontrol.implementations.ConnectionRunnable;
import com.csr.gaiacontrol.utils.Utils;

/**
 * <p>This activity is the activity to control the equalizer for a device connected to the application.</p>
 */

public class UpdateActivity extends ModelActivity implements UpdateVMFragment.IUpdateVMListener,
        ConnectionRunnable.IConnectionListener {

    /**
     * For the debug mode, the tag to display for logs.
     */
    private static final String TAG = "UpdateActivity";

    /**
     * The fragment to display the update for ADK4.0 to the user.
     */
    private UpdateVMFragment mVMFragment;
    /**
     * The handler to run some tasks.
     */
    private static final Handler mHandler = new Handler();
    /**
     * The runnable used for a timeout when we try to connect to the device.
     */
    private static ConnectionRunnable mConnectionRunnable;
    /**
     * A dialog to display information to the user when we attempt to connect to the device.
     */
    private AlertDialog mAttemptConnectionDialog;
    /**
     * A dialog to display a question to the user when we can't connect to the device.
     */
    private AlertDialog mFailedConnectionDialog;
    /**
     * To know if the disconnection is coming from this application or is coming from the board.
     */
    private boolean isDisconnectionFromApp = false;
    /**
     * The device in which we are connected.
     */
    private BluetoothDevice mDevice;


    @Override
    public void sendPacket(int commandId, int... param) {
        mGaiaLink.sendCommand(Gaia.VENDOR_CSR, commandId, param);
    }

    @Override
    public void sendPacket(int commandId, byte[] payload) {
        mGaiaLink.sendCommand(Gaia.VENDOR_CSR, commandId, payload);
    }

    @Override
    public void disconnectDevice() {
        isDisconnectionFromApp = true;
        mGaiaLink.disconnect();
    }

    @Override
    public void registerForNotifications(Gaia.EventId event) {
        registerNotification(event);
    }

    @Override
    public void unregisterForNotifications(Gaia.EventId event) {
        if (mGaiaLink.isConnected()) {
            cancelNotification(event);
        }
    }

    @Override
    public void connect() {
        if (!mGaiaLink.isConnected()) {
            mGaiaLink.connect(mDevice, mGaiaLink.getTransport());
        }
    }

    @Override
    public void connectFailed() {
        mAttemptConnectionDialog.cancel();
        mFailedConnectionDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        this.init();
    }

    @Override
    protected Handler getGaiaHandler() {
        return new GaiaHandler(this);
    }

    // When the activity is resumed.
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (!mVMFragment.isVisible()) {
            FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, mVMFragment);
            fragmentTransaction.commit();
        }
    }

    /**
     * To initialise objects used in this activity.
     */
    private void init() {
        this.setSupportActionBar((Toolbar) findViewById(R.id.tb_menu));
        //noinspection ConstantConditions
        this.getSupportActionBar().setLogo(R.drawable.ic_update_small);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.buildDialogs();
        mVMFragment = UpdateVMFragment.newInstance();

        mConnectionRunnable = new ConnectionRunnable(this);
    }

    /**
     * To initialise alert dialogs to display with this activity.
     */
    private void buildDialogs () {
        // build the dialog to show a progress bar when we try to reconnect.
        AlertDialog.Builder attemptDialogBuilder = new AlertDialog.Builder(UpdateActivity.this);
        attemptDialogBuilder.setTitle(getString(R.string.alert_attempt_connection_title));

        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") View dialogLayout = inflater.inflate(R.layout.dialog_progress_bar, null);
        attemptDialogBuilder.setView(dialogLayout);

        attemptDialogBuilder.setCancelable(false);
        mAttemptConnectionDialog = attemptDialogBuilder.create();

        // build a dialog to ask to the user to try a reconnection.
        AlertDialog.Builder noConnectionDialogBuilder = new AlertDialog.Builder(UpdateActivity.this);
        noConnectionDialogBuilder.setTitle(getString(R.string.alert_connection_failed_title));
        noConnectionDialogBuilder.setMessage(getString(R.string.alert_connection_failed_text));
        // set positive button: "try again" message
        noConnectionDialogBuilder.setPositiveButton(getString(R.string.alert_connection_failed_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        attemptReconnection();
                    }
                });
        // set cancel button
        noConnectionDialogBuilder.setNegativeButton(getString(R.string.alert_connection_failed_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        noConnectionDialogBuilder.setCancelable(false);
        mFailedConnectionDialog = noConnectionDialogBuilder.create();
    }

    /**
     * To manage errors information catching inside the handler and coming from the library.
     *
     * @param error
     *            The error coming from the library formatting as a <code>GaiaError<</code>.
     */
    private void handleError(GaiaError error) {
        switch (error.getType()) {
            case CONNECTION_FAILED:
                if (DEBUG) Log.w(TAG, "Received error: " + error.getStringException());
                mHandler.postDelayed(mConnectionRunnable, ConnectionRunnable.CONNECTION_TIMEOUT);
                break;
            case ILLEGAL_ARGUMENT:
            case SENDING_FAILED:
            case RECEIVING_FAILED:
            default:
                if (DEBUG) Log.w(TAG, "Received error: " + error.getStringException());
        }
    }

    /**
     * To manage packets from Gaia device which are "PACKET" directly by the library.
     *
     * @param msg
     *            The message coming from the handler which calls this method.
     */
    @SuppressWarnings("ConstantConditions")
    private void handlePacket(Message msg) {
        GaiaPacket packet = (GaiaPacket) msg.obj;
        boolean validate;

        switch (packet.getCommand()) {
            case Gaia.COMMAND_VM_UPGRADE_CONNECT:
                validate = checkStatus(packet);
                if (DEBUG) Log.i(TAG, "Received \"VM connection\" packet with a " + validate + " status.");
                if (validate) {
                    mVMFragment.onUpdateActivated();
                }
                else {
                    mVMFragment.onUpdateActivatedFailed();
                }
                break;
            case Gaia.COMMAND_VM_UPGRADE_DISCONNECT:
                validate = checkStatus(packet);
                if (DEBUG) Log.i(TAG, "Received \"VM disconnection\" packet with a " + validate + " status.");
                mVMFragment.onVMDisconnected();
                break;
            case Gaia.COMMAND_VM_UPGRADE_CONTROL:
                validate = checkStatus(packet);
                if (DEBUG) Log.i(TAG, "Received \"VM Control\" packet with a " + validate + " status.");
                if (validate) {
                    mVMFragment.onVMControlSucceed();
                }
                else {
                    mVMFragment.onVMControlFailed();
                }
                break;
            case Gaia.COMMAND_EVENT_NOTIFICATION:
                if (DEBUG) Log.i(TAG, "Received \"Notification\" packet.");
                handleEvent(packet);
                break;
        default:
            //noinspection PointlessBooleanExpression
            if (DEBUG && packet.isAcknowledgement())
                Log.i(TAG, "ACK - command: " + packet.getCommand() + " - status: " + packet.getStatus());
            checkStatus(packet);
            break;

        }

    }

    /**
     * To handle events coming from Gaia device.
     */
    private void handleEvent(GaiaPacket packet) {
        Gaia.EventId event = packet.getEvent();
        switch (event) {
            case VMU_PACKET:
                mVMFragment.handlerVMEvent(packet);
                break;

            default:
                if (DEBUG) Log.i(TAG, "Received event: " + event);
        }
    }

    /**
     * To check the status of an acknowledgement packet.
     *
     * @param packet
     *            the packet to check.
     *
     * @return true if the status is SUCCESS and the packet is an acknowledgment, false otherwise.
     */
    private boolean checkStatus(GaiaPacket packet) {
        if (!packet.isAcknowledgement()) {
            return false;
        }
        switch (packet.getStatus()) {
        case SUCCESS:
            return true;
        case NOT_SUPPORTED:
        case AUTHENTICATING:
        case INCORRECT_STATE:
        case INSUFFICIENT_RESOURCES:
        case INVALID_PARAMETER:
        case NOT_AUTHENTICATED:
        default:
            if (DEBUG)
                Log.i(TAG, "Status " + packet.getStatus().toString() + " with the command " + Utils.getIntToHexadecimal(packet.getCommand()));
        }
        return false;
    }

    /**
     * The method to attempt to reconnect to the device.
     */
    private void attemptReconnection() {
        mHandler.postDelayed(mConnectionRunnable, ConnectionRunnable.CONNECTION_TIMEOUT);
        mAttemptConnectionDialog.show();
    }

    /**
     * This method is called when we detect that the device is disconnected.
     */
    private void onDeviceDisconnected() {
        if (isDisconnectionFromApp) {
            isDisconnectionFromApp = false;
            finish();
        }
        else {
            mDevice = mGaiaLink.getBluetoothDevice();
            if (mVMFragment.isUpdating()) {
                mVMFragment.onDeviceDisconnected();
            }
            attemptReconnection();
        }
    }

    /**
     * This method is called when we are connected to a device. This allows to act depending on the application actual state.
     */
    private void onDeviceConnected() {
        mHandler.removeCallbacks(mConnectionRunnable);
        mConnectionRunnable.restart();

        mAttemptConnectionDialog.cancel();

        if (mVMFragment.isUpdating()) {
            mVMFragment.onDeviceConnected();
        }
    }


    /**
     * The class which allows to manage messages from Gaia devices.
     */
    private static class GaiaHandler extends Handler {

        /**
         * The reference to this activity.
         */
        final WeakReference<UpdateActivity> mActivity;

        /**
         * The constructor for this activity.
         *
         * @param activity
         *            this activity.
         */
        public GaiaHandler(UpdateActivity activity) {
            super();
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            GaiaLink.Message packetType = GaiaLink.Message.valueOf(msg.what);
            if (packetType == null) {
                return;
            }

            UpdateActivity parentActivity = mActivity.get();
            String handleMessage = "Handle a message from Gaia: ";

            switch (packetType) {
            case PACKET:
                parentActivity.handlePacket(msg);
                break;

            case CONNECTED:
                if (DEBUG)
                    Log.i(TAG, handleMessage + "CONNECTED");
                parentActivity.onDeviceConnected();
                break;

            case DISCONNECTED:
                if (DEBUG)
                    Log.i(TAG, handleMessage + "DISCONNECTED");
                parentActivity.onDeviceDisconnected();
                break;

            case ERROR:
                if (DEBUG)
                    Log.i(TAG, handleMessage + "ERROR");
                GaiaError error = (GaiaError) msg.obj;
                parentActivity.handleError(error);
                break;

            default:
                if (DEBUG)
                    Log.d(TAG, handleMessage + "UNKNOWN MESSAGE: " + msg);
                break;
            }
        }
    }
}
