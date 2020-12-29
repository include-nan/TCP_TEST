package com.ouc.tcp.test;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_PACKET;

import java.util.TimerTask;

/**
 * @author zjn12
 * @email zjn1210@outlook.com
 * @date 2020/12/27
 * @time 0:29
 */
public class Send_Window extends Slide_Window {

    private final UDT_Timer[] timers = new UDT_Timer[size];

    public Send_Window(Client client) {
        super(client);
    }

    public void sendPacket(TCP_PACKET packet) {
        System.out.println(packet.getTcpH().getTh_seq());
        //在窗口中初始化这个包的相关数据
        int index = nextseqnum % size;
        packets[index] = packet;
        isAck[index] = false;

        timers[index] = new UDT_Timer();
        UDT_RetransTask task = new UDT_RetransTask(client, packet);
        timers[index].schedule(task, 3000, 3000);

        nextseqnum++;
        //设置错误控制标志
        //0.信道无差错  //1.只出错     //2.只丢包     //3.只延迟
        //4.出错/丢包  //5.出错/延迟   //6.丢包/延迟  //7.出错/丢包/延迟
        packet.getTcpH().setTh_eflag((byte) 7);
        client.send(packet);
    }

    public void recvPacket(TCP_PACKET packet) {
        int ack = packet.getTcpH().getTh_ack();      //ack相当于是序号*100+1
        System.out.println("received ack : " + ack);
        if (ack >= base && ack <= base + size) {
            int index = ack % size;
            if (timers[index] != null)
                timers[index].cancel();
            isAck[index] = true;

            System.out.println("the send is : ");
            System.out.println("\tindex = " + index + " base = " + base);
            System.out.println("\tnextseqnum = " + nextseqnum + " end = " + end);

            if (ack == base) {
                //收到的包是窗口的第一个包，将窗口下沿移到下一个没有ack的包位置
                int i;
                for (i = base; i <= nextseqnum && isAck[i % size]; i++) {
                    packets[i % size] = null;
                    isAck[i % size] = false;
                    if (timers[i % size] != null) {
                        timers[i % size].cancel();
                        timers[i % size] = null;
                    }
                }
                base = Math.min(i, nextseqnum);
                System.out.println("new base is " + base);
                end = base + size - 1;
            }
        }
    }
}
