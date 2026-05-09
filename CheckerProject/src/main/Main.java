package main;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import controller.GameController;
import model.Board;
import model.Move;
import model.Piece;
import view.GameView;

public class Main {
    public static void main(String[] args) {
        Board board = new Board();
        GameController controller = new GameController(board);

        JFrame f = new JFrame("Checkers");
        f.setLayout(new BorderLayout());
        f.add(new GameView(controller), BorderLayout.CENTER);
        JPanel pl = new JPanel(new FlowLayout());
        pl.add(new Button("Restart"));

        f.add(pl, BorderLayout.SOUTH);
        f.setSize(600, 630);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);

    }
}