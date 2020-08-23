package com.coolioasjulio.topiccloud;

public class Word {
    public final String word;
    public final double weight;

    public Word(String word, double weight) {
        this.word = word;
        this.weight = weight;
    }

    @Override
    public String toString() {
        return word + " - " + weight;
    }
}
