/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/
package com.csr.gaiacontrol.activities;

import java.lang.ref.WeakReference;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.csr.gaia.library.Gaia;
import com.csr.gaia.library.GaiaError;
import com.csr.gaia.library.GaiaLink;
import com.csr.gaia.library.GaiaPacket;
import com.csr.gaiacontrol.R;
import com.csr.gaiacontrol.adapters.InformationListAdapter;
import com.csr.gaiacontrol.utils.Utils;
import com.csr.gaiacontrol.views.DividerItemDecoration;

public class InformationActivity extends ModelActivity implements InformationListAdapter.IListAdapterListener {

    /**
     * For the debug mode, the tag to display for logs.
     */
    private static final String TAG = "DeviceActivity";
    /**
     * The handler to run some tasks.
     */
    private static final Handler mHandler = new Handler();
    /**
     * The time to wait before to check the state for information as battery level or RSSI signal.
     */
    private static final int WAITING_TIME = 5000;
    /**
     * The adapter for the information to display as items in the recycler view.
     */
    private InformationListAdapter mListAdapter;

    @Override
    public String getInformationName(int position) {
        Information information = Information.getInformationFromInt(position);
        if (information == null) {
            return getResources().getString(R.string.info_no_title);
        }
        else {
            switch (information) {
                case NAME:
                    return getResources().getString(R.string.info_name);
                case BLUETOOTH_ADDRESS:
                    return getResources().getString(R.string.info_bluetooth_address);
                case SIGNAL_LEVEL:
                    return getResources().getString(R.string.info_rssi_signal);
                case BATTERY_LEVEL:
                    return getResources().getString(R.string.info_battery_level);
                case BATTERY_STATUS:
                    return getResources().getString(R.string.info_battery_status);
                case API_VERSION:
                    return getResources().getString(R.string.info_api_version);
                default:
                    return getResources().getString(R.string.info_no_title);
            }
        }
    }

