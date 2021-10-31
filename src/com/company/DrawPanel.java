package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DrawPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
    private int x = 0, y = 0;
    private ScreenConverter sc;
    private Line ox, oy;
    private Line l;
    private List<Polygon> polygons = new ArrayList<>();
    private List<Line> refactorLines = new ArrayList<>();
    private Polygon newPolygon = null;
    private RealPoint editingPoint = null;

    public DrawPanel() {
        sc = new ScreenConverter(-2, 2, 4, 4, 800, 600);
        ox = new Line(new RealPoint(-1, 0), new RealPoint(1, 0));
        oy = new Line(new RealPoint(0, -1), new RealPoint(0, 1));
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);

        /*l = new Line(new RealPoint(0, 0), new RealPoint(0, 0));

        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                ScreenPoint sp = new ScreenPoint(e.getX(), e.getY());
                RealPoint rp = sc.s2r(sp);
                l.setP2(rp);
                //x = e.getX();
                //y = e.getY();
                repaint();
            }
        });
         */
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
            drawPolygon(g, sc, polygon);
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

    private static final int DELTA = 10;
    private static boolean isPointNearby(ScreenPoint p1, ScreenPoint p2, int delta) {
        double distance = Math.sqrt(Math.pow(p2.getX() - p1.getX(), 2) + Math.pow(p2.getY() - p1.getY(), 2));
        if (delta > distance) {
            return true;
        } else {
            return false;
        }
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

    private RealPoint findPoint(ScreenConverter sc, ScreenPoint searchPoint, int eps) {
        for (Polygon polygon: polygons) {
            ArrayList<RealPoint> points = polygon.getPoints();
            for (RealPoint rp: points) {
                if (isPointNearby(sc.r2s(rp), searchPoint, DELTA)) {
                    return rp;
                }
            }
        }
        return null;
    }

//    private static double distanceToLine(ScreenPoint lp1, ScreenPoint lp2, ScreenPoint cp) {
//
//    }
//
//    private static Line findLine(ScreenConverter sc, java.util.List<Line> lines, ScreenPoint searchPoint, int eps ) {
//        for (Line l : lines) {
//            if (distanceToLine(sc.r2s(l.getP1()), sc.r2s(l.getP2()), searchPoint) < eps)
//                return l;
//        }
//        return null;
//    }

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
            editingPoint = findPoint(sc, currentPoint, DELTA);
            if (newPolygon == null) {
                if (editingPoint != null) {
                    refactorLines.add(new Line(editingPoint, editingPoint));
                } else {
                    firstPoint = currentPoint;
                    RealPoint p = sc.s2r(currentPoint);
                    refactorLines.add(new Line(p, p));
                }
            } else {
                RealPoint prevPoint = refactorLines.get(refactorLines.size() - 1).getP2();
                refactorLines.add(new Line(prevPoint, sc.s2r(currentPoint)));
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
            if (newPolygon == null) {
                if (editingPoint != null) {
                    RealPoint newPoint = sc.s2r(currentPoint);
                    double newX = newPoint.getX();
                    double newY = newPoint.getY();
                    editingPoint.setX(newX);
                    editingPoint.setY(newY);
                    editingPoint = null;
                    refactorLines.clear();
                } else {
                    newPolygon = new Polygon(sc.s2r(firstPoint));
                    newPolygon.addPoint(sc.s2r(currentPoint));
                }
            } else {
                if (isPointNearby(firstPoint, new ScreenPoint(e.getX(), e.getY()), DELTA)) {
                    polygons.add(newPolygon);
                    newPolygon = null;
                    refactorLines.clear();
                } else {
                    newPolygon.addPoint(sc.s2r(currentPoint));
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
        if (SwingUtilities.isRightMouseButton(e)) {
            ScreenPoint curPoint = new ScreenPoint(e.getX(), e.getY());
            RealPoint p1 = sc.s2r(curPoint);
            RealPoint p2 = sc.s2r(firstPoint);
            RealPoint delta = p2.minus(p1);
            sc.moveCorner(delta);
            firstPoint = curPoint;
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            if (refactorLines != null) {
                if (newPolygon != null) {
                    refactorLines.get(refactorLines.size() - 1).setP2(sc.s2r(new ScreenPoint(e.getX(), e.getY())));
                } else {
                    for (Line line: refactorLines) {
                        line.setP2(sc.s2r(new ScreenPoint(e.getX(), e.getY())));
                    }
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
}
