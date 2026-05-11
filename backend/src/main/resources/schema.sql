-- Safety net for the @ElementCollection collection table on Problem.tags.
-- Hibernate's ddl-auto=update has been observed to skip creating this table
-- against Aiven MySQL on first boot, which then breaks the DataSeeder when it
-- tries to save a Problem with tags. Spring runs this AFTER Hibernate's schema
-- update (defer-datasource-initialization=true) and BEFORE CommandLineRunners,
-- so the seeder can rely on the table existing.

CREATE TABLE IF NOT EXISTS problem_tags (
    problem_id BIGINT NOT NULL,
    tag        VARCHAR(255)
);
