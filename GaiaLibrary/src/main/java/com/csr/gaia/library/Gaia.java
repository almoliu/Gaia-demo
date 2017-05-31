/******************************************************************************
 *  Copyright (C) Cambridge Silicon Radio Limited 2015
 *
 *  This software is provided to the customer for evaluation
 *  purposes only and, as such early feedback on performance and operation
 *  is anticipated. The software source code is subject to change and
 *  not intended for production. Use of developmental release software is
 *  at the user's own risk. This software is provided "as is," and CSR
 *  cautions users to determine for themselves the suitability of using the
 *  beta release version of this software. CSR makes no warranty or
 *  representation whatsoever of merchantability or fitness of the product
 *  for any particular purpose or use. In no event shall CSR be liable for
 *  any consequential, incidental or special damages whatsoever arising out
 *  of the use of or inability to use this software, even if the user has
 *  advised CSR of the possibility of such damages.
 *
 ******************************************************************************/
package com.csr.gaia.library;

import com.csr.gaia.library.exceptions.GaiaFrameException;

/**
 * The class which contains all types of commands and specific information for the gaia protocol.
 */

@SuppressWarnings("unused")
public class Gaia {
    static final byte SOF = (byte) 0xFF;

    /*
     0 bytes  1         2        3        4        5        6        7        8          9    len+8
     +--------+---------+--------+--------+--------+--------+--------+--------+ +--------+--------+ +--------+
     |   SOF  | VERSION | FLAGS  | LENGTH |    VENDOR ID    |   COMMAND ID    | | PAYLOAD   ...   | | CHECK  |
     +--------+---------+--------+--------+--------+--------+--------+--------+ +--------+--------+ +--------+
    */

    /**
     * Maximum number of bytes in the payload portion of a GAIA packet.
     */
    public static final int MAX_PACKET_PAYLOAD = 254;
    /**
     * Size of the header for a packet.
     */
    public static final int PACKET_HEADER_SIZE = 8;

    private static final int OFFS_SOF = 0;
    private static final int OFFS_VERSION = 1;
    static final int OFFS_FLAGS = 2;
    static final int OFFS_PAYLOAD_LENGTH = 3;
    static final int OFFS_VENDOR_ID = 4;
    private static final int OFFS_VENDOR_ID_H = OFFS_VENDOR_ID;
    private static final int OFFS_VENDOR_ID_L = OFFS_VENDOR_ID + 1;
    static final int OFFS_COMMAND_ID = 6;
    private static final int OFFS_COMMAND_ID_H = OFFS_COMMAND_ID;
    private static final int OFFS_COMMAND_ID_L = OFFS_COMMAND_ID + 1;
    static final int OFFS_PAYLOAD = 8;

    static final int FLAG_CHECK = 0x01;

    static final int COMMAND_MASK = 0x7FFF;
    static final int ACK_MASK = 0x8000;

    public static final int VENDOR_NONE = 0x7FFE;
    public static final int VENDOR_CSR = 0x000A;

    public static final int COMMAND_INTENT_GET = 0x0080;

    public static final int COMMAND_TYPE_MASK = 0x7F00;
    public static final int COMMAND_TYPE_CONFIGURATION = 0x0100;
    public static final int COMMAND_TYPE_CONTROL = 0x0200;
    public static final int COMMAND_TYPE_STATUS = 0x0300;
    public static final int COMMAND_TYPE_FEATURE = 0x0500;
    public static final int COMMAND_TYPE_DEBUG = 0x0700;
    public static final int COMMAND_TYPE_NOTIFICATION = 0x4000;


    /* *******************************
     * Configuration commands 0x01nn
     * ******************************* */

