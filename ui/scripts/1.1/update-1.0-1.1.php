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
// OBM - File : update-1.0-1.1.php                                           //
//     - Desc : Upgrade data from 1.0 to 1.1                                 //
// 2005-12-22 - Aliacom                                                      //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");

$clist_mode_normal = 0;
$clist_mode_expert = 1;

// Correct companycategory to companycategory1
$l_q = get_list_list();
process_list_list_structure($l_q);
$l_q = get_list_list();
process_list_list_query($l_q);
$i_q = get_import_list();
process_import_list($i_q);

// Update List to set correct mode
$l_q = get_list_list();
process_list_list_mode($l_q);


///////////////////////////////////////////////////////////////////////////////
// Get the List list
///////////////////////////////////////////////////////////////////////////////
function get_list_list() {
  global $cdg_sql;

  $query = "SELECT
      list_id,
      list_name,
      list_mode,
      list_structure,
      list_query
    FROM List
    ORDER BY list_id";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Process the List list, convert the stored structure
// companycategory_code -> companycategory1_code
// Parameters:
//   - $l_q : DBO List list (with associated info)
///////////////////////////////////////////////////////////////////////////////
function process_list_list_structure($l_q) {

  $pattern = "20:\"companycategory_code";
  $new_text = "21:\"companycategory1_code";

  $nb_l = $l_q->num_rows();
  $nb = 0;
  $list_q = new DB_OBM;

  echo "** Processing List list (converting structure) : $nb_l entries\n";

  while ($l_q->next_record()) {
    $id = $l_q->f("list_id");
    $name = $l_q->f("list_name");
    $structure = $l_q->f("list_structure");

    echo "List $id";

    if (preg_match("/$pattern/", $structure)) {
      $nb++;
      $new_structure = addslashes(preg_replace("/$pattern/", $new_text, $structure));
      $query = "UPDATE List
      SET list_structure='$new_structure'
      WHERE list_id='$id'";
      $list_q->query($query);
      echo " - Corrected\n";
    } else {
      echo " - OK\n";
    }
  }

  echo "** End Processing List list structure : $nb_l entries, $nb processed\n";

}


///////////////////////////////////////////////////////////////////////////////
// Process the List list, convert the stored query
// convert companycategory_code -> companycategory1_code
// Function -> FontactFunction
// function_ -> contactfunction_
// Parameters:
//   - $l_q : DBO List list (with associated info)
///////////////////////////////////////////////////////////////////////////////
function process_list_list_query($l_q) {

  $pattern = " Function";
  $new_text = " ContactFunction";
  $pattern2 = " function_";
  $new_text2 = " contactfunction_";
  $pattern3 = " CompanyCategory ";
  $new_text3 = " CompanyCategory1 ";
  $pattern4 = "companycategory_";
  $new_text4 = "companycategory1_";
  $pattern5 = " CompanyCategoryLink ";
  $new_text5 = " CompanyCategory1Link ";
  $pattern6 = "companycategorylink";
  $new_text6 = "companycategory1link";

  $nb_l = $l_q->num_rows();
  $nb = 0;
  $list_q = new DB_OBM;

  echo "** Processing List list (converting query) : $nb_l entries\n";

  while ($l_q->next_record()) {
    $id = $l_q->f("list_id");
    $name = $l_q->f("list_name");
    $structure = $l_q->f("list_structure");
    $query = $l_q->f("list_query");

    echo "List $id";

    if ( (preg_match("/$pattern/", $query))
	  || (preg_match("/$pattern2/", $query))
	  || (preg_match("/$pattern3/", $query))
	  || (preg_match("/$pattern4/", $query))
	  || (preg_match("/$pattern5/", $query))
	  || (preg_match("/$pattern6/", $query)) ) {
      $nb++;
      $new_query = preg_replace("/$pattern/", $new_text, $query);
      $new_query = preg_replace("/$pattern2/", $new_text2, $new_query) ;
      $new_query = preg_replace("/$pattern3/", $new_text3, $new_query) ;
      $new_query = preg_replace("/$pattern4/", $new_text4, $new_query) ;
      $new_query = preg_replace("/$pattern5/", $new_text5, $new_query) ;
      $new_query = addslashes(preg_replace("/$pattern6/", $new_text6, $new_query));
      $query = "UPDATE List
      SET list_query='$new_query'
      WHERE list_id='$id'";
      $list_q->query($query);
      echo " - Corrected\n";
    } else {
      echo " - OK\n";
    }
  }

  echo "** End Processing List list query : $nb_l entries, $nb processed\n";

}


///////////////////////////////////////////////////////////////////////////////
// Get the Import list
///////////////////////////////////////////////////////////////////////////////
function get_import_list() {
  global $cdg_sql;

  $query = "SELECT
      import_id,
      import_name,
      import_desc
    FROM Import
    ORDER BY import_id";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Process the Import list (convert comp_cat -> comp_cat1)
// Parameters:
//   - $i_q : DBO Import list (with associated info)
///////////////////////////////////////////////////////////////////////////////
function process_import_list($i_q) {

  $pattern = "comp_cat\"";
  $new_text = "comp_cat1\"";
  $pattern2 = "l_cat\"";
  $new_text2 = "l_category1\"";

  $nb_i = $i_q->num_rows();
  $nb = 0;
  $obm_q = new DB_OBM;

  echo "** Processing Import list (converting desc) : $nb_i entries\n";

  while ($i_q->next_record()) {
    $id = $i_q->f("import_id");
    $name = $i_q->f("import_name");
    $desc = $i_q->f("import_desc");

    echo "Import $id";

    if ( (preg_match("/$pattern/", $desc))
	 || (preg_match("/$pattern2/", $desc)) ) {
      $nb++;
      $new_desc = preg_replace("/$pattern/", $new_text, $desc);
      $new_desc = addslashes(preg_replace("/$pattern2/", $new_text2, $new_desc));
      $query = "UPDATE Import
      SET import_desc='$new_desc'
      WHERE import_id='$id'";
      $obm_q->query($query);
      echo " - Corrected\n";
    } else {
      echo " - OK\n";
    }
  }

  echo "** End Processing Import list : $nb_i entries, $nb processed\n";

}


///////////////////////////////////////////////////////////////////////////////
// Return the current mode of a given list according to parameters
// Parameters:
//   - $query    : list query
//   - $criteria : list criteria (unserialized from structureà or parameter
// Returns:
//   $clist_mode_expert || $clist_mode_normal
///////////////////////////////////////////////////////////////////////////////
function check_list_mode($query, $criteria) {
  global $clist_mode_expert, $clist_mode_normal;

  if ($criteria == "" && $query != "") {
    $mode = $clist_mode_expert;
  } else {
    $mode = $clist_mode_normal;
  }

  return $mode;
}


///////////////////////////////////////////////////////////////////////////////
// Process the List list, to update mode
// Parameters:
//   - $l_q : DBO List list (with associated info)
///////////////////////////////////////////////////////////////////////////////
function process_list_list_mode($l_q) {

  $nb_l = $l_q->num_rows();
  $nb = 0;
  $list_q = new DB_OBM;

  echo "** Processing List list (setting Mode) : $nb_l entries\n";

  while ($l_q->next_record()) {
    $id = $l_q->f("list_id");
    $name = $l_q->f("list_name");
    $mode = $l_q->f("list_mode");
    $structure = $l_q->f("list_structure");
    $criteria = unserialize($structure);
    $query = $l_q->f("list_query");

    echo "List $id";

    $new_mode = check_list_mode($query, $criteria);

    echo ":$mode:$new_mode:";
    if ("$mode" !== "$new_mode") {
      $nb++;
      $query = "UPDATE List
      SET list_mode='$new_mode'
      WHERE list_id='$id'";
      $list_q->query($query);
      echo " - Corrected\n";
    } else {
      echo " - OK\n";
    }
  }

  echo "** End Processing List list Mode : $nb_l entries, $nb processed\n";

}




</script>