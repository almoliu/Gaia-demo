/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/
package com.csr.gaiacontrol.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.csr.gaia.library.Gaia;
import com.csr.gaia.library.GaiaError;
import com.csr.gaia.library.GaiaLink;
import com.csr.gaia.library.GaiaPacket;
import com.csr.gaiacontrol.R;
import com.csr.gaiacontrol.utils.Consts;
import com.csr.gaiacontrol.utils.Utils;

import java.lang.ref.WeakReference;

/**
 * <p>This activity is the main activity for this application. It navigates between all other activities depending on
 * the user choice about the feature he wants to use. Also this activity starts the ConnectionActivity if the Gaia Link
 * is not connected.</p>
 */

public class MainActivity extends ModelActivity implements View.OnClickListener {

    /**
     * For the debug mode, the tag to display for logs.
     */
    private static final String TAG = "MainActivity";
    /**
     * The handler to run some tasks.
     */
    private static final Handler mHandler = new Handler();
    /**
     * The time to wait before to check the state for information as battery level or RSSI signal.
     */
    private static final int TIME_TO_CHECK = 5000;
    /**
     * To have access to the instance which controls the led.
     */
    private Button mButtonLed;
    /**
     * To know if the led is activated.
     */
    private boolean ledActivated = false;
    /**
     * To have access to the instance which displays the battery level.
     */
    private ImageView mImageViewBatteryLevel;
    /**
     * To have access to the instance which displays the device name.
     */
    private TextView mTextViewDeviceName;
    /**
     * To have access to the instance which displays the signal level.
     */
    private ImageView mImageViewSignalLevel;
    /**
     * To have access to the instance which displays the version number.
     */
    private TextView mTextViewVersionNumber;
    /**
     * To know if the device is charging.
     */
    private boolean isCharging = false;
    /**
     * To know the current battery level.
     */
    private int mBatteryLevel = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_change_device:
            startConnectionActivity();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.bt_led:
            onClickLedButton();
            break;
        case R.id.bt_equalizer:
            Intent intentEqualizer = new Intent(this, EqualizerActivity.class);
            startActivity(intentEqualizer);
            break;
        case R.id.bt_update:
            Intent intentUpdate = new Intent(this, UpdateActivity.class);
            startActivity(intentUpdate);
            break;
        case R.id.bt_device_information:
            Intent intentDevice = new Intent(this, InformationActivity.class);
            startActivity(intentDevice);
            break;
        case R.id.bt_tws:
            Intent intentTWS = new Intent(this, TWSActivity.class);
            startActivity(intentTWS);
            break;
        case R.id.bt_remote:
            Intent intentRemote = new Intent(this, RemoteActivity.class);
            startActivity(intentRemote);
            break;

        default:
            makeToast(R.string.toast_not_implemented, Toast.LENGTH_SHORT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mGaiaLink.isConnected()) {
            startConnectionActivity();
        }
        else {
            getInformation();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        // removing all active runnable
        mHandler.removeCallbacks(mRunnableBattery);
        mHandler.removeCallbacks(mRunnableRSSI);
        if (mGaiaLink.isConnected()) {
            cancelNotification(Gaia.EventId.CHARGER_CONNECTION);
        }
    }

    /**
     * To initialise objects used in this activity.
     */
    private void init() {
        this.setSupportActionBar((Toolbar) findViewById(R.id.tb_menu));
        //noinspection ConstantConditions
        this.getSupportActionBar().setLogo(R.drawable.ic_launcher_no_shape);

        mButtonLed = (Button) findViewById(R.id.bt_led);
        mButtonLed.setOnClickListener(this);

        mImageViewBatteryLevel = (ImageView) findViewById(R.id.iv_battery);
        mImageViewSignalLevel = (ImageView) findViewById(R.id.iv_signal);
        mTextViewDeviceName = (TextView) findViewById(R.id.tv_device_name);
        mTextViewVersionNumber = (TextView) findViewById(R.id.tv_device_info);

        findViewById(R.id.bt_equalizer).setOnClickListener(this);
        findViewById(R.id.bt_device_information).setOnClickListener(this);
        findViewById(R.id.bt_tws).setOnClickListener(this);
        findViewById(R.id.bt_update).setOnClickListener(this);
        findViewById(R.id.bt_remote).setOnClickListener(this);
    }

