--
-- PostgreSQL database dump
--

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: account; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE account (
    account_id integer NOT NULL,
    account_domain_id integer NOT NULL,
    account_timeupdate timestamp without time zone,
    account_timecreate timestamp without time zone DEFAULT now(),
    account_userupdate integer,
    account_usercreate integer,
    account_bank character varying(60) DEFAULT ''::character varying NOT NULL,
    account_number character varying(11) DEFAULT '0'::character varying NOT NULL,
    account_balance numeric(15,2) DEFAULT 0.00 NOT NULL,
    account_today numeric(15,2) DEFAULT 0.00 NOT NULL,
    account_comment character varying(100),
    account_label character varying(40) DEFAULT ''::character varying NOT NULL
);


--
-- Name: accountentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE accountentity (
    accountentity_entity_id integer NOT NULL,
    accountentity_account_id integer NOT NULL
);


--
-- Name: activeuserobm; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE activeuserobm (
    activeuserobm_sid character varying(32) DEFAULT ''::character varying NOT NULL,
    activeuserobm_session_name character varying(32) DEFAULT ''::character varying NOT NULL,
    activeuserobm_userobm_id integer,
    activeuserobm_timeupdate timestamp without time zone,
    activeuserobm_timecreate timestamp without time zone DEFAULT now(),
    activeuserobm_nb_connexions integer DEFAULT 0 NOT NULL,
    activeuserobm_lastpage character varying(64) DEFAULT '0'::character varying NOT NULL,
    activeuserobm_ip character varying(32) DEFAULT '0'::character varying NOT NULL
);


--
-- Name: address; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE address (
    address_id integer NOT NULL,
    address_entity_id integer NOT NULL,
    address_street1 character varying(255),
    address_street2 character varying(255),
    address_street3 character varying(2555),
    address_zipcode character varying(14),
    address_town character varying(128),
    address_expresspostal character varying(16),
    address_country character(2),
    address_label character varying(255)
);


--
-- Name: calendarentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE calendarentity (
    calendarentity_entity_id integer NOT NULL,
    calendarentity_calendar_id integer NOT NULL
);


--
-- Name: category; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE category (
    category_id integer NOT NULL,
    category_domain_id integer NOT NULL,
    category_timeupdate timestamp without time zone,
    category_timecreate timestamp without time zone DEFAULT now(),
    category_userupdate integer,
    category_usercreate integer,
    category_category character varying(24) DEFAULT ''::character varying NOT NULL,
    category_code character varying(10) DEFAULT ''::character varying NOT NULL,
    category_label character varying(100) DEFAULT ''::character varying NOT NULL
);


--
-- Name: categorylink; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE categorylink (
    categorylink_category_id integer NOT NULL,
    categorylink_entity_id integer NOT NULL,
    categorylink_category character varying(24) DEFAULT ''::character varying NOT NULL
);


--
-- Name: company; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE company (
    company_id integer NOT NULL,
    company_domain_id integer NOT NULL,
    company_timeupdate timestamp without time zone,
    company_timecreate timestamp without time zone DEFAULT now(),
    company_userupdate integer,
    company_usercreate integer,
    company_datasource_id integer,
    company_number character varying(32),
    company_vat character varying(20),
    company_siret character varying(14) DEFAULT ''::character varying,
    company_archive smallint DEFAULT 0 NOT NULL,
    company_name character varying(96) DEFAULT ''::character varying NOT NULL,
    company_aka character varying(255),
    company_sound character varying(48),
    company_type_id integer,
    company_activity_id integer,
    company_nafcode_id integer,
    company_marketingmanager_id integer,
    company_address1 character varying(64),
    company_address2 character varying(64),
    company_address3 character varying(64),
    company_zipcode character varying(14),
    company_town character varying(64),
    company_expresspostal character varying(16),
    company_country_iso3166 character(2) DEFAULT '0'::bpchar,
    company_phone character varying(32),
    company_fax character varying(32),
    company_web character varying(64),
    company_email character varying(64),
    company_contact_number integer DEFAULT 0 NOT NULL,
    company_deal_number integer DEFAULT 0 NOT NULL,
    company_deal_total integer DEFAULT 0 NOT NULL,
    company_comment text
);


--
-- Name: companyactivity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE companyactivity (
    companyactivity_id integer NOT NULL,
    companyactivity_domain_id integer NOT NULL,
    companyactivity_timeupdate timestamp without time zone,
    companyactivity_timecreate timestamp without time zone DEFAULT now(),
    companyactivity_userupdate integer,
    companyactivity_usercreate integer,
    companyactivity_code character varying(10) DEFAULT ''::character varying NOT NULL,
    companyactivity_label character varying(64)
);


--
-- Name: companyentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE companyentity (
    companyentity_entity_id integer NOT NULL,
    companyentity_company_id integer NOT NULL
);


--
-- Name: companynafcode; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE companynafcode (
    companynafcode_id integer NOT NULL,
    companynafcode_domain_id integer NOT NULL,
    companynafcode_timeupdate timestamp without time zone,
    companynafcode_timecreate timestamp without time zone DEFAULT now(),
    companynafcode_userupdate integer,
    companynafcode_usercreate integer,
    companynafcode_title integer DEFAULT 0 NOT NULL,
    companynafcode_code character varying(4),
    companynafcode_label character varying(128)
);


--
-- Name: companytype; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE companytype (
    companytype_id integer NOT NULL,
    companytype_domain_id integer NOT NULL,
    companytype_timeupdate timestamp without time zone,
    companytype_timecreate timestamp without time zone DEFAULT now(),
    companytype_userupdate integer,
    companytype_usercreate integer,
    companytype_code character varying(10) DEFAULT ''::character varying NOT NULL,
    companytype_label character(12)
);


--
-- Name: contact; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE contact (
    contact_id integer NOT NULL,
    contact_domain_id integer NOT NULL,
    contact_timeupdate timestamp without time zone,
    contact_timecreate timestamp without time zone DEFAULT now(),
    contact_userupdate integer,
    contact_usercreate integer,
    contact_datasource_id integer,
    contact_company_id integer,
    contact_company character varying(64),
    contact_kind_id integer,
    contact_marketingmanager_id integer,
    contact_lastname character varying(64) DEFAULT ''::character varying NOT NULL,
    contact_firstname character varying(64),
    contact_aka character varying(255),
    contact_sound character varying(48),
    contact_service character varying(64),
    contact_address1 character varying(64),
    contact_address2 character varying(64),
    contact_address3 character varying(64),
    contact_zipcode character varying(14),
    contact_town character varying(64),
    contact_expresspostal character varying(16),
    contact_country_iso3166 character(2) DEFAULT '0'::bpchar,
    contact_function_id integer,
    contact_title character varying(64),
    contact_phone character varying(32),
    contact_homephone character varying(32),
    contact_mobilephone character varying(32),
    contact_fax character varying(32),
    contact_email character varying(128),
    contact_email2 character varying(128),
    contact_mailing_ok smallint DEFAULT 0,
    contact_newsletter smallint DEFAULT 0,
    contact_archive smallint DEFAULT 0,
    contact_privacy integer DEFAULT 0,
    contact_date timestamp without time zone,
    contact_comment text,
    contact_comment2 text,
    contact_comment3 text,
    contact_birthday_id integer,
    contact_collected boolean DEFAULT false,
    contact_origin character varying(255) NOT NULL
);


--
-- Name: contactentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE contactentity (
    contactentity_entity_id integer NOT NULL,
    contactentity_contact_id integer NOT NULL
);


--
-- Name: contactfunction; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE contactfunction (
    contactfunction_id integer NOT NULL,
    contactfunction_domain_id integer NOT NULL,
    contactfunction_timeupdate timestamp without time zone,
    contactfunction_timecreate timestamp without time zone DEFAULT now(),
    contactfunction_userupdate integer,
    contactfunction_usercreate integer,
    contactfunction_code character varying(10) DEFAULT ''::character varying,
    contactfunction_label character varying(64)
);


--
-- Name: contactlist; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE contactlist (
    contactlist_list_id integer NOT NULL,
    contactlist_contact_id integer NOT NULL
);


--
-- Name: contract; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE contract (
    contract_id integer NOT NULL,
    contract_domain_id integer NOT NULL,
    contract_timeupdate timestamp without time zone,
    contract_timecreate timestamp without time zone DEFAULT now(),
    contract_userupdate integer,
    contract_usercreate integer,
    contract_deal_id integer,
    contract_company_id integer,
    contract_label character varying(128) DEFAULT NULL::character varying,
    contract_number character varying(20) DEFAULT NULL::character varying,
    contract_datesignature date,
    contract_datebegin date,
    contract_dateexp date,
    contract_daterenew date,
    contract_datecancel date,
    contract_type_id integer,
    contract_priority_id integer,
    contract_status_id integer,
    contract_kind integer DEFAULT 0,
    contract_format integer DEFAULT 0,
    contract_ticketnumber integer DEFAULT 0,
    contract_duration integer DEFAULT 0,
    contract_autorenewal integer DEFAULT 0,
    contract_contact1_id integer,
    contract_contact2_id integer,
    contract_techmanager_id integer,
    contract_marketmanager_id integer,
    contract_privacy integer DEFAULT 0,
    contract_archive integer DEFAULT 0,
    contract_clause text,
    contract_comment text
);


--
-- Name: contractentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE contractentity (
    contractentity_entity_id integer NOT NULL,
    contractentity_contract_id integer NOT NULL
);


--
-- Name: contractpriority; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE contractpriority (
    contractpriority_id integer NOT NULL,
    contractpriority_domain_id integer NOT NULL,
    contractpriority_timeupdate timestamp without time zone,
    contractpriority_timecreate timestamp without time zone DEFAULT now(),
    contractpriority_userupdate integer,
    contractpriority_usercreate integer,
    contractpriority_color character varying(6) DEFAULT NULL::character varying,
    contractpriority_code character varying(10) DEFAULT ''::character varying,
    contractpriority_label character varying(64) DEFAULT NULL::character varying
);


--
-- Name: contractstatus; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE contractstatus (
    contractstatus_id integer NOT NULL,
    contractstatus_domain_id integer NOT NULL,
    contractstatus_timeupdate timestamp without time zone,
    contractstatus_timecreate timestamp without time zone DEFAULT now(),
    contractstatus_userupdate integer,
    contractstatus_usercreate integer,
    contractstatus_code character varying(10) DEFAULT ''::character varying,
    contractstatus_label character varying(64) DEFAULT NULL::character varying
);


--
-- Name: contracttype; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE contracttype (
    contracttype_id integer NOT NULL,
    contracttype_domain_id integer NOT NULL,
    contracttype_timeupdate timestamp without time zone,
    contracttype_timecreate timestamp without time zone DEFAULT now(),
    contracttype_userupdate integer,
    contracttype_usercreate integer,
    contracttype_code character varying(10) DEFAULT ''::character varying,
    contracttype_label character varying(64) DEFAULT NULL::character varying
);


--
-- Name: country; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE country (
    country_domain_id integer NOT NULL,
    country_timeupdate timestamp without time zone,
    country_timecreate timestamp without time zone DEFAULT now(),
    country_userupdate integer,
    country_usercreate integer,
    country_iso3166 character(2) NOT NULL,
    country_name character varying(64),
    country_lang character(2) NOT NULL,
    country_phone character varying(4)
);


--
-- Name: cv; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cv (
    cv_id integer NOT NULL,
    cv_domain_id integer NOT NULL,
    cv_timeupdate timestamp without time zone,
    cv_timecreate timestamp without time zone DEFAULT now(),
    cv_userupdate integer,
    cv_usercreate integer,
    cv_userobm_id integer,
    cv_title character varying(255),
    cv_additionnalrefs text,
    cv_comment text
);


--
-- Name: cventity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cventity (
    cventity_entity_id integer NOT NULL,
    cventity_cv_id integer NOT NULL
);


--
-- Name: datasource; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE datasource (
    datasource_id integer NOT NULL,
    datasource_domain_id integer NOT NULL,
    datasource_timeupdate timestamp without time zone,
    datasource_timecreate timestamp without time zone DEFAULT now(),
    datasource_userupdate integer,
    datasource_usercreate integer,
    datasource_name character varying(64)
);


--
-- Name: deal; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE deal (
    deal_id integer NOT NULL,
    deal_domain_id integer NOT NULL,
    deal_timeupdate timestamp without time zone,
    deal_timecreate timestamp without time zone DEFAULT now(),
    deal_userupdate integer,
    deal_usercreate integer,
    deal_number character varying(32),
    deal_label character varying(128),
    deal_datebegin date,
    deal_parentdeal_id integer,
    deal_type_id integer,
    deal_region_id integer,
    deal_tasktype_id integer,
    deal_company_id integer NOT NULL,
    deal_contact1_id integer,
    deal_contact2_id integer,
    deal_marketingmanager_id integer,
    deal_technicalmanager_id integer,
    deal_source_id integer,
    deal_source character varying(64),
    deal_dateproposal date,
    deal_dateexpected date,
    deal_datealarm date,
    deal_dateend date,
    deal_amount numeric(12,2),
    deal_margin numeric(12,2),
    deal_commission numeric(4,2) DEFAULT 0,
    deal_hitrate integer DEFAULT 0,
    deal_status_id integer,
    deal_archive smallint DEFAULT 0,
    deal_todo character varying(128),
    deal_privacy integer DEFAULT 0,
    deal_comment text
);


--
-- Name: dealcompany; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE dealcompany (
    dealcompany_id integer NOT NULL,
    dealcompany_timeupdate timestamp without time zone,
    dealcompany_timecreate timestamp without time zone DEFAULT now(),
    dealcompany_userupdate integer DEFAULT 0,
    dealcompany_usercreate integer DEFAULT 0,
    dealcompany_deal_id integer NOT NULL,
    dealcompany_company_id integer NOT NULL,
    dealcompany_role_id integer
);


--
-- Name: dealcompanyrole; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE dealcompanyrole (
    dealcompanyrole_id integer NOT NULL,
    dealcompanyrole_domain_id integer NOT NULL,
    dealcompanyrole_timeupdate timestamp without time zone,
    dealcompanyrole_timecreate timestamp without time zone DEFAULT now(),
    dealcompanyrole_userupdate integer DEFAULT 0,
    dealcompanyrole_usercreate integer DEFAULT 0,
    dealcompanyrole_code character varying(10) DEFAULT ''::character varying,
    dealcompanyrole_label character varying(64) DEFAULT ''::character varying NOT NULL
);


--
-- Name: dealentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE dealentity (
    dealentity_entity_id integer NOT NULL,
    dealentity_deal_id integer NOT NULL
);


--
-- Name: dealstatus; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE dealstatus (
    dealstatus_id integer NOT NULL,
    dealstatus_domain_id integer NOT NULL,
    dealstatus_timeupdate timestamp without time zone,
    dealstatus_timecreate timestamp without time zone DEFAULT now(),
    dealstatus_userupdate integer,
    dealstatus_usercreate integer,
    dealstatus_label character varying(24),
    dealstatus_order integer,
    dealstatus_hitrate character(3)
);


--
-- Name: dealtype; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE dealtype (
    dealtype_id integer NOT NULL,
    dealtype_domain_id integer NOT NULL,
    dealtype_timeupdate timestamp without time zone,
    dealtype_timecreate timestamp without time zone DEFAULT now(),
    dealtype_userupdate integer,
    dealtype_usercreate integer,
    dealtype_inout character varying(1) DEFAULT '-'::character varying,
    dealtype_code character varying(10),
    dealtype_label character varying(16)
);


--
-- Name: defaultodttemplate; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE defaultodttemplate (
    defaultodttemplate_id integer NOT NULL,
    defaultodttemplate_domain_id integer NOT NULL,
    defaultodttemplate_entity character varying(32),
    defaultodttemplate_document_id integer NOT NULL,
    defaultodttemplate_label character varying(64) DEFAULT ''::character varying
);


--
-- Name: defaultodttemplateentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE defaultodttemplateentity (
    defaultodttemplateentity_entity_id integer NOT NULL,
    defaultodttemplateentity_defaultodttemplate_id integer NOT NULL
);


--
-- Name: deleted; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE deleted (
    deleted_id integer NOT NULL,
    deleted_domain_id integer,
    deleted_user_id integer,
    deleted_delegation character varying(64) DEFAULT ''::character varying,
    deleted_table character varying(32),
    deleted_entity_id integer,
    deleted_timestamp timestamp without time zone
);


--
-- Name: deletedcontact; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE deletedcontact (
    deletedcontact_contact_id integer NOT NULL,
    deletedcontact_timestamp timestamp without time zone
);


--
-- Name: deletedevent; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE deletedevent (
    deletedevent_id integer NOT NULL,
    deletedevent_event_id integer,
    deletedevent_user_id integer,
    deletedevent_timestamp timestamp without time zone
);


--
-- Name: deletedtodo; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE deletedtodo (
    deletedtodo_todo_id integer NOT NULL,
    deletedtodo_timestamp timestamp without time zone
);


--
-- Name: deleteduser; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE deleteduser (
    deleteduser_user_id integer NOT NULL,
    deleteduser_timestamp timestamp without time zone
);


--
-- Name: displaypref; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE displaypref (
    display_id integer NOT NULL,
    display_user_id integer,
    display_entity character varying(32) DEFAULT ''::character varying NOT NULL,
    display_fieldname character varying(64) DEFAULT ''::character varying NOT NULL,
    display_fieldorder integer,
    display_display integer DEFAULT 1 NOT NULL
);


--
-- Name: document; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE document (
    document_id integer NOT NULL,
    document_domain_id integer NOT NULL,
    document_timeupdate timestamp without time zone,
    document_timecreate timestamp without time zone DEFAULT now(),
    document_userupdate integer,
    document_usercreate integer,
    document_title character varying(255) DEFAULT NULL::character varying,
    document_name character varying(255) DEFAULT NULL::character varying,
    document_kind integer,
    document_mimetype_id integer,
    document_privacy integer DEFAULT 0 NOT NULL,
    document_size integer,
    document_author character varying(255) DEFAULT NULL::character varying,
    document_path text,
    document_acl text
);


--
-- Name: documententity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE documententity (
    documententity_entity_id integer NOT NULL,
    documententity_document_id integer NOT NULL
);


--
-- Name: documentlink; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE documentlink (
    documentlink_document_id integer NOT NULL,
    documentlink_entity_id integer NOT NULL
);


--
-- Name: documentmimetype; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE documentmimetype (
    documentmimetype_id integer NOT NULL,
    documentmimetype_domain_id integer NOT NULL,
    documentmimetype_timeupdate timestamp without time zone,
    documentmimetype_timecreate timestamp without time zone DEFAULT now(),
    documentmimetype_userupdate integer,
    documentmimetype_usercreate integer,
    documentmimetype_label character varying(255) DEFAULT NULL::character varying,
    documentmimetype_extension character varying(10) DEFAULT NULL::character varying,
    documentmimetype_mime character varying(255) DEFAULT NULL::character varying
);


--
-- Name: domain; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE domain (
    domain_id integer NOT NULL,
    domain_timeupdate timestamp without time zone,
    domain_timecreate timestamp without time zone DEFAULT now(),
    domain_usercreate integer,
    domain_userupdate integer,
    domain_label character varying(32) NOT NULL,
    domain_description character varying(255),
    domain_name character varying(128),
    domain_alias text,
    domain_mail_server_auto integer,
    domain_global boolean DEFAULT false
);


--
-- Name: domainentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE domainentity (
    domainentity_entity_id integer NOT NULL,
    domainentity_domain_id integer NOT NULL
);


--
-- Name: domainmailserver; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE domainmailserver (
    domainmailserver_domain_id integer NOT NULL,
    domainmailserver_mailserver_id integer NOT NULL,
    domainmailserver_role character varying(16) DEFAULT 'imap'::character varying NOT NULL
);


--
-- Name: domainproperty; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE domainproperty (
    domainproperty_key character varying(255) NOT NULL,
    domainproperty_type character varying(32),
    domainproperty_default character varying(64),
    domainproperty_readonly integer DEFAULT 0
);


--
-- Name: domainpropertyvalue; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE domainpropertyvalue (
    domainpropertyvalue_domain_id integer NOT NULL,
    domainpropertyvalue_property_key character varying(255) NOT NULL,
    domainpropertyvalue_value character varying(255) NOT NULL
);


--
-- Name: domainservice; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE domainservice (
    domainservice_service_id integer NOT NULL,
    domainservice_domain_id integer NOT NULL
);


--
-- Name: domainservicevalue; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE domainservicevalue (
    domainservicevalue_serviceproperty_id integer NOT NULL,
    domainservicevalue_domain_id integer NOT NULL,
    domainservicevalue_value text
);


--
-- Name: email; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE email (
    email_id integer NOT NULL,
    email_entity_id integer NOT NULL,
    email_label character varying(255) NOT NULL,
    email_address character varying(255)
);


--
-- Name: entity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE entity (
    entity_id integer NOT NULL,
    entity_mailing boolean
);


--
-- Name: entityright; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE entityright (
    entityright_entity_id integer NOT NULL,
    entityright_consumer_id integer NOT NULL,
    entityright_access integer DEFAULT 0 NOT NULL,
    entityright_read integer DEFAULT 0 NOT NULL,
    entityright_write integer DEFAULT 0 NOT NULL,
    entityright_admin integer DEFAULT 0 NOT NULL
);


--
-- Name: vcomponent; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE vcomponent AS ENUM (
    'VEVENT',
    'VTODO',
    'VJOURNAL',
    'VFREEBUSY'
);


--
-- Name: vopacity; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE vopacity AS ENUM (
    'OPAQUE',
    'TRANSPARENT'
);


--
-- Name: event; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE event (
    event_id integer NOT NULL,
    event_domain_id integer NOT NULL,
    event_timeupdate timestamp without time zone,
    event_timecreate timestamp without time zone DEFAULT now(),
    event_userupdate integer,
    event_usercreate integer,
    event_parent_id integer,
    event_ext_id character varying(255) DEFAULT ''::character varying,
    event_type vcomponent DEFAULT 'VEVENT'::vcomponent,
    event_origin character varying(255) DEFAULT ''::character varying NOT NULL,
    event_owner integer,
    event_timezone character varying(255) DEFAULT 'GMT'::character varying,
    event_opacity vopacity DEFAULT 'OPAQUE'::vopacity,
    event_title character varying(255) DEFAULT NULL::character varying,
    event_location character varying(100) DEFAULT NULL::character varying,
    event_category1_id integer,
    event_priority integer,
    event_privacy integer,
    event_date timestamp without time zone,
    event_duration integer DEFAULT 0 NOT NULL,
    event_allday boolean DEFAULT false,
    event_repeatkind character varying(20) DEFAULT NULL::character varying,
    event_repeatfrequence integer,
    event_repeatdays character varying(7) DEFAULT NULL::character varying,
    event_endrepeat timestamp without time zone,
    event_color character varying(7),
    event_completed timestamp without time zone,
    event_url text,
    event_description text,
    event_properties text
);


--
-- Name: eventalert; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE eventalert (
    eventalert_timeupdate timestamp without time zone,
    eventalert_timecreate timestamp without time zone DEFAULT now(),
    eventalert_userupdate integer,
    eventalert_usercreate integer,
    eventalert_event_id integer,
    eventalert_user_id integer,
    eventalert_duration integer DEFAULT 0 NOT NULL
);


--
-- Name: eventcategory1; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE eventcategory1 (
    eventcategory1_id integer NOT NULL,
    eventcategory1_domain_id integer NOT NULL,
    eventcategory1_timeupdate timestamp without time zone,
    eventcategory1_timecreate timestamp without time zone DEFAULT now(),
    eventcategory1_userupdate integer,
    eventcategory1_usercreate integer,
    eventcategory1_code character varying(10) DEFAULT ''::character varying,
    eventcategory1_label character varying(128) DEFAULT NULL::character varying,
    eventcategory1_color character(6)
);


