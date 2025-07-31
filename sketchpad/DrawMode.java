public enum DrawMode {
    LINE,           // 直线
    RECTANGLE,      // 矩形
    SQUARE,         // 正方形（矩形等边特例）
    ELLIPSE,        // 椭圆
    CIRCLE,         // 圆（椭圆特例）
    FREEHAND,       // 自由绘制（手绘线条）
    POLYGON_OPEN,   // 开放多边形（不闭合）
    POLYGON_CLOSED, // 闭合多边形
    NONE            // 空模式（默认/清除状态）
}
