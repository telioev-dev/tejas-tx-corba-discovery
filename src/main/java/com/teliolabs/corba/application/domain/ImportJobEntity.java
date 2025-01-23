package com.teliolabs.corba.application.domain;

import com.teliolabs.corba.application.types.DiscoveryItemType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class ImportJobEntity extends JobEntity {
    private DiscoveryItemType entity;
    private Integer discoveryCount;

    @Override
    public String toString() {
        return "ImportJob {" +
                "entity=" + entity +
                ", jobId=" + jobId +
                ", vendor='" + vendor + '\'' +
                ", circle='" + circle + '\'' +
                ", startTimestamp=" + startTimestamp +
                ", discoveryCount=" + discoveryCount +
                '}';
    }
}
