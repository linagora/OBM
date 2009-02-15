<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2009 OBM.org project members team                   |
 |                                                                         |
 | This program is free software; you can redistribute it and/or           |
 | modify it under the terms of the GNU General Public License             |
 | as published by the Free Software Foundation; version 2                 |
 | of the License.                                                         |
 |                                                                         |
 | This program is distributed in the hope that it will be useful,         |
 | but WITHOUT ANY WARRANTY; without even the implied warranty of          |
 | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
 | GNU General Public License for more details.                            |
 +-------------------------------------------------------------------------+
 | http://www.obm.org                                                      |
 +-------------------------------------------------------------------------+
*/
?>
<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : update-1.2-2.0.php                                           //
//     - Desc : Upgrade data from 1.2 to 2.0                                 //
// 2006-09-27 - AliaSource                                                   //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
$path = "../../php";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("../../obminclude/global.inc");

echo "**** OBM : data migration 1.2 -> 2.0 : DB $obmdb_db ($obmdb_host)\n";

clean_category_tables();

// Migrate all user categories to new model
$c_q = get_category_list("CompanyCategory1");
process_category_list("CompanyCategory1", "company", $c_q);

$c_q = get_category_list("ContactCategory1");
process_category_list("ContactCategory1", "contact", $c_q);

$c_q = get_category_list("ContactCategory2");
process_category_list("ContactCategory2", "contact", $c_q);

$c_q = get_category_list("ContactCategory3");
process_category_list("ContactCategory3", "contact", $c_q);

$c_q = get_category_list("ContactCategory4");
process_category_list("ContactCategory4", "contact", $c_q);

$c_q = get_category_list("ContactCategory5");
process_category_list("ContactCategory5", "Contact", $c_q, "mono", "category5_id");

$c_q = get_category_list("DealCategory1");
process_category_list("DealCategory1", "Deal", $c_q);

$c_q = get_category_list("DocumentCategory1");
process_category_list("DocumentCategory1", "Document", $c_q, "mono", "category1_id");

$c_q = get_category_list("DocumentCategory2");
process_category_list("DocumentCategory2", "Document", $c_q, "mono", "category2_id");

$c_q = get_category_list("IncidentCategory2");
process_category_list("IncidentCategory2", "Incident", $c_q, "mono", "category2_id");

$hash_compcat_code = get_companycategory1_code_hash();

// Update list criteria (contactcategorylink_category_id => contactcategory_id)
$l_q = get_list_list();
process_list_list($l_q);

// purge database
remove_deprecated_category_infos();


