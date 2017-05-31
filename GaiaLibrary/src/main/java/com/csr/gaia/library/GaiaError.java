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

/**
 * The class to represent an error or an exception coming from the library to broadcast it to the application using this
 * library.
 */

@SuppressWarnings("unused")
public class GaiaError {

    /**
     * The handled exception.
     */
    private final Exception mException;
    /**
     * The type of error which occurs.
     */
    private final TypeException mType;
    /**
     * If the error/exception is linked to a command, this field will be > 0.
     */
    private int mCommand = -1;

    /**
     * All types of errors and exception the library can communicate to the application.
     */
    public enum TypeException {
        /**
         * When attempt to connect but already connected to a device.
         */
        ALREADY_CONNECTED,
        /**
         * When attempt to connect, but bluetooth not supported by the device.
         */
        BLUETOOTH_NOT_SUPPORTED,
        /**
         * When attempt to connect but the given device has an unknown address.
         */
        DEVICE_UNKNOWN_ADDRESS,
        /**
         * When attempt to connect but the given transport used for connection is unsupported. Supported transports are:
         * RFCOMM & SPP.
         */
        UNSUPPORTED_TRANSPORT,
        /**
         * When attempt to connect but the connection failed.
         */
        CONNECTION_FAILED,
        /**
         * When a problem occurs with at least one of the given argument - null, not expected, etc.
         */
        ILLEGAL_ARGUMENT,
        /**
         * When attempt to send an information to the device and it's not working - the device seems to be not available.
         */
        SENDING_FAILED,
        /**
         * When attempt to interact with a device without being connected to.
         */
        NOT_CONNECTED,
        /**
         * When attempt to receive a message and failed.
         */
        RECEIVING_FAILED
    }

    /**
     * To build an error/exception just knowing the exception type. The exception will be create automatically depending
     * on the type.
     * 
     * @param type
     *            The exception type.
     */
    public GaiaError(TypeException type) {
        this.mType = type;
        this.mException = new Exception(this.getMessageTypeException());
    }

    /**
     * To build an error/exception using the exception type and the exception - to broadcast the exception message.
     * 
     * @param type
     *            the exception type.
     * @param exception
     *            the exception to broadcast.
     */
    public GaiaError(TypeException type, Exception exception) {
        this.mType = type;
        this.mException = exception;
    }

    /**
     * To build an error/exception using the type, the exception to broadcast and the command if it links to a command.
     * 
     * @param type
     *            the exception type.
     * @param exception
     *            the exception to broadcast.
     * @param command
     *            the linked command to the exception.
     */
    public GaiaError(TypeException type, Exception exception, int command) {
        this.mType = type;
        this.mException = exception;
        this.mCommand = command;
    }

    /**
     * Get the exception object linked to this error.
     * 
     * @return the exception object, built on the type or the broadcast exception caught by the library.
     */
    public String getStringException() {
        return mException.toString();
    }

    /**
     * Get the type of this error.
     * 
     * @return the exception type.
     */
    public TypeException getType() {
        return mType;
    }

    /**
     * Get the command where the exception or the error occurs during its execution. If there is no command linked to
     * this error, the value is -1.
     * 
     * @return the linked command.
     */
    public int getCommand() {
        return mCommand;
    }

    /**
     * To create the message for an exception when the library doesn't have any exception to broadcast. This method is
     * used by the constructor with the following signature: <code></code>.
     * 
     * @return
     *          The string to display about this error.
     */
    private String getMessageTypeException() {

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Exception: ");

        switch (mType) {
            case ALREADY_CONNECTED:
                strBuilder.append("a device is already connected.");
                break;
            case BLUETOOTH_NOT_SUPPORTED:
                strBuilder.append("this device does not support Bluetooth.");
                break;
            case CONNECTION_FAILED:
                strBuilder.append("connection to the device failed.");
                break;
            case DEVICE_UNKNOWN_ADDRESS:
                strBuilder.append("the given device has a wrong address.");
                break;
            case ILLEGAL_ARGUMENT:
                strBuilder.append("at least one of the given arguments doesn't match with expectations.");
                break;
            case SENDING_FAILED:
                strBuilder.append("Sending a message to a device failed.");
                break;
            case NOT_CONNECTED:
                strBuilder.append("No connected device.");
                break;
            case UNSUPPORTED_TRANSPORT:
                strBuilder.append("the given transport is unsupported.");
                break;
        }

        return strBuilder.toString();
    }
}
