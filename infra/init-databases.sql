-- Creates one database per service. Postgres only auto-creates POSTGRES_DB, and the
-- services connect to jdbc:postgresql://.../<service-name>, so without these they fail
-- at startup with "database ... does not exist". Hibernate creates tables, never databases.
--
-- Runs only on first boot, when the data volume is empty. Adding a database later means
-- either creating it by hand or removing the volume:
--   docker compose -f infra/docker-compose.yml down -v
--
-- Names are hyphenated, so they must stay double-quoted.
CREATE DATABASE "user-service";
CREATE DATABASE "posts-service";
CREATE DATABASE "notification-service";
