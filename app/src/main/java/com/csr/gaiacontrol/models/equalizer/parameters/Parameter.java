/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.csr.gaiacontrol.models.equalizer.parameters;

/**
 * <p>This class allows to define all the characteristics for each customizable parameter. Each parameter is defined
 * by a range, a value and a {@link ParameterType parameter type}.</p>
 * <p>The current parameter value is represented by an int in a range defined by a factor and the parameter type.</p>
 * <p>However, the real value and bounds for the parameter are the labels one calculated using the factor as follow:
 * <code>parameterValue = realValue * factor</code>.
 * </p>
 */
public abstract class Parameter {

    // ====== CONSTS ===============================================================================

    /**
     * The allocated case in an array to know the minimum value for a parameter.
     */
    private static final int MIN_BOUND = 0;
    /**
     * The allocated case in an array to know the maximum value for a parameter.
     */
    private static final int MAX_BOUND = 1;


    // ====== PRIVATE FIELDS ===============================================================================

    /**
     * The bounds for the integer range of this parameter value.
     */
    private final int[] mParameterBounds = new int[2];
    /**
     * The label values for the real bounds of the parameter.
     */
    private final String[] mLabelBounds = new String[2];
    /**
     * To know if this parameter has to be updated.
     */
    private boolean isUpToDate = false;
    /**
     * To know the type if this parameter.
     */
    private final ParameterType mParameterType;
    /**
     * The current integer value of this parameter.
     */
    private int mValue;
    /**
     * The factor used to calculate the real value of this parameter.
     */
    private final int mFactor;


    // ====== PACKAGE FIELD ===============================================================================

    /**
     * To know if this parameter can be modified - depending on the filter of a band, a parameter can be no modifiable.
     */
    boolean isConfigurable = false;


    // ====== CONSTRUCTOR ===============================================================================

    /**
     * To build a Parameter object defined by its type.
     *
     * @param parameterType
     *              the type of the parameter.
     */
    Parameter(ParameterType parameterType) {
        mParameterType = parameterType;
        mFactor = getFactor();
    }


    // ====== GETTERS ===============================================================================

    /**
     * <p>To get the type of this parameter.</p>
     *
     * @return The type of this parameter: GAIN,
     */
    public ParameterType getParameterType() {
        return mParameterType;
    }

    /**
     * <p>To get the value of this parameter which corresponds to the integer range.</p>
     *
     * return The value contains in the integer range defined by the factor of this parameter.
     */
    public int getValue() {
        return mValue;
    }

    /**
     * <p>To get the value of the current value in a range from 0 to the length of the integer range.</p>
     *
     * @return the value of this parameter in a range from 0 to the integer range of this parameter - given by {@link
     * Parameter#getBoundsLength()}.
     */
    public int getLengthValue() {
        return mValue - mParameterBounds[MIN_BOUND];
    }

    /**
     * <p>To get the length of the integer range. This could be used to create an interval from 0 to this
     * length for instance to know the maximum bound of a {@link android.widget.SeekBar SeekBar}.</p>
     *
     * @return the difference between the maximum bound and the minimum bound.
     */
    public int getBoundsLength() {
        return mParameterBounds[MAX_BOUND] - mParameterBounds[MIN_BOUND];
    }

    /**
     * To know if this parameter is configurable.
     *
     * @return true if it is possible, false otherwise.
     */
    public boolean isConfigurable() {
        return isConfigurable;
    }

    /**
     * To know if this parameter is up to date.
     *
     * @return true if this parameter has been defined as up to date, false if it has to be updated.
     */
    public boolean isUpToDate() {
        return isUpToDate;
    }

    /**
     * To get the label which corresponds to the real minimum bound for the range of this parameter.
     *
     * @return A readable value with the unit corresponding to the real minimum bound for this parameter.
     */
    public String getLabelMinBound() {
        if (isConfigurable) {
            return mLabelBounds[MIN_BOUND];
        }
        else {
            return "";
        }
    }

    /**
     * To get the label which corresponds to the real maximum bound for the range of this parameter.
     *
     * @return A readable value with the unit corresponding to the real maximum bound for this parameter.
     */
    public String getLabelMaxBound() {
        if (isConfigurable) {
            return mLabelBounds[MAX_BOUND];
        } else {
            return "";
        }
    }

    /**
     * To get the label which corresponds to the real value of this parameter.
     *
     * @return The readable real value with the unit corresponding to this parameter.
     */
    public String getLabelValue() {
        double realValue = mValue / (double) mFactor;
        return getLabel(realValue);
    }

    /**
     * <p>To get the actual minimum bound of the integer range.</p>
     *
     * @return the minimum bound.
     */
    public int getMinBound() {
        return mParameterBounds[MIN_BOUND];
    }

    /**
     * <p>To get the actual maximum bound of the integer range.</p>
     *
     * @return the maximum bound.
     */
    public int getMaxBound() {
        return mParameterBounds[MAX_BOUND];
    }


    // ====== SETTERS ===============================================================================

    /**
     * <p>To define the current integer value for the integer range of this parameter.</p>
     */
    public void setValue(int parameterValue) {
        isUpToDate = true;
        mValue = parameterValue;
    }

    /**
     * <p>To define the current value of this parameter by giving the corresponding value in an interval from 0 to
     * the length of the integer range.</p>
     *
     * @param lengthValue
     *          The corresponding value in a range of 0 to the Integer range length.
     */
    public void setValueFromLength(int lengthValue) {
        mValue = lengthValue + mParameterBounds[MIN_BOUND];
    }

    /**
     * To define this parameter as configurable by giving its new real range.
     *
     * @param minBound
     *          The minimum bound of the real range.
     * @param maxBound
     *          The maximum bound of the real range.
     */
    public void setConfigurable(double minBound, double maxBound) {
        isConfigurable = true;
        setBound(MIN_BOUND, minBound);
        setBound(MAX_BOUND, maxBound);
    }

    /**
     * To define this parameter as not configurable by the user.
     */
    public void setNotConfigurable() {
        isConfigurable = false;
    }

    /**
     * To define this Parameter as have to be updated.
     */
    public void hasTobeUpdated() {
        isUpToDate = false;
    }


    // ====== PRIVATE METHODS ===============================================================================

    /**
     * <p>To define the given bound of this parameter range. This method will create the label corresponding to the
     * real bound, and will create the integer value corresponding to the integer range.</p>
     *
     * @param position
     *              The bound to set the value.
     * @param value
     *              The value to set to the bound.
     */
    private void setBound(int position, double value) {
        mLabelBounds[position] = getLabel(value);
        mParameterBounds[position] = (int) (value * mFactor);
    }


    // ====== ABSTRACT METHODS ===============================================================================

    /**
     * To get a human readable value to display.
     *
     * @param value
     *          the value to get as a readable one.
     *
     * @return A formatted value readable for humans.
     */
    abstract String getLabel(double value);

    /**
     * <p>To get the factor corresponding to this parameter.</p>
     *
     * @return The factor defined by the type of this parameter.
     */
    abstract int getFactor();
}
