package com.ifihada.hextilewallpaper.periodic;

import com.ifihada.hextilewallpaper.Tiles;

public interface IPeriodic
{
  // initialise everything afresh. must not store t.
  public void init(Tiles t);
  
  // return true if current animation is done
  public boolean step(Tiles t);
  
  public void render(Tiles t);
}