    @Override
    protected Handler getGaiaHandler() {
        return new GaiaHandler(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BluetoothDevice device = mGaiaLink.getBluetoothDevice();
        mListAdapter.setValue(Information.NAME.ordinal(), device.getName());
        mListAdapter.setValue(Information.BLUETOOTH_ADDRESS.ordinal(), device.getAddress());

        askForBatteryLevel();
        askForAPIVersion();
        askForRSSILevel();
        registerNotification(Gaia.EventId.CHARGER_CONNECTION);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // removing all active runnable & notifications
        mHandler.removeCallbacks(mRunnableBattery);
        mHandler.removeCallbacks(mRunnableRSSI);
        if (mGaiaLink.isConnected()) {
            cancelNotification(Gaia.EventId.CHARGER_CONNECTION);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        this.init();
    }

    /**
     * To initialise objects used in this activity.
     */
    private void init() {
        // manage the action bar
        this.setSupportActionBar((Toolbar) findViewById(R.id.tb_menu));
        //noinspection ConstantConditions
        this.getSupportActionBar().setLogo(R.drawable.ic_info_small);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_information_list);
        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        // add a divider to the list
        recyclerView.addItemDecoration(new DividerItemDecoration(this));

        // specify an adapter for the recycler view
        mListAdapter = new InformationListAdapter(this);
        recyclerView.setAdapter(mListAdapter);
    }

    /**
     * To request the battery level from the device.
     */
    private void askForBatteryLevel() {
        sendGaiaPacket(Gaia.COMMAND_GET_CURRENT_BATTERY_LEVEL);
    }

    /**
     * To request the API version from the device.
     */
    private void askForAPIVersion() {
        sendGaiaPacket(Gaia.COMMAND_GET_API_VERSION);
    }

    /**
     * To request the RSSI level from the device.
     */
    private void askForRSSILevel() {
        sendGaiaPacket(Gaia.COMMAND_GET_CURRENT_RSSI);
    }

    /**
     * To manage packets from Gaia device which are "PACKET" directly by the library.
     *
     * @param packet
     *            The message coming from the handler which calls this method.
     */
    private void handlePacket(GaiaPacket packet) {
        boolean validate;
        switch (packet.getCommand()) {
        case Gaia.COMMAND_GET_CURRENT_BATTERY_LEVEL:
            validate = checkStatus(packet);
            if (DEBUG)
                Log.i(TAG, "Received \"COMMAND_GET_CURRENT_BATTERY_LEVEL\" packet with a " + validate + " status.");
            receiveGetCurrentBatteryLevel(packet);
            break;

        case Gaia.COMMAND_GET_CURRENT_RSSI:
            validate = checkStatus(packet);
            if (DEBUG)
                Log.i(TAG, "Received \"COMMAND_GET_CURRENT_RSSI\" packet with a " + validate + " status.");
            receiveGetCurrentRSSI(packet);
            break;

        case Gaia.COMMAND_GET_API_VERSION:
            validate = checkStatus(packet);
            if (DEBUG)
                Log.i(TAG, "Received \"COMMAND_GET_API_VERSION\" packet with a " + validate + " status.");
            receiveGetAPIVersion(packet);
            break;

        case Gaia.COMMAND_EVENT_NOTIFICATION:
            if (DEBUG)
                Log.i(TAG, "Received \"Notification\" packet.");
            handleNotification(packet);
            break;

        default:
            if (DEBUG)
                Log.d(TAG, "Received packet - command: " + Utils.getIntToHexadecimal(packet.getCommandId())
                        + " - payload: " + Utils.getStringFromBytes(packet.getPayload()));
        }
    }

    /**
     * When we received a packet about a command which is not supported by the device.
     *
     * @param packet
     *            the concerned packet.
     */
    private void receivePacketCommandNotSupported(GaiaPacket packet) {
        switch (packet.getCommand()) {
            case Gaia.COMMAND_GET_CURRENT_BATTERY_LEVEL:
                if (DEBUG)
                    Log.w(TAG, "Received \"COMMAND_GET_CURRENT_BATTERY_LEVEL\" not supported.");
                mListAdapter.setValue(Information.BATTERY_LEVEL.ordinal(), getString(R.string.info_not_supported));
                break;

            case Gaia.COMMAND_GET_CURRENT_RSSI:
                if (DEBUG)
                    Log.w(TAG, "Received \"COMMAND_GET_CURRENT_RSSI\" not supported.");
                mListAdapter.setValue(Information.SIGNAL_LEVEL.ordinal(), getString(R.string.info_not_supported));
                break;

            case Gaia.COMMAND_GET_API_VERSION:
                if (DEBUG)
                    Log.w(TAG, "Received \"COMMAND_GET_API_VERSION\" not supported.");
                mListAdapter.setValue(Information.API_VERSION.ordinal(), getString(R.string.info_not_supported));
                break;

            case Gaia.COMMAND_EVENT_NOTIFICATION:
                if (DEBUG)
                    Log.w(TAG, "Received \"COMMAND_EVENT_NOTIFICATION\" not supported.");
                mListAdapter.setValue(Information.BATTERY_STATUS.ordinal(), getString(R.string.info_not_supported));
                break;
        }
    }

    /**
     * Called when we receive a packet about the command COMMAND_GET_CURRENT_BATTERY_LEVEL to manage the application
     * depending on information from the packet.
     *
     * @param packet
     *            The packet about the received command COMMAND_GET_CURRENT_BATTERY_LEVEL.
     */
    private void receiveGetCurrentBatteryLevel(GaiaPacket packet) {
        if (checkStatus(packet)) {
            int level = Utils.extractIntField(packet.getPayload(), 1, 2, false);
            // we display the received value
            mListAdapter.setValue(Information.BATTERY_LEVEL.ordinal(), level + " mV");
            // we need to retrieve this information constantly
            mHandler.postDelayed(mRunnableBattery, WAITING_TIME);
        }
    }

    /**
     * Called when we receive a packet about the command COMMAND_GET_CURRENT_RSSI to manage the application depending on
     * information from the packet.
     *
     * @param packet
     *            The packet about the received command COMMAND_GET_CURRENT_RSSI.
     */
    private void receiveGetCurrentRSSI(GaiaPacket packet) {
        if (checkStatus(packet)) {
            int level = packet.getByte(1);
            // we display the received value
            mListAdapter.setValue(Information.SIGNAL_LEVEL.ordinal(), level + " dBm");
            // we need to retrieve this information constantly
            mHandler.postDelayed(mRunnableRSSI, WAITING_TIME);
        }
    }

    /**
     * Called when we receive a packet about the command COMMAND_GET_CURRENT_RSSI to manage the application depending on
     * information from the packet.
     *
     * @param packet
     *            The packet about the received command COMMAND_GET_CURRENT_RSSI.
     */
    private void receiveGetAPIVersion(GaiaPacket packet) {
        if (checkStatus(packet)) {
            mListAdapter.setValue(Information.API_VERSION.ordinal(), packet.getByte(1) + "." + packet.getByte(2) + "."
                    + packet.getByte(3));
        }
    }

    /**
     * To handle notifications coming from the Gaia device.
     */
    private void handleNotification(GaiaPacket packet) {
        Gaia.EventId event = packet.getEvent();
        switch (event) {
        case CHARGER_CONNECTION:
            String text;
            if (packet.getPayload()[1] == 0x01) {
                text = getString(R.string.info_battery_status_in_charge);
            }
            else {
                text = getString(R.string.info_battery_status_no_charge);
            }
            mListAdapter.setValue(Information.BATTERY_STATUS.ordinal(), text);
            break;

        default:
            if (DEBUG)
                Log.i(TAG, "Received event: " + event);
        }
    }

    /**
     * To manage errors information catching inside the handler and coming from the library.
     *
     * @param error
     *            The error coming from the library formatting as a <code>GaiaError<</code>.
     */
    private void handleError(GaiaError error) {
        switch (error.getType()) {
        case SENDING_FAILED:
            String message;
            if (error.getCommand() > 0) {
                message = "Send command " + error.getCommand() + " failed";

            }
            else {
                message = "Send command failed";
            }
            if (DEBUG)
                Log.w(TAG, message + ": " + error.getStringException());
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            break;
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
            receivePacketCommandNotSupported(packet);
            break;
        case AUTHENTICATING:
        case INCORRECT_STATE:
        case INSUFFICIENT_RESOURCES:
        case INVALID_PARAMETER:
        case NOT_AUTHENTICATED:
        default:
            if (DEBUG)
                Log.w(TAG, "Status " + packet.getStatus().toString() + " with the command " + packet.getCommand());
        }
        return false;
    }

    /**
     * To start a task to get the battery level from the device.
     */
    private final Runnable mRunnableBattery = new Runnable() {
        @Override
        public void run() {
            askForBatteryLevel();
        }
    };

    /**
     * To start a task to get the RSSI value from the device.
     */
    private final Runnable mRunnableRSSI = new Runnable() {
        @Override
        public void run() {
            askForRSSILevel();
        }
    };

    /**
     * All information we want to display about the connected device.
     */
    public enum Information {
        NAME, BLUETOOTH_ADDRESS, SIGNAL_LEVEL, BATTERY_LEVEL, BATTERY_STATUS, API_VERSION;

        /**
         * To keep constantly this array without calling the values() method which is copying an array when it's called.
         */
        private static final Information[] values = Information.values();

        /**
         * To get the information matching the corresponding int value in this enumeration.
         *
         * @param i
         *            the int value from which we want the matching Information.
         *
         * @return the matching Information
         */
        public static Information getInformationFromInt(int i) {
            if (i < 0 || i >= values.length) {
                return null;
            }

            return Information.values[i];
        }

    }

    /**
     * The class which allows to manage messages from Gaia devices.
     */
    private static class GaiaHandler extends Handler {

        /**
         * The reference to this activity.
         */
        final WeakReference<InformationActivity> mActivity;

        /**
         * The constructor for this activity.
         *
         * @param activity
         *            this activity.
         */
        public GaiaHandler(InformationActivity activity) {
            super();
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            InformationActivity parentActivity = mActivity.get();
            String handleMessage = "Handle a message from Gaia: ";
            GaiaLink.Message message = GaiaLink.Message.valueOf(msg.what);
            if (message == null) {
                if (DEBUG)
                    Log.d(TAG, handleMessage + "NULL");
                return;
            }
            switch (message) {
            case PACKET:
                GaiaPacket packet = (GaiaPacket) msg.obj;
                parentActivity.handlePacket(packet);
                break;

            case DISCONNECTED:
                if (DEBUG)
                    Log.d(TAG, handleMessage + "DISCONNECTED");
                Toast.makeText(parentActivity, R.string.toast_disconnected, Toast.LENGTH_SHORT).show();
                parentActivity.finish();
                break;

            case ERROR:
                if (DEBUG)
                    Log.d(TAG, handleMessage + "ERROR");
                GaiaError error = (GaiaError) msg.obj;
                parentActivity.handleError(error);
                break;

            case STREAM:
                if (DEBUG)
                    Log.d(TAG, handleMessage + "STREAM");
                break;

            default:
                if (DEBUG)
                    Log.d(TAG, handleMessage + "UNKNOWN MESSAGE: " + msg);
                break;
            }
        }
    }
}
