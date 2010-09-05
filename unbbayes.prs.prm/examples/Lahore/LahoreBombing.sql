/* VERSION = 0.0.1 ORACLE-LIKE */
/* Non standard UnBBayes-PRM SQL script file. */
/* This file was generated by UnBBayes-PRM plugin on Sun Sep 05 11:34:47 BRT 2010 */
/* You may change the order of the statements, but please, do not change the statements themselves. */


CREATE TABLE "AttackPlan" (
	 "id" 	 VARCHAR2(300) 	 not null,
	 "isActivePlan" 	 VARCHAR2(300) 	 
);
ALTER TABLE "AttackPlan" ADD CONSTRAINT PK_AttackPlan PRIMARY KEY ("id");
ALTER TABLE "AttackPlan" ADD CONSTRAINT CK_isActivePlan CHECK ( "isActivePlan" IN ('true', 'false'));

CREATE TABLE "CellPhone" (
	 "id" 	 VARCHAR2(300) 	 not null
);
ALTER TABLE "CellPhone" ADD CONSTRAINT PK_CellPhone PRIMARY KEY ("id");

CREATE TABLE "PrsnOfIntrst" (
	 "id" 	 VARCHAR2(300) 	 not null,
	 "isWeaponSupplier" 	 VARCHAR2(300) 	 ,
	 "explosiveResidueReport" 	 VARCHAR2(300) 	 
);
ALTER TABLE "PrsnOfIntrst" ADD CONSTRAINT PK_PrsnOfIntrst PRIMARY KEY ("id");
ALTER TABLE "PrsnOfIntrst" ADD CONSTRAINT CK_isWeaponSupplier CHECK ( "isWeaponSupplier" IN ('true', 'false'));
ALTER TABLE "PrsnOfIntrst" ADD CONSTRAINT CK_explosiveResidueReport CHECK ( "explosiveResidueReport" IN ('true', 'false'));

CREATE TABLE "Venue" (
	 "id" 	 VARCHAR2(300) 	 not null,
	 "politicalImportance" 	 VARCHAR2(300) 	 
);
ALTER TABLE "Venue" ADD CONSTRAINT PK_Venue PRIMARY KEY ("id");
ALTER TABLE "Venue" ADD CONSTRAINT CK_politicalImportance CHECK ( "politicalImportance" IN ('high', 'low'));

CREATE TABLE "AttackPlanTarget" (
	 "id" 	 VARCHAR2(300) 	 not null,
	 "attackPlan_id" 	 VARCHAR2(300) 	 not null,
	 "venue_id" 	 VARCHAR2(300) 	 not null,
	 "isTarget" 	 VARCHAR2(300) 	 ,
	 "isMeetingVenue" 	 VARCHAR2(300) 	 
);
ALTER TABLE "AttackPlanTarget" ADD CONSTRAINT PK_AttackPlanTarget PRIMARY KEY ("id");
ALTER TABLE "AttackPlanTarget" ADD CONSTRAINT CK_isTarget CHECK ( "isTarget" IN ('true', 'false'));
ALTER TABLE "AttackPlanTarget" ADD CONSTRAINT CK_isMeetingVenue CHECK ( "isMeetingVenue" IN ('true', 'false'));

ALTER TABLE "AttackPlanTarget" ADD CONSTRAINT FK_ATTACKPLANTARGET_PLAN FOREIGN KEY ("attackPlan_id")  REFERENCES "AttackPlan" ("id");

ALTER TABLE "AttackPlanTarget" ADD CONSTRAINT FK_ATTACKPLANTARGET_VENUE FOREIGN KEY ("venue_id")  REFERENCES "Venue" ("id");

