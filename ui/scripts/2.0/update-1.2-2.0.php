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
process_companycategory1_list($c_q);


///////////////////////////////////////////////////////////////////////////////
// Get the CompanyCategory1 list
///////////////////////////////////////////////////////////////////////////////
function get_companycategory1_list() {
  global $cdg_sql;

  $query = "SELECT *
    FROM CompanyCategory1
    ORDER BY companycategory1_id";

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

  $category = "companycategory1";

  $nb_c = $c_q->num_rows();
  $nb = 0;
  $obm_q = new DB_OBM;
  $l_q = new DB_OBM;

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
    FROM CompanyCategory1Link
    WHERE companycategory1link_category_id='$id'";
    $obm_q->query($query);

    // For each link
    while ($obm_q->next_record()) {
      $ent_id = $obm_q->f("companycategory1link_company_id");
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
        'company')";
	$l_q->query($query);
      }
    }
  }

  echo "** End Processing CompanyCategory1 list : $nb_c entries, $nb processed\n";

}



</script>