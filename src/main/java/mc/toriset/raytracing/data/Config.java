package mc.toriset.raytracing.data;

import mc.toriset.raytracing.render.Camera;
import mc.toriset.raytracing.math.AABB;
import mc.toriset.raytracing.math.Vector3D;
import mc.toriset.raytracing.world.WorldObject;

import java.awt.*;
import java.util.ArrayList;

public class Config {
    public static final int VIRTUAL_WIDTH = 16 * 50;
    public static final int VIRTUAL_HEIGHT = 9 * 50;
    public static final int WINDOW_WIDTH = 1024;
    public static final int WINDOW_HEIGHT = 576;
    public static final int FPS = 240;
    public static final String WINDOW_TITLE = "Pixel Canvas";
}