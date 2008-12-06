CREATE OR REPLACE FUNCTION on_obmsession_change() RETURNS trigger AS '
BEGIN
new.obmsession_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER obmsession_changed BEFORE UPDATE ON obmsession FOR EACH ROW EXECUTE PROCEDURE on_obmsession_change();

UPDATE activeuserobm SET activeuserobm_timecreate=NOW() WHERE activeuserobm_timecreate IS NULL;
ALTER TABLE activeuserobm ALTER COLUMN activeuserobm_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_activeuserobm_change() RETURNS trigger AS '
BEGIN
new.activeuserobm_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_activeuserobm_create() RETURNS trigger AS '
BEGIN
new.activeuserobm_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER activeuserobm_created BEFORE INSERT ON activeuserobm FOR EACH ROW EXECUTE PROCEDURE on_activeuserobm_create();
CREATE TRIGGER activeuserobm_changed BEFORE UPDATE ON activeuserobm FOR EACH ROW EXECUTE PROCEDURE on_activeuserobm_change();

UPDATE userobm_sessionlog SET userobm_sessionlog_timecreate=NOW() WHERE userobm_sessionlog_timecreate IS NULL;
ALTER TABLE userobm_sessionlog ALTER COLUMN userobm_sessionlog_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_userobm_sessionlog_change() RETURNS trigger AS '
BEGIN
new.userobm_sessionlog_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_userobm_sessionlog_create() RETURNS trigger AS '
BEGIN
new.userobm_sessionlog_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER userobm_sessionlog_created BEFORE INSERT ON userobm_sessionlog FOR EACH ROW EXECUTE PROCEDURE on_userobm_sessionlog_create();
CREATE TRIGGER userobm_sessionlog_changed BEFORE UPDATE ON userobm_sessionlog FOR EACH ROW EXECUTE PROCEDURE on_userobm_sessionlog_change();

UPDATE userobm SET userobm_timecreate=NOW() WHERE userobm_timecreate IS NULL;
ALTER TABLE userobm ALTER COLUMN userobm_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_userobm_change() RETURNS trigger AS '
BEGIN
new.userobm_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_userobm_create() RETURNS trigger AS '
BEGIN
new.userobm_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER userobm_created BEFORE INSERT ON userobm FOR EACH ROW EXECUTE PROCEDURE on_userobm_create();
CREATE TRIGGER userobm_changed BEFORE UPDATE ON userobm FOR EACH ROW EXECUTE PROCEDURE on_userobm_change();

UPDATE category SET category_timecreate=NOW() WHERE category_timecreate IS NULL;
ALTER TABLE category ALTER COLUMN category_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_category_change() RETURNS trigger AS '
BEGIN
new.category_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_category_create() RETURNS trigger AS '
BEGIN
new.category_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER category_created BEFORE INSERT ON category FOR EACH ROW EXECUTE PROCEDURE on_category_create();
CREATE TRIGGER category_changed BEFORE UPDATE ON category FOR EACH ROW EXECUTE PROCEDURE on_category_change();

UPDATE datasource SET datasource_timecreate=NOW() WHERE datasource_timecreate IS NULL;
ALTER TABLE datasource ALTER COLUMN datasource_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_datasource_change() RETURNS trigger AS '
BEGIN
new.datasource_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_datasource_create() RETURNS trigger AS '
BEGIN
new.datasource_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER datasource_created BEFORE INSERT ON datasource FOR EACH ROW EXECUTE PROCEDURE on_datasource_create();
CREATE TRIGGER datasource_changed BEFORE UPDATE ON datasource FOR EACH ROW EXECUTE PROCEDURE on_datasource_change();

UPDATE country SET country_timecreate=NOW() WHERE country_timecreate IS NULL;
ALTER TABLE country ALTER COLUMN country_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_country_change() RETURNS trigger AS '
BEGIN
new.country_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_country_create() RETURNS trigger AS '
BEGIN
new.country_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER country_created BEFORE INSERT ON country FOR EACH ROW EXECUTE PROCEDURE on_country_create();
CREATE TRIGGER country_changed BEFORE UPDATE ON country FOR EACH ROW EXECUTE PROCEDURE on_country_change();

UPDATE region SET region_timecreate=NOW() WHERE region_timecreate IS NULL;
ALTER TABLE region ALTER COLUMN region_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_region_change() RETURNS trigger AS '
BEGIN
new.region_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_region_create() RETURNS trigger AS '
BEGIN
new.region_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER region_created BEFORE INSERT ON region FOR EACH ROW EXECUTE PROCEDURE on_region_create();
CREATE TRIGGER region_changed BEFORE UPDATE ON region FOR EACH ROW EXECUTE PROCEDURE on_region_change();

UPDATE companytype SET companytype_timecreate=NOW() WHERE companytype_timecreate IS NULL;
ALTER TABLE companytype ALTER COLUMN companytype_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_companytype_change() RETURNS trigger AS '
BEGIN
new.companytype_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_companytype_create() RETURNS trigger AS '
BEGIN
new.companytype_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER companytype_created BEFORE INSERT ON companytype FOR EACH ROW EXECUTE PROCEDURE on_companytype_create();
CREATE TRIGGER companytype_changed BEFORE UPDATE ON companytype FOR EACH ROW EXECUTE PROCEDURE on_companytype_change();

