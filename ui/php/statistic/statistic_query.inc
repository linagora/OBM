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
// OBM - File : statistic_query.inc                                          //
//     - Desc : statistic query File                                         //
// 2004-04-19 Rande Mehdi                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$          //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// statistic Contact Per responsible query execution
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function run_query_statistic_contact_manager() {
  global $cdg_sql;

  $multidomain = sql_multidomain('contact');

  $query = "SELECT userobm_id,
      userobm_lastname,
      userobm_firstname,
      COUNT(contact_id) as cont_stat 
    FROM Contact, UserObm
    WHERE contact_marketingmanager_id = userobm_id
      $multidomain
    GROUP BY contact_marketingmanager_id,
      userobm_id, userobm_lastname,userobm_firstname";

  display_debug_msg($query, $cdg_sql, 'run_query_statistic_contact_manager()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q; 
}


///////////////////////////////////////////////////////////////////////////////
// statistic Company Per responsible query execution
// Parametes:
///////////////////////////////////////////////////////////////////////////////
function run_query_statistic_company_manager() {
  global $cdg_sql;

  $multidomain = sql_multidomain('company');

  $query = "SELECT userobm_id,
      userobm_lastname,
      userobm_firstname,
      COUNT(company_id) as comp_stat 
    FROM Company, UserObm
    WHERE company_marketingmanager_id = userobm_id
      $multidomain
    GROUP BY company_marketingmanager_id,userobm_id,userobm_firstname,
      userobm_lastname";

  display_debug_msg($query, $cdg_sql, 'run_query_statistic_company_manager()');
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// statistic about category and country
// Parameters:
//   - $entity   : entity to display stats on
//   - $category : category to display stats on
///////////////////////////////////////////////////////////////////////////////
function run_query_statistic_entity_per_country_per_cat($entity, $category) {
  global $cdg_sql;

  $lang = get_lang();
  $table = ucfirst($entity);
  $entityTable = $table.'Entity';
  $entityId = $entity.'entity_entity_id';
  $entityEntityId = $entity.'entity_'.$entity.'_id';
  $multidomain = sql_multidomain($entity);

  $query = "SELECT
      country_iso3166,
      country_name,
      category_id,
      category_code,
      category_label,
      count(${entity}_id) as nb 
    FROM $table
      INNER JOIN $entityTable ON $entityEntityId =  ${entity}_id
      LEFT JOIN Address as ad1 ON $entityId=ad1.address_entity_id
        AND ad1.address_label = 'WORK;X-OBM-Ref1'
      LEFT JOIN Country as ctry1 ON ad1.address_country=ctry1.country_iso3166
           AND ctry1.country_lang='$lang'
      LEFT JOIN CategoryLink ON categorylink_category = '$category'
                               AND categorylink_entity_id = $entityId
      LEFT JOIN Category ON categorylink_category_id = category_id
    WHERE 1=1
      $multidomain
    GROUP BY country_iso3166,
      country_name,
      category_id,
      category_code,
      category_label
    ORDER BY country_iso3166, category_id";
  
  display_debug_msg($query, $cdg_sql, 'run_query_statistic_entity_per_country_per_cat()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// return Entity number
// Parameters:
//   - $entity : entity to search
///////////////////////////////////////////////////////////////////////////////
function get_entity_nb($entity) {
  global $cdg_sql;

  $table = ucfirst($entity);
  $multidomain = sql_multidomain("$entity");
  $query = "SELECT count(*) as nb
    FROM $table
    WHERE 1=1
      $multidomain";
  
  display_debug_msg($query, $cdg_sql, 'get_entity_nb()');
  $obm_q= new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q->f('nb');
}


///////////////////////////////////////////////////////////////////////////////
// return all the lists
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function run_query_statistic_get_lists() {
  global $cdg_sql;
 
  $multidomain = sql_multidomain('list');
  $query = "SELECT list_id, list_name
    FROM List 
    WHERE 1=1
      $multidomain
    ORDER BY list_name";
  
  display_debug_msg($query, $cdg_sql, 'run_query_statistic_get_lists()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// return the list query
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function run_query_statistic_get_list($id) {
  global $cdg_sql;
 
  $multidomain = sql_multidomain('list');
  $query = "SELECT *
    FROM List
    WHERE list_id = '$id'
      $multidomain";
  
  display_debug_msg($query, $cdg_sql, 'run_query_statistic_get_list()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// statistic about category and country
// Parameters:
//   - $obm_q    : the list of the entity to select
//   - $entity   : entity to display stats on
//   - $category : category to display stats on
///////////////////////////////////////////////////////////////////////////////
function run_query_statistic_selected_entity_per_country_per_cat($obm_q, $entity, $category) {
  global $cdg_sql;

  $lang = get_lang();
  $table = ucfirst($entity);
  $entityTable = $table.'Entity';
  $entityId = $entity.'entity_entity_id';
  $entityEntityId = $entity.'entity_'.$entity.'_id';
  $multidomain = sql_multidomain("$entity");
  
  if ($obm_q->nf()!= 0) {
    $where_entity = "AND ${entity}_id in(";
    while ($obm_q->next_record()) {
      $where_entity .= $coma.$obm_q->f("${entity}_id");
      $coma = ',';
    }
    $where_entity .= ')';
  }

  $query = "SELECT
      country_iso3166,
      country_name,
      category_id,
      category_code,
      category_label,
      count(${entity}_id) as nb 
    FROM $table
      INNER JOIN $entityTable ON $entityEntityId =  ${entity}_id
      LEFT JOIN Address as ad1 ON $entityId=ad1.address_entity_id
        AND ad1.address_label = 'WORK;X-OBM-Ref1'
      LEFT JOIN Country as ctry1 ON ad1.address_country=ctry1.country_iso3166
           AND ctry1.country_lang='$lang'
      LEFT JOIN CategoryLink ON categorylink_category = '$category'
                               AND categorylink_entity_id = $entityId
      LEFT JOIN Category ON categorylink_category_id = category_id
    WHERE 1=1
      $where_entity
      $multidomain
    GROUP BY country_iso3166,
      country_name,
      category_id,
      category_code,
      category_label
    ORDER BY country_iso3166, category_id";
  
  display_debug_msg($query, $cdg_sql, 'run_query_statistic_selected_entity_per_country_per_cat()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// return the contact date 
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function get_statistic_contact_date_total() {
  global $obmdb_dbtype, $db_type_mysql, $db_type_pgsql, $cdg_sql;

  $legend = array('Total');

  $multidomain = sql_multidomain('contact');

  // XXXXXX test a changer. requete doit etre ecrite 1 seule fois !
  if ($obmdb_dbtype == $db_type_mysql) {
    $query = "SELECT count(*) as nb,
        YEAR(contact_date) as year,
        MONTH(contact_date) as month
      FROM Contact
      WHERE contact_date !='0000-00-00'
        $multidomain
      GROUP BY YEAR(contact_date),
        MONTH(contact_date)
      ORDER BY YEAR(contact_date), MONTH(contact_date)";
  } elseif ($obmdb_dbtype == $db_type_pgsql) {
    $query = "SELECT count(*) as nb,
        extract (year from  contact_date) as year,
        extract(month from contact_date) as month 
      FROM Contact
      WHERE contact_date is not null
        $multidomain
      GROUP BY extract (year from contact_date),
               extract(month from contact_date)
      ORDER BY year ASC, month ASC
";
  }

  display_debug_msg($query, $cdg_sql, 'get_statistic_contact_date_total()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  $obm_q->next_record();
  $first_year = $obm_q->f('year');
  $first_month = $obm_q->f('month');
  while ($obm_q->next_record()) {
    $last_year = $obm_q->f('year');
    $last_month = $obm_q->f('month');
  }
  $end_month = 12;
  $obm_q->seek(0);

  for($year = $first_year; $year <= $last_year; $year++) {
    if ($year == $last_year) $end_month = $last_month;
    for($month = $first_month; $month <= $end_month; $month++) {
      $values[$year][$month] = 0;
    }
    $first_month = 1;
  }

  while ($obm_q->next_record()) {
    $year = $obm_q->f('year');
    $month = $obm_q->f('month');
    $nb = $obm_q->f('nb');
    $values[$year][$month] = $nb;
  }

  $data = array();
  $legend = array();
  $i = 0;
  foreach ($values as $year => $one_year) {
    foreach ($one_year as $month => $nb) {
      $total += $nb;
      $data[$i] = $total;
      if ($month == 1) {
	$legend[$i] = "$month/$year";
      } else {
	$legend[$i] = "$month";
      }
      $i++;
    }
  }

  return array (
     'data' => $data,
     'legend' => $legend,
  );
}

?>
