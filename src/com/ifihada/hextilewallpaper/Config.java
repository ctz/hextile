package com.ifihada.hextilewallpaper;

import android.util.Log;

public class Config
{
  private static final String TAG = "Config";
  private static int tileSize = 60;
  private static int tilePadding = 6;
  
  // Colours here are RGBA
  public final static Colour DEFAULT_BASE_COLOUR = Colour.fromRGBA(0x333344ff);
  public final static Colour DEFAULT_FEAT_COLOUR_1 = Colour.fromRGBA(0x33b5e5ff);
  public final static Colour DEFAULT_FEAT_COLOUR_2 = Colour.fromRGBA(0xaa66ccff);
  public final static Colour DEFAULT_FEAT_COLOUR_3 = Colour.fromRGBA(0x99cc00ff);
  public final static Colour DEFAULT_FEAT_COLOUR_4 = Colour.fromRGBA(0xffbb33ff);
  public final static Colour DEFAULT_FEAT_COLOUR_5 = Colour.fromRGBA(0xff4444ff);
  
  private final static Colour[] featureColours = new Colour[]
  {
   DEFAULT_FEAT_COLOUR_1,
   DEFAULT_FEAT_COLOUR_2,
   DEFAULT_FEAT_COLOUR_3,
   DEFAULT_FEAT_COLOUR_4,
   DEFAULT_FEAT_COLOUR_5,
  };
  
  private static Colour baseColour = DEFAULT_BASE_COLOUR;
  
  public final static int featureColourCount = Config.featureColours.length;

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
  
  public static void setBaseColour(Colour c)
  {
    Config.baseColour = c;
  }
  
  public static Colour getBaseColour()
  {
    return Config.baseColour;
  }
  
  public static void setFeatureColour(int i, Colour c)
  {
    if (i >= 0 && i < Config.featureColours.length)
      Config.featureColours[i] = c;
  }
  
  public static Colour getFeatureColour(int i)
  {
    if (i >= 0 && i < Config.featureColours.length)
      return Config.featureColours[i];
    else
      return Config.getBaseColour();
  }
    
}
