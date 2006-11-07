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

// Update list criteria (contactcategorylink_category_id => contactcategory_id)
$l_q = get_list_list();
process_list_list($l_q);


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
// Parameters:
//   - $c_q    : DBO Category List
///////////////////////////////////////////////////////////////////////////////
function process_list_list($l_q) {
  global $hash_c1, $hash_c2;

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
      print_r($criteria["modules"]["contact"]["contactcategory1link_category_id"]);
      foreach($criteria["modules"]["contact"]["contactcategory1link_category_id"] as $value) {
	$new_val = $hash_c1[$value];
	$criteria["modules"]["contact"]["contactcategory1_id"][] = $new_val;
      }

      foreach($criteria["modules"]["contact"]["contactcategory2link_category_id"] as $value) {
	$new_val = $hash_c2[$value];
	$criteria["modules"]["contact"]["contactcategory2_id"][] = $new_val;
	$coma = ",";
      }
      unset($criteria["modules"]["contact"]["contactcategory1link_category_id"]);
      unset($criteria["modules"]["contact"]["contactcategory2link_category_id"]);
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
// Process the given Category list
// companycategory_code -> companycategory1_code
// Parameters:
//   - $table  : category table name
//   - $entity : category table name
//   - $c_q    : DBO Category List
///////////////////////////////////////////////////////////////////////////////
function process_category_list($table, $entity, $c_q) {
  global $hash_c1, $hash_c2;

  $category = strtolower($table);
  $table_link = $table."Link";
  $prefix_link = strtolower($table_link);

  $nb_c = $c_q->num_rows();
  $nb = 0;
  $obm_q = new DB_OBM;
  $l_q = new DB_OBM;

  echo "** Processing $table list : $nb_c entries\n";

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
    $code = $c_q->f("${category}_code");
    $label = $c_q->f("${category}_label");

    $nb++;
    $query = "INSERT INTO Category (
      category_timeupdate,
      category_timecreate,
      $uu_into
      category_usercreate,
      category_category,
      category_code,
      category_label
    ) VALUES (
      '$tu',
      '$tc',
      $uu_value
      '$uc',
      '$category',
      '$code',
      '$label')";
    $obm_q->query($query);

    // Get the new id to create cats hash
    $query = "SELECT category_id
      FROM Category
      WHERE category_category = '$category'
        AND category_timecreate = '$tc'
        AND category_code = '$code'
        AND category_label  ='$label'";
    $obm_q->query($query);
    $obm_q->next_record();
    $c_new_id = $obm_q->f("category_id");

    $cats[$id] = $c_new_id;

    // migrate links for one category ------------------------------------------
    $query = "SELECT *
    FROM $table_link
    WHERE ${prefix_link}_category_id='$id'";
    $obm_q->query($query);

    // For each link
    while ($obm_q->next_record()) {
      $ent_id = $obm_q->f("${prefix_link}_${entity}_id");
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

  echo "** End Processing $table list : $nb_c entries, $nb processed\n";

}



</script>