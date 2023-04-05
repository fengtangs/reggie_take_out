package com.item.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.reggie.entity.SMSCode;
import com.item.reggie.mapper.SMSCodeMapper;
import com.item.reggie.service.SMSCodeService;
import com.item.reggie.utils.Codeutils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class SMSCodeServiceImpl extends ServiceImpl<SMSCodeMapper, SMSCode> implements SMSCodeService {

    @Autowired
    private Codeutils codeutils;

    private  String [] path ={"00000","0000","000","00","0",""};

    //    @Cacheable(value = "smscache",key = "#tele")
    @CachePut(value = "smsCode",key = "#tele")
    public  String generatorcode(String tele){

        String code =codeutils.generatorcode(tele);
                return code;

    }

    @Override
    public boolean checkcode(SMSCode smsCode) {
        String code =smsCode.getCode();
        String cachecode = codeutils.getcode(smsCode.getPhone());

        return code.equals(cachecode);
    }


}
