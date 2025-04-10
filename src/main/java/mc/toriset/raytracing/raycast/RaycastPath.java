package mc.toriset.raytracing.raycast;

import mc.toriset.raytracing.math.Vector3D;
import mc.toriset.raytracing.util.RaycastUtil;
import mc.toriset.raytracing.world.World;
import mc.toriset.raytracing.world.WorldObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class RaycastPath {
    private static final int MAX_BOUNCES = 5;
    private static final double AIR_REFRACTIVE_INDEX = 1.0;
    private static final double RAY_BIAS = 0.001;
    private static final Random random = new Random(42);

    public final ArrayList<RaycastResult> hits;

    public RaycastPath(ArrayList<RaycastResult> hits) {
        this.hits = hits;
    }

    public static RaycastPath start(Vector3D trueOrigin, Vector3D trueDirection) {
        ArrayList<RaycastResult> results = new ArrayList<>();
        Set<WorldObject> recentHits = new HashSet<>();
        traceRay(trueOrigin, trueDirection, 1.0f, AIR_REFRACTIVE_INDEX, 0, results, recentHits);
        return new RaycastPath(results);
    }

    private static void traceRay(Vector3D origin, Vector3D direction, float energy,
                                 double currentRI, int depth, ArrayList<RaycastResult> results,
                                 Set<WorldObject> recentHits) {
        if (depth >= MAX_BOUNCES || energy < 0.01f) {
            return;
        }

        RaycastResult closestResult = null;
        double closestDistance = Double.MAX_VALUE;

        for (WorldObject obj : World.objects) {
            if (recentHits.contains(obj)) continue;

            RaycastResult result = RaycastUtil.rayIntersectsAABBWithDistance(origin, direction, obj);
            if (result == null) continue;

            if (result.getDistance() < closestDistance && result.getDistance() > RAY_BIAS) {
                closestDistance = result.getDistance();
                closestResult = result;
            }
        }

        if (closestResult == null) {
            return;
        }

        results.add(closestResult);

        WorldObject hitObject = closestResult.getObject();
        Vector3D hitPoint = closestResult.getHitPoint();
        Vector3D normal = closestResult.getNormal();

        Set<WorldObject> nextHits = new HashSet<>(recentHits);
        nextHits.add(hitObject);

        float reflectivity = Math.min(hitObject.reflectivity, 1.0f);
        float transparency = Math.min(hitObject.transparency, 1.0f - reflectivity);
        float roughness = hitObject.roughness;

        boolean entering = direction.dot(normal) < 0;

        if (!entering) {
            normal = normal.multiply(-1);
            nextHits.remove(hitObject);
        }

        float reflectedEnergy = energy * reflectivity;
        float refractedEnergy = energy * transparency;

        if (reflectedEnergy > 0.01f) {

            Vector3D perfectReflection = calculateReflection(direction, normal);

            Vector3D roughReflection = applyRoughness(perfectReflection, normal, roughness);

            Vector3D reflectionOrigin = hitPoint.add(roughReflection.multiply(RAY_BIAS));

            traceRay(reflectionOrigin, roughReflection, reflectedEnergy, currentRI, depth + 1, results, nextHits);
        }

        if (refractedEnergy > 0.01f) {
            double n1 = entering ? currentRI : hitObject.refractiveIndex;
            double n2 = entering ? hitObject.refractiveIndex : AIR_REFRACTIVE_INDEX;

            double cosI = Math.abs(normal.dot(direction));
            double sinT2 = (n1 / n2) * (n1 / n2) * (1.0 - cosI * cosI);

            if (sinT2 < 1.0) {

                Vector3D perfectRefraction = calculateRefraction(direction, normal, n1, n2);

                Vector3D roughRefraction = applyRoughness(perfectRefraction, normal, roughness);

                Vector3D refractionOrigin = hitPoint.add(roughRefraction.multiply(RAY_BIAS));

                double nextRI = entering ? hitObject.refractiveIndex : AIR_REFRACTIVE_INDEX;

                traceRay(refractionOrigin, roughRefraction, refractedEnergy, nextRI, depth + 1, results, nextHits);
            } else {

                Vector3D perfectReflection = calculateReflection(direction, normal);
                Vector3D roughReflection = applyRoughness(perfectReflection, normal, roughness);
                Vector3D reflectionOrigin = hitPoint.add(roughReflection.multiply(RAY_BIAS));

                traceRay(reflectionOrigin, roughReflection, refractedEnergy, currentRI, depth + 1, results, nextHits);
            }
        }
    }

    private static Vector3D applyRoughness(Vector3D direction, Vector3D normal, float roughness) {
        if (roughness <= 0.001f) {
            return direction;
        }

        Vector3D tangent = createTangent(normal);
        Vector3D bitangent = normal.cross(tangent);

        double perturbScale = roughness * Math.PI * 0.5;

        double theta = random.nextDouble() * 2.0 * Math.PI;
        double phi = random.nextDouble() * perturbScale;

        double sinPhi = Math.sin(phi);
        double x = Math.cos(theta) * sinPhi;
        double y = Math.sin(theta) * sinPhi;
        double z = Math.cos(phi);

        Vector3D worldPerturbation = tangent.multiply(x).add(bitangent.multiply(y)).add(normal.multiply(z));

        Vector3D mixedDirection = direction.multiply(1 - roughness).add(worldPerturbation.multiply(roughness));

        return mixedDirection.normalize();
    }

    private static Vector3D createTangent(Vector3D normal) {

        if (Math.abs(normal.getX()) < Math.abs(normal.getY()) && Math.abs(normal.getX()) < Math.abs(normal.getZ())) {
            return new Vector3D(0, normal.getZ(), -normal.getY()).normalize();
        } else if (Math.abs(normal.getY()) < Math.abs(normal.getZ())) {
            return new Vector3D(normal.getZ(), 0, -normal.getX()).normalize();
        } else {
            return new Vector3D(normal.getY(), -normal.getX(), 0).normalize();
        }
    }

    private static Vector3D calculateReflection(Vector3D incident, Vector3D normal) {
        double dot = incident.dot(normal);
        return incident.subtract(normal.multiply(2 * dot)).normalize();
    }

    private static Vector3D calculateRefraction(Vector3D incident, Vector3D normal, double n1, double n2) {
        incident = incident.normalize();
        double ratio = n1 / n2;
        double cosI = -normal.dot(incident);
        double sinT2 = ratio * ratio * (1.0 - cosI * cosI);

        if (sinT2 >= 1.0) {

            return calculateReflection(incident, normal);
        }

        double cosT = Math.sqrt(1.0 - sinT2);
        return incident.multiply(ratio).add(normal.multiply(ratio * cosI - cosT)).normalize();
    }

    public Color calculateColor() {
        if (hits.isEmpty()) {
            return Color.BLACK;
        }

        float rFinal = 0;
        float gFinal = 0;
        float bFinal = 0;

        float energyRemaining = 1.0f;
        float totalEnergyAccounted = 0.0f;

        for (int i = 0; i < hits.size(); i++) {
            WorldObject obj = hits.get(i).getObject();

            float reflectivity = Math.min(obj.reflectivity, 1.0f);
            float transparency = Math.min(obj.transparency, 1.0f - reflectivity);

            float absorption = 1.0f - reflectivity - transparency;

            if (i == hits.size() - 1) {
                absorption = 1.0f;
            }

            float energyAbsorbed = energyRemaining * absorption;
            totalEnergyAccounted += energyAbsorbed;

            rFinal += obj.baseColor.getRed() * energyAbsorbed;
            gFinal += obj.baseColor.getGreen() * energyAbsorbed;
            bFinal += obj.baseColor.getBlue() * energyAbsorbed;

            energyRemaining *= (1.0f - absorption);

            if (energyRemaining < 0.001f) {
                break;
            }
        }

        if (totalEnergyAccounted < 0.999f) {
            float remainingEnergy = 1.0f - totalEnergyAccounted;
            rFinal += 0 * remainingEnergy;
            gFinal += 0 * remainingEnergy;
            bFinal += 0 * remainingEnergy;
        }

        return new Color(
                Math.min(255, Math.max(0, Math.round(rFinal))),
                Math.min(255, Math.max(0, Math.round(gFinal))),
                Math.min(255, Math.max(0, Math.round(bFinal)))
        );
    }
}