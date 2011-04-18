UPDATE ObmInfo SET obminfo_value = '2.3.16-pre' WHERE obminfo_name = 'db_version';

ALTER TABLE EventLink ADD eventlink_is_organizer boolean default false;

--
-- Name: mailinglist; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--
CREATE TABLE mailinglist (
    mailinglist_id serial,
    mailinglist_domain_id integer NOT NULL,
    mailinglist_timeupdate timestamp without time zone,
    mailinglist_timecreate timestamp without time zone DEFAULT now(),
    mailinglist_userupdate integer DEFAULT NULL,
    mailinglist_usercreate integer DEFAULT NULL,
    mailinglist_owner integer NOT NULL,
    mailinglist_name character varying(64) NOT NULL,
    CONSTRAINT mailinglist_pkey PRIMARY KEY (mailinglist_id),
    CONSTRAINT mailinglist_domain_id_domain_id_fkey FOREIGN KEY (mailinglist_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT mailinglist_usercreate_userobm_id_fkey FOREIGN KEY (mailinglist_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT mailinglist_userupdate_userobm_id_fkey FOREIGN KEY (mailinglist_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT mailinglist_owner_userobm_id_fkey FOREIGN KEY (mailinglist_owner) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL
);

--
-- Name: mailinglistemail; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--
CREATE TABLE mailinglistemail (
  mailinglistemail_id serial,
  mailinglistemail_mailinglist_id integer NOT NULL,
  mailinglistemail_label character varying(255) NOT NULL,
  mailinglistemail_address character varying(255) NOT NULL,
  CONSTRAINT mailinglistemail_pkey PRIMARY KEY (mailinglistemail_id),
  CONSTRAINT mailinglistemail_mailinglist_id_mailinglist_id_fkey FOREIGN KEY (mailinglistemail_mailinglist_id) REFERENCES mailinglist(mailinglist_id) ON UPDATE CASCADE ON DELETE CASCADE
);


UPDATE ObmInfo SET obminfo_value = '2.3.16' WHERE obminfo_name = 'db_version';