    public static final int COMMAND_SET_RAW_CONFIGURATION = 0x0100;
    /**
     * Retrieves the version of the configuration set.
     */
    public static final int COMMAND_GET_CONFIGURATION_VERSION = 0x0180;
    /**
     * Configures the LED indicators. Determines patterns to be displayed in given states and on events and configures filters to be applied as events occur.
     */
    public static final int COMMAND_SET_LED_CONFIGURATION = 0x0101;
    /**
     * Retrieves the current LED configuration.
     */
    public static final int COMMAND_GET_LED_CONFIGURATION = 0x0181;
    /**
     * Configures informational tones on the device.
     */
    public static final int COMMAND_SET_TONE_CONFIGURATION = 0x0102;
    /**
     * Retrieves the currently configured tone configuration.
     */
    public static final int COMMAND_GET_TONE_CONFIGURATION = 0x0182;
    /**
     * Sets the default volume for tones and audio.
     */
    public static final int COMMAND_SET_DEFAULT_VOLUME = 0x0103;
    /**
     * Requests the default volume settings for tones and audio.
     */
    public static final int COMMAND_GET_DEFAULT_VOLUME = 0x0183;
    /**
     * Resets all settings (deletes PS keys) which override factory defaults.
     */
    public static final int COMMAND_FACTORY_DEFAULT_RESET = 0x0104;
    public static final int COMMAND_GET_CONFIGURATION_ID = 0x0184;
    /**
     * Configures per-event vibrator patterns
     */
    public static final int COMMAND_SET_VIBRATOR_CONFIGURATION = 0x0105;
    /**
     * Retrieves the currently configured vibrator configuration.
     */
    public static final int COMMAND_GET_VIBRATOR_CONFIGURATION = 0x0185;
    /**
     * Configures voice prompts to select a different language, voice etc.
     */
    public static final int COMMAND_SET_VOICE_PROMPT_CONFIGURATION = 0x0106;
    /**
     * Retrieves the currently configured voice prompt configuration.
     */
    public static final int COMMAND_GET_VOICE_PROMPT_CONFIGURATION = 0x0186;
    /**
     * Configures device features. The feature identifiers are application dependent and would be documented with the application.
     */
    public static final int COMMAND_SET_FEATURE_CONFIGURATION = 0x0107;
    /**
     * Retrieves settings of device features.
     */
    public static final int COMMAND_GET_FEATURE_CONFIGURATION = 0x0187;
    /**
     * Set User Event Configuration.
     */
    public static final int COMMAND_SET_USER_EVENT_CONFIGURATION = 0x0108;
    /**
     * Get User Event Configuration.
     */
    public static final int COMMAND_GET_USER_EVENT_CONFIGURATION = 0x0188;
    /**
     * Configures the various timers on the device. This command has a long form (where the payload holds the value of every timer) and a short form (where the payload holds a timer number and the value of that timer).
     */
    public static final int COMMAND_SET_TIMER_CONFIGURATION = 0x0109;
    /**
     * Retrieves the configuration of the various timers on the device. This command has a long form (where the response holds the value of every timer) and a short form (where the command payload holds a timer number and the response holds the number and value of that timer)
     */
    public static final int COMMAND_GET_TIMER_CONFIGURATION = 0x0189;
    /**
     * Configures the device volume control for each of the 16 volume levels.
     */
    public static final int COMMAND_SET_AUDIO_GAIN_CONFIGURATION = 0x010A;
    /**
     * Requests the device volume control configuration for each of the 16 volume levels.
     */
    public static final int COMMAND_GET_AUDIO_GAIN_CONFIGURATION = 0x018A;
    /**
     * Set Volume Configuration.
     */
    public static final int COMMAND_SET_VOLUME_CONFIGURATION = 0x010B;
    /**
     * Get Volume Configuration.
     */
    public static final int COMMAND_GET_VOLUME_CONFIGURATION = 0x018B;
    /**
     * Set Power Configuration.
     */
    public static final int COMMAND_SET_POWER_CONFIGURATION = 0x010C;
    /**
     * Get Power Configuration.
     */
    public static final int COMMAND_GET_POWER_CONFIGURATION = 0x018C;
    /**
     * Set User Tone Configuration.
     */
    public static final int COMMAND_SET_USER_TONE_CONFIGURATION = 0x010E;
    /**
     * Get User Tone Configuration
     */
    public static final int COMMAND_GET_USER_TONE_CONFIGURATION = 0x018E;
    /**
     * Set device name.
     */
    public static final int COMMAND_SET_DEVICE_NAME = 0x010F;
    /**
     * Get device name.
     */
    public static final int COMMAND_GET_DEVICE_NAME = 0x018F;
    /**
     * Sets the credentials to access the Wi-Fi access point.
     */
    public static final int COMMAND_SET_WLAN_CREDENTIALS = 0x0110;
    /**
     * Retrieves the credentials to access the Wi-Fi access point.
     */
    public static final int COMMAND_GET_WLAN_CREDENTIALS = 0x0190;
    /**
     * Set peer permitted routing.
     */
    public static final int COMMAND_SET_PEER_PERMITTED_ROUTING = 0x0111;
    /**
     * Get peer permitted routing.
     */
    public static final int COMMAND_GET_PEER_PERMITTED_ROUTING = 0x0191;
    /**
     * Set permitted next audio source.
     */
    public static final int COMMAND_SET_PERMITTED_NEXT_AUDIO_SOURCE = 0x0112;
    /**
     * Get permitted next audio source.
     */
    public static final int COMMAND_GET_PERMITTED_NEXT_AUDIO_SOURCE = 0x0192;
    /**
     * Sets the string to be sent to an AG to be dialled when the one-touch dialling feature is used.
     */
    public static final int COMMAND_SET_ONE_TOUCH_DIAL_STRING = 0x0116;
    /**
     * Returns the string to be sent to an AG to be dialled when the one-touch dialling feature is used.
     */
    public static final int COMMAND_GET_ONE_TOUCH_DIAL_STRING = 0x0196;
    /**
     * Get Mounted partitions.
     */
    public static final int COMMAND_GET_MOUNTED_PARTITIONS = 0x01A0;
    /**
     * Configures which SQIF partition is to be used for DFU operations.
     */
    public static final int COMMAND_SET_DFU_PARTITION = 0x0121;
    /**
     * Retrieves the index and size of the configured DFU partition.
     */
    public static final int COMMAND_GET_DFU_PARTITION = 0x01A1;


    /* *******************************
     *    Controls commands 0x01nn
     * ******************************* */

