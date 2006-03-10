<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : import_query.inc                                             //
//     - Desc : Import query File                                            //
// 2004-01-16 - Aliacom - Pierre Baudracco                                   //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Import Search query execution 
// Parameters :
//   - $import[]     : import search criteria
//     keys used     : name, contact
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_search($import, $p_new_order, $p_order_dir) {
  global $cdg_sql, $c_all, $ctu_sql_limit;
  
  $name = sql_search_text_parse($import["name"]);
  $dsrc = $import["datasource"];
  $market = $import["market"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);

  $timeupdate = sql_date_format($db_type, "import_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "import_timecreate", "timecreate");

  $where = "import_name $like '$name%'";

  if (($dsrc != $c_all) && ($dsrc != "")) { 
    $where .= " AND import_datasource_id='$dsrc'";
  }
  if (($market != $c_all) && ($market != "")) { 
    $where .= " AND import_marketingmanager_id='$market'";
  }

  $whereq = "WHERE $where";

  // ORDER construction
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "import_name";
  $orderq = " ORDER BY $order $p_order_dir";

  $query = "SELECT DISTINCT import_id as id,
    import_id,
    import_name,
    import_separator,
    import_enclosed,
    $timecreate,
    $timeupdate,
    A.userobm_login as usercreate,
    B.userobm_login as userupdate,
    datasource_name as import_datasource,
    C.userobm_lastname as market_lastname,
    C.userobm_firstname as market_firstname
  FROM
    Import LEFT JOIN DataSource ON import_datasource_id=datasource_id
    LEFT JOIN UserObm as A ON Import.import_usercreate=A.userobm_id
    LEFT JOIN UserObm as B ON Import.import_userupdate=B.userobm_id
    LEFT JOIN UserObm as C ON Import.import_marketingmanager_id=C.userobm_id
  $whereq
  $orderq
  $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("select count(*) FROM Import $whereq");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, "import_search()");
    $obm_q->query($query);
  }

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Import detail query execution
// Parameters:
//   - $id : deal id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type, "import_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "import_timecreate", "timecreate");

  $query = "SELECT *,
      $timecreate,
      $timeupdate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate,
      datasource_name,
      m.userobm_lastname as market_lastname,
      m.userobm_firstname as market_firstname
  FROM Import
       LEFT JOIN DataSource ON import_datasource_id = datasource_id
       LEFT JOIN UserObm as c ON import_usercreate=c.userobm_id
       LEFT JOIN UserObm as u ON import_userupdate=u.userobm_id
       LEFT JOIN UserObm as m ON import_marketingmanager_id=m.userobm_id
  WHERE import_id = '$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query) ;
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : import insertion
// Parameters:
//   - $import[] : import hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($import) {
  global $auth, $cdg_sql;

  $id = $import["id"];
  $name = $import["name"];
  $dsrc = $import["datasource"];
  $market = $import["market"];
  $sep = $import["sep"];
  $enc = $import["enclosed"];
  $desc = get_import_desc($import);

  $query = "INSERT INTO Import (import_timeupdate,
    import_timecreate,
    import_userupdate,
    import_usercreate,
    import_name,
    import_datasource_id,
    import_marketingmanager_id,
    import_separator,
    import_enclosed,
    import_desc)
  VALUES (null,
    '" . date("Y-m-d H:i:s") ."',
    null,
    '" . $auth->auth["uid"] . "',
    '$name',
    '$dsrc',
    '$market',
    '$sep',
    '$enc',
    '$desc')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Import Update query execution
