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
// Display Company search Form                                               //
// Parameters:
//   - $kind_q    : database object with kind list
//   - $act_q     : database object with activity list
//   - $usr_q     : database object with userobm list
//   - $company[] : default form values
//     keys used  : state, name, kind, zip, mm
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
// Display Company search Form                                               //
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
    $dis_action .= "<input type=\"hidden\" name=\"hd_state\" value=2 />";
  } else {
    $dis_action = "<input type=\"hidden\" name=\"action\" value=\"insert\" />";
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
        <input type=\"submit\" value=\"$l_button\" />
       </td>
      </tr>
     </table>
    </form>
";

}


///////////////////////////////////////////////////////////////////////////////
// Display Company search Form                                               //
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
    $dis_action .= "<input type=\"hidden\" name=\"hd_state\" value=1 />";
  } else {
    $dis_action = "<input type=\"hidden\" name=\"action\" value=\"create\" />";
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
        <input type=\"submit\" value=\"$l_button\" />
       </td>
      </tr>
     </table>
     </form>
";

}

///////////////////////////////////////////////////////////////////////////////
// Display Company search Form                                               //
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
//     keys used  : state, name, kind, zip
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
// HTML Display Search result                                                //
// Parameters : 
//   - $obm_q_comp : list of companies
//   - $pref_q     : the fields which have to be displayed
//   - $nb_company : nb companies returned by the search query
//   - $company[]  : company search criteria
//     keys used   : state, name, kind, zip
///////////////////////////////////////////////////////////////////////////////
function html_project_new_list($obm_q_comp, $pref_q, $nb_newprojects, $project) {
  global $l_found;

  $url = url_prepare("project_index.php?action=index");

  $dis_newprojects_list = new OBM_DISPLAY("DATA", $pref_q);
  $dis_newprojects_list->data_set = $obm_q_comp;
  $dis_newprojects_list->data_url = $url;
  $dis_newprojects_list->data_header = "both";
  $dis_newprojects_list->data_order = false;

  $dis_newprojects_list->display("dis_data_project");
}


