package unbbayes.util;

import java.awt.geom.Point2D;

public final class GeometricUtil {
	
	/**
     *  This method is responsible for finding the tangent point in the circunference of the point2 
     *  in relation to the point1.
     *
     *@param  point1  First circunference's center point (x,y).
     *@param  point2  Second circunference's center point (x,y).
     *@param  r       Circunference's radius.
     *@return         The tangent point to the second circunference.
     */
    public static Point2D.Double getCircunferenceTangentPoint(Point2D.Double point1, Point2D.Double point2, double r) {
        double x = 0;
        double y = 0;
        double x1 = point1.getX();
        double y1 = point1.getY();
        double x2 = point2.getX();
        double y2 = point2.getY();

        if (x2 < x1) {
            x = Math.abs((r * Math.cos(Math.atan((y2 - y1) / (x2 - x1)))) - x1);
            y = Math.abs((r * Math.sin(Math.atan((y2 - y1) / (x2 - x1)))) - y1);
        }
        else {
            x = Math.abs((r * Math.cos(Math.atan((y2 - y1) / (x2 - x1)))) + x1);
            y = Math.abs((r * Math.sin(Math.atan((y2 - y1) / (x2 - x1)))) + y1);
        }
        return new Point2D.Double(x, y);
    }

}
