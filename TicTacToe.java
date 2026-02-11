package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class TicTacToe extends JFrame implements ActionListener {

    private JButton[] buttons = new JButton[9];
    private JLabel statusLabel, xScoreLabel, oScoreLabel;
    private int xWins = 0, oWins = 0, moves = 0;
    private boolean xTurn = true;

    // Modern colors
    private final Color BG = new Color(25, 25, 35);
    private final Color CARD = new Color(40, 40, 55);
    private final Color GRID = new Color(50, 50, 70);
    private final Color X_COLOR = new Color(0, 200, 255);
    private final Color O_COLOR = new Color(255, 100, 100);
    private final Color WIN = new Color(0, 230, 150);

    public TicTacToe() {
        setTitle("Tic Tac Toe");
        setSize(600, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(BG);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(new EmptyBorder(20, 20, 10, 20));

        statusLabel = new JLabel("X's Turn", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        statusLabel.setForeground(Color.WHITE);
        header.add(statusLabel, BorderLayout.NORTH);

        // ScoreBoard
        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        scorePanel.setBackground(BG);

        xScoreLabel = createScoreCard("X", 0, X_COLOR);
        oScoreLabel = createScoreCard("O", 0, O_COLOR);
        scorePanel.add(xScoreLabel);
        scorePanel.add(oScoreLabel);

        header.add(scorePanel, BorderLayout.SOUTH);

        // Game Pannel
        JPanel gridPanel = new JPanel(new GridLayout(3, 3, 8, 8));
        gridPanel.setBackground(BG);
        gridPanel.setBorder(new EmptyBorder(10, 30, 10, 30));

        for (int i = 0; i < 9; i++) {
            JButton btn = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Button background
                    g2.setColor(GRID);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                    // Glow effect on hover if empty
                    if (getModel().isRollover() && getText().isEmpty()) {
                        g2.setColor(new Color(255, 255, 255, 30));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    }

                    super.paintComponent(g);
                }
            };

            btn.setFont(new Font("Segoe UI", Font.BOLD, 60));
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createEmptyBorder());
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(this);

            buttons[i] = btn;
            gridPanel.add(btn);
        }

        // Main control

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        controlPanel.setBackground(BG);

        JButton restartBtn = createModernButton("Restart Game", new Color(100, 150, 255));
        restartBtn.addActionListener(e -> resetGame());

        JButton newGameBtn = createModernButton("New Game", new Color(150, 100, 255));
        newGameBtn.addActionListener(e -> {
            xWins = oWins = 0;
            updateScores();
            resetGame();
        });

        controlPanel.add(restartBtn);
        controlPanel.add(newGameBtn);

        // Adding all components
        add(header, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JLabel createScoreCard(String player, int score, Color color) {
        JLabel card = new JLabel("<html><center><font size='6'>" + player +
                "</font><br><font size='5'>" + score + "</font></center></html>",
                SwingConstants.CENTER);
        card.setFont(new Font("Segoe UI", Font.PLAIN, 1));
        card.setPreferredSize(new Dimension(100, 80));
        card.setOpaque(true);
        card.setBackground(CARD);
        card.setForeground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(10, 5, 10, 5)
        ));
        return card;
    }

    private JButton createModernButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color btnColor = getModel().isPressed() ? color.darker() :
                        getModel().isRollover() ? color.brighter() : color;

                g2.setColor(btnColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(150, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton btn = (JButton) e.getSource();

        if (btn.getText().isEmpty()) {
            // Make move
            if (xTurn) {
                btn.setText("X");
                btn.setForeground(X_COLOR);
                statusLabel.setText("O's Turn");
                statusLabel.setForeground(O_COLOR);
            } else {
                btn.setText("O");
                btn.setForeground(O_COLOR);
                statusLabel.setText("X's Turn");
                statusLabel.setForeground(X_COLOR);
            }
            moves++;

            // Check win
            if (checkWin()) {
                String winner = xTurn ? "X" : "O";
                if (xTurn) xWins++; else oWins++;
                updateScores();

                highlightWinningLine();
                JOptionPane.showMessageDialog(this, "Player " + winner + " wins!");
                resetGame();
            } else if (moves == 9) {
                JOptionPane.showMessageDialog(this, "It's a draw!");
                resetGame();
            } else {
                xTurn = !xTurn;
            }
        }
    }

    private boolean checkWin() {
        int[][] lines = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};
        for (int[] line : lines) {
            String a = buttons[line[0]].getText();
            String b = buttons[line[1]].getText();
            String c = buttons[line[2]].getText();
            if (!a.isEmpty() && a.equals(b) && a.equals(c)) {
                return true;
            }
        }
        return false;
    }

    private void highlightWinningLine() {
        int[][] lines = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};
        for (int[] line : lines) {
            String a = buttons[line[0]].getText();
            String b = buttons[line[1]].getText();
            String c = buttons[line[2]].getText();
            if (!a.isEmpty() && a.equals(b) && a.equals(c)) {
                buttons[line[0]].setBackground(WIN);
                buttons[line[1]].setBackground(WIN);
                buttons[line[2]].setBackground(WIN);
                break;
            }
        }
    }

    private void updateScores() {
        xScoreLabel.setText("<html><center><font size='6'>X</font><br><font size='5'>" + xWins + "</font></center></html>");
        oScoreLabel.setText("<html><center><font size='6'>O</font><br><font size='5'>" + oWins + "</font></center></html>");
    }

    private void resetGame() {
        moves = 0;
        xTurn = true;
        statusLabel.setText("X's Turn");
        statusLabel.setForeground(X_COLOR);

        for (JButton btn : buttons) {
            btn.setText("");
            btn.setBackground(null);
            btn.setOpaque(false);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new TicTacToe();
        });
    }
}