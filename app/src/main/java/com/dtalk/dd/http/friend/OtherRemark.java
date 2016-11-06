package com.dtalk.dd.http.friend;

/**
 * Created by Donal on 16/7/4.
 */
public class OtherRemark {
    private String version;
    private String delstatus;
    private String comment;
    private String remarkname;

    public String getRemarkname() {
        return remarkname;
    }

    public void setRemarkname(String remarkname) {
        this.remarkname = remarkname;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDelstatus() {
        return delstatus;
    }

    public void setDelstatus(String delstatus) {
        this.delstatus = delstatus;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
