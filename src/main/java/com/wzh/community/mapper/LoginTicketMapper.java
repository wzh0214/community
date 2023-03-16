package com.wzh.community.mapper;

import com.wzh.community.entity.LoginTicket;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wzh
 * @data 2022/8/1 -18:34
 */
@Mapper
@Deprecated
public interface LoginTicketMapper {

    int insertLoginTicket(LoginTicket loginTicket);

    int updateStatus(String ticket, int status);

    LoginTicket selectByTicket(String ticket);


}
