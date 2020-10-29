package com.example.mmmmeeting.activity;


public class GrahamScan {

    //finds the convex hull of a set of points
    public static Point[] convexHull(Point[] points) {
        Point[] hull;

        //find the lowest point by y value
        // and sort the points by angle (P, P_lowest, x_axis)
        Point lowest = findLowestPoint(points);
        for(Point point : points)
            point.setAngle(lowest);
        sortByAngle(points, lowest);

        //degenerate case: remove co-linear points
        points = removeCollinear(points, lowest);

        //convex hull of less than 3 points is itself
        if(points.length < 3) {
            hull = new Point[points.length];
            for(int i = 0; i < points.length; i++)
                hull[i] = points[i];
            return hull;
        }

        //create processing stack with first 3 points
        Stack<Point> stack = new Stack<>(points.length);
        int index = 0;
        while (stack.size() < 3) {
            if(points[index] != null)
                stack.push(points[index]);
            index++;
        }

        //process the other points
        for(int i = index; i < points.length; i++) {
            Point p1 = stack.peekUnder();
            Point p2 = stack.peek();
            Point p3 = points[i];
            boolean ccw = Point.orientation(p1, p2, p3);;
            while(!ccw) {
                stack.pop();
                p1 = stack.peekUnder();
                p2 = stack.peek();
                ccw = Point.orientation(p1, p2, p3);
            }
            stack.push(points[i]);
        }

        //read the stack
        hull = new Point[stack.size()];
        for(int i = 0; i < hull.length; i ++)
            hull[i] = stack.get(i);

        return hull;
    }

    //linear scan to find lowest y coordinate, ties broken by lower x coordinate
    static Point findLowestPoint(Point[] points) {
        Point lowest = points[0];
        for(Point p : points) {
            if((p.y < lowest.y) || ((p.y == lowest.y) && p.x < lowest.x))
                lowest = p;
        }
        return lowest;
    }

    //given a point p and a set of points P, heap sort P by angle to p
    static void sortByAngle(Point[] points, Point p) {
        for(int i = points.length/2 - 1; i >= 0; i--)
            angleHeapify(points, points.length, i);

        for (int i = points.length - 1; i >= 0; i--) {
            Point temp = points[0];
            points[0] = points[i];
            points[i] = temp;
            angleHeapify(points, i, 0);
        }
    }

    //rebalance the points in the heap by their angles
    static void angleHeapify(Point[] points, int size, int index) {
        int biggest = index;
        int left = 2*index + 1;
        int right = 2*index + 2;

        if(left < size && points[left].angle > points[biggest].angle)
            biggest = left;

        if(right < size && points[right].angle > points[biggest].angle)
            biggest = right;

        if (biggest != index) {
            Point temp = points[index];
            points[index] = points[biggest];
            points[biggest] = temp;
            angleHeapify(points, size, biggest);
        }
    }

    //if there are multiple co-linear points (P_lowest, P_i, ..., P_n), only keep the furthest one
    static Point[] removeCollinear(Point[] points, Point p) {
        int numRemoved = 0;
        int currAngleInd = points.length - 1;

        for(int i = points.length - 1; i >= 0; i--) {
            if(points[i] == p)
                continue;
            if(points[i].angle == points[currAngleInd].angle && i != currAngleInd) {
                Point p1 = points[currAngleInd];
                Point p2 = points[i];

                if(p2.distanceTo(p) >= p1.distanceTo(p)) {
                    points[currAngleInd] = null;
                    currAngleInd = i;
                } else {
                    points[i] = null;
                }

                numRemoved++;
            } else
                currAngleInd = i;
        }

        int index = 0;
        Point[] newPoints = new Point[points.length - numRemoved];
        for(int i = 0; i < newPoints.length; i++) {
            while(points[index] == null)
                index++;
            newPoints[i] = points[index];
            index++;
        }

        return newPoints;
    }
}