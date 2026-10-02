package Kamino_Puzzles;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class KaminoGUI extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;

    // Game Customization State
    private int selectedSize = 7;
    private String[][] currentGrid;
    private KaminoPuzzleSolver.Point startPoint;
    private KaminoPuzzleSolver.Point endPoint;
    private int endValue;

    // Game Play State
    private List<KaminoPuzzleSolver.Point> playerPath = new ArrayList<>();
    private JPanel boardPanel;
    private KaminoPuzzleSolver.Point flashRedPoint = null;
    private javax.swing.Timer flashTimer;

    // Palette Colors
    private static final Color BG_DARK = new Color(24, 26, 31);
    private static final Color PANEL_BG = new Color(33, 37, 43);
    private static final Color ACCENT_BLUE = new Color(74, 144, 226);
    private static final Color PATH_GREEN = new Color(76, 175, 80);
    private static final Color TARGET_YELLOW = new Color(255, 214, 0);
    private static final Color ERROR_RED = new Color(239, 83, 80);
    private static final Color TILE_DEFAULT = new Color(44, 49, 58);
    private static final Color TEXT_LIGHT = new Color(230, 235, 245);

    public KaminoGUI() {
        setTitle("Kamino Puzzle Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 850);
        setMinimumSize(new Dimension(600, 600));
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        mainContainer.add(createHomeScreen(), "HOME");
        mainContainer.add(createCustomizationScreen(), "CUSTOMIZE");
        mainContainer.add(createGameScreenWrapper(), "GAME");

        add(mainContainer);
        cardLayout.show(mainContainer, "HOME");
    }

    // ==========================================================
    // 1. HOME SCREEN
    // ==========================================================
    private JPanel createHomeScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_DARK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new java.awt.Insets(15, 15, 15, 15);

        JLabel titleLabel = new JLabel("KAMINO");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 72));
        titleLabel.setForeground(ACCENT_BLUE);
        gbc.gridy = 0;
        panel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("A Path Finding Puzzle of Numerical Gradients");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        subtitleLabel.setForeground(TEXT_LIGHT);
        gbc.gridy = 1;
        panel.add(subtitleLabel, gbc);

        JButton playBtn = createStyledButton("PLAY", 200, 55, ACCENT_BLUE);
        playBtn.addActionListener(e -> cardLayout.show(mainContainer, "CUSTOMIZE"));
        gbc.gridy = 2;
        gbc.insets = new java.awt.Insets(40, 15, 15, 15);
        panel.add(playBtn, gbc);

        return panel;
    }

    // ==========================================================
    // 2. CUSTOMIZATION SCREEN
    // ==========================================================
    private JPanel createCustomizationScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_DARK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new java.awt.Insets(15, 15, 15, 15);

        JLabel headerLabel = new JLabel("Custom Game Settings");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        headerLabel.setForeground(TEXT_LIGHT);
        gbc.gridy = 0;
        panel.add(headerLabel, gbc);

        JPanel settingsBox = new JPanel(new GridLayout(3, 1, 10, 15));
        settingsBox.setBackground(PANEL_BG);
        settingsBox.setBorder(new EmptyBorder(25, 35, 25, 35));

        JLabel sizeLabel = new JLabel("Board Dimension: 7 x 7", SwingConstants.CENTER);
        sizeLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        sizeLabel.setForeground(TEXT_LIGHT);
        settingsBox.add(sizeLabel);

        JSlider sizeSlider = new JSlider(2, 75, selectedSize);
        sizeSlider.setBackground(PANEL_BG);
        sizeSlider.setForeground(TEXT_LIGHT);
        sizeSlider.setMajorTickSpacing(15);
        sizeSlider.setMinorTickSpacing(5);
        sizeSlider.setPaintTicks(true);
        sizeSlider.setPaintLabels(true);

        sizeSlider.addChangeListener(e -> {
            selectedSize = sizeSlider.getValue();
            sizeLabel.setText("Board Dimension: " + selectedSize + " x " + selectedSize);
        });
        settingsBox.add(sizeSlider);

        JLabel noteLabel = new JLabel("Rule: Up = Higher, Down = Lower, Left/Right = Equal", SwingConstants.CENTER);
        noteLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        noteLabel.setForeground(Color.LIGHT_GRAY);
        settingsBox.add(noteLabel);

        gbc.gridy = 1;
        panel.add(settingsBox, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);

        JButton backBtn = createStyledButton("Back", 140, 45, new Color(90, 98, 110));
        backBtn.addActionListener(e -> cardLayout.show(mainContainer, "HOME"));
        btnPanel.add(backBtn);

        JButton startBtn = createStyledButton("Start Game", 180, 45, ACCENT_BLUE);
        startBtn.addActionListener(e -> startNewGame(selectedSize));
        btnPanel.add(startBtn);

        gbc.gridy = 2;
        panel.add(btnPanel, gbc);

        return panel;
    }

    // ==========================================================
    // 3. GAME SCREEN WRAPPER
    // ==========================================================
    private JPanel gameScreenPanel;
    private JLabel statusLabel;

    private JPanel createGameScreenWrapper() {
        gameScreenPanel = new JPanel(new BorderLayout());
        gameScreenPanel.setBackground(BG_DARK);

        // Header Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(PANEL_BG);
        topBar.setBorder(new EmptyBorder(12, 20, 12, 20));

        statusLabel = new JLabel("Start your path from START");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        statusLabel.setForeground(TEXT_LIGHT);
        topBar.add(statusLabel, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        JButton resetPathBtn = createStyledButton("Clear Path", 120, 35, new Color(100, 110, 120));
        resetPathBtn.addActionListener(e -> resetPlayerPath());
        controls.add(resetPathBtn);

        JButton menuBtn = createStyledButton("Menu", 90, 35, new Color(70, 75, 85));
        menuBtn.addActionListener(e -> cardLayout.show(mainContainer, "HOME"));
        controls.add(menuBtn);

        topBar.add(controls, BorderLayout.EAST);
        gameScreenPanel.add(topBar, BorderLayout.NORTH);

        boardPanel = new BoardCanvas();
        JScrollPane scrollPane = new JScrollPane(boardPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        gameScreenPanel.add(scrollPane, BorderLayout.CENTER);

        return gameScreenPanel;
    }

    // ==========================================================
    // GAME INITIALIZATION & GENERATION
    // ==========================================================
    private void startNewGame(int size) {
        this.selectedSize = size;
        this.currentGrid = KaminoPuzzleGenerator.generate(size, size, true);

        // Extract markers
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                String token = currentGrid[r][c].trim().toUpperCase();
                if (token.equals("S")) {
                    startPoint = new KaminoPuzzleSolver.Point(r, c);
                } else if (token.startsWith("E:") || token.equals("E")) {
                    endPoint = new KaminoPuzzleSolver.Point(r, c);
                    if (token.startsWith("E:")) {
                        endValue = Integer.parseInt(token.substring(2));
                    } else {
                        endValue = 2;
                    }
                }
            }
        }

        resetPlayerPath();
        cardLayout.show(mainContainer, "GAME");
        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private void resetPlayerPath() {
        playerPath.clear();
        playerPath.add(startPoint);
        statusLabel.setText("Click or drag from START to the yellow END square.");
        boardPanel.repaint();
    }

    private Integer getTileNumericValue(int r, int c) {
        String token = currentGrid[r][c].trim().toUpperCase();
        if (token.equals("S")) return null;
        if (token.startsWith("E:")) return Integer.parseInt(token.substring(2));
        if (token.equals("E")) return 2;
        return Integer.parseInt(token);
    }

    // ==========================================================
    // 4. MOVE VALIDATION & PATH DRAGGING
    // ==========================================================
    private void attemptMoveTo(int r, int c) {
        if (r < 0 || r >= selectedSize || c < 0 || c >= selectedSize) return;
        KaminoPuzzleSolver.Point target = new KaminoPuzzleSolver.Point(r, c);

        // If clicking the current head, do nothing
        KaminoPuzzleSolver.Point currentHead = playerPath.get(playerPath.size() - 1);
        if (currentHead.equals(target)) return;

        // Undo move: if user steps or drags back onto immediate previous tile
        if (playerPath.size() > 1 && playerPath.get(playerPath.size() - 2).equals(target)) {
            playerPath.remove(playerPath.size() - 1);
            boardPanel.repaint();
            return;
        }

        // Must be adjacent (Manhattan distance == 1)
        int dist = Math.abs(currentHead.row - target.row) + Math.abs(currentHead.col - target.col);
        if (dist != 1) {
            triggerIllegalFlash(target);
            return;
        }

        // Cannot revisit a previously covered tile (no self-intersecting loops)
        if (playerPath.contains(target)) {
            triggerIllegalFlash(target);
            return;
        }

        // Check Kamino rules
        Integer currVal = currentHead.equals(startPoint) ? null : getTileNumericValue(currentHead.row, currentHead.col);
        Integer nextVal = getTileNumericValue(target.row, target.col);

        boolean validMove = false;
        if (currentHead.equals(startPoint)) {
            // First step out of START is unrestricted
            validMove = true;
        } else if (currVal != null && nextVal != null) {
            if (target.row < currentHead.row && nextVal > currVal) {        // UP: higher
                validMove = true;
            } else if (target.row > currentHead.row && nextVal < currVal) {  // DOWN: lower
                validMove = true;
            } else if (target.row == currentHead.row && nextVal.equals(currVal)) { // LEFT/RIGHT: equal
                validMove = true;
            }
        }

        if (validMove) {
            playerPath.add(target);
            boardPanel.repaint();

            // Check Win Condition
            if (target.equals(endPoint)) {
                handleGameWon();
            }
        } else {
            triggerIllegalFlash(target);
        }
    }

    private void triggerIllegalFlash(KaminoPuzzleSolver.Point pt) {
        flashRedPoint = pt;
        boardPanel.repaint();

        if (flashTimer != null && flashTimer.isRunning()) {
            flashTimer.stop();
        }
        flashTimer = new javax.swing.Timer(300, e -> {
            flashRedPoint = null;
            boardPanel.repaint();
        });
        flashTimer.setRepeats(false);
        flashTimer.start();
    }

    // ==========================================================
    // 5. VICTORY DIALOG & POST-GAME FLOW
    // ==========================================================
    private void handleGameWon() {
        statusLabel.setText("PUZZLE COMPLETED!");
        SwingUtilities.invokeLater(() -> {
            String[] options = {"Play Again (Same Settings)", "Main Menu"};
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Congratulations! You solved the " + selectedSize + "x" + selectedSize + " puzzle in " + (playerPath.size() - 1) + " moves!",
                    "Puzzle Solved!",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice == JOptionPane.YES_OPTION) {
                startNewGame(selectedSize);
            } else {
                cardLayout.show(mainContainer, "HOME");
            }
        });
    }

    // ==========================================================
    // BOARD DRAWING & MOUSE GESTURES (CLICK + DRAG)
    // ==========================================================
    private class BoardCanvas extends JPanel {
        private final int BASE_CELL_SIZE = 55;

        public BoardCanvas() {
            setBackground(BG_DARK);

            MouseAdapter mouseHandler = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    handleMouseInput(e.getPoint());
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    handleMouseInput(e.getPoint());
                }
            };
            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
        }

        private int getCellSize() {
            if (selectedSize <= 10) return 60;
            if (selectedSize <= 25) return 40;
            if (selectedSize <= 50) return 26;
            return 20; // 75x75
        }

        @Override
        public Dimension getPreferredSize() {
            int cellSize = getCellSize();
            int totalDim = cellSize * selectedSize + 40;
            return new Dimension(totalDim, totalDim);
        }

        private void handleMouseInput(java.awt.Point mousePos) {
            int cellSize = getCellSize();
            int startX = (getWidth() - (cellSize * selectedSize)) / 2;
            int startY = (getHeight() - (cellSize * selectedSize)) / 2;
            startX = Math.max(20, startX);
            startY = Math.max(20, startY);

            int col = (mousePos.x - startX) / cellSize;
            int row = (mousePos.y - startY) / cellSize;

            if (row >= 0 && row < selectedSize && col >= 0 && col < selectedSize) {
                attemptMoveTo(row, col);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (currentGrid == null) return;

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int cellSize = getCellSize();
            int startX = Math.max(20, (getWidth() - (cellSize * selectedSize)) / 2);
            int startY = Math.max(20, (getHeight() - (cellSize * selectedSize)) / 2);

            Set<KaminoPuzzleSolver.Point> pathSet = new HashSet<>(playerPath);

            // 1. Draw tiles
            for (int r = 0; r < selectedSize; r++) {
                for (int c = 0; c < selectedSize; c++) {
                    int x = startX + c * cellSize;
                    int y = startY + r * cellSize;
                    KaminoPuzzleSolver.Point cellPoint = new KaminoPuzzleSolver.Point(r, c);

                    // Determine background color
                    if (cellPoint.equals(flashRedPoint)) {
                        g2d.setColor(ERROR_RED);
                    } else if (pathSet.contains(cellPoint)) {
                        g2d.setColor(PATH_GREEN);
                    } else if (cellPoint.equals(endPoint)) {
                        g2d.setColor(TARGET_YELLOW);
                    } else {
                        g2d.setColor(TILE_DEFAULT);
                    }

                    g2d.fillRoundRect(x + 1, y + 1, cellSize - 2, cellSize - 2, 6, 6);
                    g2d.setColor(new Color(20, 22, 26));
                    g2d.drawRoundRect(x + 1, y + 1, cellSize - 2, cellSize - 2, 6, 6);

                    // Determine text
                    String text;
                    Color textColor = TEXT_LIGHT;

                    if (cellPoint.equals(startPoint)) {
                        text = "START";
                        textColor = Color.WHITE;
                    } else if (cellPoint.equals(endPoint)) {
                        text = String.valueOf(endValue);
                        textColor = Color.BLACK; // black on yellow for strong contrast
                    } else {
                        text = currentGrid[r][c];
                    }

                    // Adaptive font sizing based on grid scale
                    int fontSize = Math.max(9, (int) (cellSize * (text.length() > 2 ? 0.30 : 0.45)));
                    g2d.setFont(new Font("SansSerif", Font.BOLD, fontSize));
                    g2d.setColor(textColor);

                    FontMetrics fm = g2d.getFontMetrics();
                    int textX = x + (cellSize - fm.stringWidth(text)) / 2;
                    int textY = y + ((cellSize - fm.getHeight()) / 2) + fm.getAscent();
                    g2d.drawString(text, textX, textY);
                }
            }

            // 2. Draw connecting stroke line through current path
            if (playerPath.size() > 1) {
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.setStroke(new BasicStroke(Math.max(2, cellSize / 8), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < playerPath.size() - 1; i++) {
                    KaminoPuzzleSolver.Point p1 = playerPath.get(i);
                    KaminoPuzzleSolver.Point p2 = playerPath.get(i + 1);

                    int x1 = startX + p1.col * cellSize + cellSize / 2;
                    int y1 = startY + p1.row * cellSize + cellSize / 2;
                    int x2 = startX + p2.col * cellSize + cellSize / 2;
                    int y2 = startY + p2.row * cellSize + cellSize / 2;

                    g2d.drawLine(x1, y1, x2, y2);
                }
            }
        }
    }

    // ==========================================================
    // UI COMPONENT STYLING UTILITIES
    // ==========================================================
    private JButton createStyledButton(String text, int width, int height, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(width, height));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover Effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        return btn;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            KaminoGUI gui = new KaminoGUI();
            gui.setVisible(true);
        });
    }
}