    /**
     * The host can raise/lower the current volume or mute/unmute audio using this command.
     */
    public static final int COMMAND_CHANGE_VOLUME = 0x0201;
    /**
     * A host can cause a device to warm reset using this command. The device will transmit an acknowledgement and then do a warm reset.
     */
    public static final int COMMAND_DEVICE_RESET = 0x0202;
    /**
     * Requests the device's current boot mode.
     */
    public static final int COMMAND_GET_BOOT_MODE = 0x0282;
    /**
     * Sets the state of device PIO pins.
     */
    public static final int COMMAND_SET_PIO_CONTROL = 0x0203;
    /**
     * Gets the state of device PIOs.
     */
    public static final int COMMAND_GET_PIO_CONTROL = 0x0283;
    /**
     * The host can request the device to physically power on or off by sending a "Set Power State" command. The device will transmit an acknowledgement in response to the hosts request, if accepted the device shall also physically power on / off.
     */
    public static final int COMMAND_SET_POWER_STATE = 0x0204;
    /**
     * The host can request to retrieve the devices current power state. The device will transmit an acknowledgement and if successful, shall also indicate its current power state.
     */
    public static final int COMMAND_GET_POWER_STATE = 0x0284;
    /**
     * Sets the orientation of the volume control buttons on the device.
     */
    public static final int COMMAND_SET_VOLUME_ORIENTATION = 0x0205;
    /**
     * Requests the current orientation of the volume control buttons on the device.
     */
    public static final int COMMAND_GET_VOLUME_ORIENTATION = 0x0285;
    /**
     * Enables or disables use of the vibrator in the headset, if one is present.
     */
    public static final int COMMAND_SET_VIBRATOR_CONTROL = 0x0206;
    /**
     * Requests the current setting of the vibrator.
     */
    public static final int COMMAND_GET_VIBRATOR_CONTROL = 0x0286;
    /**
     * Enables or disables LEDs (or equivalent indicators) on the headset.
     */
    public static final int COMMAND_SET_LED_CONTROL = 0x0207;
    /**
     * Establishes whether LED indicators are enabled.
     */
    public static final int COMMAND_GET_LED_CONTROL = 0x0287;
    /**
     * Sent from a headset to control an FM receiver on the phone, or from a handset to control a receiver in a headset.
     */
    public static final int COMMAND_FM_CONTROL = 0x0208;
    /**
     * Play tone.
     */
    public static final int COMMAND_PLAY_TONE = 0x0209;
    /**
     * Enables or disables voice prompts on the headset.
     */
    public static final int COMMAND_SET_VOICE_PROMPT_CONTROL = 0x020A;
    /**
     * Establishes whether voice prompts are enabled.
     */
    public static final int COMMAND_GET_VOICE_PROMPT_CONTROL = 0x028A;
    /**
     * Selects the next available language for Text-to-Speech functions.
     */
    public static final int COMMAND_CHANGE_AUDIO_PROMPT_LANGUAGE = 0x020B;
    /**
     * Enables or disables simple speech recognition on the headset.
     */
    public static final int COMMAND_SET_SPEECH_RECOGNITION_CONTROL = 0x020C;
    /**
     * Establishes whether speech recognition is enabled.
     */
    public static final int COMMAND_GET_SPEECH_RECOGNITION_CONTROL = 0x028C;
    /**
     * Alert LEDs.
     */
    public static final int COMMAND_ALERT_LEDS = 0x020D;
    /**
     * Alert tone.
     */
    public static final int COMMAND_ALERT_TONE = 0x020E;
    /**
     * Alert the device user with LED patterns, tones or vibration. The method and meaning of each alert is application-dependent and is configured using the appropriate LED, tone or vibrator event configuration.
     */
    public static final int COMMAND_ALERT_EVENT = 0x0210;
    /**
     * Alert voice.
     */
    public static final int COMMAND_ALERT_VOICE = 0x0211;
    /**
     * Set audio prompt language.
     */
    public static final int COMMAND_SET_AUDIO_PROMPT_LANGUAGE = 0x0212;
    /**
     * Get audio prompt language.
     */
    public static final int COMMAND_GET_AUDIO_PROMPT_LANGUAGE = 0x0292;
    /**
     * Starts the Simple Speech Recognition engine on the device. A successful acknowledgement indicates that speech recognition has started; the actual speech recognition result will be relayed later via a GAIA_EVENT_SPEECH_RECOGNITION notification.
     */
    public static final int COMMAND_START_SPEECH_RECOGNITION = 0x0213;
    /**
     * Selects an audio equaliser preset.
     */
    public static final int COMMAND_SET_EQ_CONTROL = 0x0214;
    /**
     * Gets the currently selected audio equaliser preset.
     */
    public static final int COMMAND_GET_EQ_CONTROL = 0x0294;
    /**
     * Enables or disables bass boost on the headset.
     */
    public static final int COMMAND_SET_BASS_BOOST_CONTROL = 0x0215;
    /**
     * Establishes whether bass boost is enabled.
     */
    public static final int COMMAND_GET_BASS_BOOST_CONTROL = 0x0295;
    /**
     * Enables or disables 3D sound enhancement on the headset.
     */
    public static final int COMMAND_SET_3D_ENHANCEMENT_CONTROL = 0x0216;
    /**
     * Establishes whether 3D Enhancement is enabled.
     */
    public static final int COMMAND_GET_3D_ENHANCEMENT_CONTROL = 0x0296;
    /**
     * Switches to the next available equaliser preset. If issued while the last available preset is selected, switches to the first.
     */
    public static final int COMMAND_SWITCH_EQ_CONTROL = 0x0217;
    /**
     * Turns on the Bass Boost effect if it was turned off; turns Bass Boost off if it was on.
     */
    public static final int COMMAND_TOGGLE_BASS_BOOST_CONTROL = 0x0218;
    /**
     * Turns on the 3D Enhancement effect if it was turned off; turns 3D Enhancement off if it was on.
     */
    public static final int COMMAND_TOGGLE_3D_ENHANCEMENT_CONTROL = 0x0219;
    /**
     * Sets a parameter of the parametric equaliser and optionally recalculates the filter coefficients.
     */
    public static final int COMMAND_SET_EQ_PARAMETER = 0x021A;
    /**
     * Gets a parameter of the parametric equaliser.
     */
    public static final int COMMAND_GET_EQ_PARAMETER = 0x029A;
    /**
     * Sets a group of parameters of the parametric equaliser.
     */
    public static final int COMMAND_SET_EQ_GROUP_PARAMETER = 0x021B;
    /**
     * Gets a group of parameters of the parametric equaliser.
     */
    public static final int COMMAND_GET_EQ_GROUP_PARAMETER = 0x029B;
    /**
     * Display control.
     */
    public static final int COMMAND_DISPLAY_CONTROL = 0x021C;
    /**
     * Puts a Bluetooth device into pairing mode, making it discoverable and connectable.
     */
    public static final int COMMAND_ENTER_BLUETOOTH_PAIRING_MODE = 0x021D;
    /**
     * Sets the device audio source.
     */
    public static final int COMMAND_SET_AUDIO_SOURCE = 0x021E;
    /**
     * Gets the currently selected audio source.
     */
    public static final int COMMAND_GET_AUDIO_SOURCE = 0x029E;
    /**
     * Sends an AVRC command to the device.
     */
    public static final int COMMAND_AV_REMOTE_CONTROL = 0x021F;
    /**
     * Enables or disables the User-configured parametric equaliser on the device (compare Set EQ Control).
     */
    public static final int COMMAND_SET_USER_EQ_CONTROL = 0x0220;
    /**
     * Establishes whether User EQ is enabled.
     */
    public static final int COMMAND_GET_USER_EQ_CONTROL = 0x02A0;
    /**
     * Turns on the User EQ if it was turned off; turns User EQ off if it was on.
     */
    public static final int COMMAND_TOGGLE_USER_EQ_CONTROL = 0x0221;
    /**
     * Enables or disables the speaker equaliser on the device.
     */
    public static final int COMMAND_SET_SPEAKER_EQ_CONTROL = 0x0222;
    /**
     * Establishes whether Speaker EQ is enabled.
     */
    public static final int COMMAND_GET_SPEAKER_EQ_CONTROL = 0x02A2;
    /**
     * Turns on the Speaker EQ if it was turned off; turns Speaker EQ off if it was on.
     */
    public static final int COMMAND_TOGGLE_SPEAKER_EQ_CONTROL = 0x0223;
    /**
     * Controls the routing of True Wireless Stereo channels.
     */
    public static final int COMMAND_SET_TWS_AUDIO_ROUTING = 0x0224;
    /**
     * Returns the current routing of True Wireless Stereo channels.
     */
    public static final int COMMAND_GET_TWS_AUDIO_ROUTING = 0x02A4;
    /**
     * Controls the volume of True Wireless Stereo output.
     */
    public static final int COMMAND_SET_TWS_VOLUME = 0x0225;
    /**
     * Returns the current volume setting of True Wireless Stereo.
     */
    public static final int COMMAND_GET_TWS_VOLUME = 0x02A5;
    /**
     * Trims the volume of True Wireless Stereo output.
     */
    public static final int COMMAND_TRIM_TWS_VOLUME = 0x0226;
    /**
     * Enables or disables reservation of one link for a peer device.
     */
    public static final int COMMAND_SET_PEER_LINK_RESERVED = 0x0227;
    /**
     * Establishes whether one link is reserved for a peer device.
     */
    public static final int COMMAND_GET_PEER_LINK_RESERVED = 0x02A7;
    /**
     * Requests the peer in a True Wireless Stereo session to begin Advertising. The command payload length will be 1 if no target address is specified or 8 if a Typed Bluetooth Device Address is specified.
     */
    public static final int COMMAND_TWS_PEER_START_ADVERTISING = 0x022A;
    /**
     * Requests the device send a "Find Me" request to the HID remote connected to it.
     */
    public static final int COMMAND_FIND_MY_REMOTE = 0x022B;

