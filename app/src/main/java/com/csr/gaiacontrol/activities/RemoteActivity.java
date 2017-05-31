/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/
package com.csr.gaiacontrol.activities;

import java.lang.ref.WeakReference;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.csr.gaia.library.Gaia;
import com.csr.gaia.library.GaiaError;
import com.csr.gaia.library.GaiaLink;
import com.csr.gaia.library.GaiaPacket;
import com.csr.gaiacontrol.R;
import com.csr.gaiacontrol.utils.Utils;

/**
 * <p>This activity is the activity to control the audio sound for the audio device connected to the application.</p>
 */

public class RemoteActivity extends ModelActivity implements View.OnClickListener {

    /**
     * For the debug mode, the tag to display for logs.
     */
    private static final String TAG = "RemoteActivity";

    /**
     * The AV remote control operation for volume up.
     */
    private static final int AV_VOLUME_UP = 0x41;
    /**
     * The AV remote control operation for volume down.
     */
    private static final int AV_VOLUME_DOWN = 0x42;
    /**
     * The AV remote control operation for mute.
     */
    private static final int AV_MUTE = 0x43;
    /**
     * The AV remote control operation for play.
     */
    private static final int AV_PLAY = 0x44;
    /**
     * The AV remote control operation for stop.
     */
    private static final int AV_STOP = 0x45;
    /**
     * The AV remote control operation for pause.
     */
    private static final int AV_PAUSE = 0x46;
    /**
     * The AV remote control operation for reward.
     */
    private static final int AV_REWIND = 0x4C;
    /**
     * The AV remote control operation for forward.
     */
    private static final int AV_FORWARD = 0x4B;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.bt_volume_up:
            sendGaiaPacketRemoteControl(AV_VOLUME_UP);
            break;

        case R.id.bt_volume_down:
            sendGaiaPacketRemoteControl(AV_VOLUME_DOWN);
            break;

        case R.id.bt_mute:
            sendGaiaPacketRemoteControl(AV_MUTE);
            break;

        case R.id.bt_pause:
            sendGaiaPacketRemoteControl(AV_PAUSE);
            break;

        case R.id.bt_play:
            sendGaiaPacketRemoteControl(AV_PLAY);
            break;

        case R.id.bt_forward:
            sendGaiaPacketRemoteControl(AV_FORWARD);
            break;

        case R.id.bt_rewind:
            sendGaiaPacketRemoteControl(AV_REWIND);
            break;

        case R.id.bt_stop:
            sendGaiaPacketRemoteControl(AV_STOP);
            break;
        }

    }

    @Override
    protected Handler getGaiaHandler() {
        return new GaiaHandler(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
        this.init();
    }

    /**
     * To initialise objects used in this activity.
     */
    private void init() {
        // manage the action bar
        this.setSupportActionBar((Toolbar) findViewById(R.id.tb_menu));
        //noinspection ConstantConditions
        this.getSupportActionBar().setLogo(R.drawable.ic_remote_small);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // adding listener for each button
        findViewById(R.id.bt_volume_down).setOnClickListener(this);
        findViewById(R.id.bt_volume_up).setOnClickListener(this);
        findViewById(R.id.bt_mute).setOnClickListener(this);
        findViewById(R.id.bt_pause).setOnClickListener(this);
        findViewById(R.id.bt_forward).setOnClickListener(this);
        findViewById(R.id.bt_rewind).setOnClickListener(this);
        findViewById(R.id.bt_stop).setOnClickListener(this);
        findViewById(R.id.bt_play).setOnClickListener(this);
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
        case Gaia.COMMAND_AV_REMOTE_CONTROL:
            validate = checkStatus(packet);
            if (DEBUG)
                Log.i(TAG, "Received \"COMMAND_AV_REMOTE_CONTROL\" packet with a " + validate + " status.");
            break;

        default:
            if (DEBUG)
                Log.d(TAG, "Received packet - command: " + Utils.getIntToHexadecimal(packet.getCommandId())
                        + " - payload: " + Utils.getStringFromBytes(packet.getPayload()));
        }
    }

    /**
     * When we received a packet about a command which is not supported by the device.
     */
    private void receivePacketCommandNotSupported() {
        this.finish();
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
        Gaia.Status status = packet.getStatus();
        switch (status) {
        case SUCCESS:
            return true;
        case NOT_SUPPORTED:
            receivePacketCommandNotSupported();
            break;
        case INCORRECT_STATE:
        case AUTHENTICATING:
        case INSUFFICIENT_RESOURCES:
        case INVALID_PARAMETER:
        case NOT_AUTHENTICATED:
        default:
            if (DEBUG)
                Log.w(TAG, "Status " + status + " with the command " + packet.getCommand());
        }
        return false;
    }

    /**
     * To send a COMMAND_AV_REMOTE_CONTROL packet to the device.
     *
     * @param parameter
     *            the parameter for the command.
     */
    private void sendGaiaPacketRemoteControl(int parameter) {
        sendGaiaPacket(Gaia.COMMAND_AV_REMOTE_CONTROL, parameter);
    }

    /**
     * The class which allows to manage messages from Gaia devices.
     */
    private static class GaiaHandler extends Handler {

        /**
         * The reference to this activity.
         */
        final WeakReference<RemoteActivity> mActivity;

        /**
         * The constructor for this activity.
         *
         * @param activity
         *            this activity.
         */
        public GaiaHandler(RemoteActivity activity) {
            super();
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            RemoteActivity parentActivity = mActivity.get();
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
