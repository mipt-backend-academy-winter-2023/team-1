CREATE TABLE "user" (
    "id" SERIAL,
    "username" VARCHAR NOT NULL PRIMARY KEY,
    "password"  VARCHAR NOT NULL
);

INSERT INTO "user" (username, password)
VALUES ('TanyaR', '-191130277');