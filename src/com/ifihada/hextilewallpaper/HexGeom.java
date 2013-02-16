package com.ifihada.hextilewallpaper;

import android.graphics.Canvas;
import android.graphics.Paint;

class HexGeom
{
  final float r;
  final float d;
  private final float w;
  final float h;
  private final float[] verts;
  private final Paint shadow, hilight;

  HexGeom( final float r)
  {
    this.r = r;
    this.d = r * 2;
    this.w = r * .5f;
    this.h = r * Const.sin60;

    this.verts = new float[]
    { 0f, 0f, -w, -h, w, -h, // 0-5
        0f, 0f, w, -h, r, 0f, // 6-11
        0f, 0f, r, 0f, w, h, // 12-17
        0f, 0f, w, h, -w, h, // 18-23
        0f, 0f, -w, h, -r, 0f, 0f, 0f, -r, 0f, -w, -h };

    this.shadow = new Paint(Paint.ANTI_ALIAS_FLAG);
    this.shadow.setColor(0x5f000000);
    this.hilight = new Paint(Paint.ANTI_ALIAS_FLAG);
    this.hilight.setColor(0x7fffffff);
  }

  void draw(Canvas cv, float x, float y, Paint fill)
  {
    cv.save();
    cv.translate(x, y);
    cv.drawVertices(Canvas.VertexMode.TRIANGLE_FAN, this.verts.length,
        this.verts, 0, this.verts, 0, (int[]) null, 0, (short[]) null, 0,
        0, fill);
    cv.drawLine(verts[2], verts[3], verts[4], verts[5], this.hilight);
    cv.drawLine(verts[20], verts[21], verts[22], verts[23], this.shadow);
    cv.restore();
  }

  boolean within(float ox, float oy, float tx, float ty)
  {
    return ox - w < tx && ox + w > tx && oy - h < ty && oy + h > ty;
  }
}