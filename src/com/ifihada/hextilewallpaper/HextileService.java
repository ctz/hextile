package com.ifihada.hextilewallpaper;

import net.rbgrn.android.glwallpaperservice.GLWallpaperService;

import android.graphics.Canvas;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class HextileService extends GLWallpaperService
{
  @Override
  public Engine onCreateEngine()
  {
    return new HextileEngine();
  }

  class HextileEngine extends Engine
  {
    private Handler handler = new Handler();
    private final Runnable drawRunner = new Runnable()
    {
      @Override
      public void run()
      {
        draw(true);
      }
    };

    boolean visible = true;

    public HextileEngine()
    {
      this.handler.post(this.drawRunner);
    }
    
    @Override
    public void onSurfaceDestroyed(SurfaceHolder holder)
    {
      super.onSurfaceDestroyed(holder);
      this.onVisibilityChanged(false);
    }

    @Override
    public void onSurfaceChanged(SurfaceHolder holder, int format, int width,
        int height)
    {
      this.tiles.resize(width, height);
      super.onSurfaceChanged(holder, format, width, height);
    }

    @Override
    public void onVisibilityChanged(boolean visible)
    {
      this.visible = visible;

      if (this.visible)
        this.handler.post(this.drawRunner);
      else
        this.handler.removeCallbacks(this.drawRunner);
    }

    @Override
    public void onTouchEvent(MotionEvent ev)
    {
      if (ev.getAction() == MotionEvent.ACTION_UP)
        return;
      boolean scheduleDraw = false;
      for (int p = 0; p < ev.getPointerCount(); p++)
        scheduleDraw = this.tiles.handleTouch(ev.getX(p), ev.getY(p)) || scheduleDraw;
      if (scheduleDraw)
        this.draw(false);
    }

    private void draw(boolean periodic)
    {
      SurfaceHolder holder = getSurfaceHolder();
      Canvas canvas = null;

      try
      {
        canvas = holder.lockCanvas();
        canvas.drawRGB(0, 0, 0);
        if (periodic)
          this.tiles.step();
        this.tiles.render(canvas);
      } finally
      {
        if (canvas != null)
          holder.unlockCanvasAndPost(canvas);
      }

      this.handler.removeCallbacks(this.drawRunner);
      if (this.visible)
        this.handler.postDelayed(this.drawRunner, 500);
    }

    Tiles tiles = new Tiles();
  }
}
