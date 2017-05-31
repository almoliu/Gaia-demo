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
 * This class represents all codes for VM Upgrade packets the application and the device can exchange.
 */

@SuppressWarnings("unused")
public final class OpCodes {

    /**
     * Used by the application to request an upgrade procedure is started. Device will respond with UPDATE_START_CFM.
     */
    public static final int UPDATE_START_REQ = 0x01;

    /**
     * Used by the board to Respond to the UPDATE_START_REQ message.
     */
    public static final int UPDATE_START_CFM = 0x02;

    /**
     * Used by the application to start data transfer of upgrade image data. The board will respond with one or more
     * UPDATE_DATA messages.
     */
    public static final int UPDATE_DATA_BYTES_REQ = 0x03;

    /**
     * Used by the board to transfer sections of the upgrade image file to the application.
     */
    public static final int UPDATE_DATA = 0x04;

    /**
     * Used by the application to abort the upgrade procedure.
     */
    public static final int UPDATE_ABORT_REQ = 0x07;

    /**
     * Used by the board to answer to the abort request from the application.
     */
    public static final int UPDATE_ABORT_CFM = 0x08;

    /**
     * Used by the board to indicate that it has successfully received and validated the upgrade image file.
     */
    public static final int UPDATE_TRANSFER_COMPLETE_IND = 0x0B;

    /**
     * Used by the board to respond to the UPDATE_TRANSFER_COMPLETE_IND message.
     */
    public static final int UPDATE_TRANSFER_COMPLETE_RES = 0x0C;

    /**
     * Deprecated?
     */
    public static final int UPDATE_IN_PROGRESS_RES = 0x0E;

    /**
     * Used by the board to indicate it is ready for permission to commit the upgrade.
     */
    public static final int UPDATE_COMMIT_REQ = 0x0F;

    /**
     * Used by the application to respond to the UPDATE_COMMIT_REQ message from the board.
     */
    public static final int UPDATE_COMMIT_CFM = 0x10;

    /**
     * Used by the board to inform the application about errors or warnings. Errors are considered as fatal. Warnings
     * are considered as informational.
     */
    public static final int UPDATE_ERROR_WARN_IND = 0x11;

    /**
     * Used by the board to indicate the upgrade has been completed.
     */
    public static final int UPDATE_COMPLETE_IND = 0x12;

    /**
     * Used by the application to synchronize with the board before any other protocol message.
     */
    public static final int UPDATE_SYNC_REQ = 0x13;

    /**
     * Used by the board to respond to the UPDATE_SYNC_REQ message.
     */
    public static final int UPDATE_SYNC_CFM = 0x14;

    /**
     * Used by the application to begin a data transfer.
     */
    public static final int UPDATE_START_DATA_REQ = 0x15;

    /**
     * Used by the application to request for executable partition validation status.
     */
    public static final int UPDATE_IS_VALIDATION_DONE_REQ = 0x16;

    /**
     * used by the board to respond to the UPDATE_IS_VALIDATION_DONE_REQ message.
     */
    public static final int UPDATE_IS_VALIDATION_DONE_CFM = 0x17;

    /**
     * ?
     */
    public static final int UPDATE_VERSION_REQ = 0x19;

    /**
     * ?
     */
    public static final int UPDATE_VERSION_CFM = 0x1A;

    /**
     * ?
     */
    public static final int UPDATE_VARIANT_REQ = 0x1B;

    /**
     * ?
     */
    public static final int UPDATE_VARIANT_CFM = 0x1C;

    /**
     * ?
     */
    public static final int UPDATE_ERASE_SQIF_REQ = 0x1D;

    /**
     * Used by the application to respond to the UPDATE_ERASE_SQIF_REQ message.
     */
    public static final int UPDATE_ERASE_SQIF_CFM = 0x1E;

    /**
     * Used by the application to confirm it received an error or a warning message from the board.
     */
    public static final int UPDATE_ERROR_WARN_RES = 0x1F;




    /* ******* DEPRECATED OPERATION CODES ******* */

    /**
     * @deprecated
     */
    public static final int UPDATE_SUSPEND_IND = 0x05;
    /**
     * @deprecated
     */
    public static final int UPDATE_RESUME_IND = 0x06;
    /**
     * @deprecated
     */
    public static final int UPDATE_PROGRESS_REQ = 0x09;
    /**
     * @deprecated
     */
    public static final int UPDATE_PROGRESS_CFM = 0x0A;
    /**
     * @deprecated
     */
    public static final int UPDATE_IN_PROGRESS_IND = 0x0D;
    /**
     * Was used by the application.
     * @deprecated
     */
    public static final int UPDATE_SYNC_AFTER_REBOOT_REQ = 0x18;



    /* ******* SPECIAL INFORMATION FOR OPCODES ******* */

    /**
     * The length for the data of the UPDATE_SYNC_REQ_LENGTH message.
     */
    public static final int UPDATE_SYNC_REQ_LENGTH = 4;
    /**
     * Value for the first byte for data when we are sending the last UPDATE_DATA message.
     */
    public static final byte UPDATE_DATA_LAST_PACKET = 1;
    /**
     * Value for the first byte for data when we are not sending the last UPDATE_DATA message.
     */
    public static final byte UPDATE_DATA_NOT_LAST_PACKET = 0;
    /**
     * Value for an UPDATE_START_CFM message when the device is ready to start the update process.
     */
    public static final int UPDATE_START_CFM_SUCCESS = 0x00;
    /**
     * Value for an UPDATE_START_CFM message when the device is not ready to start the update process.
     */
    public static final int UPDATE_START_ERROR_APP_NOT_READY = 0x09;
    /**
     * The length for the data of the UPDATE_DATA_BYTES_REQ message.
     */
    public static final int UPDATE_DATA_BYTES_REQ_LENGTH = 8;
    /**
     * Used by the application to confirm it received an error or a warning message from the board.
     */
    public static final int UPDATE_ERROR_WARN_RES_LENGTH = 2;
    /**
     * Used by the application to confirm that the update should continue.
     */
    public static final int UPDATE_TRANSFER_COMPLETE_CONTINUE = 0x00;
    /**
     * Used by the application to confirm that the update should abort.
     */
    public static final int UPDATE_TRANSFER_COMPLETE_ABORT = 0x01;
    /**
     * Used by the application to confirm to the board the user wishes to continue the update process.
     */
    public static final int UPDATE_IN_PROGRESS_CONTINUE = 0x00;
    /**
     * Used by the application to confirm the user wants to commit the update.
     */
    public static final int UPDATE_COMMIT_CONTINUE = 0x00;
    /**
     * Used by the application to confirm the user doesn't want to commit the update for now.
     */
    public static final int UPDATE_COMMIT_ABORT = 0x01;
}
