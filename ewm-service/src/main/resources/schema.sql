CREATE TABLE IF NOT EXISTS users
(
    user_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name    VARCHAR(250) NOT NULL,
    email   VARCHAR(254) UNIQUE NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (user_id)
);

CREATE TABLE IF NOT EXISTS category
(
    category_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name        VARCHAR(50) UNIQUE NOT NULL,
    CONSTRAINT pk_category PRIMARY KEY (category_id)
);

CREATE TABLE IF NOT EXISTS location
(
    location_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    lat         FLOAT NOT NULL,
    lon         FLOAT NOT NULL,
    CONSTRAINT pk_location PRIMARY KEY (location_id)
);

CREATE TABLE IF NOT EXISTS event
(
    event_id           BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    annotation         VARCHAR(2000) NOT NULL,
    category_id        BIGINT NOT NULL,
    confirmed_requests INTEGER NOT NULL,
    created_on         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    description        VARCHAR(7000) NOT NULL,
    event_date         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_id            BIGINT NOT NULL,
    location_id        BIGINT NOT NULL,
    paid               BOOLEAN NOT NULL,
    participant_limit  INTEGER NOT NULL,
    published_on       TIMESTAMP WITHOUT TIME ZONE,
    request_moderation BOOLEAN NOT NULL,
    event_state        VARCHAR(20) NOT NULL,
    title              VARCHAR(120) NOT NULL,
    views              BIGINT NOT NULL,
    CONSTRAINT pk_event PRIMARY KEY (event_id),
    CONSTRAINT fk_event_to_category FOREIGN KEY (category_id) REFERENCES category (category_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_event_to_users FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_event_to_location FOREIGN KEY (location_id) REFERENCES location (location_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS compilation
(
    compilation_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    pinned         BOOLEAN NOT NULL,
    title          VARCHAR(50) UNIQUE NOT NULL,
    CONSTRAINT pk_compilation PRIMARY KEY (compilation_id)
);

CREATE TABLE IF NOT EXISTS event_compilation
(
    event_id       BIGINT NOT NULL,
    compilation_id BIGINT NOT NULL,
    CONSTRAINT pk_event_compilation PRIMARY KEY (event_id, compilation_id),
    CONSTRAINT fk_to_event FOREIGN KEY (event_id) REFERENCES event (event_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_to_compilation FOREIGN KEY (compilation_id) REFERENCES compilation (compilation_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS participate_request
(
    request_id   BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    created      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    event_id     BIGINT NOT NULL,
    requester_id BIGINT NOT NULL,
    status       VARCHAR(10) NOT NULL,
    CONSTRAINT pk_request PRIMARY KEY (request_id),
    CONSTRAINT uc_event_requester UNIQUE (event_id, requester_id),
    CONSTRAINT fk_request_to_event FOREIGN KEY (event_id) REFERENCES event (event_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_request_to_users FOREIGN KEY (requester_id) REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE
);