--
-- Name: evententity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE evententity (
    evententity_entity_id integer NOT NULL,
    evententity_event_id integer NOT NULL
);


--
-- Name: eventexception; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE eventexception (
    eventexception_timeupdate timestamp without time zone,
    eventexception_timecreate timestamp without time zone DEFAULT now(),
    eventexception_userupdate integer,
    eventexception_usercreate integer,
    eventexception_event_id integer NOT NULL,
    eventexception_date timestamp without time zone NOT NULL
);


--
-- Name: vpartstat; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE vpartstat AS ENUM (
    'NEEDS-ACTION',
    'ACCEPTED',
    'DECLINED',
    'TENTATIVE',
    'DELEGATED',
    'COMPLETED',
    'IN-PROGRESS'
);


--
-- Name: vrole; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE vrole AS ENUM (
    'CHAIR',
    'REQ',
    'OPT',
    'NON'
);


--
-- Name: eventlink; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE eventlink (
    eventlink_timeupdate timestamp without time zone,
    eventlink_timecreate timestamp without time zone DEFAULT now(),
    eventlink_userupdate integer,
    eventlink_usercreate integer,
    eventlink_event_id integer NOT NULL,
    eventlink_entity_id integer NOT NULL,
    eventlink_state vpartstat DEFAULT 'NEEDS-ACTION'::vpartstat,
    eventlink_required vrole DEFAULT 'REQ'::vrole,
    eventlink_percent double precision DEFAULT 0
);


--
-- Name: groupentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE groupentity (
    groupentity_entity_id integer NOT NULL,
    groupentity_group_id integer NOT NULL
);


--
-- Name: groupgroup; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE groupgroup (
    groupgroup_parent_id integer NOT NULL,
    groupgroup_child_id integer NOT NULL
);


--
-- Name: host; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE host (
    host_id integer NOT NULL,
    host_domain_id integer NOT NULL,
    host_timeupdate timestamp without time zone,
    host_timecreate timestamp without time zone DEFAULT now(),
    host_userupdate integer,
    host_usercreate integer,
    host_uid integer,
    host_gid integer,
    host_samba integer DEFAULT 0,
    host_name character varying(32) NOT NULL,
    host_ip character varying(16),
    host_delegation character varying(64) DEFAULT ''::character varying,
    host_description character varying(128),
    host_web_perms integer DEFAULT 0,
    host_web_list text DEFAULT ''::text,
    host_web_all integer DEFAULT 0,
    host_ftp_perms integer DEFAULT 0,
    host_firewall_perms character varying(128)
);


--
-- Name: hostentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE hostentity (
    hostentity_entity_id integer NOT NULL,
    hostentity_host_id integer NOT NULL
);


--
-- Name: hostservice; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE hostservice (
    hostservice_service_id integer NOT NULL,
    hostservice_host_id integer NOT NULL
);


--
-- Name: hostservicevalue; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE hostservicevalue (
    hostservicevalue_serviceproperty_id integer NOT NULL,
    hostservicevalue_host_id integer NOT NULL,
    hostservicevalue_value text
);


--
-- Name: im; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE im (
    im_id integer NOT NULL,
    im_entity_id integer NOT NULL,
    im_label character varying(255),
    im_address character varying(255),
    im_protocol character varying(255)
);


--
-- Name: import; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE import (
    import_id integer NOT NULL,
    import_domain_id integer NOT NULL,
    import_timeupdate timestamp without time zone,
    import_timecreate timestamp without time zone DEFAULT now(),
    import_userupdate integer,
    import_usercreate integer,
    import_name character varying(64) NOT NULL,
    import_datasource_id integer,
    import_marketingmanager_id integer,
    import_separator character varying(3),
    import_enclosed character(1),
    import_desc text
);


--
-- Name: importentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE importentity (
    importentity_entity_id integer NOT NULL,
    importentity_import_id integer NOT NULL
);


--
-- Name: incident; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE incident (
    incident_id integer NOT NULL,
    incident_domain_id integer NOT NULL,
    incident_timeupdate timestamp without time zone,
    incident_timecreate timestamp without time zone DEFAULT now(),
    incident_userupdate integer,
    incident_usercreate integer,
    incident_contract_id integer NOT NULL,
    incident_label character varying(100) DEFAULT NULL::character varying,
    incident_reference character varying(32) DEFAULT NULL::character varying,
    incident_date timestamp without time zone,
    incident_priority_id integer,
    incident_status_id integer,
    incident_resolutiontype_id integer,
    incident_logger integer,
    incident_owner integer,
    incident_duration character(4) DEFAULT '0'::bpchar,
    incident_archive smallint DEFAULT 0 NOT NULL,
    incident_comment text,
    incident_resolution text
);


--
-- Name: incidententity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE incidententity (
    incidententity_entity_id integer NOT NULL,
    incidententity_incident_id integer NOT NULL
);


--
-- Name: incidentpriority; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE incidentpriority (
    incidentpriority_id integer NOT NULL,
    incidentpriority_domain_id integer NOT NULL,
    incidentpriority_timeupdate timestamp without time zone,
    incidentpriority_timecreate timestamp without time zone DEFAULT now(),
    incidentpriority_userupdate integer,
    incidentpriority_usercreate integer,
    incidentpriority_code character varying(10) DEFAULT ''::character varying,
    incidentpriority_label character varying(32) DEFAULT NULL::character varying,
    incidentpriority_color character(6)
);


--
-- Name: incidentresolutiontype; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE incidentresolutiontype (
    incidentresolutiontype_id integer NOT NULL,
    incidentresolutiontype_domain_id integer NOT NULL,
    incidentresolutiontype_timeupdate timestamp without time zone,
    incidentresolutiontype_timecreate timestamp without time zone DEFAULT now(),
    incidentresolutiontype_userupdate integer,
    incidentresolutiontype_usercreate integer,
    incidentresolutiontype_code character varying(10) DEFAULT ''::character varying,
    incidentresolutiontype_label character varying(32) DEFAULT NULL::character varying
);


--
-- Name: incidentstatus; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE incidentstatus (
    incidentstatus_id integer NOT NULL,
    incidentstatus_domain_id integer NOT NULL,
    incidentstatus_timeupdate timestamp without time zone,
    incidentstatus_timecreate timestamp without time zone DEFAULT now(),
    incidentstatus_userupdate integer,
    incidentstatus_usercreate integer,
    incidentstatus_code character varying(10) DEFAULT ''::character varying,
    incidentstatus_label character varying(32) DEFAULT NULL::character varying
);


--
-- Name: invoice; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE invoice (
    invoice_id integer NOT NULL,
    invoice_domain_id integer NOT NULL,
    invoice_timeupdate timestamp without time zone,
    invoice_timecreate timestamp without time zone DEFAULT now(),
    invoice_userupdate integer,
    invoice_usercreate integer,
    invoice_company_id integer NOT NULL,
    invoice_deal_id integer,
    invoice_project_id integer,
    invoice_number character varying(10) DEFAULT '0'::character varying,
    invoice_label character varying(40) DEFAULT ''::character varying NOT NULL,
    invoice_amount_ht numeric(10,2),
    invoice_amount_ttc numeric(10,2),
    invoice_status_id integer NOT NULL,
    invoice_date date,
    invoice_expiration_date date,
    invoice_payment_date date,
    invoice_inout character(1),
    invoice_credit_memo integer DEFAULT 0 NOT NULL,
    invoice_archive smallint DEFAULT 0 NOT NULL,
    invoice_comment text
);


--
-- Name: invoiceentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE invoiceentity (
    invoiceentity_entity_id integer NOT NULL,
    invoiceentity_invoice_id integer NOT NULL
);


--
-- Name: kind; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE kind (
    kind_id integer NOT NULL,
    kind_domain_id integer NOT NULL,
    kind_timeupdate timestamp without time zone,
    kind_timecreate timestamp without time zone DEFAULT now(),
    kind_userupdate integer,
    kind_usercreate integer,
    kind_minilabel character varying(64),
    kind_header character varying(64),
    kind_lang character(2),
    kind_default integer DEFAULT 0 NOT NULL
);


--
-- Name: lead; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE lead (
    lead_id integer NOT NULL,
    lead_domain_id integer NOT NULL,
    lead_timeupdate timestamp without time zone,
    lead_timecreate timestamp without time zone DEFAULT now(),
    lead_userupdate integer DEFAULT 0,
    lead_usercreate integer DEFAULT 0,
    lead_source_id integer,
    lead_manager_id integer,
    lead_company_id integer NOT NULL,
    lead_contact_id integer,
    lead_privacy integer DEFAULT 0,
    lead_name character varying(64),
    lead_date date,
    lead_datealarm date,
    lead_status_id integer,
    lead_archive smallint DEFAULT 0,
    lead_todo character varying(128),
    lead_comment text
);


--
-- Name: leadentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE leadentity (
    leadentity_entity_id integer NOT NULL,
    leadentity_lead_id integer NOT NULL
);


--
-- Name: leadsource; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE leadsource (
    leadsource_id integer NOT NULL,
    leadsource_domain_id integer NOT NULL,
    leadsource_timeupdate timestamp without time zone,
    leadsource_timecreate timestamp without time zone DEFAULT now(),
    leadsource_userupdate integer DEFAULT 0,
    leadsource_usercreate integer DEFAULT 0,
    leadsource_code character varying(10) DEFAULT ''::character varying,
    leadsource_label character varying(100) DEFAULT ''::character varying NOT NULL
);


--
-- Name: leadstatus; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE leadstatus (
    leadstatus_id integer NOT NULL,
    leadstatus_domain_id integer NOT NULL,
    leadstatus_timeupdate timestamp without time zone,
    leadstatus_timecreate timestamp without time zone DEFAULT now(),
    leadstatus_userupdate integer,
    leadstatus_usercreate integer,
    leadstatus_code character varying(10),
    leadstatus_label character varying(24)
);


--
-- Name: list; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE list (
    list_id integer NOT NULL,
    list_domain_id integer NOT NULL,
    list_timeupdate timestamp without time zone,
    list_timecreate timestamp without time zone DEFAULT now(),
    list_userupdate integer,
    list_usercreate integer,
    list_privacy integer DEFAULT 0,
    list_name character varying(64) NOT NULL,
    list_subject character varying(128),
    list_email character varying(128),
    list_mode integer DEFAULT 0,
    list_mailing_ok integer DEFAULT 0,
    list_contact_archive integer DEFAULT 0,
    list_info_publication integer DEFAULT 0,
    list_static_nb integer DEFAULT 0,
    list_query_nb integer DEFAULT 0,
    list_query text,
    list_structure text
);


--
-- Name: listentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE listentity (
    listentity_entity_id integer NOT NULL,
    listentity_list_id integer NOT NULL
);


--
-- Name: mailboxentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE mailboxentity (
    mailboxentity_entity_id integer NOT NULL,
    mailboxentity_mailbox_id integer NOT NULL
);


--
-- Name: mailserver; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE mailserver (
    mailserver_id integer NOT NULL,
    mailserver_timeupdate timestamp without time zone,
    mailserver_timecreate timestamp without time zone DEFAULT now(),
    mailserver_userupdate integer,
    mailserver_usercreate integer,
    mailserver_host_id integer NOT NULL,
    mailserver_relayhost_id integer,
    mailserver_imap integer DEFAULT 0,
    mailserver_smtp_in integer DEFAULT 0,
    mailserver_smtp_out integer DEFAULT 0
);


--
-- Name: mailservernetwork; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE mailservernetwork (
    mailservernetwork_host_id integer NOT NULL,
    mailservernetwork_ip character varying(16) DEFAULT ''::character varying NOT NULL
);


--
-- Name: mailshare; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE mailshare (
    mailshare_id integer NOT NULL,
    mailshare_domain_id integer NOT NULL,
    mailshare_timeupdate timestamp without time zone,
    mailshare_timecreate timestamp without time zone DEFAULT now(),
    mailshare_userupdate integer,
    mailshare_usercreate integer,
    mailshare_name character varying(32),
    mailshare_archive integer DEFAULT 0 NOT NULL,
    mailshare_quota character varying(8) DEFAULT '0'::character varying NOT NULL,
    mailshare_mail_server_id integer,
    mailshare_delegation character varying(64) DEFAULT ''::character varying,
    mailshare_description character varying(255),
    mailshare_email text
);


--
-- Name: mailshareentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE mailshareentity (
    mailshareentity_entity_id integer NOT NULL,
    mailshareentity_mailshare_id integer NOT NULL
);


--
-- Name: obmbookmark; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE obmbookmark (
    obmbookmark_id integer NOT NULL,
    obmbookmark_user_id integer NOT NULL,
    obmbookmark_label character varying(48) DEFAULT ''::character varying NOT NULL,
    obmbookmark_entity character varying(24) DEFAULT ''::character varying NOT NULL
);


--
-- Name: obmbookmarkentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE obmbookmarkentity (
    obmbookmarkentity_entity_id integer NOT NULL,
    obmbookmarkentity_obmbookmark_id integer NOT NULL
);


--
-- Name: obmbookmarkproperty; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE obmbookmarkproperty (
    obmbookmarkproperty_id integer NOT NULL,
    obmbookmarkproperty_bookmark_id integer NOT NULL,
    obmbookmarkproperty_property character varying(64) DEFAULT ''::character varying NOT NULL,
    obmbookmarkproperty_value character varying(64) DEFAULT ''::character varying NOT NULL
);


--
-- Name: obminfo; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE obminfo (
    obminfo_name character varying(32) DEFAULT ''::character varying NOT NULL,
    obminfo_value character varying(255) DEFAULT ''::character varying
);


--
-- Name: obmsession; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE obmsession (
    obmsession_sid character varying(32) DEFAULT ''::character varying NOT NULL,
    obmsession_timeupdate timestamp without time zone,
    obmsession_name character varying(32) DEFAULT ''::character varying NOT NULL,
    obmsession_data text
);


--
-- Name: of_usergroup; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE of_usergroup (
    of_usergroup_group_id integer NOT NULL,
    of_usergroup_user_id integer NOT NULL
);


--
-- Name: ogroup; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE ogroup (
    ogroup_id integer NOT NULL,
    ogroup_domain_id integer NOT NULL,
    ogroup_timeupdate timestamp without time zone,
    ogroup_timecreate timestamp without time zone DEFAULT now(),
    ogroup_userupdate integer,
    ogroup_usercreate integer,
    ogroup_organizationalchart_id integer NOT NULL,
    ogroup_parent_id integer,
    ogroup_name character varying(32) NOT NULL,
    ogroup_level character varying(16)
);


--
-- Name: ogroupentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE ogroupentity (
    ogroupentity_entity_id integer NOT NULL,
    ogroupentity_ogroup_id integer NOT NULL
);


--
-- Name: ogrouplink; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE ogrouplink (
    ogrouplink_id integer NOT NULL,
    ogrouplink_domain_id integer NOT NULL,
    ogrouplink_timeupdate timestamp without time zone,
    ogrouplink_timecreate timestamp without time zone DEFAULT now(),
    ogrouplink_userupdate integer,
    ogrouplink_usercreate integer,
    ogrouplink_ogroup_id integer NOT NULL,
    ogrouplink_entity_id integer NOT NULL
);


--
-- Name: organizationalchart; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE organizationalchart (
    organizationalchart_id integer NOT NULL,
    organizationalchart_domain_id integer NOT NULL,
    organizationalchart_timeupdate timestamp without time zone,
    organizationalchart_timecreate timestamp without time zone DEFAULT now(),
    organizationalchart_userupdate integer,
    organizationalchart_usercreate integer,
    organizationalchart_name character varying(32) NOT NULL,
    organizationalchart_description character varying(64),
    organizationalchart_archive integer DEFAULT 0 NOT NULL
);


--
-- Name: organizationalchartentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE organizationalchartentity (
    organizationalchartentity_entity_id integer NOT NULL,
    organizationalchartentity_organizationalchart_id integer NOT NULL
);


--
-- Name: p_domain; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE p_domain (
    domain_id integer NOT NULL,
    domain_timeupdate timestamp without time zone,
    domain_timecreate timestamp without time zone,
    domain_usercreate integer,
    domain_userupdate integer,
    domain_label character varying(32) NOT NULL,
    domain_description character varying(255),
    domain_name character varying(128),
    domain_alias text,
    domain_mail_server_auto integer,
    domain_global boolean
);


--
-- Name: p_entityright; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE p_entityright (
    entityright_entity_id integer NOT NULL,
    entityright_consumer_id integer NOT NULL,
    entityright_access integer NOT NULL,
    entityright_read integer NOT NULL,
    entityright_write integer NOT NULL,
    entityright_admin integer NOT NULL
);


--
-- Name: p_groupgroup; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE p_groupgroup (
    groupgroup_parent_id integer NOT NULL,
    groupgroup_child_id integer NOT NULL
);


--
-- Name: p_host; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE p_host (
    host_id integer NOT NULL,
    host_domain_id integer NOT NULL,
    host_timeupdate timestamp without time zone,
    host_timecreate timestamp without time zone,
    host_userupdate integer,
    host_usercreate integer,
    host_uid integer,
    host_gid integer,
    host_samba integer,
    host_name character varying(32) NOT NULL,
    host_ip character varying(16),
    host_delegation character varying(64),
    host_description character varying(128),
    host_web_perms integer,
    host_web_list text,
    host_web_all integer,
    host_ftp_perms integer,
    host_firewall_perms character varying(128)
);


--
-- Name: p_mailserver; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE p_mailserver (
    mailserver_id integer NOT NULL,
    mailserver_timeupdate timestamp without time zone,
    mailserver_timecreate timestamp without time zone,
    mailserver_userupdate integer,
    mailserver_usercreate integer,
    mailserver_host_id integer NOT NULL,
    mailserver_relayhost_id integer,
    mailserver_imap integer,
    mailserver_smtp_in integer,
    mailserver_smtp_out integer
);


--
-- Name: p_mailservernetwork; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE p_mailservernetwork (
    mailservernetwork_host_id integer NOT NULL,
    mailservernetwork_ip character varying(16) NOT NULL
);


--
-- Name: p_mailshare; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE p_mailshare (
    mailshare_id integer NOT NULL,
    mailshare_domain_id integer NOT NULL,
    mailshare_timeupdate timestamp without time zone,
    mailshare_timecreate timestamp without time zone,
    mailshare_userupdate integer,
    mailshare_usercreate integer,
    mailshare_name character varying(32),
    mailshare_archive integer NOT NULL,
    mailshare_quota character varying(8) NOT NULL,
    mailshare_mail_server_id integer,
    mailshare_delegation character varying(64),
    mailshare_description character varying(255),
    mailshare_email text
);


--
-- Name: p_of_usergroup; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE p_of_usergroup (
    of_usergroup_group_id integer NOT NULL,
    of_usergroup_user_id integer NOT NULL
);


--
-- Name: p_samba; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE p_samba (
    samba_domain_id integer NOT NULL,
    samba_name character varying(255) NOT NULL,
    samba_value character varying(255) NOT NULL
);


--
-- Name: p_ugroup; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE p_ugroup (
    group_id integer NOT NULL,
    group_domain_id integer NOT NULL,
    group_timeupdate timestamp without time zone,
    group_timecreate timestamp without time zone,
    group_userupdate integer,
    group_usercreate integer,
    group_system integer,
    group_privacy integer,
    group_local integer,
    group_ext_id integer,
    group_samba integer,
    group_gid integer,
    group_mailing integer,
    group_delegation character varying(64),
    group_manager_id integer,
    group_name character varying(32) NOT NULL,
    group_desc character varying(128),
    group_email character varying(128),
    group_contacts text
);


--
-- Name: p_userobm; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE p_userobm (
    userobm_id integer NOT NULL,
    userobm_domain_id integer NOT NULL,
    userobm_timeupdate timestamp without time zone,
    userobm_timecreate timestamp without time zone,
    userobm_userupdate integer,
    userobm_usercreate integer,
    userobm_local integer,
    userobm_ext_id character varying(16),
    userobm_system integer,
    userobm_archive smallint NOT NULL,
    userobm_timelastaccess timestamp without time zone,
    userobm_login character varying(32) NOT NULL,
    userobm_nb_login_failed integer,
    userobm_password_type character varying(6) NOT NULL,
    userobm_password character varying(64) NOT NULL,
    userobm_password_dateexp date,
    userobm_account_dateexp date,
    userobm_perms character varying(254),
    userobm_delegation_target character varying(64),
    userobm_delegation character varying(64),
    userobm_calendar_version timestamp without time zone,
    userobm_uid integer,
    userobm_gid integer,
    userobm_datebegin date,
    userobm_hidden integer,
    userobm_kind character varying(12),
    userobm_lastname character varying(64),
    userobm_firstname character varying(64),
    userobm_title character varying(64),
    userobm_sound character varying(64),
    userobm_company character varying(64),
    userobm_direction character varying(64),
    userobm_service character varying(64),
    userobm_address1 character varying(64),
    userobm_address2 character varying(64),
    userobm_address3 character varying(64),
    userobm_zipcode character varying(14),
    userobm_town character varying(64),
    userobm_expresspostal character varying(16),
    userobm_country_iso3166 character(2),
    userobm_phone character varying(32),
    userobm_phone2 character varying(32),
    userobm_mobile character varying(32),
    userobm_fax character varying(32),
    userobm_fax2 character varying(32),
    userobm_web_perms integer,
    userobm_web_list text,
    userobm_web_all integer,
    userobm_mail_perms integer,
    userobm_mail_ext_perms integer,
    userobm_email text,
    userobm_mail_server_id integer,
    userobm_mail_quota integer,
    userobm_mail_quota_use integer,
    userobm_mail_login_date timestamp without time zone,
    userobm_nomade_perms integer,
    userobm_nomade_enable integer,
    userobm_nomade_local_copy integer,
    userobm_nomade_datebegin timestamp without time zone,
    userobm_nomade_dateend timestamp without time zone,
    userobm_email_nomade character varying(64),
    userobm_vacation_enable integer,
    userobm_vacation_datebegin timestamp without time zone,
    userobm_vacation_dateend timestamp without time zone,
    userobm_vacation_message text,
    userobm_samba_perms integer,
    userobm_samba_home character varying(255),
    userobm_samba_home_drive character(2),
    userobm_samba_logon_script character varying(128),
    userobm_host_id integer,
    userobm_description character varying(255),
    userobm_location character varying(255),
    userobm_education character varying(255),
    userobm_photo_id integer
);


--
-- Name: p_userobmgroup; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE p_userobmgroup (
    userobmgroup_group_id integer NOT NULL,
    userobmgroup_userobm_id integer NOT NULL
);


--
-- Name: parentdeal; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE parentdeal (
    parentdeal_id integer NOT NULL,
    parentdeal_domain_id integer NOT NULL,
    parentdeal_timeupdate timestamp without time zone,
    parentdeal_timecreate timestamp without time zone DEFAULT now(),
    parentdeal_userupdate integer,
    parentdeal_usercreate integer,
    parentdeal_label character varying(128) NOT NULL,
    parentdeal_marketingmanager_id integer,
    parentdeal_technicalmanager_id integer,
    parentdeal_archive smallint DEFAULT 0,
    parentdeal_comment text
);


--
-- Name: parentdealentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE parentdealentity (
    parentdealentity_entity_id integer NOT NULL,
    parentdealentity_parentdeal_id integer NOT NULL
);


