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
import android.widget.Toast;

import com.csr.gaia.library.Gaia;
import com.csr.gaia.library.GaiaError;
import com.csr.gaia.library.GaiaLink;
import com.csr.gaia.library.GaiaPacket;
import com.csr.gaiacontrol.R;
import com.csr.gaiacontrol.fragments.SpeakerFragment;
import com.csr.gaiacontrol.utils.Utils;

public class TWSActivity extends ModelActivity implements SpeakerFragment.ISpeakerFragmentListener {

    /**
     * For the debug mode, the tag to display for logs.
     */
    private static final String TAG = "TWSActivity";
    /**
     * The fragment to define the channel and the volume for the master speaker.
     */
    private SpeakerFragment mMasterSpeakerFragment;
    /**
     * The fragment to define the channel and the volume for the slave speaker.
     */
    private SpeakerFragment mSlaveSpeakerFragment;
    /**
     * The value to send to the speaker when a message concerns the master speaker.
     */
    private static final int MASTER_SPEAKER = 0x00;
    /**
     * The value to send to the speaker when a message concerns the slave speaker.
     */
    private static final int SLAVE_SPEAKER = 0x01;
    /**
     * The maximum volume value for a speaker.
     */
    private static final int MAX_VOLUME = 127;

    @Override
    public void sendVolume(int speaker, int volume) {
        sendGaiaPacket(Gaia.COMMAND_SET_TWS_VOLUME, speaker, volume * MAX_VOLUME / 100);

    }

    @Override
    public void sendChannel(int speaker, int channel) {
        sendGaiaPacket(Gaia.COMMAND_SET_TWS_AUDIO_ROUTING, speaker, channel);
        switch (speaker) {
            case MASTER_SPEAKER:
                askForSlaveChannel();
                break;
            case SLAVE_SPEAKER:
                askForMasterChannel();
        }
    }

    @Override
    protected Handler getGaiaHandler() {
        return new GaiaHandler(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        askForMasterVolume();
        askForMasterChannel();
        askForSlaveVolume();
        askForSlaveChannel();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tws);
        this.init();
    }

    /**
     * To request the states for the master speaker about its volume.
     */
    private void askForMasterVolume() {
        sendGaiaPacket(Gaia.COMMAND_GET_TWS_VOLUME, MASTER_SPEAKER);
    }

    /**
     * To request the states for the master speaker about its channel.
     */
    private void askForMasterChannel() {
        sendGaiaPacket(Gaia.COMMAND_GET_TWS_AUDIO_ROUTING, MASTER_SPEAKER);
    }

    /**
     * To request the states for the slave speaker about its volume.
     */
    private void askForSlaveVolume() {
        sendGaiaPacket(Gaia.COMMAND_GET_TWS_VOLUME, SLAVE_SPEAKER);
    }

    /**
     * To request the states for the slave speaker about its channel.
     */
    private void askForSlaveChannel() {
        sendGaiaPacket(Gaia.COMMAND_GET_TWS_AUDIO_ROUTING, SLAVE_SPEAKER);
    }

    /**
     * To initialise objects used in this activity.
     */
    private void init() {
        // manage the action bar
        this.setSupportActionBar((Toolbar) findViewById(R.id.tb_menu));
        //noinspection ConstantConditions
        this.getSupportActionBar().setLogo(R.drawable.ic_speaker_small);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMasterSpeakerFragment = (SpeakerFragment) getSupportFragmentManager().findFragmentById(R.id.f_master_speaker);
        mMasterSpeakerFragment.setSpeakerValue(MASTER_SPEAKER);
        mSlaveSpeakerFragment = (SpeakerFragment) getSupportFragmentManager().findFragmentById(R.id.f_slave_speaker);
        mSlaveSpeakerFragment.setSpeakerValue(SLAVE_SPEAKER);
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
        case Gaia.COMMAND_GET_TWS_AUDIO_ROUTING:
            validate = checkStatus(packet);
            if (DEBUG)
                Log.i(TAG, "Received \"COMMAND_GET_TWS_AUDIO_ROUTING\" packet with a " + validate + " status.");
            if (validate)
                receiveGetChannel(packet);
            break;

        case Gaia.COMMAND_GET_TWS_VOLUME:
            validate = checkStatus(packet);
            if (DEBUG)
                Log.i(TAG, "Received \"COMMAND_GET_TWS_AUDIO_ROUTING\" packet with a " + validate + " status.");
            if (validate)
                receiveGetVolume(packet);
            break;

            case Gaia.COMMAND_SET_TWS_AUDIO_ROUTING:
                validate = checkStatus(packet);
                if (DEBUG)
                    Log.i(TAG, "Received \"COMMAND_SET_TWS_AUDIO_ROUTING\" packet with a " + validate + " status.");
                break;

            case Gaia.COMMAND_SET_TWS_VOLUME:
                validate = checkStatus(packet);
                if (DEBUG)
                    Log.i(TAG, "Received \"COMMAND_SET_TWS_VOLUME\" packet with a " + validate + " status.");
                break;

        default:
            if (DEBUG)
                Log.d(TAG, "Received packet - command: " + Utils.getIntToHexadecimal(packet.getCommandId())
                        + " - payload: " + Utils.getStringFromBytes(packet.getPayload()));
        }
    }

    /**
     * When we receive a successful packet for the GET AUDIO ROUTING command.
     */
    private void receiveGetChannel(GaiaPacket packet) {
        int speaker = packet.getByte(1);
        int channel = packet.getByte(2);

        switch (speaker) {
        case MASTER_SPEAKER:
            mMasterSpeakerFragment.setChannel(channel);
            break;
        case SLAVE_SPEAKER:
            mSlaveSpeakerFragment.setChannel(channel);
            break;
        }
    }

    /**
     * When we receive a successful packet for the GET VOLUME command.
     */
    private void receiveGetVolume(GaiaPacket packet) {
        int speaker = packet.getByte(1);
        int volume = packet.getByte(2);

        switch (speaker) {
        case MASTER_SPEAKER:
            mMasterSpeakerFragment.setVolume(volume);
            break;
        case SLAVE_SPEAKER:
            mSlaveSpeakerFragment.setVolume(volume);
            break;
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
            case INCORRECT_STATE:
            case AUTHENTICATING:
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
     * The class which allows to manage messages from Gaia devices.
     */
    private static class GaiaHandler extends Handler {

        /**
         * The reference to this activity.
         */
        final WeakReference<TWSActivity> mActivity;

        /**
         * The constructor for this activity.
         *
         * @param activity
         *            this activity.
         */
        public GaiaHandler(TWSActivity activity) {
            super();
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            TWSActivity parentActivity = mActivity.get();
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
