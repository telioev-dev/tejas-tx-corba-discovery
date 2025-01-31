package com.teliolabs.corba.data.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
public class EquipmentEntity {
    private Long pk;
    private String meName;
    private String meLabel;
    private String userLabel;
    private String softwareVersion;
    private String serialNumber;
    private String expectedEquipment;
    private String installedEquipment;
    private String location;

    private boolean isDeleted;
    private ZonedDateTime lastModifiedDate;
    private ZonedDateTime deltaTimestamp;
}
