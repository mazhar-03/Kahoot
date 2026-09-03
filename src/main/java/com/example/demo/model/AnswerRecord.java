package com.example.demo.model;

public class AnswerRecord {
    private final int questionIndex;
    private final String questionText;
    private final int selectedAnswerIndex;
    private final String selectedAnswerText;
    private final int correctAnswerIndex;
    private final String correctAnswerText;
    private final boolean correct;
    private final int pointsEarned;

    public AnswerRecord(int questionIndex, String questionText, int selectedAnswerIndex, String selectedAnswerText,
                        int correctAnswerIndex, String correctAnswerText, boolean correct, int pointsEarned) {
        this.questionIndex = questionIndex;
        this.questionText = questionText;
        this.selectedAnswerIndex = selectedAnswerIndex;
        this.selectedAnswerText = selectedAnswerText;
        this.correctAnswerIndex = correctAnswerIndex;
        this.correctAnswerText = correctAnswerText;
        this.correct = correct;
        this.pointsEarned = pointsEarned;
    }

    public int getQuestionIndex() { return questionIndex; }
    public String getQuestionText() { return questionText; }
    public int getSelectedAnswerIndex() { return selectedAnswerIndex; }
    public String getSelectedAnswerText() { return selectedAnswerText; }
    public int getCorrectAnswerIndex() { return correctAnswerIndex; }
    public String getCorrectAnswerText() { return correctAnswerText; }
    public boolean isCorrect() { return correct; }
    public int getPointsEarned() { return pointsEarned; }
}
