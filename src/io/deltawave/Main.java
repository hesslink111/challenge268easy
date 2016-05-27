package io.deltawave;

import io.deltawave.cardgame.blackjack.GameManager;
import io.deltawave.server.BroadcastThread;
import io.deltawave.server.ClientsList;
import io.deltawave.server.Heartbeat;
import io.deltawave.server.ConnectionAccepter;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

    public static void main(String[] args) {

        //Setup game
        GameManager gm = new GameManager();

        //Listen for clients
        ConnectionAccepter ca = new ConnectionAccepter();
        ca.start();

        //No longer avoiding this
        //Send heartbeat to all clients every second
        Timer heartbeatTimer = new Timer();
        heartbeatTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Heartbeat heartbeat = new Heartbeat();
                heartbeat.sendHeartbeat();
            }
        }, 0, 10000);

        //Broadcast existence
        BroadcastThread bct = new BroadcastThread();


    }

}
