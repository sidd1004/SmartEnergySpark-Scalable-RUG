package com.scalable.appliancePrediction.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
@Table
public class Mst_appliance {
    @PrimaryKey
    private @NonNull String ApplianceId;
    private @NonNull String applianceName;
}