UPDATE companyactivity SET companyactivity_timecreate=NOW() WHERE companyactivity_timecreate IS NULL;
ALTER TABLE companyactivity ALTER COLUMN companyactivity_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_companyactivity_change() RETURNS trigger AS '
BEGIN
new.companyactivity_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_companyactivity_create() RETURNS trigger AS '
BEGIN
new.companyactivity_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER companyactivity_created BEFORE INSERT ON companyactivity FOR EACH ROW EXECUTE PROCEDURE on_companyactivity_create();
CREATE TRIGGER companyactivity_changed BEFORE UPDATE ON companyactivity FOR EACH ROW EXECUTE PROCEDURE on_companyactivity_change();

UPDATE companynafcode SET companynafcode_timecreate=NOW() WHERE companynafcode_timecreate IS NULL;
ALTER TABLE companynafcode ALTER COLUMN companynafcode_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_companynafcode_change() RETURNS trigger AS '
BEGIN
new.companynafcode_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_companynafcode_create() RETURNS trigger AS '
BEGIN
new.companynafcode_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER companynafcode_created BEFORE INSERT ON companynafcode FOR EACH ROW EXECUTE PROCEDURE on_companynafcode_create();
CREATE TRIGGER companynafcode_changed BEFORE UPDATE ON companynafcode FOR EACH ROW EXECUTE PROCEDURE on_companynafcode_change();

UPDATE company SET company_timecreate=NOW() WHERE company_timecreate IS NULL;
ALTER TABLE company ALTER COLUMN company_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_company_change() RETURNS trigger AS '
BEGIN
new.company_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_company_create() RETURNS trigger AS '
BEGIN
new.company_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER company_created BEFORE INSERT ON company FOR EACH ROW EXECUTE PROCEDURE on_company_create();
CREATE TRIGGER company_changed BEFORE UPDATE ON company FOR EACH ROW EXECUTE PROCEDURE on_company_change();

UPDATE contact SET contact_timecreate=NOW() WHERE contact_timecreate IS NULL;
ALTER TABLE contact ALTER COLUMN contact_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_contact_change() RETURNS trigger AS '
BEGIN
new.contact_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_contact_create() RETURNS trigger AS '
BEGIN
new.contact_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER contact_created BEFORE INSERT ON contact FOR EACH ROW EXECUTE PROCEDURE on_contact_create();
CREATE TRIGGER contact_changed BEFORE UPDATE ON contact FOR EACH ROW EXECUTE PROCEDURE on_contact_change();

UPDATE kind SET kind_timecreate=NOW() WHERE kind_timecreate IS NULL;
ALTER TABLE kind ALTER COLUMN kind_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_kind_change() RETURNS trigger AS '
BEGIN
new.kind_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_kind_create() RETURNS trigger AS '
BEGIN
new.kind_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER kind_created BEFORE INSERT ON kind FOR EACH ROW EXECUTE PROCEDURE on_kind_create();
CREATE TRIGGER kind_changed BEFORE UPDATE ON kind FOR EACH ROW EXECUTE PROCEDURE on_kind_change();

UPDATE contactfunction SET contactfunction_timecreate=NOW() WHERE contactfunction_timecreate IS NULL;
ALTER TABLE contactfunction ALTER COLUMN contactfunction_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_contactfunction_change() RETURNS trigger AS '
BEGIN
new.contactfunction_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_contactfunction_create() RETURNS trigger AS '
BEGIN
new.contactfunction_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER contactfunction_created BEFORE INSERT ON contactfunction FOR EACH ROW EXECUTE PROCEDURE on_contactfunction_create();
CREATE TRIGGER contactfunction_changed BEFORE UPDATE ON contactfunction FOR EACH ROW EXECUTE PROCEDURE on_contactfunction_change();

UPDATE leadsource SET leadsource_timecreate=NOW() WHERE leadsource_timecreate IS NULL;
ALTER TABLE leadsource ALTER COLUMN leadsource_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_leadsource_change() RETURNS trigger AS '
BEGIN
new.leadsource_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_leadsource_create() RETURNS trigger AS '
BEGIN
new.leadsource_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER leadsource_created BEFORE INSERT ON leadsource FOR EACH ROW EXECUTE PROCEDURE on_leadsource_create();
CREATE TRIGGER leadsource_changed BEFORE UPDATE ON leadsource FOR EACH ROW EXECUTE PROCEDURE on_leadsource_change();

UPDATE leadstatus SET leadstatus_timecreate=NOW() WHERE leadstatus_timecreate IS NULL;
ALTER TABLE leadstatus ALTER COLUMN leadstatus_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_leadstatus_change() RETURNS trigger AS '
BEGIN
new.leadstatus_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_leadstatus_create() RETURNS trigger AS '
BEGIN
new.leadstatus_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER leadstatus_created BEFORE INSERT ON leadstatus FOR EACH ROW EXECUTE PROCEDURE on_leadstatus_create();
CREATE TRIGGER leadstatus_changed BEFORE UPDATE ON leadstatus FOR EACH ROW EXECUTE PROCEDURE on_leadstatus_change();

UPDATE lead SET lead_timecreate=NOW() WHERE lead_timecreate IS NULL;
ALTER TABLE lead ALTER COLUMN lead_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_lead_change() RETURNS trigger AS '
BEGIN
new.lead_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_lead_create() RETURNS trigger AS '
BEGIN
new.lead_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER lead_created BEFORE INSERT ON lead FOR EACH ROW EXECUTE PROCEDURE on_lead_create();
CREATE TRIGGER lead_changed BEFORE UPDATE ON lead FOR EACH ROW EXECUTE PROCEDURE on_lead_change();

