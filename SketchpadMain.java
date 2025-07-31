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
    private Label statusLabel; // 状态提示栏

    public SketchpadMain() {
        super("Sketchpad Main Page");
        setSize(800, 600);
        setLayout(new BorderLayout());

        // 创建绘图区
        canvas = new SketchpadCanvas();
        add(canvas, BorderLayout.CENTER);

        // 创建状态提示栏
        statusLabel = new Label("Current Mode: LINE");        
        canvas.setStatusCallback(statusLabel::setText);
        add(statusLabel, BorderLayout.SOUTH); // 添加到底部

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

        // 添加按钮点击事件 + 状态栏更新
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

        // 设置快捷键：Ctrl+X / C / V
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
