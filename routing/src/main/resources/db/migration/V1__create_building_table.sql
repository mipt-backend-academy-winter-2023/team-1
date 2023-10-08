CREATE TABLE IF NOT EXISTS "building" (
    "id" INT NOT NULL PRIMARY KEY,
    "longitude" FLOAT NOT NULL,
    "latitude" FLOAT NOT NULL,
    "name" VARCHAR(128) NOT NULL
);

 INSERT INTO "building" (id, longitude, latitude, name)
 VALUES (1001, 37.491874, 55.840450, 'Бизнес-центр  ''Водный'' (Голвинское шоссе, 5к1)'),
        (1002, 37.485638, 55.838998, '''Инвитро'' (ул. АДмирала Макарова, 45)'),
        (1003, 37.495748, 55.837557, '''Мосгаз'' (Головинское шоссе, 10Г)');
