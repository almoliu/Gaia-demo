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

import com.csr.gaia.library.Gaia.AsrResult;

/**
 * This class encapsulates a Gaia packet.
 */

@SuppressWarnings({"SameParameterValue", "WeakerAccess", "unused"})
public class GaiaPacket {
    private int mVendorId = Gaia.VENDOR_NONE;
    private int mCommandId = 0;
    private byte[] mPayload = null;

    /**
     * Constructor that builds a command from a byte sequence.
     * 
     * @param source
     *            Array of bytes to build the command from.
     */
    GaiaPacket(byte[] source) {
        buildPacket(source, source.length);
    }

    /**
     * Constructor that builds a command from a specified number of bytes in a byte sequence.
     * 
     * @param source
     *            Array of bytes to build the command from.
     * @param source_length
     *            Number of bytes from the array to use.
     */
    GaiaPacket(byte[] source, int source_length) {
        buildPacket(source, source_length);
    }

    /**
     * Combine two bytes at a particular offset in an array of bytes to make a 16-bit value.
     * 
     * @param array
     *            Array of bytes to retrieve the value from.
     * @param offset
     *            Offset within the array to get the value from.
     * @return 16-bit value.
     */
    private int getIntFromByteArray(byte[] array, int offset) {
        int value;

        try {
            value = ((array[offset] & 0xFF) << 8) | (array[offset + 1] & 0xFF);
        }

        catch (ArrayIndexOutOfBoundsException e) {
            value = 0;
        }

        return value;
    }

    /**
     * Build a GAIA command payload from a byte sequence.
     * 
     * @param source
     *            Array of bytes to build the command from.
     * @param sourceLength
     *            Number of bytes from the array to use.
     */
    private void buildPacket(byte[] source, int sourceLength) {
        int flags = source[Gaia.OFFS_FLAGS];
        int payloadLength = sourceLength - Gaia.OFFS_PAYLOAD;

        if ((flags & Gaia.FLAG_CHECK) != 0) {
            --payloadLength;
        }

        mVendorId = getIntFromByteArray(source, Gaia.OFFS_VENDOR_ID);
        mCommandId = getIntFromByteArray(source, Gaia.OFFS_COMMAND_ID);

        if (payloadLength > 0) {
            mPayload = new byte[payloadLength];
            //noinspection ManualArrayCopy
            for (int i = 0; i < payloadLength; ++i) {
                mPayload[i] = source[i + Gaia.OFFS_PAYLOAD];
            }
        }
    }

    /**
     * Check if this command has the ACK bit set.
     * 
     * @return True if the command is an acknowledgement.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean isAcknowledgement() {
        return (mCommandId & Gaia.ACK_MASK) != 0;
    }

    /**
     * Check if this command is a known CSR command.
     * 
     * @return True if command has vendor set to CSR.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean isKnownCommand() {
        return (mVendorId == Gaia.VENDOR_CSR);
    }

    /**
     * Check if this command is a known CSR command and also matches the specified value.
     * 
     * @param commandId
     *            The value to match the command against.
     *
     * @return true if the command exists.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean isKnownCommand(int commandId) {
        return isKnownCommand() && (mCommandId == commandId);
    }

    /**
     * Get the event found in byte zero of the payload if the packet is an event packet.
     * 
     * @return The event ID or null if it is not an event packet or if the packet does not contain any information.
     */
    public Gaia.EventId getEvent() {
        if (mPayload == null || mPayload.length == 0 || !isKnownCommand(Gaia.COMMAND_EVENT_NOTIFICATION))
            return null;

        return Gaia.EventId.valueOf(mPayload[0]);
    }

    /**
     * <p>Get the status byte from the payload of an acknowledgement packet.</p> <p>By convention in acknowledgement
     * packets the first byte contains the command status or 'result' of the command. Additional data may be presented
     * in the acknowledgement packet, as defined by individual commands.</p>
     * 
     * @return The status code as defined in Gaia.EventId. Null if this packet does not contain any payload or is not an
     *         acknowledgment.
     */
    public Gaia.Status getStatus() {
        if (mPayload == null || mPayload.length == 0 || !isAcknowledgement())
            return null;

        return Gaia.Status.valueOf(mPayload[0]);
    }

    /**
     * Get the entire payload.
     * 
     * @return Array of bytes containing the payload.
     */
    public byte[] getPayload() {
        return mPayload;
    }

    /**
     * Get a single byte from the payload at the specified offset.
     * 
     * @param offset
     *            Offset within the payload.
     * @return Value at the specified offset.
     */
    @SuppressWarnings("WeakerAccess")
    public int getByte(int offset) {
        int value;

        try {
            value = mPayload[offset];
        }

        catch (ArrayIndexOutOfBoundsException e) {
            value = 0;
        }

        return value;
    }

    /**
     * Get the byte at payload offset 1.
     * 
     * @return Value at offset 1.
     */
    @SuppressWarnings("WeakerAccess")
    public int getByte() {
        return getByte(1);
    }

    /**
     * Get the byte at the specified offset in the payload interpreted as a boolean value.
     * 
     * @param offset
     *            Offset within the payload to get the boolean value from.
     * @return True if the value at the specified offset is non zero.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean getBoolean(int offset) {
        return getByte(offset) != 0;
    }

    /**
     * Get the byte at the offset 1 in the payload interpreted as a boolean value.
     * 
     * @return True if the value is non zero.
     */
    public boolean getBoolean() {
        return getBoolean(1);
    }

    /**
     * To cast the result from the packet to a speech recognition result.
     * 
     * @return the result for the speech recognition.
     */
    public AsrResult getAsrResult() {
        return AsrResult.valueOf(getByte());
    }

    /**
     * Combine two bytes at a specified offset in the payload to make a 16-bit value.
     * 
     * @param offset
     *            Offset within the payload to get the value.
     * @return 16-bit value.
     */
    @SuppressWarnings("WeakerAccess")
    public int getShort(int offset) {
        return getIntFromByteArray(mPayload, offset);
    }

    /**
     * Get the 16-bit value at offset 1 in the payload.
     * 
     * @return 16-bit value at offset 1.
     */
    public int getShort() {
        return getShort(1);
    }

    /**
     * Combine two bytes at a specified offset in the payload to make a 32-bit value.
     * 
     * @param offset
     *            Offset within the payload to get the value.
     * @return 32-bit value.
     */
    public int getInt(int offset) {
        int value;

        try {
            value =
                    ((mPayload[offset] & 0xFF) << 24) | ((mPayload[offset + 1] & 0xFF) << 16)
                            | ((mPayload[offset + 2] & 0xFF) << 8) | (mPayload[offset + 3] & 0xFF);
        }

        catch (ArrayIndexOutOfBoundsException e) {
            value = 0;
        }

        return value;
    }

    /**
     * Get the vendor identifier for this command.
     * 
     * @return The vendor identifier.
     */
    public int getVendorId() {
        return mVendorId;
    }

    /**
     * Get the raw command ID for this command with the ACK bit stripped out.
     * 
     * @return The command ID without the acknowledgment.
     */
    public int getCommand() {
        return mCommandId & Gaia.COMMAND_MASK;
    }

    /**
     * Get the command ID including the ACK bit.
     * 
     * @return The command ID.
     */
    public int getCommandId() {
        return mCommandId;
    }
}
