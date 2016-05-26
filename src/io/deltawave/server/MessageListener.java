package io.deltawave.server;

/**
 * Created by will on 5/24/16.
 */
public interface MessageListener {

    public void onMessageReceived(ConnectedClient client, String messageType, String messageBody);

}
