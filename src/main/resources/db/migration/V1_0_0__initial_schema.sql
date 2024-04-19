-- Idonow initial DB schema

-- Extensions
CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;
COMMENT ON EXTENSION postgis IS 'PostGIS geometry and geography spatial types and functions';

-- SEQUENCES
-- Batch processing tables only (set default increment of hibernate)

CREATE SEQUENCE public.profession_cat_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.profession_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.system_user_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.user_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.verification_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.service_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- TABLES

CREATE TABLE public.currency (
    id                      bigserial, -- auto-increment identity (static data table)
    fraction_digits         integer,
    display_name            varchar(100)                NOT NULL,
    iso_code                varchar(3)                  NOT NULL,
    numeric_code            varchar(3)                  NOT NULL,
    symbol                  varchar(10),

    PRIMARY KEY (id),
    CONSTRAINT uk_iso_code
        UNIQUE (iso_code),
    CONSTRAINT uk_numeric_code
        UNIQUE (numeric_code)
);

CREATE TABLE public.country (
    id                      bigserial, -- auto-increment identity (static data table)
    country_code            varchar(5)                  NOT NULL,
    name                    varchar(255)                NOT NULL,
    currency_id             bigint                      NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_country_code
        UNIQUE (country_code),
    CONSTRAINT fk_country__currency_id
        FOREIGN KEY (currency_id) REFERENCES currency
);

CREATE TABLE public.measurement_unit (
    id                      bigserial, -- auto-increment identity (static data table)
    common_code             varchar(20)                 NOT NULL,
    level_cat               varchar(20)                 NOT NULL,
    name                    varchar(30)                 NOT NULL,
    representation_symbol   varchar(20),

    PRIMARY KEY (id),
    CONSTRAINT uk_common_code
        UNIQUE (common_code)
);

CREATE TABLE public.role (
    id                      bigserial, -- auto-increment identity (static data table)
    description             varchar(100)                NOT NULL,
    role_type               varchar(20)                 NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_role_type
        UNIQUE (role_type)
);

CREATE TABLE public.profession_category (
    id                      bigint                      NOT NULL,
    category_name           varchar(255)                NOT NULL,
    parent_id               bigint,

    PRIMARY KEY (id),
    CONSTRAINT uk_parent_id__category_name
        UNIQUE NULLS NOT DISTINCT (parent_id, category_name),
    CONSTRAINT fk_profession_category__profession_category_id
        FOREIGN KEY (parent_id) REFERENCES profession_category
);

CREATE TABLE public.profession (
    id                      bigint                      NOT NULL,
    profession_name         varchar(255)                NOT NULL,
    profession_category_id  bigint                      NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_profession_name__profession_category_id
        UNIQUE (profession_name, profession_category_id),
    CONSTRAINT fk_profession__profession_category_id
        FOREIGN KEY (profession_category_id) REFERENCES profession_category
);

CREATE TABLE public.system_user (
    id                      bigint                      NOT NULL,
    first_name              varchar(255)                NOT NULL,
    last_name               varchar(255)                NOT NULL,
    contact_email           varchar(255)                NOT NULL,
    password                varchar(255)                NOT NULL,
    contact_phone_number            varchar(25)                 NOT NULL,
    active                  boolean                     NOT NULL,
    role_id                 bigint                      NOT NULL,
    online                  boolean                     NOT NULL,
    socket_session_id       varchar(255),
    created                 timestamp                   NOT NULL,
    updated                 timestamp                   NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_contact_email
        UNIQUE (contact_email),
    CONSTRAINT uk_contact_phone_number
        UNIQUE (contact_phone_number),
    CONSTRAINT fk_system_user__role_id
        FOREIGN KEY (role_id) REFERENCES role
 );