--
-- Name: payment; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE payment (
    payment_id integer NOT NULL,
    payment_domain_id integer NOT NULL,
    payment_timeupdate timestamp without time zone,
    payment_timecreate timestamp without time zone DEFAULT now(),
    payment_userupdate integer,
    payment_usercreate integer,
    payment_company_id integer,
    payment_account_id integer,
    payment_paymentkind_id integer,
    payment_amount numeric(10,2) DEFAULT 0.0 NOT NULL,
    payment_date date,
    payment_inout character(1) DEFAULT '+'::bpchar NOT NULL,
    payment_number character varying(24) DEFAULT ''::character varying NOT NULL,
    payment_checked smallint DEFAULT 0 NOT NULL,
    payment_gap numeric(10,2) DEFAULT 0.0 NOT NULL,
    payment_comment text
);


--
-- Name: paymententity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE paymententity (
    paymententity_entity_id integer NOT NULL,
    paymententity_payment_id integer NOT NULL
);


--
-- Name: paymentinvoice; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE paymentinvoice (
    paymentinvoice_invoice_id integer NOT NULL,
    paymentinvoice_payment_id integer NOT NULL,
    paymentinvoice_timeupdate timestamp without time zone,
    paymentinvoice_timecreate timestamp without time zone DEFAULT now(),
    paymentinvoice_userupdate integer,
    paymentinvoice_usercreate integer,
    paymentinvoice_amount numeric(10,2) DEFAULT 0::numeric NOT NULL
);


--
-- Name: paymentkind; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE paymentkind (
    paymentkind_id integer NOT NULL,
    paymentkind_domain_id integer NOT NULL,
    paymentkind_shortlabel character varying(3) DEFAULT ''::character varying NOT NULL,
    paymentkind_label character varying(40) DEFAULT ''::character varying NOT NULL
);


--
-- Name: phone; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE phone (
    phone_id integer NOT NULL,
    phone_entity_id integer NOT NULL,
    phone_label character varying(255) NOT NULL,
    phone_number character varying(32)
);


--
-- Name: profile; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE profile (
    profile_id integer NOT NULL,
    profile_domain_id integer NOT NULL,
    profile_timeupdate timestamp without time zone,
    profile_timecreate timestamp without time zone DEFAULT now(),
    profile_userupdate integer,
    profile_usercreate integer,
    profile_name character varying(64) DEFAULT NULL::character varying
);


--
-- Name: profileentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE profileentity (
    profileentity_entity_id integer NOT NULL,
    profileentity_profile_id integer NOT NULL
);


--
-- Name: profilemodule; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE profilemodule (
    profilemodule_id integer NOT NULL,
    profilemodule_domain_id integer NOT NULL,
    profilemodule_profile_id integer,
    profilemodule_module_name character varying(64) DEFAULT ''::character varying NOT NULL,
    profilemodule_right integer
);


--
-- Name: profileproperty; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE profileproperty (
    profileproperty_id integer NOT NULL,
    profileproperty_type character varying(32) DEFAULT NULL::character varying,
    profileproperty_default text,
    profileproperty_readonly smallint DEFAULT 0,
    profileproperty_name character varying(32) DEFAULT ''::character varying NOT NULL
);


--
-- Name: profilepropertyvalue; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE profilepropertyvalue (
    profilepropertyvalue_id integer NOT NULL,
    profilepropertyvalue_profile_id integer,
    profilepropertyvalue_property_id integer,
    profilepropertyvalue_property_value text DEFAULT ''::text NOT NULL
);


--
-- Name: profilesection; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE profilesection (
    profilesection_id integer NOT NULL,
    profilesection_domain_id integer NOT NULL,
    profilesection_profile_id integer,
    profilesection_section_name character varying(64) DEFAULT ''::character varying NOT NULL,
    profilesection_show smallint
);


--
-- Name: project; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE project (
    project_id integer NOT NULL,
    project_domain_id integer NOT NULL,
    project_timeupdate timestamp without time zone,
    project_timecreate timestamp without time zone DEFAULT now(),
    project_userupdate integer,
    project_usercreate integer,
    project_name character varying(128),
    project_shortname character varying(10),
    project_type_id integer,
    project_tasktype_id integer,
    project_company_id integer,
    project_deal_id integer,
    project_soldtime integer,
    project_estimatedtime integer,
    project_datebegin date,
    project_dateend date,
    project_archive smallint DEFAULT 0,
    project_comment text,
    project_reference_date character varying(32),
    project_reference_duration character varying(16),
    project_reference_desc text,
    project_reference_tech text
);


--
-- Name: projectclosing; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE projectclosing (
    projectclosing_id integer NOT NULL,
    projectclosing_project_id integer NOT NULL,
    projectclosing_timeupdate timestamp without time zone,
    projectclosing_timecreate timestamp without time zone DEFAULT now(),
    projectclosing_userupdate integer,
    projectclosing_usercreate integer,
    projectclosing_date timestamp without time zone NOT NULL,
    projectclosing_used integer NOT NULL,
    projectclosing_remaining integer NOT NULL,
    projectclosing_type integer,
    projectclosing_comment text
);


--
-- Name: projectcv; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE projectcv (
    projectcv_project_id integer NOT NULL,
    projectcv_cv_id integer NOT NULL,
    projectcv_role character varying(128)
);


--
-- Name: projectentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE projectentity (
    projectentity_entity_id integer NOT NULL,
    projectentity_project_id integer NOT NULL
);


--
-- Name: projectreftask; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE projectreftask (
    projectreftask_id integer NOT NULL,
    projectreftask_timeupdate timestamp without time zone,
    projectreftask_timecreate timestamp without time zone DEFAULT now(),
    projectreftask_userupdate integer,
    projectreftask_usercreate integer,
    projectreftask_tasktype_id integer,
    projectreftask_code character varying(10) DEFAULT ''::character varying,
    projectreftask_label character varying(128) DEFAULT NULL::character varying
);


--
-- Name: projecttask; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE projecttask (
    projecttask_id integer NOT NULL,
    projecttask_project_id integer NOT NULL,
    projecttask_timeupdate timestamp without time zone,
    projecttask_timecreate timestamp without time zone DEFAULT now(),
    projecttask_userupdate integer,
    projecttask_usercreate integer,
    projecttask_label character varying(128) DEFAULT NULL::character varying,
    projecttask_parenttask_id integer,
    projecttask_rank integer,
    projecttask_datebegin date,
    projecttask_dateend date
);


--
-- Name: projectuser; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE projectuser (
    projectuser_id integer NOT NULL,
    projectuser_project_id integer NOT NULL,
    projectuser_user_id integer NOT NULL,
    projectuser_projecttask_id integer,
    projectuser_timeupdate timestamp without time zone,
    projectuser_timecreate timestamp without time zone DEFAULT now(),
    projectuser_userupdate integer,
    projectuser_usercreate integer,
    projectuser_projectedtime double precision,
    projectuser_missingtime double precision,
    projectuser_validity timestamp without time zone,
    projectuser_soldprice integer,
    projectuser_manager integer
);


--
-- Name: publication; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE publication (
    publication_id integer NOT NULL,
    publication_domain_id integer NOT NULL,
    publication_timeupdate timestamp without time zone,
    publication_timecreate timestamp without time zone DEFAULT now(),
    publication_userupdate integer,
    publication_usercreate integer,
    publication_title character varying(64) NOT NULL,
    publication_type_id integer,
    publication_year integer,
    publication_lang character varying(30),
    publication_desc text
);


--
-- Name: publicationentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE publicationentity (
    publicationentity_entity_id integer NOT NULL,
    publicationentity_publication_id integer NOT NULL
);


--
-- Name: publicationtype; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE publicationtype (
    publicationtype_id integer NOT NULL,
    publicationtype_domain_id integer NOT NULL,
    publicationtype_timeupdate timestamp without time zone,
    publicationtype_timecreate timestamp without time zone DEFAULT now(),
    publicationtype_userupdate integer,
    publicationtype_usercreate integer,
    publicationtype_code character varying(10) DEFAULT ''::character varying NOT NULL,
    publicationtype_label character varying(64)
);


--
-- Name: region; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE region (
    region_id integer NOT NULL,
    region_domain_id integer NOT NULL,
    region_timeupdate timestamp without time zone,
    region_timecreate timestamp without time zone DEFAULT now(),
    region_userupdate integer,
    region_usercreate integer,
    region_code character varying(10) DEFAULT ''::character varying NOT NULL,
    region_label character varying(64)
);


--
-- Name: resource; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE resource (
    resource_id integer NOT NULL,
    resource_domain_id integer NOT NULL,
    resource_rtype_id integer,
    resource_timeupdate timestamp without time zone,
    resource_timecreate timestamp without time zone DEFAULT now(),
    resource_userupdate integer,
    resource_usercreate integer,
    resource_name character varying(32) DEFAULT ''::character varying NOT NULL,
    resource_description character varying(255),
    resource_qty integer DEFAULT 0
);


--
-- Name: resourceentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE resourceentity (
    resourceentity_entity_id integer NOT NULL,
    resourceentity_resource_id integer NOT NULL
);


--
-- Name: resourcegroup; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE resourcegroup (
    resourcegroup_rgroup_id integer NOT NULL,
    resourcegroup_resource_id integer NOT NULL
);


--
-- Name: resourcegroupentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE resourcegroupentity (
    resourcegroupentity_entity_id integer NOT NULL,
    resourcegroupentity_resourcegroup_id integer NOT NULL
);


--
-- Name: resourceitem; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE resourceitem (
    resourceitem_id integer NOT NULL,
    resourceitem_domain_id integer NOT NULL,
    resourceitem_label character varying(32) NOT NULL,
    resourceitem_resourcetype_id integer NOT NULL,
    resourceitem_description text
);


--
-- Name: resourcetype; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE resourcetype (
    resourcetype_id integer NOT NULL,
    resourcetype_domain_id integer NOT NULL,
    resourcetype_label character varying(32) NOT NULL,
    resourcetype_property character varying(32),
    resourcetype_pkind integer DEFAULT 0 NOT NULL
);


--
-- Name: rgroup; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE rgroup (
    rgroup_id integer NOT NULL,
    rgroup_domain_id integer NOT NULL,
    rgroup_timeupdate timestamp without time zone,
    rgroup_timecreate timestamp without time zone DEFAULT now(),
    rgroup_userupdate integer,
    rgroup_usercreate integer,
    rgroup_privacy integer DEFAULT 0,
    rgroup_name character varying(32) NOT NULL,
    rgroup_desc character varying(128)
);


--
-- Name: samba; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE samba (
    samba_domain_id integer NOT NULL,
    samba_name character varying(255) DEFAULT ''::character varying NOT NULL,
    samba_value character varying(255) DEFAULT ''::character varying NOT NULL
);


--
-- Name: service; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE service (
    service_id integer NOT NULL,
    service_key character varying(255)
);


--
-- Name: servicedomain; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE servicedomain (
    servicedomain_id integer NOT NULL
);


--
-- Name: servicehost; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE servicehost (
    servicehost_id integer NOT NULL
);


--
-- Name: serviceproperty; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE serviceproperty (
    serviceproperty_id integer NOT NULL,
    serviceproperty_service_id integer NOT NULL,
    serviceproperty_key character varying(255),
    serviceproperty_default text,
    serviceproperty_type character varying(255),
    serviceproperty_min smallint,
    serviceproperty_max smallint
);


--
-- Name: servicepropertydomain; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE servicepropertydomain (
    servicepropertydomain_id integer NOT NULL
);


--
-- Name: servicepropertyhost; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE servicepropertyhost (
    servicepropertyhost_id integer NOT NULL
);


--
-- Name: stats; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE stats (
    stats_name character varying(32) DEFAULT ''::character varying NOT NULL,
    stats_value character varying(255) DEFAULT ''::character varying NOT NULL
);


--
-- Name: subscription; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE subscription (
    subscription_id integer NOT NULL,
    subscription_domain_id integer NOT NULL,
    subscription_publication_id integer NOT NULL,
    subscription_contact_id integer NOT NULL,
    subscription_timeupdate timestamp without time zone,
    subscription_timecreate timestamp without time zone DEFAULT now(),
    subscription_userupdate integer,
    subscription_usercreate integer,
    subscription_quantity integer,
    subscription_renewal integer NOT NULL,
    subscription_reception_id integer,
    subscription_date_begin timestamp without time zone,
    subscription_date_end timestamp without time zone
);


--
-- Name: subscriptionentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE subscriptionentity (
    subscriptionentity_entity_id integer NOT NULL,
    subscriptionentity_subscription_id integer NOT NULL
);


--
-- Name: subscriptionreception; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE subscriptionreception (
    subscriptionreception_id integer NOT NULL,
    subscriptionreception_domain_id integer NOT NULL,
    subscriptionreception_timeupdate timestamp without time zone,
    subscriptionreception_timecreate timestamp without time zone DEFAULT now(),
    subscriptionreception_userupdate integer,
    subscriptionreception_usercreate integer,
    subscriptionreception_code character varying(10) DEFAULT ''::character varying NOT NULL,
    subscriptionreception_label character(12)
);


--
-- Name: tasktype; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE tasktype (
    tasktype_id integer NOT NULL,
    tasktype_domain_id integer NOT NULL,
    tasktype_timeupdate timestamp without time zone,
    tasktype_timecreate timestamp without time zone DEFAULT now(),
    tasktype_userupdate integer,
    tasktype_usercreate integer,
    tasktype_internal integer NOT NULL,
    tasktype_code character varying(10),
    tasktype_label character varying(32) DEFAULT NULL::character varying
);


--
-- Name: timetask; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE timetask (
    timetask_id integer NOT NULL,
    timetask_timeupdate timestamp without time zone,
    timetask_timecreate timestamp without time zone DEFAULT now(),
    timetask_userupdate integer,
    timetask_usercreate integer,
    timetask_user_id integer,
    timetask_date timestamp without time zone NOT NULL,
    timetask_projecttask_id integer,
    timetask_length double precision,
    timetask_tasktype_id integer,
    timetask_label character varying(255) DEFAULT NULL::character varying,
    timetask_status integer
);


--
-- Name: todo; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE todo (
    todo_id integer NOT NULL,
    todo_domain_id integer NOT NULL,
    todo_timeupdate timestamp without time zone,
    todo_timecreate timestamp without time zone DEFAULT now(),
    todo_userupdate integer,
    todo_usercreate integer,
    todo_user integer,
    todo_privacy integer DEFAULT 0 NOT NULL,
    todo_date timestamp without time zone,
    todo_deadline timestamp without time zone,
    todo_dateend timestamp without time zone,
    todo_priority integer,
    todo_percent integer,
    todo_title character varying(80) DEFAULT NULL::character varying,
    todo_status character varying(32),
    todo_webpage character varying(255),
    todo_content text
);


--
-- Name: ugroup; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE ugroup (
    group_id integer NOT NULL,
    group_domain_id integer NOT NULL,
    group_timeupdate timestamp without time zone,
    group_timecreate timestamp without time zone DEFAULT now(),
    group_userupdate integer,
    group_usercreate integer,
    group_system integer DEFAULT 0,
    group_privacy integer DEFAULT 0,
    group_local integer DEFAULT 1,
    group_ext_id integer,
    group_samba integer DEFAULT 0,
    group_gid integer,
    group_mailing integer DEFAULT 0,
    group_delegation character varying(64) DEFAULT ''::character varying,
    group_manager_id integer,
    group_name character varying(32) NOT NULL,
    group_desc character varying(128),
    group_email character varying(128),
    group_contacts text
);


--
-- Name: updated; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE updated (
    updated_id integer NOT NULL,
    updated_domain_id integer,
    updated_user_id integer,
    updated_delegation character varying(64) DEFAULT ''::character varying,
    updated_table character varying(32),
    updated_entity_id integer,
    updated_type character(1)
);


--
-- Name: updatedlinks; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE updatedlinks (
    updatedlinks_id integer NOT NULL,
    updatedlinks_domain_id integer,
    updatedlinks_user_id integer,
    updatedlinks_delegation character varying(64),
    updatedlinks_table character varying(32),
    updatedlinks_entity character varying(32),
    updatedlinks_entity_id integer
);


--
-- Name: userentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE userentity (
    userentity_entity_id integer NOT NULL,
    userentity_user_id integer NOT NULL
);


--
-- Name: userobm; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE userobm (
    userobm_id integer NOT NULL,
    userobm_domain_id integer NOT NULL,
    userobm_timeupdate timestamp without time zone,
    userobm_timecreate timestamp without time zone DEFAULT now(),
    userobm_userupdate integer,
    userobm_usercreate integer,
    userobm_local integer DEFAULT 1,
    userobm_ext_id character varying(16),
    userobm_system integer DEFAULT 0,
    userobm_archive smallint DEFAULT 0 NOT NULL,
    userobm_timelastaccess timestamp without time zone,
    userobm_login character varying(32) DEFAULT ''::character varying NOT NULL,
    userobm_nb_login_failed integer DEFAULT 0,
    userobm_password_type character varying(6) DEFAULT 'PLAIN'::character varying NOT NULL,
    userobm_password character varying(64) DEFAULT ''::character varying NOT NULL,
    userobm_password_dateexp date,
    userobm_account_dateexp date,
    userobm_perms character varying(254),
    userobm_delegation_target character varying(64) DEFAULT ''::character varying,
    userobm_delegation character varying(64) DEFAULT ''::character varying,
    userobm_calendar_version timestamp without time zone,
    userobm_uid integer,
    userobm_gid integer,
    userobm_datebegin date,
    userobm_hidden integer DEFAULT 0,
    userobm_kind character varying(12),
    userobm_lastname character varying(64) DEFAULT ''::character varying,
    userobm_firstname character varying(64) DEFAULT ''::character varying,
    userobm_title character varying(64) DEFAULT ''::character varying,
    userobm_sound character varying(64),
    userobm_company character varying(64),
    userobm_direction character varying(64),
    userobm_service character varying(64),
    userobm_address1 character varying(64),
    userobm_address2 character varying(64),
    userobm_address3 character varying(64),
    userobm_zipcode character varying(14),
    userobm_town character varying(64),
    userobm_expresspostal character varying(16),
    userobm_country_iso3166 character(2) DEFAULT '0'::bpchar,
    userobm_phone character varying(32) DEFAULT ''::character varying,
    userobm_phone2 character varying(32) DEFAULT ''::character varying,
    userobm_mobile character varying(32) DEFAULT ''::character varying,
    userobm_fax character varying(32) DEFAULT ''::character varying,
    userobm_fax2 character varying(32) DEFAULT ''::character varying,
    userobm_web_perms integer DEFAULT 0,
    userobm_web_list text,
    userobm_web_all integer DEFAULT 0,
    userobm_mail_perms integer DEFAULT 0,
    userobm_mail_ext_perms integer DEFAULT 0,
    userobm_email text DEFAULT ''::text,
    userobm_mail_server_id integer,
    userobm_mail_quota integer DEFAULT 0,
    userobm_mail_quota_use integer DEFAULT 0,
    userobm_mail_login_date timestamp without time zone,
    userobm_nomade_perms integer DEFAULT 0,
    userobm_nomade_enable integer DEFAULT 0,
    userobm_nomade_local_copy integer DEFAULT 0,
    userobm_nomade_datebegin timestamp without time zone,
    userobm_nomade_dateend timestamp without time zone,
    userobm_email_nomade character varying(64) DEFAULT ''::character varying,
    userobm_vacation_enable integer DEFAULT 0,
    userobm_vacation_datebegin timestamp without time zone,
    userobm_vacation_dateend timestamp without time zone,
    userobm_vacation_message text DEFAULT ''::text,
    userobm_samba_perms integer DEFAULT 0,
    userobm_samba_home character varying(255) DEFAULT ''::character varying,
    userobm_samba_home_drive character(2) DEFAULT ''::bpchar,
    userobm_samba_logon_script character varying(128) DEFAULT ''::character varying,
    userobm_host_id integer,
    userobm_description character varying(255),
    userobm_location character varying(255),
    userobm_education character varying(255),
    userobm_photo_id integer
);


--
-- Name: userobm_sessionlog; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE userobm_sessionlog (
    userobm_sessionlog_sid character varying(32) DEFAULT ''::character varying NOT NULL,
    userobm_sessionlog_session_name character varying(32) DEFAULT ''::character varying NOT NULL,
    userobm_sessionlog_userobm_id integer,
    userobm_sessionlog_timeupdate timestamp without time zone,
    userobm_sessionlog_timecreate timestamp without time zone DEFAULT now(),
    userobm_sessionlog_nb_connexions integer DEFAULT 0 NOT NULL,
    userobm_sessionlog_lastpage character varying(32) DEFAULT '0'::character varying NOT NULL,
    userobm_sessionlog_ip character varying(32) DEFAULT '0'::character varying NOT NULL
);


--
-- Name: userobmgroup; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE userobmgroup (
    userobmgroup_group_id integer NOT NULL,
    userobmgroup_userobm_id integer NOT NULL
);


--
-- Name: userobmpref; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE userobmpref (
    userobmpref_id integer NOT NULL,
    userobmpref_user_id integer,
    userobmpref_option character varying(50) NOT NULL,
    userobmpref_value character varying(50) NOT NULL
);


--
-- Name: usersystem; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE usersystem (
    usersystem_id integer NOT NULL,
    usersystem_login character varying(32) DEFAULT ''::character varying NOT NULL,
    usersystem_password character varying(32) DEFAULT ''::character varying NOT NULL,
    usersystem_uid character varying(6) DEFAULT NULL::character varying,
    usersystem_gid character varying(6) DEFAULT NULL::character varying,
    usersystem_homedir character varying(32) DEFAULT '/tmp'::character varying NOT NULL,
    usersystem_lastname character varying(32) DEFAULT NULL::character varying,
    usersystem_firstname character varying(32) DEFAULT NULL::character varying,
    usersystem_shell character varying(32) DEFAULT NULL::character varying
);


--
-- Name: website; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE website (
    website_id integer NOT NULL,
    website_entity_id integer NOT NULL,
    website_label character varying(255) NOT NULL,
    website_url text
);


--
-- Name: account_account_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE account_account_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: account_account_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE account_account_id_seq OWNED BY account.account_id;


--
-- Name: address_address_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE address_address_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: address_address_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE address_address_id_seq OWNED BY address.address_id;


--
-- Name: category_category_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE category_category_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: category_category_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE category_category_id_seq OWNED BY category.category_id;


--
-- Name: company_company_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE company_company_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: company_company_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE company_company_id_seq OWNED BY company.company_id;


--
-- Name: companyactivity_companyactivity_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE companyactivity_companyactivity_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: companyactivity_companyactivity_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE companyactivity_companyactivity_id_seq OWNED BY companyactivity.companyactivity_id;


--
-- Name: companynafcode_companynafcode_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE companynafcode_companynafcode_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: companynafcode_companynafcode_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE companynafcode_companynafcode_id_seq OWNED BY companynafcode.companynafcode_id;


--
-- Name: companytype_companytype_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE companytype_companytype_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: companytype_companytype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE companytype_companytype_id_seq OWNED BY companytype.companytype_id;


--
-- Name: contact_contact_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE contact_contact_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: contact_contact_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE contact_contact_id_seq OWNED BY contact.contact_id;


--
-- Name: contactfunction_contactfunction_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE contactfunction_contactfunction_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: contactfunction_contactfunction_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE contactfunction_contactfunction_id_seq OWNED BY contactfunction.contactfunction_id;


--
-- Name: contract_contract_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE contract_contract_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: contract_contract_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE contract_contract_id_seq OWNED BY contract.contract_id;


--
-- Name: contractpriority_contractpriority_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE contractpriority_contractpriority_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: contractpriority_contractpriority_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE contractpriority_contractpriority_id_seq OWNED BY contractpriority.contractpriority_id;


--
-- Name: contractstatus_contractstatus_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE contractstatus_contractstatus_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: contractstatus_contractstatus_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE contractstatus_contractstatus_id_seq OWNED BY contractstatus.contractstatus_id;


