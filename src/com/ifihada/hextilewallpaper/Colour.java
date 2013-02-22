package com.ifihada.hextilewallpaper;

import android.annotation.SuppressLint;

public class Colour
{
  float r, g, b, a;
  
  public Colour()
  {
    this(0);
  }
  
  private Colour(int c)
  {
    this.setRGBA(c);
  }
  
  public Colour(Colour x)
  {
    this.set(x);
  }
  
  public Colour setRGBA(int c)
  {
    this.r = RGBA_R(c) / 255f;
    this.g = RGBA_G(c) / 255f;
    this.b = RGBA_B(c) / 255f;
    this.a = RGBA_A(c) / 255f;
    return this;
  }
  
  public Colour setARGB(int c)
  {
    this.r = ARGB_R(c) / 255f;
    this.g = ARGB_G(c) / 255f;
    this.b = ARGB_B(c) / 255f;
    this.a = ARGB_A(c) / 255f;
    return this;
  }
  
  public static Colour fromRGBA(int c)
  {
    Colour out = new Colour();
    out.setRGBA(c);
    return out;
  }
  
  public static Colour fromARGB(int c)
  {
    Colour out = new Colour();
    out.setARGB(c);
    return out;
  }
  
  public Colour set(Colour other)
  {
    this.r = other.r;
    this.g = other.g;
    this.b = other.b;
    this.a = other.a;
    return this;
  }
  
  @SuppressLint("DefaultLocale")
  public String toString()
  {
    int argb = this.getARGB();
    return String.format("#%06x - rgb(%d, %d, %d)",
                         argb & 0xffffff,
                         Colour.ARGB_R(argb),
                         Colour.ARGB_G(argb),
                         Colour.ARGB_B(argb));
  }
  
  public Colour write(float[] f, int offs)
  {
    f[offs + 0] = this.r;
    f[offs + 1] = this.g;
    f[offs + 2] = this.b;
    f[offs + 3] = this.a;
    return this;
  }
  
  public int getARGB()
  {
    int ia, ir, ig, ib;

    ia = (int) (this.a * 255);
    ir = (int) (this.r * 255);
    ig = (int) (this.g * 255);
    ib = (int) (this.b * 255);
    
    ia &= 0xff;
    ir &= 0xff;
    ig &= 0xff;
    ib &= 0xff;
    
    return ia << 24 | ir << 16 | ig << 8 | ib; 
  }
  
  public boolean equalsOrClose(Colour target)
  {
    float resolution = 0.005f;
    return (this.r == target.r || nearEnough(this.r, target.r, resolution)) &&
           (this.g == target.g || nearEnough(this.g, target.g, resolution)) &&
           (this.b == target.b || nearEnough(this.b, target.b, resolution));
  }
  
  // returns false if already at or close to target
  public boolean lerpRGB(Colour target, float alpha)
  {
    if (this.equalsOrClose(target))
    {
      this.r = target.r;
      this.g = target.g;
      this.b = target.b;
      return false;
    }

    this.r = lerp(this.r, target.r, alpha);
    this.g = lerp(this.g, target.g, alpha);
    this.b = lerp(this.b, target.b, alpha);
    return true;
  }
  
  public Colour mulRGB(float m)
  {
    this.r = saturate(this.r * m, 0f, 1f);
    this.g = saturate(this.g * m, 0f, 1f);
    this.b = saturate(this.b * m, 0f, 1f);
    return this;
  }
  
  static float lerp(float x, float y, float alpha)
  {
    float d = y - x;
    return x + d * alpha;
  }
  
  static boolean nearEnough(float x, float y, float diff)
  {
    return Math.abs(y - x) <= diff;
  }
  
  static float saturate(float x, float low, float high)
  {
    if (x < low)
      return low;
    if (x > high)
      return high;
    return x;
  }

  // Assume RGBA
  static int RGBA_R(int c) { return c >> 24 & 0xff; }
  static int RGBA_G(int c) { return c >> 16 & 0xff; }
  static int RGBA_B(int c) { return c >> 8 & 0xff; }
  static int RGBA_A(int c) { return c & 0xff; }
  
  static int ARGB_A(int c) { return c >> 24 & 0xff; }
  static int ARGB_R(int c) { return c >> 16 & 0xff; }
  static int ARGB_G(int c) { return c >> 8 & 0xff; }
  static int ARGB_B(int c) { return c & 0xff; }
}
