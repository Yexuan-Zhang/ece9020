import java.awt.*;
import java.util.List;

public class SelectionManager {
    private Shape selectedShape = null;
    private Point offsetFromMouse = null;

    public Shape getSelectedShape() {
        return selectedShape;
    }

    public void selectShapeAt(Point p, List<Shape> shapes) {
        for (int i = shapes.size() - 1; i >= 0; i--) { 
            Shape shape = shapes.get(i);
            if (shape.containsPoint(p.x, p.y)) {
                selectedShape = shape;
                offsetFromMouse = new Point(p.x, p.y); 
                return;
            }
        }
        selectedShape = null;
        offsetFromMouse = null;
    }

    public void moveSelectedTo(Point newMousePoint) {
        if (selectedShape != null && offsetFromMouse != null) {
            selectedShape.moveTo(newMousePoint.x, newMousePoint.y);
        }
    }

    public void deleteSelected(List<Shape> shapes) {
        if (selectedShape != null) {
            shapes.remove(selectedShape);
            selectedShape = null;
            offsetFromMouse = null;
        }
    }

    public Shape copySelected() {
        if (selectedShape != null) {
            return null;
        }
        return null;
    }
}