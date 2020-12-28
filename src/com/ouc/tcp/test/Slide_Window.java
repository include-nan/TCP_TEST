package com.ouc.tcp.test;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.message.TCP_PACKET;

/**
 * @author zjn12
 * @email zjn1210@outlook.com
 * @date 2020/12/27
 * @time 0:04
 */
public class Slide_Window{
    public Client client;
    public int size = 15;
    public TCP_PACKET[] packets = new TCP_PACKET[size];
    public volatile int base = 0;
    public volatile int nextseqnum = 0;
    public volatile int end = size - 1;
    public volatile int sequence = 1;
    public boolean[] isAck = new boolean[size];

    public Slide_Window(Client client) {
        this.client = client;
    }

    public boolean isFull() {
        return nextseqnum == end;
    }
}
