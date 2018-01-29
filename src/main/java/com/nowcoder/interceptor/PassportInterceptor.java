package com.nowcoder.interceptor;

import com.nowcoder.dao.LoginTicketDao;
import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.HostHolder;
import com.nowcoder.model.LoginTicket;
import com.nowcoder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class PassportInterceptor implements HandlerInterceptor {
    @Autowired
    LoginTicketDao loginTicketDao;

    @Autowired
    UserDAO userDAO;

    @Autowired
    HostHolder hostHolder;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket=null;
        if(request.getCookies()!=null){
            for(Cookie cookie:request.getCookies()){
                if(cookie.getName().equals("ticket")){
                    ticket=cookie.getValue();
                    break;
                }
            }
        }
        if(ticket!=null){
            LoginTicket loginTicket=loginTicketDao.selectByTicket(ticket);
            if(loginTicket==null||loginTicket.getExpired().before(new Date())||loginTicket.getStatus()!=0){
                return true;
            }
            //通过ticket找用户
            User user=userDAO.selectById(loginTicket.getUserId());
            //往本地线程threadLocal中加入用户
            hostHolder.setUsers(user);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //放入上下文进行渲染，变量名为user
        if(modelAndView !=null){
            modelAndView.addObject("user",hostHolder.getUser());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
