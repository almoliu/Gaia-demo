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

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.csr.gaia.library.Gaia.Status;
import com.csr.gaia.library.exceptions.GaiaFrameException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * This class is the main manager to communicate with Gaia devices.
 */

@SuppressWarnings("unused")
public class GaiaLink {

    /**
     * All types of interaction the Android device has with the GAIA device.
     */
    public enum Message {
        /**
         * When we received a GAIA packet from the GAIA device to broadcast to the application.
         */
        PACKET,
        /**
         * To inform the application the GAIA device is now connected to the Android device.
         */
        CONNECTED,
        /**
         * To inform the application an error occurs on the communication with the GAIA device.
         */
        ERROR,
        /**
         * To inform the application about a message for debug.
         */
        DEBUG,
        /**
         * To inform the application the GAIA device is now disconnected to the Android device.
         */
        DISCONNECTED,
        /**
         * To inform the application the library is streaming messages to the GAIA device.
         */
        STREAM;

        private static final Message[] values = Message.values();

        public static Message valueOf(int what) {

            if (what < 0 || what >= values.length)
                return null;

            return values[what];
        }
    }

    /**
     * All types of transports which could be used to communicate with the device.
     */
    public enum Transport {
        BT_SPP, BT_GAIA
    }

    // End of public fields

    private static final String TAG = "GaiaLink";
    private static boolean mDebug = true;

    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID GAIA_UUID = UUID.fromString("00001107-D102-11E1-9B23-00025B00A5A5");

    @SuppressWarnings("FieldCanBeLocal")
    private final int MAX_BUFFER = 1024;
    private boolean mVerbose = false;
    private boolean mDfuBusy = false;

    private BluetoothAdapter mBTAdapter = null;
    private BluetoothDevice mBTDevice = null;

    private BluetoothSocket mBTSocket = null;

    private InputStream mInputStream = null;
    private Reader mReader;
    private Handler mReceiveHandler = null;
    private final Handler mLogHandler = null;

    private Transport mTransport = Transport.BT_GAIA;
    private boolean mIsConnected = false;

    /**
     * Instance of this object.
     */
    private static GaiaLink mInstance;

    /**
     * To retrieve the instance for this class.
     *
     * @return The instance for the GaiaLink class.
     */
    public static GaiaLink getInstance() {
        if (mInstance == null) {
            mInstance = new GaiaLink();
        }
        return mInstance;
    }

