package com.teliolabs.corba.application.domain;

import com.teliolabs.corba.application.types.DiscoveryItemType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class DeltaJobEntity extends JobEntity {
    private DiscoveryItemType entity;
    private Integer discoveryCount;
    private Integer deletedCount;
    private Integer upsertedCount;
}