--
-- Name: contracttype_contracttype_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE contracttype_contracttype_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: contracttype_contracttype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE contracttype_contracttype_id_seq OWNED BY contracttype.contracttype_id;


--
-- Name: cv_cv_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE cv_cv_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: cv_cv_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE cv_cv_id_seq OWNED BY cv.cv_id;


--
-- Name: datasource_datasource_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE datasource_datasource_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: datasource_datasource_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE datasource_datasource_id_seq OWNED BY datasource.datasource_id;


--
-- Name: deal_deal_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE deal_deal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: deal_deal_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE deal_deal_id_seq OWNED BY deal.deal_id;


--
-- Name: dealcompany_dealcompany_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE dealcompany_dealcompany_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: dealcompany_dealcompany_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE dealcompany_dealcompany_id_seq OWNED BY dealcompany.dealcompany_id;


--
-- Name: dealcompanyrole_dealcompanyrole_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE dealcompanyrole_dealcompanyrole_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: dealcompanyrole_dealcompanyrole_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE dealcompanyrole_dealcompanyrole_id_seq OWNED BY dealcompanyrole.dealcompanyrole_id;


--
-- Name: dealstatus_dealstatus_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE dealstatus_dealstatus_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: dealstatus_dealstatus_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE dealstatus_dealstatus_id_seq OWNED BY dealstatus.dealstatus_id;


--
-- Name: dealtype_dealtype_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE dealtype_dealtype_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: dealtype_dealtype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE dealtype_dealtype_id_seq OWNED BY dealtype.dealtype_id;


--
-- Name: defaultodttemplate_defaultodttemplate_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE defaultodttemplate_defaultodttemplate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: defaultodttemplate_defaultodttemplate_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE defaultodttemplate_defaultodttemplate_id_seq OWNED BY defaultodttemplate.defaultodttemplate_id;


--
-- Name: deleted_deleted_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE deleted_deleted_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: deleted_deleted_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE deleted_deleted_id_seq OWNED BY deleted.deleted_id;


--
-- Name: deletedevent_deletedevent_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE deletedevent_deletedevent_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: deletedevent_deletedevent_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE deletedevent_deletedevent_id_seq OWNED BY deletedevent.deletedevent_id;


--
-- Name: displaypref_display_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE displaypref_display_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: displaypref_display_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE displaypref_display_id_seq OWNED BY displaypref.display_id;


--
-- Name: document_document_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE document_document_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: document_document_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE document_document_id_seq OWNED BY document.document_id;


--
-- Name: documentmimetype_documentmimetype_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE documentmimetype_documentmimetype_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: documentmimetype_documentmimetype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE documentmimetype_documentmimetype_id_seq OWNED BY documentmimetype.documentmimetype_id;


--
-- Name: domain_domain_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE domain_domain_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: domain_domain_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE domain_domain_id_seq OWNED BY domain.domain_id;


--
-- Name: email_email_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE email_email_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: email_email_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE email_email_id_seq OWNED BY email.email_id;


--
-- Name: entity_entity_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE entity_entity_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: entity_entity_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE entity_entity_id_seq OWNED BY entity.entity_id;


--
-- Name: event_event_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE event_event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: event_event_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE event_event_id_seq OWNED BY event.event_id;


--
-- Name: eventcategory1_eventcategory1_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE eventcategory1_eventcategory1_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: eventcategory1_eventcategory1_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE eventcategory1_eventcategory1_id_seq OWNED BY eventcategory1.eventcategory1_id;


--
-- Name: host_host_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE host_host_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: host_host_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE host_host_id_seq OWNED BY host.host_id;


--
-- Name: im_im_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE im_im_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: im_im_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE im_im_id_seq OWNED BY im.im_id;


--
-- Name: import_import_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE import_import_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: import_import_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE import_import_id_seq OWNED BY import.import_id;


--
-- Name: incident_incident_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE incident_incident_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: incident_incident_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE incident_incident_id_seq OWNED BY incident.incident_id;


--
-- Name: incidentpriority_incidentpriority_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE incidentpriority_incidentpriority_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: incidentpriority_incidentpriority_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE incidentpriority_incidentpriority_id_seq OWNED BY incidentpriority.incidentpriority_id;


--
-- Name: incidentresolutiontype_incidentresolutiontype_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE incidentresolutiontype_incidentresolutiontype_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: incidentresolutiontype_incidentresolutiontype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE incidentresolutiontype_incidentresolutiontype_id_seq OWNED BY incidentresolutiontype.incidentresolutiontype_id;


--
-- Name: incidentstatus_incidentstatus_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE incidentstatus_incidentstatus_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: incidentstatus_incidentstatus_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE incidentstatus_incidentstatus_id_seq OWNED BY incidentstatus.incidentstatus_id;


--
-- Name: invoice_invoice_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE invoice_invoice_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: invoice_invoice_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE invoice_invoice_id_seq OWNED BY invoice.invoice_id;


--
-- Name: kind_kind_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE kind_kind_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: kind_kind_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE kind_kind_id_seq OWNED BY kind.kind_id;


--
-- Name: lead_lead_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE lead_lead_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: lead_lead_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE lead_lead_id_seq OWNED BY lead.lead_id;


--
-- Name: leadsource_leadsource_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE leadsource_leadsource_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: leadsource_leadsource_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE leadsource_leadsource_id_seq OWNED BY leadsource.leadsource_id;


--
-- Name: leadstatus_leadstatus_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE leadstatus_leadstatus_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: leadstatus_leadstatus_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE leadstatus_leadstatus_id_seq OWNED BY leadstatus.leadstatus_id;


--
-- Name: list_list_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE list_list_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: list_list_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE list_list_id_seq OWNED BY list.list_id;


--
-- Name: mailserver_mailserver_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE mailserver_mailserver_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: mailserver_mailserver_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE mailserver_mailserver_id_seq OWNED BY mailserver.mailserver_id;


--
-- Name: mailshare_mailshare_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE mailshare_mailshare_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: mailshare_mailshare_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE mailshare_mailshare_id_seq OWNED BY mailshare.mailshare_id;


--
-- Name: obmbookmark_obmbookmark_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE obmbookmark_obmbookmark_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: obmbookmark_obmbookmark_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE obmbookmark_obmbookmark_id_seq OWNED BY obmbookmark.obmbookmark_id;


--
-- Name: obmbookmarkproperty_obmbookmarkproperty_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE obmbookmarkproperty_obmbookmarkproperty_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: obmbookmarkproperty_obmbookmarkproperty_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE obmbookmarkproperty_obmbookmarkproperty_id_seq OWNED BY obmbookmarkproperty.obmbookmarkproperty_id;


--
-- Name: ogroup_ogroup_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE ogroup_ogroup_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: ogroup_ogroup_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE ogroup_ogroup_id_seq OWNED BY ogroup.ogroup_id;


--
-- Name: ogrouplink_ogrouplink_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE ogrouplink_ogrouplink_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: ogrouplink_ogrouplink_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE ogrouplink_ogrouplink_id_seq OWNED BY ogrouplink.ogrouplink_id;


--
-- Name: organizationalchart_organizationalchart_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE organizationalchart_organizationalchart_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: organizationalchart_organizationalchart_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE organizationalchart_organizationalchart_id_seq OWNED BY organizationalchart.organizationalchart_id;


--
-- Name: parentdeal_parentdeal_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE parentdeal_parentdeal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: parentdeal_parentdeal_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE parentdeal_parentdeal_id_seq OWNED BY parentdeal.parentdeal_id;


--
-- Name: payment_payment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE payment_payment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: payment_payment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE payment_payment_id_seq OWNED BY payment.payment_id;


--
-- Name: paymentkind_paymentkind_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE paymentkind_paymentkind_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: paymentkind_paymentkind_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE paymentkind_paymentkind_id_seq OWNED BY paymentkind.paymentkind_id;


--
-- Name: phone_phone_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE phone_phone_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: phone_phone_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE phone_phone_id_seq OWNED BY phone.phone_id;


--
-- Name: profile_profile_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE profile_profile_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: profile_profile_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE profile_profile_id_seq OWNED BY profile.profile_id;


--
-- Name: profilemodule_profilemodule_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE profilemodule_profilemodule_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: profilemodule_profilemodule_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE profilemodule_profilemodule_id_seq OWNED BY profilemodule.profilemodule_id;


--
-- Name: profileproperty_profileproperty_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE profileproperty_profileproperty_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: profileproperty_profileproperty_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE profileproperty_profileproperty_id_seq OWNED BY profileproperty.profileproperty_id;


--
-- Name: profilepropertyvalue_profilepropertyvalue_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE profilepropertyvalue_profilepropertyvalue_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: profilepropertyvalue_profilepropertyvalue_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE profilepropertyvalue_profilepropertyvalue_id_seq OWNED BY profilepropertyvalue.profilepropertyvalue_id;


--
-- Name: profilesection_profilesection_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE profilesection_profilesection_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: profilesection_profilesection_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE profilesection_profilesection_id_seq OWNED BY profilesection.profilesection_id;


--
-- Name: project_project_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE project_project_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: project_project_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE project_project_id_seq OWNED BY project.project_id;


--
-- Name: projectclosing_projectclosing_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE projectclosing_projectclosing_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: projectclosing_projectclosing_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE projectclosing_projectclosing_id_seq OWNED BY projectclosing.projectclosing_id;


--
-- Name: projectreftask_projectreftask_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE projectreftask_projectreftask_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: projectreftask_projectreftask_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE projectreftask_projectreftask_id_seq OWNED BY projectreftask.projectreftask_id;


--
-- Name: projecttask_projecttask_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE projecttask_projecttask_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: projecttask_projecttask_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE projecttask_projecttask_id_seq OWNED BY projecttask.projecttask_id;


--
-- Name: projectuser_projectuser_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE projectuser_projectuser_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: projectuser_projectuser_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE projectuser_projectuser_id_seq OWNED BY projectuser.projectuser_id;


--
-- Name: publication_publication_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE publication_publication_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: publication_publication_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE publication_publication_id_seq OWNED BY publication.publication_id;


--
-- Name: publicationtype_publicationtype_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE publicationtype_publicationtype_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: publicationtype_publicationtype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE publicationtype_publicationtype_id_seq OWNED BY publicationtype.publicationtype_id;


--
-- Name: region_region_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE region_region_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: region_region_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE region_region_id_seq OWNED BY region.region_id;


--
-- Name: resource_resource_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE resource_resource_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: resource_resource_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE resource_resource_id_seq OWNED BY resource.resource_id;


--
-- Name: resourceitem_resourceitem_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE resourceitem_resourceitem_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: resourceitem_resourceitem_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE resourceitem_resourceitem_id_seq OWNED BY resourceitem.resourceitem_id;


--
-- Name: resourcetype_resourcetype_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE resourcetype_resourcetype_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: resourcetype_resourcetype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE resourcetype_resourcetype_id_seq OWNED BY resourcetype.resourcetype_id;


--
-- Name: rgroup_rgroup_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE rgroup_rgroup_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: rgroup_rgroup_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE rgroup_rgroup_id_seq OWNED BY rgroup.rgroup_id;


--
-- Name: service_service_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE service_service_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: service_service_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE service_service_id_seq OWNED BY service.service_id;


--
-- Name: serviceproperty_serviceproperty_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE serviceproperty_serviceproperty_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: serviceproperty_serviceproperty_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE serviceproperty_serviceproperty_id_seq OWNED BY serviceproperty.serviceproperty_id;


--
-- Name: subscription_subscription_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE subscription_subscription_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: subscription_subscription_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE subscription_subscription_id_seq OWNED BY subscription.subscription_id;


--
-- Name: subscriptionreception_subscriptionreception_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE subscriptionreception_subscriptionreception_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: subscriptionreception_subscriptionreception_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE subscriptionreception_subscriptionreception_id_seq OWNED BY subscriptionreception.subscriptionreception_id;


--
-- Name: tasktype_tasktype_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE tasktype_tasktype_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: tasktype_tasktype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE tasktype_tasktype_id_seq OWNED BY tasktype.tasktype_id;


--
-- Name: timetask_timetask_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE timetask_timetask_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: timetask_timetask_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE timetask_timetask_id_seq OWNED BY timetask.timetask_id;


--
-- Name: todo_todo_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE todo_todo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: todo_todo_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE todo_todo_id_seq OWNED BY todo.todo_id;


--
-- Name: ugroup_group_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE ugroup_group_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: ugroup_group_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE ugroup_group_id_seq OWNED BY ugroup.group_id;


--
-- Name: updated_updated_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE updated_updated_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: updated_updated_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE updated_updated_id_seq OWNED BY updated.updated_id;


--
-- Name: updatedlinks_updatedlinks_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE updatedlinks_updatedlinks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: updatedlinks_updatedlinks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE updatedlinks_updatedlinks_id_seq OWNED BY updatedlinks.updatedlinks_id;


--
-- Name: userobm_userobm_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE userobm_userobm_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: userobm_userobm_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE userobm_userobm_id_seq OWNED BY userobm.userobm_id;


--
-- Name: userobmpref_userobmpref_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE userobmpref_userobmpref_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: userobmpref_userobmpref_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE userobmpref_userobmpref_id_seq OWNED BY userobmpref.userobmpref_id;


--
-- Name: usersystem_usersystem_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE usersystem_usersystem_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: usersystem_usersystem_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE usersystem_usersystem_id_seq OWNED BY usersystem.usersystem_id;


--
-- Name: website_website_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE website_website_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: website_website_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE website_website_id_seq OWNED BY website.website_id;


--
-- Name: account_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE account ALTER COLUMN account_id SET DEFAULT nextval('account_account_id_seq'::regclass);


--
-- Name: address_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE address ALTER COLUMN address_id SET DEFAULT nextval('address_address_id_seq'::regclass);


--
-- Name: category_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE category ALTER COLUMN category_id SET DEFAULT nextval('category_category_id_seq'::regclass);


--
-- Name: company_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE company ALTER COLUMN company_id SET DEFAULT nextval('company_company_id_seq'::regclass);


--
-- Name: companyactivity_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE companyactivity ALTER COLUMN companyactivity_id SET DEFAULT nextval('companyactivity_companyactivity_id_seq'::regclass);


--
-- Name: companynafcode_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE companynafcode ALTER COLUMN companynafcode_id SET DEFAULT nextval('companynafcode_companynafcode_id_seq'::regclass);


--
-- Name: companytype_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE companytype ALTER COLUMN companytype_id SET DEFAULT nextval('companytype_companytype_id_seq'::regclass);


--
-- Name: contact_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE contact ALTER COLUMN contact_id SET DEFAULT nextval('contact_contact_id_seq'::regclass);


--
-- Name: contactfunction_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE contactfunction ALTER COLUMN contactfunction_id SET DEFAULT nextval('contactfunction_contactfunction_id_seq'::regclass);


--
-- Name: contract_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE contract ALTER COLUMN contract_id SET DEFAULT nextval('contract_contract_id_seq'::regclass);


--
-- Name: contractpriority_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE contractpriority ALTER COLUMN contractpriority_id SET DEFAULT nextval('contractpriority_contractpriority_id_seq'::regclass);


--
-- Name: contractstatus_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE contractstatus ALTER COLUMN contractstatus_id SET DEFAULT nextval('contractstatus_contractstatus_id_seq'::regclass);


--
-- Name: contracttype_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE contracttype ALTER COLUMN contracttype_id SET DEFAULT nextval('contracttype_contracttype_id_seq'::regclass);


--
-- Name: cv_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE cv ALTER COLUMN cv_id SET DEFAULT nextval('cv_cv_id_seq'::regclass);


--
-- Name: datasource_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE datasource ALTER COLUMN datasource_id SET DEFAULT nextval('datasource_datasource_id_seq'::regclass);


--
-- Name: deal_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE deal ALTER COLUMN deal_id SET DEFAULT nextval('deal_deal_id_seq'::regclass);


--
-- Name: dealcompany_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE dealcompany ALTER COLUMN dealcompany_id SET DEFAULT nextval('dealcompany_dealcompany_id_seq'::regclass);


--
-- Name: dealcompanyrole_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE dealcompanyrole ALTER COLUMN dealcompanyrole_id SET DEFAULT nextval('dealcompanyrole_dealcompanyrole_id_seq'::regclass);


--
-- Name: dealstatus_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE dealstatus ALTER COLUMN dealstatus_id SET DEFAULT nextval('dealstatus_dealstatus_id_seq'::regclass);


--
-- Name: dealtype_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE dealtype ALTER COLUMN dealtype_id SET DEFAULT nextval('dealtype_dealtype_id_seq'::regclass);


--
-- Name: defaultodttemplate_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE defaultodttemplate ALTER COLUMN defaultodttemplate_id SET DEFAULT nextval('defaultodttemplate_defaultodttemplate_id_seq'::regclass);


--
-- Name: deleted_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE deleted ALTER COLUMN deleted_id SET DEFAULT nextval('deleted_deleted_id_seq'::regclass);


--
-- Name: deletedevent_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE deletedevent ALTER COLUMN deletedevent_id SET DEFAULT nextval('deletedevent_deletedevent_id_seq'::regclass);


--
-- Name: display_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE displaypref ALTER COLUMN display_id SET DEFAULT nextval('displaypref_display_id_seq'::regclass);


--
-- Name: document_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE document ALTER COLUMN document_id SET DEFAULT nextval('document_document_id_seq'::regclass);


--
-- Name: documentmimetype_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE documentmimetype ALTER COLUMN documentmimetype_id SET DEFAULT nextval('documentmimetype_documentmimetype_id_seq'::regclass);


--
-- Name: domain_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE domain ALTER COLUMN domain_id SET DEFAULT nextval('domain_domain_id_seq'::regclass);


--
-- Name: email_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE email ALTER COLUMN email_id SET DEFAULT nextval('email_email_id_seq'::regclass);


--
-- Name: entity_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE entity ALTER COLUMN entity_id SET DEFAULT nextval('entity_entity_id_seq'::regclass);


--
-- Name: event_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE event ALTER COLUMN event_id SET DEFAULT nextval('event_event_id_seq'::regclass);


--
-- Name: eventcategory1_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE eventcategory1 ALTER COLUMN eventcategory1_id SET DEFAULT nextval('eventcategory1_eventcategory1_id_seq'::regclass);


--
-- Name: host_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE host ALTER COLUMN host_id SET DEFAULT nextval('host_host_id_seq'::regclass);


--
-- Name: im_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE im ALTER COLUMN im_id SET DEFAULT nextval('im_im_id_seq'::regclass);


--
-- Name: import_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE import ALTER COLUMN import_id SET DEFAULT nextval('import_import_id_seq'::regclass);


--
-- Name: incident_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE incident ALTER COLUMN incident_id SET DEFAULT nextval('incident_incident_id_seq'::regclass);


--
-- Name: incidentpriority_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE incidentpriority ALTER COLUMN incidentpriority_id SET DEFAULT nextval('incidentpriority_incidentpriority_id_seq'::regclass);


--
-- Name: incidentresolutiontype_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE incidentresolutiontype ALTER COLUMN incidentresolutiontype_id SET DEFAULT nextval('incidentresolutiontype_incidentresolutiontype_id_seq'::regclass);


--
-- Name: incidentstatus_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE incidentstatus ALTER COLUMN incidentstatus_id SET DEFAULT nextval('incidentstatus_incidentstatus_id_seq'::regclass);


--
-- Name: invoice_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE invoice ALTER COLUMN invoice_id SET DEFAULT nextval('invoice_invoice_id_seq'::regclass);


--
-- Name: kind_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE kind ALTER COLUMN kind_id SET DEFAULT nextval('kind_kind_id_seq'::regclass);


--
-- Name: lead_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE lead ALTER COLUMN lead_id SET DEFAULT nextval('lead_lead_id_seq'::regclass);


--
-- Name: leadsource_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE leadsource ALTER COLUMN leadsource_id SET DEFAULT nextval('leadsource_leadsource_id_seq'::regclass);


--
-- Name: leadstatus_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE leadstatus ALTER COLUMN leadstatus_id SET DEFAULT nextval('leadstatus_leadstatus_id_seq'::regclass);


--
-- Name: list_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE list ALTER COLUMN list_id SET DEFAULT nextval('list_list_id_seq'::regclass);


--
-- Name: mailserver_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE mailserver ALTER COLUMN mailserver_id SET DEFAULT nextval('mailserver_mailserver_id_seq'::regclass);


--
-- Name: mailshare_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE mailshare ALTER COLUMN mailshare_id SET DEFAULT nextval('mailshare_mailshare_id_seq'::regclass);


--
-- Name: obmbookmark_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE obmbookmark ALTER COLUMN obmbookmark_id SET DEFAULT nextval('obmbookmark_obmbookmark_id_seq'::regclass);


--
-- Name: obmbookmarkproperty_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE obmbookmarkproperty ALTER COLUMN obmbookmarkproperty_id SET DEFAULT nextval('obmbookmarkproperty_obmbookmarkproperty_id_seq'::regclass);


--
-- Name: ogroup_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ogroup ALTER COLUMN ogroup_id SET DEFAULT nextval('ogroup_ogroup_id_seq'::regclass);


--
-- Name: ogrouplink_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ogrouplink ALTER COLUMN ogrouplink_id SET DEFAULT nextval('ogrouplink_ogrouplink_id_seq'::regclass);


--
-- Name: organizationalchart_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE organizationalchart ALTER COLUMN organizationalchart_id SET DEFAULT nextval('organizationalchart_organizationalchart_id_seq'::regclass);


--
-- Name: parentdeal_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE parentdeal ALTER COLUMN parentdeal_id SET DEFAULT nextval('parentdeal_parentdeal_id_seq'::regclass);


--
-- Name: payment_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE payment ALTER COLUMN payment_id SET DEFAULT nextval('payment_payment_id_seq'::regclass);


--
-- Name: paymentkind_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE paymentkind ALTER COLUMN paymentkind_id SET DEFAULT nextval('paymentkind_paymentkind_id_seq'::regclass);


--
-- Name: phone_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE phone ALTER COLUMN phone_id SET DEFAULT nextval('phone_phone_id_seq'::regclass);


--
-- Name: profile_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE profile ALTER COLUMN profile_id SET DEFAULT nextval('profile_profile_id_seq'::regclass);


--
-- Name: profilemodule_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE profilemodule ALTER COLUMN profilemodule_id SET DEFAULT nextval('profilemodule_profilemodule_id_seq'::regclass);


--
-- Name: profileproperty_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE profileproperty ALTER COLUMN profileproperty_id SET DEFAULT nextval('profileproperty_profileproperty_id_seq'::regclass);


--
-- Name: profilepropertyvalue_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE profilepropertyvalue ALTER COLUMN profilepropertyvalue_id SET DEFAULT nextval('profilepropertyvalue_profilepropertyvalue_id_seq'::regclass);


--
-- Name: profilesection_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE profilesection ALTER COLUMN profilesection_id SET DEFAULT nextval('profilesection_profilesection_id_seq'::regclass);


--
-- Name: project_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE project ALTER COLUMN project_id SET DEFAULT nextval('project_project_id_seq'::regclass);


--
-- Name: projectclosing_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE projectclosing ALTER COLUMN projectclosing_id SET DEFAULT nextval('projectclosing_projectclosing_id_seq'::regclass);


--
-- Name: projectreftask_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE projectreftask ALTER COLUMN projectreftask_id SET DEFAULT nextval('projectreftask_projectreftask_id_seq'::regclass);


--
-- Name: projecttask_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE projecttask ALTER COLUMN projecttask_id SET DEFAULT nextval('projecttask_projecttask_id_seq'::regclass);


--
-- Name: projectuser_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE projectuser ALTER COLUMN projectuser_id SET DEFAULT nextval('projectuser_projectuser_id_seq'::regclass);


--
-- Name: publication_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE publication ALTER COLUMN publication_id SET DEFAULT nextval('publication_publication_id_seq'::regclass);


