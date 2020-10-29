package com.example.mmmmeeting.activity;


import com.google.android.gms.maps.model.LatLng;

public class Point {
    double x;
    double y;

    double angle;

    public Point(double xCoord, double yCoord){
        x = xCoord;
        y = yCoord;
    }

    //angle of this point with respect to p
    public void setAngle(Point p) {
        angle = Math.toDegrees(Math.atan2(y-p.y, x-p.x));
        if (angle < 0)
            angle += 360;
    }

    //returns true for ccw, false for cw
    public static boolean orientation(Point p1, Point p2, Point p3) {
        return (p2.y - p1.y) * (p3.x - p2.x) - (p2.x - p1.x)*(p3.y - p2.y) < 0;
    }

    //distance from this point to the other
    public double distanceTo(Point other) {
        double xDiff = Math.abs(x - other.x);
        double yDiff = Math.abs(y - other.y);

        return Math.sqrt(xDiff*xDiff + yDiff*yDiff);
    }

    @Override
    public String toString() {
        return "[" + x +"," + y + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point o = (Point) obj;
            return this.x == o.x && this.y == o.y;
        }
        return false;
    }
    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }

    public LatLng getposition(){return new LatLng(x,y);}
}
