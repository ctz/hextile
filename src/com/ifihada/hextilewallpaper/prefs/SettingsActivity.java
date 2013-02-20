package com.ifihada.hextilewallpaper.prefs;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;

import java.util.List;

import com.ifihada.hextilewallpaper.HextileService;
import com.ifihada.hextilewallpaper.R;

public class SettingsActivity extends PreferenceActivity
{
  public static final String SIZE_PREF = "size";
  public static final String GAP_PREF = "gap";
  
  @Override
  protected void onPostCreate(Bundle savedInstanceState)
  {
    super.onPostCreate(savedInstanceState);
    setupSimplePreferencesScreen();
  }

  private void setupSimplePreferencesScreen()
  {
    addPreferencesFromResource(R.xml.pref_general);

    PreferenceCategory fakeHeader = new PreferenceCategory(this);
    fakeHeader.setTitle(R.string.pref_header_layout);
    getPreferenceScreen().addPreference(fakeHeader);
    addPreferencesFromResource(R.xml.pref_layout);

    /*
    fakeHeader = new PreferenceCategory(this);
    fakeHeader.setTitle(R.string.pref_header_colours);
    getPreferenceScreen().addPreference(fakeHeader);
    addPreferencesFromResource(R.xml.pref_colours);
    */

    bindPreferenceSummaryToValue(findPreference(SIZE_PREF));
    bindPreferenceSummaryToValue(findPreference(GAP_PREF));
    // bindPreferenceSummaryToValue(findPreference("colours"));
  }

  /**
   * A preference value change listener that updates the preference's summary to
   * reflect its new value.
   */
  private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener()
  {
    @Override
    public boolean onPreferenceChange(Preference preference, Object value)
    {
      String stringValue = value.toString();

      if (preference instanceof ListPreference)
      {
        // For list preferences, look up the correct display value in
        // the preference's 'entries' list.
        ListPreference listPreference = (ListPreference) preference;
        int index = listPreference.findIndexOfValue(stringValue);

        // Set the summary to reflect the new value.
        preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

      } else {
        // For all other preferences, set the summary to the value's
        // simple string representation.
        preference.setSummary(stringValue);
      }
      
      HextileService.applySetting(preference.getKey(),
                                  PreferenceManager
                                    .getDefaultSharedPreferences(preference.getContext())
                                    .getString(preference.getKey(), ""));
      return true;
    }
  };

  private static void bindPreferenceSummaryToValue(Preference preference)
  {
    // Set the listener to watch for value changes.
    preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

    // Trigger the listener immediately with the preference's
    // current value.
    sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                                                             PreferenceManager
                                                                 .getDefaultSharedPreferences(preference.getContext())
                                                                 .getString(preference.getKey(), ""));
  }
}
