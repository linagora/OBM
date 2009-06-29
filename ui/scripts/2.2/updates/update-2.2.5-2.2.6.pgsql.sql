-- /////////////////////////////////////////////////////////////////////////////
-- OBM - File : update-2.2.5-2.2.6.pgsql.sql
-- 2009-06-23 Mehdi Rande
-- /////////////////////////////////////////////////////////////////////////////
-- $Id:$
-- /////////////////////////////////////////////////////////////////////////////

-- module 'resource'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'resource', 'resource_delegation', 5, 1);

-- module 'people'
INSERT INTO DisplayPref (display_user_id, display_entity, display_fieldname, display_fieldorder, display_display) values (null, 'people', 'userobm_delegation', 11, 1);
INSERT INTO DisplayPref (display_user_id, display_entity, display_fieldname, display_fieldorder, display_display) values (null, 'people', 'userobm_vacation', 12, 1);

-- contact query optimization
--
-- Name: contact_privacy_key; Type: INDEX; Schema: public; Owner: -; Tablespace:
--
CREATE INDEX contact_privacy_key ON Contact (contact_privacy);

-- foreign key child key
--
-- Name: account_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX account_userupdate_fkey ON account (account_userupdate);
--
-- Name: account_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX account_domain_id_fkey ON account (account_domain_id);
--
-- Name: account_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX account_usercreate_fkey ON account (account_usercreate);
--
-- Name: accountentity_account_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX accountentity_account_id_fkey ON accountentity (accountentity_account_id);
--
-- Name: accountentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX accountentity_entity_id_fkey ON accountentity (accountentity_entity_id);
--
-- Name: activeuserobm_userobm_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX activeuserobm_userobm_id_fkey ON activeuserobm (activeuserobm_userobm_id);
--
-- Name: address_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX address_entity_id_fkey ON address (address_entity_id);
--
-- Name: cv_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX cv_usercreate_fkey ON cv (cv_usercreate);
--
-- Name: cv_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX cv_domain_id_fkey ON cv (cv_domain_id);
--
-- Name: cv_userobm_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX cv_userobm_id_fkey ON cv (cv_userobm_id);
--
-- Name: cv_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX cv_userupdate_fkey ON cv (cv_userupdate);
--
-- Name: calendarentity_calendar_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX calendarentity_calendar_id_fkey ON calendarentity (calendarentity_calendar_id);
--
-- Name: calendarentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX calendarentity_entity_id_fkey ON calendarentity (calendarentity_entity_id);
--
-- Name: campaign_parent_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX campaign_parent_fkey ON campaign (campaign_parent);
--
-- Name: campaign_email_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX campaign_email_fkey ON campaign (campaign_email);
--
-- Name: campaigndisabledentity_campaign_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX campaigndisabledentity_campaign_id_fkey ON campaigndisabledentity (campaigndisabledentity_campaign_id);
--
-- Name: campaigndisabledentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX campaigndisabledentity_entity_id_fkey ON campaigndisabledentity (campaigndisabledentity_entity_id);
--
-- Name: campaignentity_campaign_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX campaignentity_campaign_id_fkey ON campaignentity (campaignentity_campaign_id);
--
-- Name: campaignentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX campaignentity_entity_id_fkey ON campaignentity (campaignentity_entity_id);
--
-- Name: campaignmailtarget_campaign_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX campaignmailtarget_campaign_id_fkey ON campaignmailtarget (campaignmailtarget_campaign_id);
--
-- Name: campaignmailtarget_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX campaignmailtarget_entity_id_fkey ON campaignmailtarget (campaignmailtarget_entity_id);
--
-- Name: campaigntarget_campaign_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX campaigntarget_campaign_id_fkey ON campaigntarget (campaigntarget_campaign_id);
--
-- Name: campaigntarget_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX campaigntarget_entity_id_fkey ON campaigntarget (campaigntarget_entity_id);
--
-- Name: category_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX category_usercreate_fkey ON category (category_usercreate);
--
-- Name: category_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX category_domain_id_fkey ON category (category_domain_id);
--
-- Name: category_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX category_userupdate_fkey ON category (category_userupdate);
--
-- Name: categorylink_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX categorylink_entity_id_fkey ON categorylink (categorylink_entity_id);
--
-- Name: categorylink_category_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX categorylink_category_id_fkey ON categorylink (categorylink_category_id);
--
-- Name: company_marketingmanager_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX company_marketingmanager_id_fkey ON company (company_marketingmanager_id);
--
-- Name: company_activity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX company_activity_id_fkey ON company (company_activity_id);
--
-- Name: company_datasource_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX company_datasource_id_fkey ON company (company_datasource_id);
--
-- Name: company_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX company_domain_id_fkey ON company (company_domain_id);
--
-- Name: company_nafcode_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX company_nafcode_id_fkey ON company (company_nafcode_id);
--
-- Name: company_type_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX company_type_id_fkey ON company (company_type_id);
--
-- Name: company_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX company_usercreate_fkey ON company (company_usercreate);
--
-- Name: company_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX company_userupdate_fkey ON company (company_userupdate);
--
-- Name: companyactivity_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX companyactivity_usercreate_fkey ON companyactivity (companyactivity_usercreate);
--
-- Name: companyactivity_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX companyactivity_domain_id_fkey ON companyactivity (companyactivity_domain_id);
--
-- Name: companyactivity_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX companyactivity_userupdate_fkey ON companyactivity (companyactivity_userupdate);
--
-- Name: companyentity_company_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX companyentity_company_id_fkey ON companyentity (companyentity_company_id);
--
-- Name: companyentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX companyentity_entity_id_fkey ON companyentity (companyentity_entity_id);
--
-- Name: companynafcode_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX companynafcode_usercreate_fkey ON companynafcode (companynafcode_usercreate);
--
-- Name: companynafcode_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX companynafcode_domain_id_fkey ON companynafcode (companynafcode_domain_id);
--
-- Name: companynafcode_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX companynafcode_userupdate_fkey ON companynafcode (companynafcode_userupdate);
--
-- Name: companytype_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX companytype_usercreate_fkey ON companytype (companytype_usercreate);
--
-- Name: companytype_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX companytype_domain_id_fkey ON companytype (companytype_domain_id);
--
-- Name: companytype_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX companytype_userupdate_fkey ON companytype (companytype_userupdate);
--
-- Name: contact_function_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contact_function_id_fkey ON contact (contact_function_id);
--
-- Name: contact_company_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contact_company_id_fkey ON contact (contact_company_id);
--
-- Name: contact_datasource_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contact_datasource_id_fkey ON contact (contact_datasource_id);
--
-- Name: contact_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contact_domain_id_fkey ON contact (contact_domain_id);
--
-- Name: contact_kind_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contact_kind_id_fkey ON contact (contact_kind_id);
--
-- Name: contact_marketingmanager_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contact_marketingmanager_id_fkey ON contact (contact_marketingmanager_id);
--
-- Name: contact_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contact_usercreate_fkey ON contact (contact_usercreate);
--
-- Name: contact_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contact_userupdate_fkey ON contact (contact_userupdate);
--
-- Name: contact_birthday_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contact_birthday_id_fkey ON contact (contact_birthday_id);
--
-- Name: contact_anniversary_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contact_anniversary_id_fkey ON contact (contact_anniversary_id);
--
-- Name: contact_photo_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contact_photo_id_fkey ON contact (contact_photo_id);
--
-- Name: contactentity_contact_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contactentity_contact_id_fkey ON contactentity (contactentity_contact_id);
--
-- Name: contactentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contactentity_entity_id_fkey ON contactentity (contactentity_entity_id);
--
-- Name: contactfunction_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contactfunction_usercreate_fkey ON contactfunction (contactfunction_usercreate);
--
-- Name: contactfunction_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contactfunction_domain_id_fkey ON contactfunction (contactfunction_domain_id);
--
-- Name: contactfunction_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contactfunction_userupdate_fkey ON contactfunction (contactfunction_userupdate);
--
-- Name: contactlist_contact_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contactlist_contact_id_fkey ON contactlist (contactlist_contact_id);
--
-- Name: contactlist_list_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contactlist_list_id_fkey ON contactlist (contactlist_list_id);
--
-- Name: contract_marketmanager_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contract_marketmanager_id_fkey ON contract (contract_marketmanager_id);
--
-- Name: contract_company_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contract_company_id_fkey ON contract (contract_company_id);
--
-- Name: contract_contact1_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contract_contact1_id_fkey ON contract (contract_contact1_id);
--
-- Name: contract_contact2_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contract_contact2_id_fkey ON contract (contract_contact2_id);
--
-- Name: contract_deal_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contract_deal_id_fkey ON contract (contract_deal_id);
--
-- Name: contract_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contract_domain_id_fkey ON contract (contract_domain_id);
--
-- Name: contract_priority_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contract_priority_id_fkey ON contract (contract_priority_id);
--
-- Name: contract_status_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contract_status_id_fkey ON contract (contract_status_id);
--
-- Name: contract_techmanager_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contract_techmanager_id_fkey ON contract (contract_techmanager_id);
--
-- Name: contract_type_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contract_type_id_fkey ON contract (contract_type_id);
--
-- Name: contract_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contract_usercreate_fkey ON contract (contract_usercreate);
--
-- Name: contract_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contract_userupdate_fkey ON contract (contract_userupdate);
--
-- Name: contractentity_contract_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contractentity_contract_id_fkey ON contractentity (contractentity_contract_id);
--
-- Name: contractentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contractentity_entity_id_fkey ON contractentity (contractentity_entity_id);
--
-- Name: contractpriority_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contractpriority_usercreate_fkey ON contractpriority (contractpriority_usercreate);
--
-- Name: contractpriority_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contractpriority_domain_id_fkey ON contractpriority (contractpriority_domain_id);
--
-- Name: contractpriority_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contractpriority_userupdate_fkey ON contractpriority (contractpriority_userupdate);
--
-- Name: contractstatus_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contractstatus_usercreate_fkey ON contractstatus (contractstatus_usercreate);
--
-- Name: contractstatus_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contractstatus_domain_id_fkey ON contractstatus (contractstatus_domain_id);
--
-- Name: contractstatus_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contractstatus_userupdate_fkey ON contractstatus (contractstatus_userupdate);
--
-- Name: contracttype_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contracttype_usercreate_fkey ON contracttype (contracttype_usercreate);
--
-- Name: contracttype_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contracttype_domain_id_fkey ON contracttype (contracttype_domain_id);
--
-- Name: contracttype_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX contracttype_userupdate_fkey ON contracttype (contracttype_userupdate);
--
-- Name: country_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX country_usercreate_fkey ON country (country_usercreate);
--
-- Name: country_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX country_domain_id_fkey ON country (country_domain_id);
--
-- Name: country_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX country_userupdate_fkey ON country (country_userupdate);
--
-- Name: cventity_cv_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX cventity_cv_id_fkey ON cventity (cventity_cv_id);
--
-- Name: cventity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX cventity_entity_id_fkey ON cventity (cventity_entity_id);
--
-- Name: datasource_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX datasource_usercreate_fkey ON datasource (datasource_usercreate);
--
-- Name: datasource_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX datasource_domain_id_fkey ON datasource (datasource_domain_id);
--
-- Name: datasource_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX datasource_userupdate_fkey ON datasource (datasource_userupdate);
--
-- Name: deal_source_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX deal_source_id_fkey ON deal (deal_source_id);
--
-- Name: deal_company_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX deal_company_id_fkey ON deal (deal_company_id);
--
-- Name: deal_contact1_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX deal_contact1_id_fkey ON deal (deal_contact1_id);
--
-- Name: deal_contact2_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX deal_contact2_id_fkey ON deal (deal_contact2_id);
--
-- Name: deal_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX deal_domain_id_fkey ON deal (deal_domain_id);
--
-- Name: deal_marketingmanager_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX deal_marketingmanager_id_fkey ON deal (deal_marketingmanager_id);
--
-- Name: deal_parentdeal_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX deal_parentdeal_id_fkey ON deal (deal_parentdeal_id);
--
-- Name: deal_region_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX deal_region_id_fkey ON deal (deal_region_id);
--
-- Name: deal_tasktype_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX deal_tasktype_id_fkey ON deal (deal_tasktype_id);
--
-- Name: deal_technicalmanager_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX deal_technicalmanager_id_fkey ON deal (deal_technicalmanager_id);
--
-- Name: deal_type_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX deal_type_id_fkey ON deal (deal_type_id);
--
-- Name: deal_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX deal_usercreate_fkey ON deal (deal_usercreate);
--
-- Name: deal_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX deal_userupdate_fkey ON deal (deal_userupdate);
--
-- Name: dealcompany_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX dealcompany_usercreate_fkey ON dealcompany (dealcompany_usercreate);
--
-- Name: dealcompany_company_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX dealcompany_company_id_fkey ON dealcompany (dealcompany_company_id);
--
-- Name: dealcompany_deal_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX dealcompany_deal_id_fkey ON dealcompany (dealcompany_deal_id);
--
-- Name: dealcompany_role_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX dealcompany_role_id_fkey ON dealcompany (dealcompany_role_id);
--
-- Name: dealcompany_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX dealcompany_userupdate_fkey ON dealcompany (dealcompany_userupdate);
--
-- Name: dealcompanyrole_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX dealcompanyrole_usercreate_fkey ON dealcompanyrole (dealcompanyrole_usercreate);
--
-- Name: dealcompanyrole_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX dealcompanyrole_domain_id_fkey ON dealcompanyrole (dealcompanyrole_domain_id);
--
-- Name: dealcompanyrole_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX dealcompanyrole_userupdate_fkey ON dealcompanyrole (dealcompanyrole_userupdate);
--
-- Name: dealentity_deal_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX dealentity_deal_id_fkey ON dealentity (dealentity_deal_id);
--
-- Name: dealentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX dealentity_entity_id_fkey ON dealentity (dealentity_entity_id);
--
-- Name: dealstatus_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX dealstatus_usercreate_fkey ON dealstatus (dealstatus_usercreate);
--
-- Name: dealstatus_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX dealstatus_domain_id_fkey ON dealstatus (dealstatus_domain_id);
--
-- Name: dealstatus_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX dealstatus_userupdate_fkey ON dealstatus (dealstatus_userupdate);
--
-- Name: dealtype_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX dealtype_usercreate_fkey ON dealtype (dealtype_usercreate);
--
-- Name: dealtype_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX dealtype_domain_id_fkey ON dealtype (dealtype_domain_id);
--
-- Name: dealtype_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX dealtype_userupdate_fkey ON dealtype (dealtype_userupdate);
--
-- Name: defaultodttemplate_document_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX defaultodttemplate_document_id_fkey ON defaultodttemplate (defaultodttemplate_document_id);
--
-- Name: defaultodttemplate_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX defaultodttemplate_domain_id_fkey ON defaultodttemplate (defaultodttemplate_domain_id);
--
-- Name: deleted_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX deleted_domain_id_fkey ON deleted (deleted_domain_id);
--
-- Name: deleted_user_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX deleted_user_id_fkey ON deleted (deleted_user_id);
--
-- Name: display_user_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX display_user_id_fkey ON displaypref (display_user_id);
--
-- Name: document_mimetype_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX document_mimetype_id_fkey ON document (document_mimetype_id);
--
-- Name: document_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX document_domain_id_fkey ON document (document_domain_id);
--
-- Name: document_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX document_usercreate_fkey ON document (document_usercreate);
--
-- Name: document_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX document_userupdate_fkey ON document (document_userupdate);
--
-- Name: documententity_document_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX documententity_document_id_fkey ON documententity (documententity_document_id);
--
-- Name: documententity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX documententity_entity_id_fkey ON documententity (documententity_entity_id);
--
-- Name: documentlink_document_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX documentlink_document_id_fkey ON documentlink (documentlink_document_id);
--
-- Name: documentlink_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX documentlink_entity_id_fkey ON documentlink (documentlink_entity_id);
--
-- Name: documentmimetype_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX documentmimetype_usercreate_fkey ON documentmimetype (documentmimetype_usercreate);
--
-- Name: documentmimetype_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX documentmimetype_domain_id_fkey ON documentmimetype (documentmimetype_domain_id);
--
-- Name: documentmimetype_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX documentmimetype_userupdate_fkey ON documentmimetype (documentmimetype_userupdate);
--
-- Name: domain_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX domain_usercreate_fkey ON domain (domain_usercreate);
--
-- Name: domain_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX domain_userupdate_fkey ON domain (domain_userupdate);
--
-- Name: domainentity_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX domainentity_domain_id_fkey ON domainentity (domainentity_domain_id);
--
-- Name: domainentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX domainentity_entity_id_fkey ON domainentity (domainentity_entity_id);
--
-- Name: domainpropertyvalue_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX domainpropertyvalue_domain_id_fkey ON domainpropertyvalue (domainpropertyvalue_domain_id);
--
-- Name: email_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX email_entity_id_fkey ON email (email_entity_id);
--
-- Name: entityright_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX entityright_entity_id_fkey ON entityright (entityright_entity_id);
--
-- Name: entityright_consumer_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX entityright_consumer_id_fkey ON entityright (entityright_consumer_id);
--
-- Name: event_category1_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX event_category1_id_fkey ON event (event_category1_id);
--
-- Name: event_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX event_domain_id_fkey ON event (event_domain_id);
--
-- Name: event_owner_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX event_owner_fkey ON event (event_owner);
--
-- Name: event_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX event_usercreate_fkey ON event (event_usercreate);
--
-- Name: event_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX event_userupdate_fkey ON event (event_userupdate);
--
-- Name: eventalert_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX eventalert_usercreate_fkey ON eventalert (eventalert_usercreate);
--
-- Name: eventalert_event_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX eventalert_event_id_fkey ON eventalert (eventalert_event_id);
--
-- Name: eventalert_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX eventalert_userupdate_fkey ON eventalert (eventalert_userupdate);
--
-- Name: eventalert_user_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX eventalert_user_id_fkey ON eventalert (eventalert_user_id);
--
-- Name: eventcategory1_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX eventcategory1_usercreate_fkey ON eventcategory1 (eventcategory1_usercreate);
--
-- Name: eventcategory1_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX eventcategory1_domain_id_fkey ON eventcategory1 (eventcategory1_domain_id);
--
-- Name: eventcategory1_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX eventcategory1_userupdate_fkey ON eventcategory1 (eventcategory1_userupdate);
--
-- Name: evententity_event_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX evententity_event_id_fkey ON evententity (evententity_event_id);
--
-- Name: evententity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX evententity_entity_id_fkey ON evententity (evententity_entity_id);
--
-- Name: eventexception_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX eventexception_usercreate_fkey ON eventexception (eventexception_usercreate);
--
-- Name: eventexception_event_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX eventexception_event_id_fkey ON eventexception (eventexception_event_id);
--
-- Name: eventexception_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX eventexception_userupdate_fkey ON eventexception (eventexception_userupdate);
--
-- Name: eventlink_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX eventlink_entity_id_fkey ON eventlink (eventlink_entity_id);
--
-- Name: eventlink_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX eventlink_usercreate_fkey ON eventlink (eventlink_usercreate);
--
-- Name: eventlink_event_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX eventlink_event_id_fkey ON eventlink (eventlink_event_id);
--
-- Name: eventlink_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX eventlink_userupdate_fkey ON eventlink (eventlink_userupdate);
--
-- Name: groupentity_group_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX groupentity_group_id_fkey ON groupentity (groupentity_group_id);
--
-- Name: groupentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX groupentity_entity_id_fkey ON groupentity (groupentity_entity_id);
--
-- Name: groupgroup_child_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX groupgroup_child_id_fkey ON groupgroup (groupgroup_child_id);
--
-- Name: groupgroup_parent_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX groupgroup_parent_id_fkey ON groupgroup (groupgroup_parent_id);
--
-- Name: host_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX host_usercreate_fkey ON host (host_usercreate);
--
-- Name: host_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX host_domain_id_fkey ON host (host_domain_id);
--
-- Name: host_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX host_userupdate_fkey ON host (host_userupdate);
--
-- Name: hostentity_host_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX hostentity_host_id_fkey ON hostentity (hostentity_host_id);
--
-- Name: hostentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX hostentity_entity_id_fkey ON hostentity (hostentity_entity_id);
--
-- Name: im_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX im_entity_id_fkey ON im (im_entity_id);
--
-- Name: import_marketingmanager_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX import_marketingmanager_id_fkey ON import (import_marketingmanager_id);
--
-- Name: import_datasource_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX import_datasource_id_fkey ON import (import_datasource_id);
--
-- Name: import_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX import_domain_id_fkey ON import (import_domain_id);
--
-- Name: import_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX import_usercreate_fkey ON import (import_usercreate);
--
-- Name: import_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX import_userupdate_fkey ON import (import_userupdate);
--
-- Name: importentity_import_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX importentity_import_id_fkey ON importentity (importentity_import_id);
--
-- Name: importentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX importentity_entity_id_fkey ON importentity (importentity_entity_id);
--
-- Name: incident_owner_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX incident_owner_fkey ON incident (incident_owner);
--
-- Name: incident_contract_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX incident_contract_id_fkey ON incident (incident_contract_id);
--
-- Name: incident_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX incident_domain_id_fkey ON incident (incident_domain_id);
--
-- Name: incident_logger_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX incident_logger_fkey ON incident (incident_logger);
--
-- Name: incident_priority_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX incident_priority_id_fkey ON incident (incident_priority_id);
--
-- Name: incident_resolutiontype_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX incident_resolutiontype_id_fkey ON incident (incident_resolutiontype_id);
--
-- Name: incident_status_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX incident_status_id_fkey ON incident (incident_status_id);
--
-- Name: incident_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX incident_usercreate_fkey ON incident (incident_usercreate);
--
-- Name: incident_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX incident_userupdate_fkey ON incident (incident_userupdate);
--
-- Name: incidententity_incident_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX incidententity_incident_id_fkey ON incidententity (incidententity_incident_id);
--
-- Name: incidententity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX incidententity_entity_id_fkey ON incidententity (incidententity_entity_id);
--
-- Name: incidentpriority_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX incidentpriority_usercreate_fkey ON incidentpriority (incidentpriority_usercreate);
--
-- Name: incidentpriority_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX incidentpriority_domain_id_fkey ON incidentpriority (incidentpriority_domain_id);
--
-- Name: incidentpriority_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX incidentpriority_userupdate_fkey ON incidentpriority (incidentpriority_userupdate);
--
-- Name: incidentresolutiontype_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX incidentresolutiontype_usercreate_fkey ON incidentresolutiontype (incidentresolutiontype_usercreate);
--
-- Name: incidentresolutiontype_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX incidentresolutiontype_domain_id_fkey ON incidentresolutiontype (incidentresolutiontype_domain_id);
--
-- Name: incidentresolutiontype_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX incidentresolutiontype_userupdate_fkey ON incidentresolutiontype (incidentresolutiontype_userupdate);
--
-- Name: incidentstatus_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX incidentstatus_usercreate_fkey ON incidentstatus (incidentstatus_usercreate);
--
-- Name: incidentstatus_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX incidentstatus_domain_id_fkey ON incidentstatus (incidentstatus_domain_id);
--
-- Name: incidentstatus_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX incidentstatus_userupdate_fkey ON incidentstatus (incidentstatus_userupdate);
--
-- Name: invoice_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX invoice_usercreate_fkey ON invoice (invoice_usercreate);
--
-- Name: invoice_company_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX invoice_company_id_fkey ON invoice (invoice_company_id);
--
-- Name: invoice_deal_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX invoice_deal_id_fkey ON invoice (invoice_deal_id);
--
-- Name: invoice_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX invoice_domain_id_fkey ON invoice (invoice_domain_id);
--
-- Name: invoice_project_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX invoice_project_id_fkey ON invoice (invoice_project_id);
--
-- Name: invoice_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX invoice_userupdate_fkey ON invoice (invoice_userupdate);
--
-- Name: invoiceentity_invoice_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX invoiceentity_invoice_id_fkey ON invoiceentity (invoiceentity_invoice_id);
--
-- Name: invoiceentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX invoiceentity_entity_id_fkey ON invoiceentity (invoiceentity_entity_id);
--
-- Name: kind_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX kind_usercreate_fkey ON kind (kind_usercreate);
--
-- Name: kind_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX kind_domain_id_fkey ON kind (kind_domain_id);
--
-- Name: kind_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX kind_userupdate_fkey ON kind (kind_userupdate);
--
-- Name: lead_status_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX lead_status_id_fkey ON lead (lead_status_id);
--
-- Name: lead_company_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX lead_company_id_fkey ON lead (lead_company_id);
--
-- Name: lead_contact_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX lead_contact_id_fkey ON lead (lead_contact_id);
--
-- Name: lead_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX lead_domain_id_fkey ON lead (lead_domain_id);
--
-- Name: lead_manager_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX lead_manager_id_fkey ON lead (lead_manager_id);
--
-- Name: lead_source_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX lead_source_id_fkey ON lead (lead_source_id);
--
-- Name: lead_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX lead_usercreate_fkey ON lead (lead_usercreate);
--
-- Name: lead_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX lead_userupdate_fkey ON lead (lead_userupdate);
--
-- Name: leadentity_lead_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX leadentity_lead_id_fkey ON leadentity (leadentity_lead_id);
--
-- Name: leadentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX leadentity_entity_id_fkey ON leadentity (leadentity_entity_id);
--
-- Name: leadsource_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX leadsource_usercreate_fkey ON leadsource (leadsource_usercreate);
--
-- Name: leadsource_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX leadsource_domain_id_fkey ON leadsource (leadsource_domain_id);
--
-- Name: leadsource_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX leadsource_userupdate_fkey ON leadsource (leadsource_userupdate);
--
-- Name: leadstatus_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX leadstatus_usercreate_fkey ON leadstatus (leadstatus_usercreate);
--
-- Name: leadstatus_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX leadstatus_domain_id_fkey ON leadstatus (leadstatus_domain_id);
--
-- Name: leadstatus_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX leadstatus_userupdate_fkey ON leadstatus (leadstatus_userupdate);
--
-- Name: list_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX list_usercreate_fkey ON list (list_usercreate);
--
-- Name: list_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX list_domain_id_fkey ON list (list_domain_id);
--
-- Name: list_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX list_userupdate_fkey ON list (list_userupdate);
--
-- Name: listentity_list_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX listentity_list_id_fkey ON listentity (listentity_list_id);
--
-- Name: listentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX listentity_entity_id_fkey ON listentity (listentity_entity_id);
--
-- Name: mailshare_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX mailshare_usercreate_fkey ON mailshare (mailshare_usercreate);
--
-- Name: mailshare_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX mailshare_domain_id_fkey ON mailshare (mailshare_domain_id);
--
-- Name: mailshare_mail_server_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX mailshare_mail_server_id_fkey ON mailshare (mailshare_mail_server_id);
--
-- Name: mailshare_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX mailshare_userupdate_fkey ON mailshare (mailshare_userupdate);
--
-- Name: mailboxentity_mailbox_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX mailboxentity_mailbox_id_fkey ON mailboxentity (mailboxentity_mailbox_id);
--
-- Name: mailboxentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX mailboxentity_entity_id_fkey ON mailboxentity (mailboxentity_entity_id);
--
-- Name: mailshareentity_mailshare_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX mailshareentity_mailshare_id_fkey ON mailshareentity (mailshareentity_mailshare_id);
--
-- Name: mailshareentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX mailshareentity_entity_id_fkey ON mailshareentity (mailshareentity_entity_id);
--
-- Name: ogroup_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX ogroup_usercreate_fkey ON ogroup (ogroup_usercreate);
--
-- Name: ogroup_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX ogroup_domain_id_fkey ON ogroup (ogroup_domain_id);
--
-- Name: ogroup_organizationalchart_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX ogroup_organizationalchart_id_fkey ON ogroup (ogroup_organizationalchart_id);
--
-- Name: ogroup_parent_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX ogroup_parent_id_fkey ON ogroup (ogroup_parent_id);
--
-- Name: ogroup_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX ogroup_userupdate_fkey ON ogroup (ogroup_userupdate);
--
-- Name: ogrouplink_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX ogrouplink_entity_id_fkey ON ogrouplink (ogrouplink_entity_id);
--
-- Name: ogrouplink_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX ogrouplink_usercreate_fkey ON ogrouplink (ogrouplink_usercreate);
--
-- Name: ogrouplink_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX ogrouplink_domain_id_fkey ON ogrouplink (ogrouplink_domain_id);
--
-- Name: ogrouplink_ogroup_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX ogrouplink_ogroup_id_fkey ON ogrouplink (ogrouplink_ogroup_id);
--
-- Name: ogrouplink_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX ogrouplink_userupdate_fkey ON ogrouplink (ogrouplink_userupdate);
--
-- Name: obmbookmark_user_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX obmbookmark_user_id_fkey ON obmbookmark (obmbookmark_user_id);
--
-- Name: obmbookmarkproperty_bookmark_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX obmbookmarkproperty_bookmark_id_fkey ON obmbookmarkproperty (obmbookmarkproperty_bookmark_id);
--
-- Name: obmbookmarkentity_obmbookmark_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX obmbookmarkentity_obmbookmark_id_fkey ON obmbookmarkentity (obmbookmarkentity_obmbookmark_id);
--
-- Name: obmbookmarkentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX obmbookmarkentity_entity_id_fkey ON obmbookmarkentity (obmbookmarkentity_entity_id);
--
-- Name: ogroupentity_ogroup_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX ogroupentity_ogroup_id_fkey ON ogroupentity (ogroupentity_ogroup_id);
--
-- Name: ogroupentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX ogroupentity_entity_id_fkey ON ogroupentity (ogroupentity_entity_id);
--
-- Name: organizationalchart_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX organizationalchart_usercreate_fkey ON organizationalchart (organizationalchart_usercreate);
--
-- Name: organizationalchart_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX organizationalchart_domain_id_fkey ON organizationalchart (organizationalchart_domain_id);
--
-- Name: organizationalchart_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX organizationalchart_userupdate_fkey ON organizationalchart (organizationalchart_userupdate);
--
-- Name: organizationalchartentity_organizationalchart_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX organizationalchartentity_organizationalchart_id_fkey ON organizationalchartentity (organizationalchartentity_organizationalchart_id);
--
-- Name: organizationalchartentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX organizationalchartentity_entity_id_fkey ON organizationalchartentity (organizationalchartentity_entity_id);
--
-- Name: parentdeal_technicalmanager_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX parentdeal_technicalmanager_id_fkey ON parentdeal (parentdeal_technicalmanager_id);
--
-- Name: parentdeal_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX parentdeal_domain_id_fkey ON parentdeal (parentdeal_domain_id);
--
-- Name: parentdeal_marketingmanager_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX parentdeal_marketingmanager_id_fkey ON parentdeal (parentdeal_marketingmanager_id);
--
-- Name: parentdeal_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX parentdeal_usercreate_fkey ON parentdeal (parentdeal_usercreate);
--
-- Name: parentdeal_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX parentdeal_userupdate_fkey ON parentdeal (parentdeal_userupdate);
--
-- Name: parentdealentity_parentdeal_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX parentdealentity_parentdeal_id_fkey ON parentdealentity (parentdealentity_parentdeal_id);
--
-- Name: parentdealentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX parentdealentity_entity_id_fkey ON parentdealentity (parentdealentity_entity_id);
--
-- Name: payment_paymentkind_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX payment_paymentkind_id_fkey ON payment (payment_paymentkind_id);
--
-- Name: payment_account_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX payment_account_id_fkey ON payment (payment_account_id);
--
-- Name: payment_company_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX payment_company_id_fkey ON payment (payment_company_id);
--
-- Name: payment_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX payment_domain_id_fkey ON payment (payment_domain_id);
--
-- Name: payment_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX payment_usercreate_fkey ON payment (payment_usercreate);
--
-- Name: payment_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX payment_userupdate_fkey ON payment (payment_userupdate);
--
-- Name: paymententity_payment_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX paymententity_payment_id_fkey ON paymententity (paymententity_payment_id);
--
-- Name: paymententity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX paymententity_entity_id_fkey ON paymententity (paymententity_entity_id);
--
-- Name: paymentinvoice_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX paymentinvoice_userupdate_fkey ON paymentinvoice (paymentinvoice_userupdate);
--
-- Name: paymentinvoice_invoice_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX paymentinvoice_invoice_id_fkey ON paymentinvoice (paymentinvoice_invoice_id);
--
-- Name: paymentinvoice_payment_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX paymentinvoice_payment_id_fkey ON paymentinvoice (paymentinvoice_payment_id);
--
-- Name: paymentinvoice_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX paymentinvoice_usercreate_fkey ON paymentinvoice (paymentinvoice_usercreate);
--
-- Name: paymentkind_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX paymentkind_domain_id_fkey ON paymentkind (paymentkind_domain_id);
--
-- Name: phone_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX phone_entity_id_fkey ON phone (phone_entity_id);
--
-- Name: profileentity_profile_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX profileentity_profile_id_fkey ON profileentity (profileentity_profile_id);
--
-- Name: profileentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX profileentity_entity_id_fkey ON profileentity (profileentity_entity_id);
--
-- Name: profilemodule_profile_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX profilemodule_profile_id_fkey ON profilemodule (profilemodule_profile_id);
--
-- Name: profileproperty_profile_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX profileproperty_profile_id_fkey ON profileproperty (profileproperty_profile_id);
--
-- Name: profilesection_profile_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX profilesection_profile_id_fkey ON profilesection (profilesection_profile_id);
--
-- Name: project_type_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX project_type_id_fkey ON project (project_type_id);
--
-- Name: project_company_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX project_company_id_fkey ON project (project_company_id);
--
-- Name: project_deal_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX project_deal_id_fkey ON project (project_deal_id);
--
-- Name: project_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX project_domain_id_fkey ON project (project_domain_id);
--
-- Name: project_tasktype_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX project_tasktype_id_fkey ON project (project_tasktype_id);
--
-- Name: project_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX project_usercreate_fkey ON project (project_usercreate);
--
-- Name: project_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX project_userupdate_fkey ON project (project_userupdate);
--
-- Name: projectcv_cv_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX projectcv_cv_id_fkey ON projectcv (projectcv_cv_id);
--
-- Name: projectcv_project_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX projectcv_project_id_fkey ON projectcv (projectcv_project_id);
--
-- Name: projectclosing_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX projectclosing_usercreate_fkey ON projectclosing (projectclosing_usercreate);
--
-- Name: projectclosing_project_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX projectclosing_project_id_fkey ON projectclosing (projectclosing_project_id);
--
-- Name: projectclosing_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX projectclosing_userupdate_fkey ON projectclosing (projectclosing_userupdate);
--
-- Name: projectentity_project_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX projectentity_project_id_fkey ON projectentity (projectentity_project_id);
--
-- Name: projectentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX projectentity_entity_id_fkey ON projectentity (projectentity_entity_id);
--
-- Name: projectreftask_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX projectreftask_usercreate_fkey ON projectreftask (projectreftask_usercreate);
--
-- Name: projectreftask_tasktype_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX projectreftask_tasktype_id_fkey ON projectreftask (projectreftask_tasktype_id);
--
-- Name: projectreftask_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX projectreftask_userupdate_fkey ON projectreftask (projectreftask_userupdate);
--
-- Name: projecttask_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX projecttask_usercreate_fkey ON projecttask (projecttask_usercreate);
--
-- Name: projecttask_parenttask_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX projecttask_parenttask_id_fkey ON projecttask (projecttask_parenttask_id);
--
-- Name: projecttask_project_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX projecttask_project_id_fkey ON projecttask (projecttask_project_id);
--
-- Name: projecttask_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX projecttask_userupdate_fkey ON projecttask (projecttask_userupdate);
--
-- Name: projectuser_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX projectuser_usercreate_fkey ON projectuser (projectuser_usercreate);
--
-- Name: projectuser_project_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX projectuser_project_id_fkey ON projectuser (projectuser_project_id);
--
-- Name: projectuser_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX projectuser_userupdate_fkey ON projectuser (projectuser_userupdate);
--
-- Name: projectuser_user_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX projectuser_user_id_fkey ON projectuser (projectuser_user_id);
--
-- Name: publication_type_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX publication_type_id_fkey ON publication (publication_type_id);
--
-- Name: publication_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX publication_domain_id_fkey ON publication (publication_domain_id);
--
-- Name: publication_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX publication_usercreate_fkey ON publication (publication_usercreate);
--
-- Name: publication_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX publication_userupdate_fkey ON publication (publication_userupdate);
--
-- Name: publicationentity_publication_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX publicationentity_publication_id_fkey ON publicationentity (publicationentity_publication_id);
--
-- Name: publicationentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX publicationentity_entity_id_fkey ON publicationentity (publicationentity_entity_id);
--
-- Name: publicationtype_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX publicationtype_usercreate_fkey ON publicationtype (publicationtype_usercreate);
--
-- Name: publicationtype_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX publicationtype_domain_id_fkey ON publicationtype (publicationtype_domain_id);
--
-- Name: publicationtype_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX publicationtype_userupdate_fkey ON publicationtype (publicationtype_userupdate);
--
-- Name: rgroup_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX rgroup_usercreate_fkey ON rgroup (rgroup_usercreate);
--
-- Name: rgroup_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX rgroup_domain_id_fkey ON rgroup (rgroup_domain_id);
--
-- Name: rgroup_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX rgroup_userupdate_fkey ON rgroup (rgroup_userupdate);
--
-- Name: region_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX region_usercreate_fkey ON region (region_usercreate);
--
-- Name: region_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX region_domain_id_fkey ON region (region_domain_id);
--
-- Name: region_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX region_userupdate_fkey ON region (region_userupdate);
--
-- Name: resource_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX resource_domain_id_fkey ON resource (resource_domain_id);
--
-- Name: resource_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX resource_userupdate_fkey ON resource (resource_userupdate);
--
-- Name: resource_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX resource_usercreate_fkey ON resource (resource_usercreate);
--
-- Name: resource_rtype_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX resource_rtype_id_fkey ON resource (resource_rtype_id);
--
-- Name: resourceentity_resource_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX resourceentity_resource_id_fkey ON resourceentity (resourceentity_resource_id);
--
-- Name: resourceentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX resourceentity_entity_id_fkey ON resourceentity (resourceentity_entity_id);
--
-- Name: resourcegroup_resource_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX resourcegroup_resource_id_fkey ON resourcegroup (resourcegroup_resource_id);
--
-- Name: resourcegroup_rgroup_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX resourcegroup_rgroup_id_fkey ON resourcegroup (resourcegroup_rgroup_id);
--
-- Name: resourceitem_resourcetype_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX resourceitem_resourcetype_id_fkey ON resourceitem (resourceitem_resourcetype_id);
--
-- Name: resourceitem_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX resourceitem_domain_id_fkey ON resourceitem (resourceitem_domain_id);
--
-- Name: resourcetype_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX resourcetype_domain_id_fkey ON resourcetype (resourcetype_domain_id);
--
-- Name: resourcegroupentity_resourcegroup_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX resourcegroupentity_resourcegroup_id_fkey ON resourcegroupentity (resourcegroupentity_resourcegroup_id);
--
-- Name: resourcegroupentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX resourcegroupentity_entity_id_fkey ON resourcegroupentity (resourcegroupentity_entity_id);
--
-- Name: ssoticket_user_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX ssoticket_user_id_fkey ON ssoticket (ssoticket_user_id);
--
-- Name: service_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX service_entity_id_fkey ON service (service_entity_id);
--
-- Name: serviceproperty_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX serviceproperty_entity_id_fkey ON serviceproperty (serviceproperty_entity_id);
--
-- Name: subscription_reception_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX subscription_reception_id_fkey ON subscription (subscription_reception_id);
--
-- Name: subscription_contact_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX subscription_contact_id_fkey ON subscription (subscription_contact_id);
--
-- Name: subscription_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX subscription_domain_id_fkey ON subscription (subscription_domain_id);
--
-- Name: subscription_publication_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX subscription_publication_id_fkey ON subscription (subscription_publication_id);
--
-- Name: subscription_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX subscription_usercreate_fkey ON subscription (subscription_usercreate);
--
-- Name: subscription_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX subscription_userupdate_fkey ON subscription (subscription_userupdate);
--
-- Name: subscriptionentity_subscription_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX subscriptionentity_subscription_id_fkey ON subscriptionentity (subscriptionentity_subscription_id);
--
-- Name: subscriptionentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX subscriptionentity_entity_id_fkey ON subscriptionentity (subscriptionentity_entity_id);
--
-- Name: subscriptionreception_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX subscriptionreception_usercreate_fkey ON subscriptionreception (subscriptionreception_usercreate);
--
-- Name: subscriptionreception_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX subscriptionreception_domain_id_fkey ON subscriptionreception (subscriptionreception_domain_id);
--
-- Name: subscriptionreception_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX subscriptionreception_userupdate_fkey ON subscriptionreception (subscriptionreception_userupdate);
--
-- Name: taskevent_task_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX taskevent_task_id_fkey ON taskevent (taskevent_task_id);
--
-- Name: taskevent_event_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX taskevent_event_id_fkey ON taskevent (taskevent_event_id);
--
-- Name: tasktype_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX tasktype_usercreate_fkey ON tasktype (tasktype_usercreate);
--
-- Name: tasktype_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX tasktype_domain_id_fkey ON tasktype (tasktype_domain_id);
--
-- Name: tasktype_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX tasktype_userupdate_fkey ON tasktype (tasktype_userupdate);
--
-- Name: timetask_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX timetask_usercreate_fkey ON timetask (timetask_usercreate);
--
-- Name: timetask_projecttask_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX timetask_projecttask_id_fkey ON timetask (timetask_projecttask_id);
--
-- Name: timetask_tasktype_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX timetask_tasktype_id_fkey ON timetask (timetask_tasktype_id);
--
-- Name: timetask_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX timetask_userupdate_fkey ON timetask (timetask_userupdate);
--
-- Name: timetask_user_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX timetask_user_id_fkey ON timetask (timetask_user_id);
--
-- Name: group_manager_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX group_manager_id_fkey ON ugroup (group_manager_id);
--
-- Name: group_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX group_domain_id_fkey ON ugroup (group_domain_id);
--
-- Name: group_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX group_usercreate_fkey ON ugroup (group_usercreate);
--
-- Name: group_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX group_userupdate_fkey ON ugroup (group_userupdate);
--
-- Name: updated_user_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX updated_user_id_fkey ON updated (updated_user_id);
--
-- Name: updated_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX updated_domain_id_fkey ON updated (updated_domain_id);
--
-- Name: updatedlinks_user_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX updatedlinks_user_id_fkey ON updatedlinks (updatedlinks_user_id);
--
-- Name: updatedlinks_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX updatedlinks_domain_id_fkey ON updatedlinks (updatedlinks_domain_id);
--
-- Name: userentity_user_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX userentity_user_id_fkey ON userentity (userentity_user_id);
--
-- Name: userentity_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX userentity_entity_id_fkey ON userentity (userentity_entity_id);
--
-- Name: userobm_photo_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX userobm_photo_id_fkey ON userobm (userobm_photo_id);
--
-- Name: userobm_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX userobm_domain_id_fkey ON userobm (userobm_domain_id);
--
-- Name: userobm_host_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX userobm_host_id_fkey ON userobm (userobm_host_id);
--
-- Name: userobm_mail_server_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX userobm_mail_server_id_fkey ON userobm (userobm_mail_server_id);
--
-- Name: userobm_usercreate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX userobm_usercreate_fkey ON userobm (userobm_usercreate);
--
-- Name: userobm_userupdate_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX userobm_userupdate_fkey ON userobm (userobm_userupdate);
--
-- Name: userobmgroup_userobm_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX userobmgroup_userobm_id_fkey ON userobmgroup (userobmgroup_userobm_id);
--
-- Name: userobmgroup_group_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX userobmgroup_group_id_fkey ON userobmgroup (userobmgroup_group_id);
--
-- Name: userobmpref_user_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX userobmpref_user_id_fkey ON userobmpref (userobmpref_user_id);
--
-- Name: userobm_sessionlog_userobm_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX userobm_sessionlog_userobm_id_fkey ON userobm_sessionlog (userobm_sessionlog_userobm_id);
--
-- Name: website_entity_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX website_entity_id_fkey ON website (website_entity_id);
--
-- Name: of_usergroup_user_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX of_usergroup_user_id_fkey ON of_usergroup (of_usergroup_user_id);
--
-- Name: of_usergroup_group_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX of_usergroup_group_id_fkey ON of_usergroup (of_usergroup_group_id);
