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
$fieldnames["project_label"] = $l_label;
$fieldnames["project_initlabel"] = $l_label;
$fieldnames["project_company_name"] = $l_company_name;
$fieldnames["project_tasktype"] = $l_tasktype;
$fieldnames["project_soldtime"] = $l_soldtime;
$fieldnames["project_archive"] = $l_archive;


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
  
  if ($fieldname == "project_initlabel") {
//     $res["name"] = "";
    $res["url"] = "$path/project/project_index.php?action=init&amp;param_project=".$OD->data_set->f("project_id");
  }
  
  else if ($fieldname == "project_label") {
//     $res["name"] = "";
    $res["url"] = "$path/project/project_index.php?action=detailconsult&amp;param_project=".$OD->data_set->f("project_id");
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Project search Form                                               //
// Parameters:
//   - $tt_q      : database object with tasktype list
//   - $manager_q : database object with project manager list
//   - $member_q  : database object with project member list
//   - $project[] : default form values
//     keys used  : name, company_name, tt, manager, member,
///////////////////////////////////////////////////////////////////////////////
function html_project_search_form ($tt_q, $manager_q, $member_q, $project) {
  global $l_name, $l_company_name, $l_tasktype, $l_manager, $l_member;
  global $l_all, $l_find,$l_task_fac, $l_task_div, $l_task_int; 
  global $c_all;

  $name = $project["name"];
  $company_name = $project["company_name"];
  $tt = $project["tt"];
  $manager = $project["manager"];
  $member = $project["member"];

  $task_list = array($l_task_fac, $l_task_int, $l_task_div);

  // TaskType select construction
  $type_prec = -1;
  $sel_tt = "<select name=sel_tt>";
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

  // --- HTML Template --------------------------------------------------------

  echo "    
    <form method=\"get\" name=\"f_project\" id=\"f_project\" action=\"" . url_prepare("project_index.php") . "\">
    <table class=\"details\">
    <tr>
      <td class=\"textLabelForm\">$l_name<br />
        <input type=\"text\" name=\"tf_name\" id=\"tf_name\" size=\"40\"
        value=\"$name\" /></td>
      <td>&nbsp;&nbsp;</td>
      <td class=\"textLabelForm\">$l_company_name<br />
        <input type=\"text\" name=\"tf_company_name\" id=\"tf_company_name\" size=\"20\"
        value=\"$company_name\" /></td>
      <td>&nbsp;&nbsp;</td>
      <td class=\"textLabelForm\">$l_tasktype<br />
        $sel_tt</td>
    </tr><tr>
      <td class=\"textLabelForm\">$l_manager<br />
        $sel_manager</td>
      <td>&nbsp;&nbsp;</td>
      <td class=\"textLabelForm\">$l_member<br />
        $sel_member</td>
      <td colspan = 2>&nbsp;&nbsp;</td>

      <td>&nbsp;&nbsp;&nbsp;&nbsp;
        <input name=\"action\" id=\"action\" type=\"hidden\" value=\"search\" />
        <input name=\"submit\" id=\"submit\" type=\"submit\" value=\"$l_find\" />
      </td>
    </tr>
    </table>
    </form>
    <hr />";
}


///////////////////////////////////////////////////////////////////////////////
// Display New Project Form                                                  //
// Parameters:
//   - $action    : 
//   - $project_q : database object with project infos
//   - $tt_q      : database object with tasktype list
//   - $project[] : default form values
//     keys used  : name, id, tt, soldtime
///////////////////////////////////////////////////////////////////////////////
function html_project_form($action, $project_q, $tt_q, $project) {
  global $l_dealinfos, $l_projectinfos, $l_interninfos, $l_insert, $l_update;
  global $l_name, $l_tasktype, $l_company_name, $l_soldtime, $l_none;

  if ($action == "detailupdate") {
    $pid = $project_q->f("project_id");
    $name = $project_q->f("project_label");
    $tt = $project_q->f("project_tasktype_id");
    $soldtime = $project_q->f("project_soldtime");
  } else {
    $name = $project["name"];
    $pid = $project["id"];
    $tt = $project["tt"];
    $soldtime = $project["soldtime"];
  }

  // tt select
  $sel_tt = "<td>
             <select id=\"sel_tt\" name=\"sel_tt\">
             <option value=\"0\">$l_none</option>";
  while($tt_q->next_record()) {
    $tt_id = $tt_q->f("tasktype_id");
    $sel_tt .= "<option value=\"$tt_id\"";
    if ($tt_id == $tt) { $sel_tt .= " selected=\"selected\""; }
    $sel_tt .= ">". $tt_q->f("tasktype_label") . "</option>\n";
  }
  $sel_tt .= "</select></td>";
  
 // target action
  if ($action == "detailupdate") {
    $dis_action = "<input type=\"hidden\" name=\"action\" value=\"d_update\" />";
  } else {
    $dis_action = "<input type=\"hidden\" name=\"action\" value=\"task_fill\" />";
  }

  $l_button = ($action == "detailupdate" ? $l_update : $l_insert);

  // --- HTML Template --------------------------------------------------------
  echo "
    <form method=\"post\" name=\"form_project_create\" 
      onsubmit=\"if (check_project(this)) return true; else return false;\"
      action=\"".url_prepare("project_index.php")."\">
     <table class=\"details\">
       <tr>
         <td colspan=\"2\" class=\"detailHead\">$l_interninfos</td>
       </tr><tr>
        <td class=\"detailLabel\">$l_name</td>
        <td class=\"detailText\">
         <input type=\"text\" name=\"tf_name\" size=\"40\" value=\"$name\"/></td>
        </td>
       </tr><tr>
        <td class=\"detailLabel\">$l_tasktype</td>
        $sel_tt
       </tr><tr>
        <td class=\"detailLabel\">$l_soldtime</td>
        <td><input type=\"text\" name=\"tf_soldtime\" size=\"10\" maxlength=\"8\" value=\"$soldtime\"/></td>
      </tr>
     </table>
     <table class =\"details\">
      <tr>
       <td class=\"detailHead\">
        $dis_action
        <input type=\"hidden\" name=\"param_project\" value=$pid />
        <input type=\"hidden\" name=\"hd_status\" value=2 />
        <input type=\"submit\" value=\"$l_button\" />
       </td>
      </tr>
     </table>
    </form>
";

}


///////////////////////////////////////////////////////////////////////////////
// Display Init Project Form                                                 //
// Parameters:
//   - $action    : 
//   - $project_q : database object with project infos
//   - $project[] : default form values
//     keys used  : id, soldtime
///////////////////////////////////////////////////////////////////////////////
function html_project_init_form($action, $project_q, $project) {
  global $l_dealinfos, $l_projectinfos, $l_member_add, $l_insert, $l_update;
  global $l_name, $l_tasktype, $l_company_name, $l_soldtime;

  $pid = $project["id"];

  $name = $project_q->f("project_label");
  $tt = $project_q->f("project_tasktype_label");
  $company = $project_q->f("project_company_name");

  if ($action == "detailupdate") {
    $soldtime = $project_q->f("project_soldtime");
  } else {
    $soldtime = $project["soldtime"];
  }

  // target action
  if ($action == "detailupdate") {
    $dis_action = "<input type=\"hidden\" name=\"action\" value=\"d_update\" />";
  } else {
    $dis_action = "<input type=\"hidden\" name=\"action\" value=\"task_fill\" />";
  }

  $l_button = ($action == "detailupdate" ? $l_update : $l_insert);

  // --- HTML Template --------------------------------------------------------
  echo "
    <form method=\"post\" name=\"form_project_init\" 
      onsubmit=\"if (check_init_project(this)) return true; else return false;\"
      action=\"".url_prepare("project_index.php")."\">
     <table class=\"details\">
       <tr>
         <td colspan=\"2\" class=\"detailHead\">$l_dealinfos</td>
       </tr><tr>
        <td class=\"detailLabel\">$l_name</td>
        <td class=\"detailText\">$name</td>
       </tr><tr>
        <td class=\"detailLabel\">$l_company_name</td>
        <td class=\"detailText\">$company</td>
       </tr><tr>
        <td class=\"detailLabel\">$l_tasktype</td>
        <td class=\"detailText\">$tt</td>
       </tr><tr>
         <td colspan=\"2\" class=\"detailHead\">$l_projectinfos</td>
       </tr><tr>
        <td class=\"detailLabel\">$l_soldtime</td>
        <td><input type=\"text\" name=\"tf_soldtime\" size=\"10\" maxlength=\"8\" value=\"$soldtime\"/></td>
      </tr>
     </table>
     <table class =\"details\">
      <tr>
       <td class=\"detailHead\">
        $dis_action
        <input type=\"hidden\" name=\"param_project\" value=\"$pid\" />
        <input type=\"hidden\" name=\"hd_status\" value=1 />
        <input type=\"submit\" value=\"$l_button\" />
       </td>
      </tr>
     </table>
     </form>
";

}

///////////////////////////////////////////////////////////////////////////////
// Display Task Add Form                                                     //
// Parameters:
//   - $ptask_q   : database object with project parent tasks list
//   - $project[] : default form values
//     keys used  : id
///////////////////////////////////////////////////////////////////////////////
function html_project_taskadd_form($ptask_q, $project) {
  global $l_none, $l_taskname, $l_parent, $l_task_add;
  global $l_tasksinfos;
  
  $pid = $project["id"];

  if (($ptask_q != 0) && ($ptask_q->num_rows() != 0)) {
    // ptask select
    $sel_ptask = "<tr>
                   <td class=\"detailLabel\">$l_parent</td>
                   <td><select id=\"sel_ptask\" name=\"sel_ptask\">
                    <option value=\"0\">$l_none</option>\n";

    $nrows = $ptask_q->num_rows();

    while ($ptask_q->next_record()) {

      $parent = $ptask_q->f("projecttask_parent");

      if ($parent == 0) {

	$ptask_id = $ptask_q->f("projecttask_id");
	$sel_ptask .= "<option value=\"$ptask_id\"";
	$sel_ptask .= ">". $ptask_q->f("projecttask_label") . "</option>\n";
      }
    }

    $sel_ptask .= "</select></td></tr>\n";
    $ptask_q->seek(0);
  }

  // --- HTML Template --------------------------------------------------------

  echo "
    <form method=\"post\" name=\"form_taskadd\"
     onsubmit=\"if (check_taskadd(this)) return true; else return false;\"
     action=\"". url_prepare("project_index.php") ."\">
    <table class=\"details\">
      <tr>
       <td colspan=\"2\" class=\"detailHead\">$l_tasksinfos</td>
      </tr>
      <tr><td colspan=\"2\">&nbsp;</td></tr>
      <tr>
       <td class=\"detailLabel\">$l_taskname</td>
       <td><input type=\"text\" name=\"tf_tasklabel\" size=\"30\"/></td>
      </tr>
       $sel_ptask
    </table>
                 
    <table class=\"details\">
     <tr>
      <td align=\"center\">
       <input type=\"hidden\" name=\"param_project\" value=\"$pid\" />
       <input type=\"hidden\" name=\"action\" value=\"task_add\" />
       <input type=\"submit\" value=\"$l_task_add\" />
      </td>
     </tr>
   </table>
   </form>
   ";

}

///////////////////////////////////////////////////////////////////////////////
// Display Member Add Window                                                 //
// Parameters:
//   - $project[] : default form values
//     keys used  : id
///////////////////////////////////////////////////////////////////////////////
function html_project_memberadd_form($project) {
  
  $pid = $project["id"];

  $ext_open  = "window.name='Liste'\n";
  $ext_open .= "window.open('../user/user_index.php?action=ext_get_ids";
  $ext_open .= "&popup=1&title=$l_member_add&ext_action=member_add";
  $ext_open .= "&ext_url=../project/project_index.php&ext_id=$pid";
  $ext_open .= "&ext_target=Liste','','height=400,width=620,scrollbars=yes')";

  echo "
    <script type=\"text/javascript\">
      $ext_open
    </script>
  ";
}

///////////////////////////////////////////////////////////////////////////////
// Display the new Projects search result                                    //
// Parameters:
//   - $company[] : company search criteria
//     keys used  : status, name, kind, zip
///////////////////////////////////////////////////////////////////////////////
function dis_project_new_list($project) {
  global $auth;
  global $l_new_projects, $l_no_display;
  
  $pref_q = run_query_display_pref($auth->auth["uid"], "project_new");

  $obm_q = run_query_newprojects();

  $nb_newprojects = $obm_q->num_rows();

  if ($nb_newprojects != 0) {
    display_info_msg($l_new_projects) ;
    html_project_new_list($obm_q, $pref_q, $nb_newprojects, $project);
  }
  else
    display_info_msg($l_no_projects) ;

}


///////////////////////////////////////////////////////////////////////////////
// HTML Display new Projects search result                                   //
// Parameters : 
//   - $obm_q_comp : list of companies
//   - $pref_q     : the fields which have to be displayed
//   - $nb_company : nb companies returned by the search query
//   - $company[]  : company search criteria
//     keys used   : status, name, kind, zip
///////////////////////////////////////////////////////////////////////////////
function html_project_new_list($obm_q_comp, $pref_q, $nb_newprojects, $project) {
  global $l_found;

  $url = url_prepare("project_index.php?action=index");

  $dis_newprojects_list = new OBM_DISPLAY("DATA", $pref_q, "project");
  $dis_newprojects_list->data_set = $obm_q_comp;
  $dis_newprojects_list->data_url = $url;
  $dis_newprojects_list->data_header = "both";
  $dis_newprojects_list->data_order = false;

  $dis_newprojects_list->display("dis_data_project");
}


///////////////////////////////////////////////////////////////////////////////
// Display the Project search result                                         //
// Parameters:
//   - $project[] : project search criteria
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function dis_project_search_list($project) {
  global $l_no_found;
  global $auth, $new_order, $order_dir;

  $obm_q = run_query_search($project, $new_order, $order_dir);

  $nb_project = $obm_q->num_rows();

  if ($nb_project == 0) {
    display_warn_msg($l_no_found);
  } else {
    $pref_q = run_query_display_pref($auth->auth["uid"], "project", 0);
    html_project_search_list($obm_q, $pref_q, $nb_project, $project);
  }
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
  //-- Labels
  global $perms_user,$l_perms_user, $l_found;

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

  display_info_msg($nb_project." ".$l_found);

  $dis_projects_list->display("dis_data_project");

}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Project Infos (main informations)                            //
// Parameters :
//   - $project_q   : DBO : information about the project
//   - $project[]   : project search criteria
//     keys used    : 
///////////////////////////////////////////////////////////////////////////////
function html_project_infos($project_q, $project) {
  global $l_name, $l_tasktype, $l_company_name, $l_soldtime;
  global $l_interninfos, $l_externinfos, $l_membersinfos;

  $name = $project_q->f("project_label");
  $tt = $project_q->f("project_tasktype_label");
  $company = $project_q->f("project_company_name");
  $status = $project_q->f("project_status");
  $soldtime = $project_q->f("project_soldtime");

  // Table header
  $title = ($status == 1 ? $l_externinfos : $l_interninfos);

  // Company name
  if ($status == 1) {
    $dis_company = "<td class=\"detailLabel\">$l_company_name</td>
                    <td class=\"detailText\">$company</td>
                    </tr><tr>";
  }
  // --- HTML Template --------------------------------------------------------

  echo "
     <table class=\"details\">
       <tr>
         <td colspan=\"2\" class=\"detailHead\">$title</td>
       </tr>
       <tr><td colspan=5>&nbsp;</td></tr>
       <tr>
        <td class=\"detailLabel\">$l_name</td>
        <td class=\"detailText\">$name</td>
       </tr><tr>
        $dis_company
        <td class=\"detailLabel\">$l_tasktype</td>
        <td class=\"detailText\">$tt</td>
       </tr><tr>
        <td class=\"detailLabel\">$l_soldtime</td>
        <td class=\"detailText\">$soldtime</td>
       </tr>
     </table>
  ";
}

///////////////////////////////////////////////////////////////////////////////
// HTML Display Project Consult (main infos, advance infos, members infos)   //
// Parameters :
//   -  $project_q : DBO : information about the project
//   -  $tasks_q   : DBO : list of tasks in the project 
//   -  $members_q : DBO : list of members and related time infos
//   - $project[]  : project search criteria
//     keys used   : id
///////////////////////////////////////////////////////////////////////////////
function html_project_consult($project_q, $tasks_q, $members_q, $project) {
  global $l_name, $l_tasktype, $l_company_name, $l_soldtime, $l_total;
  global $l_interninfos, $l_externinfos, $l_membersinfos, $l_advanceinfos;
  global $l_projected, $l_used, $l_remaining, $l_missing, $l_diff, $l_sold;
  global $l_allotted, $l_no_members, $l_adv_update;
  global $l_allosold, $l_usedsold, $l_progeval, $l_progress;

  $allotted = 0;
  $usedtime = 0;
  $missingtime = 0;

  $idx = 0;
  $cpt_r=0;
  $cpt_t=0;
  $cpt_m=0;
  $pid = $project["id"];

  // retrieve project related infos
  $name = $project_q->f("project_label");
  $tt = $project_q->f("project_tasktype_label");
  $company = $project_q->f("project_company_name");
  $status = $project_q->f("project_status");
  
  // Table header
  $title = ($status == 1 ? $l_externinfos : $l_interninfos);

  // Company name
  if ($status == 1) {
    $dis_company = "<td class=\"detailLabel\">$l_company_name</td>
                    <td class=\"detailText\">$company</td>
                    </tr><tr>";
  }

  // list the tasks of the project
  while ($tasks_q->next_record()) {
    $t_id = $tasks_q->f("projecttask_id");
    $t_parent = $tasks_q->f("projecttask_parent");
    $t_label = $tasks_q->f("projecttask_label");
    
    $array_raw[$cpt_r]["id"] = $t_id;
    $array_raw[$cpt_r]["label"] = $t_label;
    $array_raw[$cpt_r]["parent"] = $t_parent;
      
    $cpt_r++;
  }

  // reorder the task list
  while ($cpt_t < $cpt_r) {
    $t_id = $array_raw[$idx]["id"];
    $t_label = $array_raw[$idx]["label"];
    $t_parent = $array_raw[$idx]["parent"];
    
    $array_tsk[$cpt_t]["id"] = $t_id;
    $array_tsk[$cpt_t]["label"] = $t_label;
    $array_tsk[$cpt_t]["parent"] = 1;
    $array_parent[$t_id] = 0;
    $cpt_t++;
    
    while (list($key,$val) = each($array_raw)) {
      if ($val["parent"] == $t_id) {
	$c_id = $val["id"];
	$array_tsk[$cpt_t]["id"] = $c_id;
	$array_tsk[$cpt_t]["label"] = "|--- ". $val["label"];
	$array_tsk[$cpt_t]["parent"] = 0;
	$array_child[$t_id] = 1; 
	$array_parent[$c_id] = $t_id;
	$cpt_t++; 
      }
    }
    
    reset($array_raw);
    $idx++;
  }
  
  // list the members of the project
  while ($members_q->next_record()) {
      
    $m_id = $members_q->f("userobm_id");
    $m_task = $members_q->f("projectuser_projecttask_id");
    $m_proj = $members_q->f("projected_time");
    $m_miss = $members_q->f("missing_time");
    $m_used = $members_q->f("used_time");

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

    if ($m_id != $prev_id){

      $prev_id = $m_id;
      $m_name = $members_q->f("userobm_lastname");

      $array_mem[$cpt_m]["id"] =$m_id;
      $array_mem[$cpt_m]["name"] = $m_name;
	
      $cpt_m++;
    }
  }

  $dis_memhead = "<td>&nbsp;</td>";

  // diplay the members of the project
  for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {
    $m_id = $array_mem[$cpt_e]["id"];
    $m_name = $array_mem[$cpt_e]["name"];
    $dis_memhead .= "<td class=\"detailLabel\">$m_name</td>\n";
  }

  $dis_memhead .= "<td class=\"detailLabel\">$l_total</td>\n";

  // display the rest of the array (ie. tasknames - projected times)
  for ($cpt_d=0; $cpt_d<$cpt_t; $cpt_d++) {

    $t_id = $array_tsk[$cpt_d]["id"];
    $t_label = $array_tsk[$cpt_d]["label"];
    $t_parent = $array_tsk[$cpt_d]["parent"];

    $t_haschild = $array_child[$t_id];

    $dis_member .= "<tr>\n<td class=\"detailLabel\">$t_label</td>\n";
      
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
	$dis_member .= "<td class=\"detailText\">&nbsp;</td>\n";
      } else {
	$dis_member .= "<td class=\"detailText\">$tm_label</td>\n";
      }
    }

    if ($t_parent) {
      //write the total for a parent task
      $tm_proj = $array_partot["proj"][$t_id];
      $tm_miss = $array_partot["miss"][$t_id];
      $tm_used = $array_partot["used"][$t_id];
      $tm_label = number_format($tm_used, 1) ."/". $tm_miss ."/". $tm_proj;
      $dis_member .= "<td class=\"detailText\">$tm_label</td>\n";
    } else {
      $dis_member .= "<td>&nbsp;</td>";
    }

    $dis_member .= "</tr>\n";
  }

  //display the last line (totals by members)
  $dis_member .= "<tr>\n <td class=\"detailLabel\">$l_total</td>\n";

  for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {
    $m_id = $array_mem[$cpt_e]["id"];

    $tm_proj = $array_memtot["proj"][$m_id];
    $tm_miss = $array_memtot["miss"][$m_id];
    $tm_used = $array_memtot["used"][$m_id];
    $tm_label = number_format($tm_used, 1) ."/". $tm_miss ."/". $tm_proj;
    $dis_member .= "<td class=\"detailText\">$tm_label</td>\n";
  }

  //last cell (total of totals)
  $tm_proj = $timetot["proj"];
  $tm_miss = $timetot["miss"];
  $tm_used = $timetot["used"];
  $tm_label = number_format($tm_used, 1) ."/". $tm_miss ."/". $tm_proj;
  $dis_member .= "<td class=\"detailText\">$tm_label</td>\n";
  $dis_member .= "</tr>\n";

  // Project advance stats
  $soldtime = $project_q->f("project_soldtime");
  //   $remainingtime = $soldtime - $usedtime;
  //   $difftime = $remainingtime - $missingtime;

  $ratio_allosold = intval(($allotted/$soldtime)*100);
  $ratio_usedsold = intval(($usedtime/$soldtime)*100);

  //bcontins a transformer en indicateur compréhensible
  if (($usedtime + $missingtime) == 0)
    $ratio_progress = 0;
  else
    $ratio_progress = intval(($usedtime/($usedtime + $missingtime))*100);
  
  $ratio_progeval = intval((($usedtime + $missingtime)/$soldtime)*100);

  //advance array
  $dis_advance = "<tr>
                   <td class=\"detailLabel\">&nbsp;$l_sold&nbsp;</td>
                   <td class=\"detailText\" align=\"right\">$soldtime</td>
                   <td width=10%>&nbsp;&nbsp;</td>
                   <td class=\"detailLabel\">&nbsp;$l_allosold&nbsp;</td>
                   <td class=\"detailText\" align=\"right\">$ratio_allosold %</td>
                  </tr>
                  <tr>
                   <td class=\"detailLabel\">&nbsp;$l_allotted&nbsp;</td>
                   <td class=\"detailText\" align=\"right\">$allotted</td>
                   <td>&nbsp;&nbsp;</td>
                   <td class=\"detailLabel\">&nbsp;$l_usedsold&nbsp;</td>
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
                   <td class=\"detailText\" align=\"right\">$ratio_progeval</td>
                  </tr>";

  // diplay buttons
    
  $span = $cpt_m + 2;

  $dis_button = "<form method=\"post\" name=\"form_advance\"
                   action=\"".url_prepare("project_index.php")."\">
                   <table class=\"details\">
                    <tr>
                     <td align=\"center\">
                      <input type=\"hidden\" name=\"action\" value=\"advanceupdate\" />
                      <input type=\"hidden\" name=\"param_project\" value=$pid />
                      <input type=\"submit\" value=\"$l_adv_update\" />
                     </td>
                    </tr>
                   </table>
                  </form>
                  ";

  // --- HTML Template --------------------------------------------------------

  echo "
     <table class=\"details\">
       <tr>
         <td colspan=\"2\" class=\"detailHead\">$title</td>
       </tr>
       <tr><td colspan=5>&nbsp;</td></tr>
       <tr>
        <td class=\"detailLabel\">$l_name</td>
        <td class=\"detailText\">$name</td>
       </tr><tr>
        $dis_company
        <td class=\"detailLabel\">$l_tasktype</td>
        <td class=\"detailText\">$tt</td>
       </tr>
     </table>
	
     <br>

     <table class=\"details\">
       <tr>
        <td colspan=\"5\" class=\"detailHead\">$l_advanceinfos</td>
       </tr>
       <tr><td colspan=5>&nbsp;</td></tr>
       $dis_advance
     </table>

     <br>

     <table class=\"details\">
       <tr>
        <td colspan=\"$span\" class=\"detailHead\">$l_membersinfos</td>
       </tr>
       <tr><td colspan=\"$span\">&nbsp;</td></tr>
       $dis_memhead
       $dis_member
     </table>

     $dis_button";
}

