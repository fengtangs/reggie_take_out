package com.item.reggie.utils;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class Codeutils {


    private  String [] path ={"00000","0000","000","00","0"," "};

    //    @Cacheable(value = "smscache",key = "#tele")
    public  String generatorcode(String tele){

        //生成验证码


        int hasrh= tele.hashCode();
        int encryption=123456;
        long result = hasrh^encryption;
        long nowtime = System.currentTimeMillis();
        result=result^nowtime;
        long code = result%100000;
        code =code<0? -code: code;
        String codeStr =code+"";
        int len=codeStr.length();
        return path[len-1]+codeStr;

    }



    @Cacheable(value = "smsCode",key = "#tele")
    public String getcode(String tele){

        return null;

    }
}
