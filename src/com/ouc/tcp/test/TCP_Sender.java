package com.ouc.tcp.test;

import com.ouc.tcp.client.TCP_Sender_ADT;
import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.*;
import com.ouc.tcp.tool.TCP_TOOL;

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

    UDT_Timer udt_timer;
    UDT_RetransTask udt_retransTask;

    Send_Window send_window = new Send_Window();
    Slide_Window<Send_Window> send_SlideWindows = new Slide_Window<>(5);

    /*构造函数*/
    public TCP_Sender() {
        super();    //调用超类构造函数
        super.initTCP_Sender(this);        //初始化TCP发送端
    }

    @Override
    //可靠发送（应用层调用）：封装应用层数据，产生TCP数据报；需要修改
    public void rdt_send(int dataIndex, int[] appData) {

        //生成TCP数据报（设置序号和数据字段/校验和),注意打包的顺序
        tcpH.setTh_seq(dataIndex * appData.length + 1);//包序号设置为字节流号：
        tcpS.setData(appData);
        tcpPack = new TCP_PACKET(tcpH, tcpS, destinAddr);

        tcpH.setTh_sum(CheckSum.computeChkSum(tcpPack));
        tcpPack.setTcpH(tcpH);

        send_window = new Send_Window(tcpPack.getTcpH().getTh_seq(), false);

        if (send_SlideWindows.addLast(send_window)) {

            System.out.println("the first of slideWindow is " + send_SlideWindows.getFirst().getThe_seq());

            //发送TCP数据报
            udt_send(tcpPack);
            flag = 0;
            // 设置计时器
            udt_timer = new UDT_Timer();
            udt_retransTask = new UDT_RetransTask(client, tcpPack);
            udt_timer.schedule(udt_retransTask, 3000, 3000);

            //等待ACK报文
            //waitACK();
            while (flag == 0) {
            }
        }
    }

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
//        if (!ackQueue.isEmpty()) {
//            int currentAck = ackQueue.poll();
//            // System.out.println("CurrentAck: " + currentAck);
//            for (int i = 0; i < send_SlideWindows.getLength(); i++) {
//                if (currentAck > 0) {
//                    System.out.println("\n the size of send_SlideWindows is " + send_SlideWindows.getSize());
//                    if (!send_SlideWindows.isEmpty()) {
//                        if (currentAck == send_SlideWindows.get(i).getThe_seq() && !send_SlideWindows.get(i).isAcked()) {
//                            System.out.println("Clear: " + tcpPack.getTcpH().getTh_seq());
//                            flag = 1;
//                            udt_timer.cancel();
//                            send_SlideWindows.get(i).setAcked(true);
//                            break;
//                        } else if (send_SlideWindows.get(i).isAcked()) {
//                            break;
//                        }
//                    } else {
//                        System.out.println("Retransmit: " + tcpPack.getTcpH().getTh_seq());
//                        udt_send(tcpPack);
//                        flag = 0;
//                    }
//                }
//            }
//            if (!send_SlideWindows.isEmpty() && send_SlideWindows.getFirst().isAcked()) {
//                send_SlideWindows.removeFirst();
//            }
        if (!ackQueue.isEmpty()) {
            int currentAck = ackQueue.poll();
            System.out.println("\n the size of send_SlideWindows is " + send_SlideWindows.getSize());
            int temp = 0;
            if (currentAck >= send_SlideWindows.getFirst().getThe_seq()) {
                for (int i = 0; i < send_SlideWindows.getSize(); i++) {
                    if (currentAck == send_SlideWindows.get(i).getThe_seq()) {
                        System.out.println("Clear: " + tcpPack.getTcpH().getTh_seq());
                        flag = 1;
                        udt_timer.cancel();
                        send_SlideWindows.get(i).setAcked(true);
                        temp = 1;
                        if (!send_SlideWindows.isEmpty() && send_SlideWindows.getFirst().isAcked()) {
                            send_SlideWindows.removeFirst();
                        }
                        break;
                    }
                }
                if (temp == 0) {
                    System.out.println("Retransmit: " + tcpPack.getTcpH().getTh_seq());
                    udt_send(tcpPack);
                    flag = 0;
                }
            }
        }
    }

    @Override
    //接收到ACK报文：检查校验和，将确认号插入ack队列;NACK的确认号为－1；不需要修改
    public void recv(TCP_PACKET recvPack) {
        System.out.println("Receive ACK Number： " + recvPack.getTcpH().getTh_ack());
        ackQueue.add(recvPack.getTcpH().getTh_ack());
        System.out.println();

        //处理ACK报文
        waitACK();
    }
}
