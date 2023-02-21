package com.item.reggie.filter;

import javax.servlet.http.HttpServletRequest;

public class CheckFilter {

    public static boolean checkemployee (HttpServletRequest httpServletRequest){
        if(httpServletRequest.getSession().getAttribute("employee")==null)
        {
            return false;
        }
        return true;
    }
}
