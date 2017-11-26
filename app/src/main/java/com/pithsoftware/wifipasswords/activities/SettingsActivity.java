package com.pithsoftware.wifipasswords.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.pithsoftware.wifipasswords.R;
import com.pithsoftware.wifipasswords.dialogs.AboutDialogFragment;
import com.pithsoftware.wifipasswords.extras.AppCompatPreferenceActivity;
import com.pithsoftware.wifipasswords.extras.MyApplication;
import com.pithsoftware.wifipasswords.extras.RequestCodes;


public class SettingsActivity extends AppCompatPreferenceActivity {

    SettingsFragment mSettingsFragment;

    static final String SETTINGS_FRAGMENT_TAG = "settings_fragment_tag";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (MyApplication.sIsDark) {
            setTheme(R.style.AppTheme_Dark);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar;
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.action_settings));

        ActionBar sBar = getSupportActionBar();
        if (sBar != null) {
            sBar.setDisplayShowHomeEnabled(true);
            sBar.setDisplayHomeAsUpEnabled(true);
            sBar.setDisplayShowTitleEnabled(true);
        }


        if (savedInstanceState == null) {
            mSettingsFragment = new SettingsFragment();

        } else {
            mSettingsFragment = (SettingsFragment) getFragmentManager().findFragmentByTag(SETTINGS_FRAGMENT_TAG);
        }

        // Display the fragment as the main content
        getFragmentManager().beginTransaction()
                .replace(R.id.settings_frame, mSettingsFragment, SETTINGS_FRAGMENT_TAG).commit();


    }


    //Required Method to Override to Validated Fragments
    @Override
    protected boolean isValidFragment(String fragmentName) {

        return SettingsFragment.class.getName().equals(fragmentName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_help:
                AboutDialogFragment dialog = AboutDialogFragment.getInstance();
                dialog.show(getFragmentManager(), getString(R.string.dialog_about_key));
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    /***************************************************************/
    /****************** Settings Fragment **************************/
    /***************************************************************/
    public static class SettingsFragment extends PreferenceFragment {

        /***** Bind Summary to value - Listener *****/
        private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener
                = (preference, newValue) -> {

            getActivity().setResult(RESULT_OK);

            String stringValue = newValue.toString();

            if (preference instanceof EditTextPreference) {

                preference.setSummary(stringValue);

            } else if (preference instanceof ListPreference) {

                int index = ((ListPreference) preference).findIndexOfValue(stringValue);
                String summary = "";
                summary += ((ListPreference) preference).getEntries()[index];
                preference.setSummary(summary);
            }

            return true;
        };

        private void bindPreferenceSummaryToValue(Preference preference) {
            // Set the listener to watch for value changes.
            preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getActivity().setResult(RESULT_CANCELED);

            loadGeneralPreferences();

        }


        //Helper method for onCreate
        public void loadGeneralPreferences() {

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            findPreference(getString(R.string.pref_show_no_password_key)).setOnPreferenceClickListener(preference -> {
                getActivity().setResult(RequestCodes.SHOW_NO_PASSWORD_CODE);
                return true;
            });

            findPreference(getString(R.string.pref_dark_theme_key)).setOnPreferenceClickListener(preference -> {
                MyApplication.darkTheme((CheckBoxPreference) preference);
                getActivity().setResult(RequestCodes.DARK_THEME);
                getActivity().finish();
                return true;
            });

        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
        }

    }
}
