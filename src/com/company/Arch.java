package com.company;

public class Arch {
    private int x;
    private int y;
    private int width;
    private int height;
    private int startAngle;
    private int arcAngle;
    private int touchX1;
    private int touchY1;
    private int touchX2;
    private int touchY2;

    public Arch(int x, int y, int width, int height, int startAngle, int arcAngle, int touchX1, int touchY1, int touchX2, int touchY2) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.startAngle = startAngle;
        this.arcAngle = arcAngle;
        this.touchX1 = touchX1;
        this.touchY1 = touchY1;
        this.touchX2 = touchX2;
        this.touchY2 = touchY2;
    }

    public int getTouchX1() {
        return touchX1;
    }

    public void setTouchX1(int touchX1) {
        this.touchX1 = touchX1;
    }

    public int getTouchY1() {
        return touchY1;
    }

    public void setTouchY1(int touchY1) {
        this.touchY1 = touchY1;
    }

    public int getTouchX2() {
        return touchX2;
    }

    public void setTouchX2(int touchX2) {
        this.touchX2 = touchX2;
    }

    public int getTouchY2() {
        return touchY2;
    }

    public void setTouchY2(int touchY2) {
        this.touchY2 = touchY2;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(int startAngle) {
        this.startAngle = startAngle;
    }

    public int getArcAngle() {
        return arcAngle;
    }

    public void setArcAngle(int arcAngle) {
        this.arcAngle = arcAngle;
    }
}
