import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// 画布面板，绘图主逻辑
public class SketchpadCanvas extends Canvas {

    private DrawMode currentMode = DrawMode.LINE;
    private List<Shape> shapes = new ArrayList<>();

    // 当前正在绘制中的状态
    private int startX, startY, endX, endY;
    private List<Point> freehandPoints = new ArrayList<>();
    private List<Point> polygonPoints = new ArrayList<>();
    private boolean isDrawing = false;
    private Color currentColor = Color.BLACK;
    private Shape selectedShape = null;
    private int lastMouseX, lastMouseY;
    private boolean isDragging = false;
    private Shape clipboardShape = null;

    private Consumer<String> statusCallback;
    public void setStatusCallback(Consumer<String> callback) {
        this.statusCallback = callback;
    }

    public void setCurrentColor(Color color) {
        this.currentColor = color;
    }

    public SketchpadCanvas() {
        setBackground(Color.WHITE);

        // 鼠标按下
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                selectedShape = null;
                isDragging = false;

                for (int i = shapes.size() - 1; i >= 0; i--) {
                    Shape s = shapes.get(i);
                    if (s.containsPoint(lastMouseX, lastMouseY)) {
                        selectedShape = s;
                            // if (statusCallback != null) {
                            //     statusCallback.accept("Shape selected");
                            // }
                        return;
                    }
                }

                startX = e.getX();
                startY = e.getY();
                isDrawing = true;

                if (currentMode == DrawMode.FREEHAND) {
                    freehandPoints = new ArrayList<>();
                    freehandPoints.add(new Point(startX, startY));
                } else if (currentMode == DrawMode.POLYGON_OPEN || currentMode == DrawMode.POLYGON_CLOSED) {
                    polygonPoints.add(new Point(startX, startY));
                    repaint();
                }

                
            }

