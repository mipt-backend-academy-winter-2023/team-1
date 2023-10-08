CREATE TABLE IF NOT EXISTS "crossroad" (
    "id" INT NOT NULL PRIMARY KEY,
    "longitude" FLOAT NOT NULL,
    "latitude" FLOAT NOT NULL
);

 INSERT INTO "crossroad" (id, longitude, latitude)
 VALUES (1, 37.487218, 55.839313),
        (2, 37.493773, 55.839349);
