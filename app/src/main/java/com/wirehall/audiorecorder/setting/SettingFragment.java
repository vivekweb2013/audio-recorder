package com.wirehall.audiorecorder.setting;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.wirehall.audiorecorder.R;
import com.wirehall.audiorecorder.setting.pathpref.PathPrefDialog;
import com.wirehall.audiorecorder.setting.pathpref.PathPreference;

public class SettingFragment extends PreferenceFragmentCompat {
    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        //SO: https://stackoverflow.com/questions/18509369/android-how-to-get-remove-margin-padding-in-preference-screen
        //Bug: https://issuetracker.google.com/issues/111907042
        super.setPreferenceScreen(preferenceScreen);
        if (preferenceScreen != null) {
            int count = preferenceScreen.getPreferenceCount();
            for (int i = 0; i < count; i++)
                preferenceScreen.getPreference(i).setIconSpaceReserved(false);
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof PathPreference) {
            DialogFragment pathPrefDialog = PathPrefDialog.newInstance(preference.getKey());
            pathPrefDialog.setTargetFragment(this, 0);
            pathPrefDialog.show(getParentFragmentManager(), null);
        } else super.onDisplayPreferenceDialog(preference);
    }

}
