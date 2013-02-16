package com.ifihada.hextilewallpaper;

import java.nio.ReadOnlyBufferException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

import android.graphics.Canvas;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class HextileService extends WallpaperService
{

  @Override
  public Engine onCreateEngine()
  {
    return new HextileEngine();
  }

  private class HextileEngine extends Engine
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

    private final float sin60 = 0.866f;

    private class HexGeom
    {
      private final float r, d, w, h;
      private final float[] verts;
      private final Paint shadow, hilight;

      HexGeom(final float r)
      {
        this.r = r;
        this.d = r * 2;
        this.w = r * .5f;
        this.h = r * sin60;

        this.verts = new float[]
        { 0f, 0f, -w, -h, w, -h, // 0-5
            0f, 0f, w, -h, r, 0f, // 6-11
            0f, 0f, r, 0f, w, h, // 12-17
            0f, 0f, w, h, -w, h, // 18-23
            0f, 0f, -w, h, -r, 0f, 0f, 0f, -r, 0f, -w, -h };

        this.shadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.shadow.setColor(0x5f000000);
        this.hilight = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.hilight.setColor(0x7fffffff);
      }

      void draw(Canvas cv, float x, float y, Paint fill)
      {
        cv.save();
        cv.translate(x, y);
        cv.drawVertices(Canvas.VertexMode.TRIANGLE_FAN, this.verts.length,
            this.verts, 0, this.verts, 0, (int[]) null, 0, (short[]) null, 0,
            0, fill);
        cv.drawLine(verts[2], verts[3], verts[4], verts[5], this.hilight);
        cv.drawLine(verts[20], verts[21], verts[22], verts[23], this.shadow);
        cv.restore();
      }

      boolean within(float ox, float oy, float tx, float ty)
      {
        return ox - w < tx && ox + w > tx && oy - h < ty && oy + h > ty;
      }
    }

    class TilePosition
    {
      float x;
      float y;
      float cx;
      Point i;
      boolean oddRow;

      TilePosition()
      {
        this.x = 0f;
        this.y = 0f;
        this.cx = 0f;
        this.i = new Point(0, 0);
        this.oddRow = false;
      }
      
      public String toString()
      {
        return String.format("TilePosition[%f->%f,%f][%d,%d][%s]",
                             this.x, this.cx, this.y,
                             this.i.x, this.i.y,
                             this.oddRow ? "odd" : "even"
                             );
      }
    }

    class Tiles
    {
      static final String TAG = "Tiles";
      HexGeom geom = new HexGeom(75f);
      int width, height;

      Tiles()
      {
        this(0, 0);
      }

      Tiles(int w, int h)
      {
        this.resize(w, h);
      }

      void resize(int w, int h)
      {
        this.width = w;
        this.height = h;
        this.onResize();
      }

      static final int MAX_STATES = 6;
      final int[][] colours = new int[][]
      {
          // base
          { 0xff333344, 0xff222233 },

          { 0xff33b5e5, 0xff0099cc },
          { 0xffaa66cc, 0xff9933cc },
          { 0xff99cc00, 0xff669900 },
          { 0xffffbb33, 0xffff8800 },
          { 0xffff4444, 0xffcc0000 } };

      Shader[] baseColour = new Shader[MAX_STATES];
      Shader topLighting;
      Matrix mp = new Matrix();
      Paint[] paint = new Paint[MAX_STATES];

      Iterable<TilePosition> eachTile()
      {
        return new Iterable<TilePosition>()
        {
          @Override
          public Iterator<TilePosition> iterator()
          {
            return new Iterator<TilePosition>()
            {
              TilePosition p = new TilePosition();
              boolean first = true;

              /* low, hi, step */
              float ly = Tiles.this.geom.d * -2;
              float hy = Tiles.this.height + Tiles.this.geom.d;
              float sy = Tiles.this.geom.h + Tiles.this.padh;

              float lx = Tiles.this.geom.d * -2;
              float hx = Tiles.this.width + Tiles.this.geom.d;
              float sx = Tiles.this.geom.d + Tiles.this.geom.r + Tiles.this.pad * 2;

              @Override
              public boolean hasNext()
              {
                return !this.atEnd();
              }

              private boolean atEnd()
              {
                return this.p.x + this.sx > this.hx
                    && this.p.y + this.sy > this.hy;
              }

              @Override
              public TilePosition next()
              {
                if (this.atEnd())
                  throw new NoSuchElementException();
                
                if (this.first)
                {
                  this.p.i.x = 0;
                  this.p.i.y = 0;
                  this.p.x = this.lx;
                  this.p.y = this.ly;
                  this.p.oddRow = false;
                  this.first = false;
                  return this.p;
                }
                
                this.step();
                return this.p;
              }
              
              private void step()
              {
                this.p.i.x += 1;
                this.p.x += this.sx;
                
                if (this.p.oddRow)
                {
                  this.p.cx = this.p.x + (Tiles.this.geom.d + Tiles.this.geom.r) / 2 + Tiles.this.pad; 
                } else {
                  this.p.cx = this.p.x;
                }
                
                if (this.p.x > this.hx)
                {
                  this.p.i.x = 0;
                  this.p.i.y += 1;
                  this.p.x = this.lx;
                  this.p.y += this.sy;
                  this.p.oddRow = !this.p.oddRow;
                }
              }

              @Override
              public void remove()
              {
                throw new UnsupportedOperationException();
              }
            };
          }
        };
      }

      void onResize()
      {
        this.topLighting = new LinearGradient(0, -this.geom.h, 0, this.geom.h,
            0x3fffffff, 0x2f000000, Shader.TileMode.CLAMP);

        for (int i = 0; i < MAX_STATES; i++)
        {
          this.baseColour[i] = new LinearGradient(0, 0, 0, this.height,
              colours[i][0], colours[i][1], Shader.TileMode.CLAMP);
          Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
          p.setColor(0xff336699);
          p.setStyle(Style.FILL);
          p.setShader(new ComposeShader(this.baseColour[i], this.topLighting,
              PorterDuff.Mode.SRC_ATOP));
          this.paint[i] = p;
        }
      }

      Map<Point, Integer> stateMap = new HashMap<Point, Integer>();
      
      void spread(int x, int y)
      {
        if (x < 0 || y < 0)
          return;
        
        Point p = new Point(x, y);
        Integer st = this.stateMap.get(p);
        if (st != null)
          return;
        this.stateMap.put(p, 3);
      }
      
      void stepCell(Point loc)
      {
        Integer state = this.stateMap.get(loc);
        int istate;
        if (state == null)
          return;
        
        istate = state;
        
        istate += 1;
        istate %= MAX_STATES;
        
        if (istate == 0)
        {
          this.stateMap.remove(loc);
        } else {
          this.stateMap.put(new Point(loc), istate);
        }
      }

      Paint paintForCell(float x, float y, Point loc)
      {
        Integer state = this.stateMap.get(loc);
        int istate;
        if (state == null)
          istate = 0;
        else
          istate = state;

        this.mp.setTranslate(-x, -y);
        this.baseColour[istate].setLocalMatrix(this.mp);
        return this.paint[istate];
      }

      float pad = 3f;
      float padh = pad * sin60;

      void render(Canvas canvas)
      {
        for (TilePosition t : this.eachTile())
        {
          Paint paint = this.paintForCell(t.cx, t.y, t.i);
          this.geom.draw(canvas, t.cx, t.y, paint);
        }
      }
      
      void step()
      {
        for (TilePosition t : this.eachTile())
        {
          this.stepCell(t.i);
        }
      }

      boolean handleTouch(float touchx, float touchy)
      {
        for (TilePosition t : this.eachTile())
        {
          if (this.geom.within(t.cx, t.y, touchx, touchy))
          {
            return this.pointTouched(t.i);
          }
        }
        
        return false;
      }

      private boolean pointTouched(Point p)
      {
        this.stateMap.put(new Point(p), 1);
        return true;
      }
    }

    Tiles tiles = new Tiles();
  }
}
