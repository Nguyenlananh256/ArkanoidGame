package com.arkanoid;

import java.io.*;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PrimitiveIterator;

public class HighScoreManager {
    private static HighScoreManager instance;

    private List<Score> scores = new ArrayList<>();

    private HighScoreManager() {
        loadScores();
    }

    public static HighScoreManager getInstance() {
        if (instance == null) {
            instance = new HighScoreManager();
        }
        return instance;
    }

    public List<Score> getScores() {
        return scores;
    }

    public void addScore(String name, int score) {
        if (isHighScore(score)) {
            Score newScore = new Score(name, score);
            scores.add(newScore);
            Collections.sort(scores);
            if (scores.size() > GameConstants.MAX_SCORES) {
                scores.remove(GameConstants.MAX_SCORES);
            }
            saveScores();
        }
    }

    public void loadScores() {
        scores.clear();
        File file = new File(GameConstants.HIGH_SCORES_FILE);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    scores.add(new Score(parts[0], Integer.parseInt(parts[1])));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveScores() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(GameConstants.HIGH_SCORES_FILE))) {
            for (Score s : scores) {
                bw.write(s.name + "," + s.score);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isHighScore(int score) {
        if (scores.size() < 5) {
            return true;
        }
        return score > scores.get(GameConstants.MAX_SCORES - 1).score;
    }
}

class Score implements Comparable<Score> {
    public String name;
    public int score;

    public Score(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public int compareTo(Score o) {
        return o.score - this.score;
    }
}
