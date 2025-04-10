package mc.toriset.raytracing.world;

import mc.toriset.raytracing.math.AABB;
import mc.toriset.raytracing.math.Vector3D;

import java.awt.*;

public class WorldObject {
    public float reflectivity; // how much light bounces off and is put into final calculation
    public float roughness; // how much scattering there is
    public Color baseColor;
    public AABB boundingBox;

    public WorldObject(float reflectivity, float roughness, Color baseColor, Vector3D origin, double width, double height, double depth) {
        this.reflectivity = reflectivity;
        this.roughness = roughness;
        this.baseColor = baseColor;
        this.boundingBox = new AABB(origin, origin.add(new Vector3D(width, height, depth)));
    }

    public WorldObject(float reflectivity, float roughness, Color baseColor, AABB boundingBox) {
        this.reflectivity = reflectivity;
        this.roughness = roughness;
        this.baseColor = baseColor;
        this.boundingBox = boundingBox;
    }

    public Vector3D getOrigin() {
        return boundingBox.getMin();
    }
}
