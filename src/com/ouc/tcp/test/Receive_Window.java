package com.ouc.tcp.test;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.message.TCP_PACKET;

import java.util.Vector;

/**
 * @author zjn12
 * @email zjn1210@outlook.com
 * @date 2020/12/27
 * @time 14:58
 */
public class Receive_Window extends Slide_Window {

    public Receive_Window(Client client) {
        super(client);
    }

    public Vector<TCP_PACKET> recvPacket(TCP_PACKET packet) {
        Vector<TCP_PACKET> vector = new Vector<>();
        int seq = packet.getTcpH().getTh_seq();
        int index = seq % size;

        System.out.println("the receive is : ");
        System.out.println("\tseq = " + seq + " index = " + index);
        System.out.println("\tbase = " + base + " end = " + end);

        if (index >= 0) {
            isAck[index] = true;
            packets[index] = packet;
            if (seq == base) {          //收到的包是窗口的第一个包
                int i;
                for (i = base; i <= end && isAck[i % size]; i++) {
                    vector.addElement(packets[i % size]);
                    isAck[i % size] = false;
                    packets[i % size] = null;
                }
                base = i;               //移动窗口位置
                end = base + size - 1;
            }
        }
        return vector;
    }
}
