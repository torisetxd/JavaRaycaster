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
        objects.add(new WorldObject(0, 1, Color.GRAY, new AABB(-5, 0, -5, 5, 0, 5)));

        objects.add(new WorldObject(0.25f, 0.03f, Color.BLUE, new AABB(0,0 + 0.5,0,3,1 + 0.5,1)));
        objects.add(new WorldObject(0.5f, 0.03f, Color.BLUE, new AABB(0,1 + 0.5,0,2,2 + 0.5,1)));
        objects.add(new WorldObject(0.75f, 0.03f, Color.BLUE, new AABB(0,2 + 0.5,0,1,3 + 0.5,1)));

//        objects.add(new WorldObject(0, 1, Color.GRAY, new AABB(0,0,0,1,1,1)));
//        objects.add(new WorldObject(0, 1, Color.GRAY, new AABB(0,0,0,1,1,1)));


        objects.add(new WorldObject(0f, 1, Color.RED, new AABB(-5, 0, 4, 5, 3, 5)));
    }
}
