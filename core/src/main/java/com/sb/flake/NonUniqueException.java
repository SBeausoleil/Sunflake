package com.sb.flake;

public class NonUniqueException extends Exception {
    private final Object alreadyIndexed;
    private final Object colliding;
    private final long collision;

    public NonUniqueException(Object alreadyIndexed, Object colliding, long collision) {
        super("Colliding worker ids: [" + alreadyIndexed + ", " + colliding + "], hash collision = " + collision);
        this.alreadyIndexed = alreadyIndexed;
        this.colliding = colliding;
        this.collision = collision;
    }

    public Object getAlreadyIndexed() {
        return alreadyIndexed;
    }

    public Object getColliding() {
        return colliding;
    }

    public long getCollision() {
        return collision;
    }
}
