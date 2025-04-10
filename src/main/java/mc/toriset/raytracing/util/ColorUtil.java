package mc.toriset.raytracing.util;

import mc.toriset.raytracing.render.Canvas;

import java.awt.Color;
import java.util.Random;

public class ColorUtil {
    private static final Random random = new Random();
    
    public static Color generateRandomColor() {
        return new Color(
            random.nextInt(206) + 50,
            random.nextInt(206) + 50,
            random.nextInt(206) + 50
        );
    }
    
    public static void drawRandomPixels(Canvas canvas, int count) {
        canvas.clear();
        for (int i = 0; i < count; i++) {
            int x = random.nextInt(canvas.getVirtualWidth());
            int y = random.nextInt(canvas.getVirtualHeight());
            Color color = generateRandomColor();
            canvas.setPixel(x, y, color);
        }
    }
}