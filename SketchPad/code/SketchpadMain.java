import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class ColorDialog {
    public static Color showColorDialog(Component parent, Color currentColor) {
        return JColorChooser.showDialog(parent, "Select Drawing Colour", currentColor);
    }
}

public class SketchpadMain extends Frame {

    private SketchpadCanvas canvas; 
    private Color selectedColor = Color.BLACK;
    private Label statusLabel; 

    public SketchpadMain() {
        super("Sketchpad Main Page");
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Creating canvas
        canvas = new SketchpadCanvas();
        add(canvas, BorderLayout.CENTER);

        // Creating label hints
        statusLabel = new Label("Current Mode: LINE");        
        canvas.setStatusCallback(statusLabel::setText);
        add(statusLabel, BorderLayout.SOUTH); // Add to the bottom

        // Creating button panel
        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new FlowLayout());

        Button lineBtn = new Button("Line");
        Button rectBtn = new Button("Rectangle");
        Button sqrBtn = new Button("Square");
        Button ellipseBtn = new Button("Ellipse");
        Button cleBtn = new Button("Circle");
        Button freehandBtn = new Button("Freehand");
        Button plyBtn = new Button("Polygon");
        Button colorBtn = new Button("Choose Colour");
        Button clearBtn = new Button("Clear");
        Button cutBtn = new Button("Cut");
        Button copyBtn = new Button("Copy");
        Button pasteBtn = new Button("Paste");

        // Updating
        lineBtn.addActionListener(e -> {
            canvas.setMode(DrawMode.LINE);
            statusLabel.setText("Current Mode: LINE");
        });
        rectBtn.addActionListener(e -> {
            canvas.setMode(DrawMode.RECTANGLE);
            statusLabel.setText("Current Mode: RECTANGLE");
        });
        sqrBtn.addActionListener(e -> {
            canvas.setMode(DrawMode.SQUARE);
            statusLabel.setText("Current Mode: SQUARE");
        });
        ellipseBtn.addActionListener(e -> {
            canvas.setMode(DrawMode.ELLIPSE);
            statusLabel.setText("Current Mode: ELLIPSE");
        });
        cleBtn.addActionListener(e -> {
            canvas.setMode(DrawMode.CIRCLE);
            statusLabel.setText("Current Mode: CIRCLE");
        });
        plyBtn.addActionListener(e -> {
            canvas.setMode(DrawMode.POLYGON_CLOSED);
            statusLabel.setText("Current Mode: POLYGON");
        });
        freehandBtn.addActionListener(e -> {
            canvas.setMode(DrawMode.FREEHAND);
            statusLabel.setText("Current Mode: FREEHAND");
        });

        clearBtn.addActionListener(e -> {
            canvas.clearShapes();
            canvas.clearClipboard();
            statusLabel.setText("Canvas cleared");
        });

        colorBtn.addActionListener(e -> {
            Color chosenColor = ColorDialog.showColorDialog(this, selectedColor);
            if (chosenColor != null) {
                selectedColor = chosenColor;
                canvas.setCurrentColor(selectedColor);
                statusLabel.setText("Selected Color Updated");
            }
        });

        cutBtn.addActionListener(e -> {
            canvas.cutSelectedShape();
            statusLabel.setText("Cut selected shape");
        });

        copyBtn.addActionListener(e -> {
            canvas.copySelectedShape();
            statusLabel.setText("Copied selected shape");
        });

        pasteBtn.addActionListener(e -> {
            if (canvas.getClipboardShape() != null) {
                canvas.pasteShape();
                statusLabel.setText("Pasted shape");
            } else {
                statusLabel.setText("Clipboard is empty or nothing copied yet");
            }
        });

        // Adding shortcuts: Ctrl+X / C / V
        canvas.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown()) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_X:
                            canvas.cutSelectedShape();
                            statusLabel.setText("Cut selected shape (Ctrl+X)");
                            break;
                        case KeyEvent.VK_C:
                            canvas.copySelectedShape();
                            statusLabel.setText("Copied selected shape (Ctrl+C)");
                            break;
                        case KeyEvent.VK_V:
                            canvas.pasteShape();
                            statusLabel.setText("Pasted shape (Ctrl+V)");
                            break;
                    }
                }
            }
        });

        canvas.setFocusable(true);
        canvas.requestFocusInWindow();

        // Adding buttons
        buttonPanel.add(lineBtn);
        buttonPanel.add(rectBtn);
        buttonPanel.add(sqrBtn);
        buttonPanel.add(ellipseBtn);
        buttonPanel.add(cleBtn);
        buttonPanel.add(plyBtn);
        buttonPanel.add(freehandBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(colorBtn);
        buttonPanel.add(cutBtn);
        buttonPanel.add(copyBtn);
        buttonPanel.add(pasteBtn);

        add(buttonPanel, BorderLayout.NORTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        new SketchpadMain();
    }
}
