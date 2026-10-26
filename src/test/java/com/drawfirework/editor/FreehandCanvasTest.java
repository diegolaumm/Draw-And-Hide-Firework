package com.drawfirework.editor;

import com.drawfirework.canvas.CanvasData;
import org.junit.Test;
import static org.junit.Assert.*;

public class FreehandCanvasTest {
    @Test public void dimensionsAndUntouchedPixelsAreLossless() {
        for(int w=1;w<=32;w++) for(int h=1;h<=32;h++) {
            int[] p=new int[w*h]; for(int i=0;i<p.length;i++) p[i]=i*123456789;
            var original=new CanvasData(w,h,p); var c=new FreehandCanvas(original);
            assertEquals(w*8,c.width()); assertEquals(h*8,c.height());
            assertSame(original,c.snapshot());
            c.setDiameter(2); c.endStroke(); assertSame(original,c.snapshot());
            c.paint(0,0,0xFFFF0000); assertEquals(w,c.snapshot().width()); assertEquals(h,c.snapshot().height());
        }
        assertEquals(256,new FreehandCanvas(null).width());
        assertThrows(IllegalArgumentException.class,()->new FreehandCanvas(new CanvasData(33,1,new int[33])));
    }
    @Test public void fastDragAndBrushThickness() {
        var thin=new FreehandCanvas(null); thin.setDiameter(2);
        thin.paint(8,128,0xFFFF0000); thin.paint(248,128,0xFFFF0000);
        var thick=new FreehandCanvas(null); thick.setDiameter(24);
        thick.paint(8,128,0xFFFF0000); thick.paint(248,128,0xFFFF0000);
        for(int x=8;x<248;x++) assertTrue(thin.pixel(x,128)>>>24>0);
        assertEquals(0,thin.pixel(128,135)); assertEquals(255,thick.pixel(128,135)>>>24);
        assertTrue((thin.snapshot().pixel(16,16)>>>24)<(thick.snapshot().pixel(16,16)>>>24));
    }
    @Test public void alphaCompositingDoesNotAccumulateWithinStroke() {
        var c=new FreehandCanvas(null); c.setDiameter(16);
        c.paint(30,30,0x80FF0000); c.paint(30,30,0x80FF0000);
        assertEquals(0x80FF0000,c.pixel(30,30));
        c.endStroke(); c.paint(30,30,0x800000FF);
        assertEquals(192,c.pixel(30,30)>>>24);
        assertEquals(85,(c.pixel(30,30)>>>16)&255); assertEquals(170,c.pixel(30,30)&255);
        c.endStroke(); c.paint(30,30,0); assertEquals(0,c.pixel(30,30));
    }
    @Test public void boundaryAndOutsideReentry() {
        var c=new FreehandCanvas(null); c.setDiameter(64);
        assertTrue(c.paint(0,0,-1)); assertTrue(c.paint(255.999,255.999,-1));
        for(double v:new double[]{-1,256,Double.NaN,Double.POSITIVE_INFINITY,Double.MAX_VALUE})
            assertFalse(c.paint(v,4,-1));
        assertThrows(IllegalArgumentException.class,()->c.setDiameter(0));
        assertThrows(IllegalArgumentException.class,()->c.setDiameter(65));
        var d=new FreehandCanvas(null); d.paint(0,0,-1); d.paint(-1,0,-1); d.paint(250,250,-1);
        assertEquals(0,d.pixel(128,128));
    }
    @Test public void thinLinePreservesCoverageAndHueWithoutBlackFringe() {
        var c=new FreehandCanvas(null); c.setDiameter(1);
        c.paint(8.5,128.5,0xFFFF0000); c.paint(247.5,128.5,0xFFFF0000);
        CanvasData out=c.snapshot();
        for(int x=2;x<30;x++) {
            int p=out.pixel(x,16); assertTrue(p>>>24>0); assertTrue(p>>>24<64);
            assertEquals(0xFF0000,p&0xFFFFFF);
        }
        assertEquals(out,c.snapshot());
    }
    @Test public void diagonalAndArcRemainConnectedAtOutputScale() {
        var d=new FreehandCanvas(null); d.setDiameter(2);
        d.paint(8,8,0xFF00FF00); d.paint(248,248,0xFF00FF00);
        for(int i=1;i<31;i++) assertTrue(d.snapshot().pixel(i,i)>>>24>0);
        var arc=new FreehandCanvas(null); arc.setDiameter(3);
        for(int degree=0;degree<=180;degree+=2) {
            double a=Math.toRadians(degree); arc.paint(128+80*Math.cos(a),128-80*Math.sin(a),0xFF0000FF);
        }
        var out=arc.snapshot();
        for(int degree=0;degree<=180;degree+=5) {
            double a=Math.toRadians(degree); int x=(int)((128+80*Math.cos(a))/8),y=(int)((128-80*Math.sin(a))/8);
            assertTrue("arc angle "+degree,out.pixel(x,y)>>>24>0);
        }
        assertEquals(0,out.pixel(16,16));
    }
    @Test public void transparentRgbCannotPolluteColorsAndMassIsPreserved() {
        int[] p=new int[1024]; java.util.Arrays.fill(p,0x000000FF);
        var c=new FreehandCanvas(new CanvasData(32,32,p)); c.setDiameter(3);
        c.paint(10,60,0xFFFF0000); c.paint(240,60,0xFFFF0000);
        var out=c.snapshot(); long mass=0,outMass=0;
        for(int y=0;y<256;y++) for(int x=0;x<256;x++) mass+=c.pixel(x,y)>>>24;
        for(int v:out.pixels()) { outMass+=v>>>24; if(v>>>24>0) assertEquals(0xFF0000,v&0xFFFFFF); }
        assertEquals(mass/64.0,outMass,32.0);
    }
    @Test public void colorBoundaryUsesPremultipliedMixAndEraseRetainsHue() {
        assertEquals(0xC05500AA,FreehandCanvas.composite(0x80FF0000,0x800000FF,255));
        assertEquals(0x7FFF0000,FreehandCanvas.composite(0xFFFF0000,0,128));
        var c=new FreehandCanvas(null); c.setDiameter(8);
        c.paint(30,30,0xFFFF0000); c.endStroke(); c.paint(34,30,0xFF0000FF);
        int p=c.snapshot().pixel(3,3);
        assertTrue((p>>>24)>0); assertTrue(((p>>>16)&255)>0); assertTrue((p&255)>0);
        assertEquals(0,(p>>>8)&255);
    }
    @Test public void undoRedoSingleStroke() {
        var c = new FreehandCanvas(null);
        assertFalse(c.canUndo());
        assertFalse(c.canRedo());
        c.paint(10, 10, 0xFFFF0000);
        c.endStroke();
        assertTrue(c.canUndo());
        assertFalse(c.canRedo());
        assertTrue(c.pixel(10, 10) != 0);

        assertTrue(c.undo());
        assertFalse(c.canUndo());
        assertTrue(c.canRedo());
        assertEquals(0, c.pixel(10, 10));

        assertTrue(c.redo());
        assertTrue(c.canUndo());
        assertFalse(c.canRedo());
        assertTrue(c.pixel(10, 10) != 0);
    }
    @Test public void undoRedoMultipleStrokesAndRedoBranchTruncation() {
        var c = new FreehandCanvas(null);
        c.paint(10, 10, 0xFFFF0000); c.endStroke();
        c.paint(20, 20, 0xFF00FF00); c.endStroke();
        c.paint(30, 30, 0xFF0000FF); c.endStroke();

        assertTrue(c.undo()); // undo 3
        assertEquals(0, c.pixel(30, 30));
        assertTrue(c.pixel(20, 20) != 0);

        assertTrue(c.undo()); // undo 2
        assertEquals(0, c.pixel(20, 20));
        assertTrue(c.pixel(10, 10) != 0);

        // New stroke truncates redo stack
        c.paint(40, 40, 0xFFFFFF00); c.endStroke();
        assertFalse(c.canRedo());

        assertTrue(c.undo()); // undo 4
        assertEquals(0, c.pixel(40, 40));
        assertTrue(c.pixel(10, 10) != 0);

        assertTrue(c.undo()); // undo 1
        assertEquals(0, c.pixel(10, 10));
        assertFalse(c.canUndo());
    }
    @Test public void historyBoundedByMaxLimit() {
        var c = new FreehandCanvas(null);
        for (int i = 0; i < 30; i++) {
            c.paint(i * 4, 10, 0xFFFF0000);
            c.endStroke();
        }
        int undoCount = 0;
        while (c.undo()) {
            undoCount++;
        }
        assertEquals(FreehandCanvas.MAX_HISTORY, undoCount);
        assertEquals(16, FreehandCanvas.MAX_HISTORY);
    }
    @Test public void undoWhileStrokeActiveUndoesInProgressStroke() {
        var c = new FreehandCanvas(null);
        // User starts drawing first stroke, mouse is still down (active = true)
        c.paint(25, 25, 0xFFFF0000);
        assertTrue(c.isActive());
        assertTrue(c.pixel(25, 25) != 0);
        assertTrue(c.canUndo());

        // Pressing Undo while stroke is in progress:
        assertTrue(c.undo());
        assertFalse(c.isActive());
        assertEquals(0, c.pixel(25, 25)); // restored to blank!

        // Can redo to restore that stroke
        assertTrue(c.redo());
        assertTrue(c.pixel(25, 25) != 0);
    }
    @Test public void redoWhileNewStrokeActiveSafelyClearsRedoWithoutException() {
        var c = new FreehandCanvas(null);
        // 1. Draw stroke 1
        c.paint(10, 10, 0xFFFF0000);
        c.endStroke();

        // 2. Undo, so redoStack has stroke 1
        assertTrue(c.undo());
        assertTrue(c.canRedo());

        // 3. User starts drawing a new stroke and keeps mouse down (active = true)
        c.paint(50, 50, 0xFF00FF00);
        assertTrue(c.isActive());

        // 4. User presses Ctrl+Y (Redo) while mouse is held down:
        // Must safely commit new stroke, clear redo branch, return false, and NEVER throw NoSuchElementException!
        assertFalse(c.redo());
        assertFalse(c.canRedo());
        assertEquals(0xFF00FF00, c.pixel(50, 50));

        // 5. Subsequent Undo undos the new stroke back to blank
        assertTrue(c.undo());
        assertEquals(0, c.pixel(50, 50));
    }
    @Test public void floodFillEmptyCanvasAndUndo() {
        var c = new FreehandCanvas(null);
        assertTrue(c.fill(50, 50, 0xFF4080FF));
        for (int y = 0; y < 256; y += 32) {
            for (int x = 0; x < 256; x += 32) {
                assertEquals(0xFF4080FF, c.pixel(x, y));
            }
        }
        assertTrue(c.undo());
        assertEquals(0, c.pixel(50, 50));
    }
    @Test public void floodFillBoundedRegion() {
        var c = new FreehandCanvas(null);
        c.setDiameter(4);
        // Draw a closed box around the perimeter: (10,10) -> (50,10) -> (50,50) -> (10,50) -> (10,10)
        c.paint(10, 10, 0xFFFF0000);
        c.paint(50, 10, 0xFFFF0000);
        c.paint(50, 50, 0xFFFF0000);
        c.paint(10, 50, 0xFFFF0000);
        c.paint(10, 10, 0xFFFF0000);
        c.endStroke();

        // Inside at (30,30) is initially transparent
        assertEquals(0, c.pixel(30, 30));

        // Fill inside at (30,30) with Blue
        assertTrue(c.fill(30, 30, 0xFF0000FF));
        assertEquals(0xFF0000FF, c.pixel(30, 30));
        // Outside remains transparent
        assertEquals(0, c.pixel(5, 5));
        assertEquals(0, c.pixel(100, 100));

        // Fill with eraser inside
        assertTrue(c.fill(30, 30, 0));
        assertEquals(0, c.pixel(30, 30));
        // Undo eraser
        assertTrue(c.undo());
        assertEquals(0xFF0000FF, c.pixel(30, 30));
    }
    @Test public void floodFillIdenticalColorOrOutOfBoundsNoOp() {
        var c = new FreehandCanvas(null);
        assertFalse(c.fill(10, 10, 0)); // transparent -> transparent: no-op
        assertFalse(c.canUndo());

        c.paint(10, 10, 0xFFFF0000); c.endStroke();
        assertFalse(c.fill(10, 10, 0xFFFF0000)); // same color: no-op

        assertFalse(c.fill(-1, 10, 0xFF00FF00)); // out of bounds
        assertFalse(c.fill(300, 10, 0xFF00FF00));
        assertFalse(c.fill(Double.NaN, 10, 0xFF00FF00));
    }
}
