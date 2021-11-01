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
                g.setColor(Color.GREEN);
                drawPolygon(g, sc, polygon);
                g.setColor(Color.BLACK);
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

    private static void drawPolygon(Graphics2D g, ScreenConverter sc, Polygon polygon) {
        ArrayList<RealPoint> points = polygon.getPoints();
        if (points.size() > 1) {
            ScreenPoint p1 = sc.r2s(points.get(0));
            ScreenPoint p2;
            for (int i = 1; i < points.size(); i++) {
                p2 = sc.r2s(points.get(i));
                g.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                p1 = p2;
            }
            p2 = sc.r2s(points.get(0));
            g.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        }
    }

    private static final int DELTA = 10;
    private static boolean isPointNearby(ScreenPoint p1, ScreenPoint p2, int delta) {
        double distance = Math.sqrt(Math.pow(p2.getX() - p1.getX(), 2) + Math.pow(p2.getY() - p1.getY(), 2));
        if (delta > distance) {
            return true;
        } else {
            return false;
        }
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
        if (SwingUtilities.isRightMouseButton(e)) {
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
        if (SwingUtilities.isRightMouseButton(e)) {
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
        if (SwingUtilities.isRightMouseButton(e)) {
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
