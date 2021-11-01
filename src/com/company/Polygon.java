package com.company;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Polygon {
    private ArrayList<RealPoint> points = new ArrayList<>();
    private int r;

    public Polygon(RealPoint p) {
        points.add(p);
        r = 0;
    }

    public void addPoint(RealPoint p) {
        if (!points.contains(p)){
            points.add(p);
        }
    }

    public ArrayList<RealPoint> getPoints() {
        return points;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }
}
