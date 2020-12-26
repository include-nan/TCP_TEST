package com.ouc.tcp.test;

import java.util.zip.CRC32;

import com.ouc.tcp.message.TCP_HEADER;
import com.ouc.tcp.message.TCP_PACKET;

/**
 * @author zjn12
 * @email zjn1210@outlook.com
 * @date 2020/12/8
 * @time 18:09
 */
public class CheckSum {
    /**
     * seq 序号
     * ack 确认好
     * sum 校验和
     *
     * @return checksum
     */
    /*计算TCP报文段校验和：只需校验TCP首部中的seq、ack和sum，以及TCP数据字段*/
    public static short computeChkSum(TCP_PACKET tcpPack) {
        int checkSum = 0;

        TCP_HEADER head = tcpPack.getTcpH();

        int seq = head.getTh_seq();
        int ack = head.getTh_ack();
        int[] data = (tcpPack.getTcpS().getData());

        for (int datum : data) {
            checkSum += datum;
        }
        checkSum += seq;
        checkSum += ack;

        checkSum = (checkSum & 0xffff) + (checkSum >> 16);

        return (short) checkSum;
    }
}
