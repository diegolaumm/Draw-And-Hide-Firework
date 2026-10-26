package com.drawfirework.firework;

import com.drawfirework.client.CanvasOptions;
import com.drawfirework.client.DustPlane;
import com.drawfirework.client.DustStyle;
import com.drawfirework.client.FireworkTimeline;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.junit.Test;
import static org.junit.Assert.*;

/** Behavioral checks against the state and coordinate implementation used by HeartPrototype. */
public class CameraOrientationStabilityTest {
    private static Quaternionf camera(float yaw, float pitch) {
        // Input fixture only: no test-side world-position implementation.
        return new Quaternionf().rotationYXZ((float)Math.PI - (float)Math.toRadians(yaw),
                -(float)Math.toRadians(pitch), 0);
    }
    private static final FireworkTimeline TIME = DustStyle.BLOOM_130.timeline();
    private DustPlane start(CanvasOptions options) {
        return DustPlane.start(options, new Vector3d(10, 64, 20), new Vector3d(0, 0, 1), TIME);
    }
    private static void same(Vector3d expected, Vector3d actual) {
        assertEquals(expected.x, actual.x, 1e-6);
        assertEquals(expected.y, actual.y, 1e-6);
        assertEquals(expected.z, actual.z, 1e-6);
    }

    @Test public void canvasDefaultFactoryActuallyLocksOrientation() {
        DustPlane plane = start(CanvasOptions.DEFAULT);
        assertFalse(plane.update(19, camera(90, 40)));
        assertTrue(plane.update(20, camera(0, 0)));
        same(new Vector3d(9, 66, 32), plane.position(1, 2, 0, 1));
        Vector3d initial = plane.position(1, 2, .04f, 1);
        for (int tick = 21; tick < TIME.holdEnd(); tick++) {
            assertTrue(plane.update(tick, camera(tick * 13, tick - 40)));
            same(initial, plane.position(1, 2, .04f, 1));
        }
    }

    @Test public void fixedCapturesExplosionCameraRatherThanLaunchCamera() {
        DustPlane plane = start(CanvasOptions.DEFAULT);
        plane.update(1, camera(0, 0));
        plane.update(20, camera(90, 30));
        Vector3d initial = plane.position(2, 1, 0, 1);
        assertTrue(initial.distance(new Vector3d(8, 65, 32)) > .1);
        plane.update(28, camera(-90, -60));
        same(initial, plane.position(2, 1, 0, 1));
    }

    @Test public void playerMovementCannotChangeCapturedWorldAnchor() {
        Vector3d eye = new Vector3d(10, 64, 20);
        Vector3d look = new Vector3d(0, 0, 1);
        DustPlane plane = DustPlane.start(CanvasOptions.DEFAULT, eye, look, TIME);
        plane.update(20, camera(30, 10));
        Vector3d initial = plane.position(2, 1, 0, 1);
        // Actual factory inputs are mutable: moving them must not alias the captured anchor.
        eye.set(100, 70, -50);
        look.set(1, 0, 0);
        plane.update(30, camera(-80, 70));
        same(new Vector3d(10, 64, 32), plane.position(0, 0, 0, 1));
        same(initial, plane.position(2, 1, 0, 1));
    }

    @Test public void explicitDynamicAndOriginalHeartUpdateAndReturnWithoutDrift() {
        for (CanvasOptions options : new CanvasOptions[]{null, new CanvasOptions(100, 266, false, false)}) {
            DustPlane plane = start(options);
            plane.update(20, camera(0, 0));
            Vector3d initial = plane.position(2, 1, 0, 1);
            plane.update(28, camera(45, 25));
            assertTrue(initial.distance(plane.position(2, 1, 0, 1)) > .1);
            plane.update(30, camera(0, 0));
            same(initial, plane.position(2, 1, 0, 1));
            same(new Vector3d(10, 64, 32), plane.position(0, 0, 0, 1));
        }
    }

    @Test public void formationUsesProductionCoordinatesAndSpread() {
        DustPlane plane = start(CanvasOptions.DEFAULT);
        plane.update(20, camera(0, 0));
        same(new Vector3d(10, 64, 32), plane.position(2, 1, 0, FireworkTimeline.spread(20)));
        plane.update(24, camera(90, 50));
        same(new Vector3d(9, 64.5, 32), plane.position(2, 1, 0, FireworkTimeline.spread(24)));
        plane.update(28, camera(180, -45));
        same(new Vector3d(8, 65, 32), plane.position(2, 1, 0, FireworkTimeline.spread(28)));
    }

    @Test public void dispersionAndFadeRejectPositionLocksAndRetainReleaseOrientation() {
        for (CanvasOptions options : new CanvasOptions[]{CanvasOptions.DEFAULT, null}) {
            DustPlane plane = start(options);
            plane.update(20, camera(30, 10));
            plane.update(TIME.holdEnd() - 1, camera(45, 20));
            var release = plane.rotate(.04f, .02f, .001f);
            for (int tick = TIME.holdEnd(); tick < TIME.end(); tick++) {
                assertFalse(plane.update(tick, camera(-tick * 7, 60)));
                assertEquals(release, plane.rotate(.04f, .02f, .001f));
                assertThrows(IllegalStateException.class, () -> plane.position(1, 1, 0, 1));
            }
        }
    }
}