    @Override
    protected Handler getGaiaHandler() {
        return new GaiaHandler(this);
    }



    /**
     * To start the activity where the user can choose the device he wants to use.
     */
    private void startConnectionActivity() {
        Intent intent = new Intent(this, ConnectionActivity.class);
        startActivity(intent);
    }

    /**
     * To display the button led as activated or deactivated.
     * 
     * @param activate
     *            true to display the device led as activated. Should fit with should the led state.
     */
    private void activateLed(boolean activate) {
        if (activate) {
            mButtonLed.setBackgroundResource(R.drawable.tile_led_on);
            mButtonLed.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_led_on, 0, 0);
        }
        else {
            mButtonLed.setBackgroundResource(R.drawable.tile_led_off);
            mButtonLed.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_led_off_white,
                0, 0);
        }
    }

    /**
     * To manage packets from Gaia device which are "PACKET" directly by the library.
     * 
     * @param msg
     *            The message coming from the handler which calls this method.
     */
    private void handlePacket(Message msg) {
        GaiaPacket packet = (GaiaPacket) msg.obj;
        Gaia.Status status = packet.getStatus();

        switch (packet.getCommand()) {
        case Gaia.COMMAND_GET_LED_CONTROL:
            if (DEBUG)
                Log.i(TAG, "Received \"COMMAND_GET_LED_CONTROL\" packet with a " + status + " status.");
            if (checkStatus(packet))
                receivePacketGetLedControl(packet);
            break;

        case Gaia.COMMAND_GET_CURRENT_BATTERY_LEVEL:
            if (DEBUG)
                Log.i(TAG, "Received \"COMMAND_GET_CURRENT_BATTERY_LEVEL\" packet with a " + status + " status.");
            if (checkStatus(packet))
                receivePacketGetCurrentBatteryLevel(packet);
            break;

        case Gaia.COMMAND_GET_CURRENT_RSSI:
            if (DEBUG)
                Log.i(TAG, "Received \"COMMAND_GET_CURRENT_RSSI\" packet with a " + status + " status.");
            if (checkStatus(packet))
                receivePacketGetCurrentRSSI(packet);
            break;

        case Gaia.COMMAND_GET_API_VERSION:
            if (DEBUG)
                Log.i(TAG, "Received \"COMMAND_GET_API_VERSION\" packet with a " + status + " status.");
            if (checkStatus(packet))
                receivePacketGetAPIVersion(packet);
            break;

        case Gaia.COMMAND_EVENT_NOTIFICATION:
            if (DEBUG)
                Log.i(TAG, "Received \"Notification\" packet.");
            handleNotification(packet);
            break;

        case Gaia.COMMAND_GET_USER_EQ_CONTROL:
            // to know if the EQ feature is available
            if (DEBUG)
                Log.i(TAG, "Received \"COMMAND_GET_USER_EQ_CONTROL\" packet with a " + status + " status.");
            if (!status.equals(Gaia.Status.NOT_SUPPORTED))
                findViewById(R.id.bt_equalizer).setVisibility(View.VISIBLE);
            break;

        case Gaia.COMMAND_GET_3D_ENHANCEMENT_CONTROL:
            // to know if the EQ feature is available
            if (DEBUG)
                Log.i(TAG, "Received \"COMMAND_GET_3D_ENHANCEMENT_CONTROL\" packet with a " + status + " status.");
            if (!status.equals(Gaia.Status.NOT_SUPPORTED))
                findViewById(R.id.bt_equalizer).setVisibility(View.VISIBLE);
            break;

        case Gaia.COMMAND_GET_BASS_BOOST_CONTROL:
            // to know if the EQ feature is available
            if (DEBUG)
                Log.i(TAG, "Received \"COMMAND_GET_BASS_BOOST_CONTROL\" packet with a " + status + " status.");
            if (!status.equals(Gaia.Status.NOT_SUPPORTED))
                findViewById(R.id.bt_equalizer).setVisibility(View.VISIBLE);
            break;

        case Gaia.COMMAND_GET_TWS_AUDIO_ROUTING:
            // to know if the TWS feature is available
            if (DEBUG)
                Log.i(TAG, "Received \"COMMAND_GET_TWS_AUDIO_ROUTING\" packet with a " + status + " status.");
            if (!status.equals(Gaia.Status.NOT_SUPPORTED))
                findViewById(R.id.bt_tws).setVisibility(View.VISIBLE);
            break;

        case Gaia.COMMAND_GET_TWS_VOLUME:
            // to know if the TWS feature is available
            if (DEBUG)
                Log.i(TAG, "Received \"COMMAND_GET_TWS_VOLUME\" packet with a " + status + " status.");
            if (!status.equals(Gaia.Status.NOT_SUPPORTED))
                findViewById(R.id.bt_tws).setVisibility(View.VISIBLE);
            break;

        case Gaia.COMMAND_VM_UPGRADE_CONNECT:
            // to know if the TWS feature is available
            if (DEBUG)
                Log.i(TAG, "Received \"COMMAND_VM_UPGRADE_CONNECT\" packet with a " + status + " status.");
            if (!status.equals(Gaia.Status.NOT_SUPPORTED)) {
                findViewById(R.id.bt_update).setVisibility(View.VISIBLE);
            }
            sendGaiaPacket(Gaia.COMMAND_VM_UPGRADE_DISCONNECT);
            break;

            case Gaia.COMMAND_AV_REMOTE_CONTROL:
                // To know if the remote control feature is available
                if (DEBUG)
                    Log.i(TAG, "Received \"COMMAND_AV_REMOTE_CONTROL\" packet with a " + status + " status.");
                if (!status.equals(Gaia.Status.NOT_SUPPORTED))
                    findViewById(R.id.bt_remote).setVisibility(View.VISIBLE);
                break;

        default:
            if (DEBUG)
                Log.d(TAG, "Received packet - command: " + Utils.getIntToHexadecimal(packet.getCommandId())
                        + " - payload: " + Utils.getStringFromBytes(packet.getPayload()));
        }

    }

    /**
     * Called when we receive a packet about the command COMMAND_GET_LED_CONTROL to manage the application depending on
     * information from the packet.
     *
     * @param packet
     *            The packet about the received command COMMAND_GET_LED_CONTROL.
     */
    private void receivePacketGetLedControl(GaiaPacket packet) {
        mButtonLed.setVisibility(View.VISIBLE);
        mButtonLed.setEnabled(true);
        ledActivated = packet.getBoolean();
        activateLed(ledActivated);
    }

    /**
     * Called when we receive a packet about the command COMMAND_GET_CURRENT_BATTERY_LEVEL to manage the application
     * depending on information from the packet.
     *
     * @param packet
     *            The packet about the received command COMMAND_GET_CURRENT_BATTERY_LEVEL.
     */
    private void receivePacketGetCurrentBatteryLevel(GaiaPacket packet) {
        mImageViewBatteryLevel.setVisibility(View.VISIBLE);
        mBatteryLevel = Utils.extractIntField(packet.getPayload(), 1, 2, false);
        // we display the received value
        updateDisplayBattery();
        // we need to retrieve this information constantly
        mHandler.postDelayed(mRunnableBattery, TIME_TO_CHECK);
    }

    /**
     * Called when we receive a packet about the command COMMAND_GET_CURRENT_RSSI to manage the application depending on
     * information from the packet.
     *
     * @param packet
     *            The packet about the received command COMMAND_GET_CURRENT_RSSI.
     */
    private void receivePacketGetCurrentRSSI(GaiaPacket packet) {
        mImageViewSignalLevel.setVisibility(View.VISIBLE);
        int level = packet.getByte(1);
        // we display the received value
        showSignal(level);
        // we need to retrieve this information constantly
        mHandler.postDelayed(mRunnableRSSI, TIME_TO_CHECK);
    }

    /**
     * Called when we receive a packet about the command COMMAND_GET_CURRENT_RSSI to manage the application depending on
     * information from the packet.
     *
     * @param packet
     *            The packet about the received command COMMAND_GET_CURRENT_RSSI.
     */
    private void receivePacketGetAPIVersion(GaiaPacket packet) {
        String APIText = "API version " + packet.getByte(1) + "." + packet.getByte(2) + "."
                + packet.getByte(3);
        mTextViewVersionNumber.setVisibility(View.VISIBLE);
        mTextViewVersionNumber.setText(APIText);
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
     * When the user clicks on the led button this method is called.
     */
    private void onClickLedButton() {
        ledActivated = !ledActivated;
        sendGaiaPacket(Gaia.COMMAND_SET_LED_CONTROL, ledActivated ? 1 : 0);
        activateLed(ledActivated);
    }

    /**
     * To handle notifications coming from the Gaia device.
     */
    private void handleNotification(GaiaPacket packet) {
        Gaia.EventId event = packet.getEvent();
        switch (event) {
        case CHARGER_CONNECTION:
            isCharging = packet.getPayload()[1] == 0x01;
            updateDisplayBattery();
            break;

        default:
            if (DEBUG)
                Log.i(TAG, "Received event: " + event);
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
        if (packet.getStatus() == Gaia.Status.SUCCESS) {
            return true;
        }
        else {
            if (DEBUG)
                Log.w(TAG, "Status " + packet.getStatus().toString() + " with the command " + packet.getCommand());
            switch (packet.getStatus()) {
            case NOT_SUPPORTED:
                receivePacketCommandNotSupported(packet);
                break;
            }
            return false;
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
        case Gaia.COMMAND_GET_LED_CONTROL:
        case Gaia.COMMAND_SET_LED_CONTROL:
            mButtonLed.setVisibility(View.GONE);
            break;

        case Gaia.COMMAND_GET_CURRENT_BATTERY_LEVEL:
            mImageViewBatteryLevel.setVisibility(View.GONE);
            break;

        case Gaia.COMMAND_GET_CURRENT_RSSI:
            mImageViewSignalLevel.setVisibility(View.GONE);
            break;

        case Gaia.COMMAND_GET_API_VERSION:
            mTextViewVersionNumber.setVisibility(View.GONE);
            break;

        case Gaia.COMMAND_EVENT_NOTIFICATION:
            break;
        }
    }

    /**
     * To display default information for all items which are displaying some information about the device and request
     * these different information.
     */
    private void getInformation() {
        // Display default information
        String deviceName = mGaiaLink.getBluetoothDevice().getName();
        mTextViewDeviceName.setText(deviceName);
        showSignal(1);
        updateDisplayBattery();
        mTextViewVersionNumber.setText("v0.1");

        // get information from device

      /*askForLedState();
        askForBatteryLevel();
        askForAPIVersion();
        askForRSSILevel();
        askForEQService();
        askForTWSService();
        askForUpdateService();
        askForRemoteService();*/

        registerNotification(Gaia.EventId.CHARGER_CONNECTION);
    }

    /**
     * To request the LED state from the device.
     */
    private void askForLedState() {
        mButtonLed.setVisibility(View.GONE);
        sendGaiaPacket(Gaia.COMMAND_GET_LED_CONTROL);
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
     * To request the EQ service from the device. We ask for all services the EQ can provide and if at least once is
     * available - means will return an acknowledgment packet - the EQ tile will be available.
     */
    private void askForEQService() {
        findViewById(R.id.bt_equalizer).setVisibility(View.GONE);
        sendGaiaPacket(Gaia.COMMAND_GET_3D_ENHANCEMENT_CONTROL);
        sendGaiaPacket(Gaia.COMMAND_GET_BASS_BOOST_CONTROL);
        sendGaiaPacket(Gaia.COMMAND_GET_USER_EQ_CONTROL);
    }

    /**
     * To request the TWS service from the device. We ask for all services the TWS can provide and if at least once is
     * available - means will return an acknowledgment packet - the TWS tile will be available.
     */
    private void askForTWSService() {
        findViewById(R.id.bt_tws).setVisibility(View.GONE);
        sendGaiaPacket(Gaia.COMMAND_GET_TWS_AUDIO_ROUTING);
        sendGaiaPacket(Gaia.COMMAND_GET_TWS_VOLUME);
    }

    /**
     * To request the Update service from the device. We ask for all services the EQ can provide and if at least once is
     * available - means will return an acknowledgment packet - the Update tile will be available.
     */
    private void askForUpdateService() {
        findViewById(R.id.bt_update).setVisibility(View.GONE);
        sendGaiaPacket(Gaia.COMMAND_VM_UPGRADE_CONNECT);
    }

    /**
     * To request the Remote service from the device.
     */
    private void askForRemoteService() {
        findViewById(R.id.bt_remote).setVisibility(View.GONE);
        sendGaiaPacket(Gaia.COMMAND_AV_REMOTE_CONTROL);
    }

    /**
     * To display the corresponding image depending on the value for the RSSI level.
     *
     * @param rssi
     *            the corresponding value to display.
     */
    private void showSignal(int rssi) {
        // The RSSI is a negative number, Close to zero, the signal is strong, far and away the signal is low.
        // We consider between -60 and 0 the signal stays strength. Then the strength level decreases by 10 until -90.
        if (-60 <= rssi && rssi <= 0) {
            mImageViewSignalLevel.setImageResource(R.drawable.ic_signal_4);
        }
        else if (-70 <= rssi && rssi < -60) {
            mImageViewSignalLevel.setImageResource(R.drawable.ic_signal_3);
        }
        else if (-80 <= rssi && rssi < -70) {
            mImageViewSignalLevel.setImageResource(R.drawable.ic_signal_2);
        }
        else if (-90 <= rssi && rssi < -80) {
            mImageViewSignalLevel.setImageResource(R.drawable.ic_signal_1);
        }
        else if (rssi < -90) {
            mImageViewSignalLevel.setImageResource(R.drawable.ic_signal_0);
        }
        else {
            mImageViewSignalLevel.setImageResource(R.drawable.ic_signal_unknown);
        }
    }

    /**
     * To display the battery level as an image depending on the level value.
     */
    private void updateDisplayBattery() {
        // The battery level to display depends on a percentage, we calculate the percentage.
        int value = mBatteryLevel * 100 / Consts.BATTERY_LEVEL_MAX;

        // depending on the percentage for the battery level and if the battery is charging we display the corresponding
        // feature.
        // We pick the number depending on images we have.
        if (isCharging && value >= 95) {
            mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_charging_full);
        }
        else if (value >= 95) {
            mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_full);
        }
        else if (isCharging && value >= 85) {
            mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_charging_90);
        }
        else if (value >= 85) {
            mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_90);
        }
        else if (isCharging && value >= 70) {
            mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_charging_80);
        }
        else if (value >= 70) {
            mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_80);
        }
        else if (isCharging && value >= 55) {
            mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_charging_60);
        }
        else if (value >= 55) {
            mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_60);
        }
        else if (isCharging && value >= 40) {
            mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_charging_50);
        }
        else if (value >= 40) {
            mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_50);
        }
        else if (isCharging && value >= 25) {
            mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_charging_30);
        }
        else if (value >= 25) {
            mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_30);
        }
        else if (isCharging && value >= 10) {
            mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_charging_20);
        }
        else if (value >= 10) {
            mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_20);
        }
        else if (isCharging && value > 2) {
            mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_charging_05);
        }
        else if (value > 2) {
            mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_05);
        }
        else if (isCharging && value >= 0) {
            mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_charging_00);
        }
        else if (value >= 0) {
            mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_00);
        }
        else {
            mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_unknown);
        }
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
     * The class which allows to manage messages from Gaia devices.
     */
    private static class GaiaHandler extends Handler {

        /**
         * The reference to this activity.
         */
        final WeakReference<MainActivity> mActivity;

        /**
         * The constructor for this activity.
         *
         * @param activity
         *            this activity.
         */
        public GaiaHandler(MainActivity activity) {
            super();
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity parentActivity = mActivity.get();
            String handleMessage = "Handle a message from Gaia: ";
            GaiaLink.Message message = GaiaLink.Message.valueOf(msg.what);
            if (message == null) {
                if (DEBUG)
                    Log.d(TAG, handleMessage + "NULL");
                return;
            }
            switch (message) {
            case PACKET:
                parentActivity.handlePacket(msg);
                break;

            case CONNECTED:
                if (DEBUG)
                    Log.d(TAG, handleMessage + "CONNECTED");
                break;

            case DISCONNECTED:
                if (DEBUG)
                    Log.d(TAG, handleMessage + "DISCONNECTED");
                parentActivity.makeToast(R.string.toast_disconnected, Toast.LENGTH_SHORT);
                parentActivity.startConnectionActivity();
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