///////////////////////////////////////////////////////////////////////////////
// Display the Project search result                                         //
// Parameters:
//   - $deal[]    : deal search criteria
//     keys used  : company_id, name, company
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
//   - $obm_q_deals : list of deals
//   - $pref_q      : fields to display in the list of deals 
//   - $nb_deal     : nb deals found
//   - $deal[]      : deal search criteria
//     keys used    : parent, label, company_name, archive, kind, cat, state,
//                    manager, dateafter, datebefore
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

  $dis_projects_list=new OBM_DISPLAY("DATA", $pref_q);
  $dis_projects_list->data_set = $obm_q_projects;
  $dis_projects_list->data_url = $url;
  $dis_projects_list->data_header = "both";

  // --- HTML Template --------------------------------------------------------

  display_info_msg($nb_project." ".$l_found);

  $dis_projects_list->display("dis_data_project");

}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Contact Consult                                              //
// Parameters :
//   -  $deal_q    : DBO : information about the deal
//   -  $con_q     : DBO : list of contact from the company 
//   -  $comp_id   : id of the company concerned by the deal
///////////////////////////////////////////////////////////////////////////////
function html_project_infos($project_q, $project) {
  global $l_name, $l_tasktype, $l_company_name, $l_soldtime;
  global $l_interninfos, $l_externinfos, $l_membersinfos;

  $name = $project_q->f("project_label");
  $tt = $project_q->f("project_tasktype_label");
  $company = $project_q->f("project_company_name");
  $state = $project_q->f("project_state");
  $soldtime = $project_q->f("project_soldtime");

  // Table header
  $title = ($state == 1 ? $l_externinfos : $l_interninfos);

  // Company name
  if ($state == 1) {
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
// HTML Display Contact Consult                                              //
// Parameters :
//   -  $deal_q    : DBO : information about the deal
//   -  $con_q     : DBO : list of contact from the company 
//   -  $comp_id   : id of the company concerned by the deal
///////////////////////////////////////////////////////////////////////////////
function html_project_consult($project_q, $members_q, $project) {
  global $l_name, $l_tasktype, $l_company_name, $l_soldtime;
  global $l_interninfos, $l_externinfos, $l_membersinfos, $l_advanceinfos;
  global $l_projected, $l_used, $l_remaining, $l_missing, $l_diff, $l_sold;
  global $l_allotted, $l_no_members, $l_adv_update;
  global $l_allosold, $l_usedsold, $l_progeval, $l_progress;

  $allotted = 0;
  $usedtime = 0;
  $missingtime = 0;

  $p_id = $project["id"];

  $name = $project_q->f("project_label");
  $tt = $project_q->f("project_tasktype_label");
  $company = $project_q->f("project_company_name");
  $state = $project_q->f("project_state");
  
  // Table header
  $title = ($state == 1 ? $l_externinfos : $l_interninfos);

  // Company name
  if ($state == 1) {
    $dis_company = "<td class=\"detailLabel\">$l_company_name</td>
                    <td class=\"detailText\">$company</td>
                    </tr><tr>";
  }

  // project members stats
  if ($members_q->num_rows() != 0) {

    $dis_members = "<tr>
                      <td class=\"detailLabel\">&nbsp;$l_name&nbsp;</td>
                      <td class=\"detailLabel\">&nbsp;$l_projected&nbsp;</td>
                      <td class=\"detailLabel\">&nbsp;$l_used&nbsp;</td>
                      <td class=\"detailLabel\">&nbsp;$l_remaining&nbsp;</td>
                      <td class=\"detailLabel\">&nbsp;$l_missing&nbsp;</td>
                      <td class=\"detailLabel\">&nbsp;$l_diff&nbsp;</td>
                    </tr>";

    while($members_q->next_record()) {
      $m_name = $members_q->f("userobm_lastname") ." ". $members_q->f("userobm_firstname");

      $projected = $members_q->f("projected_time");
      $used = $members_q->f("used_time");
      $used = number_format($used, 1);
      $remaining = $projected - $used;
      $missing = $members_q->f("missing_time");
      $diff = $remaining - $missing;

      $allotted += $projected;
      $usedtime += $used;
      $missingtime += $missing;

      $dis_members .= "<tr>
                       <td class=\"detailLabel\">$m_name &nbsp;&nbsp;</td>
                       <td class=\"detailText\">$projected</td>
                       <td class=\"detailText\">$used</td>
                       <td class=\"detailText\">$remaining</td>
                       <td class=\"detailText\">$missing</td>
                       <td class=\"detailText\">$diff</td>
                       </tr>";
    }
  }
  else {
    $dis_members .= "<tr><td class=\"detailText\" colspan=5>$l_no_members</td></tr>";
  }

  // Project advance stats
  $soldtime = $project_q->f("project_soldtime");
  $remainingtime = $soldtime - $usedtime;
  $difftime = $remainingtime - $missingtime;

  $ratio_allosold = intval(($allotted/$soldtime)*100);
  $ratio_usedsold = intval(($usedtime/$soldtime)*100);

  //bcontins temporaire a virer
  if (($usedtime + $missingtime) == 0)
    $ratio_progress = 0;
  else
    $ratio_progress = intval(($usedtime/($usedtime + $missingtime))*100);
  
  $ratio_progeval = intval((($usedtime + $missingtime)/$soldtime)*100);

  // smlp faire les pourcentages
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

  $dis_button = "<form method=\"post\" name=\"form_advance\"
                  action=\"".url_prepare("project_index.php")."\">
                  <table class=\"details\">
                   <tr>
                    <td align=\"center\">
                     <input type=\"hidden\" name=\"action\" value=\"advanceupdate\" />
                     <input type=\"hidden\" name=\"param_project\" value=$p_id />
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
        <td colspan=\"6\" class=\"detailHead\">$l_membersinfos</td>
       </tr>
       <tr><td colspan=6>&nbsp;</td></tr>
       $dis_members
     </table>

     $dis_button";
}

///////////////////////////////////////////////////////////////////////////////
// HTML Display Contact Consult                                              //
// Parameters :
//   -  $deal_q    : DBO : information about the deal
//   -  $con_q     : DBO : list of contact from the company 
//   -  $comp_id   : id of the company concerned by the deal
///////////////////////////////////////////////////////////////////////////////
function html_project_memberlist($members_q, $project) {
  global $l_membersinfos, $l_del_member_sel, $l_valid_member, $l_member_add;
  global $l_no_members;

  $id = $project["id"];

  if (($members_q != 0) && ($members_q->num_rows() != 0)) {

    while ($members_q->next_record()) {
      
      $m_id = $members_q->f("userobm_id");
      $m_name = $members_q->f("userobm_lastname") ." ". $members_q->f("userobm_firstname");
      $m_miss = $members_q->f("missing_time");
      
      $dis_members .= "<tr>\n";
      $dis_members .= "  <td align=\"right\">";
      $dis_members .= "    <input type=\"checkbox\" name=\"cb_user$m_id\" size=\"10\" maxlength=\"8\" value=\"\"/>";
      $dis_members .= "  </td>\n";;
      $dis_members .= "  <td class=\"detailLabel\">&nbsp;$m_name&nbsp;</td>\n";
      $dis_members .= "  <td><input type=\"text\" name=\"tf_projected[$m_id]\" size=\"10\" maxlength=\"8\" value=\"$m_miss\"/></td>\n";;
      $dis_members .= "</tr>\n";
    }

    $dis_button  = "<td>";
    $dis_button .= "  <input type=\"submit\" value=\"$l_del_member_sel\"";
    $dis_button .= "   onclick=\"submit_memberdel(this.form)\" />";
    $dis_button .= "</td><td>";
    $dis_button .= "  <input type=\"submit\" value=\"$l_valid_member\"";
    $dis_button .= "   onclick=\"if(submit_valid(this.form)) return true; else return false;\" />";
    $dis_button .= "</td>";
    
  } else {

    $dis_members  = "<tr>\n";
    $dis_members .= "  <td class=\"detailText\">$l_no_members</td>\n";
    $dis_members .= "</tr>\n";
    
    $dis_button  = "<td>";
    $dis_button .= "  <input type=\"submit\" value=\"$l_member_add\"";
    $dis_button .= "   onclick=\"if(submit_addmember(this.form)) return true; else return false;\" />";
    $dis_button .= "</td>";
    
  }
 
  echo "
    <form name=\"form_memberlist\" method=\"post\">
    <table class=\"details\">
      <tr>
        <td colspan=\"6\" class=\"detailHead\">$l_membersinfos</td>
      </tr>
      <tr><td colspan=6>&nbsp;</td></tr>
      $dis_members
    </table>
     <table class=\"details\">
       <tr>
         $dis_button
         <input type=\"hidden\" name=\"param_project\" value=\"$id\" />
       </tr>
     </table>
     </form>
";


}

///////////////////////////////////////////////////////////////////////////////
// HTML Display Contact Consult                                              //
// Parameters :
//   -  $deal_q    : DBO : information about the deal
//   -  $con_q     : DBO : list of contact from the company 
//   -  $comp_id   : id of the company concerned by the deal
///////////////////////////////////////////////////////////////////////////////
function html_project_advance($members_q, $project) {
  global $l_name, $l_tasktype, $l_company_name, $l_soldtime;
  global $l_projected, $l_used, $l_remaining, $l_prevmissing, $l_nextmissing;
  global $l_advanceinfos, $l_adv_update;

  $p_id = $project["id"];

  $dis_members = "<tr>
                   <td class=\"detailLabel\">&nbsp;$l_name&nbsp;</td>
                   <td class=\"detailLabel\">&nbsp;$l_projected&nbsp;</td>
                   <td class=\"detailLabel\">&nbsp;$l_used&nbsp;</td>
                   <td class=\"detailLabel\">&nbsp;$l_remaining&nbsp;</td>
                   <td class=\"detailLabel\">&nbsp;$l_prevmissing&nbsp;</td>
                   <td class=\"detailLabel\">&nbsp;$l_nextmissing&nbsp;</td>
                  </tr>";

  while($members_q->next_record()) {
    $m_id = $members_q->f("userobm_id");
    $m_name = $members_q->f("userobm_lastname") ." ". $members_q->f("userobm_firstname");
    
    $projected = $members_q->f("projected_time");
    $used = $members_q->f("used_time");
    $remaining = $projected - $used;
    $missing = $members_q->f("missing_time");
    
    $dis_members .= "<tr>
                      <td class=\"detailLabel\">$m_name &nbsp;&nbsp;</td>
                      <td class=\"detailText\" align=\"right\">$projected</td>
                      <td class=\"detailText\" align=\"right\">$used</td>
                      <td class=\"detailText\" align=\"right\">$remaining</td>
                      <td class=\"detailText\" align=\"right\">$missing</td>
                      <td><input type=\"text\" name=\"tf_missing[$m_id]\" size=\"10\" maxlength=\"8\" value=\"\"/></td>
                     </tr>";
  }

  $dis_button = "<table class=\"details\">
                   <tr>
                    <td align=\"center\">
                     <input type=\"hidden\" name=\"action\" value=\"a_update\" />
                     <input type=\"hidden\" name=\"param_project\" value=$p_id />
                     <input type=\"submit\" value=\"$l_adv_update\" />
                    </td>
                   </tr>
                  </table>
                 </form>
                ";

  // --- HTML Template --------------------------------------------------------
  
  echo "
     <form method=\"post\" name=\"form_advance2\"
         action=\"".url_prepare("project_index.php")."\">
     <table class=\"details\">
       <tr>
        <td colspan=\"6\" class=\"detailHead\">$l_membersinfos</td>
       </tr>
       <tr><td colspan=6>&nbsp;</td></tr>
       $dis_members
     </table>
      $dis_button
     </form>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the Company Display preference screen                             //
// Parameters:
//   - $display_pref_q : DBO : Field list to display
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
