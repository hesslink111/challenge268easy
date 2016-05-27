package io.deltawave.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
            "Ivan", "Reginald", "Applesauce", "Bean", "Whitehouse", "Oppenheimer",
            "Breadcrumb", "Walnut", "Pandora", "Teakettle", "Housemouse", "Giraffe"};

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

    public void removeClient(ConnectedClient client, String message) {
        connectedClients.remove(client);
        if(client.isConnected()) {
            client.disconnect(message);
            connectionChangeListeners.stream().forEach(ccl -> ccl.onDisconnect(client));
            sendToAll(client.getUsername() + " has disconnected.");
        }
    }

    public void removeClients(List<ConnectedClient> clientList, String message) {
        clientList.stream().forEach(c -> removeClient(c, message));
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
        if(messageType.equals("SETUSERNAME")) {
            //Set username
            if(isNameFree(messageBody)) {
                String oldName = client.getUsername();
                client.setUsername(messageBody);
                client.send("You are " + messageBody);
                sendToAll(oldName + " is now known as " + messageBody);
            } else {
                client.send("Name not available");
            }
        } else if(messageType.equals("LISTUSERS")) {
            connectedClients.stream().forEach(c -> client.send(c.getUsername()));
        } else if(messageType.equals("SENDMESSAGE") || messageType.equals("SENDMSG")) {
            String[] bodyParts = messageBody.split(" ");
            if(bodyParts.length > 1) {
                String recipient = bodyParts[0];
                String message = messageBody.substring(recipient.length()+1, messageBody.length());
                //find recipient
                Optional<ConnectedClient> recipientClient = connectedClients.stream().filter(c -> c.getUsername().equals(recipient)).findAny();
                if(recipientClient.isPresent()) {
                    recipientClient.get().send(client.getUsername() + " -> " + recipientClient.get().getUsername() + ": " + message);
                    client.send("Message sent");
                } else {
                    client.send("Message could not be sent: no recipient by that name");
                }
            }
        } else if(messageType.equals("SENDALL")) {
            connectedClients.stream().forEach(c -> c.send(client.getUsername() + " -> all: " + messageBody));
            client.send("Message sent to all");
        }
    }

    private boolean isNameFree(String name) {
        return name.length() > 0 && name.matches("\\S+") && connectedClients.stream().noneMatch(c -> c.getUsername().equals(name));
    }

    private String getRandomName() {
        Random random = new Random();
        int index = random.nextInt(ClientsList.names.length);
        return ClientsList.names[index];
    }
}
