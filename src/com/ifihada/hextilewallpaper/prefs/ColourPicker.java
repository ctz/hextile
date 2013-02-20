package com.ifihada.hextilewallpaper.prefs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

class ColourPicker extends View
{
  public ColourPicker(Context context, AttributeSet attrs)
  {
    super(context, attrs);
  }
  
  Paint p = new Paint();
  float[] hsv = new float[3];
  
  @Override
  public void onDraw(Canvas c)
  {
    hsv[2] = 1f;
    
    for (int x = 0; x < c.getWidth(); x++)
    {
      hsv[0] = (x / (float) c.getWidth()) * 360f;
      
      for (int y = 0; y < c.getHeight(); y++)
      {
        hsv[1] = y / (float) c.getHeight();
        p.setColor(Color.HSVToColor(hsv));
        c.drawLine(x, y, x + 1, y + 1, p);
      }
    }
  }
}