    public static final int COMMAND_SET_CODEC = 0x0240;
    public static final int COMMAND_GET_CODEC = 0x02C0;

    // Special control commands designed for Arden/Swift board
    public static final int COMMAND_MASTER_VOLUME = 0x022C;
    public static final int COMMAND_GET_MASTER_VOLUME = 0x02AC;


    /* ***********************************
     *    Polled status commands 0x03nn
     * *********************************** */

    /**
     * Get the Gaia Protocol and API version numbers from the device.
     */
    public static final int COMMAND_GET_API_VERSION = 0x0300;
    /**
     * Get the current RSSI value for the Bluetooth link from the device. The RSSI is specified in dBm using 2's compliment representation, e.g. -20 = 0xEC.
     */
    public static final int COMMAND_GET_CURRENT_RSSI = 0x0301;
    /**
     * Get the current battery level from the device. Battery level is specified in mV stored as a uint16, e.g. 3,300mV = 0x0CE4.
     */
    public static final int COMMAND_GET_CURRENT_BATTERY_LEVEL = 0x0302;
    /**
     * Requests the BlueCore hardware, design and module identification.
     */
    public static final int COMMAND_GET_MODULE_ID = 0x0303;
    /**
     * Requests the application software to identify itself. The acknowledgement payload contains eight octets of application identification optionally followed by nul-terminated human-readable text. The identification information is application dependent; the headset copies fields from the Bluetooth Device ID.
     */
    public static final int COMMAND_GET_APPLICATION_VERSION = 0x0304;
    /**
     * Requests the logic state of the chip PIOs.
     */
    public static final int COMMAND_GET_PIO_STATE = 0x0306;
    /**
     * Requests the value read by a given analogue-to-digital converter.
     */
    public static final int COMMAND_READ_ADC = 0x0307;
    /**
     * Requests the Bluetooth device address of the peer.
     */
    public static final int COMMAND_GET_PEER_ADDRESS = 0x030A;
    /**
     * @deprecated Use COMMAND_DFU_GET_RESULT.
     */
    public static final int COMMAND_GET_DFU_STATUS = 0x0310;


