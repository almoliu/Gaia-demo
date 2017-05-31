/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.csr.gaiacontrol.activities;

import java.lang.ref.WeakReference;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.csr.gaia.library.Gaia;
import com.csr.gaia.library.GaiaError;
import com.csr.gaia.library.GaiaLink;
import com.csr.gaia.library.GaiaPacket;
import com.csr.gaiacontrol.R;
import com.csr.gaiacontrol.models.equalizer.Band;
import com.csr.gaiacontrol.models.equalizer.Bank;
import com.csr.gaiacontrol.models.equalizer.parameters.Filter;
import com.csr.gaiacontrol.models.equalizer.parameters.Parameter;
import com.csr.gaiacontrol.models.equalizer.parameters.ParameterType;
import com.csr.gaiacontrol.utils.Utils;
import com.csr.gaiacontrol.views.PresetButton;
import com.csr.gaiacontrol.views.SliderLayout;

/**
 * <p>This activity is the activity to control the custom equalizer for a device connected to the application.</p>
 */
public class CustomEqualizerActivity extends ModelActivity implements SliderLayout.SliderListener {

    /**
     * For the debug mode, the tag to display for logs.
     */
    private static final String TAG = "CustomEqualizerActivity";

    /**
     * The first byte of the EQ PARAMETER command is always 0x01 as the only bank which is customizable is the 1.
     */
    private static final int EQ_PARAMETER_FIRST_BYTE = 0x01;
    /**
     * The length of the payload from the received GAIA packet when we request the EQ PARAMETER configuration.
     */
    private static final int GET_EQ_PARAMETER_PAYLOAD_LENGTH = 5;
    /**
     * The value for the master gain parameter in the parameter ID from the payload in a GAIA packet for the
     * EQ_PARAMETER commands.
     */
    private static final int GENERAL_BAND = 0;
    /**
     * The value for the master gain parameter in the parameter ID from the payload in a GAIA packet for the
     * EQ_PARAMETER commands.
     */
    private static final int PARAMETER_MASTER_GAIN = 1;

    /**
     * To keep the instance for the slider about the frequency to get and set the value.
     */
    private SliderLayout mSLFrequency;
    /**
     * To keep the instance for the slider about the gain to get and set the value.
     */
    private SliderLayout mSLGain;
    /**
     * To keep the instance for the slider about the quality to get and set the value.
     */
    private SliderLayout mSLQuality;
    /**
     * To keep the instance for the slider about the master gain to get and set the value.
     */
    private SliderLayout mSLMasterGain;
    /**
     * To keep instances for the band buttons to select and deselect them.
     */
    private final Button[] mBandButtons = new Button[6];
    /**
     * To keep instances for the filter buttons to select and deselect them.
     */
    private final Button[] mFilters = new Button[Filter.getSize()];
    /**
     * The layout to display while retrieving any information.
     */
    private View mProgressLayout;
    /**
     * The dialog to show a message to the user.
     */
    private AlertDialog mIncorrectStateDialog;
    /**
     * To know if the dialog to show a message to the user is already on screen.
     */
    private boolean mIsIncorrectStateDialogDisplayed = false;
    /**
     * To know if the board has to recalculate the bank: this has to be done if the current bank is the custom one. The
     * value for this one is 0x00 for no recalculation, > 0x00 otherwise.
     */
    private int mRecalculation = 0x00;

    /**
     * All the values displayed to the user for the selected band.
     */
    private final Bank mBank = new Bank(5);

