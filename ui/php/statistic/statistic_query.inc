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

</script>