    /* *************************************
     *    Feature control commands 0x05nn
     * ************************************* */

    public static final int COMMAND_GET_AUTH_BITMAPS = 0x0580;
    /**
     * Initiate a Gaia Authentication exchange.
     */
    public static final int COMMAND_AUTHENTICATE_REQUEST = 0x0501;
    /**
     * Provide authentication credentials.
     */
    public static final int COMMAND_AUTHENTICATE_RESPONSE = 0x0502;
    /**
     * The host can use this command to enable or disable a feature which it is authenticated to use.
     */
    public static final int COMMAND_SET_FEATURE = 0x0503;
    /**
     * The host can use this command to request the status of a feature.
     */
    public static final int COMMAND_GET_FEATURE = 0x0583;
    /**
     * The host uses this command to enable a GAIA session with a device which does not have the session enabled by default.
     */
    public static final int COMMAND_SET_SESSION_ENABLE = 0x0504;
    /**
     * Retrieves the session enabled state.
     */
    public static final int COMMAND_GET_SESSION_ENABLE = 0x0584;


    /* **********************************
     *    Data transfer commands 0x06nn
     * ********************************** */

    /**
     * Initialise a data transfer session.
     */
    public static final int COMMAND_DATA_TRANSFER_SETUP = 0x0601;
    /**
     * The host uses this command to indicate closure of a data transfer session, providing the Session ID in the packet payload. The device can release any resources required to maintain a data transfer session at this point, as the host must perform another Data Transfer Setup before sending any more data.
     */
    public static final int COMMAND_DATA_TRANSFER_CLOSE = 0x0602;
    /**
     * A host can use this command to transfer data to a device.
     */
    public static final int COMMAND_HOST_TO_DEVICE_DATA = 0x0603;
    /**
     * A device can use this command to transfer data to the host.
     */
    public static final int COMMAND_DEVICE_TO_HOST_DATA = 0x0604;
    /**
     * Initiates an I2C Transfer (write and/or read).
     */
    public static final int COMMAND_I2C_TRANSFER = 0x0608;
    /**
     * Retrieves information on a storage partition.
     */
    public static final int COMMAND_GET_STORAGE_PARTITION_STATUS = 0x0610;
    /**
     * Prepares a device storage partition for access from the host.
     */
    public static final int COMMAND_OPEN_STORAGE_PARTITION = 0x0611;
    /**
     * Prepares a UART for access from the host.
     */
    public static final int COMMAND_OPEN_UART = 0x0612;
    /**
     * Writes raw data to an open storage partition.
     */
    public static final int COMMAND_WRITE_STORAGE_PARTITION = 0x0615;
    /**
     * Writes data to an open stream.
     */
    public static final int COMMAND_WRITE_STREAM = 0x0617;
    /**
     * Closes a storage partition.
     */
    public static final int COMMAND_CLOSE_STORAGE_PARTITION = 0x0618;
    /**
     * Mounts a device storage partition for access from the device.
     */
    public static final int COMMAND_MOUNT_STORAGE_PARTITION = 0x061A;
    /**
     * Get file status.
     */
    public static final int COMMAND_GET_FILE_STATUS = 0x0620;
    /**
     * Prepares a file for access from the host.
     */
    public static final int COMMAND_OPEN_FILE = 0x0621;
    /**
     * Reads data from an open file.
     */
    public static final int COMMAND_READ_FILE = 0x0624;
    /**
     * Closes a file.
     */
    public static final int COMMAND_CLOSE_FILE = 0x0628;
    /**
     * Indicates to the host that the device wishes to receive a Device Firmware Upgrade image.
     */
    public static final int COMMAND_DFU_REQUEST = 0x0630;
    /**
     * Readies the device to receive a Device Firmware Upgrade image. The payload will be 8 or 136 octets depending on
     * the message digest type.
     */
    public static final int COMMAND_DFU_BEGIN = 0x0631;
    public static final int COMMAND_DFU_WRITE = 0x0632;
    /**
     * Commands the device to install the DFU image and restart.
     */
    public static final int COMMAND_DFU_COMMIT = 0x0633;
    /**
     * Requests the status of the last completed DFU operation.
     */
    public static final int COMMAND_DFU_GET_RESULT = 0x0634;
    /**
     * Begins a VM Upgrade session over GAIA, allowing VM Upgrade Protocol packets to be sent using the VM Upgrade Control and VM Upgrade Data commands.
     */
    public static final int COMMAND_VM_UPGRADE_CONNECT = 0x0640;
    /**
     * Ends a VM Upgrade session over GAIA.
     */
    public static final int COMMAND_VM_UPGRADE_DISCONNECT = 0x0641;
    /**
     * Tunnels a VM Upgrade Protocol packet.
     */
    public static final int COMMAND_VM_UPGRADE_CONTROL = 0x0642;
    /**
     * Introduces VM Upgrade Protocol data.
     */
    public static final int COMMAND_VM_UPGRADE_DATA = 0x0643;


