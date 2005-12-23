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

$l_q = get_list_list();
process_list_list($l_q);


///////////////////////////////////////////////////////////////////////////////
// Get the List list
///////////////////////////////////////////////////////////////////////////////
function get_list_list() {
  global $cdg_sql;

  $query = "SELECT
      list_id,
      list_name,
      list_structure
    FROM List
    ORDER BY list_id";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Process the List list
// Parameters:
//   - $l_q : DBO List list (with associated info)
///////////////////////////////////////////////////////////////////////////////
function process_list_list($l_q) {

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
      $new_structure = preg_replace("/$pattern/", $new_text, $structure,1);
      $query = "UPDATE List
      SET list_structure='$new_structure'
      WHERE list_id='$id'";
      $list_q->query($query);
      echo " - Corrected\n";
    } else {
      echo " - OK\n";
    }
  }

  echo "** End Processing List list : $nb_l entries, $nb processed\n";

}


</script>