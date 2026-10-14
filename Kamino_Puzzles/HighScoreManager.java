package Kamino_Puzzles;

import java.io.*;
import java.util.*;

public class HighScoreManager {
    private static final String HIGHSCORE_FILE = "kamino_highscores.txt";

    public static class ScoreEntry implements Comparable<ScoreEntry> {
        public String name;
        public int moves;
        public int boardSize;
        public String mode;

        public ScoreEntry(String name, int moves, int boardSize, String mode) {
            this.name = name;
            this.moves = moves;
            this.boardSize = boardSize;
            this.mode = mode;
        }

        @Override
        public int compareTo(ScoreEntry other) {
            return Integer.compare(this.moves, other.moves); // Lower moves rank higher
        }
    }

    public static List<ScoreEntry> loadScores() {
        List<ScoreEntry> scores = new ArrayList<>();
        File file = new File(HIGHSCORE_FILE);
        if (!file.exists()) return scores;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String name = parts[0];
                    int moves = Integer.parseInt(parts[1]);
                    int size = Integer.parseInt(parts[2]);
                    String mode = parts[3];
                    scores.add(new ScoreEntry(name, moves, size, mode));
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        Collections.sort(scores);
        return scores;
    }

    public static void addScore(String name, int moves, int boardSize, String mode) {
        List<ScoreEntry> scores = loadScores();
        scores.add(new ScoreEntry(name == null || name.trim().isEmpty() ? "Anonymous" : name.trim(), moves, boardSize, mode));
        Collections.sort(scores);

        try (PrintWriter writer = new PrintWriter(new FileWriter(HIGHSCORE_FILE))) {
            for (ScoreEntry entry : scores) {
                writer.println(entry.name + "," + entry.moves + "," + entry.boardSize + "," + entry.mode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}