package mc.toriset.raytracing.raycast;

import mc.toriset.raytracing.math.Vector3D;
import mc.toriset.raytracing.util.RaycastUtil;
import mc.toriset.raytracing.world.World;
import mc.toriset.raytracing.world.WorldObject;

import java.awt.*;
import java.util.ArrayList;

public class RaycastPath {
    private static final int MAX_BOUNCES = 2;
    public final ArrayList<RaycastResult> hits;

    public RaycastPath(ArrayList<RaycastResult> hits) {
        this.hits = hits;
    }


    public static RaycastPath start(Vector3D trueOrigin, Vector3D trueDirection) {
        ArrayList<RaycastResult> results = new ArrayList<>();
        Vector3D origin = trueOrigin;
        Vector3D direction = trueDirection;
        for (int i = 0; i < MAX_BOUNCES; i++) {
            RaycastResult closestResult = null;
            double        closestDistance = Double.MAX_VALUE;

            for (WorldObject obj : World.objects) {
                RaycastResult result = RaycastUtil.rayIntersectsAABBWithDistance(origin, direction, obj);
                if (result == null) continue;

                if (result.getDistance() < closestDistance) {
                    closestDistance = result.getDistance();
                    closestResult   = result;
                }
            }

            if (closestResult != null) {
                origin = closestResult.getHitPoint();
                Vector3D normal = closestResult.getNormal();
                double dotProduct = direction.dot(normal);
                direction = direction.subtract(normal.multiply(2 * dotProduct));
                results.add(closestResult);
            } else {
                break;
            }

            if (closestResult.getObject().reflectivity == 0) break;
        }
        return new RaycastPath(results);
    }

    public Color calculateColor() {
        if (hits.isEmpty()) {
            return Color.BLACK;
        }

        float rAccum = 0;
        float gAccum = 0;
        float bAccum = 0;

        float remainingIntensity = 1.0f;
        float totalWeight = 0;

        for (int i = 0; i < hits.size(); i++) {
            RaycastResult hit = hits.get(i);
            WorldObject obj = hit.getObject();

            float weight;
            if (i == 0) {
                // First hit gets full intensity
                weight = 1.0f - obj.reflectivity;
            } else {
                // Subsequent hits get scaled by previous reflectivity and their own absorption
                RaycastResult prevHit = hits.get(i-1);
                weight = remainingIntensity * (1.0f - obj.reflectivity);
                remainingIntensity *= prevHit.getObject().reflectivity;
            }

            totalWeight += weight;

            // Add weighted color contribution
            rAccum += weight * obj.baseColor.getRed();
            gAccum += weight * obj.baseColor.getGreen();
            bAccum += weight * obj.baseColor.getBlue();

            // If no more light intensity left, break
            if (remainingIntensity < 0.001f) break;
        }

        // If we have accumulated weights, normalize the color
        if (totalWeight > 0) {
            rAccum /= totalWeight;
            gAccum /= totalWeight;
            bAccum /= totalWeight;
        }

        return new Color(
                Math.min(255, Math.max(0, Math.round(rAccum))),
                Math.min(255, Math.max(0, Math.round(gAccum))),
                Math.min(255, Math.max(0, Math.round(bAccum)))
        );
    }
}
