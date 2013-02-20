package com.ifihada.hextilewallpaper.prefs;

import android.content.Context;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

import com.ifihada.hextilewallpaper.R;
import com.ifihada.hextilewallpaper.R.layout;

public class ColourPreference extends DialogPreference
{
  public ColourPreference(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    
    this.setDialogLayoutResource(R.layout.colourpicker_dialog);
    this.setPositiveButtonText(android.R.string.ok);
    this.setNegativeButtonText(android.R.string.cancel);
    this.setDialogIcon(null);
  }
}

