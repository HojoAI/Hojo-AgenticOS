--
-- PostgreSQL schema: Omniasst (voice / vector)
-- Normalized for local and CI: PostgreSQL 14+ with pgvector.
-- Idempotent: safe to re-run after a failed or partial import (drops then recreates these objects).
-- Objects are created in the database you connect to (psql -d DBNAME / PGDATABASE). This file does NOT CREATE DATABASE.
-- Repository convention: database and role name "omniasst" (create once: CREATE ROLE omniasst WITH LOGIN ...; CREATE DATABASE omniasst OWNER omniasst;).
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

-- *not* creating schema, since initdb creates it

CREATE EXTENSION IF NOT EXISTS vector WITH SCHEMA public;

COMMENT ON EXTENSION vector IS 'vector data type and ivfflat and hnsw access methods';

DROP TABLE IF EXISTS public.user_voiceprints CASCADE;
DROP TABLE IF EXISTS public.user_voice_profiles CASCADE;
DROP SEQUENCE IF EXISTS public.user_voiceprints_id_seq CASCADE;
DROP SEQUENCE IF EXISTS public.user_voice_profiles_id_seq CASCADE;

CREATE SEQUENCE public.user_voice_profiles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

SET default_tablespace = '';
SET default_table_access_method = heap;

CREATE TABLE public.user_voice_profiles (
    id bigint DEFAULT nextval('public.user_voice_profiles_id_seq'::regclass) NOT NULL,
    user_id character varying(255) NOT NULL,
    voice_id character varying(255) NOT NULL,
    voice_name character varying(255),
    provider character varying(50) DEFAULT 'hojo'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    nickname character varying(255)
);

CREATE SEQUENCE public.user_voiceprints_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.user_voiceprints (
    id bigint DEFAULT nextval('public.user_voiceprints_id_seq'::regclass) NOT NULL,
    user_id character varying(100) NOT NULL,
    user_name character varying(200) NOT NULL,
    avatar character varying(500),
    voiceprint public.vector(192) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.user_voiceprints IS '用户声纹表，存储用户的声纹向量';

COMMENT ON COLUMN public.user_voiceprints.user_id IS '用户ID';

COMMENT ON COLUMN public.user_voiceprints.user_name IS '用户名';

COMMENT ON COLUMN public.user_voiceprints.avatar IS '用户头像URL';

COMMENT ON COLUMN public.user_voiceprints.voiceprint IS '声纹向量（192维）';

ALTER TABLE ONLY public.user_voice_profiles
    ADD CONSTRAINT user_voice_profiles_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.user_voice_profiles
    ADD CONSTRAINT user_voice_profiles_user_id_voice_id_key UNIQUE (user_id, voice_id);

ALTER TABLE ONLY public.user_voiceprints
    ADD CONSTRAINT user_voiceprints_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.user_voiceprints
    ADD CONSTRAINT user_voiceprints_user_id_key UNIQUE (user_id);

CREATE INDEX idx_user_voice_profiles_provider ON public.user_voice_profiles USING btree (provider);

CREATE INDEX idx_user_voice_profiles_user_id ON public.user_voice_profiles USING btree (user_id);

CREATE INDEX idx_user_voice_profiles_voice_id ON public.user_voice_profiles USING btree (voice_id);

CREATE INDEX idx_user_voiceprints_user_id ON public.user_voiceprints USING btree (user_id);

CREATE INDEX idx_user_voiceprints_voiceprint_ivfflat ON public.user_voiceprints USING ivfflat (voiceprint public.vector_cosine_ops) WITH (lists='100');