CREATE TABLE public."user" (
    id                      bigint                      NOT NULL,
    first_name              varchar(255)                NOT NULL,
    last_name               varchar(255)                NOT NULL,
    email                   varchar(255)                NOT NULL,
    password                varchar(255)                NOT NULL,
    phone_number            varchar(25)                 NOT NULL,
    active                  boolean                     NOT NULL,
    email_verified          boolean                     NOT NULL,
    phone_number_verified   boolean                     NOT NULL,
    profile_picture_name    varchar(255),
    cover_picture_name      varchar(255),
    account_config          jsonb,
    country_id              bigint                      NOT NULL,
    role_id                 bigint                      NOT NULL,
    created                 timestamp                   NOT NULL,
    password_updated        timestamp                   NOT NULL,
    email_updated           timestamp                   NOT NULL,
    updated                 timestamp                   NOT NULL,
    online                  boolean                     NOT NULL,
    socket_session_id       varchar(255),

    PRIMARY KEY (id),
    CONSTRAINT uk_email
        UNIQUE (email),
    CONSTRAINT uk_phone_number
        UNIQUE (phone_number),
    CONSTRAINT uk_profile_picture_name
        UNIQUE (profile_picture_name),
    CONSTRAINT uk_cover_picture_name
        UNIQUE (cover_picture_name),
    CONSTRAINT fk_user__country_id
        FOREIGN KEY (country_id) REFERENCES country,
    CONSTRAINT fk_user__role_id
        FOREIGN KEY (role_id) REFERENCES role
);

CREATE TABLE public.professional (
    user_id                 bigint                      NOT NULL,
    self_description        varchar(1000),
    address                 varchar(255)                NOT NULL,
    resume_name             varchar(255),
    location                geography                   NOT NULL,
    active                  boolean                     NOT NULL,
    balance                 numeric(16,2)               NOT NULL,
    work_start_time         time                        NOT NULL,
    work_end_time           time                        NOT NULL,
    weekend                 boolean                     NOT NULL,
    created                 timestamp                   NOT NULL,
    updated                 timestamp                   NOT NULL,

    PRIMARY KEY (user_id),
    CONSTRAINT uk_resume_name
        UNIQUE (resume_name),
    CONSTRAINT fk_professional__user_id
        FOREIGN KEY (user_id) REFERENCES "user",
    CONSTRAINT ck_work_start_time__work_end_time
        CHECK ( work_end_time > professional.work_start_time )
);

CREATE TABLE public.working_sample (
    professional_id         bigint                      NOT NULL,
    image_name              varchar(255)                NOT NULL,

    PRIMARY KEY (professional_id, image_name),
    CONSTRAINT uk_image_name
        UNIQUE (image_name),
    CONSTRAINT fk_working_sample__professional_id
        FOREIGN KEY (professional_id) REFERENCES professional
);

CREATE TABLE public.service (
    id                      bigint                      NOT NULL,
    price                   numeric(16, 2)              NOT NULL,
    profession_id           bigint                      NOT NULL,
    measurement_unit_id     bigint                      NOT NULL,
    professional_id         bigint                      NOT NULL,
    created                 timestamp                   NOT NULL,
    updated                 timestamp                   NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_profession_id__professional_id
        UNIQUE (profession_id, professional_id),
    CONSTRAINT fk_service__profession_id
        FOREIGN KEY (profession_id) REFERENCES profession,
    CONSTRAINT fk_service__measurement_unit_id
        FOREIGN KEY (measurement_unit_id) REFERENCES measurement_unit,
    CONSTRAINT fk_service__professional_id
        FOREIGN KEY (professional_id) REFERENCES professional
);

CREATE TABLE public.verification_token (
    id                      bigint                      NOT NULL,
    token                   varchar(255)                NOT NULL,
    user_id                 bigint                      NOT NULL,
    token_type              varchar(20)                 NOT NULL,
    attempt_number          smallint default 0          NOT NULL,
    created                 timestamp                   NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_token
        UNIQUE (token),
    CONSTRAINT uk_token_type__user_id
        UNIQUE (token_type, user_id),
    CONSTRAINT fk_verification_token__user_id
        FOREIGN KEY (user_id) REFERENCES "user"
);

CREATE TABLE public.system_verification_token (
                                           id                      bigint                      NOT NULL,
                                           system_token                   varchar(255)                NOT NULL,
                                           system_user_id                 bigint                      NOT NULL,
                                           token_type              varchar(20)                 NOT NULL,
                                           attempt_number          smallint default 0          NOT NULL,
                                           created                 timestamp                   NOT NULL,

                                           PRIMARY KEY (id),
                                           CONSTRAINT uk_system_token
                                               UNIQUE (system_token),
                                           CONSTRAINT uk_system_token_type__system_user_id
                                               UNIQUE (token_type, system_user_id),
                                           CONSTRAINT fk_verification_token__system_user_id
                                               FOREIGN KEY (system_user_id) REFERENCES "system_user"
);

