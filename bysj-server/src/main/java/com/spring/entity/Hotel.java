package com.spring.entity;


import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "lvyou_hotel")
public class Hotel implements Serializable {
    @GeneratedValue(generator = "JDBC") // 自增的主键映射
    @Id
    @Column(name = "id",insertable=false)
    private Integer id;
    @Column(name = "hotel_name")
    private String hotelName;
    @Column(name = "hotel_address")
    private String hotelAddress;
    @Column(name = "hotel_star")
    private Integer hotelStar;
    @Column(name = "hotel_city")
    private String hotelCity;
    @Column(name = "hotel_money")
    private Integer hotelMoney;
    @Column(name = "hotel_introduce")
    private String hotelIntroduce;
    @Column(name = "hotel_picture")
    private String hotelPicture;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHotelPicture() {
        return hotelPicture;
    }

    public void setHotelPicture(String hotelPicture) {
        this.hotelPicture = hotelPicture;
    }


    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getHotelAddress() {
        return hotelAddress;
    }

    public void setHotelAddress(String hotelAddress) {
        this.hotelAddress = hotelAddress;
    }

    public Integer getHotelStar() {
        return hotelStar;
    }

    public void setHotelStar(Integer hotelStar) {
        this.hotelStar = hotelStar;
    }

    public String getHotelCity() {
        return hotelCity;
    }

    public void setHotelCity(String hotelCity) {
        this.hotelCity = hotelCity;
    }

    public Integer getHotelMoney() {
        return hotelMoney;
    }

    public void setHotelMoney(Integer hotelMoney) {
        this.hotelMoney = hotelMoney;
    }

    public String getHotelIntroduce() {
        return hotelIntroduce;
    }

    public void setHotelIntroduce(String hotelIntroduce) {
        this.hotelIntroduce = hotelIntroduce;
    }
}
