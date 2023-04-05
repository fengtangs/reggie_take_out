package com.item.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.reggie.entity.SMSCode;

public interface SMSCodeService extends IService<SMSCode> {
    public String generatorcode(String tele);

    public boolean checkcode(SMSCode smsCode);
}
