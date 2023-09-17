CREATE TABLE "Users" (
    "id"         SERIAL PRIMARY KEY,
    "first_name" VARCHAR NOT NULL,
    "last_name"  VARCHAR NOT NULL
);

CREATE TABLE "Passwords" (
    "id"            INTEGER PRIMARY KEY,
    "salt"          VARCHAR NOT NULL,
    "password_hash" VARCHAR NOT NULL
);
