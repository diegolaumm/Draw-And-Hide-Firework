package com.drawfirework.client;

import org.junit.Test;
import org.joml.Vector3d;
import org.joml.Quaternionf;
import static org.junit.Assert.*;

public class PlaybackPlaneTest {
    @Test public void sharedCanvasConversionKeepsFixedDefaultsAndBudget() {
        var canvas=com.drawfirework.firework.DevCanvases.smiley();
        var pattern=HeartPrototype.canvasPattern(canvas,CanvasOptions.DEFAULT);
        assertTrue(CanvasOptions.DEFAULT.fixed());
        assertEquals(com.drawfirework.firework.DustPattern.fromCanvas(canvas,3,266),pattern);
        assertTrue(pattern.coreParticles(3)<=800);
    }
    @Test public void explicitServerCenterUsesExistingFixedGeometry() {
        var center=new Vector3d(20,80,30);
        var p=DustPlane.atCenter(CanvasOptions.DEFAULT,center,DustStyle.BLOOM_130.timeline());
        center.set(0,0,0);
        assertEquals(new Vector3d(20,80,30),p.center());
        p.update(20,new Quaternionf()); var first=p.position(1,2,0,1);
        p.update(21,new Quaternionf().rotateY(1)); assertEquals(first,p.position(1,2,0,1));
        assertFalse(p.update(DustStyle.BLOOM_130.timeline().holdEnd(),new Quaternionf()));
    }
}
