package net.sf.jcgm.core.workaround;

import java.awt.geom.CubicCurve2D;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author BROS
 */
public class BezierFlattener {

    static class Point {
        double x;
        double y;

        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        Point midpoint(Point other) {
            return new Point((this.x + other.x) / 2, (this.y + other.y) / 2);
        }

        @Override
        public String toString() {
            return String.format("(%.2f, %.2f)", this.x, this.y);
        }
    }

    static List<Point> flattenCurve(CubicCurve2D curve, double epsilon) {
        Point p0, p1, p2, p3; 
        if (curve instanceof CubicCurve2D.Float) {
            CubicCurve2D.Float floatCurve = (CubicCurve2D.Float) curve;
            p0 = new Point(floatCurve.x1, floatCurve.y1);
            p1 = new Point(floatCurve.ctrlx1, floatCurve.ctrly1);
            p2 = new Point(floatCurve.ctrlx2, floatCurve.ctrly2);
            p3 = new Point(floatCurve.x2, floatCurve.y2);
        } else if (curve instanceof CubicCurve2D.Double) {
            CubicCurve2D.Double doubleCurve = (CubicCurve2D.Double) curve;
            p0 = new Point(doubleCurve.x1, doubleCurve.y1);
            p1 = new Point(doubleCurve.ctrlx1, doubleCurve.ctrly1);
            p2 = new Point(doubleCurve.ctrlx2, doubleCurve.ctrly2);
            p3 = new Point(doubleCurve.x2, doubleCurve.y2);
        } else {
            throw new IllegalArgumentException("unimplemented CubicCurve2D [" + curve.getClass() + "]");
        }

        List<Point> result = new ArrayList<>();
        flattenRecursive(p0, p1, p2, p3, epsilon, result);
        result.add(p3); // Ensure the last point is included
        return result;
    }

    private static void flattenRecursive(Point p0, Point p1, Point p2, Point p3, double epsilon, List<Point> result) {
        if (isFlatEnough(p0, p1, p2, p3, epsilon)) {
            result.add(p0); // Add start point of the segment
        } else {
            // Subdivide the curve
            Point a = p0.midpoint(p1);
            Point b = p1.midpoint(p2);
            Point c = p2.midpoint(p3);
            Point d = a.midpoint(b);
            Point e = b.midpoint(c);
            Point f = d.midpoint(e); // Midpoint on the curve at t=0.5

            // Recurse on both halves
            flattenRecursive(p0, a, d, f, epsilon, result);
            flattenRecursive(f, e, c, p3, epsilon, result);
        }
    }

    private static boolean isFlatEnough(Point p0, Point p1, Point p2, Point p3, double epsilon) {
        double d1 = distanceFromLine(p1, p0, p3);
        double d2 = distanceFromLine(p2, p0, p3);
        final double max = Math.max(d1, d2);
        if (Double.isNaN(max)) {
            return true;
        }
        return max < epsilon;
    }

    private static double distanceFromLine(Point p, Point a, Point b) {
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double numerator = Math.abs(dy * p.x - dx * p.y + b.x * a.y - b.y * a.x);
        double denominator = Math.sqrt(dx * dx + dy * dy);
        return numerator / denominator;
    }
}
