package com.ifihada.hextilewallpaper.periodic;

import com.ifihada.hextilewallpaper.Tiles;

public abstract class MovingRay implements IPeriodic
{
  float pos[] = new float[2];
  int currentColour;
  
  static final int X = 0;
  static final int Y = 1;
  
  static final int RANDOM = -1;
  static final int TOP_LEFT = 0;
  static final int BOTTOM_RIGHT = 1;
  
  float deltaX;
  float deltaY;
  int startX;
  int startY;
  
  public MovingRay(float dx, float dy, int startx, int starty)
  {
    this.deltaX = dx;
    this.deltaY = dy;
    this.startX = startx;
    this.startY = starty;
  }
  
  @Override
  public void init(Tiles t)
  {
    if (this.startX == RANDOM)
      this.pos[X] = PeriodicManager.randomX(t);
    else
      this.pos[X] = (float) this.startX * t.width;
    
    if (this.startY == RANDOM)
      this.pos[Y] = PeriodicManager.randomY(t);
    else
      this.pos[Y] = (float) this.startY * t.height;
    
    this.currentColour = PeriodicManager.randomColour();
  }
  
  @Override
  public boolean step(Tiles t)
  {    
    int step = t.tileSize / 4;

    this.pos[X] += step * this.deltaX;
    this.pos[Y] += step * this.deltaY;
      
    if (this.pos[Y] >= t.height || this.pos[Y] < 0 ||
        this.pos[X] >= t.width || this.pos[X] < 0)
    {
      return true;
    }
    
    return false;
  }
  
  @Override
  public void render(Tiles t)
  {
    t.handleTouch(this.pos[X], this.pos[Y], this.currentColour);
  }
}

class RayDownwards extends MovingRay
{
  public RayDownwards()
  {
    super(0f, 1f, RANDOM, TOP_LEFT);
  }
}

class RayUpwards extends MovingRay
{
  public RayUpwards()
  {
    super(0f, -1f, RANDOM, BOTTOM_RIGHT);
  }
}

class RayRightwards extends MovingRay
{
  public RayRightwards()
  {
    super(1f, 0f, TOP_LEFT, RANDOM);
  }
}
class RayLeftwards extends MovingRay
{
  public RayLeftwards()
  {
    super(-1f, 0f, BOTTOM_RIGHT, RANDOM);
  }
}
