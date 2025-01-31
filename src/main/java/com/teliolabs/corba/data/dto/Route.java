package com.teliolabs.corba.data.dto;

import com.teliolabs.corba.data.types.PathType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
public class Route {
    private String sncId;
    private String nativeEmsName;
    private String aManagedElement;
    private String zManagedElement;
    private boolean active;
    private String aPtp;
    private String aCtp;
    private String zPtp;
    private String zCtp;
    private PathType pathType;
    private String circle;
    private String vendor;

    // meta
    private boolean isDeleted;
    private ZonedDateTime lastModifiedDate;
    private ZonedDateTime deltaTimestamp;
}