CREATE TABLE "AgentAttackPlan" (
	 "id" 	 VARCHAR2(300) 	 not null,
	 "prsnOfIntrst_id" 	 VARCHAR2(300) 	 not null,
	 "attackPlan_id" 	 VARCHAR2(300) 	 not null,
	 "isAgentOf" 	 VARCHAR2(300) 	 ,
	 "suppliesWpnMtrl" 	 VARCHAR2(300) 	 
);
ALTER TABLE "AgentAttackPlan" ADD CONSTRAINT PK_AgentAttackPlan PRIMARY KEY ("id");
ALTER TABLE "AgentAttackPlan" ADD CONSTRAINT CK_isAgentOf CHECK ( "isAgentOf" IN ('true', 'false'));
ALTER TABLE "AgentAttackPlan" ADD CONSTRAINT CK_suppliesWpnMtrl CHECK ( "suppliesWpnMtrl" IN ('true', 'false'));

ALTER TABLE "AgentAttackPlan" ADD CONSTRAINT FK_AGENTATTACKPLAN_AGENT FOREIGN KEY ("prsnOfIntrst_id")  REFERENCES "PrsnOfIntrst" ("id");

ALTER TABLE "AgentAttackPlan" ADD CONSTRAINT FK_AGENTATTACKPLAN_PLAN FOREIGN KEY ("attackPlan_id")  REFERENCES "AttackPlan" ("id");

CREATE TABLE "LocationReport" (
	 "id" 	 VARCHAR2(300) 	 not null,
	 "prsnOfIntrst_id" 	 VARCHAR2(300) 	 not null,
	 "venue_id" 	 VARCHAR2(300) 	 not null,
	 "missingCarReport" 	 VARCHAR2(300) 	 ,
	 "HUMINTLocationReport" 	 VARCHAR2(300) 	 ,
	 "agentLocation_id" 	 VARCHAR2(300) 	 not null
);
ALTER TABLE "LocationReport" ADD CONSTRAINT PK_LocationReport PRIMARY KEY ("id");
ALTER TABLE "LocationReport" ADD CONSTRAINT CK_missingCarReport CHECK ( "missingCarReport" IN ('true', 'false'));
ALTER TABLE "LocationReport" ADD CONSTRAINT CK_HUMINTLocationReport CHECK ( "HUMINTLocationReport" IN ('true', 'false'));

ALTER TABLE "LocationReport" ADD CONSTRAINT FK_LOCATIONREPORT_AGENT FOREIGN KEY ("prsnOfIntrst_id")  REFERENCES "PrsnOfIntrst" ("id");

ALTER TABLE "LocationReport" ADD CONSTRAINT FK_LOCATIONREPORT_VENUE FOREIGN KEY ("venue_id")  REFERENCES "Venue" ("id");

ALTER TABLE "LocationReport" ADD CONSTRAINT FKl_LOCATIONREPORT_AGENTLOCATION FOREIGN KEY ("agentLocation_id")  REFERENCES "AgentLocation" ("id");

CREATE TABLE "SocialNetwork" (
	 "id" 	 VARCHAR2(300) 	 not null,
	 "agt1" 	 VARCHAR2(300) 	 not null,
	 "agt2" 	 VARCHAR2(300) 	 not null,
	 "SNRival" 	 VARCHAR2(300) 	 ,
	 "SNRelated" 	 VARCHAR2(300) 	 
);
ALTER TABLE "SocialNetwork" ADD CONSTRAINT PK_SocialNetwork PRIMARY KEY ("id");
ALTER TABLE "SocialNetwork" ADD CONSTRAINT CK_SNRival CHECK ( "SNRival" IN ('true', 'false'));
ALTER TABLE "SocialNetwork" ADD CONSTRAINT CK_SNRelated CHECK ( "SNRelated" IN ('true', 'false'));

ALTER TABLE "SocialNetwork" ADD CONSTRAINT FK_SOCIALNETWORK_AGT1 FOREIGN KEY ("agt1")  REFERENCES "PrsnOfIntrst" ("id");

