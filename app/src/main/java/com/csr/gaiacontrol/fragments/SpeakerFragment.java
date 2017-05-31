/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/
package com.csr.gaiacontrol.fragments;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.csr.gaiacontrol.R;

/**
 * This fragment allows to control a speaker view.
 */
public class SpeakerFragment extends Fragment {

    /**
     * The listener to interact with the activity which implements this fragment.
     */
    private ISpeakerFragmentListener mActivityListener;
    /**
     * The text view which displays the speaker name.
     */
    private TextView mTVTitle;
    /**
     * The speaker name.
     */
    private String mTitle;
    /**
     * The number which fits to this speaker.
     */
    private int mSpeakerValue;
    /**
     * The seek bar which allows the user to change the volume.
     */
    private SeekBar mSBVolume;
    /**
     * The channel which is selected and displayed to the user.
     */
    private Channel mChannel = Channel.MONO;
    /**
     * The text view to display the channel name.
     */
    private TextView mTVChannel;
    /**
     * The left speaker picture.
     */
    private ImageView mIVLeftSpeaker;
    /**
     * The right speaker picture.
     */
    private ImageView mIVRightSpeaker;
    /**
     * To know if the volume is disabled because this feature is not supported.
     */
    private boolean volumeDisabled = true;
    /**
     * To know if the channel is disabled because this feature is not supported.
     */
    private boolean channelDisabled = true;

    private View mVChanelLabel;
    private View mVChanelSpeakers;
    private View mVVolume;
    private View mVFragment;

    /**
     * All channel values.
     */
    private enum Channel {
        STEREO, LEFT, RIGHT, MONO;

        /**
         * To keep constantly this array without calling the values() method which is copying an array when it's called.
         */
        private static final Channel[] values = Channel.values();

        /**
         * To retrieve the channel which is following the one from where this method has been called.
         * 
         * @return the next channel
         */
        private Channel getNextChannel() {
            int next = (this.ordinal() + 1) % values.length;
            return values[next];
        }

        /**
         * To know the channel which follows the one from where this method has been called.
         *
         * @return the previous channel
         */
        private Channel getPreviousChannel() {
            int thisChannel = this.ordinal();
            int previous = thisChannel == 0 ? values.length - 1 : thisChannel - 1;
            return values[previous];
        }

        /**
         * To obtain the channel depending on its ordinal value from this enumeration.
         *
         * @param value
         *              the ordinal value.
         *
         * @return
         *          The channel.
         */
        private static Channel getChannelFromInt (int value) {
            if (value < 0 || value >= values.length) {
                return null;
            }
            return values[value];
        }
    }

    /**
     * Empty constructor - required.
     */
    public SpeakerFragment() {
    }

