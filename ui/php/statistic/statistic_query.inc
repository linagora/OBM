<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : statistic_query.inc                                          //
//     - Desc : statistic query File                                         //
// 2004-04-19 Rande Mehdi                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$          //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// statistic Contact Per responsible query execution                         //
// Parametes:
///////////////////////////////////////////////////////////////////////////////
function run_query_contact_per_resp() {
  global $cdg_sql;

  $query = "select userobm_id,
      userobm_lastname,
      userobm_firstname,
      COUNT(contact_id) as cont_stat 
    from Contact, UserObm
    where contact_marketingmanager_id = userobm_id
    group by contact_marketingmanager_id,
      userobm_id, userobm_lastname,userobm_firstname
   ";

  display_debug_msg($query, $cdg_sql);
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q; 
}


///////////////////////////////////////////////////////////////////////////////
// statistic Company Per responsible query execution                         //
// Parametes:
///////////////////////////////////////////////////////////////////////////////
function run_query_company_per_resp() {
  global $cdg_sql;

  $query = "select userobm_id,
      userobm_lastname,
      userobm_firstname,
      COUNT(company_id) as comp_stat 
    from Company, UserObm
    where company_marketingmanager_id = userobm_id
    group by company_marketingmanager_id,userobm_id,userobm_firstname,
      userobm_lastname";

  display_debug_msg($query, $cdg_sql);
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// statistic about category and country
// Parametes:
///////////////////////////////////////////////////////////////////////////////
function run_query_company_per_country_per_cat() {
  global $cdg_sql;

  $lang = get_lang();

  $query = "Select  
      country_iso3166,
      country_name,
      companycategory_id,
      companycategory_code,
      companycategory_label,
      count(company_id) as nb 
    from Company
      left join Country ON company_country_iso3166 = country_iso3166
                           and country_lang='$lang'
      left join CompanyCategoryLink ON companycategorylink_company_id = company_id
      left join CompanyCategory ON companycategory_id = companycategorylink_category_id
    group by country_iso3166, country_name,
      companycategory_id, companycategory_code,companycategory_label
    order by country_iso3166, companycategory_id";
  
  display_debug_msg($query, $cdg_sql);
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// return Company number
// Parametes:
///////////////////////////////////////////////////////////////////////////////
function run_query_nb_company() {
  global $cdg_sql;
 
  $query = "Select count(*) as nb from Company";
  
  display_debug_msg($query, $cdg_sql);
  $obm_q= new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q->f("nb");
}

///////////////////////////////////////////////////////////////////////////////
// return all the lists
// Parametes:
///////////////////////////////////////////////////////////////////////////////
function run_query_get_lists() {
  global $cdg_sql;
 
  $query = "Select list_id, list_name from List Order by list_name";
  
  display_debug_msg($query, $cdg_sql);
  $obm_q= new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// return the list query
// Parametes:
///////////////////////////////////////////////////////////////////////////////
function run_query_get_list($id) {
  global $cdg_sql;
 
  $query = "Select * from List where list_id = '$id'";
  
  display_debug_msg($query, $cdg_sql);
  $obm_q= new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Request a list of company Id with a list from list module
// Parametes: The query to make the list of company
///////////////////////////////////////////////////////////////////////////////
function run_query_get_selected_company($query,$id) {
  global $cdg_sql;

  if (preg_match('/(FROM (.*))( GROUP BY)?(?(3)|$)/Uis', $query, $matches)) { 
   $query_f = "select distinct(company_id) ". $matches[1];
   $or = "OR";
  }
  else {
    $query_f = "select distinct(company_id) from Contact 
     LEFT JOIN Company ON Contact.contact_company_id = Company.company_id where";
  }
  $query_c = "select distinct(contact_id) as id from ContactList
         LEFT JOIN Contact ON contactlist_contact_id=contact_id
         LEFT JOIN Company ON Contact.contact_company_id = Company.company_id
	 where
         contactlist_list_id='$id'";

  display_debug_msg($query_c, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query_c, 1);
  if($obm_q->nf() != 0) {
    $query_f .= "$or contact_id IN (";
    while($obm_q->next_record()) {
      $query_f .= $coma .$obm_q->f("id");
      $coma =",";
    }
    $query_f .= " )";
  }
  display_debug_msg($query_f, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query_f, 1);
  
  return $obm_q;
  
}
  
///////////////////////////////////////////////////////////////////////////////
// statistic about category and country
// Parametes: the list of the company to select
///////////////////////////////////////////////////////////////////////////////
function run_query_selected_company_per_country_per_cat($obm_q) {
  global $cdg_sql;
  $lang = get_lang();
  if($obm_q->nf()!= 0) { 
    $where = " where company_id in(";
    while($obm_q->next_record()) {
      $where .= $coma.$obm_q->f("company_id");
      $coma = ",";
    }
    $where .= ")";
  }
  $query = "Select  
      country_iso3166,
      country_name,
      companycategory_id,
      companycategory_code,
      companycategory_label,
      count(company_id) as nb 
    from Company
      left join Country ON company_country_iso3166 = country_iso3166
                           and country_lang='$lang'
      left join CompanyCategoryLink ON companycategorylink_company_id = company_id
      left join CompanyCategory ON companycategory_id = companycategorylink_category_id
    $where
    group by country_iso3166, country_name,
      companycategory_id, companycategory_code,companycategory_label
    order by country_iso3166, companycategory_id";
  
  display_debug_msg($query, $cdg_sql);
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}
</script>