UPDATE parentdeal SET parentdeal_timecreate=NOW() WHERE parentdeal_timecreate IS NULL;
ALTER TABLE parentdeal ALTER COLUMN parentdeal_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_parentdeal_change() RETURNS trigger AS '
BEGIN
new.parentdeal_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_parentdeal_create() RETURNS trigger AS '
BEGIN
new.parentdeal_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER parentdeal_created BEFORE INSERT ON parentdeal FOR EACH ROW EXECUTE PROCEDURE on_parentdeal_create();
CREATE TRIGGER parentdeal_changed BEFORE UPDATE ON parentdeal FOR EACH ROW EXECUTE PROCEDURE on_parentdeal_change();

UPDATE deal SET deal_timecreate=NOW() WHERE deal_timecreate IS NULL;
ALTER TABLE deal ALTER COLUMN deal_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_deal_change() RETURNS trigger AS '
BEGIN
new.deal_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_deal_create() RETURNS trigger AS '
BEGIN
new.deal_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER deal_created BEFORE INSERT ON deal FOR EACH ROW EXECUTE PROCEDURE on_deal_create();
CREATE TRIGGER deal_changed BEFORE UPDATE ON deal FOR EACH ROW EXECUTE PROCEDURE on_deal_change();

UPDATE dealstatus SET dealstatus_timecreate=NOW() WHERE dealstatus_timecreate IS NULL;
ALTER TABLE dealstatus ALTER COLUMN dealstatus_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_dealstatus_change() RETURNS trigger AS '
BEGIN
new.dealstatus_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_dealstatus_create() RETURNS trigger AS '
BEGIN
new.dealstatus_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER dealstatus_created BEFORE INSERT ON dealstatus FOR EACH ROW EXECUTE PROCEDURE on_dealstatus_create();
CREATE TRIGGER dealstatus_changed BEFORE UPDATE ON dealstatus FOR EACH ROW EXECUTE PROCEDURE on_dealstatus_change();

UPDATE dealtype SET dealtype_timecreate=NOW() WHERE dealtype_timecreate IS NULL;
ALTER TABLE dealtype ALTER COLUMN dealtype_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_dealtype_change() RETURNS trigger AS '
BEGIN
new.dealtype_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_dealtype_create() RETURNS trigger AS '
BEGIN
new.dealtype_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER dealtype_created BEFORE INSERT ON dealtype FOR EACH ROW EXECUTE PROCEDURE on_dealtype_create();
CREATE TRIGGER dealtype_changed BEFORE UPDATE ON dealtype FOR EACH ROW EXECUTE PROCEDURE on_dealtype_change();

UPDATE dealcompanyrole SET dealcompanyrole_timecreate=NOW() WHERE dealcompanyrole_timecreate IS NULL;
ALTER TABLE dealcompanyrole ALTER COLUMN dealcompanyrole_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_dealcompanyrole_change() RETURNS trigger AS '
BEGIN
new.dealcompanyrole_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_dealcompanyrole_create() RETURNS trigger AS '
BEGIN
new.dealcompanyrole_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER dealcompanyrole_created BEFORE INSERT ON dealcompanyrole FOR EACH ROW EXECUTE PROCEDURE on_dealcompanyrole_create();
CREATE TRIGGER dealcompanyrole_changed BEFORE UPDATE ON dealcompanyrole FOR EACH ROW EXECUTE PROCEDURE on_dealcompanyrole_change();

UPDATE dealcompany SET dealcompany_timecreate=NOW() WHERE dealcompany_timecreate IS NULL;
ALTER TABLE dealcompany ALTER COLUMN dealcompany_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_dealcompany_change() RETURNS trigger AS '
BEGIN
new.dealcompany_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_dealcompany_create() RETURNS trigger AS '
BEGIN
new.dealcompany_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER dealcompany_created BEFORE INSERT ON dealcompany FOR EACH ROW EXECUTE PROCEDURE on_dealcompany_create();
CREATE TRIGGER dealcompany_changed BEFORE UPDATE ON dealcompany FOR EACH ROW EXECUTE PROCEDURE on_dealcompany_change();

UPDATE list SET list_timecreate=NOW() WHERE list_timecreate IS NULL;
ALTER TABLE list ALTER COLUMN list_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_list_change() RETURNS trigger AS '
BEGIN
new.list_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_list_create() RETURNS trigger AS '
BEGIN
new.list_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER list_created BEFORE INSERT ON list FOR EACH ROW EXECUTE PROCEDURE on_list_create();
CREATE TRIGGER list_changed BEFORE UPDATE ON list FOR EACH ROW EXECUTE PROCEDURE on_list_change();

UPDATE event SET event_timecreate=NOW() WHERE event_timecreate IS NULL;
ALTER TABLE event ALTER COLUMN event_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_event_change() RETURNS trigger AS '
BEGIN
new.event_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_event_create() RETURNS trigger AS '
BEGIN
new.event_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER event_created BEFORE INSERT ON event FOR EACH ROW EXECUTE PROCEDURE on_event_create();
CREATE TRIGGER event_changed BEFORE UPDATE ON event FOR EACH ROW EXECUTE PROCEDURE on_event_change();

