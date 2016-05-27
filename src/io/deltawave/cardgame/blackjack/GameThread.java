package io.deltawave.cardgame.blackjack;

import io.deltawave.cardgame.Card;
import io.deltawave.cardgame.Deck;
import io.deltawave.server.ClientsList;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by will on 5/26/16.
 */
public class GameThread extends Thread {

    private ClientsList cList;
    private Deck deck;

    private ArrayList<Player> playingPlayers;
    private ArrayDeque<Player> notDone;
    private ArrayList<Player> nextTurn;

    //State
    private boolean playing;
    private boolean waiting;
    private Player waitingForPlayer;
    private ArrayList<String> waitingForActions;
    private String actionChosen;
    private final Object waitObject = new Object();

    public GameThread(Collection<Player> players) {
        this.cList = ClientsList.getInstance();
        playingPlayers = new ArrayList<>(players);
        notDone = new ArrayDeque<>(players);
        nextTurn = new ArrayList<>();

        deck = new Deck();

        //Default state
        playing = true;
        waiting = false;
        waitingForPlayer = null;
        waitingForActions = new ArrayList<>();
        actionChosen = "";
    }

    public boolean isPlaying() {
        return playing;
    }

    public void removePlayer(Player player) {
        deck.addAll(player.getCards());
        notDone.remove(player);
        nextTurn.remove(player);
        playingPlayers.remove(player);

        //Check if they are being waited on
        synchronized (waitObject) {
            if (waiting && waitingForPlayer == player) {
                //Uh oh
                waiting = false;
                waitObject.notify();
            }
        }

    }

    @Override
    public void run() {
        //Start the game
        System.out.println("Starting game");
        System.out.println("Number of players: " + playingPlayers.size());

        //Reset and shuffle the deck
        deck.resetDeck();

        //Remove all their cards
        playingPlayers.stream().forEach(p -> p.resetHand());

        //Deal two to each player
        System.out.println("Dealing");
        playingPlayers.stream()
                .forEachOrdered(this::giveCardSecret);
        playingPlayers.stream()
                        .forEachOrdered(this::giveCard);

        //Remove all the guys who have reached the limit
        notDone.removeAll(playingPlayers.stream().filter(p -> p.getMinHandValue() >= 21).collect(Collectors.toList()));

        //Keep dealing until all are done
        while(!notDone.isEmpty()) {
            nextTurn.addAll(notDone);
            while(!notDone.isEmpty()) {

                //It is this player's turn now
                Player p = notDone.removeFirst();

                //Take or pass
                if(offerCard(p)) {
                    System.out.println("Player took card");
                    //Check if they have 21
                    if (p.getMinHandValue() >= 21) {
                        //You don't get to play next turn
                        nextTurn.remove(p);
                    }
                } else {
                    System.out.println("Player did not take card");
                    //You can't play next turn either
                    nextTurn.remove(p);
                }

            }
            notDone.addAll(nextTurn);
            nextTurn.clear();
        }

        //Count points and such

        //Find all the guys that busted
        playingPlayers.stream()
                .filter(p -> p.getMinHandValue() > 21)
                .forEachOrdered(p -> cList.sendToAll(p.getUsername() + " has busted: " + p.getMinHandValue()));

        //Find everyone else's score
        playingPlayers.stream()
                .filter(p -> p.getMinHandValue() <= 21)
                .forEachOrdered(p -> cList.sendToAll(p.getUsername() + " has " + p.getBestHandValue()));

        //Finally, reset the game state
        playing = false;
    }


    //True if taken, false otherwise
    private boolean offerCard(Player p) {

        //Offer card (take or pass)
        p.sendMessage("TAKE OR PASS");

        //Wait until they take the card
        waitForPlayerAction(p, "TAKE", "PASS");

        if(actionChosen.equals("TAKE")) {

            giveCard(p);
            return true;

        } else if(actionChosen.equals("PASS")) {
            //Don't give card

            cList.sendToAll("Player " + p.getUsername() + " passes");
            return false;
        }

        return false;
    }

    public void giveCard(Player p) {
        //Get card
        Card c = deck.getCard();

        //Give card
        p.giveCard(c);

        //Tell everyone
        cList.sendToAll("Player " + p.getUsername() + " takes a " + c.getValue() + " of " + c.getSuite());
    }

    public void giveCardSecret(Player p) {
        //Get card
        Card c = deck.getCard();

        //Give card
        p.giveCard(c);

        //Tell player
        p.sendMessage("Secret: " + p.getUsername() + " takes " + c.getValue() + " of " + c.getSuite());
    }

    public void waitForPlayerAction(Player p, String...actions) {
        //Set to wait
        waiting = true;
        waitingForPlayer = p;

        //Add all actions
        for(String action: actions) {
            waitingForActions.add(action);
        }

        //Wait until the action is taken
        synchronized(waitObject) {
            while (waiting) {
                try {
                    waitObject.wait();
                } catch (InterruptedException e) {
                    System.out.println("GameManager wait state interrupted");
                    e.printStackTrace();
                }
            }
        }

        //Clear the waiting-for actions
        waitingForActions.clear();

        //Clear the waiting-for player
        waitingForPlayer = null;
    }

    public void checkMessageReceivedFromWaitingPlayer(Player player, String messageType, String messageBody) {
        synchronized(waitObject) {
            if (waitingForPlayer == player) {

                //Check action
                if (waitingForActions.contains(messageType)) {

                    //Set action chosen
                    actionChosen = messageType;

                    //Wake game thread
                    waiting = false;
                    waitObject.notify();
                }
            }
        }
    }

}
