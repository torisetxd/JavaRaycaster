package mc.toriset.raytracing.render;

import mc.toriset.raytracing.raycast.RaycastPath;
import mc.toriset.raytracing.math.Vector3D;
import mc.toriset.raytracing.world.World;

import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;

public class ProgressiveRenderer {

    private static final int MIN_SAMPLES_PER_FRAME = 1;
    private static final int MAX_TOTAL_SAMPLES = 256;

    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    private static ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);

    private static int currentIteration = 0;
    private static int[][] sampleCounts;
    private static Color[][] accumulatedColors;
    private static boolean isFirstFrame = true;
    private static boolean isRendering = false;
    private static long lastMoveTime = 0;
    private static int stabilityDelay = 500;
    private static AtomicInteger completedTiles = new AtomicInteger(0);
    private static int totalTilesToRender = 0;

    private static int currentSampleCount = 0;
    private static int totalSamples = 0;
    private static int totalPixels = 0;
    private static long renderStartTime = 0;
    private static String renderStatusMessage = "Initializing...";

    public enum Mode {
        FAST,
        PROGRESSIVE,
        HIGH_QUALITY
    }

    private static Mode currentMode = Mode.PROGRESSIVE;

    public static void init(int width, int height) {
        sampleCounts = new int[width][height];
        accumulatedColors = new Color[width][height];
        totalPixels = width * height;
        reset();
    }

    public static void reset() {
        currentIteration = 0;
        isFirstFrame = true;
        totalSamples = 0;
        currentSampleCount = 0;

        if (sampleCounts != null) {
            for (int x = 0; x < sampleCounts.length; x++) {
                for (int y = 0; y < sampleCounts[0].length; y++) {
                    sampleCounts[x][y] = 0;
                    accumulatedColors[x][y] = null;
                }
            }
        }

        renderStatusMessage = "Ready";
    }

    public static void notifyMovement() {
        lastMoveTime = System.currentTimeMillis();
        if (currentMode == Mode.PROGRESSIVE) {

            reset();
        }
    }

    public static void setMode(Mode mode) {
        currentMode = mode;
        reset();
    }

    public static RenderStats getRenderStats() {
        RenderStats stats = new RenderStats();
        stats.renderMode = currentMode;
        stats.averageSamples = currentSampleCount;
        stats.percentComplete = totalTilesToRender > 0 ?
                (completedTiles.get() * 100 / totalTilesToRender) : 0;
        stats.elapsedTimeMs = System.currentTimeMillis() - renderStartTime;
        stats.statusMessage = renderStatusMessage;
        stats.isRendering = isRendering;
        return stats;
    }

    public static void setStabilityDelay(int milliseconds) {
        stabilityDelay = milliseconds;
    }

    private static boolean isSceneStable() {
        return System.currentTimeMillis() - lastMoveTime > stabilityDelay;
    }

    public static void render(Canvas canvas) {
        if (isRendering) {
            return;
        }

        int width = canvas.getVirtualWidth();
        int height = canvas.getVirtualHeight();

        if (sampleCounts == null || sampleCounts.length != width || sampleCounts[0].length != height) {
            init(width, height);
        }

        int samplesThisFrame;

        if (currentMode == Mode.FAST || !isSceneStable()) {
            samplesThisFrame = MIN_SAMPLES_PER_FRAME;
            if (!isFirstFrame && isSceneStable() && currentMode == Mode.PROGRESSIVE) {

                renderStatusMessage = "Fast mode (moving)";
            } else {

                reset();
                renderStatusMessage = "Fast mode";
            }
        } else if (currentMode == Mode.HIGH_QUALITY) {
            samplesThisFrame = MAX_TOTAL_SAMPLES;
            if (!isFirstFrame) {
                renderStatusMessage = "High quality render complete";
                return;
            }
            renderStatusMessage = "Rendering in high quality...";
        } else {

            if (currentIteration == 0) {
                samplesThisFrame = 1;
                renderStatusMessage = "Progressive: Initial pass";
            } else if (currentIteration < 5) {
                samplesThisFrame = 1;
                renderStatusMessage = "Progressive: Refinement pass " + currentIteration;
            } else if (currentIteration < 10) {
                samplesThisFrame = 2;
                renderStatusMessage = "Progressive: Refinement pass " + currentIteration;
            } else {
                samplesThisFrame = 4;
                renderStatusMessage = "Progressive: Deep refinement";
            }
        }

        boolean isMaxSamplesReached = true;
        for (int x = 0; x < width; x += 10) {
            for (int y = 0; y < height; y += 10) {
                if (sampleCounts[x][y] < MAX_TOTAL_SAMPLES) {
                    isMaxSamplesReached = false;
                    break;
                }
            }
            if (!isMaxSamplesReached) break;
        }

        if (isMaxSamplesReached && !isFirstFrame) {
            renderStatusMessage = "Maximum quality reached";
            return;
        }

        isRendering = true;
        renderStartTime = System.currentTimeMillis();

        double aspect = (double)width / height;
        double horizontalHalfTan = Math.tan(Math.toRadians(World.camera.fov / 2.0));
        double verticalHalfTan = horizontalHalfTan / aspect;

        int tileSize = 32;
        int tilesX = (width + tileSize - 1) / tileSize;
        int tilesY = (height + tileSize - 1) / tileSize;
        totalTilesToRender = tilesX * tilesY;

        completedTiles.set(0);
        CountDownLatch latch = new CountDownLatch(totalTilesToRender);

        for (int tileY = 0; tileY < tilesY; tileY++) {
            for (int tileX = 0; tileX < tilesX; tileX++) {
                final int tx = tileX;
                final int ty = tileY;

                threadPool.submit(() -> {
                    try {
                        renderTile(canvas, tx, ty, tileSize, width, height,
                                horizontalHalfTan, verticalHalfTan, samplesThisFrame);

                        int completed = completedTiles.incrementAndGet();
                        if (completed % 10 == 0 || completed == totalTilesToRender) {
                            int percent = completed * 100 / totalTilesToRender;
                            renderStatusMessage = String.format("%s: %d%% complete",
                                    getModeString(), percent);
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        currentIteration++;
        isFirstFrame = false;
        isRendering = false;

        int totalSampleCount = 0;
        for (int x = 0; x < width; x += 10) {
            for (int y = 0; y < height; y += 10) {
                totalSampleCount += sampleCounts[x][y];
            }
        }
        currentSampleCount = totalSampleCount / ((width / 10) * (height / 10));

        long renderTime = System.currentTimeMillis() - renderStartTime;
        renderStatusMessage = String.format("%s: %d samples, rendered in %.1fs",
                getModeString(), currentSampleCount, renderTime/1000.0);
    }

    private static String getModeString() {
        switch (currentMode) {
            case FAST: return "Fast mode";
            case HIGH_QUALITY: return "High quality";
            case PROGRESSIVE: return "Progressive";
            default: return "Unknown mode";
        }
    }

    private static void renderTile(Canvas canvas, int tileX, int tileY, int tileSize,
                                   int width, int height, double horizontalHalfTan,
                                   double verticalHalfTan, int samplesThisFrame) {

        int startX = tileX * tileSize;
        int startY = tileY * tileSize;
        int endX = Math.min(startX + tileSize, width);
        int endY = Math.min(startY + tileSize, height);

        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {

                if (sampleCounts[x][y] >= MAX_TOTAL_SAMPLES) {
                    continue;
                }

                int r = 0, g = 0, b = 0;

                if (accumulatedColors[x][y] != null) {
                    r = accumulatedColors[x][y].getRed() * sampleCounts[x][y];
                    g = accumulatedColors[x][y].getGreen() * sampleCounts[x][y];
                    b = accumulatedColors[x][y].getBlue() * sampleCounts[x][y];
                }

                for (int s = 0; s < samplesThisFrame; s++) {

                    double offsetX = (double)s / samplesThisFrame + Math.random() / samplesThisFrame;
                    double offsetY = (double)(s % 2) / 2 + Math.random() / 2;

                    double sampleX = x + offsetX;
                    double sampleY = y + offsetY;

                    double ndcX = (2.0 * sampleX / width) - 1.0;
                    double ndcY = 1.0 - (2.0 * sampleY / height);

                    double px = ndcX * horizontalHalfTan;
                    double py = ndcY * verticalHalfTan;
                    double pz = 1.0;

                    Vector3D dirCam = new Vector3D(px, py, pz).normalize();
                    Vector3D dirWorld = LegacyRenderer.rotateByYawPitch(dirCam, World.camera.yaw, World.camera.pitch);

                    RaycastPath raycastPath = RaycastPath.start(World.camera.location, dirWorld);
                    Color sampleColor = raycastPath.hits.isEmpty() ? Color.BLACK : raycastPath.calculateColor();

                    r += sampleColor.getRed();
                    g += sampleColor.getGreen();
                    b += sampleColor.getBlue();
                }

                int newSampleCount = sampleCounts[x][y] + samplesThisFrame;
                sampleCounts[x][y] = newSampleCount;
                totalSamples += samplesThisFrame;

                r = r / newSampleCount;
                g = g / newSampleCount;
                b = b / newSampleCount;

                Color newColor = new Color(
                        Math.min(255, Math.max(0, r)),
                        Math.min(255, Math.max(0, g)),
                        Math.min(255, Math.max(0, b))
                );

                accumulatedColors[x][y] = newColor;

                canvas.setPixel(x, y, newColor);
            }
        }
    }

    public static void shutdown() {
        threadPool.shutdown();
    }

    public static class RenderStats {
        public Mode renderMode;
        public int averageSamples;
        public int percentComplete;
        public long elapsedTimeMs;
        public String statusMessage;
        public boolean isRendering;
    }
}