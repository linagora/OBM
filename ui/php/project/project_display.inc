<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : project_display.inc                                          //
//     - Desc : Project Display File                                         //
// 2003-07-08 Bastien Continsouzas                                           //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
 

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["project_name"] = $l_name;
$fieldnames["project_company_name"] = $l_company_name;
$fieldnames["project_tasktype"] = $l_tasktype;
$fieldnames["project_soldtime"] = $l_soldtime;
$fieldnames["project_archive"] = $l_archive_first;


///////////////////////////////////////////////////////////////////////////////
// Display project specific dataset fields                                   //
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_project(&$OD, $fieldname, $link_ok) {
  global $path, $set_theme, $ico_mail, $ico_web, $ico_contact_new;
  
  if ($fieldname == "project_name") {
    $res["url"] = "$path/project/project_index.php?action=detailconsult&amp;param_project=".$OD->data_set->f("project_id");
  }

  else if ($fieldname == "project_archive") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) $res["name"] = "X";
    else $res["name"] = "&nbsp;";
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Project detail page                                               //
// Parameters:
//   - $pid : project id
///////////////////////////////////////////////////////////////////////////////
function dis_project_consult($pid) {
  global $display, $uid;
  global $l_query_error, $l_error_visibility;

  $project_q = run_query_detail($pid);
  $tasks_q = run_query_tasks($pid);
  $members_q = run_query_members($pid);
  $membertime_q = run_query_membertime($pid);

  if ($project_q->num_rows() != 1) {
    $display["msg"] .= display_err_msg($l_query_error . " - " . $project_q->query . " !");
  }

  if (($project_q->f("project_visibility") == 0) ||
      ($project_q->f("usercreate") == $uid) ) {
    $display["detailInfo"] = display_record_info($project_q->f("usercreate"),$project_q->f("userupdate"), $project_q->f("timecreate"),$project_q->f("timeupdate"));
    $display["detail"] = html_project_consult($project_q, $tasks_q, $members_q, $membertime_q);
  } else {
    // this project's page has "private" access
    $display["msg"] .= display_err_msg($l_error_visibility);
    $action = "";
  }

}


