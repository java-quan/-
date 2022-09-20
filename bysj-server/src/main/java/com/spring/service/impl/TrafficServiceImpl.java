package com.spring.service.impl;

import com.base.ServiceBase;
import com.spring.dao.TrafficMapper;
import com.spring.entity.Traffic;
import com.spring.service.TrafficService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("TrafficService")
public class TrafficServiceImpl extends ServiceBase<Traffic> implements TrafficService {
    @Resource
    private TrafficMapper dao;

    @Override
    protected TrafficMapper getDao() {
        return dao;
    }
}