UPDATE eventlink SET eventlink_timecreate=NOW() WHERE eventlink_timecreate IS NULL;
ALTER TABLE eventlink ALTER COLUMN eventlink_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_eventlink_change() RETURNS trigger AS '
BEGIN
new.eventlink_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_eventlink_create() RETURNS trigger AS '
BEGIN
new.eventlink_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER eventlink_created BEFORE INSERT ON eventlink FOR EACH ROW EXECUTE PROCEDURE on_eventlink_create();
CREATE TRIGGER eventlink_changed BEFORE UPDATE ON eventlink FOR EACH ROW EXECUTE PROCEDURE on_eventlink_change();

UPDATE eventexception SET eventexception_timecreate=NOW() WHERE eventexception_timecreate IS NULL;
ALTER TABLE eventexception ALTER COLUMN eventexception_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_eventexception_change() RETURNS trigger AS '
BEGIN
new.eventexception_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_eventexception_create() RETURNS trigger AS '
BEGIN
new.eventexception_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER eventexception_created BEFORE INSERT ON eventexception FOR EACH ROW EXECUTE PROCEDURE on_eventexception_create();
CREATE TRIGGER eventexception_changed BEFORE UPDATE ON eventexception FOR EACH ROW EXECUTE PROCEDURE on_eventexception_change();

UPDATE eventalert SET eventalert_timecreate=NOW() WHERE eventalert_timecreate IS NULL;
ALTER TABLE eventalert ALTER COLUMN eventalert_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_eventalert_change() RETURNS trigger AS '
BEGIN
new.eventalert_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_eventalert_create() RETURNS trigger AS '
BEGIN
new.eventalert_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER eventalert_created BEFORE INSERT ON eventalert FOR EACH ROW EXECUTE PROCEDURE on_eventalert_create();
CREATE TRIGGER eventalert_changed BEFORE UPDATE ON eventalert FOR EACH ROW EXECUTE PROCEDURE on_eventalert_change();

UPDATE eventcategory1 SET eventcategory1_timecreate=NOW() WHERE eventcategory1_timecreate IS NULL;
ALTER TABLE eventcategory1 ALTER COLUMN eventcategory1_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_eventcategory1_change() RETURNS trigger AS '
BEGIN
new.eventcategory1_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_eventcategory1_create() RETURNS trigger AS '
BEGIN
new.eventcategory1_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER eventcategory1_created BEFORE INSERT ON eventcategory1 FOR EACH ROW EXECUTE PROCEDURE on_eventcategory1_create();
CREATE TRIGGER eventcategory1_changed BEFORE UPDATE ON eventcategory1 FOR EACH ROW EXECUTE PROCEDURE on_eventcategory1_change();

UPDATE publication SET publication_timecreate=NOW() WHERE publication_timecreate IS NULL;
ALTER TABLE publication ALTER COLUMN publication_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_publication_change() RETURNS trigger AS '
BEGIN
new.publication_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_publication_create() RETURNS trigger AS '
BEGIN
new.publication_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER publication_created BEFORE INSERT ON publication FOR EACH ROW EXECUTE PROCEDURE on_publication_create();
CREATE TRIGGER publication_changed BEFORE UPDATE ON publication FOR EACH ROW EXECUTE PROCEDURE on_publication_change();

UPDATE publicationtype SET publicationtype_timecreate=NOW() WHERE publicationtype_timecreate IS NULL;
ALTER TABLE publicationtype ALTER COLUMN publicationtype_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_publicationtype_change() RETURNS trigger AS '
BEGIN
new.publicationtype_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_publicationtype_create() RETURNS trigger AS '
BEGIN
new.publicationtype_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER publicationtype_created BEFORE INSERT ON publicationtype FOR EACH ROW EXECUTE PROCEDURE on_publicationtype_create();
CREATE TRIGGER publicationtype_changed BEFORE UPDATE ON publicationtype FOR EACH ROW EXECUTE PROCEDURE on_publicationtype_change();

UPDATE subscription SET subscription_timecreate=NOW() WHERE subscription_timecreate IS NULL;
ALTER TABLE subscription ALTER COLUMN subscription_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_subscription_change() RETURNS trigger AS '
BEGIN
new.subscription_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_subscription_create() RETURNS trigger AS '
BEGIN
new.subscription_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER subscription_created BEFORE INSERT ON subscription FOR EACH ROW EXECUTE PROCEDURE on_subscription_create();
CREATE TRIGGER subscription_changed BEFORE UPDATE ON subscription FOR EACH ROW EXECUTE PROCEDURE on_subscription_change();

UPDATE subscriptionreception SET subscriptionreception_timecreate=NOW() WHERE subscriptionreception_timecreate IS NULL;
ALTER TABLE subscriptionreception ALTER COLUMN subscriptionreception_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_subscriptionreception_change() RETURNS trigger AS '
BEGIN
new.subscriptionreception_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_subscriptionreception_create() RETURNS trigger AS '
BEGIN
new.subscriptionreception_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER subscriptionreception_created BEFORE INSERT ON subscriptionreception FOR EACH ROW EXECUTE PROCEDURE on_subscriptionreception_create();
CREATE TRIGGER subscriptionreception_changed BEFORE UPDATE ON subscriptionreception FOR EACH ROW EXECUTE PROCEDURE on_subscriptionreception_change();

