/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.csr.gaiacontrol.models.equalizer.parameters;

import java.text.DecimalFormat;

/**
 * <p>The parameter "frequency" refers to a band parameter and its configuration and bounds depend on the selected
 * band filter.</p>
 * <p>The factor for this parameter is 3 times the real value.</p>
 * <p>This class overrides the methods {@link Parameter#setValueFromLength(int) setValueFromLength} and {@link
 * Parameter#getLengthValue() getLengthValue} to provide a logarithmic scale. The logarithmic scale follows these
 * equations:</p>
 * <ul>
 *     <li><code>y = exp(log(min) + x * (log(max) - log(min)) / (max - min))</code></li>
 *     <li><code>x = (max - min) * (log(y) - log(min)) / (log(max) - log(min))</code></li>
 *     <li><code>x</code> represents the integer value</li>
 *     <li><code>y</code> represents the "length"</li>
 *     <li><code>min</code> represents the minimum bound of the integer range</li>
 *     <li><code>max</code> represents the maximum bound of the integer range</li>
 * </ul>
 */

public class Frequency extends Parameter {

    /**
     * To define a human readable format for the decimal numbers.
     */
    private final DecimalFormat mDecimalFormat = new DecimalFormat();
    /**
     * To convert the frequency from the packet value to the displayed one and vice-versa,
     * we have to multiply by a certain factor defined in the GAIA protocol.
     */
    private static final int FACTOR = 3;
    /**
     * <p>To keep the needed values to calculate the logarithmic scale value which only depends of the range bounds.</p>
     */
    private LogValues mLogValues = new LogValues();

    /**
     * <p>To build a new {@link Parameter} of the type Frequency.</p>
     */
    public Frequency() {
        super(ParameterType.FREQUENCY);
    }

    @Override
    String getLabel(double value) {
        if (isConfigurable) {
            if (value < 50) {
                mDecimalFormat.setMaximumFractionDigits(1);
                return mDecimalFormat.format(value) + " Hz";
            } else if (value < 1000) {
                mDecimalFormat.setMaximumFractionDigits(0);
                return mDecimalFormat.format(value) + " Hz";
            } else {
                value = value / 1000;
                mDecimalFormat.setMaximumFractionDigits(1);
                return mDecimalFormat.format(value) + " kHz";
            }
        }
        else {
            return "- Hz";
        }
    }

    @Override
    public int getFactor() {
        return FACTOR;
    }

    @Override
    public int getLengthValue() {
        double length = mLogValues.rangeLength
                * (Math.log(this.getValue()) - mLogValues.logMin)
                / mLogValues.logRange;
        return (int) Math.round(length);
    }

    @Override
    public void setConfigurable(double minBound, double maxBound) {
        super.setConfigurable(minBound, maxBound);

        // we calculate the constant values linked to the given range for the logarithmic scale.
        mLogValues.rangeLength = getMaxBound() - getMinBound();
        mLogValues.logMax = Math.log(getMaxBound());
        mLogValues.logMin = Math.log(getMinBound());
        mLogValues.logRange = mLogValues.logMax - mLogValues.logMin;
    }

    @Override
    public void setValueFromLength(int lengthValue) {
        double result = mLogValues.logMin + lengthValue * mLogValues.logRange / mLogValues.rangeLength;
        result = Math.exp(result);
        int integer = (int) Math.round(result);
        this.setValue(integer);
    }

    /**
     * To calculate the logarithmic scale for the frequency, some numbers are independent of the parameter current
     * value and only depends on the integer range. This class allows to keep these constants without recalculating
     * while the range does not change.
     */
    private class LogValues {
        /**
         * This value represents the length of the range defines by the minimum integer bound and the maximum integer
         * bound.
         */
        int rangeLength;
        /**
         * This value represents the log of the maximum integer bound.
         */
        double logMax;
        /**
         * This value represents the log of the minimum integer bound.
         */
        double logMin;
        /**
         * This value represents the range length from {@link LogValues#logMin} to {@link LogValues#logMax}.
         */
        double logRange;
    }
}
