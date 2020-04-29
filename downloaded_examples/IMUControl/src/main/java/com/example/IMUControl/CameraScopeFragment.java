package com.example.IMUControl;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing camera or a scope.
 */
public class CameraScopeFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private int sectionNumber;
    private OnFragmentInteractionListener mListener1;

    public CameraScopeFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static CameraScopeFragment newInstance(int sectionNumber) {
        CameraScopeFragment fragment = new CameraScopeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = null;
        sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
        switch (sectionNumber) {
            case 1:
                rootView = inflater.inflate(R.layout.chart_1, container, false);
                break;
        }

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener1 = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener1 = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        mListener1.onFragmentStart("dot" + sectionNumber);
    }

    @Override
    public void onResume() {
        super.onResume();
        mListener1.onFragmentResume("dot" + sectionNumber);
    }

    @Override
    public void onPause() {
        super.onPause();
        mListener1.onFragmentPause("dot" + sectionNumber);
    }
}
