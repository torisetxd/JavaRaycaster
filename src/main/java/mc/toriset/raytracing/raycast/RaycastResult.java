package mc.toriset.raytracing.raycast;

import mc.toriset.raytracing.math.Vector3D;
import mc.toriset.raytracing.world.WorldObject;

public class RaycastResult {
    private final Vector3D hitPoint;
    private final Vector3D normal;
    private final double distance;
    private final WorldObject object;

    public RaycastResult(Vector3D hitPoint, Vector3D normal, WorldObject object, double distance) {
        this.hitPoint = hitPoint;
        this.normal = normal;
        this.distance = distance;
        this.object = object;
    }

    public Vector3D getHitPoint() {
        return hitPoint;
    }

    public Vector3D getNormal() {
        return normal;
    }

    public double getDistance() {
        return distance;
    }

    public WorldObject getObject() {
        return object;
    }
}