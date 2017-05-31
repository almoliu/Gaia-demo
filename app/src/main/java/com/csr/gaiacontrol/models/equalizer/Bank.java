/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.csr.gaiacontrol.models.equalizer;

import com.csr.gaiacontrol.models.equalizer.parameters.MasterGain;
import com.csr.gaiacontrol.models.equalizer.parameters.Parameter;

/**
 * This class represents a customizable Bank for the equalizer. A Bank is defined by a group of bands which allow to filter the sound as defined on each band.
 */

public class Bank {

    /**
     * All bands for this bank.
     */
    private final Band[] mBands;
    /**
     * The current band to configure.
     */
    private int mCurrentBand;

    /**
     * The default minimum value for the master gain.
     */
    private static final double MASTER_GAIN_MIN = -36;
    /**
     * * The default maximum value for the master gain.
     */
    private static final double MASTER_GAIN_MAX = 12;

    /**
     * The values for the master gain displayed to the user.
     */
    private final Parameter mMasterGain = new MasterGain();


    @SuppressWarnings("SameParameterValue")
    public Bank (int number) {
        mBands = new Band[number];
        for (int i=0; i<number; i++) {
            mBands[i] = new Band();
        }
        mCurrentBand = 1;
        mMasterGain.setConfigurable(MASTER_GAIN_MIN, MASTER_GAIN_MAX);
    }

    /**
     * To define the band to configure.
     *
     * @param number
     *          The number band to set as "in configuration".
     */
    public void setCurrentBand (int number) {
        if (number < 1) {
            number = 1;
        }
        else if (number >= mBands.length) {
            number = mBands.length;
        }
        mCurrentBand = number;
    }

    /**
     * To get the current band which is configurable.
     *
     * @return the number which corresponds to the current band.
     */
    public int getNumberCurrentBand() {
        return mCurrentBand;
    }

    /**
     * To get the current configurable band.
     *
     * @return the current selected band by the user.
     */
    public Band getCurrentBand() {
        return mBands[mCurrentBand-1];
    }

    /**
     * To get the band which corresponds to the given number.
     *
     * @param number
     *              the number is going from 1 to the maximum of bands.
     *
     * @return the asked band.
     */
    public Band getBand(int number) {
        if (number < 1) {
            number = 1;
        }
        else if (number > mBands.length) {
            number = mBands.length;
        }
        return mBands[number-1];
    }

    /**
     * To get the master gain parameter for this bank.
     *
     * @return the master gain parameter.
     */
    public Parameter getMasterGain() {
        return mMasterGain;
    }

    /**
     * To define this bank as has to be updated.
     */
    public void hasToBeUpdated() {
        for (Band mBand : mBands) {
            mBand.hasToBeUpdated();
        }
    }
}