    @Override
    public void onProgressChangedByUser(int progress, int id) {
        Parameter parameter = null;
        SliderLayout sliderLayout = null;

        switch (id) {
        case R.id.sl_frequency:
            parameter = mBank.getCurrentBand().getFrequency();
            sliderLayout = mSLFrequency;
            break;
        case R.id.sl_gain:
            parameter = mBank.getCurrentBand().getGain();
            sliderLayout = mSLGain;
            break;
        case R.id.sl_master_gain:
            parameter = mBank.getMasterGain();
            sliderLayout = mSLMasterGain;
            break;
        case R.id.sl_quality:
            parameter = mBank.getCurrentBand().getQuality();
            sliderLayout = mSLQuality;
            break;
        }

        if (parameter != null && sliderLayout != null) {
            parameter.setValueFromLength(progress);
            updateDisplayParameterValue(sliderLayout, parameter);
        }
    }

    @Override
    public void onStopTrackingTouch(int progress, int id) {
        Parameter parameter = null;
        SliderLayout sliderLayout = null;

        switch (id) {
        case R.id.sl_frequency:
            parameter = mBank.getCurrentBand().getFrequency();
            sliderLayout = mSLFrequency;
            break;
        case R.id.sl_gain:
            parameter = mBank.getCurrentBand().getGain();
            sliderLayout = mSLGain;
            break;
        case R.id.sl_master_gain:
            parameter = mBank.getMasterGain();
            sliderLayout = mSLMasterGain;
            break;
        case R.id.sl_quality:
            parameter = mBank.getCurrentBand().getQuality();
            sliderLayout = mSLQuality;
            break;
        }

        if (parameter != null && sliderLayout != null) {
            parameter.setValueFromLength(progress);
            updateDisplayParameterValue(sliderLayout, parameter);
            ParameterType parameterType = parameter.getParameterType();
            int parameterValue = (parameterType != null) ? parameterType.ordinal() : PARAMETER_MASTER_GAIN;
            int band = (parameterType != null) ? mBank.getNumberCurrentBand() : GENERAL_BAND;
            sendSetEQParameterPacket(band, parameterValue, parameter.getValue());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.custom_equaliser_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_refresh_equaliser:
            refreshValues();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshValues();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_equalizer);
        this.init();
    }

    @Override
    protected Handler getGaiaHandler() {
        return new GaiaHandler(this);
    }

    /**
     * To refresh the values of the configurable bank by requesting them to the board.
     */
    private void refreshValues() {
        mProgressLayout.setVisibility(View.VISIBLE);
        mBank.hasToBeUpdated();
        // master gain
        askForMasterGain();
        // filter
        int band = mBank.getNumberCurrentBand();
        sendGetEQParameterPacket(band, ParameterType.FILTER.ordinal());
        sendGaiaPacket(Gaia.COMMAND_GET_EQ_CONTROL); // to know if the current pre-set is the custom one.
    }

    /**
     * To select a new current band.
     *
     * @param band
     *            The new current band.
     */
    private void selectBand(int band) {
        // deselect previous values on the UI
        mBandButtons[mBank.getNumberCurrentBand()].setSelected(false);
        mFilters[mBank.getCurrentBand().getFilter().ordinal()].setSelected(false);
        // select new values on the UI
        mBandButtons[band].setSelected(true);

        // define the new band
        mBank.setCurrentBand(band);

        // update the displayed values
        updateDisplayParameters();
        mBank.getBand(band).hasToBeUpdated();

        sendGetEQParameterPacket(band, ParameterType.FILTER.ordinal());
    }

    /**
     * To initialise objects used in this activity.
     */
    private void init() {
        this.setSupportActionBar((Toolbar) findViewById(R.id.tb_menu));
        //noinspection ConstantConditions
        this.getSupportActionBar().setLogo(R.drawable.ic_equalizer_small);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressLayout = findViewById(R.id.l_progress_bar);

        initSettingsComponents();
        initBandsComponents();
        initFiltersComponents();

        buildDialogs();
    }

