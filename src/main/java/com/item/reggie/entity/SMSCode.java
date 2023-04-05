package com.item.reggie.entity;

import lombok.Data;

@Data
public class SMSCode {
    private String phone;
    private  String code;
    private long time;
}
