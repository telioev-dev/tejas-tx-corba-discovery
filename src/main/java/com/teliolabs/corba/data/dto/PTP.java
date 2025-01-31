
package com.teliolabs.corba.data.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
public class PTP {
    private String ptpId;
    private String portLocation;
    private String circle;
    private String vendor;
    private String meName;
    private String meLabel;
    private String traceTx;
    private String traceRx;
    private String productName;
    private String portNativeName;
    private String slot;
    private String rate;
    private String type;
    private boolean isDeleted;
    private ZonedDateTime lastModifiedDate;
    private ZonedDateTime deltaTimestamp;
}
