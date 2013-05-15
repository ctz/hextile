package com.ifihada.hextilewallpaper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.ifihada.hextilewallpaper.periodic.PeriodicManager;

import net.rbgrn.android.glwallpaperservice.*;

public class HextileRenderer implements GLWallpaperService.Renderer
{
  private Tiles tiles = new Tiles();
  //private PeriodicManager periodic = new PeriodicManager();
  
  static boolean DEBUG = false;
  static int DEBUG_POINTS = 128;
  private FloatBuffer debugVertexBuffer;
  private int debugUsed;
  private FloatBuffer infillVertexBuffer;
  private int infillUsed;
  
  public HextileRenderer(GLWallpaperService.GLEngine engine)
  {
    this.sync();
    
    if (DEBUG)
    {
      this.debugVertexBuffer = ByteBuffer.allocateDirect(DEBUG_POINTS * 3 * 4)
                                         .order(ByteOrder.nativeOrder())
                                         .asFloatBuffer();
      this.debugUsed = 0;
      this.infillVertexBuffer = ByteBuffer.allocateDirect(DEBUG_POINTS * 3 * 4)
                                          .order(ByteOrder.nativeOrder())
                                          .asFloatBuffer();
      this.infillUsed = 0;
    }
  }
  
  public void sync()
  {
    synchronized (this)
    {
      //this.periodic.sync();
      this.tiles.sync();
    }
  }
  
  public void initial()
  {
    synchronized (this)
    {
      //this.periodic.start();
      this.tiles.dirty = true;
    }
  }
  
  public boolean step()
  {
    synchronized (this)
    {
      //this.periodic.step(this.tiles);
      return this.tiles.step();
    }
  }
    
  @Override
  public void onDrawFrame(GL10 gl)
  {
    Colour back = Config.getBackColour();
    gl.glClearColor(back.r, back.g, back.b, back.a);
    gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

    synchronized (this)
    {
      this.tiles.render(gl);
    }
    
    if (DEBUG && this.debugUsed > 0)
    {
      gl.glLoadIdentity();
      gl.glPointSize(10f);
      
      gl.glColor4f(255, 0, 0, 255);
      gl.glVertexPointer(3, GL10.GL_FLOAT, 0, this.debugVertexBuffer);
      gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
      gl.glDrawArrays(GL10.GL_POINTS, 0, this.debugUsed);
      gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

      gl.glColor4f(255, 255, 0, 255);
      gl.glVertexPointer(3, GL10.GL_FLOAT, 0, this.infillVertexBuffer);
      gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
      gl.glDrawArrays(GL10.GL_POINTS, 0, this.infillUsed);
      gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int w, int h)
  {
    synchronized (this)
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
  
  public void handleTouchInfill(float x, float y, int selectedColour)
  {
    synchronized (this)
    {
      this.tiles.handleTouch(x, y, selectedColour);

      if (this.infillVertexBuffer != null)
      {
        if (this.infillUsed < DEBUG_POINTS)
        {
          this.infillVertexBuffer.position(this.infillUsed * 3);
          this.infillVertexBuffer.put(x)
                                 .put(y)
                                 .put(0)
                                 .position(0);
          this.infillUsed++;
        }
      }
    }
  }

  public void handleTouch(float x, float y, int selectedColour)
  {
    synchronized (this)
    {
      this.tiles.handleTouch(x, y, selectedColour);

      if (this.debugVertexBuffer != null)
      {
        if (this.debugUsed < DEBUG_POINTS)
        {
          this.debugVertexBuffer.position(this.debugUsed * 3);
          this.debugVertexBuffer.put(x)
                                .put(y)
                                .put(0)
                                .position(0);
          this.debugUsed++;
        }
      }
    }
  }
  
  public synchronized void resetDebugTrace()
  {
    this.debugUsed = 0;
    this.infillUsed = 0;
  }
}