    /* **********************************
     *    Debugging commands 0x07nn
     * ********************************** */

    /**
     * Requests the device to perform no operation; serves to establish that the Gaia protocol handler is alive.
     */
    public static final int COMMAND_NO_OPERATION = 0x0700;
    /**
     * Requests the values of the device debugging flags.
     */
    public static final int COMMAND_GET_DEBUG_FLAGS = 0x0701;
    /**
     * Sets the values of the device debugging flags.
     */
    public static final int COMMAND_SET_DEBUG_FLAGS = 0x0702;
    /**
     * Retrieves the value of the indicated PS key.
     */
    public static final int COMMAND_RETRIEVE_PS_KEY = 0x0710;
    /**
     * Retrieves the value of the indicated PS key.
     */
    public static final int COMMAND_RETRIEVE_FULL_PS_KEY = 0x0711;
    /**
     * Sets the value of the indicated PS key.
     */
    public static final int COMMAND_STORE_PS_KEY = 0x0712;
    /**
     * Flood fill the store to force a defragment at next boot.
     */
    public static final int COMMAND_FLOOD_PS = 0x0713;
    /**
     * Sets the value of the indicated PS key.
     */
    public static final int COMMAND_STORE_FULL_PS_KEY = 0x0714;
    /**
     * Results in a GAIA_DEBUG_MESSAGE being sent up from the Gaia library to the application task. Its interpretation is entirely user defined.
     */
    public static final int COMMAND_SEND_DEBUG_MESSAGE = 0x0720;
    /**
     * Sends an arbitrary message to the on-chip application.
     */
    public static final int COMMAND_SEND_APPLICATION_MESSAGE = 0x0721;
    /**
     * Sends an arbitrary message to the Kalimba DSP.
     */
    public static final int COMMAND_SEND_KALIMBA_MESSAGE = 0x0722;
    /**
     * Retrieves the number of available malloc() slots and the space available for PS keys.
     */
    public static final int COMMAND_GET_MEMORY_SLOTS = 0x0730;
    /**
     * Retrieves the value of the specified 16-bit debug variable.
     */
    public static final int COMMAND_GET_DEBUG_VARIABLE = 0x0740;
    /**
     * Sets the value of the specified 16-bit debug variable.
     */
    public static final int COMMAND_SET_DEBUG_VARIABLE = 0x0741;
    /**
     * Removes all authenticated devices from the paired device list and any associated attribute data.
     */
    public static final int COMMAND_DELETE_PDL = 0x0750;
    /**
     * Sent to a BLE slave device, causing it to request a new set of connection parameters.
     */
    public static final int COMMAND_SET_BLE_CONNECTION_PARAMETERS = 0x0752;


    /* **********************************
     *    Notification commands 0x04nn
     * ********************************** */

    /**
     * Hosts register for notifications using the Register Notification command, specifying an Event Type from table below as the first byte of payload, with optional parameters as defined per event in successive payload bytes.
     */
    public static final int COMMAND_REGISTER_NOTIFICATION = 0x4001;
    /**
     * Requests the current status of an event type. For threshold type events where multiple levels may be registered, the response indicates how many notifications are registered. Where an event may be simply registered or not the number will be 1 or 0.
     */
    public static final int COMMAND_GET_NOTIFICATION = 0x4081;
    /**
     * A host can cancel event notification by sending a Cancel Notification command, the first byte of payload will be the Event Type being cancelled.
     */
    public static final int COMMAND_CANCEL_NOTIFICATION = 0x4002;
    /**
     * Assuming successful registration, the host will asynchronously receive one or more Event Notification command(s) (Command ID 0x4003). The first byte of the Event Notification command payload will be the Event Type code, indicating the notification type. For example, 0x03 indicating a battery level low threshold event notification. Further data in the Event Notification payload is dependent on the notification type and defined on a per-notification basis below.
     */
    public static final int COMMAND_EVENT_NOTIFICATION = 0x4003;



    private static final int PROTOCOL_VERSION = 1;
    private static final byte DEFAULT_FLAGS = 0x00;

    public static final int MAX_PAYLOAD = 254;
    public static final int MAX_PACKET = 270;

    public static final int FEATURE_DISABLED = 0x00;
    public static final int FEATURE_ENABLED = 0x01;

