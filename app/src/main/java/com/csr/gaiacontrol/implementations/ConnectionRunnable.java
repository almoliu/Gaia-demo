/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/
package com.csr.gaiacontrol.implementations;

/**
 * This class defines how the application should act when the attempt to connect to a device is stopped by a timeout.
 */

public class ConnectionRunnable implements Runnable {

    /**
     * The number of attempts for this runnable.
     */
    private static final int MAX_CONNECTION_ATTEMPTS = 30;
    /**
     * The timeout for this runnable when it starts by an handler using the postDelayed method.
     */
    public static final int CONNECTION_TIMEOUT = 10000;

    /**
     * The number of attempts to connect to the device.
     */
    private static int mConnectionAttempts = 0;
    /**
     * The listener to interact with the activity in which this Runnable is attached.
     */
    private final IConnectionListener mListener;

    /**
     * The constructor of this class.
     *
     * @param listener
     *              The listener to attach to this Runnable.
     */
    public ConnectionRunnable (IConnectionListener listener) {
        mListener = listener;
    }

    @Override
    public void run() {
        mConnectionAttempts++;
        if (mConnectionAttempts <= MAX_CONNECTION_ATTEMPTS) {
            mListener.connect();
        }
        else {
            restart();
            mListener.connectFailed();
        }
    }

    /**
     * To restart counts for this Runnable.
     */
    public void restart() {
        mConnectionAttempts = 0;
    }

    /**
     * This interface allows the fragment to communicate information or call the activity on which it is attached.
     */
    public interface IConnectionListener {
        /**
         * To attempt a new connection to a device.
         */
        void connect();

        /**
         * When too many connections have been attempted.
         */
        void connectFailed();
    }
}
