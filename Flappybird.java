package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class Flappybird {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird - EASY MODE");
        GamePanel panel = new GamePanel();

        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }
}

class GamePanel extends JPanel implements ActionListener, KeyListener {
    private int birdX = 100;
    private int birdY = 250;
    private int birdwidth = 40;
    private int birdheight = 30;
    private int birdvelocity = 0;
    private final int birdgravity = 1;
    private final int jump = -12; // Balanced jump
    private boolean gameOver = false;
    private boolean gameStarted = false;
    private int score = 0;
    private int highScore = 0;

    private ArrayList<GamePipe> pipes;
    private final int pipegap = 250; // MUCH BIGGER GAP - EASY MODE
    private final int pipewidth = 70; // Thinner pipes
    private final int pipespeed = 3; // Slower speed
    private Random random;
    private Timer timer;

    // Colors for better visuals
    private Color skyColor = new Color(135, 206, 235);
    private Color birdColor = new Color(255, 204, 0);
    private Color pipeColor = new Color(76, 175, 80);
    private Color groundColor = new Color(160, 120, 80);
    private Color textColor = Color.WHITE;

    public GamePanel() {
        setBackground(skyColor);
        setFocusable(true);
        addKeyListener(this);
        pipes = new ArrayList<>();
        random = new Random();
        timer = new Timer(20, this);

        // Initialize game
        resetGame();
        requestFocusInWindow();
    }

    private void addPipe(boolean start) {
        int minHeight = 60; // Lower minimum height
        int maxHeight = 300; // Upper limit for height
        int height = minHeight + random.nextInt(maxHeight - minHeight);
        int x;

        if (start || pipes.isEmpty()) {
            x = 800;
        } else {
            // More space between pipes - EASIER
            x = pipes.get(pipes.size() - 1).x + 350;
        }

        pipes.add(new GamePipe(x, 0, pipewidth, height)); // Top pipe
        pipes.add(new GamePipe(x, height + pipegap, pipewidth, 600 - height - pipegap)); // Bottom pipe
    }

    private void movePipes() {
        for (int i = 0; i < pipes.size(); i++) {
            GamePipe pipe = pipes.get(i);
            pipe.x -= pipespeed;

            // Remove pipes that are off screen
            if (pipe.x + pipewidth < 0) {
                pipes.remove(pipe);
                i--;
            }
        }

        // Add new pipes when needed
        if (pipes.isEmpty() || pipes.get(pipes.size() - 1).x < 350) {
            addPipe(false);
        }
    }

    private void checkCollisions() {
        // Ground collision - with some margin
        if (birdY >= 600 - birdheight - 40) {
            gameOver = true;
            birdY = 600 - birdheight - 40;
            return;
        }

        // Ceiling collision - very soft
        if (birdY <= 5) {
            birdY = 5;
            birdvelocity = 0;
        }

        // Bird rectangle with SMALLER collision box (easier to pass)
        Rectangle birdRect = new Rectangle(birdX + 5, birdY + 5, birdwidth - 10, birdheight - 10);

        // Pipe collisions
        for (GamePipe pipe : pipes) {
            Rectangle pipeRect = new Rectangle(pipe.x, pipe.y, pipe.width, pipe.height);
            if (birdRect.intersects(pipeRect)) {
                gameOver = true;
                return;
            }
        }
    }

    private void updateScore() {
        for (GamePipe pipe : pipes) {
            // Check if bird passed a pipe (top pipe only)
            if (pipe.y == 0 && birdX > pipe.x + pipewidth && !pipe.passed) {
                score++;
                pipe.passed = true;
                if (score > highScore) {
                    highScore = score;
                }
                break;
            }
        }
    }

    private void resetGame() {
        birdX = 100;
        birdY = 250;
        birdvelocity = 0;
        score = 0;
        gameOver = false;
        gameStarted = false;

        pipes.clear();
        // Start with fewer pipes
        addPipe(true);
        addPipe(true);

        timer.stop();
        repaint();
    }

