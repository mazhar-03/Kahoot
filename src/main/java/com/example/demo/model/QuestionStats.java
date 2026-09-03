package com.example.demo.model;

public class QuestionStats {
    private final int questionNumber;
    private final int answeredCount;
    private final int totalPlayers;
    private final int correctCount;
    private final int incorrectCount;
    private final int correctPercentage;
    private final int pointsAwarded;
    private final int cumulativePoints;

    public QuestionStats(int questionNumber, int answeredCount, int totalPlayers, int correctCount,
                         int incorrectCount, int correctPercentage, int pointsAwarded, int cumulativePoints) {
        this.questionNumber = questionNumber;
        this.answeredCount = answeredCount;
        this.totalPlayers = totalPlayers;
        this.correctCount = correctCount;
        this.incorrectCount = incorrectCount;
        this.correctPercentage = correctPercentage;
        this.pointsAwarded = pointsAwarded;
        this.cumulativePoints = cumulativePoints;
    }

    public int getQuestionNumber() { return questionNumber; }
    public int getAnsweredCount() { return answeredCount; }
    public int getTotalPlayers() { return totalPlayers; }
    public int getCorrectCount() { return correctCount; }
    public int getIncorrectCount() { return incorrectCount; }
    public int getCorrectPercentage() { return correctPercentage; }
    public int getPointsAwarded() { return pointsAwarded; }
    public int getCumulativePoints() { return cumulativePoints; }
}
