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
// Parametes:
///////////////////////////////////////////////////////////////////////////////
function run_query_statistic_contact_per_resp() {
  global $cdg_sql;

  $query = "SELECT userobm_id,
      userobm_lastname,
      userobm_firstname,
      COUNT(contact_id) as cont_stat 
    FROM Contact, UserObm
    WHERE contact_marketingmanager_id = userobm_id
    GROUP BY contact_marketingmanager_id,
      userobm_id, userobm_lastname,userobm_firstname
   ";

  display_debug_msg($query, $cdg_sql, "run_query_statistic_contact_per_resp()");
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q; 
}


///////////////////////////////////////////////////////////////////////////////
// statistic Company Per responsible query execution
// Parametes:
///////////////////////////////////////////////////////////////////////////////
function run_query_statistic_company_per_resp() {
  global $cdg_sql;

  $query = "SELECT userobm_id,
      userobm_lastname,
      userobm_firstname,
      COUNT(company_id) as comp_stat 
    FROM Company, UserObm
    WHERE company_marketingmanager_id = userobm_id
    GROUP BY company_marketingmanager_id,userobm_id,userobm_firstname,
      userobm_lastname";

  display_debug_msg($query, $cdg_sql, "run_query_statistic_company_per_resp()");
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// statistic about category and country
// Parametes:
///////////////////////////////////////////////////////////////////////////////
function run_query_statistic_company_per_country_per_cat() {
  global $cdg_sql;

  $lang = get_lang();

  $query = "SELECT  
      country_iso3166,
      country_name,
      companycategory1_id,
      companycategory1_code,
      companycategory1_label,
      count(company_id) as nb 
    FROM Company
      LEFT JOIN Country ON company_country_iso3166 = country_iso3166
                           AND country_lang='$lang'
      LEFT JOIN CompanyCategory1Link ON companycategory1link_company_id = company_id
      LEFT JOIN CompanyCategory1 ON companycategory1_id = companycategory1link_category_id
    GROUP BY country_iso3166, country_name,
      companycategory1_id, companycategory1_code,companycategory1_label
    ORDER BY country_iso3166, companycategory1_id";
  
  display_debug_msg($query, $cdg_sql, "run_query_statistic_company_per_country_per_cat()");
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// return Company number
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function run_query_statistic_nb_company() {
  global $cdg_sql;
 
  $query = "SELECT count(*) as nb FROM Company";
  
  display_debug_msg($query, $cdg_sql, "run_query_statistic_nb_company()");
  $obm_q= new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q->f("nb");
}


///////////////////////////////////////////////////////////////////////////////
// return all the lists
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function run_query_statistic_get_lists() {
  global $cdg_sql;
 
  $query = "SELECT list_id, list_name FROM List ORDER BY list_name";
  
  display_debug_msg($query, $cdg_sql, "run_query_statistic_get_lists()");
  $obm_q= new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// return the list query
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function run_query_statistic_get_list($id) {
  global $cdg_sql;
 
  $query = "SELECT * FROM List WHERE list_id = '$id'";
  
  display_debug_msg($query, $cdg_sql, "run_query_statistic_get_list()");
  $obm_q= new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// statistic about category and country
// Parameters: the list of the company to select
///////////////////////////////////////////////////////////////////////////////
function run_query_statistic_selected_company_per_country_per_cat($obm_q) {
  global $cdg_sql;

  $lang = get_lang();
  if ($obm_q->nf()!= 0) { 
    $where = " where company_id in(";
    while ($obm_q->next_record()) {
      $where .= $coma.$obm_q->f("company_id");
      $coma = ",";
    }
    $where .= ")";
  }
  $query = "SELECT  
      country_iso3166,
      country_name,
      companycategory1_id,
      companycategory1_code,
      companycategory1_label,
      count(company_id) as nb 
    FROM Company
      LEFT JOIN Country ON company_country_iso3166 = country_iso3166
                           AND country_lang='$lang'
      LEFT JOIN CompanyCategory1Link ON companycategory1link_company_id = company_id
      LEFT JOIN CompanyCategory1 ON companycategory1_id = companycategory1link_category_id
    $where
    GROUP BY country_iso3166, country_name,
      companycategory1_id, companycategory1_code,companycategory1_label
    ORDER BY country_iso3166, companycategory1_id";
  
  display_debug_msg($query, $cdg_sql, "run_query_statistic_selected_company_per_country_per_cat()");
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// return the contact date 
// Parameters:
////////////////////////////////////////////////////////////////////////////////
function get_statistic_contact_date_total() {
  global $obmdb_dbtype, $db_type_mysql, $db_type_pgsql, $cdg_sql;

  $legend = array("Total");

  if ($obmdb_dbtype == $db_type_mysql) {
    $query = "select count(*) as nb, YEAR(contact_date) as year, MONTH(contact_date) as month from Contact where contact_date !='0' group by YEAR(contact_date), MONTH(contact_date) order by YEAR(contact_date) , MONTH(contact_date)";
  } elseif ($obmdb_dbtype == $db_type_pgsql) {
    $query = "select count(*) as nb, extract (year from  contact_date) as year, extract(month from contact_date) as month from Contact where contact_date is not null group by  extract (year from contact_date), extract(month from contact_date)";
  }
  display_debug_msg($query, $cdg_sql, "get_statistic_contact_date_total()");
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  $data=array(); $i=0; $sum=0;
  $month=0;$year=0; 
  while ($obm_q->next_record()) {
    // Months without data
    if (($obm_q->f("month") != (($month % 12) + 1))) {
      while ($obm_q->f("month") != (($month % 12) + 1)) {
        $month = ($month % 12) + 1;

	if ($year == 0) {
           $year = $obm_q->f("year");
        } elseif ($month == 1) {
           $year += 1;
        } 
	if ($month == 1)
          $legend[$i] = $year;
        else
          $legend[$i] = "";
        $data[$i] = $sum;
        $i++;
      }
    }

    $sum +=  $obm_q->f("nb");
	if ($obm_q->f("month") == 1)
          $legend[$i] = $obm_q->f("year");
        else
          $legend[$i] = "";
    $data[$i] = $sum;

    $month = $obm_q->f("month");
    $year = $obm_q->f("year");
    $i++;
  }

  return array (
     "data" => $data,
     "legend" => $legend,
  );
}

?>