ALTER TABLE "SocialNetwork" ADD CONSTRAINT FK_SOCIALNETWORK_AGT2 FOREIGN KEY ("agt2")  REFERENCES "PrsnOfIntrst" ("id");

CREATE TABLE "AgentLocation" (
	 "id" 	 VARCHAR2(300) 	 not null,
	 "prsnOfIntrst_id" 	 VARCHAR2(300) 	 not null,
	 "venue_id" 	 VARCHAR2(300) 	 not null,
	 "residesAt" 	 VARCHAR2(300) 	 ,
	 "isExpectedAt" 	 VARCHAR2(300) 	 ,
	 "agentAt" 	 VARCHAR2(300) 	 
);
ALTER TABLE "AgentLocation" ADD CONSTRAINT PK_AgentLocation PRIMARY KEY ("id");
ALTER TABLE "AgentLocation" ADD CONSTRAINT CK_residesAt CHECK ( "residesAt" IN ('true', 'false'));
ALTER TABLE "AgentLocation" ADD CONSTRAINT CK_isExpectedAt CHECK ( "isExpectedAt" IN ('true', 'false'));
ALTER TABLE "AgentLocation" ADD CONSTRAINT CK_agentAt CHECK ( "agentAt" IN ('true', 'false'));

ALTER TABLE "AgentLocation" ADD CONSTRAINT FK_AGENTLOCATION_AGENT FOREIGN KEY ("prsnOfIntrst_id")  REFERENCES "PrsnOfIntrst" ("id");

ALTER TABLE "AgentLocation" ADD CONSTRAINT FK_AGENTLOCATION_VENUE FOREIGN KEY ("venue_id")  REFERENCES "Venue" ("id");

CREATE TABLE "PlanExecution" (
	 "id" 	 VARCHAR2(300) 	 not null,
	 "prsnOfIntrst_id" 	 VARCHAR2(300) 	 not null,
	 "attackPlan_id" 	 VARCHAR2(300) 	 not null,
	 "Executes" 	 VARCHAR2(300) 	 ,
	 "PlantsExplosives" 	 VARCHAR2(300) 	 
);
ALTER TABLE "PlanExecution" ADD CONSTRAINT PK_PlanExecution PRIMARY KEY ("id");
ALTER TABLE "PlanExecution" ADD CONSTRAINT CK_Executes CHECK ( "Executes" IN ('true', 'false'));
ALTER TABLE "PlanExecution" ADD CONSTRAINT CK_PlantsExplosives CHECK ( "PlantsExplosives" IN ('true', 'false'));

ALTER TABLE "PlanExecution" ADD CONSTRAINT FK_PLANEXECUTION_AGENT FOREIGN KEY ("prsnOfIntrst_id")  REFERENCES "PrsnOfIntrst" ("id");

ALTER TABLE "PlanExecution" ADD CONSTRAINT FK_PLANEXECUTION_PLAN FOREIGN KEY ("attackPlan_id")  REFERENCES "AttackPlan" ("id");

CREATE TABLE "AUX_AgentLocation_AgentAttackPlan" (
	 "id" 	 VARCHAR2(300) 	 not null,
	 "agentLocation_id" 	 VARCHAR2(300) 	 not null,
	 "agentAttackPlan_id" 	 VARCHAR2(300) 	 not null,
	 "isExpectedAt" 	 VARCHAR2(300) 	 ,
	 "isAgentOf" 	 VARCHAR2(300) 	 
);
ALTER TABLE "AUX_AgentLocation_AgentAttackPlan" ADD CONSTRAINT PK_AUXISEXPECTEDATISAGENTOF PRIMARY KEY ("id");
ALTER TABLE "AUX_AgentLocation_AgentAttackPlan" ADD CONSTRAINT CK_isExpectedAt CHECK ( "isExpectedAt" IN ('true', 'false'));
ALTER TABLE "AUX_AgentLocation_AgentAttackPlan" ADD CONSTRAINT CK_isAgentOf CHECK ( "isAgentOf" IN ('true', 'false'));

