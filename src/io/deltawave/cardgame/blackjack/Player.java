package io.deltawave.cardgame.blackjack;

import io.deltawave.cardgame.Card;
import io.deltawave.server.ConnectedClient;
import io.deltawave.server.MessageListener;

import java.util.ArrayList;

/**
 * Created by will on 5/25/16.
 */
public class Player implements MessageListener {

    private ConnectedClient client;
    private ArrayList<Card> hand;

    private String mode;

    public Player(ConnectedClient client) {
        this.client = client;
        client.addMessageListener(this);

        hand = new ArrayList<>();

        //Default mode
        mode = "PLAYER";
    }

    public String getUsername() {
        return client.getUsername();
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

    @Override
    public void onMessageReceived(ConnectedClient client, String messageType, String messageBody) {
        if(messageType.equals("SETMODE")) {
            if(messageBody.equals("PLAYER")) {
                mode = messageBody;
                sendMessage("You're mode is set to PLAYER");
            } else if(messageBody.equals("SPECTATOR")) {
                mode = messageBody;
                sendMessage("You're mode is set to SPECTATOR");
            }
        }
    }

    public String getMode() {
        return mode;
    }
}
