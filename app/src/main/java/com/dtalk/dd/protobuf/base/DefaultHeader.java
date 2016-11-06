package com.dtalk.dd.protobuf.base;

import com.dtalk.dd.config.SysConstant;
import com.dtalk.dd.imservice.support.SequenceNumberMaker;
import com.dtalk.dd.utils.Logger;

public class DefaultHeader extends Header {

    public DefaultHeader(int serviceId, int commandId) {
        setVersion((short) SysConstant.PROTOCOL_VERSION);
        setFlag((short) SysConstant.PROTOCOL_FLAG);
        setServiceId((short)serviceId);
        setCommandId((short)commandId);
        short seqNo = SequenceNumberMaker.getInstance().make();
        setSeqnum(seqNo);
        setReserved((short)SysConstant.PROTOCOL_RESERVED);

    }
}
