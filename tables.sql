CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE "user" (
id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
first_name varchar(30) NOT NULL,
last_name varchar(30) NOT NULL,
email varchar UNIQUE NOT NULL,
is_active boolean NOT NULL default true,
joined_at timestamp NOT NULL default now()
);
