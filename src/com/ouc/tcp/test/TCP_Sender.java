package com.ouc.tcp.test;

import com.ouc.tcp.client.TCP_Sender_ADT;
import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_PACKET;

/**
 * @author zjn12
 * @email zjn1210@outlook.com
 * @date 2020/12/8
 * @time 18:13
 */
public class TCP_Sender extends TCP_Sender_ADT {

    private TCP_PACKET tcpPack;    //待发送的TCP数据报
    // 在访问volatile变量时不会执行加锁操作，因此也就不会使执行线程阻塞
    private volatile int flag = 0;

    private Send_Window send_window = new Send_Window(client);

    /*构造函数*/
    public TCP_Sender() {
        super();    //调用超类构造函数
        super.initTCP_Sender(this);        //初始化TCP发送端
    }

    @Override
    //可靠发送（应用层调用）：封装应用层数据，产生TCP数据报；需要修改
    public void rdt_send(int dataIndex, int[] appData) {

        //生成TCP数据报（设置序号和数据字段/校验和),注意打包的顺序
        //tcpH.setTh_seq(dataIndex * appData.length + 1);//包序号设置为字节流号：
        tcpH.setTh_seq(dataIndex);
        tcpS.setData(appData);
        tcpPack = new TCP_PACKET(tcpH, tcpS, destinAddr);

        tcpH.setTh_sum(CheckSum.computeChkSum(tcpPack));
        tcpPack.setTcpH(tcpH);

        while (send_window.isFull()) ;

        TCP_PACKET packet = new TCP_PACKET(tcpH, tcpS, destinAddr);
        try {
            send_window.sendPacket(packet.clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

    }

    // 对 udt_send() waitAck() 函数进行重写
    @Override
    //不可靠发送：将打包好的TCP数据报通过不可靠传输信道发送；仅需修改错误标志
    public void udt_send(TCP_PACKET stcpPack) {
        //设置错误控制标志
        //0.信道无差错  //1.只出错     //2.只丢包     //3.只延迟
        //4.出错/丢包  //5.出错/延迟   //6.丢包/延迟  //7.出错/丢包/延迟
        tcpH.setTh_eflag((byte) 7);
        //System.out.println("to send: "+stcpPack.getTcpH().getTh_seq());
        //发送数据报
        client.send(stcpPack);
    }

    @Override
    //需要修改
    public void waitACK() {
        //循环检查ackQueue
        //循环检查确认号对列中是否有新收到的ACK
//        while (true) {
//            if (!ackQueue.isEmpty()) {
//                int currentAck = ackQueue.poll();
//                // System.out.println("CurrentAck: "+currentAck);
//                if (currentAck == tcpPack.getTcpH().getTh_seq()) {
//                    System.out.println("Clear: " + tcpPack.getTcpH().getTh_seq());
//                    flag = 1;
//                    udt_timer.cancel();
//                    break;
//                } else {
//                    System.out.println("Retransmit: " + tcpPack.getTcpH().getTh_seq());
//                    udt_send(tcpPack);
//                    flag = 0;
//                }
//            }
//        }
    }

    @Override
    //接收到ACK报文：检查校验和，将确认号插入ack队列;NACK的确认号为－1；不需要修改
    public void recv(TCP_PACKET recvPack) {

        send_window.recvPacket(recvPack);    //使用窗口来处理ack

        System.out.println("Receive ACK Number： " + recvPack.getTcpH().getTh_ack());
        ackQueue.add(recvPack.getTcpH().getTh_ack());
        System.out.println();
    }
}
