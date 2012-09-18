
jdbc:derby:examples/bloodType/BloodType.db
jdbc:derby:/home/dav/workspace-unb/unbbayes.prs.prm2/examples/bloodType/BloodType.db 


CREATE TABLE PERSON (
	 id 	 VARCHAR(300) 	 not null,
	 Father 	 VARCHAR(300),
	 Mother 	 VARCHAR(300),
	 BloodType 	 VARCHAR(300) ,	 
	 PRIMARY KEY(id),
FOREIGN KEY(Father) REFERENCES Person(id),
FOREIGN KEY(Mother) REFERENCES Person(id)
);