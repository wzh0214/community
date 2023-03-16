package com.wzh.community.entity;

import java.nio.channels.Pipe;
import java.util.Date;

/**
 * @author wzh
 * @data 2022/8/1 -18:32
 */
public class LoginTicket {
    private int id;
    private int userId;
    private String ticket; // 登陆凭证
    private int status; // 状态 0是没过期 1是过期
    private Date expired; // 过期时间

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getExpired() {
        return expired;
    }

    public void setExpired(Date expired) {
        this.expired = expired;
    }

    @Override
    public String toString() {
        return "LoginTicket{" +
                "id=" + id +
                ", userId=" + userId +
                ", ticket='" + ticket + '\'' +
                ", status=" + status +
                ", expired=" + expired +
                '}';
    }
}
