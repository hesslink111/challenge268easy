package io.deltawave.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by will on 5/24/16.
 */
public class ClientsList implements MessageListener {

    private static ClientsList cList = null;

    public static ClientsList getInstance() {
        if(cList == null) {
            cList = new ClientsList();
        }
        return cList;
    }

    private ArrayList<ConnectedClient> connectedClients;
    private ArrayList<ConnectionChangeListener> connectionChangeListeners;

    private static String[] names = { "Carrot", "Donut", "Fitzgerald", "Hedgehog", "Chronic",
            "Pelican", "Bookworm", "Magic-Hat", "Godzilla", "Pikachu", "Fuzzy", "Napkin",
            "Slushpuppy", "Johnson", "Rocky", "Edgar", "Rodriguez", "Monty", "Sampson",
            "Teller", "Yogi", "Onion", "Mudflap", "Hammer", "Bud", "Wallaber", "Acron",
            "Dominic", "Sierra", "Amherst", "Flapjack", "Thompson", "Mace", "Carpenter",
            "Laurence", "Mickey", "Bjorn", "Friendly", "Penny", "Barry", "Mulligan",
            "Ivan", "Reginald", "Applesauce", "Bean", "Whitehouse", "Oppenheimer" };

    private ClientsList() {
        connectedClients = new ArrayList<>();
        connectionChangeListeners = new ArrayList<>();
    }

    public ArrayList<ConnectedClient> getClientList() {
        return connectedClients;
    }

    public void addClient(ConnectedClient client) {
        //Set their name
        while(client.getUsername() == null) {
            String randName = getRandomName();
            if(isNameFree(randName)) {
                client.setUsername(randName);
            }
        }

        //Add to list of clients
        connectedClients.add(client);
        client.addMessageListener(this);
        connectionChangeListeners.stream().forEach(ccl -> ccl.onConnect(client));

        client.send("You are " + client.getUsername());
        sendToAll(client.getUsername() + " has joined.");
    }

    public void removeClient(ConnectedClient client) {
        client.disconnect();
        connectedClients.remove(client);
        connectionChangeListeners.stream().forEach(ccl -> ccl.onDisconnect(client));
    }

    public void removeClients(List<ConnectedClient> clientList) {
        clientList.stream().forEach(this::removeClient);
    }

    public void addConnectionChangeListener(ConnectionChangeListener ccl) {
        connectionChangeListeners.add(ccl);
    }

    public void removeConnectionChangeListener(ConnectionChangeListener ccl) {
        connectionChangeListeners.remove(ccl);
    }

    public void sendToAll(String message) {
        connectedClients.forEach(c -> c.send(message));
        System.out.println("To all: " + message);
    }

    @Override
    public void onMessageReceived(ConnectedClient client, String messageType, String messageBody) {

        //Check if they are trying to change their name
        if(messageType.equals("USERNAME")) {
            //Set username
            if(isNameFree(messageBody)) {
                String oldName = client.getUsername();
                client.setUsername(messageBody);
                client.send("You are " + messageBody);
                sendToAll(oldName + " is now known as " + messageBody);
            } else {
                client.send("Name already taken");
            }
        }
    }

    private boolean isNameFree(String name) {
        return name.length() > 0 && connectedClients.stream().noneMatch(c -> c.getUsername().equals(name));
    }

    private String getRandomName() {
        Random random = new Random();
        int index = random.nextInt(ClientsList.names.length);
        return ClientsList.names[index];
    }
}