// Parameters:
//   - $import[] : import hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_update($import) {
  global $auth, $cdg_sql;

  $id = $import["id"];
  $name = $import["name"];
  $dsrc = $import["datasource"];
  $market = $import["market"];
  $sep = $import["sep"];
  $enc = $import["enclosed"];
  $desc = get_import_desc($import);
  
  $query = "UPDATE Import SET import_timeupdate='". date("Y-m-d H:i:s")."',
    import_userupdate='".$auth->auth["uid"]."',
    import_name='$name',
    import_datasource_id='$dsrc',
    import_marketingmanager_id='$market',
    import_separator='$sep',
    import_enclosed='$enc',
    import_desc='$desc'
  WHERE import_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $p_id : import id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($p_id) {
  global $cdg_sql;

  // Delete the Import
  $query = "DELETE FROM Import WHERE import_id='$p_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Check if a import (except with id given) with the name given already exists
// Parameters:
//   - $name : name to search for
//   - $id   : import id to exclude
// Returns:
//   - true (if an import exists) or false
///////////////////////////////////////////////////////////////////////////////
function get_import_name_exists($name, $id="") {
  global $cdg_sql;

  if ($id != "") {
    $where_id = "AND import_id != '$id'";
  }

  $query = "SELECT import_id, import_name
    FROM Import
    WHERE import_name='$name'
      $where_id";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    return true;
  } else {
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Return the imports which matches the name or the datasource
// except the one given (update mode)
// Parameters:
//   - $import[] : import hash info : keys used : id, name, datasource
///////////////////////////////////////////////////////////////////////////////
function run_query_check_import($import) {
  global $cdg_sql;

  $id = $import["id"];
  $name = $import["name"];
  $dsrc = $import["datasource"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  if ($id != "") {
    $where_id = "import_id != '$id' and ";
  }

  // We search for a similar name (as same name is already excluded)
  $wname = "import_name $like '%$name%'";

  // We search for the same datasource
  $wdsrc = "import_datasource_id = '$dsrc'";

  $query = "SELECT DISTINCT
       import_id,
       import_name,
       import_datasource_id,
       datasource_name
     FROM Import
          LEFT JOIN DataSource ON import_datasource_id=datasource_id
     WHERE $where_id
       ($wname and $wdsrc)";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Import context checking (same import exists ?)
// Parameters:
//   - $id       : import id
//   - $import[] : import values
//     keys used : name, datasource
// Returns:
//   - Import Database object with list of similar imports
///////////////////////////////////////////////////////////////////////////////
function check_import_context($id, $import) {
  global $cdg_sql;

  // return the imports with similar name and same datasource
  $import_q = run_query_check_import($import);

  return $import_q;
}


///////////////////////////////////////////////////////////////////////////////
// Import Form Data checking and formatting
// Parameters:
//   - $id       : import id (empty on insertion)
//   - $import[] : values checked
//     keys used : name, datasource
///////////////////////////////////////////////////////////////////////////////
function check_data_form($id, $import) {
  global $l_import_exists, $l_fill_name;
  global $err_msg;

  $name = $import["name"];
  $dsrc = $import["datasource"];
  $file = $import["file"];

  // MANDATORY: Import name not empty
  if (trim($name) == "") {
    $err_msg = $l_fill_name;
    return false;
  }

  // MANDATORY: Import name unique
  if (get_import_name_exists($name, $id)) {
    $err_msg = "$l_import_exists ($name)";
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Get Import field mapping sorted like file fields order
// Parameters:
//   - $import_q : Import database result 
// Returns:
//   hash : $map[1]["label"] = "Company"
//        : $map[1]["field"] = comp_name
///////////////////////////////////////////////////////////////////////////////
function get_import_field_mapping($import_q) {

  $desc = $import_q->f("import_desc");
  eval ($desc);

  // Get the mapping field_num -> $field[]
  // $map[1][label] = "Company"
  // $map[1][field] = comp_name
  while (list($field, $el) = each($comp)) {
    global ${$el["label"]};
    $label = ${$el["label"]};
    $value = $el["value"];
    $default = $el["default"];
    if (($label != "") && ($value != "")) {
      $map["$value"]["label"] = $label;
      $map["$value"]["field"] = $field;
      $map["field"][$field] = $value;
    }
    if ($default != "") {
      $map["default"][$field] = $default;
    }
  }

  while (list($field, $el) = each($con)) {
    global ${$el["label"]};
    $label = ${$el["label"]};
    $value = $el["value"];
    $default = $el["default"];
    if (($label != "") && ($value != "")) {
      $map["$value"]["label"] = $label;
      $map["$value"]["field"] = $field;
      $map["field"][$field] = $value;
    }
    if ($default != "") {
      $map["default"][$field] = $default;
    }
  }

  // We sort mapping like file mapping
  ksort($map);

  return $map;
}


///////////////////////////////////////////////////////////////////////////////
// Get fields from a file row and return it in an array
// Parameters:
//   - $row      : row from file
//   - $sep      : field separator
//   - $enclosed : enclosed by char
// Returns:
//   hash : $row[1] = value
///////////////////////////////////////////////////////////////////////////////
function get_file_row_fields($row, $sep, $enclosed) {

  if ($sep == "tab") {
    $sep = "\t";
  }

  $regexp_sep = "/$enclosed$sep$enclosed/";

  // Delete the edges enclosed characters "f1";"f2";"f3" => f1";"f2";"f3
  if ($enclosed != "") {
    $pattern = "/^$enclosed{1}(.*)$enclosed{1}\$/";
    $line = preg_replace($pattern, "$1", $row);
  } else {
    $line = $row;
  }

  $tok = preg_split($regexp_sep, $line);
  while (list($num, $value) = each($tok)) {
    $idx = $num + 1;
    $rows[$idx]= "$value";
  }

  return $rows;
}


///////////////////////////////////////////////////////////////////////////////
// Return the mapping : database fields - content from row
// Parameters:
//   - $row : row direct hash
//   - $map : import field mapping
// Returns:
//   hash : $fields[comp_name] = $content
///////////////////////////////////////////////////////////////////////////////
function get_import_row_mapping($row, $map) {
  global $field_size;

  while (list($field, $num) = each ($map["field"])) {
    if ($num != "") {
      $num--; // $map mapping start at 1 and $row at 0
      $content = $row[$num];
      // if a max size is defined for the field, we truncate the field
      // exceptions for countries values that can be iso codes or ctry names
      // for ctry this is tested after passing ctry iso hash
      if ( ($field_size[$field] > 0)
	   && ($field != "comp_ctry")
	   && ($field != "con_ctry") ) {
	$fields[$field] = substr(trim($content), 0, $field_size[$field]);
      } else {
	$fields[$field] = trim($content);
      }
    }
  }

  return $fields;
}


///////////////////////////////////////////////////////////////////////////////
// Check if an import row is valid
// Parameters:
//   - $fields : row fields content
//   - $map    : import fields mapping
// Returns:
//   true if valid
///////////////////////////////////////////////////////////////////////////////
function is_row_valid($fields, $map) {

  if ( ($fields["comp_name"] == "") && ($map["default"]["comp_name"] == "") ) {
    return false;
  } else {
    return true;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if an import row has a contact
// Parameters:
//   - $fields : row fields content
// Returns:
//   true if valid
///////////////////////////////////////////////////////////////////////////////
function row_has_contact($fields) {

  // we consider the row has a contact if lastname, firstname or job title
  if ( ( (isset($fields["con_ln"])) && ($fields["con_ln"] != "") )
     || ( (isset($fields["con_fn"])) && ($fields["con_fn"] != "") )
     || ( (isset($fields["con_func"])) && ($fields["con_func"] != "") )
     || ( (isset($fields["con_tit"])) && ($fields["con_tit"] != "") ) ) {
    return true;
  } else {
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Return the existing companies similar to the one given
// Parameters:
//   - $fields : company Id not to return
//   - $map    : import mapping fields (to get default entries)
///////////////////////////////////////////////////////////////////////////////
function get_similar_company($fields, $map) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  $name = $fields["comp_name"];
  if ($name == "") {
    $name = $map["default"]["comp_name"];
  }
  $name = addslashes($name);
  $zip = addslashes($fields["comp_zip"]);
  $town = addslashes($fields["comp_town"]);

  $query = "SELECT distinct company_id, company_name, company_zipcode 
    FROM Company
    WHERE (company_name $like '$name%' OR company_aka $like '%$name%')
      AND (company_zipcode $like '$zip' OR company_town $like '$town')";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $cpt = 0;
  while ($obm_q->next_record()) {
    $comps[$cpt]["comp_id"] = $obm_q->f("company_id");
    $comps[$cpt]["comp_name"] = $obm_q->f("company_name");
    $comps[$cpt]["comp_zip"] = $obm_q->f("company_zipcode");
    $cpt++;
  }

  return $comps;
}


///////////////////////////////////////////////////////////////////////////////
// Company Insertion query execution
// Parameters:
//   - $import_q    : Import database result 
//   - $fields[]    : Company and Contact Entry's values
//   - $defaults[]  : Default values hash
//   - $countries[] : Country hash
//   - $cats1[]      : CompanyCategory1 hash
// Returns
//   Id of the company inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_import_company_insert(&$import_q, $fields, $defaults, &$countries, &$cats1) {
  global $auth, $cdg_sql, $field_size;

  $uid = $auth->auth["uid"];

  // Set default values when needed
  if (is_array($defaults)) {
    while (list($field, $value) = each ($defaults)) {
      if ($fields[$field] == "") {
	$fields[$field] = $value;
      }
    }
  }

  $num = addslashes($fields["comp_num"]);
  $archive = "0";
  $name = addslashes($fields["comp_name"]);
  $auto_aka = format_name($name, 0, true, true);
  $aka = $auto_aka;
  $sound = phonetic_key($name);

  $dsrc = $import_q->f("import_datasource_id");
  $market = $import_q->f("import_marketingmanager_id");

  $ad1 = addslashes($fields["comp_ad1"]);
  $ad2 = addslashes($fields["comp_ad2"]);
  $ad3 = addslashes($fields["comp_ad3"]);
  $zip = $fields["comp_zip"];
  $town = addslashes($fields["comp_town"]);
  $cdx = addslashes($fields["comp_cdx"]);
  // If a country name has been found we get the corresponding iso code
  if (isset($countries[$fields["comp_ctry"]])) {
    $ctry = $countries[$fields["comp_ctry"]];
    // else we get the entry as an iso code
  } else {
    $ctry = substr(trim($fields["comp_ctry"]), 0, $field_size["comp_ctry"]);
  }
  $phone = $fields["comp_pho"];
  $fax = $fields["comp_fax"];
  $web = $fields["comp_web"];
  $email = $fields["comp_mail"];
  $com = addslashes($fields["comp_com"]);

  $query = "INSERT INTO Company (company_timeupdate,
    company_timecreate,
    company_userupdate,
    company_usercreate,
    company_datasource_id,
    company_number,
    company_archive,
    company_name,
    company_aka,
    company_sound,
    company_marketingmanager_id,
    company_address1,
    company_address2,
    company_address3,
    company_zipcode,
    company_town,
    company_expresspostal,
    company_country_iso3166,
    company_phone,
    company_fax,
    company_web,
    company_email,
    company_comment)
  VALUES (null,
    '" . date("Y-m-d H:i:s") . "',
    null,
    '$uid',
    '$dsrc',
    '$num',
    '$archive',
    '$name',
    '$aka',
    '$sound',
    '$market',
    '$ad1',
    '$ad2',
    '$ad3',
    '$zip',
    '$town',
    '$cdx',
    '$ctry',
    '$phone',
    '$fax',
    '$web',
    '$email',
    '$com')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // Get the company Id
  $query = "SELECT company_id as id
      FROM Company
      WHERE company_name = '$name'
        and company_phone = '$phone'
        and company_address1 = '$ad1'
        and company_zipcode = '$zip'
        and company_town = '$town'";
  
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();
  $id = $obm_q->f("id");

  // Set the category if given
  $cat1 = $cats1[$fields["comp_cat1"]];
  if ($cat1 != "") {

    $query = "INSERT INTO CompanyCategory1Link (
        companycategory1link_company_id,
	companycategory1link_category_id)
      VALUES (
        '$id',
        '$cat1')";
    display_debug_msg($query, $cdg_sql);
    $obm_q->query($query);
  }
  
  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Company Update query execution
// Parameters:
//   - $import_q    : Import database result 
//   - $fields[]    : Company and Contact Entry's values
//   - $defaults[]  : Default values hash
//   - $countries[] : Country hash
//   - $cats1[]      : CompanyCategory1 hash
//   - $company_id  : Company id to update
///////////////////////////////////////////////////////////////////////////////
function run_query_import_company_update(&$import_q, $fields, $defaults, &$countries, &$cats1, $company_id) {
  global $auth, $cdg_sql, $field_size;

  $uid = $auth->auth["uid"];

  // Set default values when needed
  if (is_array($defaults)) {
    while (list($field, $value) = each ($defaults)) {
      if ($fields[$field] == "") {
	$fields[$field] = $value;
      }
    }
  }

  $num = addslashes($fields["comp_num"]);
  // Name should be the same (expect case), so we don't update aka or sound
  $name = addslashes($fields["comp_name"]);

  $dsrc = $import_q->f("import_datasource_id");
  $market = $import_q->f("import_marketingmanager_id");

  $ad1 = addslashes($fields["comp_ad1"]);
  $ad2 = addslashes($fields["comp_ad2"]);
  $ad3 = addslashes($fields["comp_ad3"]);
  $zip = $fields["comp_zip"];
  $town = addslashes($fields["comp_town"]);
  $cdx = addslashes($fields["comp_cdx"]);
  // If a country name has been found we get the corresponding iso code
  if (isset($countries[$fields["comp_ctry"]])) {
    $ctry = $countries[$fields["comp_ctry"]];
    // else we get the entry as an iso code
  } else {
    $ctry = substr(trim($fields["comp_ctry"]), 0, $field_size["comp_ctry"]);
  }
  $phone = $fields["comp_pho"];
  $fax = $fields["comp_fax"];
  $web = $fields["comp_web"];
  $email = $fields["comp_mail"];
  $com = addslashes($fields["comp_com"]);

  $query = "UPDATE Company SET
    company_timeupdate='". date("Y-m-d H:i:s") ."',
    company_userupdate='$uid',
    company_datasource_id='$dsrc',
    company_marketingmanager_id='$market',
    company_number='$num',
    company_name='$name',
    company_address1='$ad1',
    company_address2='$ad2',
    company_address3='$ad3',
    company_zipcode='$zip',
    company_town='$town',
    company_expresspostal='$cdx',
    company_country_iso3166='$ctry',
    company_phone='$phone',
    company_fax='$fax',
    company_web='$web',
    company_email='$email',
    company_comment='$com'
  WHERE company_id='$company_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // Set the category if given
  $cat1 = $cats1[$fields["comp_cat1"]];
  if ($cat1 != "") {

    $query = "DELETE
      FROM CompanyCategory1Link
      WHERE companycategory1link_company_id='$company_id'
        AND companycategory1link_category_id='$cat1'";
    display_debug_msg($query, $cdg_sql);
    $obm_q->query($query);
  
    $query = "INSERT INTO CompanyCategory1Link (
        companycategory1link_company_id,
        companycategory1link_category_id)
      VALUES (
          '$company_id',
          '$cat1')";
    display_debug_msg($query, $cdg_sql);
    $obm_q->query($query);
  }
  
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Return the existing contacts similar to the one given
// Parameters:
//   - $fields : contact Id not to return
//   - $cid    : contact company id
///////////////////////////////////////////////////////////////////////////////
function get_similar_contact($fields, $cid) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  $lname = addslashes($fields["con_ln"]);
  $fname = $fields["con_fn"];

  $query = "SELECT DISTINCT contact_id, contact_lastname, contact_firstname 
            FROM Contact
            WHERE contact_lastname $like '$lname%'
              AND contact_company_id = '$cid'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $cpt = 0;
  while ($obm_q->next_record()) {
    $cons[$cpt]["con_id"] = $obm_q->f("contact_id");
    $cons[$cpt]["con_lname"] = $obm_q->f("contact_lastname");
    $cons[$cpt]["con_fname"] = $obm_q->f("contact_firstname");
    $cpt++;
  }

  return $cons;
}


///////////////////////////////////////////////////////////////////////////////
// Contact Insertion query execution
// Parameters:
//   - $import_q    : Import database result 
//   - $comp_id     : Company Id
//   - $fields[]    : Company and Contact Entry's values
//   - $defaults[]  : Default values hash
//   - $countries[] : Country hash
//   - $kinds[]     : Kinds hash (from lang)
//   - $functions[] : Functions hash
///////////////////////////////////////////////////////////////////////////////
function run_query_import_contact_insert(&$import_q, $comp_id, $fields, $defaults, &$countries, &$kinds, &$functions) {
  global $auth, $cdg_sql;

  $uid = $auth->auth["uid"];
  $mailoks = array("X",'x','1',"1","Y","y");

  // Set default values when needed
  if (is_array($defaults)) {
    while (list($field, $value) = each ($defaults)) {
      if ($fields[$field] == "") {
	$fields[$field] = $value;
      }
    }
  }

  $dsrc = $import_q->f("import_datasource_id");
  $market = $import_q->f("import_marketingmanager_id");
  $lname = addslashes($fields["con_ln"]);
  $fname = addslashes($fields["con_fn"]);
  // If a lang has been found we get the corresponding kind
  if (isset($kinds[$fields["con_lang"]])) {
    $kind_id = $kinds[$fields["con_lang"]];
  } else {
    $kind_id = "0";
  }

  // If a function name has been found we get the corresponding id
  if (isset($functions[$fields["con_func"]])) {
    $function_id = $functions[$fields["con_func"]];
    // else we get the entry as an id
  } else if (($fields["con_func"] != "") && ($fields["con_func"] > 0)) {
    $function_id = $fields["con_func"];
  } else {
    $function_id = "0";
  }

  $title = addslashes($fields["con_tit"]);
  $ad1 = addslashes($fields["con_ad1"]);
  $ad2 = addslashes($fields["con_ad2"]);
  $ad3 = addslashes($fields["con_ad3"]);
  $zip = $fields["con_zip"];
  $town = addslashes($fields["con_town"]);
  $cdx = addslashes($fields["con_cdx"]);
  // If a country name has been found we get the corresponding iso code
  if (isset($countries[$fields["con_ctry"]])) {
    $ctry = $countries[$fields["con_ctry"]];
    // else we get the entry as an iso code
  } else {
    $ctry = trim($fields["con_ctry"]);
  }
  $phone = $fields["con_pho"];
  $hphone = $fields["con_hpho"];
  $mphone = $fields["con_mpho"];
  $fax = $fields["con_fax"];
  $email = $fields["con_mail"];
  $mailok = (in_array($fields["con_mailok"], $mailoks) ? '1' : '0');
  $com = addslashes($fields["con_com"]);
  $arch = '0';
  $priv = '0';

  $query = "INSERT INTO Contact (contact_timeupdate,
    contact_timecreate,
    contact_userupdate,
    contact_usercreate,
    contact_datasource_id,
    contact_company_id,
    contact_kind_id,
    contact_marketingmanager_id,
    contact_lastname,
    contact_firstname,
    contact_address1,
    contact_address2,
    contact_address3,
    contact_zipcode,
    contact_town,
    contact_expresspostal,
    contact_country_iso3166,
    contact_function_id,
    contact_title,
    contact_phone,
    contact_homephone,
    contact_mobilephone,
    contact_fax,
    contact_email,
    contact_mailing_ok,
    contact_archive,
    contact_privacy,
    contact_comment)
  VALUES (null,
    '" . date("Y-m-d H:i:s") . "',
    null,
    '$uid',
    '$dsrc',
    '$comp_id',
    '$kind_id',
    '$market',
    '$lname',
    '$fname',
    '$ad1',
    '$ad2',
    '$ad3',
    '$zip',
    '$town',
    '$cdx',
    '$ctry',
    '$function_id',
    '$title',
    '$phone',
    '$hphone',
    '$mphone',
    '$fax',
    '$email',
    '$mailok',
    '$arch',
    '$priv',
    '$com')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  
  if ($retour) {
    run_query_company_contact_number_update($comp_id);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Contact Update query execution
// Parameters:
//   - $import_q    : Import database result 
//   - $fields[]    : Company and Contact Entry's values
//   - $defaults[]  : Default values hash
//   - $countries[] : Country hash
//   - $kinds[]     : Kind hash
//   - $functions[] : Functions hash
//   - $contact_id  : Contact id to update
///////////////////////////////////////////////////////////////////////////////
function run_query_import_contact_update(&$import_q, $fields, $defaults, &$countries, &$kinds, &$functions, $contact_id) {
  global $auth, $cdg_sql;

  $uid = $auth->auth["uid"];
  $mailoks = array("X",'x','1',"1","Y","y");

  // Set default values when needed
  if (is_array($defaults)) {
    while (list($field, $value) = each ($defaults)) {
      if ($fields[$field] == "") {
	$fields[$field] = $value;
      }
    }
  }

  $dsrc = $import_q->f("import_datasource_id");
  $market = $import_q->f("import_marketingmanager_id");
  $lname = addslashes($fields["con_ln"]);
  $fname = addslashes($fields["con_fn"]);
  // If a lang has been found we get the corresponding kind
  if (isset($kinds[$fields["con_lang"]])) {
    $kind_id = $kinds[$fields["con_lang"]];
    $q_kind = "contact_kind_id='$kind_id',";
  }

  // If a function name has been found we get the corresponding id
  if (isset($functions[$fields["con_func"]])) {
    $function_id = $functions[$fields["con_func"]];
    // else we get the entry as an id
  } else if (($fields["con_func"] != "") && ($fields["con_func"] > 0)) {
    $function_id = $fields["con_func"];
    $q_function = "contact_function_id='$function_id',";
  }

  $title = addslashes($fields["con_tit"]);
  $ad1 = addslashes($fields["con_ad1"]);
  $ad2 = addslashes($fields["con_ad2"]);
  $ad3 = addslashes($fields["con_ad3"]);
  $zip = $fields["con_zip"];
  $town = addslashes($fields["con_town"]);
  $cdx = addslashes($fields["con_cdx"]);
  // If a country name has been found we get the corresponding iso code
  if (isset($countries[$fields["con_ctry"]])) {
    $ctry = $countries[$fields["con_ctry"]];
    // else we get the entry as an iso code
  } else {
    $ctry = trim($fields["con_ctry"]);
  }
  $phone = $fields["con_pho"];
  $hphone = $fields["con_hpho"];
  $mphone = $fields["con_mpho"];
  $fax = $fields["con_fax"];
  $email = $fields["con_mail"];
  $mailok = (in_array($fields["con_mailok"], $mailoks) ? '1' : '0');
  $com = addslashes($fields["con_com"]);
  $arch = '0';
  $priv = '0';

  $query = "UPDATE Contact SET
    contact_timeupdate='". date("Y-m-d H:i:s") ."',
    contact_userupdate='$uid',
    contact_datasource_id='$dsrc',
    $q_kind
    $q_function
    contact_marketingmanager_id='$market',
    contact_lastname='$lname',
    contact_firstname='$fname',
    contact_address1='$ad1',
    contact_address2='$ad2',
    contact_address3='$ad3',
    contact_zipcode='$zip',
    contact_town='$town',
    contact_expresspostal='$cdx',
    contact_country_iso3166='$ctry',
    contact_function_id='$function_id',
    contact_title='$title',
    contact_phone='$phone',
    contact_homephone='$hphone',
    contact_mobilephone='$mphone',
    contact_fax='$fax',
    contact_email='$email',
    contact_mailing_ok='$mailok',
    contact_archive='$arch',
    contact_privacy='$priv',
    contact_comment='$com'
  WHERE contact_id='$contact_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Return the country hash : countries["name"] = iso
///////////////////////////////////////////////////////////////////////////////
function get_import_country_hash() {
  global $cdg_sql;

  $ctry_q = run_query_global_country();

  while ($ctry_q->next_record()) {
    $name = $ctry_q->f("country_name");
    $iso = $ctry_q->f("country_iso3166");
    $countries[$name] = $iso;
  }

  return $countries;
}


///////////////////////////////////////////////////////////////////////////////
// Return the companycategory1 hash : cats["code"] = id
///////////////////////////////////////////////////////////////////////////////
function get_import_companycategory1_hash() {
  global $cdg_sql;

  $cat_q = run_query_companycategory1();

  while ($cat_q->next_record()) {
    $id = $cat_q->f("companycategory1_id");
    $code = $cat_q->f("companycategory1_code");
    $label = $cat_q->f("companycategory1_label");
    $cats[$code] = $id;
    $cats[$label] = $id;
  }

  return $cats;
}


///////////////////////////////////////////////////////////////////////////////
// Return the kind hash : kinds["lang"] = kind_id
///////////////////////////////////////////////////////////////////////////////
function get_import_kind_hash() {
  global $cdg_sql;

  $kind_q = run_query_import_kind();

  while ($kind_q->next_record()) {
    $lang = strtoupper($kind_q->f("kind_lang"));
    $id = $kind_q->f("kind_id");
    if (! isset($kinds[$lang])) {
      $kinds[$lang] = $id;
    }
  }

  return $kinds;
}


///////////////////////////////////////////////////////////////////////////////
// Return the contact function hash : functions["label"] = id
///////////////////////////////////////////////////////////////////////////////
function get_import_function_hash() {
  global $cdg_sql;

  $func_q = run_query_contactfunction();

  if (is_object($func_q)) {
    while ($func_q->next_record()) {
      $id = $func_q->f("contactfunction_id");
      $label = $func_q->f("contactfunction_label");
      $functions[$label] = $id;
    }
  }

  return $functions;
}


///////////////////////////////////////////////////////////////////////////////
// Contact: Kind select query execution
// Returns : DB object result with all kinds
///////////////////////////////////////////////////////////////////////////////
function run_query_import_kind() {
  global $cdg_sql;

  $query = "SELECT kind_id,
      kind_lang,
      kind_default
    FROM Kind
    ORDER BY kind_lang, kind_default DESC";

  display_debug_msg($query, $cdg_sql);
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


</script>
