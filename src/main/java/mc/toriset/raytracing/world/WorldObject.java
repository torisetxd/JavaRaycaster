package mc.toriset.raytracing.world;

import mc.toriset.raytracing.math.AABB;
import mc.toriset.raytracing.math.Vector3D;

import java.awt.*;

public class WorldObject {
    public float reflectivity; // how much light bounces off and is put into final calculation
    public float transparency; // how much light passes through
    public float roughness; // how much scattering there is
    public float refractiveIndex; // for transparent materials (1.0 = air, 1.33 = water, 1.5 = glass)
    public Color baseColor;
    public AABB boundingBox;

    public WorldObject(float reflectivity, float transparency, float roughness, float refractiveIndex,
                       Color baseColor, Vector3D origin, double width, double height, double depth) {
        this.reflectivity = reflectivity;
        this.transparency = transparency;
        this.roughness = roughness;
        this.refractiveIndex = refractiveIndex;
        this.baseColor = baseColor;
        this.boundingBox = new AABB(origin, origin.add(new Vector3D(width, height, depth)));
    }

    public WorldObject(float reflectivity, float transparency, float roughness, float refractiveIndex,
                       Color baseColor, AABB boundingBox) {
        this.reflectivity = reflectivity;
        this.transparency = transparency;
        this.roughness = roughness;
        this.refractiveIndex = refractiveIndex;
        this.baseColor = baseColor;
        this.boundingBox = boundingBox;
    }

    // Constructor for backward compatibility
    public WorldObject(float reflectivity, float roughness, Color baseColor, AABB boundingBox) {
        this(reflectivity, 0.0f, roughness, 1.0f, baseColor, boundingBox);
    }

    // Constructor for backward compatibility
    public WorldObject(float reflectivity, float roughness, Color baseColor, Vector3D origin,
                       double width, double height, double depth) {
        this(reflectivity, 0.0f, roughness, 1.0f, baseColor, origin, width, height, depth);
    }

    public Vector3D getOrigin() {
        return boundingBox.getMin();
    }
}