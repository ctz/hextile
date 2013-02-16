package com.ifihada.hextilewallpaper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import android.graphics.Canvas;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.Paint.Style;


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
          float ly = geom.d * -2;
          float hy = height + geom.d;
          float sy = geom.h + padh;

          float lx = geom.d * -2;
          float hx = width + geom.d;
          float sx = geom.d + geom.r + pad * 2;

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
              this.p.cx = this.p.x + (geom.d + geom.r) / 2 + pad; 
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
  float padh = pad * Const.sin60;

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