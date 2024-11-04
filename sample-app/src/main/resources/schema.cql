CREATE KEYSPACE IF NOT EXISTS junit WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy', 'dc1' : 1 };
USE junit;
CREATE TABLE IF NOT EXISTS accounts (id TEXT PRIMARY KEY, name TEXT);
-- DROP TABLE accountentity;

-- SELECT * FROM accountentity