    /**
     * The different states during the device firmware upgrade.
     */
    public enum DfuState {
        DOWNLOAD, DOWNLOAD_FAILURE, VERIFICATION, VERIFICATION_FAILURE, VERIFICATION_SUCCESS;

        private static final DfuState[] values = DfuState.values();

        public static DfuState valueOf(int state) {
            if (state < 0 || state >= values.length)
                return null;

            return values[state];
        }
    }

    /**
     * <p>The different status for an acknowledgment packet.</p>
     * <p>By convention, the first octet in an acknowledgement (ACK) packet is a status code indicating the success or the reason for the failure of a request.</p>
     */
    public enum Status {
        /**
         * The request completed successfully.
         */
        SUCCESS,
        /**
         * An invalid COMMAND ID has been sent or is not supported by the device.
         */
        NOT_SUPPORTED,
        /**
         * The host is not authenticated to use a Command ID or to control a feature type.
         */
        NOT_AUTHENTICATED,
        /**
         * The COMMAND ID used is valid but the GAIA device could not complete it successfully.
         */
        INSUFFICIENT_RESOURCES,
        /**
         * The GAIA device is in the process of authenticating the host.
         */
        AUTHENTICATING,
        /**
         * The parameters sent were invalid: missing parameters, too much parameters, range, etc.
         */
        INVALID_PARAMETER,
        /**
         * The GAIA device is not in the correct state to process the command: needs to stream music, use a certain source, etc.
         */
        INCORRECT_STATE,
        /**
         * The command is in progress.
         * <p>Acknowledgements with IN_PROGRESS status may be sent once or periodically during the processing of a time-consuming operation to indicate that the operation has not stalled.</p>
         */
        IN_PROGRESS;

        private static final Status[] values = Status.values();

        public static Status valueOf(int status) {
            if (status < 0 || status >= values.length)
                return null;

            return values[status];
        }
    }

    /**
     * All notifications which can be sent by the device.
     */
    public enum EventId {
        /**
         * This is not a notification. (0x00)
         */
        START,
        /**
         * This event provides a way for hosts to receive notification of changes in the RSSI of a device's Bluetooth link with the host. (0x01)
         */
        RSSI_LOW_THRESHOLD,
        /**
         * This command provides a way for hosts to receive notification of changes in the RSSI of a device's Bluetooth link with the host. (0x02)
         */
        RSSI_HIGH_THRESHOLD,
        /**
         * This command provides a way for hosts to receive notification of changes in the battery level of a device. (0x03)
         */
        BATTERY_LOW_THRESHOLD,
        /**
         * This command provides a way for hosts to receive notification of changes in the battery level of a device. (0x04)
         */
        BATTERY_HIGH_THRESHOLD,
        /**
         * A host can register to receive notifications of the device changes in state. (0x05)
         */
        DEVICE_STATE_CHANGED,
        /**
         * A host can register to receive notification of a change in PIO state. The host provides a uint32 bitmap of PIO pins about which it wishes to receive state change notifications. (0x06)
         */
        PIO_CHANGED,
        /**
         * A host can register to receive debug messages from a device. (0x07)
         */
        DEBUG_MESSAGE,
        /**
         * A host can register to receive a notification when the device battery has been fully charged. (0x08)
         */
        BATTERY_CHARGED,
        /**
         * A host can register to receive a notification when the battery charger is connected or disconnected. (0x09)
         */
        CHARGER_CONNECTION,
        /**
         * A host can register to receive a notification when the capacitive touch sensors' state changes. Removed from V1.0 of the API but sounds useful. (0x0A)
         */
        CAPSENSE_UPDATE,
        /**
         * A host can register to receive a notification when an application-specific user action takes place, for instance a long button press. Not the same as PIO Changed. Removed from V1.0 of the API but sounds useful. (0x0B)
         */
        USER_ACTION,
        /**
         * A host can register to receive a notification when the Speech Recognition system thinks it heard something. (0x0C)
         */
        SPEECH_RECOGNITION,
        /**
         * (0x0D ?)
         */
        AV_COMMAND,
        /**
         * (0x0E ?)
         */
        REMOTE_BATTERY_LEVEL,
        /**
         * (0x0F ?)
         */
        KEY,
        /**
         * This notification event indicates the progress of a Device Firmware Upgrade operation. (0x10)
         */
        DFU_STATE,
        /**
         * This notification event indicates that data has been received by a UART. (0x11)
         */
        UART_RECEIVED_DATA,
        /**
         * This notification event encapsulates a VM Upgrade Protocol packet. (0x12)
         */
        VMU_PACKET;

        private static final EventId[] values = EventId.values();

        public static EventId valueOf(int id) {
            if (id < 0 || id >= values.length)
                return null;

            return values[id];
        }

    }

    /**
     * The results returned by the SPEECH RECOGNITION command.
     */
    public enum AsrResult {
        UNRECOGNISED, NO, YES, WAIT, CANCEL;

        private static final AsrResult[] values = AsrResult.values();

        public static AsrResult valueOf(int id) {
            if (id < 0 || id >= values.length)
                return null;

            return values[id];
        }
    }

