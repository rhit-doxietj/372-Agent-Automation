package Kamino_Puzzles;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class KaminoGUI extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;

    // Game Mode State
    public enum GameMode { SOLO, RACE }
    private GameMode currentMode = GameMode.SOLO;

    // Customization State
    private int selectedSize = 7;

    // Player Board State
    private String[][] playerGrid;
    private KaminoPuzzleSolver.Point playerStartPoint;
    private KaminoPuzzleSolver.Point playerEndPoint;
    private int playerEndValue;
    private List<KaminoPuzzleSolver.Point> playerPath = new ArrayList<>();
    private List<KaminoPuzzleSolver.Point> animatedFlowPath = new ArrayList<>();
    private boolean isAnimatingFlow = false;
    private BoardCanvas playerBoardCanvas;
    private KaminoPuzzleSolver.Point flashRedPoint = null;
    private javax.swing.Timer flashTimer;

    // Ghost Board State (Race Mode)
    private String[][] ghostGrid;
    private KaminoPuzzleSolver.Point ghostStartPoint;
    private KaminoPuzzleSolver.Point ghostEndPoint;
    private int ghostEndValue;
    private List<KaminoPuzzleSolver.Point> ghostOptimalPath = new ArrayList<>();
    private List<KaminoPuzzleSolver.Point> ghostCurrentPath = new ArrayList<>();
    private BoardCanvas ghostBoardCanvas;
    private javax.swing.Timer ghostStepTimer;
    private int ghostStepIndex = 0;

    // UI Panels & Labels
    private JPanel boardsContainer;
    private JLabel statusLabel;
    private JLabel raceProgressLabel;

    // Palette Colors
    private static final Color BG_DARK = new Color(24, 26, 31);
    private static final Color PANEL_BG = new Color(33, 37, 43);
    private static final Color ACCENT_BLUE = new Color(74, 144, 226);
    private static final Color ACCENT_PURPLE = new Color(171, 71, 188);
    private static final Color PATH_GREEN = new Color(76, 175, 80);
    private static final Color TARGET_YELLOW = new Color(255, 214, 0);
    private static final Color ERROR_RED = new Color(239, 83, 80);
    private static final Color TILE_DEFAULT = new Color(44, 49, 58);
    private static final Color TEXT_LIGHT = new Color(230, 235, 245);

    public KaminoGUI() {
        setTitle("Kamino.IO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 860);
        setMinimumSize(new Dimension(750, 650));
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        mainContainer.add(createHomeScreen(), "HOME");
        mainContainer.add(createRulesScreen(), "RULES");
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
        gbc.insets = new Insets(10, 15, 10, 15);

        JLabel titleLabel = new JLabel("KAMINO.IO");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 75));
        titleLabel.setForeground(ACCENT_BLUE);
        gbc.gridy = 0;
        panel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("A Numerical Gradient Pathfinding Game");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        subtitleLabel.setForeground(TEXT_LIGHT);
        gbc.gridy = 1;
        panel.add(subtitleLabel, gbc);

        // Mode: Solo
        JButton soloBtn = createStyledButton("SOLO PLAY", 240, 48, ACCENT_BLUE);
        soloBtn.addActionListener(e -> {
            currentMode = GameMode.SOLO;
            cardLayout.show(mainContainer, "CUSTOMIZE");
        });
        gbc.gridy = 2;
        gbc.insets = new Insets(30, 15, 10, 15);
        panel.add(soloBtn, gbc);

        // Mode: Race the Solver
        JButton raceBtn = createStyledButton("RACE THE SOLVER", 240, 48, ACCENT_PURPLE);
        raceBtn.addActionListener(e -> {
            currentMode = GameMode.RACE;
            cardLayout.show(mainContainer, "CUSTOMIZE");
        });
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 15, 10, 15);
        panel.add(raceBtn, gbc);

        // Rules
        JButton rulesBtn = createStyledButton("RULES", 240, 48, new Color(90, 100, 115));
        rulesBtn.addActionListener(e -> cardLayout.show(mainContainer, "RULES"));
        gbc.gridy = 4;
        panel.add(rulesBtn, gbc);

        return panel;
    }

    // ==========================================================
    // 2. RULES SCREEN
    // ==========================================================
    private JPanel createRulesScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_DARK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(12, 20, 12, 20);

        JLabel header = new JLabel("HOW TO PLAY");
        header.setFont(new Font("SansSerif", Font.BOLD, 38));
        header.setForeground(TEXT_LIGHT);
        gbc.gridy = 0;
        panel.add(header, gbc);

        JPanel contentBox = new JPanel(new GridLayout(6, 1, 10, 14));
        contentBox.setBackground(PANEL_BG);
        contentBox.setBorder(new EmptyBorder(25, 35, 25, 35));

        contentBox.add(createRuleLine("1. Goal", "Navigate from START to the Yellow TARGET square."));
        contentBox.add(createRuleLine("2. Upwards [ ^ ]", "You can only step UP if the destination number is STRICTLY HIGHER."));
        contentBox.add(createRuleLine("3. Downwards [ v ]", "You can only step DOWN if the destination number is STRICTLY LOWER."));
        contentBox.add(createRuleLine("4. Sideways [ < > ]", "You can only step LEFT or RIGHT if the destination number is EQUAL."));
        contentBox.add(createRuleLine("5. Start Move", "The first move stepping off START is free in any direction."));
        contentBox.add(createRuleLine("6. Race Mode", "Race against an AI ghost running on a separate puzzle board!"));

        gbc.gridy = 1;
        panel.add(contentBox, gbc);

        JButton backBtn = createStyledButton("Back to Menu", 180, 45, ACCENT_BLUE);
        backBtn.addActionListener(e -> cardLayout.show(mainContainer, "HOME"));
        gbc.gridy = 2;
        panel.add(backBtn, gbc);

        return panel;
    }

    private JPanel createRuleLine(String title, String desc) {
        JPanel line = new JPanel(new BorderLayout(15, 0));
        line.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLbl.setForeground(ACCENT_BLUE);
        titleLbl.setPreferredSize(new Dimension(190, 24));

        JLabel descLbl = new JLabel(desc);
        descLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        descLbl.setForeground(TEXT_LIGHT);

        line.add(titleLbl, BorderLayout.WEST);
        line.add(descLbl, BorderLayout.CENTER);
        return line;
    }

    // ==========================================================
    // 3. CUSTOMIZATION SCREEN
    // ==========================================================
    private JPanel createCustomizationScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_DARK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(15, 15, 15, 15);

        JLabel headerLabel = new JLabel("Game Setup");
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

        JLabel modeInfoLabel = new JLabel("Mode: Selected on Home Menu", SwingConstants.CENTER);
        modeInfoLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        modeInfoLabel.setForeground(Color.LIGHT_GRAY);
        settingsBox.add(modeInfoLabel);

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
    // 4. GAME SCREEN WRAPPER
    // ==========================================================
    private JPanel createGameScreenWrapper() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_DARK);

        // Header Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(PANEL_BG);
        topBar.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel labelGroup = new JPanel(new GridLayout(2, 1, 0, 4));
        labelGroup.setOpaque(false);

        statusLabel = new JLabel("Start your path from START");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        statusLabel.setForeground(TEXT_LIGHT);
        labelGroup.add(statusLabel);

        raceProgressLabel = new JLabel("");
        raceProgressLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        raceProgressLabel.setForeground(new Color(200, 205, 215));
        labelGroup.add(raceProgressLabel);

        topBar.add(labelGroup, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        JButton resetPathBtn = createStyledButton("Clear Path", 120, 35, new Color(100, 110, 120));
        resetPathBtn.addActionListener(e -> {
            if (!isAnimatingFlow) resetPlayerPath();
        });
        controls.add(resetPathBtn);

        JButton menuBtn = createStyledButton("Menu", 90, 35, new Color(70, 75, 85));
        menuBtn.addActionListener(e -> {
            stopGhostTimer();
            cardLayout.show(mainContainer, "HOME");
        });
        controls.add(menuBtn);

        topBar.add(controls, BorderLayout.EAST);
        wrapper.add(topBar, BorderLayout.NORTH);

        // Center Container that dynamically switches between Solo and Race Layouts
        boardsContainer = new JPanel(new BorderLayout());
        boardsContainer.setBackground(BG_DARK);
        wrapper.add(boardsContainer, BorderLayout.CENTER);

        return wrapper;
    }

    // ==========================================================
    // GAME INITIALIZATION & RUNTIME
    // ==========================================================
    private void startNewGame(int size) {
        stopGhostTimer();
        this.selectedSize = size;
        this.isAnimatingFlow = false;
        this.animatedFlowPath.clear();

        // 1. Generate Player Puzzle Board
        this.playerGrid = KaminoPuzzleGenerator.generate(size, size, true);
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                String token = playerGrid[r][c].trim().toUpperCase();
                if (token.equals("S")) {
                    playerStartPoint = new KaminoPuzzleSolver.Point(r, c);
                } else if (token.startsWith("E:") || token.equals("E")) {
                    playerEndPoint = new KaminoPuzzleSolver.Point(r, c);
                    playerEndValue = token.startsWith("E:") ? Integer.parseInt(token.substring(2)) : 2;
                }
            }
        }
        playerPath.clear();
        playerPath.add(playerStartPoint);

        playerBoardCanvas = new BoardCanvas(false);
        JScrollPane playerScroll = createScrollPane(playerBoardCanvas);

        // 2. Setup Boards Layout
        boardsContainer.removeAll();

        if (currentMode == GameMode.SOLO) {
            raceProgressLabel.setText("Solo Mode: Find the path to the yellow end square.");
            boardsContainer.add(playerScroll, BorderLayout.CENTER);
        } else {
            // RACE MODE: Generate a SECOND, completely separate board for Ghost
            this.ghostGrid = KaminoPuzzleGenerator.generate(size, size, true);
            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    String token = ghostGrid[r][c].trim().toUpperCase();
                    if (token.equals("S")) {
                        ghostStartPoint = new KaminoPuzzleSolver.Point(r, c);
                    } else if (token.startsWith("E:") || token.equals("E")) {
                        ghostEndPoint = new KaminoPuzzleSolver.Point(r, c);
                        ghostEndValue = token.startsWith("E:") ? Integer.parseInt(token.substring(2)) : 2;
                    }
                }
            }

            // Compute optimal path for Ghost
            KaminoPuzzleSolver ghostSolver = new KaminoPuzzleSolver(ghostGrid);
            ghostOptimalPath = ghostSolver.solve();
            ghostCurrentPath.clear();
            if (ghostOptimalPath != null && !ghostOptimalPath.isEmpty()) {
                ghostCurrentPath.add(ghostOptimalPath.get(0));
            }

            ghostBoardCanvas = new BoardCanvas(true);
            JScrollPane ghostScroll = createScrollPane(ghostBoardCanvas);

            // Wrap each board with a header label
            JPanel leftWrapper = createBoardCard("YOUR BOARD", playerScroll, ACCENT_BLUE);
            JPanel rightWrapper = createBoardCard("GHOST SOLVER (AI)", ghostScroll, ACCENT_PURPLE);

            JPanel splitPanel = new JPanel(new GridLayout(1, 2, 12, 0));
            splitPanel.setBackground(BG_DARK);
            splitPanel.setBorder(new EmptyBorder(8, 12, 12, 12));
            splitPanel.add(leftWrapper);
            splitPanel.add(rightWrapper);

            boardsContainer.add(splitPanel, BorderLayout.CENTER);
            startGhostRunner();
        }

        statusLabel.setText("Click or drag from START to the yellow END square.");
        cardLayout.show(mainContainer, "GAME");
        boardsContainer.revalidate();
        boardsContainer.repaint();
    }

    private JPanel createBoardCard(String title, JScrollPane scroll, Color titleColor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createLineBorder(new Color(50, 56, 68), 1));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        titleLbl.setForeground(titleColor);
        titleLbl.setBorder(new EmptyBorder(8, 0, 8, 0));

        panel.add(titleLbl, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane createScrollPane(Component view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private void resetPlayerPath() {
        playerPath.clear();
        playerPath.add(playerStartPoint);
        statusLabel.setText("Path cleared. Move toward the yellow target.");
        playerBoardCanvas.repaint();
    }

    private void stopGhostTimer() {
        if (ghostStepTimer != null && ghostStepTimer.isRunning()) {
            ghostStepTimer.stop();
        }
    }

    // ==========================================================
    // GHOST RUNNER LOGIC (RACE MODE)
    // ==========================================================
    private void startGhostRunner() {
        ghostStepIndex = 1;
        updateRaceProgressText();

        // Speed: faster on massive boards so it stays engaging
        int stepDelay = Math.max(250, 750 - (selectedSize * 5));

        ghostStepTimer = new javax.swing.Timer(stepDelay, e -> {
            if (isAnimatingFlow) return;

            if (ghostOptimalPath != null && ghostStepIndex < ghostOptimalPath.size()) {
                ghostCurrentPath.add(ghostOptimalPath.get(ghostStepIndex));
                ghostBoardCanvas.repaint();
                ghostStepIndex++;
                updateRaceProgressText();

                // Ghost reaches target first -> Defeat!
                if (ghostStepIndex == ghostOptimalPath.size()) {
                    stopGhostTimer();
                    handleGhostWon();
                }
            }
        });
        ghostStepTimer.start();
    }

    private void updateRaceProgressText() {
        if (currentMode != GameMode.RACE || ghostOptimalPath == null) return;
        int ghostRemaining = Math.max(0, ghostOptimalPath.size() - ghostCurrentPath.size());
        raceProgressLabel.setText("Your Moves: " + (playerPath.size() - 1) + "  |  Ghost Steps Left: " + ghostRemaining);
    }

    private void handleGhostWon() {
        statusLabel.setText("THE GHOST WON THE RACE!");
        SwingUtilities.invokeLater(() -> {
            String[] options = {"Retry Race", "Main Menu"};
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "The Ghost Solver completed its puzzle first!\nWould you like to try again with the same settings?",
                    "Defeat!",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
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
    // AUDIO: FAMILY FEUD STRIKE BUZZER
    // ==========================================================
    private void playErrorSound() {
        new Thread(() -> {
            try {
                int sampleRate = 16000;
                int pulseDurationMs = 110;
                int gapDurationMs = 45;

                int pulseSamples = (sampleRate * pulseDurationMs) / 1000;
                int gapSamples = (sampleRate * gapDurationMs) / 1000;
                int totalSamples = (pulseSamples * 2) + gapSamples;

                byte[] buffer = new byte[totalSamples];
                double f1 = 165.0;
                double f2 = 233.0;

                int idx = 0;
                for (int pulse = 0; pulse < 2; pulse++) {
                    for (int i = 0; i < pulseSamples; i++) {
                        double t = (double) i / sampleRate;
                        double wave = Math.sin(2.0 * Math.PI * f1 * t)
                                    + 0.5 * Math.sin(2.0 * Math.PI * (f1 * 3) * t)
                                    + Math.sin(2.0 * Math.PI * f2 * t)
                                    + 0.5 * Math.sin(2.0 * Math.PI * (f2 * 3) * t);

                        double saturated = Math.max(-1.0, Math.min(1.0, wave * 1.8));

                        double envelope = 1.0;
                        int ramp = sampleRate / 100;
                        if (i < ramp) {
                            envelope = (double) i / ramp;
                        } else if (i > pulseSamples - ramp) {
                            envelope = (double) (pulseSamples - i) / ramp;
                        }

                        buffer[idx++] = (byte) (saturated * envelope * 95);
                    }

                    if (pulse == 0) {
                        for (int i = 0; i < gapSamples; i++) {
                            buffer[idx++] = 0;
                        }
                    }
                }

                AudioFormat format = new AudioFormat((float) sampleRate, 8, 1, true, false);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format, totalSamples);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
                line.close();
            } catch (Exception ignored) {}
        }).start();
    }

    // ==========================================================
    // MOVE VALIDATION
    // ==========================================================
    private Integer getTileNumericValue(String[][] grid, int r, int c) {
        String token = grid[r][c].trim().toUpperCase();
        if (token.equals("S")) return null;
        if (token.startsWith("E:")) return Integer.parseInt(token.substring(2));
        if (token.equals("E")) return 2;
        return Integer.parseInt(token);
    }

    private void attemptMoveTo(int r, int c) {
        if (isAnimatingFlow) return;
        if (r < 0 || r >= selectedSize || c < 0 || c >= selectedSize) return;
        KaminoPuzzleSolver.Point target = new KaminoPuzzleSolver.Point(r, c);

        KaminoPuzzleSolver.Point currentHead = playerPath.get(playerPath.size() - 1);
        if (currentHead.equals(target)) return;

        // Step back / undo
        if (playerPath.size() > 1 && playerPath.get(playerPath.size() - 2).equals(target)) {
            playerPath.remove(playerPath.size() - 1);
            playerBoardCanvas.repaint();
            updateRaceProgressText();
            return;
        }

        // Must be adjacent
        int dist = Math.abs(currentHead.row - target.row) + Math.abs(currentHead.col - target.col);
        if (dist != 1 || playerPath.contains(target)) {
            triggerIllegalMove(target);
            return;
        }

        Integer currVal = currentHead.equals(playerStartPoint) ? null : getTileNumericValue(playerGrid, currentHead.row, currentHead.col);
        Integer nextVal = getTileNumericValue(playerGrid, target.row, target.col);

        boolean validMove = false;
        if (currentHead.equals(playerStartPoint)) {
            validMove = true;
        } else if (currVal != null && nextVal != null) {
            if (target.row < currentHead.row && nextVal > currVal) {
                validMove = true;
            } else if (target.row > currentHead.row && nextVal < currVal) {
                validMove = true;
            } else if (target.row == currentHead.row && nextVal.equals(currVal)) {
                validMove = true;
            }
        }

        if (validMove) {
            playerPath.add(target);
            playerBoardCanvas.repaint();
            updateRaceProgressText();

            // Win condition reached!
            if (target.equals(playerEndPoint)) {
                stopGhostTimer();
                triggerVictoryWaterFlowAnimation();
            }
        } else {
            triggerIllegalMove(target);
        }
    }

    private void triggerIllegalMove(KaminoPuzzleSolver.Point pt) {
        playErrorSound();
        flashRedPoint = pt;
        playerBoardCanvas.repaint();

        if (flashTimer != null && flashTimer.isRunning()) {
            flashTimer.stop();
        }
        flashTimer = new javax.swing.Timer(300, e -> {
            flashRedPoint = null;
            playerBoardCanvas.repaint();
        });
        flashTimer.setRepeats(false);
        flashTimer.start();
    }

    // ==========================================================
    // WATER FLOW ANIMATION & WIN DIALOG
    // ==========================================================
    private void triggerVictoryWaterFlowAnimation() {
        isAnimatingFlow = true;
        statusLabel.setText("YOU REACHED THE GOAL! Verifying route...");

        final List<KaminoPuzzleSolver.Point> fullSolution = new ArrayList<>(playerPath);

        // Step 1: Clear screen path
        playerPath.clear();
        animatedFlowPath.clear();
        playerBoardCanvas.repaint();

        // Step 2: Flow tile-by-tile
        int delay = Math.max(35, Math.min(180, 2500 / fullSolution.size()));

        javax.swing.Timer flowTimer = new javax.swing.Timer(delay, null);
        flowTimer.addActionListener(new ActionListener() {
            int stepIndex = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (stepIndex < fullSolution.size()) {
                    animatedFlowPath.add(fullSolution.get(stepIndex));
                    playerBoardCanvas.repaint();
                    stepIndex++;
                } else {
                    flowTimer.stop();
                    isAnimatingFlow = false;
                    statusLabel.setText("PUZZLE COMPLETED!");
                    promptVictoryOptions();
                }
            }
        });
        flowTimer.start();
    }

    private void promptVictoryOptions() {
        SwingUtilities.invokeLater(() -> {
            String title = (currentMode == GameMode.RACE) ? "You Beat the Solver!" : "Puzzle Solved!";
            String message = (currentMode == GameMode.RACE)
                    ? "Victory! You beat the Ghost Solver in " + (animatedFlowPath.size() - 1) + " moves!"
                    : "Congratulations! You solved the " + selectedSize + "x" + selectedSize + " puzzle in " + (animatedFlowPath.size() - 1) + " moves!";

            String[] options = {"Play Again (Same Settings)", "Main Menu"};
            int choice = JOptionPane.showOptionDialog(
                    this,
                    message,
                    title,
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
    // REUSABLE CANVAS (PLAYER OR GHOST)
    // ==========================================================
    private class BoardCanvas extends JPanel {
        private final boolean isGhost;

        public BoardCanvas(boolean isGhost) {
            this.isGhost = isGhost;
            setBackground(BG_DARK);

            if (!isGhost) {
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
        }

        private int getCellSize() {
            if (currentMode == GameMode.RACE) {
                if (selectedSize <= 10) return 46;
                if (selectedSize <= 25) return 32;
                if (selectedSize <= 50) return 22;
                return 16;
            } else {
                if (selectedSize <= 10) return 60;
                if (selectedSize <= 25) return 40;
                if (selectedSize <= 50) return 26;
                return 20;
            }
        }

        @Override
        public Dimension getPreferredSize() {
            int cellSize = getCellSize();
            int totalDim = cellSize * selectedSize + 40;
            return new Dimension(totalDim, totalDim);
        }

        private void handleMouseInput(java.awt.Point mousePos) {
            int cellSize = getCellSize();
            int startX = Math.max(20, (getWidth() - (cellSize * selectedSize)) / 2);
            int startY = Math.max(20, (getHeight() - (cellSize * selectedSize)) / 2);

            int col = (mousePos.x - startX) / cellSize;
            int row = (mousePos.y - startY) / cellSize;

            if (row >= 0 && row < selectedSize && col >= 0 && col < selectedSize) {
                attemptMoveTo(row, col);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            String[][] grid = isGhost ? ghostGrid : playerGrid;
            if (grid == null) return;

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int cellSize = getCellSize();
            int startX = Math.max(20, (getWidth() - (cellSize * selectedSize)) / 2);
            int startY = Math.max(20, (getHeight() - (cellSize * selectedSize)) / 2);

            List<KaminoPuzzleSolver.Point> activePath = isGhost
                    ? ghostCurrentPath
                    : (isAnimatingFlow ? animatedFlowPath : playerPath);
            Set<KaminoPuzzleSolver.Point> pathSet = new HashSet<>(activePath);

            KaminoPuzzleSolver.Point sPoint = isGhost ? ghostStartPoint : playerStartPoint;
            KaminoPuzzleSolver.Point ePoint = isGhost ? ghostEndPoint : playerEndPoint;
            int eVal = isGhost ? ghostEndValue : playerEndValue;

            // 1. Draw tiles
            for (int r = 0; r < selectedSize; r++) {
                for (int c = 0; c < selectedSize; c++) {
                    int x = startX + c * cellSize;
                    int y = startY + r * cellSize;
                    KaminoPuzzleSolver.Point cellPoint = new KaminoPuzzleSolver.Point(r, c);

                    if (!isGhost && cellPoint.equals(flashRedPoint)) {
                        g2d.setColor(ERROR_RED);
                    } else if (pathSet.contains(cellPoint)) {
                        g2d.setColor(isGhost ? ACCENT_PURPLE : PATH_GREEN);
                    } else if (cellPoint.equals(ePoint)) {
                        g2d.setColor(TARGET_YELLOW);
                    } else {
                        g2d.setColor(TILE_DEFAULT);
                    }

                    g2d.fillRoundRect(x + 1, y + 1, cellSize - 2, cellSize - 2, 6, 6);
                    g2d.setColor(new Color(20, 22, 26));
                    g2d.drawRoundRect(x + 1, y + 1, cellSize - 2, cellSize - 2, 6, 6);

                    String text;
                    Color textColor = TEXT_LIGHT;

                    if (cellPoint.equals(sPoint)) {
                        text = "START";
                        textColor = Color.WHITE;
                    } else if (cellPoint.equals(ePoint)) {
                        text = String.valueOf(eVal);
                        textColor = Color.BLACK;
                    } else {
                        text = grid[r][c];
                    }

                    int fontSize = Math.max(8, (int) (cellSize * (text.length() > 2 ? 0.28 : 0.44)));
                    g2d.setFont(new Font("SansSerif", Font.BOLD, fontSize));
                    g2d.setColor(textColor);

                    FontMetrics fm = g2d.getFontMetrics();
                    int textX = x + (cellSize - fm.stringWidth(text)) / 2;
                    int textY = y + ((cellSize - fm.getHeight()) / 2) + fm.getAscent();
                    g2d.drawString(text, textX, textY);
                }
            }

            // 2. Draw path pipe line
            if (activePath.size() > 1) {
                g2d.setColor(new Color(255, 255, 255, 220));
                g2d.setStroke(new BasicStroke(Math.max(2, cellSize / 7), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < activePath.size() - 1; i++) {
                    KaminoPuzzleSolver.Point p1 = activePath.get(i);
                    KaminoPuzzleSolver.Point p2 = activePath.get(i + 1);

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
    // STYLED BUTTON UTILITY
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