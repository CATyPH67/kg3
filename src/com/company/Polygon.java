package com.company;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Polygon {
    private ArrayList<RealPoint> points = new ArrayList<>();

    public Polygon(RealPoint p) {
        points.add(p);
    }

    public void addPoint(RealPoint p) {
        if (!points.contains(p)){
            points.add(p);
        }
    }

    public ArrayList<RealPoint> getPoints() {
        return points;
    }
}
