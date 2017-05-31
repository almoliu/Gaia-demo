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
package com.csr.vmupgradelibrary;

/**
 * This class allows to build a packet for the VM upgrade as defined in the VM upgrade documentation.
 */

@SuppressWarnings("unused")
public class VMUPacket {

    /*
     * The composition for a VMU packet:

         0 bytes  1        2         3         4        length+3
         +--------+--------+--------+ +--------+--------+
         | OPCODE |     LENGTH      | |     DATA...     |
         +--------+--------+--------+ +--------+--------+
     */

    /**
     * The number of bytes to define the packet length information.
     */
    private static final int NB_BYTES_LENGTH = 2;
    /**
     * The number of bytes to define the packet operation code information.
     */
    private static final int NB_BYTES_OPCODE = 1;
    /**
     * The offset for the operation code information.
     */
    private static final int OFFSET_OPCODE = 0;
    /**
     * The offset for the length information.
     */
    private static final int OFFSET_LENGTH = OFFSET_OPCODE + NB_BYTES_OPCODE;
    /**
     * The offset for the data information.
     */
    private static final int OFFSET_DATA = OFFSET_LENGTH + NB_BYTES_LENGTH;

    public static final int LENGTH_REQUIRED_INFORMATION = NB_BYTES_LENGTH + NB_BYTES_OPCODE;

    /**
     * The packet length information.
     */
    private final int mLength;
    /**
     * The packet operation code information.
     */
    private final int mOpCode;
    /**
     * The packet data information.
     */
    private final byte[] mData;

    /**
     * To create a new instance of VM Upgrade packet.
     * 
     * @param opCode
     *            the operation code for this packet.
     * @param length
     *            the data length for this packet.
     * @param data
     *            the date for this packet.
     */
    public VMUPacket(int opCode, int length, byte[] data) {
        this.mOpCode = opCode;
        this.mLength = length;
        this.mData = data;
    }

    /**
     * To build a packet from a bytes array as sending by a device.
     * 
     * @param data
     *            the packet sent by the device for VM upgrade information.
     * @return A new instance of a VMU packet built with the given data.
     */
    public static VMUPacket buildPacketFromBytes(byte[] data) {
        if (data.length >= 2) {
            int opCode = data[0];
            int length = data[OFFSET_LENGTH] << 8 | data[OFFSET_LENGTH + 1];
            byte[] packetData = new byte[length];

            System.arraycopy(data, OFFSET_DATA, packetData, 0, length);

            return new VMUPacket(opCode, length, packetData);
        }
        else {
            return new VMUPacket(-1, 0, null);
        }
    }

    /**
     * To get the bytes array corresponding to this VMU packet.
     * 
     * @return a bytes array built with these packet information.
     * 
     */
    public byte[] getBytes() {
        byte[] packet = new byte[mLength + OFFSET_DATA];
        packet[OFFSET_OPCODE] = (byte) mOpCode;

        packet[OFFSET_LENGTH] = (byte) (mLength >> 8);
        packet[OFFSET_LENGTH + 1] = (byte) mLength;

        if (mData != null && mData.length > 0) {
            Utils.putArrayField(mData, 0, packet, OFFSET_DATA, mLength, false);
        }

        return packet;
    }

    /**
     * To get the operation code.
     * 
     * @return the operation code.
     */
    public int getOpCode() {
        return mOpCode;
    }

    /**
     * To get the data length.
     * 
     * @return the data length.
     */
    public int getLength() {
        return mLength;
    }

    /**
     * To get the packet data.
     * 
     * @return the packet data.
     */
    public byte[] getData() {
        return mData;
    }

    /**
     * To get the first data of this packet.
     * 
     * @return the first data.
     */
    public byte getFirstData() {
        if (mData.length > 1)
            return mData[0];
        else
            return 0;
    }
}
