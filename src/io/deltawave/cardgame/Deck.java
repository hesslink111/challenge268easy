package io.deltawave.cardgame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Stream;

/**
 * Created by will on 5/25/16.
 */
public class Deck {

    ArrayList<Card> cards;

    public Deck() {
        cards = new ArrayList<>();
    }

    public void resetDeck() {
        cards.clear();
        setupDeck();
        shuffleCards();
    }

    public void setupDeck() {
        HashMap<String, Integer> values = new HashMap<>();
        values.put("ACE", 1);
        values.put("TWO", 2);
        values.put("THREE", 3);
        values.put("FOUR", 4);
        values.put("FIVE", 5);
        values.put("SIX", 6);
        values.put("SEVEN", 7);
        values.put("EIGHT", 8);
        values.put("NINE", 9);
        values.put("TEN", 10);
        values.put("JACK", 11);
        values.put("QUEEN", 12);
        values.put("KING", 13);

        ArrayList<String> suites = new ArrayList<>();
        suites.add("SPADES");
        suites.add("CLUBS");
        suites.add("HEARTS");
        suites.add("DIAMONDS");

        values.entrySet().stream().forEach(v -> suites.stream().forEach(s -> cards.add(new Card(s, v.getKey(), v.getValue()))));
    }

    public void shuffleCards() {
        Random random = new Random();

        //Probably no need for this type of thing, but it's just so fun
        Stream.iterate(0, i -> i+1)
                .limit(cards.size()-1)
                .forEach(i -> {
                    int randomIndex = random.nextInt(52);
                    Card randomCard = cards.get(randomIndex);
                    cards.set(randomIndex, cards.get(i));
                    cards.set(i, randomCard);
                });
    }

    public void addAll(ArrayList<Card> cards) {
        cards.addAll(cards);
    }

    public Card getCard() {
        return cards.remove(cards.size()-1);
    }

    public Card peek() {
        return cards.get(cards.size()-1);
    }
}
