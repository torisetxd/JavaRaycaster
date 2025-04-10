package mc.toriset.raytracing.render;

import mc.toriset.raytracing.data.Multisampling;
import mc.toriset.raytracing.raycast.RaycastPath;
import mc.toriset.raytracing.math.Vector3D;
import mc.toriset.raytracing.world.World;

import java.awt.*;

public class LegacyRenderer {

    private static final int SAMPLES_PER_PIXEL = 3;

    public static Vector3D rotateByYawPitch(Vector3D v, double yaw, double pitch) {
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);

        double cosYaw = Math.cos(yawRad);
        double sinYaw = Math.sin(yawRad);
        double rx = v.getX() * cosYaw - v.getZ() * sinYaw;
        double rz = v.getX() * sinYaw + v.getZ() * cosYaw;
        Vector3D yawed = new Vector3D(rx, v.getY(), rz);

        double cosPitch = Math.cos(pitchRad);
        double sinPitch = Math.sin(pitchRad);
        double ry = yawed.getY() * cosPitch - yawed.getZ() * sinPitch;
        double rz2 = yawed.getY() * sinPitch + yawed.getZ() * cosPitch;

        return new Vector3D(yawed.getX(), ry, rz2);
    }

    public static void render(Canvas canvas) {
        double width = canvas.getVirtualWidth();
        double height = canvas.getVirtualHeight();

        double aspect = width / height;
        double horizontalHalfTan = Math.tan(Math.toRadians(World.camera.fov / 2.0));
        double verticalHalfTan = horizontalHalfTan / aspect;

        Multisampling.Pattern pattern = Multisampling.currentPattern;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                int r = 0, g = 0, b = 0;

                for (int s = 0; s < SAMPLES_PER_PIXEL; s++) {

                    Multisampling.Point2D offset = Multisampling.getSampleOffset(s, SAMPLES_PER_PIXEL);

                    double ndcX = (2.0 * (x + offset.x) / width) - 1.0;
                    double ndcY = 1.0 - (2.0 * (y + offset.y) / height);

                    double px = ndcX * horizontalHalfTan;
                    double py = ndcY * verticalHalfTan;
                    double pz = 1.0;

                    Vector3D dirCam = new Vector3D(px, py, pz).normalize();
                    Vector3D dirWorld = rotateByYawPitch(dirCam, World.camera.yaw, World.camera.pitch);

                    RaycastPath raycastPath = RaycastPath.start(World.camera.location, dirWorld);
                    Color sampleColor;

                    if (raycastPath.hits.isEmpty()) {
                        sampleColor = Color.BLACK;
                    } else {
                        sampleColor = raycastPath.calculateColor();
                    }

                    r += sampleColor.getRed();
                    g += sampleColor.getGreen();
                    b += sampleColor.getBlue();
                }

                r /= SAMPLES_PER_PIXEL;
                g /= SAMPLES_PER_PIXEL;
                b /= SAMPLES_PER_PIXEL;

                canvas.setPixel(x, y, new Color(r, g, b));
            }
        }
    }

    public static void configureSamplingQuality(int fps) {

        if (fps > 60) {
            Multisampling.currentPattern = Multisampling.Pattern.JITTERED;

        } else if (fps > 30) {
            Multisampling.currentPattern = Multisampling.Pattern.REGULAR_GRID;
        } else {
            Multisampling.currentPattern = Multisampling.Pattern.REGULAR_GRID;

        }
    }
}