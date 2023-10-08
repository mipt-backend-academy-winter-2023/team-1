CREATE TABLE IF NOT EXISTS "street" (
    "from_id" INT NOT NULL,
    "to_id" INT NOT NULL,
    "name" VARCHAR(128) NOT NULL
);

 INSERT INTO "street" (from_id, to_id, name)
 VALUES (1001, 1, 'Головинское шоссе'),
        (1002, 1, 'Головинское шоссе'),
        (1002, 2, 'Головинское шоссе'),
        (1001, 2, 'Головинское шоссе'),
        (1003, 2, 'Головинское шоссе');
