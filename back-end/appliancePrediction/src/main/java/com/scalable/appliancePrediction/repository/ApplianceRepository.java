package com.scalable.appliancePrediction.repository;

import org.springframework.data.repository.CrudRepository;

import com.scalable.appliancePrediction.model.Mst_appliance;

public interface ApplianceRepository extends CrudRepository<Mst_appliance, String> {
}
