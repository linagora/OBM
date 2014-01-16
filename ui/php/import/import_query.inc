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
///////////////////////////////////////////////////////////////////////////////
function run_query_import_search($import) {
  global $cdg_sql, $c_all, $ctu_sql_limit;
  
  $sql_order_dir = $company["sql_order_dir"];
  $sql_order_field = $company["sql_order_field"];
  $multidomain = sql_multidomain("import");
  
  $name = sql_search_text_parse($import["name"]);
  $dsrc = $import["datasource"];
  $market = $import["market"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);

  $timeupdate = sql_date_format($db_type, "import_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "import_timecreate", "timecreate");

  $where = "import_name $like '".$obm_q->escape($name)."%'";

  if (($dsrc != $c_all) && ($dsrc != "")) { 
    $dsrc = sql_parse_id($dsrc, true);
    $where .= " AND import_datasource_id $dsrc ";
  }
  if (($market != $c_all) && ($market != "")) { 
    $market = sql_parse_id($market, true);
    $where .= " AND import_marketingmanager_id $market ";
  }

  $whereq = "WHERE $where $multidomain";

  // ORDER construction
  $order = (strcmp($sql_order_field,"") != 0) ? $sql_order_field : "import_name";
  $orderq = " ORDER BY $order $sql_order_dir";

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
    display_debug_msg($query, $cdg_sql, "run_query_import_search()");
    $obm_q->query($query);
  }

  display_debug_msg($query, $cdg_sql, "run_query_import_search()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Import detail query execution
// Parameters:
//   - $id : deal id
///////////////////////////////////////////////////////////////////////////////
function run_query_import_detail($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $multidomain = sql_multidomain("import");
  $id = sql_parse_id($id, true);
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
  WHERE import_id $id 
    $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_import_detail()");
  $obm_q->query($query) ;
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : import insertion
// Parameters:
//   - $import[] : import hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_import_insert($import) {
  global $cdg_sql, $obm;

  $obm_q = new DB_OBM;
  $domain_id = sql_parse_id($obm["domain_id"]);
  $id = sql_parse_id($import["import_id"]);
  $name = $import["name"];
  $dsrc = sql_parse_id($import["datasource"]);
  $market = sql_parse_id($import["market"]);
  $sep = $import["sep"];
  $enc = $import["enclosed"];
  $desc = get_import_desc($import);

  $query = "INSERT INTO Import (
    import_domain_id,
    import_timeupdate,
    import_timecreate,
    import_userupdate,
    import_usercreate,
    import_name,
    import_datasource_id,
    import_marketingmanager_id,
    import_separator,
    import_enclosed,
    import_desc)
  VALUES (
    $domain_id,
    null,
    '" . date("Y-m-d H:i:s") ."',
    null,
    " . $obm["uid"] . ",
    '".$obm_q->escape($name)."',
    $dsrc,
    $market,
    '".$obm_q->escape($sep)."',
    '".$obm_q->escape($enc)."',
    '".$obm_q->escape($desc)."')";

  display_debug_msg($query, $cdg_sql, "run_query_import_insert()");
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Import Update query execution
// Parameters:
//   - $import[] : import hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_import_update($import) {
  global $obm, $cdg_sql;

  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain("import");
  $id = sql_parse_id($import["import_id"], true);
  $name = $import["name"];
  $dsrc = sql_parse_id($import["datasource"]);
  $market = sql_parse_id($import["market"]);
  $sep = $import["sep"];
  $enc = $import["enclosed"];
  $desc = get_import_desc($import);

  $query = "UPDATE Import SET import_timeupdate='". date("Y-m-d H:i:s")."',
    import_userupdate=".$obm["uid"].",
    import_name='".$obm_q->escape($name)."',
    import_datasource_id=$dsrc,
    import_marketingmanager_id=$market,
    import_separator='".$obm_q->escape($sep)."',
    import_enclosed='".$obm_q->escape($enc)."',
    import_desc='".$obm_q->escape($desc)."'
  WHERE import_id $id 
    $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_import_update()");
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $p_id : import id
///////////////////////////////////////////////////////////////////////////////
function run_query_import_delete($p_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("import");
  $p_id = sql_parse_id($p_id, true);
  // Delete the Import
  $query = "DELETE
    FROM Import
    WHERE import_id $p_id $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_import_delete()");
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

  $multidomain = sql_multidomain("import");

  if ($id != "") {
    $id = sql_parse_id($id, true, true);
    $where_id = "AND import_id $id ";
  }

  $query = "SELECT import_id, import_name
    FROM Import
    WHERE import_name='$name'
      $where_id
      $multidomain";

  display_debug_msg($query, $cdg_sql, "get_import_name_exists()");
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
function run_query_import_check($import) {
  global $cdg_sql;

  $multidomain = sql_multidomain("import");
  $id = sql_parse_id($import["import_id"], true, true);
  $name = $import["name"];
  $dsrc = sql_parse_id($import["datasource"], true);

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  if ($id != "") {
    $where_id = "AND import_id $id ";
  }

  // We search for a similar name (as same name is already excluded)
  $wname = "import_name $like '%$name%'";

  // We search for the same datasource
  $wdsrc = "import_datasource_id $dsrc ";

  $query = "SELECT DISTINCT
       import_id,
       import_name,
       import_datasource_id,
       datasource_name
     FROM Import
          LEFT JOIN DataSource ON import_datasource_id=datasource_id
     WHERE
       ($wname AND $wdsrc)
       $where_id
       $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_import_check()");
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
  $import_q = run_query_import_check($import);

  return $import_q;
}


///////////////////////////////////////////////////////////////////////////////
// Import Form Data checking and formatting
// Parameters:
//   - $id       : import id (empty on insertion)
//   - $import[] : values checked
//     keys used : name, datasource
///////////////////////////////////////////////////////////////////////////////
function check_import_data_form($id, $import) {
  global $l_import_exists, $l_fill_name;
  global $err;

  $name = $import["name"];
  $dsrc = $import["datasource"];
  $file = $import["file"];

  // MANDATORY: Import name not empty
  if (trim($name) == "") {
    $err["msg"] = $l_fill_name;
    return false;
  }

  // MANDATORY: Import name unique
  if (get_import_name_exists($name, $id)) {
    $err["msg"] = "$l_import_exists ($name)";
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
function get_import_file_row_fields($row, $sep, $enclosed) {

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
function import_is_row_valid($fields, $map) {

    return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if an import row has a contact
// Parameters:
//   - $fields : row fields content
// Returns:
//   true if valid
///////////////////////////////////////////////////////////////////////////////
function import_row_has_contact($fields) {

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
function get_import_similar_company($fields, $map, &$countries) {
  global $cdg_sql, $cgp_import_precision,$field_size;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $multidomain = sql_multidomain("company");

  $name = $fields["comp_name"];
  if ($name == "") {
    $name = $map["default"]["comp_name"];
  }

  if (isset($countries[$fields["comp_ctry"]])) {
    $ctry = $countries[$fields["comp_ctry"]];
  } else {
    $ctry = substr(trim($fields["comp_ctry"]), 0, $field_size["comp_ctry"]);
  }
  if ($ctry == "") {
    $ctry = "0";
  }
  $cgp_import_precision = 3;
  $short_name = substr($name, 0, $cgp_import_precision);
  $name = $obm_q->escape($name);
  $zip = $obm_q->escape($fields["comp_zip"]);
  $town = $obm_q->escape($fields["comp_town"]);
  if( $ctry == "") {
    $zip_query = "OR (address_zipcode $like '".$obm_q->escape($zip)."' OR address_town $like '".$obm_q->escape($town)."')";
  }
  $query = "SELECT distinct company_id, company_name, address_zipcode 
    FROM Company 
    INNER JOIN CompanyEntity on companyentity_company_id = company_id
    LEFT JOIN Address ON address_entity_id = companyentity_entity_id
    WHERE (company_name $like '$short_name%' OR company_aka $like '%".$obm_q->escape($name)."%')
      AND (address_country $like '$ctry' $zip_query)
      $multidomain";

  display_debug_msg($query, $cdg_sql, "get_import_similar_company()");
  $obm_q->query($query);
  $cpt_same = 0;
  $cpt_similar = 0;
  $comps = array();
  while ($obm_q->next_record()) {
    $db_id = $obm_q->f("company_id");
    $db_name = $obm_q->f("company_name");
    $db_zip = $obm_q->f("address_zipcode");
    if (($name == $db_name) && ($zip == $db_zip)) {
      $comps["same"][$cpt_same]["comp_id"] = $db_id;
      $comps["same"][$cpt_same]["comp_name"] = $db_name;
      $comps["same"][$cpt_same]["comp_zip"] = $db_zip;
      $cpt_same++;
    } else {
      $comps["similar"][$cpt_similar]["comp_id"] = $db_id;
      $comps["similar"][$cpt_similar]["comp_name"] = $db_name;
      $comps["similar"][$cpt_similar]["comp_zip"] = $db_zip;
      $cpt_similar++;
    }
  }

  // If there is a same company, we delete the similar ones (no use, cause pb)
/*  if (count($comps["same"]) > 0) {
    unset($comps["similar"]);
}*/
  return $comps;
}


///////////////////////////////////////////////////////////////////////////////
// Company Insertion query execution
// Parameters:
//   - $import_q    : Import database result 
//   - $fields[]    : Company and Contact Entry's values
//   - $defaults[]  : Default values hash
//   - $countries[] : Country hash
//   - $userdata[]  : Userdata hash array[company|contact][category][$cat_name]
// Returns
//   Id of the company inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_import_company_insert(&$import_q, $fields, $defaults, &$countries, &$userdata) {
  global $cdg_sql, $obm, $field_size;
  $obm_q = new DB_OBM;

  $obm_q = new DB_OBM;
  $uid = sql_parse_id($obm["uid"]);
  $domain_id = sql_parse_id($obm["domain_id"]);

  // Set default values when needed
  if (is_array($defaults)) {
    while (list($field, $value) = each ($defaults)) {
      if ($fields[$field] == "") {
	$fields[$field] = $value;
      }
    }
  }

  $num = $obm_q->escape($fields["comp_num"]);
  $archive = "0";
  $name = $obm_q->escape($fields["comp_name"]);
  $auto_aka = format_name($name, 0, true, true);
  $aka = $auto_aka;
  $sound = phonetic_key($name);

  $dsrc = sql_parse_id($import_q->f("import_datasource_id"));
  $market = sql_parse_id($import_q->f("import_marketingmanager_id"));

  $ad1 = $obm_q->escape($fields["comp_ad1"]);
  $ad2 = $obm_q->escape($fields["comp_ad2"]);
  $ad3 = $obm_q->escape($fields["comp_ad3"]);
  $zip = $fields["comp_zip"];
  $town = $obm_q->escape($fields["comp_town"]);
  $cdx = $obm_q->escape($fields["comp_cdx"]);
  // If a country name has been found we get the corresponding iso code
  if (isset($countries[$fields["comp_ctry"]])) {
    $ctry = $countries[$fields["comp_ctry"]];
    // else we get the entry as an iso code
  } else {
    $ctry = substr(trim($fields["comp_ctry"]), 0, $field_size["comp_ctry"]);
  }
  // If country still empty we set it to 0
  if ($ctry == "") {
    $ctry = "0";
  }
  $phone = $fields["comp_pho"];
  $fax = $fields["comp_fax"];
  $web = $fields["comp_web"];
  $email = $fields["comp_mail"];

  $coords['phones'][] = array('label' => 'WORK_VOICE', 'number' => $phone);
  $coords['phones'][] = array('label' => 'WORK_FAX', 'number' => $fax);
  $coords['emails'][] = array('label' => 'INTERNET', 'address' => $email);
  $coords['websites'][] = array('label' => 'URL', 'url' => $web);
  $coords['addresses'][] = array(
    'label' => 'HQ', 
    'street' => "$ad1\n$ad2\n$ad3", 
    'zipcode' => $zip, 
    'town' => $town, 
    'expresspostal' => $cdx,
    'country' => $ctry);

  $com = $obm_q->escape($fields["comp_com"]);

  $query = "INSERT INTO Company (
    company_domain_id,
    company_timeupdate,
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
    company_comment)
  VALUES (
    '$domain_id',
    null,
    '" . date("Y-m-d H:i:s") . "',
    null,
    $uid,
    $dsrc,
    '".$obm_q->escape($num)."',
    '$archive',
    '".$obm_q->escape($name)."',
    '".$obm_q->escape($aka)."',
    '".$obm_q->escape($sound)."',
    $market,
    '".$obm_q->escape($com)."')";

  display_debug_msg($query, $cdg_sql, "run_query_import_company_insert(1)");
  $retour = $obm_q->query($query);

  $id = $obm_q->lastid();

  if ($id > 0) {
    $entity_id = of_entity_insert('company', $id);
    // User data handling
    if (is_array($userdata["company"]["category"])) {
      foreach($userdata["company"]["category"] as $cat_name => $hash) {
	$param_values[$cat_name] = $hash[$fields["comp_$cat_name"]];
      }
      $ret = of_userdata_query_update("company", $id, $param_values);
    }
    run_query_entity_coords_update($entity_id, $coords);
  }

  return $id;
}


/**
 * run_query_entity_coords_update 
 * 
 * @param mixed $id 
 * @param mixed $params 
 * @access public
 * @return void
 */
function run_query_entity_coords_update($id, $params) {
  global $cdg_sql;
  $phones = $params['phones'];
  $addresses = $params['addresses'];
  $ims = $params['ims'];
  $websites = $params['websites'];
  $emails = $params['emails'];
  $obm_q = new DB_OBM;

  $query = "DELETE FROM Phone WHERE phone_entity_id = $id"; 
  display_debug_msg($query, $cdg_sql, 'run_query_entity_coords_update(phone)');
  $obm_q->query($query);
  if(is_array($phones)) {
    $cpt = array();
    foreach($phones as $phone) {
      if(trim($phone['number']) != '' ) {
        $phone['label'] = str_replace('_', ';', $phone['label']);
        $cpt[$phone['label']]++;
        $query = "INSERT INTO Phone (phone_entity_id, phone_number, phone_label) VALUES ($id, '".$obm_q->escape($phone[number])."', '$phone[label];X-OBM-Ref".$cpt[$phone['label']]."')";
        display_debug_msg($query, $cdg_sql, 'run_query_entity_coords_update(phone)');
        $obm_q->query($query);
      }
    }
  }
  $query = "DELETE FROM Address WHERE address_entity_id = $id"; 
  display_debug_msg($query, $cdg_sql, 'run_query_entity_coords_update(address)');
  $obm_q->query($query);  
  if(is_array($addresses)) {
    $cpt = array();
    foreach($addresses as $address) {
      if(trim($address['street']) != '' || (trim($address['country']) != '' && trim($address['country']) != 'none') || trim($address['zipcode']) != ''
         || trim($address['expresspostal']) != '') {
        if(trim($address['country']) == 'none') $address['country'] = '';
        $address['label'] = str_replace('_', ';', $address['label']);
        $cpt[$address['label']]++;
        $query = "INSERT INTO Address (
          address_entity_id,
          address_street,
          address_zipcode,
          address_town,
          address_expresspostal,
          address_country,
          address_label
        ) VALUES (
          $id, 
          '".$obm_q->escape($address[street])."',
          '".$obm_q->escape($address[zipcode])."',
          '".$obm_q->escape($address[town])."',
          '".$obm_q->escape($address[expresspostal])."',
          '".$obm_q->escape($address[country])."',
          '$address[label];X-OBM-Ref".$cpt[$address['label']]."'
        )";
        display_debug_msg($query, $cdg_sql, 'run_query_entity_coords_update(address)');
        $obm_q->query($query);
      }
    }
  }  
  $query = "DELETE FROM Website WHERE website_entity_id = $id"; 
  display_debug_msg($query, $cdg_sql, 'run_query_entity_coords_update(website)');
  $obm_q->query($query);  

  if(is_array($websites)) {
    $cpt = array();
    foreach($websites as $website) {
      if(trim($website['url']) != '' ) {
        $website['label'] = str_replace('_', ';', $website['label']);
        $cpt[$website['label']]++;
        $query = "INSERT INTO Website (website_entity_id, website_url, website_label) VALUES ($id, '".$obm_q->escape($website[url])."', '$website[label];X-OBM-Ref".$cpt[$website['label']]."')";
        display_debug_msg($query, $cdg_sql, 'run_query_entity_coords_update(website)');
        $obm_q->query($query);        
      }
    }
  }  

  $query = "DELETE FROM IM WHERE im_entity_id = $id"; 
  display_debug_msg($query, $cdg_sql, 'run_query_entity_coords_update(IM)');
  $obm_q->query($query);  

  if(is_array($ims)) {
    $cpt = array();
    foreach($ims as $im) {
      if(trim($im['address']) != '' ) {
        $query = "INSERT INTO IM (im_entity_id, im_address, im_protocol, im_label) VALUES ($id, '".$obm_q->escape($im[address])."', '".$obm_q->escape($im[protocol])."', '".$obm_q->escape($im[label])."')";
        display_debug_msg($query, $cdg_sql, 'run_query_entity_coords_update(IM)');
        $obm_q->query($query);        
      }
    }
  }    

  $query = "DELETE FROM Email WHERE email_entity_id = $id"; 
  display_debug_msg($query, $cdg_sql, 'run_query_entity_coords_update(Email)');
  $obm_q->query($query);  

  if(is_array($emails)) {
    $cpt = array();
    foreach($emails as $email) {
      if(trim($email['address']) != '' ) {
        $email['label'] = str_replace('_', ';', $email['label']);
        $cpt[$email['label']]++;
        $query = "INSERT INTO Email (email_entity_id, email_address, email_label) VALUES ($id, '".$obm_q->escape($email[address])."', '$email[label];X-OBM-Ref".$cpt[$email['label']]."')";
        display_debug_msg($query, $cdg_sql, 'run_query_entity_coords_update(Email)');
        $obm_q->query($query);        
      }
    }
  }      
}

///////////////////////////////////////////////////////////////////////////////
// Company Update query execution
// Parameters:
//   - $import_q    : Import database result 
//   - $fields[]    : Company and Contact Entry's values
//   - $defaults[]  : Default values hash
//   - $countries[] : Country hash
//   - $userdata[]  : Userdata hash array[company|contact][category][$cat_name]
//   - $company_id  : Company id to update
///////////////////////////////////////////////////////////////////////////////
function run_query_import_company_update(&$import_q, $fields, $defaults, &$countries, &$userdata, $company_id) {
  global $obm, $cdg_sql, $field_size;
  $obm_q = new DB_OBM;

  $obm_q = new DB_OBM;
  $uid = sql_parse_id($obm["uid"]);

  // Set default values when needed
  if (is_array($defaults)) {
    while (list($field, $value) = each ($defaults)) {
      if ($fields[$field] == "") {
	$fields[$field] = $value;
      }
    }
  }

  $num = $obm_q->escape($fields["comp_num"]);
  // Name should be the same (expect case), so we don't update aka or sound
  $name = $obm_q->escape($fields["comp_name"]);

  $dsrc = sql_parse_id($import_q->f("import_datasource_id"));
  $market = sql_parse_id($import_q->f("import_marketingmanager_id"));

  $ad1 = $obm_q->escape($fields["comp_ad1"]);
  $ad2 = $obm_q->escape($fields["comp_ad2"]);
  $ad3 = $obm_q->escape($fields["comp_ad3"]);
  $zip = $fields["comp_zip"];
  $town = $obm_q->escape($fields["comp_town"]);
  $cdx = $obm_q->escape($fields["comp_cdx"]);
  // If a country name has been found we get the corresponding iso code
  if (isset($countries[$fields["comp_ctry"]])) {
    $ctry = $countries[$fields["comp_ctry"]];
    // else we get the entry as an iso code
  } else {
    $ctry = substr(trim($fields["comp_ctry"]), 0, $field_size["comp_ctry"]);
  }
  // If country still empty we set it to 0
  if ($ctry == "") {
    $ctry = "0";
  }
  $phone = $fields["comp_pho"];
  $fax = $fields["comp_fax"];
  $web = $fields["comp_web"];
  $email = $fields["comp_mail"];


  $coords['phones'][] = array('label' => 'WORK_VOICE', 'number' => $phone);
  $coords['phones'][] = array('label' => 'WORK_FAX', 'number' => $fax);
  $coords['emails'][] = array('label' => 'INTERNET', 'address' => $email);
  $coords['websites'][] = array('label' => 'URL', 'url' => $web);
  $coords['addresses'][] = array(
    'label' => 'HQ', 
    'street' => "$ad1\n$ad2\n$ad3", 
    'zipcode' => $zip, 
    'town' => $town, 
    'expresspostal' => $cdx,
    'country' => $ctry);

  $com = $obm_q->escape($fields["comp_com"]);
  $comp_id = sql_parse_id($company_id, true);
  $query = "UPDATE Company SET
    company_timeupdate='". date("Y-m-d H:i:s") ."',
    company_userupdate=$uid,
    company_datasource_id=$dsrc,
    company_marketingmanager_id=$market,
    company_number='".$obm_q->escape($num)."',
    company_name='".$obm_q->escape($name)."',
    company_comment='".$obm_q->escape($com)."'
  WHERE company_id $comp_id ";

  display_debug_msg($query, $cdg_sql, "run_query_import_company_update()");
  $retour = $obm_q->query($query);
  $entity_id = of_entity_get('company', $company_id);
  run_query_entity_coords_update($entity_id, $coords);

  // User data handling
  if (is_array($userdata["company"]["category"])) {
    foreach($userdata["company"]["category"] as $cat_name => $hash) {
      $param_values[$cat_name] = $hash[$fields["comp_$cat_name"]];
    }
    $ret = of_userdata_query_update("company", $company_id, $param_values, false);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Return the existing contacts similar to the one given
// Parameters:
//   - $fields : contact Id not to return
//   - $cid    : contact company id
///////////////////////////////////////////////////////////////////////////////
function get_import_similar_contact($fields, $cid) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $cid = sql_parse_id($cid, true);
  $lname = $obm_q->escape($fields["con_ln"]);
  $fname = $fields["con_fn"];

  $query = "SELECT DISTINCT contact_id, contact_lastname, contact_firstname 
            FROM Contact
            WHERE contact_lastname $like '".$obm_q->escape($lname)."%'
              AND contact_company_id $cid ";

  display_debug_msg($query, $cdg_sql, "get_import_similar_contact()");
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
//   - $userdata[]  : Userdata hash array[company|contact][category][$cat_name]
///////////////////////////////////////////////////////////////////////////////
function run_query_import_contact_insert(&$import_q, $comp_id, $fields, $defaults, &$countries, &$kinds, &$functions, &$userdata) {
  global $obm, $cdg_sql;
  $obm_q = new DB_OBM;

  $obm_q = new DB_OBM;
  $uid = sql_parse_id($obm["uid"]);
  $domain_id = sql_parse_id($obm["domain_id"]);
  $mailoks = array("X",'x','1',"1","Y","y");

  // Set default values when needed
  if (is_array($defaults)) {
    while (list($field, $value) = each ($defaults)) {
      if ($fields[$field] == "") {
	$fields[$field] = $value;
      }
    }
  }

  $dsrc = sql_parse_id($import_q->f("import_datasource_id"));
  $market = sql_parse_id($import_q->f("import_marketingmanager_id"));
  $lname = $obm_q->escape($fields["con_ln"]);
  $fname = $obm_q->escape($fields["con_fn"]);
  // If a lang has been found we get the corresponding kind
  if (isset($kinds[strtoupper($fields["con_lang"])])) {
    $kind_id = $kinds[strtoupper($fields["con_lang"])];
  } else {
    $kind_id = "0";
  }
  $kind_id = sql_parse_id($kind_id);
  // If a function name has been found we get the corresponding id
  if (isset($functions[$fields["con_func"]])) {
    $function_id = $functions[$fields["con_func"]];
    // else we get the entry as an id
  } else if (($fields["con_func"] != "") && ($fields["con_func"] > 0)) {
    $function_id = $fields["con_func"];
  } else {
    $function_id = "0";
  }
  $function_id = sql_parse_id($function_id);
  $title = $obm_q->escape($fields["con_tit"]);
  $ad1 = $obm_q->escape($fields["con_ad1"]);
  $ad2 = $obm_q->escape($fields["con_ad2"]);
  $ad3 = $obm_q->escape($fields["con_ad3"]);
  $zip = $fields["con_zip"];
  $town = $obm_q->escape($fields["con_town"]);
  $cdx = $obm_q->escape($fields["con_cdx"]);
  // If a country name has been found we get the corresponding iso code
  if (isset($countries[$fields["con_ctry"]])) {
    $ctry = $countries[$fields["con_ctry"]];
    // else we get the entry as an iso code
  } else {
    $ctry = trim($fields["con_ctry"]);
  }
  // If country still empty we set it to 0
  if ($ctry == "") {
    $ctry = "0";
  }
  $phone = $fields["con_pho"];
  $hphone = $fields["con_hpho"];
  $mphone = $fields["con_mpho"];
  $fax = $fields["con_fax"];
  $email = $fields["con_mail"];

  $coords['phones'][] = array('label' => 'WORK_VOICE', 'number' => $phone);
  $coords['phones'][] = array('label' => 'HOME_VOICE', 'number' => $hphone);
  $coords['phones'][] = array('label' => 'CELL_VOICE', 'number' => $mphone);
  $coords['phones'][] = array('label' => 'WORK_FAX', 'number' => $fax);
  $coords['emails'][] = array('label' => 'INTERNET', 'address' => $email);
  $coords['addresses'][] = array(
    'label' => 'HQ', 
    'street' => "$ad1\n$ad2\n$ad3", 
    'zipcode' => $zip, 
    'town' => $town, 
    'expresspostal' => $cdx,
    'country' => $ctry);

  $mailok = (in_array($fields["con_mailok"], $mailoks) ? '1' : '0');
  $com = $obm_q->escape($fields["con_com"]);
  $arch = '0';
  $company_id = sql_parse_id($comp_id);
  $contact_addressbook_id = OBM_AddressBook::lookupPublicAddressBookIdFor($domain_id);
  
  $query = "INSERT INTO Contact (
  	contact_addressbook_id,  
  	contact_domain_id,
    contact_timecreate,
    contact_userupdate,
    contact_usercreate,
    contact_datasource_id,
    contact_company_id,
    contact_kind_id,
    contact_origin,
    contact_marketingmanager_id,
    contact_lastname,
    contact_firstname,
    contact_function_id,
    contact_title,
    contact_mailing_ok,
    contact_archive,
    contact_comment)
  VALUES (
  	$contact_addressbook_id,
    $domain_id,
    NOW(),
    null,
    $uid,
    $dsrc,
    $company_id,
    $kind_id,
    '$GLOBALS[c_origin_import]',
    $market,
    '".$obm_q->escape($lname)."',
    '".$obm_q->escape($fname)."',
    $function_id,
    '".$obm_q->escape($title)."',
    '".$obm_q->escape($mailok)."',
    $arch,
    '".$obm_q->escape($com)."')";

  display_debug_msg($query, $cdg_sql, "run_query_import_contact_insert(1)");
  $retour = $obm_q->query($query);

  $id = $obm_q->lastid();

  if ($id > 0) {
    $entity_id  = of_entity_insert('contact',$id);
    run_query_global_company_contact_number_update($comp_id);

    // User data handling
    if (is_array($userdata["contact"]["category"])) {
      foreach($userdata["contact"]["category"] as $cat_name => $hash) {
	$param_values[$cat_name] = $hash[$fields["con_$cat_name"]];
      }
      $ret = of_userdata_query_update("contact", $id, $param_values);
    }
    run_query_entity_coords_update($entity_id, $coords);
  }
  $inserted_contact = OBM_Contact::get($id);
  OBM_Contact::solrStore($inserted_contact);
  OBM_IndexingService::commit('contact');
  return $id;
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
//   - $userdata[]  : Userdata hash array[company|contact][category][$cat_name]
//   - $contact_id  : Contact id to update
///////////////////////////////////////////////////////////////////////////////
function run_query_import_contact_update(&$import_q, $fields, $defaults, &$countries, &$kinds, &$functions, &$userdata, $contact_id) {
  global $obm, $cdg_sql;
  $obm_q = new DB_OBM;

  $obm_q = new DB_OBM;
  $uid = sql_parse_id($obm["uid"]);
  $mailoks = array("X",'x','1',"1","Y","y");

  // Set default values when needed
  if (is_array($defaults)) {
    while (list($field, $value) = each ($defaults)) {
      if ($fields[$field] == "") {
	$fields[$field] = $value;
      }
    }
  }

  $dsrc = sql_parse_id($import_q->f("import_datasource_id"));
  $market = sql_parse_id($import_q->f("import_marketingmanager_id"));
  $lname = $obm_q->escape($fields["con_ln"]);
  $fname = $obm_q->escape($fields["con_fn"]);
  // If a lang has been found we get the corresponding kind
  if (isset($kinds[strtoupper($fields["con_lang"])])) {
    $kind_id = $kinds[strtoupper($fields["con_lang"])];
  }
  $kind_id = sql_parse_id($kind_id);
  // If a function name has been found we get the corresponding id
  if (isset($functions[$fields["con_func"]])) {
    $function_id = $functions[$fields["con_func"]];
    // else we get the entry as an id
  } else if (($fields["con_func"] != "") && ($fields["con_func"] > 0)) {
    $function_id = $fields["con_func"];
  }
  $function_id = sql_parse_id($function_id);
  $title = $obm_q->escape($fields["con_tit"]);
  $ad1 = $obm_q->escape($fields["con_ad1"]);
  $ad2 = $obm_q->escape($fields["con_ad2"]);
  $ad3 = $obm_q->escape($fields["con_ad3"]);
  $zip = $fields["con_zip"];
  $town = $obm_q->escape($fields["con_town"]);
  $cdx = $obm_q->escape($fields["con_cdx"]);
  // If a country name has been found we get the corresponding iso code
  if (isset($countries[$fields["con_ctry"]])) {
    $ctry = $countries[$fields["con_ctry"]];
    // else we get the entry as an iso code
  } else {
    $ctry = trim($fields["con_ctry"]);
  }
  // If country still empty we set it to 0
  if ($ctry == "") {
    $ctry = "0";
  }
  $phone = $fields["con_pho"];
  $hphone = $fields["con_hpho"];
  $mphone = $fields["con_mpho"];
  $fax = $fields["con_fax"];
  $email = $fields["con_mail"];

  $coords['phones'][] = array('label' => 'WORK_VOICE', 'number' => $phone);
  $coords['phones'][] = array('label' => 'HOME_VOICE', 'number' => $hphone);
  $coords['phones'][] = array('label' => 'CELL_VOICE', 'number' => $mphone);
  $coords['phones'][] = array('label' => 'WORK_FAX', 'number' => $fax);
  $coords['emails'][] = array('label' => 'INTERNET', 'address' => $email);
  $coords['addresses'][] = array(
    'label' => 'HQ', 
    'street' => "$ad1\n$ad2\n$ad3", 
    'zipcode' => $zip, 
    'town' => $town, 
    'expresspostal' => $cdx,
    'country' => $ctry);  
  $mailok = (in_array($fields["con_mailok"], $mailoks) ? '1' : '0');
  $com = $obm_q->escape($fields["con_com"]);
  $arch = '0';
  $cont_id = sql_parse_id($contact_id, true);
  $query = "UPDATE Contact SET
    contact_timeupdate='". date("Y-m-d H:i:s") ."',
    contact_userupdate $uid ,
    contact_datasource_id $dsrc ,
    contact_kind_id $kind_id ,
    contact_function_id $function_id ,
    contact_marketingmanager_id $market ,
    contact_lastname='".$obm_q->escape($lname)."',
    contact_firstname='".$obm_q->escape($fname)."',
    contact_country_iso3166='".$obm_q->escape($ctry)."',
    contact_title='".$obm_q->escape($title)."',
    contact_mailing_ok='".$obm_q->escape($mailok)."',
    contact_archive='".$obm_q->escape($arch)."',
    contact_comment='".$obm_q->escape($com)."'
  WHERE contact_id $cont_id ";

  display_debug_msg($query, $cdg_sql, "run_query_import_contact_update()");
  $retour = $obm_q->query($query);
  $entity_id = of_entity_get('contact', $contact_id);
  run_query_entity_coords_update($entity_id, $coords);
  // User data handling
  if (is_array($userdata["contact"]["category"])) {
    foreach($userdata["contact"]["category"] as $cat_name => $hash) {
      $param_values[$cat_name] = $hash[$fields["con_$cat_name"]];
    }
    $ret = of_userdata_query_update("contact", $contact_id, $param_values, false);
  }

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
// Return the user category hash : cats["code"] = id
// Parameters:
//   - $cat : category
///////////////////////////////////////////////////////////////////////////////
function get_import_category_user_hash($cat) {
  global $cdg_sql;

  $cats = of_category_user_get_ordered($cat);

  foreach ($cats as $one_cat) {
    $id = $one_cat["id"];
    $code = $one_cat["code"];
    $label = $one_cat["label"];
    $hash[$code] = $id;
    $hash[$label] = $id;
  }

  return $hash;
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

  $func_q = run_query_global_contactfunction();

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

  display_debug_msg($query, $cdg_sql, "run_query_import_kind()");
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


?>
