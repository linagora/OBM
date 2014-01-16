<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_ref_query.inc                                          //
//     - Desc : Referential Data Query File                                  //
// 2003-12-05 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Country insertion query construction and execution
// Parameters:
//   - $ctry : data source hash info : keys used : name
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_country_insert($ctry) {
  global $obm, $cdg_sql;
  $obm_q = new DB_OBM;

  $timecreate = date('Y-m-d H:i:s');
  $usercreate = sql_parse_id( $obm['uid']);
  $name = $ctry['ctry_name'];
  $iso = $ctry['ctry_code'];
  $lang = $ctry['ctry_lang'];
  $phone = $ctry['ctry_phone'];
  $domain_id = sql_parse_id( $obm['domain_id']);

  $query = "INSERT INTO Country (
      country_timecreate,
      country_usercreate,
      country_iso3166,
      country_name,
      country_lang,
      country_phone,
      country_domain_id
    ) VALUES (
     '$timecreate',
     '$usercreate',
     '".$obm_q->escape($iso)."',
     '".$obm_q->escape($name)."',
     '".$obm_q->escape($lang)."',
     '".$obm_q->escape($phone)."',
     $domain_id)";

  display_debug_msg($query, $cdg_sql, 'run_query_admin_ref_country_insert()');
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Country update query execution
// Parameters:
//   - $ctry : data source hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_country_update($ctry) {
  global $obm, $cdg_sql;
  $obm_q = new DB_OBM;

  $timeupdate = date('Y-m-d H:i:s');
  $userupdate = sql_parse_id( $obm['uid']);
  $name = $ctry['ctry_name'];
  $iso = $ctry['ctry_code'];
  $lang = $ctry['ctry_lang'];
  $phone = $ctry['ctry_phone'];
  $old_iso = $ctry['ctry_old_code'];
  $old_lang = $ctry['ctry_old_lang'];

  $multidomain = sql_multidomain('country');

  $query = "UPDATE Country SET
      country_iso3166='".$obm_q->escape($iso)."',
      country_name='".$obm_q->escape($name)."',
      country_lang='".$obm_q->escape($lang)."',
      country_phone='".$obm_q->escape($phone)."',
      country_timeupdate='$timeupdate',
      country_userupdate='$userupdate'
    WHERE
      country_iso3166='".$obm_q->escape($old_iso)."'
      AND country_lang='".$obm_q->escape($old_lang)."'
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_admin_ref_country_update()');
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Country deletion query execution
// Parameters:
//   - $ctry : data source hash info : keys used : old_iso, old_lang
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_country_delete($ctry) {
  global $cdg_sql;

  $iso = $ctry['ctry_code'];
  $lang = $ctry['ctry_lang'];
  $multidomain = sql_multidomain('country');

  $query = "DELETE FROM Country
    WHERE country_iso3166='$iso'
      AND country_lang='$lang'
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_admin_ref_country_delete()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Country - Company links query execution
// Parameters:
//   - $iso : country_iso3166
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_country_company_links($iso) {
  global $cdg_sql;

  $multidomain = sql_multidomain('company');
  $query = "SELECT company_id,
      company_name,
      address_country as company_country_iso3166
    FROM Company
    INNER JOIN CompanyEntity ON company_id = companyentity_company_id
    INNER JOIN Address ON address_entity_id = companyentity_entity_id
    WHERE address_country='$iso'
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_admin_ref_country_company_links()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Country - Contact links query execution
// Parameters:
//   - $iso : country_iso3166
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_country_contact_links($iso) {
  global $cdg_sql;

  $multidomain = sql_multidomain('contact');
  $query = "SELECT contact_id,
      contact_lastname,
      contact_firstname,
      address_country as contact_country_iso3166
    FROM Contact
    INNER JOIN ContactEntity ON contact_id = contactentity_contact_id
    INNER JOIN Address ON address_entity_id = contactentity_entity_id
    WHERE address_country='$iso'    
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_admin_ref_country_contact_links()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the name of a Country from its iso, lang
// Parameters:
//   - $iso  : country_iso3166
//   - $lang : country_lang
///////////////////////////////////////////////////////////////////////////////
function get_admin_ref_country_name($iso, $lang) {
  global $cdg_sql;

  $multidomain = sql_multidomain('country');
  $query = "SELECT country_name
    FROM Country
    WHERE country_iso3166='$iso'
      AND country_lang='$lang'
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_admin_ref_country_name()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f('country_name');
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Data Source insertion query construction and execution
// Parameters:
//   - $dsrc : data source hash info : keys used : name
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_datasource_insert($dsrc) {
  global $obm, $cdg_sql;

  $timecreate = date('Y-m-d H:i:s');
  $usercreate = sql_parse_id( $obm['uid']);
  $name = $dsrc['dsrc_name'];
  $domain_id = $obm['domain_id'];
  $query = "INSERT INTO DataSource (
      datasource_timecreate,
      datasource_usercreate,
      datasource_name,
      datasource_domain_id
    )
    VALUES (
      '$timecreate',
      $usercreate,
      '".$obm_q->escape($name)."',
      $domain_id
    )";

  display_debug_msg($query, $cdg_sql, 'run_query_admin_ref_datasource_insert()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Data Source update query execution
// Parameters:
//   - $dsrc : data source hash info : keys used : act, name
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_datasource_update($dsrc) {
  global $obm, $cdg_sql;

  $timeupdate = date('Y-m-d H:i:s');
  $userupdate = sql_parse_id( $obm['uid']);
  $id = sql_parse_id( $dsrc['dsrc'], true);
  $name = $dsrc['dsrc_name'];
  $multidomain = sql_multidomain('datasource');

  $query = "UPDATE DataSource SET
      datasource_name='$name',
      datasource_timeupdate='$timeupdate',
      datasource_userupdate=$userupdate
    WHERE
      datasource_id $id 
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_admin_ref_datasource_update()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Data Source deletion query execution
// Parameters:
//   - $dsrc : data source hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_datasource_delete($dsrc) {
  global $cdg_sql;
  $multidomain = sql_multidomain('datasource');

  $id = sql_parse_id( $dsrc['dsrc'], true);

  $query = "DELETE FROM DataSource WHERE datasource_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_admin_ref_datasource_delete()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Data Source - Company links query execution
// Parameters:
//   - $dsrc : data source hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_datasource_company_links($dsrc) {
  global $cdg_sql;

  $id = sql_parse_id( $dsrc['dsrc'], true);
  $multidomain = sql_multidomain('company');

  $query = "SELECT company_id,
      company_name,
      company_datasource_id
    FROM Company
    WHERE company_datasource_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_admin_ref_datasource_company_links()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Data Source - Contact links query execution
// Parameters:
//   - $dsrc : data source hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_datasource_contact_links($dsrc) {
  global $cdg_sql;

  $multidomain = sql_multidomain('contact');
  $id = sql_parse_id( $dsrc['dsrc'], true);

  $query = "SELECT contact_id,
      contact_lastname,
      contact_firstname,
      contact_datasource_id
    FROM Contact
    WHERE contact_datasource_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_admin_ref_datasource_contact_links()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the name of a Data Source from its id
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_admin_ref_datasource_name($id) {
  global $cdg_sql;
  $multidomain = sql_multidomain('datasource');
  $id = sql_parse_id( $id, true);
  $query = "SELECT datasource_name
    FROM DataSource
    WHERE datasource_id $id 
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_admin_ref_datasource_name()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f('datasource_name');
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Task Type insertion query construction and execution
// Parameters:
//   - $tt : task type hash info : keys used : label, internal
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_tasktype_insert($tt) {
  global $obm, $cdg_sql;
  $obm_q = new DB_OBM;

  $timecreate = date('Y-m-d H:i:s');
  $usercreate = sql_parse_id( $obm['uid']);
  $code = $tt['tt_code'];
  $label = $tt['tt_label'];
  $int = $tt['internal'];
  $domain_id = sql_parse_id( $obm['domain_id']);
  $query = "INSERT INTO TaskType (
      tasktype_timecreate,
      tasktype_usercreate,
      tasktype_internal,
      tasktype_code,
      tasktype_label,
      tasktype_domain_id
    ) VALUES (
      '$timecreate',
      $usercreate,
      '".$obm_q->escape($int)."',
      '".$obm_q->escape($code)."',
      '".$obm_q->escape($label)."',
      $domain_id
    )";

  display_debug_msg($query, $cdg_sql, 'run_query_admin_ref_tasktype_insert()');
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Task Type update query execution
// Parameters:
//   - $tt : task type hash info : keys used : tasktype, label, int
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_tasktype_update($tt) {
  global $obm, $cdg_sql;
  $obm_q = new DB_OBM;

  $multidomain = sql_multidomain('tasktype');
  $timeupdate = date('Y-m-d H:i:s');
  $userupdate = sql_parse_id( $obm['uid']);
  $id = sql_parse_id( $tt['tasktype'], true);
  $code = $tt['tt_code'];
  $label = $tt['tt_label'];

  $query = "UPDATE TaskType SET
      tasktype_code='".$obm_q->escape($code)."',
      tasktype_label='".$obm_q->escape($label)."',
      tasktype_timeupdate='$timeupdate',
      tasktype_userupdate=$userupdate
    WHERE
      tasktype_id $id 
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_admin_ref_tasktype_update()');
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Task Type deletion query execution
// Parameters:
//   - $tt : task type hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_tasktype_delete($tt) {
  global $cdg_sql;

  $id = $tt['tasktype'];
  $multidomain = sql_multidomain('tasktype');

  $query = "DELETE FROM TaskType WHERE tasktype_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_admin_ref_tasktype_delete()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Task Type - Deal links query execution
// Parameters:
//   - $tt : task type hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_tasktype_deal_links($tt) {
  global $cdg_sql;

  $id = sql_parse_id( $tt['tasktype_id'], true);
  $multidomain = sql_multidomain('deal');

  $query = "SELECT deal_id,
      deal_label,
      deal_tasktype_id
    FROM Deal
    WHERE deal_tasktype_id $id 
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_admin_ref_tasktype_deal_links()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Task Type - Project links query execution
// Parameters:
//   - $tt : task type hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_tasktype_project_links($tt) {
  global $cdg_sql;

  $id = sql_parse_id( $tt['tasktype_id'], true);
  $multidomain = sql_multidomain('project');

  $query = "SELECT project_id,
      project_name,
      project_tasktype_id
    FROM Project
    WHERE project_tasktype_id $id 
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_admin_ref_tasktype_project_links()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Task Type - TimeTask links query execution
// Parameters:
//   - $tt : task type hash info : keys used : id
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_ref_tasktype_timetask_links($tt) {
  global $cdg_sql;

  $id = sql_parse_id( $tt['tasktype_id'], true);
  $multidomain = sql_multidomain('userobm');

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $timedate = sql_date_format($db_type, 'timetask_date', 'date');

  $query = "SELECT timetask_id,
      timetask_user_id,
      $timedate,
      userobm_lastname as lastname,
      userobm_firstname as firstname,
      timetask_tasktype_id
    FROM TimeTask
         LEFT JOIN UserObm ON timetask_user_id=userobm_id
    WHERE timetask_tasktype_id $id 
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_admin_ref_tasktype_timetask_links()');
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of a Task Type from its id
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_admin_ref_tasktype_label($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('tasktype');
  $id = sql_parse_id( $id, true);
  $query = "SELECT tasktype_label
    FROM TaskType
    WHERE tasktype_id $id 
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_admin_ref_tasktype_label()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f('tasktype_label');
  return $retour;
}

?>
