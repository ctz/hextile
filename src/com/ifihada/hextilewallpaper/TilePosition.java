package com.ifihada.hextilewallpaper;

class TilePosition
{
  float x;
  float y;
  float cx;
  int ix;
  int iy;
  
  // adjacent tiles
  int a1x, a1y;
  int a2x, a2y;
  int a3x, a3y;
  int a4x, a4y;
  boolean oddRow;
  int colour;

  TilePosition()
  {
    this.x = 0f;
    this.y = 0f;
    this.cx = 0f;
    this.ix = 0;
    this.iy = 0;
    this.a1x = this.a1y = this.a2x = this.a2y = 0;
    this.a3x = this.a3y = this.a4x = this.a4y = 0;
    this.oddRow = false;
    this.colour = 0xff0000ff;
  }
  
  public String toString()
  {
    return String.format("TilePosition[%f->%f,%f][%d,%d][%s]",
                         this.x, this.cx, this.y,
                         this.ix, this.iy,
                         this.oddRow ? "odd" : "even"
                         );
  }
  
  void updateAdjacent()
  {
    this.a1x = this.a1y = this.a2x = this.a2y = -1;
    this.a3x = this.a3y = this.a4x = this.a4y = -1;
    
    if (this.oddRow)
    {
      this.a1x = this.ix + 1;
      this.a1y = this.iy + 1;
      this.a2x = this.ix;
      this.a2y = this.iy + 1;
      this.a3x = this.ix + 1;
      this.a3y = this.iy - 1;
      this.a4x = this.ix;
      this.a4y = this.iy - 1;
    } else {
      this.a1x = this.ix;
      this.a1y = this.iy + 1;
      this.a2x = this.ix - 1;
      this.a2y = this.iy + 1;
      this.a3x = this.ix;
      this.a3y = this.iy - 1;
      this.a4x = this.ix - 1;
      this.a4y = this.iy - 1;
    }
  }
}