package Kamino_Puzzles;

import java.io.*;
import java.util.*;

public class HighScoreManager {
    private static final String HIGHSCORE_FILE = "puzzle_hub_highscores.txt";

    public static class ScoreEntry implements Comparable<ScoreEntry> {
        public String name;
        public int scoreValue; 
        public String details;
        public String gameType;

        public ScoreEntry(String name, int scoreValue, String details, String gameType) {
            this.name = name;
            this.scoreValue = scoreValue;
            this.details = details;
            this.gameType = gameType;
        }

        @Override
        public int compareTo(ScoreEntry other) {
            return Integer.compare(this.scoreValue, other.scoreValue);
        }
    }

    public static List<ScoreEntry> loadScores(String targetGameType) {
        List<ScoreEntry> scores = new ArrayList<>();
        File file = new File(HIGHSCORE_FILE);
        if (!file.exists()) return scores;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String gameType = parts[0];
                    if (gameType.equalsIgnoreCase(targetGameType)) {
                        String name = parts[1];
                        int scoreValue = Integer.parseInt(parts[2]);
                        String details = parts[3];
                        scores.add(new ScoreEntry(name, scoreValue, details, gameType));
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        Collections.sort(scores);
        return scores;
    }

    public static void addScore(String name, int scoreValue, String details, String gameType) {
        List<ScoreEntry> allScores = new ArrayList<>();
        File file = new File(HIGHSCORE_FILE);
        
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 4) {
                        allScores.add(new ScoreEntry(parts[1], Integer.parseInt(parts[2]), parts[3], parts[0]));
                    }
                }
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
            }
        }

        allScores.add(new ScoreEntry(name == null || name.trim().isEmpty() ? "Anonymous" : name.trim(), scoreValue, details, gameType));
        Collections.sort(allScores);

        try (PrintWriter writer = new PrintWriter(new FileWriter(HIGHSCORE_FILE))) {
            for (ScoreEntry entry : allScores) {
                writer.println(entry.gameType + "," + entry.name + "," + entry.scoreValue + "," + entry.details);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}