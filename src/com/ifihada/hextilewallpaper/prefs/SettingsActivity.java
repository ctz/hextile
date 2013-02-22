package com.ifihada.hextilewallpaper.prefs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import yuku.ambilwarna.widget.AmbilWarnaPreference;

import com.ifihada.hextilewallpaper.Colour;
import com.ifihada.hextilewallpaper.Config;
import com.ifihada.hextilewallpaper.HextileService;
import com.ifihada.hextilewallpaper.R;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity
{
  public static final String SIZE_PREF = "size";
  public static final String GAP_PREF = "gap";
  public static final String BASE_COLOUR_PREF = "base_colour";
  public static final String FEAT_COLOUR_PREF_1 = "feat_colour_1";
  public static final String FEAT_COLOUR_PREF_2 = "feat_colour_2";
  public static final String FEAT_COLOUR_PREF_3 = "feat_colour_3";
  public static final String FEAT_COLOUR_PREF_4 = "feat_colour_4";
  public static final String FEAT_COLOUR_PREF_5 = "feat_colour_5";
  public static final String RESET_COLOURS = "reset_colours";
  
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

    fakeHeader = new PreferenceCategory(this);
    fakeHeader.setTitle(R.string.pref_header_colours);
    getPreferenceScreen().addPreference(fakeHeader);
    addPreferencesFromResource(R.xml.pref_colours);

    bindPreferenceSummaryToValue(findPreference(SIZE_PREF));
    bindPreferenceSummaryToValue(findPreference(GAP_PREF));
    this.bindColourPrefs();
    this.updateResetButton();
    
    findPreference(RESET_COLOURS).setOnPreferenceClickListener(new OnPreferenceClickListener()
    {
      @Override
      public boolean onPreferenceClick(Preference preference)
      {
        new AlertDialog.Builder(SettingsActivity.this)
            .setIcon(android.R.drawable.ic_menu_delete)
            .setTitle(R.string.really_reset_colours_title)
            .setMessage(R.string.really_reset_colours)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which)
              {
                SettingsActivity.this.resetColours();
              }
            })
            .show();
        return true;
      }
    });
  }
  
  private void bindColourPrefs()
  {
    Preference.OnPreferenceChangeListener bindPreferenceColour = new Preference.OnPreferenceChangeListener()
    {
      @Override
      public boolean onPreferenceChange(Preference pref, Object value)
      {
        if (!(pref instanceof AmbilWarnaPreference))
          return false;
        
        Colour c = Colour.fromARGB((Integer) value);
        pref.setSummary(c.toString());

        HextileService.applySetting(pref.getKey(), value.toString());
        SettingsActivity.this.updateResetButton();
        return true;
      }
    };
    
    bindColourPreference(findPreference(BASE_COLOUR_PREF), bindPreferenceColour);
    bindColourPreference(findPreference(FEAT_COLOUR_PREF_1), bindPreferenceColour);
    bindColourPreference(findPreference(FEAT_COLOUR_PREF_2), bindPreferenceColour);
    bindColourPreference(findPreference(FEAT_COLOUR_PREF_3), bindPreferenceColour);
    bindColourPreference(findPreference(FEAT_COLOUR_PREF_4), bindPreferenceColour);
    bindColourPreference(findPreference(FEAT_COLOUR_PREF_5), bindPreferenceColour);
  }
  
  private boolean coloursAreDefaults()
  {
    return Config.getBaseColour().equalsOrClose(Config.DEFAULT_BASE_COLOUR) &&
        Config.getFeatureColour(0).equalsOrClose(Config.DEFAULT_FEAT_COLOUR_1) &&
        Config.getFeatureColour(1).equalsOrClose(Config.DEFAULT_FEAT_COLOUR_2) &&
        Config.getFeatureColour(2).equalsOrClose(Config.DEFAULT_FEAT_COLOUR_3) &&
        Config.getFeatureColour(3).equalsOrClose(Config.DEFAULT_FEAT_COLOUR_4) &&
        Config.getFeatureColour(4).equalsOrClose(Config.DEFAULT_FEAT_COLOUR_5);
  }
  
  private void updateResetButton()
  {
    Preference p = findPreference(RESET_COLOURS);
    p.setEnabled(!this.coloursAreDefaults());
  }
  
  private void setColour(String key, Colour value)
  {
    Preference pref = findPreference(key);
    if (pref == null || !(pref instanceof AmbilWarnaPreference))
      return;
    
    ((AmbilWarnaPreference) pref).forceSetValue(value.getARGB());    
  }
  
  private void resetColours()
  {
    setColour(BASE_COLOUR_PREF, Config.DEFAULT_BASE_COLOUR);
    setColour(FEAT_COLOUR_PREF_1, Config.DEFAULT_FEAT_COLOUR_1);
    setColour(FEAT_COLOUR_PREF_2, Config.DEFAULT_FEAT_COLOUR_2);
    setColour(FEAT_COLOUR_PREF_3, Config.DEFAULT_FEAT_COLOUR_3);
    setColour(FEAT_COLOUR_PREF_4, Config.DEFAULT_FEAT_COLOUR_4);
    setColour(FEAT_COLOUR_PREF_5, Config.DEFAULT_FEAT_COLOUR_5);
    this.bindColourPrefs();
    HextileService.readSettings(this);
  }

  /**
   * A preference value change listener that updates the preference's summary to
   * reflect its new value.
   */
  private static Preference.OnPreferenceChangeListener bindPreferenceSummary = new Preference.OnPreferenceChangeListener()
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
      } else if (preference instanceof AmbilWarnaPreference) {
        
      } else {
        // For all other preferences, set the summary to the value's
        // simple string representation.
        preference.setSummary(stringValue);
      }
      
      HextileService.applySetting(preference.getKey(), stringValue);
      return true;
    }
  };

  private static void bindPreferenceSummaryToValue(Preference preference)
  {
    if (preference == null)
      return;
    
    // Set the listener to watch for value changes.
    preference.setOnPreferenceChangeListener(SettingsActivity.bindPreferenceSummary);

    // Trigger the listener immediately with the preference's
    // current value.
    SettingsActivity.bindPreferenceSummary.onPreferenceChange(preference,
                                                              PreferenceManager
                                                                 .getDefaultSharedPreferences(preference.getContext())
                                                                 .getString(preference.getKey(), ""));
  }
  
  private static void bindColourPreference(Preference pref, Preference.OnPreferenceChangeListener listener)
  {
    pref.setOnPreferenceChangeListener(listener);
    listener.onPreferenceChange(pref,
                                PreferenceManager
                                   .getDefaultSharedPreferences(pref.getContext())
                                   .getInt(pref.getKey(), 0));
  }
}
