package io.deltawave.cardgame.blackjack;

import io.deltawave.cardgame.Card;
import io.deltawave.cardgame.Deck;
import io.deltawave.server.ClientsList;
import io.deltawave.server.ConnectedClient;
import io.deltawave.server.ConnectionChangeListener;
import io.deltawave.server.MessageListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by will on 5/25/16.
 */
public class GameManager implements ConnectionChangeListener, MessageListener {

    private ClientsList cList;

    private HashMap<ConnectedClient, Player> playerMap;

    private GameThread gameThread;


    public GameManager() {
        this.cList = ClientsList.getInstance();
        cList.addConnectionChangeListener(this);

        playerMap = new HashMap<>();

    }

    public void addAllPlayers(ArrayList<ConnectedClient> clients) {
        clients.stream().forEach(c -> addPlayer(c));
    }

    public void addPlayer(ConnectedClient client) {

        client.addMessageListener(this);
    }

    @Override
    public void onConnect(ConnectedClient client) {
        //Deal in if game currently running
        Player player = new Player(client);
        playerMap.put(client, player);
        client.addMessageListener(this);
    }

    @Override
    public void onDisconnect(ConnectedClient client) {
        //take cards away
        client.removeMessageListener(this);
        Player player = playerMap.get(client);
        playerMap.remove(client);
        if(gameThread != null) {
            gameThread.removePlayer(player);
        }
    }

    @Override
    public void onMessageReceived(ConnectedClient client, String messageType, String messageBody) {

        if(messageType.equals("START")) {
            //Start the game (if not started already

            if (gameThread == null || !gameThread.isPlaying()) {
                //Start the game in a new thread
                gameThread = new GameThread(playerMap.values());
                gameThread.start();

            } else {
                client.send("Bub, we're already playing.");
            }

        } else {
            if(gameThread != null) {
                Player player = playerMap.get(client);
                gameThread.checkMessageReceivedFromWaitingPlayer(player, messageType, messageBody);
            }
        }
    }

}
