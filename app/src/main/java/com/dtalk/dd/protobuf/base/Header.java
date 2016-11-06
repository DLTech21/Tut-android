package com.dtalk.dd.protobuf.base;

import com.dtalk.dd.config.SysConstant;
import com.dtalk.dd.utils.Logger;

/**
 * TCP协议的头文件
 *
 * @author dolphinWang
 * @time 2014/04/30
 */
public class Header {



    private int length; // 数据包长度，包括包头

    private short version; // 版本号

    private short flag;

    private short serviceId; // SID

    private short commandId; // CID

    private short seqnum;

    private short reserved; // 保留，可用于如序列号等


    public Header() {

        length = 0;

        version = 0;

        serviceId = 0;

        commandId = 0;

        reserved = 0;

        flag = 0;

        seqnum = 0;

    }


    public short getFlag() {

        return flag;

    }


    public void setFlag(short flag) {

        this.flag = flag;

    }


    public short getSeqnum() {

        return seqnum;

    }


    public void setSeqnum(short seq) {

        this.seqnum = seq;

    }


    /**
     * 头文件的压包函数
     *
     * @return 数据包
     */

    public DataBuffer encode() {
        DataBuffer db = new DataBuffer(SysConstant.PROTOCOL_HEADER_LENGTH);
        db.writeInt(length);
        db.writeShort(version);
        db.writeShort(flag);
        db.writeShort(serviceId);
        db.writeShort(commandId);
        db.writeShort(seqnum);
        db.writeShort(reserved);
        return db;

    }


    /**
     * 头文件的解包函数
     *
     * @param buffer
     */

    public void decode(DataBuffer buffer) {

        if (null == buffer)
            return;
        try {
            length = buffer.readInt();
            version = buffer.readShort();
            flag = buffer.readShort();
            serviceId = buffer.readShort();
            commandId = buffer.readShort();
            seqnum = buffer.readShort();
            reserved = buffer.readShort();
        } catch (Exception e) {
            Logger.e(e.getMessage());
        }
    }


    @Override

    public String toString() {

        return "Header [length=" + length + ", version=" + version + ", flag="

                + flag + ", serviceId=" + serviceId + ", commandId="

                + commandId + ", seq=" + seqnum + ", reserved=" + reserved

                + "]";
    }

    public short getCommandId() {
        return commandId;
    }

    public void setCommandId(short commandID) {
        this.commandId = commandID;
    }

    public short getServiceId() {
        return serviceId;
    }

    public void setServiceId(short serviceID) {
        this.serviceId = serviceID;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public int getReserved() {
        return reserved;
    }

    public void setReserved(short reserved) {
        this.reserved = reserved;
    }
}