UPDATE document SET document_timecreate=NOW() WHERE document_timecreate IS NULL;
ALTER TABLE document ALTER COLUMN document_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_document_change() RETURNS trigger AS '
BEGIN
new.document_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_document_create() RETURNS trigger AS '
BEGIN
new.document_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER document_created BEFORE INSERT ON document FOR EACH ROW EXECUTE PROCEDURE on_document_create();
CREATE TRIGGER document_changed BEFORE UPDATE ON document FOR EACH ROW EXECUTE PROCEDURE on_document_change();

UPDATE documentmimetype SET documentmimetype_timecreate=NOW() WHERE documentmimetype_timecreate IS NULL;
ALTER TABLE documentmimetype ALTER COLUMN documentmimetype_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_documentmimetype_change() RETURNS trigger AS '
BEGIN
new.documentmimetype_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_documentmimetype_create() RETURNS trigger AS '
BEGIN
new.documentmimetype_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER documentmimetype_created BEFORE INSERT ON documentmimetype FOR EACH ROW EXECUTE PROCEDURE on_documentmimetype_create();
CREATE TRIGGER documentmimetype_changed BEFORE UPDATE ON documentmimetype FOR EACH ROW EXECUTE PROCEDURE on_documentmimetype_change();

UPDATE project SET project_timecreate=NOW() WHERE project_timecreate IS NULL;
ALTER TABLE project ALTER COLUMN project_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_project_change() RETURNS trigger AS '
BEGIN
new.project_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_project_create() RETURNS trigger AS '
BEGIN
new.project_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER project_created BEFORE INSERT ON project FOR EACH ROW EXECUTE PROCEDURE on_project_create();
CREATE TRIGGER project_changed BEFORE UPDATE ON project FOR EACH ROW EXECUTE PROCEDURE on_project_change();

UPDATE projecttask SET projecttask_timecreate=NOW() WHERE projecttask_timecreate IS NULL;
ALTER TABLE projecttask ALTER COLUMN projecttask_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_projecttask_change() RETURNS trigger AS '
BEGIN
new.projecttask_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_projecttask_create() RETURNS trigger AS '
BEGIN
new.projecttask_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER projecttask_created BEFORE INSERT ON projecttask FOR EACH ROW EXECUTE PROCEDURE on_projecttask_create();
CREATE TRIGGER projecttask_changed BEFORE UPDATE ON projecttask FOR EACH ROW EXECUTE PROCEDURE on_projecttask_change();

UPDATE projectreftask SET projectreftask_timecreate=NOW() WHERE projectreftask_timecreate IS NULL;
ALTER TABLE projectreftask ALTER COLUMN projectreftask_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_projectreftask_change() RETURNS trigger AS '
BEGIN
new.projectreftask_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_projectreftask_create() RETURNS trigger AS '
BEGIN
new.projectreftask_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER projectreftask_created BEFORE INSERT ON projectreftask FOR EACH ROW EXECUTE PROCEDURE on_projectreftask_create();
CREATE TRIGGER projectreftask_changed BEFORE UPDATE ON projectreftask FOR EACH ROW EXECUTE PROCEDURE on_projectreftask_change();

UPDATE projectuser SET projectuser_timecreate=NOW() WHERE projectuser_timecreate IS NULL;
ALTER TABLE projectuser ALTER COLUMN projectuser_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_projectuser_change() RETURNS trigger AS '
BEGIN
new.projectuser_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_projectuser_create() RETURNS trigger AS '
BEGIN
new.projectuser_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER projectuser_created BEFORE INSERT ON projectuser FOR EACH ROW EXECUTE PROCEDURE on_projectuser_create();
CREATE TRIGGER projectuser_changed BEFORE UPDATE ON projectuser FOR EACH ROW EXECUTE PROCEDURE on_projectuser_change();

UPDATE projectclosing SET projectclosing_timecreate=NOW() WHERE projectclosing_timecreate IS NULL;
ALTER TABLE projectclosing ALTER COLUMN projectclosing_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_projectclosing_change() RETURNS trigger AS '
BEGIN
new.projectclosing_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_projectclosing_create() RETURNS trigger AS '
BEGIN
new.projectclosing_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER projectclosing_created BEFORE INSERT ON projectclosing FOR EACH ROW EXECUTE PROCEDURE on_projectclosing_create();
CREATE TRIGGER projectclosing_changed BEFORE UPDATE ON projectclosing FOR EACH ROW EXECUTE PROCEDURE on_projectclosing_change();

UPDATE cv SET cv_timecreate=NOW() WHERE cv_timecreate IS NULL;
ALTER TABLE cv ALTER COLUMN cv_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_cv_change() RETURNS trigger AS '
BEGIN
new.cv_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_cv_create() RETURNS trigger AS '
BEGIN
new.cv_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER cv_created BEFORE INSERT ON cv FOR EACH ROW EXECUTE PROCEDURE on_cv_create();
CREATE TRIGGER cv_changed BEFORE UPDATE ON cv FOR EACH ROW EXECUTE PROCEDURE on_cv_change();

UPDATE timetask SET timetask_timecreate=NOW() WHERE timetask_timecreate IS NULL;
ALTER TABLE timetask ALTER COLUMN timetask_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_timetask_change() RETURNS trigger AS '
BEGIN
new.timetask_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_timetask_create() RETURNS trigger AS '
BEGIN
new.timetask_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER timetask_created BEFORE INSERT ON timetask FOR EACH ROW EXECUTE PROCEDURE on_timetask_create();
CREATE TRIGGER timetask_changed BEFORE UPDATE ON timetask FOR EACH ROW EXECUTE PROCEDURE on_timetask_change();

