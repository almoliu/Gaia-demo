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
 * This enumeration allows to know all resume points the board can send us into the UPDATE_SYNC_CFM packet. Each resume
 * point represents the step where the update should restart once the UPDATE_SYNC_REQ and the UPDATE_START_REQ have been
 * sent.
 * ALl resume points to display during the update. The enumeration order provides the real resume points order.
 * The resume points are defined from the VM library documentation. Except the last one which is only used for display.
 */
public enum ResumePoints {
    /**
     * This is the 0 resume point, that means the update will start from the beginning, the UPDATE_START_DATA_REQ request.
     */
    DATA_TRANSFER,
    /**
     * This is the 1 resume point, that means the update should resume from the UPDATE_IS_CSR_VALID_DONE_REQ request.
     */
    VALIDATION,
    /**
     * This is the 2 resume point, that means the update should resume from the UPDATE_TRANSFER_COMPLETE_RES request.
     */
    TRANSFER_COMPLETE,
    /**
     * This is the 3 resume point, that means the update should resume from the UPDATE_IN_PROGRESS_RES request.
     */
    IN_PROGRESS,
    /**
     * This is the 4 resume point, that means the update should resume from the UPDATE_COMMIT_CFM confirmation request.
     */
    COMMIT;

    /**
     * To keep constantly this array without calling the values() method which is copying an array when it's called.
     */
    private static final ResumePoints[] values = ResumePoints.values();

    /**
     * To get the resume point matching the corresponding int value in this enumeration.
     *
     * @param value
     *            the int value from which we want the matching resume point.
     *
     * @return the matching Resume point.
     */
    public static ResumePoints valueOf(int value) {
        if (value < 0 || value >= values.length) {
            return null;
        }

        return ResumePoints.values[value];
    }

    /**
     * To get the number of resume points in this enumeration.
     *
     * @return
     *          the number of resume points.
     */
    public static int getLength() {
        return values.length;
    }

    /**
     * To get the label for the corresponding resume point.
     *
     * @return The label which corresponds to the resume point.
     */
    public static String getLabel(ResumePoints step) {

        if (step == null) {
            return "Initialisation";
        }
        switch (step) {
            case DATA_TRANSFER:
                return "Data transfer";
            case VALIDATION:
                return "Data validation";
            case TRANSFER_COMPLETE:
                return "Data transfer complete";
            case IN_PROGRESS:
                return "Update in progress";
            case COMMIT:
                return "Update commit";
            default:
                return "Initialisation";
        }

    }
}
