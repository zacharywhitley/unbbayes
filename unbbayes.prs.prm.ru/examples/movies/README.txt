This is the Movie Example of the article......


// Creation script
CREATE TABLE MOVIE
(
id int,
gnere varchar(255),
budget int,
decade int,
PRIMARY KEY(id)
);

CREATE TABLE THEATER
(
id int,
theaterType varchar(255),
location varchar(255),
PRIMARY KEY(id)
);

CREATE TABLE SHOW
(
id int,
theater int,
movie int,
PRIMARY KEY(id),
FOREIGN KEY(theater) REFERENCES THEATER(id),
FOREIGN KEY(movie) REFERENCES MOVIE(id)
);

insert into "APP"."MOVIE" ("ID", "GNERE", "BUDGET", "DECADE") values(0, 'Thrieller', 400, 123)
insert into "APP"."MOVIE" ("ID", "GNERE", "BUDGET", "DECADE") values(2, 'Foreign', 25, null)
insert into "APP"."MOVIE" ("ID", "GNERE", "BUDGET", "DECADE") values(1, 'Foreign', null, null)
insert into "APP"."MOVIE" ("ID", "GNERE", "BUDGET", "DECADE") values(4, 'Foreign', null, null)
insert into "APP"."MOVIE" ("ID", "GNERE", "BUDGET", "DECADE") values(7, 'Thrieller', null, null)
insert into "APP"."MOVIE" ("ID", "GNERE", "BUDGET", "DECADE") values(6, 'Foreign', null, null)

