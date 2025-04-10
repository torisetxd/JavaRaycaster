package mc.toriset.raytracing;

import mc.toriset.raytracing.data.Config;
import mc.toriset.raytracing.render.Canvas;
import mc.toriset.raytracing.render.ProgressiveRenderer;
import mc.toriset.raytracing.world.World;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Main {
    private static boolean movementOccurred = false;
    private static long lastFrameTime = 0;
    private static int frameCount = 0;
    private static int fps = 0;
    private static long lastFpsUpdateTime = 0;

    public static void main(String[] args) {

        int virtualWidth = Config.VIRTUAL_WIDTH;
        int virtualHeight = Config.VIRTUAL_HEIGHT;
        int windowWidth = Config.WINDOW_WIDTH;
        int windowHeight = Config.WINDOW_HEIGHT;

        double virtualAspect = (double)virtualWidth / virtualHeight;
        double windowAspect = (double)windowWidth / windowHeight;

        if (Math.abs(virtualAspect - windowAspect) > 0.01) {
            System.out.println("Warning: Virtual and window aspect ratios don't match.");
            System.out.println("Virtual: " + virtualWidth + "x" + virtualHeight + " (" + virtualAspect + ")");
            System.out.println("Window: " + windowWidth + "x" + windowHeight + " (" + windowAspect + ")");

            virtualHeight = (int)(virtualWidth / windowAspect);
            System.out.println("Adjusted virtual size to: " + virtualWidth + "x" + virtualHeight);
        }

        Canvas canvas = new Canvas(
                virtualWidth,
                virtualHeight,
                windowWidth,
                windowHeight
        );

        ProgressiveRenderer.init(virtualWidth, virtualHeight);

        canvas.registerKeyHandler(KeyEvent.VK_R, () -> {
            canvas.clear(Color.BLACK);
            ProgressiveRenderer.reset();
            notifyMovement();
        });

        canvas.registerKeyHandler(KeyEvent.VK_1, () -> {
            ProgressiveRenderer.setMode(ProgressiveRenderer.Mode.FAST);
            System.out.println("Rendering Mode: FAST");
        });

        canvas.registerKeyHandler(KeyEvent.VK_2, () -> {
            ProgressiveRenderer.setMode(ProgressiveRenderer.Mode.PROGRESSIVE);
            System.out.println("Rendering Mode: PROGRESSIVE");
        });

        float movementSpeed = 0.3f / 10f;
        canvas.registerKeyHandler(KeyEvent.VK_W, () -> {
            World.camera.location.z += movementSpeed;
            notifyMovement();
        });
        canvas.registerKeyHandler(KeyEvent.VK_S, () -> {
            World.camera.location.z -= movementSpeed;
            notifyMovement();
        });
        canvas.registerKeyHandler(KeyEvent.VK_A, () -> {
            World.camera.location.x -= movementSpeed;
            notifyMovement();
        });
        canvas.registerKeyHandler(KeyEvent.VK_D, () -> {
            World.camera.location.x += movementSpeed;
            notifyMovement();
        });
        canvas.registerKeyHandler(KeyEvent.VK_SPACE, () -> {
            World.camera.location.y += movementSpeed;
            notifyMovement();
        });
        canvas.registerKeyHandler(KeyEvent.VK_SHIFT, () -> {
            World.camera.location.y -= movementSpeed;
            notifyMovement();
        });

        canvas.setMouseMoveHandler(delta -> {
            float sensitivity = 0.1f;
            World.camera.yaw -= delta.x * sensitivity;

            notifyMovement();
        });

        canvas.registerKeyHandler(KeyEvent.VK_K, canvas::toggleMouseLock);

        canvas.setUpdateHandler(c -> {

            long currentTime = System.currentTimeMillis();
            frameCount++;

            if (currentTime - lastFpsUpdateTime > 1000) {
                fps = frameCount;
                frameCount = 0;
                lastFpsUpdateTime = currentTime;
            }

            boolean shouldRender = false;

            if (movementOccurred) {

                shouldRender = true;
                movementOccurred = false;
            } else {

                long timeSinceLastFrame = currentTime - lastFrameTime;
                if (timeSinceLastFrame > 50) {
                    shouldRender = true;
                }
            }

            if (shouldRender) {
                lastFrameTime = currentTime;

                ProgressiveRenderer.render(canvas);
            }
        });

        canvas.start();

        canvas.clear(Color.BLACK);
        ProgressiveRenderer.reset();
        ProgressiveRenderer.render(canvas);

        Runtime.getRuntime().addShutdownHook(new Thread(ProgressiveRenderer::shutdown));
    }

    private static void notifyMovement() {
        movementOccurred = true;
        ProgressiveRenderer.notifyMovement();
    }
}