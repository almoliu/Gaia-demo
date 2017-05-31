/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.csr.gaiacontrol.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import com.csr.gaiacontrol.R;

/**
 * To personalize buttons depending on their state & their proprieties.
 */
public class PresetButton extends Button {

    /**
     * The drawable to use when the button is selected.
     */
    private int mSelectedDrawable;
    /**
     * The drawable to use when the button is unselected.
     */
    private int mUnselectedDrawable;
    /**
     * The preset which fits with this button.
     */
    private int mPreset;

    /**
     * The total number of presets for the equalizer.
     */
    public static final int PRESET_CUSTOM = 1;

    public PresetButton(Context context) {
        super(context);
        init();
    }

    public PresetButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PresetButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * To mark this preset button as selected.
     *
     * @param selected
     *              true if the button is selected, false otherwise.
     */
    @SuppressWarnings("deprecation")
    public void selectButton (boolean selected) {
        setSelected(selected);

        if (getPreset() != PRESET_CUSTOM) {
            setEnabled(!selected);
        }

        if (selected) {
            setCompoundDrawablesWithIntrinsicBounds(0, mSelectedDrawable, 0, 0);
            setTextColor(getResources().getColor(R.color.primary_text));
            setBackgroundColor(getResources().getColor(R.color.material_deep_orange_50));
        } else {
            setCompoundDrawablesWithIntrinsicBounds(0, mUnselectedDrawable, 0, 0);
            setTextColor(getResources().getColor(R.color.secondary_text));
            //noinspection deprecation
            setBackground(getResources().getDrawable(R.drawable.flat_button_preset_background));
        }
    }

    /**
     * To return the preset which fits with this button.
     *
     * @return the value for the button preset.
     */
    public int getPreset() {
        return mPreset;
    }

    /**
     * To initialize fields depending on which preset button it is.
     */
    private void init() {
        switch (this.getId()) {
            case R.id.bt_preset_0:
                mSelectedDrawable = R.drawable.ic_preset_default;
                mUnselectedDrawable = R.drawable.ic_preset_default_light;
                mPreset = 0;
                break;
            case R.id.bt_preset_1:
                mSelectedDrawable = R.drawable.ic_preset_custom;
                mUnselectedDrawable = R.drawable.ic_preset_custom_light;
                mPreset = PRESET_CUSTOM;
                break;
            case R.id.bt_preset_2:
                mSelectedDrawable = R.drawable.ic_preset_rock;
                mUnselectedDrawable = R.drawable.ic_preset_rock_light;
                mPreset = 2;
                break;
            case R.id.bt_preset_3:
                mSelectedDrawable = R.drawable.ic_preset_jazz;
                mUnselectedDrawable = R.drawable.ic_preset_jazz_light;
                mPreset = 3;
                break;
            case R.id.bt_preset_4:
                mSelectedDrawable = R.drawable.ic_preset_folk;
                mUnselectedDrawable = R.drawable.ic_preset_folk_light;
                mPreset = 4;
                break;
            case R.id.bt_preset_5:
                mSelectedDrawable = R.drawable.ic_preset_pop;
                mUnselectedDrawable = R.drawable.ic_preset_pop_light;
                mPreset = 5;
                break;
            case R.id.bt_preset_6:
                mSelectedDrawable = R.drawable.ic_preset_classic;
                mUnselectedDrawable = R.drawable.ic_preset_classic_light;
                mPreset = 6;
                break;
        }
    }
}
