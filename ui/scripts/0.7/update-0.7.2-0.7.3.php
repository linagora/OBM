<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : update-0.7.2-0.7.3.php                                       //
//     - Desc : Update data from 0.7.2 to 0.7.3 (Project data)               //
// 2003-11-21 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
require("$obminclude/phplib/obmlib.inc");

// deal_project_status
// - 0 : deal without project
// - 1 : deal with project
// - 2 : only project

$d_q = get_project_list();
process_project_list($d_q);


///////////////////////////////////////////////////////////////////////////////
// Query execution - Deal with project or project list                       //
///////////////////////////////////////////////////////////////////////////////
function get_project_list() {

  echo "Retrieving current Projects\n";

  $query = "
    select *
    from Deal
    where deal_project_status != 0
    order by deal_id
    ";

  $d_q = new DB_OBM;
  $d_q->query($query);

  return $d_q;
}


///////////////////////////////////////////////////////////////////////////////
// Process the project list                                                  //
// Parameters:
//   - $d_q : DBO Project list (Deal with associated process)
///////////////////////////////////////////////////////////////////////////////
function process_project_list($d_q) {

  $nb_pro = $d_q->num_rows();

  echo "** Processing Project list : $nb_pro entries\n";

  while ($d_q->next_record()) {
    $pro = "";
    $deal_id = $d_q->f("deal_id");
    $pro["deal"] = $d_q->f("deal_id");
    $pro["label"] = addslashes($d_q->f("deal_label"));
    $pro["tt"] = $d_q->f("deal_tasktype_id");
    $pro["company"] = $d_q->f("deal_company_id");
    $pro["soldtime"] = $d_q->f("deal_soldtime");
    $pro["datebegin"] = $d_q->f("deal_datebegin");
    $pro["archive"] = $d_q->f("deal_archive");

    $pro_id = insert_one_project($pro);
    echo "Deal $deal_id -> Project $pro_id (".$pro["label"].")\n";
    update_project_tables($deal_id, $pro_id);
  }

  echo "** End Processing Project list : $nb_pro entries\n";

}


///////////////////////////////////////////////////////////////////////////////
// Insert one project and return its new Id                                  //
// Parameters:
//   - $pro : project hash
///////////////////////////////////////////////////////////////////////////////
function insert_one_project($pro) {

  $query = "insert into Project (
      project_name,
      project_tasktype_id,
      project_company_id,
      project_deal_id,
      project_soldtime,
      project_datebegin,
      project_archive)
    values (
      '".$pro["label"]."',
      '".$pro["tt"]."',
      '".$pro["company"]."',
      '".$pro["deal"]."',
      '".$pro["soldtime"]."',
      '".$pro["datebegin"]."',
      '".$pro["archive"]."'
    )";

  $pro_q = new DB_OBM;
  $pro_q->query($query);

  $query = "select project_id
    from Project
    where
      project_name = '" . $pro["label"] . "'
      and project_tasktype_id = '" . $pro["tt"] . "'
      and project_company_id = '" . $pro["company"] . "'
      and project_deal_id = '" . $pro["deal"] . "'
      and project_soldtime = '" . $pro["soldtime"] . "'
      and project_datebegin = '" . $pro["datebegin"] . "'
      and project_archive = '" . $pro["archive"] . "'
    ";

  $pro_q->query($query);
  $pro_q->next_record();
  $new_id = $pro_q->f("project_id");

  return $new_id;
}


///////////////////////////////////////////////////////////////////////////////
// Update project references in lonks to project                             //
// Parameters:
//   - $deal_id : old project id (table Deal)
//   - $pro_id  : new project id (table Project)
///////////////////////////////////////////////////////////////////////////////
function update_project_tables($deal_id, $pro_id) {

  // Theses updates works as there are no entries for new Projects links
  // before these updates and Deal list is ordered on deal_id so deal_id
  // processed is > current project id inserted
  // So the update can not affect some new projects entries

  // ProjectUser : Field projectuser_project_id
  $query = "update ProjectUser
    set projectuser_project_id = '$pro_id'
    where projectuser_project_id = '$deal_id'";
  $pro_q = new DB_OBM;
  $pro_q->query($query);

  // ProjectTask : Field projecttask_project_id
  $query = "update ProjectTask
    set projecttask_project_id = '$pro_id'
    where projecttask_project_id = '$deal_id'";
  $pro_q = new DB_OBM;
  $pro_q->query($query);

  // ProjectStat : Field projectstat_project_id
  $query = "update ProjectStat
    set projectstat_project_id = '$pro_id'
    where projectstat_project_id = '$deal_id'";
  $pro_q = new DB_OBM;
  $pro_q->query($query);
}

</script>
