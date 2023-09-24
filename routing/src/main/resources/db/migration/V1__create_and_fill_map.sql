-- earth_distance ( a: earth, b: earth ) -> float8; Returns the great circle distance between two points on the surface of the Earth.
-- ll_to_earth ( lat: float8, lon: float8 ) -> earth; given its latitude (argument 1) and longitude (argument 2) in degrees

CREATE TABLE crossroads (
    id     serial PRIMARY KEY,
    coords earth NOT NULL
);

CREATE TABLE roads (
    start_crossroad int,
    end_crossroad   int,
    PRIMARY KEY (start_crossroad, end_crossroad)
);

CREATE TABLE houses (
    id     serial PRIMARY KEY,
    name   text,
    coords earth NOT NULL
);

CREATE TABLE house_crossroad (
    house_id     int,
    crossroad_id int,
    PRIMARY KEY (house_id, crossroad_id)
);

-- example map for tests: https://watabou.github.io/neighbourhood/?seed=1494220440&tags=leafy%2Cring%2Csmall
