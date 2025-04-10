package mc.toriset.raytracing.util;

import mc.toriset.raytracing.math.Vector3D;
import mc.toriset.raytracing.raycast.RaycastResult;
import mc.toriset.raytracing.world.WorldObject;

public class RaycastUtil {
    private static final double EPSILON = 0.0001;

    public static Vector3D getPointOnRay(Vector3D origin, Vector3D direction, double distance) {
        return origin.add(direction.multiply(distance));
    }

    public static RaycastResult rayIntersectsAABBWithDistance(Vector3D origin, Vector3D dir, WorldObject object) {
        Vector3D min = object.boundingBox.getMin();
        Vector3D max = object.boundingBox.getMax();
        double tMin = Double.NEGATIVE_INFINITY;
        double tMax = Double.POSITIVE_INFINITY;

        int hitFace = -1;

        for (int i = 0; i < 3; i++) {
            double d = 0;
            double minVal = 0;
            double maxVal = 0;

            switch (i) {
                case 0:
                    d = dir.getX();
                    minVal = min.getX();
                    maxVal = max.getX();
                    break;
                case 1:
                    d = dir.getY();
                    minVal = min.getY();
                    maxVal = max.getY();
                    break;
                case 2:
                    d = dir.getZ();
                    minVal = min.getZ();
                    maxVal = max.getZ();
                    break;
            }

            if (Math.abs(d) < EPSILON) {

                if (getOriginComponent(origin, i) < minVal || getOriginComponent(origin, i) > maxVal) {
                    return null;
                }
                continue;
            }

            double invD = 1.0 / d;
            double t0 = (minVal - getOriginComponent(origin, i)) * invD;
            double t1 = (maxVal - getOriginComponent(origin, i)) * invD;

            if (t0 > t1) {
                double temp = t0;
                t0 = t1;
                t1 = temp;
            }

            if (t0 > tMin) {
                tMin = t0;

                hitFace = 2 * i + (d < 0 ? 1 : 0);
            }

            tMax = Math.min(tMax, t1);

            if (tMax < tMin - EPSILON) {
                return null;
            }
        }

        double hitDistance = tMin > EPSILON ? tMin : tMax;

        if (hitDistance < EPSILON) {
            return null;
        }

        Vector3D hitPoint = getPointOnRay(origin, dir, hitDistance);

        Vector3D normal;
        switch (hitFace) {
            case 0: normal = new Vector3D(-1, 0, 0); break;
            case 1: normal = new Vector3D(1, 0, 0); break;
            case 2: normal = new Vector3D(0, -1, 0); break;
            case 3: normal = new Vector3D(0, 1, 0); break;
            case 4: normal = new Vector3D(0, 0, -1); break;
            case 5: normal = new Vector3D(0, 0, 1); break;
            default:

                normal = calculateNormalFromPoint(hitPoint, min, max);
                break;
        }

        return new RaycastResult(hitPoint, normal, object, hitDistance);
    }

    private static double getOriginComponent(Vector3D origin, int component) {
        switch (component) {
            case 0: return origin.getX();
            case 1: return origin.getY();
            case 2: return origin.getZ();
            default: return 0;
        }
    }

    private static Vector3D calculateNormalFromPoint(Vector3D hitPoint, Vector3D min, Vector3D max) {

        double distToXMin = Math.abs(hitPoint.getX() - min.getX());
        double distToXMax = Math.abs(hitPoint.getX() - max.getX());
        double distToYMin = Math.abs(hitPoint.getY() - min.getY());
        double distToYMax = Math.abs(hitPoint.getY() - max.getY());
        double distToZMin = Math.abs(hitPoint.getZ() - min.getZ());
        double distToZMax = Math.abs(hitPoint.getZ() - max.getZ());

        double minDist = Double.MAX_VALUE;
        Vector3D normal = new Vector3D(0, 0, 0);

        if (distToXMin < minDist) { minDist = distToXMin; normal = new Vector3D(-1, 0, 0); }
        if (distToXMax < minDist) { minDist = distToXMax; normal = new Vector3D(1, 0, 0); }
        if (distToYMin < minDist) { minDist = distToYMin; normal = new Vector3D(0, -1, 0); }
        if (distToYMax < minDist) { minDist = distToYMax; normal = new Vector3D(0, 1, 0); }
        if (distToZMin < minDist) { minDist = distToZMin; normal = new Vector3D(0, 0, -1); }
        if (distToZMax < minDist) { normal = new Vector3D(0, 0, 1); }

        return normal;
    }
}