package io.deltawave.cardgame;

/**
 * Created by will on 5/25/16.
 */
public class Card {

    private String suite;
    private String value;
    private int val;

    public Card(String suite, String value, int val) {
        this.suite = suite;
        this.value = value;
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public String getValue() {
        return value;
    }

    public String getSuite() {
        return suite;
    }

    @Override
    public String toString() {
        return suite + " " + value;
    }
}
