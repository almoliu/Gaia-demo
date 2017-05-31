/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.csr.gaiacontrol.models.equalizer;

import com.csr.gaiacontrol.models.equalizer.parameters.Filter;
import com.csr.gaiacontrol.models.equalizer.parameters.Frequency;
import com.csr.gaiacontrol.models.equalizer.parameters.Gain;
import com.csr.gaiacontrol.models.equalizer.parameters.Parameter;
import com.csr.gaiacontrol.models.equalizer.parameters.ParameterType;
import com.csr.gaiacontrol.models.equalizer.parameters.Quality;

/**
 * The values for each parameter for a band.
 */
public class Band {
    /**
     * The band filter.
     */
    private Filter mFilter = Filter.BYPASS;

    private boolean isFilterUpToDate = false;

    /**
     * All the parameters which characterise on a band.
     */
    private final Parameter[] mParameters = new Parameter[ParameterType.getSize()];

    public Band() {
        mParameters[ParameterType.FREQUENCY.ordinal()] = new Frequency();
        mParameters[ParameterType.GAIN.ordinal()] = new Gain();
        mParameters[ParameterType.QUALITY.ordinal()] = new Quality();
    }

    /**
     * To define the filter for this band.
     *
     * @param filter
     *              The filter defined for this band.
     *
     * @param fromUser
     *              To know if this filter has been selected by the user or is a value update
     */
    public void setFilter(Filter filter, boolean fromUser) {
        mFilter = filter;
        Filter.defineParameters(filter, mParameters[ParameterType.FREQUENCY.ordinal()],
                mParameters[ParameterType.GAIN.ordinal()], mParameters[ParameterType.QUALITY.ordinal()]);
        if (!fromUser) {
            isFilterUpToDate = true;
        }
    }

    /**
     * To get the filter defined for this band.
     *
     * @return the defined filter.
     */
    public Filter getFilter() {
        return mFilter;
    }

    /**
     * To get the frequency parameter defined for this band.
     *
     * @return the frequency parameter.
     */
    public Parameter getFrequency() {
        return mParameters[ParameterType.FREQUENCY.ordinal()];
    }

    /**
     * To get the gain parameter defined for this band.
     *
     * @return the gain parameter.
     */
    public Parameter getGain() {
        return mParameters[ParameterType.GAIN.ordinal()];
    }

    /**
     * To get the quality parameter defined for this band.
     *
     * @return the quality parameter.
     */
    public Parameter getQuality() {
        return mParameters[ParameterType.QUALITY.ordinal()];
    }

    /**
     * To know if the values for this Band has been updated.
     *
     * @return true if it has been updated, false if it has to be updated.
     */
    public boolean isUpToDate() {
        for (int i=1; i<mParameters.length; i++) {
            if (mParameters[i].isConfigurable() && !mParameters[i].isUpToDate()) {
                return false;
            }
        }
        return isFilterUpToDate;
    }

    /**
     * To define this Band as have to be updated.
     */
    public void hasToBeUpdated() {
        isFilterUpToDate = false;
        for (int i=1; i<mParameters.length; i++) {
            mParameters[i].hasTobeUpdated();
        }
    }
}
