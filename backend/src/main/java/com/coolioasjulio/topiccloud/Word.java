package com.coolioasjulio.topiccloud;

import java.util.Objects;

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
        int compare = Double.compare(value, o.value);
        return compare == 0 ? text.compareTo(o.text) : compare;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word = (Word) o;
        return Double.compare(word.value, value) == 0 &&
                text.equals(word.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }
}