ALTER TABLE "AUX_AgentLocation_AgentAttackPlan" ADD CONSTRAINT FK_AUXISEXPECTEDATISAGENTOF_AGENTLOCATION FOREIGN KEY ("agentLocation_id")  REFERENCES "AgentLocation" ("id");

ALTER TABLE "AUX_AgentLocation_AgentAttackPlan" ADD CONSTRAINT FK_AUXISEXPECTEDATISAGENTOF_AGENTATTACKPLAN FOREIGN KEY ("agentAttackPlan_id")  REFERENCES "AgentAttackPlan" ("id");

CREATE TABLE "AUX_SNRival_isAgentOf" (
	 "id" 	 VARCHAR2(300) 	 not null,
	 "agentAttackPlan_id" 	 VARCHAR2(300) 	 not null,
	 "SocialNetwork_id" 	 VARCHAR2(300) 	 not null,
	 "SNRival" 	 VARCHAR2(300) 	 
);
ALTER TABLE "AUX_SNRival_isAgentOf" ADD CONSTRAINT PK_AUXSNRIVALISAGENTOF PRIMARY KEY ("id");
ALTER TABLE "AUX_SNRival_isAgentOf" ADD CONSTRAINT CK_SNRival CHECK ( "SNRival" IN ('true', 'false'));

ALTER TABLE "AUX_SNRival_isAgentOf" ADD CONSTRAINT FK_AUXSNRIVALISAGENTOF_AGENTATTACKPLAN FOREIGN KEY ("agentAttackPlan_id")  REFERENCES "AgentAttackPlan" ("id");

ALTER TABLE "AUX_SNRival_isAgentOf" ADD CONSTRAINT FK_AUXSNRIVALISAGENTOF_SOCIALNETWORK FOREIGN KEY ("SocialNetwork_id")  REFERENCES "SocialNetwork" ("id");

CREATE TABLE "AUX_isAgentOf_SNRelated_agt1" (
	 "id" 	 VARCHAR2(300) 	 not null,
	 "agentAttackPlan_id" 	 VARCHAR2(300) 	 not null,
	 "socialNetwork_id" 	 VARCHAR2(300) 	 not null,
	 "isAgentOf" 	 VARCHAR2(300) 	 
);
ALTER TABLE "AUX_isAgentOf_SNRelated_agt1" ADD CONSTRAINT PK_AUXISAGENTOFSNRELATEDAGT1 PRIMARY KEY ("id");
ALTER TABLE "AUX_isAgentOf_SNRelated_agt1" ADD CONSTRAINT CK_isAgentOf CHECK ( "isAgentOf" IN ('true', 'false'));

ALTER TABLE "AUX_isAgentOf_SNRelated_agt1" ADD CONSTRAINT FK_AUXISAGENTOFSNRELATEDAGT1_AGENTATTACKPLAN FOREIGN KEY ("agentAttackPlan_id")  REFERENCES "AgentAttackPlan" ("id");

ALTER TABLE "AUX_isAgentOf_SNRelated_agt1" ADD CONSTRAINT FK_AUXISAGENTOFSNRELATEDAGT1_SOCIALNETWORK FOREIGN KEY ("socialNetwork_id")  REFERENCES "SocialNetwork" ("id");

CREATE TABLE "AUX_isAgentOf_SNRelated_agt2" (
	 "id" 	 VARCHAR2(300) 	 not null,
	 "agentAttackPlan_id" 	 VARCHAR2(300) 	 not null,
	 "socialNetwork_id" 	 VARCHAR2(300) 	 not null,
	 "isAgentOf" 	 VARCHAR2(300) 	 
);
ALTER TABLE "AUX_isAgentOf_SNRelated_agt2" ADD CONSTRAINT PK_AUXISAGENTOFSNRELATEDAGT2 PRIMARY KEY ("id");
ALTER TABLE "AUX_isAgentOf_SNRelated_agt2" ADD CONSTRAINT CK_isAgentOf CHECK ( "isAgentOf" IN ('true', 'false'));

