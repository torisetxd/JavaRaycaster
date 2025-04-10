package mc.toriset.raytracing.math;

public class Vector3D {
    public double x;
    public double y;
    public double z;

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D() {
        this(0, 0, 0);
    }

    public Vector3D(Vector3D other) {
        this(other.x, other.y, other.z);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public Vector3D add(Vector3D other) {
        return new Vector3D(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vector3D subtract(Vector3D other) {
        return new Vector3D(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Vector3D multiply(double scalar) {
        return new Vector3D(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public Vector3D divide(double scalar) {
        if (scalar == 0) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        return new Vector3D(this.x / scalar, this.y / scalar, this.z / scalar);
    }

    public double dot(Vector3D other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public Vector3D cross(Vector3D other) {
        return new Vector3D(
            this.y * other.z - this.z * other.y,
            this.z * other.x - this.x * other.z,
            this.x * other.y - this.y * other.x
        );
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public double lengthSquared() {
        return x * x + y * y + z * z;
    }

    public double distance(Vector3D other) {
        return Math.sqrt(distanceSquared(other));
    }

    public double distanceSquared(Vector3D other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public Vector3D normalize() {
        double len = length();
        if (len > 0) {
            return new Vector3D(x / len, y / len, z / len);
        }
        return new Vector3D(0, 0, 0);
    }

    public Vector3D midpoint(Vector3D other) {
        return new Vector3D(
            (this.x + other.x) / 2,
            (this.y + other.y) / 2,
            (this.z + other.z) / 2
        );
    }

    public Vector3D min(Vector3D other) {
        return new Vector3D(
            Math.min(this.x, other.x),
            Math.min(this.y, other.y),
            Math.min(this.z, other.z)
        );
    }

    public Vector3D max(Vector3D other) {
        return new Vector3D(
            Math.max(this.x, other.x),
            Math.max(this.y, other.y),
            Math.max(this.z, other.z)
        );
    }

    public Vector3D lerp(Vector3D target, double alpha) {
        return new Vector3D(
            this.x + (target.x - this.x) * alpha,
            this.y + (target.y - this.y) * alpha,
            this.z + (target.z - this.z) * alpha
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Vector3D other = (Vector3D) obj;
        
        return Double.compare(other.x, x) == 0 &&
               Double.compare(other.y, y) == 0 &&
               Double.compare(other.z, z) == 0;
    }

    @Override
    public int hashCode() {
        int result = 17;
        long xBits = Double.doubleToLongBits(x);
        long yBits = Double.doubleToLongBits(y);
        long zBits = Double.doubleToLongBits(z);
        result = 31 * result + (int) (xBits ^ (xBits >>> 32));
        result = 31 * result + (int) (yBits ^ (yBits >>> 32));
        result = 31 * result + (int) (zBits ^ (zBits >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Vector3D(" + x + ", " + y + ", " + z + ")";
    }
}