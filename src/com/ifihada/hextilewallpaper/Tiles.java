package com.ifihada.hextilewallpaper;

import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.microedition.khronos.opengles.GL10;

class Tiles
{
  static final String TAG = "Tiles";
  HexGeom geom = new HexGeom(50f);
  int width, height;
  boolean dirty;

  final float pad = 5f;
  final float padh = pad * Const.sin60;

  int[][] colours;
  
  Tiles()
  {
    this(0, 0);
  }

  Tiles(int w, int h)
  {
    this.resize(w, h);
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
    
    int[][] newcolours = new int[xmax + 1][ymax + 1];
    
    for (int[] column : newcolours)
    {
      for (int i = 0; i < column.length; i++)
      {
        column[i] = BASE_COLOUR;
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
        }
      }
    }
    
    this.colours = newcolours;
  }
  
  final static int BASE_COLOUR = 0x333344ff;
  final static int[] COLOURS = new int[]
  {
   0x33b5e5ff,
   0xaa66ccff,
   0x99cc00ff,
   0xffbb33ff,
   0xff4444ff,
  };

  /*
  final int[][] fixedcolours = new int[][]
  {
    // base
    { 0x333344ff, 0x222233ff },
  
    { 0x33b5e5ff, 0x0099ccff },
    { 0xaa66ccff, 0x9933ccff },
    { 0x99cc00ff, 0x669900ff },
    { 0xffbb33ff, 0xff8800ff },
    { 0xff4444ff, 0xcc0000ff }
  };
  */

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
  
  int getColour(int x, int y)
  {
    if (this.colours == null ||
        x >= this.colours.length ||
        y >= this.colours[x].length)
      return 0x00000000;
    else
      return this.colours[x][y];
  }
  
  int merge(int target, int x, int y, int density)
  {
    if (x < 0 || x >= this.colours.length)
      return target;
    if (y < 0 || y >= this.colours[x].length)
      return target;
    int source = this.colours[x][y];
    if (source == Tiles.BASE_COLOUR)
      return target;
    else
      return Colour.rgbaLerp(target, source, density);
  }
  
  void propagate(int sx, int sy, int tx, int ty)
  {
    if (sx < 0 || sx >= this.colours.length ||
        tx < 0 || tx >= this.colours.length)
      return;
    if (sy < 0 || sy >= this.colours[sx].length ||
        ty < 0 || ty >= this.colours[tx].length)
      return;
    
    int source = this.colours[sx][sy];
    if (source != Tiles.BASE_COLOUR)
      this.colours[tx][ty] = Colour.rgbaLerp(this.colours[tx][ty], this.colours[sx][sy], 1);
  }
  
  void spread(TilePosition t)
  {
    propagate(t.ix, t.iy, t.a1x, t.a1y);
    propagate(t.ix, t.iy, t.a2x, t.a2y);
    propagate(t.ix, t.iy, t.a3x, t.a3y);
    propagate(t.ix, t.iy, t.a4x, t.a4y);
  }
  
  boolean stepCell(TilePosition t)
  {
    int target = this.colours[t.ix][t.iy];
    int original = target;
    
    /*
    target = merge(target, t.a1x, t.a1y, adjDensity);
    target = merge(target, t.a2x, t.a2y, adjDensity);
    target = merge(target, t.a3x, t.a3y, adjDensity);
    target = merge(target, t.a4x, t.a4y, adjDensity);
    */
    
    target = Colour.rgbaLerp(target, Tiles.BASE_COLOUR, 1);
    if (original != target)
    {
      this.colours[t.ix][t.iy] = target;
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

  boolean handleTouch(float touchx, float touchy, boolean firstInBatch)
  {
    for (TilePosition t : this.eachTile())
    {
      if (this.geom.within(t.cx, t.y, touchx, touchy))
      {
        this.pointTouched(t.ix, t.iy, firstInBatch);
        this.dirty = true;
        return true;
      }
    }
    
    return false;
  }
  
  private int selectedColour = 0;

  private boolean pointTouched(int x, int y, boolean firstInBatch)
  {
    if (firstInBatch)
    {
      this.selectedColour = (this.selectedColour + 1) % COLOURS.length;
    }
    
    this.colours[x][y] = COLOURS[this.selectedColour];
    return true;
  }
}