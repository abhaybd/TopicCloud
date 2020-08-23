package com.coolioasjulio.topiccloud;

public class Word implements Comparable<Word> {
    public final String text;
    public final double value;

    public Word(String text, double value) {
        this.text = text;
        this.value = value;
    }



    @Override
    public String toString() {
        return text + " - " + value;
    }

    @Override
    public int compareTo(Word o) {
        return Double.compare(value, o.value);
    }
}
