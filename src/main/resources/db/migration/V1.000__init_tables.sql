CREATE TABLE users(
    id int primary key,
    registered_at timestamp not null
);

CREATE TABLE guides(
    id serial primary key,
    delay int8,
    title varchar(255) not null,
    text varchar(2048) not null
);

CREATE TABLE user_guides(
    user_id int not null,
    guide_id int not null,
    PRIMARY KEY (user_id, guide_id)
);

CREATE TABLE answers(
    id serial primary key,
    text varchar(255) NOT NULL,
    question_id int NOT NULL
);

CREATE TABLE questions(
    id serial primary key,
    text varchar(255) NOT NULL,
    guide_id int,
    answer_id int,
    FOREIGN KEY (guide_id) REFERENCES guides (id),
    FOREIGN KEY (answer_id) REFERENCES answers (id)
);

ALTER TABLE answers ADD FOREIGN KEY (question_id) REFERENCES questions(id);
