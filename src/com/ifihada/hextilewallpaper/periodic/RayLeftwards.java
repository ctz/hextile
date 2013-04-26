package com.ifihada.hextilewallpaper.periodic;

import com.ifihada.hextilewallpaper.Tiles;

public class RayLeftwards extends MovingRay
{
  @Override
  public String getName()
  {
    return "ray-leftwards";
  }

  @Override
  public String getDescription()
  {
    return "Dot moving left";
  }
  
  int chosenY;
  int mirror;
  
  @Override
  void startPos(int[] pos, Tiles t)
  {
    pos[X] = t.maxX;
    pos[Y] = PeriodicManager.randomY(t);
    this.chosenY = pos[Y];
    
    if (this.chosenY % 2 == 0)
    {
      this.mirror = -1;
    } else {
      this.mirror = 1;
    }
  }

  @Override
  void nextPos(int[] pos)
  {
    if (pos[Y] != this.chosenY)
    {
      if (this.mirror == 1)
        pos[X] -= 1;
      pos[Y] += this.mirror;
    } else {
      if (this.mirror == -1)
        pos[X] -= 1;
      pos[Y] -= this.mirror;
    }
  }
}