ALTER TABLE "AUX_isAgentOf_SNRelated_agt2" ADD CONSTRAINT FK_AUXISAGENTOFSNRELATEDAGT2_AGENTATTACKPLAN FOREIGN KEY ("agentAttackPlan_id")  REFERENCES "AgentAttackPlan" ("id");

ALTER TABLE "AUX_isAgentOf_SNRelated_agt2" ADD CONSTRAINT FK_AUXISAGENTOFSNRELATEDAGT2_SOCIALNETWORK FOREIGN KEY ("socialNetwork_id")  REFERENCES "SocialNetwork" ("id");

CREATE TABLE "AUX_AttackPlanTarget_AgentLocation" (
	 "id" 	 VARCHAR2(300) 	 not null,
	 "agentLocation_id" 	 VARCHAR2(300) 	 not null,
	 "attackPlanTarget_id" 	 VARCHAR2(300) 	 not null,
	 "isTarget" 	 VARCHAR2(300) 	 ,
	 "isMeetingVenue" 	 VARCHAR2(300) 	 
);
ALTER TABLE "AUX_AttackPlanTarget_AgentLocation" ADD CONSTRAINT PK_AUXATTACKPLANTARGETAGENTLOCATION PRIMARY KEY ("id");
ALTER TABLE "AUX_AttackPlanTarget_AgentLocation" ADD CONSTRAINT CK_isTarget CHECK ( "isTarget" IN ('true', 'false'));
ALTER TABLE "AUX_AttackPlanTarget_AgentLocation" ADD CONSTRAINT CK_isMeetingVenue CHECK ( "isMeetingVenue" IN ('true', 'false'));

ALTER TABLE "AUX_AttackPlanTarget_AgentLocation" ADD CONSTRAINT FK_AUXATTACKPLANTARGETAGENTLOCATION_AGENTLOCATION FOREIGN KEY ("agentLocation_id")  REFERENCES "AgentLocation" ("id");

ALTER TABLE "AUX_AttackPlanTarget_AgentLocation" ADD CONSTRAINT FK_AUXATTACKPLANTARGETAGENTLOCATION_ATTACKPLANTARGET FOREIGN KEY ("attackPlanTarget_id")  REFERENCES "AttackPlanTarget" ("id");

/* Storing dependencies as in-table comments (this is a temporary solution to be solved in future releases) */
/* Format: <listOfParents>; {<listOfProbabilities>} */
/* The <listOfParents> is a comma separated list having the following format: <parentClass>.<parentColumn>(<aggregateFunction>){<listOfForeignKeys>} */
/* The ".<aggregateFunction>" is usually "mode" (with no double quotes) or white space if none. */
/* The <listOfForeignKeys> and <listOfProbabilities> are white space separated list. */
/* The [<listOfProbabilities>] is something like {0.1 0.9 0.9 0.1} */
/* If a foreign key in <listOfForeignKeys> is written as !<foreignKeyName>, then it will be marked as an inverse foreign key (one-to-many) */

