--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;
SET search_path = public, pg_catalog;
SET default_tablespace = '';
SET default_with_oids = false;

--
-- Name: pimdata_type; Type: TYPE; Schema: public; Owner: obm
--

CREATE TYPE pimdata_type AS ENUM (
    'EMAIL',
    'CALENDAR',
    'CONTACTS',
    'TASKS'
);


ALTER TYPE public.pimdata_type OWNER TO obm;

--
-- Name: taskperiod; Type: TYPE; Schema: public; Owner: obm
--

CREATE TYPE taskperiod AS ENUM (
    'MORNING',
    'AFTERNOON',
    'ALLDAY'
);


ALTER TYPE public.taskperiod OWNER TO obm;

--
-- Name: userstatus; Type: TYPE; Schema: public; Owner: obm
--

CREATE TYPE userstatus AS ENUM (
    'INIT',
    'VALID'
);


ALTER TYPE public.userstatus OWNER TO obm;

--
-- Name: vcomponent; Type: TYPE; Schema: public; Owner: obm
--

CREATE TYPE vcomponent AS ENUM (
    'VEVENT',
    'VTODO',
    'VJOURNAL',
    'VFREEBUSY'
);


ALTER TYPE public.vcomponent OWNER TO obm;

--
-- Name: vkind; Type: TYPE; Schema: public; Owner: obm
--

CREATE TYPE vkind AS ENUM (
    'VEVENT',
    'VCONTACT'
);


ALTER TYPE public.vkind OWNER TO obm;

--
-- Name: vopacity; Type: TYPE; Schema: public; Owner: obm
--

CREATE TYPE vopacity AS ENUM (
    'OPAQUE',
    'TRANSPARENT'
);


ALTER TYPE public.vopacity OWNER TO obm;

--
-- Name: vpartstat; Type: TYPE; Schema: public; Owner: obm
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


ALTER TYPE public.vpartstat OWNER TO obm;

--
-- Name: vrole; Type: TYPE; Schema: public; Owner: obm
--

CREATE TYPE vrole AS ENUM (
    'CHAIR',
    'REQ',
    'OPT',
    'NON'
);


ALTER TYPE public.vrole OWNER TO obm;

--
-- Name: on_account_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_account_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.account_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_account_change() OWNER TO obm;

--
-- Name: on_account_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_account_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.account_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_account_create() OWNER TO obm;

--
-- Name: on_activeuserobm_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_activeuserobm_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.activeuserobm_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_activeuserobm_change() OWNER TO obm;

--
-- Name: on_activeuserobm_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_activeuserobm_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.activeuserobm_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_activeuserobm_create() OWNER TO obm;

--
-- Name: on_addressbook_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_addressbook_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_addressbook_change() OWNER TO obm;

--
-- Name: on_addressbook_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_addressbook_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_addressbook_create() OWNER TO obm;

--
-- Name: on_category_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_category_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.category_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_category_change() OWNER TO obm;

--
-- Name: on_category_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_category_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.category_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_category_create() OWNER TO obm;

--
-- Name: on_company_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_company_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.company_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_company_change() OWNER TO obm;

--
-- Name: on_company_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_company_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.company_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_company_create() OWNER TO obm;

--
-- Name: on_companyactivity_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_companyactivity_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.companyactivity_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_companyactivity_change() OWNER TO obm;

--
-- Name: on_companyactivity_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_companyactivity_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.companyactivity_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_companyactivity_create() OWNER TO obm;

--
-- Name: on_companynafcode_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_companynafcode_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.companynafcode_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_companynafcode_change() OWNER TO obm;

--
-- Name: on_companynafcode_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_companynafcode_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.companynafcode_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_companynafcode_create() OWNER TO obm;

--
-- Name: on_companytype_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_companytype_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.companytype_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_companytype_change() OWNER TO obm;

--
-- Name: on_companytype_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_companytype_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.companytype_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_companytype_create() OWNER TO obm;

--
-- Name: on_contact_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_contact_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.contact_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_contact_change() OWNER TO obm;

--
-- Name: on_contact_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_contact_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.contact_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_contact_create() OWNER TO obm;

--
-- Name: on_contactfunction_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_contactfunction_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.contactfunction_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_contactfunction_change() OWNER TO obm;

--
-- Name: on_contactfunction_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_contactfunction_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.contactfunction_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_contactfunction_create() OWNER TO obm;

--
-- Name: on_contract_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_contract_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.contract_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_contract_change() OWNER TO obm;

--
-- Name: on_contract_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_contract_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.contract_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_contract_create() OWNER TO obm;

--
-- Name: on_contractpriority_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_contractpriority_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.contractpriority_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_contractpriority_change() OWNER TO obm;

--
-- Name: on_contractpriority_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_contractpriority_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.contractpriority_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_contractpriority_create() OWNER TO obm;

--
-- Name: on_contractstatus_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_contractstatus_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.contractstatus_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_contractstatus_change() OWNER TO obm;

--
-- Name: on_contractstatus_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_contractstatus_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.contractstatus_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_contractstatus_create() OWNER TO obm;

--
-- Name: on_contracttype_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_contracttype_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.contracttype_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_contracttype_change() OWNER TO obm;

--
-- Name: on_contracttype_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_contracttype_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.contracttype_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_contracttype_create() OWNER TO obm;

--
-- Name: on_country_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_country_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.country_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_country_change() OWNER TO obm;

--
-- Name: on_country_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_country_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.country_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_country_create() OWNER TO obm;

--
-- Name: on_cv_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_cv_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.cv_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_cv_change() OWNER TO obm;

--
-- Name: on_cv_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_cv_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.cv_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_cv_create() OWNER TO obm;

--
-- Name: on_datasource_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_datasource_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.datasource_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_datasource_change() OWNER TO obm;

--
-- Name: on_datasource_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_datasource_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.datasource_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_datasource_create() OWNER TO obm;

--
-- Name: on_deal_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_deal_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.deal_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_deal_change() OWNER TO obm;

--
-- Name: on_deal_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_deal_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.deal_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_deal_create() OWNER TO obm;

--
-- Name: on_dealcompany_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_dealcompany_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.dealcompany_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_dealcompany_change() OWNER TO obm;

--
-- Name: on_dealcompany_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_dealcompany_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.dealcompany_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_dealcompany_create() OWNER TO obm;

--
-- Name: on_dealcompanyrole_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_dealcompanyrole_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.dealcompanyrole_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_dealcompanyrole_change() OWNER TO obm;

--
-- Name: on_dealcompanyrole_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_dealcompanyrole_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.dealcompanyrole_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_dealcompanyrole_create() OWNER TO obm;

--
-- Name: on_dealstatus_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_dealstatus_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.dealstatus_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_dealstatus_change() OWNER TO obm;

--
-- Name: on_dealstatus_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_dealstatus_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.dealstatus_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_dealstatus_create() OWNER TO obm;

--
-- Name: on_dealtype_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_dealtype_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.dealtype_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_dealtype_change() OWNER TO obm;

--
-- Name: on_dealtype_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_dealtype_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.dealtype_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_dealtype_create() OWNER TO obm;

--
-- Name: on_document_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_document_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.document_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_document_change() OWNER TO obm;

--
-- Name: on_document_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_document_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.document_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_document_create() OWNER TO obm;

--
-- Name: on_documentmimetype_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_documentmimetype_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.documentmimetype_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_documentmimetype_change() OWNER TO obm;

--
-- Name: on_documentmimetype_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_documentmimetype_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.documentmimetype_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_documentmimetype_create() OWNER TO obm;

--
-- Name: on_domain_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_domain_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.domain_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_domain_change() OWNER TO obm;

--
-- Name: on_domain_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_domain_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.domain_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_domain_create() OWNER TO obm;

--
-- Name: on_event_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_event_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.event_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_event_change() OWNER TO obm;

--
-- Name: on_event_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_event_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.event_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_event_create() OWNER TO obm;

--
-- Name: on_eventalert_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_eventalert_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.eventalert_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_eventalert_change() OWNER TO obm;

--
-- Name: on_eventalert_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_eventalert_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.eventalert_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_eventalert_create() OWNER TO obm;

--
-- Name: on_eventcategory1_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_eventcategory1_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.eventcategory1_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_eventcategory1_change() OWNER TO obm;

--
-- Name: on_eventcategory1_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_eventcategory1_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.eventcategory1_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_eventcategory1_create() OWNER TO obm;

--
-- Name: on_eventexception_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_eventexception_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.eventexception_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_eventexception_change() OWNER TO obm;

--
-- Name: on_eventexception_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_eventexception_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.eventexception_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_eventexception_create() OWNER TO obm;

--
-- Name: on_eventlink_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_eventlink_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.eventlink_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_eventlink_change() OWNER TO obm;

--
-- Name: on_eventlink_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_eventlink_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.eventlink_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_eventlink_create() OWNER TO obm;

--
-- Name: on_eventtemplate_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_eventtemplate_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.eventtemplate_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_eventtemplate_change() OWNER TO obm;

--
-- Name: on_eventtemplate_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_eventtemplate_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.eventtemplate_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_eventtemplate_create() OWNER TO obm;

--
-- Name: on_host_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_host_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.host_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_host_change() OWNER TO obm;

--
-- Name: on_host_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_host_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.host_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_host_create() OWNER TO obm;

--
-- Name: on_import_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_import_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.import_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_import_change() OWNER TO obm;

--
-- Name: on_import_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_import_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.import_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_import_create() OWNER TO obm;

--
-- Name: on_incident_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_incident_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.incident_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_incident_change() OWNER TO obm;

--
-- Name: on_incident_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_incident_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.incident_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_incident_create() OWNER TO obm;

--
-- Name: on_incidentpriority_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_incidentpriority_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.incidentpriority_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_incidentpriority_change() OWNER TO obm;

--
-- Name: on_incidentpriority_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_incidentpriority_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.incidentpriority_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_incidentpriority_create() OWNER TO obm;

--
-- Name: on_incidentresolutiontype_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_incidentresolutiontype_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.incidentresolutiontype_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_incidentresolutiontype_change() OWNER TO obm;

--
-- Name: on_incidentresolutiontype_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_incidentresolutiontype_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.incidentresolutiontype_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_incidentresolutiontype_create() OWNER TO obm;

--
-- Name: on_incidentstatus_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_incidentstatus_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.incidentstatus_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_incidentstatus_change() OWNER TO obm;

--
-- Name: on_incidentstatus_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_incidentstatus_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.incidentstatus_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_incidentstatus_create() OWNER TO obm;

--
-- Name: on_invoice_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_invoice_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.invoice_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_invoice_change() OWNER TO obm;

--
-- Name: on_invoice_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_invoice_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.invoice_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_invoice_create() OWNER TO obm;

--
-- Name: on_kind_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_kind_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.kind_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_kind_change() OWNER TO obm;

--
-- Name: on_kind_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_kind_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.kind_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_kind_create() OWNER TO obm;

--
-- Name: on_lead_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_lead_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.lead_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_lead_change() OWNER TO obm;

--
-- Name: on_lead_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_lead_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.lead_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_lead_create() OWNER TO obm;

--
-- Name: on_leadsource_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_leadsource_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.leadsource_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_leadsource_change() OWNER TO obm;

--
-- Name: on_leadsource_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_leadsource_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.leadsource_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_leadsource_create() OWNER TO obm;

--
-- Name: on_leadstatus_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_leadstatus_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.leadstatus_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_leadstatus_change() OWNER TO obm;

--
-- Name: on_leadstatus_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_leadstatus_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.leadstatus_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_leadstatus_create() OWNER TO obm;

--
-- Name: on_list_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_list_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.list_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_list_change() OWNER TO obm;

--
-- Name: on_list_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_list_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.list_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_list_create() OWNER TO obm;

--
-- Name: on_mailshare_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_mailshare_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.mailshare_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_mailshare_change() OWNER TO obm;

--
-- Name: on_mailshare_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_mailshare_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.mailshare_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_mailshare_create() OWNER TO obm;

--
-- Name: on_obmsession_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_obmsession_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.obmsession_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_obmsession_change() OWNER TO obm;

--
-- Name: on_ogroup_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_ogroup_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.ogroup_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_ogroup_change() OWNER TO obm;

--
-- Name: on_ogroup_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_ogroup_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.ogroup_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_ogroup_create() OWNER TO obm;

--
-- Name: on_ogrouplink_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_ogrouplink_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.ogrouplink_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_ogrouplink_change() OWNER TO obm;

--
-- Name: on_ogrouplink_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_ogrouplink_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.ogrouplink_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_ogrouplink_create() OWNER TO obm;

--
-- Name: on_organizationalchart_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_organizationalchart_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.organizationalchart_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_organizationalchart_change() OWNER TO obm;

--
-- Name: on_organizationalchart_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_organizationalchart_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.organizationalchart_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_organizationalchart_create() OWNER TO obm;

--
-- Name: on_parentdeal_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_parentdeal_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.parentdeal_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_parentdeal_change() OWNER TO obm;

--
-- Name: on_parentdeal_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_parentdeal_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.parentdeal_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_parentdeal_create() OWNER TO obm;

--
-- Name: on_payment_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_payment_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.payment_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_payment_change() OWNER TO obm;

--
-- Name: on_payment_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_payment_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.payment_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_payment_create() OWNER TO obm;

--
-- Name: on_paymentinvoice_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_paymentinvoice_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.paymentinvoice_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_paymentinvoice_change() OWNER TO obm;

--
-- Name: on_paymentinvoice_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_paymentinvoice_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.paymentinvoice_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_paymentinvoice_create() OWNER TO obm;

--
-- Name: on_plannedtask_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_plannedtask_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.plannedtask_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_plannedtask_change() OWNER TO obm;

--
-- Name: on_plannedtask_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_plannedtask_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.plannedtask_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_plannedtask_create() OWNER TO obm;

--
-- Name: on_profile_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_profile_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.profile_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_profile_change() OWNER TO obm;

--
-- Name: on_profile_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_profile_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.profile_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_profile_create() OWNER TO obm;

--
-- Name: on_project_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_project_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.project_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_project_change() OWNER TO obm;

--
-- Name: on_project_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_project_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.project_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_project_create() OWNER TO obm;

--
-- Name: on_projectclosing_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_projectclosing_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.projectclosing_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_projectclosing_change() OWNER TO obm;

--
-- Name: on_projectclosing_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_projectclosing_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.projectclosing_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_projectclosing_create() OWNER TO obm;

--
-- Name: on_projectreftask_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_projectreftask_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.projectreftask_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_projectreftask_change() OWNER TO obm;

--
-- Name: on_projectreftask_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_projectreftask_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.projectreftask_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_projectreftask_create() OWNER TO obm;

--
-- Name: on_projecttask_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_projecttask_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.projecttask_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_projecttask_change() OWNER TO obm;

--
-- Name: on_projecttask_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_projecttask_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.projecttask_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_projecttask_create() OWNER TO obm;

--
-- Name: on_projectuser_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_projectuser_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.projectuser_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_projectuser_change() OWNER TO obm;

--
-- Name: on_projectuser_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_projectuser_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.projectuser_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_projectuser_create() OWNER TO obm;

--
-- Name: on_publication_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_publication_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.publication_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_publication_change() OWNER TO obm;

--
-- Name: on_publication_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_publication_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.publication_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_publication_create() OWNER TO obm;

--
-- Name: on_publicationtype_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_publicationtype_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.publicationtype_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_publicationtype_change() OWNER TO obm;

--
-- Name: on_publicationtype_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_publicationtype_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.publicationtype_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_publicationtype_create() OWNER TO obm;

--
-- Name: on_region_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_region_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.region_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_region_change() OWNER TO obm;

--
-- Name: on_region_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_region_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.region_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_region_create() OWNER TO obm;

--
-- Name: on_resource_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_resource_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.resource_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_resource_change() OWNER TO obm;

--
-- Name: on_resource_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_resource_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.resource_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_resource_create() OWNER TO obm;

--
-- Name: on_rgroup_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_rgroup_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.rgroup_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_rgroup_change() OWNER TO obm;

--
-- Name: on_rgroup_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_rgroup_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.rgroup_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_rgroup_create() OWNER TO obm;

--
-- Name: on_subscription_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_subscription_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.subscription_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_subscription_change() OWNER TO obm;

--
-- Name: on_subscription_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_subscription_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.subscription_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_subscription_create() OWNER TO obm;

--
-- Name: on_subscriptionreception_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_subscriptionreception_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.subscriptionreception_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_subscriptionreception_change() OWNER TO obm;

--
-- Name: on_subscriptionreception_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_subscriptionreception_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.subscriptionreception_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_subscriptionreception_create() OWNER TO obm;

--
-- Name: on_tasktype_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_tasktype_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.tasktype_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_tasktype_change() OWNER TO obm;

--
-- Name: on_tasktype_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_tasktype_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.tasktype_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_tasktype_create() OWNER TO obm;

--
-- Name: on_tasktypegroup_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_tasktypegroup_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.tasktypegroup_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_tasktypegroup_change() OWNER TO obm;

--
-- Name: on_tasktypegroup_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_tasktypegroup_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.tasktypegroup_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_tasktypegroup_create() OWNER TO obm;

--
-- Name: on_timetask_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_timetask_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.timetask_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_timetask_change() OWNER TO obm;

--
-- Name: on_timetask_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_timetask_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.timetask_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_timetask_create() OWNER TO obm;

--
-- Name: on_ugroup_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_ugroup_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.group_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_ugroup_change() OWNER TO obm;

--
-- Name: on_ugroup_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_ugroup_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.group_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_ugroup_create() OWNER TO obm;

--
-- Name: on_userobm_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_userobm_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
IF new.userobm_timelastaccess = old.userobm_timelastaccess THEN
	new.userobm_timeupdate := current_timestamp;
END IF;
RETURN new;
END
$$;


ALTER FUNCTION public.on_userobm_change() OWNER TO obm;

--
-- Name: on_userobm_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_userobm_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.userobm_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_userobm_create() OWNER TO obm;

--
-- Name: on_userobm_sessionlog_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_userobm_sessionlog_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.userobm_sessionlog_timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_userobm_sessionlog_change() OWNER TO obm;

--
-- Name: on_userobm_sessionlog_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_userobm_sessionlog_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.userobm_sessionlog_timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_userobm_sessionlog_create() OWNER TO obm;

--
-- Name: on_userpattern_change(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_userpattern_change() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.timeupdate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_userpattern_change() OWNER TO obm;

--
-- Name: on_userpattern_create(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION on_userpattern_create() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
new.timecreate := current_timestamp;
RETURN new;
END
$$;


ALTER FUNCTION public.on_userpattern_create() OWNER TO obm;

--
-- Name: uuid(); Type: FUNCTION; Schema: public; Owner: obm
--

CREATE FUNCTION uuid() RETURNS uuid
    LANGUAGE sql
    AS $$
 SELECT CAST(md5(current_database()|| user ||current_timestamp ||random()) as uuid)
$$;


ALTER FUNCTION public.uuid() OWNER TO obm;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: _contactgroup; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE _contactgroup (
    contact_id integer NOT NULL,
    group_id integer NOT NULL
);


ALTER TABLE public._contactgroup OWNER TO obm;

--
-- Name: _userpattern; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE _userpattern (
    id integer,
    pattern character varying(255)
);


ALTER TABLE public._userpattern OWNER TO obm;

--
-- Name: account; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE account (
    account_id integer NOT NULL,
    account_domain_id integer NOT NULL,
    account_timeupdate timestamp without time zone,
    account_timecreate timestamp without time zone DEFAULT now(),
    account_userupdate integer,
    account_usercreate integer,
    account_bank character varying(60) DEFAULT ''::character varying NOT NULL,
    account_number character varying(64) DEFAULT '0'::character varying NOT NULL,
    account_balance numeric(15,2) DEFAULT 0.00 NOT NULL,
    account_today numeric(15,2) DEFAULT 0.00 NOT NULL,
    account_comment character varying(100),
    account_label character varying(40) DEFAULT ''::character varying NOT NULL
);


ALTER TABLE public.account OWNER TO obm;

--
-- Name: account_account_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE account_account_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.account_account_id_seq OWNER TO obm;

--
-- Name: account_account_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE account_account_id_seq OWNED BY account.account_id;


--
-- Name: accountentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE accountentity (
    accountentity_entity_id integer NOT NULL,
    accountentity_account_id integer NOT NULL
);


ALTER TABLE public.accountentity OWNER TO obm;

--
-- Name: activeuserobm; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.activeuserobm OWNER TO obm;

--
-- Name: address; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE address (
    address_id integer NOT NULL,
    address_entity_id integer NOT NULL,
    address_street text,
    address_zipcode character varying(14),
    address_town character varying(128),
    address_expresspostal character varying(16),
    address_state character varying(128),
    address_country character(2),
    address_label character varying(255)
);


ALTER TABLE public.address OWNER TO obm;

--
-- Name: address_address_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE address_address_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.address_address_id_seq OWNER TO obm;

--
-- Name: address_address_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE address_address_id_seq OWNED BY address.address_id;


--
-- Name: addressbook; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE addressbook (
    id integer NOT NULL,
    domain_id integer NOT NULL,
    timeupdate timestamp without time zone,
    timecreate timestamp without time zone DEFAULT now(),
    userupdate integer,
    usercreate integer,
    origin character varying(255) NOT NULL,
    owner integer,
    name character varying(64) NOT NULL,
    is_default boolean DEFAULT false,
    syncable boolean DEFAULT true
);


ALTER TABLE public.addressbook OWNER TO obm;

--
-- Name: addressbook_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE addressbook_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.addressbook_id_seq OWNER TO obm;

--
-- Name: addressbook_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE addressbook_id_seq OWNED BY addressbook.id;


--
-- Name: addressbookentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE addressbookentity (
    addressbookentity_entity_id integer NOT NULL,
    addressbookentity_addressbook_id integer NOT NULL
);


ALTER TABLE public.addressbookentity OWNER TO obm;

--
-- Name: calendarcolor; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE calendarcolor (
    user_id integer NOT NULL,
    entity_id integer NOT NULL,
    eventowner integer
);


ALTER TABLE public.calendarcolor OWNER TO obm;

--
-- Name: calendarentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE calendarentity (
    calendarentity_entity_id integer NOT NULL,
    calendarentity_calendar_id integer NOT NULL
);


ALTER TABLE public.calendarentity OWNER TO obm;

--
-- Name: campaign; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE campaign (
    campaign_id integer NOT NULL,
    campaign_name character varying(50) DEFAULT NULL::character varying,
    campaign_timeupdate timestamp without time zone,
    campaign_timecreate timestamp without time zone DEFAULT now(),
    campaign_userupdate integer,
    campaign_usercreate integer,
    campaign_manager_id integer,
    campaign_tracker_key integer,
    campaign_refer_url character varying(255) DEFAULT NULL::character varying,
    campaign_nb_sent integer,
    campaign_nb_error integer,
    campaign_nb_inqueue integer,
    campaign_progress integer DEFAULT 0,
    campaign_start_date date,
    campaign_end_date date,
    campaign_status integer,
    campaign_type integer,
    campaign_objective text,
    campaign_comment text,
    campaign_domain_id integer NOT NULL,
    campaign_email integer,
    campaign_parent integer,
    campaign_child_order integer
);


ALTER TABLE public.campaign OWNER TO obm;

--
-- Name: campaign_campaign_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE campaign_campaign_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.campaign_campaign_id_seq OWNER TO obm;

--
-- Name: campaign_campaign_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE campaign_campaign_id_seq OWNED BY campaign.campaign_id;


--
-- Name: campaigndisabledentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE campaigndisabledentity (
    campaigndisabledentity_entity_id integer NOT NULL,
    campaigndisabledentity_campaign_id integer NOT NULL
);


ALTER TABLE public.campaigndisabledentity OWNER TO obm;

--
-- Name: campaignentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE campaignentity (
    campaignentity_entity_id integer NOT NULL,
    campaignentity_campaign_id integer NOT NULL
);


ALTER TABLE public.campaignentity OWNER TO obm;

--
-- Name: campaignmailcontent; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE campaignmailcontent (
    campaignmailcontent_id integer NOT NULL,
    campaignmailcontent_refext_id character varying(8),
    campaignmailcontent_content character varying
);


ALTER TABLE public.campaignmailcontent OWNER TO obm;

--
-- Name: campaignmailcontent_campaignmailcontent_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE campaignmailcontent_campaignmailcontent_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.campaignmailcontent_campaignmailcontent_id_seq OWNER TO obm;

--
-- Name: campaignmailcontent_campaignmailcontent_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE campaignmailcontent_campaignmailcontent_id_seq OWNED BY campaignmailcontent.campaignmailcontent_id;


--
-- Name: campaignmailtarget; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE campaignmailtarget (
    campaignmailtarget_id integer NOT NULL,
    campaignmailtarget_campaign_id integer NOT NULL,
    campaignmailtarget_entity_id integer,
    campaignmailtarget_status integer
);


ALTER TABLE public.campaignmailtarget OWNER TO obm;

--
-- Name: campaignmailtarget_campaignmailtarget_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE campaignmailtarget_campaignmailtarget_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.campaignmailtarget_campaignmailtarget_id_seq OWNER TO obm;

--
-- Name: campaignmailtarget_campaignmailtarget_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE campaignmailtarget_campaignmailtarget_id_seq OWNED BY campaignmailtarget.campaignmailtarget_id;


--
-- Name: campaignpushtarget; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE campaignpushtarget (
    campaignpushtarget_id integer NOT NULL,
    campaignpushtarget_mailcontent_id integer NOT NULL,
    campaignpushtarget_refext_id character varying(8),
    campaignpushtarget_status smallint DEFAULT 1 NOT NULL,
    campaignpushtarget_email_address character varying(512) NOT NULL,
    campaignpushtarget_properties text,
    campaignpushtarget_start_time timestamp without time zone,
    campaignpushtarget_sent_time timestamp without time zone,
    campaignpushtarget_retries smallint
);


ALTER TABLE public.campaignpushtarget OWNER TO obm;

--
-- Name: campaignpushtarget_campaignpushtarget_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE campaignpushtarget_campaignpushtarget_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.campaignpushtarget_campaignpushtarget_id_seq OWNER TO obm;

--
-- Name: campaignpushtarget_campaignpushtarget_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE campaignpushtarget_campaignpushtarget_id_seq OWNED BY campaignpushtarget.campaignpushtarget_id;


--
-- Name: campaigntarget; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE campaigntarget (
    campaigntarget_id integer NOT NULL,
    campaigntarget_campaign_id integer NOT NULL,
    campaigntarget_entity_id integer,
    campaigntarget_status integer
);


ALTER TABLE public.campaigntarget OWNER TO obm;

--
-- Name: campaigntarget_campaigntarget_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE campaigntarget_campaigntarget_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.campaigntarget_campaigntarget_id_seq OWNER TO obm;

--
-- Name: campaigntarget_campaigntarget_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE campaigntarget_campaigntarget_id_seq OWNED BY campaigntarget.campaigntarget_id;


--
-- Name: category; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE category (
    category_id integer NOT NULL,
    category_domain_id integer NOT NULL,
    category_timeupdate timestamp without time zone,
    category_timecreate timestamp without time zone DEFAULT now(),
    category_userupdate integer,
    category_usercreate integer,
    category_category character varying(24) DEFAULT ''::character varying NOT NULL,
    category_code character varying(100) DEFAULT ''::character varying NOT NULL,
    category_label character varying(100) DEFAULT ''::character varying NOT NULL
);


ALTER TABLE public.category OWNER TO obm;

--
-- Name: category_category_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE category_category_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.category_category_id_seq OWNER TO obm;

--
-- Name: category_category_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE category_category_id_seq OWNED BY category.category_id;


--
-- Name: categorylink; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE categorylink (
    categorylink_category_id integer NOT NULL,
    categorylink_entity_id integer NOT NULL,
    categorylink_category character varying(24) DEFAULT ''::character varying NOT NULL
);


ALTER TABLE public.categorylink OWNER TO obm;

--
-- Name: commitedoperation; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE commitedoperation (
    commitedoperation_hash_client_id character varying(44) NOT NULL,
    commitedoperation_entity_id integer NOT NULL,
    commitedoperation_kind vkind NOT NULL
);


ALTER TABLE public.commitedoperation OWNER TO obm;

--
-- Name: company; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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
    company_contact_number integer DEFAULT 0 NOT NULL,
    company_deal_number integer DEFAULT 0 NOT NULL,
    company_deal_total integer DEFAULT 0 NOT NULL,
    company_comment text
);


ALTER TABLE public.company OWNER TO obm;

--
-- Name: company_company_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE company_company_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.company_company_id_seq OWNER TO obm;

--
-- Name: company_company_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE company_company_id_seq OWNED BY company.company_id;


--
-- Name: companyactivity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.companyactivity OWNER TO obm;

--
-- Name: companyactivity_companyactivity_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE companyactivity_companyactivity_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.companyactivity_companyactivity_id_seq OWNER TO obm;

--
-- Name: companyactivity_companyactivity_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE companyactivity_companyactivity_id_seq OWNED BY companyactivity.companyactivity_id;


--
-- Name: companyentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE companyentity (
    companyentity_entity_id integer NOT NULL,
    companyentity_company_id integer NOT NULL
);


ALTER TABLE public.companyentity OWNER TO obm;

--
-- Name: companynafcode; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.companynafcode OWNER TO obm;

--
-- Name: companynafcode_companynafcode_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE companynafcode_companynafcode_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.companynafcode_companynafcode_id_seq OWNER TO obm;

--
-- Name: companynafcode_companynafcode_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE companynafcode_companynafcode_id_seq OWNED BY companynafcode.companynafcode_id;


--
-- Name: companytype; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.companytype OWNER TO obm;

--
-- Name: companytype_companytype_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE companytype_companytype_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.companytype_companytype_id_seq OWNER TO obm;

--
-- Name: companytype_companytype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE companytype_companytype_id_seq OWNED BY companytype.companytype_id;


--
-- Name: contact; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE contact (
    contact_id integer NOT NULL,
    contact_domain_id integer NOT NULL,
    contact_timeupdate timestamp without time zone,
    contact_timecreate timestamp without time zone DEFAULT now(),
    contact_userupdate integer,
    contact_usercreate integer,
    contact_datasource_id integer,
    contact_addressbook_id integer,
    contact_company_id integer,
    contact_company character varying(64),
    contact_kind_id integer,
    contact_marketingmanager_id integer,
    contact_commonname character varying(256) DEFAULT ''::character varying,
    contact_lastname character varying(64) DEFAULT NULL::character varying,
    contact_firstname character varying(64),
    contact_middlename character varying(32) DEFAULT NULL::character varying,
    contact_suffix character varying(16) DEFAULT NULL::character varying,
    contact_aka character varying(255),
    contact_sound character varying(48),
    contact_manager character varying(64),
    contact_assistant character varying(64),
    contact_spouse character varying(64),
    contact_category character varying(255),
    contact_service character varying(64),
    contact_function_id integer,
    contact_title character varying(64),
    contact_mailing_ok smallint DEFAULT 0,
    contact_newsletter smallint DEFAULT 0,
    contact_archive smallint DEFAULT 0 NOT NULL,
    contact_date timestamp without time zone,
    contact_birthday_id integer,
    contact_anniversary_id integer,
    contact_photo_id integer,
    contact_comment text,
    contact_comment2 text,
    contact_comment3 text,
    contact_collected boolean DEFAULT false,
    contact_origin character varying(255) NOT NULL
);


ALTER TABLE public.contact OWNER TO obm;

--
-- Name: contact_contact_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE contact_contact_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.contact_contact_id_seq OWNER TO obm;

--
-- Name: contact_contact_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE contact_contact_id_seq OWNED BY contact.contact_id;


--
-- Name: contactentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE contactentity (
    contactentity_entity_id integer NOT NULL,
    contactentity_contact_id integer NOT NULL
);


ALTER TABLE public.contactentity OWNER TO obm;

--
-- Name: contactfunction; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.contactfunction OWNER TO obm;

--
-- Name: contactfunction_contactfunction_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE contactfunction_contactfunction_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.contactfunction_contactfunction_id_seq OWNER TO obm;

--
-- Name: contactfunction_contactfunction_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE contactfunction_contactfunction_id_seq OWNED BY contactfunction.contactfunction_id;


--
-- Name: contactgroup; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE contactgroup (
    contact_id integer NOT NULL,
    group_id integer NOT NULL
);


ALTER TABLE public.contactgroup OWNER TO obm;

--
-- Name: contactlist; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE contactlist (
    contactlist_list_id integer NOT NULL,
    contactlist_contact_id integer NOT NULL
);


ALTER TABLE public.contactlist OWNER TO obm;

--
-- Name: contract; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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
    contract_archive smallint DEFAULT 0 NOT NULL,
    contract_clause text,
    contract_comment text
);


ALTER TABLE public.contract OWNER TO obm;

--
-- Name: contract_contract_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE contract_contract_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.contract_contract_id_seq OWNER TO obm;

--
-- Name: contract_contract_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE contract_contract_id_seq OWNED BY contract.contract_id;


--
-- Name: contractentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE contractentity (
    contractentity_entity_id integer NOT NULL,
    contractentity_contract_id integer NOT NULL
);


ALTER TABLE public.contractentity OWNER TO obm;

--
-- Name: contractpriority; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.contractpriority OWNER TO obm;

--
-- Name: contractpriority_contractpriority_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE contractpriority_contractpriority_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.contractpriority_contractpriority_id_seq OWNER TO obm;

--
-- Name: contractpriority_contractpriority_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE contractpriority_contractpriority_id_seq OWNED BY contractpriority.contractpriority_id;


--
-- Name: contractstatus; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.contractstatus OWNER TO obm;

--
-- Name: contractstatus_contractstatus_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE contractstatus_contractstatus_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.contractstatus_contractstatus_id_seq OWNER TO obm;

--
-- Name: contractstatus_contractstatus_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE contractstatus_contractstatus_id_seq OWNED BY contractstatus.contractstatus_id;


--
-- Name: contracttype; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.contracttype OWNER TO obm;

--
-- Name: contracttype_contracttype_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE contracttype_contracttype_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.contracttype_contracttype_id_seq OWNER TO obm;

--
-- Name: contracttype_contracttype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE contracttype_contracttype_id_seq OWNED BY contracttype.contracttype_id;


--
-- Name: country; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.country OWNER TO obm;

--
-- Name: cv; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.cv OWNER TO obm;

--
-- Name: cv_cv_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE cv_cv_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.cv_cv_id_seq OWNER TO obm;

--
-- Name: cv_cv_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE cv_cv_id_seq OWNED BY cv.cv_id;


--
-- Name: cventity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE cventity (
    cventity_entity_id integer NOT NULL,
    cventity_cv_id integer NOT NULL
);


ALTER TABLE public.cventity OWNER TO obm;

--
-- Name: datasource; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.datasource OWNER TO obm;

--
-- Name: datasource_datasource_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE datasource_datasource_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.datasource_datasource_id_seq OWNER TO obm;

--
-- Name: datasource_datasource_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE datasource_datasource_id_seq OWNED BY datasource.datasource_id;


--
-- Name: deal; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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
    deal_hitrate integer DEFAULT 0 NOT NULL,
    deal_status_id integer,
    deal_archive smallint DEFAULT 0 NOT NULL,
    deal_todo character varying(128),
    deal_privacy integer DEFAULT 0,
    deal_comment text
);


ALTER TABLE public.deal OWNER TO obm;

--
-- Name: deal_deal_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE deal_deal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.deal_deal_id_seq OWNER TO obm;

--
-- Name: deal_deal_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE deal_deal_id_seq OWNED BY deal.deal_id;


--
-- Name: dealcompany; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE dealcompany (
    dealcompany_id integer NOT NULL,
    dealcompany_timeupdate timestamp without time zone,
    dealcompany_timecreate timestamp without time zone DEFAULT now(),
    dealcompany_userupdate integer,
    dealcompany_usercreate integer,
    dealcompany_deal_id integer NOT NULL,
    dealcompany_company_id integer NOT NULL,
    dealcompany_role_id integer
);


ALTER TABLE public.dealcompany OWNER TO obm;

--
-- Name: dealcompany_dealcompany_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE dealcompany_dealcompany_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.dealcompany_dealcompany_id_seq OWNER TO obm;

--
-- Name: dealcompany_dealcompany_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE dealcompany_dealcompany_id_seq OWNED BY dealcompany.dealcompany_id;


--
-- Name: dealcompanyrole; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE dealcompanyrole (
    dealcompanyrole_id integer NOT NULL,
    dealcompanyrole_domain_id integer NOT NULL,
    dealcompanyrole_timeupdate timestamp without time zone,
    dealcompanyrole_timecreate timestamp without time zone DEFAULT now(),
    dealcompanyrole_userupdate integer,
    dealcompanyrole_usercreate integer,
    dealcompanyrole_code character varying(10) DEFAULT ''::character varying,
    dealcompanyrole_label character varying(64) DEFAULT ''::character varying NOT NULL
);


ALTER TABLE public.dealcompanyrole OWNER TO obm;

--
-- Name: dealcompanyrole_dealcompanyrole_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE dealcompanyrole_dealcompanyrole_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.dealcompanyrole_dealcompanyrole_id_seq OWNER TO obm;

--
-- Name: dealcompanyrole_dealcompanyrole_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE dealcompanyrole_dealcompanyrole_id_seq OWNED BY dealcompanyrole.dealcompanyrole_id;


--
-- Name: dealentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE dealentity (
    dealentity_entity_id integer NOT NULL,
    dealentity_deal_id integer NOT NULL
);


ALTER TABLE public.dealentity OWNER TO obm;

--
-- Name: dealstatus; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.dealstatus OWNER TO obm;

--
-- Name: dealstatus_dealstatus_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE dealstatus_dealstatus_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.dealstatus_dealstatus_id_seq OWNER TO obm;

--
-- Name: dealstatus_dealstatus_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE dealstatus_dealstatus_id_seq OWNED BY dealstatus.dealstatus_id;


--
-- Name: dealtype; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.dealtype OWNER TO obm;

--
-- Name: dealtype_dealtype_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE dealtype_dealtype_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.dealtype_dealtype_id_seq OWNER TO obm;

--
-- Name: dealtype_dealtype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE dealtype_dealtype_id_seq OWNED BY dealtype.dealtype_id;


