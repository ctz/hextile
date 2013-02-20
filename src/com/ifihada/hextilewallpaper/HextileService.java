package com.ifihada.hextilewallpaper;

import com.ifihada.hextilewallpaper.prefs.SettingsActivity;

import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import net.rbgrn.android.glwallpaperservice.GLWallpaperService;

public class HextileService extends GLWallpaperService
{
  static HextileEngine latestEngine;
  
  @Override
  public Engine onCreateEngine()
  {
    this.readSetting(SettingsActivity.SIZE_PREF);
    this.readSetting(SettingsActivity.GAP_PREF);
    HextileService.latestEngine = new HextileEngine();
    return HextileService.latestEngine;
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
      Config.TileSize = Integer.parseInt(value);
    } else if (key.equals(SettingsActivity.GAP_PREF)) {
      Config.TilePadding = Integer.parseInt(value);
    }
    
    if (HextileService.latestEngine != null)
      HextileService.latestEngine.sync();
  }

  class HextileEngine extends GLEngine
  {
    HextileRenderer renderer;
    
    public HextileEngine()
    {
      super();

      this.renderer = new HextileRenderer(this);
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
        while (!this.isCancelled())
        {
          if (HextileEngine.this.renderer.step())
            HextileEngine.this.requestRender();
          
          try
          {
            Thread.sleep(FRAME_DELAY);
          } catch (Exception e) {}
        }
        return null;
      }
    }
  }
}
