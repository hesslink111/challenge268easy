package io.deltawave.cardgame.blackjack;

import io.deltawave.cardgame.Card;
import io.deltawave.server.ConnectedClient;
import io.deltawave.server.MessageListener;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by will on 5/25/16.
 */
public class Player {

    private ConnectedClient client;
    private ArrayList<Card> hand;
    private String name;

    private static String[] names = { "Carrot", "Donut", "Fitzgerald", "Hedgehog", "Chronic",
            "Pelican", "Bookworm", "Magic-Hat", "Godzilla", "Pikachu", "Fuzzy", "Napkin",
            "Slushpuppy", "Johnson", "Rocky", "Edgar", "Rodriguez", "Monty", "Sampson",
            "Teller", "Yogi", "Onion", "Mudflap", "Hammer", "Bud" };

    public Player(ConnectedClient client) {
        this.client = client;
        hand = new ArrayList<>();

        chooseRandomName();
        client.send("You are " + name);
    }

    private void chooseRandomName() {
        Random random = new Random();
        int index = random.nextInt(Player.names.length);
        name = Player.names[index];
    }

    public String getName() {
        return name;
    }

    public void giveCard(Card c) {
        hand.add(c);
    }

    public void removeCard(Card c) {
        hand.remove(c);
    }

    public void resetHand() {
        hand.clear();
    }

    public ArrayList<Card> getCards() {
        return hand;
    }

    public int getMinHandValue() {
        return hand.stream()
                .map(c -> c.getVal())
                .map(val -> val>10?10:val)
                .mapToInt(val -> val.intValue())
                .sum();
    }

    public int getMaxHandValue() {
        return hand.stream()
                .map(c -> c.getVal())
                .map(val -> val>10?10:val)
                .map(val -> val==1?11:val)
                .mapToInt(val -> val.intValue())
                .sum();
    }

    public int getBestHandValue() {
        int max = getMaxHandValue();
        int min = getMinHandValue();
        return max <= 21 ? max : min;
    }

    public void sendMessage(String message) {
        client.send(message);
    }

}