    /**
     * To initialise the settings UI components - mainly sliders.
     */
    private void initSettingsComponents() {
        mSLFrequency = (SliderLayout) findViewById(R.id.sl_frequency);
        mSLFrequency.initialize(getString(R.string.frequency_title),
                mBank.getCurrentBand().getFrequency().getLabelValue(), this);
        mSLGain = (SliderLayout) findViewById(R.id.sl_gain);
        mSLGain.initialize(getString(R.string.gain_title), mBank.getCurrentBand().getGain().getLabelValue(), this);
        mSLQuality = (SliderLayout) findViewById(R.id.sl_quality);
        mSLQuality.initialize(getString(R.string.quality_title), mBank.getCurrentBand().getQuality().getLabelValue(),
                this);
        mSLMasterGain = (SliderLayout) findViewById(R.id.sl_master_gain);
        mSLMasterGain.initialize("", mBank.getMasterGain().getLabelValue(), this);
        mSLMasterGain.setSliderBounds(mBank.getMasterGain().getBoundsLength(),
                mBank.getMasterGain().getLabelMinBound(),
                mBank.getMasterGain().getLabelMaxBound());
        mSLMasterGain.hideTitle();
    }

    /**
     * To initialise the filters UI components.
     */
    private void initFiltersComponents() {
        mFilters[Filter.BYPASS.ordinal()] = (Button) findViewById(R.id.bt_BYPASS);
        mFilters[Filter.BYPASS.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.BYPASS);
            }
        });

        mFilters[Filter.LOW_PASS_1.ordinal()] = (Button) findViewById(R.id.bt_LPF1);
        mFilters[Filter.LOW_PASS_1.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.LOW_PASS_1);
            }
        });

        mFilters[Filter.HIGH_PASS_1.ordinal()] = (Button) findViewById(R.id.bt_HPF1);
        mFilters[Filter.HIGH_PASS_1.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.HIGH_PASS_1);
            }
        });

        mFilters[Filter.ALL_PASS_1.ordinal()] = (Button) findViewById(R.id.bt_APF1);
        mFilters[Filter.ALL_PASS_1.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.ALL_PASS_1);
            }
        });
        mFilters[Filter.LOW_SHELF_1.ordinal()] = (Button) findViewById(R.id.bt_LS1);
        mFilters[Filter.LOW_SHELF_1.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.LOW_SHELF_1);
            }
        });
        mFilters[Filter.HIGH_SHELF_1.ordinal()] = (Button) findViewById(R.id.bt_HS1);
        mFilters[Filter.HIGH_SHELF_1.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.HIGH_SHELF_1);
            }
        });
        mFilters[Filter.TILT_1.ordinal()] = (Button) findViewById(R.id.bt_Tilt1);
        mFilters[Filter.TILT_1.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.TILT_1);
            }
        });
        mFilters[Filter.LOW_PASS_2.ordinal()] = (Button) findViewById(R.id.bt_LPF2);
        mFilters[Filter.LOW_PASS_2.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.LOW_PASS_2);
            }
        });
        mFilters[Filter.HIGH_PASS_2.ordinal()] = (Button) findViewById(R.id.bt_HPF2);
        mFilters[Filter.HIGH_PASS_2.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.HIGH_PASS_2);
            }
        });
        mFilters[Filter.ALL_PASS_2.ordinal()] = (Button) findViewById(R.id.bt_APF2);
        mFilters[Filter.ALL_PASS_2.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.ALL_PASS_2);
            }
        });
        mFilters[Filter.LOW_SHELF_2.ordinal()] = (Button) findViewById(R.id.bt_LS2);
        mFilters[Filter.LOW_SHELF_2.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.LOW_SHELF_2);
            }
        });
        mFilters[Filter.HIGH_SHELF_2.ordinal()] = (Button) findViewById(R.id.bt_HS2);
        mFilters[Filter.HIGH_SHELF_2.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.HIGH_SHELF_2);
            }
        });
        mFilters[Filter.TILT_2.ordinal()] = (Button) findViewById(R.id.bt_Tilt2);
        mFilters[Filter.TILT_2.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.TILT_2);
            }
        });
        mFilters[Filter.PARAMETRIC_EQUALIZER.ordinal()] = (Button) findViewById(R.id.bt_PEQ);
        mFilters[Filter.PARAMETRIC_EQUALIZER.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.PARAMETRIC_EQUALIZER);
            }
        });
    }

    /**
     * To initialise the bands UI components.
     */
    private void initBandsComponents() {
        mBandButtons[1] = (Button) findViewById(R.id.bt_band_1);
        mBandButtons[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectBand(1);
            }
        });
        mBandButtons[2] = (Button) findViewById(R.id.bt_band_2);
        mBandButtons[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectBand(2);
            }
        });
        mBandButtons[3] = (Button) findViewById(R.id.bt_band_3);
        mBandButtons[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectBand(3);
            }
        });
        mBandButtons[4] = (Button) findViewById(R.id.bt_band_4);
        mBandButtons[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectBand(4);
            }
        });
        mBandButtons[5] = (Button) findViewById(R.id.bt_band_5);
        mBandButtons[5].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectBand(5);
            }
        });

        mBandButtons[mBank.getNumberCurrentBand()].setSelected(true);
    }

    /**
     * This method is called when the user is selecting a new filter type for the current band.
     *
     * @param filter
     *            The selected filter.
     */
    private void onFilterButtonClick(Filter filter) {
        setFilter(mBank.getNumberCurrentBand(), filter, true);
    }

    /**
     * To define the filter for a specific band.
     *
     * @param bandNumber
     *            the band for which we want to define the filter.
     * @param filter
     *            the filter to define for the given band number.
     * @param fromUser
     *            to know if this setting is coming from a user action (true) or a value received from the board
     *            (false).
     */
    private void setFilter(int bandNumber, Filter filter, boolean fromUser) {
        boolean isCurrentBand = bandNumber == mBank.getNumberCurrentBand();

        // updating the UI if the band is the current one.
        if (isCurrentBand) {
            mFilters[mBank.getCurrentBand().getFilter().ordinal()].setSelected(false);
            mFilters[filter.ordinal()].setSelected(true);
        }

        // defining the filter for the specific band.
        Band band = mBank.getBand(bandNumber);
        band.setFilter(filter, fromUser);

        // if the information is coming from the user the values are not anymore up to date and we have to update the
        // board with the new value.
        if (fromUser) {
            band.hasToBeUpdated();
            sendSetEQParameterPacket(bandNumber, ParameterType.FILTER.ordinal(), filter.ordinal());
        }

        // We have to request the values for the different parameters.
        if (band.getFrequency().isConfigurable()) {
            sendGetEQParameterPacket(bandNumber, ParameterType.FREQUENCY.ordinal());
        }
        if (band.getGain().isConfigurable()) {
            sendGetEQParameterPacket(bandNumber, ParameterType.GAIN.ordinal());
        }
        if (band.getQuality().isConfigurable()) {
            sendGetEQParameterPacket(bandNumber, ParameterType.QUALITY.ordinal());
        }

        if (isCurrentBand) {
            updateDisplayParameters();
            updateParametersSlidersBounds();
        }
    }

    /**
     * To refresh the information displayed on the UI.
     */
    private void updateParametersSlidersBounds() {
        // update frequency
        updateParameterSliderBounds(mSLFrequency, mBank.getCurrentBand().getFrequency());
        // update gain
        updateParameterSliderBounds(mSLGain, mBank.getCurrentBand().getGain());
        // update quality
        updateParameterSliderBounds(mSLQuality, mBank.getCurrentBand().getQuality());
    }

    /**
     * To refresh the UI for a specified parameter.
     *
     * @param sliderLayout
     *            The slider layout which corresponds to the given parameter.
     * @param parameter
     *            The parameter for which the UI has to be refreshed.
     */
    private void updateParameterSliderBounds(SliderLayout sliderLayout, Parameter parameter) {
        sliderLayout.setEnabled(parameter.isConfigurable());
        sliderLayout.setSliderBounds(parameter.getBoundsLength(), parameter.getLabelMinBound(),
                parameter.getLabelMaxBound());
        sliderLayout.setSliderProgress(parameter.getValue());
    }

    /**
     * To request the master gain of a bank.
     */
    private void askForMasterGain() {
        sendGetEQParameterPacket(GENERAL_BAND, PARAMETER_MASTER_GAIN);
    }

    /**
     * To send a EQ Parameter packet to the board to request values for the specified band and parameter.
     *
     * @param band
     *            The band for which we want a value: 0 for a general parameter of the bank, 1 to 5 for a specific band.
     * @param parameter
     *            The parameter for which we want the value.
     */
    private void sendGetEQParameterPacket(int band, int parameter) {
        int value = buildParameterID(band, parameter);
        sendGaiaPacket(Gaia.COMMAND_GET_EQ_PARAMETER, EQ_PARAMETER_FIRST_BYTE, value);
    }

    /**
     * <p>To build the low byte of the Parameter ID for the GET and SET EQ_PARAMETER commands.</p> <p>The different
     * values for this parameter ID are:</p> <ul> <li>band 0 & parameter 0: requests the number of bands.</li> <li>band
     * 0 & parameter 1: requests get the master gain for the bank.</li> <li>band 1-5 & parameter 0: requests the filter
     * type for the specified band.</li> <li>band 1-5 & parameter 1: requests the frequency for the specified band.</li>
     * <li>band 1-5 & parameter 2: requests the gain for the specified band.</li> <li>band 1-5 & parameter 3: requests
     * the quality type for the specified band.</li> </ul>
     *
     * @param band
     *            The band for which we want a value.
     * @param parameter
     *            The parameter for which we want the value.
     *
     * @return the parameter ID.
     */
    private int buildParameterID(int band, int parameter) {
        return (band << 4) | parameter;
    }

    /**
     * To send a COMMAND_SET_EQ_PARAMETER GAIA packet to the board to set the given value for the given parameter.
     *
     * @param band
     *            the band for which we want to set a parameter to the given value.
     * @param parameter
     *            the parameter to set to the given value.
     * @param value
     *            the new value for the band parameter.
     */
    private void sendSetEQParameterPacket(int band, int parameter, int value) {
        byte[] payload = new byte[5];
        payload[0] = EQ_PARAMETER_FIRST_BYTE;
        payload[1] = (byte) buildParameterID(band, parameter);
        Utils.putField(value, payload, 2, 2, false);
        payload[4] = (byte) mRecalculation; // recalculating in live - only if the custom pre-set is used.
        sendGaiaPacket(Gaia.COMMAND_SET_EQ_PARAMETER, payload);
    }

    /**
     * Called when we receive a packet about the command COMMAND_GET_EQ_PARAMETER to manage the application depending on
     * information from the packet. The packet should have the following payload: <ul> <li>Offset 0: packet status</li>
     * <li>Offset 1: parameter ID, high byte</li> <li>Offset 2: parameter ID, low byte</li> <li>Offset 3: value, high
     * byte</li> <li>Offset 4: value, low byte</li> </ul>
     *
     * @param packet
     *            The packet about the received command COMMAND_GET_EQ_PARAMETER.
     */
    private void receivePacketGetEQParameter(GaiaPacket packet) {

        byte[] payload = packet.getPayload();
        if (DEBUG)
            Log.w(TAG, "EQ PARAM, payload: " + Utils.getStringFromBytes(payload));

        final int OFFSET_PARAMETER_ID_LOW_BYTE = 2;
        final int START_OFFSET_VALUE = 3;
        final int LENGTH_VALUE = 2;

        // checking if there are enough arguments in the payload
        if (payload.length < GET_EQ_PARAMETER_PAYLOAD_LENGTH) {
            if (DEBUG)
                Log.w(TAG, "Received \"COMMAND_GET_EQ_PARAMETER\" packet with missing arguments.");
            return;
        }

        // retrieving the different arguments from the received packet.
        int band = (payload[OFFSET_PARAMETER_ID_LOW_BYTE] & 0xF0) >>> Utils.BITS_IN_HEXADECIMAL;
        int param = payload[OFFSET_PARAMETER_ID_LOW_BYTE] & 0xF;

        // master gain for the bank
        if (band == GENERAL_BAND && param == PARAMETER_MASTER_GAIN) {
            int masterGainValue = Utils.extractShortField(payload, START_OFFSET_VALUE, LENGTH_VALUE, false);
            receiveMasterGainValue(masterGainValue);
            if (DEBUG)
                Log.e(TAG, "MASTER GAIN - value: " + masterGainValue);
        }
        else {
            ParameterType parameterType = ParameterType.valueOf(param);

            // checking of the parameter type is defined for this application.
            if (parameterType == null) {
                if (DEBUG)
                    Log.w(TAG, "Received \"COMMAND_GET_EQ_PARAMETER\" packet with an unknown parameter type: " + param);
                return;
            }

            // acting depending on the parameter type for the received band
            switch (parameterType) {
            case FILTER:
                int filterValue = Utils.extractIntField(payload, START_OFFSET_VALUE, LENGTH_VALUE, false);
                Filter filter = Filter.valueOf(filterValue);
                if (filter == null) {
                    if (DEBUG)
                        Log.w(TAG, "Received \"COMMAND_GET_EQ_PARAMETER\" packet with an unknown filter type: "
                                + filterValue);
                    return;
                }
                receiveFilterValue(band, filter);
                Log.e(TAG,
                        "BAND: " + band + " - PARAM: " + parameterType.toString() + " - FILTER: " + filter.toString());
                break;

            case FREQUENCY:
                int frequencyValue = Utils.extractIntField(payload, START_OFFSET_VALUE, LENGTH_VALUE, false);
                receiveParameterValue(band, frequencyValue, mBank.getBand(band).getFrequency(), mSLFrequency);
                Log.e(TAG,
                        "BAND: " + band + " - PARAM: " + parameterType.toString() + " - FREQUENCY: " + frequencyValue);
                break;

            case GAIN:
                int gainValue = Utils.extractShortField(payload, START_OFFSET_VALUE, LENGTH_VALUE, false);
                receiveParameterValue(band, gainValue, mBank.getBand(band).getGain(), mSLGain);
                Log.e(TAG, "BAND: " + band + " - PARAM: " + parameterType.toString() + " - GAIN: " + gainValue);
                break;

            case QUALITY:
                int qualityValue = Utils.extractIntField(payload, START_OFFSET_VALUE, LENGTH_VALUE, false);
                receiveParameterValue(band, qualityValue, mBank.getBand(band).getQuality(), mSLQuality);
                Log.e(TAG, "BAND: " + band + " - PARAM: " + parameterType.toString() + " - QUALITY: " + qualityValue);
                break;
            }
        }

        // in the case where all updates have been done
        if (mBank.getCurrentBand().isUpToDate()) {
            mProgressLayout.setVisibility(View.GONE);
        }
    }

    /**
     * When we received a packet from the board to get the new value for the filter of a certain band.
     *
     * @param band
     *            The band for which we received the filter value.
     * @param filter
     *            The new filter for the given band.
     */
    private void receiveFilterValue(int band, Filter filter) {
        setFilter(band, filter, false);
    }

    /**
     * When we receive a value for one of the different parameters of a certain band.
     *
     * @param band
     *            The band for which we received a new parameter value.
     * @param value
     *            The new parameter value for the given band.
     * @param parameter
     *            The parameter for which we received a new value.
     */
    private void receiveParameterValue(int band, int value, Parameter parameter, SliderLayout mSliderLayout) {
        parameter.setValue(value);

        if (band == mBank.getNumberCurrentBand()) {
            updateDisplayParameterValue(mSliderLayout, parameter);
        }
    }

    /**
     * To update the parameters sliders which are linked to the current band.
     */
    private void updateDisplayParameters() {
        updateDisplayParameterValue(mSLFrequency, mBank.getCurrentBand().getFrequency());
        updateDisplayParameterValue(mSLGain, mBank.getCurrentBand().getGain());
        updateDisplayParameterValue(mSLQuality, mBank.getCurrentBand().getQuality());
    }

    /**
     * To refresh any slider layout UI with the corresponding parameter values.
     * 
     * @param sliderLayout
     *            The slider UI which has to be updated.
     * @param parameter
     *            The parameter values for the slider.
     */
    private void updateDisplayParameterValue(SliderLayout sliderLayout, Parameter parameter) {
        if (parameter.isConfigurable()) {
            sliderLayout.setEnabled(true);
            sliderLayout.setSliderProgress(parameter.getLengthValue());
            sliderLayout.displayValue(parameter.getLabelValue());
        } else {
            sliderLayout.setEnabled(false);
            sliderLayout.displayValue(parameter.getLabelValue());
        }
    }

    /**
     * To initialise alert dialogs to display with this activity.
     */
    private void buildDialogs() {
        // build the dialog to show a progress bar when we try to reconnect.
        AlertDialog.Builder incorrectStateDialogBuilder = new AlertDialog.Builder(CustomEqualizerActivity.this);
        incorrectStateDialogBuilder.setTitle(getString(R.string.dialog_incorrect_state_title));

        incorrectStateDialogBuilder.setMessage(getString(R.string.dialog_incorrect_state_message));
        incorrectStateDialogBuilder.setPositiveButton(getString(R.string.alert_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mIsIncorrectStateDialogDisplayed = false;
                        finish();
                    }
                });

        incorrectStateDialogBuilder.setCancelable(false);
        mIncorrectStateDialog = incorrectStateDialogBuilder.create();
    }

    /**
     * When we receive a value for the master gain of the configurable bank.
     *
     * @param masterGainValue
     *            The new master gain value for the bank.
     */
    private void receiveMasterGainValue(int masterGainValue) {
        mBank.getMasterGain().setValue(masterGainValue);
        mSLMasterGain.setSliderProgress(mBank.getMasterGain().getLengthValue());
        mSLMasterGain.displayValue(mBank.getMasterGain().getLabelValue());
    }

    /**
     * Called when we receive a packet about the command COMMAND_GET_EQ_CONTROL to know the current bank which is used
     * on the board.
     *
     * @param packet
     *            The packet about the received command COMMAND_GET_EQ_CONTROL.
     */
    private void receivePacketGetEqControl(GaiaPacket packet) {
        int selectedPreset = packet.getByte();
        mRecalculation = selectedPreset == PresetButton.PRESET_CUSTOM ? 0x01 : 0x00;
    }

    /**
     * To manage errors information catching inside the handler and coming from the library.
     *
     * @param error
     *            The error coming from the library formatting as a <code>GaiaError<</code>.
     */
    private void handleError(GaiaError error) {
        switch (error.getType()) {
        case SENDING_FAILED:
            String message;
            if (error.getCommand() > 0) {
                message = "Send command " + error.getCommand() + " failed";
            }
            else {
                message = "Send command failed";
            }
            if (DEBUG)
                Log.w(TAG, message + ": " + error.getStringException());
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            break;
        }
    }

    /**
     * To manage packets from Gaia device which are "PACKET" directly by the library.
     *
     * @param msg
     *            The message coming from the handler which calls this method.
     */
    private void handlePacket(Message msg) {
        GaiaPacket packet = (GaiaPacket) msg.obj;
        boolean validate = checkStatus(packet);

        if (validate) {

            switch (packet.getCommand()) {

            case Gaia.COMMAND_GET_EQ_PARAMETER:
                if (DEBUG)
                    Log.i(TAG, "Received \"COMMAND_GET_EQ_PARAMETER\" packet with a TRUE status.");
                receivePacketGetEQParameter(packet);
                break;

            case Gaia.COMMAND_GET_EQ_CONTROL:
                if (DEBUG)
                    Log.i(TAG, "Received \"COMMAND_GET_EQ_CONTROL\" packet with a TRUE status.");
                receivePacketGetEqControl(packet);
                break;

            default:
                // noinspection PointlessBooleanExpression,ConstantConditions
                if (DEBUG)
                    Log.d(TAG,
                            "Received packet - command: " + Utils.getIntToHexadecimal(packet.getCommandId())
                                    + " - status: " + packet.getStatus() + " - payload: "
                                    + Utils.getStringFromBytes(packet.getPayload()));
                break;

            }
        }

    }

    /**
     * To check the status of an acknowledgement packet.
     *
     * @param packet
     *            the packet to check.
     * @return true if the status is SUCCESS and the packet is an acknowledgment, false otherwise.
     */
    private boolean checkStatus(GaiaPacket packet) {
        if (!packet.isAcknowledgement()) {
            return false;
        }
        switch (packet.getStatus()) {
        case SUCCESS:
            return true;
        case NOT_SUPPORTED:
            receivePacketCommandNotSupported(packet);
            break;
        case AUTHENTICATING:
        case INCORRECT_STATE:
            // dialog to ask for streaming
            // boolean to know if we should display the
            if (!mIsIncorrectStateDialogDisplayed) {
                mIsIncorrectStateDialogDisplayed = true;
                mIncorrectStateDialog.show();
            }
            break;
        case INSUFFICIENT_RESOURCES:
        case INVALID_PARAMETER:
        case NOT_AUTHENTICATED:
        default:
            if (DEBUG)
                Log.w(TAG, "Status " + packet.getStatus().toString() + " with the command "
                        + Utils.getIntToHexadecimal(packet.getCommandId()));
        }
        return false;
    }

    /**
     * When we received a packet about a command which is not supported by the device.
     *
     * @param packet
     *            the concerned packet.
     */
    private void receivePacketCommandNotSupported(GaiaPacket packet) {
        switch (packet.getCommand()) {
        case Gaia.COMMAND_SET_EQ_PARAMETER:
        case Gaia.COMMAND_GET_EQ_PARAMETER:
            if (DEBUG)
                Log.d(TAG, "At least one of the commands for the EQ parameter feature is not supported.");
            finish();
            break;
        }
    }

    /**
     * The class which allows to manage messages from Gaia devices.
     */
    private static class GaiaHandler extends Handler {

        /**
         * The reference to this activity.
         */
        final WeakReference<CustomEqualizerActivity> mActivity;

        /**
         * The constructor for this activity.
         *
         * @param activity
         *            this activity.
         */
        public GaiaHandler(CustomEqualizerActivity activity) {
            super();
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            CustomEqualizerActivity parentActivity = mActivity.get();
            String handleMessage = "Handle a message from Gaia: ";
            GaiaLink.Message message = GaiaLink.Message.valueOf(msg.what);
            if (message == null) {
                if (DEBUG)
                    Log.d(TAG, handleMessage + "NULL");
                return;
            }
            switch (message) {
            case PACKET:
                parentActivity.handlePacket(msg);
                break;

            case DISCONNECTED:
                if (DEBUG)
                    Log.d(TAG, handleMessage + "DISCONNECTED");
                Toast.makeText(parentActivity, R.string.toast_disconnected, Toast.LENGTH_SHORT).show();
                parentActivity.finish();
                break;

            case ERROR:
                if (DEBUG)
                    Log.d(TAG, handleMessage + "ERROR");
                GaiaError error = (GaiaError) msg.obj;
                parentActivity.handleError(error);
                break;

            default:
                if (DEBUG)
                    Log.d(TAG, handleMessage + "OTHER MESSAGE");
                break;
            }
        }
    }
}
