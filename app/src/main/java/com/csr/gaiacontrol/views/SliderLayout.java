/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.csr.gaiacontrol.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.csr.gaiacontrol.R;

/**
 * This class extends the LinearLayout class to be able to access easily to some UI components which are used many times in the UI.
 */

public class SliderLayout extends LinearLayout {

    /**
     * The slider for this layout.
     */
    private SeekBar mSeekBar;
    /**
     * The text view to display the current value from the slider.
     */
    private TextView mTVValue;
    /**
     * The title for this view.
     */
    private TextView mTVTitle;
    /**
     * The text view to display the minimum value for the slider.
     */
    private TextView mTVMinValue;
    /**
     * The text view to display the maximum value for the slider.
     */
    private TextView mTVMaxValue;

    /**
     * The listener with which to interact when the user is using the slider.
     */
    private SliderListener mListener;

    public SliderLayout(Context context) {
        super(context);
    }

    public SliderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SliderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mSeekBar = (SeekBar) findViewById(R.id.sb_slider);
        mTVValue = (TextView) findViewById(R.id.tv_value);
        mTVTitle = (TextView) findViewById(R.id.tv_title);
        mTVMinValue = (TextView) findViewById(R.id.tv_slider_min_value);
        mTVMaxValue = (TextView) findViewById(R.id.tv_slider_max_value);
    }

    /**
     * To enable or disable the view.
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        mSeekBar.setEnabled(enabled);
        mTVValue.setEnabled(enabled);
        mTVMinValue.setEnabled(enabled);
        mTVMaxValue.setEnabled(enabled);
        mTVTitle.setEnabled(enabled);
    }

    /**
     * To display the label for the current value.
     */
    public void displayValue(String valueString) {
        mTVValue.setText(valueString);
    }

    /**
     * To hide the title if it didn't need to be displayed.
     */
    public void hideTitle() {
        mTVTitle.setVisibility(GONE);
    }

    /**
     * To define the current progress for the slider.
     *
     * @param valueInt
     *          the value to set which has to be between 0 and 100.
     */
    public void setSliderProgress(int valueInt) {
        if (valueInt < 0) {
            valueInt = 0;
        }
        else if (valueInt > mSeekBar.getMax()) {
            valueInt = mSeekBar.getMax();
        }

        mSeekBar.setProgress(valueInt);
    }

    /**
     * To define the default values to display for the view.
     *
     * @param title
     *          The view title.
     * @param value
     *          The label for the current value.
     * @param listener
     *          The listener to use to interact with.
     */
    public void initialize (String title, String value, SliderListener listener) {
        mTVTitle.setText(title);
        mTVValue.setText(value);
        mListener = listener;

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    mListener.onProgressChangedByUser(i, getId());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mListener.onStopTrackingTouch(seekBar.getProgress(), getId());
            }
        });

    }

    /**
     * To define the labels for the minimum and the maximum values of the slider.
     *
     * @param minText
     *              the label for the minimum value.
     * @param maxText
     *              the label for the maximum value.
     */
    public void setSliderBounds(int length, String minText, String maxText) {
        mTVMinValue.setText(minText);
        mTVMaxValue.setText(maxText);
        mSeekBar.setMax(length);
    }

    /**
     *  The listener to interact with when the user is using the slider.
     */
    public interface SliderListener {
        /**
         * This method is called when the user is touching the slider and changing the value by sliding.
         *
         * @param progress
         *          the new value of the progress on the slider.
         * @param id
         *          The ID of the SliderLayout which is calling the listener.
         */
        void onProgressChangedByUser(int progress, int id);

        /**
         * This method is called when the user stops to interact with the slider.
         *
         * @param progress
         *          the new value of the progress on the slider.
         * @param id
         *          The ID of the SliderLayout which is calling the listener.
         */
        void onStopTrackingTouch (int progress, int id);
    }
}