///////////////////////////////////////////////////////////////////////////////
// HTML Display Project Tasklist                                             //
// Parameters :
//   - $tasks_q     : DBO : list of tasks related to the deal
//   - $project[]   : project search criteria
//     keys used    : id
///////////////////////////////////////////////////////////////////////////////
function html_project_tasklist($tasks_q, $project) {
  global $l_tasksinfos, $l_del_task_sel, $l_valid_task, $l_task_add;
  global $l_no_tasks;

  $pid = $project["id"];

  if (($tasks_q != 0) && ($tasks_q->num_rows() != 0)) {

    while ($tasks_q->next_record()) {

      $t_id = $tasks_q->f("projecttask_id");
      $t_label = $tasks_q->f("projecttask_label");
      $t_parent = $tasks_q->f("projecttask_parent");
      $t_rank = $tasks_q->f("projecttask_rank");
      
      if ($t_parent == 0) {
	$array_task[$t_id] = "<tr>\n<td align=\"right\"><input type=\"checkbox\" name=\"cb_task$t_id\"/></td>\n";
	$array_task[$t_id] .= "<td class=\"detailLabel\" colspan=\"3\">$t_label</td>\n</tr>\n";
      } else {
	$array_task[$t_parent] .= "<tr>\n<td align=\"right\"><input type=\"checkbox\" name=\"cb_task$t_id\"/></td>\n";
	$array_task[$t_parent] .= "<td class=\"detailLabel\" colspan=\"3\">|----- $t_label</td>\n</tr>\n";
      }
    }

    while (list($key,$val) = each($array_task)) {
      $dis_task .= $val;
    }

    $dis_button  = "<tr>\n";
    $dis_button .= " <td>&nbsp;</td>\n";
    $dis_button .= " <td>\n";
    $dis_button .= "  <input type=\"hidden\" name=\"param_project\" value=\"$pid\" />\n";
    $dis_button .= "  <input type=\"hidden\" name=\"action\" value=\"task_del\" />\n";
    $dis_button .= "  <input type=\"submit\" value=\"$l_del_task_sel\" />\n";
    $dis_button .= " <td>\n";
    $dis_button .= " </form>\n";
    $dis_button .= " <form name=\"form_taskvalid\" method=\"post\">\n";
    $dis_button .= " <td>\n";
    $dis_button .= "  <input type=\"hidden\" name=\"param_project\" value=\"$pid\" />\n";
    $dis_button .= "  <input type=\"hidden\" name=\"action\" value=\"member_fill\" />\n";
    $dis_button .= "  <input type=\"submit\" value=\"$l_valid_task\" />\n";
    $dis_button .= " <td>\n";
    $dis_button .= "</tr>\n";

  } else {

    $dis_task = "<tr><td>&nbsp;</td><td class=\"detailText\" colspan=\"2\">$l_no_tasks</td></tr>\n";
  }
  
  echo "
    <form name=\"form_tasklist\" method=\"post\">
     <table class=\"details\">
      $dis_task
      $dis_button
     </table>
    </form>
  ";

}

