package com.wzh.community.util;

import com.wzh.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * @author wzh
 * @data 2022/8/2 -20:53
 *
 * 持有用户信息，用于代替session对象
 */
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUsers(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }


    public void clear() {
        users.remove();
    }
}
