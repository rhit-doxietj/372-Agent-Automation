import Kamino_Puzzles.KaminoGUI;
import javax.swing.*;
import java.awt.*;

public class GameLauncher extends JFrame {

    private static final Color BG_DARK = new Color(24, 26, 31);
    private static final Color ACCENT_BLUE = new Color(74, 144, 226);
    private static final Color ACCENT_PURPLE = new Color(171, 71, 188);
    private static final Color TEXT_LIGHT = new Color(230, 235, 245);

    public GameLauncher() {
        setTitle("Puzzle Hub - Kamino & Recto");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_DARK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(15, 20, 15, 20);

        JLabel titleLabel = new JLabel("PUZZLE HUB");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
        titleLabel.setForeground(ACCENT_BLUE);
        gbc.gridy = 0;
        panel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("Select a game to play");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitleLabel.setForeground(TEXT_LIGHT);
        gbc.gridy = 1;
        panel.add(subtitleLabel, gbc);

        // Kamino Button
        JButton kaminoBtn = createStyledButton("Play Kamino.IO", 280, 50, ACCENT_BLUE);
        kaminoBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> {
                KaminoGUI kaminoGame = new KaminoGUI(() -> new GameLauncher().setVisible(true));
                kaminoGame.setVisible(true);
            });
        });
        gbc.gridy = 2;
        gbc.insets = new Insets(30, 20, 10, 20);
        panel.add(kaminoBtn, gbc);

        // Recto Button
        JButton rectoBtn = createStyledButton("Play Recto-Room", 280, 50, ACCENT_PURPLE);
        rectoBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> {
                RectoGUI rectoGame = new RectoGUI(() -> new GameLauncher().setVisible(true));
                rectoGame.setVisible(true);
            });
        });
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 20, 15, 20);
        panel.add(rectoBtn, gbc);

        // Quit Hub Button
        JButton quitBtn = createStyledButton("Quit", 280, 40, new Color(100, 100, 110));
        quitBtn.addActionListener(e -> System.exit(0));
        gbc.gridy = 4;
        panel.add(quitBtn, gbc);

        add(panel);
    }

    private JButton createStyledButton(String text, int width, int height, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(width, height));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(bgColor.brighter());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
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
            new GameLauncher().setVisible(true);
        });
    }
}