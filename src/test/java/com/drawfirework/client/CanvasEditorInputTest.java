package com.drawfirework.client;

import com.drawfirework.canvas.CanvasData;
import com.drawfirework.editor.CanvasEditorModel;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.junit.Test;
import static org.junit.Assert.*;

public class CanvasEditorInputTest {
    private MouseButtonEvent event(double x, double y, int button) {
        return new MouseButtonEvent(x,y,new MouseButtonInfo(button,0));
    }
    private MouseButtonEvent left(double x, double y) {
        return event(x,y,InputConstants.MOUSE_BUTTON_LEFT);
    }
    @Test public void nativeLeftClickUsesSelectedColorAndWorkingCopy() {
        var original = new CanvasData(2,2,new int[4]);
        var model = new CanvasEditorModel(original); var input = new CanvasEditorInput(model);
        assertTrue(input.click(left(12,22),10,20,4,0xFFFF4040));
        assertEquals(0xFFFF4040,model.pixel(0,0));
        assertTrue(input.release(left(12,22)));
        assertTrue(input.click(left(16,26),10,20,4,0xFF4080FF));
        assertEquals(0xFF4080FF,model.pixel(1,1));
        assertEquals(0,original.pixel(0,0)); assertEquals(0,original.pixel(1,1));
        assertEquals(model.pixel(1,1),model.snapshot().pixel(1,1));
    }
    @Test public void nativeDragInterpolatesAndReleaseStopsPainting() {
        var model=new CanvasEditorModel(null); var input=new CanvasEditorInput(model);
        assertFalse(input.drag(left(10,20),10,20,4,-1));
        assertTrue(input.click(left(10,20),10,20,4,-1));
        assertTrue(input.drag(left(73.9,83.9),10,20,4,-1));
        for(int i=0;i<16;i++) assertEquals(-1,model.pixel(i,i));
        assertTrue(input.release(left(1000,1000)));
        assertFalse(input.drag(left(10,80),10,20,4,0xFFFF0000));
        assertEquals(0,model.pixel(0,15));
    }
    @Test public void eraserAndResetKeepCancelIsolated() {
        var original=new CanvasData(1,1,new int[]{0xFF123456});
        var model=new CanvasEditorModel(original); var input=new CanvasEditorInput(model);
        assertTrue(input.click(left(11,21),10,20,4,0));
        assertEquals(0,model.pixel(0,0));
        input.reset();
        assertFalse(input.drag(left(11,21),10,20,4,-1));
        assertFalse(input.release(left(11,21)));
        assertEquals(0xFF123456,original.pixel(0,0));
    }
    @Test public void toolsOutsideBoundsAndNonLeftButtonsNeverPaint() {
        var model=new CanvasEditorModel(null); var input=new CanvasEditorInput(model);
        var before=model.snapshot();
        for(int button:new int[]{0,InputConstants.MOUSE_BUTTON_MIDDLE,InputConstants.MOUSE_BUTTON_RIGHT}) {
            assertFalse(input.click(event(10,20,button),10,20,4,-1));
            assertFalse(input.drag(event(10,20,button),10,20,4,-1));
            assertFalse(input.release(event(10,20,button)));
        }
        for(double[] p:new double[][]{{9.9,20},{10,19.9},{74,20},{10,84},{20,120}}) {
            assertFalse(input.click(left(p[0],p[1]),10,20,4,-1));
            assertFalse(input.drag(left(12,22),10,20,4,-1));
        }
        assertEquals(before,model.snapshot());
        assertTrue(input.click(left(10,20),10,20,4,-1));
        assertTrue(input.drag(left(1000,1000),10,20,4,-1));
        assertTrue(input.drag(left(73.9,83.9),10,20,4,-1));
        assertEquals(0,model.pixel(8,8)); // Outside/reentry must not bridge the canvas.
        assertTrue(input.release(left(73.9,83.9)));
        assertFalse(input.click(left(74,84),10,20,4,-1));
        assertEquals(-1,model.pixel(15,15));
    }
}
