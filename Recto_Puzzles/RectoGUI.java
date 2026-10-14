import Kamino_Puzzles.HighScoreManager;
import PuzzleHub_Shared.UIUtils;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class RectoGUI extends JFrame {

    public enum GameMode { CLASSIC, TIME_TRIAL, HARDCORE, FOG_OF_WAR }
    public enum BoardSize { SMALL, MEDIUM, LARGE, CUSTOM }

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private Runnable onReturnToHub;

    private GameMode selectedMode = GameMode.CLASSIC;
    private BoardSize selectedSize = BoardSize.MEDIUM;
    private String selectedDifficulty = "Medium";
    private int customRows = 8;
    private int customCols = 8;

    private float soundVolume = 0.8f;
    private boolean soundMuted = false;

    private int lives = 3;
    private int timerSeconds = 0;
    private Timer gameTimer;
    private int[][] grid;
    private int[][] clueIndexMap;
    private Recto solver;
    private final List<Recto.Rect> playerRects = new ArrayList<>();
    private boolean[][] revealedCells;
    private boolean isGameOver = false;
    private boolean showSolutionOverlay = false;

    private Point dragStart = null;
    private Point dragEnd = null;

    private Recto.Rect errorRect = null;
    private Timer errorTimer = null;

    private JLabel timerLabel;
    private JLabel livesLabel;
    private JPanel gameOverBar;
    private JButton showSolutionBtn;
    private BoardPanel boardPanel;

    public RectoGUI() {
        this(null);
    }

    public RectoGUI(Runnable onReturnToHub) {
        this.onReturnToHub = onReturnToHub;
        setTitle("Recto-Room");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 860);
        setMinimumSize(new Dimension(750, 650));
        setLocationRelativeTo(null);

        mainPanel.add(createHomeScreen(), "HOME");
        mainPanel.add(createGameModeScreen(), "MODES");
        mainPanel.add(createPuzzleSetupScreen(), "PUZZLE_SETUP");
        mainPanel.add(createSettingsScreen(), "SETTINGS");
        mainPanel.add(createGamePlayScreen(), "GAME");

        add(mainPanel);
        cardLayout.show(mainPanel, "HOME");
    }

    private JPanel createHomeScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtils.BG_DARK);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(12, 15, 12, 15));

        JButton rulesBtn = UIUtils.createStyledButton("Rules", 130, 38, new Color(90, 100, 115));
        rulesBtn.addActionListener(e -> showRulesDialog());
        topBar.add(rulesBtn);
        panel.add(topBar, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel title = new JLabel("RECTO-ROOM");
        title.setFont(new Font("SansSerif", Font.BOLD, 75));
        title.setForeground(UIUtils.ACCENT_PURPLE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Grid Partitioning Logic Game");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 18));
        subtitle.setForeground(UIUtils.TEXT_LIGHT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton startBtn = UIUtils.createStyledButton("Start Game", 240, 48, UIUtils.ACCENT_BLUE);
        JButton highScoresBtn = UIUtils.createStyledButton("High Scores", 240, 48, new Color(211, 84, 0));
        JButton settingsBtn = UIUtils.createStyledButton("Settings", 240, 48, new Color(90, 100, 115));
        JButton quitBtn = onReturnToHub != null ? 
            UIUtils.createStyledButton("Back to Hub", 240, 40, new Color(70, 75, 85)) : 
            UIUtils.createStyledButton("Quit", 240, 40, new Color(178, 34, 34));

        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        highScoresBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        settingsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        quitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        startBtn.addActionListener(e -> cardLayout.show(mainPanel, "MODES"));
        highScoresBtn.addActionListener(e -> showHighScoresDialog());
        settingsBtn.addActionListener(e -> cardLayout.show(mainPanel, "SETTINGS"));
        if (onReturnToHub != null) {
            quitBtn.addActionListener(e -> {
                dispose();
                onReturnToHub.run();
            });
        } else {
            quitBtn.addActionListener(e -> System.exit(0));
        }

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(title);
        centerPanel.add(subtitle);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        centerPanel.add(startBtn);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        centerPanel.add(highScoresBtn);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        centerPanel.add(settingsBtn);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        centerPanel.add(quitBtn);
        centerPanel.add(Box.createVerticalGlue());

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    private void showHighScoresDialog() {
        JDialog dialog = new JDialog(this, "Recto High Scores", true);
        dialog.setSize(580, 480);
        dialog.setLocationRelativeTo(this);

        JPanel mainDialogPanel = new JPanel(new BorderLayout(0, 15));
        mainDialogPanel.setBackground(UIUtils.BG_DARK);
        mainDialogPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel titleLabel = new JLabel("RECTO LEADERBOARD", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(211, 84, 0));
        mainDialogPanel.add(titleLabel, BorderLayout.NORTH);

        List<HighScoreManager.ScoreEntry> scores = HighScoreManager.loadScores("RECTO");

        String[] columnNames = {"Rank", "Player", "Time / Score", "Mode / Size"};
        Object[][] data = new Object[scores.size()][4];

        for (int i = 0; i < scores.size(); i++) {
            HighScoreManager.ScoreEntry entry = scores.get(i);
            data[i][0] = "#" + (i + 1);
            data[i][1] = entry.name;
            data[i][2] = entry.scoreValue + "s";
            data[i][3] = entry.details;
        }

        JTable table = new JTable(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table.setRowHeight(32);
        table.setBackground(UIUtils.PANEL_BG);
        table.setForeground(UIUtils.TEXT_LIGHT);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(70, 80, 95));
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(50, 56, 68));
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(30, 34, 42));
        header.setForeground(UIUtils.ACCENT_PURPLE);
        header.setFont(new Font("SansSerif", Font.BOLD, 15));
        header.setPreferredSize(new Dimension(0, 36));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSelected, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, val, isSelected, hasFocus, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) {
                    comp.setBackground(r % 2 == 0 ? UIUtils.PANEL_BG : new Color(38, 43, 52));
                }
                return comp;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(UIUtils.PANEL_BG);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(50, 56, 68), 1));
        mainDialogPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        JButton closeBtn = UIUtils.createStyledButton("Close", 140, 40, new Color(90, 100, 115));
        closeBtn.addActionListener(e -> dialog.dispose());
        bottomPanel.add(closeBtn);

        mainDialogPanel.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainDialogPanel);
        dialog.setVisible(true);
    }

    private void showRulesDialog() {
        String rulesText = "RECTO RULES:\n\n" +
                "1. Divide the grid into non-overlapping rectangular boxes.\n" +
                "2. Each box must enclose EXACTLY ONE clue number.\n" +
                "3. The dimensions of the box (Height + Width) must equal the clue number (h + w = Clue).\n" +
                "4. All cells on the grid must be covered to complete the puzzle.\n\n" +
                "CONTROLS:\n" +
                "- Click and drag across cells to form a box.\n" +
                "- Right-click (or click) an existing box to remove/deselect it.";
        JOptionPane.showMessageDialog(this, rulesText, "How to Play Recto", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createGameModeScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtils.BG_DARK);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel header = new JLabel("Select Game Mode", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 36));
        header.setForeground(UIUtils.TEXT_LIGHT);
        panel.add(header, BorderLayout.NORTH);

        JPanel modesGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        modesGrid.setOpaque(false);
        modesGrid.setBorder(new EmptyBorder(30, 0, 30, 0));

        JButton classicBtn = createModeCard("Classic", "Standard Recto rules with 3 Lives.", UIUtils.ACCENT_BLUE);
        JButton timeTrialBtn = createModeCard("Time Trial", "3 Lives with a time limit scaling by difficulty.", new Color(211, 84, 0));
        JButton hardcoreBtn = createModeCard("Hardcore", "Single mistake equals instant failure (1 Life).", new Color(192, 57, 43));
        JButton fogBtn = createModeCard("Fog of War", "Grid is obscured until sections are revealed.", UIUtils.ACCENT_PURPLE);

        classicBtn.addActionListener(e -> selectMode(GameMode.CLASSIC));
        timeTrialBtn.addActionListener(e -> selectMode(GameMode.TIME_TRIAL));
        hardcoreBtn.addActionListener(e -> selectMode(GameMode.HARDCORE));
        fogBtn.addActionListener(e -> selectMode(GameMode.FOG_OF_WAR));

        modesGrid.add(classicBtn);
        modesGrid.add(timeTrialBtn);
        modesGrid.add(hardcoreBtn);
        modesGrid.add(fogBtn);

        panel.add(modesGrid, BorderLayout.CENTER);

        JButton backBtn = UIUtils.createStyledButton("Back to Main Menu", 200, 45, new Color(90, 98, 110));
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "HOME"));
        
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setOpaque(false);
        southPanel.add(backBtn);
        panel.add(southPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JButton createModeCard(String title, String desc, Color bg) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(60, 68, 80), 1));

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
        cardLayout.show(mainPanel, "PUZZLE_SETUP");
    }

    private JPanel createPuzzleSetupScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIUtils.BG_DARK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Puzzle Configuration", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 36));
        title.setForeground(UIUtils.TEXT_LIGHT);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        JPanel settingsBox = new JPanel(new GridLayout(5, 2, 10, 15));
        settingsBox.setBackground(UIUtils.PANEL_BG);
        settingsBox.setBorder(new EmptyBorder(25, 35, 25, 35));

        JLabel sizeLbl = new JLabel("Board Size:");
        sizeLbl.setForeground(UIUtils.TEXT_LIGHT);
        JComboBox<String> sizeCombo = new JComboBox<>(new String[]{"Small (5x5)", "Medium (8x8)", "Large (12x12)", "Custom"});
        sizeCombo.setSelectedIndex(1);
        settingsBox.add(sizeLbl);
        settingsBox.add(sizeCombo);

        JLabel rowLbl = new JLabel("Custom Rows (1-99):");
        rowLbl.setForeground(Color.GRAY);
        JSpinner rowSpinner = new JSpinner(new SpinnerNumberModel(15, 1, 99, 1));
        rowSpinner.setEnabled(false);
        settingsBox.add(rowLbl);
        settingsBox.add(rowSpinner);

        JLabel colLbl = new JLabel("Custom Cols (1-99):");
        colLbl.setForeground(Color.GRAY);
        JSpinner colSpinner = new JSpinner(new SpinnerNumberModel(15, 1, 99, 1));
        colSpinner.setEnabled(false);
        settingsBox.add(colLbl);
        settingsBox.add(colSpinner);

        sizeCombo.addActionListener(e -> {
            boolean isCustom = sizeCombo.getSelectedIndex() == 3;
            rowSpinner.setEnabled(isCustom);
            colSpinner.setEnabled(isCustom);
            rowLbl.setForeground(isCustom ? UIUtils.TEXT_LIGHT : Color.GRAY);
            colLbl.setForeground(isCustom ? UIUtils.TEXT_LIGHT : Color.GRAY);

            switch (sizeCombo.getSelectedIndex()) {
                case 0 -> selectedSize = BoardSize.SMALL;
                case 1 -> selectedSize = BoardSize.MEDIUM;
                case 2 -> selectedSize = BoardSize.LARGE;
                case 3 -> selectedSize = BoardSize.CUSTOM;
            }
        });

        JLabel diffLbl = new JLabel("Difficulty:");
        diffLbl.setForeground(UIUtils.TEXT_LIGHT);
        JComboBox<String> diffCombo = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        diffCombo.setSelectedIndex(1);
        diffCombo.addActionListener(e -> selectedDifficulty = (String) diffCombo.getSelectedItem());
        settingsBox.add(diffLbl);
        settingsBox.add(diffCombo);

        gbc.gridy = 1; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(settingsBox, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);

        JButton backBtn = UIUtils.createStyledButton("Back", 140, 45, new Color(90, 98, 110));
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "MODES"));

        JButton launchBtn = UIUtils.createStyledButton("Play Puzzle", 180, 45, UIUtils.ACCENT_BLUE);
        launchBtn.addActionListener(e -> {
            if (selectedSize == BoardSize.CUSTOM) {
                this.customRows = (int) rowSpinner.getValue();
                this.customCols = (int) colSpinner.getValue();
            }
            startNewGame();
        });

        btnPanel.add(backBtn);
        btnPanel.add(launchBtn);

        gbc.gridy = 2; gbc.insets = new Insets(20, 15, 15, 15);
        panel.add(btnPanel, gbc);

        return panel;
    }

    private JPanel createSettingsScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIUtils.BG_DARK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Sound Settings", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 36));
        title.setForeground(UIUtils.TEXT_LIGHT);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        JPanel settingsBox = new JPanel(new GridLayout(2, 2, 10, 15));
        settingsBox.setBackground(UIUtils.PANEL_BG);
        settingsBox.setBorder(new EmptyBorder(25, 35, 25, 35));

        JLabel soundLbl = new JLabel("Sound Volume:");
        soundLbl.setForeground(UIUtils.TEXT_LIGHT);
        JSlider volSlider = new JSlider(0, 100, (int) (soundVolume * 100));
        volSlider.setOpaque(false);
        volSlider.addChangeListener(e -> soundVolume = volSlider.getValue() / 100.0f);
        settingsBox.add(soundLbl);
        settingsBox.add(volSlider);

        JCheckBox muteBox = new JCheckBox("Mute Audio");
        muteBox.setSelected(soundMuted);
        muteBox.setForeground(UIUtils.TEXT_LIGHT);
        muteBox.setOpaque(false);
        muteBox.addActionListener(e -> soundMuted = muteBox.isSelected());
        settingsBox.add(muteBox);

        gbc.gridy = 1; gbc.gridwidth = 2;
        panel.add(settingsBox, gbc);

        JButton backBtn = UIUtils.createStyledButton("Back to Main Menu", 200, 45, new Color(90, 98, 110));
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "HOME"));

        gbc.gridy = 2; gbc.insets = new Insets(20, 15, 15, 15);
        panel.add(backBtn, gbc);

        return panel;
    }

    private JPanel createGamePlayScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtils.BG_DARK);

        JPanel hud = new JPanel(new BorderLayout());
        hud.setBackground(UIUtils.PANEL_BG);
        hud.setBorder(new EmptyBorder(12, 20, 12, 20));

        livesLabel = new JLabel("Lives: 3");
        livesLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        livesLabel.setForeground(new Color(239, 83, 80));

        timerLabel = new JLabel("Time: 00:00");
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        timerLabel.setForeground(UIUtils.TEXT_LIGHT);
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel navControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navControls.setOpaque(false);

        JButton menuBtn = UIUtils.createStyledButton("Main Menu", 110, 35, new Color(70, 75, 85));
        menuBtn.addActionListener(e -> {
            if (gameTimer != null) gameTimer.stop();
            if (onReturnToHub != null) {
                dispose();
                onReturnToHub.run();
            } else {
                cardLayout.show(mainPanel, "HOME");
            }
        });

        navControls.add(menuBtn);

        hud.add(livesLabel, BorderLayout.WEST);
        hud.add(timerLabel, BorderLayout.CENTER);
        hud.add(navControls, BorderLayout.EAST);

        gameOverBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        gameOverBar.setBackground(new Color(180, 40, 40));
        gameOverBar.setVisible(false);

        JLabel gameOverText = new JLabel("GAME OVER!");
        gameOverText.setFont(new Font("SansSerif", Font.BOLD, 18));
        gameOverText.setForeground(Color.WHITE);

        showSolutionBtn = UIUtils.createStyledButton("Hide Solution", 140, 35, new Color(70, 130, 180));
        showSolutionBtn.addActionListener(e -> {
            showSolutionOverlay = !showSolutionOverlay;
            showSolutionBtn.setText(showSolutionOverlay ? "Hide Solution" : "Show Solution");
            boardPanel.repaint();
        });

        JButton retryBtn = UIUtils.createStyledButton("Try Again", 120, 35, new Color(46, 139, 87));
        retryBtn.addActionListener(e -> startNewGame());

        gameOverBar.add(gameOverText);
        gameOverBar.add(showSolutionBtn);
        gameOverBar.add(retryBtn);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(hud, BorderLayout.NORTH);
        topContainer.add(gameOverBar, BorderLayout.SOUTH);

        panel.add(topContainer, BorderLayout.NORTH);

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

        RectoGenerator.GenerationResult genRes = RectoGenerator.generateWithPartitions(r, c);
        this.grid = genRes.grid;
        this.clueIndexMap = new int[r][c];

        for (int i = 0; i < r; i++) {
            Arrays.fill(this.clueIndexMap[i], -1);
        }

        int clueCounter = 0;
        for (int row = 0; row < r; row++) {
            for (int col = 0; col < c; col++) {
                if (grid[row][col] > 0) {
                    clueIndexMap[row][col] = clueCounter++;
                }
            }
        }

        this.solver = new Recto(grid);

        for (int i = 0; i < genRes.partitions.size(); i++) {
            Recto.Rect partition = genRes.partitions.get(i);
            int partitionClueId = -1;

            for (int pr = partition.r1; pr <= partition.r2; pr++) {
                for (int pc = partition.c1; pc <= partition.c2; pc++) {
                    if (grid[pr][pc] > 0 && clueIndexMap[pr][pc] != -1) {
                        partitionClueId = clueIndexMap[pr][pc];
                        break;
                    }
                }
                if (partitionClueId != -1) break;
            }

            int mappedId = (partitionClueId != -1) ? partitionClueId : i;
            this.solver.setSolutionRect(mappedId, partition);
        }

        if (r <= 8 && c <= 8) {
            this.solver.solve();
        }

        this.playerRects.clear();
        this.errorRect = null;
        if (errorTimer != null) errorTimer.stop();

        this.isGameOver = false;
        this.showSolutionOverlay = false;
        this.gameOverBar.setVisible(false);
        this.showSolutionBtn.setText("Hide Solution");

        this.lives = (selectedMode == GameMode.HARDCORE) ? 1 : 3;
        livesLabel.setText("Lives: " + lives);

        this.revealedCells = new boolean[r][c];
        if (selectedMode == GameMode.FOG_OF_WAR) {
            List<Point> clueLocations = new ArrayList<>();
            for (int row = 0; row < r; row++) {
                for (int col = 0; col < c; col++) {
                    if (grid[row][col] > 0) {
                        clueLocations.add(new Point(row, col));
                    }
                }
            }

            if (!clueLocations.isEmpty()) {
                Point firstClue = clueLocations.get(0);
                revealAroundCell(firstClue.x, firstClue.y, 1);
            }
            updateFogOfWar(null);
        } else {
            for (boolean[] row : revealedCells) Arrays.fill(row, true);
        }

        if (gameTimer != null) gameTimer.stop();
        if (selectedMode == GameMode.TIME_TRIAL) {
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

    private void revealAroundCell(int cr, int cc, int radius) {
        int rMax = grid.length;
        int cMax = grid[0].length;
        for (int r = Math.max(0, cr - radius); r <= Math.min(rMax - 1, cr + radius); r++) {
            for (int c = Math.max(0, cc - radius); c <= Math.min(cMax - 1, cc + radius); c++) {
                revealedCells[r][c] = true;
            }
        }
    }

    private void updateFogOfWar(Recto.Rect placed) {
        if (selectedMode != GameMode.FOG_OF_WAR) return;

        if (placed != null) {
            int r1 = Math.max(0, placed.r1 - 1);
            int r2 = Math.min(grid.length - 1, placed.r2 + 1);
            int c1 = Math.max(0, placed.c1 - 1);
            int c2 = Math.min(grid[0].length - 1, placed.c2 + 1);

            for (int r = r1; r <= r2; r++) {
                for (int c = c1; c <= c2; c++) {
                    revealedCells[r][c] = true;
                }
            }
        }

        boolean hasVisibleUnsolvedClue = false;
        List<Point> unsolvedClues = new ArrayList<>();

        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[0].length; c++) {
                if (grid[r][c] > 0) {
                    final int row = r;
                    final int col = c;
                    boolean solved = playerRects.stream().anyMatch(rect -> 
                        row >= rect.r1 && row <= rect.r2 && col >= rect.c1 && col <= rect.c2
                    );

                    if (!solved) {
                        unsolvedClues.add(new Point(r, c));
                        if (revealedCells[r][c]) {
                            hasVisibleUnsolvedClue = true;
                        }
                    }
                }
            }
        }

        if (!hasVisibleUnsolvedClue && !unsolvedClues.isEmpty()) {
            Point closest = null;
            double minDistance = Double.MAX_VALUE;

            for (Point clue : unsolvedClues) {
                for (int r = 0; r < grid.length; r++) {
                    for (int c = 0; c < grid[0].length; c++) {
                        if (revealedCells[r][c]) {
                            double dist = Math.hypot(clue.x - r, clue.y - c);
                            if (dist < minDistance) {
                                minDistance = dist;
                                closest = clue;
                            }
                        }
                    }
                }
            }

            if (closest != null) {
                revealAroundCell(closest.x, closest.y, 1);
            } else {
                Point fallback = unsolvedClues.get(0);
                revealAroundCell(fallback.x, fallback.y, 1);
            }
        }
    }

    private void handleTimerTick() {
        if (isGameOver) return;

        if (selectedMode == GameMode.TIME_TRIAL) {
            timerSeconds--;
            if (timerSeconds <= 0) {
                triggerGameOver();
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

    private void triggerGameOver() {
        this.isGameOver = true;
        if (gameTimer != null) gameTimer.stop();
        playSound(false);

        this.showSolutionOverlay = true;
        this.showSolutionBtn.setText("Hide Solution");

        gameOverBar.setVisible(true);
        boardPanel.repaint();
    }

    private void checkVictoryCondition() {
        int totalCells = grid.length * grid[0].length;
        int coveredCells = 0;
        for (Recto.Rect rect : playerRects) {
            coveredCells += (rect.r2 - rect.r1 + 1) * (rect.c2 - rect.c1 + 1);
        }

        if (coveredCells == totalCells) {
            if (gameTimer != null) gameTimer.stop();
            playSound(true);

            String playerName = JOptionPane.showInputDialog(
                this,
                "Victory! You solved the Recto puzzle in " + timerSeconds + " seconds!\nEnter your name for the High Scores:",
                "Save High Score",
                JOptionPane.PLAIN_MESSAGE
            );

            String boardInfo = grid.length + "x" + grid[0].length;
            HighScoreManager.addScore(playerName, timerSeconds, selectedMode + " (" + boardInfo + ")", "RECTO");

            JOptionPane.showMessageDialog(this, "Congratulations! You solved the Recto puzzle!", "Victory", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(mainPanel, "MODES");
        }
    }

    private void playSound(boolean isCorrect) {
        if (soundMuted || soundVolume <= 0.01f) return;
        if (isCorrect) {
            UIUtils.playTone(1046.50, 450, soundVolume);
        } else {
            UIUtils.playTone(150.0, 350, soundVolume);
        }
    }

    private class BoardPanel extends JPanel {
        private final int PADDING = 20;

        public BoardPanel() {
            setBackground(UIUtils.BG_DARK);

            MouseAdapter adapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (isGameOver || grid == null) return;
                    Point cell = getCellAtPoint(e.getPoint());
                    if (cell != null) {
                        Recto.Rect existing = getRectAtCell(cell.x, cell.y);
                        if (existing != null && SwingUtilities.isRightMouseButton(e)) {
                            playerRects.remove(existing);
                            updateFogOfWar(null);
                            repaint();
                            return;
                        }

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

                    if (dragStart.equals(dragEnd) && SwingUtilities.isLeftMouseButton(e)) {
                        Recto.Rect existing = getRectAtCell(dragStart.x, dragStart.y);
                        if (existing != null) {
                            playerRects.remove(existing);
                            updateFogOfWar(null);
                            dragStart = null;
                            dragEnd = null;
                            repaint();
                            return;
                        }
                    }

                    validateAndPlaceRect(dragStart, dragEnd);
                    dragStart = null;
                    dragEnd = null;
                    repaint();
                }
            };

            addMouseListener(adapter);
            addMouseMotionListener(adapter);
        }

        private Recto.Rect getRectAtCell(int r, int c) {
            for (Recto.Rect rect : playerRects) {
                if (r >= rect.r1 && r <= rect.r2 && c >= rect.c1 && c <= rect.c2) {
                    return rect;
                }
            }
            return null;
        }

        private Point getCellAtPoint(Point p) {
            int rows = grid.length;
            int cols = grid[0].length;
            int cellSize = Math.max(1, Math.min((getHeight() - 2 * PADDING) / rows, (getWidth() - 2 * PADDING) / cols));

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

            int clueCount = 0;
            int foundClueId = -1;

            for (int r = r1; r <= r2; r++) {
                for (int c = c1; c <= c2; c++) {
                    if (grid[r][c] > 0) {
                        clueCount++;
                        foundClueId = clueIndexMap[r][c];
                    }
                }
            }

            boolean isValidPlacement = false;
            if (clueCount == 1 && foundClueId != -1) {
                Recto.Rect expectedSolution = solver.getSolutionRect(foundClueId);
                if (expectedSolution != null) {
                    isValidPlacement = (expectedSolution.r1 == r1 && expectedSolution.r2 == r2 &&
                                        expectedSolution.c1 == c1 && expectedSolution.c2 == c2);
                }
            }

            if (isValidPlacement) {
                playSound(true);
                Recto.Rect placed = new Recto.Rect(foundClueId, r1, c1, r2, c2);
                playerRects.add(placed);

                updateFogOfWar(placed);
                checkVictoryCondition();
            } else {
                playSound(false);
                lives--;
                livesLabel.setText("Lives: " + lives);

                errorRect = new Recto.Rect(-1, r1, c1, r2, c2);
                if (errorTimer != null) errorTimer.stop();
                errorTimer = new Timer(1000, e -> {
                    errorRect = null;
                    repaint();
                });
                errorTimer.setRepeats(false);
                errorTimer.start();

                if (lives <= 0) {
                    triggerGameOver();
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
            int cellSize = Math.max(1, Math.min((getHeight() - 2 * PADDING) / rows, (getWidth() - 2 * PADDING) / cols));

            int startX = (getWidth() - cols * cellSize) / 2;
            int startY = (getHeight() - rows * cellSize) / 2;

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int x = startX + c * cellSize;
                    int y = startY + r * cellSize;

                    if (selectedMode == GameMode.FOG_OF_WAR && !revealedCells[r][c] && !showSolutionOverlay) {
                        g2.setColor(new Color(15, 15, 20));
                        g2.fillRect(x, y, cellSize, cellSize);
                        g2.setColor(new Color(30, 30, 40));
                        g2.drawRect(x, y, cellSize, cellSize);
                        continue;
                    }

                    g2.setColor(new Color(44, 49, 58));
                    g2.fillRect(x, y, cellSize, cellSize);
                    g2.setColor(new Color(20, 22, 26));
                    g2.drawRect(x, y, cellSize, cellSize);

                    if (grid[r][c] > 0 && cellSize >= 8) {
                        g2.setColor(UIUtils.TEXT_LIGHT);
                        g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(8, cellSize / 2)));
                        String valStr = String.valueOf(grid[r][c]);
                        FontMetrics fm = g2.getFontMetrics();
                        int tx = x + (cellSize - fm.stringWidth(valStr)) / 2;
                        int ty = y + (cellSize + fm.getAscent() - fm.getDescent()) / 2;
                        g2.drawString(valStr, tx, ty);
                    }
                }
            }

            g2.setStroke(new BasicStroke(Math.max(1, cellSize / 10)));
            for (Recto.Rect rect : playerRects) {
                int rx = startX + rect.c1 * cellSize;
                int ry = startY + rect.r1 * cellSize;
                int rw = (rect.c2 - rect.c1 + 1) * cellSize;
                int rh = (rect.r2 - rect.r1 + 1) * cellSize;

                g2.setColor(new Color(76, 175, 80, 80));
                g2.fillRect(rx, ry, rw, rh);
                g2.setColor(new Color(76, 175, 80));
                g2.drawRect(rx, ry, rw, rh);
            }

            if (errorRect != null) {
                int rx = startX + errorRect.c1 * cellSize;
                int ry = startY + errorRect.r1 * cellSize;
                int rw = (errorRect.c2 - errorRect.c1 + 1) * cellSize;
                int erh = (errorRect.r2 - errorRect.r1 + 1) * cellSize;

                g2.setColor(new Color(239, 83, 80, 100));
                g2.fillRect(rx, ry, rw, erh);
                g2.setColor(new Color(239, 83, 80));
                g2.drawRect(rx, ry, rw, erh);
            }

            if (dragStart != null && dragEnd != null) {
                int r1 = Math.min(dragStart.x, dragEnd.x);
                int r2 = Math.max(dragStart.x, dragEnd.x);
                int c1 = Math.min(dragStart.y, dragEnd.y);
                int c2 = Math.max(dragStart.y, dragEnd.y);

                int dx = startX + c1 * cellSize;
                int dy = startY + r1 * cellSize;
                int dw = (c2 - c1 + 1) * cellSize;
                int dh = (r2 - r1 + 1) * cellSize;

                g2.setColor(new Color(70, 130, 180, 100));
                g2.fillRect(dx, dy, dw, dh);
                g2.setColor(UIUtils.ACCENT_BLUE);
                g2.drawRect(dx, dy, dw, dh);
            }

            if (showSolutionOverlay) {
                g2.setStroke(new BasicStroke(
                    Math.max(2, cellSize / 6),
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL,
                    0,
                    new float[]{4},
                    0
                ));
                g2.setColor(new Color(239, 83, 80));

                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {
                        int owner = solver.getCellOwner(r, c);
                        int x = startX + c * cellSize;
                        int y = startY + r * cellSize;

                        if (r == 0 || solver.getCellOwner(r - 1, c) != owner) {
                            g2.drawLine(x, y, x + cellSize, y);
                        }
                        if (r == rows - 1 || solver.getCellOwner(r + 1, c) != owner) {
                            g2.drawLine(x, y + cellSize, x + cellSize, y + cellSize);
                        }
                        if (c == 0 || solver.getCellOwner(r, c - 1) != owner) {
                            g2.drawLine(x, y, x, y + cellSize);
                        }
                        if (c == cols - 1 || solver.getCellOwner(r, c + 1) != owner) {
                            g2.drawLine(x + cellSize, y, x + cellSize, y + cellSize);
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new RectoGUI().setVisible(true);
        });
    }
}