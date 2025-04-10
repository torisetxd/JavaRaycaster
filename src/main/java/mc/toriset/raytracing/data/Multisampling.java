package mc.toriset.raytracing.data;

public class Multisampling {

    public enum Pattern {
        RANDOM,
        REGULAR_GRID,
        JITTERED,
        POISSON
    }

    public static Pattern currentPattern = Pattern.REGULAR_GRID;

    public static Point2D getSampleOffset(int sampleIndex, int totalSamples) {
        switch (currentPattern) {
            case RANDOM:
                return new Point2D(Math.random(), Math.random());

            case REGULAR_GRID:

                int gridSize = (int) Math.ceil(Math.sqrt(totalSamples));
                int x = sampleIndex % gridSize;
                int y = sampleIndex / gridSize;
                return new Point2D(
                        (x + 0.5) / gridSize,
                        (y + 0.5) / gridSize
                );

            case JITTERED:

                int jGridSize = (int) Math.ceil(Math.sqrt(totalSamples));
                int jx = sampleIndex % jGridSize;
                int jy = sampleIndex / jGridSize;
                return new Point2D(
                        (jx + Math.random()) / jGridSize,
                        (jy + Math.random()) / jGridSize
                );

            case POISSON:

                return getPoissonSample(sampleIndex, totalSamples);

            default:
                return new Point2D(0.5, 0.5);
        }
    }

    private static Point2D getPoissonSample(int index, int totalSamples) {

        if (totalSamples == 4) {
            switch (index) {
                case 0: return new Point2D(0.3, 0.3);
                case 1: return new Point2D(0.7, 0.3);
                case 2: return new Point2D(0.3, 0.7);
                case 3: return new Point2D(0.7, 0.7);
            }
        } else if (totalSamples == 8) {
            switch (index) {
                case 0: return new Point2D(0.2, 0.2);
                case 1: return new Point2D(0.7, 0.2);
                case 2: return new Point2D(0.3, 0.5);
                case 3: return new Point2D(0.8, 0.5);
                case 4: return new Point2D(0.2, 0.8);
                case 5: return new Point2D(0.7, 0.8);
                case 6: return new Point2D(0.5, 0.3);
                case 7: return new Point2D(0.5, 0.7);
            }
        } else if (totalSamples == 16) {

            double x = (index % 4) / 4.0 + Math.random() * 0.15;
            double y = (index / 4) / 4.0 + Math.random() * 0.15;
            return new Point2D(x, y);
        }

        int size = (int) Math.ceil(Math.sqrt(totalSamples));
        return new Point2D(
                (index % size + Math.random()) / size,
                (index / size + Math.random()) / size
        );
    }

    public static class Point2D {
        public final double x;
        public final double y;

        public Point2D(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}