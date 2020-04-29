package com.example.IMUControl;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the scopes or camera view.
 */
class CameraScopeSectionsPagerAdapter extends FragmentPagerAdapter {

    public CameraScopeSectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a CameraScopeFragment (defined as a static inner class below).
        return CameraScopeFragment.newInstance(position + 1);
    }

    @Override
    public int getCount() {
        // Show 1 total pages.
        return 1;
    }

}
