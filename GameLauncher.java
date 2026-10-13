import Kamino_Puzzles.KaminoGUI;
import PuzzleHub_Shared.UIUtils;
import java.awt.*;
import javax.swing.*;

public class GameLauncher extends JFrame {

    public GameLauncher() {
        setTitle("Puzzle Hub - Kamino & Recto");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIUtils.BG_DARK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(15, 20, 15, 20);

        JLabel titleLabel = new JLabel("PUZZLE HUB");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
        titleLabel.setForeground(UIUtils.ACCENT_BLUE);
        gbc.gridy = 0;
        panel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("Select a game to play");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitleLabel.setForeground(UIUtils.TEXT_LIGHT);
        gbc.gridy = 1;
        panel.add(subtitleLabel, gbc);

        // Kamino Button
        JButton kaminoBtn = UIUtils.createStyledButton("Play Kamino.IO", 280, 50, UIUtils.ACCENT_BLUE);
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
        JButton rectoBtn = UIUtils.createStyledButton("Play Recto-Room", 280, 50, UIUtils.ACCENT_PURPLE);
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
        JButton quitBtn = UIUtils.createStyledButton("Quit", 280, 40, new Color(100, 100, 110));
        quitBtn.addActionListener(e -> System.exit(0));
        gbc.gridy = 4;
        panel.add(quitBtn, gbc);

        add(panel);
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