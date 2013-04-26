package com.ifihada.hextilewallpaper;

import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

public class Tiles
{
  static final String TAG = "Tiles";
  HexGeom geom;
  HexGeom outerGeom;
  boolean dirty;
  
  boolean invertedLayout;

  float pad;
  float padh;

  public int width, height;
  public int maxX, maxY;

  Colour[][] colours;
  
  Tiles()
  {
    this(0, 0);
  }

  Tiles(int w, int h)
  {
    this.sync();
    this.resize(w, h);
  }

  public void sync()
  {
    Log.v(TAG, "Tiles sync'd");
    int tileSize = Config.getTileSize();
    this.pad = Config.getTilePadding();
    this.padh = this.pad * Const.sin60;
    
    this.geom = new HexGeom(tileSize);
    this.geom.setShading(Config.getShading());
    this.geom.setHighlighting(Config.getHighlighting());
    
    this.invertedLayout = Config.getInverted();
    
    if (this.invertedLayout)
    {
      // make padding more obvious
      this.pad = (this.pad + 1f) * 2;
      this.padh = this.pad * Const.sin60;
      
      this.outerGeom = new HexGeom(tileSize + this.pad * 0.666f);
      this.outerGeom.setShading(false);
      this.outerGeom.setHighlighting(false);
    } else {
      this.outerGeom = null;
    }
    
    this.dirty = true;
  }

  void resize(int w, int h)
  {
    Log.v(TAG, "Tiles resized");
    this.width = w;
    this.height = h;
    this.onResize();
    this.dirty = true;
  }
  
  void onResize()
  {
    int xmax = 0, ymax = 0;
    
    TileIterator it = new TileIterator();
    it.reset();
    
    while (it.hasNext())
    {
      TilePosition t = it.next();
      
      if (t.ix > xmax)
        xmax = t.ix;
      if (t.iy > ymax)
        ymax = t.iy;
    }
    
    Colour[][] newcolours = new Colour[xmax + 1][ymax + 1];
    Colour base = this.getBaseColour();
    
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
    this.maxX = xmax + 1;
    this.maxY = ymax + 1;
    Log.v(TAG, "onResize() completed");
  }
  
  float lowBound(int w, float s)
  {
    float w2 = w / 2;
    int n = (int) (w2 / s);
    n += 2;
    return w2 - s * n;
  }
  
  class TileIterator implements Iterator<TilePosition>
  {
    TilePosition p = new TilePosition();

    /* low, hi, step */
    float hy, sy, ly;
    float hx, sx, lx;
    
    void reset()
    {
      hy = height + geom.d;
      sy = geom.h + pad / 2;
      ly = lowBound(height, sy);
      
      hx = width + geom.d;
      sx = geom.d + geom.r + pad * 2;
      lx = lowBound(width, sx);
      
      p.reset();
      selectStart();
    }
    
    void reset(float tgtx, float tgty)
    {
      reset();
      
      int slop = 2;
      float basex = tgtx - this.sx * slop;
      float basey = tgty - this.sy * slop;
      this.fastForward(basex, basey);
      this.hy = tgty + this.sy * slop;
    }
    
    private void selectStart()
    {
      this.p.ix = 0;
      this.p.iy = 0;
      this.p.x = this.lx;
      this.p.y = this.ly;
      this.p.oddRow = false;
      this.p.colour = getColour(this.p.ix, this.p.iy);
      this.p.updateAdjacent();
    }
    
    private void fastForward(float ix, float iy)
    {
      while (this.hasNext())
      {
        TilePosition t = this.next();
        if (t.cx > ix && t.y > iy)
          break;
      }
    }

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
  }
  
  Colour getColour(int x, int y)
  {
    if (this.colours == null ||
        x >= this.colours.length ||
        y >= this.colours[x].length)
      return Colour.BLACK;
    else
      return this.colours[x][y];
  }

  static float BASE_LERP = 0.01f;
  static float PROP_LERP = 0.025f;
  
  void propagate(int sx, int sy, int tx, int ty)
  {
    if (sx < 0 || sx >= this.colours.length ||
        tx < 0 || tx >= this.colours.length)
      return;
    if (sy < 0 || sy >= this.colours[sx].length ||
        ty < 0 || ty >= this.colours[tx].length)
      return;
    
    this.colours[tx][ty].lerpRGB(this.colours[sx][sy], PROP_LERP);
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
  
  private Colour getBaseColour()
  {
    if (this.invertedLayout)
      return Config.getBackColour();
    else
      return Config.getBaseColour();
  }
  
  boolean stepCell(TilePosition t)
  {
    if (!this.validPosition(t))
      return false;
    
    if (this.colours[t.ix][t.iy].lerpRGB(this.getBaseColour(), BASE_LERP))
    {
      spread(t);
      return true;
    } else {
      return false;
    }
  }

  private TileIterator renderIterator = new TileIterator();
  private TileIterator stepIterator = new TileIterator();
  
  private void renderStandard(GL10 gl)
  {
    renderIterator.reset();
    
    while (renderIterator.hasNext())
    {
      TilePosition t = renderIterator.next();
      this.geom.draw(gl, t);
    }
  }
  
  private void renderInverted(GL10 gl)
  {
    // render backgrounds
    renderIterator.reset();
    while (renderIterator.hasNext())
    {
      TilePosition t = renderIterator.next();
      this.outerGeom.draw(gl, t);
    }
    
    // render foregrounds
    Colour c = Config.getBaseColour();
    renderIterator.reset();
    while (renderIterator.hasNext())
    {
      TilePosition t = renderIterator.next();
      t.colour = c;
      this.geom.draw(gl, t);
    }
  }
  
  public void render(GL10 gl)
  {
    if (this.invertedLayout)
    {
      this.renderInverted(gl);
    } else {
      this.renderStandard(gl);
    }
  }
  
  public boolean step()
  {
    boolean changed = false;
    
    if (!this.dirty)
      return false;

    stepIterator.reset();
    while (stepIterator.hasNext())
    {
      TilePosition t = stepIterator.next();
      
      if (this.stepCell(t))
        changed = true;
    }
    
    if (!changed && this.dirty)
      Log.v(TAG, "renderer stopping");
    
    if (changed)
      this.dirty = true;
    else
      this.dirty = false;
    
    return changed;
  }
  
  private TileIterator touchIterator = new TileIterator();

  public boolean handleTouch(float touchx, float touchy, int colour)
  {
    touchIterator.reset(touchx, touchy);
    
    while (touchIterator.hasNext())
    {
      TilePosition t = touchIterator.next();
      
      if (this.geom.withinRadius(t.cx, t.y, touchx, touchy))
      {
        this.pointTouched(t.ix, t.iy, colour);
        break;
      }
    }
    
    return false;
  }

  public void pointTouched(int x, int y, int colour)
  {
    if (x > 0 &&
        y > 0 &&
        x < this.colours.length &&
        y < this.colours[x].length)
    {
      this.colours[x][y].set(Config.getFeatureColour(colour));
      this.dirty = true;
    }
  }
}