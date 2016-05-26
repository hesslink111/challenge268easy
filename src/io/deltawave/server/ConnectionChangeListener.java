package io.deltawave.server;

/**
 * Created by will on 5/25/16.
 */
public interface ConnectionChangeListener {

    public void onConnect(ConnectedClient client);

    public void onDisconnect(ConnectedClient client);

}
