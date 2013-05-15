package com.ifihada.hextilewallpaper.periodic;

import com.ifihada.hextilewallpaper.Tiles;

public class Dots implements IPeriodic
{
  float x, y;
  int currentColour;
  boolean rendered;

  @Override
  public boolean step(Tiles t)
  {
    return this.rendered;
  }
  
  @Override
  public void render(Tiles t)
  {
    t.handleTouch(this.x, this.y, this.currentColour);
    this.rendered = true;
  }

  @Override
  public void init(Tiles t)
  {
    this.rendered = false;
    this.x = PeriodicManager.randomX(t);
    this.y = PeriodicManager.randomY(t);
    this.currentColour = PeriodicManager.randomColour();
  }
}
