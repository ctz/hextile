package com.ifihada.hextilewallpaper;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.opengles.GL10;
import android.graphics.Paint;

class HexGeom
{
  final float r;
  final float d;
  private final float w;
  final float h;
  private final float[] verts;
  private final float[] colours;
  private final short[] indices, topLineIndices, bottomLineIndices;
  private final Paint shadow, hilight;

  private FloatBuffer vertexBuffer;
  private FloatBuffer colourBuffer;
  private ShortBuffer indexBuffer;
  private ShortBuffer topLineBuffer;
  private ShortBuffer bottomLineBuffer;
  
  static FloatBuffer toFloatBuffer(float[] array)
  {
    Buffer b;
    b = ByteBuffer
        .allocateDirect(4 * array.length)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(array)
        .position(0);
    return (FloatBuffer) b;
  }
  
  static ShortBuffer toShortBuffer(short[] array)
  {
    Buffer b;
    b = ByteBuffer
        .allocateDirect(2 * array.length)
        .order(ByteOrder.nativeOrder())
        .asShortBuffer()
        .put(array)
        .position(0);
    return (ShortBuffer) b;
  }

  HexGeom(final float r)
  {
    this.r = r;
    this.d = r * 2;
    this.w = r * .5f;
    this.h = r * Const.sin60;

    /*   1_____2
     *   /     \
     * 6/  0.   \3
     *  \       /
     *   \_____/
     *   5     4
     */
    this.verts = new float[]
    {
      0f, 0f, 0f,
      -w, -h, 0f,
      w, -h, 0f,
      r, 0f, 0f,
      w, h, 0f,
      -w, h, 0f,
      -r, 0f, 0f
     };
    
    this.indices = new short[]
    {
     0, 1, 2,
     0, 2, 3,
     0, 3, 4,
     0, 4, 5,
     0, 5, 6,
     0, 6, 1
    };
    
    this.colours = new float[]
    {
     1.0f, 0.0f, 0.0f, 1.0f,
     
     0.8f, 0.0f, 0.0f, 1.0f,
     0.8f, 0.0f, 0.0f, 1.0f,
     
     1.0f, 0.0f, 0.0f, 1.0f,
     
     1.0f, 0.2f, 0.2f, 1.0f,
     1.0f, 0.2f, 0.2f, 1.0f,
     
     1.0f, 0.0f, 0.0f, 1.0f
    };
    
    this.topLineIndices = new short[]
    {
     1, 2
    };
    
    this.bottomLineIndices = new short[]
    {
     4, 5
    };
    
    this.vertexBuffer = toFloatBuffer(this.verts);
    this.colourBuffer = toFloatBuffer(this.colours);
    
    this.indexBuffer = toShortBuffer(this.indices);
    this.topLineBuffer = toShortBuffer(this.topLineIndices);
    this.bottomLineBuffer = toShortBuffer(this.bottomLineIndices);

    this.shadow = new Paint(Paint.ANTI_ALIAS_FLAG);
    this.shadow.setColor(0x5f000000);
    this.hilight = new Paint(Paint.ANTI_ALIAS_FLAG);
    this.hilight.setColor(0x7fffffff);
  }
  
  static Colour tmp = new Colour();
  
  void setColour(Colour colour)
  {
    float darken = 0.8f;
    float lighten = 1.2f;
    
    colour.write(this.colours, 0)
          .write(this.colours, 12)
          .write(this.colours, 24);
    tmp.set(colour)
       .mulRGB(lighten)
       .write(this.colours, 4)
       .write(this.colours, 8);
    tmp.set(colour)
       .mulRGB(darken)
       .write(this.colours, 16)
       .write(this.colours, 20);
    
    this.colourBuffer.put(this.colours).position(0);
  }
  
  void draw(GL10 gl, TilePosition t)
  {
    this.setColour(t.colour);
    
    gl.glMatrixMode(GL10.GL_MODELVIEW);
    gl.glLoadIdentity();
    gl.glTranslatef(t.cx, t.y, 0f);
    gl.glScalef(1f, 1f, 1f);
    
    gl.glDisable(GL10.GL_CULL_FACE);
    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, this.vertexBuffer);
    gl.glColorPointer(4, GL10.GL_FLOAT, 0, this.colourBuffer);

    gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
    gl.glColor4f(0f, 0f, 0f, 1f);
    gl.glDrawElements(GL10.GL_TRIANGLES, this.indices.length, GL10.GL_UNSIGNED_SHORT, this.indexBuffer);
    gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    
    gl.glColor4f(1f, 1f, 1f, 0.5f);
    gl.glDrawElements(GL10.GL_LINES, this.topLineIndices.length, GL10.GL_UNSIGNED_SHORT, this.topLineBuffer);
    
    gl.glColor4f(0f, 0f, 0f, 0.5f);
    gl.glDrawElements(GL10.GL_LINES, this.bottomLineIndices.length, GL10.GL_UNSIGNED_SHORT, this.bottomLineBuffer);
    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }

  boolean within(float ox, float oy, float tx, float ty)
  {
    return ox - w < tx && ox + w > tx && oy - h < ty && oy + h > ty;
  }
}