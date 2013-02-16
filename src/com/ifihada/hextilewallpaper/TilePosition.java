package com.ifihada.hextilewallpaper;

import android.graphics.Point;

class TilePosition
{
  float x;
  float y;
  float cx;
  Point i;
  boolean oddRow;

  TilePosition()
  {
    this.x = 0f;
    this.y = 0f;
    this.cx = 0f;
    this.i = new Point(0, 0);
    this.oddRow = false;
  }
  
  public String toString()
  {
    return String.format("TilePosition[%f->%f,%f][%d,%d][%s]",
                         this.x, this.cx, this.y,
                         this.i.x, this.i.y,
                         this.oddRow ? "odd" : "even"
                         );
  }
}