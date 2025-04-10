package mc.toriset.raytracing;

import mc.toriset.raytracing.data.Config;
import mc.toriset.raytracing.render.Canvas;
import mc.toriset.raytracing.render.Renderer;
import mc.toriset.raytracing.world.World;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Main {
    public static void main(String[] args) {
        Canvas canvas = new Canvas(
                Config.VIRTUAL_WIDTH,
                Config.VIRTUAL_HEIGHT,
                Config.WINDOW_WIDTH,
                Config.WINDOW_HEIGHT
        );

        canvas.registerKeyHandler(KeyEvent.VK_R, () -> {
            canvas.clear(Color.BLACK);
            Renderer.render(canvas);
        });

        float movementSpeed = 0.3f / 10f;
        canvas.registerKeyHandler(KeyEvent.VK_W, () -> {
            World.camera.location.z += movementSpeed;
        });
        canvas.registerKeyHandler(KeyEvent.VK_S, () -> {
            World.camera.location.z -= movementSpeed;
        });
        canvas.registerKeyHandler(KeyEvent.VK_A, () -> {
            World.camera.location.x -= movementSpeed;
        });
        canvas.registerKeyHandler(KeyEvent.VK_D, () -> {
            World.camera.location.x += movementSpeed;
        });


        canvas.setMouseMoveHandler(delta -> {
            float sensitivity = 0.1f;
            World.camera.yaw -= delta.x * sensitivity;
        });

        canvas.registerKeyHandler(KeyEvent.VK_K, canvas::toggleMouseLock);

        canvas.start();
    }
}