package mc.toriset.raytracing.render;

import mc.toriset.raytracing.util.RaycastUtil;
import mc.toriset.raytracing.math.Vector3D;

public class Camera {
    public float fov;
    public double yaw, pitch;
    public Vector3D location;

    public Camera(float fov, double yaw, double pitch, Vector3D location) {
        this.fov = fov;
        this.yaw = yaw;
        this.pitch = pitch;
        this.location = location;
    }
}
