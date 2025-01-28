package com.teliolabs.corba.data.dto;

import com.teliolabs.corba.data.types.TopologyType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
public class Topology {
    private int pk;
    private String tpLinkName;
    private String nativeEmsName;
    private int rate;
    private String linkType;
    private String direction;
    private String aEndEms;
    private String aEndMeName;
    private String aEndMeLabel;
    private String aEndPortName;
    private String aEndPortLabel;
    private String zEndEms;
    private String zEndMeName;
    private String zEndMeLabel;
    private String zEndPortName;
    private String zEndPortLabel;
    private String userLabel;
    private String protection;
    private String ringName;
    private String inconsistent;
    private TopologyType topologyType;
    private String technologyLayer;
    private String circle;
    private boolean isDeleted;
    private ZonedDateTime lastModifiedDate;
}
