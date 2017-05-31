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
package com.csr.vmupgradelibrary.codes;

/**
 * <p>This class gives all codes the board may send - after a command or asynchronously. These codes are encapsulated into an UPDATE_ERRORWARN_IND message.</p>
 * <ul>
 * <li>Errors are considered as fatal: the board will abort the process.</li>
 * <li>Warnings are considered as informational: the board will choose to abort or continue the process.</li>
 * </ul>
 */

@SuppressWarnings("WeakerAccess")
public final class ReturnCodes {

    public static final int ERROR_UNKNOWN_ID = 0x11;
    public static final int ERROR_BAD_LENGTH_DEPRECATED = 0x12;
    public static final int ERROR_WRONG_VARIANT = 0x13;
    public static final int ERROR_WRONG_PARTITION_NUMBER = 0x14;
    public static final int ERROR_PARTITION_SIZE_MISMATCH = 0x15;
    public static final int ERROR_PARTITION_TYPE_NOT_FOUND = 0x16;
    public static final int ERROR_PARTITION_OPEN_FAILED = 0x17;
    public static final int ERROR_PARTITION_WRITE_FAILED = 0x18;
    public static final int ERROR_PARTITION_CLOSE_FAILED_1 = 0x19;
    public static final int ERROR_SFS_VALIDATION_FAILED = 0x1A;
    public static final int ERROR_OEM_VALIDATION_FAILED = 0x1B;
    public static final int ERROR_UPDATE_FAILED = 0x1C;
    public static final int ERROR_APP_NOT_READY = 0x1D;
    public static final int ERROR_LOADER_ERROR = 0x1E;
    public static final int ERROR_UNEXPECTED_LOADER_MSG = 0x1F;
    public static final int ERROR_MISSING_LOADER_MSG = 0x20;
    public static final int ERROR_BATTERY_LOW = 0x21;
    public static final int ERROR_BAD_LENGTH_PARTITION_PARSE = 0x38;
    public static final int ERROR_BAD_LENGTH_TOO_SHORT = 0x39;
    public static final int ERROR_BAD_LENGTH_UPGRADE_HEADER = 0x3A;
    public static final int ERROR_BAD_LENGTH_PARTITION_HEADER = 0x3B;
    public static final int ERROR_BAD_LENGTH_SIGNATURE = 0x3C;
    public static final int ERROR_BAD_LENGTH_DATAHDR_RESUME = 0x3D;
    public static final int ERROR_PARTITION_CLOSE_FAILED_2 = 0x40;
    public static final int ERROR_PARTITION_CLOSE_FAILED_HEADER = 0x41;
    public static final int ERROR_PARTITION_TYPE_NOT_MATCHING = 0x48;
    public static final int ERROR_PARTITION_TYPE_TWO_DFU = 0x49;
    public static final int ERROR_PARTITION_WRITE_FAILED_HEADER = 0x50;
    public static final int ERROR_PARTITION_WRITE_FAILED_DATA = 0x51;
    public static final int ERROR_INTERNAL_ERROR_1 = 0x65;
    public static final int ERROR_INTERNAL_ERROR_2 = 0x66;
    public static final int ERROR_INTERNAL_ERROR_3 = 0x67;
    public static final int ERROR_INTERNAL_ERROR_4 = 0x68;
    public static final int ERROR_INTERNAL_ERROR_5 = 0x69;
    public static final int ERROR_INTERNAL_ERROR_6 = 0x6A;
    public static final int ERROR_INTERNAL_ERROR_7 = 0x6B;
    public static final int WARN_APP_CONFIG_VERSION_INCOMPATIBLE = 0x80;
    /**
     * This error means the file is already uploaded onto the board.
     */
    public static final int WARN_SYNC_ID_IS_DIFFERENT = 0x81;


    public static String getReturnCodesMessage(int code) {
        switch(code) {
            case ERROR_UNKNOWN_ID:
                return "Error: unknown ID";
            case ERROR_BAD_LENGTH_DEPRECATED:
                return "Deprecated error: bad length";
            case ERROR_WRONG_VARIANT:
                return "Error: wrong variant";
            case ERROR_WRONG_PARTITION_NUMBER:
                return "Error: wrong partition number";
            case ERROR_PARTITION_SIZE_MISMATCH:
                return "Error: partition size mismatch";
            case ERROR_PARTITION_TYPE_NOT_FOUND:
                return "Error: partition type not found";
            case ERROR_PARTITION_OPEN_FAILED:
                return "Error: partition open failed";
            case ERROR_PARTITION_WRITE_FAILED:
                return "Error: partition write failed";
            case ERROR_PARTITION_CLOSE_FAILED_1:
                return "Partition close failed type 1";
            case ERROR_SFS_VALIDATION_FAILED:
                return "Error: SFS validation failed";
            case ERROR_OEM_VALIDATION_FAILED:
                return "Error: OEM validation failed";
            case ERROR_UPDATE_FAILED:
                return "Error: update failed";
            case ERROR_APP_NOT_READY:
                return "Error: application not ready";
            case ERROR_LOADER_ERROR:
                return "Error: loader error";
            case ERROR_UNEXPECTED_LOADER_MSG:
                return "Error: unexpected loader message";
            case ERROR_MISSING_LOADER_MSG:
                return "Error: missing loader message";
            case ERROR_BATTERY_LOW:
                return "Error: battery low";
            case ERROR_BAD_LENGTH_PARTITION_PARSE:
                return "Error: bad length partition parse";
            case ERROR_BAD_LENGTH_TOO_SHORT:
                return "Error: bad length too short";
            case ERROR_BAD_LENGTH_UPGRADE_HEADER:
                return "Error: bad length upgrade header";
            case ERROR_BAD_LENGTH_PARTITION_HEADER:
                return "Error: bad length partition header";
            case ERROR_BAD_LENGTH_SIGNATURE:
                return "Error: bad length signature";
            case ERROR_BAD_LENGTH_DATAHDR_RESUME:
                return "Error: bad length data handler resume";
            case ERROR_PARTITION_CLOSE_FAILED_2:
                return "Error: partition close failed type 2";
            case ERROR_PARTITION_CLOSE_FAILED_HEADER:
                return "Error: partition close failed header";
            case ERROR_PARTITION_TYPE_NOT_MATCHING:
                return "Error: partition type not matching";
            case ERROR_PARTITION_TYPE_TWO_DFU:
                return "Error: partition type two DFU";
            case ERROR_PARTITION_WRITE_FAILED_HEADER:
                return "Error: partition write failed header";
            case ERROR_PARTITION_WRITE_FAILED_DATA:
                return "Error: partition write failed data";
            case ERROR_INTERNAL_ERROR_1:
                return "Error: internal error 1";
            case ERROR_INTERNAL_ERROR_2:
                return "Error: internal error 2";
            case ERROR_INTERNAL_ERROR_3:
                return "Error: internal error 3";
            case ERROR_INTERNAL_ERROR_4:
                return "Error: internal error 4";
            case ERROR_INTERNAL_ERROR_5:
                return "Error: internal error 5";
            case ERROR_INTERNAL_ERROR_6:
                return "Error: internal error 6";
            case ERROR_INTERNAL_ERROR_7:
                return "Error: internal error 7";
            case WARN_APP_CONFIG_VERSION_INCOMPATIBLE:
                return "Warning: application configuration version incompatible";
            default:
                return "";
        }
    }
}
