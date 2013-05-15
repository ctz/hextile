package com.ifihada.hextilewallpaper.periodic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import com.ifihada.hextilewallpaper.Config;
import com.ifihada.hextilewallpaper.Tiles;

public class PeriodicManager
{
  static final String TAG = "PeriodicManager";
  
  ArrayList<IPeriodic> selected = new ArrayList<IPeriodic>();
  ArrayList<IPeriodic> actives = new ArrayList<IPeriodic>();
  int maxSimultaneous = 4;
  
  public void sync()
  {
    this.selected.clear();
    this.actives.clear();

    this.selected.add(new RayDownwards());
    this.selected.add(new RayUpwards());
    this.selected.add(new RayLeftwards());
    this.selected.add(new RayRightwards());
    this.selected.add(new Dots());
  }
  
  public void start()
  {
    this.actives.clear();
  }
  
  static final int STEP_FRAMES = 2;
  static final int CHOOSE_FRAMES = 64;
  int frame;
  
  private void chooseNew(Tiles t)
  {
    int nselected = this.selected.size();
    int tries = 5;
    
    if (nselected == 0)
      return;
    
    while (this.actives.size() < this.maxSimultaneous && tries-- > 0)
    {
      int idx = rand.nextInt(nselected);
      IPeriodic chosen = this.selected.get(idx);
      if (this.actives.contains(chosen))
        continue;
      chosen.init(t);
      this.actives.add(chosen);
    }
  }
  
  public void step(Tiles t)
  {
    this.frame += 1;
    
    if (this.selected.size() == 0)
      return;
    
    if (this.frame % STEP_FRAMES == 0)
    {
      for (Iterator<IPeriodic> it = this.actives.iterator();
           it.hasNext();
          )
      {
        IPeriodic i = it.next();
        if (i.step(t))
          it.remove();
      }
    }
    
    for (IPeriodic i : this.actives)
      i.render(t);
    
    if (this.frame % CHOOSE_FRAMES == 0)
      this.chooseNew(t);
  }

  static final int BORDER = 2;
  static Random rand = new Random();
  
  static float randomX(Tiles t)
  {
    return PeriodicManager.rand.nextFloat() * t.width;
  }

  static float randomY(Tiles t)
  {
    return PeriodicManager.rand.nextFloat() * t.height;
  }
  
  static int randomColour()
  {
    return PeriodicManager.rand.nextInt(Config.featureColourCount);
  }
}