UPDATE tasktype SET tasktype_timecreate=NOW() WHERE tasktype_timecreate IS NULL;
ALTER TABLE tasktype ALTER COLUMN tasktype_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_tasktype_change() RETURNS trigger AS '
BEGIN
new.tasktype_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_tasktype_create() RETURNS trigger AS '
BEGIN
new.tasktype_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER tasktype_created BEFORE INSERT ON tasktype FOR EACH ROW EXECUTE PROCEDURE on_tasktype_create();
CREATE TRIGGER tasktype_changed BEFORE UPDATE ON tasktype FOR EACH ROW EXECUTE PROCEDURE on_tasktype_change();

UPDATE contract SET contract_timecreate=NOW() WHERE contract_timecreate IS NULL;
ALTER TABLE contract ALTER COLUMN contract_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_contract_change() RETURNS trigger AS '
BEGIN
new.contract_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_contract_create() RETURNS trigger AS '
BEGIN
new.contract_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER contract_created BEFORE INSERT ON contract FOR EACH ROW EXECUTE PROCEDURE on_contract_create();
CREATE TRIGGER contract_changed BEFORE UPDATE ON contract FOR EACH ROW EXECUTE PROCEDURE on_contract_change();

UPDATE contracttype SET contracttype_timecreate=NOW() WHERE contracttype_timecreate IS NULL;
ALTER TABLE contracttype ALTER COLUMN contracttype_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_contracttype_change() RETURNS trigger AS '
BEGIN
new.contracttype_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_contracttype_create() RETURNS trigger AS '
BEGIN
new.contracttype_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER contracttype_created BEFORE INSERT ON contracttype FOR EACH ROW EXECUTE PROCEDURE on_contracttype_create();
CREATE TRIGGER contracttype_changed BEFORE UPDATE ON contracttype FOR EACH ROW EXECUTE PROCEDURE on_contracttype_change();

UPDATE contractpriority SET contractpriority_timecreate=NOW() WHERE contractpriority_timecreate IS NULL;
ALTER TABLE contractpriority ALTER COLUMN contractpriority_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_contractpriority_change() RETURNS trigger AS '
BEGIN
new.contractpriority_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_contractpriority_create() RETURNS trigger AS '
BEGIN
new.contractpriority_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER contractpriority_created BEFORE INSERT ON contractpriority FOR EACH ROW EXECUTE PROCEDURE on_contractpriority_create();
CREATE TRIGGER contractpriority_changed BEFORE UPDATE ON contractpriority FOR EACH ROW EXECUTE PROCEDURE on_contractpriority_change();

UPDATE contractstatus SET contractstatus_timecreate=NOW() WHERE contractstatus_timecreate IS NULL;
ALTER TABLE contractstatus ALTER COLUMN contractstatus_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_contractstatus_change() RETURNS trigger AS '
BEGIN
new.contractstatus_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_contractstatus_create() RETURNS trigger AS '
BEGIN
new.contractstatus_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER contractstatus_created BEFORE INSERT ON contractstatus FOR EACH ROW EXECUTE PROCEDURE on_contractstatus_create();
CREATE TRIGGER contractstatus_changed BEFORE UPDATE ON contractstatus FOR EACH ROW EXECUTE PROCEDURE on_contractstatus_change();

UPDATE incident SET incident_timecreate=NOW() WHERE incident_timecreate IS NULL;
ALTER TABLE incident ALTER COLUMN incident_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_incident_change() RETURNS trigger AS '
BEGIN
new.incident_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_incident_create() RETURNS trigger AS '
BEGIN
new.incident_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER incident_created BEFORE INSERT ON incident FOR EACH ROW EXECUTE PROCEDURE on_incident_create();
CREATE TRIGGER incident_changed BEFORE UPDATE ON incident FOR EACH ROW EXECUTE PROCEDURE on_incident_change();

UPDATE incidentpriority SET incidentpriority_timecreate=NOW() WHERE incidentpriority_timecreate IS NULL;
ALTER TABLE incidentpriority ALTER COLUMN incidentpriority_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_incidentpriority_change() RETURNS trigger AS '
BEGIN
new.incidentpriority_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_incidentpriority_create() RETURNS trigger AS '
BEGIN
new.incidentpriority_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER incidentpriority_created BEFORE INSERT ON incidentpriority FOR EACH ROW EXECUTE PROCEDURE on_incidentpriority_create();
CREATE TRIGGER incidentpriority_changed BEFORE UPDATE ON incidentpriority FOR EACH ROW EXECUTE PROCEDURE on_incidentpriority_change();

UPDATE incidentstatus SET incidentstatus_timecreate=NOW() WHERE incidentstatus_timecreate IS NULL;
ALTER TABLE incidentstatus ALTER COLUMN incidentstatus_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_incidentstatus_change() RETURNS trigger AS '
BEGIN
new.incidentstatus_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_incidentstatus_create() RETURNS trigger AS '
BEGIN
new.incidentstatus_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER incidentstatus_created BEFORE INSERT ON incidentstatus FOR EACH ROW EXECUTE PROCEDURE on_incidentstatus_create();
CREATE TRIGGER incidentstatus_changed BEFORE UPDATE ON incidentstatus FOR EACH ROW EXECUTE PROCEDURE on_incidentstatus_change();