///////////////////////////////////////////////////////////////////////////////
// Clean Category tables before doing anything to allow re-run the script
///////////////////////////////////////////////////////////////////////////////
function clean_category_tables() {
  global $cdg_sql;

  echo "* Clean Category and CategoryLink tables\n";

  $obm_q = new DB_OBM;

  $query = "DELETE FROM Category";
  $obm_q->query($query);

  $query = "DELETE FROM CategoryLink";
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Clean Database from Deprecated category tables (after update !)
///////////////////////////////////////////////////////////////////////////////
function remove_deprecated_category_infos() {
  global $cdg_sql;

  echo "* Remove Deprecated Category tables\n";

  remove_one_table("CompanyCategory1");
  remove_one_table("CompanyCategory1Link");
  remove_one_table("ContactCategory1");
  remove_one_table("ContactCategory1Link");
  remove_one_table("ContactCategory2");
  remove_one_table("ContactCategory2Link");
  remove_one_table("ContactCategory3");
  remove_one_table("ContactCategory3Link");
  remove_one_table("ContactCategory4");
  remove_one_table("ContactCategory4Link");
  remove_one_table("ContactCategory5");
  remove_one_table("DealCategory1");
  remove_one_table("DealCategory1Link");
  remove_one_table("DocumentCategory1");
  remove_one_table("DocumentCategory2");
  remove_one_table("IncidentCategory2");

  echo "* Remove Deprecated Category fields\n";

  remove_one_field("Contact", "contact_category5_id");
  remove_one_field("Incident", "incident_category2_id");
  remove_one_field("Document", "document_category1_id");
  remove_one_field("Document", "document_category2_id");

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Remove One table
///////////////////////////////////////////////////////////////////////////////
function remove_one_table($table) {
  global $cdg_sql;

  $obm_q = new DB_OBM;

  echo "$table\n";
  $query = "DROP TABLE $table";
  $ret = $obm_q->query($query);

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Remove One field
///////////////////////////////////////////////////////////////////////////////
function remove_one_field($table, $field) {
  global $cdg_sql;

  $obm_q = new DB_OBM;

  echo "$table : $field\n";
  $query = "ALTER TABLE $table DROP COLUMN $field";
  $ret = $obm_q->query($query);

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Get the List list
///////////////////////////////////////////////////////////////////////////////
function get_list_list() {
  global $cdg_sql;

  $query = "SELECT *
    FROM List
";

  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Process the list List (convert criteria)
// contactcategory1link_category_id -> contactcategory1_id
// contactcategory2link_category_id -> contactcategory2_id
// companycategory1_code -> companycategory1
// Parameters:
//   - $c_q    : DBO Category List
///////////////////////////////////////////////////////////////////////////////
function process_list_list($l_q) {
  global $hash_c1, $hash_c2, $hash_compcat_code;

  $obm_q = new DB_OBM;
  $cpt = 0;
  $nb = $l_q->num_rows();
  echo "** Processing List list : $nb entries\n";

  while ($l_q->next_record()) {
    $id = $l_q->f("list_id");
    $structure = $l_q->f("list_structure");
    
    if ($structure != "") {
      $cpt++;
      $criteria = unserialize($structure);
      // Convert contactcategory1link_category_id => contactcategory1
      if (is_array($criteria["modules"]["contact"]["contactcategory1link_category_id"])) {
        foreach($criteria["modules"]["contact"]["contactcategory1link_category_id"] as $value) {
          $values = explode(",",$value);
          $new_val = array();
          foreach($values as $val) {
            $new_val[] = $hash_c1[$val];
          }
          $criteria["modules"]["contact"]["contactcategory1"][] = implode(",",$new_val);
          $criteria["modules"]["contact"]["contactcategory1_tree"][] = "false";
	}
	unset($criteria["modules"]["contact"]["contactcategory1link_category_id"]);
      }

      // Convert contactcategory2link_category_id => contactcategory1
      if (is_array($criteria["modules"]["contact"]["contactcategory2link_category_id"])) {

        foreach($criteria["modules"]["contact"]["contactcategory2link_category_id"] as $value) {
          $values = explode(",",$value);
          $new_val = array();
          foreach($values as $val) {
            $new_val[] = $hash_c1[$val];
          }          
	  $criteria["modules"]["contact"]["contactcategory2"][] = implode(",",$new_val);
	  $criteria["modules"]["contact"]["contactcategory2_tree"][] = "false";
	}
	unset($criteria["modules"]["contact"]["contactcategory2link_category_id"]);
      }

      // Convert companycategory1_code => companycategory1
      if (is_array($criteria["modules"]["company"]["companycategory1_code"])) {

	foreach($criteria["modules"]["company"]["companycategory1_code"] as $value) {
	  $new_val = $hash_compcat_code[$value];
	  $criteria["modules"]["company"]["companycategory1"][] = $new_val;
	  $criteria["modules"]["company"]["companycategory1_tree"][] = "true";
	}
	unset($criteria["modules"]["company"]["companycategory1_code"]);
      }

      $list_structure = addslashes(serialize($criteria));
      
      $query = "UPDATE List
        SET list_structure='$list_structure'
        WHERE list_id='$id'";
      $retour = $obm_q->query($query);
    }

  }

  echo "** End Processing List list : $nb entries, $cpt updated\n";

}


///////////////////////////////////////////////////////////////////////////////
// Get the ContactCategory list
// Parameters:
//   - $table : category table name
///////////////////////////////////////////////////////////////////////////////
function get_category_list($table) {
  global $cdg_sql;

  $category = strtolower($table);

  $query = "SELECT *
    FROM $table
    ORDER BY ${category}_id";

  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the ContactCategory list
// Parameters:
//   - $table : category table name
///////////////////////////////////////////////////////////////////////////////
function get_companycategory1_code_hash() {
  global $cdg_sql;

  $hash = array();

  $query = "SELECT category_code, category_id
    FROM Category
    WHERE category_category = 'companycategory1'";

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  while ($obm_q->next_record()) {
    $id = $obm_q->f("category_id");
    $code = $obm_q->f("category_code");
    $hash[$code] = $id;
  }
  return $hash;
}


///////////////////////////////////////////////////////////////////////////////
// Process the given Category list
// companycategory_code -> companycategory1_code
// Parameters:
//   - $table     : category table name
//   - $entityu   : entity name (whith upper case)
//   - $c_q       : DBO Category List
//   - $mode      : "multi" or "mono"
//   - $cat_field : category field name in entity table (eg : categiory5_id)
///////////////////////////////////////////////////////////////////////////////
function process_category_list($table, $entityu, $c_q, $mode="multi", $cat_field="") {
  global $hash_c1, $hash_c2;

  $category = strtolower($table);
  $entity = strtolower($entityu);
  $table_link = $table."Link";
  $prefix_link = strtolower($table_link);

  $nb_c = $c_q->num_rows();
  $nb = 0;
  $obm_q = new DB_OBM;
  $l_q = new DB_OBM;

  echo "* Processing $table list : $nb_c entries...";

  $now = date("Y-m-d H:i:s");
  while ($c_q->next_record()) {
    $id = $c_q->f("${category}_id");
    $tu = $c_q->f("${category}_timeupdate");
    if ($tu == "") {
      $tu = $now;
    }
    $tc = $c_q->f("${category}_timecreate");
    if ($tc == "") {
      $tc = $now;
    }
    $uu = $c_q->f("${category}_userupdate");
    $uu_into = "";
    $uu_value = "";
    if ($uu != "") {
      $uu_into = "category_userupdate,";
      $uu_value = "'$uu',";
    }
    $uc = $c_q->f("${category}_usercreate");
    $uc_into = "";
    $uc_value = "";
    if ($uc != "") {
      $uc_into = "category_usercreate,";
      $uc_value = "'$uc',";
    }
    $code = $c_q->f("${category}_code");
    $label = addslashes($c_q->f("${category}_label"));

    $nb++;
    $query = "INSERT INTO Category (
      category_domain_id,
      category_timeupdate,
      category_timecreate,
      $uu_into
      $uc_into
      category_category,
      category_code,
      category_label
    ) VALUES (
      1,
      '$tu',
      '$tc',
      $uu_value
      $uc_value
      '$category',
      '$code',
      '$label')";
    $obm_q->query($query);

    // Get the new id to create cats hash
    $query = "SELECT category_id
      FROM Category
      WHERE category_category = '$category'
        AND category_domain_id = 1
        AND category_timecreate = '$tc'
        AND category_code = '$code'
        AND category_label  ='$label'";
    $obm_q->query($query);
    $obm_q->next_record();
    $c_new_id = $obm_q->f("category_id");

    $cats[$id] = $c_new_id;

    // migrate links for one category -----------------------------------------

    if ($mode == "multi") {
      $query = "SELECT *, ${prefix_link}_${entity}_id as ent_id
      FROM $table_link
      WHERE ${prefix_link}_category_id='$id'";
      $obm_q->query($query);

    } else {
      // "mono" mode
      $query = "SELECT ${entity}_id as ent_id, ${entity}_${cat_field}
      FROM $entityu
      WHERE ${entity}_${cat_field}='$id'";
      $obm_q->query($query);
    }

    // For each link
    while ($obm_q->next_record()) {
      $ent_id = $obm_q->f("ent_id");
      if ($ent_id > 0) {
	$query = "INSERT INTO CategoryLink (
          categorylink_category_id,
          categorylink_entity_id,
          categorylink_category,
          categorylink_entity
        ) VALUES (
          '$c_new_id',
          '$ent_id',
          '$category',
          '$entity')";
	$l_q->query($query);
      }
    }

  }

  // Save contact categories hash
  if ($category == "contactcategory1") {
    $hash_c1 = $cats;
  } else if ($category == "contactcategory2") {
    $hash_c2 = $cats;
  }

  echo " $nb processed : OK\n";

}



</script>