    /**
     * Returns descriptive string representing a status code.
     * 
     * @param status
     *            The status code to be translated
     */
    public static String statusText(Status status) {
        switch (status) {
        case SUCCESS:
            return "Success";

        case NOT_SUPPORTED:
            return "Command not supported";

        case NOT_AUTHENTICATED:
            return "Not authenticated";

        case INSUFFICIENT_RESOURCES:
            return "Insufficient resources";

        case AUTHENTICATING:
            return "Authentication in progress";

        case INVALID_PARAMETER:
            return "Invalid parameter";

        default:
            return "Unknown status code " + status;
        }
    }

    /**
     * Build a GAIA frame.
     *
     * @param vendor_id
     *            Vendor identifier.
     * @param command_id
     *            Command identifier.
     * @param payload
     *            Array of payload bytes
     * @param payload_length
     *            Length of payload.
     * @param flags
     *            Flags byte.
     *
     * @return Correctly formatted GAIA frame as an array of bytes.
     *
     * @throws GaiaFrameException
     *             when arguments do not match with expectations.
     */
    public static byte[] frame(int vendor_id, int command_id, byte[] payload, int payload_length, byte flags)
            throws GaiaFrameException {
        if (payload_length > MAX_PAYLOAD) {
            throw new GaiaFrameException(GaiaFrameException.Type.ILLEGAL_ARGUMENTS_PAYLOAD_LENGTH_TOO_LONG);
        }

        boolean use_check = (flags & FLAG_CHECK) != 0;
        int packet_length = payload_length + OFFS_PAYLOAD + (use_check ? 1 : 0);
        byte[] data = new byte[packet_length];

        data[OFFS_SOF] = SOF;
        data[OFFS_VERSION] = PROTOCOL_VERSION;
        data[OFFS_FLAGS] = flags;
        data[OFFS_PAYLOAD_LENGTH] = (byte) payload_length;
        data[OFFS_VENDOR_ID_H] = (byte) (vendor_id >> 8);
        data[OFFS_VENDOR_ID_L] = (byte) vendor_id;
        data[OFFS_COMMAND_ID_H] = (byte) (command_id >> 8);
        data[OFFS_COMMAND_ID_L] = (byte) command_id;

        //noinspection ManualArrayCopy
        for (int idx = 0; idx < payload_length; ++idx)
            data[idx + OFFS_PAYLOAD] = payload[idx];

        if (use_check) {
            byte check = 0;

            for (int idx = 0; idx < packet_length - 1; ++idx)
                check ^= data[idx];

            data[packet_length - 1] = check;
        }

        return data;
    }

    /**
     * Build a GAIA frame with default flags set.
     * 
     * @param vendor_id
     *            Vendor identifier.
     * @param command_id
     *            Command identifier.
     * @param payload
     *            Array of payload bytes
     * @param payload_length
     *            Length of payload.
     * @return Correctly formatted GAIA frame as an array of bytes.
     *
     * @throws GaiaFrameException
     *             when an error occurs during building frame.
     */
    public static byte[] frame(int vendor_id, int command_id, byte[] payload, int payload_length)
            throws GaiaFrameException {
        return frame(vendor_id, command_id, payload, payload_length, DEFAULT_FLAGS);
    }

    /**
     * Build a GAIA frame.
     * 
     * @param vendor_id
     *            Vendor identifier.
     * @param command_id
     *            Command identifier.
     * @param payload
     *            Array of payload bytes (payload length will be set to the size of this array).
     * @param flags
     *            Flags byte.
     * @return Correctly formatted GAIA frame as an array of bytes.
     *
     * @throws GaiaFrameException
     *             when an error occurs during building frame.
     */
    @SuppressWarnings("SameParameterValue")
    public static byte[] frame(int vendor_id, int command_id, byte[] payload, byte flags) throws GaiaFrameException {
        int payload_length;

        if (payload == null)
            payload_length = 0;

        else
            payload_length = payload.length;

        return frame(vendor_id, command_id, payload, payload_length, flags);
    }

    /**
     * Build a GAIA frame with default flags set.
     * 
     * @param vendor_id
     *            Vendor identifier.
     * @param command_id
     *            Command identifier.
     * @param payload
     *            Array of payload bytes (payload length will be set to the size of this array).
     * @return Correctly formatted GAIA frame as an array of bytes.
     *
     * @throws GaiaFrameException
     *             when an error occurs during building frame.
     */
    @SuppressWarnings("SameParameterValue")
    public static byte[] frame(int vendor_id, int command_id, byte[] payload) throws GaiaFrameException {
        return frame(vendor_id, command_id, payload, DEFAULT_FLAGS);
    }

    /**
     * Build a GAIA frame with no payload.
     * 
     * @param vendor_id
     *            Vendor identifier.
     * @param command_id
     *            Command identifier.
     *
     * @return Correctly formatted GAIA frame as an array of bytes.
     *
     * @throws GaiaFrameException
     *             when an error occurs during building frame.
     */
    public static byte[] frame(int vendor_id, int command_id) throws GaiaFrameException {
        return frame(vendor_id, command_id, null);
    }

    /**
     * Get 8-bit hex string representation of byte.
     * 
     * @param b
     *            The value.
     * @return Hex value as a string.
     */
    public static String hexb(byte b) {
        return String.format("%02X", b & 0xFF);
    }

    /**
     * Get 16-bit hex string representation of byte.
     * 
     * @param i
     *            The value.
     * @return Hex value as a string.
     */
    public static String hexw(int i) {
        return String.format("%04X", i & 0xFFFF);
    }
}
