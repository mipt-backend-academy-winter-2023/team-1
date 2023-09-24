CREATE TABLE "street" (
    "fromId" INT,
    "toId" INT,
    "name" VARCHAR
);

INSERT INTO "street" (fromId, toId)
VALUES (?, ?, ?),
       (?, ?, ?),
       (?, ?, ?),
       (?, ?, ?),
       (?, ?, ?),
       (?, ?, ?),
       (?, ?, ?),
       (?, ?, ?);
