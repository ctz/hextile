package com.ifihada.hextilewallpaper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import net.rbgrn.android.glwallpaperservice.*;

public class HextileRenderer implements GLWallpaperService.Renderer
{
  Tiles tiles = new Tiles();
  Object sync = new Object();
  
  public HextileRenderer(GLWallpaperService.GLEngine engine)
  {
  }
  
  public void sync()
  {
    synchronized (sync)
    {
      this.tiles.sync();
    }
  }
  
  public void initial()
  {
    synchronized (sync)
    {
      this.tiles.dirty = true;
    }
  }
  
  public boolean step()
  {
    synchronized (sync)
    {
      return this.tiles.step();
    }
  }
    
  @Override
  public void onDrawFrame(GL10 gl)
  {
    Colour back = Config.getBackColour();
    gl.glClearColor(back.r, back.g, back.b, back.a);
    gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

    synchronized (sync)
    {
      this.tiles.render(gl);
    }
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int w, int h)
  {
    synchronized (sync)
    {
      this.tiles.resize(w, h);
    }
    
    gl.glViewport(0, 0, w, h);
    gl.glMatrixMode(GL10.GL_PROJECTION);
    gl.glLoadIdentity();
    gl.glOrthof(0, w, h, 0, -1f, 1f);
    gl.glMatrixMode(GL10.GL_MODELVIEW);
    gl.glLoadIdentity();
    
    gl.glEnable(GL10.GL_BLEND);
    gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
    gl.glEnable(GL10.GL_MULTISAMPLE);
    gl.glEnable(GL10.GL_LINE_SMOOTH);
    gl.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);
    gl.glHint(GL10.GL_POLYGON_SMOOTH_HINT, GL10.GL_NICEST);
  }

  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig cnf)
  {
    this.initial();
  }
  
  public void release()
  {
  }
}
