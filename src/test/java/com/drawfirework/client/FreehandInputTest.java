package com.drawfirework.client;

import com.drawfirework.editor.FreehandCanvas;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.junit.Test;
import static org.junit.Assert.*;

public class FreehandInputTest {
    private MouseButtonEvent event(double x,double y,int button) {
        return new MouseButtonEvent(x,y,new MouseButtonInfo(button,0));
    }
    private MouseButtonEvent left(double x,double y) { return event(x,y,InputConstants.MOUSE_BUTTON_LEFT); }
    @Test public void nativeCoordinatesScaleContinuouslyAndReleaseStops() {
        var c=new FreehandCanvas(null); var input=new FreehandInput(c);
        assertFalse(input.drag(left(10,20),10,20,128,128,-1));
        assertTrue(input.click(left(20.25,30.25),10,20,128,128,0xFFFF0000));
        assertEquals(0xFFFF0000,c.pixel(20,20));
        assertTrue(input.drag(left(120,130),10,20,128,128,0xFFFF0000));
        for(int i=21;i<220;i++) assertTrue(c.pixel(i,i)>>>24>0);
        assertTrue(input.release(left(1000,1000)));
        assertFalse(input.drag(left(30,40),10,20,128,128,-1));
    }
    @Test public void otherButtonsToolsAndOutsideReentryDoNotBridge() {
        var c=new FreehandCanvas(null); var input=new FreehandInput(c);
        for(int b:new int[]{0,InputConstants.MOUSE_BUTTON_RIGHT,InputConstants.MOUSE_BUTTON_MIDDLE})
            assertFalse(input.click(event(12,22,b),10,20,256,256,-1));
        assertFalse(input.click(left(10,300),10,20,256,256,-1));
        assertTrue(input.click(left(10,20),10,20,256,256,-1));
        assertTrue(input.drag(left(-100,20),10,20,256,256,-1));
        assertTrue(input.drag(left(265,275),10,20,256,256,-1));
        assertEquals(0,c.pixel(128,128));
        input.reset(); assertFalse(input.drag(left(138,148),10,20,256,256,-1));
    }
    @Test public void eraserUsesSameBrushAndCancelLeavesOriginal() {
        var original=com.drawfirework.canvas.CanvasData.blank();
        var c=new FreehandCanvas(original); var input=new FreehandInput(c); c.setDiameter(32);
        input.click(left(128,128),0,0,256,256,0xFF0000FF); input.release(left(128,128));
        input.click(left(128,128),0,0,256,256,0);
        assertEquals(0,c.pixel(128,128)); assertEquals(0,original.pixel(16,16));
    }
    @Test public void fillToolWorksThroughInputAndIgnoresDrag() {
        var c = new FreehandCanvas(null);
        var input = new FreehandInput(c);
        input.setTool(FreehandInput.Tool.FILL);
        assertEquals(FreehandInput.Tool.FILL, input.tool());
        assertTrue(input.click(left(128, 128), 0, 0, 256, 256, 0xFF4080FF));
        assertEquals(0xFF4080FF, c.pixel(0, 0));
        assertEquals(0xFF4080FF, c.pixel(255, 255));
        // Drag does not trigger fill repeatedly
        assertFalse(input.drag(left(150, 150), 0, 0, 256, 256, 0xFFFF0000));
        assertFalse(input.release(left(150, 150)));
        // Undo works
        assertTrue(c.undo());
        assertEquals(0, c.pixel(0, 0));
    }
}
