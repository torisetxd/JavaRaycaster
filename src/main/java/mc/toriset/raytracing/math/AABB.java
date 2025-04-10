package mc.toriset.raytracing.math;

public class AABB {
    private Vector3D min;
    private Vector3D max;

    public AABB(Vector3D min, Vector3D max) {
        this.min = new Vector3D(
            Math.min(min.getX(), max.getX()),
            Math.min(min.getY(), max.getY()),
            Math.min(min.getZ(), max.getZ())
        );
        
        this.max = new Vector3D(
            Math.max(min.getX(), max.getX()),
            Math.max(min.getY(), max.getY()),
            Math.max(min.getZ(), max.getZ())
        );
    }

    public AABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this(new Vector3D(minX, minY, minZ), new Vector3D(maxX, maxY, maxZ));
    }

    public AABB(AABB other) {
        this(other.min, other.max);
    }

    public static AABB fromCenterAndSize(Vector3D center, Vector3D size) {
        Vector3D halfSize = size.multiply(0.5);
        return new AABB(
            center.subtract(halfSize),
            center.add(halfSize)
        );
    }

    public Vector3D getMin() {
        return new Vector3D(min);
    }

    public Vector3D getMax() {
        return new Vector3D(max);
    }

    public Vector3D getCenter() {
        return min.add(max).multiply(0.5);
    }

    public Vector3D getSize() {
        return max.subtract(min);
    }

    public Vector3D getExtents() {
        return getSize().multiply(0.5);
    }

    public double getWidth() {
        return max.getX() - min.getX();
    }

    public double getHeight() {
        return max.getY() - min.getY();
    }

    public double getDepth() {
        return max.getZ() - min.getZ();
    }

    public double getVolume() {
        return getWidth() * getHeight() * getDepth();
    }

    public boolean contains(Vector3D point) {
        return point.getX() >= min.getX() && point.getX() <= max.getX() &&
               point.getY() >= min.getY() && point.getY() <= max.getY() &&
               point.getZ() >= min.getZ() && point.getZ() <= max.getZ();
    }

    public boolean contains(AABB other) {
        return contains(other.getMin()) && contains(other.getMax());
    }

    public boolean intersects(AABB other) {
        return !(other.min.getX() > this.max.getX() || 
                 other.max.getX() < this.min.getX() || 
                 other.min.getY() > this.max.getY() ||
                 other.max.getY() < this.min.getY() || 
                 other.min.getZ() > this.max.getZ() || 
                 other.max.getZ() < this.min.getZ());
    }

    public AABB intersection(AABB other) {
        if (!intersects(other)) {
            return null;
        }
        
        Vector3D newMin = new Vector3D(
            Math.max(this.min.getX(), other.min.getX()),
            Math.max(this.min.getY(), other.min.getY()),
            Math.max(this.min.getZ(), other.min.getZ())
        );
        
        Vector3D newMax = new Vector3D(
            Math.min(this.max.getX(), other.max.getX()),
            Math.min(this.max.getY(), other.max.getY()),
            Math.min(this.max.getZ(), other.max.getZ())
        );
        
        return new AABB(newMin, newMax);
    }

    public AABB union(AABB other) {
        Vector3D newMin = new Vector3D(
            Math.min(this.min.getX(), other.min.getX()),
            Math.min(this.min.getY(), other.min.getY()),
            Math.min(this.min.getZ(), other.min.getZ())
        );
        
        Vector3D newMax = new Vector3D(
            Math.max(this.max.getX(), other.max.getX()),
            Math.max(this.max.getY(), other.max.getY()),
            Math.max(this.max.getZ(), other.max.getZ())
        );
        
        return new AABB(newMin, newMax);
    }

    public AABB expand(double amount) {
        Vector3D expansion = new Vector3D(amount, amount, amount);
        return new AABB(
            min.subtract(expansion),
            max.add(expansion)
        );
    }

    public AABB expand(Vector3D amount) {
        return new AABB(
            min.subtract(amount),
            max.add(amount)
        );
    }

    public double distanceToPoint(Vector3D point) {
        double dx = Math.max(0, Math.max(min.getX() - point.getX(), point.getX() - max.getX()));
        double dy = Math.max(0, Math.max(min.getY() - point.getY(), point.getY() - max.getY()));
        double dz = Math.max(0, Math.max(min.getZ() - point.getZ(), point.getZ() - max.getZ()));
        
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public Vector3D closestPoint(Vector3D point) {
        return new Vector3D(
            Math.max(min.getX(), Math.min(point.getX(), max.getX())),
            Math.max(min.getY(), Math.min(point.getY(), max.getY())),
            Math.max(min.getZ(), Math.min(point.getZ(), max.getZ()))
        );
    }

    public AABB translate(Vector3D translation) {
        return new AABB(
            min.add(translation),
            max.add(translation)
        );
    }

    public AABB scale(double factor) {
        Vector3D center = getCenter();
        Vector3D newExtents = getExtents().multiply(factor);
        return fromCenterAndSize(center, newExtents.multiply(2));
    }

    public AABB scale(Vector3D factors) {
        Vector3D center = getCenter();
        Vector3D size = getSize();
        Vector3D newSize = new Vector3D(
            size.getX() * factors.getX(),
            size.getY() * factors.getY(),
            size.getZ() * factors.getZ()
        );
        return fromCenterAndSize(center, newSize);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        AABB other = (AABB) obj;
        return min.equals(other.min) && max.equals(other.max);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + min.hashCode();
        result = 31 * result + max.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AABB(min=" + min + ", max=" + max + ")";
    }
}