package mc.toriset.raytracing.render;

import mc.toriset.raytracing.data.Config;
import mc.toriset.raytracing.raycast.RaycastPath;
import mc.toriset.raytracing.raycast.RaycastResult;
import mc.toriset.raytracing.util.RaycastUtil;
import mc.toriset.raytracing.math.Vector3D;
import mc.toriset.raytracing.world.World;
import mc.toriset.raytracing.world.WorldObject;

import java.awt.*;

public class Renderer {
    public static Vector3D rotateByYawPitch(Vector3D v, double yaw, double pitch) {
        double yawRad   = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);

        double cosYaw = Math.cos(yawRad);
        double sinYaw = Math.sin(yawRad);
        double rx = v.getX() * cosYaw - v.getZ() * sinYaw;
        double rz = v.getX() * sinYaw + v.getZ() * cosYaw;
        Vector3D yawed = new Vector3D(rx, v.getY(), rz);

        double cosPitch = Math.cos(pitchRad);
        double sinPitch = Math.sin(pitchRad);
        double ry = yawed.getY() * cosPitch - yawed.getZ() * sinPitch;
        double rz2= yawed.getY() * sinPitch + yawed.getZ() * cosPitch;

        return new Vector3D(yawed.getX(), ry, rz2);
    }

    public static void render(Canvas canvas) {
        double width  = canvas.getVirtualWidth();
        double height = canvas.getVirtualHeight();

        double aspect = width / height;
        double horizontalHalfTan = Math.tan(Math.toRadians(World.camera.fov / 2.0));
        double verticalHalfTan   = horizontalHalfTan / aspect;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double ndcX = (2.0 * (x + 0.5) / width) - 1.0;
                double ndcY = 1.0 - (2.0 * (y + 0.5) / height);

                double px = ndcX * horizontalHalfTan;
                double py = ndcY * verticalHalfTan;
                double pz = 1.0;

                Vector3D dirCam = new Vector3D(px, py, pz).normalize();

                Vector3D dirWorld = rotateByYawPitch(dirCam,
                        World.camera.yaw, World.camera.pitch);

                RaycastPath raycastPath = RaycastPath.start(World.camera.location, dirWorld);
                if (raycastPath.hits.isEmpty()) {
                    canvas.setPixel(x,y,Color.BLACK);
                } else {
                    canvas.setPixel(x,y, raycastPath.calculateColor());
                }
//                RaycastResult closestResult = null;
//                double        closestDistance = Double.MAX_VALUE;
//
//                for (WorldObject obj : World.objects) {
//                    RaycastResult result = RaycastUtil.rayIntersectsAABBWithDistance(
//                            World.camera.location, dirWorld, obj
//                    );
//                    if (result == null) continue;
//
//                    if (result.getDistance() < closestDistance) {
//                        closestDistance = result.getDistance();
//                        closestResult   = result;
//                    }
//                }
//
//                if (closestResult == null) {
//                    canvas.setPixel(x, y, Color.BLACK);
//                } else {
//                    canvas.setPixel(x, y, closestResult.getObject().baseColor);
//                }
            }
        }
    }
}
