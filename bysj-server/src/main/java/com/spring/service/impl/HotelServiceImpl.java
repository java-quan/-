package com.spring.service.impl;

import com.base.ServiceBase;
import com.spring.dao.HotelMapper;
import com.spring.entity.Hotel;
import com.spring.service.HotelService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("HotelService")
public class HotelServiceImpl extends ServiceBase<Hotel> implements HotelService {
    @Resource
    private HotelMapper dao;

    @Override
    protected HotelMapper getDao() {
        return dao;
    }
}
