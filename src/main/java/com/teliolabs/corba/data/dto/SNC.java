package com.teliolabs.corba.data.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
public class SNC {
    private int pk;
    private String sncId;
    private String sncName;
    private Long circuitId;
    private Integer srfId;
    private short sncRate;
    private String vCat;
    private String aEndMe;
    private String aEndMeLabel;
    private String aEndPtp;
    private String aEndPtpLabel;
    private String aEndChannel;
    private String zEndMe;
    private String zEndMeLabel;
    private String zEndPtp;
    private String zEndPtpLabel;
    private String zEndChannel;
    private String circle;
    private String vendor;
    private boolean isDeleted;
    private ZonedDateTime lastModifiedDate;
}
