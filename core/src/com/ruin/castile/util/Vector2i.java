package com.ruin.castile.util;

/**
 *
 * @author ruin
 */
public class Vector2i {

    public int x;
    public int y;

    /**
     * Constructs and initializes a Vector2i from the specified x and y
     * coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2i(Vector2i other) {
        this.x = other.x;
        this.y = other.y;
    }

    public Vector2i() {
    }
    
    public static Vector2i zero() {
        return new Vector2i(0, 0);
    }

    public void add(Vector2i other) {
        this.x += other.x;
        this.y += other.y;
    }

    public void add(Vector2i a, Vector2i b) {
        this.x = a.x + b.x;
        this.y = a.y + b.y;
    }
    
    public Vector2i addLocal(Vector2i other) {
        return new Vector2i(this.x + other.x, this.y + other.y);
    }

    public void sub(Vector2i other) {
        this.x -= other.x;
        this.y -= other.y;
    }

    public void sub(Vector2i a, Vector2i b) {
        this.x = a.x - b.x;
        this.y = a.y - b.y;
    }
    
    public void div(int factor) {
        this.x /= factor;
        this.y /= factor;
    }
    
    public Vector2i divLocal(float factor) {
        return new Vector2i((int)(this.x/factor), (int)(this.y/factor));
    }
    
    public Vector2i mulLocal(float factor) {
        return new Vector2i((int)(this.x*factor), (int)(this.y*factor));
    }
    
    public Vector2i subLocal(Vector2i other) {
        return new Vector2i(this.x - other.x, this.y - other.y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    public void set(Vector2i other) {
        this.x = other.x;
        this.y = other.y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
    
    public Vector2i rotateAround(Vector2i pivot, float rot) {
        int nowX = (int) (pivot.x + (this.x - pivot.x) * Math.cos(-rot) + (this.y - pivot.y) * Math.sin(-rot));
        int nowY = (int) (pivot.y + (this.y - pivot.y) * Math.cos(-rot) - (this.x - pivot.x) * Math.sin(-rot));
        return new Vector2i(nowX, nowY);
    }

    public Vector2i relative(Vector2i offset, float rot) {
        Vector2i front = this.addLocal(offset);
        return front.rotateAround(this, rot);
    }

    public static float angleBetween(Vector2i a, Vector2i b) {
        float dx = b.x - a.x;
        float dy = b.y - a.y;
        return (float) (Math.atan2(dy, dx) - (Math.PI / 2));
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.x;
        hash = 97 * hash + this.y;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Vector2i other = (Vector2i) obj;
        return true;
    }
    
    

}
