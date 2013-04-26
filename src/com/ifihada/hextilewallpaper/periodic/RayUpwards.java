package com.ifihada.hextilewallpaper.periodic;

import com.ifihada.hextilewallpaper.Tiles;

public class RayUpwards extends MovingRay
{
  @Override
  public String getName()
  {
    return "ray-upwards";
  }

  @Override
  public String getDescription()
  {
    return "Dot rising upwards";
  }
  
  @Override
  void startPos(int[] pos, Tiles t)
  {
    pos[X] = PeriodicManager.randomX(t);
    pos[Y] = t.maxY;
  }

  @Override
  void nextPos(int[] pos)
  {
    pos[Y] -= 2;
  }
}
