package com.item.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.item.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已经完成登录
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
   public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();//匹配器

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest)servletRequest;
        HttpServletResponse response=(HttpServletResponse) servletResponse;

         // 1.获取本此的url
        String RequsetURL=request.getRequestURI();
        log.info("拦截到请求：{}",request.getRequestURI());
        //定义不需要处理的路径
        String[] urls=new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**"
        };

        //判断本次请求是否需要处理
        boolean check= check(urls,RequsetURL);
        //如果不需要处理，直接放行
        if(check){
            filterChain.doFilter(request,response);
            log.info("本次请求不需要处理",RequsetURL);
            return;
        }

        //判断登录状态
        if(request.getSession().getAttribute("employee")!=null){
            filterChain.doFilter(request,response);
            log.info("用户已经成功登录，用户ID：{}",request.getSession().getAttribute("employee"));
            return;
        }

        log.info("用户没有登录：{}");
        //如果没有登陆。通过输出流的方式返回数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;


    }
    public boolean check(String[] urls,String requestURL){
        for (String url:urls)
        {
            boolean match=PATH_MATCHER.match(url,requestURL);
            if(match){
                return true;
            }

        }
        return false;
    }
}
