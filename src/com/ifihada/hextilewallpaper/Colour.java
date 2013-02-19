package com.ifihada.hextilewallpaper;

public class Colour
{
  static void rgbaWrite(float[] f, int offs, int col)
  {
    f[offs + 0] = R(col) / 255f;
    f[offs + 1] = G(col) / 255f;
    f[offs + 2] = B(col) / 255f;
    f[offs + 3] = A(col) / 255f;
  }
  
  static int saturate(float x, int low, int high)
  {
    if (x < low)
      return low;
    if (x > high)
      return high;
    return (int) x;
  }

  static int saturate(int x, int low, int high)
  {
    if (x < low)
      return low;
    if (x > high)
      return high;
    return x;
  }
  
  static int lerp(int x, int y, int mul)
  {
    if (x == y)
      return x;
    
    int d = y - x;
    int sign = d >= 0 ? 1 : -1;
    int mag = d * sign;
    
    if (mag <= 2)
      mul *= 1;
    if (mag <= 8)
      mul *= 1;
    if (mag <= 16)
      mul *= 2;
    if (mag <= 64)
      mul *= 3;
    else
      mul *= 4;
    
    return x + saturate(1 * mul, 1, mag) * sign;
  }

  static int R(int c) { return c >> 24 & 0xff; }
  static int G(int c) { return c >> 16 & 0xff; }
  static int B(int c) { return c >> 8 & 0xff; }
  static int A(int c) { return c & 0xff; }
  
  static int RGBA(int r, int g, int b, int a)
  {
    return (r & 0xff) << 24 | (g & 0xff) << 16 | (b & 0xff) << 8 | (a & 0xff);
  }
  
  static int rgbaMul(int col, float a)
  {
    return RGBA(saturate(R(col) * a, 0, 255),
                saturate(G(col) * a, 0, 255),
                saturate(B(col) * a, 0, 255),
                A(col));
  }
  
  static int rgbaLerp(int col, int targ, int mul)
  {
    return RGBA(lerp(R(col), R(targ), mul),
                lerp(G(col), G(targ), mul),
                lerp(B(col), B(targ), mul),
                lerp(A(col), A(targ), mul));
  }
}
