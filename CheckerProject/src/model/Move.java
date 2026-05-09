package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Move lưu chuỗi các ô đi qua (path) và danh sách vị trí các quân bị ăn (captures).
 * path: danh sách Point(col, row) theo thứ tự: start -> ... -> final
 */
public class Move {
    public final List<Point> path = new ArrayList<>();
    public final List<Point> captures = new ArrayList<>();

    public Move() {}

    // tiện: move đơn bước
    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        path.add(new Point(fromCol, fromRow));
        path.add(new Point(toCol, toRow));
    }

    public void addStep(int row, int col) {
        path.add(new Point(col, row));
    }

    public void addCapture(int row, int col) {
        captures.add(new Point(col, row));
    }

    public boolean isCapture() {
        return !captures.isEmpty();
    }

    // trả về Point cuối cùng (to): x = col, y = row
    public Point to() {
        if (path.isEmpty()) return null;
        return path.get(path.size() - 1);
    }

    // trả về Point đầu (from)
    public Point from() {
        if (path.isEmpty()) return null;
        return path.get(0);
    }

    public int getFromRow() { return from().y; }
    public int getFromCol() { return from().x; }
    public int getToRow()   { Point t = to(); return t==null? -1 : t.y; }
    public int getToCol()   { Point t = to(); return t==null? -1 : t.x; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Move:");
        for (Point p : path) sb.append(" ("+p.x+","+p.y+")");
        if (isCapture()) {
            sb.append(" captures:");
            for (Point c : captures) sb.append(" ("+c.x+","+c.y+")");
        }
        return sb.toString();
    }
}