--
-- Name: defaultodttemplate; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE defaultodttemplate (
    defaultodttemplate_id integer NOT NULL,
    defaultodttemplate_domain_id integer NOT NULL,
    defaultodttemplate_entity character varying(32),
    defaultodttemplate_document_id integer NOT NULL,
    defaultodttemplate_label character varying(64) DEFAULT ''::character varying
);


ALTER TABLE public.defaultodttemplate OWNER TO obm;

--
-- Name: defaultodttemplate_defaultodttemplate_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE defaultodttemplate_defaultodttemplate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.defaultodttemplate_defaultodttemplate_id_seq OWNER TO obm;

--
-- Name: defaultodttemplate_defaultodttemplate_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE defaultodttemplate_defaultodttemplate_id_seq OWNED BY defaultodttemplate.defaultodttemplate_id;


--
-- Name: deleted; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE deleted (
    deleted_id integer NOT NULL,
    deleted_domain_id integer,
    deleted_user_id integer,
    deleted_delegation character varying(256) DEFAULT ''::character varying,
    deleted_table character varying(32),
    deleted_entity_id integer,
    deleted_timestamp timestamp without time zone
);


ALTER TABLE public.deleted OWNER TO obm;

--
-- Name: deleted_deleted_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE deleted_deleted_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.deleted_deleted_id_seq OWNER TO obm;

--
-- Name: deleted_deleted_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE deleted_deleted_id_seq OWNED BY deleted.deleted_id;


--
-- Name: deletedaddressbook; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE deletedaddressbook (
    addressbook_id integer NOT NULL,
    user_id integer NOT NULL,
    "timestamp" timestamp without time zone,
    origin character varying(255) NOT NULL
);


ALTER TABLE public.deletedaddressbook OWNER TO obm;

--
-- Name: deletedcontact; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE deletedcontact (
    deletedcontact_contact_id integer NOT NULL,
    deletedcontact_addressbook_id integer,
    deletedcontact_timestamp timestamp without time zone,
    deletedcontact_origin character varying(255) NOT NULL
);


ALTER TABLE public.deletedcontact OWNER TO obm;

--
-- Name: deletedevent; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE deletedevent (
    deletedevent_id integer NOT NULL,
    deletedevent_event_id integer,
    deletedevent_event_ext_id character varying(300) DEFAULT ''::character varying,
    deletedevent_user_id integer,
    deletedevent_origin character varying(255) NOT NULL,
    deletedevent_type vcomponent DEFAULT 'VEVENT'::vcomponent,
    deletedevent_timestamp timestamp without time zone
);


ALTER TABLE public.deletedevent OWNER TO obm;

--
-- Name: deletedevent_deletedevent_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE deletedevent_deletedevent_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.deletedevent_deletedevent_id_seq OWNER TO obm;

--
-- Name: deletedevent_deletedevent_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE deletedevent_deletedevent_id_seq OWNED BY deletedevent.deletedevent_id;


--
-- Name: deletedeventlink_deletedeventlink_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE deletedeventlink_deletedeventlink_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.deletedeventlink_deletedeventlink_id_seq OWNER TO obm;

--
-- Name: deletedeventlink; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE deletedeventlink (
    deletedeventlink_id integer DEFAULT nextval('deletedeventlink_deletedeventlink_id_seq'::regclass) NOT NULL,
    deletedeventlink_userobm_id integer NOT NULL,
    deletedeventlink_event_id integer NOT NULL,
    deletedeventlink_event_ext_id character varying(300) NOT NULL,
    deletedeventlink_time_removed timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.deletedeventlink OWNER TO obm;

--
-- Name: deletedsyncedaddressbook; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE deletedsyncedaddressbook (
    user_id integer NOT NULL,
    addressbook_id integer NOT NULL,
    "timestamp" timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.deletedsyncedaddressbook OWNER TO obm;

--
-- Name: deleteduser; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE deleteduser (
    deleteduser_user_id integer NOT NULL,
    deleteduser_timestamp timestamp without time zone
);


ALTER TABLE public.deleteduser OWNER TO obm;

--
-- Name: displaypref; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE displaypref (
    display_id integer NOT NULL,
    display_user_id integer,
    display_entity character varying(32) DEFAULT ''::character varying NOT NULL,
    display_fieldname character varying(64) DEFAULT ''::character varying NOT NULL,
    display_fieldorder integer,
    display_display integer DEFAULT 1 NOT NULL
);


ALTER TABLE public.displaypref OWNER TO obm;

--
-- Name: displaypref_display_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE displaypref_display_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.displaypref_display_id_seq OWNER TO obm;

--
-- Name: displaypref_display_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE displaypref_display_id_seq OWNED BY displaypref.display_id;


--
-- Name: document; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.document OWNER TO obm;

--
-- Name: document_document_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE document_document_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.document_document_id_seq OWNER TO obm;

--
-- Name: document_document_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE document_document_id_seq OWNED BY document.document_id;


--
-- Name: documententity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE documententity (
    documententity_entity_id integer NOT NULL,
    documententity_document_id integer NOT NULL
);


ALTER TABLE public.documententity OWNER TO obm;

--
-- Name: documentlink; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE documentlink (
    documentlink_document_id integer NOT NULL,
    documentlink_entity_id integer NOT NULL,
    documentlink_usercreate integer
);


ALTER TABLE public.documentlink OWNER TO obm;

--
-- Name: documentmimetype; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.documentmimetype OWNER TO obm;

--
-- Name: documentmimetype_documentmimetype_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE documentmimetype_documentmimetype_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.documentmimetype_documentmimetype_id_seq OWNER TO obm;

--
-- Name: documentmimetype_documentmimetype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE documentmimetype_documentmimetype_id_seq OWNED BY documentmimetype.documentmimetype_id;


--
-- Name: domain; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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
    domain_global boolean DEFAULT false,
    domain_uuid character(36) NOT NULL
);


ALTER TABLE public.domain OWNER TO obm;

--
-- Name: domain_domain_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE domain_domain_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.domain_domain_id_seq OWNER TO obm;

--
-- Name: domain_domain_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE domain_domain_id_seq OWNED BY domain.domain_id;


--
-- Name: domainentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE domainentity (
    domainentity_entity_id integer NOT NULL,
    domainentity_domain_id integer NOT NULL
);


ALTER TABLE public.domainentity OWNER TO obm;

--
-- Name: domainproperty; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE domainproperty (
    domainproperty_key character varying(255) NOT NULL,
    domainproperty_type character varying(32),
    domainproperty_default character varying(64),
    domainproperty_readonly integer DEFAULT 0
);


ALTER TABLE public.domainproperty OWNER TO obm;

--
-- Name: domainpropertyvalue; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE domainpropertyvalue (
    domainpropertyvalue_domain_id integer NOT NULL,
    domainpropertyvalue_property_key character varying(255) NOT NULL,
    domainpropertyvalue_value character varying(255) NOT NULL
);


ALTER TABLE public.domainpropertyvalue OWNER TO obm;

--
-- Name: email; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE email (
    email_id integer NOT NULL,
    email_entity_id integer NOT NULL,
    email_label character varying(255) NOT NULL,
    email_address character varying(255)
);


ALTER TABLE public.email OWNER TO obm;

--
-- Name: email_email_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE email_email_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.email_email_id_seq OWNER TO obm;

--
-- Name: email_email_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE email_email_id_seq OWNED BY email.email_id;


--
-- Name: entity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE entity (
    entity_id integer NOT NULL,
    entity_mailing boolean
);


ALTER TABLE public.entity OWNER TO obm;

--
-- Name: entity_entity_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE entity_entity_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.entity_entity_id_seq OWNER TO obm;

--
-- Name: entity_entity_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE entity_entity_id_seq OWNED BY entity.entity_id;


--
-- Name: entityright; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE entityright (
    entityright_id integer NOT NULL,
    entityright_entity_id integer NOT NULL,
    entityright_consumer_id integer,
    entityright_access integer DEFAULT 0 NOT NULL,
    entityright_read integer DEFAULT 0 NOT NULL,
    entityright_write integer DEFAULT 0 NOT NULL,
    entityright_admin integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.entityright OWNER TO obm;

--
-- Name: entityright_entityright_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE entityright_entityright_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.entityright_entityright_id_seq OWNER TO obm;

--
-- Name: entityright_entityright_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE entityright_entityright_id_seq OWNED BY entityright.entityright_id;


--
-- Name: event; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE event (
    event_id integer NOT NULL,
    event_domain_id integer NOT NULL,
    event_timeupdate timestamp without time zone,
    event_timecreate timestamp without time zone DEFAULT now(),
    event_userupdate integer,
    event_usercreate integer,
    event_ext_id character varying(300) NOT NULL,
    event_type vcomponent DEFAULT 'VEVENT'::vcomponent,
    event_origin character varying(255) DEFAULT ''::character varying NOT NULL,
    event_owner integer,
    event_timezone character varying(255) DEFAULT 'GMT'::character varying,
    event_opacity vopacity DEFAULT 'OPAQUE'::vopacity,
    event_title character varying(255) DEFAULT NULL::character varying,
    event_location character varying(255) DEFAULT NULL::character varying,
    event_category1_id integer,
    event_priority integer,
    event_privacy integer DEFAULT 0 NOT NULL,
    event_date timestamp without time zone,
    event_duration integer DEFAULT 0 NOT NULL,
    event_allday boolean DEFAULT false,
    event_repeatkind character varying(20) DEFAULT 'none'::character varying NOT NULL,
    event_repeatfrequence integer,
    event_repeatdays character varying(7) DEFAULT NULL::character varying,
    event_endrepeat timestamp without time zone,
    event_color character varying(7),
    event_completed timestamp without time zone,
    event_url text,
    event_allow_documents boolean DEFAULT false,
    event_description text,
    event_properties text,
    event_tag_id integer,
    event_sequence integer DEFAULT 0,
    CONSTRAINT duration_check CHECK ((event_duration >= 0))
);


ALTER TABLE public.event OWNER TO obm;

--
-- Name: event_event_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE event_event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.event_event_id_seq OWNER TO obm;

--
-- Name: event_event_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE event_event_id_seq OWNED BY event.event_id;


--
-- Name: eventalert; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.eventalert OWNER TO obm;

--
-- Name: eventcategory1; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.eventcategory1 OWNER TO obm;

--
-- Name: eventcategory1_eventcategory1_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE eventcategory1_eventcategory1_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.eventcategory1_eventcategory1_id_seq OWNER TO obm;

--
-- Name: eventcategory1_eventcategory1_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE eventcategory1_eventcategory1_id_seq OWNED BY eventcategory1.eventcategory1_id;


--
-- Name: evententity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE evententity (
    evententity_entity_id integer NOT NULL,
    evententity_event_id integer NOT NULL
);


ALTER TABLE public.evententity OWNER TO obm;

--
-- Name: eventexception; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE eventexception (
    eventexception_timeupdate timestamp without time zone,
    eventexception_timecreate timestamp without time zone DEFAULT now(),
    eventexception_userupdate integer,
    eventexception_usercreate integer,
    eventexception_parent_id integer NOT NULL,
    eventexception_child_id integer,
    eventexception_date timestamp without time zone NOT NULL
);


ALTER TABLE public.eventexception OWNER TO obm;

--
-- Name: eventlink; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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
    eventlink_percent double precision DEFAULT 0,
    eventlink_is_organizer boolean DEFAULT false,
    eventlink_comment character varying(255)
);


ALTER TABLE public.eventlink OWNER TO obm;

--
-- Name: eventtag; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE eventtag (
    eventtag_id integer NOT NULL,
    eventtag_user_id integer NOT NULL,
    eventtag_label character varying(128) DEFAULT ''::character varying,
    eventtag_color character(7) DEFAULT NULL::bpchar
);


ALTER TABLE public.eventtag OWNER TO obm;

--
-- Name: eventtag_eventtag_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE eventtag_eventtag_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.eventtag_eventtag_id_seq OWNER TO obm;

--
-- Name: eventtag_eventtag_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE eventtag_eventtag_id_seq OWNED BY eventtag.eventtag_id;


--
-- Name: eventtemplate; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE eventtemplate (
    eventtemplate_id integer NOT NULL,
    eventtemplate_domain_id integer NOT NULL,
    eventtemplate_timeupdate timestamp without time zone,
    eventtemplate_timecreate timestamp without time zone DEFAULT now(),
    eventtemplate_userupdate integer,
    eventtemplate_usercreate integer,
    eventtemplate_owner integer,
    eventtemplate_name character varying(255) DEFAULT NULL::character varying,
    eventtemplate_title character varying(255) DEFAULT NULL::character varying,
    eventtemplate_location character varying(100) DEFAULT NULL::character varying,
    eventtemplate_category1_id integer,
    eventtemplate_priority integer,
    eventtemplate_privacy integer,
    eventtemplate_date timestamp without time zone,
    eventtemplate_duration integer DEFAULT 0 NOT NULL,
    eventtemplate_allday boolean DEFAULT false,
    eventtemplate_repeatkind character varying(20) DEFAULT 'none'::character varying NOT NULL,
    eventtemplate_repeatfrequence integer,
    eventtemplate_repeatdays character varying(7) DEFAULT NULL::character varying,
    eventtemplate_endrepeat timestamp without time zone,
    eventtemplate_allow_documents boolean DEFAULT false,
    eventtemplate_alert integer DEFAULT 0 NOT NULL,
    eventtemplate_description text,
    eventtemplate_properties text,
    eventtemplate_tag_id integer,
    eventtemplate_user_ids text,
    eventtemplate_contact_ids text,
    eventtemplate_resource_ids text,
    eventtemplate_document_ids text,
    eventtemplate_organizer integer DEFAULT 0,
    eventtemplate_group_ids text,
    eventtemplate_force_insertion boolean DEFAULT false,
    eventtemplate_opacity vopacity DEFAULT 'OPAQUE'::vopacity,
    eventtemplate_show_user_calendar boolean DEFAULT false,
    eventtemplate_show_resource_calendar boolean DEFAULT false
);


ALTER TABLE public.eventtemplate OWNER TO obm;

--
-- Name: eventtemplate_eventtemplate_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE eventtemplate_eventtemplate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.eventtemplate_eventtemplate_id_seq OWNER TO obm;

--
-- Name: eventtemplate_eventtemplate_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE eventtemplate_eventtemplate_id_seq OWNED BY eventtemplate.eventtemplate_id;


--
-- Name: field; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE field (
    id integer NOT NULL,
    entity_id integer NOT NULL,
    field character varying(255),
    value text
);


ALTER TABLE public.field OWNER TO obm;

--
-- Name: field_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE field_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.field_id_seq OWNER TO obm;

--
-- Name: field_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE field_id_seq OWNED BY field.id;


--
-- Name: groupentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE groupentity (
    groupentity_entity_id integer NOT NULL,
    groupentity_group_id integer NOT NULL
);


ALTER TABLE public.groupentity OWNER TO obm;

--
-- Name: groupgroup; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE groupgroup (
    groupgroup_parent_id integer NOT NULL,
    groupgroup_child_id integer NOT NULL
);


ALTER TABLE public.groupgroup OWNER TO obm;

--
-- Name: host; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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
    host_archive smallint DEFAULT 0 NOT NULL,
    host_name character varying(32) NOT NULL,
    host_fqdn character varying(255),
    host_ip character varying(16),
    host_delegation character varying(256) DEFAULT ''::character varying,
    host_description character varying(128)
);


ALTER TABLE public.host OWNER TO obm;

--
-- Name: host_host_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE host_host_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.host_host_id_seq OWNER TO obm;

--
-- Name: host_host_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE host_host_id_seq OWNED BY host.host_id;


--
-- Name: hostentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE hostentity (
    hostentity_entity_id integer NOT NULL,
    hostentity_host_id integer NOT NULL
);


ALTER TABLE public.hostentity OWNER TO obm;

--
-- Name: im; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE im (
    im_id integer NOT NULL,
    im_entity_id integer NOT NULL,
    im_label character varying(255),
    im_address character varying(255),
    im_protocol character varying(255)
);


ALTER TABLE public.im OWNER TO obm;

--
-- Name: im_im_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE im_im_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.im_im_id_seq OWNER TO obm;

--
-- Name: im_im_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE im_im_id_seq OWNED BY im.im_id;


--
-- Name: import; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.import OWNER TO obm;

--
-- Name: import_import_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE import_import_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.import_import_id_seq OWNER TO obm;

--
-- Name: import_import_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE import_import_id_seq OWNED BY import.import_id;


--
-- Name: importentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE importentity (
    importentity_entity_id integer NOT NULL,
    importentity_import_id integer NOT NULL
);


ALTER TABLE public.importentity OWNER TO obm;

--
-- Name: incident; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.incident OWNER TO obm;

--
-- Name: incident_incident_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE incident_incident_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.incident_incident_id_seq OWNER TO obm;

--
-- Name: incident_incident_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE incident_incident_id_seq OWNED BY incident.incident_id;


--
-- Name: incidententity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE incidententity (
    incidententity_entity_id integer NOT NULL,
    incidententity_incident_id integer NOT NULL
);


ALTER TABLE public.incidententity OWNER TO obm;

--
-- Name: incidentpriority; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.incidentpriority OWNER TO obm;

--
-- Name: incidentpriority_incidentpriority_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE incidentpriority_incidentpriority_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.incidentpriority_incidentpriority_id_seq OWNER TO obm;

--
-- Name: incidentpriority_incidentpriority_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE incidentpriority_incidentpriority_id_seq OWNED BY incidentpriority.incidentpriority_id;


--
-- Name: incidentresolutiontype; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.incidentresolutiontype OWNER TO obm;

--
-- Name: incidentresolutiontype_incidentresolutiontype_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE incidentresolutiontype_incidentresolutiontype_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.incidentresolutiontype_incidentresolutiontype_id_seq OWNER TO obm;

--
-- Name: incidentresolutiontype_incidentresolutiontype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE incidentresolutiontype_incidentresolutiontype_id_seq OWNED BY incidentresolutiontype.incidentresolutiontype_id;


--
-- Name: incidentstatus; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.incidentstatus OWNER TO obm;

--
-- Name: incidentstatus_incidentstatus_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE incidentstatus_incidentstatus_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.incidentstatus_incidentstatus_id_seq OWNER TO obm;

--
-- Name: incidentstatus_incidentstatus_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE incidentstatus_incidentstatus_id_seq OWNED BY incidentstatus.incidentstatus_id;


--
-- Name: invoice; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.invoice OWNER TO obm;

--
-- Name: invoice_invoice_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE invoice_invoice_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.invoice_invoice_id_seq OWNER TO obm;

--
-- Name: invoice_invoice_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE invoice_invoice_id_seq OWNED BY invoice.invoice_id;


--
-- Name: invoiceentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE invoiceentity (
    invoiceentity_entity_id integer NOT NULL,
    invoiceentity_invoice_id integer NOT NULL
);


ALTER TABLE public.invoiceentity OWNER TO obm;

--
-- Name: kind; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.kind OWNER TO obm;

--
-- Name: kind_kind_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE kind_kind_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.kind_kind_id_seq OWNER TO obm;

--
-- Name: kind_kind_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE kind_kind_id_seq OWNED BY kind.kind_id;


--
-- Name: lead; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE lead (
    lead_id integer NOT NULL,
    lead_domain_id integer NOT NULL,
    lead_timeupdate timestamp without time zone,
    lead_timecreate timestamp without time zone DEFAULT now(),
    lead_userupdate integer,
    lead_usercreate integer,
    lead_source_id integer,
    lead_manager_id integer,
    lead_company_id integer NOT NULL,
    lead_contact_id integer,
    lead_privacy integer DEFAULT 0,
    lead_priority integer DEFAULT 0,
    lead_name character varying(64),
    lead_date date,
    lead_datealarm date,
    lead_status_id integer,
    lead_archive smallint DEFAULT 0 NOT NULL,
    lead_todo character varying(128),
    lead_comment text
);


ALTER TABLE public.lead OWNER TO obm;

--
-- Name: lead_lead_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE lead_lead_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.lead_lead_id_seq OWNER TO obm;

--
-- Name: lead_lead_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE lead_lead_id_seq OWNED BY lead.lead_id;


--
-- Name: leadentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE leadentity (
    leadentity_entity_id integer NOT NULL,
    leadentity_lead_id integer NOT NULL
);


ALTER TABLE public.leadentity OWNER TO obm;

--
-- Name: leadsource; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE leadsource (
    leadsource_id integer NOT NULL,
    leadsource_domain_id integer NOT NULL,
    leadsource_timeupdate timestamp without time zone,
    leadsource_timecreate timestamp without time zone DEFAULT now(),
    leadsource_userupdate integer,
    leadsource_usercreate integer,
    leadsource_code character varying(10) DEFAULT ''::character varying,
    leadsource_label character varying(100) DEFAULT ''::character varying NOT NULL
);


ALTER TABLE public.leadsource OWNER TO obm;

--
-- Name: leadsource_leadsource_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE leadsource_leadsource_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.leadsource_leadsource_id_seq OWNER TO obm;

--
-- Name: leadsource_leadsource_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE leadsource_leadsource_id_seq OWNED BY leadsource.leadsource_id;


--
-- Name: leadstatus; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.leadstatus OWNER TO obm;

--
-- Name: leadstatus_leadstatus_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE leadstatus_leadstatus_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.leadstatus_leadstatus_id_seq OWNER TO obm;

--
-- Name: leadstatus_leadstatus_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE leadstatus_leadstatus_id_seq OWNED BY leadstatus.leadstatus_id;


--
-- Name: list; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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
    list_contact_archive smallint DEFAULT 0 NOT NULL,
    list_info_publication integer DEFAULT 0,
    list_static_nb integer DEFAULT 0,
    list_query_nb integer DEFAULT 0,
    list_query text,
    list_structure text
);


ALTER TABLE public.list OWNER TO obm;

--
-- Name: list_list_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE list_list_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.list_list_id_seq OWNER TO obm;

--
-- Name: list_list_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE list_list_id_seq OWNED BY list.list_id;


--
-- Name: listentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE listentity (
    listentity_entity_id integer NOT NULL,
    listentity_list_id integer NOT NULL
);


ALTER TABLE public.listentity OWNER TO obm;

--
-- Name: logmodifiedaddress; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE logmodifiedaddress (
    contact_id integer,
    contact_firstname character varying(64),
    contact_lastname character varying(64),
    address_street text,
    address_zipcode character varying(14),
    address_town character varying(128),
    address_expresspostal character varying(16),
    address_state character varying(128),
    address_country character(2),
    address_label character varying(255),
    address_entity_id integer,
    contactentity_contact_id integer
);


ALTER TABLE public.logmodifiedaddress OWNER TO obm;

--
-- Name: mailboxentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE mailboxentity (
    mailboxentity_entity_id integer NOT NULL,
    mailboxentity_mailbox_id integer NOT NULL
);


ALTER TABLE public.mailboxentity OWNER TO obm;

--
-- Name: mailinglist; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE mailinglist (
    mailinglist_id integer NOT NULL,
    mailinglist_domain_id integer NOT NULL,
    mailinglist_timeupdate timestamp without time zone,
    mailinglist_timecreate timestamp without time zone DEFAULT now(),
    mailinglist_userupdate integer,
    mailinglist_usercreate integer,
    mailinglist_owner integer NOT NULL,
    mailinglist_name character varying(64) NOT NULL
);


ALTER TABLE public.mailinglist OWNER TO obm;

--
-- Name: mailinglist_mailinglist_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE mailinglist_mailinglist_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.mailinglist_mailinglist_id_seq OWNER TO obm;

--
-- Name: mailinglist_mailinglist_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE mailinglist_mailinglist_id_seq OWNED BY mailinglist.mailinglist_id;


--
-- Name: mailinglistemail; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE mailinglistemail (
    mailinglistemail_id integer NOT NULL,
    mailinglistemail_mailinglist_id integer NOT NULL,
    mailinglistemail_label character varying(255) NOT NULL,
    mailinglistemail_address character varying(255) NOT NULL
);


ALTER TABLE public.mailinglistemail OWNER TO obm;

--
-- Name: mailinglistemail_mailinglistemail_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE mailinglistemail_mailinglistemail_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.mailinglistemail_mailinglistemail_id_seq OWNER TO obm;

--
-- Name: mailinglistemail_mailinglistemail_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE mailinglistemail_mailinglistemail_id_seq OWNED BY mailinglistemail.mailinglistemail_id;


--
-- Name: mailshare; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE mailshare (
    mailshare_id integer NOT NULL,
    mailshare_domain_id integer NOT NULL,
    mailshare_timeupdate timestamp without time zone,
    mailshare_timecreate timestamp without time zone DEFAULT now(),
    mailshare_userupdate integer,
    mailshare_usercreate integer,
    mailshare_name character varying(32),
    mailshare_archive smallint DEFAULT 0 NOT NULL,
    mailshare_quota character varying(8) DEFAULT '0'::character varying NOT NULL,
    mailshare_mail_server_id integer,
    mailshare_delegation character varying(256) DEFAULT ''::character varying,
    mailshare_description character varying(255),
    mailshare_email text
);


ALTER TABLE public.mailshare OWNER TO obm;

--
-- Name: mailshare_mailshare_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE mailshare_mailshare_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.mailshare_mailshare_id_seq OWNER TO obm;

--
-- Name: mailshare_mailshare_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE mailshare_mailshare_id_seq OWNED BY mailshare.mailshare_id;


--
-- Name: mailshareentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE mailshareentity (
    mailshareentity_entity_id integer NOT NULL,
    mailshareentity_mailshare_id integer NOT NULL
);


ALTER TABLE public.mailshareentity OWNER TO obm;

--
-- Name: obmbookmark; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE obmbookmark (
    obmbookmark_id integer NOT NULL,
    obmbookmark_user_id integer NOT NULL,
    obmbookmark_label character varying(48) DEFAULT ''::character varying NOT NULL,
    obmbookmark_entity character varying(24) DEFAULT ''::character varying NOT NULL
);


ALTER TABLE public.obmbookmark OWNER TO obm;

--
-- Name: obmbookmark_obmbookmark_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE obmbookmark_obmbookmark_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.obmbookmark_obmbookmark_id_seq OWNER TO obm;

--
-- Name: obmbookmark_obmbookmark_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE obmbookmark_obmbookmark_id_seq OWNED BY obmbookmark.obmbookmark_id;


--
-- Name: obmbookmarkentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE obmbookmarkentity (
    obmbookmarkentity_entity_id integer NOT NULL,
    obmbookmarkentity_obmbookmark_id integer NOT NULL
);


ALTER TABLE public.obmbookmarkentity OWNER TO obm;

--
-- Name: obmbookmarkproperty; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE obmbookmarkproperty (
    obmbookmarkproperty_id integer NOT NULL,
    obmbookmarkproperty_bookmark_id integer NOT NULL,
    obmbookmarkproperty_property character varying(255) DEFAULT ''::character varying NOT NULL,
    obmbookmarkproperty_value character varying(256) DEFAULT ''::character varying NOT NULL
);


ALTER TABLE public.obmbookmarkproperty OWNER TO obm;

--
-- Name: obmbookmarkproperty_obmbookmarkproperty_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE obmbookmarkproperty_obmbookmarkproperty_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.obmbookmarkproperty_obmbookmarkproperty_id_seq OWNER TO obm;

--
-- Name: obmbookmarkproperty_obmbookmarkproperty_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE obmbookmarkproperty_obmbookmarkproperty_id_seq OWNED BY obmbookmarkproperty.obmbookmarkproperty_id;


--
-- Name: obminfo; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE obminfo (
    obminfo_name character varying(32) DEFAULT ''::character varying NOT NULL,
    obminfo_value character varying(255) DEFAULT ''::character varying
);


ALTER TABLE public.obminfo OWNER TO obm;

--
-- Name: obmsession; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE obmsession (
    obmsession_sid character varying(32) DEFAULT ''::character varying NOT NULL,
    obmsession_timeupdate timestamp without time zone,
    obmsession_name character varying(32) DEFAULT ''::character varying NOT NULL,
    obmsession_data text
);


ALTER TABLE public.obmsession OWNER TO obm;

--
-- Name: of_usergroup; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE of_usergroup (
    of_usergroup_group_id integer NOT NULL,
    of_usergroup_user_id integer NOT NULL
);


ALTER TABLE public.of_usergroup OWNER TO obm;

--
-- Name: ogroup; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.ogroup OWNER TO obm;

--
-- Name: ogroup_ogroup_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE ogroup_ogroup_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.ogroup_ogroup_id_seq OWNER TO obm;

--
-- Name: ogroup_ogroup_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE ogroup_ogroup_id_seq OWNED BY ogroup.ogroup_id;


--
-- Name: ogroupentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE ogroupentity (
    ogroupentity_entity_id integer NOT NULL,
    ogroupentity_ogroup_id integer NOT NULL
);


ALTER TABLE public.ogroupentity OWNER TO obm;

--
-- Name: ogrouplink; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.ogrouplink OWNER TO obm;

--
-- Name: ogrouplink_ogrouplink_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE ogrouplink_ogrouplink_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.ogrouplink_ogrouplink_id_seq OWNER TO obm;

--
-- Name: ogrouplink_ogrouplink_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE ogrouplink_ogrouplink_id_seq OWNED BY ogrouplink.ogrouplink_id;


--
-- Name: opush_device; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE opush_device (
    id integer NOT NULL,
    identifier character varying(255) NOT NULL,
    owner integer,
    type character varying(64) NOT NULL
);


ALTER TABLE public.opush_device OWNER TO obm;

--
-- Name: opush_device_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE opush_device_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.opush_device_id_seq OWNER TO obm;

--
-- Name: opush_device_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE opush_device_id_seq OWNED BY opush_device.id;


--
-- Name: opush_event_mapping; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE opush_event_mapping (
    id integer NOT NULL,
    device_id integer NOT NULL,
    event_uid character varying(300) NOT NULL,
    event_ext_id character varying(300) NOT NULL,
    event_ext_id_hash bytea NOT NULL
);


ALTER TABLE public.opush_event_mapping OWNER TO obm;

--
-- Name: opush_event_mapping_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE opush_event_mapping_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.opush_event_mapping_id_seq OWNER TO obm;

--
-- Name: opush_event_mapping_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE opush_event_mapping_id_seq OWNED BY opush_event_mapping.id;


--
-- Name: opush_folder_mapping; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE opush_folder_mapping (
    id integer NOT NULL,
    device_id integer NOT NULL,
    collection character varying(255) NOT NULL
);


ALTER TABLE public.opush_folder_mapping OWNER TO obm;

--
-- Name: opush_folder_mapping_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE opush_folder_mapping_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.opush_folder_mapping_id_seq OWNER TO obm;

--
-- Name: opush_folder_mapping_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE opush_folder_mapping_id_seq OWNED BY opush_folder_mapping.id;


--
-- Name: opush_folder_snapshot; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE opush_folder_snapshot (
    id integer NOT NULL,
    folder_sync_state_id integer NOT NULL,
    collection_id integer NOT NULL
);


ALTER TABLE public.opush_folder_snapshot OWNER TO obm;

--
-- Name: opush_folder_snapshot_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE opush_folder_snapshot_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.opush_folder_snapshot_id_seq OWNER TO obm;

--
-- Name: opush_folder_snapshot_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE opush_folder_snapshot_id_seq OWNED BY opush_folder_snapshot.id;


--
-- Name: opush_folder_sync_state; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE opush_folder_sync_state (
    id integer NOT NULL,
    sync_key character varying(64) NOT NULL,
    device_id integer NOT NULL
);


ALTER TABLE public.opush_folder_sync_state OWNER TO obm;

--
-- Name: opush_folder_sync_state_backend_mapping; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE opush_folder_sync_state_backend_mapping (
    id integer NOT NULL,
    data_type pimdata_type NOT NULL,
    folder_sync_state_id integer NOT NULL,
    last_sync timestamp without time zone NOT NULL
);


ALTER TABLE public.opush_folder_sync_state_backend_mapping OWNER TO obm;

--
-- Name: opush_folder_sync_state_backend_mapping_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE opush_folder_sync_state_backend_mapping_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.opush_folder_sync_state_backend_mapping_id_seq OWNER TO obm;

--
-- Name: opush_folder_sync_state_backend_mapping_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE opush_folder_sync_state_backend_mapping_id_seq OWNED BY opush_folder_sync_state_backend_mapping.id;


--
-- Name: opush_folder_sync_state_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE opush_folder_sync_state_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.opush_folder_sync_state_id_seq OWNER TO obm;

--
-- Name: opush_folder_sync_state_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE opush_folder_sync_state_id_seq OWNED BY opush_folder_sync_state.id;


--
-- Name: opush_ping_heartbeat; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE opush_ping_heartbeat (
    device_id integer NOT NULL,
    last_heartbeat integer NOT NULL
);


ALTER TABLE public.opush_ping_heartbeat OWNER TO obm;

--
-- Name: opush_sec_policy; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE opush_sec_policy (
    id integer NOT NULL,
    device_password_enabled boolean DEFAULT false
);


ALTER TABLE public.opush_sec_policy OWNER TO obm;

--
-- Name: opush_sec_policy_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE opush_sec_policy_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.opush_sec_policy_id_seq OWNER TO obm;

--
-- Name: opush_sec_policy_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE opush_sec_policy_id_seq OWNED BY opush_sec_policy.id;


--
-- Name: opush_sync_perms; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE opush_sync_perms (
    owner integer,
    device_id integer NOT NULL,
    policy integer,
    pending_accept boolean NOT NULL
);


ALTER TABLE public.opush_sync_perms OWNER TO obm;

--
-- Name: opush_sync_state; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE opush_sync_state (
    id integer NOT NULL,
    sync_key character varying(64) NOT NULL,
    collection_id integer NOT NULL,
    device_id integer NOT NULL,
    last_sync timestamp without time zone NOT NULL
);


ALTER TABLE public.opush_sync_state OWNER TO obm;

--
-- Name: opush_sync_state_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE opush_sync_state_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.opush_sync_state_id_seq OWNER TO obm;

--
-- Name: opush_sync_state_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE opush_sync_state_id_seq OWNED BY opush_sync_state.id;


--
-- Name: opush_synced_item; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE opush_synced_item (
    id integer NOT NULL,
    sync_state_id integer NOT NULL,
    item_id integer NOT NULL,
    addition boolean NOT NULL
);


ALTER TABLE public.opush_synced_item OWNER TO obm;

--
-- Name: opush_synced_item_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE opush_synced_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.opush_synced_item_id_seq OWNER TO obm;

--
-- Name: opush_synced_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE opush_synced_item_id_seq OWNED BY opush_synced_item.id;


--
-- Name: organizationalchart; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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
    organizationalchart_archive smallint DEFAULT 0 NOT NULL
);


ALTER TABLE public.organizationalchart OWNER TO obm;

--
-- Name: organizationalchart_organizationalchart_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE organizationalchart_organizationalchart_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.organizationalchart_organizationalchart_id_seq OWNER TO obm;

--
-- Name: organizationalchart_organizationalchart_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE organizationalchart_organizationalchart_id_seq OWNED BY organizationalchart.organizationalchart_id;


--
-- Name: organizationalchartentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE organizationalchartentity (
    organizationalchartentity_entity_id integer NOT NULL,
    organizationalchartentity_organizationalchart_id integer NOT NULL
);


ALTER TABLE public.organizationalchartentity OWNER TO obm;

--
-- Name: p__contactgroup; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE p__contactgroup (
    contact_id integer NOT NULL,
    group_id integer NOT NULL
);


ALTER TABLE public.p__contactgroup OWNER TO obm;

--
-- Name: p_categorylink; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE p_categorylink (
    categorylink_category_id integer NOT NULL,
    categorylink_entity_id integer NOT NULL,
    categorylink_category character varying(24) NOT NULL
);


ALTER TABLE public.p_categorylink OWNER TO obm;

--
-- Name: p_domain; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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
    domain_global boolean,
    domain_uuid character(36) NOT NULL
);


ALTER TABLE public.p_domain OWNER TO obm;

--
-- Name: p_domainentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE p_domainentity (
    domainentity_entity_id integer NOT NULL,
    domainentity_domain_id integer NOT NULL
);


ALTER TABLE public.p_domainentity OWNER TO obm;

--
-- Name: p_entityright; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE p_entityright (
    entityright_id integer NOT NULL,
    entityright_entity_id integer NOT NULL,
    entityright_consumer_id integer,
    entityright_access integer NOT NULL,
    entityright_read integer NOT NULL,
    entityright_write integer NOT NULL,
    entityright_admin integer NOT NULL
);


ALTER TABLE public.p_entityright OWNER TO obm;

--
-- Name: p_field; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE p_field (
    id integer NOT NULL,
    entity_id integer NOT NULL,
    field character varying(255),
    value text
);


ALTER TABLE public.p_field OWNER TO obm;

--
-- Name: p_groupentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE p_groupentity (
    groupentity_entity_id integer NOT NULL,
    groupentity_group_id integer NOT NULL
);


ALTER TABLE public.p_groupentity OWNER TO obm;

--
-- Name: p_host; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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
    host_archive smallint NOT NULL,
    host_name character varying(32) NOT NULL,
    host_fqdn character varying(255),
    host_ip character varying(16),
    host_delegation character varying(256),
    host_description character varying(128)
);


ALTER TABLE public.p_host OWNER TO obm;

--
-- Name: p_hostentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE p_hostentity (
    hostentity_entity_id integer NOT NULL,
    hostentity_host_id integer NOT NULL
);


ALTER TABLE public.p_hostentity OWNER TO obm;

--
-- Name: p_mailboxentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE p_mailboxentity (
    mailboxentity_entity_id integer NOT NULL,
    mailboxentity_mailbox_id integer NOT NULL
);


ALTER TABLE public.p_mailboxentity OWNER TO obm;

--
-- Name: p_mailshare; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE p_mailshare (
    mailshare_id integer NOT NULL,
    mailshare_domain_id integer NOT NULL,
    mailshare_timeupdate timestamp without time zone,
    mailshare_timecreate timestamp without time zone,
    mailshare_userupdate integer,
    mailshare_usercreate integer,
    mailshare_name character varying(32),
    mailshare_archive smallint NOT NULL,
    mailshare_quota character varying(8) NOT NULL,
    mailshare_mail_server_id integer,
    mailshare_delegation character varying(256),
    mailshare_description character varying(255),
    mailshare_email text
);


