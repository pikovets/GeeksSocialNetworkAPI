CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS "user" (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    first_name VARCHAR(40) NOT NULL,
    last_name VARCHAR(50),
    email VARCHAR UNIQUE NOT NULL,
    password VARCHAR NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_join DATE DEFAULT current_date,
    role VARCHAR DEFAULT 'USER',
    photo_link VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS Profile (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE REFERENCES "user" (id),
    bio VARCHAR(150),
    birthday DATE,
    sex VARCHAR(25),
    address VARCHAR(255),
    join_date DATE NOT NULL DEFAULT current_date
);

CREATE TABLE IF NOT EXISTS Community (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description VARCHAR(150),
    category VARCHAR,
    photo_link VARCHAR(255),
    publish_permission VARCHAR,
    join_type VARCHAR,
    created_date DATE NOT NULL DEFAULT current_date
);

CREATE TABLE IF NOT EXISTS User_Community (
    user_id UUID REFERENCES "user" (id) NOT NULL,
    community_id UUID REFERENCES Community (id) NOT NULL,
    user_role VARCHAR NOT NULL,
    PRIMARY KEY (user_id, community_id)
);

CREATE TABLE IF NOT EXISTS Post (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    photo_link VARCHAR(255),
    text VARCHAR(2200),
    date TIMESTAMP NOT NULL DEFAULT now(),
    author_id UUID REFERENCES "user" (id) NOT NULL,
    community_id UUID REFERENCES Community (id)
);

CREATE TABLE IF NOT EXISTS Post_Like (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id UUID REFERENCES Post (id) NOT NULL,
    user_id UUID REFERENCES "user" (id) NOT NULL
);

CREATE TABLE IF NOT EXISTS User_Relationship (
    requester_id UUID REFERENCES "user" (id),
    acceptor_id UUID REFERENCES "user" (id),
    type VARCHAR(50) NOT NULL,
    PRIMARY KEY (requester_id, acceptor_id)
);

CREATE TABLE IF NOT EXISTS Comment (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    date TIMESTAMP NOT NULL DEFAULT now(),
    text VARCHAR(2200) NOT NULL,
    post_id UUID REFERENCES Post (id) NOT NULL,
    user_id UUID REFERENCES "user" (id) NOT NULL,
    parent_comment_id UUID REFERENCES Comment(id)
);

CREATE TABLE IF NOT EXISTS Comment_Like (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    comment_id UUID REFERENCES Comment (id) NOT NULL,
    user_id UUID REFERENCES "user" (id) NOT NULL
);

CREATE TABLE IF NOT EXISTS MESSAGE (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sender_id UUID REFERENCES "user" (id) NOT NULL,
    receiver_id UUID REFERENCES "user" (id) NOT NULL,
    text VARCHAR(2200) NOT NULL,
    timestamp TIMESTAMP DEFAULT now()
);