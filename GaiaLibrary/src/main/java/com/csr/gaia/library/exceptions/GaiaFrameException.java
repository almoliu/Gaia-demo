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
package com.csr.gaia.library.exceptions;

/**
 * A GaiaFrameException is thrown when a problem occurs during a frame building.
 */
@SuppressWarnings("unused")
public class GaiaFrameException extends Exception {

    /**
     * The type of the gaia frame exception.
     */
    private final Type mType;

    /**
     * All types of gaia frame exceptions.
     */
    public enum Type {
        /**
         * Example: payload length > payload
         */
        ILLEGAL_ARGUMENTS_PAYLOAD_LENGTH_TOO_LONG
    }

    /**
     * Class constructor for this exception.
     *
     * @param type
     *            the type of this exception.
     */
    @SuppressWarnings("SameParameterValue")
    public GaiaFrameException(Type type) {
        super();
        this.mType = type;
    }

    /**
     * To know the type of this exception.
     *
     * @return the exception type.
     */
    public Type getType() {
        return this.mType;
    }

    @Override
    public String toString() {

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Build of a frame failed: ");

        switch (mType) {
            case ILLEGAL_ARGUMENTS_PAYLOAD_LENGTH_TOO_LONG:
                strBuilder.append("illegal arguments, the payload length is bigger than the length of the payload array.");
                break;
        }

        return strBuilder.toString();
    }

}
