package com.ifihada.hextilewallpaper;

import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

class Tiles
{
  static final String TAG = "Tiles";
  HexGeom geom = new HexGeom(50f);
  int width, height;
  boolean dirty;

  float pad;
  float padh;

  Colour[][] colours;
  
  Tiles()
  {
    this(0, 0);
  }

  Tiles(int w, int h)
  {
    this.resize(w, h);
    this.sync();
  }

  public void sync()
  {
    Log.v(TAG, "Tiles sync'd");
    this.geom = new HexGeom(Config.getTileSize());
    this.pad = Config.getTilePadding();
    this.padh = this.pad * Const.sin60;
    this.dirty = true;
  }

  void resize(int w, int h)
  {
    this.width = w;
    this.height = h;
    this.onResize();
    this.dirty = true;
  }
  
  void onResize()
  {
    int xmax = 0, ymax = 0;
    
    for (TilePosition t : this.eachTile())
    {
      if (t.ix > xmax)
        xmax = t.ix;
      if (t.iy > ymax)
        ymax = t.iy;
    }
    
    Colour[][] newcolours = new Colour[xmax + 1][ymax + 1];
    Colour base = Config.getBaseColour();
    
    for (Colour[] column : newcolours)
    {
      for (int i = 0; i < column.length; i++)
      {
        column[i] = new Colour(base);
      }
    }
    
    if (this.colours != null)
    {
      for (int x = 0; x < this.colours.length; x++)
      {
        for (int y = 0; y < this.colours[x].length; y++)
        {
          if (x < xmax && y < ymax)
            newcolours[x][y] = this.colours[x][y];
          this.colours[x][y] = null;
        }
      }
    }
    
    this.colours = newcolours;
  }

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
              this.p.ix = 0;
              this.p.iy = 0;
              this.p.x = this.lx;
              this.p.y = this.ly;
              this.p.oddRow = false;
              this.p.colour = getColour(0, 0);
              this.p.updateAdjacent();
              this.first = false;
              return this.p;
            }
            
            this.step();
            return this.p;
          }
          
          private void step()
          {
            this.p.ix += 1;
            this.p.x += this.sx;
            
            if (this.p.oddRow)
            {
              this.p.cx = this.p.x + (geom.d + geom.r) / 2 + pad; 
            } else {
              this.p.cx = this.p.x;
            }
            
            if (this.p.x > this.hx)
            {
              this.p.ix = 0;
              this.p.iy += 1;
              this.p.x = this.lx;
              this.p.y += this.sy;
              this.p.oddRow = !this.p.oddRow;
            }

            this.p.updateAdjacent();
            this.p.colour = getColour(this.p.ix, this.p.iy);
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
  
  static Colour BLACK = Colour.fromRGBA(0x00000000);
  
  Colour getColour(int x, int y)
  {
    if (this.colours == null ||
        x >= this.colours.length ||
        y >= this.colours[x].length)
      return BLACK;
    else
      return this.colours[x][y];
  }
  
  void propagate(int sx, int sy, int tx, int ty)
  {
    if (sx < 0 || sx >= this.colours.length ||
        tx < 0 || tx >= this.colours.length)
      return;
    if (sy < 0 || sy >= this.colours[sx].length ||
        ty < 0 || ty >= this.colours[tx].length)
      return;
    
    this.colours[tx][ty].lerpRGB(this.colours[sx][sy], 0.025f);
  }
  
  void spread(TilePosition t)
  {
    propagate(t.ix, t.iy, t.a1x, t.a1y);
    propagate(t.ix, t.iy, t.a2x, t.a2y);
    propagate(t.ix, t.iy, t.a3x, t.a3y);
    propagate(t.ix, t.iy, t.a4x, t.a4y);
  }
  
  boolean validPosition(int x, int y)
  {
    return x >= 0 && x < this.colours.length &&
           y >= 0 && y < this.colours[x].length;
  }
  
  boolean validPosition(TilePosition t)
  {
    return validPosition(t.ix, t.iy);
  }
  
  boolean stepCell(TilePosition t)
  {
    if (!this.validPosition(t))
      return false;
    
    if (this.colours[t.ix][t.iy].lerpRGB(Config.getBaseColour(), 0.01f))
    {
      spread(t);
      return true;
    } else {
      return false;
    }
  }
  
  void render(GL10 gl)
  {
    for (TilePosition t : this.eachTile())
    {
      this.geom.draw(gl, t);
    }
  }
  
  boolean step()
  {
    boolean changed = false;
    
    if (!this.dirty)
      return false;
    
    for (TilePosition t : this.eachTile())
    {
      if (this.stepCell(t))
        changed = true;
    }
    
    if (changed)
      this.dirty = true;
    else
      this.dirty = false;
    
    return changed;
  }

  boolean handleTouch(float touchx, float touchy, int colour)
  {
    for (TilePosition t : this.eachTile())
    {
      if (this.geom.within(t.cx, t.y, touchx, touchy))
      {
        this.pointTouched(t.ix, t.iy, colour);
        this.dirty = true;
        return true;
      }
    }
    
    return false;
  }

  private boolean pointTouched(int x, int y, int colour)
  {    
    this.colours[x][y].set(Config.getFeatureColour(colour));
    return true;
  }
}