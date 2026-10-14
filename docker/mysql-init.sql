-- One database per service: no service reads another service's tables.
CREATE DATABASE IF NOT EXISTS catalog_db;
CREATE DATABASE IF NOT EXISTS member_db;
CREATE DATABASE IF NOT EXISTS circulation_db;