ALTER TABLE public.p_mailshare OWNER TO obm;

--
-- Name: p_mailshareentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE p_mailshareentity (
    mailshareentity_entity_id integer NOT NULL,
    mailshareentity_mailshare_id integer NOT NULL
);


ALTER TABLE public.p_mailshareentity OWNER TO obm;

--
-- Name: p_of_usergroup; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE p_of_usergroup (
    of_usergroup_group_id integer NOT NULL,
    of_usergroup_user_id integer NOT NULL
);


ALTER TABLE public.p_of_usergroup OWNER TO obm;

--
-- Name: p_service; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE p_service (
    service_id integer NOT NULL,
    service_service character varying(255) NOT NULL,
    service_entity_id integer NOT NULL
);


ALTER TABLE public.p_service OWNER TO obm;

--
-- Name: p_serviceproperty; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE p_serviceproperty (
    serviceproperty_id integer NOT NULL,
    serviceproperty_service character varying(255) NOT NULL,
    serviceproperty_property character varying(255) NOT NULL,
    serviceproperty_entity_id integer NOT NULL,
    serviceproperty_value text
);


ALTER TABLE public.p_serviceproperty OWNER TO obm;

--
-- Name: p_ugroup; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE p_ugroup (
    group_id integer NOT NULL,
    group_domain_id integer NOT NULL,
    group_timeupdate timestamp without time zone,
    group_timecreate timestamp without time zone,
    group_userupdate integer,
    group_usercreate integer,
    group_system integer,
    group_archive smallint NOT NULL,
    group_privacy integer,
    group_local integer,
    group_ext_id character varying(255),
    group_samba integer,
    group_gid integer,
    group_mailing integer,
    group_delegation character varying(256),
    group_manager_id integer,
    group_name character varying(255) NOT NULL,
    group_desc character varying(128),
    group_email character varying(128)
);


ALTER TABLE public.p_ugroup OWNER TO obm;

--
-- Name: p_userentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE p_userentity (
    userentity_entity_id integer NOT NULL,
    userentity_user_id integer NOT NULL
);


ALTER TABLE public.p_userentity OWNER TO obm;

--
-- Name: p_userobm; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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
    userobm_status userstatus,
    userobm_timelastaccess timestamp without time zone,
    userobm_login character varying(80) NOT NULL,
    userobm_nb_login_failed integer,
    userobm_password_type character varying(6) NOT NULL,
    userobm_password character varying(64) NOT NULL,
    userobm_password_dateexp date,
    userobm_account_dateexp date,
    userobm_perms character varying(254),
    userobm_delegation_target character varying(256),
    userobm_delegation character varying(256),
    userobm_calendar_version timestamp without time zone,
    userobm_uid integer,
    userobm_gid integer,
    userobm_datebegin date,
    userobm_hidden integer,
    userobm_kind character varying(64),
    userobm_commonname character varying(256),
    userobm_lastname character varying(64),
    userobm_firstname character varying(64),
    userobm_title character varying(256),
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
    userobm_email_nomade text,
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


ALTER TABLE public.p_userobm OWNER TO obm;

--
-- Name: parentdeal; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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
    parentdeal_archive smallint DEFAULT 0 NOT NULL,
    parentdeal_comment text
);


ALTER TABLE public.parentdeal OWNER TO obm;

--
-- Name: parentdeal_parentdeal_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE parentdeal_parentdeal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.parentdeal_parentdeal_id_seq OWNER TO obm;

--
-- Name: parentdeal_parentdeal_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE parentdeal_parentdeal_id_seq OWNED BY parentdeal.parentdeal_id;


--
-- Name: parentdealentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE parentdealentity (
    parentdealentity_entity_id integer NOT NULL,
    parentdealentity_parentdeal_id integer NOT NULL
);


ALTER TABLE public.parentdealentity OWNER TO obm;

--
-- Name: payment; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.payment OWNER TO obm;

--
-- Name: payment_payment_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE payment_payment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.payment_payment_id_seq OWNER TO obm;

--
-- Name: payment_payment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE payment_payment_id_seq OWNED BY payment.payment_id;


--
-- Name: paymententity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE paymententity (
    paymententity_entity_id integer NOT NULL,
    paymententity_payment_id integer NOT NULL
);


ALTER TABLE public.paymententity OWNER TO obm;

--
-- Name: paymentinvoice; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE paymentinvoice (
    paymentinvoice_invoice_id integer NOT NULL,
    paymentinvoice_payment_id integer NOT NULL,
    paymentinvoice_timeupdate timestamp without time zone,
    paymentinvoice_timecreate timestamp without time zone DEFAULT now(),
    paymentinvoice_userupdate integer,
    paymentinvoice_usercreate integer,
    paymentinvoice_amount numeric(10,2) DEFAULT (0)::numeric NOT NULL
);


ALTER TABLE public.paymentinvoice OWNER TO obm;

--
-- Name: paymentkind; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE paymentkind (
    paymentkind_id integer NOT NULL,
    paymentkind_domain_id integer NOT NULL,
    paymentkind_shortlabel character varying(3) DEFAULT ''::character varying NOT NULL,
    paymentkind_label character varying(40) DEFAULT ''::character varying NOT NULL
);


ALTER TABLE public.paymentkind OWNER TO obm;

--
-- Name: paymentkind_paymentkind_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE paymentkind_paymentkind_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.paymentkind_paymentkind_id_seq OWNER TO obm;

--
-- Name: paymentkind_paymentkind_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE paymentkind_paymentkind_id_seq OWNED BY paymentkind.paymentkind_id;


--
-- Name: phone; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE phone (
    phone_id integer NOT NULL,
    phone_entity_id integer NOT NULL,
    phone_label character varying(255) NOT NULL,
    phone_number character varying(32)
);


ALTER TABLE public.phone OWNER TO obm;

--
-- Name: phone_phone_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE phone_phone_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.phone_phone_id_seq OWNER TO obm;

--
-- Name: phone_phone_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE phone_phone_id_seq OWNED BY phone.phone_id;


--
-- Name: plannedtask; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE plannedtask (
    plannedtask_id integer NOT NULL,
    plannedtask_domain_id integer DEFAULT 0,
    plannedtask_timeupdate timestamp without time zone,
    plannedtask_timecreate timestamp without time zone DEFAULT now(),
    plannedtask_userupdate integer,
    plannedtask_usercreate integer,
    plannedtask_user_id integer,
    plannedtask_datebegin date,
    plannedtask_dateend date,
    plannedtask_period taskperiod DEFAULT 'MORNING'::taskperiod,
    plannedtask_project_id integer,
    plannedtask_tasktype_id integer,
    plannedtask_overrun boolean DEFAULT false,
    plannedtask_comment text
);


ALTER TABLE public.plannedtask OWNER TO obm;

--
-- Name: plannedtask_plannedtask_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE plannedtask_plannedtask_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.plannedtask_plannedtask_id_seq OWNER TO obm;

--
-- Name: plannedtask_plannedtask_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE plannedtask_plannedtask_id_seq OWNED BY plannedtask.plannedtask_id;


--
-- Name: profile; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.profile OWNER TO obm;

--
-- Name: profile_profile_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE profile_profile_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.profile_profile_id_seq OWNER TO obm;

--
-- Name: profile_profile_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE profile_profile_id_seq OWNED BY profile.profile_id;


--
-- Name: profileentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE profileentity (
    profileentity_entity_id integer NOT NULL,
    profileentity_profile_id integer NOT NULL
);


ALTER TABLE public.profileentity OWNER TO obm;

--
-- Name: profilemodule; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE profilemodule (
    profilemodule_id integer NOT NULL,
    profilemodule_domain_id integer NOT NULL,
    profilemodule_profile_id integer,
    profilemodule_module_name character varying(64) DEFAULT ''::character varying NOT NULL,
    profilemodule_right integer
);


ALTER TABLE public.profilemodule OWNER TO obm;

--
-- Name: profilemodule_profilemodule_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE profilemodule_profilemodule_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.profilemodule_profilemodule_id_seq OWNER TO obm;

--
-- Name: profilemodule_profilemodule_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE profilemodule_profilemodule_id_seq OWNED BY profilemodule.profilemodule_id;


--
-- Name: profileproperty; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE profileproperty (
    profileproperty_id integer NOT NULL,
    profileproperty_profile_id integer,
    profileproperty_name character varying(32) DEFAULT ''::character varying NOT NULL,
    profileproperty_value text DEFAULT ''::text NOT NULL
);


ALTER TABLE public.profileproperty OWNER TO obm;

--
-- Name: profileproperty_profileproperty_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE profileproperty_profileproperty_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.profileproperty_profileproperty_id_seq OWNER TO obm;

--
-- Name: profileproperty_profileproperty_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE profileproperty_profileproperty_id_seq OWNED BY profileproperty.profileproperty_id;


--
-- Name: profilesection; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE profilesection (
    profilesection_id integer NOT NULL,
    profilesection_domain_id integer NOT NULL,
    profilesection_profile_id integer,
    profilesection_section_name character varying(64) DEFAULT ''::character varying NOT NULL,
    profilesection_show boolean
);


ALTER TABLE public.profilesection OWNER TO obm;

--
-- Name: profilesection_profilesection_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE profilesection_profilesection_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.profilesection_profilesection_id_seq OWNER TO obm;

--
-- Name: profilesection_profilesection_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE profilesection_profilesection_id_seq OWNED BY profilesection.profilesection_id;


--
-- Name: project; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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
    project_soldtime numeric(12,2),
    project_estimatedtime numeric(12,2),
    project_datebegin date,
    project_dateend date,
    project_archive smallint DEFAULT 0 NOT NULL,
    project_comment text,
    project_reference_date character varying(32),
    project_reference_duration character varying(16),
    project_reference_desc text,
    project_reference_tech text
);


ALTER TABLE public.project OWNER TO obm;

--
-- Name: project_project_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE project_project_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.project_project_id_seq OWNER TO obm;

--
-- Name: project_project_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE project_project_id_seq OWNED BY project.project_id;


--
-- Name: projectclosing; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.projectclosing OWNER TO obm;

--
-- Name: projectclosing_projectclosing_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE projectclosing_projectclosing_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.projectclosing_projectclosing_id_seq OWNER TO obm;

--
-- Name: projectclosing_projectclosing_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE projectclosing_projectclosing_id_seq OWNED BY projectclosing.projectclosing_id;


--
-- Name: projectcv; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE projectcv (
    projectcv_project_id integer NOT NULL,
    projectcv_cv_id integer NOT NULL,
    projectcv_role character varying(128)
);


ALTER TABLE public.projectcv OWNER TO obm;

--
-- Name: projectentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE projectentity (
    projectentity_entity_id integer NOT NULL,
    projectentity_project_id integer NOT NULL
);


ALTER TABLE public.projectentity OWNER TO obm;

--
-- Name: projectreftask; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.projectreftask OWNER TO obm;

--
-- Name: projectreftask_projectreftask_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE projectreftask_projectreftask_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.projectreftask_projectreftask_id_seq OWNER TO obm;

--
-- Name: projectreftask_projectreftask_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE projectreftask_projectreftask_id_seq OWNED BY projectreftask.projectreftask_id;


--
-- Name: projecttask; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.projecttask OWNER TO obm;

--
-- Name: projecttask_projecttask_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE projecttask_projecttask_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.projecttask_projecttask_id_seq OWNER TO obm;

--
-- Name: projecttask_projecttask_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE projecttask_projecttask_id_seq OWNED BY projecttask.projecttask_id;


--
-- Name: projectuser; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.projectuser OWNER TO obm;

--
-- Name: projectuser_projectuser_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE projectuser_projectuser_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.projectuser_projectuser_id_seq OWNER TO obm;

--
-- Name: projectuser_projectuser_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE projectuser_projectuser_id_seq OWNED BY projectuser.projectuser_id;


--
-- Name: publication; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.publication OWNER TO obm;

--
-- Name: publication_publication_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE publication_publication_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.publication_publication_id_seq OWNER TO obm;

--
-- Name: publication_publication_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE publication_publication_id_seq OWNED BY publication.publication_id;


--
-- Name: publicationentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE publicationentity (
    publicationentity_entity_id integer NOT NULL,
    publicationentity_publication_id integer NOT NULL
);


ALTER TABLE public.publicationentity OWNER TO obm;

--
-- Name: publicationtype; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.publicationtype OWNER TO obm;

--
-- Name: publicationtype_publicationtype_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE publicationtype_publicationtype_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.publicationtype_publicationtype_id_seq OWNER TO obm;

--
-- Name: publicationtype_publicationtype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE publicationtype_publicationtype_id_seq OWNED BY publicationtype.publicationtype_id;


--
-- Name: region; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.region OWNER TO obm;

--
-- Name: region_region_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE region_region_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.region_region_id_seq OWNER TO obm;

--
-- Name: region_region_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE region_region_id_seq OWNED BY region.region_id;


--
-- Name: resource; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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
    resource_delegation character varying(256) DEFAULT ''::character varying,
    resource_description character varying(255),
    resource_qty integer DEFAULT 0,
    resource_email text
);


ALTER TABLE public.resource OWNER TO obm;

--
-- Name: resource_resource_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE resource_resource_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.resource_resource_id_seq OWNER TO obm;

--
-- Name: resource_resource_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE resource_resource_id_seq OWNED BY resource.resource_id;


--
-- Name: resourceentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE resourceentity (
    resourceentity_entity_id integer NOT NULL,
    resourceentity_resource_id integer NOT NULL
);


ALTER TABLE public.resourceentity OWNER TO obm;

--
-- Name: resourcegroup; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE resourcegroup (
    resourcegroup_rgroup_id integer NOT NULL,
    resourcegroup_resource_id integer NOT NULL
);


ALTER TABLE public.resourcegroup OWNER TO obm;

--
-- Name: resourcegroupentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE resourcegroupentity (
    resourcegroupentity_entity_id integer NOT NULL,
    resourcegroupentity_resourcegroup_id integer NOT NULL
);


ALTER TABLE public.resourcegroupentity OWNER TO obm;

--
-- Name: resourceitem; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE resourceitem (
    resourceitem_id integer NOT NULL,
    resourceitem_domain_id integer NOT NULL,
    resourceitem_label character varying(32) NOT NULL,
    resourceitem_resourcetype_id integer NOT NULL,
    resourceitem_description text
);


ALTER TABLE public.resourceitem OWNER TO obm;

--
-- Name: resourceitem_resourceitem_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE resourceitem_resourceitem_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.resourceitem_resourceitem_id_seq OWNER TO obm;

--
-- Name: resourceitem_resourceitem_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE resourceitem_resourceitem_id_seq OWNED BY resourceitem.resourceitem_id;


--
-- Name: resourcetype; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE resourcetype (
    resourcetype_id integer NOT NULL,
    resourcetype_domain_id integer NOT NULL,
    resourcetype_label character varying(32) NOT NULL,
    resourcetype_property character varying(32),
    resourcetype_pkind integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.resourcetype OWNER TO obm;

--
-- Name: resourcetype_resourcetype_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE resourcetype_resourcetype_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.resourcetype_resourcetype_id_seq OWNER TO obm;

--
-- Name: resourcetype_resourcetype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE resourcetype_resourcetype_id_seq OWNED BY resourcetype.resourcetype_id;


--
-- Name: rgroup; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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
    rgroup_delegation character varying(256) DEFAULT ''::character varying,
    rgroup_desc character varying(128)
);


ALTER TABLE public.rgroup OWNER TO obm;

--
-- Name: rgroup_rgroup_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE rgroup_rgroup_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.rgroup_rgroup_id_seq OWNER TO obm;

--
-- Name: rgroup_rgroup_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE rgroup_rgroup_id_seq OWNED BY rgroup.rgroup_id;


--
-- Name: service; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE service (
    service_id integer NOT NULL,
    service_service character varying(255) NOT NULL,
    service_entity_id integer NOT NULL
);


ALTER TABLE public.service OWNER TO obm;

--
-- Name: service_service_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE service_service_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.service_service_id_seq OWNER TO obm;

--
-- Name: service_service_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE service_service_id_seq OWNED BY service.service_id;


--
-- Name: serviceproperty; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE serviceproperty (
    serviceproperty_id integer NOT NULL,
    serviceproperty_service character varying(255) NOT NULL,
    serviceproperty_property character varying(255) NOT NULL,
    serviceproperty_entity_id integer NOT NULL,
    serviceproperty_value text
);


ALTER TABLE public.serviceproperty OWNER TO obm;

--
-- Name: serviceproperty_serviceproperty_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE serviceproperty_serviceproperty_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.serviceproperty_serviceproperty_id_seq OWNER TO obm;

--
-- Name: serviceproperty_serviceproperty_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE serviceproperty_serviceproperty_id_seq OWNED BY serviceproperty.serviceproperty_id;


--
-- Name: ssoticket; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE ssoticket (
    ssoticket_ticket character varying(255) NOT NULL,
    ssoticket_user_id integer,
    ssoticket_timestamp timestamp without time zone NOT NULL
);


ALTER TABLE public.ssoticket OWNER TO obm;

--
-- Name: stats; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE stats (
    stats_name character varying(32) DEFAULT ''::character varying NOT NULL,
    stats_value character varying(255) DEFAULT ''::character varying NOT NULL
);


ALTER TABLE public.stats OWNER TO obm;

--
-- Name: subscription; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.subscription OWNER TO obm;

--
-- Name: subscription_subscription_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE subscription_subscription_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.subscription_subscription_id_seq OWNER TO obm;

--
-- Name: subscription_subscription_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE subscription_subscription_id_seq OWNED BY subscription.subscription_id;


--
-- Name: subscriptionentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE subscriptionentity (
    subscriptionentity_entity_id integer NOT NULL,
    subscriptionentity_subscription_id integer NOT NULL
);


ALTER TABLE public.subscriptionentity OWNER TO obm;

--
-- Name: subscriptionreception; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.subscriptionreception OWNER TO obm;

--
-- Name: subscriptionreception_subscriptionreception_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE subscriptionreception_subscriptionreception_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.subscriptionreception_subscriptionreception_id_seq OWNER TO obm;

--
-- Name: subscriptionreception_subscriptionreception_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE subscriptionreception_subscriptionreception_id_seq OWNED BY subscriptionreception.subscriptionreception_id;


