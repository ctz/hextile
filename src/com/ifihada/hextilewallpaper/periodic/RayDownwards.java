package com.ifihada.hextilewallpaper.periodic;

import com.ifihada.hextilewallpaper.Tiles;

public class RayDownwards extends MovingRay
{
  @Override
  public String getName()
  {
    return "ray-downwards";
  }

  @Override
  public String getDescription()
  {
    return "Dot falling downwards";
  }

  @Override
  void startPos(int pos[], Tiles t)
  {
    pos[X] = PeriodicManager.randomX(t);
    pos[Y] = 0;
  }
  
  @Override
  void nextPos(int pos[])
  {
    pos[Y] += 2;
  }
}
