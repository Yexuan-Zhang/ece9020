import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class ColorDialog {
    public static Color showColorDialog(Component parent, Color currentColor) {
        return JColorChooser.showDialog(parent, "Select Drawing Colour", currentColor);
    }
}

public class SketchpadMain extends Frame {

    private SketchpadCanvas canvas;  // 自定义绘图区
    private Color selectedColor = Color.BLACK;

    public SketchpadMain() {
        super("Sketchpad Main Page");
        setSize(800, 600);
        setLayout(new BorderLayout());

        // 创建绘图区
        canvas = new SketchpadCanvas();
        add(canvas, BorderLayout.CENTER);

        // 创建按钮面板
        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new FlowLayout());

        // 添加模式按钮
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

        // 添加按钮点击事件
        lineBtn.addActionListener(e -> canvas.setMode(DrawMode.LINE));
        rectBtn.addActionListener(e -> canvas.setMode(DrawMode.RECTANGLE));
        sqrBtn.addActionListener(e -> canvas.setMode(DrawMode.SQUARE));
        ellipseBtn.addActionListener(e -> canvas.setMode(DrawMode.ELLIPSE));
        cleBtn.addActionListener(e -> canvas.setMode(DrawMode.CIRCLE));
        plyBtn.addActionListener(e -> canvas.setMode(DrawMode.POLYGON_CLOSED));
        freehandBtn.addActionListener(e -> canvas.setMode(DrawMode.FREEHAND));
        clearBtn.addActionListener(e -> canvas.clearShapes());
        colorBtn.addActionListener(e -> {
            Color chosenColor = ColorDialog.showColorDialog(this, selectedColor);
            if(chosenColor != null) {
                selectedColor = chosenColor;
                canvas.setCurrentColor(selectedColor);
            }
        });

        cutBtn.addActionListener(e -> canvas.cutSelectedShape());
        // Add keyboard shortcut ctrl+x for cut
        canvas.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_X) {
                    canvas.cutSelectedShape();
                }
            }
        });
        canvas.setFocusable(true);
        canvas.requestFocusInWindow();

        copyBtn.addActionListener(e -> canvas.copySelectedShape());
        pasteBtn.addActionListener(e -> canvas.pasteShape());
        // Add keyboard shortcuts ctrl+c / ctrl+v
        canvas.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_C) canvas.copySelectedShape();
                    if (e.getKeyCode() == KeyEvent.VK_V) canvas.pasteShape();
                }
            }
        });
        canvas.setFocusable(true);
        canvas.requestFocusInWindow();

        // 加入按钮到面板
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

        // 关闭窗口事件
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