--
-- Name: publicationtype_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE publicationtype ALTER COLUMN publicationtype_id SET DEFAULT nextval('publicationtype_publicationtype_id_seq'::regclass);


--
-- Name: region_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE region ALTER COLUMN region_id SET DEFAULT nextval('region_region_id_seq'::regclass);


--
-- Name: resource_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE resource ALTER COLUMN resource_id SET DEFAULT nextval('resource_resource_id_seq'::regclass);


--
-- Name: resourceitem_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE resourceitem ALTER COLUMN resourceitem_id SET DEFAULT nextval('resourceitem_resourceitem_id_seq'::regclass);


--
-- Name: resourcetype_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE resourcetype ALTER COLUMN resourcetype_id SET DEFAULT nextval('resourcetype_resourcetype_id_seq'::regclass);


--
-- Name: rgroup_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE rgroup ALTER COLUMN rgroup_id SET DEFAULT nextval('rgroup_rgroup_id_seq'::regclass);


--
-- Name: service_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE service ALTER COLUMN service_id SET DEFAULT nextval('service_service_id_seq'::regclass);


--
-- Name: serviceproperty_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE serviceproperty ALTER COLUMN serviceproperty_id SET DEFAULT nextval('serviceproperty_serviceproperty_id_seq'::regclass);


--
-- Name: subscription_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE subscription ALTER COLUMN subscription_id SET DEFAULT nextval('subscription_subscription_id_seq'::regclass);


--
-- Name: subscriptionreception_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE subscriptionreception ALTER COLUMN subscriptionreception_id SET DEFAULT nextval('subscriptionreception_subscriptionreception_id_seq'::regclass);


--
-- Name: tasktype_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE tasktype ALTER COLUMN tasktype_id SET DEFAULT nextval('tasktype_tasktype_id_seq'::regclass);


--
-- Name: timetask_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE timetask ALTER COLUMN timetask_id SET DEFAULT nextval('timetask_timetask_id_seq'::regclass);


--
-- Name: todo_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE todo ALTER COLUMN todo_id SET DEFAULT nextval('todo_todo_id_seq'::regclass);


--
-- Name: group_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ugroup ALTER COLUMN group_id SET DEFAULT nextval('ugroup_group_id_seq'::regclass);


--
-- Name: updated_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE updated ALTER COLUMN updated_id SET DEFAULT nextval('updated_updated_id_seq'::regclass);


--
-- Name: updatedlinks_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE updatedlinks ALTER COLUMN updatedlinks_id SET DEFAULT nextval('updatedlinks_updatedlinks_id_seq'::regclass);


--
-- Name: userobm_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE userobm ALTER COLUMN userobm_id SET DEFAULT nextval('userobm_userobm_id_seq'::regclass);


--
-- Name: userobmpref_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE userobmpref ALTER COLUMN userobmpref_id SET DEFAULT nextval('userobmpref_userobmpref_id_seq'::regclass);


--
-- Name: usersystem_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE usersystem ALTER COLUMN usersystem_id SET DEFAULT nextval('usersystem_usersystem_id_seq'::regclass);


--
-- Name: website_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE website ALTER COLUMN website_id SET DEFAULT nextval('website_website_id_seq'::regclass);


--
-- Name: account_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY account
    ADD CONSTRAINT account_pkey PRIMARY KEY (account_id);


--
-- Name: accountentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY accountentity
    ADD CONSTRAINT accountentity_pkey PRIMARY KEY (accountentity_entity_id, accountentity_account_id);


--
-- Name: activeuserobm_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY activeuserobm
    ADD CONSTRAINT activeuserobm_pkey PRIMARY KEY (activeuserobm_sid);


--
-- Name: address_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY address
    ADD CONSTRAINT address_pkey PRIMARY KEY (address_id);


--
-- Name: calendarentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY calendarentity
    ADD CONSTRAINT calendarentity_pkey PRIMARY KEY (calendarentity_entity_id, calendarentity_calendar_id);


--
-- Name: category_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY category
    ADD CONSTRAINT category_pkey PRIMARY KEY (category_id);


--
-- Name: categorylink_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY categorylink
    ADD CONSTRAINT categorylink_pkey PRIMARY KEY (categorylink_category_id, categorylink_entity_id);


--
-- Name: company_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY company
    ADD CONSTRAINT company_pkey PRIMARY KEY (company_id);


--
-- Name: companyactivity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY companyactivity
    ADD CONSTRAINT companyactivity_pkey PRIMARY KEY (companyactivity_id);


--
-- Name: companyentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY companyentity
    ADD CONSTRAINT companyentity_pkey PRIMARY KEY (companyentity_entity_id, companyentity_company_id);


--
-- Name: companynafcode_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY companynafcode
    ADD CONSTRAINT companynafcode_pkey PRIMARY KEY (companynafcode_id);


--
-- Name: companytype_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY companytype
    ADD CONSTRAINT companytype_pkey PRIMARY KEY (companytype_id);


--
-- Name: contact_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_pkey PRIMARY KEY (contact_id);


--
-- Name: contactentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY contactentity
    ADD CONSTRAINT contactentity_pkey PRIMARY KEY (contactentity_entity_id, contactentity_contact_id);


--
-- Name: contactfunction_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY contactfunction
    ADD CONSTRAINT contactfunction_pkey PRIMARY KEY (contactfunction_id);


--
-- Name: contract_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_pkey PRIMARY KEY (contract_id);


--
-- Name: contractentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY contractentity
    ADD CONSTRAINT contractentity_pkey PRIMARY KEY (contractentity_entity_id, contractentity_contract_id);


--
-- Name: contractpriority_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY contractpriority
    ADD CONSTRAINT contractpriority_pkey PRIMARY KEY (contractpriority_id);


--
-- Name: contractstatus_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY contractstatus
    ADD CONSTRAINT contractstatus_pkey PRIMARY KEY (contractstatus_id);


--
-- Name: contracttype_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY contracttype
    ADD CONSTRAINT contracttype_pkey PRIMARY KEY (contracttype_id);


--
-- Name: country_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY country
    ADD CONSTRAINT country_pkey PRIMARY KEY (country_iso3166, country_lang);


--
-- Name: cv_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cv
    ADD CONSTRAINT cv_pkey PRIMARY KEY (cv_id);


--
-- Name: cventity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cventity
    ADD CONSTRAINT cventity_pkey PRIMARY KEY (cventity_entity_id, cventity_cv_id);


--
-- Name: datasource_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY datasource
    ADD CONSTRAINT datasource_pkey PRIMARY KEY (datasource_id);


--
-- Name: deal_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_pkey PRIMARY KEY (deal_id);


--
-- Name: dealcompany_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY dealcompany
    ADD CONSTRAINT dealcompany_pkey PRIMARY KEY (dealcompany_id);


--
-- Name: dealcompanyrole_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY dealcompanyrole
    ADD CONSTRAINT dealcompanyrole_pkey PRIMARY KEY (dealcompanyrole_id);


--
-- Name: dealentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY dealentity
    ADD CONSTRAINT dealentity_pkey PRIMARY KEY (dealentity_entity_id, dealentity_deal_id);


--
-- Name: dealstatus_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY dealstatus
    ADD CONSTRAINT dealstatus_pkey PRIMARY KEY (dealstatus_id);


--
-- Name: dealtype_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY dealtype
    ADD CONSTRAINT dealtype_pkey PRIMARY KEY (dealtype_id);


--
-- Name: defaultodttemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY defaultodttemplate
    ADD CONSTRAINT defaultodttemplate_pkey PRIMARY KEY (defaultodttemplate_id);


--
-- Name: defaultodttemplateentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY defaultodttemplateentity
    ADD CONSTRAINT defaultodttemplateentity_pkey PRIMARY KEY (defaultodttemplateentity_entity_id, defaultodttemplateentity_defaultodttemplate_id);


--
-- Name: deleted_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY deleted
    ADD CONSTRAINT deleted_pkey PRIMARY KEY (deleted_id);


--
-- Name: deletedcontact_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY deletedcontact
    ADD CONSTRAINT deletedcontact_pkey PRIMARY KEY (deletedcontact_contact_id);


--
-- Name: deletedtodo_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY deletedtodo
    ADD CONSTRAINT deletedtodo_pkey PRIMARY KEY (deletedtodo_todo_id);


--
-- Name: deleteduser_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY deleteduser
    ADD CONSTRAINT deleteduser_pkey PRIMARY KEY (deleteduser_user_id);


--
-- Name: displaypref_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY displaypref
    ADD CONSTRAINT displaypref_key UNIQUE (display_user_id, display_entity, display_fieldname);


--
-- Name: displaypref_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY displaypref
    ADD CONSTRAINT displaypref_pkey PRIMARY KEY (display_id);


--
-- Name: document_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY document
    ADD CONSTRAINT document_pkey PRIMARY KEY (document_id);


--
-- Name: documententity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY documententity
    ADD CONSTRAINT documententity_pkey PRIMARY KEY (documententity_entity_id, documententity_document_id);


--
-- Name: documentlink_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY documentlink
    ADD CONSTRAINT documentlink_pkey PRIMARY KEY (documentlink_document_id, documentlink_entity_id);


--
-- Name: documentmimetype_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY documentmimetype
    ADD CONSTRAINT documentmimetype_pkey PRIMARY KEY (documentmimetype_id);


--
-- Name: domain_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY domain
    ADD CONSTRAINT domain_pkey PRIMARY KEY (domain_id);


--
-- Name: domainentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY domainentity
    ADD CONSTRAINT domainentity_pkey PRIMARY KEY (domainentity_entity_id, domainentity_domain_id);


--
-- Name: domainproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY domainproperty
    ADD CONSTRAINT domainproperty_pkey PRIMARY KEY (domainproperty_key);


--
-- Name: domainpropertyvalue_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY domainpropertyvalue
    ADD CONSTRAINT domainpropertyvalue_pkey PRIMARY KEY (domainpropertyvalue_domain_id, domainpropertyvalue_property_key);


--
-- Name: domainservice_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY domainservice
    ADD CONSTRAINT domainservice_pkey PRIMARY KEY (domainservice_service_id, domainservice_domain_id);


--
-- Name: domainservicevalue_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY domainservicevalue
    ADD CONSTRAINT domainservicevalue_pkey PRIMARY KEY (domainservicevalue_serviceproperty_id, domainservicevalue_domain_id);


--
-- Name: email_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY email
    ADD CONSTRAINT email_pkey PRIMARY KEY (email_id);


--
-- Name: entity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY entity
    ADD CONSTRAINT entity_pkey PRIMARY KEY (entity_id);


--
-- Name: entityright_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY entityright
    ADD CONSTRAINT entityright_pkey PRIMARY KEY (entityright_entity_id, entityright_consumer_id);


--
-- Name: event_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY event
    ADD CONSTRAINT event_pkey PRIMARY KEY (event_id);


--
-- Name: eventcategory1_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY eventcategory1
    ADD CONSTRAINT eventcategory1_pkey PRIMARY KEY (eventcategory1_id);


--
-- Name: evententity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY evententity
    ADD CONSTRAINT evententity_pkey PRIMARY KEY (evententity_entity_id, evententity_event_id);


--
-- Name: eventexception_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY eventexception
    ADD CONSTRAINT eventexception_pkey PRIMARY KEY (eventexception_event_id, eventexception_date);


--
-- Name: eventlink_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY eventlink
    ADD CONSTRAINT eventlink_pkey PRIMARY KEY (eventlink_event_id, eventlink_entity_id);


--
-- Name: groupentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY groupentity
    ADD CONSTRAINT groupentity_pkey PRIMARY KEY (groupentity_entity_id, groupentity_group_id);


--
-- Name: groupgroup_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY groupgroup
    ADD CONSTRAINT groupgroup_pkey PRIMARY KEY (groupgroup_parent_id, groupgroup_child_id);


--
-- Name: host_host_name_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY host
    ADD CONSTRAINT host_host_name_key UNIQUE (host_name);


--
-- Name: host_host_uid_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY host
    ADD CONSTRAINT host_host_uid_key UNIQUE (host_uid);


--
-- Name: host_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY host
    ADD CONSTRAINT host_pkey PRIMARY KEY (host_id);


--
-- Name: hostentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY hostentity
    ADD CONSTRAINT hostentity_pkey PRIMARY KEY (hostentity_entity_id, hostentity_host_id);


--
-- Name: hostservice_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY hostservice
    ADD CONSTRAINT hostservice_pkey PRIMARY KEY (hostservice_service_id, hostservice_host_id);


--
-- Name: hostservicevalue_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY hostservicevalue
    ADD CONSTRAINT hostservicevalue_pkey PRIMARY KEY (hostservicevalue_serviceproperty_id, hostservicevalue_host_id);


--
-- Name: im_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY im
    ADD CONSTRAINT im_pkey PRIMARY KEY (im_id);


--
-- Name: import_import_name_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY import
    ADD CONSTRAINT import_import_name_key UNIQUE (import_name);


--
-- Name: import_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY import
    ADD CONSTRAINT import_pkey PRIMARY KEY (import_id);


--
-- Name: importentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY importentity
    ADD CONSTRAINT importentity_pkey PRIMARY KEY (importentity_entity_id, importentity_import_id);


--
-- Name: incident_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_pkey PRIMARY KEY (incident_id);


--
-- Name: incidententity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY incidententity
    ADD CONSTRAINT incidententity_pkey PRIMARY KEY (incidententity_entity_id, incidententity_incident_id);


--
-- Name: incidentpriority_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY incidentpriority
    ADD CONSTRAINT incidentpriority_pkey PRIMARY KEY (incidentpriority_id);


--
-- Name: incidentresolutiontype_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY incidentresolutiontype
    ADD CONSTRAINT incidentresolutiontype_pkey PRIMARY KEY (incidentresolutiontype_id);


--
-- Name: incidentstatus_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY incidentstatus
    ADD CONSTRAINT incidentstatus_pkey PRIMARY KEY (incidentstatus_id);


--
-- Name: invoice_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY invoice
    ADD CONSTRAINT invoice_pkey PRIMARY KEY (invoice_id);


--
-- Name: invoiceentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY invoiceentity
    ADD CONSTRAINT invoiceentity_pkey PRIMARY KEY (invoiceentity_entity_id, invoiceentity_invoice_id);


--
-- Name: kind_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY kind
    ADD CONSTRAINT kind_pkey PRIMARY KEY (kind_id);


--
-- Name: lead_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY lead
    ADD CONSTRAINT lead_pkey PRIMARY KEY (lead_id);


--
-- Name: leadentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY leadentity
    ADD CONSTRAINT leadentity_pkey PRIMARY KEY (leadentity_entity_id, leadentity_lead_id);


--
-- Name: leadsource_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY leadsource
    ADD CONSTRAINT leadsource_pkey PRIMARY KEY (leadsource_id);


--
-- Name: leadstatus_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY leadstatus
    ADD CONSTRAINT leadstatus_pkey PRIMARY KEY (leadstatus_id);


--
-- Name: list_list_name_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY list
    ADD CONSTRAINT list_list_name_key UNIQUE (list_name);


--
-- Name: list_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY list
    ADD CONSTRAINT list_pkey PRIMARY KEY (list_id);


--
-- Name: listentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY listentity
    ADD CONSTRAINT listentity_pkey PRIMARY KEY (listentity_entity_id, listentity_list_id);


--
-- Name: mailboxentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY mailboxentity
    ADD CONSTRAINT mailboxentity_pkey PRIMARY KEY (mailboxentity_entity_id, mailboxentity_mailbox_id);


--
-- Name: mailserver_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY mailserver
    ADD CONSTRAINT mailserver_pkey PRIMARY KEY (mailserver_id);


--
-- Name: mailshare_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY mailshare
    ADD CONSTRAINT mailshare_pkey PRIMARY KEY (mailshare_id);


--
-- Name: mailshareentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY mailshareentity
    ADD CONSTRAINT mailshareentity_pkey PRIMARY KEY (mailshareentity_entity_id, mailshareentity_mailshare_id);


--
-- Name: obmbookmark_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY obmbookmark
    ADD CONSTRAINT obmbookmark_pkey PRIMARY KEY (obmbookmark_id);


--
-- Name: obmbookmarkentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY obmbookmarkentity
    ADD CONSTRAINT obmbookmarkentity_pkey PRIMARY KEY (obmbookmarkentity_entity_id, obmbookmarkentity_obmbookmark_id);


--
-- Name: obmbookmarkproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY obmbookmarkproperty
    ADD CONSTRAINT obmbookmarkproperty_pkey PRIMARY KEY (obmbookmarkproperty_id);


--
-- Name: obminfo_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY obminfo
    ADD CONSTRAINT obminfo_pkey PRIMARY KEY (obminfo_name);


--
-- Name: obmsession_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY obmsession
    ADD CONSTRAINT obmsession_pkey PRIMARY KEY (obmsession_sid, obmsession_name);


--
-- Name: of_usergroup_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY of_usergroup
    ADD CONSTRAINT of_usergroup_pkey PRIMARY KEY (of_usergroup_group_id, of_usergroup_user_id);


--
-- Name: ogroup_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY ogroup
    ADD CONSTRAINT ogroup_pkey PRIMARY KEY (ogroup_id);


--
-- Name: ogroupentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY ogroupentity
    ADD CONSTRAINT ogroupentity_pkey PRIMARY KEY (ogroupentity_entity_id, ogroupentity_ogroup_id);


--
-- Name: ogrouplink_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY ogrouplink
    ADD CONSTRAINT ogrouplink_pkey PRIMARY KEY (ogrouplink_id);


--
-- Name: organizationalchart_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY organizationalchart
    ADD CONSTRAINT organizationalchart_pkey PRIMARY KEY (organizationalchart_id);


--
-- Name: organizationalchartentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY organizationalchartentity
    ADD CONSTRAINT organizationalchartentity_pkey PRIMARY KEY (organizationalchartentity_entity_id, organizationalchartentity_organizationalchart_id);


--
-- Name: parentdeal_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY parentdeal
    ADD CONSTRAINT parentdeal_pkey PRIMARY KEY (parentdeal_id);


--
-- Name: parentdealentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY parentdealentity
    ADD CONSTRAINT parentdealentity_pkey PRIMARY KEY (parentdealentity_entity_id, parentdealentity_parentdeal_id);


--
-- Name: payment_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY payment
    ADD CONSTRAINT payment_pkey PRIMARY KEY (payment_id);


--
-- Name: paymententity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY paymententity
    ADD CONSTRAINT paymententity_pkey PRIMARY KEY (paymententity_entity_id, paymententity_payment_id);


--
-- Name: paymentinvoice_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY paymentinvoice
    ADD CONSTRAINT paymentinvoice_pkey PRIMARY KEY (paymentinvoice_invoice_id, paymentinvoice_payment_id);


--
-- Name: paymentkind_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY paymentkind
    ADD CONSTRAINT paymentkind_pkey PRIMARY KEY (paymentkind_id);


--
-- Name: phone_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY phone
    ADD CONSTRAINT phone_pkey PRIMARY KEY (phone_id);


--
-- Name: profile_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY profile
    ADD CONSTRAINT profile_pkey PRIMARY KEY (profile_id);


--
-- Name: profileentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY profileentity
    ADD CONSTRAINT profileentity_pkey PRIMARY KEY (profileentity_entity_id, profileentity_profile_id);


--
-- Name: profilemodule_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY profilemodule
    ADD CONSTRAINT profilemodule_pkey PRIMARY KEY (profilemodule_id);


--
-- Name: profileproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY profileproperty
    ADD CONSTRAINT profileproperty_pkey PRIMARY KEY (profileproperty_id);


--
-- Name: profilepropertyvalue_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY profilepropertyvalue
    ADD CONSTRAINT profilepropertyvalue_pkey PRIMARY KEY (profilepropertyvalue_id);


--
-- Name: profilesection_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY profilesection
    ADD CONSTRAINT profilesection_pkey PRIMARY KEY (profilesection_id);


--
-- Name: project_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_pkey PRIMARY KEY (project_id);


--
-- Name: projectclosing_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY projectclosing
    ADD CONSTRAINT projectclosing_pkey PRIMARY KEY (projectclosing_id);


--
-- Name: projectcv_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY projectcv
    ADD CONSTRAINT projectcv_pkey PRIMARY KEY (projectcv_project_id, projectcv_cv_id);


--
-- Name: projectentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY projectentity
    ADD CONSTRAINT projectentity_pkey PRIMARY KEY (projectentity_entity_id, projectentity_project_id);


--
-- Name: projectreftask_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY projectreftask
    ADD CONSTRAINT projectreftask_pkey PRIMARY KEY (projectreftask_id);


--
-- Name: projecttask_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY projecttask
    ADD CONSTRAINT projecttask_pkey PRIMARY KEY (projecttask_id);


--
-- Name: projectuser_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY projectuser
    ADD CONSTRAINT projectuser_pkey PRIMARY KEY (projectuser_id);


--
-- Name: publication_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY publication
    ADD CONSTRAINT publication_pkey PRIMARY KEY (publication_id);


--
-- Name: publicationentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY publicationentity
    ADD CONSTRAINT publicationentity_pkey PRIMARY KEY (publicationentity_entity_id, publicationentity_publication_id);


--
-- Name: publicationtype_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY publicationtype
    ADD CONSTRAINT publicationtype_pkey PRIMARY KEY (publicationtype_id);


--
-- Name: region_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY region
    ADD CONSTRAINT region_pkey PRIMARY KEY (region_id);


--
-- Name: resource_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY resource
    ADD CONSTRAINT resource_pkey PRIMARY KEY (resource_id);


--
-- Name: resource_resource_name_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY resource
    ADD CONSTRAINT resource_resource_name_key UNIQUE (resource_name);


--
-- Name: resourceentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY resourceentity
    ADD CONSTRAINT resourceentity_pkey PRIMARY KEY (resourceentity_entity_id, resourceentity_resource_id);


--
-- Name: resourcegroupentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY resourcegroupentity
    ADD CONSTRAINT resourcegroupentity_pkey PRIMARY KEY (resourcegroupentity_entity_id, resourcegroupentity_resourcegroup_id);


--
-- Name: resourceitem_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY resourceitem
    ADD CONSTRAINT resourceitem_pkey PRIMARY KEY (resourceitem_id);


--
-- Name: resourcetype_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY resourcetype
    ADD CONSTRAINT resourcetype_pkey PRIMARY KEY (resourcetype_id);


--
-- Name: rgroup_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY rgroup
    ADD CONSTRAINT rgroup_pkey PRIMARY KEY (rgroup_id);


--
-- Name: service_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY service
    ADD CONSTRAINT service_pkey PRIMARY KEY (service_id);


--
-- Name: service_service_key_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY service
    ADD CONSTRAINT service_service_key_key UNIQUE (service_key);


--
-- Name: servicedomain_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY servicedomain
    ADD CONSTRAINT servicedomain_pkey PRIMARY KEY (servicedomain_id);


--
-- Name: servicehost_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY servicehost
    ADD CONSTRAINT servicehost_pkey PRIMARY KEY (servicehost_id);


--
-- Name: serviceproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY serviceproperty
    ADD CONSTRAINT serviceproperty_pkey PRIMARY KEY (serviceproperty_id);


--
-- Name: serviceproperty_serviceproperty_key_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY serviceproperty
    ADD CONSTRAINT serviceproperty_serviceproperty_key_key UNIQUE (serviceproperty_key);


