package com.sb.flake;

public class NonUniqueException extends Exception {
    private final Object alreadyIndexed;
    private final Object colliding;
    private final int collision;

    public NonUniqueException(Object alreadyIndexed, Object colliding, int collision) {
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

    public int getCollision() {
        return collision;
    }
}
