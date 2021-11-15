package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DrawPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
    private int x = 0, y = 0;
    private ScreenConverter sc;
    private List<Polygon> polygons = new ArrayList<>();
    private List<Line> refactorLines = new ArrayList<>();
    private Polygon newPolygon = null;
    private Polygon editingPolygon = null;
    private RealPoint editingPoint = null;

    public DrawPanel() {
        sc = new ScreenConverter(-2, 2, 4, 4, 800, 600);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
        this.addKeyListener(this);
    }




    @Override
    protected void paintComponent(Graphics origG) {
        sc.setSw(getWidth());
        sc.setSh(getHeight());

        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);

        for (Polygon polygon : polygons) {
            if (polygon == editingPolygon) {
                drawEditingPolygon(g, sc, polygon);
            } else {
                drawPolygon(g, sc, polygon);
            }
        }

        if (refactorLines != null) {
            g.setColor(Color.RED);
            for(Line line: refactorLines) {
                drawLine(g, sc, line);
            }
            g.setColor(Color.BLACK);
        }

        origG.drawImage(bi, 0, 0, null);
        g.dispose();
    }

    private static void drawLine(Graphics2D g, ScreenConverter sc, Line l) {
        ScreenPoint p1 = sc.r2s(l.getP1());
        ScreenPoint p2 = sc.r2s(l.getP2());
        g.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    private static void drawEditingPolygon(Graphics2D g, ScreenConverter sc, Polygon polygon) {
        Color prevColor = g.getColor();
        g.setColor(Color.GREEN);
        ArrayList<RealPoint> points = polygon.getPoints();
        for (RealPoint point: points) {
            ScreenPoint sp = sc.r2s(point);
            g.fillOval(sp.getX() - DELTA / 2, sp.getY() - DELTA / 2, DELTA, DELTA);
        }
        drawPolygon(g, sc, polygon);
        g.setColor(prevColor);
    }

    private static void drawPolygon(Graphics2D g, ScreenConverter sc, Polygon polygon) {
        ArrayList<RealPoint> points = polygon.getPoints();
        if (points.size() > 1) {
            int r = polygon.getR();
            if(r == 0) {
                ScreenPoint p1 = sc.r2s(points.get(0));
                ScreenPoint p2;
                for (int i = 1; i < points.size(); i++) {
                    p2 = sc.r2s(points.get(i));
                    g.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                    p1 = p2;
                }
                p2 = sc.r2s(points.get(0));
                g.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            } else {
                ScreenPoint p1 = sc.r2s(points.get(0));
                ScreenPoint p2 = sc.r2s(points.get(1));
                ScreenPoint p3 = sc.r2s(points.get(2));
                Arch arc = findArc(sc, p1, p2, p3, r, g);
                int touchPrevX = arc.getTouchX2();
                int touchPrevY = arc.getTouchY2();
                int touchPrevX1 = arc.getTouchX1();
                int touchPrevY1 = arc.getTouchY1();
                g.drawArc(arc.getX(), arc.getY(), arc.getWidth(), arc.getHeight(), arc.getStartAngle(), arc.getArcAngle());
                for (int i = 3; i < points.size(); i++) {
                    p1 = sc.r2s(points.get(i - 2));
                    p2 = sc.r2s(points.get(i - 1));
                    p3 = sc.r2s(points.get(i));
                    arc = findArc(sc, p1, p2, p3, r, g);
                    g.drawLine(arc.getTouchX1(), arc.getTouchY1(), touchPrevX, touchPrevY);
                    g.drawArc(arc.getX(), arc.getY(), arc.getWidth(), arc.getHeight(), arc.getStartAngle(), arc.getArcAngle());
                    touchPrevX = arc.getTouchX2();
                    touchPrevY = arc.getTouchY2();
                }
                p1 = p2;
                p2 = p3;
                p3 = sc.r2s(points.get(0));
                arc = findArc(sc, p1, p2, p3, r, g);
                g.drawLine(arc.getTouchX1(), arc.getTouchY1(), touchPrevX, touchPrevY);
                g.drawArc(arc.getX(), arc.getY(), arc.getWidth(), arc.getHeight(), arc.getStartAngle(), arc.getArcAngle());
                touchPrevX = arc.getTouchX2();
                touchPrevY = arc.getTouchY2();
                p1 = p2;
                p2 = p3;
                p3 = sc.r2s(points.get(1));
                arc = findArc(sc, p1, p2, p3, r, g);
                g.drawLine(arc.getTouchX1(), arc.getTouchY1(), touchPrevX, touchPrevY);
                g.drawArc(arc.getX(), arc.getY(), arc.getWidth(), arc.getHeight(), arc.getStartAngle(), arc.getArcAngle());
                touchPrevX = arc.getTouchX2();
                touchPrevY = arc.getTouchY2();
                g.drawLine(touchPrevX1, touchPrevY1, touchPrevX, touchPrevY);
            }
        }
    }

    private static Arch findArc(ScreenConverter sc, ScreenPoint p1, ScreenPoint p2, ScreenPoint p3, int r, Graphics g) {
        int x, y, startAngle, arcAngle, touchX1, touchY1, touchX2, touchY2;
        double x0 = p2.getX();
        double y0 = p2.getY();
        double x1 = p1.getX();
        double y1 = p1.getY();
        double x2 = p2.getX();
        double y2 = p2.getY();
        double x3 = p3.getX();
        double y3 = p3.getY();
        double a1 = y0 - y1;
        double b1 = x1 - x0;
        double a2 = y2 - y3;
        double b2 = x3 - x2;
        double cosA = b1 / Math.sqrt(b1 * b1 + a1 * a1);
        double sinA = Math.sqrt(1 - cosA * cosA);
        double r1 = r / sinA;
        cosA = b2 / Math.sqrt(b2 * b2 + a2 * a2);
        sinA = Math.sqrt(1 - cosA * cosA);
        double r2 = r / sinA;
        if (((y2 <= y1) && (y2 <= y3)) || ((y2 >= y1) && (y2 >= y3))) {
            if (x1 < x3) {
                x0 += r1;
                x1 += r1;
                x2 -= r2;
                x3 -= r2;
            } else {
                x0 -= r1;
                x1 -= r1;
                x2 += r2;
                x3 += r2;
            }
        } else if ((x2 <= x1) && (x2 <= x3)) {
            x0 += r1;
            x1 += r1;
            x2 += r2;
            x3 += r2;
        } else if ((x2 >= x1) && (x2 >= x3)) {
            x0 -= r1;
            x1 -= r1;
            x2 -= r2;
            x3 -= r2;
        }
        a1 = y0 - y1;
        b1 = x1 - x0;
        double c1 = x0 * y1 - x1 * y0;
        a2 = y2 - y3;
        b2 = x3 - x2;
        double c2 = x2 * y3 - x3 * y2;
        double det = a1 * b2 - a2 * b1;
        x = (int)((b1 * c2 - b2 * c1) / det);
        y = (int)((a2 * c1 - a1 * c2) / det);

        x2 = p2.getX();
        y2 = p2.getY();

        double l = findDistance(new ScreenPoint(x, y), p2);
        double d = Math.sqrt(l * l - r * r);
        double cs = (x - x2) / l;
        double u1;
        if (y < y2) {
            u1 = Math.acos(cs);
        } else {
            u1 = 2 * Math.PI - Math.acos(cs);
        }
        cs = d / l;
        double u2 = Math.acos(cs);
        double u = u1 - u2;
        touchX1 = (int)(x2 + d * Math.cos(u));
        touchY1 = (int)(y2 - d * Math.sin(u));
        u = u1 + u2;
        touchX2 = (int)(x2 + d * Math.cos(u));
        touchY2 = (int)(y2 - d * Math.sin(u));

        startAngle = (int)Math.toDegrees(Math.atan2(y - touchY1, touchX1 - x));
        arcAngle = (int)Math.toDegrees(Math.atan2(y - touchY2, touchX2 - x));

        return new Arch(x - r, y - r, r * 2, r * 2, startAngle, (int)angleDiff(startAngle, arcAngle),
                touchX1, touchY1, touchX2, touchY2);
    }

    static double angleDiff(double a, double b) {
        double d = b - a;
        while (d >= 180f) { d -= 360f; }
        while (d < -180f) { d += 360f; }
        return d;
    }

    private static final int DELTA = 10;
    private static boolean isPointNearby(ScreenPoint p1, ScreenPoint p2, int delta) {
        double distance = findDistance(p1, p2);
        if (delta > distance) {
            return true;
        } else {
            return false;
        }
    }
    private static double findDistance(ScreenPoint p1, ScreenPoint p2) {
        return Math.sqrt(Math.pow(p2.getX() - p1.getX(), 2) + Math.pow(p2.getY() - p1.getY(), 2));
    }

    private static boolean isLineNearby(ScreenConverter sc, ScreenPoint p0, Line line, int delta) {
        double d;
        ScreenPoint p1 = sc.r2s(line.getP1());
        ScreenPoint p2 = sc.r2s(line.getP2());
        double x0 = p0.getX();
        double y0 = p0.getY();
        double x1 = p1.getX();
        double y1 = p1.getY();
        double x2 = p2.getX();
        double y2 = p2.getY();
        double a = y2 - y1;
        double b = x1 - x2;
        double c = y1 * (x2 - x1) - x1 * (y2 - y1);
        d = (Math.abs(a * x0 + b * y0 + c) /
                (Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2))));
        if ((((x1 - delta < x0) && (x0 < x2 + delta)) || ((x2 - delta < x0) && (x0 < x1 + delta))) &&
                (((y1 - delta < y0) && (y0 < y2 + delta)) || ((y2 - delta < y0) && (y0 < y1 + delta)))) {
            if (d < delta) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private Polygon findPolygon(ScreenConverter sc, ScreenPoint searchPoint, int delta) {
        for (Polygon polygon : polygons) {
            ArrayList<RealPoint> points = polygon.getPoints();
            RealPoint p1 = points.get(0);
            RealPoint p2;
            for (int i = 1; i < points.size(); i++) {
                p2 = points.get(i);
                Line line = new Line(p1, p2);
                if (isLineNearby(sc, searchPoint, line, DELTA)) {
                    return polygon;
                }
                p1 = p2;
            }
            p2 = points.get(0);
            Line line = new Line(p1, p2);
            if (isLineNearby(sc, searchPoint, line, DELTA)) {
                return polygon;
            }
        }
        return null;
    }

    private RealPoint findPointInPolygon(ScreenConverter sc, ScreenPoint searchPoint, Polygon polygon, int delta) {
        ArrayList<RealPoint> points = polygon.getPoints();
        for (RealPoint p: points) {
            if (isPointNearby(sc.r2s(p), searchPoint, DELTA)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    private ScreenPoint firstPoint = null;
    @Override
    public void mousePressed(MouseEvent e) {
        ScreenPoint currentPoint = new ScreenPoint(e.getX(), e.getY());
        if (SwingUtilities.isMiddleMouseButton(e)) {
            if (editingPolygon != null) {
                firstPoint = currentPoint;
            }
        } else if (SwingUtilities.isRightMouseButton(e)) {
            firstPoint = currentPoint;
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            if (newPolygon != null) {
                RealPoint prevPoint = refactorLines.get(refactorLines.size() - 1).getP2();
                refactorLines.add(new Line(prevPoint, sc.s2r(currentPoint)));
            } else {
                editingPolygon = findPolygon(sc, currentPoint, DELTA);
                if (editingPolygon != null) {
                    editingPoint = findPointInPolygon(sc, currentPoint, editingPolygon, DELTA);
                } else {
                    firstPoint = currentPoint;
                    newPolygon = new Polygon(sc.s2r(firstPoint));
                    RealPoint p = sc.s2r(currentPoint);
                    refactorLines.add(new Line(p, p));
                }
            }
        }
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        ScreenPoint currentPoint = new ScreenPoint(e.getX(), e.getY());
        if (SwingUtilities.isMiddleMouseButton(e)) {
            firstPoint = null;
        } else if (SwingUtilities.isRightMouseButton(e)) {
            firstPoint = null;
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            if (newPolygon != null) {
                if (isPointNearby(firstPoint, new ScreenPoint(e.getX(), e.getY()), DELTA)) {
                    polygons.add(newPolygon);
                    newPolygon = null;
                    refactorLines.clear();
                } else {
                    newPolygon.addPoint(sc.s2r(currentPoint));
                }
            } else {
                if (editingPoint != null) {
                    editingPoint = null;
                    refactorLines.clear();
                }
            }
        }
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        ScreenPoint currentPoint = new ScreenPoint(e.getX(), e.getY());
        if (SwingUtilities.isMiddleMouseButton(e)) {
            if (editingPolygon != null) {
                int r = (int)findDistance(firstPoint, currentPoint) - DELTA;
                if (r > 0) {
                    editingPolygon.setR(r);
                } else {
                    editingPolygon.setR(0);
                }
            }
        } else if (SwingUtilities.isRightMouseButton(e)) {
            RealPoint p1 = sc.s2r(currentPoint);
            RealPoint p2 = sc.s2r(firstPoint);
            RealPoint delta = p2.minus(p1);
            sc.moveCorner(delta);
            firstPoint = currentPoint;
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            if (newPolygon != null) {
                refactorLines.get(refactorLines.size() - 1).setP2(sc.s2r(new ScreenPoint(e.getX(), e.getY())));
            } else {
                if (editingPoint != null) {
                    RealPoint newPoint = sc.s2r(currentPoint);
                    double newX = newPoint.getX();
                    double newY = newPoint.getY();
                    editingPoint.setX(newX);
                    editingPoint.setY(newY);
                }
            }
        }
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    private static final double SCALE_STEP = 0.1;
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int clicks = e.getWheelRotation();
        double scale = 1;
        double coef = 1 + SCALE_STEP * (clicks < 0 ? -1 : 1);
        for (int i = Math.abs(clicks); i > 0 ; i--) {
            scale *= coef;
        }
        sc.changeScale(scale);
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            refactorLines.clear();
            newPolygon = null;
            repaint();
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            editingPolygon = null;
            editingPoint = null;
            repaint();
        }
    }
}
