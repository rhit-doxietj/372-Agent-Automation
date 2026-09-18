import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class RectoGUI extends JFrame {

    public enum GameMode { CLASSIC, TIME_TRIAL, HARDCORE, FOG_OF_WAR }
    public enum BoardSize { SMALL, MEDIUM, LARGE, CUSTOM }

    // Navigation and state
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);

    private GameMode selectedMode = GameMode.CLASSIC;
    private BoardSize selectedSize = BoardSize.MEDIUM;
    private String selectedDifficulty = "Medium";
    private int customRows = 8;
    private int customCols = 8;

    // Game Board State
    private int lives = 3;
    private int timerSeconds = 0;
    private Timer gameTimer;
    private int[][] grid;
    private Recto solver;
    private final List<Recto.Rect> playerRects = new ArrayList<>();
    private boolean[][] revealedCells;
    private boolean isGameOver = false;
    private boolean showSolutionOverlay = false;

    // Drag selection tracking
    private Point dragStart = null;
    private Point dragEnd = null;

    // Components
    private JLabel timerLabel;
    private JLabel livesLabel;
    private BoardPanel boardPanel;

    public RectoGUI() {
        setTitle("Recto Puzzle Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        mainPanel.add(createHomeScreen(), "HOME");
        mainPanel.add(createGameModeScreen(), "MODES");
        mainPanel.add(createSettingsScreen(), "SETTINGS");
        mainPanel.add(createGamePlayScreen(), "GAME");

        add(mainPanel);
        cardLayout.show(mainPanel, "HOME");
    }

    // =========================================================================
    // 1. HOME SCREEN
    // =========================================================================
    private JPanel createHomeScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 32, 40));

        // Top right bar for rules button
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton rulesBtn = new JButton("Rules");
        styleButton(rulesBtn, new Color(70, 130, 180));
        rulesBtn.addActionListener(e -> showRulesDialog());
        topBar.add(rulesBtn);
        panel.add(topBar, BorderLayout.NORTH);

        // Center content
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel title = new JLabel("GRIDLOCK");
        title.setFont(new Font("SansSerif", Font.BOLD, 64));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Grid Partitioning Logic Game");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 18));
        subtitle.setForeground(new Color(180, 180, 180));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton startBtn = new JButton("Start Game");
        JButton settingsBtn = new JButton("Settings");
        JButton quitBtn = new JButton("Quit");

        styleButton(startBtn, new Color(46, 139, 87));
        styleButton(settingsBtn, new Color(100, 100, 100));
        styleButton(quitBtn, new Color(178, 34, 34));

        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        settingsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        quitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        startBtn.addActionListener(e -> cardLayout.show(mainPanel, "MODES"));
        settingsBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Settings: Audio and Display options can be adjusted here.", "Settings", JOptionPane.INFORMATION_MESSAGE));
        quitBtn.addActionListener(e -> System.exit(0));

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(title);
        centerPanel.add(subtitle);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        centerPanel.add(startBtn);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        centerPanel.add(settingsBtn);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        centerPanel.add(quitBtn);
        centerPanel.add(Box.createVerticalGlue());

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    private void showRulesDialog() {
        String rulesText = "RECTO RULES:\n\n" +
                "1. Divide the grid into non-overlapping rectangular boxes.\n" +
                "2. Each box must enclose EXACTLY ONE clue number.\n" +
                "3. The dimensions of the box (Height + Width) must equal the clue number (h + w = Clue).\n" +
                "4. All cells on the grid must be covered to complete the puzzle.\n\n" +
                "GAME MODES:\n" +
                "- Classic: Standard puzzle with 3 Lives.\n" +
                "- Time Trial: Solve against a scaling clock with 3 Lives.\n" +
                "- Hardcore: 1 mistake and it's Game Over!\n" +
                "- Fog of War: Board is obscured until you unlock adjacent areas.";
        JOptionPane.showMessageDialog(this, rulesText, "How to Play Recto", JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================================
    // 2. GAME MODE SELECTION SCREEN
    // =========================================================================
    private JPanel createGameModeScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(35, 37, 45));
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel header = new JLabel("Select Game Mode", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 32));
        header.setForeground(Color.WHITE);
        panel.add(header, BorderLayout.NORTH);

        JPanel modesGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        modesGrid.setOpaque(false);
        modesGrid.setBorder(new EmptyBorder(30, 0, 30, 0));

        JButton classicBtn = createModeCard("Classic", "Standard Recto rules with 3 Lives.", new Color(41, 128, 185));
        JButton timeTrialBtn = createModeCard("Time Trial", "3 Lives with a time limit scaling by difficulty.", new Color(211, 84, 0));
        JButton hardcoreBtn = createModeCard("Hardcore", "Single mistake equals instant failure (1 Life).", new Color(192, 57, 43));
        JButton fogBtn = createModeCard("Fog of War", "Grid is obscured until sections are revealed.", new Color(142, 68, 173));

        classicBtn.addActionListener(e -> selectMode(GameMode.CLASSIC));
        timeTrialBtn.addActionListener(e -> selectMode(GameMode.TIME_TRIAL));
        hardcoreBtn.addActionListener(e -> selectMode(GameMode.HARDCORE));
        fogBtn.addActionListener(e -> selectMode(GameMode.FOG_OF_WAR));

        modesGrid.add(classicBtn);
        modesGrid.add(timeTrialBtn);
        modesGrid.add(hardcoreBtn);
        modesGrid.add(fogBtn);

        panel.add(modesGrid, BorderLayout.CENTER);

        JButton backBtn = new JButton("Back");
        styleButton(backBtn, new Color(100, 100, 100));
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "HOME"));
        panel.add(backBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JButton createModeCard(String title, String desc, Color bg) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLbl.setForeground(Color.WHITE);

        JLabel descLbl = new JLabel("<html><center>" + desc + "</center></html>", SwingConstants.CENTER);
        descLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        descLbl.setForeground(new Color(230, 230, 230));

        button.add(titleLbl, BorderLayout.NORTH);
        button.add(descLbl, BorderLayout.CENTER);
        return button;
    }

    private void selectMode(GameMode mode) {
        this.selectedMode = mode;
        cardLayout.show(mainPanel, "SETTINGS");
    }

    // =========================================================================
    // 3. WORLD / BOARD SETTINGS SCREEN
    // =========================================================================
    private JPanel createSettingsScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(40, 44, 52));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("World & Board Settings", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        // Size Selector
        gbc.gridwidth = 1; gbc.gridy = 1; gbc.gridx = 0;
        JLabel sizeLbl = new JLabel("Board Size:");
        sizeLbl.setForeground(Color.WHITE);
        panel.add(sizeLbl, gbc);

        JComboBox<String> sizeCombo = new JComboBox<>(new String[]{"Small (5x5)", "Medium (8x8)", "Large (12x12)", "Custom"});
        sizeCombo.setSelectedIndex(1);
        gbc.gridx = 1;
        panel.add(sizeCombo, gbc);

        // Custom Bounds Inputs (Lower bound 0, Upper bound 100)
        JLabel rowLbl = new JLabel("Custom Rows (1-100):");
        rowLbl.setForeground(Color.GRAY);
        JSpinner rowSpinner = new JSpinner(new SpinnerNumberModel(8, 1, 100, 1));
        rowSpinner.setEnabled(false);

        JLabel colLbl = new JLabel("Custom Cols (1-100):");
        colLbl.setForeground(Color.GRAY);
        JSpinner colSpinner = new JSpinner(new SpinnerNumberModel(8, 1, 100, 1));
        colSpinner.setEnabled(false);

        gbc.gridy = 2; gbc.gridx = 0; panel.add(rowLbl, gbc);
        gbc.gridx = 1; panel.add(rowSpinner, gbc);
        gbc.gridy = 3; gbc.gridx = 0; panel.add(colLbl, gbc);
        gbc.gridx = 1; panel.add(colSpinner, gbc);

        sizeCombo.addActionListener(e -> {
            boolean isCustom = sizeCombo.getSelectedIndex() == 3;
            rowSpinner.setEnabled(isCustom);
            colSpinner.setEnabled(isCustom);
            rowLbl.setForeground(isCustom ? Color.WHITE : Color.GRAY);
            colLbl.setForeground(isCustom ? Color.WHITE : Color.GRAY);

            switch (sizeCombo.getSelectedIndex()) {
                case 0 -> selectedSize = BoardSize.SMALL;
                case 1 -> selectedSize = BoardSize.MEDIUM;
                case 2 -> selectedSize = BoardSize.LARGE;
                case 3 -> selectedSize = BoardSize.CUSTOM;
            }
        });

        // Difficulty Selector
        gbc.gridy = 4; gbc.gridx = 0;
        JLabel diffLbl = new JLabel("Difficulty:");
        diffLbl.setForeground(Color.WHITE);
        panel.add(diffLbl, gbc);

        JComboBox<String> diffCombo = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        diffCombo.setSelectedIndex(1);
        gbc.gridx = 1;
        panel.add(diffCombo, gbc);

        diffCombo.addActionListener(e -> selectedDifficulty = (String) diffCombo.getSelectedItem());

        // Confirm / Launch
        JButton launchBtn = new JButton("Confirm & Generate Board");
        styleButton(launchBtn, new Color(46, 139, 87));
        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(30, 10, 10, 10);
        panel.add(launchBtn, gbc);

        launchBtn.addActionListener(e -> {
            if (selectedSize == BoardSize.CUSTOM) {
                this.customRows = (int) rowSpinner.getValue();
                this.customCols = (int) colSpinner.getValue();
            }
            startNewGame();
        });

        return panel;
    }

    // =========================================================================
    // 4. GAMEPLAY SCREEN & LOGIC
    // =========================================================================
    private JPanel createGamePlayScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(25, 25, 30));

        // HUD Header
        JPanel hud = new JPanel(new BorderLayout());
        hud.setOpaque(false);
        hud.setBorder(new EmptyBorder(10, 20, 10, 20));

        livesLabel = new JLabel("Lives: 3");
        livesLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        livesLabel.setForeground(Color.RED);

        timerLabel = new JLabel("Time: 00:00");
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        timerLabel.setForeground(Color.CYAN);

        JButton menuBtn = new JButton("Main Menu");
        styleButton(menuBtn, new Color(100, 100, 100));
        menuBtn.addActionListener(e -> {
            if (gameTimer != null) gameTimer.stop();
            cardLayout.show(mainPanel, "HOME");
        });

        hud.add(livesLabel, BorderLayout.WEST);
        hud.add(timerLabel, BorderLayout.CENTER);
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        hud.add(menuBtn, BorderLayout.EAST);

        panel.add(hud, BorderLayout.NORTH);

        boardPanel = new BoardPanel();
        panel.add(boardPanel, BorderLayout.CENTER);

        return panel;
    }

    private void startNewGame() {
        int r = 8, c = 8;
        switch (selectedSize) {
            case SMALL -> { r = 5; c = 5; }
            case MEDIUM -> { r = 8; c = 8; }
            case LARGE -> { r = 12; c = 12; }
            case CUSTOM -> { r = customRows; c = customCols; }
        }

        // Initialize puzzle using existing RectoGenerator logic[cite: 2]
        this.grid = RectoGenerator.generate(r, c);
        this.solver = new Recto(grid);
        this.solver.solve(); // Solve once to establish reference solution

        this.playerRects.clear();
        this.isGameOver = false;
        this.showSolutionOverlay = false;

        // Lives Setup
        this.lives = (selectedMode == GameMode.HARDCORE) ? 1 : 3;
        livesLabel.setText("Lives: " + lives);

        // Fog of War Setup
        this.revealedCells = new boolean[r][c];
        if (selectedMode == GameMode.FOG_OF_WAR) {
            // Reveal initial top-left corner region
            for (int i = 0; i < Math.min(3, r); i++) {
                for (int j = 0; j < Math.min(3, c); j++) {
                    revealedCells[i][j] = true;
                }
            }
        } else {
            for (boolean[] row : revealedCells) Arrays.fill(row, true);
        }

        // Timer Setup
        if (gameTimer != null) gameTimer.stop();
        if (selectedMode == GameMode.TIME_TRIAL) {
            // Timer scales down as difficulty increases
            this.timerSeconds = switch (selectedDifficulty) {
                case "Easy" -> r * c * 4;
                case "Hard" -> r * c * 2;
                default -> r * c * 3;
            };
        } else {
            this.timerSeconds = 0;
        }

        updateTimerDisplay();
        gameTimer = new Timer(1000, e -> handleTimerTick());
        gameTimer.start();

        cardLayout.show(mainPanel, "GAME");
        boardPanel.repaint();
    }

    private void handleTimerTick() {
        if (isGameOver) return;

        if (selectedMode == GameMode.TIME_TRIAL) {
            timerSeconds--;
            if (timerSeconds <= 0) {
                triggerGameOver("Time Expired!");
            }
        } else {
            timerSeconds++;
        }
        updateTimerDisplay();
    }

    private void updateTimerDisplay() {
        int m = Math.abs(timerSeconds) / 60;
        int s = Math.abs(timerSeconds) % 60;
        timerLabel.setText(String.format("Time: %02d:%02d", m, s));
    }

    private void triggerGameOver(String reason) {
        this.isGameOver = true;
        this.showSolutionOverlay = true;
        if (gameTimer != null) gameTimer.stop();

        boardPanel.repaint();

        int result = JOptionPane.showOptionDialog(this,
                reason + "\nWould you like to try again or return to the main menu?",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new String[]{"Try Again", "Main Menu"},
                "Try Again");

        if (result == JOptionPane.YES_OPTION) {
            startNewGame();
        } else {
            cardLayout.show(mainPanel, "HOME");
        }
    }

    private void checkVictoryCondition() {
        int totalCells = grid.length * grid[0].length;
        int coveredCells = 0;
        for (Recto.Rect rect : playerRects) {
            coveredCells += (rect.r2 - rect.r1 + 1) * (rect.c2 - rect.c1 + 1);
        }

        if (coveredCells == totalCells) {
            if (gameTimer != null) gameTimer.stop();
            JOptionPane.showMessageDialog(this, "Congratulations! You solved the Recto puzzle!", "Victory", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(mainPanel, "HOME");
        }
    }

    // =========================================================================
    // 5. INTERACTIVE BOARD PANEL (DRAG & DROP RENDERER)
    // =========================================================================
    private class BoardPanel extends JPanel {
        private final int PADDING = 40;

        public BoardPanel() {
            setBackground(new Color(20, 22, 28));

            MouseAdapter adapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (isGameOver || grid == null) return;
                    Point cell = getCellAtPoint(e.getPoint());
                    if (cell != null) {
                        dragStart = cell;
                        dragEnd = cell;
                        repaint();
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (isGameOver || dragStart == null) return;
                    Point cell = getCellAtPoint(e.getPoint());
                    if (cell != null) {
                        dragEnd = cell;
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (isGameOver || dragStart == null || dragEnd == null) return;
                    validateAndPlaceRect(dragStart, dragEnd);
                    dragStart = null;
                    dragEnd = null;
                    repaint();
                }
            };

            addMouseListener(adapter);
            addMouseMotionListener(adapter);
        }

        private Point getCellAtPoint(Point p) {
            int rows = grid.length;
            int cols = grid[0].length;
            int cellSize = Math.min((getHeight() - 2 * PADDING) / rows, (getWidth() - 2 * PADDING) / cols);

            int startX = (getWidth() - cols * cellSize) / 2;
            int startY = (getHeight() - rows * cellSize) / 2;

            if (p.x < startX || p.y < startY) return null;
            int c = (p.x - startX) / cellSize;
            int r = (p.y - startY) / cellSize;

            if (r >= 0 && r < rows && c >= 0 && c < cols) {
                return new Point(r, c);
            }
            return null;
        }

        private void validateAndPlaceRect(Point p1, Point p2) {
            int r1 = Math.min(p1.x, p2.x);
            int r2 = Math.max(p1.x, p2.x);
            int c1 = Math.min(p1.y, p2.y);
            int c2 = Math.max(p1.y, p2.y);

            int h = r2 - r1 + 1;
            int w = c2 - c1 + 1;

            // Check clue enclosure count and sum rule validation[cite: 1]
            int clueCount = 0;
            int foundClueValue = -1;
            int foundClueId = -1;

            int clueIdTracker = 0;
            for (int r = 0; r < grid.length; r++) {
                for (int c = 0; c < grid[0].length; c++) {
                    if (grid[r][c] > 0) {
                        if (r >= r1 && r <= r2 && c >= c1 && c <= c2) {
                            clueCount++;
                            foundClueValue = grid[r][c];
                            foundClueId = clueIdTracker;
                        }
                        clueIdTracker++;
                    }
                }
            }

            // Check cell overlaps with existing player placements
            boolean overlaps = false;
            for (Recto.Rect existing : playerRects) {
                if (!(r2 < existing.r1 || r1 > existing.r2 || c2 < existing.c1 || c1 > existing.c2)) {
                    overlaps = true;
                    break;
                }
            }

            // Recto rule: Box must have exactly 1 clue, matching h + w = clue sum, with no overlaps[cite: 1]
            boolean isValid = (clueCount == 1) && ((h + w) == foundClueValue) && !overlaps;

            if (isValid) {
                playerRects.add(new Recto.Rect(foundClueId, r1, c1, r2, c2));

                // Clear Fog of War around completed box
                if (selectedMode == GameMode.FOG_OF_WAR) {
                    for (int r = Math.max(0, r1 - 1); r <= Math.min(grid.length - 1, r2 + 1); r++) {
                        for (int c = Math.max(0, c1 - 1); c <= Math.min(grid[0].length - 1, c2 + 1); c++) {
                            revealedCells[r][c] = true;
                        }
                    }
                }
                checkVictoryCondition();
            } else {
                // Deduct life on mistake
                lives--;
                livesLabel.setText("Lives: " + lives);
                if (lives <= 0) {
                    triggerGameOver("Out of Lives!");
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (grid == null) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int rows = grid.length;
            int cols = grid[0].length;
            int cellSize = Math.min((getHeight() - 2 * PADDING) / rows, (getWidth() - 2 * PADDING) / cols);

            int startX = (getWidth() - cols * cellSize) / 2;
            int startY = (getHeight() - rows * cellSize) / 2;

            // Render Cells
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int x = startX + c * cellSize;
                    int y = startY + r * cellSize;

                    if (selectedMode == GameMode.FOG_OF_WAR && !revealedCells[r][c]) {
                        g2.setColor(new Color(15, 15, 20));
                        g2.fillRect(x, y, cellSize, cellSize);
                        g2.setColor(new Color(30, 30, 40));
                        g2.drawRect(x, y, cellSize, cellSize);
                        continue;
                    }

                    g2.setColor(new Color(45, 48, 58));
                    g2.fillRect(x, y, cellSize, cellSize);
                    g2.setColor(new Color(60, 64, 76));
                    g2.drawRect(x, y, cellSize, cellSize);

                    // Render Clues
                    if (grid[r][c] > 0) {
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("SansSerif", Font.BOLD, cellSize / 2));
                        String valStr = String.valueOf(grid[r][c]);
                        FontMetrics fm = g2.getFontMetrics();
                        int tx = x + (cellSize - fm.stringWidth(valStr)) / 2;
                        int ty = y + (cellSize + fm.getAscent() - fm.getDescent()) / 2;
                        g2.drawString(valStr, tx, ty);
                    }
                }
            }

            // Render Player Rectangles
            g2.setStroke(new BasicStroke(3));
            for (Recto.Rect rect : playerRects) {
                int rx = startX + rect.c1 * cellSize;
                int ry = startY + rect.r1 * cellSize;
                int rw = (rect.c2 - rect.c1 + 1) * cellSize;
                int rh = (rect.r2 - rect.r1 + 1) * cellSize;

                g2.setColor(new Color(46, 204, 113, 80));
                g2.fillRect(rx, ry, rw, rh);
                g2.setColor(new Color(46, 204, 113));
                g2.drawRect(rx, ry, rw, rh);
            }

            // Render Active Drag Box Selection
            if (dragStart != null && dragEnd != null) {
                int r1 = Math.min(dragStart.x, dragEnd.x);
                int r2 = Math.max(dragStart.x, dragEnd.x);
                int c1 = Math.min(dragStart.y, dragEnd.y);
                int c2 = Math.max(dragStart.y, dragEnd.y);

                int dx = startX + c1 * cellSize;
                int dy = startY + r1 * cellSize;
                int dw = (c2 - c1 + 1) * cellSize;
                int dh = (r2 - r1 + 1) * cellSize;

                g2.setColor(new Color(52, 152, 219, 100));
                g2.fillRect(dx, dy, dw, dh);
                g2.setColor(new Color(52, 152, 219));
                g2.drawRect(dx, dy, dw, dh);
            }

            // Render Solution Overlay on Game Over
            if (showSolutionOverlay) {
                g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{6}, 0));
                g2.setColor(new Color(231, 76, 60));

                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {
                        int owner = solver.getCellOwner(r, c);
                        int x = startX + c * cellSize;
                        int y = startY + r * cellSize;

                        if (c == cols - 1 || solver.getCellOwner(r, c + 1) != owner) {
                            g2.drawLine(x + cellSize, y, x + cellSize, y + cellSize);
                        }
                        if (r == rows - 1 || solver.getCellOwner(r + 1, c) != owner) {
                            g2.drawLine(x, y + cellSize, x + cellSize, y + cellSize);
                        }
                    }
                }
            }
        }
    }

    // Helper Utility to style UI buttons
    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(200, 40));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new RectoGUI().setVisible(true);
        });
    }
}