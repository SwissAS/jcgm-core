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

    static List<Point> flattenCurve(CubicCurve2D.Double curve, double epsilon) {
        Point P0 = new Point(curve.x1, curve.y1);
        Point P1 = new Point(curve.ctrlx1, curve.ctrly1);
        Point P2 = new Point(curve.ctrlx2, curve.ctrly2);
        Point P3 = new Point(curve.x2, curve.y2);

        List<Point> result = new ArrayList<>();
        flattenRecursive(P0, P1, P2, P3, epsilon, result);
        result.add(P3); // Ensure the last point is included
        return result;
    }

    private static void flattenRecursive(Point P0, Point P1, Point P2, Point P3, double epsilon, List<Point> result) {
        if (isFlatEnough(P0, P1, P2, P3, epsilon)) {
            result.add(P0); // Add start point of the segment
        } else {
            // Subdivide the curve
            Point A = P0.midpoint(P1);
            Point B = P1.midpoint(P2);
            Point C = P2.midpoint(P3);
            Point D = A.midpoint(B);
            Point E = B.midpoint(C);
            Point F = D.midpoint(E); // Midpoint on the curve at t=0.5

            // Recurse on both halves
            flattenRecursive(P0, A, D, F, epsilon, result);
            flattenRecursive(F, E, C, P3, epsilon, result);
        }
    }

    private static boolean isFlatEnough(Point P0, Point P1, Point P2, Point P3, double epsilon) {
        double d1 = distanceFromLine(P1, P0, P3);
        double d2 = distanceFromLine(P2, P0, P3);
        return Math.max(d1, d2) < epsilon;
    }

    private static double distanceFromLine(Point P, Point A, Point B) {
        double dx = B.x - A.x;
        double dy = B.y - A.y;
        double numerator = Math.abs(dy * P.x - dx * P.y + B.x * A.y - B.y * A.x);
        double denominator = Math.sqrt(dx * dx + dy * dy);
        return numerator / denominator;
    }
}