COMMENT ON COLUMN AttackPlan.isActivePlan IS ' ;  { 0.01 0.99 }';
COMMENT ON COLUMN PrsnOfIntrst.isWeaponSupplier IS ' ;  { 0.01 0.99 }';
COMMENT ON COLUMN PrsnOfIntrst.explosiveResidueReport IS ' ;  { 1.0 0.0 }';
COMMENT ON COLUMN Venue.politicalImportance IS ' ;  { 0.05 0.95 }';
COMMENT ON COLUMN AttackPlanTarget.isTarget IS 'AttackPlan.isActivePlan()[ FK_ATTACKPLANTARGET_PLAN ] , Venue.politicalImportance()[ FK_ATTACKPLANTARGET_VENUE ] ;  { 0.7 0.3 0.0 1.0 0.0030 0.997 0.0 1.0 }';
COMMENT ON COLUMN AttackPlanTarget.isMeetingVenue IS ' ;  { 1.0 0.0 }';
COMMENT ON COLUMN AgentAttackPlan.isAgentOf IS 'PrsnOfIntrst.isWeaponSupplier()[ FK_AGENTATTACKPLAN_AGENT ] , AttackPlan.isActivePlan()[ FK_AGENTATTACKPLAN_PLAN ] , AUX_AgentLocation_AgentAttackPlan.isExpectedAt(Mode)[ !FK_AUXISEXPECTEDATISAGENTOF_AGENTATTACKPLAN ] , AUX_SNRival_isAgentOf.SNRival(Mode)[ !FK_AUXSNRIVALISAGENTOF_AGENTATTACKPLAN ] ;  { 0.75 0.25 0.02 0.98 0.0 1.0 0.0 1.0 0.05 0.95 0.0050 0.995 0.0 1.0 0.0 1.0 0.05 95.0 0.0050 0.995 0.05 0.95 0.0 1.0 0.05 0.95 0.0050 0.995 0.0 1.0 0.0 1.0 }';
COMMENT ON COLUMN AgentAttackPlan.suppliesWpnMtrl IS 'AgentAttackPlan.isAgentOf()[ ] , PrsnOfIntrst.isWeaponSupplier()[ FK_AGENTATTACKPLAN_AGENT ] ;  { 0.99 0.01 0.0 1.0 0.0 1.0 0.0 1.0 }';
COMMENT ON COLUMN LocationReport.missingCarReport IS 'AgentLocation.isExpectedAt()[ FKl_LOCATIONREPORT_AGENTLOCATION ] , AgentLocation.agentAt()[ FKl_LOCATIONREPORT_AGENTLOCATION ] ;  { 0.2 8.0 0.7 0.3 0.01 0.99 0.01 0.99 }';
COMMENT ON COLUMN LocationReport.HUMINTLocationReport IS 'AgentLocation.isExpectedAt()[ FKl_LOCATIONREPORT_AGENTLOCATION ] , AgentLocation.agentAt()[ FKl_LOCATIONREPORT_AGENTLOCATION ] ;  { 0.8 0.2 0.08 0.92 0.01 0.99 0.0010 0.999 }';
COMMENT ON COLUMN SocialNetwork.SNRival IS ' ;  { 0.01 0.99 }';
COMMENT ON COLUMN SocialNetwork.SNRelated IS 'SocialNetwork.SNRival()[ ] , AUX_isAgentOf_SNRelated_agt1.isAgentOf(Mode)[ !FK_AUXISAGENTOFSNRELATEDAGT1_SOCIALNETWORK ] , AUX_isAgentOf_SNRelated_agt2.isAgentOf(Mode)[ !FK_AUXISAGENTOFSNRELATEDAGT2_SOCIALNETWORK ] ;  { 1.0 0.0 0.65 0.35 1.0 0.0 0.02 0.98 1.0 0.0 0.02 0.98 1.0 0.0 0.02 0.98 }';
COMMENT ON COLUMN AgentLocation.residesAt IS ' ;  { 0.0010 0.999 }';
COMMENT ON COLUMN AgentLocation.isExpectedAt IS 'AgentLocation.residesAt()[ ] ;  { 0.35 0.65 0.01 0.99 }';
COMMENT ON COLUMN AgentLocation.agentAt IS 'AgentLocation.isExpectedAt()[ ] , AUX_AgentLocation_AgentAttackPlan.isAgentOf(Mode)[ !FK_AUXISEXPECTEDATISAGENTOF_AGENTLOCATION ] , AUX_AttackPlanTarget_AgentLocation.isTarget(Mode)[ !FK_AUXATTACKPLANTARGETAGENTLOCATION_AGENTLOCATION ] , AUX_AttackPlanTarget_AgentLocation.isMeetingVenue(Mode)[ !FK_AUXATTACKPLANTARGETAGENTLOCATION_AGENTLOCATION ] ;  { 0.01 0.99 0.01 0.99 0.8 0.2 0.01 0.99 0.8 0.2 0.5 0.5 0.8 0.2 0.01 0.99 0.01 0.99 0.01 0.99 0.8 0.2 0.01 0.99 0.8 0.2 0.01 0.99 0.8 0.2 0.01 0.99 }';
COMMENT ON COLUMN PlanExecution.Executes IS ' ;  { 1.0 0.0 }';
COMMENT ON COLUMN PlanExecution.PlantsExplosives IS ' ;  { 1.0 0.0 }';
COMMENT ON COLUMN AUX_AgentLocation_AgentAttackPlan.isExpectedAt IS 'AgentLocation.isExpectedAt()[ FK_AUXISEXPECTEDATISAGENTOF_AGENTLOCATION ] ;  { 1.0 0.0 0.0 1.0 }';
COMMENT ON COLUMN AUX_AgentLocation_AgentAttackPlan.isAgentOf IS 'AgentAttackPlan.isAgentOf()[ FK_AUXISEXPECTEDATISAGENTOF_AGENTATTACKPLAN ] ;  { 1.0 0.0 0.0 1.0 }';
COMMENT ON COLUMN AUX_SNRival_isAgentOf.SNRival IS 'SocialNetwork.SNRival()[ FK_AUXSNRIVALISAGENTOF_SOCIALNETWORK ] ;  { 1.0 0.0 0.0 1.0 }';
COMMENT ON COLUMN AUX_isAgentOf_SNRelated_agt1.isAgentOf IS 'AgentAttackPlan.isAgentOf()[ FK_AUXISAGENTOFSNRELATEDAGT1_AGENTATTACKPLAN ] ;  { 1.0 0.0 0.0 1.0 }';
COMMENT ON COLUMN AUX_isAgentOf_SNRelated_agt2.isAgentOf IS 'AgentAttackPlan.isAgentOf()[ FK_AUXISAGENTOFSNRELATEDAGT2_AGENTATTACKPLAN ] ;  { 1.0 0.0 0.0 1.0 }';
COMMENT ON COLUMN AUX_AttackPlanTarget_AgentLocation.isTarget IS 'AttackPlanTarget.isTarget()[ FK_AUXATTACKPLANTARGETAGENTLOCATION_ATTACKPLANTARGET ] ;  { 1.0 0.0 0.0 1.0 }';
COMMENT ON COLUMN AUX_AttackPlanTarget_AgentLocation.isMeetingVenue IS 'AttackPlanTarget.isMeetingVenue()[ FK_AUXATTACKPLANTARGETAGENTLOCATION_ATTACKPLANTARGET ] ;  { 1.0 0.0 0.0 1.0 }';
INSERT INTO "AttackPlan" (isActivePlan, id) VALUES (NULL, 'ConfAtk');
INSERT INTO "PrsnOfIntrst" (id, explosiveResidueReport, isWeaponSupplier) VALUES ('V', NULL, NULL);
INSERT INTO "PrsnOfIntrst" (id, explosiveResidueReport, isWeaponSupplier) VALUES ('C', NULL, NULL);
INSERT INTO "PrsnOfIntrst" (id, explosiveResidueReport, isWeaponSupplier) VALUES ('AD', NULL, NULL);
INSERT INTO "PrsnOfIntrst" (id, explosiveResidueReport, isWeaponSupplier) VALUES ('P', NULL, NULL);
INSERT INTO "PrsnOfIntrst" (id, explosiveResidueReport, isWeaponSupplier) VALUES ('TL6', NULL, NULL);
INSERT INTO "Venue" (id, politicalImportance) VALUES ('ResC', NULL);
INSERT INTO "Venue" (id, politicalImportance) VALUES ('Conf', NULL);
INSERT INTO "AttackPlanTarget" (id, isTarget, attackPlan_id, venue_id) VALUES (NULL, NULL, 'ConfAtk', 'ResC');
INSERT INTO "AttackPlanTarget" (id, isTarget, attackPlan_id, venue_id) VALUES (NULL, NULL, 'ConfAtk', 'Conf');
INSERT INTO "AttackPlanTarget" (id, isTarget, attackPlan_id, venue_id) VALUES (NULL, NULL, NULL, 'Conf');
INSERT INTO "SocialNetwork" (SNRelated, agt2, id, SNRival, agt1) VALUES (NULL, 'C', '0', NULL, 'V');
INSERT INTO "SocialNetwork" (SNRelated, agt2, id, SNRival, agt1) VALUES (NULL, 'C', '1', NULL, 'AD');
INSERT INTO "SocialNetwork" (SNRelated, agt2, id, SNRival, agt1) VALUES (NULL, 'C', '2', NULL, 'P');
INSERT INTO "SocialNetwork" (SNRelated, agt2, id, SNRival, agt1) VALUES (NULL, 'C', '3', NULL, 'TL6');
INSERT INTO "SocialNetwork" (SNRelated, agt2, id, SNRival, agt1) VALUES (NULL, 'V', '4', NULL, 'C');
INSERT INTO "SocialNetwork" (SNRelated, agt2, id, SNRival, agt1) VALUES (NULL, 'V', '5', NULL, 'AD');
INSERT INTO "SocialNetwork" (SNRelated, agt2, id, SNRival, agt1) VALUES (NULL, 'V', '6', NULL, 'P');
INSERT INTO "SocialNetwork" (SNRelated, agt2, id, SNRival, agt1) VALUES (NULL, 'V', '7', NULL, 'TL6');
INSERT INTO "SocialNetwork" (SNRelated, agt2, id, SNRival, agt1) VALUES (NULL, 'AD', '8', NULL, 'V');
INSERT INTO "SocialNetwork" (SNRelated, agt2, id, SNRival, agt1) VALUES (NULL, 'AD', '9', NULL, 'C');
INSERT INTO "SocialNetwork" (SNRelated, agt2, id, SNRival, agt1) VALUES (NULL, 'AD', '10', NULL, 'P');
INSERT INTO "SocialNetwork" (SNRelated, agt2, id, SNRival, agt1) VALUES (NULL, 'AD', '11', NULL, 'TL6');
INSERT INTO "SocialNetwork" (SNRelated, agt2, id, SNRival, agt1) VALUES (NULL, 'P', '12', NULL, 'V');
INSERT INTO "SocialNetwork" (SNRelated, agt2, id, SNRival, agt1) VALUES (NULL, 'P', '13', NULL, 'C');
INSERT INTO "SocialNetwork" (SNRelated, agt2, id, SNRival, agt1) VALUES (NULL, 'P', '14', NULL, 'TL6');
INSERT INTO "SocialNetwork" (SNRelated, agt2, id, SNRival, agt1) VALUES (NULL, 'P', '15', NULL, 'AD');
INSERT INTO "SocialNetwork" (SNRelated, agt2, id, SNRival, agt1) VALUES (NULL, 'TL6', '16', NULL, 'V');
INSERT INTO "SocialNetwork" (SNRelated, agt2, id, SNRival, agt1) VALUES (NULL, 'TL6', '17', NULL, 'C');
INSERT INTO "SocialNetwork" (SNRelated, agt2, id, SNRival, agt1) VALUES (NULL, 'TL6', '18', NULL, 'P');
INSERT INTO "SocialNetwork" (SNRelated, agt2, id, SNRival, agt1) VALUES (NULL, 'TL6', '19', NULL, 'AD');
