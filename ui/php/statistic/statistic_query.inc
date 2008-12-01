<?php
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
      LEFT JOIN Country ON ${entity}_country_iso3166 = country_iso3166
                           AND country_lang='$lang'
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
      LEFT JOIN Country ON ${entity}_country_iso3166 = country_iso3166
                           AND country_lang='$lang'
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
      WHERE contact_date !='0'
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
      GROUP BY extract (year from contact_date), extract(month from contact_date)";
  }
  display_debug_msg($query, $cdg_sql, 'get_statistic_contact_date_total()');
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  $data=array(); $i=0; $sum=0;
  $month=0;$year=0; 
  while ($obm_q->next_record()) {
    // Months without data
    if (($obm_q->f('month') != (($month % 12) + 1))) {
      while ($obm_q->f('month') != (($month % 12) + 1)) {
        $month = ($month % 12) + 1;

	if ($year == 0) {
           $year = $obm_q->f('year');
        } elseif ($month == 1) {
           $year += 1;
        } 
	if ($month == 1)
          $legend[$i] = $year;
        else
          $legend[$i] = '';
        $data[$i] = $sum;
        $i++;
      }
    }

    $sum +=  $obm_q->f('nb');
	if ($obm_q->f('month') == 1)
          $legend[$i] = $obm_q->f('year');
        else
          $legend[$i] = '';
    $data[$i] = $sum;

    $month = $obm_q->f('month');
    $year = $obm_q->f('year');
    $i++;
  }

  return array (
     'data' => $data,
     'legend' => $legend,
  );
}

?>
