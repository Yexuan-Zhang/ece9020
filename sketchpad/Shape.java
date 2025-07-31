import java.awt.*;
import java.util.List;

public abstract class Shape {
    protected Color color;

    public Shape(Color color) {
        this.color = color;
    }

    public void setColor(Color c) {
        this.color = c;
    }

    public abstract void draw(Graphics g);
    public abstract boolean containsPoint(int x, int y);
    public abstract void moveTo(int x, int y);
    public abstract void moveBy(int dx, int dy);
}

// LineShape.java
class LineShape extends Shape {
    int x1, y1, x2, y2;
    public LineShape(int x1, int y1, int x2, int y2, Color color) {
        super(color);
        this.x1 = x1; 
        this.y1 = y1;
        this.x2 = x2; 
        this.y2 = y2;
    }
    public void draw(Graphics g) {
        g.setColor(color);
        g.drawLine(x1, y1, x2, y2);
    }

    public boolean containsPoint(int x, int y) {
        double dist = ptSegDist(x1, y1, x2, y2, x, y);
        return dist <=  5;
    }

    private double ptSegDist(int x1, int y1, int x2, int y2, int px, int py) {
        double dx = x2 - x1, dy = y2 - y1;
        if (dx == 0 && dy == 0) return Point.distance(x1, y1, px, py);
        double t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));
        double projX = x1 + t * dx, projY = y1 + t * dy;
        return Point.distance(px, py, projX, projY);
    }

    public void moveBy(int dx, int dy) {
        x1 += dx;
        y1 += dy;
        x2 += dx;
        y2 += dy;
    }

    public void moveTo(int newX, int newY) {
        int dx = newX - x1;
        int dy = newY - y1;
        moveBy(dx, dy);
    }
}

// RectangleShape.java
class RectangleShape extends Shape {
    int x, y, width, height;
    public RectangleShape(int x1, int y1, int x2, int y2, Color color) {
        super(color);
        this.x = Math.min(x1, x2);
        this.y = Math.min(y1, y2);
        this.width = Math.abs(x2 - x1);
        this.height = Math.abs(y2 - y1);
    }
    public void draw(Graphics g) {
        g.setColor(color);
        g.drawRect(x, y, width, height);
    }

    public boolean containsPoint(int x, int y) {
        return (x >= this.x && x <= this.x + width &&
                y >= this.y && y <= this.y + height);
    }

    public void moveBy(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    public void moveTo(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }
}

// EllipseShape.java
class EllipseShape extends Shape {
    int x, y, width, height;
    public EllipseShape(int x1, int y1, int x2, int y2, Color color) {
        super(color);
        this.x = Math.min(x1, x2);
        this.y = Math.min(y1, y2);
        this.width = Math.abs(x2 - x1);
        this.height = Math.abs(y2 - y1);
    }
    public void draw(Graphics g) {
        g.setColor(color);
        g.drawOval(x, y, width, height);
    }

    public boolean containsPoint(int x, int y) {
        double rx = width / 2.0;
        double ry = height / 2.0;
        double cx = this.x + rx;
        double cy = this.y + ry;

        return Math.pow((x - cx) / rx, 2) + Math.pow((y - cy) / ry, 2) <= 1.0;
    }

    public void moveBy(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    public void moveTo(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }
}

// SquareShape.java
class SquareShape extends Shape {
    int x, y, size;
    public SquareShape(int x1, int y1, int x2, int y2, Color color) {
        super(color);
        size = Math.min(Math.abs(x2 - x1), Math.abs(y2 - y1));
        x = x1;
        y = y1;
        if (x2 < x1) x -= size;
        if (y2 < y1) y -= size;
    }
    public void draw(Graphics g) {
        g.setColor(color);
        g.drawRect(x, y, size, size);
    }

    public boolean containsPoint(int x, int y) {
        return (x >= this.x && x <= this.x + size &&
                y >= this.y && y <= this.y + size);
    }

    public void moveBy(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

        public void moveTo(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }
}

// CircleShape.java
class CircleShape extends Shape {
    int x, y, size;
    public CircleShape(int x1, int y1, int x2, int y2, Color color) {
        super(color);
        size = Math.min(Math.abs(x2 - x1), Math.abs(y2 - y1));
        x = x1;
        y = y1;
        if (x2 < x1) x -= size;
        if (y2 < y1) y -= size;
    }
    public void draw(Graphics g) {
        g.setColor(color);
        g.drawOval(x, y, size, size);
    }

    public boolean containsPoint(int x, int y) {
        double radius = size / 2.0;
        double cx = this.x + radius;
        double cy = this.y + radius;

        return Math.pow(x - cx, 2) + Math.pow(y - cy, 2) <= radius * radius;
    }

    public void moveBy(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    public void moveTo(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }
}

// FreehandShape.java
class FreehandShape extends Shape {
    List<Point> points;
    public FreehandShape(List<Point> points, Color color) {
        super(color);
        this.points = points;
    }
    public void draw(Graphics g) {
        g.setColor(color);
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    public boolean containsPoint(int x, int y) {
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            double dist = ptSegDist(p1.x, p1.y, p2.x, p2.y, x, y);
            if (dist <= 5) return true;
        }
        return false;
    }

    private double ptSegDist(int x1, int y1, int x2, int y2, int px, int py) {
        double dx = x2 - x1, dy = y2 - y1;
        if (dx == 0 && dy == 0) return Point.distance(x1, y1, px, py);
        double t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));
        double projX = x1 + t * dx, projY = y1 + t * dy;
        return Point.distance(px, py, projX, projY);
    }

    public void moveBy(int dx, int dy) {
        for (Point p : points) {
            p.translate(dx, dy);
        }
    }

    public void moveTo(int newX, int newY) {
        if (points.isEmpty()) return;
        int dx = newX - points.get(0).x;
        int dy = newY - points.get(0).y;
        moveBy(dx, dy);
    }
}

// PolygonShape.java
class PolygonShape extends Shape {
    List<Point> points;
    private boolean closed;

    public PolygonShape(List<Point> points, boolean closed, Color color) {
        super(color);
        this.points = points;
        this.closed = closed;
    }

    public void draw(Graphics g) {
        g.setColor(color);
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
        if (closed && points.size() > 2) {
            Point p1 = points.get(points.size() - 1);
            Point p2 = points.get(0);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    public boolean containsPoint(int x, int y) {
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            double dist = ptSegDist(p1.x, p1.y, p2.x, p2.y, x, y);
            if (dist <= 5) return true;
        }
        if (closed && points.size() > 2) {
            Point p1 = points.get(points.size() - 1);
            Point p2 = points.get(0);
            double dist = ptSegDist(p1.x, p1.y, p2.x, p2.y, x, y);
            if (dist <= 5) return true;
        }
        return false;
    }

    private double ptSegDist(int x1, int y1, int x2, int y2, int px, int py) {
        double dx = x2 - x1, dy = y2 - y1;
        if (dx == 0 && dy == 0) return Point.distance(x1, y1, px, py);
        double t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));
        double projX = x1 + t * dx, projY = y1 + t * dy;
        return Point.distance(px, py, projX, projY);
    }

    public void moveBy(int dx, int dy) {
        for (Point p : points) {
            p.translate(dx, dy);
        }
    }

    public void moveTo(int newX, int newY) {
        if (points.isEmpty()) return;
        int dx = newX - points.get(0).x;
        int dy = newY - points.get(0).y;
        moveBy(dx, dy);
    }

    public boolean isClosed() {
        return closed;
    }
}
