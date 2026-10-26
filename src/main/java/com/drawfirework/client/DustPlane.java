package com.drawfirework.client;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;

/** Small runtime state: world anchor, orientation capture and formation/hold positioning gate. */
public final class DustPlane {
    private final Vector3d center;
    private final boolean fixed;
    private final FireworkTimeline timeline;
    private final Quaternionf rotation = new Quaternionf();
    private boolean initialized;
    private boolean positioning;

    private DustPlane(boolean fixed, Vector3dc eye, Vector3dc look, FireworkTimeline timeline) {
        this.fixed = fixed;
        this.center = new Vector3d(look).mul(12).add(eye);
        this.timeline = timeline;
    }

    /** Null options is the original heart path; canvas uses its actual command options. */
    public static DustPlane start(CanvasOptions options, Vector3dc eye, Vector3dc look,
                                  FireworkTimeline timeline) {
        return new DustPlane(options != null && options.fixed(), eye, look, timeline);
    }

    public static DustPlane atCenter(CanvasOptions options, Vector3dc center, FireworkTimeline timeline) {
        return new DustPlane(options.fixed(), center, new Vector3d(), timeline);
    }

    /** Returns whether this tick may lock particle positions. D/E never reposition. */
    public boolean update(int age, Quaternionfc camera) {
        positioning = age >= FireworkTimeline.LAUNCH_END && age < timeline.holdEnd();
        if (positioning && (!initialized || !fixed)) {
            rotation.set(camera);
            initialized = true;
        }
        return positioning;
    }

    public Vector3d position(float x, float y, float z, float spread) {
        if (!positioning) throw new IllegalStateException("Outside formation/hold");
        return new Vector3d(center).add(new Vector3d(rotate(x * spread, y * spread, z * spread)));
    }

    public Vector3d center() {
        return new Vector3d(center);
    }

    public Vector3f rotate(float x, float y, float z) {
        return new Vector3f(x, y, z).rotate(rotation);
    }
}
