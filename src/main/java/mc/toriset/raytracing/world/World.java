package mc.toriset.raytracing.world;

import mc.toriset.raytracing.math.AABB;
import mc.toriset.raytracing.math.Vector3D;
import mc.toriset.raytracing.render.Camera;

import java.awt.*;
import java.util.ArrayList;

public class World {
    public static ArrayList<WorldObject> objects = new ArrayList<>();
    public static final Camera camera = new Camera(120, 135,0, new Vector3D(3.5,1.85,3));


    static {
        objects.add(new WorldObject(0.1f, 0, 0.3f, 1, Color.GRAY, new AABB(-5, 0, -5, 5, 0.01, 5)));
        objects.add(new WorldObject(0.2f, 0.5f, 0.1f, 1.5f, Color.BLUE, new AABB(0, 0.5, 0, 3, 1.5, 0.1)));
        objects.add(new WorldObject(0.4f, 0.3f, 0.3f, 1.5f, Color.BLUE, new AABB(0, 1.5, 0, 2, 2.5, 0.1)));
        objects.add(new WorldObject(0.7f, 0.1f, 0.7f, 1.5f, Color.BLUE, new AABB(0, 2.5, 0, 1, 3.5, 0.1)));
        objects.add(new WorldObject(0.1f, 0, 0.05f, 1, Color.RED, new AABB(-5, 0, 3, 5, 3, 3.1)));
//        objects.add(new WorldObject(0.1f, 0.9f, 0.05f, 1.5f,
//                new Color(220, 240, 255), new AABB(-4, 0.5, 0, -2.5, 2, 1.5)));

//        objects.add(new WorldObject(0.1f, 0.8f, 0.7f, 1.33f,
//                new Color(200, 200, 255), new AABB(-4, 0.5, 1.7, -2.5, 2, 3)));
    }
}