    /**
     * Class constructor.<br/> Use the getInstance method.
     */
    private GaiaLink() {
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Returns the used transport.
     * 
     * @return The transport used to connect to a device.
     */
    public Transport getTransport() {
        return mTransport;
    }

    /**
     * Establishes an outbound connection to the specified device.
     * 
     * @param device
     *            Bluetooth or IP address of the remote device depending on the transport.
     *
     */
    public void connect(BluetoothDevice device, Transport transport) {

        if (mIsConnected) {
            handleError("connect: already connected.", GaiaError.TypeException.ALREADY_CONNECTED);
        }

        else if (device == null || transport == null) {
            handleError("connect: at least one argument is null.", GaiaError.TypeException.ILLEGAL_ARGUMENT);
        }

        else {
            mTransport = transport;

            if (mTransport.equals(Transport.BT_SPP) || mTransport.equals(Transport.BT_GAIA)) {
                connectBluetooth(device);
            }
        }
    }

    /**
     * Set the debug level which controls how verbose we are with debugging information sent to logcat.
     * 
     * @param level
     *            The debug level. Set to zero for no debug logging, 1 for standard debug messages, 2 for verbose debug
     *            messages.
     */
    @SuppressWarnings("unused")
    public void setDebugLevel(int level) {
        mDebug = level > 0;
        mVerbose = level > 1;
    }

    /**
     * Disconnects from the remote device.
     */
    public void disconnect() {
        mIsConnected = false;
        mDfuBusy = false;

        if (mTransport.equals(Transport.BT_GAIA) || mTransport.equals(Transport.BT_SPP)) {
            disconnectBluetooth();
        }
    }

    /**
     * Sends unframed data to the remote device
     *
     * @param buffer
     *            The data to send
     */
    public void sendRaw(byte[] buffer) throws IOException {
        sendRaw(buffer, buffer.length);
    }

    /**
     * Sends unframed data to the remote device
     *
     * @param buffer
     *            The data to send
     * @param count
     *            The number of bytes to send
     */
    public void sendRaw(byte[] buffer, int count) throws IOException {
        OutputStream o = mBTSocket.getOutputStream();
        o.write(buffer, 0, count);
        o.flush();

        if (mReceiveHandler != null) {
            mReceiveHandler.obtainMessage(Message.STREAM.ordinal(), count).sendToTarget();
        }
    }

    /**
     * Sends a Gaia command to the remote device.
     * 
     * @param vendorId
     *            The vendor identifier qualifying the command.
     * @param commandId
     *            The command identifier.
     * @param payload
     *            Array of command-specific bytes.
     * @param payloadLength
     *            The number of payload bytes to send.
     */
    @SuppressWarnings("WeakerAccess")
    public void sendCommand(int vendorId, int commandId, byte[] payload, int payloadLength) {
        byte[] data;
        try {
            data = Gaia.frame(vendorId, commandId, payload, payloadLength);

            //noinspection ConstantConditions
            if (mLogHandler != null) {
                String text = "\u2192 " + Gaia.hexw(vendorId) + " " + Gaia.hexw(commandId);

                for (byte aPayload : payload) {
                    text += " " + Gaia.hexb(aPayload);
                }

                if (mDebug)
                    Log.d(TAG, text);
                mLogHandler.obtainMessage(Message.DEBUG.ordinal(), text).sendToTarget();
            }

            sendCommandData(data, commandId);
        }
        catch (GaiaFrameException e) {
            handleException("sendCommand", GaiaError.TypeException.SENDING_FAILED, e, commandId);
        }
    }

    /**
     * Sends a Gaia command to the remote device.
     * 
     * @param vendorId
     *            The vendor identifier qualifying the command.
     * @param commandId
     *            The command identifier.
     * @param payload
     *            Array of command-specific bytes.
     */
    public void sendCommand(int vendorId, int commandId, byte[] payload) {
        if (payload == null)
            sendCommand(vendorId, commandId);

        else
            sendCommand(vendorId, commandId, payload, payload.length);
    }

    /**
     * Sends a Gaia command to the remote device.
     * 
     * @param vendorId
     *            The vendor identifier qualifying the command.
     * @param commandId
     *            The command identifier.
     * @param param
     *            Command-specific integers.
     */
    public void sendCommand(int vendorId, int commandId, int... param) {
        if (param == null || param.length == 0) {
            byte[] data;
            try {
                data = Gaia.frame(vendorId, commandId);

                //noinspection ConstantConditions
                if (mLogHandler != null) {
                    String text = "\u2192 " + Gaia.hexw(vendorId) + " " + Gaia.hexw(commandId);
                    Log.d(TAG, text);
                    mLogHandler.obtainMessage(Message.DEBUG.ordinal(), text).sendToTarget();
                }

                sendCommandData(data, commandId);
            }
            catch (GaiaFrameException e) {
                handleException("sendCommand", GaiaError.TypeException.SENDING_FAILED, e, commandId);
            }
        }

        else {
            // Convenient but involves copying the payload twice. It's usually short.
            byte[] payload;
            payload = new byte[param.length];

            for (int idx = 0; idx < param.length; ++idx)
                payload[idx] = (byte) param[idx];

            sendCommand(vendorId, commandId, payload);
        }
    }

    /**
     * Sends a Gaia enable-style command to the remote device.
     * 
     * @param vendorId
     *            The vendor identifier qualifying the command.
     * @param commandId
     *            The command identifier.
     * @param enable
     *            Enable (true) or disable (false).
     */
    @SuppressWarnings("SameParameterValue")
    public void sendCommand(int vendorId, int commandId, boolean enable) {
        sendCommand(vendorId, commandId, enable ? Gaia.FEATURE_ENABLED : Gaia.FEATURE_DISABLED);
    }

    /**
     * Sends a Gaia acknowledgement to the remote device.
     * 
     * @param vendorId
     *            The vendor identifier qualifying the command.
     * @param commandId
     *            The command identifier.
     * @param status
     *            The status of the command.
     * @param param
     *            Acknowledgement-specific integers.
     */
    @SuppressWarnings("WeakerAccess")
    public void sendAcknowledgement(int vendorId, int commandId, Gaia.Status status, int... param) {
        // Convenient but involves copying the payload twice. It's usually short.
        byte[] payload;

        if (param == null)
            payload = new byte[1];

        else {
            payload = new byte[param.length + 1];

            for (int idx = 0; idx < param.length; ++idx)
                payload[idx + 1] = (byte) param[idx];
        }

        payload[0] = (byte) status.ordinal();
        sendCommand(vendorId, commandId | Gaia.ACK_MASK, payload);
    }

    /**
     * Sends a Gaia acknowledgement to the remote device.
     *
     * @param packet
     *            The packet which contains the vendorId and the command id for this acknowledgment.
     * @param status
     *            The status of the command.
     */
    public void sendAcknowledgement(GaiaPacket packet, Status status) {
        sendAcknowledgement(packet.getVendorId(), packet.getCommandId(), status);
    }

    /**
     * Sends a Gaia acknowledgement to the remote device.
     *
     * @param packet
     *            The packet which contains the vendorId and the command id for this acknowledgment.
     * @param status
     *            The status of the command.
     *
     * @param payload
     *            Any complementary information for this acknowledgment packet.
     */
    public void sendAcknowledgement(GaiaPacket packet, Status status, int... payload) {
        sendAcknowledgement(packet.getVendorId(), packet.getCommandId(), status, payload);
    }

    /**
     * Requests notification of the given event
     * 
     * @param event
     *            The Event for which notifications are to be raised
     */
    @SuppressWarnings("SameParameterValue")
    public void registerNotification(int vendorID, Gaia.EventId event) throws IllegalArgumentException {
        byte[] args;

        switch (event) {
        case START:
        case DEVICE_STATE_CHANGED:
        case DEBUG_MESSAGE:
        case BATTERY_CHARGED:
        case CHARGER_CONNECTION:
        case CAPSENSE_UPDATE:
        case USER_ACTION:
        case SPEECH_RECOGNITION:
        case AV_COMMAND:
        case REMOTE_BATTERY_LEVEL:
        case VMU_PACKET:
            args = new byte[1];
            break;

        default:
            handleException("registerNotification", GaiaError.TypeException.ILLEGAL_ARGUMENT, null,
                    Gaia.COMMAND_REGISTER_NOTIFICATION);
            return;
        }

        args[0] = (byte) event.ordinal();
        sendCommand(vendorID, Gaia.COMMAND_REGISTER_NOTIFICATION, args);
    }

    /**
     * Requests notification of the given event
     * 
     * @param event
     *            The Event for which notifications are to be raised
     * @param level
     *            The level at which events are to be raised
     */
    public void registerNotification(int vendorID, Gaia.EventId event, int level) {
        byte[] args;

        switch (event) {
        case RSSI_LOW_THRESHOLD:
        case RSSI_HIGH_THRESHOLD:
            args = new byte[2];
            args[1] = (byte) level;
            vendorID = Gaia.VENDOR_CSR;
            break;

        case BATTERY_LOW_THRESHOLD:
        case BATTERY_HIGH_THRESHOLD:
            args = new byte[3];
            args[1] = (byte) (level >>> 8);
            args[2] = (byte) level;
            vendorID = Gaia.VENDOR_CSR;
            break;

        case PIO_CHANGED:
            args = new byte[5];
            args[1] = (byte) (level >>> 24);
            args[2] = (byte) (level >>> 16);
            args[3] = (byte) (level >>> 8);
            args[4] = (byte) level;
            break;

        default:
            handleException("registerNotification", GaiaError.TypeException.ILLEGAL_ARGUMENT, null,
                    Gaia.COMMAND_REGISTER_NOTIFICATION);
            return;
        }

        args[0] = (byte) event.ordinal();
        sendCommand(vendorID, Gaia.COMMAND_REGISTER_NOTIFICATION, args);
    }

    /**
     * Requests notification of the given event
     * 
     * @param event
     *            The Event for which notifications are to be raised
     * @param level1
     *            The first level at which events are to be raised
     * @param level2
     *            The second level at which events are to be raised
     */
    public void registerNotification(Gaia.EventId event, int level1, int level2) {
        byte[] args;

        switch (event) {
        case RSSI_LOW_THRESHOLD:
        case RSSI_HIGH_THRESHOLD:
            args = new byte[3];
            args[1] = (byte) level1;
            args[2] = (byte) level2;
            break;

        case BATTERY_LOW_THRESHOLD:
        case BATTERY_HIGH_THRESHOLD:
            args = new byte[5];
            args[1] = (byte) (level1 >>> 8);
            args[2] = (byte) level1;
            args[3] = (byte) (level2 >>> 8);
            args[4] = (byte) level2;
            break;

        default:
            handleException("registerNotification", GaiaError.TypeException.ILLEGAL_ARGUMENT, null,
                    Gaia.COMMAND_REGISTER_NOTIFICATION);
            return;
        }

        args[0] = (byte) event.ordinal();
        sendCommand(Gaia.VENDOR_CSR, Gaia.COMMAND_REGISTER_NOTIFICATION, args);
    }

    /**
     * Requests the status of notifications for the given event
     * 
     * @param event
     *            The Event for which the status is requested
     */
    public void getNotification(int vendorID, Gaia.EventId event) {
        byte[] args = new byte[1];
        args[0] = (byte) event.ordinal();
        sendCommand(vendorID, Gaia.COMMAND_GET_NOTIFICATION, args);
    }

    /**
     * Cancels notification of the given event
     * 
     * @param event
     *            The Event for which notifications are no longer to be raised.
     */
    @SuppressWarnings("SameParameterValue")
    public void cancelNotification(int vendorID, Gaia.EventId event) {
        byte[] args = new byte[1];
        args[0] = (byte) event.ordinal();
        sendCommand(vendorID, Gaia.COMMAND_CANCEL_NOTIFICATION, args);
    }

    /**
     * Sets the target for Gaia messages received from the remote device.
     * 
     * @param handler
     *            The Handler for Gaia messages received from the remote device.
     */
    public void setReceiveHandler(Handler handler) {
        mReceiveHandler = handler;
    }

    /**
     * Returns the friendly name of the remote device or null if there is none.
     * 
     * @return Friendly name as a string.
     */
    public String getName() {
        return mBTDevice.getName();
    }

    /**
     * Thread to connect the Bluetooth socket and start the thread that reads from the socket.
     */
    private class Connector extends Thread {
        public void run() {
            try {
                mBTAdapter.cancelDiscovery();
                mBTSocket.connect();
                mInputStream = mBTSocket.getInputStream();
                mReader = new Reader();
                mReader.start();
            }

            catch (Exception e) {
                handleException("Connector", GaiaError.TypeException.CONNECTION_FAILED, e);
            }
        }
    }

    /**
     * Thread to read incoming packets from SPP, GAIA or UDP.
     */
    private class Reader extends Thread {
        final byte[] packet = new byte[Gaia.MAX_PACKET];
        int flags;
        int packet_length = 0;
        int expected = Gaia.MAX_PAYLOAD;

        boolean going;

        public void run() {
            if (mTransport.equals(Transport.BT_GAIA) || mTransport.equals((Transport.BT_SPP))) {
                runSppReader();
            }

            if (mReceiveHandler == null)
                Log.e(TAG, "reader: no receive handler");
            else {
                mIsConnected = false;
                mReceiveHandler.obtainMessage(Message.DISCONNECTED.ordinal()).sendToTarget();
            }
        }

        private void runSppReader() {
            byte[] buffer = new byte[MAX_BUFFER];
            int bytes;

            Log.i(TAG, "runSppReader start...");

            mReceiveHandler.obtainMessage(Message.CONNECTED.ordinal(), mBTDevice.getAddress()).sendToTarget();
            mIsConnected = true;
            going = true;

            while (going) {
                try {
                    bytes = mInputStream.read(buffer);

                    if (bytes < 0) {
                        going = false;
                    }
                    else {
                        scanStream(buffer, bytes);
                    }
                }
                catch (IOException e) {
                    handleError("RunSPPReader failed: " + e.toString(), GaiaError.TypeException.RECEIVING_FAILED);
                    going = false;
                }
            }

        }

        private void scanStream(byte[] buffer, int length) {
            for (int i = 0; i < length; ++i) {
                if ((packet_length > 0) && (packet_length < Gaia.MAX_PACKET)) {
                    packet[packet_length] = buffer[i];

                    if (packet_length == Gaia.OFFS_FLAGS)
                        flags = buffer[i];

                    else if (packet_length == Gaia.OFFS_PAYLOAD_LENGTH) {
                        expected = buffer[i] + Gaia.OFFS_PAYLOAD + (((flags & Gaia.FLAG_CHECK) != 0) ? 1 : 0);
                        if (mVerbose)
                            Log.d(TAG, "expect " + expected);
                    }

                    ++packet_length;

                    if (packet_length == expected) {
                        if (mVerbose)
                            Log.d(TAG, "got " + expected);

                        if (mReceiveHandler == null) {
                            if (mDebug)
                                Log.e(TAG, "No receiver");
                        }
                        else {
                            GaiaPacket command = new GaiaPacket(packet, packet_length);
                            logCommand(command);

                            if (command.getEvent() == Gaia.EventId.START && !mIsConnected) {
                                if (mDebug)
                                    Log.i(TAG, "connection starts");
                                mReceiveHandler.obtainMessage(Message.CONNECTED.ordinal(), mBTDevice.getAddress())
                                        .sendToTarget();
                                mIsConnected = true;
                            }

                            else {
                                if (mDebug)
                                    Log.i(TAG, "received command 0x" + Gaia.hexw(command.getCommand()));
                                mReceiveHandler.obtainMessage(Message.PACKET.ordinal(), command).sendToTarget();
                            }
                        }

                        packet_length = 0;
                        expected = Gaia.MAX_PAYLOAD;
                    }
                } else if (buffer[i] == Gaia.SOF)
                    packet_length = 1;
            }
        }
    }

    /**
     * Obtain the BluetoothDevice object.
     * 
     * @return BluetoothDevice object.
     */
    public BluetoothDevice getBluetoothDevice() {
        return mBTDevice;
    }

    /**
     * To know if we are connected to a BluetoothDevice, using this library.
     * 
     * @return true if we are connected, else otherwise.
     */
    public boolean isConnected() {
        return mIsConnected;
    }

    /**
     * Log a hex string representation of a commands payload bytes to the log handler.
     *
     * @param command
     *            The command to log.
     */
    private void logCommand(GaiaPacket command) {
        //noinspection ConstantConditions
        if (mLogHandler != null) {
            String text = "\u2190 " + Gaia.hexw(command.getVendorId()) + " " + Gaia.hexw(command.getCommandId());

            if (command.getPayload() != null)
                for (int i = 0; i < command.getPayload().length; ++i)
                    text += " " + Gaia.hexb(command.getPayload()[i]);

            if (mDebug)
                Log.d(TAG, text);
            mLogHandler.obtainMessage(Message.DEBUG.ordinal(), text).sendToTarget();
        }
    }

    /**
     * Write data to the Bluetooth or datagram socket.
     *
     * @param data
     *            Array of bytes to send.
     */
    private void sendCommandData(byte[] data, int commandId) {
        if (!mDfuBusy) {
            if (mTransport.equals(Transport.BT_SPP) || mTransport.equals(Transport.BT_GAIA)) {
                if (mBTSocket == null) {
                    handleError("sendCommandData: not connected.", GaiaError.TypeException.NOT_CONNECTED);
                }

                if (mDebug)
                    Log.i(TAG, "send command 0x" + Gaia.hexw(commandId));
                try {
                    mBTSocket.getOutputStream().write(data);
                }
                catch (IOException e) {
                    handleException("sendCommandData", GaiaError.TypeException.SENDING_FAILED, e, commandId);
                }
            }
        }
    }

    /**
     * Disconnect Bluetooth device.
     */
    private void disconnectBluetooth() {
        if (mDebug)
            Log.i(TAG, "disconnect BT");

        if (mBTSocket != null) {
            // HTC SPP disconnection is buggy; ask the other end to do it for us
            // sendCommand(VENDOR_CSR, META_DISCONNECT);
            try {
                mReader = null;

                if (mInputStream != null)
                    mInputStream.close();

                mBTSocket.getOutputStream().close();
                mBTSocket.close();

                mBTSocket = null;
                mBTDevice = null;

                mIsConnected = false;
            }
            catch (IOException e) {
                if (mDebug)
                    Log.e(TAG, e.toString());
            }
        }
    }

    /**
     * Check for RFCOMM security.
     *
     * @return True if RFCOMM security is implemented.
     */
    private boolean btIsSecure() {
        // Establish if RFCOMM security is implemented, in which case we'll
        // use the insecure variants of the RFCOMM functions
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1;
    }

    /**
     * Create a Bluetooth connection.
     *
     * @param device
     *            Remote Bluetooth device to connect to.
     */
    private void connectBluetooth(BluetoothDevice device) {

        if (!getBluetoothAvailable()) {
            handleError("connectBluetooth: Bluetooth not available.", GaiaError.TypeException.BLUETOOTH_NOT_SUPPORTED);
        }
        else if (!BluetoothAdapter.checkBluetoothAddress(device.getAddress())) {
            handleError("connectBluetooth: the given device has a wrong address.",
                    GaiaError.TypeException.DEVICE_UNKNOWN_ADDRESS);
        }

        else {
            if (mDebug)
                Log.i(TAG, "connect BT " + device.getAddress());

            mBTDevice = device;

            switch (mTransport) {
            case BT_GAIA:
                mBTSocket = createSocket(GAIA_UUID);
                break;

            case BT_SPP:
                mBTSocket = createSocket(SPP_UUID);
                break;

            default:
                handleError("connectBluetooth: unsupported transport.", GaiaError.TypeException.UNSUPPORTED_TRANSPORT);
            }

            Connector connector = new Connector();
            connector.start();
        }
    }

    /**
     * Create the RFCOMM bluetooth socket.
     *
     * @param uuid
     *            UUID to create the socket with.
     *
     * @return BluetoothSocket object.
     */
    @TargetApi(10)
    private BluetoothSocket createSocket(UUID uuid) {
        BluetoothSocket socket = null;
        try {
            if (btIsSecure()) {
                socket = mBTDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            }
            else {
                socket = mBTDevice.createRfcommSocketToServiceRecord(uuid);
            }
        }
        catch (IOException e) {
            if (mDebug)
                Log.w(TAG, "createSocket: " + e.toString());

            try {
                // This is a workaround that reportedly helps on some older devices like HTC Desire, where using
                // the standard createRfcommSocketToServiceRecord() method always causes connect() to fail.
                //noinspection RedundantArrayCreation
                Method method = mBTDevice.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
                // noinspection UnnecessaryBoxing
                socket = (BluetoothSocket) method.invoke(mBTDevice, Integer.valueOf(1));
                return socket;
            }
            catch (Exception e1) {
                // NoSuchMethodException from method getMethod: impossible to retrieve the method.
                // IllegalArgumentException from method invoke: problem with arguments which don't match with
                // expectations.
                // IllegalAccessException from method invoke: if invoked object is not accessible.
                // InvocationTargetException from method invoke: Exception thrown by the invoked method.
                if (mDebug)
                    handleException("createSocket", GaiaError.TypeException.CONNECTION_FAILED, e1);
            }
        }

        return socket;
    }

    /**
     * Returns the availability of Bluetooth
     *
     * @return true if the local Bluetooth adapter is available
     */
    private boolean getBluetoothAvailable() {
        /* Bluetooth is available only if the adapter exists */
        return mBTAdapter != null;
    }

    /**
     * To handle exceptions when it needs to inform the related application.
     *
     * @param name
     *            The method name from where this method is called.
     * @param type
     *            The type of errors to inform the application.
     * @param exception
     *            If there is an exception, add it here, otherwise "null".
     */
    @SuppressWarnings("SameParameterValue")
    private void handleException(String name, GaiaError.TypeException type, Exception exception) {
        if (mDebug) {
            Log.e(TAG, name + ": " + exception.toString());
        }
        if (mReceiveHandler != null) {
            GaiaError error = new GaiaError(type, exception);
            mReceiveHandler.obtainMessage(Message.ERROR.ordinal(), error).sendToTarget();
        }
    }

    /**
     * To handle exceptions when it needs to inform the related application.
     *
     * @param name
     *            The method name from where this method is called.
     * @param type
     *            The type of errors to inform the application.
     * @param exception
     *            If there is an exception, add it here, otherwise "null".
     * @param command
     *            The command where the error occurs.
     */
    private void handleException(String name, GaiaError.TypeException type, Exception exception, int command) {
        if (mDebug) {
            Log.e(TAG, name + ": " + exception.toString());
        }
        if (mReceiveHandler != null) {
            GaiaError error = new GaiaError(type, exception, command);
            mReceiveHandler.obtainMessage(Message.ERROR.ordinal(), error).sendToTarget();
        }
    }

    /**
     * To handle errors when it needs to inform the related application.
     *
     * @param message
     *            The message to display for a log.
     * @param type
     *            The type of errors to inform the application.
     */
    private void handleError(String message, GaiaError.TypeException type) {
        if (mDebug) {
            Log.e(TAG, message);
        }
        if (mReceiveHandler != null) {
            GaiaError error = new GaiaError(type);
            mReceiveHandler.obtainMessage(Message.ERROR.ordinal(), error).sendToTarget();
        }
    }
}