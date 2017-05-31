/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.csr.gaiacontrol.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * This class contains all useful methods for this application.
 */
public final class Utils {


    public static final int BYTES_IN_INT = 4;
    private static final int BITS_IN_BYTE = 8;
    public static final int BITS_IN_HEXADECIMAL = 4;
    private static final int BYTES_IN_SHORT = 2;

    /**
     * Extract an <code>int</code> field from an array.
     * @param source The array to extract from.
     * @param offset Offset within source array.
     * @param length Number of bytes to use (maximum 4).
     * @param reverse True if bytes should be interpreted in reverse (little endian) order.
     * @return The extracted integer.
     */
    @SuppressWarnings("SameParameterValue")
    public static int extractIntField(byte [] source, int offset, int length, boolean reverse) {
        if (length < 0 | length > BYTES_IN_INT) throw new IndexOutOfBoundsException("Length must be between 0 and " + BYTES_IN_INT);
        int result = 0;
        int shift = (length-1) * BITS_IN_BYTE;

        if (reverse) {
            for (int i = offset+length-1; i >= offset; i--) {
                result |= ((source[i] & 0xFF) << shift);
                shift -= BITS_IN_BYTE;
            }
        }
        else {
            for (int i = offset; i < offset+length; i++) {
                result |= ((source[i] & 0xFF) << shift);
                shift -= BITS_IN_BYTE;
            }
        }
        return result;
    }

    /**
     * Extract a <code>short</code> field from an array.
     * @param source The array to extract from.
     * @param offset Offset within source array.
     * @param length Number of bytes to use (maximum 2).
     * @param reverse True if bytes should be interpreted in reverse (little endian) order.
     * @return The extracted integer.
     */
    public static short extractShortField(byte [] source, int offset, int length, @SuppressWarnings("SameParameterValue") boolean reverse) {
        if (length < 0 | length > BYTES_IN_SHORT) throw new IndexOutOfBoundsException("Length must be between 0 and " + BYTES_IN_SHORT);
        short result = 0;
        int shift = (length-1) * BITS_IN_BYTE;

        if (reverse) {
            for (int i = offset+length-1; i >= offset; i--) {
                result |= ((source[i] & 0xFF) << shift);
                shift -= BITS_IN_BYTE;
            }
        }
        else {
            for (int i = offset; i < offset+length; i++) {
                result |= ((source[i] & 0xFF) << shift);
                shift -= BITS_IN_BYTE;
            }
        }
        return result;
    }

    /**
     * Convert a byte array to a human readable String.
     * 
     * @param value
     *            The byte array.
     * @return String object containing values in byte array formatted as hex.
     */
    public static String getStringFromBytes(byte[] value) {
        if (value == null)
            return "null";
        String out = "";
        for (byte b : value) {
            out += String.format("0x%02x ", b);
        }
        return out;
    }

    /**
     * Take an <code>int</code> value, split it into bytes and insert it into the specified array.
     * If a length < 4 is specified then most significant bytes are truncated.
     * @param sourceValue The value to insert.
     * @param target Destination array.
     * @param targetOffset Offset in target array to insert at.
     * @param length Number of bytes from the <code>int</code> to insert.
     * @param reverse True if bytes should be put into array in reverse (little endian) order.
     */
    @SuppressWarnings("SameParameterValue")
    public static void putField(int sourceValue, byte [] target, int targetOffset, int length, boolean reverse) {
        if (length < 0 | length > BYTES_IN_INT) throw new IndexOutOfBoundsException("Length must be between 0 and " + BYTES_IN_INT);

        if (reverse) {
            int shift = 0;
            int j = 0;
            for (int i = length-1; i >= 0; i--) {
                int mask = 0xFF << shift;
                target[j+targetOffset] = (byte)((sourceValue & mask) >> shift);
                shift += BITS_IN_BYTE;
                j++;
            }
        }
        else {
            int shift = (length-1) * BITS_IN_BYTE;
            for (int i = 0; i < length; i++) {
                int mask = 0xFF << shift;
                target[i+targetOffset] = (byte)((sourceValue & mask) >> shift);
                shift -= BITS_IN_BYTE;
            }
        }
    }

    /**
     * Copy an array from a source to a target.
     *
     * @param source
     *            The source array.
     * @param sourceOffset
     *            Offset within source array.
     * @param target
     *            The target array.
     * @param targetOffset
     *            Offset within target array.
     * @param length
     *            Number of bytes to copy.
     * @param reverse
     *            True if bytes should be put into array in reverse (little endian) order.
     */
    @SuppressWarnings("SameParameterValue")
    public static void putArrayField(byte[] source, int sourceOffset, byte[] target, int targetOffset, int length,
                              boolean reverse) {
        if (reverse) {
            int j = sourceOffset + length - 1;
            for (int i = targetOffset; i < targetOffset + length; i++) {
                target[i] = source[j];
                j--;
            }
        }
        else {
            System.arraycopy(source, sourceOffset, target, targetOffset, length);
        }
    }

    /**
     * To obtain the MD5 checksum from a file.
     *
     * @param file
     *                  The path to the file which we want the MD5 checksum.
     */
    public static byte[] getMD5FromFile(File file) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0) {
                    digest.update(buffer, 0, numRead);
                }
            }

            return digest.digest();
        }
        catch (Exception e) {
            return new byte[0];
        }
        finally {
            if (inputStream != null) {
                //noinspection EmptyCatchBlock
                try {
                    inputStream.close();
                }
                catch (Exception e) {
                }
            }
        }
    }

    /**
     * To retrieve an array of bytes from a file.
     * 
     * @param file
     *              The file to obtain the bytes.
     */
    public static byte[] getBytesFromFile(File file) {

        byte[] result;

        try {
            InputStream inputStream = new FileInputStream(file);
            int length = Long.valueOf(file.length()).intValue();
            result = new byte[length];
            //noinspection ResultOfMethodCallIgnored
            inputStream.read(result);
            inputStream.close();
        }
        catch (Exception e) {
            return null;
        }
        return result;

    }

    /**
     * Get 16-bit hexadecimal string representation of byte.
     *
     * @param i
     *            The value.
     *            
     * @return Hex value as a string.
     */
    public static String getIntToHexadecimal(int i) {
        return String.format("%04X", i & 0xFFFF);
    }

    /**
     * To retrieve a time format from a long in millisecond.
     *
     * @param time
     *              the time in ms.
     *
     * @return
     *          The time with a format like --h or --m or --s depending on which biggest kind is different of 0.
     */
    public static String getStringFromTime(long time) {
        long seconds = time / 1000;

        if (seconds > 60) {
            long minutes = seconds / 60;

            if (minutes > 60) {
                long hours = minutes / 60;
                return hours + "h";
            }
            else {
                return minutes + "min";
            }
        }
        else {
            return seconds + "s";
        }

    }
}
