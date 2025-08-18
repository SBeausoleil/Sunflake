CREATE TABLE IF NOT EXISTS scratch (
    id BIGINT PRIMARY KEY
);
TRUNCATE scratch;

INSERT INTO scratch
VALUES (1), (2), (3), (5);

/* From https://stackoverflow.com/a/176219 */
SELECT id + 1 AS available
FROM scratch s1
WHERE NOT EXISTS (SELECT * FROM scratch s2 WHERE s1.id + 1 = s2.id)
LIMIT 1;