package mc.toriset.raytracing.util;


import mc.toriset.raytracing.math.AABB;
import mc.toriset.raytracing.math.Vector3D;
import mc.toriset.raytracing.raycast.RaycastResult;
import mc.toriset.raytracing.world.WorldObject;

public class RaycastUtil {
    public static Vector3D getDirection(double yaw, double pitch) {
        double pitchRad = Math.toRadians(pitch);
        double yawRad = Math.toRadians(yaw);

        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);

        return new Vector3D(x, y, z);
    }

    public static Vector3D getPointOnRay(Vector3D origin, Vector3D direction, double distance) {
        return origin.add(direction.multiply(distance));
    }


    public static RaycastResult rayIntersectsAABBWithDistance(Vector3D origin, Vector3D dir, WorldObject object) {
        Vector3D min = object.boundingBox.getMin();
        Vector3D max = object.boundingBox.getMax();
        double tMin = -Double.MAX_VALUE;
        double tMax = Double.MAX_VALUE;

        Vector3D safeDir = new Vector3D(
                Math.abs(dir.getX()) < 0.0001 ? 0.0001 : dir.getX(),
                Math.abs(dir.getY()) < 0.0001 ? 0.0001 : dir.getY(),
                Math.abs(dir.getZ()) < 0.0001 ? 0.0001 : dir.getZ()
        );

        double invDirX = 1.0 / safeDir.getX();
        double t0x = (min.getX() - origin.getX()) * invDirX;
        double t1x = (max.getX() - origin.getX()) * invDirX;
        if (t0x > t1x) {
            double temp = t0x;
            t0x = t1x;
            t1x = temp;
        }
        tMin = Math.max(tMin, t0x);
        tMax = Math.min(tMax, t1x);
        if (tMax < tMin) return null;

        double invDirY = 1.0 / safeDir.getY();
        double t0y = (min.getY() - origin.getY()) * invDirY;
        double t1y = (max.getY() - origin.getY()) * invDirY;
        if (t0y > t1y) {
            double temp = t0y;
            t0y = t1y;
            t1y = temp;
        }
        tMin = Math.max(tMin, t0y);
        tMax = Math.min(tMax, t1y);
        if (tMax < tMin) return null;

        double invDirZ = 1.0 / safeDir.getZ();
        double t0z = (min.getZ() - origin.getZ()) * invDirZ;
        double t1z = (max.getZ() - origin.getZ()) * invDirZ;
        if (t0z > t1z) {
            double temp = t0z;
            t0z = t1z;
            t1z = temp;
        }
        tMin = Math.max(tMin, t0z);
        tMax = Math.min(tMax, t1z);
        if (tMax < tMin) return null;

        double hitDistance = tMin > 0 ? tMin : tMax;

        if (hitDistance < 0) return null;

        Vector3D hitPoint = getPointOnRay(origin, dir, hitDistance);
        Vector3D normal = calculateNormal(hitPoint, min, max);

        return new RaycastResult(hitPoint, normal, object, hitDistance);
    }

    private static Vector3D calculateNormal(Vector3D hitPoint, Vector3D min, Vector3D max) {
        double epsilon = 0.0001;

        Vector3D normal = new Vector3D(0, 0, 0);

        if (Math.abs(hitPoint.getX() - min.getX()) < epsilon) {
            normal = new Vector3D(-1, 0, 0);
        } else if (Math.abs(hitPoint.getX() - max.getX()) < epsilon) {
            normal = new Vector3D(1, 0, 0);
        } else if (Math.abs(hitPoint.getY() - min.getY()) < epsilon) {
            normal = new Vector3D(0, -1, 0);
        } else if (Math.abs(hitPoint.getY() - max.getY()) < epsilon) {
            normal = new Vector3D(0, 1, 0);
        } else if (Math.abs(hitPoint.getZ() - min.getZ()) < epsilon) {
            normal = new Vector3D(0, 0, -1);
        } else if (Math.abs(hitPoint.getZ() - max.getZ()) < epsilon) {
            normal = new Vector3D(0, 0, 1);
        }

        return normal;
    }

}