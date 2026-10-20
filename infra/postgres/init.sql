-- Runs only when the Postgres volume is first created.
-- learntrack_auth is created by POSTGRES_DB; each service gets its own database.
CREATE DATABASE learntrack_course;
CREATE DATABASE learntrack_notification;