UPDATE incidentresolutiontype SET incidentresolutiontype_timecreate=NOW() WHERE incidentresolutiontype_timecreate IS NULL;
ALTER TABLE incidentresolutiontype ALTER COLUMN incidentresolutiontype_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_incidentresolutiontype_change() RETURNS trigger AS '
BEGIN
new.incidentresolutiontype_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_incidentresolutiontype_create() RETURNS trigger AS '
BEGIN
new.incidentresolutiontype_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER incidentresolutiontype_created BEFORE INSERT ON incidentresolutiontype FOR EACH ROW EXECUTE PROCEDURE on_incidentresolutiontype_create();
CREATE TRIGGER incidentresolutiontype_changed BEFORE UPDATE ON incidentresolutiontype FOR EACH ROW EXECUTE PROCEDURE on_incidentresolutiontype_change();

UPDATE invoice SET invoice_timecreate=NOW() WHERE invoice_timecreate IS NULL;
ALTER TABLE invoice ALTER COLUMN invoice_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_invoice_change() RETURNS trigger AS '
BEGIN
new.invoice_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_invoice_create() RETURNS trigger AS '
BEGIN
new.invoice_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER invoice_created BEFORE INSERT ON invoice FOR EACH ROW EXECUTE PROCEDURE on_invoice_create();
CREATE TRIGGER invoice_changed BEFORE UPDATE ON invoice FOR EACH ROW EXECUTE PROCEDURE on_invoice_change();

UPDATE payment SET payment_timecreate=NOW() WHERE payment_timecreate IS NULL;
ALTER TABLE payment ALTER COLUMN payment_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_payment_change() RETURNS trigger AS '
BEGIN
new.payment_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_payment_create() RETURNS trigger AS '
BEGIN
new.payment_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER payment_created BEFORE INSERT ON payment FOR EACH ROW EXECUTE PROCEDURE on_payment_create();
CREATE TRIGGER payment_changed BEFORE UPDATE ON payment FOR EACH ROW EXECUTE PROCEDURE on_payment_change();

UPDATE paymentinvoice SET paymentinvoice_timecreate=NOW() WHERE paymentinvoice_timecreate IS NULL;
ALTER TABLE paymentinvoice ALTER COLUMN paymentinvoice_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_paymentinvoice_change() RETURNS trigger AS '
BEGIN
new.paymentinvoice_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_paymentinvoice_create() RETURNS trigger AS '
BEGIN
new.paymentinvoice_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER paymentinvoice_created BEFORE INSERT ON paymentinvoice FOR EACH ROW EXECUTE PROCEDURE on_paymentinvoice_create();
CREATE TRIGGER paymentinvoice_changed BEFORE UPDATE ON paymentinvoice FOR EACH ROW EXECUTE PROCEDURE on_paymentinvoice_change();

UPDATE account SET account_timecreate=NOW() WHERE account_timecreate IS NULL;
ALTER TABLE account ALTER COLUMN account_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_account_change() RETURNS trigger AS '
BEGIN
new.account_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_account_create() RETURNS trigger AS '
BEGIN
new.account_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER account_created BEFORE INSERT ON account FOR EACH ROW EXECUTE PROCEDURE on_account_create();
CREATE TRIGGER account_changed BEFORE UPDATE ON account FOR EACH ROW EXECUTE PROCEDURE on_account_change();

UPDATE ugroup SET group_timecreate=NOW() WHERE group_timecreate IS NULL;
ALTER TABLE ugroup ALTER COLUMN group_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_ugroup_change() RETURNS trigger AS '
BEGIN
new.group_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_ugroup_create() RETURNS trigger AS '
BEGIN
new.group_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER ugroup_created BEFORE INSERT ON ugroup FOR EACH ROW EXECUTE PROCEDURE on_ugroup_create();
CREATE TRIGGER ugroup_changed BEFORE UPDATE ON ugroup FOR EACH ROW EXECUTE PROCEDURE on_ugroup_change();

UPDATE organizationalchart SET organizationalchart_timecreate=NOW() WHERE organizationalchart_timecreate IS NULL;
ALTER TABLE organizationalchart ALTER COLUMN organizationalchart_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_organizationalchart_change() RETURNS trigger AS '
BEGIN
new.organizationalchart_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_organizationalchart_create() RETURNS trigger AS '
BEGIN
new.organizationalchart_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER organizationalchart_created BEFORE INSERT ON organizationalchart FOR EACH ROW EXECUTE PROCEDURE on_organizationalchart_create();
CREATE TRIGGER organizationalchart_changed BEFORE UPDATE ON organizationalchart FOR EACH ROW EXECUTE PROCEDURE on_organizationalchart_change();

UPDATE ogroup SET ogroup_timecreate=NOW() WHERE ogroup_timecreate IS NULL;
ALTER TABLE ogroup ALTER COLUMN ogroup_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_ogroup_change() RETURNS trigger AS '
BEGIN
new.ogroup_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_ogroup_create() RETURNS trigger AS '
BEGIN
new.ogroup_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER ogroup_created BEFORE INSERT ON ogroup FOR EACH ROW EXECUTE PROCEDURE on_ogroup_create();
CREATE TRIGGER ogroup_changed BEFORE UPDATE ON ogroup FOR EACH ROW EXECUTE PROCEDURE on_ogroup_change();

