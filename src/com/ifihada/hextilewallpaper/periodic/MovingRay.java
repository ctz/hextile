package com.ifihada.hextilewallpaper.periodic;

import com.ifihada.hextilewallpaper.Tiles;

public abstract class MovingRay implements IPeriodic
{
  int pos[] = new int[2];
  int currentColour;
  
  abstract void startPos(int pos[], Tiles t);
  abstract void nextPos(int pos[]);
  
  static final int X = 0;
  static final int Y = 1;

  @Override
  public boolean step(Tiles t)
  {
    if (this.pos[X] == -1 && this.pos[Y] == -1)
    {
      this.startPos(this.pos, t);
      this.currentColour = PeriodicManager.randomColour();
      return false;
    }
    
    this.nextPos(this.pos);
      
    if (this.pos[Y] >= t.maxY || this.pos[Y] < 0 ||
        this.pos[X] >= t.maxX || this.pos[X] < 0)
    {
      this.reset();
      return true;
    }
    
    return false;
  }
  
  @Override
  public void render(Tiles t)
  {
    t.pointTouched(this.pos[X], this.pos[Y], this.currentColour);
  }

  @Override
  public void reset()
  {
    this.pos[X] = -1;
    this.pos[Y] = -1;
  }
}
