package com.example.IMUControl;

/* Copyright 2016 The MathWorks, Inc. */

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;


public class AppFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public AppFragment() {}

    public static AppFragment newInstance() {
        AppFragment fragment = new AppFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListener.onFragmentCreate("App");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_app, container, false);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        mListener.onFragmentStart("App");
    }

    @Override
    public void onResume() {
        super.onResume();
        mListener.onFragmentResume("App");
    }

    @Override
    public void onPause() {
        super.onPause();
        mListener.onFragmentPause("App");
    }
}
