/**************************************************************************************************
 * Copyright 2015 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/
package com.csr.gaiacontrol.fragments;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.csr.gaiacontrol.R;
import com.csr.gaiacontrol.adapters.DevicesListAdapter;
import com.csr.gaiacontrol.views.DividerItemDecoration;

/**
 * This fragment allows to control the view for the connection part to a GAIA device.
 */
public class ConnectionFragment extends Fragment implements DevicesListAdapter.IListAdapterListener,
        View.OnClickListener {

    /**
     * The listener to interact with the activity which implements this fragment.
     */
    private IConnectionFragmentListener mActivityListener;
    /**
     * The adapter used by the RecyclerView for the devices list.
     */
    private DevicesListAdapter mDevicesListAdapter;
    /**
     * The button to start the connection using GAIA.
     */
    private Button mStartButton;
    /**
     * To have an instance to access to the layout which displays the progress bar.
     */
    private View mLayoutProgressBar;
    /**
     * To have an instance to access to the layout which displays the progress bar.
     */
    private View mLayoutNoDevice;

    /**
     * The factory method to create a new instance of this fragment using the provided parameters.
     *
     * @return A new instance of fragment ConnectionFragment.
     */
    public static ConnectionFragment newInstance() {
        return new ConnectionFragment();
    }

    /**
     * Empty constructor - required.
     */
    public ConnectionFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_connection, container, false);
        this.init(view);
        return view;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mActivityListener = (IConnectionFragmentListener) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement IConnectionFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivityListener = null;
    }

    @Override
    public void onClick(View v) {
        if (validate()) {
            mActivityListener.connect(mDevicesListAdapter.getSelectedItem());
        }
    }

    /**
     * <p>This interface allows to communicate with this fragment by implementing it.<p/> This interface must be
     * implemented by activities that contains it to interact with it.
     */
    public interface IConnectionFragmentListener {
        /**
         * This method allows to start an handling when the Fragment is ready.
         */
        void start();

        /**
         * This method allows to start the connection to the Gaia device selected by the user using the transport also
         * selected by the user.
         *
         * @param device
         *            The selected device by an user.
         */
        void connect(BluetoothDevice device);
    }

    // When the fragment is resumed.
    @Override
    public void onResume() {
        super.onResume();
        mActivityListener.start();
    }

    public void onDeviceItemSelected() {
        activateStartButton();
    }

    /**
     * This method allows to update the list of devices with a new devices set.
     * 
     * @param listDevices
     *            The new list of devices.
     */
    public void setListDevices(Set<BluetoothDevice> listDevices) {
        displayMessageNoDevice(listDevices.size() < 1);
        mDevicesListAdapter.setListDevices(listDevices);
    }

    /**
     * This method allows to display a progress bar to indicate to the user that the application attempts to connect.
     *
     * @param activate
     *            true if we have to display the progress bar, false otherwise.
     */
    public void displayProgressBar(boolean activate) {
        int visibility = activate ? View.VISIBLE : View.GONE;
        mLayoutProgressBar.setVisibility(visibility);
        this.enabledComponents(!activate);
    }

    /**
     * This method allows to display a message to indicate to the user that he needs to pair devices to use this application.
     *
     * @param activate
     *            true if we have to display the message, false otherwise.
     */
    private void displayMessageNoDevice(boolean activate) {
        int visibility = activate ? View.VISIBLE : View.GONE;
        mLayoutNoDevice.setVisibility(visibility);
    }

    /**
     * This method allows to enable or disable components for the user interaction with.
     * 
     * @param activate
     *            true to enable the components, otherwise false.
     */
    private void enabledComponents(boolean activate) {
        mDevicesListAdapter.setEnabled(activate);
        mStartButton.setEnabled(activate && validate());
    }

    /**
     * This method allows to initialize components.
     *
     * @param view
     *            The inflated view for this fragment.
     */
    private void init(View view) {
        mLayoutProgressBar = view.findViewById(R.id.l_progress_bar);
        mLayoutNoDevice = view.findViewById(R.id.tv_no_available_device);
        mStartButton = (Button) view.findViewById(R.id.bt_start);
        mStartButton.setOnClickListener(this);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_devices_list);

        // use a linear layout manager
        LinearLayoutManager devicesListLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(devicesListLayoutManager);
        recyclerView.setHasFixedSize(true);

        // add a divider to the list
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));

        // specify an adapter (see also next example)
        mDevicesListAdapter = new DevicesListAdapter(this);
        recyclerView.setAdapter(mDevicesListAdapter);
    }

    /**
     * This methods allows to activate the click on the button for the user.
     */
    private void activateStartButton() {
        mStartButton.setEnabled(validate());
    }

    /**
     * To check if the user has selected all needed information inside the form connection.
     */
    private boolean validate() {
        return mDevicesListAdapter.hasSelection();
    }

}