UPDATE ogrouplink SET ogrouplink_timecreate=NOW() WHERE ogrouplink_timecreate IS NULL;
ALTER TABLE ogrouplink ALTER COLUMN ogrouplink_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_ogrouplink_change() RETURNS trigger AS '
BEGIN
new.ogrouplink_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_ogrouplink_create() RETURNS trigger AS '
BEGIN
new.ogrouplink_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER ogrouplink_created BEFORE INSERT ON ogrouplink FOR EACH ROW EXECUTE PROCEDURE on_ogrouplink_create();
CREATE TRIGGER ogrouplink_changed BEFORE UPDATE ON ogrouplink FOR EACH ROW EXECUTE PROCEDURE on_ogrouplink_change();

UPDATE import SET import_timecreate=NOW() WHERE import_timecreate IS NULL;
ALTER TABLE import ALTER COLUMN import_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_import_change() RETURNS trigger AS '
BEGIN
new.import_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_import_create() RETURNS trigger AS '
BEGIN
new.import_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER import_created BEFORE INSERT ON import FOR EACH ROW EXECUTE PROCEDURE on_import_create();
CREATE TRIGGER import_changed BEFORE UPDATE ON import FOR EACH ROW EXECUTE PROCEDURE on_import_change();

UPDATE resource SET resource_timecreate=NOW() WHERE resource_timecreate IS NULL;
ALTER TABLE resource ALTER COLUMN resource_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_resource_change() RETURNS trigger AS '
BEGIN
new.resource_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_resource_create() RETURNS trigger AS '
BEGIN
new.resource_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER resource_created BEFORE INSERT ON resource FOR EACH ROW EXECUTE PROCEDURE on_resource_create();
CREATE TRIGGER resource_changed BEFORE UPDATE ON resource FOR EACH ROW EXECUTE PROCEDURE on_resource_change();

UPDATE rgroup SET rgroup_timecreate=NOW() WHERE rgroup_timecreate IS NULL;
ALTER TABLE rgroup ALTER COLUMN rgroup_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_rgroup_change() RETURNS trigger AS '
BEGIN
new.rgroup_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_rgroup_create() RETURNS trigger AS '
BEGIN
new.rgroup_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER rgroup_created BEFORE INSERT ON rgroup FOR EACH ROW EXECUTE PROCEDURE on_rgroup_create();
CREATE TRIGGER rgroup_changed BEFORE UPDATE ON rgroup FOR EACH ROW EXECUTE PROCEDURE on_rgroup_change();

UPDATE domain SET domain_timecreate=NOW() WHERE domain_timecreate IS NULL;
ALTER TABLE domain ALTER COLUMN domain_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_domain_change() RETURNS trigger AS '
BEGIN
new.domain_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_domain_create() RETURNS trigger AS '
BEGIN
new.domain_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER domain_created BEFORE INSERT ON domain FOR EACH ROW EXECUTE PROCEDURE on_domain_create();
CREATE TRIGGER domain_changed BEFORE UPDATE ON domain FOR EACH ROW EXECUTE PROCEDURE on_domain_change();

UPDATE host SET host_timecreate=NOW() WHERE host_timecreate IS NULL;
ALTER TABLE host ALTER COLUMN host_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_host_change() RETURNS trigger AS '
BEGIN
new.host_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_host_create() RETURNS trigger AS '
BEGIN
new.host_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER host_created BEFORE INSERT ON host FOR EACH ROW EXECUTE PROCEDURE on_host_create();
CREATE TRIGGER host_changed BEFORE UPDATE ON host FOR EACH ROW EXECUTE PROCEDURE on_host_change();

UPDATE mailshare SET mailshare_timecreate=NOW() WHERE mailshare_timecreate IS NULL;
ALTER TABLE mailshare ALTER COLUMN mailshare_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_mailshare_change() RETURNS trigger AS '
BEGIN
new.mailshare_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_mailshare_create() RETURNS trigger AS '
BEGIN
new.mailshare_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER mailshare_created BEFORE INSERT ON mailshare FOR EACH ROW EXECUTE PROCEDURE on_mailshare_create();
CREATE TRIGGER mailshare_changed BEFORE UPDATE ON mailshare FOR EACH ROW EXECUTE PROCEDURE on_mailshare_change();

UPDATE mailserver SET mailserver_timecreate=NOW() WHERE mailserver_timecreate IS NULL;
ALTER TABLE mailserver ALTER COLUMN mailserver_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_mailserver_change() RETURNS trigger AS '
BEGIN
new.mailserver_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_mailserver_create() RETURNS trigger AS '
BEGIN
new.mailserver_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER mailserver_created BEFORE INSERT ON mailserver FOR EACH ROW EXECUTE PROCEDURE on_mailserver_create();
CREATE TRIGGER mailserver_changed BEFORE UPDATE ON mailserver FOR EACH ROW EXECUTE PROCEDURE on_mailserver_change();

UPDATE profile SET profile_timecreate=NOW() WHERE profile_timecreate IS NULL;
ALTER TABLE profile ALTER COLUMN profile_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_profile_change() RETURNS trigger AS '
BEGIN
new.profile_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_profile_create() RETURNS trigger AS '
BEGIN
new.profile_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER profile_created BEFORE INSERT ON profile FOR EACH ROW EXECUTE PROCEDURE on_profile_create();
CREATE TRIGGER profile_changed BEFORE UPDATE ON profile FOR EACH ROW EXECUTE PROCEDURE on_profile_change();

