package com.drawfirework.editor;

import com.drawfirework.canvas.CanvasData;
import org.junit.Test;
import static org.junit.Assert.*;

public class CanvasEditorTest {
    @Test public void newCanvasAndExactCoordinateMapping() {
        var m = new CanvasEditorModel(null);
        assertEquals(16,m.width()); assertEquals(16,m.height());
        assertEquals(0,m.indexAt(10,20,10,20,4));
        assertEquals(17,m.indexAt(14,24,10,20,4));
        assertEquals(255,m.indexAt(73.999,83.999,10,20,4));
        for (double[] p : new double[][]{{9.9,20},{10,19.9},{74,20},{10,84},{Double.NaN,20},{10,Double.POSITIVE_INFINITY}})
            assertEquals(-1,m.indexAt(p[0],p[1],10,20,4));
        assertEquals(-1,m.indexAt(10,20,10,20,0));
    }
    @Test public void actualPaintInterpolatesDragAndErases() {
        var m = new CanvasEditorModel(null);
        assertTrue(m.paint(0,0,0,0,4,0xFFFF0000));
        assertTrue(m.paint(63,63,0,0,4,0xFFFF0000));
        for (int i=0;i<16;i++) assertEquals(0xFFFF0000,m.pixel(i,i));
        m.endStroke(); m.paint(32,32,0,0,4,0);
        assertEquals(0,m.pixel(8,8)); assertEquals(0xFFFF0000,m.pixel(7,7));
    }
    @Test public void outsideDragNeverWrapsAndReentryStartsNewStroke() {
        var m = new CanvasEditorModel(null);
        m.paint(0,0,0,0,4,-1);
        assertFalse(m.paint(-1,200,0,0,4,-1));
        assertFalse(m.paint(64,0,0,0,4,-1));
        m.paint(63,63,0,0,4,-1);
        assertEquals(0,m.pixel(8,8)); assertEquals(-1,m.pixel(15,15));
        assertEquals(2,java.util.Arrays.stream(m.snapshot().pixels()).filter(x->x==-1).count());
    }
    @Test public void allExistingRectangleSizesAndAlphaSurviveCancelAndCopy() {
        for(int w=1;w<=32;w++) for(int h=1;h<=32;h++) {
            int[] pixels=new int[w*h]; for(int i=0;i<pixels.length;i++) pixels[i]=i*1234567;
            var original=new CanvasData(w,h,pixels); var editor=new CanvasEditorModel(original);
            assertEquals(original,editor.snapshot());
            editor.paint(0,0,0,0,2,0xFF123456);
            assertArrayEquals(pixels,original.pixels()); // no save: source/component immutable and unchanged
            var copy=editor.snapshot().pixels(); copy[0]=0;
            assertEquals(0xFF123456,editor.pixel(0,0));
        }
        assertThrows(IllegalArgumentException.class,()->new CanvasEditorModel(new CanvasData(33,1,new int[33])));
    }
}
