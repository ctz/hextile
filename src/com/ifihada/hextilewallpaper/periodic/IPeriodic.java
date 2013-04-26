package com.ifihada.hextilewallpaper.periodic;

import com.ifihada.hextilewallpaper.Tiles;

public interface IPeriodic
{
  public String getName();
  public String getDescription();
  public void reset();
  
  // return true if current animation is done
  public boolean step(Tiles t);
  
  public void render(Tiles t);
}