///////////////////////////////////////////////////////////////////////////////
// Display Project search Form                                               //
// Parameters:
//   - $project[] : default form values
//     keys used  : name, company_name, tt, manager, member,
///////////////////////////////////////////////////////////////////////////////
function dis_project_search_form($project) {

  $tt_q = run_query_tasktype();
  $manager_q = run_query_managers();
  $member_q = run_query_members();

  $block = html_project_search_form ($tt_q, $manager_q, $member_q, $project);
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Html Project search Form                                          //
// Parameters:
//   - $tt_q      : database object with tasktype list
//   - $manager_q : database object with project manager list
//   - $member_q  : database object with project member list
//   - $project[] : default form values
//     keys used  : name, company_name, tt, manager, member,
///////////////////////////////////////////////////////////////////////////////
function html_project_search_form ($tt_q, $manager_q, $member_q, $project) {
  global $l_name, $l_company_name, $l_tasktype, $l_manager, $l_member;
  global $l_all, $l_find,$l_task_fac, $l_task_div, $l_task_int, $l_archive;
  global $c_all;

  $name = $project["name"];
  $company_name = $project["company_name"];
  $tt = $project["tt"];
  $manager = $project["manager"];
  $member = $project["member"];
  $archive = $project["archive"];

  $task_list = array($l_task_fac, $l_task_int, $l_task_div);

  // TaskType select construction
  $type_prec = -1;
  $sel_tt = "<select name=\"sel_tt\">";
  $sel_tt .= "<option value=\"$c_all\">$l_all</option>";
  while($tt_q->next_record()) {
    //put menu titles
    if (($task_i = $tt_q->f("tasktype_internal")) != $type_prec) {
      $sel_tt .= "<option disabled=\"disabled\">".$task_list[$task_i]." </option>";
      $type_prec = $task_i;
    }
    $tt_id = $tt_q->f("tasktype_id");
    $tt_label = $tt_q->f("tasktype_label");
    $sel_tt .= "\n<option value=\"$tt_id\"";
    if ($tt == $tt_id) $sel_tt .= " selected"; 
    $sel_tt .= ">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;$tt_label</option>\n";
  }
  $sel_tt .= "</select>";

  // manager select
  $sel_manager = "<select id=\"sel_manager\" name=\"sel_manager\">
    <option value=\"$c_all\">$l_all</option>";
  while ($manager_q->next_record()) {
    $mg_id = $manager_q->f("manager_id");
    $sel_manager .= "\n<option value=\"$mg_id\"";
    if ($mg_id == $manager) { $sel_manager .= " selected=\"selected\""; }
    $sel_manager .= ">". $manager_q->f("manager_lastname")." ".$manager_q->f("manager_firstname") . "</option>\n";
  }
  $sel_manager .= "</select>";

  // member select
  $sel_member = "<select id=\"sel_member\" name=\"sel_member\">
    <option value=\"$c_all\">$l_all</option>\n";
  while($member_q->next_record()) {
    $mb_id = $member_q->f("member_id");
    $sel_member .= "<option value=\"$mb_id\"";
    if ($mb_id == $member) { $sel_member .= " selected=\"selected\""; }
    $sel_member .= ">". $member_q->f("member_lastname")." ".$member_q->f("member_firstname") . "</option>\n";
  }
  $sel_member .= "</select>";

  $checked = ($archive) ? "checked" : "";
  $cb_archive = "<input type=\"checkbox\" name=\"cb_archive\" $checked />";

  // --- HTML Template --------------------------------------------------------

  $block = "
    <form method=\"get\" name=\"f_project\" id=\"f_project\" action=\"" . url_prepare("project_index.php") . "\">

    <div class=\"search\">
      <div class=\"searchLabel\">$l_name<br />
        <input type=\"text\" name=\"tf_name\" size=\"40\" value=\"$name\" />
      </div>
      <div class=\"searchLabel\">$l_company_name<br />
        <input type=\"text\" name=\"tf_company_name\" id=\"tf_company_name\" size=\"20\" value=\"$company_name\" />
      </div>
      <div class=\"searchLabel\">$l_tasktype<br />
        $sel_tt
      </div>
      <div class=\"searchLabel\">$l_manager<br />
        $sel_manager
      </div>
      <div class=\"searchLabel\">$l_member<br />
        $sel_member
      </div>
      <div class=\"searchLabel\">$l_archive<br />
        $cb_archive
      </div>
      <div class=\"searchLabel\">&nbsp;<br />
        <input name=\"action\" type=\"hidden\" value=\"search\" />
        <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      </div>
      <div class=\"searchEnd\"></div>
    </div>

    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Project Consult (main infos, advance infos, members infos)   //
// Parameters :
//   - $project_q : DBO : information about the project
//   - $tasks_q   : DBO : list of tasks in the project 
//   - $members_q : DBO : list of members and related time infos
//      keys used : id
///////////////////////////////////////////////////////////////////////////////
function html_project_consult($project_q, $tasks_q, $members_q, $membertime_q) {
  global $set_theme, $display, $action;
  global $l_name, $l_tasktype, $l_company_name, $l_soldtime, $l_total;
  global $l_company, $l_deal, $l_project, $l_progress, $l_progressinfos;
  global $l_projected, $l_used, $l_missing, $l_sold;
  global $l_allotted, $l_no_members, $l_adv_update, $l_project, $l_resume;
  global $l_allosold, $l_usedsold, $l_progeval, $l_nbtask;
  global $l_nbmem, $l_tasksdef, $l_membersdef, $l_archive, $l_yes, $l_no;
  global $l_projected, $l_alloproj, $l_usedproj, $l_no_tasks;

  $cpt_t = 0;
  $cpt_m = 0;

  // retrieve project related infos
  $pid = $project_q->f("project_id");
  $name = $project_q->f("project_name");
  $tt = $project_q->f("project_tasktype_label");
  $company = $project_q->f("project_company_id");
  $company_name = $project_q->f("company_name");
  $deal = $project_q->f("project_deal_id");
  $deal_label = $project_q->f("deal_label");
  $arc_status = ($project_q->f("project_archive")) ? "$l_yes" : "$l_no";
  $soldtime = $project_q->f("project_soldtime");

  // Labels are different for sales and R&D projects
  if ($company != "") {
    $text_time = $l_sold;
    $text_allotime = $l_allosold;
    $text_usedtime = $l_usedsold;
  } else {
    $text_time = $l_projected;
    $text_allotime = $l_alloproj;
    $text_usedtime = $l_usedproj;
  }

  // Company name
  if ($company > 0) {
    if ($deal > 0) {
      $dis_deal = "
    </tr><tr>
      <td class=\"detailLabel\">$l_deal</td>
      <td class=\"detailText\">$deal_label</td>";
    }
    $dis_comp = "
    <div class=\"detailHead\">$l_company</div>
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_company</td>
      <td class=\"detailText\">$company_name</td>
    $dis_deal
    </tr>
    </table>";
  }

  if (($members_q != 0) && ($members_q->num_rows() != 0)){
    while ($members_q->next_record()) {
      $array_mem[$cpt_m]["id"] = $members_q->f("member_id");
      $array_mem[$cpt_m]["name"] = $members_q->f("member_lastname");
      $array_mem[$cpt_m]["firstname"] = $members_q->f("member_firstname");
      $cpt_m++;
    }
  }

  $has_tasks = ($tasks_q != 0) && ($tasks_q->num_rows() != 0);
  $has_members = ($members_q != 0) && ($members_q->num_rows() != 0);
  $nb_tsk = ($has_tasks) ? $tasks_q->nf() : 0;

  // we don't have informations about time allocation
  //  if (($membertime_q == 0) or ($membertime_q->num_rows() == 0)) {
  if (!($has_tasks) or !($has_members)) {

    $tab_style = "class=\"detail\"";

    $dis_advance = "
     <tr>
       <td class=\"detailLabel\">&nbsp;$l_sold&nbsp;</td>
       <td class=\"detailText\" align=\"right\">$soldtime</td>
       <td width=\"10%\">&nbsp;&nbsp;</td>
       <td class=\"detailLabel\">&nbsp;$l_allosold&nbsp;</td>
       <td class=\"detailText\" align=\"right\">0 %</td>
      </tr>";    

      // we display the task list
      if ($has_tasks) { 

	$dis_member .= "
       <tr>
        <td class=\"detailLabel\" colspan=\"2\">$l_tasksdef :</td>
       </tr>";

	while ($tasks_q->next_record()) {
	  $tsk_name = $tasks_q->f("projecttask_label");

	  $img = ($tasks_q->f("parent_id")) ? "<img border=\"0\" src=\"/images/$set_theme/hierarchy.png\" alt=\"\" />" : "";

	  $dis_member .= "
       <tr>
        <td class=\"detailLabel\">&nbsp;</td>
        <td class=\"detailText\">$img $tsk_name</td>
       </tr>";
	}
      
      } else {
	$dis_member .= "
        <tr>
         <td class=\"detailLabel\" colspan=\"2\">$l_no_tasks</td>
        </tr>";
      }
    
      $dis_member .= "
     <tr>
      <td>&nbsp;</td>
     <tr>";

      if ($has_members){
     
	$dis_member .= "
       <tr>
        <td class=\"detailLabel\" colspan=\"2\">$l_membersdef :</td>
       </tr>";

	while (list($key,$val) = each($array_mem)) {
	  $mem_name = $val["firstname"] ." ". $val["name"];

	  $dis_member .= "
       <tr>
        <td class=\"detailLabel\">&nbsp;</td>
        <td class=\"detailText\">- $mem_name</td>
       </tr>";
	}

      } else {
	$dis_member .= "
       <tr>
        <td class=\"detailLabel\" colspan=\"2\">$l_no_members</td>
       </tr>";
      }

    // Display the detailled advance array
  } else {
    
    $allotted = 0;
    $usedtime = 0;
    $missingtime = 0;

    $tab_style = "class=\"projectDetail\"";

    while ($tasks_q->next_record()) {

      $t_id = $tasks_q->f("projecttask_id");
      $t_parent = $tasks_q->f("parent_id");

      $array_tsk[$cpt_t]["id"] = $t_id;
      $array_tsk[$cpt_t]["label"] = $tasks_q->f("projecttask_label");
      $array_tsk[$cpt_t]["parent"] = ($t_parent == 0) ? 1 : 0;

      if ($t_parent)
	$array_child[$t_parent] = 1;

      $array_parent[$t_id] = $t_parent;

      $cpt_t++; 
    }
  
    if ($membertime_q->nf() == 0) $action = "consultnoadv";

    // list the members of the project
    while ($membertime_q->next_record()) {
      
      $m_id = $membertime_q->f("userobm_id");
      $m_task = $membertime_q->f("projectuser_projecttask_id");
      $m_proj = $membertime_q->f("projected_time");
      $m_miss = $membertime_q->f("missing_time");
      $m_used = $membertime_q->f("used_time");

      //time (used/missing/projected) for each (task/member) couple
      $array_time["proj"][$m_task][$m_id] = $m_proj;
      $array_time["miss"][$m_task][$m_id] = $m_miss;
      $array_time["used"][$m_task][$m_id] = $m_used;

      //totals by parent tasks
      if ($array_parent[$m_task] == 0) {
	$array_partot["proj"][$m_task] += $m_proj;
	$array_partot["miss"][$m_task] += $m_miss;
	$array_partot["used"][$m_task] += $m_used;
      } else {
	$m_ptask = $array_parent[$m_task];
	$array_partot["proj"][$m_ptask] += $m_proj;
	$array_partot["miss"][$m_ptask] += $m_miss;
	$array_partot["used"][$m_ptask] += $m_used;
      }

      //totals by members
      $array_memtot["proj"][$m_id] += $m_proj;
      $array_memtot["miss"][$m_id] += $m_miss;
      $array_memtot["used"][$m_id] += $m_used;

      //complete total
      $timetot["proj"] += $m_proj;
      $timetot["miss"] += $m_miss;
      $timetot["used"] += $m_used;

      //used for evaluation of the project advance
      $allotted += $m_proj;
      $missingtime += $m_miss;
      $usedtime += $m_used;
    }

    $dis_memhead = "<td class=\"projectHead\">&nbsp;</td>\n";

    // diplay the members of the project
    for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {
      $m_id = $array_mem[$cpt_e]["id"];
      $m_name = $array_mem[$cpt_e]["name"];
      $dis_memhead .= "<td class=\"projectHead\">$m_name</td>\n";
    }

    $dis_memhead .= "<td class=\"projectHead\">$l_total</td>\n";

    // display the rest of the array (ie. tasknames - projected times)
    for ($cpt_d=0; $cpt_d<$cpt_t; $cpt_d++) {

      $t_id = $array_tsk[$cpt_d]["id"];
      $t_label = $array_tsk[$cpt_d]["label"];
      $t_parent = $array_tsk[$cpt_d]["parent"];

      $t_haschild = $array_child[$t_id];

      if ($t_parent) {
	$dis_member .= "<tr>\n<td class=\"projectPtask\">$t_label</td>\n";
      } else {
	$dis_member .= "<tr>\n<td class=\"projectCtask\">
        <img border=\"0\" src=\"/images/$set_theme/hierarchy.png\" alt=\"\" />
        $t_label</td>\n";
      }

      //write a line (corresponding to a task)
      for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {

	$m_id = $array_mem[$cpt_e]["id"];
	$tm_proj = $array_time["proj"][$t_id][$m_id];
	$tm_miss = $array_time["miss"][$t_id][$m_id];
	$tm_used = $array_time["used"][$t_id][$m_id];

	if ($tm_proj != 0)
	  $tm_label = number_format($tm_used, 1) ."/". $tm_miss ."/". $tm_proj;
	else
	  $tm_label = "";

	if ($t_haschild) {
	  $dis_member .= "<td class=\"projectPtime\">&nbsp;</td>\n";
	} else if ($t_parent) {
	  $dis_member .= "<td class=\"projectPtime\">$tm_label</td>\n";
	} else {
	  $dis_member .= "<td class=\"projectCtime\">$tm_label</td>\n";
	}
      }

      if ($t_parent) {
	//write the total for a parent task
	$tm_proj = $array_partot["proj"][$t_id];
	$tm_miss = $array_partot["miss"][$t_id];
	$tm_used = $array_partot["used"][$t_id];

	if (!(isset($tm_miss))) $tm_miss = 0;
	if (!(isset($tm_proj))) $tm_proj = 0;

	if (($tm_used + $tm_miss) > (1.10 * $tm_proj))
	  $class = "projectPlate";
	else if (($tm_used + $tm_miss) < (0.90 * $tm_proj))
	  $class = "projectPearly";
	else
	  $class = "projectPtime";

	$tm_label = number_format($tm_used, 1) ."/". number_format($tm_miss) ."/". number_format($tm_proj);
	$dis_member .= "<td class=\"$class\">$tm_label</td>\n";
      } else {
	$dis_member .= "<td class=\"projectCtime\">&nbsp;</td>";
      }

      $dis_member .= "</tr>\n";
    }

    //display the last line (totals by members)
    $dis_member .= "<tr>\n <td class=\"projectPtask\">$l_total</td>\n";

    for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {
      $m_id = $array_mem[$cpt_e]["id"];

      $tm_proj = $array_memtot["proj"][$m_id];
      $tm_miss = $array_memtot["miss"][$m_id];
      $tm_used = $array_memtot["used"][$m_id];
      $tm_label = number_format($tm_used, 1) ."/". number_format($tm_miss) ."/". number_format($tm_proj);

      $dis_member .= "<td class=\"projectPtime\">$tm_label</td>\n";
    }

    //last cell (total of totals)
    $tm_proj = $timetot["proj"];
    $tm_miss = $timetot["miss"];
    $tm_used = $timetot["used"];

    if (!(isset($tm_miss))) $tm_miss = 0;
    if (!(isset($tm_proj))) $tm_proj = 0;

    $tm_label = number_format($tm_used, 1) ."/". $tm_miss ."/". $tm_proj;
    $dis_member .= "<td class=\"projectPtime\">$tm_label</td>\n";
    $dis_member .= "</tr>\n";


    // ratios
    $ratio_allosold = intval(($allotted/$soldtime)*100);
    $ratio_usedsold = intval(($usedtime/$soldtime)*100);

    $ratio_progress = (($usedtime + $missingtime) == 0) ? 0 : intval(($usedtime/($usedtime + $missingtime))*100);
  
    $ratio_progeval = intval((($usedtime + $missingtime)/$soldtime)*100);

    if ($ratio_progeval < 70)
      $symbol_progeval = "++";
    else if ($ratio_progeval < 85)
      $symbol_progeval = "+";
    else if ($ratio_progeval > 140)
      $symbol_progeval = "--";
    else if ($ratio_progeval > 120)
      $symbol_progeval = "-";
    else
      $symbol_progeval = "=";


    $span = $cpt_m + 2;
  
    //advance array
    $dis_advance = "
    <tr>
     <td class=\"detailLabel\">&nbsp;$text_time&nbsp;</td>
     <td class=\"detailText\" align=\"right\">$soldtime</td>
     <td>&nbsp;&nbsp;</td>
     <td class=\"detailLabel\">&nbsp;$text_allotime&nbsp;</td>
     <td class=\"detailText\" align=\"right\">$ratio_allosold %</td>
    </tr>
    <tr>
     <td class=\"detailLabel\">&nbsp;$l_allotted&nbsp;</td>
     <td class=\"detailText\" align=\"right\">$allotted</td>
     <td>&nbsp;&nbsp;</td>
     <td class=\"detailLabel\">&nbsp;$text_usedtime&nbsp;</td>
     <td class=\"detailText\" align=\"right\">$ratio_usedsold %</td>
    </tr>
    <tr>
     <td class=\"detailLabel\">&nbsp;$l_used&nbsp;</td>
     <td class=\"detailText\" align=\"right\">$usedtime</td>
     <td>&nbsp;&nbsp;</td>
     <td class=\"detailLabel\">&nbsp;$l_progress&nbsp;</td>
     <td class=\"detailText\" align=\"right\">$ratio_progress %</td>
    </tr>
    <tr>
     <td class=\"detailLabel\">&nbsp;$l_missing&nbsp;</td>
     <td class=\"detailText\" align=\"right\">$missingtime</td>
     <td>&nbsp;&nbsp;</td>
     <td class=\"detailLabel\">&nbsp;$l_progeval&nbsp;</td>
     <td class=\"detailText\" align=\"right\">$symbol_progeval</td>
    </tr>";

  } 

  // --- HTML Template --------------------------------------------------------

  $display["title"] = "<div class=\"title\">$l_project : $name</div>";

  $block = "
    $dis_comp

    <div class=\"detailHead\">$l_project</div>

    <table class=\"detail\">
     <tr>
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailText\">$name</td>
     </tr>
     <tr>
      $dis_company
      <td class=\"detailLabel\">$l_tasktype</td>
      <td class=\"detailText\">$tt</td>
     </tr>
     <tr>
      <td class=\"detailLabel\">$l_resume</td>
      <td class=\"detailText\"># $l_nbtask : $nb_tsk / # $l_nbmem : $cpt_m</td>
     </tr>
     <tr>
      <td class=\"detailLabel\">$l_archive</td>
      <td class=\"detailText\">$arc_status</td>
     </tr>
    </table>
 	
    <div class=\"detailHead\">$l_progress</div>

    <table class=\"detail\">
      $dis_advance
    </table>

    <div class=\"detailHead\">$l_progressinfos</div>

    <div class=\"projectCenter\">
    <table $tab_style>
     <tr>
      $dis_memhead
     </tr>
     $dis_member
    </table>
    </div>
    ";
  //remettre le cellpadding
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display New Project Form                                                  //
// Parameters:
//   - $action    : 
//   - $project_q : database object with project infos
//   - $project[] : default form values
//     keys used  : name, id, tt, soldtime
///////////////////////////////////////////////////////////////////////////////
function html_project_form($action, $project_q, $project) {
  global $l_company, $l_deal, $l_project, $l_member, $l_insert, $l_update;
  global $l_name, $l_tasktype, $l_task, $l_projtime, $l_none, $l_archive;
  global $ctt_sales, $ctt_research;

  if ($action == "detailupdate") {
    $pid = $project_q->f("project_id");
    $name = $project_q->f("project_name");
    $tt = $project_q->f("project_tasktype_id");
    $company = $project_q->f("project_company_id");
    $deal = $project_q->f("project_deal_id");
    $soldtime = $project_q->f("project_soldtime");
    $arc_val = ($project_q->f("project_archive")) ? "checked" : "";
    $company_name = $project_q->f("company_name");
    $deal_label = $project_q->f("deal_label");
  } else {
    $pid = $project["id"];
    $name = $project["name"];
    $tt = $project["tt"];
    $company = $project["company"];
    $deal = $project["deal"];
    $soldtime = $project["soldtime"];
    $company_name = $project["company_name"];
    $deal_label = $project["deal_label"];
    $task = $project["tasklabel"];

    if ($action == "new") {
      $name = $deal_label;

      // member select
      $mb_q = run_query_get_obmusers();
      $sel_member = "<select id=\"sel_member\" name=\"sel_member\">
        <option value=\"0\">$l_none</option>\n";
      while($mb_q->next_record()) {
	$mb_id = $mb_q->f("userobm_id");
	$mb_name = $mb_q->f("userobm_lastname")." ".$mb_q->f("userobm_firstname");
	$sel_member .= "
        <option value=\"$mb_id\">$mb_name</option>";
      }
      $sel_member .= "</select>";

      $dis_member = "
    </tr><tr>
      <td class=\"detailLabel\">$l_member</td>
      <td class=\"detailForm\">$sel_member</td>";

      // task
      $dis_task = "
    </tr><tr>
      <td class=\"detailLabel\">$l_task</td>
      <td class=\"detailForm\"><input name=\"tf_tasklabel\" size=\"40\" maxlength=\"128\" value=\"$task\" /></td>";
    }
  }

  // We select the task list depending on task category (or if company filled)
  if ($company > 0) {
    $tt_q = run_query_tasktype($ctt_sales);
    if ($deal > 0) {
      $dis_deal = "
    </tr><tr>
      <td class=\"detailLabel\">$l_deal</td>
      <td class=\"detailText\">$deal_label</td>";
    }
    $dis_comp = "
    <div class=\"detailHead\">$l_company</div>
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_company</td>
      <td class=\"detailText\">$company_name</td>
    $dis_deal
    </tr>
    </table>";
  } else {
    $tt_q = run_query_tasktype($ctt_research);
    $dis_comp = "";
  }

  // task type select
  $sel_tt = "
        <select id=\"sel_tt\" name=\"sel_tt\">
          <option value=\"0\">$l_none</option>";
  while ($tt_q->next_record()) {
    $tt_id = $tt_q->f("tasktype_id");
    $tt_label = $tt_q->f("tasktype_label");
    $sel_tt .= "
          <option value=\"$tt_id\"";
    if ($tt_id == $tt) { $sel_tt .= " selected=\"selected\""; }
    $sel_tt .= ">$tt_label</option>";
  }
  $sel_tt .= "</select>\n";

  // archive field
  if ($action == "detailupdate") {
    $dis_archive = "<tr>
      <td class=\"detailLabel\">$l_archive</td>
      <td class=\"detailForm\"><input type=\"checkbox\" name=\"cb_archive\" $arc_val /></td>
     </tr>";
  } else {
    $dis_archive = "";
  }

  // target action
  if ($action == "detailupdate") {
    $dis_action = "
      <input type=\"hidden\" name=\"action\" value=\"update\" />";
    $text_button = $l_update;
  } else {
    $dis_action = "
      <input type=\"hidden\" name=\"action\" value=\"insert\" />";
    $text_button = $l_insert;
  }

  $dis_button = "$dis_action
      <input type=\"hidden\" name=\"param_project\" value=\"$pid\" />
      <input type=\"hidden\" name=\"param_company\" value=\"$company\" />
      <input type=\"hidden\" name=\"param_deal\" value=\"$deal\" />
      <input type=\"submit\" value=\"$text_button\" />";
  
  if ($action == "detailupdate") 
    $display["title"] = "<div class=\"title\">$l_project : $name</div>";
  
  // --- HTML Template --------------------------------------------------------
  
  $block = "
    <form method=\"post\" name=\"form_project_create\" 
      onsubmit=\"if (check_project(this)) return true; else return false;\"
      action=\"".url_prepare("project_index.php")."\">

    $dis_comp

    <div class=\"detailHead\">$l_project</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailForm\"><input type=\"text\" name=\"tf_name\" size=\"40\" value=\"$name\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_tasktype</td>
      <td class=\"detailForm\">$sel_tt</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_projtime</td>
      <td class=\"detailForm\"><input type=\"text\" name=\"tf_soldtime\" size=\"10\" maxlength=\"8\" value=\"$soldtime\" /></td>
   $dis_task
   $dis_member
   </tr>
    $dis_archive
   </table>

   <div class=\"detailButton\">
   <p class=\"detailButtons\">$dis_button</p>
   </div>
   </form>
   ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Display and check the project links (and delete form if ok)
// Parameters:
//   - $p_id : project id
///////////////////////////////////////////////////////////////////////////////
function dis_check_links($p_id) {
  global $l_can_delete, $l_cant_delete, $l_back;
  global $l_link_timetask, $l_link_timetask_no;
  global $path, $display;

  $delete_ok = true;

  // Links from Time management
  $obm_q = run_query_project_timetask_links($p_id);

  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $display_linkt ="
      <tr>
        <td class=\"detailHead\">$l_link_timetask</td>
      </tr>";
    while ($obm_q->next_record()) {
      $tlabel = $obm_q->f("projecttask_label");
      $uid = $obm_q->f("userobm_id");
      $uname = $obm_q->f("userobm_lastname") . " " . $obm_q->f("userobm_firstname");
      $display_linkt .= "
      <tr>
        <td class=\"detailLabel\">$tlabel
        (<a class=\"detailLabel\" href=\"" . url_prepare("$path/user/user_index.php?action=detailconsult&amp;param_user=$uid") . "\">$uname</a>)
        </td>
      </tr>";
    }
  } else {
    $display_linkt = "
      <tr>
        <td class=\"detailHead\">$l_link_timetask_no</td>
      </tr>";
  }

  $dis_back = "<form name=\"form_back\" method=\"post\"
    action=\"".url_prepare("project_index.php") ."\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"param_project\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $block .= "<table class=\"detail\">
   $display_linkt
   </table>";

  if ($delete_ok == true) {
    $display["msg"] .= display_ok_msg($l_can_delete);
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">" . dis_delete_form($p_id) . "</p>
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  } else {
    $display["msg"] .= display_warn_msg($l_cant_delete);
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Displays the delete form (button)                                         //
// Parameters:
//   - $p_id : project Id
///////////////////////////////////////////////////////////////////////////////
function dis_delete_form($p_id) {
  global $l_delete;

  $ret = "<form name=\"form_delete\" method=\"get\"
      action=\"" . url_prepare("project_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"param_project\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onclick=\"if (confirm_del(this.form)) return true; else return false;\" />
    </form>";

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Display Task Add Form                                                     //
// Parameters:
//   - $ptask_q   : database object with project parent tasks list
//   - $project[] : default form values
//     keys used  : id
///////////////////////////////////////////////////////////////////////////////
function html_project_taskadd_form($ptask_q, $project) {
  global $display;
  global $l_none, $l_taskname, $l_parent, $l_task_add, $l_project;
  global $l_tasksinfos;
  
  $pid = $project["id"];
  $pname = $project["name"];

  $tf_taskname = "
    <tr>
      <td class=\"detailLabel\">$l_taskname</td>
      <td class=\"detailForm\"><input type=\"text\" name=\"tf_tasklabel\" size=\"30\" /></td>
    </tr>";

  if (($ptask_q != 0) && ($ptask_q->num_rows() != 0)) {
    // ptask select
    $sel_ptask = "
    <tr>
      <td class=\"detailLabel\">$l_parent</td>
      <td class=\"detailForm\">
        <select id=\"sel_ptask\" name=\"sel_ptask\">
          <option value=\"0\">$l_none</option>\n";
    $nrows = $ptask_q->num_rows();

    while ($ptask_q->next_record()) {
      $parent = $ptask_q->f("parent_id");

      if ($parent == 0) {
	$ptask_id = $ptask_q->f("projecttask_id");
	$sel_ptask .= "<option value=\"$ptask_id\"";
	$sel_ptask .= ">". $ptask_q->f("projecttask_label") . "</option>\n";
      }
    }

    $sel_ptask .= "</select></td></tr>\n";
    $ptask_q->seek(0);
  }

  $dis_button = "
       <input type=\"hidden\" name=\"param_project\" value=\"$pid\" />
       <input type=\"hidden\" name=\"action\" value=\"task_add\" />
       <input type=\"submit\" value=\"$l_task_add\" />";

  $display["title"] = "<div class=\"title\">$l_project : $pname</div>";

  // --- HTML Template --------------------------------------------------------

  $block = "
   <form method=\"post\" name=\"form_taskadd\"
     onsubmit=\"if (check_taskadd(this)) return true; else return false;\"
     action=\"". url_prepare("project_index.php") ."\">
 
    <div class=\"detailHead\">$l_tasksinfos</div>

    <table class=\"detail\">
       $tf_taskname
       $sel_ptask
    </table>

    <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_button</p>
    </div>

   </form>
   ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Project search result                                         //
// Parameters:
//   - $project[] : project search criteria
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function dis_project_search_list($project) {
  global $display, $l_no_found;
  global $auth, $new_order, $order_dir;

  $pref_q = run_query_display_pref($auth->auth["uid"], "project", 0);
  $obm_q = run_query_search($project, $new_order, $order_dir);

  $nb_project = $obm_q->num_rows();

  if ($nb_project == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $block = html_project_search_list($obm_q, $pref_q, $nb_project, $project);
  }

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// HTML Display the Project Search result                                    //
// Parameters:
//   - $obm_q_projects : list of projects
//   - $pref_q      : fields to display in the list of projects 
//   - $nb_deal     : nb projects found
//   - $project[]   : project search criteria
//     keys used    : name, company_name, tt, manager, member
///////////////////////////////////////////////////////////////////////////////
function html_project_search_list($obm_q_projects, $pref_q, $nb_project, $project) {
  global $display, $perms_user, $l_found;

  $name = $project["name"];
  $company_name = $project["company_name"];
  $tt = $project["tt"];
  $manager = $project["manager"];
  $member = $project["member"];

  $url = url_prepare("project_index.php?tf_name=$name&amp;tf_company_name=$company_name&amp;sel_tt=$tt&amp;sel_manager=$manager&amp;sel_member=$member&amp;action=search");

  $dis_projects_list=new OBM_DISPLAY("DATA", $pref_q, "project");
  $dis_projects_list->data_set = $obm_q_projects;
  $dis_projects_list->data_url = $url;
  $dis_projects_list->data_header = "both";

  // --- HTML Template --------------------------------------------------------

  $display["msg"] .= display_info_msg($nb_project." ".$l_found);
  $block = $dis_projects_list->display("dis_data_project");

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Project Tasklist                                             //
// Parameters :
//   - $tasks_q     : DBO : list of tasks related to the deal
//   - $project[]   : project search criteria
//     keys used    : id
///////////////////////////////////////////////////////////////////////////////
function html_project_tasklist($tasks_q, $project) {
  global $set_theme;
  global $l_tasksinfos, $l_del_task_sel, $l_task_add;
  global $l_no_tasks;

  $pid = $project["id"];

  if (($tasks_q != 0) && ($tasks_q->num_rows() != 0)) {

    while ($tasks_q->next_record()) {

      $t_id = $tasks_q->f("projecttask_id");
      $t_label = $tasks_q->f("projecttask_label");
      $t_parent = $tasks_q->f("parent_id");
      $t_rank = $tasks_q->f("projecttask_rank");
      
      if ($t_parent == 0) {
	$dis_task .= "
         <tr>
          <td align=\"right\" width=\"5%\">
	   <input type=\"checkbox\" name=\"cb_task$t_id\" />
	  </td>
	  <td class=\"detailText\" colspan=\"2\">$t_label</td>
         </tr>";
      } else {
	$dis_task .= "
         <tr>
          <td align=\"right\">
           <input type=\"checkbox\" name=\"cb_task$t_id\" />
          </td>
	  <td class=\"detailText\" colspan=\"2\">
           <img border=\"0\" src=\"/images/$set_theme/hierarchy.png\" alt=\"\" />$t_label
          </td>
         </tr>";
      }
   }
    
    $dis_button  = "
      <input type=\"hidden\" name=\"param_project\" value=\"$pid\" />
      <input type=\"hidden\" name=\"action\" value=\"task_del\" />
      <input type=\"submit\" value=\"$l_del_task_sel\" />";

  } else {
    
    $dis_task = "<tr><td class=\"detailLabel\">$l_no_tasks</td></tr>\n";
  }
  
  $block = "
    <form name=\"form_tasklist\" method=\"post\">
     <table class=\"detail\">
      $dis_task
     </table>

    <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_button</p>
    </div>

    </form>
  ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Project Member List                                          //
// Parameters :
//   - $tasks_q     : DBO : list of tasks related to the deal
//   - $members_q   : DBO : list of members
//   - $project[]   : project search criteria
//     keys used    : id
///////////////////////////////////////////////////////////////////////////////
function html_project_member_form($members_q, $project) {
  global $display;
  global $l_no_members, $l_member_add, $l_membersinfos, $l_del_member_sel;
  global $l_project, $l_manager, $l_member;

  $pid = $project["id"];
  $pname = $project["name"];

  if (($members_q != 0) && ($members_q->num_rows() != 0)) {

    $class = "projectMemberlist";

    while ($members_q->next_record()) {

      $m_id = $members_q->f("member_id");
      $m_name = $members_q->f("member_lastname"). " " .$members_q->f("member_firstname");
      $m_manager = $members_q->f("member_manager");

      if ($m_manager)
	$rd_manager = "
          <input type=\"radio\" name=\"rd_man$m_id\" value=\"1\" checked
            onchange=\"change_status($m_id, 1, form_memberdel)\" />$l_manager
          <input type=\"radio\" name=\"rd_man$m_id\" value=\"0\"
            onchange=\"change_status($m_id, 0, form_memberdel)\" />$l_member";
      else
	$rd_manager = "
          <input type=\"radio\" name=\"rd_man$m_id\" value=\"1\"
            onchange=\"change_status($m_id, 1, form_memberdel)\" />$l_manager
          <input type=\"radio\" name=\"rd_man$m_id\" value=\"0\" checked
            onchange=\"change_status($m_id, 0, form_memberdel)\" />$l_member";
      
      $dis_member .= "
        <tr>
         <td class=\"projectMember\"><input type=\"checkbox\" name=\"cb_u$m_id\"/></td>
         <td class=\"projectMember\">$m_name</td>
         <td class=\"projectMemberrd\">$rd_manager</td>
        </tr>";
    }

    $dis_button  = "
      <input type=\"hidden\" name=\"param_project\" value=\"$pid\" />
      <input type=\"hidden\" name=\"param_user\" value=\"\" />
      <input type=\"hidden\" name=\"param_status\" value=\"\" />
      <input type=\"hidden\" name=\"action\" value=\"member_del\" />
      <input type=\"submit\" value=\"$l_del_member_sel\"
             onclick=\"submit_memberdel(this.form)\" />";

    $span = 3;

  } else {

    $class = "detail";

    $dis_member  = "
      <tr>
       <td class=\"detailLabel\">$l_no_members</td>
      </tr>";

    $span = 1;
  }

  $display["title"] = "<div class=\"title\">$l_project : $pname</div>";

  // --- HTML Template --------------------------------------------------------

  $block = "
   <form name=\"form_memberdel\" method=\"post\"
         action=\"".url_prepare("project_index.php")."\">

    <div class=\"detailHead\">$l_membersinfos</div>

    <table class=\"$class\">
       $dis_member
    </table>

    <div class=\"detailButton\">
     <p class=\"detailButtons\">$dis_button</p>
    </div>

   </form>
   ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Project Member Time List                                     //
// Parameters :
//   - $tasks_q     : DBO : list of tasks related to the deal
//   - $members_q   : DBO : list of members
//   - $project[]   : project search criteria
//     keys used    : id
///////////////////////////////////////////////////////////////////////////////
function html_project_membertime_form($tasks_q, $members_q, $membertime_q, $project) {
  global $display, $set_theme;
  global $l_memberstimeinfos, $l_del_member_sel, $l_validate, $l_member_add;
  global $l_no_members, $l_project;

  $cpt_t=0;
  $cpt_m=0;
  $pid = $project["id"];
  $pname = $project["name"];

  // task list
  while ($tasks_q->next_record()) {
    
    $pt_id = $tasks_q->f("projecttask_id");
    $par_id = $tasks_q->f("parent_id");
    $parent = $tasks_q->f("parent_group");

    $array_tsk[$cpt_t]["id"] = $pt_id;
    $array_tsk[$cpt_t]["label"] = $tasks_q->f("projecttask_label");

    $array_child[$par_id] = ($par_id) ? 1 : 0;
    $array_parent[$pt_id] = ($pt_id == $parent) ? 1 : 0;

    $cpt_t++;
  }

  // list the members of the project
  while ($members_q->next_record()) {
    
    $array_mem[$cpt_m]["id"] = $members_q->f("member_id");
    $array_mem[$cpt_m]["name"] = $members_q->f("member_lastname");  
    $cpt_m++;
  }

    if (($membertime_q != 0) && ($membertime_q->num_rows() != 0)) {
      while ($membertime_q->next_record()) {
	$m_id = $membertime_q->f("userobm_id");
	$m_task = $membertime_q->f("projectuser_projecttask_id");
	$m_proj = $membertime_q->f("projected_time");
	$array_time[$m_task][$m_id] = $m_proj;
      } 
    }

    $dis_memhead = "<tr><td class=\"projectHead\">&nbsp;</td>";

    // diplay the members of the project
    for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {
      $m_id = $array_mem[$cpt_e]["id"];
      $m_name = $array_mem[$cpt_e]["name"];
      $dis_memhead .= "<td class=\"projectHead\" width=\"50\">$m_name</td>\n";
    }

    $dis_memhead .= "<tr>";

    // display the rest of the array (ie. tasknames - projected times)
    for ($cpt_d=0; $cpt_d<$cpt_t; $cpt_d++) {

      $t_id = $array_tsk[$cpt_d]["id"];
      $t_label = $array_tsk[$cpt_d]["label"];

      $t_parent = $array_child[$t_id];

      if ($array_parent[$t_id])
	$dis_member .= "<tr>\n<td class=\"projectPtask\">$t_label</td>\n";
      else
      	$dis_member .= "<tr>\n<td class=\"projectCtask\">
          <img border=\"0\" src=\"/images/$set_theme/hierarchy.png\" alt=\"\" />$t_label</td>\n";

      //ecrit la ligne pour un tache sans enfants
      for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {

	$m_id = $array_mem[$cpt_e]["id"];
	$tm_proj = $array_time[$t_id][$m_id];

	if ($t_parent) {
 	  $dis_member .= "<td class=\"projectPtime\">&nbsp;</td>\n";
	} else if ($array_parent[$t_id]) {
	  $dis_member .= "
            <td class=\"projectPtime\">
             <input type=\"text\" name=\"tf_projected[$m_id][$t_id]\" size=\"10\" maxlength=\"8\" value=\"$tm_proj\" />
	    </td>";
	} else {
	  $dis_member .= "
            <td class=\"projectCtime\">
             <input type=\"text\" name=\"tf_projected[$m_id][$t_id]\" size=\"10\" maxlength=\"8\" value=\"$tm_proj\" />
	    </td>";
	}
      }

      $dis_member .= "</tr>\n";
    }
    
    $dis_button .= "
       <input type=\"submit\" value=\"$l_validate\"
          onclick=\"if(check_membertimevalid(this.form, $cpt_m)) return true; else return false;\" />";
     
     $span = $cpt_m*2 + 1;
     
     $display["title"] = "<div class=\"title\">$l_project : $pname</div>";

  // --- HTML Template --------------------------------------------------------

   $block = "
     <form name=\"form_membertimevalid\" method=\"post\">

      <div class=\"detailHead\">$l_memberstimeinfos</div>

      <table class=\"projectDetail\">
       $dis_memhead
       $dis_member
      </table>

      <input type=\"hidden\" name=\"param_project\" value=\"$pid\" />

     <div class=\"detailButton\">
      <p class=\"detailButtons\">$dis_button</p>
     </div>

     </form>
     ";

   return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Project Advance Form                                         //
// Parameters :
//   - $members_q  : DBO : list of members
//   - $tasks_q    : DBO : list of tasks related to the deal
//   - $project[]   : project search criteria
//     keys used    : id
///////////////////////////////////////////////////////////////////////////////
function html_project_advance($members_q, $membertime_q, $tasks_q, $project) {
  global $display, $set_theme;
  global $l_project, $l_name, $l_tasktype, $l_company_name, $l_soldtime;
  global $l_projected, $l_used, $l_advupdateinfos, $l_adv_update;

  $cpt_t=0;
  $cpt_m=0;
  $pid = $project["id"];
  $pname = $project["name"];

  // list the tasks of the project
  while ($tasks_q->next_record()) {
    $pt_id = $tasks_q->f("projecttask_id");
    $par_id = $tasks_q->f("parent_id");
    $parent = $tasks_q->f("parent_group");

    $array_tsk[$cpt_t]["id"] = $pt_id;
    $array_tsk[$cpt_t]["label"] = $tasks_q->f("projecttask_label");

    $array_parent[$pt_id] = ($pt_id == $parent) ? 1 : 0;

    $cpt_t++;
  }

  while ($members_q->next_record()) {
    $array_mem[$cpt_m]["id"] = $members_q->f("member_id");
    $array_mem[$cpt_m]["name"] = $members_q->f("member_lastname");
    $array_mem[$cpt_m]["firstname"] = $members_q->f("member_firstname");

    $cpt_m++;
  }

  if (($membertime_q != 0) && ($membertime_q->num_rows() != 0)) {
    while ($membertime_q->next_record()) {
      $m_id = $membertime_q->f("userobm_id");
      $m_task = $membertime_q->f("projectuser_projecttask_id");

      $array_time["proj"][$m_task][$m_id] = $membertime_q->f("projected_time");
      $array_time["miss"][$m_task][$m_id] = $membertime_q->f("missing_time");
    } 
  }

  $dis_memhead = "<td class=\"projectHead\">&nbsp;</td>";

  // diplay the members of the project
  for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {
    $m_id = $array_mem[$cpt_e]["id"];
    $m_name = $array_mem[$cpt_e]["name"];
    $dis_memhead .= "<td class=\"projectHead\">$m_name</td>\n";
  }

  // display the rest of the array (ie. tasknames - projected times)
  for ($cpt_d=0; $cpt_d<$cpt_t; $cpt_d++) {

    $t_id = $array_tsk[$cpt_d]["id"];
    $t_label = $array_tsk[$cpt_d]["label"];

    $t_parent = $array_child[$t_id];

    if ($array_parent["$t_id"]) {
      $img = "";
      $classTask = "projectPtask";
      $classInput = "projectPtime";
    }else{
      $img = "<img border=\"0\" src=\"/images/$set_theme/hierarchy.png\" alt=\"\" />";
      $classTask = "projectCtask";
      $classInput = "projectCtime";
    }

    $dis_member .= "<tr>\n<td class=\"$classTask\">$img $t_label</td>\n";
      
    //ecrit la ligne pour un tache sans enfants
    for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {

      $m_id = $array_mem[$cpt_e]["id"];
      $tm_proj = $array_time["proj"][$t_id][$m_id];
      $tm_miss = $array_time["miss"][$t_id][$m_id];

      if ($tm_proj != 0) {
	$dis_member .= "
          <td class=\"$classInput\">
	   <input type=\"text\" name=\"tf_missing[$t_id][$m_id]\" size=\"10\" maxlength=\"8\" value=\"$tm_miss\" />
	  </td>\n";
      } else {
	$dis_member .= "<td class=\"$classInput\">&nbsp;</td>\n";
      }
    }
      
    $dis_member .= "</tr>\n";
  }
    
  $dis_button = "
    <input type=\"hidden\" name=\"action\" value=\"a_update\" />
    <input type=\"hidden\" name=\"param_project\" value=\"$pid\" />
    <input type=\"submit\" value=\"$l_adv_update\" />
    ";
    
  $span = $cpt_m + 1;

  $display["title"] = "<div class=\"title\">$l_project : $pname</div>";

  // --- HTML Template --------------------------------------------------------
  
  $block = "
     <form method=\"post\" name=\"form_advance2\"
         onsubmit=\"if (check_advancevalid(this)) return true; else return false;\"
         action=\"".url_prepare("project_index.php")."\">

     <div class=\"detailHead\">$l_advupdateinfos</div>

     <table class=\"projectDetail\">
       $dis_memhead
       $dis_member
     </table>

     <div class=\"detailButton\">
      <p class=\"detailButtons\">$dis_button</p>
     </div>

     </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Project Display preference screen                             //
// Parameters:
//   - $display_newpref_q    : DBO : Field list to display
//   - $display_searchpref_q : DBO : Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_project_display_pref($display_searchpref_q) {
  global $l_searchproject_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $display_searchpref_q);
  $dis_pref->display_entity = "project"; 
  $dis_pref->pref_title = $l_searchproject_display;

  $block .= "<td>";
  $block .= $dis_pref->display();
  $block .= "</td></tr></table>";

  $block .= $dis_pref->dis_pref_help();
  return $block;
}

</SCRIPT>
