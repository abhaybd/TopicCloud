package com.coolioasjulio.topiccloud;

public class Word implements Comparable<Word> {
    public String text;
    public double value;
    public double score;

    public Word(String text, double value, double score) {
        this.text = text;
        this.value = value;
        this.score = score;
    }

    @Override
    public String toString() {
        return String.format("Word(text=%s, value=%s, score=%s)", text, value, score);
    }

    @Override
    public int compareTo(Word o) {
        return Double.compare(value, o.value);
    }
}
