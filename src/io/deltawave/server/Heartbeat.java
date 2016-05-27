package io.deltawave.server;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by will on 5/24/16.
 */
public class Heartbeat implements MessageListener {

    private ClientsList cList;

    private ArrayList<ConnectedClient> notReceivedHeartbeatReply;

    public Heartbeat() {
        this.cList = ClientsList.getInstance();
        notReceivedHeartbeatReply = new ArrayList<>(cList.getClientList());
    }

    public void sendHeartbeat() {
        //After 2 seconds, check that everyone responded
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                cList.removeClients(notReceivedHeartbeatReply, "No heartbeat reply");
            }
        }, 2000);

        //Send the actual heartbeat
        cList.getClientList().parallelStream().forEach(this::sendHeartbeat);
    }

    private void sendHeartbeat(ConnectedClient connectedClient) {
        connectedClient.send("000 Heartbeat");
        connectedClient.addMessageListener(this);
    }

    @Override
    public void onMessageReceived(ConnectedClient client, String messageType, String messageBody) {
        if (messageType.equals("001")) {
            //Received heartbeat reply
            client.removeMessageListener(this);
            notReceivedHeartbeatReply.remove(client);
        }
    }
}
