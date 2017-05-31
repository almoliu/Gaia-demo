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
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.csr.gaia.library.Gaia;
import com.csr.gaia.library.GaiaError;
import com.csr.gaia.library.GaiaLink;
import com.csr.gaia.library.GaiaPacket;
import com.csr.gaiacontrol.R;
import com.csr.gaiacontrol.views.PresetButton;

import java.lang.ref.WeakReference;

/**
 * <p>This activity is the activity to control the equalizer for a device connected to the application.</p>
 */

public class EqualizerActivity extends ModelActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    /**
     * For the debug mode, the tag to display for logs.
     */
    private static final String TAG = "EqualizerActivity";
    /**
     * The total number of presets.
     */
    private static final int NUMBER_PRESETS = 7; // The 7th is the custom equalizer feature

    /**
     * The switch to customize the equalizer.
     */
    private Switch mSwitchPresets;
    /**
     * The switch to enable or disable the 3D.
     */
    private Switch mSwitch3D;
    /**
     * The switch to enable or disable the bass boost.
     */
    private Switch mSwitchBass;
    /**
     * All preset buttons.
     */
    private final PresetButton[] mPresets = new PresetButton[NUMBER_PRESETS];
    /**
     * The button to start the configuration of Bank 1.
     */
    private Button mButtonConfigure;

    /**
     * The button selected by the user or by default.
     */
    private int mSelectedPreset = -1;

    @Override
    public void onClick(View v) {
        int selectedPreset = ((PresetButton) v).getPreset();
        selectPreset(selectedPreset);
        sendGaiaPacket(Gaia.COMMAND_SET_EQ_CONTROL, selectedPreset);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sw_presets:
                sendGaiaPacket(Gaia.COMMAND_SET_USER_EQ_CONTROL, isChecked);
                if (isChecked) {
                    sendGaiaPacket(Gaia.COMMAND_GET_EQ_CONTROL);
                    /*almo test*/
                    activatePresets(true);
                }
                else {
                    activatePresets(false);
                }
                break;
            case R.id.sw_bass:
                sendGaiaPacket(Gaia.COMMAND_SET_BASS_BOOST_CONTROL, isChecked);
                break;
            case R.id.sw_3d:
                sendGaiaPacket(Gaia.COMMAND_SET_3D_ENHANCEMENT_CONTROL, isChecked);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGaiaLink.isConnected()) {
            // disable all actions while we don't know the device state.
            mSwitchPresets.setEnabled(true);//false
            mSwitch3D.setEnabled(false);
            mSwitchPresets.setEnabled(true);//false
            activatePresets(true);//false

            // we ask its state to the device.
            sendGaiaPacket(Gaia.COMMAND_GET_3D_ENHANCEMENT_CONTROL);
            sendGaiaPacket(Gaia.COMMAND_GET_BASS_BOOST_CONTROL);
            sendGaiaPacket(Gaia.COMMAND_GET_USER_EQ_CONTROL);
        }
        else {
            this.finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);
        this.init();
    }

    /**
     * To initialise objects used in this activity.
     */
    private void init() {

        this.setSupportActionBar((Toolbar) findViewById(R.id.tb_menu));
        //noinspection ConstantConditions
        this.getSupportActionBar().setLogo(R.drawable.ic_equalizer_small);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        PresetButton buttonClassic = (PresetButton) findViewById(R.id.bt_preset_6);
        buttonClassic.setOnClickListener(this);
        mPresets[buttonClassic.getPreset()] = buttonClassic;

        PresetButton buttonPop = (PresetButton) findViewById(R.id.bt_preset_5);
        buttonPop.setOnClickListener(this);
        mPresets[buttonPop.getPreset()] = buttonPop;

        PresetButton buttonRock = (PresetButton) findViewById(R.id.bt_preset_2);
        buttonRock.setOnClickListener(this);
        mPresets[buttonRock.getPreset()] = buttonRock;

        PresetButton buttonJazz = (PresetButton) findViewById(R.id.bt_preset_3);
        buttonJazz.setOnClickListener(this);
        mPresets[buttonJazz.getPreset()] = buttonJazz;

        PresetButton buttonFolk = (PresetButton) findViewById(R.id.bt_preset_4);
        buttonFolk.setOnClickListener(this);
        mPresets[buttonFolk.getPreset()] = buttonFolk;

        PresetButton buttonDefault = (PresetButton) findViewById(R.id.bt_preset_0);
        buttonDefault.setOnClickListener(this);
        mPresets[buttonDefault.getPreset()] = buttonDefault;

        PresetButton buttonCustom = (PresetButton) findViewById(R.id.bt_preset_1);
        buttonCustom.setOnClickListener(this);
        mPresets[PresetButton.PRESET_CUSTOM] = buttonCustom;

        mSwitchPresets = (Switch) findViewById(R.id.sw_presets);
        mSwitchPresets.setOnCheckedChangeListener(this);
        mSwitch3D = (Switch) findViewById(R.id.sw_3d);
        mSwitch3D.setOnCheckedChangeListener(this);
        mSwitchBass = (Switch) findViewById(R.id.sw_bass);
        mSwitchBass.setOnCheckedChangeListener(this);

        mButtonConfigure = (Button) findViewById(R.id.bt_configure_bank_1);
        mButtonConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCustomization();
            }
        });
    }

    @Override
    protected Handler getGaiaHandler() {
        return new GaiaHandler(this);
    }

    /**
     * To select a preset for the UI.
     *
     * @param selected
     *              The preset to show as selected.
     */
    private void selectPreset(int selected) {
        if (mSelectedPreset >= 0 && mSelectedPreset < NUMBER_PRESETS) {
            mPresets[mSelectedPreset].selectButton(false);
        }
        mPresets[selected].selectButton(true);
        mSelectedPreset = selected;
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
     * To hide or display the presets view depending on act on the customization switch.
     */
    private void activatePresets (boolean activate) {
        int visibility = activate ? View.VISIBLE : View.GONE;
        findViewById(R.id.tl_presets).setVisibility(visibility);
        mButtonConfigure.setVisibility(visibility);
    }

    /**
     * To start the view to allow the user to customize the bands.
     */
    private void startCustomization() {
        Intent intentCustomEqualizer = new Intent(this, CustomEqualizerActivity.class);
        startActivity(intentCustomEqualizer);
    }

    /**
     * To manage packets from Gaia device which are "PACKET" directly by the library.
     *
     * @param msg
     *            The message coming from the handler which calls this method.
     */
    private void handlePacket(Message msg) {
        GaiaPacket packet = (GaiaPacket) msg.obj;

        switch (packet.getCommand()) {
            case Gaia.COMMAND_GET_USER_EQ_CONTROL:
                receivePacketGetUserEqControl(packet);
                break;
            case Gaia.COMMAND_GET_EQ_CONTROL:
                receivePacketGetEqControl(packet);
                break;
            case Gaia.COMMAND_GET_3D_ENHANCEMENT_CONTROL:
                receivePacketGet3dEnhancementControl(packet);
                break;
            case Gaia.COMMAND_GET_BASS_BOOST_CONTROL:
                receivePacketGetBassBoostControl(packet);
                break;
            case Gaia.COMMAND_SET_USER_EQ_CONTROL:
            case Gaia.COMMAND_SET_EQ_CONTROL:
            case Gaia.COMMAND_SET_3D_ENHANCEMENT_CONTROL:
            case Gaia.COMMAND_SET_BASS_BOOST_CONTROL:
                //noinspection PointlessBooleanExpression,ConstantConditions
                if (DEBUG && packet.isAcknowledgement())
                    Log.w(TAG, "ACK - command: " + packet.getCommand() + " - status: " + packet.getStatus());
                checkStatus(packet);
                break;

        }

    }

    /**
     * Called when we receive a packet about the command COMMAND_GET_3D_ENHANCEMENT_CONTROL to manage the application depending on
     * information from the packet.
     *
     * @param packet
     *            The packet about the received command COMMAND_GET_3D_ENHANCEMENT_CONTROL.
     */
    private void receivePacketGetUserEqControl (GaiaPacket packet) {
        if (checkStatus(packet)) {
            boolean activated = packet.getBoolean();
            mSwitchPresets.setEnabled(true);
            mSwitchPresets.setChecked(activated);
            if (activated) {
                sendGaiaPacket(Gaia.COMMAND_GET_EQ_CONTROL);
            }
            else {
                activatePresets(false);
            }
        }
    }

    /**
     * Called when we receive a packet about the command COMMAND_GET_USER_EQ_CONTROL to manage the application depending on
     * information from the packet.
     *
     * @param packet
     *            The packet about the received command COMMAND_GET_USER_EQ_CONTROL.
     */
    private void receivePacketGet3dEnhancementControl (GaiaPacket packet) {
        if (checkStatus(packet)) {
            boolean activated = packet.getBoolean();
            mSwitch3D.setEnabled(true);
            mSwitch3D.setChecked(activated);
        }
    }

    /**
     * Called when we receive a packet about the command COMMAND_GET_USER_EQ_CONTROL to manage the application depending on
     * information from the packet.
     *
     * @param packet
     *            The packet about the received command COMMAND_GET_USER_EQ_CONTROL.
     */
    private void receivePacketGetBassBoostControl (GaiaPacket packet) {
        if (checkStatus(packet)) {
            boolean activated = packet.getBoolean();
            mSwitchBass.setEnabled(true);
            mSwitchBass.setChecked(activated);
        }
    }

    /**
     * Called when we receive a packet about the command COMMAND_GET_EQ_CONTROL to manage the application depending on
     * information from the packet.
     *
     * @param packet
     *            The packet about the received command COMMAND_GET_EQ_CONTROL.
     */
    private void receivePacketGetEqControl(GaiaPacket packet) {
        int selectedPreset = packet.getByte();
        if (mSwitchPresets.isChecked()) {
            activatePresets(true);
        }
        selectPreset(selectedPreset);
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
     * When we received a packet about a command which is not supported by the device.
     *
     * @param packet
     *              the concerned packet.
     */
    private void receivePacketCommandNotSupported(GaiaPacket packet) {
        switch (packet.getCommand()) {
            case Gaia.COMMAND_GET_USER_EQ_CONTROL:
            case Gaia.COMMAND_SET_USER_EQ_CONTROL:
            case Gaia.COMMAND_GET_EQ_CONTROL:
            case Gaia.COMMAND_SET_EQ_CONTROL:
                activatePresets(false);
                mSwitchPresets.setVisibility(View.GONE);
                findViewById(R.id.tv_info_custom).setVisibility(View.VISIBLE);
                break;
            case Gaia.COMMAND_GET_3D_ENHANCEMENT_CONTROL:
            case Gaia.COMMAND_SET_3D_ENHANCEMENT_CONTROL:
                mSwitch3D.setVisibility(View.GONE);
                findViewById(R.id.tv_info_3D).setVisibility(View.VISIBLE);
                break;
            case Gaia.COMMAND_GET_BASS_BOOST_CONTROL:
            case Gaia.COMMAND_SET_BASS_BOOST_CONTROL:
                mSwitchBass.setVisibility(View.GONE);
                findViewById(R.id.tv_info_bass_boost).setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * The class which allows to manage messages from Gaia devices.
     */
    private static class GaiaHandler extends Handler {

        /**
         * The reference to this activity.
         */
        final WeakReference<EqualizerActivity> mActivity;

        /**
         * The constructor for this activity.
         *
         * @param activity
         *              this activity.
         */
        public GaiaHandler(EqualizerActivity activity) {
            super();
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            EqualizerActivity parentActivity = mActivity.get();
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

            default:
                if (DEBUG)
                    Log.d(TAG, handleMessage + "SOMETHING ELSE");
                break;
            }
        }
    }
}
