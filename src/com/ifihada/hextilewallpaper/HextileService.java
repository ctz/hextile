package com.ifihada.hextilewallpaper;

import java.util.WeakHashMap;

import com.ifihada.hextilewallpaper.prefs.SettingsActivity;

import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import net.rbgrn.android.glwallpaperservice.GLWallpaperService;

public class HextileService extends GLWallpaperService
{
  static WeakHashMap<HextileEngine, String> recentEngines = new WeakHashMap<HextileEngine, String>(); 
  
  @Override
  public Engine onCreateEngine()
  {
    this.readSetting(SettingsActivity.SIZE_PREF);
    this.readSetting(SettingsActivity.GAP_PREF);
    
    HextileEngine e = new HextileEngine();
    
    HextileService.recentEngines.put(e, null);
    return e;
  }
  
  private void readSetting(String key)
  {
    HextileService.applySetting(key,
                                PreferenceManager.getDefaultSharedPreferences(this)
                                                 .getString(key,  "")
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
    }
    
    HextileService.syncEngines();
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
    
    boolean inBatch = false;
    boolean isVisible = true;
    RenderTask renderTask = null;
    
    @Override
    public void onTouchEvent(MotionEvent ev)
    {
      for (int p = 0; p < ev.getPointerCount(); p++)
      {
        this.renderer.tiles.handleTouch(ev.getX(p), ev.getY(p), !inBatch);
        inBatch = true;
      }
      
      if (ev.getAction() == MotionEvent.ACTION_UP)
      {
        this.inBatch = false;
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
