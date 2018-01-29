package com.nowcoder.service;

import com.nowcoder.dao.LoginTicketDao;
import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.LoginTicket;
import com.nowcoder.model.User;
import com.nowcoder.util.WendaUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.Msg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by nowcoder on 2016/7/2.
 */
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private LoginTicketDao loginTicketDao;

    public Map<String ,String> register(String username,String password){
        Map<String,String> map=new HashMap<String, String>();
        if(StringUtils.isBlank(username)){
            map.put("msg","用户名不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("msg","密码不能为空！");
            return map;
        }
        User user=userDAO.selectByName(username);
        if(user!=null){
            map.put("msg","该用户名已被注册！");
            return map;
        }
        user=new User();
        user.setName(username);
        user.setHeadUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setSalt(UUID.randomUUID().toString().substring(0,5));
        user.setPassword(WendaUtil.MD5(password+user.getSalt()));
        userDAO.addUser(user);
        String ticket=addLoginTicket(user.getId());
        map.put("ticket",ticket);
        return map;
    }

    public Map<String,String> login(String username, String password){
        Map<String,String> map=new HashMap<String, String>();
        if(StringUtils.isBlank(username)){
            map.put("msg","用户名不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("msg","密码不能为空！");
            return map;
        }
        User user = userDAO.selectByName(username);
        if(user==null){
            map.put("msg","该用户不存在！");
            return map;
        }
        if(!WendaUtil.MD5(password+user.getSalt()).equals(user.getPassword())){
            map.put("msg","密码错误！");
            return  map;
        }
        String ticket=addLoginTicket(user.getId());
        map.put("ticket",ticket);
        return map;
    }


    public String addLoginTicket(int userId){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(userId);
        Date now = new Date();
        now.setTime(3600*24*100+now.getTime());
        loginTicket.setExpired(now);
        loginTicket.setStatus(0);
        loginTicket.setTicket(UUID.randomUUID().toString().replace("-",""));
        loginTicketDao.addTicket(loginTicket);
        return loginTicket.getTicket();
    }

    public void logout(String ticket){
        loginTicketDao.updateStatus(ticket,1);
    }

    public User gerUser(int id){
        return userDAO.selectById(id);
    }
}
