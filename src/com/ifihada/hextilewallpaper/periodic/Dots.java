package com.ifihada.hextilewallpaper.periodic;

import com.ifihada.hextilewallpaper.Tiles;

public class Dots implements IPeriodic
{
  @Override
  public String getName()
  {
    return "dots";
  }

  @Override
  public String getDescription()
  {
    return "Random dots";
  }
  
  int x, y;
  int currentColour;
  boolean rendered;

  @Override
  public boolean step(Tiles t)
  {
    this.x = PeriodicManager.randomX(t);
    this.y = PeriodicManager.randomY(t);
    this.currentColour = PeriodicManager.randomColour();
    return this.rendered;
  }
  
  @Override
  public void render(Tiles t)
  {
    t.pointTouched(this.x, this.y, this.currentColour);
    this.rendered = true;
  }

  @Override
  public void reset()
  {
    this.rendered = false;
  }
}
