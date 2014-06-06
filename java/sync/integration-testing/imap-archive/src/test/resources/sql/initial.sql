CREATE TABLE usersystem (
    usersystem_id integer NOT NULL,
    usersystem_login character varying(32) DEFAULT ''::character varying NOT NULL,
    usersystem_password character varying(32) DEFAULT ''::character varying NOT NULL,
    usersystem_uid character varying(6) DEFAULT NULL,
    usersystem_gid character varying(6) DEFAULT NULL,
    usersystem_homedir character varying(32) DEFAULT '/tmp'::character varying NOT NULL,
    usersystem_lastname character varying(32) DEFAULT NULL,
    usersystem_firstname character varying(32) DEFAULT NULL,
    usersystem_shell character varying(32) DEFAULT NULL
);

--
-- Name: usersystem_usersystem_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE usersystem_usersystem_id_seq
    INCREMENT BY 1
    
    
    CACHE 1;

--
-- Name: usersystem_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE usersystem ALTER COLUMN usersystem_id SET DEFAULT nextval('usersystem_usersystem_id_seq');

--
-- Name: usersystem_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE usersystem
    ADD CONSTRAINT usersystem_pkey PRIMARY KEY (usersystem_id);


--
-- Name: usersystem_usersystem_login_key; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE usersystem
    ADD CONSTRAINT usersystem_usersystem_login_key UNIQUE (usersystem_login);

