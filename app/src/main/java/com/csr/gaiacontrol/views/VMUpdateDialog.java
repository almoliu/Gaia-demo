/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.csr.gaiacontrol.views;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.csr.gaiacontrol.R;
import com.csr.gaiacontrol.utils.Consts;
import com.csr.vmupgradelibrary.codes.ResumePoints;

import java.text.DecimalFormat;

/**
 * This fragment allows to build a dialog to display information during the VM update.
 */

public class VMUpdateDialog extends DialogFragment {

    /**
     * The listener to interact with the fragment which implements this fragment.
     */
    private IDialogUpdateListener mListener;
    /**
     * The progress bar displayed to the user to show the transfer progress.
     */
    private ProgressBar mProgressBar;
    /**
     * The progress bar displayed to the user to for steps other than the DATA_TRANSFER.
     */
    private View mIndeterminateProgressBar;
    /**
     * The textView error to display the error message which corresponds to the error code.
     */
    private TextView mTVErrorMessage;
    /**
     * The text view to display the actual step.
     */
    private TextView mTVStep;
    /**
     * The text view to display a percentage during a process.
     */
    private TextView mTVPercentage;
    /**
     * The text view to display the dialog title.
     */
    private TextView mTVTitle;
    /**
     * The view to display information about data transfer.
     */
    private View mLTransfer;
    /**
     * The text view to display the time during the transfer.
     */
    private TextView mTVTime;
    /**
     * The view to display an error.
     */
    private View mLError;
    /**
     * The view to inform the user the error is coming from the board.
     */
    private TextView mTVErrorAppOrBoard;
    /**
     * The text view to display the error code.
     */
    private TextView mTVErrorCode;
    /**
     * To display a number in a specific decimal format.
     */
    private final DecimalFormat mDecimalFormat = new DecimalFormat();

    /**
     * The factory method to create a new instance of this fragment using the provided parameters.
     *
     * @return A new instance of fragment VMUpdateDialog.
     */
    public static VMUpdateDialog newInstance(IDialogUpdateListener listener) {
        VMUpdateDialog fragment = new VMUpdateDialog();
        fragment.setListener(listener);
        return fragment;
    }

    /**
     * Constructor.
     */
    public VMUpdateDialog() {
        super();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // the central view: no other choice than "null" for the last parameter, see Android developer documentation.
        @SuppressLint("InflateParams") View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_update_in_progress, null);
        builder.setView(view);
        // the abort button
        builder.setNegativeButton(R.string.update_abort, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clear();
                if (mListener != null) {
                    mListener.abortUpdate();
                }
            }
        });
        // the user can not dismiss the dialog using the back button.
        setCancelable(false);

        init(view);

        return builder.create();
    }

    /**
     * To display a percentage number during the Step DATA_TRANSFER.
     *
     * @param percentage
     *                  The percentage to display, between 0 and 100.
     */
    public void displayTransferProgress(double percentage, String time) {
        if (this.isAdded() || this.isVisible()) {
            if (percentage < 0) {
                percentage = 0;
            }
            else if (percentage > 100) {
                percentage = 100;
            }
            String percentageText = mDecimalFormat.format(percentage) + " " + Consts.PERCENTAGE_CHARACTER;
            mTVPercentage.setText(percentageText);
            mProgressBar.setProgress((int) percentage);

            mTVTime.setText(time);
        }
    }

    /**
     * To display an error message.
     */
    public void displayError(String code, String message) {
        mLError.setVisibility(View.VISIBLE);
        mTVErrorAppOrBoard.setText(getResources().getString(R.string.update_error_from_board));
        mIndeterminateProgressBar.setVisibility(View.GONE);

        if (code.length() > 0) {
            mTVErrorCode.setVisibility(View.VISIBLE);
            mTVErrorCode.setText(code);
        }
        else {
            mTVErrorCode.setVisibility(View.GONE);
        }
        if (message.length() > 0) {
            mTVErrorMessage.setVisibility(View.VISIBLE);
            mTVErrorMessage.setText(message);
        }
        else {
            mTVErrorMessage.setVisibility(View.GONE);
        }
    }

    /**
     * To display a specific message as an error.
     *
     * @param message
     *              THe specific message to display.
     */
    public void displayError(String message) {
        mLError.setVisibility(View.VISIBLE);
        mTVErrorCode.setVisibility(View.GONE);
        mTVErrorMessage.setVisibility(View.GONE);
        mTVErrorAppOrBoard.setText(message);
        mIndeterminateProgressBar.setVisibility(View.GONE);
    }

    /**
     * To update the view depending on the actual step.
     */
    public void updateStep() {
        ResumePoints step = mListener.getResumePoint();
        int stepNumber = step == null ? 0 : step.ordinal() + 1;
        mTVStep.setText(ResumePoints.getLabel(mListener.getResumePoint()));
        String titleText = "Update - Step " + stepNumber + " of " + ResumePoints.getLength();
        mTVTitle.setText(titleText);

        if (step == ResumePoints.DATA_TRANSFER) {
            mLTransfer.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            mIndeterminateProgressBar.setVisibility(View.GONE);
        }
        else {
            mLTransfer.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
            mIndeterminateProgressBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * To clear the content on this view.
     */
    private void clear () {
        updateStep();
        hideError();
    }

    /**
     * To hide the error message.
     */
    private void hideError() {
        mLError.setVisibility(View.GONE);
    }

    /**
     * This method allows to initialize components.
     *
     * @param view
     *            The inflated view for this fragment.
     */
    private void init(View view) {
        mTVTitle = (TextView) view.findViewById(R.id.tv_update_title);
        mTVStep = (TextView) view.findViewById(R.id.tv_step);
        mLTransfer = view.findViewById(R.id.ll_transfer);
        mTVPercentage = (TextView) view.findViewById(R.id.tv_percentage);
        mTVTime = (TextView) view.findViewById(R.id.tv_time);
        mProgressBar = (ProgressBar) view.findViewById(R.id.pb_update);
        mIndeterminateProgressBar = view.findViewById(R.id.pb_update_indeterminate);
        mLError = view.findViewById(R.id.rl_error);
        mTVErrorAppOrBoard = (TextView) view.findViewById(R.id.tv_update_error_from_board);
        mTVErrorCode = (TextView) view.findViewById(R.id.tv_update_error_code);
        mTVErrorMessage = (TextView) view.findViewById(R.id.tv_update_error_message);

        mDecimalFormat.setMaximumFractionDigits(1);

        clear();
    }

    /**
     * To define the listener for actions on this dialog. We can't use the onAttach method to define a listener: here
     * the listener is a fragment.
     *
     * @param listener
     *            The listener which will listen this dialog.
     */
    private void setListener(IDialogUpdateListener listener) {
        this.mListener = listener;
    }

    /**
     * This interface allows this Dialog fragment to communicate with its listener.
     */
    public interface IDialogUpdateListener {
        /**
         * To abort the update.
         */
        void abortUpdate();


        /**
         * To know the step for the dialog to display.
         *
         * @return
         *          The actual resume point to display the step into the dialog.
         */
        ResumePoints getResumePoint();
    }
}