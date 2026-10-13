package PuzzleHub_Shared;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.sound.sampled.*;
import javax.swing.*;

public class UIUtils {
    // Shared Dark Theme Palette
    public static final Color BG_DARK = new Color(24, 26, 31);
    public static final Color PANEL_BG = new Color(33, 37, 43);
    public static final Color ACCENT_BLUE = new Color(74, 144, 226);
    public static final Color ACCENT_PURPLE = new Color(171, 71, 188);
    public static final Color TEXT_LIGHT = new Color(230, 235, 245);

    /**
     * Creates a standard styled button with hover brightness animation used across all menus.
     */
    public static JButton createStyledButton(String text, int width, int height, Color bgColor) {
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

    /**
     * Shared audio generator for error/success beeps across puzzle apps.
     */
    public static void playTone(double frequency, int durationMs, float volume) {
        new Thread(() -> {
            try {
                float sampleRate = 16000;
                int numSamples = (int) ((sampleRate * durationMs) / 1000);
                byte[] buffer = new byte[numSamples];

                for (int i = 0; i < numSamples; i++) {
                    double t = (double) i / sampleRate;
                    double wave = Math.sin(2.0 * Math.PI * frequency * t);
                    double envelope = 1.0 - ((double) i / numSamples); // fade out
                    buffer[i] = (byte) (wave * envelope * volume * 90);
                }

                AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format, numSamples);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
                line.close();
            } catch (Exception ignored) {}
        }).start();
    }
}