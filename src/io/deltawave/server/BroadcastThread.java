package io.deltawave.server;

import java.io.IOException;
import java.net.*;

/**
 * Created by will on 5/27/16.
 */
public class BroadcastThread extends Thread {

    private DatagramSocket socket;

    private DatagramPacket packet;

    public BroadcastThread() {

        ClientsList cList = ClientsList.getInstance();

        String message = "111 blackjack " + cList.getClientList().size();

        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            packet = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName("255.255.255.255"), 2000);

            this.start();

        } catch (SocketException e) {
            System.out.println("Could not open broadcast socket");
            e.printStackTrace();
        } catch (UnknownHostException e) {
            System.out.println("Could not create broadcast packet");
            e.printStackTrace();
        }

    }

    @Override
    public void run() {

        while(true) {
            try {
                socket.send(packet);
            } catch (IOException e) {
                System.out.println("Could not send broadcast");
                e.printStackTrace();
            }

            try {
                sleep(10000);
            } catch (InterruptedException e) {
                System.out.println("BroadcastThread sleep interrupted");
                e.printStackTrace();
            }
        }


    }
}
