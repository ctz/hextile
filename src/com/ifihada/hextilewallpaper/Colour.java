package com.ifihada.hextilewallpaper;

public class Colour
{
  float r, g, b, a;
  
  Colour()
  {
    this(0);
  }
  
  Colour(int c)
  {
    this.setInt(c);
  }
  
  Colour(Colour x)
  {
    this.r = x.r;
    this.g = x.g;
    this.b = x.b;
    this.a = x.a;
  }
  
  public Colour setInt(int c)
  {
    this.r = R(c) / 255f;
    this.g = G(c) / 255f;
    this.b = B(c) / 255f;
    this.a = A(c) / 255f;
    return this;
  }
  
  public Colour set(Colour other)
  {
    this.r = other.r;
    this.g = other.g;
    this.b = other.b;
    this.a = other.a;
    return this;
  }
  
  public Colour write(float[] f, int offs)
  {
    f[offs + 0] = this.r;
    f[offs + 1] = this.g;
    f[offs + 2] = this.b;
    f[offs + 3] = this.a;
    return this;
  }
  
  // returns false if already at or close to target
  public boolean lerpRGB(Colour target, float alpha)
  {
    float resolution = 0.01f;
    
    if ((this.r == target.r || nearEnough(this.r, target.r, resolution)) &&
        (this.g == target.g || nearEnough(this.g, target.g, resolution)) &&
        (this.b == target.b || nearEnough(this.b, target.b, resolution)))
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

  static int R(int c) { return c >> 24 & 0xff; }
  static int G(int c) { return c >> 16 & 0xff; }
  static int B(int c) { return c >> 8 & 0xff; }
  static int A(int c) { return c & 0xff; }
}