            public void mouseReleased(MouseEvent e) {
                endX = e.getX();
                endY = e.getY();

                if(isDragging) {
                    isDragging = false;
                    selectedShape = null;
                    return;
                }

                if(!isDrawing) {
                    if (statusCallback != null) {
                        statusCallback.accept("Shape is selected");
                    }
                    return;
                }
                
                // Prevent zero-size shapes
                if (startX == endX && startY == endY) {
                    return;
                }
                
                switch (currentMode) {
                    case LINE:
                        shapes.add(new LineShape(startX, startY, endX, endY, currentColor));
                        break;
                    case RECTANGLE:
                        shapes.add(new RectangleShape(startX, startY, endX, endY, currentColor));
                        break;
                    case ELLIPSE:
                        shapes.add(new EllipseShape(startX, startY, endX, endY, currentColor));
                        break;
                    case SQUARE:
                        shapes.add(new SquareShape(startX, startY, endX, endY, currentColor));
                        break;
                    case CIRCLE:
                        shapes.add(new CircleShape(startX, startY, endX, endY, currentColor));
                        break;
                    case FREEHAND:
                        shapes.add(new FreehandShape(new ArrayList<>(freehandPoints), currentColor));
                        break;
                    case POLYGON_CLOSED:
                        if (polygonPoints.size() >= 3 && isCloseToFirstPoint(endX, endY)) {
                            shapes.add(new PolygonShape(new ArrayList<>(polygonPoints), true, currentColor));
                            polygonPoints.clear();
                        }
                        break;
                    case POLYGON_OPEN:
                        if (polygonPoints.size() >= 2 && e.getClickCount() == 2) {
                            shapes.add(new PolygonShape(new ArrayList<>(polygonPoints), false, currentColor));
                            polygonPoints.clear();
                        }
                        break;
                    default:
                        break;
                }

                repaint();
                isDrawing = false;
            }
        });

        // 鼠标拖动
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                endX = e.getX();
                endY = e.getY();
                if (currentMode == DrawMode.FREEHAND) {
                    freehandPoints.add(new Point(endX, endY));
                }
                repaint();

                if (selectedShape != null) {
                    int dx = e.getX() - lastMouseX;
                    int dy = e.getY() - lastMouseY;
                    selectedShape.moveBy(dx, dy);
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                    isDragging = true;
                    repaint();
                }
            }
        });
    }

    public void setMode(DrawMode mode) {
        this.currentMode = mode;
        updateCursor();
    }

    public void clearShapes() {
        shapes.clear();
        polygonPoints.clear();
        repaint();
    }

    private boolean isCloseToFirstPoint(int x, int y) {
        if (polygonPoints.isEmpty()) return false;
        Point first = polygonPoints.get(0);
        return first.distance(x, y) < 10;
    }

    public void paint(Graphics g) {
        // 画所有已完成图形
        for (Shape shape : shapes) {
            shape.draw(g);
        }

        // 当前正在拖动中的图形（预览）
        if (isDrawing) {
            g.setColor(Color.GRAY);
            switch (currentMode) {
                case LINE:
                    g.drawLine(startX, startY, endX, endY);
                    break;
                case RECTANGLE:
                    drawRectPreview(g);
                    break;
                case ELLIPSE:
                    drawOvalPreview(g);
                    break;
                case SQUARE:
                    drawSquarePreview(g);
                    break;
                case CIRCLE:
                    drawCirclePreview(g);
                    break;
                case FREEHAND:
                    drawFreehandPreview(g);
                    break;
                default:
                    break;
            }
        }

        // 预览多边形点
        if ((currentMode == DrawMode.POLYGON_OPEN || currentMode == DrawMode.POLYGON_CLOSED) && !polygonPoints.isEmpty()) {
            //g.setColor(Color.BLUE);
            for (int i = 0; i < polygonPoints.size() - 1; i++) {
                Point p1 = polygonPoints.get(i);
                Point p2 = polygonPoints.get(i + 1);
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }

    // Cut the selected object
    public void cutSelectedShape() {
        if(selectedShape != null) {
            shapes.remove(selectedShape);
            selectedShape = null;
            repaint();
        }
    }

    // Copy and paste the selected object
    public void copySelectedShape() {
        if (selectedShape != null) {
            clipboardShape = cloneShape(selectedShape);
        }
    }

    public Shape getClipboardShape() {
        return clipboardShape;
    }
    
    public void clearClipboard() {
        clipboardShape = null;
    }

    public void pasteShape() {
        if (clipboardShape != null) {
            Shape pasted = cloneShape(clipboardShape);
            pasted.moveBy(20, 20); // Offset slightly to visualize paste
            shapes.add(pasted);
            repaint();
        }
    }

    private void drawRectPreview(Graphics g) {
        int x = Math.min(startX, endX);
        int y = Math.min(startY, endY);
        int w = Math.abs(endX - startX);
        int h = Math.abs(endY - startY);
        g.drawRect(x, y, w, h);
    }

    private void drawOvalPreview(Graphics g) {
        int x = Math.min(startX, endX);
        int y = Math.min(startY, endY);
        int w = Math.abs(endX - startX);
        int h = Math.abs(endY - startY);
        g.drawOval(x, y, w, h);
    }

    private void drawSquarePreview(Graphics g) {
        int size = Math.min(Math.abs(endX - startX), Math.abs(endY - startY));
        int x = startX;
        int y = startY;
        if (endX < startX) x -= size;
        if (endY < startY) y -= size;
        g.drawRect(x, y, size, size);
    }

    private void drawCirclePreview(Graphics g) {
        int size = Math.min(Math.abs(endX - startX), Math.abs(endY - startY));
        int x = startX;
        int y = startY;
        if (endX < startX) x -= size;
        if (endY < startY) y -= size;
        g.drawOval(x, y, size, size);
    }

    private void drawFreehandPreview(Graphics g) {
        if (freehandPoints != null && freehandPoints.size() > 1) {
            g.setColor(Color.GRAY);
            for (int i = 0; i < freehandPoints.size() - 1; i++) {
                Point p1 = freehandPoints.get(i);
                Point p2 = freehandPoints.get(i + 1);
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }  
    }

    private Shape cloneShape(Shape s) {
        if (s instanceof LineShape l) {
            return new LineShape(l.x1, l.y1, l.x2, l.y2, l.color);
        } else if (s instanceof RectangleShape r) {
            return new RectangleShape(r.x, r.y, r.x + r.width, r.y + r.height, r.color);
        } else if (s instanceof EllipseShape e) {
            return new EllipseShape(e.x, e.y, e.x + e.width, e.y + e.height, e.color);
        } else if (s instanceof SquareShape sq) {
            return new SquareShape(sq.x, sq.y, sq.x + sq.size, sq.y + sq.size, sq.color);
        } else if (s instanceof CircleShape c) {
            return new CircleShape(c.x, c.y, c.x + c.size, c.y + c.size, c.color);
        } else if (s instanceof FreehandShape fh) {
            List<Point> copied = new ArrayList<>();
            for (Point p : fh.points) copied.add(new Point(p.x, p.y));
            return new FreehandShape(copied, fh.color);
        } else if (s instanceof PolygonShape pg) {
            List<Point> copied = new ArrayList<>();
            for (Point p : pg.points) copied.add(new Point(p.x, p.y));
            return new PolygonShape(copied, pg.isClosed(), pg.color);
        }
        return null;
    }

    public void updateCursor() {
        switch (currentMode) {
            case LINE:
            case RECTANGLE:
            case SQUARE:
            case ELLIPSE:
            case CIRCLE:
            case POLYGON_OPEN:
            case POLYGON_CLOSED:
            case FREEHAND:
                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                break;
            default:
                setCursor(Cursor.getDefaultCursor());
        }
    }
}