--
-- Name: servicepropertydomain_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY servicepropertydomain
    ADD CONSTRAINT servicepropertydomain_pkey PRIMARY KEY (servicepropertydomain_id);


--
-- Name: servicepropertyhost_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY servicepropertyhost
    ADD CONSTRAINT servicepropertyhost_pkey PRIMARY KEY (servicepropertyhost_id);


--
-- Name: stats_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY stats
    ADD CONSTRAINT stats_pkey PRIMARY KEY (stats_name);


--
-- Name: subscription_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY subscription
    ADD CONSTRAINT subscription_pkey PRIMARY KEY (subscription_id);


--
-- Name: subscriptionentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY subscriptionentity
    ADD CONSTRAINT subscriptionentity_pkey PRIMARY KEY (subscriptionentity_entity_id, subscriptionentity_subscription_id);


--
-- Name: subscriptionreception_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY subscriptionreception
    ADD CONSTRAINT subscriptionreception_pkey PRIMARY KEY (subscriptionreception_id);


--
-- Name: tasktype_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY tasktype
    ADD CONSTRAINT tasktype_pkey PRIMARY KEY (tasktype_id);


--
-- Name: timetask_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY timetask
    ADD CONSTRAINT timetask_pkey PRIMARY KEY (timetask_id);


--
-- Name: todo_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY todo
    ADD CONSTRAINT todo_pkey PRIMARY KEY (todo_id);


--
-- Name: ugroup_group_gid_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY ugroup
    ADD CONSTRAINT ugroup_group_gid_key UNIQUE (group_gid);


--
-- Name: ugroup_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY ugroup
    ADD CONSTRAINT ugroup_pkey PRIMARY KEY (group_id);


--
-- Name: updated_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY updated
    ADD CONSTRAINT updated_pkey PRIMARY KEY (updated_id);


--
-- Name: updatedlinks_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY updatedlinks
    ADD CONSTRAINT updatedlinks_pkey PRIMARY KEY (updatedlinks_id);


--
-- Name: userentity_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY userentity
    ADD CONSTRAINT userentity_pkey PRIMARY KEY (userentity_entity_id, userentity_user_id);


--
-- Name: userobm_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY userobm
    ADD CONSTRAINT userobm_pkey PRIMARY KEY (userobm_id);


--
-- Name: userobm_sessionlog_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY userobm_sessionlog
    ADD CONSTRAINT userobm_sessionlog_pkey PRIMARY KEY (userobm_sessionlog_sid);


--
-- Name: userobmgroup_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY userobmgroup
    ADD CONSTRAINT userobmgroup_pkey PRIMARY KEY (userobmgroup_group_id, userobmgroup_userobm_id);


--
-- Name: userobmpref_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY userobmpref
    ADD CONSTRAINT userobmpref_key UNIQUE (userobmpref_user_id, userobmpref_option);


--
-- Name: userobmpref_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY userobmpref
    ADD CONSTRAINT userobmpref_pkey PRIMARY KEY (userobmpref_id);


--
-- Name: usersystem_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY usersystem
    ADD CONSTRAINT usersystem_pkey PRIMARY KEY (usersystem_id);


--
-- Name: usersystem_usersystem_login_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY usersystem
    ADD CONSTRAINT usersystem_usersystem_login_key UNIQUE (usersystem_login);


--
-- Name: website_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY website
    ADD CONSTRAINT website_pkey PRIMARY KEY (website_id);


--
-- Name: bkm_idx_user; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX bkm_idx_user ON obmbookmark USING btree (obmbookmark_user_id);


--
-- Name: bkmprop_idx_bkm; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX bkmprop_idx_bkm ON obmbookmarkproperty USING btree (obmbookmarkproperty_bookmark_id);


--
-- Name: cat_idx_cat; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX cat_idx_cat ON category USING btree (category_category);


--
-- Name: catl_idx_cat; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX catl_idx_cat ON categorylink USING btree (categorylink_category);


--
-- Name: catl_idx_entid; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX catl_idx_entid ON categorylink USING btree (categorylink_entity_id);


--
-- Name: dealcompany_idx_deal; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dealcompany_idx_deal ON dealcompany USING btree (dealcompany_deal_id);


--
-- Name: displaypref_entity_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX displaypref_entity_index ON displaypref USING btree (display_entity);


--
-- Name: displaypref_user_id_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX displaypref_user_id_index ON displaypref USING btree (display_user_id);


--
-- Name: idx_dce_event_id; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_dce_event_id ON deletedevent USING btree (deletedevent_event_id);


--
-- Name: idx_dce_user_id; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_dce_user_id ON deletedevent USING btree (deletedevent_user_id);


--
-- Name: idx_eventalert_user; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_eventalert_user ON eventalert USING btree (eventalert_user_id);


--
-- Name: k_login_user_userobm_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX k_login_user_userobm_index ON userobm USING btree (userobm_login);


--
-- Name: k_name_resource_resource_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX k_name_resource_resource_index ON resource USING btree (resource_name);


--
-- Name: k_uid_user_userobm_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX k_uid_user_userobm_index ON userobm USING btree (userobm_uid);


--
-- Name: project_idx_comp; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX project_idx_comp ON project USING btree (project_company_id);


--
-- Name: project_idx_deal; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX project_idx_deal ON project USING btree (project_deal_id);


--
-- Name: pt_idx_pro; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX pt_idx_pro ON projecttask USING btree (projecttask_project_id);


--
-- Name: pu_idx_pro; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX pu_idx_pro ON projectuser USING btree (projectuser_project_id);


--
-- Name: pu_idx_pt; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX pu_idx_pt ON projectuser USING btree (projectuser_projecttask_id);


--
-- Name: pu_idx_user; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX pu_idx_user ON projectuser USING btree (projectuser_user_id);


--
-- Name: tt_idx_pt; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX tt_idx_pt ON timetask USING btree (timetask_projecttask_id);

