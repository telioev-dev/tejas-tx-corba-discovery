package com.teliolabs.corba.data.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
public class ManagedElement {
    private int pk;
    private String nativeEmsName;
    private String meName;
    private String userLabel;
    private String productName;
    private String ipAddress;
    private String softwareVersion;
    private String location;
    private String circle;
    private String vendor;
    private boolean isDeleted;
    private ZonedDateTime lastModifiedDate;
}