--
-- Name: syncedaddressbook; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE syncedaddressbook (
    user_id integer NOT NULL,
    addressbook_id integer NOT NULL,
    "timestamp" timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.syncedaddressbook OWNER TO obm;

--
-- Name: taskevent; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE taskevent (
    taskevent_task_id integer NOT NULL,
    taskevent_event_id integer NOT NULL
);


ALTER TABLE public.taskevent OWNER TO obm;

--
-- Name: tasktype; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE tasktype (
    tasktype_id integer NOT NULL,
    tasktype_domain_id integer NOT NULL,
    tasktype_timeupdate timestamp without time zone,
    tasktype_timecreate timestamp without time zone DEFAULT now(),
    tasktype_userupdate integer,
    tasktype_usercreate integer,
    tasktype_internal integer NOT NULL,
    tasktype_tasktypegroup_id integer,
    tasktype_code character varying(10),
    tasktype_label character varying(32) DEFAULT NULL::character varying
);


ALTER TABLE public.tasktype OWNER TO obm;

--
-- Name: tasktype_tasktype_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE tasktype_tasktype_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.tasktype_tasktype_id_seq OWNER TO obm;

--
-- Name: tasktype_tasktype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE tasktype_tasktype_id_seq OWNED BY tasktype.tasktype_id;


--
-- Name: tasktypegroup; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE tasktypegroup (
    tasktypegroup_id integer NOT NULL,
    tasktypegroup_domain_id integer NOT NULL,
    tasktypegroup_timeupdate timestamp without time zone,
    tasktypegroup_timecreate timestamp without time zone DEFAULT now(),
    tasktypegroup_userupdate integer,
    tasktypegroup_usercreate integer,
    tasktypegroup_label character varying(32),
    tasktypegroup_code character varying(20),
    tasktypegroup_bgcolor character varying(7) DEFAULT NULL::character varying,
    tasktypegroup_fgcolor character varying(7) DEFAULT NULL::character varying
);


ALTER TABLE public.tasktypegroup OWNER TO obm;

--
-- Name: tasktypegroup_tasktypegroup_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE tasktypegroup_tasktypegroup_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.tasktypegroup_tasktypegroup_id_seq OWNER TO obm;

--
-- Name: tasktypegroup_tasktypegroup_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE tasktypegroup_tasktypegroup_id_seq OWNED BY tasktypegroup.tasktypegroup_id;


--
-- Name: timetask; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.timetask OWNER TO obm;

--
-- Name: timetask_timetask_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE timetask_timetask_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.timetask_timetask_id_seq OWNER TO obm;

--
-- Name: timetask_timetask_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE timetask_timetask_id_seq OWNED BY timetask.timetask_id;


--
-- Name: token; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE token (
    token character varying(300) NOT NULL,
    property character varying(255) NOT NULL,
    value character varying(255) NOT NULL
);


ALTER TABLE public.token OWNER TO obm;

--
-- Name: trusttoken; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE trusttoken (
    id integer NOT NULL,
    token character(36) NOT NULL,
    login character varying(80) NOT NULL,
    time_created timestamp without time zone DEFAULT now()
);


ALTER TABLE public.trusttoken OWNER TO obm;

--
-- Name: trusttoken_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE trusttoken_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.trusttoken_id_seq OWNER TO obm;

--
-- Name: trusttoken_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE trusttoken_id_seq OWNED BY trusttoken.id;


--
-- Name: ugroup; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE ugroup (
    group_id integer NOT NULL,
    group_domain_id integer NOT NULL,
    group_timeupdate timestamp without time zone,
    group_timecreate timestamp without time zone DEFAULT now(),
    group_userupdate integer,
    group_usercreate integer,
    group_system integer DEFAULT 0,
    group_archive smallint DEFAULT 0 NOT NULL,
    group_privacy integer DEFAULT 0,
    group_local integer DEFAULT 1,
    group_ext_id character varying(255),
    group_samba integer DEFAULT 0,
    group_gid integer,
    group_mailing integer DEFAULT 0,
    group_delegation character varying(256) DEFAULT ''::character varying,
    group_manager_id integer,
    group_name character varying(255) NOT NULL,
    group_desc character varying(128),
    group_email character varying(128)
);


ALTER TABLE public.ugroup OWNER TO obm;

--
-- Name: ugroup_group_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE ugroup_group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.ugroup_group_id_seq OWNER TO obm;

--
-- Name: ugroup_group_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE ugroup_group_id_seq OWNED BY ugroup.group_id;


--
-- Name: updated; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE updated (
    updated_id integer NOT NULL,
    updated_domain_id integer,
    updated_user_id integer,
    updated_delegation character varying(256) DEFAULT ''::character varying,
    updated_table character varying(32),
    updated_entity_id integer,
    updated_type character(1)
);


ALTER TABLE public.updated OWNER TO obm;

--
-- Name: updated_updated_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE updated_updated_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.updated_updated_id_seq OWNER TO obm;

--
-- Name: updated_updated_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE updated_updated_id_seq OWNED BY updated.updated_id;


--
-- Name: updatedlinks; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE updatedlinks (
    updatedlinks_id integer NOT NULL,
    updatedlinks_domain_id integer,
    updatedlinks_user_id integer,
    updatedlinks_delegation character varying(256) DEFAULT ''::character varying,
    updatedlinks_table character varying(32),
    updatedlinks_entity character varying(32),
    updatedlinks_entity_id integer
);


ALTER TABLE public.updatedlinks OWNER TO obm;

--
-- Name: updatedlinks_updatedlinks_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE updatedlinks_updatedlinks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.updatedlinks_updatedlinks_id_seq OWNER TO obm;

--
-- Name: updatedlinks_updatedlinks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE updatedlinks_updatedlinks_id_seq OWNED BY updatedlinks.updatedlinks_id;


--
-- Name: userentity; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE userentity (
    userentity_entity_id integer NOT NULL,
    userentity_user_id integer NOT NULL
);


ALTER TABLE public.userentity OWNER TO obm;

--
-- Name: userobm; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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
    userobm_status userstatus DEFAULT 'VALID'::userstatus,
    userobm_timelastaccess timestamp without time zone,
    userobm_login character varying(80) DEFAULT ''::character varying NOT NULL,
    userobm_nb_login_failed integer DEFAULT 0,
    userobm_password_type character varying(6) DEFAULT 'PLAIN'::character varying NOT NULL,
    userobm_password character varying(64) DEFAULT ''::character varying NOT NULL,
    userobm_password_dateexp date,
    userobm_account_dateexp date,
    userobm_perms character varying(254),
    userobm_delegation_target character varying(256) DEFAULT ''::character varying,
    userobm_delegation character varying(256) DEFAULT ''::character varying,
    userobm_calendar_version timestamp without time zone,
    userobm_uid integer,
    userobm_gid integer,
    userobm_datebegin date,
    userobm_hidden integer DEFAULT 0,
    userobm_kind character varying(64),
    userobm_commonname character varying(256) DEFAULT ''::character varying,
    userobm_lastname character varying(64) DEFAULT ''::character varying,
    userobm_firstname character varying(64) DEFAULT ''::character varying,
    userobm_title character varying(256) DEFAULT ''::character varying,
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
    userobm_email_nomade text DEFAULT ''::text,
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


ALTER TABLE public.userobm OWNER TO obm;

--
-- Name: userobm_sessionlog; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.userobm_sessionlog OWNER TO obm;

--
-- Name: userobm_userobm_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE userobm_userobm_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.userobm_userobm_id_seq OWNER TO obm;

--
-- Name: userobm_userobm_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE userobm_userobm_id_seq OWNED BY userobm.userobm_id;


--
-- Name: userobmgroup; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE userobmgroup (
    userobmgroup_group_id integer NOT NULL,
    userobmgroup_userobm_id integer NOT NULL
);


ALTER TABLE public.userobmgroup OWNER TO obm;

--
-- Name: userobmpref; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE userobmpref (
    userobmpref_id integer NOT NULL,
    userobmpref_user_id integer,
    userobmpref_option character varying(50) NOT NULL,
    userobmpref_value character varying(50) NOT NULL
);


ALTER TABLE public.userobmpref OWNER TO obm;

--
-- Name: userobmpref_userobmpref_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE userobmpref_userobmpref_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.userobmpref_userobmpref_id_seq OWNER TO obm;

--
-- Name: userobmpref_userobmpref_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE userobmpref_userobmpref_id_seq OWNED BY userobmpref.userobmpref_id;


--
-- Name: userpattern; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE userpattern (
    id integer NOT NULL,
    domain_id integer NOT NULL,
    timeupdate timestamp without time zone,
    timecreate timestamp without time zone,
    userupdate integer,
    usercreate integer,
    title character varying(255) NOT NULL,
    description text
);


ALTER TABLE public.userpattern OWNER TO obm;

--
-- Name: userpattern_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE userpattern_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.userpattern_id_seq OWNER TO obm;

--
-- Name: userpattern_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE userpattern_id_seq OWNED BY userpattern.id;


--
-- Name: userpattern_property; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE userpattern_property (
    userpattern_id integer NOT NULL,
    attribute character varying(255) NOT NULL,
    value text
);


ALTER TABLE public.userpattern_property OWNER TO obm;

--
-- Name: usersystem; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
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


ALTER TABLE public.usersystem OWNER TO obm;

--
-- Name: usersystem_usersystem_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE usersystem_usersystem_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.usersystem_usersystem_id_seq OWNER TO obm;

--
-- Name: usersystem_usersystem_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE usersystem_usersystem_id_seq OWNED BY usersystem.usersystem_id;


--
-- Name: website; Type: TABLE; Schema: public; Owner: obm; Tablespace: 
--

CREATE TABLE website (
    website_id integer NOT NULL,
    website_entity_id integer NOT NULL,
    website_label character varying(255) NOT NULL,
    website_url text
);


ALTER TABLE public.website OWNER TO obm;

--
-- Name: website_website_id_seq; Type: SEQUENCE; Schema: public; Owner: obm
--

CREATE SEQUENCE website_website_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.website_website_id_seq OWNER TO obm;

--
-- Name: website_website_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: obm
--

ALTER SEQUENCE website_website_id_seq OWNED BY website.website_id;


--
-- Name: account_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY account ALTER COLUMN account_id SET DEFAULT nextval('account_account_id_seq'::regclass);


--
-- Name: address_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY address ALTER COLUMN address_id SET DEFAULT nextval('address_address_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY addressbook ALTER COLUMN id SET DEFAULT nextval('addressbook_id_seq'::regclass);


--
-- Name: campaign_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY campaign ALTER COLUMN campaign_id SET DEFAULT nextval('campaign_campaign_id_seq'::regclass);


--
-- Name: campaignmailcontent_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY campaignmailcontent ALTER COLUMN campaignmailcontent_id SET DEFAULT nextval('campaignmailcontent_campaignmailcontent_id_seq'::regclass);


--
-- Name: campaignmailtarget_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY campaignmailtarget ALTER COLUMN campaignmailtarget_id SET DEFAULT nextval('campaignmailtarget_campaignmailtarget_id_seq'::regclass);


--
-- Name: campaignpushtarget_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY campaignpushtarget ALTER COLUMN campaignpushtarget_id SET DEFAULT nextval('campaignpushtarget_campaignpushtarget_id_seq'::regclass);


--
-- Name: campaigntarget_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY campaigntarget ALTER COLUMN campaigntarget_id SET DEFAULT nextval('campaigntarget_campaigntarget_id_seq'::regclass);


--
-- Name: category_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY category ALTER COLUMN category_id SET DEFAULT nextval('category_category_id_seq'::regclass);


--
-- Name: company_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY company ALTER COLUMN company_id SET DEFAULT nextval('company_company_id_seq'::regclass);


--
-- Name: companyactivity_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY companyactivity ALTER COLUMN companyactivity_id SET DEFAULT nextval('companyactivity_companyactivity_id_seq'::regclass);


--
-- Name: companynafcode_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY companynafcode ALTER COLUMN companynafcode_id SET DEFAULT nextval('companynafcode_companynafcode_id_seq'::regclass);


--
-- Name: companytype_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY companytype ALTER COLUMN companytype_id SET DEFAULT nextval('companytype_companytype_id_seq'::regclass);


--
-- Name: contact_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contact ALTER COLUMN contact_id SET DEFAULT nextval('contact_contact_id_seq'::regclass);


--
-- Name: contactfunction_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contactfunction ALTER COLUMN contactfunction_id SET DEFAULT nextval('contactfunction_contactfunction_id_seq'::regclass);


--
-- Name: contract_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contract ALTER COLUMN contract_id SET DEFAULT nextval('contract_contract_id_seq'::regclass);


--
-- Name: contractpriority_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contractpriority ALTER COLUMN contractpriority_id SET DEFAULT nextval('contractpriority_contractpriority_id_seq'::regclass);


--
-- Name: contractstatus_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contractstatus ALTER COLUMN contractstatus_id SET DEFAULT nextval('contractstatus_contractstatus_id_seq'::regclass);


--
-- Name: contracttype_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contracttype ALTER COLUMN contracttype_id SET DEFAULT nextval('contracttype_contracttype_id_seq'::regclass);


--
-- Name: cv_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY cv ALTER COLUMN cv_id SET DEFAULT nextval('cv_cv_id_seq'::regclass);


--
-- Name: datasource_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY datasource ALTER COLUMN datasource_id SET DEFAULT nextval('datasource_datasource_id_seq'::regclass);


--
-- Name: deal_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY deal ALTER COLUMN deal_id SET DEFAULT nextval('deal_deal_id_seq'::regclass);


--
-- Name: dealcompany_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY dealcompany ALTER COLUMN dealcompany_id SET DEFAULT nextval('dealcompany_dealcompany_id_seq'::regclass);


--
-- Name: dealcompanyrole_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY dealcompanyrole ALTER COLUMN dealcompanyrole_id SET DEFAULT nextval('dealcompanyrole_dealcompanyrole_id_seq'::regclass);


--
-- Name: dealstatus_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY dealstatus ALTER COLUMN dealstatus_id SET DEFAULT nextval('dealstatus_dealstatus_id_seq'::regclass);


--
-- Name: dealtype_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY dealtype ALTER COLUMN dealtype_id SET DEFAULT nextval('dealtype_dealtype_id_seq'::regclass);


--
-- Name: defaultodttemplate_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY defaultodttemplate ALTER COLUMN defaultodttemplate_id SET DEFAULT nextval('defaultodttemplate_defaultodttemplate_id_seq'::regclass);


--
-- Name: deleted_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY deleted ALTER COLUMN deleted_id SET DEFAULT nextval('deleted_deleted_id_seq'::regclass);


--
-- Name: deletedevent_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY deletedevent ALTER COLUMN deletedevent_id SET DEFAULT nextval('deletedevent_deletedevent_id_seq'::regclass);


--
-- Name: display_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY displaypref ALTER COLUMN display_id SET DEFAULT nextval('displaypref_display_id_seq'::regclass);


--
-- Name: document_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY document ALTER COLUMN document_id SET DEFAULT nextval('document_document_id_seq'::regclass);


--
-- Name: documentmimetype_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY documentmimetype ALTER COLUMN documentmimetype_id SET DEFAULT nextval('documentmimetype_documentmimetype_id_seq'::regclass);


--
-- Name: domain_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY domain ALTER COLUMN domain_id SET DEFAULT nextval('domain_domain_id_seq'::regclass);


--
-- Name: email_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY email ALTER COLUMN email_id SET DEFAULT nextval('email_email_id_seq'::regclass);


--
-- Name: entity_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY entity ALTER COLUMN entity_id SET DEFAULT nextval('entity_entity_id_seq'::regclass);


--
-- Name: entityright_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY entityright ALTER COLUMN entityright_id SET DEFAULT nextval('entityright_entityright_id_seq'::regclass);


--
-- Name: event_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY event ALTER COLUMN event_id SET DEFAULT nextval('event_event_id_seq'::regclass);


--
-- Name: eventcategory1_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventcategory1 ALTER COLUMN eventcategory1_id SET DEFAULT nextval('eventcategory1_eventcategory1_id_seq'::regclass);


--
-- Name: eventtag_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventtag ALTER COLUMN eventtag_id SET DEFAULT nextval('eventtag_eventtag_id_seq'::regclass);


--
-- Name: eventtemplate_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventtemplate ALTER COLUMN eventtemplate_id SET DEFAULT nextval('eventtemplate_eventtemplate_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY field ALTER COLUMN id SET DEFAULT nextval('field_id_seq'::regclass);


--
-- Name: host_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY host ALTER COLUMN host_id SET DEFAULT nextval('host_host_id_seq'::regclass);


--
-- Name: im_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY im ALTER COLUMN im_id SET DEFAULT nextval('im_im_id_seq'::regclass);


--
-- Name: import_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY import ALTER COLUMN import_id SET DEFAULT nextval('import_import_id_seq'::regclass);


--
-- Name: incident_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incident ALTER COLUMN incident_id SET DEFAULT nextval('incident_incident_id_seq'::regclass);


--
-- Name: incidentpriority_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incidentpriority ALTER COLUMN incidentpriority_id SET DEFAULT nextval('incidentpriority_incidentpriority_id_seq'::regclass);


--
-- Name: incidentresolutiontype_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incidentresolutiontype ALTER COLUMN incidentresolutiontype_id SET DEFAULT nextval('incidentresolutiontype_incidentresolutiontype_id_seq'::regclass);


--
-- Name: incidentstatus_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incidentstatus ALTER COLUMN incidentstatus_id SET DEFAULT nextval('incidentstatus_incidentstatus_id_seq'::regclass);


--
-- Name: invoice_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY invoice ALTER COLUMN invoice_id SET DEFAULT nextval('invoice_invoice_id_seq'::regclass);


--
-- Name: kind_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY kind ALTER COLUMN kind_id SET DEFAULT nextval('kind_kind_id_seq'::regclass);


--
-- Name: lead_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY lead ALTER COLUMN lead_id SET DEFAULT nextval('lead_lead_id_seq'::regclass);


--
-- Name: leadsource_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY leadsource ALTER COLUMN leadsource_id SET DEFAULT nextval('leadsource_leadsource_id_seq'::regclass);


--
-- Name: leadstatus_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY leadstatus ALTER COLUMN leadstatus_id SET DEFAULT nextval('leadstatus_leadstatus_id_seq'::regclass);


--
-- Name: list_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY list ALTER COLUMN list_id SET DEFAULT nextval('list_list_id_seq'::regclass);


--
-- Name: mailinglist_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY mailinglist ALTER COLUMN mailinglist_id SET DEFAULT nextval('mailinglist_mailinglist_id_seq'::regclass);


--
-- Name: mailinglistemail_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY mailinglistemail ALTER COLUMN mailinglistemail_id SET DEFAULT nextval('mailinglistemail_mailinglistemail_id_seq'::regclass);


--
-- Name: mailshare_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY mailshare ALTER COLUMN mailshare_id SET DEFAULT nextval('mailshare_mailshare_id_seq'::regclass);


--
-- Name: obmbookmark_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY obmbookmark ALTER COLUMN obmbookmark_id SET DEFAULT nextval('obmbookmark_obmbookmark_id_seq'::regclass);


--
-- Name: obmbookmarkproperty_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY obmbookmarkproperty ALTER COLUMN obmbookmarkproperty_id SET DEFAULT nextval('obmbookmarkproperty_obmbookmarkproperty_id_seq'::regclass);


--
-- Name: ogroup_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY ogroup ALTER COLUMN ogroup_id SET DEFAULT nextval('ogroup_ogroup_id_seq'::regclass);


--
-- Name: ogrouplink_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY ogrouplink ALTER COLUMN ogrouplink_id SET DEFAULT nextval('ogrouplink_ogrouplink_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_device ALTER COLUMN id SET DEFAULT nextval('opush_device_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_event_mapping ALTER COLUMN id SET DEFAULT nextval('opush_event_mapping_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_folder_mapping ALTER COLUMN id SET DEFAULT nextval('opush_folder_mapping_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_folder_snapshot ALTER COLUMN id SET DEFAULT nextval('opush_folder_snapshot_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_folder_sync_state ALTER COLUMN id SET DEFAULT nextval('opush_folder_sync_state_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_folder_sync_state_backend_mapping ALTER COLUMN id SET DEFAULT nextval('opush_folder_sync_state_backend_mapping_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_sec_policy ALTER COLUMN id SET DEFAULT nextval('opush_sec_policy_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_sync_state ALTER COLUMN id SET DEFAULT nextval('opush_sync_state_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_synced_item ALTER COLUMN id SET DEFAULT nextval('opush_synced_item_id_seq'::regclass);


--
-- Name: organizationalchart_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY organizationalchart ALTER COLUMN organizationalchart_id SET DEFAULT nextval('organizationalchart_organizationalchart_id_seq'::regclass);


--
-- Name: parentdeal_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY parentdeal ALTER COLUMN parentdeal_id SET DEFAULT nextval('parentdeal_parentdeal_id_seq'::regclass);


--
-- Name: payment_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY payment ALTER COLUMN payment_id SET DEFAULT nextval('payment_payment_id_seq'::regclass);


--
-- Name: paymentkind_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY paymentkind ALTER COLUMN paymentkind_id SET DEFAULT nextval('paymentkind_paymentkind_id_seq'::regclass);


--
-- Name: phone_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY phone ALTER COLUMN phone_id SET DEFAULT nextval('phone_phone_id_seq'::regclass);


--
-- Name: plannedtask_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY plannedtask ALTER COLUMN plannedtask_id SET DEFAULT nextval('plannedtask_plannedtask_id_seq'::regclass);


--
-- Name: profile_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY profile ALTER COLUMN profile_id SET DEFAULT nextval('profile_profile_id_seq'::regclass);


--
-- Name: profilemodule_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY profilemodule ALTER COLUMN profilemodule_id SET DEFAULT nextval('profilemodule_profilemodule_id_seq'::regclass);


--
-- Name: profileproperty_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY profileproperty ALTER COLUMN profileproperty_id SET DEFAULT nextval('profileproperty_profileproperty_id_seq'::regclass);


--
-- Name: profilesection_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY profilesection ALTER COLUMN profilesection_id SET DEFAULT nextval('profilesection_profilesection_id_seq'::regclass);


--
-- Name: project_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY project ALTER COLUMN project_id SET DEFAULT nextval('project_project_id_seq'::regclass);


--
-- Name: projectclosing_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY projectclosing ALTER COLUMN projectclosing_id SET DEFAULT nextval('projectclosing_projectclosing_id_seq'::regclass);


--
-- Name: projectreftask_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY projectreftask ALTER COLUMN projectreftask_id SET DEFAULT nextval('projectreftask_projectreftask_id_seq'::regclass);


--
-- Name: projecttask_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY projecttask ALTER COLUMN projecttask_id SET DEFAULT nextval('projecttask_projecttask_id_seq'::regclass);


--
-- Name: projectuser_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY projectuser ALTER COLUMN projectuser_id SET DEFAULT nextval('projectuser_projectuser_id_seq'::regclass);


--
-- Name: publication_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY publication ALTER COLUMN publication_id SET DEFAULT nextval('publication_publication_id_seq'::regclass);


--
-- Name: publicationtype_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY publicationtype ALTER COLUMN publicationtype_id SET DEFAULT nextval('publicationtype_publicationtype_id_seq'::regclass);


--
-- Name: region_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY region ALTER COLUMN region_id SET DEFAULT nextval('region_region_id_seq'::regclass);


--
-- Name: resource_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY resource ALTER COLUMN resource_id SET DEFAULT nextval('resource_resource_id_seq'::regclass);


--
-- Name: resourceitem_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY resourceitem ALTER COLUMN resourceitem_id SET DEFAULT nextval('resourceitem_resourceitem_id_seq'::regclass);


--
-- Name: resourcetype_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY resourcetype ALTER COLUMN resourcetype_id SET DEFAULT nextval('resourcetype_resourcetype_id_seq'::regclass);


--
-- Name: rgroup_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY rgroup ALTER COLUMN rgroup_id SET DEFAULT nextval('rgroup_rgroup_id_seq'::regclass);


--
-- Name: service_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY service ALTER COLUMN service_id SET DEFAULT nextval('service_service_id_seq'::regclass);


--
-- Name: serviceproperty_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY serviceproperty ALTER COLUMN serviceproperty_id SET DEFAULT nextval('serviceproperty_serviceproperty_id_seq'::regclass);


--
-- Name: subscription_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY subscription ALTER COLUMN subscription_id SET DEFAULT nextval('subscription_subscription_id_seq'::regclass);


--
-- Name: subscriptionreception_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY subscriptionreception ALTER COLUMN subscriptionreception_id SET DEFAULT nextval('subscriptionreception_subscriptionreception_id_seq'::regclass);


--
-- Name: tasktype_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY tasktype ALTER COLUMN tasktype_id SET DEFAULT nextval('tasktype_tasktype_id_seq'::regclass);


--
-- Name: tasktypegroup_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY tasktypegroup ALTER COLUMN tasktypegroup_id SET DEFAULT nextval('tasktypegroup_tasktypegroup_id_seq'::regclass);


--
-- Name: timetask_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY timetask ALTER COLUMN timetask_id SET DEFAULT nextval('timetask_timetask_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY trusttoken ALTER COLUMN id SET DEFAULT nextval('trusttoken_id_seq'::regclass);


--
-- Name: group_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY ugroup ALTER COLUMN group_id SET DEFAULT nextval('ugroup_group_id_seq'::regclass);


--
-- Name: updated_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY updated ALTER COLUMN updated_id SET DEFAULT nextval('updated_updated_id_seq'::regclass);


--
-- Name: updatedlinks_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY updatedlinks ALTER COLUMN updatedlinks_id SET DEFAULT nextval('updatedlinks_updatedlinks_id_seq'::regclass);


--
-- Name: userobm_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY userobm ALTER COLUMN userobm_id SET DEFAULT nextval('userobm_userobm_id_seq'::regclass);


--
-- Name: userobmpref_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY userobmpref ALTER COLUMN userobmpref_id SET DEFAULT nextval('userobmpref_userobmpref_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY userpattern ALTER COLUMN id SET DEFAULT nextval('userpattern_id_seq'::regclass);


--
-- Name: usersystem_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY usersystem ALTER COLUMN usersystem_id SET DEFAULT nextval('usersystem_usersystem_id_seq'::regclass);


--
-- Name: website_id; Type: DEFAULT; Schema: public; Owner: obm
--

ALTER TABLE ONLY website ALTER COLUMN website_id SET DEFAULT nextval('website_website_id_seq'::regclass);


--
-- Name: _contactgroup_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY _contactgroup
    ADD CONSTRAINT _contactgroup_pkey PRIMARY KEY (contact_id, group_id);


--
-- Name: account_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY account
    ADD CONSTRAINT account_pkey PRIMARY KEY (account_id);


--
-- Name: accountentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY accountentity
    ADD CONSTRAINT accountentity_pkey PRIMARY KEY (accountentity_entity_id, accountentity_account_id);


--
-- Name: activeuserobm_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY activeuserobm
    ADD CONSTRAINT activeuserobm_pkey PRIMARY KEY (activeuserobm_sid);


--
-- Name: address_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY address
    ADD CONSTRAINT address_pkey PRIMARY KEY (address_id);


--
-- Name: addressbook_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY addressbook
    ADD CONSTRAINT addressbook_pkey PRIMARY KEY (id);


--
-- Name: addressbookentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY addressbookentity
    ADD CONSTRAINT addressbookentity_pkey PRIMARY KEY (addressbookentity_entity_id, addressbookentity_addressbook_id);


--
-- Name: calendarentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY calendarentity
    ADD CONSTRAINT calendarentity_pkey PRIMARY KEY (calendarentity_entity_id, calendarentity_calendar_id);


--
-- Name: campaign_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY campaign
    ADD CONSTRAINT campaign_pkey PRIMARY KEY (campaign_id);


--
-- Name: campaigndisabledentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY campaigndisabledentity
    ADD CONSTRAINT campaigndisabledentity_pkey PRIMARY KEY (campaigndisabledentity_entity_id, campaigndisabledentity_campaign_id);


--
-- Name: campaignentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY campaignentity
    ADD CONSTRAINT campaignentity_pkey PRIMARY KEY (campaignentity_entity_id, campaignentity_campaign_id);


--
-- Name: campaignmailcontent_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY campaignmailcontent
    ADD CONSTRAINT campaignmailcontent_pkey PRIMARY KEY (campaignmailcontent_id);


--
-- Name: campaignmailtarget_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY campaignmailtarget
    ADD CONSTRAINT campaignmailtarget_pkey PRIMARY KEY (campaignmailtarget_id);


--
-- Name: campaignpushtarget_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY campaignpushtarget
    ADD CONSTRAINT campaignpushtarget_pkey PRIMARY KEY (campaignpushtarget_id);


--
-- Name: campaigntarget_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY campaigntarget
    ADD CONSTRAINT campaigntarget_pkey PRIMARY KEY (campaigntarget_id);


--
-- Name: category_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY category
    ADD CONSTRAINT category_pkey PRIMARY KEY (category_id);


--
-- Name: categorycategory_categorycode_uniquekey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY category
    ADD CONSTRAINT categorycategory_categorycode_uniquekey UNIQUE (category_domain_id, category_category, category_code, category_label);


--
-- Name: categorylink_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY categorylink
    ADD CONSTRAINT categorylink_pkey PRIMARY KEY (categorylink_category_id, categorylink_entity_id);


--
-- Name: commitedoperation_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY commitedoperation
    ADD CONSTRAINT commitedoperation_pkey PRIMARY KEY (commitedoperation_hash_client_id);


--
-- Name: company_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY company
    ADD CONSTRAINT company_pkey PRIMARY KEY (company_id);


--
-- Name: companyactivity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY companyactivity
    ADD CONSTRAINT companyactivity_pkey PRIMARY KEY (companyactivity_id);


--
-- Name: companyentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY companyentity
    ADD CONSTRAINT companyentity_pkey PRIMARY KEY (companyentity_entity_id, companyentity_company_id);


--
-- Name: companynafcode_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY companynafcode
    ADD CONSTRAINT companynafcode_pkey PRIMARY KEY (companynafcode_id);


--
-- Name: companytype_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY companytype
    ADD CONSTRAINT companytype_pkey PRIMARY KEY (companytype_id);


--
-- Name: contact_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_pkey PRIMARY KEY (contact_id);


--
-- Name: contactentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY contactentity
    ADD CONSTRAINT contactentity_pkey PRIMARY KEY (contactentity_entity_id, contactentity_contact_id);


--
-- Name: contactfunction_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY contactfunction
    ADD CONSTRAINT contactfunction_pkey PRIMARY KEY (contactfunction_id);


--
-- Name: contactgroup_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY contactgroup
    ADD CONSTRAINT contactgroup_pkey PRIMARY KEY (contact_id, group_id);


--
-- Name: contract_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_pkey PRIMARY KEY (contract_id);


--
-- Name: contractentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY contractentity
    ADD CONSTRAINT contractentity_pkey PRIMARY KEY (contractentity_entity_id, contractentity_contract_id);


--
-- Name: contractpriority_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY contractpriority
    ADD CONSTRAINT contractpriority_pkey PRIMARY KEY (contractpriority_id);


--
-- Name: contractstatus_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY contractstatus
    ADD CONSTRAINT contractstatus_pkey PRIMARY KEY (contractstatus_id);


--
-- Name: contracttype_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY contracttype
    ADD CONSTRAINT contracttype_pkey PRIMARY KEY (contracttype_id);


--
-- Name: country_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY country
    ADD CONSTRAINT country_pkey PRIMARY KEY (country_iso3166, country_lang);


--
-- Name: cv_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY cv
    ADD CONSTRAINT cv_pkey PRIMARY KEY (cv_id);


--
-- Name: cventity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY cventity
    ADD CONSTRAINT cventity_pkey PRIMARY KEY (cventity_entity_id, cventity_cv_id);


--
-- Name: datasource_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY datasource
    ADD CONSTRAINT datasource_pkey PRIMARY KEY (datasource_id);


--
-- Name: deal_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_pkey PRIMARY KEY (deal_id);


--
-- Name: dealcompany_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY dealcompany
    ADD CONSTRAINT dealcompany_pkey PRIMARY KEY (dealcompany_id);


--
-- Name: dealcompanyrole_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY dealcompanyrole
    ADD CONSTRAINT dealcompanyrole_pkey PRIMARY KEY (dealcompanyrole_id);


--
-- Name: dealentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY dealentity
    ADD CONSTRAINT dealentity_pkey PRIMARY KEY (dealentity_entity_id, dealentity_deal_id);


--
-- Name: dealstatus_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY dealstatus
    ADD CONSTRAINT dealstatus_pkey PRIMARY KEY (dealstatus_id);


--
-- Name: dealtype_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY dealtype
    ADD CONSTRAINT dealtype_pkey PRIMARY KEY (dealtype_id);


--
-- Name: defaultodttemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY defaultodttemplate
    ADD CONSTRAINT defaultodttemplate_pkey PRIMARY KEY (defaultodttemplate_id);


--
-- Name: deleted_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY deleted
    ADD CONSTRAINT deleted_pkey PRIMARY KEY (deleted_id);


--
-- Name: deletedaddressbook_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY deletedaddressbook
    ADD CONSTRAINT deletedaddressbook_pkey PRIMARY KEY (addressbook_id);


--
-- Name: deletedevent_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY deletedevent
    ADD CONSTRAINT deletedevent_pkey PRIMARY KEY (deletedevent_id);


--
-- Name: deletedeventlink_deletedeventlink_id_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY deletedeventlink
    ADD CONSTRAINT deletedeventlink_deletedeventlink_id_pkey PRIMARY KEY (deletedeventlink_id);


--
-- Name: deletedsyncedaddressbook_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY deletedsyncedaddressbook
    ADD CONSTRAINT deletedsyncedaddressbook_pkey PRIMARY KEY (user_id, addressbook_id);


--
-- Name: deleteduser_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY deleteduser
    ADD CONSTRAINT deleteduser_pkey PRIMARY KEY (deleteduser_user_id);


--
-- Name: displaypref_key; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY displaypref
    ADD CONSTRAINT displaypref_key UNIQUE (display_user_id, display_entity, display_fieldname);


--
-- Name: displaypref_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY displaypref
    ADD CONSTRAINT displaypref_pkey PRIMARY KEY (display_id);


--
-- Name: document_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY document
    ADD CONSTRAINT document_pkey PRIMARY KEY (document_id);


--
-- Name: documententity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY documententity
    ADD CONSTRAINT documententity_pkey PRIMARY KEY (documententity_entity_id, documententity_document_id);


--
-- Name: documentlink_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY documentlink
    ADD CONSTRAINT documentlink_pkey PRIMARY KEY (documentlink_document_id, documentlink_entity_id);


--
-- Name: documentmimetype_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY documentmimetype
    ADD CONSTRAINT documentmimetype_pkey PRIMARY KEY (documentmimetype_id);


--
-- Name: domain_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY domain
    ADD CONSTRAINT domain_pkey PRIMARY KEY (domain_id);


--
-- Name: domainentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY domainentity
    ADD CONSTRAINT domainentity_pkey PRIMARY KEY (domainentity_entity_id, domainentity_domain_id);


--
-- Name: domainproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY domainproperty
    ADD CONSTRAINT domainproperty_pkey PRIMARY KEY (domainproperty_key);


--
-- Name: domainpropertyvalue_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY domainpropertyvalue
    ADD CONSTRAINT domainpropertyvalue_pkey PRIMARY KEY (domainpropertyvalue_domain_id, domainpropertyvalue_property_key);


--
-- Name: email_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY email
    ADD CONSTRAINT email_pkey PRIMARY KEY (email_id);


--
-- Name: entity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY entity
    ADD CONSTRAINT entity_pkey PRIMARY KEY (entity_id);


--
-- Name: entityright_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY entityright
    ADD CONSTRAINT entityright_pkey PRIMARY KEY (entityright_id);


--
-- Name: event_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY event
    ADD CONSTRAINT event_pkey PRIMARY KEY (event_id);


--
-- Name: eventcategory1_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY eventcategory1
    ADD CONSTRAINT eventcategory1_pkey PRIMARY KEY (eventcategory1_id);


--
-- Name: evententity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY evententity
    ADD CONSTRAINT evententity_pkey PRIMARY KEY (evententity_entity_id, evententity_event_id);


--
-- Name: eventlink_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY eventlink
    ADD CONSTRAINT eventlink_pkey PRIMARY KEY (eventlink_event_id, eventlink_entity_id);


--
-- Name: eventtag_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY eventtag
    ADD CONSTRAINT eventtag_pkey PRIMARY KEY (eventtag_id);


--
-- Name: eventtemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY eventtemplate
    ADD CONSTRAINT eventtemplate_pkey PRIMARY KEY (eventtemplate_id);


--
-- Name: field_id_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY field
    ADD CONSTRAINT field_id_pkey PRIMARY KEY (id);


--
-- Name: groupentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY groupentity
    ADD CONSTRAINT groupentity_pkey PRIMARY KEY (groupentity_entity_id, groupentity_group_id);


--
-- Name: groupgroup_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY groupgroup
    ADD CONSTRAINT groupgroup_pkey PRIMARY KEY (groupgroup_parent_id, groupgroup_child_id);


--
-- Name: host_host_name_key; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY host
    ADD CONSTRAINT host_host_name_key UNIQUE (host_name);


--
-- Name: host_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY host
    ADD CONSTRAINT host_pkey PRIMARY KEY (host_id);


--
-- Name: hostentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY hostentity
    ADD CONSTRAINT hostentity_pkey PRIMARY KEY (hostentity_entity_id, hostentity_host_id);


--
-- Name: im_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY im
    ADD CONSTRAINT im_pkey PRIMARY KEY (im_id);


--
-- Name: import_import_name_key; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY import
    ADD CONSTRAINT import_import_name_key UNIQUE (import_name);


--
-- Name: import_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY import
    ADD CONSTRAINT import_pkey PRIMARY KEY (import_id);


--
-- Name: importentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY importentity
    ADD CONSTRAINT importentity_pkey PRIMARY KEY (importentity_entity_id, importentity_import_id);


--
-- Name: incident_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_pkey PRIMARY KEY (incident_id);


--
-- Name: incidententity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY incidententity
    ADD CONSTRAINT incidententity_pkey PRIMARY KEY (incidententity_entity_id, incidententity_incident_id);


--
-- Name: incidentpriority_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY incidentpriority
    ADD CONSTRAINT incidentpriority_pkey PRIMARY KEY (incidentpriority_id);


--
-- Name: incidentresolutiontype_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY incidentresolutiontype
    ADD CONSTRAINT incidentresolutiontype_pkey PRIMARY KEY (incidentresolutiontype_id);


--
-- Name: incidentstatus_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY incidentstatus
    ADD CONSTRAINT incidentstatus_pkey PRIMARY KEY (incidentstatus_id);


--
-- Name: invoice_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY invoice
    ADD CONSTRAINT invoice_pkey PRIMARY KEY (invoice_id);


--
-- Name: invoiceentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY invoiceentity
    ADD CONSTRAINT invoiceentity_pkey PRIMARY KEY (invoiceentity_entity_id, invoiceentity_invoice_id);


--
-- Name: kind_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY kind
    ADD CONSTRAINT kind_pkey PRIMARY KEY (kind_id);


--
-- Name: lead_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY lead
    ADD CONSTRAINT lead_pkey PRIMARY KEY (lead_id);


--
-- Name: leadentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY leadentity
    ADD CONSTRAINT leadentity_pkey PRIMARY KEY (leadentity_entity_id, leadentity_lead_id);


--
-- Name: leadsource_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY leadsource
    ADD CONSTRAINT leadsource_pkey PRIMARY KEY (leadsource_id);


--
-- Name: leadstatus_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY leadstatus
    ADD CONSTRAINT leadstatus_pkey PRIMARY KEY (leadstatus_id);


--
-- Name: list_list_name_key; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY list
    ADD CONSTRAINT list_list_name_key UNIQUE (list_name);


--
-- Name: list_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY list
    ADD CONSTRAINT list_pkey PRIMARY KEY (list_id);


--
-- Name: listentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY listentity
    ADD CONSTRAINT listentity_pkey PRIMARY KEY (listentity_entity_id, listentity_list_id);


--
-- Name: mailboxentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY mailboxentity
    ADD CONSTRAINT mailboxentity_pkey PRIMARY KEY (mailboxentity_entity_id, mailboxentity_mailbox_id);


--
-- Name: mailinglist_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY mailinglist
    ADD CONSTRAINT mailinglist_pkey PRIMARY KEY (mailinglist_id);


--
-- Name: mailinglistemail_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY mailinglistemail
    ADD CONSTRAINT mailinglistemail_pkey PRIMARY KEY (mailinglistemail_id);


--
-- Name: mailshare_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY mailshare
    ADD CONSTRAINT mailshare_pkey PRIMARY KEY (mailshare_id);


--
-- Name: mailshareentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY mailshareentity
    ADD CONSTRAINT mailshareentity_pkey PRIMARY KEY (mailshareentity_entity_id, mailshareentity_mailshare_id);


--
-- Name: obmbookmark_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY obmbookmark
    ADD CONSTRAINT obmbookmark_pkey PRIMARY KEY (obmbookmark_id);


--
-- Name: obmbookmarkentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY obmbookmarkentity
    ADD CONSTRAINT obmbookmarkentity_pkey PRIMARY KEY (obmbookmarkentity_entity_id, obmbookmarkentity_obmbookmark_id);


--
-- Name: obmbookmarkproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY obmbookmarkproperty
    ADD CONSTRAINT obmbookmarkproperty_pkey PRIMARY KEY (obmbookmarkproperty_id);


--
-- Name: obminfo_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY obminfo
    ADD CONSTRAINT obminfo_pkey PRIMARY KEY (obminfo_name);


--
-- Name: obmsession_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY obmsession
    ADD CONSTRAINT obmsession_pkey PRIMARY KEY (obmsession_sid, obmsession_name);


--
-- Name: of_usergroup_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY of_usergroup
    ADD CONSTRAINT of_usergroup_pkey PRIMARY KEY (of_usergroup_group_id, of_usergroup_user_id);


--
-- Name: ogroup_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY ogroup
    ADD CONSTRAINT ogroup_pkey PRIMARY KEY (ogroup_id);


--
-- Name: ogroupentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY ogroupentity
    ADD CONSTRAINT ogroupentity_pkey PRIMARY KEY (ogroupentity_entity_id, ogroupentity_ogroup_id);


--
-- Name: ogrouplink_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY ogrouplink
    ADD CONSTRAINT ogrouplink_pkey PRIMARY KEY (ogrouplink_id);


--
-- Name: opush_device_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY opush_device
    ADD CONSTRAINT opush_device_pkey PRIMARY KEY (id);


--
-- Name: opush_event_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY opush_event_mapping
    ADD CONSTRAINT opush_event_mapping_pkey PRIMARY KEY (id);


--
-- Name: opush_folder_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY opush_folder_mapping
    ADD CONSTRAINT opush_folder_mapping_pkey PRIMARY KEY (id);


--
-- Name: opush_folder_snapshot_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY opush_folder_snapshot
    ADD CONSTRAINT opush_folder_snapshot_pkey PRIMARY KEY (id);


--
-- Name: opush_folder_sync_state_backend_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY opush_folder_sync_state_backend_mapping
    ADD CONSTRAINT opush_folder_sync_state_backend_mapping_pkey PRIMARY KEY (id);


--
-- Name: opush_folder_sync_state_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY opush_folder_sync_state
    ADD CONSTRAINT opush_folder_sync_state_pkey PRIMARY KEY (id);


--
-- Name: opush_folder_sync_state_sync_key_key; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY opush_folder_sync_state
    ADD CONSTRAINT opush_folder_sync_state_sync_key_key UNIQUE (sync_key);


--
-- Name: opush_sec_policy_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY opush_sec_policy
    ADD CONSTRAINT opush_sec_policy_pkey PRIMARY KEY (id);


--
-- Name: opush_sync_state_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY opush_sync_state
    ADD CONSTRAINT opush_sync_state_pkey PRIMARY KEY (id);


--
-- Name: opush_sync_state_sync_key_key; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY opush_sync_state
    ADD CONSTRAINT opush_sync_state_sync_key_key UNIQUE (sync_key);


--
-- Name: opush_synced_item_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY opush_synced_item
    ADD CONSTRAINT opush_synced_item_pkey PRIMARY KEY (id);


--
-- Name: organizationalchart_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY organizationalchart
    ADD CONSTRAINT organizationalchart_pkey PRIMARY KEY (organizationalchart_id);


--
-- Name: organizationalchartentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY organizationalchartentity
    ADD CONSTRAINT organizationalchartentity_pkey PRIMARY KEY (organizationalchartentity_entity_id, organizationalchartentity_organizationalchart_id);


--
-- Name: parentdeal_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY parentdeal
    ADD CONSTRAINT parentdeal_pkey PRIMARY KEY (parentdeal_id);


--
-- Name: parentdealentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY parentdealentity
    ADD CONSTRAINT parentdealentity_pkey PRIMARY KEY (parentdealentity_entity_id, parentdealentity_parentdeal_id);


--
-- Name: payment_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY payment
    ADD CONSTRAINT payment_pkey PRIMARY KEY (payment_id);


--
-- Name: paymententity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY paymententity
    ADD CONSTRAINT paymententity_pkey PRIMARY KEY (paymententity_entity_id, paymententity_payment_id);


--
-- Name: paymentinvoice_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY paymentinvoice
    ADD CONSTRAINT paymentinvoice_pkey PRIMARY KEY (paymentinvoice_invoice_id, paymentinvoice_payment_id);


--
-- Name: paymentkind_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY paymentkind
    ADD CONSTRAINT paymentkind_pkey PRIMARY KEY (paymentkind_id);


--
-- Name: phone_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY phone
    ADD CONSTRAINT phone_pkey PRIMARY KEY (phone_id);


--
-- Name: pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY calendarcolor
    ADD CONSTRAINT pkey PRIMARY KEY (user_id, entity_id);


--
-- Name: plannedtask_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY plannedtask
    ADD CONSTRAINT plannedtask_pkey PRIMARY KEY (plannedtask_id);


--
-- Name: profile_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY profile
    ADD CONSTRAINT profile_pkey PRIMARY KEY (profile_id);


--
-- Name: profileentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY profileentity
    ADD CONSTRAINT profileentity_pkey PRIMARY KEY (profileentity_entity_id, profileentity_profile_id);


--
-- Name: profilemodule_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY profilemodule
    ADD CONSTRAINT profilemodule_pkey PRIMARY KEY (profilemodule_id);


--
-- Name: profileproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY profileproperty
    ADD CONSTRAINT profileproperty_pkey PRIMARY KEY (profileproperty_id);


--
-- Name: profilesection_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY profilesection
    ADD CONSTRAINT profilesection_pkey PRIMARY KEY (profilesection_id);


--
-- Name: project_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_pkey PRIMARY KEY (project_id);


--
-- Name: projectclosing_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY projectclosing
    ADD CONSTRAINT projectclosing_pkey PRIMARY KEY (projectclosing_id);


--
-- Name: projectcv_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY projectcv
    ADD CONSTRAINT projectcv_pkey PRIMARY KEY (projectcv_project_id, projectcv_cv_id);


--
-- Name: projectentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY projectentity
    ADD CONSTRAINT projectentity_pkey PRIMARY KEY (projectentity_entity_id, projectentity_project_id);


--
-- Name: projectreftask_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY projectreftask
    ADD CONSTRAINT projectreftask_pkey PRIMARY KEY (projectreftask_id);


--
-- Name: projecttask_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY projecttask
    ADD CONSTRAINT projecttask_pkey PRIMARY KEY (projecttask_id);


--
-- Name: projectuser_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY projectuser
    ADD CONSTRAINT projectuser_pkey PRIMARY KEY (projectuser_id);


--
-- Name: publication_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY publication
    ADD CONSTRAINT publication_pkey PRIMARY KEY (publication_id);


--
-- Name: publicationentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY publicationentity
    ADD CONSTRAINT publicationentity_pkey PRIMARY KEY (publicationentity_entity_id, publicationentity_publication_id);


--
-- Name: publicationtype_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY publicationtype
    ADD CONSTRAINT publicationtype_pkey PRIMARY KEY (publicationtype_id);


--
-- Name: region_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY region
    ADD CONSTRAINT region_pkey PRIMARY KEY (region_id);


--
-- Name: resource_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY resource
    ADD CONSTRAINT resource_pkey PRIMARY KEY (resource_id);


--
-- Name: resource_resource_email_key; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY resource
    ADD CONSTRAINT resource_resource_email_key UNIQUE (resource_email);


--
-- Name: resourceentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY resourceentity
    ADD CONSTRAINT resourceentity_pkey PRIMARY KEY (resourceentity_entity_id, resourceentity_resource_id);


--
-- Name: resourcegroupentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY resourcegroupentity
    ADD CONSTRAINT resourcegroupentity_pkey PRIMARY KEY (resourcegroupentity_entity_id, resourcegroupentity_resourcegroup_id);


--
-- Name: resourceitem_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY resourceitem
    ADD CONSTRAINT resourceitem_pkey PRIMARY KEY (resourceitem_id);


--
-- Name: resourcetype_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY resourcetype
    ADD CONSTRAINT resourcetype_pkey PRIMARY KEY (resourcetype_id);


--
-- Name: rgroup_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY rgroup
    ADD CONSTRAINT rgroup_pkey PRIMARY KEY (rgroup_id);


--
-- Name: service_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY service
    ADD CONSTRAINT service_pkey PRIMARY KEY (service_id);


--
-- Name: serviceproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY serviceproperty
    ADD CONSTRAINT serviceproperty_pkey PRIMARY KEY (serviceproperty_id);


--
-- Name: ssoticket_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY ssoticket
    ADD CONSTRAINT ssoticket_pkey PRIMARY KEY (ssoticket_ticket);


--
-- Name: stats_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY stats
    ADD CONSTRAINT stats_pkey PRIMARY KEY (stats_name);


--
-- Name: subscription_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY subscription
    ADD CONSTRAINT subscription_pkey PRIMARY KEY (subscription_id);


--
-- Name: subscriptionentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY subscriptionentity
    ADD CONSTRAINT subscriptionentity_pkey PRIMARY KEY (subscriptionentity_entity_id, subscriptionentity_subscription_id);


--
-- Name: subscriptionreception_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY subscriptionreception
    ADD CONSTRAINT subscriptionreception_pkey PRIMARY KEY (subscriptionreception_id);


--
-- Name: syncedaddressbook_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY syncedaddressbook
    ADD CONSTRAINT syncedaddressbook_pkey PRIMARY KEY (user_id, addressbook_id);


--
-- Name: taskevent_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY taskevent
    ADD CONSTRAINT taskevent_pkey PRIMARY KEY (taskevent_event_id, taskevent_task_id);


--
-- Name: tasktype_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY tasktype
    ADD CONSTRAINT tasktype_pkey PRIMARY KEY (tasktype_id);


--
-- Name: tasktypegroup_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY tasktypegroup
    ADD CONSTRAINT tasktypegroup_pkey PRIMARY KEY (tasktypegroup_id);


--
-- Name: timetask_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY timetask
    ADD CONSTRAINT timetask_pkey PRIMARY KEY (timetask_id);


--
-- Name: trusttoken_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY trusttoken
    ADD CONSTRAINT trusttoken_pkey PRIMARY KEY (id);


--
-- Name: trusttoken_token_key; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY trusttoken
    ADD CONSTRAINT trusttoken_token_key UNIQUE (token);


--
-- Name: ugroup_group_gid_key; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY ugroup
    ADD CONSTRAINT ugroup_group_gid_key UNIQUE (group_gid, group_domain_id);


--
-- Name: ugroup_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY ugroup
    ADD CONSTRAINT ugroup_pkey PRIMARY KEY (group_id);


--
-- Name: unique_opush_ping_heartbeat_col_dev; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY opush_ping_heartbeat
    ADD CONSTRAINT unique_opush_ping_heartbeat_col_dev UNIQUE (device_id);


--
-- Name: updated_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY updated
    ADD CONSTRAINT updated_pkey PRIMARY KEY (updated_id);


--
-- Name: updatedlinks_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY updatedlinks
    ADD CONSTRAINT updatedlinks_pkey PRIMARY KEY (updatedlinks_id);


--
-- Name: userentity_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY userentity
    ADD CONSTRAINT userentity_pkey PRIMARY KEY (userentity_entity_id, userentity_user_id);


--
-- Name: userobm_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY userobm
    ADD CONSTRAINT userobm_pkey PRIMARY KEY (userobm_id);


--
-- Name: userobm_sessionlog_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY userobm_sessionlog
    ADD CONSTRAINT userobm_sessionlog_pkey PRIMARY KEY (userobm_sessionlog_sid);


--
-- Name: userobmgroup_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY userobmgroup
    ADD CONSTRAINT userobmgroup_pkey PRIMARY KEY (userobmgroup_group_id, userobmgroup_userobm_id);


--
-- Name: userobmpref_key; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY userobmpref
    ADD CONSTRAINT userobmpref_key UNIQUE (userobmpref_user_id, userobmpref_option);


--
-- Name: userobmpref_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY userobmpref
    ADD CONSTRAINT userobmpref_pkey PRIMARY KEY (userobmpref_id);


--
-- Name: userpattern_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY userpattern
    ADD CONSTRAINT userpattern_pkey PRIMARY KEY (id);


--
-- Name: userpattern_property_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY userpattern_property
    ADD CONSTRAINT userpattern_property_pkey PRIMARY KEY (userpattern_id, attribute);


--
-- Name: usersystem_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY usersystem
    ADD CONSTRAINT usersystem_pkey PRIMARY KEY (usersystem_id);


--
-- Name: usersystem_usersystem_login_key; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY usersystem
    ADD CONSTRAINT usersystem_usersystem_login_key UNIQUE (usersystem_login);


--
-- Name: website_pkey; Type: CONSTRAINT; Schema: public; Owner: obm; Tablespace: 
--

ALTER TABLE ONLY website
    ADD CONSTRAINT website_pkey PRIMARY KEY (website_id);


--
-- Name: _contactgroup_contact_id_contact_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX _contactgroup_contact_id_contact_id_fkey ON _contactgroup USING btree (contact_id);


--
-- Name: _contactgroup_group_id_group_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX _contactgroup_group_id_group_id_fkey ON _contactgroup USING btree (group_id);


--
-- Name: _userpattern_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX _userpattern_id_fkey ON _userpattern USING btree (id);


--
-- Name: _userpattern_pattern_idx; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX _userpattern_pattern_idx ON _userpattern USING btree (pattern);


--
-- Name: account_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX account_domain_id_fkey ON account USING btree (account_domain_id);


--
-- Name: account_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX account_usercreate_fkey ON account USING btree (account_usercreate);


--
-- Name: account_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX account_userupdate_fkey ON account USING btree (account_userupdate);


--
-- Name: accountentity_account_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX accountentity_account_id_fkey ON accountentity USING btree (accountentity_account_id);


--
-- Name: accountentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX accountentity_entity_id_fkey ON accountentity USING btree (accountentity_entity_id);


--
-- Name: activeuserobm_userobm_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX activeuserobm_userobm_id_fkey ON activeuserobm USING btree (activeuserobm_userobm_id);


--
-- Name: address_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX address_entity_id_fkey ON address USING btree (address_entity_id);


--
-- Name: addressbook_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX addressbook_domain_id_fkey ON addressbook USING btree (domain_id);


--
-- Name: addressbook_owner_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX addressbook_owner_fkey ON addressbook USING btree (owner);


--
-- Name: addressbook_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX addressbook_usercreate_fkey ON addressbook USING btree (usercreate);


--
-- Name: addressbook_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX addressbook_userupdate_fkey ON addressbook USING btree (userupdate);


--
-- Name: addressbookentity_addressbook_id_addressbook_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX addressbookentity_addressbook_id_addressbook_id_fkey ON addressbookentity USING btree (addressbookentity_addressbook_id);


--
-- Name: addressbookentity_entity_id_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX addressbookentity_entity_id_entity_id_fkey ON addressbookentity USING btree (addressbookentity_entity_id);


--
-- Name: bkm_idx_user; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX bkm_idx_user ON obmbookmark USING btree (obmbookmark_user_id);


--
-- Name: bkmprop_idx_bkm; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX bkmprop_idx_bkm ON obmbookmarkproperty USING btree (obmbookmarkproperty_bookmark_id);


--
-- Name: calendarentity_calendar_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX calendarentity_calendar_id_fkey ON calendarentity USING btree (calendarentity_calendar_id);


--
-- Name: calendarentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX calendarentity_entity_id_fkey ON calendarentity USING btree (calendarentity_entity_id);


--
-- Name: campaign_email_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX campaign_email_fkey ON campaign USING btree (campaign_email);


--
-- Name: campaign_parent_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX campaign_parent_fkey ON campaign USING btree (campaign_parent);


--
-- Name: campaigndisabledentity_campaign_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX campaigndisabledentity_campaign_id_fkey ON campaigndisabledentity USING btree (campaigndisabledentity_campaign_id);


--
-- Name: campaigndisabledentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX campaigndisabledentity_entity_id_fkey ON campaigndisabledentity USING btree (campaigndisabledentity_entity_id);


--
-- Name: campaignentity_campaign_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX campaignentity_campaign_id_fkey ON campaignentity USING btree (campaignentity_campaign_id);


--
-- Name: campaignentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX campaignentity_entity_id_fkey ON campaignentity USING btree (campaignentity_entity_id);


--
-- Name: campaignmailtarget_campaign_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX campaignmailtarget_campaign_id_fkey ON campaignmailtarget USING btree (campaignmailtarget_campaign_id);


--
-- Name: campaignmailtarget_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX campaignmailtarget_entity_id_fkey ON campaignmailtarget USING btree (campaignmailtarget_entity_id);


--
-- Name: campaigntarget_campaign_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX campaigntarget_campaign_id_fkey ON campaigntarget USING btree (campaigntarget_campaign_id);


--
-- Name: campaigntarget_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX campaigntarget_entity_id_fkey ON campaigntarget USING btree (campaigntarget_entity_id);


--
-- Name: cat_idx_cat; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX cat_idx_cat ON category USING btree (category_category);


--
-- Name: category_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX category_domain_id_fkey ON category USING btree (category_domain_id);


--
-- Name: category_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX category_usercreate_fkey ON category USING btree (category_usercreate);


--
-- Name: category_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX category_userupdate_fkey ON category USING btree (category_userupdate);


--
-- Name: categorylink_category_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX categorylink_category_id_fkey ON categorylink USING btree (categorylink_category_id);


--
-- Name: categorylink_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX categorylink_entity_id_fkey ON categorylink USING btree (categorylink_entity_id);


--
-- Name: catl_idx_cat; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX catl_idx_cat ON categorylink USING btree (categorylink_category);


--
-- Name: catl_idx_entid; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX catl_idx_entid ON categorylink USING btree (categorylink_entity_id);


--
-- Name: company_activity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX company_activity_id_fkey ON company USING btree (company_activity_id);


--
-- Name: company_datasource_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX company_datasource_id_fkey ON company USING btree (company_datasource_id);


--
-- Name: company_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX company_domain_id_fkey ON company USING btree (company_domain_id);


--
-- Name: company_marketingmanager_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX company_marketingmanager_id_fkey ON company USING btree (company_marketingmanager_id);


--
-- Name: company_nafcode_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX company_nafcode_id_fkey ON company USING btree (company_nafcode_id);


--
-- Name: company_type_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX company_type_id_fkey ON company USING btree (company_type_id);


--
-- Name: company_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX company_usercreate_fkey ON company USING btree (company_usercreate);


--
-- Name: company_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX company_userupdate_fkey ON company USING btree (company_userupdate);


--
-- Name: companyactivity_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX companyactivity_domain_id_fkey ON companyactivity USING btree (companyactivity_domain_id);


--
-- Name: companyactivity_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX companyactivity_usercreate_fkey ON companyactivity USING btree (companyactivity_usercreate);


--
-- Name: companyactivity_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX companyactivity_userupdate_fkey ON companyactivity USING btree (companyactivity_userupdate);


--
-- Name: companyentity_company_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX companyentity_company_id_fkey ON companyentity USING btree (companyentity_company_id);


--
-- Name: companyentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX companyentity_entity_id_fkey ON companyentity USING btree (companyentity_entity_id);


--
-- Name: companynafcode_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX companynafcode_domain_id_fkey ON companynafcode USING btree (companynafcode_domain_id);


--
-- Name: companynafcode_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX companynafcode_usercreate_fkey ON companynafcode USING btree (companynafcode_usercreate);


--
-- Name: companynafcode_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX companynafcode_userupdate_fkey ON companynafcode USING btree (companynafcode_userupdate);


--
-- Name: companytype_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX companytype_domain_id_fkey ON companytype USING btree (companytype_domain_id);


--
-- Name: companytype_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX companytype_usercreate_fkey ON companytype USING btree (companytype_usercreate);


--
-- Name: companytype_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX companytype_userupdate_fkey ON companytype USING btree (companytype_userupdate);


--
-- Name: contact_addressbook_id_addressbook_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contact_addressbook_id_addressbook_id_fkey ON contact USING btree (contact_addressbook_id);


--
-- Name: contact_anniversary_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contact_anniversary_id_fkey ON contact USING btree (contact_anniversary_id);


--
-- Name: contact_birthday_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contact_birthday_id_fkey ON contact USING btree (contact_birthday_id);


--
-- Name: contact_company_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contact_company_id_fkey ON contact USING btree (contact_company_id);


--
-- Name: contact_datasource_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contact_datasource_id_fkey ON contact USING btree (contact_datasource_id);


--
-- Name: contact_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contact_domain_id_fkey ON contact USING btree (contact_domain_id);


--
-- Name: contact_function_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contact_function_id_fkey ON contact USING btree (contact_function_id);


--
-- Name: contact_kind_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contact_kind_id_fkey ON contact USING btree (contact_kind_id);


--
-- Name: contact_marketingmanager_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contact_marketingmanager_id_fkey ON contact USING btree (contact_marketingmanager_id);


--
-- Name: contact_photo_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contact_photo_id_fkey ON contact USING btree (contact_photo_id);


--
-- Name: contact_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contact_usercreate_fkey ON contact USING btree (contact_usercreate);


--
-- Name: contact_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contact_userupdate_fkey ON contact USING btree (contact_userupdate);


--
-- Name: contactentity_contact_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contactentity_contact_id_fkey ON contactentity USING btree (contactentity_contact_id);


--
-- Name: contactentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contactentity_entity_id_fkey ON contactentity USING btree (contactentity_entity_id);


--
-- Name: contactfunction_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contactfunction_domain_id_fkey ON contactfunction USING btree (contactfunction_domain_id);


--
-- Name: contactfunction_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contactfunction_usercreate_fkey ON contactfunction USING btree (contactfunction_usercreate);


--
-- Name: contactfunction_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contactfunction_userupdate_fkey ON contactfunction USING btree (contactfunction_userupdate);


--
-- Name: contactgroup_contact_id_contact_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contactgroup_contact_id_contact_id_fkey ON contactgroup USING btree (contact_id);


--
-- Name: contactgroup_group_id_group_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contactgroup_group_id_group_id_fkey ON contactgroup USING btree (group_id);


--
-- Name: contactlist_contact_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contactlist_contact_id_fkey ON contactlist USING btree (contactlist_contact_id);


--
-- Name: contactlist_list_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contactlist_list_id_fkey ON contactlist USING btree (contactlist_list_id);


--
-- Name: contract_company_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contract_company_id_fkey ON contract USING btree (contract_company_id);


--
-- Name: contract_contact1_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contract_contact1_id_fkey ON contract USING btree (contract_contact1_id);


--
-- Name: contract_contact2_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contract_contact2_id_fkey ON contract USING btree (contract_contact2_id);


--
-- Name: contract_deal_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contract_deal_id_fkey ON contract USING btree (contract_deal_id);


--
-- Name: contract_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contract_domain_id_fkey ON contract USING btree (contract_domain_id);


--
-- Name: contract_marketmanager_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contract_marketmanager_id_fkey ON contract USING btree (contract_marketmanager_id);


--
-- Name: contract_priority_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contract_priority_id_fkey ON contract USING btree (contract_priority_id);


--
-- Name: contract_status_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contract_status_id_fkey ON contract USING btree (contract_status_id);


--
-- Name: contract_techmanager_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contract_techmanager_id_fkey ON contract USING btree (contract_techmanager_id);


--
-- Name: contract_type_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contract_type_id_fkey ON contract USING btree (contract_type_id);


--
-- Name: contract_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contract_usercreate_fkey ON contract USING btree (contract_usercreate);


--
-- Name: contract_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contract_userupdate_fkey ON contract USING btree (contract_userupdate);


--
-- Name: contractentity_contract_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contractentity_contract_id_fkey ON contractentity USING btree (contractentity_contract_id);


--
-- Name: contractentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contractentity_entity_id_fkey ON contractentity USING btree (contractentity_entity_id);


--
-- Name: contractpriority_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contractpriority_domain_id_fkey ON contractpriority USING btree (contractpriority_domain_id);


--
-- Name: contractpriority_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contractpriority_usercreate_fkey ON contractpriority USING btree (contractpriority_usercreate);


--
-- Name: contractpriority_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contractpriority_userupdate_fkey ON contractpriority USING btree (contractpriority_userupdate);


--
-- Name: contractstatus_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contractstatus_domain_id_fkey ON contractstatus USING btree (contractstatus_domain_id);


--
-- Name: contractstatus_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contractstatus_usercreate_fkey ON contractstatus USING btree (contractstatus_usercreate);


--
-- Name: contractstatus_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contractstatus_userupdate_fkey ON contractstatus USING btree (contractstatus_userupdate);


--
-- Name: contracttype_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contracttype_domain_id_fkey ON contracttype USING btree (contracttype_domain_id);


--
-- Name: contracttype_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contracttype_usercreate_fkey ON contracttype USING btree (contracttype_usercreate);


--
-- Name: contracttype_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX contracttype_userupdate_fkey ON contracttype USING btree (contracttype_userupdate);


--
-- Name: country_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX country_domain_id_fkey ON country USING btree (country_domain_id);


--
-- Name: country_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX country_usercreate_fkey ON country USING btree (country_usercreate);


--
-- Name: country_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX country_userupdate_fkey ON country USING btree (country_userupdate);


--
-- Name: cv_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX cv_domain_id_fkey ON cv USING btree (cv_domain_id);


--
-- Name: cv_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX cv_usercreate_fkey ON cv USING btree (cv_usercreate);


--
-- Name: cv_userobm_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX cv_userobm_id_fkey ON cv USING btree (cv_userobm_id);


--
-- Name: cv_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX cv_userupdate_fkey ON cv USING btree (cv_userupdate);


--
-- Name: cventity_cv_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX cventity_cv_id_fkey ON cventity USING btree (cventity_cv_id);


--
-- Name: cventity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX cventity_entity_id_fkey ON cventity USING btree (cventity_entity_id);


--
-- Name: datasource_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX datasource_domain_id_fkey ON datasource USING btree (datasource_domain_id);


--
-- Name: datasource_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX datasource_usercreate_fkey ON datasource USING btree (datasource_usercreate);


--
-- Name: datasource_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX datasource_userupdate_fkey ON datasource USING btree (datasource_userupdate);


--
-- Name: deal_company_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX deal_company_id_fkey ON deal USING btree (deal_company_id);


--
-- Name: deal_contact1_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX deal_contact1_id_fkey ON deal USING btree (deal_contact1_id);


--
-- Name: deal_contact2_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX deal_contact2_id_fkey ON deal USING btree (deal_contact2_id);


--
-- Name: deal_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX deal_domain_id_fkey ON deal USING btree (deal_domain_id);


--
-- Name: deal_marketingmanager_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX deal_marketingmanager_id_fkey ON deal USING btree (deal_marketingmanager_id);


--
-- Name: deal_parentdeal_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX deal_parentdeal_id_fkey ON deal USING btree (deal_parentdeal_id);


--
-- Name: deal_region_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX deal_region_id_fkey ON deal USING btree (deal_region_id);


--
-- Name: deal_source_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX deal_source_id_fkey ON deal USING btree (deal_source_id);


--
-- Name: deal_tasktype_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX deal_tasktype_id_fkey ON deal USING btree (deal_tasktype_id);


--
-- Name: deal_technicalmanager_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX deal_technicalmanager_id_fkey ON deal USING btree (deal_technicalmanager_id);


--
-- Name: deal_type_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX deal_type_id_fkey ON deal USING btree (deal_type_id);


--
-- Name: deal_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX deal_usercreate_fkey ON deal USING btree (deal_usercreate);


--
-- Name: deal_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX deal_userupdate_fkey ON deal USING btree (deal_userupdate);


--
-- Name: dealcompany_company_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX dealcompany_company_id_fkey ON dealcompany USING btree (dealcompany_company_id);


--
-- Name: dealcompany_deal_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX dealcompany_deal_id_fkey ON dealcompany USING btree (dealcompany_deal_id);


--
-- Name: dealcompany_idx_deal; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX dealcompany_idx_deal ON dealcompany USING btree (dealcompany_deal_id);


--
-- Name: dealcompany_role_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX dealcompany_role_id_fkey ON dealcompany USING btree (dealcompany_role_id);


--
-- Name: dealcompany_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX dealcompany_usercreate_fkey ON dealcompany USING btree (dealcompany_usercreate);


--
-- Name: dealcompany_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX dealcompany_userupdate_fkey ON dealcompany USING btree (dealcompany_userupdate);


--
-- Name: dealcompanyrole_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX dealcompanyrole_domain_id_fkey ON dealcompanyrole USING btree (dealcompanyrole_domain_id);


--
-- Name: dealcompanyrole_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX dealcompanyrole_usercreate_fkey ON dealcompanyrole USING btree (dealcompanyrole_usercreate);


--
-- Name: dealcompanyrole_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX dealcompanyrole_userupdate_fkey ON dealcompanyrole USING btree (dealcompanyrole_userupdate);


--
-- Name: dealentity_deal_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX dealentity_deal_id_fkey ON dealentity USING btree (dealentity_deal_id);


--
-- Name: dealentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX dealentity_entity_id_fkey ON dealentity USING btree (dealentity_entity_id);


--
-- Name: dealstatus_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX dealstatus_domain_id_fkey ON dealstatus USING btree (dealstatus_domain_id);


--
-- Name: dealstatus_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX dealstatus_usercreate_fkey ON dealstatus USING btree (dealstatus_usercreate);


--
-- Name: dealstatus_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX dealstatus_userupdate_fkey ON dealstatus USING btree (dealstatus_userupdate);


--
-- Name: dealtype_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX dealtype_domain_id_fkey ON dealtype USING btree (dealtype_domain_id);


--
-- Name: dealtype_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX dealtype_usercreate_fkey ON dealtype USING btree (dealtype_usercreate);


--
-- Name: dealtype_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX dealtype_userupdate_fkey ON dealtype USING btree (dealtype_userupdate);


--
-- Name: defaultodttemplate_document_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX defaultodttemplate_document_id_fkey ON defaultodttemplate USING btree (defaultodttemplate_document_id);


--
-- Name: defaultodttemplate_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX defaultodttemplate_domain_id_fkey ON defaultodttemplate USING btree (defaultodttemplate_domain_id);


--
-- Name: deleted_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX deleted_domain_id_fkey ON deleted USING btree (deleted_domain_id);


--
-- Name: deleted_user_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX deleted_user_id_fkey ON deleted USING btree (deleted_user_id);


--
-- Name: display_user_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX display_user_id_fkey ON displaypref USING btree (display_user_id);


--
-- Name: displaypref_entity_index; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX displaypref_entity_index ON displaypref USING btree (display_entity);


--
-- Name: displaypref_user_id_index; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX displaypref_user_id_index ON displaypref USING btree (display_user_id);


--
-- Name: document_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX document_domain_id_fkey ON document USING btree (document_domain_id);


--
-- Name: document_mimetype_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX document_mimetype_id_fkey ON document USING btree (document_mimetype_id);


--
-- Name: document_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX document_usercreate_fkey ON document USING btree (document_usercreate);


--
-- Name: document_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX document_userupdate_fkey ON document USING btree (document_userupdate);


--
-- Name: documententity_document_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX documententity_document_id_fkey ON documententity USING btree (documententity_document_id);


--
-- Name: documententity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX documententity_entity_id_fkey ON documententity USING btree (documententity_entity_id);


--
-- Name: documentlink_document_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX documentlink_document_id_fkey ON documentlink USING btree (documentlink_document_id);


--
-- Name: documentlink_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX documentlink_entity_id_fkey ON documentlink USING btree (documentlink_entity_id);


--
-- Name: documentlink_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX documentlink_usercreate_fkey ON documentlink USING btree (documentlink_usercreate);


--
-- Name: documentmimetype_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX documentmimetype_domain_id_fkey ON documentmimetype USING btree (documentmimetype_domain_id);


--
-- Name: documentmimetype_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX documentmimetype_usercreate_fkey ON documentmimetype USING btree (documentmimetype_usercreate);


--
-- Name: documentmimetype_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX documentmimetype_userupdate_fkey ON documentmimetype USING btree (documentmimetype_userupdate);


--
-- Name: domain_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX domain_usercreate_fkey ON domain USING btree (domain_usercreate);


--
-- Name: domain_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX domain_userupdate_fkey ON domain USING btree (domain_userupdate);


--
-- Name: domainentity_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX domainentity_domain_id_fkey ON domainentity USING btree (domainentity_domain_id);


--
-- Name: domainentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX domainentity_entity_id_fkey ON domainentity USING btree (domainentity_entity_id);


--
-- Name: domainpropertyvalue_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX domainpropertyvalue_domain_id_fkey ON domainpropertyvalue USING btree (domainpropertyvalue_domain_id);


--
-- Name: email_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX email_entity_id_fkey ON email USING btree (email_entity_id);


--
-- Name: entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX entity_id_fkey ON calendarcolor USING btree (entity_id);


--
-- Name: entityright_consumer_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX entityright_consumer_id_fkey ON entityright USING btree (entityright_consumer_id);


--
-- Name: entityright_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX entityright_entity_id_fkey ON entityright USING btree (entityright_entity_id);


--
-- Name: event_category1_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX event_category1_id_fkey ON event USING btree (event_category1_id);


--
-- Name: event_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX event_domain_id_fkey ON event USING btree (event_domain_id);


--
-- Name: event_owner_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX event_owner_fkey ON event USING btree (event_owner);


--
-- Name: event_timecreate_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX event_timecreate_id_fkey ON event USING btree (event_timecreate);


--
-- Name: event_timeupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX event_timeupdate_fkey ON event USING btree (event_timeupdate);


--
-- Name: event_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX event_usercreate_fkey ON event USING btree (event_usercreate);


--
-- Name: event_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX event_userupdate_fkey ON event USING btree (event_userupdate);


--
-- Name: eventalert_event_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX eventalert_event_id_fkey ON eventalert USING btree (eventalert_event_id);


--
-- Name: eventalert_user_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX eventalert_user_id_fkey ON eventalert USING btree (eventalert_user_id);


--
-- Name: eventalert_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX eventalert_usercreate_fkey ON eventalert USING btree (eventalert_usercreate);


--
-- Name: eventalert_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX eventalert_userupdate_fkey ON eventalert USING btree (eventalert_userupdate);


--
-- Name: eventcategory1_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX eventcategory1_domain_id_fkey ON eventcategory1 USING btree (eventcategory1_domain_id);


--
-- Name: eventcategory1_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX eventcategory1_usercreate_fkey ON eventcategory1 USING btree (eventcategory1_usercreate);


--
-- Name: eventcategory1_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX eventcategory1_userupdate_fkey ON eventcategory1 USING btree (eventcategory1_userupdate);


--
-- Name: evententity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX evententity_entity_id_fkey ON evententity USING btree (evententity_entity_id);


--
-- Name: evententity_event_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX evententity_event_id_fkey ON evententity USING btree (evententity_event_id);


--
-- Name: eventexception_child_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX eventexception_child_id_fkey ON eventexception USING btree (eventexception_child_id);


--
-- Name: eventexception_parent_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX eventexception_parent_id_fkey ON eventexception USING btree (eventexception_parent_id);


--
-- Name: eventexception_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX eventexception_usercreate_fkey ON eventexception USING btree (eventexception_usercreate);


--
-- Name: eventexception_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX eventexception_userupdate_fkey ON eventexception USING btree (eventexception_userupdate);


--
-- Name: eventlink_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX eventlink_entity_id_fkey ON eventlink USING btree (eventlink_entity_id);


--
-- Name: eventlink_event_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX eventlink_event_id_fkey ON eventlink USING btree (eventlink_event_id);


--
-- Name: eventlink_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX eventlink_usercreate_fkey ON eventlink USING btree (eventlink_usercreate);


--
-- Name: eventlink_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX eventlink_userupdate_fkey ON eventlink USING btree (eventlink_userupdate);


--
-- Name: eventtemplate_category1_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX eventtemplate_category1_id_fkey ON eventtemplate USING btree (eventtemplate_category1_id);


--
-- Name: eventtemplate_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX eventtemplate_domain_id_fkey ON eventtemplate USING btree (eventtemplate_domain_id);


--
-- Name: eventtemplate_owner_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX eventtemplate_owner_fkey ON eventtemplate USING btree (eventtemplate_owner);


--
-- Name: eventtemplate_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX eventtemplate_usercreate_fkey ON eventtemplate USING btree (eventtemplate_usercreate);


--
-- Name: eventtemplate_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX eventtemplate_userupdate_fkey ON eventtemplate USING btree (eventtemplate_userupdate);


--
-- Name: fki_deletedeventlink_event_id_event_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX fki_deletedeventlink_event_id_event_id_fkey ON deletedeventlink USING btree (deletedeventlink_event_id);


--
-- Name: fki_deletedeventlink_userobm_id_userobm_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX fki_deletedeventlink_userobm_id_userobm_id_fkey ON deletedeventlink USING btree (deletedeventlink_userobm_id);


--
-- Name: group_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX group_domain_id_fkey ON ugroup USING btree (group_domain_id);


--
-- Name: group_manager_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX group_manager_id_fkey ON ugroup USING btree (group_manager_id);


--
-- Name: group_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX group_usercreate_fkey ON ugroup USING btree (group_usercreate);


--
-- Name: group_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX group_userupdate_fkey ON ugroup USING btree (group_userupdate);


--
-- Name: groupentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX groupentity_entity_id_fkey ON groupentity USING btree (groupentity_entity_id);


--
-- Name: groupentity_group_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX groupentity_group_id_fkey ON groupentity USING btree (groupentity_group_id);


--
-- Name: groupgroup_child_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX groupgroup_child_id_fkey ON groupgroup USING btree (groupgroup_child_id);


--
-- Name: groupgroup_parent_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX groupgroup_parent_id_fkey ON groupgroup USING btree (groupgroup_parent_id);


--
-- Name: host_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX host_domain_id_fkey ON host USING btree (host_domain_id);


--
-- Name: host_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX host_usercreate_fkey ON host USING btree (host_usercreate);


--
-- Name: host_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX host_userupdate_fkey ON host USING btree (host_userupdate);


--
-- Name: hostentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX hostentity_entity_id_fkey ON hostentity USING btree (hostentity_entity_id);


--
-- Name: hostentity_host_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX hostentity_host_id_fkey ON hostentity USING btree (hostentity_host_id);


--
-- Name: idx_dce_event_id; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX idx_dce_event_id ON deletedevent USING btree (deletedevent_event_id);


--
-- Name: idx_dce_user_id; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX idx_dce_user_id ON deletedevent USING btree (deletedevent_user_id);


--
-- Name: idx_eventalert_user; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX idx_eventalert_user ON eventalert USING btree (eventalert_user_id);


--
-- Name: im_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX im_entity_id_fkey ON im USING btree (im_entity_id);


--
-- Name: import_datasource_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX import_datasource_id_fkey ON import USING btree (import_datasource_id);


--
-- Name: import_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX import_domain_id_fkey ON import USING btree (import_domain_id);


--
-- Name: import_marketingmanager_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX import_marketingmanager_id_fkey ON import USING btree (import_marketingmanager_id);


--
-- Name: import_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX import_usercreate_fkey ON import USING btree (import_usercreate);


--
-- Name: import_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX import_userupdate_fkey ON import USING btree (import_userupdate);


--
-- Name: importentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX importentity_entity_id_fkey ON importentity USING btree (importentity_entity_id);


--
-- Name: importentity_import_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX importentity_import_id_fkey ON importentity USING btree (importentity_import_id);


--
-- Name: incident_contract_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX incident_contract_id_fkey ON incident USING btree (incident_contract_id);


--
-- Name: incident_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX incident_domain_id_fkey ON incident USING btree (incident_domain_id);


--
-- Name: incident_logger_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX incident_logger_fkey ON incident USING btree (incident_logger);


--
-- Name: incident_owner_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX incident_owner_fkey ON incident USING btree (incident_owner);


--
-- Name: incident_priority_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX incident_priority_id_fkey ON incident USING btree (incident_priority_id);


--
-- Name: incident_resolutiontype_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX incident_resolutiontype_id_fkey ON incident USING btree (incident_resolutiontype_id);


--
-- Name: incident_status_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX incident_status_id_fkey ON incident USING btree (incident_status_id);


--
-- Name: incident_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX incident_usercreate_fkey ON incident USING btree (incident_usercreate);


--
-- Name: incident_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX incident_userupdate_fkey ON incident USING btree (incident_userupdate);


--
-- Name: incidententity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX incidententity_entity_id_fkey ON incidententity USING btree (incidententity_entity_id);


--
-- Name: incidententity_incident_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX incidententity_incident_id_fkey ON incidententity USING btree (incidententity_incident_id);


--
-- Name: incidentpriority_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX incidentpriority_domain_id_fkey ON incidentpriority USING btree (incidentpriority_domain_id);


--
-- Name: incidentpriority_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX incidentpriority_usercreate_fkey ON incidentpriority USING btree (incidentpriority_usercreate);


--
-- Name: incidentpriority_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX incidentpriority_userupdate_fkey ON incidentpriority USING btree (incidentpriority_userupdate);


--
-- Name: incidentresolutiontype_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX incidentresolutiontype_domain_id_fkey ON incidentresolutiontype USING btree (incidentresolutiontype_domain_id);


--
-- Name: incidentresolutiontype_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX incidentresolutiontype_usercreate_fkey ON incidentresolutiontype USING btree (incidentresolutiontype_usercreate);


--
-- Name: incidentresolutiontype_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX incidentresolutiontype_userupdate_fkey ON incidentresolutiontype USING btree (incidentresolutiontype_userupdate);


--
-- Name: incidentstatus_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX incidentstatus_domain_id_fkey ON incidentstatus USING btree (incidentstatus_domain_id);


--
-- Name: incidentstatus_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX incidentstatus_usercreate_fkey ON incidentstatus USING btree (incidentstatus_usercreate);


--
-- Name: incidentstatus_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX incidentstatus_userupdate_fkey ON incidentstatus USING btree (incidentstatus_userupdate);


--
-- Name: invoice_company_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX invoice_company_id_fkey ON invoice USING btree (invoice_company_id);


--
-- Name: invoice_deal_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX invoice_deal_id_fkey ON invoice USING btree (invoice_deal_id);


--
-- Name: invoice_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX invoice_domain_id_fkey ON invoice USING btree (invoice_domain_id);


--
-- Name: invoice_project_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX invoice_project_id_fkey ON invoice USING btree (invoice_project_id);


--
-- Name: invoice_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX invoice_usercreate_fkey ON invoice USING btree (invoice_usercreate);


--
-- Name: invoice_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX invoice_userupdate_fkey ON invoice USING btree (invoice_userupdate);


--
-- Name: invoiceentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX invoiceentity_entity_id_fkey ON invoiceentity USING btree (invoiceentity_entity_id);


--
-- Name: invoiceentity_invoice_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX invoiceentity_invoice_id_fkey ON invoiceentity USING btree (invoiceentity_invoice_id);


--
-- Name: k_login_user_userobm_index; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX k_login_user_userobm_index ON userobm USING btree (userobm_login);


--
-- Name: k_uid_user_userobm_index; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX k_uid_user_userobm_index ON userobm USING btree (userobm_uid);


--
-- Name: kind_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX kind_domain_id_fkey ON kind USING btree (kind_domain_id);


--
-- Name: kind_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX kind_usercreate_fkey ON kind USING btree (kind_usercreate);


--
-- Name: kind_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX kind_userupdate_fkey ON kind USING btree (kind_userupdate);


--
-- Name: lead_company_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX lead_company_id_fkey ON lead USING btree (lead_company_id);


--
-- Name: lead_contact_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX lead_contact_id_fkey ON lead USING btree (lead_contact_id);


--
-- Name: lead_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX lead_domain_id_fkey ON lead USING btree (lead_domain_id);


--
-- Name: lead_manager_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX lead_manager_id_fkey ON lead USING btree (lead_manager_id);


--
-- Name: lead_source_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX lead_source_id_fkey ON lead USING btree (lead_source_id);


--
-- Name: lead_status_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX lead_status_id_fkey ON lead USING btree (lead_status_id);


--
-- Name: lead_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX lead_usercreate_fkey ON lead USING btree (lead_usercreate);


--
-- Name: lead_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX lead_userupdate_fkey ON lead USING btree (lead_userupdate);


--
-- Name: leadentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX leadentity_entity_id_fkey ON leadentity USING btree (leadentity_entity_id);


--
-- Name: leadentity_lead_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX leadentity_lead_id_fkey ON leadentity USING btree (leadentity_lead_id);


--
-- Name: leadsource_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX leadsource_domain_id_fkey ON leadsource USING btree (leadsource_domain_id);


--
-- Name: leadsource_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX leadsource_usercreate_fkey ON leadsource USING btree (leadsource_usercreate);


--
-- Name: leadsource_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX leadsource_userupdate_fkey ON leadsource USING btree (leadsource_userupdate);


--
-- Name: leadstatus_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX leadstatus_domain_id_fkey ON leadstatus USING btree (leadstatus_domain_id);


--
-- Name: leadstatus_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX leadstatus_usercreate_fkey ON leadstatus USING btree (leadstatus_usercreate);


--
-- Name: leadstatus_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX leadstatus_userupdate_fkey ON leadstatus USING btree (leadstatus_userupdate);


--
-- Name: list_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX list_domain_id_fkey ON list USING btree (list_domain_id);


--
-- Name: list_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX list_usercreate_fkey ON list USING btree (list_usercreate);


--
-- Name: list_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX list_userupdate_fkey ON list USING btree (list_userupdate);


--
-- Name: listentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX listentity_entity_id_fkey ON listentity USING btree (listentity_entity_id);


--
-- Name: listentity_list_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX listentity_list_id_fkey ON listentity USING btree (listentity_list_id);


--
-- Name: mailboxentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX mailboxentity_entity_id_fkey ON mailboxentity USING btree (mailboxentity_entity_id);


--
-- Name: mailboxentity_mailbox_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX mailboxentity_mailbox_id_fkey ON mailboxentity USING btree (mailboxentity_mailbox_id);


--
-- Name: mailshare_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX mailshare_domain_id_fkey ON mailshare USING btree (mailshare_domain_id);


--
-- Name: mailshare_mail_server_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX mailshare_mail_server_id_fkey ON mailshare USING btree (mailshare_mail_server_id);


--
-- Name: mailshare_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX mailshare_usercreate_fkey ON mailshare USING btree (mailshare_usercreate);


--
-- Name: mailshare_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX mailshare_userupdate_fkey ON mailshare USING btree (mailshare_userupdate);


--
-- Name: mailshareentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX mailshareentity_entity_id_fkey ON mailshareentity USING btree (mailshareentity_entity_id);


--
-- Name: mailshareentity_mailshare_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX mailshareentity_mailshare_id_fkey ON mailshareentity USING btree (mailshareentity_mailshare_id);


--
-- Name: obmbookmark_user_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX obmbookmark_user_id_fkey ON obmbookmark USING btree (obmbookmark_user_id);


--
-- Name: obmbookmarkentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX obmbookmarkentity_entity_id_fkey ON obmbookmarkentity USING btree (obmbookmarkentity_entity_id);


--
-- Name: obmbookmarkentity_obmbookmark_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX obmbookmarkentity_obmbookmark_id_fkey ON obmbookmarkentity USING btree (obmbookmarkentity_obmbookmark_id);


--
-- Name: obmbookmarkproperty_bookmark_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX obmbookmarkproperty_bookmark_id_fkey ON obmbookmarkproperty USING btree (obmbookmarkproperty_bookmark_id);


--
-- Name: of_usergroup_group_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX of_usergroup_group_id_fkey ON of_usergroup USING btree (of_usergroup_group_id);


--
-- Name: of_usergroup_user_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX of_usergroup_user_id_fkey ON of_usergroup USING btree (of_usergroup_user_id);


--
-- Name: ogroup_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX ogroup_domain_id_fkey ON ogroup USING btree (ogroup_domain_id);


--
-- Name: ogroup_organizationalchart_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX ogroup_organizationalchart_id_fkey ON ogroup USING btree (ogroup_organizationalchart_id);


--
-- Name: ogroup_parent_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX ogroup_parent_id_fkey ON ogroup USING btree (ogroup_parent_id);


--
-- Name: ogroup_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX ogroup_usercreate_fkey ON ogroup USING btree (ogroup_usercreate);


--
-- Name: ogroup_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX ogroup_userupdate_fkey ON ogroup USING btree (ogroup_userupdate);


--
-- Name: ogroupentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX ogroupentity_entity_id_fkey ON ogroupentity USING btree (ogroupentity_entity_id);


--
-- Name: ogroupentity_ogroup_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX ogroupentity_ogroup_id_fkey ON ogroupentity USING btree (ogroupentity_ogroup_id);


--
-- Name: ogrouplink_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX ogrouplink_domain_id_fkey ON ogrouplink USING btree (ogrouplink_domain_id);


--
-- Name: ogrouplink_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX ogrouplink_entity_id_fkey ON ogrouplink USING btree (ogrouplink_entity_id);


--
-- Name: ogrouplink_ogroup_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX ogrouplink_ogroup_id_fkey ON ogrouplink USING btree (ogrouplink_ogroup_id);


--
-- Name: ogrouplink_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX ogrouplink_usercreate_fkey ON ogrouplink USING btree (ogrouplink_usercreate);


--
-- Name: ogrouplink_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX ogrouplink_userupdate_fkey ON ogrouplink USING btree (ogrouplink_userupdate);


--
-- Name: opush_event_mapping_device_id_event_ext_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE UNIQUE INDEX opush_event_mapping_device_id_event_ext_id_fkey ON opush_event_mapping USING btree (device_id, event_ext_id_hash);


--
-- Name: organizationalchart_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX organizationalchart_domain_id_fkey ON organizationalchart USING btree (organizationalchart_domain_id);


--
-- Name: organizationalchart_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX organizationalchart_usercreate_fkey ON organizationalchart USING btree (organizationalchart_usercreate);


--
-- Name: organizationalchart_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX organizationalchart_userupdate_fkey ON organizationalchart USING btree (organizationalchart_userupdate);


--
-- Name: organizationalchartentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX organizationalchartentity_entity_id_fkey ON organizationalchartentity USING btree (organizationalchartentity_entity_id);


--
-- Name: organizationalchartentity_organizationalchart_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX organizationalchartentity_organizationalchart_id_fkey ON organizationalchartentity USING btree (organizationalchartentity_organizationalchart_id);


--
-- Name: parentdeal_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX parentdeal_domain_id_fkey ON parentdeal USING btree (parentdeal_domain_id);


--
-- Name: parentdeal_marketingmanager_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX parentdeal_marketingmanager_id_fkey ON parentdeal USING btree (parentdeal_marketingmanager_id);


--
-- Name: parentdeal_technicalmanager_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX parentdeal_technicalmanager_id_fkey ON parentdeal USING btree (parentdeal_technicalmanager_id);


--
-- Name: parentdeal_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX parentdeal_usercreate_fkey ON parentdeal USING btree (parentdeal_usercreate);


--
-- Name: parentdeal_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX parentdeal_userupdate_fkey ON parentdeal USING btree (parentdeal_userupdate);


--
-- Name: parentdealentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX parentdealentity_entity_id_fkey ON parentdealentity USING btree (parentdealentity_entity_id);


--
-- Name: parentdealentity_parentdeal_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX parentdealentity_parentdeal_id_fkey ON parentdealentity USING btree (parentdealentity_parentdeal_id);


--
-- Name: payment_account_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX payment_account_id_fkey ON payment USING btree (payment_account_id);


--
-- Name: payment_company_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX payment_company_id_fkey ON payment USING btree (payment_company_id);


--
-- Name: payment_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX payment_domain_id_fkey ON payment USING btree (payment_domain_id);


--
-- Name: payment_paymentkind_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX payment_paymentkind_id_fkey ON payment USING btree (payment_paymentkind_id);


--
-- Name: payment_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX payment_usercreate_fkey ON payment USING btree (payment_usercreate);


--
-- Name: payment_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX payment_userupdate_fkey ON payment USING btree (payment_userupdate);


--
-- Name: paymententity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX paymententity_entity_id_fkey ON paymententity USING btree (paymententity_entity_id);


--
-- Name: paymententity_payment_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX paymententity_payment_id_fkey ON paymententity USING btree (paymententity_payment_id);


--
-- Name: paymentinvoice_invoice_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX paymentinvoice_invoice_id_fkey ON paymentinvoice USING btree (paymentinvoice_invoice_id);


--
-- Name: paymentinvoice_payment_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX paymentinvoice_payment_id_fkey ON paymentinvoice USING btree (paymentinvoice_payment_id);


--
-- Name: paymentinvoice_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX paymentinvoice_usercreate_fkey ON paymentinvoice USING btree (paymentinvoice_usercreate);


--
-- Name: paymentinvoice_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX paymentinvoice_userupdate_fkey ON paymentinvoice USING btree (paymentinvoice_userupdate);


--
-- Name: paymentkind_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX paymentkind_domain_id_fkey ON paymentkind USING btree (paymentkind_domain_id);


--
-- Name: phone_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX phone_entity_id_fkey ON phone USING btree (phone_entity_id);


--
-- Name: plannedtask_datebegin_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX plannedtask_datebegin_fkey ON plannedtask USING btree (plannedtask_datebegin);


--
-- Name: plannedtask_dateend_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX plannedtask_dateend_fkey ON plannedtask USING btree (plannedtask_dateend);


--
-- Name: plannedtask_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX plannedtask_domain_id_fkey ON plannedtask USING btree (plannedtask_domain_id);


--
-- Name: plannedtask_user_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX plannedtask_user_id_fkey ON plannedtask USING btree (plannedtask_user_id);


--
-- Name: profileentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX profileentity_entity_id_fkey ON profileentity USING btree (profileentity_entity_id);


--
-- Name: profileentity_profile_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX profileentity_profile_id_fkey ON profileentity USING btree (profileentity_profile_id);


--
-- Name: profilemodule_profile_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX profilemodule_profile_id_fkey ON profilemodule USING btree (profilemodule_profile_id);


--
-- Name: profileproperty_profile_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX profileproperty_profile_id_fkey ON profileproperty USING btree (profileproperty_profile_id);


--
-- Name: profilesection_profile_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX profilesection_profile_id_fkey ON profilesection USING btree (profilesection_profile_id);


--
-- Name: project_company_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX project_company_id_fkey ON project USING btree (project_company_id);


--
-- Name: project_deal_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX project_deal_id_fkey ON project USING btree (project_deal_id);


--
-- Name: project_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX project_domain_id_fkey ON project USING btree (project_domain_id);


--
-- Name: project_idx_comp; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX project_idx_comp ON project USING btree (project_company_id);


--
-- Name: project_idx_deal; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX project_idx_deal ON project USING btree (project_deal_id);


--
-- Name: project_tasktype_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX project_tasktype_id_fkey ON project USING btree (project_tasktype_id);


--
-- Name: project_type_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX project_type_id_fkey ON project USING btree (project_type_id);


--
-- Name: project_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX project_usercreate_fkey ON project USING btree (project_usercreate);


--
-- Name: project_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX project_userupdate_fkey ON project USING btree (project_userupdate);


--
-- Name: projectclosing_project_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX projectclosing_project_id_fkey ON projectclosing USING btree (projectclosing_project_id);


--
-- Name: projectclosing_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX projectclosing_usercreate_fkey ON projectclosing USING btree (projectclosing_usercreate);


--
-- Name: projectclosing_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX projectclosing_userupdate_fkey ON projectclosing USING btree (projectclosing_userupdate);


--
-- Name: projectcv_cv_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX projectcv_cv_id_fkey ON projectcv USING btree (projectcv_cv_id);


--
-- Name: projectcv_project_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX projectcv_project_id_fkey ON projectcv USING btree (projectcv_project_id);


--
-- Name: projectentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX projectentity_entity_id_fkey ON projectentity USING btree (projectentity_entity_id);


--
-- Name: projectentity_project_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX projectentity_project_id_fkey ON projectentity USING btree (projectentity_project_id);


--
-- Name: projectreftask_tasktype_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX projectreftask_tasktype_id_fkey ON projectreftask USING btree (projectreftask_tasktype_id);


--
-- Name: projectreftask_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX projectreftask_usercreate_fkey ON projectreftask USING btree (projectreftask_usercreate);


--
-- Name: projectreftask_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX projectreftask_userupdate_fkey ON projectreftask USING btree (projectreftask_userupdate);


--
-- Name: projecttask_parenttask_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX projecttask_parenttask_id_fkey ON projecttask USING btree (projecttask_parenttask_id);


--
-- Name: projecttask_project_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX projecttask_project_id_fkey ON projecttask USING btree (projecttask_project_id);


--
-- Name: projecttask_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX projecttask_usercreate_fkey ON projecttask USING btree (projecttask_usercreate);


--
-- Name: projecttask_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX projecttask_userupdate_fkey ON projecttask USING btree (projecttask_userupdate);


--
-- Name: projectuser_project_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX projectuser_project_id_fkey ON projectuser USING btree (projectuser_project_id);


--
-- Name: projectuser_user_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX projectuser_user_id_fkey ON projectuser USING btree (projectuser_user_id);


--
-- Name: projectuser_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX projectuser_usercreate_fkey ON projectuser USING btree (projectuser_usercreate);


--
-- Name: projectuser_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX projectuser_userupdate_fkey ON projectuser USING btree (projectuser_userupdate);


--
-- Name: pt_idx_pro; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX pt_idx_pro ON projecttask USING btree (projecttask_project_id);


--
-- Name: pu_idx_pro; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX pu_idx_pro ON projectuser USING btree (projectuser_project_id);


--
-- Name: pu_idx_pt; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX pu_idx_pt ON projectuser USING btree (projectuser_projecttask_id);


--
-- Name: pu_idx_user; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX pu_idx_user ON projectuser USING btree (projectuser_user_id);


--
-- Name: publication_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX publication_domain_id_fkey ON publication USING btree (publication_domain_id);


--
-- Name: publication_type_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX publication_type_id_fkey ON publication USING btree (publication_type_id);


--
-- Name: publication_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX publication_usercreate_fkey ON publication USING btree (publication_usercreate);


--
-- Name: publication_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX publication_userupdate_fkey ON publication USING btree (publication_userupdate);


--
-- Name: publicationentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX publicationentity_entity_id_fkey ON publicationentity USING btree (publicationentity_entity_id);


--
-- Name: publicationentity_publication_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX publicationentity_publication_id_fkey ON publicationentity USING btree (publicationentity_publication_id);


--
-- Name: publicationtype_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX publicationtype_domain_id_fkey ON publicationtype USING btree (publicationtype_domain_id);


--
-- Name: publicationtype_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX publicationtype_usercreate_fkey ON publicationtype USING btree (publicationtype_usercreate);


--
-- Name: publicationtype_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX publicationtype_userupdate_fkey ON publicationtype USING btree (publicationtype_userupdate);


--
-- Name: region_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX region_domain_id_fkey ON region USING btree (region_domain_id);


--
-- Name: region_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX region_usercreate_fkey ON region USING btree (region_usercreate);


--
-- Name: region_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX region_userupdate_fkey ON region USING btree (region_userupdate);


--
-- Name: resource_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX resource_domain_id_fkey ON resource USING btree (resource_domain_id);


--
-- Name: resource_rtype_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX resource_rtype_id_fkey ON resource USING btree (resource_rtype_id);


--
-- Name: resource_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX resource_usercreate_fkey ON resource USING btree (resource_usercreate);


--
-- Name: resource_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX resource_userupdate_fkey ON resource USING btree (resource_userupdate);


--
-- Name: resourceentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX resourceentity_entity_id_fkey ON resourceentity USING btree (resourceentity_entity_id);


--
-- Name: resourceentity_resource_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX resourceentity_resource_id_fkey ON resourceentity USING btree (resourceentity_resource_id);


--
-- Name: resourcegroup_resource_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX resourcegroup_resource_id_fkey ON resourcegroup USING btree (resourcegroup_resource_id);


--
-- Name: resourcegroup_rgroup_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX resourcegroup_rgroup_id_fkey ON resourcegroup USING btree (resourcegroup_rgroup_id);


--
-- Name: resourcegroupentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX resourcegroupentity_entity_id_fkey ON resourcegroupentity USING btree (resourcegroupentity_entity_id);


--
-- Name: resourcegroupentity_resourcegroup_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX resourcegroupentity_resourcegroup_id_fkey ON resourcegroupentity USING btree (resourcegroupentity_resourcegroup_id);


--
-- Name: resourceitem_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX resourceitem_domain_id_fkey ON resourceitem USING btree (resourceitem_domain_id);


--
-- Name: resourceitem_resourcetype_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX resourceitem_resourcetype_id_fkey ON resourceitem USING btree (resourceitem_resourcetype_id);


--
-- Name: resourcetype_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX resourcetype_domain_id_fkey ON resourcetype USING btree (resourcetype_domain_id);


--
-- Name: rgroup_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX rgroup_domain_id_fkey ON rgroup USING btree (rgroup_domain_id);


--
-- Name: rgroup_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX rgroup_usercreate_fkey ON rgroup USING btree (rgroup_usercreate);


--
-- Name: rgroup_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX rgroup_userupdate_fkey ON rgroup USING btree (rgroup_userupdate);


--
-- Name: service_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX service_entity_id_fkey ON service USING btree (service_entity_id);


--
-- Name: service_service_key; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX service_service_key ON service USING btree (service_service);


--
-- Name: serviceproperty_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX serviceproperty_entity_id_fkey ON serviceproperty USING btree (serviceproperty_entity_id);


--
-- Name: serviceproperty_property_key; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX serviceproperty_property_key ON serviceproperty USING btree (serviceproperty_property);


--
-- Name: serviceproperty_service_key; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX serviceproperty_service_key ON serviceproperty USING btree (serviceproperty_service);


--
-- Name: ssoticket_user_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX ssoticket_user_id_fkey ON ssoticket USING btree (ssoticket_user_id);


--
-- Name: subscription_contact_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX subscription_contact_id_fkey ON subscription USING btree (subscription_contact_id);


--
-- Name: subscription_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX subscription_domain_id_fkey ON subscription USING btree (subscription_domain_id);


--
-- Name: subscription_publication_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX subscription_publication_id_fkey ON subscription USING btree (subscription_publication_id);


--
-- Name: subscription_reception_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX subscription_reception_id_fkey ON subscription USING btree (subscription_reception_id);


--
-- Name: subscription_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX subscription_usercreate_fkey ON subscription USING btree (subscription_usercreate);


--
-- Name: subscription_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX subscription_userupdate_fkey ON subscription USING btree (subscription_userupdate);


--
-- Name: subscriptionentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX subscriptionentity_entity_id_fkey ON subscriptionentity USING btree (subscriptionentity_entity_id);


--
-- Name: subscriptionentity_subscription_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX subscriptionentity_subscription_id_fkey ON subscriptionentity USING btree (subscriptionentity_subscription_id);


--
-- Name: subscriptionreception_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX subscriptionreception_domain_id_fkey ON subscriptionreception USING btree (subscriptionreception_domain_id);


--
-- Name: subscriptionreception_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX subscriptionreception_usercreate_fkey ON subscriptionreception USING btree (subscriptionreception_usercreate);


--
-- Name: subscriptionreception_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX subscriptionreception_userupdate_fkey ON subscriptionreception USING btree (subscriptionreception_userupdate);


--
-- Name: taskevent_event_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX taskevent_event_id_fkey ON taskevent USING btree (taskevent_event_id);


--
-- Name: taskevent_task_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX taskevent_task_id_fkey ON taskevent USING btree (taskevent_task_id);


--
-- Name: tasktype_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX tasktype_domain_id_fkey ON tasktype USING btree (tasktype_domain_id);


--
-- Name: tasktype_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX tasktype_usercreate_fkey ON tasktype USING btree (tasktype_usercreate);


--
-- Name: tasktype_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX tasktype_userupdate_fkey ON tasktype USING btree (tasktype_userupdate);


--
-- Name: tasktypegroup_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX tasktypegroup_domain_id_fkey ON tasktypegroup USING btree (tasktypegroup_domain_id);


--
-- Name: tasktypegroup_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX tasktypegroup_usercreate_fkey ON tasktypegroup USING btree (tasktypegroup_usercreate);


--
-- Name: tasktypegroup_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX tasktypegroup_userupdate_fkey ON tasktypegroup USING btree (tasktypegroup_userupdate);


--
-- Name: timetask_projecttask_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX timetask_projecttask_id_fkey ON timetask USING btree (timetask_projecttask_id);


--
-- Name: timetask_tasktype_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX timetask_tasktype_id_fkey ON timetask USING btree (timetask_tasktype_id);


--
-- Name: timetask_user_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX timetask_user_id_fkey ON timetask USING btree (timetask_user_id);


--
-- Name: timetask_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX timetask_usercreate_fkey ON timetask USING btree (timetask_usercreate);


--
-- Name: timetask_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX timetask_userupdate_fkey ON timetask USING btree (timetask_userupdate);


--
-- Name: tt_idx_pt; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX tt_idx_pt ON timetask USING btree (timetask_projecttask_id);


--
-- Name: updated_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX updated_domain_id_fkey ON updated USING btree (updated_domain_id);


--
-- Name: updated_user_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX updated_user_id_fkey ON updated USING btree (updated_user_id);


--
-- Name: updatedlinks_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX updatedlinks_domain_id_fkey ON updatedlinks USING btree (updatedlinks_domain_id);


--
-- Name: updatedlinks_user_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX updatedlinks_user_id_fkey ON updatedlinks USING btree (updatedlinks_user_id);


--
-- Name: user_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX user_id_fkey ON calendarcolor USING btree (user_id);


--
-- Name: userentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX userentity_entity_id_fkey ON userentity USING btree (userentity_entity_id);


--
-- Name: userentity_user_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX userentity_user_id_fkey ON userentity USING btree (userentity_user_id);


--
-- Name: userobm_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX userobm_domain_id_fkey ON userobm USING btree (userobm_domain_id);


--
-- Name: userobm_host_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX userobm_host_id_fkey ON userobm USING btree (userobm_host_id);


--
-- Name: userobm_mail_server_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX userobm_mail_server_id_fkey ON userobm USING btree (userobm_mail_server_id);


--
-- Name: userobm_photo_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX userobm_photo_id_fkey ON userobm USING btree (userobm_photo_id);


--
-- Name: userobm_sessionlog_userobm_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX userobm_sessionlog_userobm_id_fkey ON userobm_sessionlog USING btree (userobm_sessionlog_userobm_id);


--
-- Name: userobm_usercreate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX userobm_usercreate_fkey ON userobm USING btree (userobm_usercreate);


--
-- Name: userobm_userupdate_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX userobm_userupdate_fkey ON userobm USING btree (userobm_userupdate);


--
-- Name: userobmgroup_group_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX userobmgroup_group_id_fkey ON userobmgroup USING btree (userobmgroup_group_id);


--
-- Name: userobmgroup_userobm_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX userobmgroup_userobm_id_fkey ON userobmgroup USING btree (userobmgroup_userobm_id);


--
-- Name: userobmpref_user_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX userobmpref_user_id_fkey ON userobmpref USING btree (userobmpref_user_id);


--
-- Name: userpattern_domain_id_domain_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX userpattern_domain_id_domain_id_fkey ON userpattern USING btree (domain_id);


--
-- Name: userpattern_property_userpattern_id_userpattern_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX userpattern_property_userpattern_id_userpattern_id_fkey ON userpattern_property USING btree (userpattern_id);


--
-- Name: userpattern_usercreate_userobm_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX userpattern_usercreate_userobm_id_fkey ON userpattern USING btree (usercreate);


--
-- Name: userpattern_userupdate_userobm_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX userpattern_userupdate_userobm_id_fkey ON userpattern USING btree (userupdate);


--
-- Name: website_entity_id_fkey; Type: INDEX; Schema: public; Owner: obm; Tablespace: 
--

CREATE INDEX website_entity_id_fkey ON website USING btree (website_entity_id);


--
-- Name: account_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER account_changed
    BEFORE UPDATE ON account
    FOR EACH ROW
    EXECUTE PROCEDURE on_account_change();


--
-- Name: account_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER account_created
    BEFORE INSERT ON account
    FOR EACH ROW
    EXECUTE PROCEDURE on_account_create();


--
-- Name: activeuserobm_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER activeuserobm_changed
    BEFORE UPDATE ON activeuserobm
    FOR EACH ROW
    EXECUTE PROCEDURE on_activeuserobm_change();


--
-- Name: activeuserobm_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER activeuserobm_created
    BEFORE INSERT ON activeuserobm
    FOR EACH ROW
    EXECUTE PROCEDURE on_activeuserobm_create();


--
-- Name: addressbook_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER addressbook_changed
    BEFORE UPDATE ON addressbook
    FOR EACH ROW
    EXECUTE PROCEDURE on_addressbook_change();


--
-- Name: addressbook_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER addressbook_created
    BEFORE INSERT ON addressbook
    FOR EACH ROW
    EXECUTE PROCEDURE on_addressbook_create();


--
-- Name: category_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER category_changed
    BEFORE UPDATE ON category
    FOR EACH ROW
    EXECUTE PROCEDURE on_category_change();


--
-- Name: category_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER category_created
    BEFORE INSERT ON category
    FOR EACH ROW
    EXECUTE PROCEDURE on_category_create();


--
-- Name: company_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER company_changed
    BEFORE UPDATE ON company
    FOR EACH ROW
    EXECUTE PROCEDURE on_company_change();


--
-- Name: company_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER company_created
    BEFORE INSERT ON company
    FOR EACH ROW
    EXECUTE PROCEDURE on_company_create();


--
-- Name: companyactivity_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER companyactivity_changed
    BEFORE UPDATE ON companyactivity
    FOR EACH ROW
    EXECUTE PROCEDURE on_companyactivity_change();


--
-- Name: companyactivity_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER companyactivity_created
    BEFORE INSERT ON companyactivity
    FOR EACH ROW
    EXECUTE PROCEDURE on_companyactivity_create();


--
-- Name: companynafcode_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER companynafcode_changed
    BEFORE UPDATE ON companynafcode
    FOR EACH ROW
    EXECUTE PROCEDURE on_companynafcode_change();


--
-- Name: companynafcode_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER companynafcode_created
    BEFORE INSERT ON companynafcode
    FOR EACH ROW
    EXECUTE PROCEDURE on_companynafcode_create();


--
-- Name: companytype_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER companytype_changed
    BEFORE UPDATE ON companytype
    FOR EACH ROW
    EXECUTE PROCEDURE on_companytype_change();


--
-- Name: companytype_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER companytype_created
    BEFORE INSERT ON companytype
    FOR EACH ROW
    EXECUTE PROCEDURE on_companytype_create();


--
-- Name: contact_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER contact_changed
    BEFORE UPDATE ON contact
    FOR EACH ROW
    EXECUTE PROCEDURE on_contact_change();


--
-- Name: contact_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER contact_created
    BEFORE INSERT ON contact
    FOR EACH ROW
    EXECUTE PROCEDURE on_contact_create();


--
-- Name: contactfunction_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER contactfunction_changed
    BEFORE UPDATE ON contactfunction
    FOR EACH ROW
    EXECUTE PROCEDURE on_contactfunction_change();


--
-- Name: contactfunction_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER contactfunction_created
    BEFORE INSERT ON contactfunction
    FOR EACH ROW
    EXECUTE PROCEDURE on_contactfunction_create();


--
-- Name: contract_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER contract_changed
    BEFORE UPDATE ON contract
    FOR EACH ROW
    EXECUTE PROCEDURE on_contract_change();


--
-- Name: contract_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER contract_created
    BEFORE INSERT ON contract
    FOR EACH ROW
    EXECUTE PROCEDURE on_contract_create();


--
-- Name: contractpriority_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER contractpriority_changed
    BEFORE UPDATE ON contractpriority
    FOR EACH ROW
    EXECUTE PROCEDURE on_contractpriority_change();


--
-- Name: contractpriority_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER contractpriority_created
    BEFORE INSERT ON contractpriority
    FOR EACH ROW
    EXECUTE PROCEDURE on_contractpriority_create();


--
-- Name: contractstatus_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER contractstatus_changed
    BEFORE UPDATE ON contractstatus
    FOR EACH ROW
    EXECUTE PROCEDURE on_contractstatus_change();


--
-- Name: contractstatus_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER contractstatus_created
    BEFORE INSERT ON contractstatus
    FOR EACH ROW
    EXECUTE PROCEDURE on_contractstatus_create();


--
-- Name: contracttype_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER contracttype_changed
    BEFORE UPDATE ON contracttype
    FOR EACH ROW
    EXECUTE PROCEDURE on_contracttype_change();


--
-- Name: contracttype_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER contracttype_created
    BEFORE INSERT ON contracttype
    FOR EACH ROW
    EXECUTE PROCEDURE on_contracttype_create();


--
-- Name: country_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER country_changed
    BEFORE UPDATE ON country
    FOR EACH ROW
    EXECUTE PROCEDURE on_country_change();


--
-- Name: country_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER country_created
    BEFORE INSERT ON country
    FOR EACH ROW
    EXECUTE PROCEDURE on_country_create();


--
-- Name: cv_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER cv_changed
    BEFORE UPDATE ON cv
    FOR EACH ROW
    EXECUTE PROCEDURE on_cv_change();


--
-- Name: cv_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER cv_created
    BEFORE INSERT ON cv
    FOR EACH ROW
    EXECUTE PROCEDURE on_cv_create();


--
-- Name: datasource_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER datasource_changed
    BEFORE UPDATE ON datasource
    FOR EACH ROW
    EXECUTE PROCEDURE on_datasource_change();


--
-- Name: datasource_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER datasource_created
    BEFORE INSERT ON datasource
    FOR EACH ROW
    EXECUTE PROCEDURE on_datasource_create();


--
-- Name: deal_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER deal_changed
    BEFORE UPDATE ON deal
    FOR EACH ROW
    EXECUTE PROCEDURE on_deal_change();


--
-- Name: deal_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER deal_created
    BEFORE INSERT ON deal
    FOR EACH ROW
    EXECUTE PROCEDURE on_deal_create();


--
-- Name: dealcompany_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER dealcompany_changed
    BEFORE UPDATE ON dealcompany
    FOR EACH ROW
    EXECUTE PROCEDURE on_dealcompany_change();


--
-- Name: dealcompany_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER dealcompany_created
    BEFORE INSERT ON dealcompany
    FOR EACH ROW
    EXECUTE PROCEDURE on_dealcompany_create();


--
-- Name: dealcompanyrole_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER dealcompanyrole_changed
    BEFORE UPDATE ON dealcompanyrole
    FOR EACH ROW
    EXECUTE PROCEDURE on_dealcompanyrole_change();


--
-- Name: dealcompanyrole_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER dealcompanyrole_created
    BEFORE INSERT ON dealcompanyrole
    FOR EACH ROW
    EXECUTE PROCEDURE on_dealcompanyrole_create();


--
-- Name: dealstatus_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER dealstatus_changed
    BEFORE UPDATE ON dealstatus
    FOR EACH ROW
    EXECUTE PROCEDURE on_dealstatus_change();


--
-- Name: dealstatus_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER dealstatus_created
    BEFORE INSERT ON dealstatus
    FOR EACH ROW
    EXECUTE PROCEDURE on_dealstatus_create();


--
-- Name: dealtype_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER dealtype_changed
    BEFORE UPDATE ON dealtype
    FOR EACH ROW
    EXECUTE PROCEDURE on_dealtype_change();


--
-- Name: dealtype_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER dealtype_created
    BEFORE INSERT ON dealtype
    FOR EACH ROW
    EXECUTE PROCEDURE on_dealtype_create();


--
-- Name: document_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER document_changed
    BEFORE UPDATE ON document
    FOR EACH ROW
    EXECUTE PROCEDURE on_document_change();


--
-- Name: document_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER document_created
    BEFORE INSERT ON document
    FOR EACH ROW
    EXECUTE PROCEDURE on_document_create();


--
-- Name: documentmimetype_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER documentmimetype_changed
    BEFORE UPDATE ON documentmimetype
    FOR EACH ROW
    EXECUTE PROCEDURE on_documentmimetype_change();


--
-- Name: documentmimetype_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER documentmimetype_created
    BEFORE INSERT ON documentmimetype
    FOR EACH ROW
    EXECUTE PROCEDURE on_documentmimetype_create();


--
-- Name: domain_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER domain_changed
    BEFORE UPDATE ON domain
    FOR EACH ROW
    EXECUTE PROCEDURE on_domain_change();


--
-- Name: domain_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER domain_created
    BEFORE INSERT ON domain
    FOR EACH ROW
    EXECUTE PROCEDURE on_domain_create();


--
-- Name: event_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER event_changed
    BEFORE UPDATE ON event
    FOR EACH ROW
    EXECUTE PROCEDURE on_event_change();


--
-- Name: event_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER event_created
    BEFORE INSERT ON event
    FOR EACH ROW
    EXECUTE PROCEDURE on_event_create();


--
-- Name: eventalert_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER eventalert_changed
    BEFORE UPDATE ON eventalert
    FOR EACH ROW
    EXECUTE PROCEDURE on_eventalert_change();


--
-- Name: eventalert_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER eventalert_created
    BEFORE INSERT ON eventalert
    FOR EACH ROW
    EXECUTE PROCEDURE on_eventalert_create();


--
-- Name: eventcategory1_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER eventcategory1_changed
    BEFORE UPDATE ON eventcategory1
    FOR EACH ROW
    EXECUTE PROCEDURE on_eventcategory1_change();


--
-- Name: eventcategory1_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER eventcategory1_created
    BEFORE INSERT ON eventcategory1
    FOR EACH ROW
    EXECUTE PROCEDURE on_eventcategory1_create();


--
-- Name: eventexception_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER eventexception_changed
    BEFORE UPDATE ON eventexception
    FOR EACH ROW
    EXECUTE PROCEDURE on_eventexception_change();


--
-- Name: eventexception_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER eventexception_created
    BEFORE INSERT ON eventexception
    FOR EACH ROW
    EXECUTE PROCEDURE on_eventexception_create();


--
-- Name: eventlink_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER eventlink_changed
    BEFORE UPDATE ON eventlink
    FOR EACH ROW
    EXECUTE PROCEDURE on_eventlink_change();


--
-- Name: eventlink_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER eventlink_created
    BEFORE INSERT ON eventlink
    FOR EACH ROW
    EXECUTE PROCEDURE on_eventlink_create();


--
-- Name: eventtemplate_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER eventtemplate_changed
    BEFORE UPDATE ON eventtemplate
    FOR EACH ROW
    EXECUTE PROCEDURE on_eventtemplate_change();


--
-- Name: eventtemplate_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER eventtemplate_created
    BEFORE INSERT ON eventtemplate
    FOR EACH ROW
    EXECUTE PROCEDURE on_eventtemplate_create();


--
-- Name: host_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER host_changed
    BEFORE UPDATE ON host
    FOR EACH ROW
    EXECUTE PROCEDURE on_host_change();


--
-- Name: host_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER host_created
    BEFORE INSERT ON host
    FOR EACH ROW
    EXECUTE PROCEDURE on_host_create();


--
-- Name: import_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER import_changed
    BEFORE UPDATE ON import
    FOR EACH ROW
    EXECUTE PROCEDURE on_import_change();


--
-- Name: import_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER import_created
    BEFORE INSERT ON import
    FOR EACH ROW
    EXECUTE PROCEDURE on_import_create();


--
-- Name: incident_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER incident_changed
    BEFORE UPDATE ON incident
    FOR EACH ROW
    EXECUTE PROCEDURE on_incident_change();


--
-- Name: incident_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER incident_created
    BEFORE INSERT ON incident
    FOR EACH ROW
    EXECUTE PROCEDURE on_incident_create();


--
-- Name: incidentpriority_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER incidentpriority_changed
    BEFORE UPDATE ON incidentpriority
    FOR EACH ROW
    EXECUTE PROCEDURE on_incidentpriority_change();


--
-- Name: incidentpriority_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER incidentpriority_created
    BEFORE INSERT ON incidentpriority
    FOR EACH ROW
    EXECUTE PROCEDURE on_incidentpriority_create();


--
-- Name: incidentresolutiontype_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER incidentresolutiontype_changed
    BEFORE UPDATE ON incidentresolutiontype
    FOR EACH ROW
    EXECUTE PROCEDURE on_incidentresolutiontype_change();


--
-- Name: incidentresolutiontype_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER incidentresolutiontype_created
    BEFORE INSERT ON incidentresolutiontype
    FOR EACH ROW
    EXECUTE PROCEDURE on_incidentresolutiontype_create();


--
-- Name: incidentstatus_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER incidentstatus_changed
    BEFORE UPDATE ON incidentstatus
    FOR EACH ROW
    EXECUTE PROCEDURE on_incidentstatus_change();


--
-- Name: incidentstatus_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER incidentstatus_created
    BEFORE INSERT ON incidentstatus
    FOR EACH ROW
    EXECUTE PROCEDURE on_incidentstatus_create();


--
-- Name: invoice_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER invoice_changed
    BEFORE UPDATE ON invoice
    FOR EACH ROW
    EXECUTE PROCEDURE on_invoice_change();


--
-- Name: invoice_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER invoice_created
    BEFORE INSERT ON invoice
    FOR EACH ROW
    EXECUTE PROCEDURE on_invoice_create();


--
-- Name: kind_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER kind_changed
    BEFORE UPDATE ON kind
    FOR EACH ROW
    EXECUTE PROCEDURE on_kind_change();


--
-- Name: kind_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER kind_created
    BEFORE INSERT ON kind
    FOR EACH ROW
    EXECUTE PROCEDURE on_kind_create();


--
-- Name: lead_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER lead_changed
    BEFORE UPDATE ON lead
    FOR EACH ROW
    EXECUTE PROCEDURE on_lead_change();


--
-- Name: lead_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER lead_created
    BEFORE INSERT ON lead
    FOR EACH ROW
    EXECUTE PROCEDURE on_lead_create();


--
-- Name: leadsource_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER leadsource_changed
    BEFORE UPDATE ON leadsource
    FOR EACH ROW
    EXECUTE PROCEDURE on_leadsource_change();


--
-- Name: leadsource_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER leadsource_created
    BEFORE INSERT ON leadsource
    FOR EACH ROW
    EXECUTE PROCEDURE on_leadsource_create();


--
-- Name: leadstatus_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER leadstatus_changed
    BEFORE UPDATE ON leadstatus
    FOR EACH ROW
    EXECUTE PROCEDURE on_leadstatus_change();


--
-- Name: leadstatus_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER leadstatus_created
    BEFORE INSERT ON leadstatus
    FOR EACH ROW
    EXECUTE PROCEDURE on_leadstatus_create();


--
-- Name: list_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER list_changed
    BEFORE UPDATE ON list
    FOR EACH ROW
    EXECUTE PROCEDURE on_list_change();


--
-- Name: list_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER list_created
    BEFORE INSERT ON list
    FOR EACH ROW
    EXECUTE PROCEDURE on_list_create();


--
-- Name: mailshare_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER mailshare_changed
    BEFORE UPDATE ON mailshare
    FOR EACH ROW
    EXECUTE PROCEDURE on_mailshare_change();


--
-- Name: mailshare_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER mailshare_created
    BEFORE INSERT ON mailshare
    FOR EACH ROW
    EXECUTE PROCEDURE on_mailshare_create();


--
-- Name: obmsession_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER obmsession_changed
    BEFORE UPDATE ON obmsession
    FOR EACH ROW
    EXECUTE PROCEDURE on_obmsession_change();


--
-- Name: ogroup_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER ogroup_changed
    BEFORE UPDATE ON ogroup
    FOR EACH ROW
    EXECUTE PROCEDURE on_ogroup_change();


--
-- Name: ogroup_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER ogroup_created
    BEFORE INSERT ON ogroup
    FOR EACH ROW
    EXECUTE PROCEDURE on_ogroup_create();


--
-- Name: ogrouplink_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER ogrouplink_changed
    BEFORE UPDATE ON ogrouplink
    FOR EACH ROW
    EXECUTE PROCEDURE on_ogrouplink_change();


--
-- Name: ogrouplink_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER ogrouplink_created
    BEFORE INSERT ON ogrouplink
    FOR EACH ROW
    EXECUTE PROCEDURE on_ogrouplink_create();


--
-- Name: organizationalchart_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER organizationalchart_changed
    BEFORE UPDATE ON organizationalchart
    FOR EACH ROW
    EXECUTE PROCEDURE on_organizationalchart_change();


--
-- Name: organizationalchart_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER organizationalchart_created
    BEFORE INSERT ON organizationalchart
    FOR EACH ROW
    EXECUTE PROCEDURE on_organizationalchart_create();


--
-- Name: parentdeal_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER parentdeal_changed
    BEFORE UPDATE ON parentdeal
    FOR EACH ROW
    EXECUTE PROCEDURE on_parentdeal_change();


--
-- Name: parentdeal_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER parentdeal_created
    BEFORE INSERT ON parentdeal
    FOR EACH ROW
    EXECUTE PROCEDURE on_parentdeal_create();


--
-- Name: payment_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER payment_changed
    BEFORE UPDATE ON payment
    FOR EACH ROW
    EXECUTE PROCEDURE on_payment_change();


--
-- Name: payment_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER payment_created
    BEFORE INSERT ON payment
    FOR EACH ROW
    EXECUTE PROCEDURE on_payment_create();


--
-- Name: paymentinvoice_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER paymentinvoice_changed
    BEFORE UPDATE ON paymentinvoice
    FOR EACH ROW
    EXECUTE PROCEDURE on_paymentinvoice_change();


--
-- Name: paymentinvoice_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER paymentinvoice_created
    BEFORE INSERT ON paymentinvoice
    FOR EACH ROW
    EXECUTE PROCEDURE on_paymentinvoice_create();


--
-- Name: plannedtask_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER plannedtask_changed
    BEFORE UPDATE ON plannedtask
    FOR EACH ROW
    EXECUTE PROCEDURE on_plannedtask_change();


--
-- Name: plannedtask_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER plannedtask_created
    BEFORE INSERT ON plannedtask
    FOR EACH ROW
    EXECUTE PROCEDURE on_plannedtask_create();


--
-- Name: profile_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER profile_changed
    BEFORE UPDATE ON profile
    FOR EACH ROW
    EXECUTE PROCEDURE on_profile_change();


--
-- Name: profile_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER profile_created
    BEFORE INSERT ON profile
    FOR EACH ROW
    EXECUTE PROCEDURE on_profile_create();


--
-- Name: project_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER project_changed
    BEFORE UPDATE ON project
    FOR EACH ROW
    EXECUTE PROCEDURE on_project_change();


--
-- Name: project_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER project_created
    BEFORE INSERT ON project
    FOR EACH ROW
    EXECUTE PROCEDURE on_project_create();


--
-- Name: projectclosing_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER projectclosing_changed
    BEFORE UPDATE ON projectclosing
    FOR EACH ROW
    EXECUTE PROCEDURE on_projectclosing_change();


--
-- Name: projectclosing_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER projectclosing_created
    BEFORE INSERT ON projectclosing
    FOR EACH ROW
    EXECUTE PROCEDURE on_projectclosing_create();


--
-- Name: projectreftask_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER projectreftask_changed
    BEFORE UPDATE ON projectreftask
    FOR EACH ROW
    EXECUTE PROCEDURE on_projectreftask_change();


--
-- Name: projectreftask_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER projectreftask_created
    BEFORE INSERT ON projectreftask
    FOR EACH ROW
    EXECUTE PROCEDURE on_projectreftask_create();


--
-- Name: projecttask_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER projecttask_changed
    BEFORE UPDATE ON projecttask
    FOR EACH ROW
    EXECUTE PROCEDURE on_projecttask_change();


--
-- Name: projecttask_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER projecttask_created
    BEFORE INSERT ON projecttask
    FOR EACH ROW
    EXECUTE PROCEDURE on_projecttask_create();


--
-- Name: projectuser_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER projectuser_changed
    BEFORE UPDATE ON projectuser
    FOR EACH ROW
    EXECUTE PROCEDURE on_projectuser_change();


--
-- Name: projectuser_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER projectuser_created
    BEFORE INSERT ON projectuser
    FOR EACH ROW
    EXECUTE PROCEDURE on_projectuser_create();


--
-- Name: publication_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER publication_changed
    BEFORE UPDATE ON publication
    FOR EACH ROW
    EXECUTE PROCEDURE on_publication_change();


--
-- Name: publication_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER publication_created
    BEFORE INSERT ON publication
    FOR EACH ROW
    EXECUTE PROCEDURE on_publication_create();


--
-- Name: publicationtype_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER publicationtype_changed
    BEFORE UPDATE ON publicationtype
    FOR EACH ROW
    EXECUTE PROCEDURE on_publicationtype_change();


--
-- Name: publicationtype_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER publicationtype_created
    BEFORE INSERT ON publicationtype
    FOR EACH ROW
    EXECUTE PROCEDURE on_publicationtype_create();


--
-- Name: region_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER region_changed
    BEFORE UPDATE ON region
    FOR EACH ROW
    EXECUTE PROCEDURE on_region_change();


--
-- Name: region_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER region_created
    BEFORE INSERT ON region
    FOR EACH ROW
    EXECUTE PROCEDURE on_region_create();


--
-- Name: resource_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER resource_changed
    BEFORE UPDATE ON resource
    FOR EACH ROW
    EXECUTE PROCEDURE on_resource_change();


--
-- Name: resource_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER resource_created
    BEFORE INSERT ON resource
    FOR EACH ROW
    EXECUTE PROCEDURE on_resource_create();


--
-- Name: rgroup_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER rgroup_changed
    BEFORE UPDATE ON rgroup
    FOR EACH ROW
    EXECUTE PROCEDURE on_rgroup_change();


--
-- Name: rgroup_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER rgroup_created
    BEFORE INSERT ON rgroup
    FOR EACH ROW
    EXECUTE PROCEDURE on_rgroup_create();


--
-- Name: subscription_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER subscription_changed
    BEFORE UPDATE ON subscription
    FOR EACH ROW
    EXECUTE PROCEDURE on_subscription_change();


--
-- Name: subscription_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER subscription_created
    BEFORE INSERT ON subscription
    FOR EACH ROW
    EXECUTE PROCEDURE on_subscription_create();


--
-- Name: subscriptionreception_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER subscriptionreception_changed
    BEFORE UPDATE ON subscriptionreception
    FOR EACH ROW
    EXECUTE PROCEDURE on_subscriptionreception_change();


--
-- Name: subscriptionreception_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER subscriptionreception_created
    BEFORE INSERT ON subscriptionreception
    FOR EACH ROW
    EXECUTE PROCEDURE on_subscriptionreception_create();


--
-- Name: tasktype_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER tasktype_changed
    BEFORE UPDATE ON tasktype
    FOR EACH ROW
    EXECUTE PROCEDURE on_tasktype_change();


--
-- Name: tasktype_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER tasktype_created
    BEFORE INSERT ON tasktype
    FOR EACH ROW
    EXECUTE PROCEDURE on_tasktype_create();


--
-- Name: tasktypegroup_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER tasktypegroup_changed
    BEFORE UPDATE ON tasktypegroup
    FOR EACH ROW
    EXECUTE PROCEDURE on_tasktypegroup_change();


--
-- Name: tasktypegroup_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER tasktypegroup_created
    BEFORE INSERT ON tasktypegroup
    FOR EACH ROW
    EXECUTE PROCEDURE on_tasktypegroup_create();


--
-- Name: timetask_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER timetask_changed
    BEFORE UPDATE ON timetask
    FOR EACH ROW
    EXECUTE PROCEDURE on_timetask_change();


--
-- Name: timetask_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER timetask_created
    BEFORE INSERT ON timetask
    FOR EACH ROW
    EXECUTE PROCEDURE on_timetask_create();


--
-- Name: ugroup_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER ugroup_changed
    BEFORE UPDATE ON ugroup
    FOR EACH ROW
    EXECUTE PROCEDURE on_ugroup_change();


--
-- Name: ugroup_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER ugroup_created
    BEFORE INSERT ON ugroup
    FOR EACH ROW
    EXECUTE PROCEDURE on_ugroup_create();


--
-- Name: userobm_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER userobm_changed
    BEFORE UPDATE ON userobm
    FOR EACH ROW
    EXECUTE PROCEDURE on_userobm_change();


--
-- Name: userobm_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER userobm_created
    BEFORE INSERT ON userobm
    FOR EACH ROW
    EXECUTE PROCEDURE on_userobm_create();


--
-- Name: userobm_sessionlog_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER userobm_sessionlog_changed
    BEFORE UPDATE ON userobm_sessionlog
    FOR EACH ROW
    EXECUTE PROCEDURE on_userobm_sessionlog_change();


--
-- Name: userobm_sessionlog_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER userobm_sessionlog_created
    BEFORE INSERT ON userobm_sessionlog
    FOR EACH ROW
    EXECUTE PROCEDURE on_userobm_sessionlog_create();


--
-- Name: userpattern_changed; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER userpattern_changed
    BEFORE UPDATE ON userpattern
    FOR EACH ROW
    EXECUTE PROCEDURE on_userpattern_change();


--
-- Name: userpattern_created; Type: TRIGGER; Schema: public; Owner: obm
--

CREATE TRIGGER userpattern_created
    BEFORE INSERT ON userpattern
    FOR EACH ROW
    EXECUTE PROCEDURE on_userpattern_create();


--
-- Name: _contactgroup_contact_id_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY _contactgroup
    ADD CONSTRAINT _contactgroup_contact_id_contact_id_fkey FOREIGN KEY (contact_id) REFERENCES contact(contact_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: _contactgroup_group_id_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY _contactgroup
    ADD CONSTRAINT _contactgroup_group_id_group_id_fkey FOREIGN KEY (group_id) REFERENCES ugroup(group_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: _userpattern_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY _userpattern
    ADD CONSTRAINT _userpattern_id_userobm_id_fkey FOREIGN KEY (id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: account_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY account
    ADD CONSTRAINT account_domain_id_domain_id_fkey FOREIGN KEY (account_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: account_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY account
    ADD CONSTRAINT account_usercreate_userobm_id_fkey FOREIGN KEY (account_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: account_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY account
    ADD CONSTRAINT account_userupdate_userobm_id_fkey FOREIGN KEY (account_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: accountentity_account_id_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY accountentity
    ADD CONSTRAINT accountentity_account_id_account_id_fkey FOREIGN KEY (accountentity_account_id) REFERENCES account(account_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: accountentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY accountentity
    ADD CONSTRAINT accountentity_entity_id_entity_id_fkey FOREIGN KEY (accountentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: activeuserobm_userobm_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY activeuserobm
    ADD CONSTRAINT activeuserobm_userobm_id_userobm_id_fkey FOREIGN KEY (activeuserobm_userobm_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: address_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY address
    ADD CONSTRAINT address_entity_id_entity_id_fkey FOREIGN KEY (address_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: addressbook_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY addressbook
    ADD CONSTRAINT addressbook_domain_id_domain_id_fkey FOREIGN KEY (domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: addressbook_owner_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY addressbook
    ADD CONSTRAINT addressbook_owner_userobm_id_fkey FOREIGN KEY (owner) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: addressbook_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY addressbook
    ADD CONSTRAINT addressbook_usercreate_userobm_id_fkey FOREIGN KEY (usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: addressbook_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY addressbook
    ADD CONSTRAINT addressbook_userupdate_userobm_id_fkey FOREIGN KEY (userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: addressbookentity_addressbook_id_addressbook_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY addressbookentity
    ADD CONSTRAINT addressbookentity_addressbook_id_addressbook_id_fkey FOREIGN KEY (addressbookentity_addressbook_id) REFERENCES addressbook(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: addressbookentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY addressbookentity
    ADD CONSTRAINT addressbookentity_entity_id_entity_id_fkey FOREIGN KEY (addressbookentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: calendarentity_calendar_id_calendar_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY calendarentity
    ADD CONSTRAINT calendarentity_calendar_id_calendar_id_fkey FOREIGN KEY (calendarentity_calendar_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: calendarentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY calendarentity
    ADD CONSTRAINT calendarentity_entity_id_entity_id_fkey FOREIGN KEY (calendarentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: campaign_email_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY campaign
    ADD CONSTRAINT campaign_email_fkey FOREIGN KEY (campaign_email) REFERENCES document(document_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: campaign_parent_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY campaign
    ADD CONSTRAINT campaign_parent_fkey FOREIGN KEY (campaign_parent) REFERENCES campaign(campaign_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: campaigndisabledentity_campaign_id_campaign_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY campaigndisabledentity
    ADD CONSTRAINT campaigndisabledentity_campaign_id_campaign_id_fkey FOREIGN KEY (campaigndisabledentity_campaign_id) REFERENCES campaign(campaign_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: campaigndisabledentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY campaigndisabledentity
    ADD CONSTRAINT campaigndisabledentity_entity_id_entity_id_fkey FOREIGN KEY (campaigndisabledentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: campaignentity_campaign_id_campaign_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY campaignentity
    ADD CONSTRAINT campaignentity_campaign_id_campaign_id_fkey FOREIGN KEY (campaignentity_campaign_id) REFERENCES campaign(campaign_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: campaignentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY campaignentity
    ADD CONSTRAINT campaignentity_entity_id_entity_id_fkey FOREIGN KEY (campaignentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: campaignmailtarget_campaign_id_campaign_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY campaignmailtarget
    ADD CONSTRAINT campaignmailtarget_campaign_id_campaign_id_fkey FOREIGN KEY (campaignmailtarget_campaign_id) REFERENCES campaign(campaign_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: campaignmailtarget_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY campaignmailtarget
    ADD CONSTRAINT campaignmailtarget_entity_id_entity_id_fkey FOREIGN KEY (campaignmailtarget_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: campaigntarget_campaign_id_campaign_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY campaigntarget
    ADD CONSTRAINT campaigntarget_campaign_id_campaign_id_fkey FOREIGN KEY (campaigntarget_campaign_id) REFERENCES campaign(campaign_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: campaigntarget_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY campaigntarget
    ADD CONSTRAINT campaigntarget_entity_id_entity_id_fkey FOREIGN KEY (campaigntarget_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: category_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY category
    ADD CONSTRAINT category_domain_id_domain_id_fkey FOREIGN KEY (category_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: category_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY category
    ADD CONSTRAINT category_usercreate_userobm_id_fkey FOREIGN KEY (category_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: category_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY category
    ADD CONSTRAINT category_userupdate_userobm_id_fkey FOREIGN KEY (category_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: categorylink_category_id_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY categorylink
    ADD CONSTRAINT categorylink_category_id_category_id_fkey FOREIGN KEY (categorylink_category_id) REFERENCES category(category_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: categorylink_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY categorylink
    ADD CONSTRAINT categorylink_entity_id_entity_id_fkey FOREIGN KEY (categorylink_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: commitedoperation_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY commitedoperation
    ADD CONSTRAINT commitedoperation_entity_id_fkey FOREIGN KEY (commitedoperation_entity_id) REFERENCES entity(entity_id) ON DELETE CASCADE;


--
-- Name: company_activity_id_companyactivity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY company
    ADD CONSTRAINT company_activity_id_companyactivity_id_fkey FOREIGN KEY (company_activity_id) REFERENCES companyactivity(companyactivity_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: company_datasource_id_datasource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY company
    ADD CONSTRAINT company_datasource_id_datasource_id_fkey FOREIGN KEY (company_datasource_id) REFERENCES datasource(datasource_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: company_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY company
    ADD CONSTRAINT company_domain_id_domain_id_fkey FOREIGN KEY (company_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: company_marketingmanager_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY company
    ADD CONSTRAINT company_marketingmanager_id_userobm_id_fkey FOREIGN KEY (company_marketingmanager_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: company_nafcode_id_companynafcode_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY company
    ADD CONSTRAINT company_nafcode_id_companynafcode_id_fkey FOREIGN KEY (company_nafcode_id) REFERENCES companynafcode(companynafcode_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: company_type_id_companytype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY company
    ADD CONSTRAINT company_type_id_companytype_id_fkey FOREIGN KEY (company_type_id) REFERENCES companytype(companytype_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: company_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY company
    ADD CONSTRAINT company_usercreate_userobm_id_fkey FOREIGN KEY (company_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: company_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY company
    ADD CONSTRAINT company_userupdate_userobm_id_fkey FOREIGN KEY (company_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: companyactivity_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY companyactivity
    ADD CONSTRAINT companyactivity_domain_id_domain_id_fkey FOREIGN KEY (companyactivity_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: companyactivity_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY companyactivity
    ADD CONSTRAINT companyactivity_usercreate_userobm_id_fkey FOREIGN KEY (companyactivity_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: companyactivity_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY companyactivity
    ADD CONSTRAINT companyactivity_userupdate_userobm_id_fkey FOREIGN KEY (companyactivity_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: companyentity_company_id_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY companyentity
    ADD CONSTRAINT companyentity_company_id_company_id_fkey FOREIGN KEY (companyentity_company_id) REFERENCES company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: companyentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY companyentity
    ADD CONSTRAINT companyentity_entity_id_entity_id_fkey FOREIGN KEY (companyentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: companynafcode_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY companynafcode
    ADD CONSTRAINT companynafcode_domain_id_domain_id_fkey FOREIGN KEY (companynafcode_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: companynafcode_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY companynafcode
    ADD CONSTRAINT companynafcode_usercreate_userobm_id_fkey FOREIGN KEY (companynafcode_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: companynafcode_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY companynafcode
    ADD CONSTRAINT companynafcode_userupdate_userobm_id_fkey FOREIGN KEY (companynafcode_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: companytype_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY companytype
    ADD CONSTRAINT companytype_domain_id_domain_id_fkey FOREIGN KEY (companytype_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: companytype_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY companytype
    ADD CONSTRAINT companytype_usercreate_userobm_id_fkey FOREIGN KEY (companytype_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: companytype_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY companytype
    ADD CONSTRAINT companytype_userupdate_userobm_id_fkey FOREIGN KEY (companytype_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contact_addressbook_id_addressbook_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_addressbook_id_addressbook_id_fkey FOREIGN KEY (contact_addressbook_id) REFERENCES addressbook(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contact_anniversary_id_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_anniversary_id_event_id_fkey FOREIGN KEY (contact_anniversary_id) REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contact_birthday_id_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_birthday_id_event_id_fkey FOREIGN KEY (contact_birthday_id) REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contact_company_id_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_company_id_company_id_fkey FOREIGN KEY (contact_company_id) REFERENCES company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contact_datasource_id_datasource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_datasource_id_datasource_id_fkey FOREIGN KEY (contact_datasource_id) REFERENCES datasource(datasource_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contact_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_domain_id_domain_id_fkey FOREIGN KEY (contact_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contact_function_id_contactfunction_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_function_id_contactfunction_id_fkey FOREIGN KEY (contact_function_id) REFERENCES contactfunction(contactfunction_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contact_kind_id_kind_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_kind_id_kind_id_fkey FOREIGN KEY (contact_kind_id) REFERENCES kind(kind_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contact_marketingmanager_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_marketingmanager_id_userobm_id_fkey FOREIGN KEY (contact_marketingmanager_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contact_photo_id_document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_photo_id_document_id_fkey FOREIGN KEY (contact_photo_id) REFERENCES document(document_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contact_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_usercreate_userobm_id_fkey FOREIGN KEY (contact_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contact_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_userupdate_userobm_id_fkey FOREIGN KEY (contact_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contactentity_contact_id_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contactentity
    ADD CONSTRAINT contactentity_contact_id_contact_id_fkey FOREIGN KEY (contactentity_contact_id) REFERENCES contact(contact_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contactentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contactentity
    ADD CONSTRAINT contactentity_entity_id_entity_id_fkey FOREIGN KEY (contactentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contactfunction_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contactfunction
    ADD CONSTRAINT contactfunction_domain_id_domain_id_fkey FOREIGN KEY (contactfunction_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contactfunction_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contactfunction
    ADD CONSTRAINT contactfunction_usercreate_userobm_id_fkey FOREIGN KEY (contactfunction_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contactfunction_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contactfunction
    ADD CONSTRAINT contactfunction_userupdate_userobm_id_fkey FOREIGN KEY (contactfunction_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contactgroup_contact_id_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contactgroup
    ADD CONSTRAINT contactgroup_contact_id_contact_id_fkey FOREIGN KEY (contact_id) REFERENCES contact(contact_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contactgroup_group_id_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contactgroup
    ADD CONSTRAINT contactgroup_group_id_group_id_fkey FOREIGN KEY (group_id) REFERENCES ugroup(group_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contactlist_contact_id_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contactlist
    ADD CONSTRAINT contactlist_contact_id_contact_id_fkey FOREIGN KEY (contactlist_contact_id) REFERENCES contact(contact_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contactlist_list_id_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contactlist
    ADD CONSTRAINT contactlist_list_id_list_id_fkey FOREIGN KEY (contactlist_list_id) REFERENCES list(list_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contract_company_id_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_company_id_company_id_fkey FOREIGN KEY (contract_company_id) REFERENCES company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contract_contact1_id_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_contact1_id_contact_id_fkey FOREIGN KEY (contract_contact1_id) REFERENCES contact(contact_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contract_contact2_id_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_contact2_id_contact_id_fkey FOREIGN KEY (contract_contact2_id) REFERENCES contact(contact_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contract_deal_id_deal_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_deal_id_deal_id_fkey FOREIGN KEY (contract_deal_id) REFERENCES deal(deal_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contract_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_domain_id_domain_id_fkey FOREIGN KEY (contract_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contract_marketmanager_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_marketmanager_id_userobm_id_fkey FOREIGN KEY (contract_marketmanager_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contract_priority_id_contractpriority_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_priority_id_contractpriority_id_fkey FOREIGN KEY (contract_priority_id) REFERENCES contractpriority(contractpriority_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contract_status_id_contractstatus_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_status_id_contractstatus_id_fkey FOREIGN KEY (contract_status_id) REFERENCES contractstatus(contractstatus_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contract_techmanager_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_techmanager_id_userobm_id_fkey FOREIGN KEY (contract_techmanager_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contract_type_id_contracttype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_type_id_contracttype_id_fkey FOREIGN KEY (contract_type_id) REFERENCES contracttype(contracttype_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contract_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_usercreate_userobm_id_fkey FOREIGN KEY (contract_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contract_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contract
    ADD CONSTRAINT contract_userupdate_userobm_id_fkey FOREIGN KEY (contract_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contractentity_contract_id_contract_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contractentity
    ADD CONSTRAINT contractentity_contract_id_contract_id_fkey FOREIGN KEY (contractentity_contract_id) REFERENCES contract(contract_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contractentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contractentity
    ADD CONSTRAINT contractentity_entity_id_entity_id_fkey FOREIGN KEY (contractentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contractpriority_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contractpriority
    ADD CONSTRAINT contractpriority_domain_id_domain_id_fkey FOREIGN KEY (contractpriority_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contractpriority_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contractpriority
    ADD CONSTRAINT contractpriority_usercreate_userobm_id_fkey FOREIGN KEY (contractpriority_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contractpriority_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contractpriority
    ADD CONSTRAINT contractpriority_userupdate_userobm_id_fkey FOREIGN KEY (contractpriority_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contractstatus_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contractstatus
    ADD CONSTRAINT contractstatus_domain_id_domain_id_fkey FOREIGN KEY (contractstatus_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contractstatus_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contractstatus
    ADD CONSTRAINT contractstatus_usercreate_userobm_id_fkey FOREIGN KEY (contractstatus_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contractstatus_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contractstatus
    ADD CONSTRAINT contractstatus_userupdate_userobm_id_fkey FOREIGN KEY (contractstatus_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contracttype_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contracttype
    ADD CONSTRAINT contracttype_domain_id_domain_id_fkey FOREIGN KEY (contracttype_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: contracttype_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contracttype
    ADD CONSTRAINT contracttype_usercreate_userobm_id_fkey FOREIGN KEY (contracttype_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: contracttype_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY contracttype
    ADD CONSTRAINT contracttype_userupdate_userobm_id_fkey FOREIGN KEY (contracttype_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: country_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY country
    ADD CONSTRAINT country_domain_id_domain_id_fkey FOREIGN KEY (country_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: country_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY country
    ADD CONSTRAINT country_usercreate_userobm_id_fkey FOREIGN KEY (country_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: country_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY country
    ADD CONSTRAINT country_userupdate_userobm_id_fkey FOREIGN KEY (country_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: cv_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY cv
    ADD CONSTRAINT cv_domain_id_domain_id_fkey FOREIGN KEY (cv_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: cv_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY cv
    ADD CONSTRAINT cv_usercreate_userobm_id_fkey FOREIGN KEY (cv_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: cv_userobm_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY cv
    ADD CONSTRAINT cv_userobm_id_userobm_id_fkey FOREIGN KEY (cv_userobm_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: cv_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY cv
    ADD CONSTRAINT cv_userupdate_userobm_id_fkey FOREIGN KEY (cv_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: cventity_cv_id_cv_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY cventity
    ADD CONSTRAINT cventity_cv_id_cv_id_fkey FOREIGN KEY (cventity_cv_id) REFERENCES cv(cv_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: cventity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY cventity
    ADD CONSTRAINT cventity_entity_id_entity_id_fkey FOREIGN KEY (cventity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: datasource_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY datasource
    ADD CONSTRAINT datasource_domain_id_domain_id_fkey FOREIGN KEY (datasource_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: datasource_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY datasource
    ADD CONSTRAINT datasource_usercreate_userobm_id_fkey FOREIGN KEY (datasource_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: datasource_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY datasource
    ADD CONSTRAINT datasource_userupdate_userobm_id_fkey FOREIGN KEY (datasource_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: deal_company_id_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_company_id_company_id_fkey FOREIGN KEY (deal_company_id) REFERENCES company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: deal_contact1_id_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_contact1_id_contact_id_fkey FOREIGN KEY (deal_contact1_id) REFERENCES contact(contact_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: deal_contact2_id_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_contact2_id_contact_id_fkey FOREIGN KEY (deal_contact2_id) REFERENCES contact(contact_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: deal_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_domain_id_domain_id_fkey FOREIGN KEY (deal_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: deal_marketingmanager_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_marketingmanager_id_userobm_id_fkey FOREIGN KEY (deal_marketingmanager_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: deal_parentdeal_id_parentdeal_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_parentdeal_id_parentdeal_id_fkey FOREIGN KEY (deal_parentdeal_id) REFERENCES parentdeal(parentdeal_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: deal_region_id_region_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_region_id_region_id_fkey FOREIGN KEY (deal_region_id) REFERENCES region(region_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: deal_source_id_leadsource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_source_id_leadsource_id_fkey FOREIGN KEY (deal_source_id) REFERENCES leadsource(leadsource_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: deal_tasktype_id_tasktype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_tasktype_id_tasktype_id_fkey FOREIGN KEY (deal_tasktype_id) REFERENCES tasktype(tasktype_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: deal_technicalmanager_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_technicalmanager_id_userobm_id_fkey FOREIGN KEY (deal_technicalmanager_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: deal_type_id_dealtype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_type_id_dealtype_id_fkey FOREIGN KEY (deal_type_id) REFERENCES dealtype(dealtype_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: deal_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_usercreate_userobm_id_fkey FOREIGN KEY (deal_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: deal_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY deal
    ADD CONSTRAINT deal_userupdate_userobm_id_fkey FOREIGN KEY (deal_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: dealcompany_company_id_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY dealcompany
    ADD CONSTRAINT dealcompany_company_id_company_id_fkey FOREIGN KEY (dealcompany_company_id) REFERENCES company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: dealcompany_deal_id_deal_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY dealcompany
    ADD CONSTRAINT dealcompany_deal_id_deal_id_fkey FOREIGN KEY (dealcompany_deal_id) REFERENCES deal(deal_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: dealcompany_role_id_dealcompanyrole_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY dealcompany
    ADD CONSTRAINT dealcompany_role_id_dealcompanyrole_id_fkey FOREIGN KEY (dealcompany_role_id) REFERENCES dealcompanyrole(dealcompanyrole_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: dealcompany_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY dealcompany
    ADD CONSTRAINT dealcompany_usercreate_userobm_id_fkey FOREIGN KEY (dealcompany_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: dealcompany_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY dealcompany
    ADD CONSTRAINT dealcompany_userupdate_userobm_id_fkey FOREIGN KEY (dealcompany_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: dealcompanyrole_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY dealcompanyrole
    ADD CONSTRAINT dealcompanyrole_domain_id_domain_id_fkey FOREIGN KEY (dealcompanyrole_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: dealcompanyrole_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY dealcompanyrole
    ADD CONSTRAINT dealcompanyrole_usercreate_userobm_id_fkey FOREIGN KEY (dealcompanyrole_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: dealcompanyrole_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY dealcompanyrole
    ADD CONSTRAINT dealcompanyrole_userupdate_userobm_id_fkey FOREIGN KEY (dealcompanyrole_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: dealentity_deal_id_deal_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY dealentity
    ADD CONSTRAINT dealentity_deal_id_deal_id_fkey FOREIGN KEY (dealentity_deal_id) REFERENCES deal(deal_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: dealentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY dealentity
    ADD CONSTRAINT dealentity_entity_id_entity_id_fkey FOREIGN KEY (dealentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: dealstatus_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY dealstatus
    ADD CONSTRAINT dealstatus_domain_id_domain_id_fkey FOREIGN KEY (dealstatus_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: dealstatus_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY dealstatus
    ADD CONSTRAINT dealstatus_usercreate_userobm_id_fkey FOREIGN KEY (dealstatus_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: dealstatus_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY dealstatus
    ADD CONSTRAINT dealstatus_userupdate_userobm_id_fkey FOREIGN KEY (dealstatus_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: dealtype_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY dealtype
    ADD CONSTRAINT dealtype_domain_id_domain_id_fkey FOREIGN KEY (dealtype_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: dealtype_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY dealtype
    ADD CONSTRAINT dealtype_usercreate_userobm_id_fkey FOREIGN KEY (dealtype_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: dealtype_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY dealtype
    ADD CONSTRAINT dealtype_userupdate_userobm_id_fkey FOREIGN KEY (dealtype_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: defaultodttemplate_document_id_document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY defaultodttemplate
    ADD CONSTRAINT defaultodttemplate_document_id_document_id_fkey FOREIGN KEY (defaultodttemplate_document_id) REFERENCES document(document_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: defaultodttemplate_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY defaultodttemplate
    ADD CONSTRAINT defaultodttemplate_domain_id_domain_id_fkey FOREIGN KEY (defaultodttemplate_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: deleted_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY deleted
    ADD CONSTRAINT deleted_domain_id_domain_id_fkey FOREIGN KEY (deleted_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: deleted_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY deleted
    ADD CONSTRAINT deleted_user_id_userobm_id_fkey FOREIGN KEY (deleted_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: deletedeventlink_event_id_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY deletedeventlink
    ADD CONSTRAINT deletedeventlink_event_id_event_id_fkey FOREIGN KEY (deletedeventlink_event_id) REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: deletedeventlink_userobm_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY deletedeventlink
    ADD CONSTRAINT deletedeventlink_userobm_id_userobm_id_fkey FOREIGN KEY (deletedeventlink_userobm_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: deletedsyncedaddressbook_addressbook_id_addressbook_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY deletedsyncedaddressbook
    ADD CONSTRAINT deletedsyncedaddressbook_addressbook_id_addressbook_id_fkey FOREIGN KEY (addressbook_id) REFERENCES addressbook(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: deletedsyncedaddressbook_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY deletedsyncedaddressbook
    ADD CONSTRAINT deletedsyncedaddressbook_user_id_userobm_id_fkey FOREIGN KEY (user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: display_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY displaypref
    ADD CONSTRAINT display_user_id_userobm_id_fkey FOREIGN KEY (display_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: document_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY document
    ADD CONSTRAINT document_domain_id_domain_id_fkey FOREIGN KEY (document_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: document_mimetype_id_documentmimetype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY document
    ADD CONSTRAINT document_mimetype_id_documentmimetype_id_fkey FOREIGN KEY (document_mimetype_id) REFERENCES documentmimetype(documentmimetype_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: document_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY document
    ADD CONSTRAINT document_usercreate_userobm_id_fkey FOREIGN KEY (document_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: document_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY document
    ADD CONSTRAINT document_userupdate_userobm_id_fkey FOREIGN KEY (document_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: documententity_document_id_document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY documententity
    ADD CONSTRAINT documententity_document_id_document_id_fkey FOREIGN KEY (documententity_document_id) REFERENCES document(document_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: documententity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY documententity
    ADD CONSTRAINT documententity_entity_id_entity_id_fkey FOREIGN KEY (documententity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: documentlink_document_id_document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY documentlink
    ADD CONSTRAINT documentlink_document_id_document_id_fkey FOREIGN KEY (documentlink_document_id) REFERENCES document(document_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: documentlink_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY documentlink
    ADD CONSTRAINT documentlink_entity_id_entity_id_fkey FOREIGN KEY (documentlink_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: documentlink_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY documentlink
    ADD CONSTRAINT documentlink_usercreate_userobm_id_fkey FOREIGN KEY (documentlink_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: documentmimetype_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY documentmimetype
    ADD CONSTRAINT documentmimetype_domain_id_domain_id_fkey FOREIGN KEY (documentmimetype_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: documentmimetype_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY documentmimetype
    ADD CONSTRAINT documentmimetype_usercreate_userobm_id_fkey FOREIGN KEY (documentmimetype_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: documentmimetype_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY documentmimetype
    ADD CONSTRAINT documentmimetype_userupdate_userobm_id_fkey FOREIGN KEY (documentmimetype_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: domain_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY domain
    ADD CONSTRAINT domain_usercreate_userobm_id_fkey FOREIGN KEY (domain_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: domain_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY domain
    ADD CONSTRAINT domain_userupdate_userobm_id_fkey FOREIGN KEY (domain_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: domainentity_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY domainentity
    ADD CONSTRAINT domainentity_domain_id_domain_id_fkey FOREIGN KEY (domainentity_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: domainentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY domainentity
    ADD CONSTRAINT domainentity_entity_id_entity_id_fkey FOREIGN KEY (domainentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: domainpropertyvalue_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY domainpropertyvalue
    ADD CONSTRAINT domainpropertyvalue_domain_id_domain_id_fkey FOREIGN KEY (domainpropertyvalue_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: email_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY email
    ADD CONSTRAINT email_entity_id_entity_id_fkey FOREIGN KEY (email_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY calendarcolor
    ADD CONSTRAINT entity_id_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: entityright_consumer_id_entity_id; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY entityright
    ADD CONSTRAINT entityright_consumer_id_entity_id FOREIGN KEY (entityright_consumer_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: entityright_entity_id_entity_id; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY entityright
    ADD CONSTRAINT entityright_entity_id_entity_id FOREIGN KEY (entityright_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: event_category1_id_eventcategory1_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY event
    ADD CONSTRAINT event_category1_id_eventcategory1_id_fkey FOREIGN KEY (event_category1_id) REFERENCES eventcategory1(eventcategory1_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: event_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY event
    ADD CONSTRAINT event_domain_id_domain_id_fkey FOREIGN KEY (event_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: event_owner_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY event
    ADD CONSTRAINT event_owner_userobm_id_fkey FOREIGN KEY (event_owner) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: event_tag_id_eventtag_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY event
    ADD CONSTRAINT event_tag_id_eventtag_id_fkey FOREIGN KEY (event_tag_id) REFERENCES eventtag(eventtag_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: event_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY event
    ADD CONSTRAINT event_usercreate_userobm_id_fkey FOREIGN KEY (event_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: event_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY event
    ADD CONSTRAINT event_userupdate_userobm_id_fkey FOREIGN KEY (event_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: eventalert_event_id_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventalert
    ADD CONSTRAINT eventalert_event_id_event_id_fkey FOREIGN KEY (eventalert_event_id) REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: eventalert_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventalert
    ADD CONSTRAINT eventalert_user_id_userobm_id_fkey FOREIGN KEY (eventalert_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: eventalert_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventalert
    ADD CONSTRAINT eventalert_usercreate_userobm_id_fkey FOREIGN KEY (eventalert_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: eventalert_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventalert
    ADD CONSTRAINT eventalert_userupdate_userobm_id_fkey FOREIGN KEY (eventalert_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: eventcategory1_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventcategory1
    ADD CONSTRAINT eventcategory1_domain_id_domain_id_fkey FOREIGN KEY (eventcategory1_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: eventcategory1_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventcategory1
    ADD CONSTRAINT eventcategory1_usercreate_userobm_id_fkey FOREIGN KEY (eventcategory1_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: eventcategory1_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventcategory1
    ADD CONSTRAINT eventcategory1_userupdate_userobm_id_fkey FOREIGN KEY (eventcategory1_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: evententity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY evententity
    ADD CONSTRAINT evententity_entity_id_entity_id_fkey FOREIGN KEY (evententity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: evententity_event_id_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY evententity
    ADD CONSTRAINT evententity_event_id_event_id_fkey FOREIGN KEY (evententity_event_id) REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: eventexception_child_id_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventexception
    ADD CONSTRAINT eventexception_child_id_event_id_fkey FOREIGN KEY (eventexception_child_id) REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: eventexception_parent_id_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventexception
    ADD CONSTRAINT eventexception_parent_id_event_id_fkey FOREIGN KEY (eventexception_parent_id) REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: eventexception_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventexception
    ADD CONSTRAINT eventexception_usercreate_userobm_id_fkey FOREIGN KEY (eventexception_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: eventexception_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventexception
    ADD CONSTRAINT eventexception_userupdate_userobm_id_fkey FOREIGN KEY (eventexception_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: eventlink_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventlink
    ADD CONSTRAINT eventlink_entity_id_entity_id_fkey FOREIGN KEY (eventlink_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: eventlink_event_id_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventlink
    ADD CONSTRAINT eventlink_event_id_event_id_fkey FOREIGN KEY (eventlink_event_id) REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: eventlink_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventlink
    ADD CONSTRAINT eventlink_usercreate_userobm_id_fkey FOREIGN KEY (eventlink_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: eventlink_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventlink
    ADD CONSTRAINT eventlink_userupdate_userobm_id_fkey FOREIGN KEY (eventlink_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: eventtag_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventtag
    ADD CONSTRAINT eventtag_user_id_userobm_id_fkey FOREIGN KEY (eventtag_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: eventtemplate_category1_id_eventcategory1_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventtemplate
    ADD CONSTRAINT eventtemplate_category1_id_eventcategory1_id_fkey FOREIGN KEY (eventtemplate_category1_id) REFERENCES eventcategory1(eventcategory1_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: eventtemplate_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventtemplate
    ADD CONSTRAINT eventtemplate_domain_id_domain_id_fkey FOREIGN KEY (eventtemplate_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: eventtemplate_owner_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventtemplate
    ADD CONSTRAINT eventtemplate_owner_userobm_id_fkey FOREIGN KEY (eventtemplate_owner) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: eventtemplate_tag_id_eventtag_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventtemplate
    ADD CONSTRAINT eventtemplate_tag_id_eventtag_id_fkey FOREIGN KEY (eventtemplate_tag_id) REFERENCES eventtag(eventtag_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: eventtemplate_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventtemplate
    ADD CONSTRAINT eventtemplate_usercreate_userobm_id_fkey FOREIGN KEY (eventtemplate_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: eventtemplate_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY eventtemplate
    ADD CONSTRAINT eventtemplate_userupdate_userobm_id_fkey FOREIGN KEY (eventtemplate_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: field_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY field
    ADD CONSTRAINT field_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: group_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY ugroup
    ADD CONSTRAINT group_domain_id_domain_id_fkey FOREIGN KEY (group_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: group_manager_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY ugroup
    ADD CONSTRAINT group_manager_id_userobm_id_fkey FOREIGN KEY (group_manager_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: group_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY ugroup
    ADD CONSTRAINT group_usercreate_userobm_id_fkey FOREIGN KEY (group_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: group_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY ugroup
    ADD CONSTRAINT group_userupdate_userobm_id_fkey FOREIGN KEY (group_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: groupentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY groupentity
    ADD CONSTRAINT groupentity_entity_id_entity_id_fkey FOREIGN KEY (groupentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: groupentity_group_id_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY groupentity
    ADD CONSTRAINT groupentity_group_id_group_id_fkey FOREIGN KEY (groupentity_group_id) REFERENCES ugroup(group_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: groupgroup_child_id_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY groupgroup
    ADD CONSTRAINT groupgroup_child_id_group_id_fkey FOREIGN KEY (groupgroup_child_id) REFERENCES ugroup(group_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: groupgroup_parent_id_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY groupgroup
    ADD CONSTRAINT groupgroup_parent_id_group_id_fkey FOREIGN KEY (groupgroup_parent_id) REFERENCES ugroup(group_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: host_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY host
    ADD CONSTRAINT host_domain_id_domain_id_fkey FOREIGN KEY (host_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: host_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY host
    ADD CONSTRAINT host_usercreate_userobm_id_fkey FOREIGN KEY (host_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: host_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY host
    ADD CONSTRAINT host_userupdate_userobm_id_fkey FOREIGN KEY (host_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: hostentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY hostentity
    ADD CONSTRAINT hostentity_entity_id_entity_id_fkey FOREIGN KEY (hostentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: hostentity_host_id_host_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY hostentity
    ADD CONSTRAINT hostentity_host_id_host_id_fkey FOREIGN KEY (hostentity_host_id) REFERENCES host(host_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: im_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY im
    ADD CONSTRAINT im_entity_id_entity_id_fkey FOREIGN KEY (im_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: import_datasource_id_datasource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY import
    ADD CONSTRAINT import_datasource_id_datasource_id_fkey FOREIGN KEY (import_datasource_id) REFERENCES datasource(datasource_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: import_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY import
    ADD CONSTRAINT import_domain_id_domain_id_fkey FOREIGN KEY (import_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: import_marketingmanager_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY import
    ADD CONSTRAINT import_marketingmanager_id_userobm_id_fkey FOREIGN KEY (import_marketingmanager_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: import_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY import
    ADD CONSTRAINT import_usercreate_userobm_id_fkey FOREIGN KEY (import_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: import_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY import
    ADD CONSTRAINT import_userupdate_userobm_id_fkey FOREIGN KEY (import_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: importentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY importentity
    ADD CONSTRAINT importentity_entity_id_entity_id_fkey FOREIGN KEY (importentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: importentity_import_id_import_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY importentity
    ADD CONSTRAINT importentity_import_id_import_id_fkey FOREIGN KEY (importentity_import_id) REFERENCES import(import_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: incident_contract_id_contract_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_contract_id_contract_id_fkey FOREIGN KEY (incident_contract_id) REFERENCES contract(contract_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: incident_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_domain_id_domain_id_fkey FOREIGN KEY (incident_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: incident_logger_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_logger_userobm_id_fkey FOREIGN KEY (incident_logger) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incident_owner_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_owner_userobm_id_fkey FOREIGN KEY (incident_owner) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incident_priority_id_incidentpriority_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_priority_id_incidentpriority_id_fkey FOREIGN KEY (incident_priority_id) REFERENCES incidentpriority(incidentpriority_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incident_resolutiontype_id_incidentresolutiontype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_resolutiontype_id_incidentresolutiontype_id_fkey FOREIGN KEY (incident_resolutiontype_id) REFERENCES incidentresolutiontype(incidentresolutiontype_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incident_status_id_incidentstatus_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_status_id_incidentstatus_id_fkey FOREIGN KEY (incident_status_id) REFERENCES incidentstatus(incidentstatus_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incident_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_usercreate_userobm_id_fkey FOREIGN KEY (incident_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incident_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_userupdate_userobm_id_fkey FOREIGN KEY (incident_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incidententity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incidententity
    ADD CONSTRAINT incidententity_entity_id_entity_id_fkey FOREIGN KEY (incidententity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: incidententity_incident_id_incident_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incidententity
    ADD CONSTRAINT incidententity_incident_id_incident_id_fkey FOREIGN KEY (incidententity_incident_id) REFERENCES incident(incident_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: incidentpriority_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incidentpriority
    ADD CONSTRAINT incidentpriority_domain_id_domain_id_fkey FOREIGN KEY (incidentpriority_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: incidentpriority_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incidentpriority
    ADD CONSTRAINT incidentpriority_usercreate_userobm_id_fkey FOREIGN KEY (incidentpriority_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incidentpriority_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incidentpriority
    ADD CONSTRAINT incidentpriority_userupdate_userobm_id_fkey FOREIGN KEY (incidentpriority_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incidentresolutiontype_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incidentresolutiontype
    ADD CONSTRAINT incidentresolutiontype_domain_id_domain_id_fkey FOREIGN KEY (incidentresolutiontype_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: incidentresolutiontype_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incidentresolutiontype
    ADD CONSTRAINT incidentresolutiontype_usercreate_userobm_id_fkey FOREIGN KEY (incidentresolutiontype_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incidentresolutiontype_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incidentresolutiontype
    ADD CONSTRAINT incidentresolutiontype_userupdate_userobm_id_fkey FOREIGN KEY (incidentresolutiontype_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incidentstatus_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incidentstatus
    ADD CONSTRAINT incidentstatus_domain_id_domain_id_fkey FOREIGN KEY (incidentstatus_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: incidentstatus_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incidentstatus
    ADD CONSTRAINT incidentstatus_usercreate_userobm_id_fkey FOREIGN KEY (incidentstatus_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: incidentstatus_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY incidentstatus
    ADD CONSTRAINT incidentstatus_userupdate_userobm_id_fkey FOREIGN KEY (incidentstatus_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: invoice_company_id_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY invoice
    ADD CONSTRAINT invoice_company_id_company_id_fkey FOREIGN KEY (invoice_company_id) REFERENCES company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: invoice_deal_id_deal_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY invoice
    ADD CONSTRAINT invoice_deal_id_deal_id_fkey FOREIGN KEY (invoice_deal_id) REFERENCES deal(deal_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: invoice_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY invoice
    ADD CONSTRAINT invoice_domain_id_domain_id_fkey FOREIGN KEY (invoice_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: invoice_project_id_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY invoice
    ADD CONSTRAINT invoice_project_id_project_id_fkey FOREIGN KEY (invoice_project_id) REFERENCES project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: invoice_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY invoice
    ADD CONSTRAINT invoice_usercreate_userobm_id_fkey FOREIGN KEY (invoice_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: invoice_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY invoice
    ADD CONSTRAINT invoice_userupdate_userobm_id_fkey FOREIGN KEY (invoice_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: invoiceentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY invoiceentity
    ADD CONSTRAINT invoiceentity_entity_id_entity_id_fkey FOREIGN KEY (invoiceentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: invoiceentity_invoice_id_invoice_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY invoiceentity
    ADD CONSTRAINT invoiceentity_invoice_id_invoice_id_fkey FOREIGN KEY (invoiceentity_invoice_id) REFERENCES invoice(invoice_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: kind_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY kind
    ADD CONSTRAINT kind_domain_id_domain_id_fkey FOREIGN KEY (kind_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: kind_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY kind
    ADD CONSTRAINT kind_usercreate_userobm_id_fkey FOREIGN KEY (kind_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: kind_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY kind
    ADD CONSTRAINT kind_userupdate_userobm_id_fkey FOREIGN KEY (kind_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: lead_company_id_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY lead
    ADD CONSTRAINT lead_company_id_company_id_fkey FOREIGN KEY (lead_company_id) REFERENCES company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: lead_contact_id_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY lead
    ADD CONSTRAINT lead_contact_id_contact_id_fkey FOREIGN KEY (lead_contact_id) REFERENCES contact(contact_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: lead_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY lead
    ADD CONSTRAINT lead_domain_id_domain_id_fkey FOREIGN KEY (lead_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: lead_manager_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY lead
    ADD CONSTRAINT lead_manager_id_userobm_id_fkey FOREIGN KEY (lead_manager_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: lead_source_id_leadsource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY lead
    ADD CONSTRAINT lead_source_id_leadsource_id_fkey FOREIGN KEY (lead_source_id) REFERENCES leadsource(leadsource_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: lead_status_id_leadstatus_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY lead
    ADD CONSTRAINT lead_status_id_leadstatus_id_fkey FOREIGN KEY (lead_status_id) REFERENCES leadstatus(leadstatus_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: lead_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY lead
    ADD CONSTRAINT lead_usercreate_userobm_id_fkey FOREIGN KEY (lead_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: lead_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY lead
    ADD CONSTRAINT lead_userupdate_userobm_id_fkey FOREIGN KEY (lead_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: leadentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY leadentity
    ADD CONSTRAINT leadentity_entity_id_entity_id_fkey FOREIGN KEY (leadentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: leadentity_lead_id_lead_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY leadentity
    ADD CONSTRAINT leadentity_lead_id_lead_id_fkey FOREIGN KEY (leadentity_lead_id) REFERENCES lead(lead_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: leadsource_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY leadsource
    ADD CONSTRAINT leadsource_domain_id_domain_id_fkey FOREIGN KEY (leadsource_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: leadsource_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY leadsource
    ADD CONSTRAINT leadsource_usercreate_userobm_id_fkey FOREIGN KEY (leadsource_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: leadsource_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY leadsource
    ADD CONSTRAINT leadsource_userupdate_userobm_id_fkey FOREIGN KEY (leadsource_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: leadstatus_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY leadstatus
    ADD CONSTRAINT leadstatus_domain_id_domain_id_fkey FOREIGN KEY (leadstatus_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: leadstatus_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY leadstatus
    ADD CONSTRAINT leadstatus_usercreate_userobm_id_fkey FOREIGN KEY (leadstatus_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: leadstatus_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY leadstatus
    ADD CONSTRAINT leadstatus_userupdate_userobm_id_fkey FOREIGN KEY (leadstatus_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: list_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY list
    ADD CONSTRAINT list_domain_id_domain_id_fkey FOREIGN KEY (list_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: list_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY list
    ADD CONSTRAINT list_usercreate_userobm_id_fkey FOREIGN KEY (list_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: list_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY list
    ADD CONSTRAINT list_userupdate_userobm_id_fkey FOREIGN KEY (list_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: listentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY listentity
    ADD CONSTRAINT listentity_entity_id_entity_id_fkey FOREIGN KEY (listentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: listentity_list_id_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY listentity
    ADD CONSTRAINT listentity_list_id_list_id_fkey FOREIGN KEY (listentity_list_id) REFERENCES list(list_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: mailboxentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY mailboxentity
    ADD CONSTRAINT mailboxentity_entity_id_entity_id_fkey FOREIGN KEY (mailboxentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: mailboxentity_mailbox_id_mailbox_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY mailboxentity
    ADD CONSTRAINT mailboxentity_mailbox_id_mailbox_id_fkey FOREIGN KEY (mailboxentity_mailbox_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: mailinglist_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY mailinglist
    ADD CONSTRAINT mailinglist_domain_id_domain_id_fkey FOREIGN KEY (mailinglist_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: mailinglist_owner_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY mailinglist
    ADD CONSTRAINT mailinglist_owner_userobm_id_fkey FOREIGN KEY (mailinglist_owner) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: mailinglist_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY mailinglist
    ADD CONSTRAINT mailinglist_usercreate_userobm_id_fkey FOREIGN KEY (mailinglist_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: mailinglist_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY mailinglist
    ADD CONSTRAINT mailinglist_userupdate_userobm_id_fkey FOREIGN KEY (mailinglist_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: mailinglistemail_mailinglist_id_mailinglist_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY mailinglistemail
    ADD CONSTRAINT mailinglistemail_mailinglist_id_mailinglist_id_fkey FOREIGN KEY (mailinglistemail_mailinglist_id) REFERENCES mailinglist(mailinglist_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: mailshare_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY mailshare
    ADD CONSTRAINT mailshare_domain_id_domain_id_fkey FOREIGN KEY (mailshare_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: mailshare_mail_server_id_mailserver_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY mailshare
    ADD CONSTRAINT mailshare_mail_server_id_mailserver_id_fkey FOREIGN KEY (mailshare_mail_server_id) REFERENCES host(host_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: mailshare_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY mailshare
    ADD CONSTRAINT mailshare_usercreate_userobm_id_fkey FOREIGN KEY (mailshare_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: mailshare_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY mailshare
    ADD CONSTRAINT mailshare_userupdate_userobm_id_fkey FOREIGN KEY (mailshare_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: mailshareentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY mailshareentity
    ADD CONSTRAINT mailshareentity_entity_id_entity_id_fkey FOREIGN KEY (mailshareentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: mailshareentity_mailshare_id_mailshare_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY mailshareentity
    ADD CONSTRAINT mailshareentity_mailshare_id_mailshare_id_fkey FOREIGN KEY (mailshareentity_mailshare_id) REFERENCES mailshare(mailshare_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: obmbookmark_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY obmbookmark
    ADD CONSTRAINT obmbookmark_user_id_userobm_id_fkey FOREIGN KEY (obmbookmark_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: obmbookmarkentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY obmbookmarkentity
    ADD CONSTRAINT obmbookmarkentity_entity_id_entity_id_fkey FOREIGN KEY (obmbookmarkentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: obmbookmarkentity_obmbookmark_id_obmbookmark_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY obmbookmarkentity
    ADD CONSTRAINT obmbookmarkentity_obmbookmark_id_obmbookmark_id_fkey FOREIGN KEY (obmbookmarkentity_obmbookmark_id) REFERENCES obmbookmark(obmbookmark_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: obmbookmarkproperty_bookmark_id_obmbookmark_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY obmbookmarkproperty
    ADD CONSTRAINT obmbookmarkproperty_bookmark_id_obmbookmark_id_fkey FOREIGN KEY (obmbookmarkproperty_bookmark_id) REFERENCES obmbookmark(obmbookmark_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: of_usergroup_group_id_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY of_usergroup
    ADD CONSTRAINT of_usergroup_group_id_group_id_fkey FOREIGN KEY (of_usergroup_group_id) REFERENCES ugroup(group_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: of_usergroup_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY of_usergroup
    ADD CONSTRAINT of_usergroup_user_id_userobm_id_fkey FOREIGN KEY (of_usergroup_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ogroup_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY ogroup
    ADD CONSTRAINT ogroup_domain_id_domain_id_fkey FOREIGN KEY (ogroup_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ogroup_organizationalchart_id_organizationalchart_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY ogroup
    ADD CONSTRAINT ogroup_organizationalchart_id_organizationalchart_id_fkey FOREIGN KEY (ogroup_organizationalchart_id) REFERENCES organizationalchart(organizationalchart_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ogroup_parent_id_ogroup_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY ogroup
    ADD CONSTRAINT ogroup_parent_id_ogroup_id_fkey FOREIGN KEY (ogroup_parent_id) REFERENCES ogroup(ogroup_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ogroup_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY ogroup
    ADD CONSTRAINT ogroup_usercreate_userobm_id_fkey FOREIGN KEY (ogroup_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: ogroup_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY ogroup
    ADD CONSTRAINT ogroup_userupdate_userobm_id_fkey FOREIGN KEY (ogroup_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: ogroupentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY ogroupentity
    ADD CONSTRAINT ogroupentity_entity_id_entity_id_fkey FOREIGN KEY (ogroupentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ogroupentity_ogroup_id_ogroup_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY ogroupentity
    ADD CONSTRAINT ogroupentity_ogroup_id_ogroup_id_fkey FOREIGN KEY (ogroupentity_ogroup_id) REFERENCES ogroup(ogroup_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ogrouplink_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY ogrouplink
    ADD CONSTRAINT ogrouplink_domain_id_domain_id_fkey FOREIGN KEY (ogrouplink_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ogrouplink_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY ogrouplink
    ADD CONSTRAINT ogrouplink_entity_id_entity_id_fkey FOREIGN KEY (ogrouplink_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ogrouplink_ogroup_id_ogroup_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY ogrouplink
    ADD CONSTRAINT ogrouplink_ogroup_id_ogroup_id_fkey FOREIGN KEY (ogrouplink_ogroup_id) REFERENCES ogroup(ogroup_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ogrouplink_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY ogrouplink
    ADD CONSTRAINT ogrouplink_usercreate_userobm_id_fkey FOREIGN KEY (ogrouplink_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: ogrouplink_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY ogrouplink
    ADD CONSTRAINT ogrouplink_userupdate_userobm_id_fkey FOREIGN KEY (ogrouplink_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: opush_device_owner_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_device
    ADD CONSTRAINT opush_device_owner_fkey FOREIGN KEY (owner) REFERENCES userobm(userobm_id) ON DELETE CASCADE;


--
-- Name: opush_event_mapping_device_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_event_mapping
    ADD CONSTRAINT opush_event_mapping_device_id_fkey FOREIGN KEY (device_id) REFERENCES opush_device(id) ON DELETE CASCADE;


--
-- Name: opush_folder_mapping_device_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_folder_mapping
    ADD CONSTRAINT opush_folder_mapping_device_id_fkey FOREIGN KEY (device_id) REFERENCES opush_device(id) ON DELETE CASCADE;


--
-- Name: opush_folder_snapshot_collection_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_folder_snapshot
    ADD CONSTRAINT opush_folder_snapshot_collection_id_fkey FOREIGN KEY (collection_id) REFERENCES opush_folder_mapping(id) ON DELETE CASCADE;


--
-- Name: opush_folder_snapshot_folder_sync_state_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_folder_snapshot
    ADD CONSTRAINT opush_folder_snapshot_folder_sync_state_id_fkey FOREIGN KEY (folder_sync_state_id) REFERENCES opush_folder_sync_state(id) ON DELETE CASCADE;


--
-- Name: opush_folder_sync_state_backend_mappi_folder_sync_state_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_folder_sync_state_backend_mapping
    ADD CONSTRAINT opush_folder_sync_state_backend_mappi_folder_sync_state_id_fkey FOREIGN KEY (folder_sync_state_id) REFERENCES opush_folder_sync_state(id) ON DELETE CASCADE;


--
-- Name: opush_folder_sync_state_device_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_folder_sync_state
    ADD CONSTRAINT opush_folder_sync_state_device_id_fkey FOREIGN KEY (device_id) REFERENCES opush_device(id) ON DELETE CASCADE;


--
-- Name: opush_ping_heartbeat_device_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_ping_heartbeat
    ADD CONSTRAINT opush_ping_heartbeat_device_id_fkey FOREIGN KEY (device_id) REFERENCES opush_device(id) ON DELETE CASCADE;


--
-- Name: opush_sync_perms_device_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_sync_perms
    ADD CONSTRAINT opush_sync_perms_device_id_fkey FOREIGN KEY (device_id) REFERENCES opush_device(id) ON DELETE CASCADE;


--
-- Name: opush_sync_perms_owner_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_sync_perms
    ADD CONSTRAINT opush_sync_perms_owner_fkey FOREIGN KEY (owner) REFERENCES userobm(userobm_id) ON DELETE CASCADE;


--
-- Name: opush_sync_perms_policy_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_sync_perms
    ADD CONSTRAINT opush_sync_perms_policy_fkey FOREIGN KEY (policy) REFERENCES opush_sec_policy(id) ON DELETE CASCADE;


--
-- Name: opush_sync_state_collection_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_sync_state
    ADD CONSTRAINT opush_sync_state_collection_id_fkey FOREIGN KEY (collection_id) REFERENCES opush_folder_mapping(id) ON DELETE CASCADE;


--
-- Name: opush_sync_state_device_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_sync_state
    ADD CONSTRAINT opush_sync_state_device_id_fkey FOREIGN KEY (device_id) REFERENCES opush_device(id) ON DELETE CASCADE;


--
-- Name: opush_synced_item_sync_state_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY opush_synced_item
    ADD CONSTRAINT opush_synced_item_sync_state_id_fkey FOREIGN KEY (sync_state_id) REFERENCES opush_sync_state(id) ON DELETE CASCADE;


--
-- Name: organizationalchart_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY organizationalchart
    ADD CONSTRAINT organizationalchart_domain_id_domain_id_fkey FOREIGN KEY (organizationalchart_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: organizationalchart_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY organizationalchart
    ADD CONSTRAINT organizationalchart_usercreate_userobm_id_fkey FOREIGN KEY (organizationalchart_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: organizationalchart_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY organizationalchart
    ADD CONSTRAINT organizationalchart_userupdate_userobm_id_fkey FOREIGN KEY (organizationalchart_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: organizationalchartentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY organizationalchartentity
    ADD CONSTRAINT organizationalchartentity_entity_id_entity_id_fkey FOREIGN KEY (organizationalchartentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: organizationalchartentity_organizationalchart_id_organizational; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY organizationalchartentity
    ADD CONSTRAINT organizationalchartentity_organizationalchart_id_organizational FOREIGN KEY (organizationalchartentity_organizationalchart_id) REFERENCES organizationalchart(organizationalchart_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: parentdeal_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY parentdeal
    ADD CONSTRAINT parentdeal_domain_id_domain_id_fkey FOREIGN KEY (parentdeal_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: parentdeal_marketingmanager_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY parentdeal
    ADD CONSTRAINT parentdeal_marketingmanager_id_userobm_id_fkey FOREIGN KEY (parentdeal_marketingmanager_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: parentdeal_technicalmanager_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY parentdeal
    ADD CONSTRAINT parentdeal_technicalmanager_id_userobm_id_fkey FOREIGN KEY (parentdeal_technicalmanager_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: parentdeal_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY parentdeal
    ADD CONSTRAINT parentdeal_usercreate_userobm_id_fkey FOREIGN KEY (parentdeal_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: parentdeal_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY parentdeal
    ADD CONSTRAINT parentdeal_userupdate_userobm_id_fkey FOREIGN KEY (parentdeal_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: parentdealentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY parentdealentity
    ADD CONSTRAINT parentdealentity_entity_id_entity_id_fkey FOREIGN KEY (parentdealentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: parentdealentity_parentdeal_id_parentdeal_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY parentdealentity
    ADD CONSTRAINT parentdealentity_parentdeal_id_parentdeal_id_fkey FOREIGN KEY (parentdealentity_parentdeal_id) REFERENCES parentdeal(parentdeal_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: payment_account_id_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY payment
    ADD CONSTRAINT payment_account_id_account_id_fkey FOREIGN KEY (payment_account_id) REFERENCES account(account_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: payment_company_id_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY payment
    ADD CONSTRAINT payment_company_id_company_id_fkey FOREIGN KEY (payment_company_id) REFERENCES company(company_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: payment_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY payment
    ADD CONSTRAINT payment_domain_id_domain_id_fkey FOREIGN KEY (payment_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: payment_paymentkind_id_paymentkind_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY payment
    ADD CONSTRAINT payment_paymentkind_id_paymentkind_id_fkey FOREIGN KEY (payment_paymentkind_id) REFERENCES paymentkind(paymentkind_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: payment_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY payment
    ADD CONSTRAINT payment_usercreate_userobm_id_fkey FOREIGN KEY (payment_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: payment_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY payment
    ADD CONSTRAINT payment_userupdate_userobm_id_fkey FOREIGN KEY (payment_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: paymententity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY paymententity
    ADD CONSTRAINT paymententity_entity_id_entity_id_fkey FOREIGN KEY (paymententity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: paymententity_payment_id_payment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY paymententity
    ADD CONSTRAINT paymententity_payment_id_payment_id_fkey FOREIGN KEY (paymententity_payment_id) REFERENCES payment(payment_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: paymentinvoice_invoice_id_invoice_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY paymentinvoice
    ADD CONSTRAINT paymentinvoice_invoice_id_invoice_id_fkey FOREIGN KEY (paymentinvoice_invoice_id) REFERENCES invoice(invoice_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: paymentinvoice_payment_id_payment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY paymentinvoice
    ADD CONSTRAINT paymentinvoice_payment_id_payment_id_fkey FOREIGN KEY (paymentinvoice_payment_id) REFERENCES payment(payment_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: paymentinvoice_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY paymentinvoice
    ADD CONSTRAINT paymentinvoice_usercreate_userobm_id_fkey FOREIGN KEY (paymentinvoice_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: paymentinvoice_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY paymentinvoice
    ADD CONSTRAINT paymentinvoice_userupdate_userobm_id_fkey FOREIGN KEY (paymentinvoice_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: paymentkind_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY paymentkind
    ADD CONSTRAINT paymentkind_domain_id_domain_id_fkey FOREIGN KEY (paymentkind_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: phone_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY phone
    ADD CONSTRAINT phone_entity_id_entity_id_fkey FOREIGN KEY (phone_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: plannedtask_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY plannedtask
    ADD CONSTRAINT plannedtask_domain_id_domain_id_fkey FOREIGN KEY (plannedtask_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: plannedtask_project_id_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY plannedtask
    ADD CONSTRAINT plannedtask_project_id_project_id_fkey FOREIGN KEY (plannedtask_project_id) REFERENCES project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: plannedtask_tasktype_id_tasktype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY plannedtask
    ADD CONSTRAINT plannedtask_tasktype_id_tasktype_id_fkey FOREIGN KEY (plannedtask_tasktype_id) REFERENCES tasktype(tasktype_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: plannedtask_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY plannedtask
    ADD CONSTRAINT plannedtask_user_id_userobm_id_fkey FOREIGN KEY (plannedtask_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: plannedtask_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY plannedtask
    ADD CONSTRAINT plannedtask_usercreate_userobm_id_fkey FOREIGN KEY (plannedtask_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: plannedtask_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY plannedtask
    ADD CONSTRAINT plannedtask_userupdate_userobm_id_fkey FOREIGN KEY (plannedtask_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: profileentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY profileentity
    ADD CONSTRAINT profileentity_entity_id_entity_id_fkey FOREIGN KEY (profileentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: profileentity_profile_id_profile_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY profileentity
    ADD CONSTRAINT profileentity_profile_id_profile_id_fkey FOREIGN KEY (profileentity_profile_id) REFERENCES profile(profile_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: profilemodule_profile_id_profile_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY profilemodule
    ADD CONSTRAINT profilemodule_profile_id_profile_id_fkey FOREIGN KEY (profilemodule_profile_id) REFERENCES profile(profile_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: profileproperty_profile_id_profile_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY profileproperty
    ADD CONSTRAINT profileproperty_profile_id_profile_id_fkey FOREIGN KEY (profileproperty_profile_id) REFERENCES profile(profile_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: profilesection_profile_id_profile_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY profilesection
    ADD CONSTRAINT profilesection_profile_id_profile_id_fkey FOREIGN KEY (profilesection_profile_id) REFERENCES profile(profile_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: project_company_id_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_company_id_company_id_fkey FOREIGN KEY (project_company_id) REFERENCES company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: project_deal_id_deal_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_deal_id_deal_id_fkey FOREIGN KEY (project_deal_id) REFERENCES deal(deal_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: project_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_domain_id_domain_id_fkey FOREIGN KEY (project_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: project_tasktype_id_tasktype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_tasktype_id_tasktype_id_fkey FOREIGN KEY (project_tasktype_id) REFERENCES tasktype(tasktype_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: project_type_id_dealtype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_type_id_dealtype_id_fkey FOREIGN KEY (project_type_id) REFERENCES dealtype(dealtype_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: project_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_usercreate_userobm_id_fkey FOREIGN KEY (project_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: project_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_userupdate_userobm_id_fkey FOREIGN KEY (project_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: projectclosing_project_id_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY projectclosing
    ADD CONSTRAINT projectclosing_project_id_project_id_fkey FOREIGN KEY (projectclosing_project_id) REFERENCES project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: projectclosing_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY projectclosing
    ADD CONSTRAINT projectclosing_usercreate_userobm_id_fkey FOREIGN KEY (projectclosing_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: projectclosing_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY projectclosing
    ADD CONSTRAINT projectclosing_userupdate_userobm_id_fkey FOREIGN KEY (projectclosing_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: projectcv_cv_id_cv_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY projectcv
    ADD CONSTRAINT projectcv_cv_id_cv_id_fkey FOREIGN KEY (projectcv_cv_id) REFERENCES cv(cv_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: projectcv_project_id_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY projectcv
    ADD CONSTRAINT projectcv_project_id_project_id_fkey FOREIGN KEY (projectcv_project_id) REFERENCES project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: projectentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY projectentity
    ADD CONSTRAINT projectentity_entity_id_entity_id_fkey FOREIGN KEY (projectentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: projectentity_project_id_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY projectentity
    ADD CONSTRAINT projectentity_project_id_project_id_fkey FOREIGN KEY (projectentity_project_id) REFERENCES project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: projectreftask_tasktype_id_tasktype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY projectreftask
    ADD CONSTRAINT projectreftask_tasktype_id_tasktype_id_fkey FOREIGN KEY (projectreftask_tasktype_id) REFERENCES tasktype(tasktype_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: projectreftask_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY projectreftask
    ADD CONSTRAINT projectreftask_usercreate_userobm_id_fkey FOREIGN KEY (projectreftask_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: projectreftask_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY projectreftask
    ADD CONSTRAINT projectreftask_userupdate_userobm_id_fkey FOREIGN KEY (projectreftask_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: projecttask_parenttask_id_projecttask_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY projecttask
    ADD CONSTRAINT projecttask_parenttask_id_projecttask_id_fkey FOREIGN KEY (projecttask_parenttask_id) REFERENCES projecttask(projecttask_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: projecttask_project_id_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY projecttask
    ADD CONSTRAINT projecttask_project_id_project_id_fkey FOREIGN KEY (projecttask_project_id) REFERENCES project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: projecttask_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY projecttask
    ADD CONSTRAINT projecttask_usercreate_userobm_id_fkey FOREIGN KEY (projecttask_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: projecttask_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY projecttask
    ADD CONSTRAINT projecttask_userupdate_userobm_id_fkey FOREIGN KEY (projecttask_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: projectuser_project_id_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY projectuser
    ADD CONSTRAINT projectuser_project_id_project_id_fkey FOREIGN KEY (projectuser_project_id) REFERENCES project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: projectuser_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY projectuser
    ADD CONSTRAINT projectuser_user_id_userobm_id_fkey FOREIGN KEY (projectuser_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: projectuser_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY projectuser
    ADD CONSTRAINT projectuser_usercreate_userobm_id_fkey FOREIGN KEY (projectuser_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: projectuser_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY projectuser
    ADD CONSTRAINT projectuser_userupdate_userobm_id_fkey FOREIGN KEY (projectuser_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: publication_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY publication
    ADD CONSTRAINT publication_domain_id_domain_id_fkey FOREIGN KEY (publication_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: publication_type_id_publicationtype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY publication
    ADD CONSTRAINT publication_type_id_publicationtype_id_fkey FOREIGN KEY (publication_type_id) REFERENCES publicationtype(publicationtype_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: publication_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY publication
    ADD CONSTRAINT publication_usercreate_userobm_id_fkey FOREIGN KEY (publication_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: publication_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY publication
    ADD CONSTRAINT publication_userupdate_userobm_id_fkey FOREIGN KEY (publication_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: publicationentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY publicationentity
    ADD CONSTRAINT publicationentity_entity_id_entity_id_fkey FOREIGN KEY (publicationentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: publicationentity_publication_id_publication_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY publicationentity
    ADD CONSTRAINT publicationentity_publication_id_publication_id_fkey FOREIGN KEY (publicationentity_publication_id) REFERENCES publication(publication_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: publicationtype_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY publicationtype
    ADD CONSTRAINT publicationtype_domain_id_domain_id_fkey FOREIGN KEY (publicationtype_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: publicationtype_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY publicationtype
    ADD CONSTRAINT publicationtype_usercreate_userobm_id_fkey FOREIGN KEY (publicationtype_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: publicationtype_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY publicationtype
    ADD CONSTRAINT publicationtype_userupdate_userobm_id_fkey FOREIGN KEY (publicationtype_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: region_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY region
    ADD CONSTRAINT region_domain_id_domain_id_fkey FOREIGN KEY (region_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: region_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY region
    ADD CONSTRAINT region_usercreate_userobm_id_fkey FOREIGN KEY (region_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: region_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY region
    ADD CONSTRAINT region_userupdate_userobm_id_fkey FOREIGN KEY (region_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: resource_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY resource
    ADD CONSTRAINT resource_domain_id_domain_id_fkey FOREIGN KEY (resource_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: resource_rtype_id_resourcetype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY resource
    ADD CONSTRAINT resource_rtype_id_resourcetype_id_fkey FOREIGN KEY (resource_rtype_id) REFERENCES resourcetype(resourcetype_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: resource_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY resource
    ADD CONSTRAINT resource_usercreate_userobm_id_fkey FOREIGN KEY (resource_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: resource_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY resource
    ADD CONSTRAINT resource_userupdate_userobm_id_fkey FOREIGN KEY (resource_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: resourceentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY resourceentity
    ADD CONSTRAINT resourceentity_entity_id_entity_id_fkey FOREIGN KEY (resourceentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: resourceentity_resource_id_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY resourceentity
    ADD CONSTRAINT resourceentity_resource_id_resource_id_fkey FOREIGN KEY (resourceentity_resource_id) REFERENCES resource(resource_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: resourcegroup_resource_id_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY resourcegroup
    ADD CONSTRAINT resourcegroup_resource_id_resource_id_fkey FOREIGN KEY (resourcegroup_resource_id) REFERENCES resource(resource_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: resourcegroup_rgroup_id_rgroup_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY resourcegroup
    ADD CONSTRAINT resourcegroup_rgroup_id_rgroup_id_fkey FOREIGN KEY (resourcegroup_rgroup_id) REFERENCES rgroup(rgroup_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: resourcegroupentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY resourcegroupentity
    ADD CONSTRAINT resourcegroupentity_entity_id_entity_id_fkey FOREIGN KEY (resourcegroupentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: resourcegroupentity_resourcegroup_id_resourcegroup_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY resourcegroupentity
    ADD CONSTRAINT resourcegroupentity_resourcegroup_id_resourcegroup_id_fkey FOREIGN KEY (resourcegroupentity_resourcegroup_id) REFERENCES rgroup(rgroup_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: resourceitem_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY resourceitem
    ADD CONSTRAINT resourceitem_domain_id_domain_id_fkey FOREIGN KEY (resourceitem_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: resourceitem_resourcetype_id_resourcetype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY resourceitem
    ADD CONSTRAINT resourceitem_resourcetype_id_resourcetype_id_fkey FOREIGN KEY (resourceitem_resourcetype_id) REFERENCES resourcetype(resourcetype_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: resourcetype_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY resourcetype
    ADD CONSTRAINT resourcetype_domain_id_domain_id_fkey FOREIGN KEY (resourcetype_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: rgroup_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY rgroup
    ADD CONSTRAINT rgroup_domain_id_domain_id_fkey FOREIGN KEY (rgroup_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: rgroup_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY rgroup
    ADD CONSTRAINT rgroup_usercreate_userobm_id_fkey FOREIGN KEY (rgroup_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: rgroup_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY rgroup
    ADD CONSTRAINT rgroup_userupdate_userobm_id_fkey FOREIGN KEY (rgroup_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: service_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY service
    ADD CONSTRAINT service_entity_id_entity_id_fkey FOREIGN KEY (service_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: serviceproperty_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY serviceproperty
    ADD CONSTRAINT serviceproperty_entity_id_entity_id_fkey FOREIGN KEY (serviceproperty_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ssoticket_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY ssoticket
    ADD CONSTRAINT ssoticket_user_id_userobm_id_fkey FOREIGN KEY (ssoticket_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: subscription_contact_id_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY subscription
    ADD CONSTRAINT subscription_contact_id_contact_id_fkey FOREIGN KEY (subscription_contact_id) REFERENCES contact(contact_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: subscription_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY subscription
    ADD CONSTRAINT subscription_domain_id_domain_id_fkey FOREIGN KEY (subscription_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: subscription_publication_id_publication_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY subscription
    ADD CONSTRAINT subscription_publication_id_publication_id_fkey FOREIGN KEY (subscription_publication_id) REFERENCES publication(publication_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: subscription_reception_id_subscriptionreception_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY subscription
    ADD CONSTRAINT subscription_reception_id_subscriptionreception_id_fkey FOREIGN KEY (subscription_reception_id) REFERENCES subscriptionreception(subscriptionreception_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: subscription_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY subscription
    ADD CONSTRAINT subscription_usercreate_userobm_id_fkey FOREIGN KEY (subscription_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: subscription_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY subscription
    ADD CONSTRAINT subscription_userupdate_userobm_id_fkey FOREIGN KEY (subscription_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: subscriptionentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY subscriptionentity
    ADD CONSTRAINT subscriptionentity_entity_id_entity_id_fkey FOREIGN KEY (subscriptionentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: subscriptionentity_subscription_id_subscription_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY subscriptionentity
    ADD CONSTRAINT subscriptionentity_subscription_id_subscription_id_fkey FOREIGN KEY (subscriptionentity_subscription_id) REFERENCES subscription(subscription_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: subscriptionreception_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY subscriptionreception
    ADD CONSTRAINT subscriptionreception_domain_id_domain_id_fkey FOREIGN KEY (subscriptionreception_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: subscriptionreception_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY subscriptionreception
    ADD CONSTRAINT subscriptionreception_usercreate_userobm_id_fkey FOREIGN KEY (subscriptionreception_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: subscriptionreception_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY subscriptionreception
    ADD CONSTRAINT subscriptionreception_userupdate_userobm_id_fkey FOREIGN KEY (subscriptionreception_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: syncedaddressbook_addressbook_id_addressbook_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY syncedaddressbook
    ADD CONSTRAINT syncedaddressbook_addressbook_id_addressbook_id_fkey FOREIGN KEY (addressbook_id) REFERENCES addressbook(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: syncedaddressbook_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY syncedaddressbook
    ADD CONSTRAINT syncedaddressbook_user_id_userobm_id_fkey FOREIGN KEY (user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: taskevent_event_id_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY taskevent
    ADD CONSTRAINT taskevent_event_id_event_id_fkey FOREIGN KEY (taskevent_event_id) REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: taskevent_task_id_projecttask_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY taskevent
    ADD CONSTRAINT taskevent_task_id_projecttask_id_fkey FOREIGN KEY (taskevent_task_id) REFERENCES projecttask(projecttask_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: tasktype_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY tasktype
    ADD CONSTRAINT tasktype_domain_id_domain_id_fkey FOREIGN KEY (tasktype_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: tasktype_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY tasktype
    ADD CONSTRAINT tasktype_usercreate_userobm_id_fkey FOREIGN KEY (tasktype_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: tasktype_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY tasktype
    ADD CONSTRAINT tasktype_userupdate_userobm_id_fkey FOREIGN KEY (tasktype_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: tasktypegroup_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY tasktypegroup
    ADD CONSTRAINT tasktypegroup_domain_id_domain_id_fkey FOREIGN KEY (tasktypegroup_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: tasktypegroup_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY tasktypegroup
    ADD CONSTRAINT tasktypegroup_usercreate_userobm_id_fkey FOREIGN KEY (tasktypegroup_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: tasktypegroup_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY tasktypegroup
    ADD CONSTRAINT tasktypegroup_userupdate_userobm_id_fkey FOREIGN KEY (tasktypegroup_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: timetask_projecttask_id_projecttask_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY timetask
    ADD CONSTRAINT timetask_projecttask_id_projecttask_id_fkey FOREIGN KEY (timetask_projecttask_id) REFERENCES projecttask(projecttask_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: timetask_tasktype_id_tasktype_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY timetask
    ADD CONSTRAINT timetask_tasktype_id_tasktype_id_fkey FOREIGN KEY (timetask_tasktype_id) REFERENCES tasktype(tasktype_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: timetask_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY timetask
    ADD CONSTRAINT timetask_user_id_userobm_id_fkey FOREIGN KEY (timetask_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: timetask_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY timetask
    ADD CONSTRAINT timetask_usercreate_userobm_id_fkey FOREIGN KEY (timetask_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: timetask_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY timetask
    ADD CONSTRAINT timetask_userupdate_userobm_id_fkey FOREIGN KEY (timetask_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: updated_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY updated
    ADD CONSTRAINT updated_domain_id_domain_id_fkey FOREIGN KEY (updated_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: updated_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY updated
    ADD CONSTRAINT updated_user_id_userobm_id_fkey FOREIGN KEY (updated_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: updatedlinks_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY updatedlinks
    ADD CONSTRAINT updatedlinks_domain_id_domain_id_fkey FOREIGN KEY (updatedlinks_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: updatedlinks_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY updatedlinks
    ADD CONSTRAINT updatedlinks_user_id_userobm_id_fkey FOREIGN KEY (updatedlinks_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: user_id_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY calendarcolor
    ADD CONSTRAINT user_id_user_id_fkey FOREIGN KEY (user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: userentity_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY userentity
    ADD CONSTRAINT userentity_entity_id_entity_id_fkey FOREIGN KEY (userentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: userentity_user_id_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY userentity
    ADD CONSTRAINT userentity_user_id_user_id_fkey FOREIGN KEY (userentity_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: userobm_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY userobm
    ADD CONSTRAINT userobm_domain_id_domain_id_fkey FOREIGN KEY (userobm_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: userobm_host_id_host_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY userobm
    ADD CONSTRAINT userobm_host_id_host_id_fkey FOREIGN KEY (userobm_host_id) REFERENCES host(host_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: userobm_mail_server_id_mailserver_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY userobm
    ADD CONSTRAINT userobm_mail_server_id_mailserver_id_fkey FOREIGN KEY (userobm_mail_server_id) REFERENCES host(host_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: userobm_photo_id_document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY userobm
    ADD CONSTRAINT userobm_photo_id_document_id_fkey FOREIGN KEY (userobm_photo_id) REFERENCES document(document_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: userobm_sessionlog_userobm_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY userobm_sessionlog
    ADD CONSTRAINT userobm_sessionlog_userobm_id_userobm_id_fkey FOREIGN KEY (userobm_sessionlog_userobm_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: userobm_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY userobm
    ADD CONSTRAINT userobm_usercreate_userobm_id_fkey FOREIGN KEY (userobm_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: userobm_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY userobm
    ADD CONSTRAINT userobm_userupdate_userobm_id_fkey FOREIGN KEY (userobm_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: userobmgroup_group_id_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY userobmgroup
    ADD CONSTRAINT userobmgroup_group_id_group_id_fkey FOREIGN KEY (userobmgroup_group_id) REFERENCES ugroup(group_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: userobmgroup_userobm_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY userobmgroup
    ADD CONSTRAINT userobmgroup_userobm_id_userobm_id_fkey FOREIGN KEY (userobmgroup_userobm_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: userobmpref_user_id_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY userobmpref
    ADD CONSTRAINT userobmpref_user_id_userobm_id_fkey FOREIGN KEY (userobmpref_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: userpattern_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY userpattern
    ADD CONSTRAINT userpattern_domain_id_domain_id_fkey FOREIGN KEY (domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: userpattern_property_userpattern_id_userpattern_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY userpattern_property
    ADD CONSTRAINT userpattern_property_userpattern_id_userpattern_id_fkey FOREIGN KEY (userpattern_id) REFERENCES userpattern(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: userpattern_usercreate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY userpattern
    ADD CONSTRAINT userpattern_usercreate_userobm_id_fkey FOREIGN KEY (usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: userpattern_userupdate_userobm_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY userpattern
    ADD CONSTRAINT userpattern_userupdate_userobm_id_fkey FOREIGN KEY (userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: website_entity_id_entity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: obm
--

ALTER TABLE ONLY website
    ADD CONSTRAINT website_entity_id_entity_id_fkey FOREIGN KEY (website_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

INSERT INTO ObmInfo SELECT 'product_id', LPAD(MD5(FLOOR(EXTRACT(EPOCH FROM TIMESTAMP 'NOW()')*RANDOM())::text), 24);