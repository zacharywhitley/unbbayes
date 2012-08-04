/* VERSION = 0.0.1 ORACLE-LIKE */
/* Non standard UnBBayes-PRM SQL script file. */
/* This file was generated by UnBBayes-PRM plugin on Wed Oct 27 14:30:32 BRST 2010 */
/* You may change the order of the statements, but please, do not change the statements themselves. */


CREATE TABLE "Person" (
	 "id" 	 VARCHAR2(300) 	 not null,
	 "Father" 	 VARCHAR2(300) 	 not null,
	 "Mother" 	 VARCHAR2(300) 	 not null,
	 "BloodType" 	 VARCHAR2(300) 	 
);
ALTER TABLE "Person" ADD CONSTRAINT PK_Person PRIMARY KEY ("id");
ALTER TABLE "Person" ADD CONSTRAINT CK_BloodType CHECK ( "BloodType" IN ('A', 'B', 'AB', 'O'));

CREATE TABLE "AuxPerson" (
	 "id" 	 VARCHAR2(300) 	 not null,
	 "BloodType" 	 VARCHAR2(300) 	 
);
ALTER TABLE "AuxPerson" ADD CONSTRAINT PK_AuxPerson PRIMARY KEY ("id");
ALTER TABLE "AuxPerson" ADD CONSTRAINT CK_BloodType CHECK ( "BloodType" IN ('A', 'B', 'AB', 'O'));

/* Storing foreign keys */

ALTER TABLE "Person" ADD CONSTRAINT FK_Person_Father FOREIGN KEY ("Father")  REFERENCES "Person" ("id");

ALTER TABLE "Person" ADD CONSTRAINT FK_Person_Mother FOREIGN KEY ("Mother")  REFERENCES "AuxPerson" ("id");

ALTER TABLE "AuxPerson" ADD CONSTRAINT FK_AuxPerson_Person FOREIGN KEY ("id")  REFERENCES "Person" ("id");

/* Storing dependencies as in-table comments (this is a temporary solution to be solved in future releases) */
/* Format: <listOfParents>; {<listOfProbabilities>} */
/* The <listOfParents> is a comma separated list having the following format: <parentClass>.<parentColumn>(<aggregateFunction>){<listOfForeignKeys>} */
/* The ".<aggregateFunction>" is usually "mode" (with no double quotes) or white space if none. */
/* The <listOfForeignKeys> and <listOfProbabilities> are white space separated list. */
/* The [<listOfProbabilities>] is something like {0.1 0.9 0.9 0.1} */
/* If a foreign key in <listOfForeignKeys> is written as !<foreignKeyName>, then it will be marked as an inverse foreign key (one-to-many) */

COMMENT ON COLUMN Person.BloodType IS 'Person.BloodType()[ FK_Person_Father ] , AuxPerson.BloodType()[ FK_Person_Mother ] ;  { 0.75 0.0 0.0 0.25 0.25 0.25 0.25 0.25 0.334 0.333 0.333 0.0 0.5 0.0 0.0 0.5 0.25 0.25 0.25 0.25 0.0 0.75 0.0 0.25 0.333 0.334 0.333 0.0 0.0 0.5 0.0 0.5 0.334 0.333 0.333 0.0 0.333 0.334 0.333 0.0 0.333 0.333 0.334 0.0 0.5 0.5 0.0 0.0 0.5 0.0 0.0 0.5 0.0 0.5 0.0 0.5 0.5 0.5 0.0 0.0 0.0 0.0 0.0 1.0 }';
COMMENT ON COLUMN AuxPerson.BloodType IS 'Person.BloodType()[ FK_AuxPerson_Person ] ;  { 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 }';

/* Storing data entries */

INSERT INTO "Person" (Father, id, BloodType, Mother) VALUES ('Augustine', 'George Washington', NULL, 'Mary');
INSERT INTO "Person" (Father, id, BloodType, Mother) VALUES (NULL, 'Augustine', NULL, NULL);
INSERT INTO "Person" (Father, id, BloodType, Mother) VALUES (NULL, 'Mary', NULL, NULL);
INSERT INTO "AuxPerson" (id, BloodType) VALUES ('Mary', NULL);