    // When the view is created.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_speaker, container, false);
        this.init(view);
        return view;
    }

    // When the view has been created.
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateChannelDisplay();
    }

    // When the fragment is attached to an activity.
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mActivityListener = (ISpeakerFragmentListener) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ISpeakerFragmentListener");
        }
    }

    // When the view is inflated inside the activity.
    @SuppressWarnings("deprecation")
    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);

        if (attrs != null) {
            final TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.SpeakerFragment);
            mTitle = a.getString(R.styleable.SpeakerFragment_speakerTitle);
            if (mTVTitle != null)
                mTVTitle.setText(mTitle);
            a.recycle();
        }

    }

    // When the fragment his detached from its activity.
    @Override
    public void onDetach() {
        super.onDetach();
        mActivityListener = null;
    }

    /**
     * To define the speaker value as master or slave.
     *
     * @param value
     *            the value for this speaker.
     */
    public void setSpeakerValue(int value) {
        mSpeakerValue = value;
    }

    /**
     * To define the volume to display.
     *
     * @param value
     *            the volume value to display.
     */
    public void setVolume(int value) {
        if (volumeDisabled) {
            volumeDisabled = false;
            enableVolume();
        }
        mSBVolume.setProgress(value);
    }

    /**
     * To define the channel to display.
     *
     * @param channel
     *              the channel value to display.
     */
    public void setChannel(int channel) {
        if (channelDisabled) {
            channelDisabled = false;
            enableChannel();
        }
        mChannel = Channel.getChannelFromInt(channel);
        updateChannelDisplay();
    }

    /**
     * To enable the channel feature.
     */
    private void enableChannel() {
        mVFragment.setVisibility(View.VISIBLE);
        mVChanelLabel.setVisibility(View.VISIBLE);
        mVChanelSpeakers.setVisibility(View.VISIBLE);
    }

    /**
     * To enable the volume feature.
     */
    private void enableVolume() {
        mVFragment.setVisibility(View.VISIBLE);
        mVVolume.setVisibility(View.VISIBLE);
    }

    /**
     * This method allows to initialize components.
     *
     * @param view
     *            The inflated view for this fragment.
     */
    private void init(View view) {
        mTVTitle = (TextView) view.findViewById(R.id.tv_speaker_name);
        if (mTitle != null)
            mTVTitle.setText(mTitle);

        mSBVolume = (SeekBar) view.findViewById(R.id.sb_volume);
        mSBVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // nothing to do
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // nothing to do
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // on stop: send the new value to the speaker
                mActivityListener.sendVolume(mSpeakerValue, mSBVolume.getProgress());
            }
        });

        View.OnClickListener leftListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPreviousChannel();
            }
        };
        View.OnClickListener rightListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNextChannel();
            }
        };

        mIVLeftSpeaker = (ImageView) view.findViewById(R.id.iv_speaker_left);
        mIVLeftSpeaker.setOnClickListener(leftListener);
        mIVRightSpeaker = (ImageView) view.findViewById(R.id.iv_speaker_right);
        mIVRightSpeaker.setOnClickListener(rightListener);
        mTVChannel = (TextView) view.findViewById(R.id.tv_channel);

        mVChanelLabel = view.findViewById(R.id.ll_channel_label);
        mVVolume = view.findViewById(R.id.ll_volume);
        mVChanelSpeakers = view.findViewById(R.id.ll_channel_speakers);
        mVFragment = view;

        ImageButton btLeft = (ImageButton) view.findViewById(R.id.ib_arrow_left);
        btLeft.setOnClickListener(leftListener);
        ImageButton btRight = (ImageButton) view.findViewById(R.id.ib_arrow_right);
        btRight.setOnClickListener(rightListener);
    }

    /**
     * To define the channel as the previous one on the enumeration.
     */
    private void setPreviousChannel() {
        mChannel = mChannel.getPreviousChannel();
        updateChannelDisplay();
        mActivityListener.sendChannel(mSpeakerValue, mChannel.ordinal());
    }

    /**
     * To define the channel as the next one on the enumeration.
     */
    private void setNextChannel() {
        mChannel = mChannel.getNextChannel();
        updateChannelDisplay();
        mActivityListener.sendChannel(mSpeakerValue, mChannel.ordinal());
    }

    /**
     * To update the channel display depending on the actual selected channel.
     */
    private void updateChannelDisplay() {
        switch (mChannel) {
        case STEREO:
            mIVLeftSpeaker.setImageResource(R.drawable.ic_speaker_blue);
            mIVRightSpeaker.setImageResource(R.drawable.ic_speaker_blue);
            mTVChannel.setText(getString(R.string.tws_stereo));
            break;
        case LEFT:
            mIVLeftSpeaker.setImageResource(R.drawable.ic_speaker_blue);
            mIVRightSpeaker.setImageResource(R.drawable.ic_speaker_grey);
            mTVChannel.setText(getString(R.string.tws_left_channel));
            break;
        case RIGHT:
            mIVLeftSpeaker.setImageResource(R.drawable.ic_speaker_grey);
            mIVRightSpeaker.setImageResource(R.drawable.ic_speaker_blue);
            mTVChannel.setText(getString(R.string.tws_right_channel));
            break;
        case MONO:
            mIVLeftSpeaker.setImageResource(R.drawable.ic_speaker_blue);
            mIVRightSpeaker.setImageResource(R.drawable.ic_speaker_blue);
            mTVChannel.setText(getString(R.string.tws_mono));
            break;
        }
    }

    /**
     * <p>This interface allows this fragment to communicate with its attached activity which has to implement it.</p>
     */
    public interface ISpeakerFragmentListener {

        /**
         * To send the new volume to the speaker.
         *
         * @param speaker
         *            The number from which fits to this speaker: 0x00 for the master, 0x01 for the slave, etc.
         * @param volume
         *            The new volume to send to the speaker.
         */
        void sendVolume(int speaker, int volume);

        /**
         * To send the selected to the speaker.
         * 
         * @param speaker
         *            The number from which fits to this speaker: 0x00 for the master, 0x01 for the slave, etc.
         * @param channel
         *            The new channel for the speaker.
         */
        void sendChannel(int speaker, int channel);
    }

}
