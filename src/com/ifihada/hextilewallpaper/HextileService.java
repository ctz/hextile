package com.ifihada.hextilewallpaper;

import java.util.WeakHashMap;

import com.ifihada.hextilewallpaper.prefs.SettingsActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import net.rbgrn.android.glwallpaperservice.GLWallpaperService;

public class HextileService extends GLWallpaperService
{
  static final String TAG = HextileService.class.getSimpleName();
  static WeakHashMap<HextileEngine, String> recentEngines = new WeakHashMap<HextileEngine, String>(); 
  
  @Override
  public Engine onCreateEngine()
  {
    HextileService.readSettings(this);
    HextileEngine e = new HextileEngine();
    
    HextileService.recentEngines.put(e, null);
    return e;
  }
  
  public static void readSettings(Context ctx)
  {
    HextileService.readStrSetting(ctx, SettingsActivity.SIZE_PREF);
    HextileService.readStrSetting(ctx, SettingsActivity.GAP_PREF);
    HextileService.readIntSetting(ctx, SettingsActivity.BASE_COLOUR_PREF);
    HextileService.readIntSetting(ctx, SettingsActivity.FEAT_COLOUR_PREF_1);
    HextileService.readIntSetting(ctx, SettingsActivity.FEAT_COLOUR_PREF_2);
    HextileService.readIntSetting(ctx, SettingsActivity.FEAT_COLOUR_PREF_3);
    HextileService.readIntSetting(ctx, SettingsActivity.FEAT_COLOUR_PREF_4);
    HextileService.readIntSetting(ctx, SettingsActivity.FEAT_COLOUR_PREF_5);
  }
  
  private static void readStrSetting(Context ctx, String key)
  {
    HextileService.applySetting(key,
                                PreferenceManager.getDefaultSharedPreferences(ctx)
                                                 .getString(key,  "")
                               );
  }
  
  private static void readIntSetting(Context ctx, String key)
  {
    HextileService.applySetting(key,
                                Integer.toString(PreferenceManager.getDefaultSharedPreferences(ctx).getInt(key, 0))
                               );
  }
  
  public static void applySetting(String key, String value)
  {
    if (value == null || value.equals(""))
      return;
    
    if (key.equals(SettingsActivity.SIZE_PREF))
    {
      Config.setTileSize(Integer.parseInt(value));
    } else if (key.equals(SettingsActivity.GAP_PREF)) {
      Config.setTilePadding(Integer.parseInt(value));
    } else if (key.equals(SettingsActivity.BASE_COLOUR_PREF)) {
      Config.setBaseColour(parseColour(value));
    } else if (key.equals(SettingsActivity.FEAT_COLOUR_PREF_1)) {
      Config.setFeatureColour(0, parseColour(value));
    } else if (key.equals(SettingsActivity.FEAT_COLOUR_PREF_2)) {
      Config.setFeatureColour(1, parseColour(value));
    } else if (key.equals(SettingsActivity.FEAT_COLOUR_PREF_3)) {
      Config.setFeatureColour(2, parseColour(value));
    } else if (key.equals(SettingsActivity.FEAT_COLOUR_PREF_4)) {
      Config.setFeatureColour(3, parseColour(value));
    } else if (key.equals(SettingsActivity.FEAT_COLOUR_PREF_5)) {
      Config.setFeatureColour(4, parseColour(value));
    } else {
      Log.v(TAG, "unhandled setting input, key = " + key);
    }
    
    HextileService.syncEngines();
  }
  
  private static Colour parseColour(String value)
  {
    int argb = Integer.parseInt(value);
    Colour c = new Colour();
    c.setARGB(argb);
    return c;
  }

  private static void syncEngines()
  {
    for (HextileEngine e : HextileService.recentEngines.keySet())
    {
      if (e != null)
        e.sync();
    }
  }

  class HextileEngine extends GLEngine
  {
    static final String TAG = "HextileEngine";
    HextileRenderer renderer;
    
    public HextileEngine()
    {
      super();

      this.renderer = new HextileRenderer(this);
      this.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
      this.setRenderer(this.renderer);
      this.setRenderMode(RENDERMODE_WHEN_DIRTY);
      this.setTouchEventsEnabled(true);
      this.start();
    }
    
    public void sync()
    {
      this.renderer.sync();
    }
    
    int selectedColour = 0;
    boolean isVisible = true;
    RenderTask renderTask = null;
    
    @Override
    public void onTouchEvent(MotionEvent ev)
    {
      for (int p = 0; p < ev.getPointerCount(); p++)
      {
        this.renderer.tiles.handleTouch(ev.getX(p), ev.getY(p), this.selectedColour);
      }
      
      if (ev.getAction() == MotionEvent.ACTION_UP)
      {
        this.selectedColour++;
        this.selectedColour %= Config.featureColourCount;
      }
    }
    
    @Override
    public void onVisibilityChanged(boolean visible)
    {
      super.onVisibilityChanged(visible);
      this.isVisible = visible;
      
      if (this.isVisible)
        this.start();
      else
        this.pause();
    }
    
    private synchronized void start()
    {
      if (this.renderTask == null)
      {
        this.renderTask = new RenderTask();
        this.renderTask.execute();
      }
    }
    
    private synchronized void pause()
    {
      if (this.renderTask != null)
      {
        this.renderTask.cancel(false);
        this.renderTask = null;
      }
    }
    
    class RenderTask extends AsyncTask<Void, Void, Void>
    {
      long FRAME_DELAY = 40;

      @Override
      protected Void doInBackground(Void... arg0)
      {
        Log.v(TAG, "RenderTask starting");
        while (!this.isCancelled())
        {
          if (HextileEngine.this.renderer.step())
            HextileEngine.this.requestRender();
          
          try
          {
            Thread.sleep(FRAME_DELAY);
          } catch (Exception e) {}
        }
        Log.v(TAG, "RenderTask completed");
        return null;
      }
    }
  }
}
