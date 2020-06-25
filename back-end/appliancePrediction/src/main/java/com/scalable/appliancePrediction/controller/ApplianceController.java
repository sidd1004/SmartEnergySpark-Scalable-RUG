package com.scalable.appliancePrediction.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.scalable.appliancePrediction.model.Mst_appliance;
import com.scalable.appliancePrediction.repository.ApplianceRepository;

@RestController
public class ApplianceController
{
    @Autowired
    ApplianceRepository applianceRepository;

    @GetMapping(value = "/healthcheck", produces = "application/json; charset=utf-8")
    public String getHealthCheck()
    {
        return "{ \"isWorking\" : true }";
    }

    @GetMapping("/appliances")
    public List<Mst_appliance> getEmployees()
    {
        Iterable<Mst_appliance> result = applianceRepository.findAll();
        List<Mst_appliance> employeesList = new ArrayList<Mst_appliance>();
        result.forEach(employeesList::add);
        return employeesList;
    }

    @GetMapping("/appliance/{id}")
    public Optional<Mst_appliance> getEmployee(@PathVariable String id)
    {
        Optional<Mst_appliance> emp = applianceRepository.findById(id);
        return emp;
    }

}