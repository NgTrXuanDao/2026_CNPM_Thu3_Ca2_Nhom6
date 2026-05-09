
package ai;

import model.Board;

public class Node {
    public Board board;
    public boolean isWhite;

    public Node(Board board, boolean isWhite) {
        this.board = board;
        this.isWhite = isWhite;
    }

    public Node() {
    }
}
