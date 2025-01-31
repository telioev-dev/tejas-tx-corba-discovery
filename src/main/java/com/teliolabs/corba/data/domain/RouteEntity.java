package com.teliolabs.corba.data.domain;

import com.teliolabs.corba.data.types.PathType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
@ToString
public class RouteEntity {
    private String sncId;
    private String sncName;
    private String aEndMe;
    private String zEndMe;
    private boolean active;
    private String aEndPtp;
    private String aEndCtp;
    private String zEndPtp;
    private String zEndCtp;
    private PathType pathType;
    private byte tupleA;
    private byte tupleB;
    private String tuple;
    private String circle;
    private String vendor;

    // meta
    private boolean isDeleted;
    private ZonedDateTime lastModifiedDate;
    private ZonedDateTime deltaTimestamp;
}
