<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : update-1.2-2.0.php                                           //
//     - Desc : Upgrade data from 1.2 to 2.0                                 //
// 2006-09-27 - AliaSource                                                   //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");

// Migrate all user categories to new model
$c_q = get_companycategory1_list();
process_list_list_structure($l_q);
$l_q = get_list_list();
process_list_list_query($l_q);
$i_q = get_import_list();
process_import_list($i_q);

// Update List to set correct mode
$l_q = get_list_list();
process_list_list_mode($l_q);


///////////////////////////////////////////////////////////////////////////////
// Get the CompanyCategory1 list
///////////////////////////////////////////////////////////////////////////////
function get_companycategory1_list() {
  global $cdg_sql;

  $query = "SELECT *
    FROM CompanyCategory1
    ORDER BY companycategory_id";

  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Process the CompanyCategory1 list
// companycategory_code -> companycategory1_code
// Parameters:
//   - $l_q : DBO List list (with associated info)
///////////////////////////////////////////////////////////////////////////////
function process_companycategory1_list($c_q) {

  $cat = "companycategory1";

  $nb_c = $c_q->num_rows();
  $nb = 0;
  $obm_q = new DB_OBM;

  echo "** Processing CompanyCategory1 list : $nb_c entries\n";

  while ($c_q->next_record()) {
    $id = $c_q->f("companycategory1_id");
    $tu = $c_q->f("companycategory1_timeupdate");
    $tc = $c_q->f("companycategory1_timecreate");
    $uu = $c_q->f("companycategory1_userupdate");
    $uc = $c_q->f("companycategory1_usercreate");
    $code = $c_q->f("companycategory1_code");
    $label = $c_q->f("companycategory1_label");

    $nb++;
    $query = "INSERT INTO Category (
      category_timeupdate,
      category_timecreate,
      category_userupdate,
      category_usercreate,
      category_category,
      category_code,
      category_label
    ) VALUES (
      '$tu',
      '$tc',
      '$uu',
      '$uc',
      '$cat',
      '$code',
      '$label')";
    $obm_q->query($query);

    // Get the new id to create cats hash
    $query = "SELECT category_id
      FROM Category
      WHERE category_category = '$cat'
        AND category_timecreate = '$tc'
        AND category_code = '$code'
        AND category_label  ='$label'";
    $obm_q->query($query);
    $obm_q->next_record();
    $c_id = $obm_q->f("category_id");

    $cats[$id] = $c_id;

    // migrate links
    $query = "SELECT *
    FROM CompanyCategory1Link
    WHERE companycategory1link_category_id='$id'";
    $obm_q->query($query);

    // XXXXXX
    while ($obm_q->next_record()) {
      $comp_id = $c_q->f("companycategory1link_company_id");
      $query = "INSERT INTO Category (
        category_timeupdate,
        category_timecreate,
        category_userupdate,
        category_usercreate,
        category_category,
        category_code,
        category_label
      ) VALUES (
        '$tu',
        '$tc',
        '$uu',
        '$uc',
        '$cat',
        '$code',
        '$label')";
    $obm_q->query($query);
    
  }

  echo "** End Processing CompanyCategory1 list : $nb_c entries, $nb processed\n";

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