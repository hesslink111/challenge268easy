package io.deltawave.server;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by will on 5/24/16.
 */
public class ClientsList {

    private static ClientsList cList = null;

    public static ClientsList getInstance() {
        if(cList == null) {
            cList = new ClientsList();
        }
        return cList;
    }

    private ArrayList<ConnectedClient> connectedClients;

    private ArrayList<ConnectionChangeListener> connectionChangeListeners;

    private ClientsList() {
        connectedClients = new ArrayList<>();
        connectionChangeListeners = new ArrayList<>();
    }

    public ArrayList<ConnectedClient> getClientList() {
        return connectedClients;
    }

    public void addClient(ConnectedClient client) {
        connectedClients.add(client);
        connectionChangeListeners.stream().forEach(ccl -> ccl.onConnect(client));
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
}
