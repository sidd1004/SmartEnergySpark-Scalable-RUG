CREATE KEYSPACE applicationlifecycle WITH REPLICATION = { 'class' : 'SimpleStrategy','replication_factor' : 3 };

USE applicationlifecycle;

CREATE TABLE IF NOT EXISTS training (
 UniqueID text,
 applianceAge text,
 energy text,
 ApplianceId text,
 labels text,
 PRIMARY KEY(UniqueID, ApplianceId)
);

CREATE TABLE IF NOT EXISTS mst_appliance (
 ApplianceId text,
 applianceName text,
 PRIMARY KEY(ApplianceId)
);

CREATE TABLE IF NOT EXISTS prediction (
 UniqueID text,
 applianceAge text,
 energy text,
 ApplianceId text,
 prediction text,
 PRIMARY KEY(UniqueID, ApplianceId)
);

Insert into applicationlifecycle.mst_appliance(ApplianceId,applianceName) values('1','Dish Washer') IF NOT EXISTS;
Insert into applicationlifecycle.mst_appliance(ApplianceId,applianceName) values('2','Furnace') IF NOT EXISTS;
Insert into applicationlifecycle.mst_appliance(ApplianceId,applianceName) values('3','Fridge') IF NOT EXISTS;
Insert into applicationlifecycle.mst_appliance(ApplianceId,applianceName) values('4','Wine Cellar') IF NOT EXISTS;
Insert into applicationlifecycle.mst_appliance(ApplianceId,applianceName) values('5','Microwave') IF NOT EXISTS;
Insert into applicationlifecycle.mst_appliance(ApplianceId,applianceName) values('6','Garage door') IF NOT EXISTS;