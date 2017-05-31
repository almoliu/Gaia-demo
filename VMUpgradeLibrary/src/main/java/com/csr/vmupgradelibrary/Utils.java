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
 * This class contains all useful methods for this library.
 */
@SuppressWarnings("unused")
final class Utils {
    /**
     * The number of bits in a byte.
     */
    private static final int BITS_IN_BYTE = 8;
    /**
     * The number of bytes to define a long.
     */
    private static final int BYTES_IN_LONG = 8;

    /**
     * Extract a <code>long</code> field from an array.
     * 
     * @param source
     *            The array to extract from.
     * @param offset
     *            Offset within source array.
     * @param length
     *            Number of bytes to use (maximum 8).
     * @param reverse
     *            True if bytes should be interpreted in reverse (little endian) order.
     * @return The extracted long.
     */
    static long extractLongField(byte[] source, int offset, int length, boolean reverse) {
        if (length < 0 | length > BYTES_IN_LONG)
            throw new IndexOutOfBoundsException("Length must be between 0 and " + BYTES_IN_LONG);
        long result = 0;
        int shift = (length - 1) * BITS_IN_BYTE;

        if (reverse) {
            for (int i = offset + length - 1; i >= offset; i--) {
                result |= ((source[i] & 0xFFL) << shift);
                shift -= BITS_IN_BYTE;
            }
        }
        else {
            for (int i = offset; i < offset + length; i++) {
                result |= ((source[i] & 0xFFL) << shift);
                shift -= BITS_IN_BYTE;
            }
        }
        return result;
    }

    /**
     * Take a <code>long</code> value, split it into bytes and insert it into the specified array. If a length < 8 is
     * specified then most significant bytes are truncated.
     * 
     * @param sourceValue
     *            The value to insert.
     * @param target
     *            Destination array.
     * @param targetOffset
     *            Offset in target array to insert at.
     * @param length
     *            Number of bytes from the <code>long</code> to insert.
     * @param reverse
     *            True if bytes should be put into array in reverse (little endian) order.
     */
    static void putField(long sourceValue, byte[] target, int targetOffset, int length, boolean reverse) {
        if (length < 0 | length > BYTES_IN_LONG)
            throw new IndexOutOfBoundsException("Length must be between 0 and " + BYTES_IN_LONG);

        if (reverse) {
            int shift = 0;
            int j = 0;
            for (int i = length - 1; i >= 0; i--) {
                long mask = 0xFFL << shift;
                target[j + targetOffset] = (byte) ((sourceValue & mask) >> shift);
                shift += BITS_IN_BYTE;
                j++;
            }
        }
        else {
            int shift = (length - 1) * BITS_IN_BYTE;
            for (int i = 0; i < length; i++) {
                long mask = 0xFFL << shift;
                target[i + targetOffset] = (byte) ((sourceValue & mask) >> shift);
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
    static void putArrayField(byte[] source, int sourceOffset, byte[] target, int targetOffset, int length,
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
     * Convert a byte array to a human readable String.
     * 
     * @param value
     *            The byte array.
     * @return String object containing values in byte array formatted as hex.
     */
    public static String hexString(byte[] value) {
        if (value == null)
            return "null";
        String out = "";
        for (byte b : value) {
            out += String.format("%02x ", b);
        }
        return out;
    }
}
