package com.company;

public class ScreenLine {
    private ScreenPoint p1;
    private ScreenPoint p2;

    public ScreenLine(ScreenPoint p1, ScreenPoint p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public ScreenPoint getP1() {
        return p1;
    }

    public void setP1(ScreenPoint p1) {
        this.p1 = p1;
    }

    public ScreenPoint getP2() {
        return p2;
    }

    public void setP2(ScreenPoint p2) {
        this.p2 = p2;
    }
}
