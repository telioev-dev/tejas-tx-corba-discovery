package com.teliolabs.corba.data.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Circle {
    private String name;
    private String host;
    private Integer port;
    private String vendor;
    private String ems;
    private String emsVersion;
    private String userName;
    private int meHowMuch;
    private int ptpHowMuch;
    private int sncHowMuch;
    private int topologyHowMuch;
    private String password;
    private String nameService;

    @Override
    public String toString() {
        return "Circle{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", vendor='" + vendor + '\'' +
                ", ems='" + ems + '\'' +
                ", emsVersion='" + emsVersion + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", nameService='" + nameService + '\'' +
                '}';
    }
}