    private void startGame() {
        if (!gameStarted) {
            gameStarted = true;
            timer.start();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw ground
        g2d.setColor(groundColor);
        g2d.fillRect(0, 560, 800, 40);

        // Draw pipes with EASY MODE visuals
        for (GamePipe pipe : pipes) {
            // Draw pipe with gradient (looks nicer)
            GradientPaint gradient = new GradientPaint(pipe.x, pipe.y, pipeColor.brighter(),
                    pipe.x + pipewidth, pipe.y + pipe.height, pipeColor.darker());
            g2d.setPaint(gradient);
            g2d.fillRect(pipe.x, pipe.y, pipe.width, pipe.height);

            // Draw pipe caps
            g2d.setColor(pipeColor.darker());
            g2d.fillRect(pipe.x - 5, pipe.y, pipe.width + 6, 12);
            if (pipe.y > 0) {
                g2d.fillRect(pipe.x - 3, pipe.y + pipe.height - 15, pipe.width + 6, 15);
            }

            // Draw BIG gap indicator (visual helper)
            if (pipe.y == 0) {
                g2d.setColor(new Color(255, 255, 0, 100)); // Semi-transparent yellow
                int gapTop = pipe.height;
                int gapBottom = pipe.height + pipegap;
                g2d.fillRect(pipe.x, gapTop, pipewidth, pipegap);

                // Draw center line
                g2d.setColor(Color.GREEN);
                g2d.setStroke(new BasicStroke(3));
                int gapCenterY = pipe.height + pipegap/2;
                g2d.drawLine(pipe.x, gapCenterY, pipe.x + pipewidth, gapCenterY);
            }
        }

        // Draw bird with happy face (it's easier now!)
        g2d.setColor(birdColor);
        g2d.fillOval(birdX, birdY, birdwidth, birdheight);

        // Draw wing
        g2d.setColor(birdColor.darker());
        int wingY = (int) (birdY + Math.sin(System.currentTimeMillis() / 100.0) * 5);
        g2d.fillArc(birdX + 5, wingY + 5, birdwidth - 10, birdheight - 10, 0, 180);

        // Draw happy eye
        g2d.setColor(Color.BLACK);
        g2d.fillOval(birdX + birdwidth - 15, birdY + 8, 8, 8);
        g2d.setColor(Color.WHITE);
        g2d.fillOval(birdX + birdwidth - 14, birdY + 9, 2, 2);

        // Draw smile (happy bird!)
        g2d.setColor(Color.BLACK);
        g2d.drawArc(birdX + 10, birdY + 15, 15, 10, 0, -180);

        // Draw beak
        g2d.setColor(Color.ORANGE);
        int[] xPoints = {birdX + birdwidth - 5, birdX + birdwidth + 10, birdX + birdwidth - 5};
        int[] yPoints = {birdY + birdheight/2, birdY + birdheight/2, birdY + birdheight/2 + 5};
        g2d.fillPolygon(xPoints, yPoints, 3);

        // Draw score with celebration
        g2d.setFont(new Font("Arial", Font.BOLD, 36));

        // Score shadow
        g2d.setColor(Color.BLACK);
        g2d.drawString("Score: " + score, 27, 52);
        g2d.drawString("High: " + highScore, 627, 52);

        // Score text
        g2d.setColor(textColor);
        g2d.drawString("Score: " + score, 25, 50);
        g2d.drawString("High: " + highScore, 625, 50);

        // Draw EASY MODE banner
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.setColor(Color.GREEN);
        g2d.drawString("", 320, 30);

        // Draw game info
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.setColor(Color.YELLOW);
        g2d.drawString("HUGE Gap: " + pipegap + "px", 25, 580);
        g2d.drawString("Slow Speed", 350, 580);
        g2d.drawString("Happy Bird! 😊", 600, 580);

        // Game over or start screen
        if (gameOver) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, 800, 600);

            g2d.setFont(new Font("Arial", Font.BOLD, 48));

            // Game Over shadow
            g2d.setColor(Color.BLACK);
            g2d.drawString("GAME OVER", 203, 253);

            // Game Over text
            g2d.setColor(Color.RED);
            g2d.drawString("GAME OVER", 200, 250);

            // Score with celebration
            g2d.setFont(new Font("Arial", Font.BOLD, 32));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Score: " + score, 300, 320);

            if (score > 5) {
               // g2d.setColor(Color.YELLOW);
                g2d.drawString("🎉 GREAT JOB! 🎉", 280, 360);
            }

            // Restart instructions
            g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            g2d.setColor(Color.GREEN);
            g2d.drawString("Press SPACE to restart", 280, 420);
        } else if (!gameStarted) {
            // Start screen
            g2d.setFont(new Font("Arial", Font.BOLD, 48));

            // Title shadow
            g2d.setColor(Color.BLACK);
            g2d.drawString("FLAPPY BIRD", 250, 250);

            // Title text
            g2d.setColor(Color.YELLOW);
            g2d.drawString("FLAPPY BIRD", 251, 253);

            // EASY MODE subtitle
            g2d.setFont(new Font("Arial", Font.BOLD, 28));
            g2d.setColor(Color.GREEN);
            g2d.drawString("", 270, 300);

            // Instructions
            g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Press SPACE to start", 280, 350);
            g2d.drawString("Press SPACE to flap", 280, 390);

            // Game settings info - EASY MODE FEATURES
            g2d.setFont(new Font("Arial", Font.PLAIN, 18));
            g2d.setColor(Color.CYAN);
            g2d.drawString("",  250, 440);
            //g2d.drawString(250, 470);
            //g2d.drawString( 250, 500);
            //g2d.drawString( 250, 530);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameStarted && !gameOver) {
            // Apply gravity (softer)
            birdvelocity += birdgravity;
            birdY += birdvelocity;

            // Move pipes (slower)
            movePipes();

            // Check collisions (easier)
            checkCollisions();

            // Update score
            updateScore();

            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!gameStarted) {
                startGame();
            } else if (gameOver) {
                resetGame();
            } else {
                birdvelocity = jump; // Balanced jump
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_R && gameOver) {
            resetGame();
        }

        // Cheat code: Press 'C' for super easy mode (even bigger gap temporarily)
        if (e.getKeyCode() == KeyEvent.VK_C && gameStarted && !gameOver) {
            // Temporarily make gap visible as even bigger (visual cheat)
            JOptionPane.showMessageDialog(this, "Cheat activated! Gap increased!");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
}

// Custom GamePipe class
class GamePipe {
    int x, y, width, height;
    boolean passed = false;

    GamePipe(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}