///////////////////////////////////////////////////////////////////////////////
// HTML Display Project Member List                                          //
// Parameters :
//   - $tasks_q     : DBO : list of tasks related to the deal
//   - $members_q   : DBO : list of members
//   - $project[]   : project search criteria
//     keys used    : id
///////////////////////////////////////////////////////////////////////////////
function html_project_membertime_form($tasks_q, $members_q, $project) {
  global $l_membersinfos, $l_del_member_sel, $l_valid_member, $l_member_add;
  global $l_no_members;

  $idx = 0;
  $cpt_r=0;
  $cpt_t=0;
  $cpt_m=0;
  $pid = $project["id"];

  if (($members_q != 0) && ($members_q->num_rows() != 0)) {

    // list the tasks of the project
    while ($tasks_q->next_record()) {
      $t_id = $tasks_q->f("projecttask_id");
      $t_parent = $tasks_q->f("projecttask_parent");
      $t_label = $tasks_q->f("projecttask_label");

      $array_raw[$cpt_r]["id"] = $t_id;
      $array_raw[$cpt_r]["label"] = $t_label;
      $array_raw[$cpt_r]["parent"] = $t_parent;

      $cpt_r++;
    }

    // reorder the task list
    while ($cpt_t < $cpt_r) {
      $t_id = $array_raw[$idx]["id"];
      $t_label = $array_raw[$idx]["label"];
      $t_parent = $array_raw[$idx]["parent"];
    
      $array_tsk[$cpt_t]["id"] = $t_id;
      $array_tsk[$cpt_t]["label"] = $t_label;
      $cpt_t++;

      while (list($key,$val) = each($array_raw)) {

	$c_id = $val["id"];

	if ($val["parent"] == $t_id) {
	  $array_tsk[$cpt_t]["id"] = $val["id"];
	  $array_tsk[$cpt_t]["label"] = "|--- ". $val["label"];
	  $array_child[$t_id] = 1; 
	  $cpt_t++; 
	}
      }

      reset($array_raw);
      $idx++;
    }

    // list the members of the project
    while ($members_q->next_record()) {
      
      $m_id = $members_q->f("userobm_id");
      $m_task = $members_q->f("projectuser_projecttask_id");
      $m_proj = $members_q->f("projected_time");

      $array_time[$m_task][$m_id] = $m_proj;

      if ($m_id != $prev_id){

	$prev_id = $m_id;
	$m_name = $members_q->f("userobm_lastname");

	$array_mem[$cpt_m]["id"] =$m_id;
	$array_mem[$cpt_m]["name"] = $m_name;
	
	$cpt_m++;
      }
    }

    $dis_memhead = "<td>&nbsp;</td>";

    // diplay the members of the project
    for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {
      $m_id = $array_mem[$cpt_e]["id"];
      $m_name = $array_mem[$cpt_e]["name"];
      $dis_memhead .= "<td><input type=\"checkbox\" name=\"cb_user$m_id\"/></td> ";
      $dis_memhead .= "<td class=\"detailLabel\">$m_name</td>\n";
    }

    // display the rest of the array (ie. tasknames - projected times)
    for ($cpt_d=0; $cpt_d<$cpt_t; $cpt_d++) {

      $t_id = $array_tsk[$cpt_d]["id"];
      $t_label = $array_tsk[$cpt_d]["label"];

      $t_parent = $array_child[$t_id];

      $dis_member .= "<tr>\n<td class=\"detailLabel\">$t_label</td>\n";
      
      //ecrit la ligne pour un tache sans enfants
      for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {

	$m_id = $array_mem[$cpt_e]["id"];
	$tm_proj = $array_time[$t_id][$m_id];

	if ($t_parent) {
 	  $dis_member .= "<td class=\"detailText\" colspan=\"2\">&nbsp;</td>\n";
	} else {
	  $dis_member .= "<td colspan=\"2\">";
	  $dis_member .= "<input type=\"text\" name=\"tf_projected[$t_id][$m_id]\" size=\"10\" maxlength=\"8\" value=\"$tm_proj\"/>";
	  $dis_member .= "</td>\n";
	}
      }
      
      $dis_member .= "</tr>\n";
    }
    
    $dis_button  = "<td>";
    $dis_button .= " <input type=\"submit\" value=\"$l_del_member_sel\"";
    $dis_button .= "  onclick=\"submit_memberdel(this.form)\" />";
    $dis_button .= "</td><td>";
    $dis_button .= " <input type=\"submit\" value=\"$l_valid_member\"";
    $dis_button .= "  onclick=\"if(check_membervalid(this.form, $cpt_m)) return true; else return false;\" />";
    $dis_button .= "</td>";
    
    $span = $cpt_m*2 + 1;

  } else {

    $dis_memtab  = "<tr>\n";
    $dis_memtab .= "  <td class=\"detailText\">$l_no_members</td>\n";
    $dis_memtab .= "</tr>\n";
    
    $dis_button  = "<td>";
    $dis_button .= "  <input type=\"submit\" value=\"$l_member_add\"";
    $dis_button .= "   onclick=\"if(submit_memberadd(this.form, $pid)) return true; else return false;\" />";
    $dis_button .= "</td>";
    
    $span = 1;

  }
 
   echo "
     <form name=\"form_memberlist\" method=\"post\">
     <table class=\"details\">
      <tr>
       <td colspan=\"$span\" class=\"detailHead\">$l_membersinfos</td>
      </tr><tr>
       $dis_memhead
      </tr>
      $dis_member
     </table>
      <input type=\"hidden\" name=\"param_project\" value=$pid>
     <table class=\"details\">
     <tr>$dis_button</tr>
     </table>
     </form>
     ";
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Project Advance Form                                         //
// Parameters :
//   - $members_q  : DBO : list of members
//   - $tasks_q    : DBO : list of tasks related to the deal
//   - $project[]   : project search criteria
//     keys used    : id
///////////////////////////////////////////////////////////////////////////////
function html_project_advance($members_q, $tasks_q, $project) {
  global $l_name, $l_tasktype, $l_company_name, $l_soldtime;
  global $l_projected, $l_used, $l_remaining, $l_prevmissing, $l_nextmissing;
  global $l_advanceinfos, $l_adv_update;

  //smlp tout refaire la
  $idx = 0;
  $cpt_r=0;
  $cpt_t=0;
  $cpt_m=0;
  $pid = $project["id"];

  // list the tasks of the project
  while ($tasks_q->next_record()) {
    $t_id = $tasks_q->f("projecttask_id");
    $t_parent = $tasks_q->f("projecttask_parent");
    $t_label = $tasks_q->f("projecttask_label");

    $array_raw[$cpt_r]["id"] = $t_id;
    $array_raw[$cpt_r]["label"] = $t_label;
    $array_raw[$cpt_r]["parent"] = $t_parent;

    $cpt_r++;
  }

  // reorder the task list
  while ($cpt_t < $cpt_r) {
    $t_id = $array_raw[$idx]["id"];
    $t_label = $array_raw[$idx]["label"];
    $t_parent = $array_raw[$idx]["parent"];
    
    $array_tsk[$cpt_t]["id"] = $t_id;
    $array_tsk[$cpt_t]["label"] = $t_label;
    $cpt_t++;

    while (list($key,$val) = each($array_raw)) {

      $c_id = $val["id"];

      if ($val["parent"] == $t_id) {
	$array_tsk[$cpt_t]["id"] = $val["id"];
	$array_tsk[$cpt_t]["label"] = "|--- ". $val["label"];
	$array_child[$t_id] = 1; 
	$cpt_t++; 
      }
    }

    reset($array_raw);
    $idx++;
  }

  // list the members of the project
  while ($members_q->next_record()) {
      
    $m_id = $members_q->f("userobm_id");
    $m_task = $members_q->f("projectuser_projecttask_id");
    $m_proj = $members_q->f("projected_time");
    $m_miss = $members_q->f("missing_time");

    $array_time["miss"][$m_task][$m_id] = $m_miss;
    $array_time["proj"][$m_task][$m_id] = $m_proj;

    if ($m_id != $prev_id){

      $prev_id = $m_id;
      $m_name = $members_q->f("userobm_lastname");

      $array_mem[$cpt_m]["id"] = $m_id;
      $array_mem[$cpt_m]["name"] = $m_name;
	
      $cpt_m++;
    }
  }

  $dis_memhead = "<td>&nbsp;</td>";

  // diplay the members of the project
  for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {
    $m_id = $array_mem[$cpt_e]["id"];
    $m_name = $array_mem[$cpt_e]["name"];
    $dis_memhead .= "<td class=\"detailLabel\">$m_name</td>\n";
  }

  // display the rest of the array (ie. tasknames - projected times)
  for ($cpt_d=0; $cpt_d<$cpt_t; $cpt_d++) {

    $t_id = $array_tsk[$cpt_d]["id"];
    $t_label = $array_tsk[$cpt_d]["label"];

    $t_parent = $array_child[$t_id];

    $dis_member .= "<tr>\n<td class=\"detailLabel\">$t_label</td>\n";
      
    //ecrit la ligne pour un tache sans enfants
    for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {

      $m_id = $array_mem[$cpt_e]["id"];
      $tm_proj = $array_time["proj"][$t_id][$m_id];
      $tm_miss = $array_time["miss"][$t_id][$m_id];

      if ($tm_proj != 0) {
	$dis_member .= "<td>";
	$dis_member .= "<input type=\"text\" name=\"tf_missing[$t_id][$m_id]\" size=\"10\" maxlength=\"8\" value=\"$tm_miss\"/>";
	$dis_member .= "</td>\n";
      } else {
	$dis_member .= "<td class=\"detailText\">&nbsp;</td>\n";
      }
    }
      
    $dis_member .= "</tr>\n";
  }
    
  $dis_button = "<table class=\"details\">
                   <tr>
                    <td align=\"center\">
                     <input type=\"hidden\" name=\"action\" value=\"a_update\" />
                     <input type=\"hidden\" name=\"param_project\" value=$pid />
                     <input type=\"submit\" value=\"$l_adv_update\" />
                    </td>
                   </tr>
                  </table>
                 </form>
                ";
    
  $span = $cpt_m + 1;

  // --- HTML Template --------------------------------------------------------
  
  echo "
     <form method=\"post\" name=\"form_advance2\"
         onsubmit=\"if (check_advancevalid(this)) return true; else return false;\"
         action=\"".url_prepare("project_index.php")."\">
     <table class=\"details\">
       <tr>
        <td colspan=\"$span\" class=\"detailHead\">$l_membersinfos</td>
       </tr>
       <tr><td colspan=$span>&nbsp;</td></tr>
       $dis_memhead
       $dis_member
     </table>
      $dis_button
     </form>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the Project Display preference screen                             //
// Parameters:
//   - $display_newpref_q    : DBO : Field list to display
//   - $display_searchpref_q : DBO : Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_project_display_pref($display_newpref_q, $display_searchpref_q) {
  global $l_newproject_display, $l_searchproject_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $display_newpref_q);
  $dis_pref->display_entity = "project_new"; 
  $dis_pref->pref_title = $l_newproject_display;

  echo "<table class=\"details\"><tr><td>";
  $dis_pref->display();
  echo "</td>";

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $display_searchpref_q);
  $dis_pref->display_entity = "project"; 
  $dis_pref->pref_title = $l_searchproject_display;

  echo "<td>";
  $dis_pref->display();
  echo "</td></tr></table>";

  $dis_pref->dis_pref_help();
}

</SCRIPT>
