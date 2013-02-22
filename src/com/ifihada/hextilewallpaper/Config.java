package com.ifihada.hextilewallpaper;

import android.util.Log;

public class Config
{
  private static final String TAG = "Config";
  private static int tileSize = 60;
  private static int tilePadding = 6;

  public static void setTileSize(int s)
  {
    Log.v(TAG, "new tile size is " + s);
    Config.tileSize = s;
  }
  
  public static int getTileSize()
  {
    return Config.tileSize;
  }
  
  public static void setTilePadding(int p)
  {
    Log.v(TAG, "new tile padding is " + p);
    Config.tilePadding = p;
  }
  
  public static int getTilePadding()
  {
    return Config.tilePadding;
  }
  
    
}