--
-- Name: account_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY account
    ADD CONSTRAINT account_domain_id_domain_id_fkey FOREIGN KEY (account_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: account_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY account
    ADD CONSTRAINT account_usercreate_userobm_id_fkey FOREIGN KEY (account_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: account_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY account
    ADD CONSTRAINT account_userupdate_userobm_id_fkey FOREIGN KEY (account_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: accountentity_account_id_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY accountentity
    ADD CONSTRAINT accountentity_account_id_account_id_fkey FOREIGN KEY (accountentity_account_id) REFERENCES account(account_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: accountentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY accountentity
    ADD CONSTRAINT accountentity_entity_id_entity_id_fkey FOREIGN KEY (accountentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: activeuserobm_userobm_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY activeuserobm
    ADD CONSTRAINT activeuserobm_userobm_id_userobm_id_fkey FOREIGN KEY (activeuserobm_userobm_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: address_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY address
    ADD CONSTRAINT address_entity_id_entity_id_fkey FOREIGN KEY (address_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: calendarentity_calendar_id_calendar_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY calendarentity
    ADD CONSTRAINT calendarentity_calendar_id_calendar_id_fkey FOREIGN KEY (calendarentity_calendar_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: calendarentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY calendarentity
    ADD CONSTRAINT calendarentity_entity_id_entity_id_fkey FOREIGN KEY (calendarentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: category_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY category
    ADD CONSTRAINT category_domain_id_domain_id_fkey FOREIGN KEY (category_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: category_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY category
    ADD CONSTRAINT category_usercreate_userobm_id_fkey FOREIGN KEY (category_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: category_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY category
    ADD CONSTRAINT category_userupdate_userobm_id_fkey FOREIGN KEY (category_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: categorylink_category_id_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY categorylink
    ADD CONSTRAINT categorylink_category_id_category_id_fkey FOREIGN KEY (categorylink_category_id) REFERENCES category(category_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: categorylink_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY categorylink
    ADD CONSTRAINT categorylink_entity_id_entity_id_fkey FOREIGN KEY (categorylink_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: company_activity_id_companyactivity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY company
    ADD CONSTRAINT company_activity_id_companyactivity_id_fkey FOREIGN KEY (company_activity_id) REFERENCES companyactivity(companyactivity_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: company_datasource_id_datasource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY company
    ADD CONSTRAINT company_datasource_id_datasource_id_fkey FOREIGN KEY (company_datasource_id) REFERENCES datasource(datasource_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: company_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY company
    ADD CONSTRAINT company_domain_id_domain_id_fkey FOREIGN KEY (company_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: company_marketingmanager_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY company
    ADD CONSTRAINT company_marketingmanager_id_userobm_id_fkey FOREIGN KEY (company_marketingmanager_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: company_nafcode_id_companynafcode_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY company
    ADD CONSTRAINT company_nafcode_id_companynafcode_id_fkey FOREIGN KEY (company_nafcode_id) REFERENCES companynafcode(companynafcode_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: company_type_id_companytype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY company
    ADD CONSTRAINT company_type_id_companytype_id_fkey FOREIGN KEY (company_type_id) REFERENCES companytype(companytype_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: company_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY company
    ADD CONSTRAINT company_usercreate_userobm_id_fkey FOREIGN KEY (company_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: company_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY company
    ADD CONSTRAINT company_userupdate_userobm_id_fkey FOREIGN KEY (company_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: companyactivity_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY companyactivity
    ADD CONSTRAINT companyactivity_domain_id_domain_id_fkey FOREIGN KEY (companyactivity_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: companyactivity_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY companyactivity
    ADD CONSTRAINT companyactivity_usercreate_userobm_id_fkey FOREIGN KEY (companyactivity_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: companyactivity_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY companyactivity
    ADD CONSTRAINT companyactivity_userupdate_userobm_id_fkey FOREIGN KEY (companyactivity_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: companyentity_company_id_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY companyentity
    ADD CONSTRAINT companyentity_company_id_company_id_fkey FOREIGN KEY (companyentity_company_id) REFERENCES company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: companyentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY companyentity
    ADD CONSTRAINT companyentity_entity_id_entity_id_fkey FOREIGN KEY (companyentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: companynafcode_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY companynafcode
    ADD CONSTRAINT companynafcode_domain_id_domain_id_fkey FOREIGN KEY (companynafcode_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: companynafcode_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY companynafcode
    ADD CONSTRAINT companynafcode_usercreate_userobm_id_fkey FOREIGN KEY (companynafcode_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: companynafcode_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY companynafcode
    ADD CONSTRAINT companynafcode_userupdate_userobm_id_fkey FOREIGN KEY (companynafcode_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: companytype_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY companytype
    ADD CONSTRAINT companytype_domain_id_domain_id_fkey FOREIGN KEY (companytype_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: companytype_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY companytype
    ADD CONSTRAINT companytype_usercreate_userobm_id_fkey FOREIGN KEY (companytype_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: companytype_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY companytype
    ADD CONSTRAINT companytype_userupdate_userobm_id_fkey FOREIGN KEY (companytype_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contact_birthday_id_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_birthday_id_event_id_fkey FOREIGN KEY (contact_birthday_id) REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contact_company_id_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_company_id_company_id_fkey FOREIGN KEY (contact_company_id) REFERENCES company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contact_datasource_id_datasource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_datasource_id_datasource_id_fkey FOREIGN KEY (contact_datasource_id) REFERENCES datasource(datasource_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contact_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_domain_id_domain_id_fkey FOREIGN KEY (contact_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contact_function_id_contactfunction_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_function_id_contactfunction_id_fkey FOREIGN KEY (contact_function_id) REFERENCES contactfunction(contactfunction_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contact_kind_id_kind_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_kind_id_kind_id_fkey FOREIGN KEY (contact_kind_id) REFERENCES kind(kind_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contact_marketingmanager_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_marketingmanager_id_userobm_id_fkey FOREIGN KEY (contact_marketingmanager_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contact_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_usercreate_userobm_id_fkey FOREIGN KEY (contact_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contact_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_userupdate_userobm_id_fkey FOREIGN KEY (contact_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contactentity_contact_id_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contactentity
    ADD CONSTRAINT contactentity_contact_id_contact_id_fkey FOREIGN KEY (contactentity_contact_id) REFERENCES contact(contact_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contactentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contactentity
    ADD CONSTRAINT contactentity_entity_id_entity_id_fkey FOREIGN KEY (contactentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contactfunction_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contactfunction
    ADD CONSTRAINT contactfunction_domain_id_domain_id_fkey FOREIGN KEY (contactfunction_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contactfunction_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contactfunction
    ADD CONSTRAINT contactfunction_usercreate_userobm_id_fkey FOREIGN KEY (contactfunction_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contactfunction_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contactfunction
    ADD CONSTRAINT contactfunction_userupdate_userobm_id_fkey FOREIGN KEY (contactfunction_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contactlist_contact_id_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contactlist
    ADD CONSTRAINT contactlist_contact_id_contact_id_fkey FOREIGN KEY (contactlist_contact_id) REFERENCES contact(contact_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contactlist_list_id_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contactlist
    ADD CONSTRAINT contactlist_list_id_list_id_fkey FOREIGN KEY (contactlist_list_id) REFERENCES list(list_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contract_company_id_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_company_id_company_id_fkey FOREIGN KEY (contract_company_id) REFERENCES company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contract_contact1_id_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_contact1_id_contact_id_fkey FOREIGN KEY (contract_contact1_id) REFERENCES contact(contact_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contract_contact2_id_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_contact2_id_contact_id_fkey FOREIGN KEY (contract_contact2_id) REFERENCES contact(contact_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contract_deal_id_deal_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_deal_id_deal_id_fkey FOREIGN KEY (contract_deal_id) REFERENCES deal(deal_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contract_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_domain_id_domain_id_fkey FOREIGN KEY (contract_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contract_marketmanager_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_marketmanager_id_userobm_id_fkey FOREIGN KEY (contract_marketmanager_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contract_priority_id_contractpriority_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_priority_id_contractpriority_id_fkey FOREIGN KEY (contract_priority_id) REFERENCES contractpriority(contractpriority_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contract_status_id_contractstatus_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_status_id_contractstatus_id_fkey FOREIGN KEY (contract_status_id) REFERENCES contractstatus(contractstatus_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contract_techmanager_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_techmanager_id_userobm_id_fkey FOREIGN KEY (contract_techmanager_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contract_type_id_contracttype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_type_id_contracttype_id_fkey FOREIGN KEY (contract_type_id) REFERENCES contracttype(contracttype_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contract_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_usercreate_userobm_id_fkey FOREIGN KEY (contract_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contract_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_userupdate_userobm_id_fkey FOREIGN KEY (contract_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contractentity_contract_id_contract_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contractentity
    ADD CONSTRAINT contractentity_contract_id_contract_id_fkey FOREIGN KEY (contractentity_contract_id) REFERENCES contract(contract_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contractentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contractentity
    ADD CONSTRAINT contractentity_entity_id_entity_id_fkey FOREIGN KEY (contractentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contractpriority_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contractpriority
    ADD CONSTRAINT contractpriority_domain_id_domain_id_fkey FOREIGN KEY (contractpriority_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contractpriority_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contractpriority
    ADD CONSTRAINT contractpriority_usercreate_userobm_id_fkey FOREIGN KEY (contractpriority_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contractpriority_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contractpriority
    ADD CONSTRAINT contractpriority_userupdate_userobm_id_fkey FOREIGN KEY (contractpriority_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contractstatus_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contractstatus
    ADD CONSTRAINT contractstatus_domain_id_domain_id_fkey FOREIGN KEY (contractstatus_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contractstatus_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contractstatus
    ADD CONSTRAINT contractstatus_usercreate_userobm_id_fkey FOREIGN KEY (contractstatus_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contractstatus_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contractstatus
    ADD CONSTRAINT contractstatus_userupdate_userobm_id_fkey FOREIGN KEY (contractstatus_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contracttype_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contracttype
    ADD CONSTRAINT contracttype_domain_id_domain_id_fkey FOREIGN KEY (contracttype_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contracttype_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contracttype
    ADD CONSTRAINT contracttype_usercreate_userobm_id_fkey FOREIGN KEY (contracttype_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contracttype_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contracttype
    ADD CONSTRAINT contracttype_userupdate_userobm_id_fkey FOREIGN KEY (contracttype_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: country_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY country
    ADD CONSTRAINT country_domain_id_domain_id_fkey FOREIGN KEY (country_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: country_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY country
    ADD CONSTRAINT country_usercreate_userobm_id_fkey FOREIGN KEY (country_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: country_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY country
    ADD CONSTRAINT country_userupdate_userobm_id_fkey FOREIGN KEY (country_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: cv_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY cv
    ADD CONSTRAINT cv_domain_id_domain_id_fkey FOREIGN KEY (cv_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: cv_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY cv
    ADD CONSTRAINT cv_usercreate_userobm_id_fkey FOREIGN KEY (cv_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: cv_userobm_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY cv
    ADD CONSTRAINT cv_userobm_id_userobm_id_fkey FOREIGN KEY (cv_userobm_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: cv_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY cv
    ADD CONSTRAINT cv_userupdate_userobm_id_fkey FOREIGN KEY (cv_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: cventity_cv_id_cv_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY cventity
    ADD CONSTRAINT cventity_cv_id_cv_id_fkey FOREIGN KEY (cventity_cv_id) REFERENCES cv(cv_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: cventity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY cventity
    ADD CONSTRAINT cventity_entity_id_entity_id_fkey FOREIGN KEY (cventity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: datasource_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY datasource
    ADD CONSTRAINT datasource_domain_id_domain_id_fkey FOREIGN KEY (datasource_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: datasource_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY datasource
    ADD CONSTRAINT datasource_usercreate_userobm_id_fkey FOREIGN KEY (datasource_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: datasource_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY datasource
    ADD CONSTRAINT datasource_userupdate_userobm_id_fkey FOREIGN KEY (datasource_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: deal_company_id_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_company_id_company_id_fkey FOREIGN KEY (deal_company_id) REFERENCES company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: deal_contact1_id_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_contact1_id_contact_id_fkey FOREIGN KEY (deal_contact1_id) REFERENCES contact(contact_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: deal_contact2_id_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_contact2_id_contact_id_fkey FOREIGN KEY (deal_contact2_id) REFERENCES contact(contact_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: deal_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_domain_id_domain_id_fkey FOREIGN KEY (deal_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: deal_marketingmanager_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_marketingmanager_id_userobm_id_fkey FOREIGN KEY (deal_marketingmanager_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: deal_parentdeal_id_parentdeal_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_parentdeal_id_parentdeal_id_fkey FOREIGN KEY (deal_parentdeal_id) REFERENCES parentdeal(parentdeal_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: deal_region_id_region_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_region_id_region_id_fkey FOREIGN KEY (deal_region_id) REFERENCES region(region_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: deal_source_id_leadsource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_source_id_leadsource_id_fkey FOREIGN KEY (deal_source_id) REFERENCES leadsource(leadsource_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: deal_tasktype_id_tasktype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_tasktype_id_tasktype_id_fkey FOREIGN KEY (deal_tasktype_id) REFERENCES tasktype(tasktype_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: deal_technicalmanager_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_technicalmanager_id_userobm_id_fkey FOREIGN KEY (deal_technicalmanager_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: deal_type_id_dealtype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_type_id_dealtype_id_fkey FOREIGN KEY (deal_type_id) REFERENCES dealtype(dealtype_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: deal_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_usercreate_userobm_id_fkey FOREIGN KEY (deal_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: deal_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_userupdate_userobm_id_fkey FOREIGN KEY (deal_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: dealcompany_company_id_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dealcompany
    ADD CONSTRAINT dealcompany_company_id_company_id_fkey FOREIGN KEY (dealcompany_company_id) REFERENCES company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: dealcompany_deal_id_deal_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dealcompany
    ADD CONSTRAINT dealcompany_deal_id_deal_id_fkey FOREIGN KEY (dealcompany_deal_id) REFERENCES deal(deal_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: dealcompany_role_id_dealcompanyrole_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dealcompany
    ADD CONSTRAINT dealcompany_role_id_dealcompanyrole_id_fkey FOREIGN KEY (dealcompany_role_id) REFERENCES dealcompanyrole(dealcompanyrole_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: dealcompany_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dealcompany
    ADD CONSTRAINT dealcompany_usercreate_userobm_id_fkey FOREIGN KEY (dealcompany_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: dealcompany_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dealcompany
    ADD CONSTRAINT dealcompany_userupdate_userobm_id_fkey FOREIGN KEY (dealcompany_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: dealcompanyrole_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dealcompanyrole
    ADD CONSTRAINT dealcompanyrole_domain_id_domain_id_fkey FOREIGN KEY (dealcompanyrole_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: dealcompanyrole_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dealcompanyrole
    ADD CONSTRAINT dealcompanyrole_usercreate_userobm_id_fkey FOREIGN KEY (dealcompanyrole_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: dealcompanyrole_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dealcompanyrole
    ADD CONSTRAINT dealcompanyrole_userupdate_userobm_id_fkey FOREIGN KEY (dealcompanyrole_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: dealentity_deal_id_deal_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dealentity
    ADD CONSTRAINT dealentity_deal_id_deal_id_fkey FOREIGN KEY (dealentity_deal_id) REFERENCES deal(deal_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: dealentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dealentity
    ADD CONSTRAINT dealentity_entity_id_entity_id_fkey FOREIGN KEY (dealentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: dealstatus_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dealstatus
    ADD CONSTRAINT dealstatus_domain_id_domain_id_fkey FOREIGN KEY (dealstatus_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: dealstatus_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dealstatus
    ADD CONSTRAINT dealstatus_usercreate_userobm_id_fkey FOREIGN KEY (dealstatus_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: dealstatus_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dealstatus
    ADD CONSTRAINT dealstatus_userupdate_userobm_id_fkey FOREIGN KEY (dealstatus_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: dealtype_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dealtype
    ADD CONSTRAINT dealtype_domain_id_domain_id_fkey FOREIGN KEY (dealtype_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: dealtype_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dealtype
    ADD CONSTRAINT dealtype_usercreate_userobm_id_fkey FOREIGN KEY (dealtype_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: dealtype_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dealtype
    ADD CONSTRAINT dealtype_userupdate_userobm_id_fkey FOREIGN KEY (dealtype_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: defaultodttemplate_document_id_document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -;
--

ALTER TABLE ONLY defaultodttemplate
    ADD CONSTRAINT defaultodttemplate_document_id_document_id_fkey FOREIGN KEY (defaultodttemplate_document_id) REFERENCES document(document_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: defaultodttemplate_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY defaultodttemplate
    ADD CONSTRAINT defaultodttemplate_domain_id_domain_id_fkey FOREIGN KEY (defaultodttemplate_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: defaultodttemplateentity_defaultodttemplate_id_defaultodttempla; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY defaultodttemplateentity
    ADD CONSTRAINT defaultodttemplateentity_defaultodttemplate_id_defaultodttempla FOREIGN KEY (defaultodttemplateentity_defaultodttemplate_id) REFERENCES defaultodttemplate(defaultodttemplate_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: defaultodttemplateentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY defaultodttemplateentity
    ADD CONSTRAINT defaultodttemplateentity_entity_id_entity_id_fkey FOREIGN KEY (defaultodttemplateentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: deleted_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY deleted
    ADD CONSTRAINT deleted_domain_id_domain_id_fkey FOREIGN KEY (deleted_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: deleted_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY deleted
    ADD CONSTRAINT deleted_user_id_userobm_id_fkey FOREIGN KEY (deleted_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: display_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY displaypref
    ADD CONSTRAINT display_user_id_userobm_id_fkey FOREIGN KEY (display_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: document_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY document
    ADD CONSTRAINT document_domain_id_domain_id_fkey FOREIGN KEY (document_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: document_mimetype_id_documentmimetype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY document
    ADD CONSTRAINT document_mimetype_id_documentmimetype_id_fkey FOREIGN KEY (document_mimetype_id) REFERENCES documentmimetype(documentmimetype_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: document_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY document
    ADD CONSTRAINT document_usercreate_userobm_id_fkey FOREIGN KEY (document_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: document_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY document
    ADD CONSTRAINT document_userupdate_userobm_id_fkey FOREIGN KEY (document_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: documententity_document_id_document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY documententity
    ADD CONSTRAINT documententity_document_id_document_id_fkey FOREIGN KEY (documententity_document_id) REFERENCES document(document_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: documententity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY documententity
    ADD CONSTRAINT documententity_entity_id_entity_id_fkey FOREIGN KEY (documententity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: documentlink_document_id_document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY documentlink
    ADD CONSTRAINT documentlink_document_id_document_id_fkey FOREIGN KEY (documentlink_document_id) REFERENCES document(document_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: documentlink_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY documentlink
    ADD CONSTRAINT documentlink_entity_id_entity_id_fkey FOREIGN KEY (documentlink_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: documentmimetype_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY documentmimetype
    ADD CONSTRAINT documentmimetype_domain_id_domain_id_fkey FOREIGN KEY (documentmimetype_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: documentmimetype_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY documentmimetype
    ADD CONSTRAINT documentmimetype_usercreate_userobm_id_fkey FOREIGN KEY (documentmimetype_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: documentmimetype_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY documentmimetype
    ADD CONSTRAINT documentmimetype_userupdate_userobm_id_fkey FOREIGN KEY (documentmimetype_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: domain_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY domain
    ADD CONSTRAINT domain_usercreate_userobm_id_fkey FOREIGN KEY (domain_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: domain_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY domain
    ADD CONSTRAINT domain_userupdate_userobm_id_fkey FOREIGN KEY (domain_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: domainentity_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY domainentity
    ADD CONSTRAINT domainentity_domain_id_domain_id_fkey FOREIGN KEY (domainentity_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: domainentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY domainentity
    ADD CONSTRAINT domainentity_entity_id_entity_id_fkey FOREIGN KEY (domainentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: domainmailserver_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY domainmailserver
    ADD CONSTRAINT domainmailserver_domain_id_domain_id_fkey FOREIGN KEY (domainmailserver_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: domainmailserver_mailserver_id_mailserver_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY domainmailserver
    ADD CONSTRAINT domainmailserver_mailserver_id_mailserver_id_fkey FOREIGN KEY (domainmailserver_mailserver_id) REFERENCES mailserver(mailserver_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: domainpropertyvalue_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY domainpropertyvalue
    ADD CONSTRAINT domainpropertyvalue_domain_id_domain_id_fkey FOREIGN KEY (domainpropertyvalue_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: domainservice_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY domainservice
    ADD CONSTRAINT domainservice_domain_id_domain_id_fkey FOREIGN KEY (domainservice_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: domainservice_service_id_domainservice_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY domainservice
    ADD CONSTRAINT domainservice_service_id_domainservice_domain_id_fkey FOREIGN KEY (domainservice_service_id) REFERENCES servicedomain(servicedomain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: domainservicevalue_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY domainservicevalue
    ADD CONSTRAINT domainservicevalue_domain_id_domain_id_fkey FOREIGN KEY (domainservicevalue_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: domainservicevalue_serviceproperty_id_serviceproperty_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY domainservicevalue
    ADD CONSTRAINT domainservicevalue_serviceproperty_id_serviceproperty_id_fkey FOREIGN KEY (domainservicevalue_serviceproperty_id) REFERENCES servicepropertydomain(servicepropertydomain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: email_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY email
    ADD CONSTRAINT email_entity_id_entity_id_fkey FOREIGN KEY (email_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: entityright_consumer_id_entity_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY entityright
    ADD CONSTRAINT entityright_consumer_id_entity_id FOREIGN KEY (entityright_consumer_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: entityright_entity_id_entity_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY entityright
    ADD CONSTRAINT entityright_entity_id_entity_id FOREIGN KEY (entityright_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: event_category1_id_eventcategory1_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY event
    ADD CONSTRAINT event_category1_id_eventcategory1_id_fkey FOREIGN KEY (event_category1_id) REFERENCES eventcategory1(eventcategory1_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: event_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY event
    ADD CONSTRAINT event_domain_id_domain_id_fkey FOREIGN KEY (event_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: event_owner_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY event
    ADD CONSTRAINT event_owner_userobm_id_fkey FOREIGN KEY (event_owner) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: event_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY event
    ADD CONSTRAINT event_usercreate_userobm_id_fkey FOREIGN KEY (event_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: event_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY event
    ADD CONSTRAINT event_userupdate_userobm_id_fkey FOREIGN KEY (event_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: eventalert_event_id_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY eventalert
    ADD CONSTRAINT eventalert_event_id_event_id_fkey FOREIGN KEY (eventalert_event_id) REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: eventalert_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY eventalert
    ADD CONSTRAINT eventalert_user_id_userobm_id_fkey FOREIGN KEY (eventalert_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: eventalert_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY eventalert
    ADD CONSTRAINT eventalert_usercreate_userobm_id_fkey FOREIGN KEY (eventalert_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: eventalert_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY eventalert
    ADD CONSTRAINT eventalert_userupdate_userobm_id_fkey FOREIGN KEY (eventalert_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: eventcategory1_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY eventcategory1
    ADD CONSTRAINT eventcategory1_domain_id_domain_id_fkey FOREIGN KEY (eventcategory1_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: eventcategory1_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY eventcategory1
    ADD CONSTRAINT eventcategory1_usercreate_userobm_id_fkey FOREIGN KEY (eventcategory1_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: eventcategory1_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY eventcategory1
    ADD CONSTRAINT eventcategory1_userupdate_userobm_id_fkey FOREIGN KEY (eventcategory1_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: evententity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY evententity
    ADD CONSTRAINT evententity_entity_id_entity_id_fkey FOREIGN KEY (evententity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: evententity_event_id_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY evententity
    ADD CONSTRAINT evententity_event_id_event_id_fkey FOREIGN KEY (evententity_event_id) REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: eventexception_event_id_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY eventexception
    ADD CONSTRAINT eventexception_event_id_event_id_fkey FOREIGN KEY (eventexception_event_id) REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: eventexception_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY eventexception
    ADD CONSTRAINT eventexception_usercreate_userobm_id_fkey FOREIGN KEY (eventexception_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: eventexception_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY eventexception
    ADD CONSTRAINT eventexception_userupdate_userobm_id_fkey FOREIGN KEY (eventexception_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: eventlink_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY eventlink
    ADD CONSTRAINT eventlink_entity_id_entity_id_fkey FOREIGN KEY (eventlink_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: eventlink_event_id_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY eventlink
    ADD CONSTRAINT eventlink_event_id_event_id_fkey FOREIGN KEY (eventlink_event_id) REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: eventlink_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY eventlink
    ADD CONSTRAINT eventlink_usercreate_userobm_id_fkey FOREIGN KEY (eventlink_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: eventlink_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY eventlink
    ADD CONSTRAINT eventlink_userupdate_userobm_id_fkey FOREIGN KEY (eventlink_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: group_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY ugroup
    ADD CONSTRAINT group_domain_id_domain_id_fkey FOREIGN KEY (group_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: group_manager_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY ugroup
    ADD CONSTRAINT group_manager_id_userobm_id_fkey FOREIGN KEY (group_manager_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: group_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY ugroup
    ADD CONSTRAINT group_usercreate_userobm_id_fkey FOREIGN KEY (group_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: group_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY ugroup
    ADD CONSTRAINT group_userupdate_userobm_id_fkey FOREIGN KEY (group_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: groupentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY groupentity
    ADD CONSTRAINT groupentity_entity_id_entity_id_fkey FOREIGN KEY (groupentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: groupentity_group_id_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY groupentity
    ADD CONSTRAINT groupentity_group_id_group_id_fkey FOREIGN KEY (groupentity_group_id) REFERENCES ugroup(group_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: groupgroup_child_id_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY groupgroup
    ADD CONSTRAINT groupgroup_child_id_group_id_fkey FOREIGN KEY (groupgroup_child_id) REFERENCES ugroup(group_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: groupgroup_parent_id_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY groupgroup
    ADD CONSTRAINT groupgroup_parent_id_group_id_fkey FOREIGN KEY (groupgroup_parent_id) REFERENCES ugroup(group_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: host_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY host
    ADD CONSTRAINT host_domain_id_domain_id_fkey FOREIGN KEY (host_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: host_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY host
    ADD CONSTRAINT host_usercreate_userobm_id_fkey FOREIGN KEY (host_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: host_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY host
    ADD CONSTRAINT host_userupdate_userobm_id_fkey FOREIGN KEY (host_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: hostentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY hostentity
    ADD CONSTRAINT hostentity_entity_id_entity_id_fkey FOREIGN KEY (hostentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: hostentity_host_id_host_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY hostentity
    ADD CONSTRAINT hostentity_host_id_host_id_fkey FOREIGN KEY (hostentity_host_id) REFERENCES host(host_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: hostservice_host_id_host_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY hostservice
    ADD CONSTRAINT hostservice_host_id_host_id_fkey FOREIGN KEY (hostservice_host_id) REFERENCES host(host_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: hostservice_service_id_hostservice_host_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY hostservice
    ADD CONSTRAINT hostservice_service_id_hostservice_host_id_fkey FOREIGN KEY (hostservice_service_id) REFERENCES servicehost(servicehost_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: hostservicevalue_host_id_host_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY hostservicevalue
    ADD CONSTRAINT hostservicevalue_host_id_host_id_fkey FOREIGN KEY (hostservicevalue_host_id) REFERENCES host(host_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: hostservicevalue_serviceproperty_id_serviceproperty_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY hostservicevalue
    ADD CONSTRAINT hostservicevalue_serviceproperty_id_serviceproperty_id_fkey FOREIGN KEY (hostservicevalue_serviceproperty_id) REFERENCES servicepropertyhost(servicepropertyhost_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: im_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY im
    ADD CONSTRAINT im_entity_id_entity_id_fkey FOREIGN KEY (im_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: import_datasource_id_datasource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY import
    ADD CONSTRAINT import_datasource_id_datasource_id_fkey FOREIGN KEY (import_datasource_id) REFERENCES datasource(datasource_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: import_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY import
    ADD CONSTRAINT import_domain_id_domain_id_fkey FOREIGN KEY (import_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: import_marketingmanager_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY import
    ADD CONSTRAINT import_marketingmanager_id_userobm_id_fkey FOREIGN KEY (import_marketingmanager_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: import_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY import
    ADD CONSTRAINT import_usercreate_userobm_id_fkey FOREIGN KEY (import_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: import_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY import
    ADD CONSTRAINT import_userupdate_userobm_id_fkey FOREIGN KEY (import_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: importentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY importentity
    ADD CONSTRAINT importentity_entity_id_entity_id_fkey FOREIGN KEY (importentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: importentity_import_id_import_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY importentity
    ADD CONSTRAINT importentity_import_id_import_id_fkey FOREIGN KEY (importentity_import_id) REFERENCES import(import_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: incident_contract_id_contract_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_contract_id_contract_id_fkey FOREIGN KEY (incident_contract_id) REFERENCES contract(contract_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: incident_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_domain_id_domain_id_fkey FOREIGN KEY (incident_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: incident_logger_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_logger_userobm_id_fkey FOREIGN KEY (incident_logger) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incident_owner_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_owner_userobm_id_fkey FOREIGN KEY (incident_owner) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incident_priority_id_incidentpriority_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_priority_id_incidentpriority_id_fkey FOREIGN KEY (incident_priority_id) REFERENCES incidentpriority(incidentpriority_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incident_resolutiontype_id_incidentresolutiontype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_resolutiontype_id_incidentresolutiontype_id_fkey FOREIGN KEY (incident_resolutiontype_id) REFERENCES incidentresolutiontype(incidentresolutiontype_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incident_status_id_incidentstatus_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_status_id_incidentstatus_id_fkey FOREIGN KEY (incident_status_id) REFERENCES incidentstatus(incidentstatus_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incident_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_usercreate_userobm_id_fkey FOREIGN KEY (incident_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incident_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_userupdate_userobm_id_fkey FOREIGN KEY (incident_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incidententity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY incidententity
    ADD CONSTRAINT incidententity_entity_id_entity_id_fkey FOREIGN KEY (incidententity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: incidententity_incident_id_incident_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY incidententity
    ADD CONSTRAINT incidententity_incident_id_incident_id_fkey FOREIGN KEY (incidententity_incident_id) REFERENCES incident(incident_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: incidentpriority_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY incidentpriority
    ADD CONSTRAINT incidentpriority_domain_id_domain_id_fkey FOREIGN KEY (incidentpriority_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: incidentpriority_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY incidentpriority
    ADD CONSTRAINT incidentpriority_usercreate_userobm_id_fkey FOREIGN KEY (incidentpriority_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incidentpriority_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY incidentpriority
    ADD CONSTRAINT incidentpriority_userupdate_userobm_id_fkey FOREIGN KEY (incidentpriority_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incidentresolutiontype_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY incidentresolutiontype
    ADD CONSTRAINT incidentresolutiontype_domain_id_domain_id_fkey FOREIGN KEY (incidentresolutiontype_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: incidentresolutiontype_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY incidentresolutiontype
    ADD CONSTRAINT incidentresolutiontype_usercreate_userobm_id_fkey FOREIGN KEY (incidentresolutiontype_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incidentresolutiontype_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY incidentresolutiontype
    ADD CONSTRAINT incidentresolutiontype_userupdate_userobm_id_fkey FOREIGN KEY (incidentresolutiontype_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incidentstatus_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY incidentstatus
    ADD CONSTRAINT incidentstatus_domain_id_domain_id_fkey FOREIGN KEY (incidentstatus_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: incidentstatus_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY incidentstatus
    ADD CONSTRAINT incidentstatus_usercreate_userobm_id_fkey FOREIGN KEY (incidentstatus_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incidentstatus_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY incidentstatus
    ADD CONSTRAINT incidentstatus_userupdate_userobm_id_fkey FOREIGN KEY (incidentstatus_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: invoice_company_id_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY invoice
    ADD CONSTRAINT invoice_company_id_company_id_fkey FOREIGN KEY (invoice_company_id) REFERENCES company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: invoice_deal_id_deal_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY invoice
    ADD CONSTRAINT invoice_deal_id_deal_id_fkey FOREIGN KEY (invoice_deal_id) REFERENCES deal(deal_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: invoice_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY invoice
    ADD CONSTRAINT invoice_domain_id_domain_id_fkey FOREIGN KEY (invoice_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: invoice_project_id_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY invoice
    ADD CONSTRAINT invoice_project_id_project_id_fkey FOREIGN KEY (invoice_project_id) REFERENCES project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: invoice_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY invoice
    ADD CONSTRAINT invoice_usercreate_userobm_id_fkey FOREIGN KEY (invoice_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: invoice_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY invoice
    ADD CONSTRAINT invoice_userupdate_userobm_id_fkey FOREIGN KEY (invoice_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: invoiceentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY invoiceentity
    ADD CONSTRAINT invoiceentity_entity_id_entity_id_fkey FOREIGN KEY (invoiceentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: invoiceentity_invoice_id_invoice_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY invoiceentity
    ADD CONSTRAINT invoiceentity_invoice_id_invoice_id_fkey FOREIGN KEY (invoiceentity_invoice_id) REFERENCES invoice(invoice_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: kind_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY kind
    ADD CONSTRAINT kind_domain_id_domain_id_fkey FOREIGN KEY (kind_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: kind_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY kind
    ADD CONSTRAINT kind_usercreate_userobm_id_fkey FOREIGN KEY (kind_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: kind_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY kind
    ADD CONSTRAINT kind_userupdate_userobm_id_fkey FOREIGN KEY (kind_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: lead_company_id_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY lead
    ADD CONSTRAINT lead_company_id_company_id_fkey FOREIGN KEY (lead_company_id) REFERENCES company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: lead_contact_id_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY lead
    ADD CONSTRAINT lead_contact_id_contact_id_fkey FOREIGN KEY (lead_contact_id) REFERENCES contact(contact_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: lead_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY lead
    ADD CONSTRAINT lead_domain_id_domain_id_fkey FOREIGN KEY (lead_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: lead_manager_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY lead
    ADD CONSTRAINT lead_manager_id_userobm_id_fkey FOREIGN KEY (lead_manager_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: lead_source_id_leadsource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY lead
    ADD CONSTRAINT lead_source_id_leadsource_id_fkey FOREIGN KEY (lead_source_id) REFERENCES leadsource(leadsource_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: lead_status_id_leadstatus_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY lead
    ADD CONSTRAINT lead_status_id_leadstatus_id_fkey FOREIGN KEY (lead_status_id) REFERENCES leadstatus(leadstatus_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: lead_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY lead
    ADD CONSTRAINT lead_usercreate_userobm_id_fkey FOREIGN KEY (lead_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: lead_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY lead
    ADD CONSTRAINT lead_userupdate_userobm_id_fkey FOREIGN KEY (lead_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: leadentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY leadentity
    ADD CONSTRAINT leadentity_entity_id_entity_id_fkey FOREIGN KEY (leadentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: leadentity_lead_id_lead_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY leadentity
    ADD CONSTRAINT leadentity_lead_id_lead_id_fkey FOREIGN KEY (leadentity_lead_id) REFERENCES lead(lead_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: leadsource_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY leadsource
    ADD CONSTRAINT leadsource_domain_id_domain_id_fkey FOREIGN KEY (leadsource_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: leadsource_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY leadsource
    ADD CONSTRAINT leadsource_usercreate_userobm_id_fkey FOREIGN KEY (leadsource_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: leadsource_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY leadsource
    ADD CONSTRAINT leadsource_userupdate_userobm_id_fkey FOREIGN KEY (leadsource_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: leadstatus_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY leadstatus
    ADD CONSTRAINT leadstatus_domain_id_domain_id_fkey FOREIGN KEY (leadstatus_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: leadstatus_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY leadstatus
    ADD CONSTRAINT leadstatus_usercreate_userobm_id_fkey FOREIGN KEY (leadstatus_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: leadstatus_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY leadstatus
    ADD CONSTRAINT leadstatus_userupdate_userobm_id_fkey FOREIGN KEY (leadstatus_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: list_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY list
    ADD CONSTRAINT list_domain_id_domain_id_fkey FOREIGN KEY (list_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: list_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY list
    ADD CONSTRAINT list_usercreate_userobm_id_fkey FOREIGN KEY (list_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: list_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY list
    ADD CONSTRAINT list_userupdate_userobm_id_fkey FOREIGN KEY (list_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: listentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY listentity
    ADD CONSTRAINT listentity_entity_id_entity_id_fkey FOREIGN KEY (listentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: listentity_list_id_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY listentity
    ADD CONSTRAINT listentity_list_id_list_id_fkey FOREIGN KEY (listentity_list_id) REFERENCES list(list_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: mailboxentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY mailboxentity
    ADD CONSTRAINT mailboxentity_entity_id_entity_id_fkey FOREIGN KEY (mailboxentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: mailboxentity_mailbox_id_mailbox_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY mailboxentity
    ADD CONSTRAINT mailboxentity_mailbox_id_mailbox_id_fkey FOREIGN KEY (mailboxentity_mailbox_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: mailserver_host_id_host_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY mailserver
    ADD CONSTRAINT mailserver_host_id_host_id_fkey FOREIGN KEY (mailserver_host_id) REFERENCES host(host_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: mailserver_relayhost_id_host_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY mailserver
    ADD CONSTRAINT mailserver_relayhost_id_host_id_fkey FOREIGN KEY (mailserver_relayhost_id) REFERENCES host(host_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: mailserver_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY mailserver
    ADD CONSTRAINT mailserver_usercreate_userobm_id_fkey FOREIGN KEY (mailserver_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: mailserver_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY mailserver
    ADD CONSTRAINT mailserver_userupdate_userobm_id_fkey FOREIGN KEY (mailserver_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: mailshare_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY mailshare
    ADD CONSTRAINT mailshare_domain_id_domain_id_fkey FOREIGN KEY (mailshare_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: mailshare_mail_server_id_mailserver_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY mailshare
    ADD CONSTRAINT mailshare_mail_server_id_mailserver_id_fkey FOREIGN KEY (mailshare_mail_server_id) REFERENCES mailserver(mailserver_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: mailshare_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY mailshare
    ADD CONSTRAINT mailshare_usercreate_userobm_id_fkey FOREIGN KEY (mailshare_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: mailshare_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY mailshare
    ADD CONSTRAINT mailshare_userupdate_userobm_id_fkey FOREIGN KEY (mailshare_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: mailshareentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY mailshareentity
    ADD CONSTRAINT mailshareentity_entity_id_entity_id_fkey FOREIGN KEY (mailshareentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: mailshareentity_mailshare_id_mailshare_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY mailshareentity
    ADD CONSTRAINT mailshareentity_mailshare_id_mailshare_id_fkey FOREIGN KEY (mailshareentity_mailshare_id) REFERENCES mailshare(mailshare_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: obmbookmark_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY obmbookmark
    ADD CONSTRAINT obmbookmark_user_id_userobm_id_fkey FOREIGN KEY (obmbookmark_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: obmbookmarkentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY obmbookmarkentity
    ADD CONSTRAINT obmbookmarkentity_entity_id_entity_id_fkey FOREIGN KEY (obmbookmarkentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: obmbookmarkentity_obmbookmark_id_obmbookmark_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY obmbookmarkentity
    ADD CONSTRAINT obmbookmarkentity_obmbookmark_id_obmbookmark_id_fkey FOREIGN KEY (obmbookmarkentity_obmbookmark_id) REFERENCES obmbookmark(obmbookmark_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: obmbookmarkproperty_bookmark_id_obmbookmark_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY obmbookmarkproperty
    ADD CONSTRAINT obmbookmarkproperty_bookmark_id_obmbookmark_id_fkey FOREIGN KEY (obmbookmarkproperty_bookmark_id) REFERENCES obmbookmark(obmbookmark_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: of_usergroup_group_id_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY of_usergroup
    ADD CONSTRAINT of_usergroup_group_id_group_id_fkey FOREIGN KEY (of_usergroup_group_id) REFERENCES ugroup(group_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: of_usergroup_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY of_usergroup
    ADD CONSTRAINT of_usergroup_user_id_userobm_id_fkey FOREIGN KEY (of_usergroup_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ogroup_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY ogroup
    ADD CONSTRAINT ogroup_domain_id_domain_id_fkey FOREIGN KEY (ogroup_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ogroup_organizationalchart_id_organizationalchart_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY ogroup
    ADD CONSTRAINT ogroup_organizationalchart_id_organizationalchart_id_fkey FOREIGN KEY (ogroup_organizationalchart_id) REFERENCES organizationalchart(organizationalchart_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ogroup_parent_id_ogroup_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY ogroup
    ADD CONSTRAINT ogroup_parent_id_ogroup_id_fkey FOREIGN KEY (ogroup_parent_id) REFERENCES ogroup(ogroup_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ogroup_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY ogroup
    ADD CONSTRAINT ogroup_usercreate_userobm_id_fkey FOREIGN KEY (ogroup_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: ogroup_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY ogroup
    ADD CONSTRAINT ogroup_userupdate_userobm_id_fkey FOREIGN KEY (ogroup_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: ogroupentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY ogroupentity
    ADD CONSTRAINT ogroupentity_entity_id_entity_id_fkey FOREIGN KEY (ogroupentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ogroupentity_ogroup_id_ogroup_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY ogroupentity
    ADD CONSTRAINT ogroupentity_ogroup_id_ogroup_id_fkey FOREIGN KEY (ogroupentity_ogroup_id) REFERENCES ogroup(ogroup_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ogrouplink_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY ogrouplink
    ADD CONSTRAINT ogrouplink_domain_id_domain_id_fkey FOREIGN KEY (ogrouplink_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ogrouplink_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY ogrouplink
    ADD CONSTRAINT ogrouplink_entity_id_entity_id_fkey FOREIGN KEY (ogrouplink_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ogrouplink_ogroup_id_ogroup_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY ogrouplink
    ADD CONSTRAINT ogrouplink_ogroup_id_ogroup_id_fkey FOREIGN KEY (ogrouplink_ogroup_id) REFERENCES ogroup(ogroup_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ogrouplink_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY ogrouplink
    ADD CONSTRAINT ogrouplink_usercreate_userobm_id_fkey FOREIGN KEY (ogrouplink_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: ogrouplink_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY ogrouplink
    ADD CONSTRAINT ogrouplink_userupdate_userobm_id_fkey FOREIGN KEY (ogrouplink_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: organizationalchart_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY organizationalchart
    ADD CONSTRAINT organizationalchart_domain_id_domain_id_fkey FOREIGN KEY (organizationalchart_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: organizationalchart_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY organizationalchart
    ADD CONSTRAINT organizationalchart_usercreate_userobm_id_fkey FOREIGN KEY (organizationalchart_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: organizationalchart_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY organizationalchart
    ADD CONSTRAINT organizationalchart_userupdate_userobm_id_fkey FOREIGN KEY (organizationalchart_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: organizationalchartentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY organizationalchartentity
    ADD CONSTRAINT organizationalchartentity_entity_id_entity_id_fkey FOREIGN KEY (organizationalchartentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: organizationalchartentity_organizationalchart_id_organizational; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY organizationalchartentity
    ADD CONSTRAINT organizationalchartentity_organizationalchart_id_organizational FOREIGN KEY (organizationalchartentity_organizationalchart_id) REFERENCES organizationalchart(organizationalchart_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: parentdeal_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY parentdeal
    ADD CONSTRAINT parentdeal_domain_id_domain_id_fkey FOREIGN KEY (parentdeal_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: parentdeal_marketingmanager_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY parentdeal
    ADD CONSTRAINT parentdeal_marketingmanager_id_userobm_id_fkey FOREIGN KEY (parentdeal_marketingmanager_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: parentdeal_technicalmanager_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY parentdeal
    ADD CONSTRAINT parentdeal_technicalmanager_id_userobm_id_fkey FOREIGN KEY (parentdeal_technicalmanager_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: parentdeal_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY parentdeal
    ADD CONSTRAINT parentdeal_usercreate_userobm_id_fkey FOREIGN KEY (parentdeal_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: parentdeal_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY parentdeal
    ADD CONSTRAINT parentdeal_userupdate_userobm_id_fkey FOREIGN KEY (parentdeal_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: parentdealentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY parentdealentity
    ADD CONSTRAINT parentdealentity_entity_id_entity_id_fkey FOREIGN KEY (parentdealentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: parentdealentity_parentdeal_id_parentdeal_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY parentdealentity
    ADD CONSTRAINT parentdealentity_parentdeal_id_parentdeal_id_fkey FOREIGN KEY (parentdealentity_parentdeal_id) REFERENCES parentdeal(parentdeal_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: payment_account_id_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY payment
    ADD CONSTRAINT payment_account_id_account_id_fkey FOREIGN KEY (payment_account_id) REFERENCES account(account_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: payment_company_id_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY payment
    ADD CONSTRAINT payment_company_id_company_id_fkey FOREIGN KEY (payment_company_id) REFERENCES company(company_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: payment_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY payment
    ADD CONSTRAINT payment_domain_id_domain_id_fkey FOREIGN KEY (payment_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: payment_paymentkind_id_paymentkind_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY payment
    ADD CONSTRAINT payment_paymentkind_id_paymentkind_id_fkey FOREIGN KEY (payment_paymentkind_id) REFERENCES paymentkind(paymentkind_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: payment_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY payment
    ADD CONSTRAINT payment_usercreate_userobm_id_fkey FOREIGN KEY (payment_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: payment_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY payment
    ADD CONSTRAINT payment_userupdate_userobm_id_fkey FOREIGN KEY (payment_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: paymententity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY paymententity
    ADD CONSTRAINT paymententity_entity_id_entity_id_fkey FOREIGN KEY (paymententity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: paymententity_payment_id_payment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY paymententity
    ADD CONSTRAINT paymententity_payment_id_payment_id_fkey FOREIGN KEY (paymententity_payment_id) REFERENCES payment(payment_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: paymentinvoice_invoice_id_invoice_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY paymentinvoice
    ADD CONSTRAINT paymentinvoice_invoice_id_invoice_id_fkey FOREIGN KEY (paymentinvoice_invoice_id) REFERENCES invoice(invoice_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: paymentinvoice_payment_id_payment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY paymentinvoice
    ADD CONSTRAINT paymentinvoice_payment_id_payment_id_fkey FOREIGN KEY (paymentinvoice_payment_id) REFERENCES payment(payment_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: paymentinvoice_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY paymentinvoice
    ADD CONSTRAINT paymentinvoice_usercreate_userobm_id_fkey FOREIGN KEY (paymentinvoice_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: paymentinvoice_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY paymentinvoice
    ADD CONSTRAINT paymentinvoice_userupdate_userobm_id_fkey FOREIGN KEY (paymentinvoice_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: paymentkind_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY paymentkind
    ADD CONSTRAINT paymentkind_domain_id_domain_id_fkey FOREIGN KEY (paymentkind_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: phone_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY phone
    ADD CONSTRAINT phone_entity_id_entity_id_fkey FOREIGN KEY (phone_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: profileentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY profileentity
    ADD CONSTRAINT profileentity_entity_id_entity_id_fkey FOREIGN KEY (profileentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: profileentity_profile_id_profile_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY profileentity
    ADD CONSTRAINT profileentity_profile_id_profile_id_fkey FOREIGN KEY (profileentity_profile_id) REFERENCES profile(profile_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: profilemodule_profile_id_profile_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY profilemodule
    ADD CONSTRAINT profilemodule_profile_id_profile_id_fkey FOREIGN KEY (profilemodule_profile_id) REFERENCES profile(profile_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: profilepropertyvalue_profile_id_profile_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY profilepropertyvalue
    ADD CONSTRAINT profilepropertyvalue_profile_id_profile_id_fkey FOREIGN KEY (profilepropertyvalue_profile_id) REFERENCES profile(profile_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: profilepropertyvalue_profileproperty_id_profileproperty_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY profilepropertyvalue
    ADD CONSTRAINT profilepropertyvalue_profileproperty_id_profileproperty_id_fkey FOREIGN KEY (profilepropertyvalue_property_id) REFERENCES profileproperty(profileproperty_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: profilesection_profile_id_profile_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY profilesection
    ADD CONSTRAINT profilesection_profile_id_profile_id_fkey FOREIGN KEY (profilesection_profile_id) REFERENCES profile(profile_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: project_company_id_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_company_id_company_id_fkey FOREIGN KEY (project_company_id) REFERENCES company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: project_deal_id_deal_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_deal_id_deal_id_fkey FOREIGN KEY (project_deal_id) REFERENCES deal(deal_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: project_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_domain_id_domain_id_fkey FOREIGN KEY (project_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: project_tasktype_id_tasktype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_tasktype_id_tasktype_id_fkey FOREIGN KEY (project_tasktype_id) REFERENCES tasktype(tasktype_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: project_type_id_dealtype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_type_id_dealtype_id_fkey FOREIGN KEY (project_type_id) REFERENCES dealtype(dealtype_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: project_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_usercreate_userobm_id_fkey FOREIGN KEY (project_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: project_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_userupdate_userobm_id_fkey FOREIGN KEY (project_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: projectclosing_project_id_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projectclosing
    ADD CONSTRAINT projectclosing_project_id_project_id_fkey FOREIGN KEY (projectclosing_project_id) REFERENCES project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: projectclosing_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projectclosing
    ADD CONSTRAINT projectclosing_usercreate_userobm_id_fkey FOREIGN KEY (projectclosing_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: projectclosing_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projectclosing
    ADD CONSTRAINT projectclosing_userupdate_userobm_id_fkey FOREIGN KEY (projectclosing_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: projectcv_cv_id_cv_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projectcv
    ADD CONSTRAINT projectcv_cv_id_cv_id_fkey FOREIGN KEY (projectcv_cv_id) REFERENCES cv(cv_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: projectcv_project_id_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projectcv
    ADD CONSTRAINT projectcv_project_id_project_id_fkey FOREIGN KEY (projectcv_project_id) REFERENCES project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: projectentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projectentity
    ADD CONSTRAINT projectentity_entity_id_entity_id_fkey FOREIGN KEY (projectentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: projectentity_project_id_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projectentity
    ADD CONSTRAINT projectentity_project_id_project_id_fkey FOREIGN KEY (projectentity_project_id) REFERENCES project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: projectreftask_tasktype_id_tasktype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projectreftask
    ADD CONSTRAINT projectreftask_tasktype_id_tasktype_id_fkey FOREIGN KEY (projectreftask_tasktype_id) REFERENCES tasktype(tasktype_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: projectreftask_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projectreftask
    ADD CONSTRAINT projectreftask_usercreate_userobm_id_fkey FOREIGN KEY (projectreftask_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: projectreftask_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projectreftask
    ADD CONSTRAINT projectreftask_userupdate_userobm_id_fkey FOREIGN KEY (projectreftask_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: projecttask_parenttask_id_projecttask_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projecttask
    ADD CONSTRAINT projecttask_parenttask_id_projecttask_id_fkey FOREIGN KEY (projecttask_parenttask_id) REFERENCES projecttask(projecttask_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: projecttask_project_id_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projecttask
    ADD CONSTRAINT projecttask_project_id_project_id_fkey FOREIGN KEY (projecttask_project_id) REFERENCES project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: projecttask_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projecttask
    ADD CONSTRAINT projecttask_usercreate_userobm_id_fkey FOREIGN KEY (projecttask_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: projecttask_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projecttask
    ADD CONSTRAINT projecttask_userupdate_userobm_id_fkey FOREIGN KEY (projecttask_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: projectuser_project_id_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projectuser
    ADD CONSTRAINT projectuser_project_id_project_id_fkey FOREIGN KEY (projectuser_project_id) REFERENCES project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: projectuser_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projectuser
    ADD CONSTRAINT projectuser_user_id_userobm_id_fkey FOREIGN KEY (projectuser_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: projectuser_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projectuser
    ADD CONSTRAINT projectuser_usercreate_userobm_id_fkey FOREIGN KEY (projectuser_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: projectuser_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projectuser
    ADD CONSTRAINT projectuser_userupdate_userobm_id_fkey FOREIGN KEY (projectuser_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: publication_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY publication
    ADD CONSTRAINT publication_domain_id_domain_id_fkey FOREIGN KEY (publication_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: publication_type_id_publicationtype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY publication
    ADD CONSTRAINT publication_type_id_publicationtype_id_fkey FOREIGN KEY (publication_type_id) REFERENCES publicationtype(publicationtype_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: publication_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY publication
    ADD CONSTRAINT publication_usercreate_userobm_id_fkey FOREIGN KEY (publication_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: publication_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY publication
    ADD CONSTRAINT publication_userupdate_userobm_id_fkey FOREIGN KEY (publication_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: publicationentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY publicationentity
    ADD CONSTRAINT publicationentity_entity_id_entity_id_fkey FOREIGN KEY (publicationentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: publicationentity_publication_id_publication_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY publicationentity
    ADD CONSTRAINT publicationentity_publication_id_publication_id_fkey FOREIGN KEY (publicationentity_publication_id) REFERENCES publication(publication_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: publicationtype_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY publicationtype
    ADD CONSTRAINT publicationtype_domain_id_domain_id_fkey FOREIGN KEY (publicationtype_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: publicationtype_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY publicationtype
    ADD CONSTRAINT publicationtype_usercreate_userobm_id_fkey FOREIGN KEY (publicationtype_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: publicationtype_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY publicationtype
    ADD CONSTRAINT publicationtype_userupdate_userobm_id_fkey FOREIGN KEY (publicationtype_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: region_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY region
    ADD CONSTRAINT region_domain_id_domain_id_fkey FOREIGN KEY (region_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: region_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY region
    ADD CONSTRAINT region_usercreate_userobm_id_fkey FOREIGN KEY (region_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: region_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY region
    ADD CONSTRAINT region_userupdate_userobm_id_fkey FOREIGN KEY (region_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: resource_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY resource
    ADD CONSTRAINT resource_domain_id_domain_id_fkey FOREIGN KEY (resource_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: resource_rtype_id_resourcetype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY resource
    ADD CONSTRAINT resource_rtype_id_resourcetype_id_fkey FOREIGN KEY (resource_rtype_id) REFERENCES resourcetype(resourcetype_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: resource_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY resource
    ADD CONSTRAINT resource_usercreate_userobm_id_fkey FOREIGN KEY (resource_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: resource_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY resource
    ADD CONSTRAINT resource_userupdate_userobm_id_fkey FOREIGN KEY (resource_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: resourceentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY resourceentity
    ADD CONSTRAINT resourceentity_entity_id_entity_id_fkey FOREIGN KEY (resourceentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: resourceentity_resource_id_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY resourceentity
    ADD CONSTRAINT resourceentity_resource_id_resource_id_fkey FOREIGN KEY (resourceentity_resource_id) REFERENCES resource(resource_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: resourcegroup_resource_id_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY resourcegroup
    ADD CONSTRAINT resourcegroup_resource_id_resource_id_fkey FOREIGN KEY (resourcegroup_resource_id) REFERENCES resource(resource_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: resourcegroup_rgroup_id_rgroup_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY resourcegroup
    ADD CONSTRAINT resourcegroup_rgroup_id_rgroup_id_fkey FOREIGN KEY (resourcegroup_rgroup_id) REFERENCES rgroup(rgroup_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: resourcegroupentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY resourcegroupentity
    ADD CONSTRAINT resourcegroupentity_entity_id_entity_id_fkey FOREIGN KEY (resourcegroupentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: resourcegroupentity_resourcegroup_id_resourcegroup_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY resourcegroupentity
    ADD CONSTRAINT resourcegroupentity_resourcegroup_id_resourcegroup_id_fkey FOREIGN KEY (resourcegroupentity_resourcegroup_id) REFERENCES rgroup(rgroup_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: resourceitem_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY resourceitem
    ADD CONSTRAINT resourceitem_domain_id_domain_id_fkey FOREIGN KEY (resourceitem_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: resourceitem_resourcetype_id_resourcetype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY resourceitem
    ADD CONSTRAINT resourceitem_resourcetype_id_resourcetype_id_fkey FOREIGN KEY (resourceitem_resourcetype_id) REFERENCES resourcetype(resourcetype_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: resourcetype_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY resourcetype
    ADD CONSTRAINT resourcetype_domain_id_domain_id_fkey FOREIGN KEY (resourcetype_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: rgroup_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY rgroup
    ADD CONSTRAINT rgroup_domain_id_domain_id_fkey FOREIGN KEY (rgroup_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: rgroup_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY rgroup
    ADD CONSTRAINT rgroup_usercreate_userobm_id_fkey FOREIGN KEY (rgroup_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: rgroup_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY rgroup
    ADD CONSTRAINT rgroup_userupdate_userobm_id_fkey FOREIGN KEY (rgroup_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: samba_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samba
    ADD CONSTRAINT samba_domain_id_domain_id_fkey FOREIGN KEY (samba_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: servicedomain_id_service_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY servicedomain
    ADD CONSTRAINT servicedomain_id_service_id_fkey FOREIGN KEY (servicedomain_id) REFERENCES service(service_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: servicehost_id_service_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY servicehost
    ADD CONSTRAINT servicehost_id_service_id_fkey FOREIGN KEY (servicehost_id) REFERENCES service(service_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: serviceproperty_service_id_service_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY serviceproperty
    ADD CONSTRAINT serviceproperty_service_id_service_id_fkey FOREIGN KEY (serviceproperty_service_id) REFERENCES service(service_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: servicepropertydomain_id_serviceproperty_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY servicepropertydomain
    ADD CONSTRAINT servicepropertydomain_id_serviceproperty_id_fkey FOREIGN KEY (servicepropertydomain_id) REFERENCES serviceproperty(serviceproperty_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: servicepropertyhost_id_serviceproperty_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY servicepropertyhost
    ADD CONSTRAINT servicepropertyhost_id_serviceproperty_id_fkey FOREIGN KEY (servicepropertyhost_id) REFERENCES serviceproperty(serviceproperty_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: subscription_contact_id_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY subscription
    ADD CONSTRAINT subscription_contact_id_contact_id_fkey FOREIGN KEY (subscription_contact_id) REFERENCES contact(contact_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: subscription_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY subscription
    ADD CONSTRAINT subscription_domain_id_domain_id_fkey FOREIGN KEY (subscription_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: subscription_publication_id_publication_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY subscription
    ADD CONSTRAINT subscription_publication_id_publication_id_fkey FOREIGN KEY (subscription_publication_id) REFERENCES publication(publication_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: subscription_reception_id_subscriptionreception_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY subscription
    ADD CONSTRAINT subscription_reception_id_subscriptionreception_id_fkey FOREIGN KEY (subscription_reception_id) REFERENCES subscriptionreception(subscriptionreception_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: subscription_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY subscription
    ADD CONSTRAINT subscription_usercreate_userobm_id_fkey FOREIGN KEY (subscription_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: subscription_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY subscription
    ADD CONSTRAINT subscription_userupdate_userobm_id_fkey FOREIGN KEY (subscription_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: subscriptionentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY subscriptionentity
    ADD CONSTRAINT subscriptionentity_entity_id_entity_id_fkey FOREIGN KEY (subscriptionentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: subscriptionentity_subscription_id_subscription_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY subscriptionentity
    ADD CONSTRAINT subscriptionentity_subscription_id_subscription_id_fkey FOREIGN KEY (subscriptionentity_subscription_id) REFERENCES subscription(subscription_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: subscriptionreception_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY subscriptionreception
    ADD CONSTRAINT subscriptionreception_domain_id_domain_id_fkey FOREIGN KEY (subscriptionreception_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: subscriptionreception_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY subscriptionreception
    ADD CONSTRAINT subscriptionreception_usercreate_userobm_id_fkey FOREIGN KEY (subscriptionreception_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: subscriptionreception_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY subscriptionreception
    ADD CONSTRAINT subscriptionreception_userupdate_userobm_id_fkey FOREIGN KEY (subscriptionreception_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: tasktype_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY tasktype
    ADD CONSTRAINT tasktype_domain_id_domain_id_fkey FOREIGN KEY (tasktype_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: tasktype_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY tasktype
    ADD CONSTRAINT tasktype_usercreate_userobm_id_fkey FOREIGN KEY (tasktype_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: tasktype_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY tasktype
    ADD CONSTRAINT tasktype_userupdate_userobm_id_fkey FOREIGN KEY (tasktype_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: timetask_projecttask_id_projecttask_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY timetask
    ADD CONSTRAINT timetask_projecttask_id_projecttask_id_fkey FOREIGN KEY (timetask_projecttask_id) REFERENCES projecttask(projecttask_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: timetask_tasktype_id_tasktype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY timetask
    ADD CONSTRAINT timetask_tasktype_id_tasktype_id_fkey FOREIGN KEY (timetask_tasktype_id) REFERENCES tasktype(tasktype_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: timetask_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY timetask
    ADD CONSTRAINT timetask_user_id_userobm_id_fkey FOREIGN KEY (timetask_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: timetask_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY timetask
    ADD CONSTRAINT timetask_usercreate_userobm_id_fkey FOREIGN KEY (timetask_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: timetask_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY timetask
    ADD CONSTRAINT timetask_userupdate_userobm_id_fkey FOREIGN KEY (timetask_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: todo_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY todo
    ADD CONSTRAINT todo_domain_id_domain_id_fkey FOREIGN KEY (todo_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: todo_user_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY todo
    ADD CONSTRAINT todo_user_userobm_id_fkey FOREIGN KEY (todo_user) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: todo_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY todo
    ADD CONSTRAINT todo_usercreate_userobm_id_fkey FOREIGN KEY (todo_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: todo_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY todo
    ADD CONSTRAINT todo_userupdate_userobm_id_fkey FOREIGN KEY (todo_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: updated_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY updated
    ADD CONSTRAINT updated_domain_id_domain_id_fkey FOREIGN KEY (updated_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: updated_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY updated
    ADD CONSTRAINT updated_user_id_userobm_id_fkey FOREIGN KEY (updated_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: updatedlinks_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY updatedlinks
    ADD CONSTRAINT updatedlinks_domain_id_domain_id_fkey FOREIGN KEY (updatedlinks_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: updatedlinks_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY updatedlinks
    ADD CONSTRAINT updatedlinks_user_id_userobm_id_fkey FOREIGN KEY (updatedlinks_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: userentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY userentity
    ADD CONSTRAINT userentity_entity_id_entity_id_fkey FOREIGN KEY (userentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: userentity_user_id_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY userentity
    ADD CONSTRAINT userentity_user_id_user_id_fkey FOREIGN KEY (userentity_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: userobm_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY userobm
    ADD CONSTRAINT userobm_domain_id_domain_id_fkey FOREIGN KEY (userobm_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: userobm_host_id_host_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY userobm
    ADD CONSTRAINT userobm_host_id_host_id_fkey FOREIGN KEY (userobm_host_id) REFERENCES host(host_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: userobm_mail_server_id_mailserver_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY userobm
    ADD CONSTRAINT userobm_mail_server_id_mailserver_id_fkey FOREIGN KEY (userobm_mail_server_id) REFERENCES mailserver(mailserver_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: userobm_photo_id_document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY userobm
    ADD CONSTRAINT userobm_photo_id_document_id_fkey FOREIGN KEY (userobm_photo_id) REFERENCES document(document_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: userobm_sessionlog_userobm_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY userobm_sessionlog
    ADD CONSTRAINT userobm_sessionlog_userobm_id_userobm_id_fkey FOREIGN KEY (userobm_sessionlog_userobm_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: userobm_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY userobm
    ADD CONSTRAINT userobm_usercreate_userobm_id_fkey FOREIGN KEY (userobm_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: userobm_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY userobm
    ADD CONSTRAINT userobm_userupdate_userobm_id_fkey FOREIGN KEY (userobm_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: userobmgroup_group_id_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY userobmgroup
    ADD CONSTRAINT userobmgroup_group_id_group_id_fkey FOREIGN KEY (userobmgroup_group_id) REFERENCES ugroup(group_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: userobmgroup_userobm_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY userobmgroup
    ADD CONSTRAINT userobmgroup_userobm_id_userobm_id_fkey FOREIGN KEY (userobmgroup_userobm_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: userobmpref_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY userobmpref
    ADD CONSTRAINT userobmpref_user_id_userobm_id_fkey FOREIGN KEY (userobmpref_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: website_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY website
    ADD CONSTRAINT website_entity_id_entity_id_fkey FOREIGN KEY (website_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: public; Type: ACL; Schema: -; Owner: -
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

