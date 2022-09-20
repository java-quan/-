package com.spring.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "lvyou_traffic")
@Data
public class Traffic implements Serializable {
    @GeneratedValue(generator = "JDBC") // 自增的主键映射
    @Id
    @Column(name = "id",insertable=false)
    private Integer id;

    @Column(name = "traffic_name")
    private String trafficName;
    @Column(name = "traffic_money")
    private Integer trafficMoney;
    @Column(name = "traffic_remark")
    private String trafficRemark;
    @Column(name = "traffic_picture")
    private String trafficPicture;


    private static final long serialVersionUID = 1L;


}
