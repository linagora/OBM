<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : project_display.inc                                          //
//     - Desc : Project Display File                                         //
// 2003-07-08 Aliacom                                                        //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
 

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["project_name"] = $l_name;
$fieldnames["project_company"] = $l_company;
$fieldnames["project_tasktype"] = $l_tasktype;
$fieldnames["project_soldtime"] = $l_soldtime;
$fieldnames["project_estimatedtime"] = $l_estimatedtime;
$fieldnames["project_datebegin"] = $l_datebegin;
$fieldnames["project_dateend"] = $l_dateend;
$fieldnames["project_archive"] = $l_archive_first;


///////////////////////////////////////////////////////////////////////////////
// Display project specific dataset fields
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
    if ($OD->display_ext == "get_id") {
      $res["url"] = "javascript:check_get_id(".$OD->data_set->f("project_id").",'".addslashes($OD->data_set->f("project_name"))."');";
    } else if ($OD->display_ext == "get_id_url") {
      $res["url"] = "javascript:check_get_id_url('$ext_url',".$OD->data_set->f("project_id").");";
    } else {
      $res["url"] = "$path/project/project_index.php?action=detailconsult&amp;param_project=".$OD->data_set->f("project_id");
    }
  }

  else if ($fieldname == "project_archive") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = " ";
    }
  }

  else if ($fieldname == "project_datebegin") {
    $date = $OD->data_set->f($fieldname);
    $res["name"] = date_format($date);
  }

  else if ($fieldname == "project_dateend") {
    $date = $OD->data_set->f($fieldname);
    $res["name"] = date_format($date);
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Project search Form
// Parameters:
//   - $project[] : default form values
//     keys used  : name, company_name, tt, manager, member,
///////////////////////////////////////////////////////////////////////////////
function dis_project_search_form($project) {

  $tts = get_global_tasktype("project");
  $member_q = run_query_project_members();

  $block = html_project_search_form ($tts, $member_q, $project);
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Html Project search Form
// Parameters:
//   - $tts       : tasktype array
//   - $member_q  : database object with project member list
//   - $project[] : default form values
//     keys used  : name, company_name, tt, manager, member,
///////////////////////////////////////////////////////////////////////////////
function html_project_search_form ($tts, $member_q, $project) {
  global $l_name, $l_company, $l_tasktype, $l_member;
  global $l_all, $l_find, $l_archive;
  global $display, $c_all, $l_select_project, $l_date;
  global $ctt_sales, $ctt_research, $ctt_others;
  global $l_tt_sales, $l_tt_research, $l_tt_others;

  $popup = $project["popup"];
  $name = $project["name"];
  $company_name = $project["company_name"];
  $tt = $project["tt"];
  $member = $project["member"];
  $archive = $project["archive"];
  $date = $project["datebegin"];
  $company_id = $project["company"];
  $deal_id = $project["deal"];

  $task_list = array($ctt_sales => $l_tt_sales,
		     $ctt_research => $l_tt_research,
		     $ctt_others => $l_tt_others);

  // TaskType select construction
  $type_prec = -1;
  $sel_tt = "<select name=\"sel_tt\">
    <option value=\"$c_all\">$l_all</option>";
  if (is_array($tts)) {
    foreach ($tts as $id=>$one_tt) {
      $tt_label = $one_tt["label"];
      $internal = $one_tt["internal"];
      // put menu titles
      if ($internal != $type_prec) {
	$sel_tt .= "<option disabled=\"disabled\">".$task_list[$internal]." </option>";
	$type_prec = $internal;
      }
      $sel_tt .= "\n<option value=\"$id\"";
      if ($tt == $id) $sel_tt .= "selected =\"selected\"";
      $sel_tt .= ">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;$tt_label</option>\n";
    }
    $sel_tt .= "</select>";
  }

  // member select
  $sel_member = "<select id=\"sel_member\" name=\"sel_member\">
    <option value=\"$c_all\">$l_all</option>\n";
  while ($member_q->next_record()) {
    $mb_id = $member_q->f("member_id");
    $sel_member .= "<option value=\"$mb_id\"";
    if ($mb_id == $member) { $sel_member .= " selected=\"selected\""; }
    $sel_member .= ">". $member_q->f("member_lastname")." ".$member_q->f("member_firstname") . "</option>\n";
  }
  $sel_member .= "</select>";

  $checked = ($archive) ? "checked" : "";
  $cb_archive = "<input type=\"checkbox\" name=\"cb_archive\" value=\"1\" $checked />";

  $url = url_prepare("project_index.php");

  if ($popup) {
    $ext_action = $project["ext_action"];
    $ext_url = $project["ext_url"];
    $ext_id = $project["ext_id"];
    $ext_title = ($project["ext_title"] ? $project["ext_title"] : "$l_select_project");
    $ext_target = $project["ext_target"];
    $ext_widget = $project["ext_widget"];
    $ext_widget_text = $project["ext_widget_text"];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
            <input name=\"ext_widget\" type=\"hidden\" value=\"$ext_widget\">
            <input name=\"ext_widget_text\" type=\"hidden\" value=\"$ext_widget_text\">
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
    $display["title"] = "<div class=\"title\">$ext_title</div>";
  }

  // --- HTML Template --------------------------------------------------------

  $block = "
    <form method=\"get\" name=\"f_search\" id=\"f_search\" action=\"$url\">

    <div class=\"search\">
      <div class=\"searchLabel\">$l_name<br />
        <input type=\"text\" name=\"tf_name\" size=\"20\" value=\"$name\" />
      </div>
      <div class=\"searchLabel\">$l_company<br />
        <input type=\"text\" name=\"tf_company_name\" id=\"tf_company_name\" size=\"20\" value=\"$company_name\" />
      </div>
      <div class=\"searchLabel\">$l_tasktype<br />
        $sel_tt
      </div>
      <div class=\"searchLabel\">$l_member<br />
        $sel_member
      </div>
      <div class=\"searchLabel\">$l_date<br />
        <script>calendar('tf_datebegin','$date')</script>
      </div>
      <div class=\"searchLabel\">$l_archive<br />
        $cb_archive
      </div>
      <div class=\"searchLabel\">&nbsp;<br />
        <input name=\"action\" type=\"hidden\" value=\"search\" />
        <input name=\"param_company\" type=\"hidden\" value=\"$company_id\" />
        <input name=\"param_deal\" type=\"hidden\" value=\"$deal_id\" />
        <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
        <input name=\"popup\" type=\"hidden\" value=\"$popup\" />      
       $ext
      </div>
      <div class=\"searchEnd\"></div>
    </div>

    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Project search result
// Parameters:
//   - $project[] : project search criteria
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function dis_project_search_list($project) {
  global $display, $auth, $l_found, $l_no_found;

  $new_order = $project["new_order"];
  $order_dir = $project["order_dir"];

  $prefs = get_display_pref($auth->auth["uid"], "project", 0);
  $obm_q = run_query_project_search($project, $new_order, $order_dir);
  $nb_project = $obm_q->num_rows_total();

  if ($nb_project == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $display["msg"] .= display_info_msg($nb_project." ".$l_found);
    $block = html_project_search_list($obm_q, $prefs, $project);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display the Project Search result
// Parameters:
//   - $proj_q    : list of projects
//   - $prefs     : fields to display in the list of projects 
//   - $project[] : project search criteria
//     keys used  : name, company_name, tt, manager, member
///////////////////////////////////////////////////////////////////////////////
function html_project_search_list($proj_q, $prefs, $project) {
  global $l_close;

  $popup = $project["popup"];
  $name = $project["name"];
  $company_name = $project["company_name"];
  $tt = $project["tt"];
  $member = $project["member"];
  $archive = $project["archive"];

  if ($popup) {
    $ext_action = $project["ext_action"];
    $ext_url = $project["ext_url"];
    $ext_id = $project["ext_id"];
    $ext_target = $project["ext_target"];
    $ext_widget = $project["ext_widget"];
    $ext_widget_text = $project["ext_widget_text"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text&amp;popup=1";
  }

  $url = url_prepare("project_index.php?action=search&amp;tf_name=$name&amp;tf_company_name=$company_name&amp;sel_tt=$tt&amp;sel_member=$member&amp;cb_archive=$archive$url_ext");

  $proj_d = new OBM_DISPLAY("DATA", $prefs, "project");
  if ($popup) {
    $proj_d->display_link = false;
    if ($ext_url != "") {
      $proj_d->display_ext = "get_id_url";
    } else if ( ($ext_widget != "") && ($ext_widget_text != "") ) { 
      $proj_d->display_ext = "get_id";
    }
   $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  $proj_d->data_set = $proj_q;
  $proj_d->data_url = $url;
  $proj_d->data_header = "both";
  $block = $proj_d->display("dis_data_project");
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Project links menu
// Parameters:
//   - $p_q : project database result 
// Returns:
//   $r : string with XHTML code
///////////////////////////////////////////////////////////////////////////////
function html_project_links($p_q) {
  global $set_theme, $ico_document, $ico_document_new, $ico_invoice;
  global $l_new, $l_module_invoice, $l_module_document;
  global $l_document_add, $l_project;
  global $path, $auth, $cgp_show, $popup_height, $popup_width;

  $id = $p_q->f("project_id");
  $name = $p_q->f("project_name");
  $uname = urlencode($name);
  $cid = $p_q->f("project_company_id");
  $cname = $p_q->f("company_name");
  $ucname = urlencode($cname);
  $did = $p_q->f("project_deal_id");
  $dlabel = $p_q->f("deal_label");
  $udlabel = urlencode($dlabel);

  // Document
  if ($cgp_show["module"]["document"]) {
    $url_doc = url_prepare("$path/document/document_index.php?action=search&amp;param_entity=$id&amp;entity=project");
    $url_doc_new = url_prepare("$path/document/document_index.php?action=new&amp;param_entity=$id&amp;entity=project");
    $url_doc_add = "$path/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_action=document_add&amp;ext_url=".urlencode($path."/project/project_index.php")."&amp;ext_id=$id&amp;ext_target=$l_project";
    $nb_document = run_query_global_document_nb ($id, "project");
    $block_doc = "
  <div class=\"linkTitle\">:: $l_module_document</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_doc\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_document\" /></a>
        <a href=\"$url_doc\">$l_module_document ($nb_document)</a></li>
    <li><a href=\"$url_doc_new\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_document_new\" /></a>
        <a href=\"$url_doc_new\">$l_new</a></li>
    <li><a href=\"\" 
	 onclick=\"window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;
	 \"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_document_new\" /></a>
	<a href=\"\" onclick=\"window.name='$l_project'; window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
	 $l_document_add</a></li>
  </ul>";
  }

  // Invoice
  if ($cgp_show["module"]["invoice"]) {
    $url_inv = url_prepare("$path/invoice/invoice_index.php?action=search&amp;param_project=$id");
    $url_inv_tot = url_prepare("$path/invoice/invoice_index.php?action=search&amp;param_project=$id&amp;cb_archive=1");
    $url_inv_new = url_prepare("$path/invoice/invoice_index.php?action=new&amp;param_project=$id&amp;project_name=$uname&amp;param_company=$cid&amp;company_name=$ucname&amp;param_deal=$did&amp;deal_label=$udlabel");
    $nb_inv_tot = get_global_linked_invoice_nb($id, "project", 1);
    $nb_inv = get_global_linked_invoice_nb($id, "project");
    $block_invoice = "
  <div class=\"linkTitle\">:: $l_module_invoice</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_inv\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_invoice\" alt=\"\" /></a>
        $l_module_invoice ( <a href=\"$url_inv\">$nb_inv</a> /
        <a href=\"$url_inv_tot\">$nb_inv_tot</a> )</li>
    <li><a href=\"$url_inv_new\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_invoice\" alt=\"\" /></a>
        <a href=\"$url_inv_new\">$l_new</a></li>
  </ul>";
  }

  // Links Template  
  $block = "
<div class=\"link\"> 
  $block_doc
  $block_invoice
</div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Project detail page
// Parameters:
//   - $pid : project id
///////////////////////////////////////////////////////////////////////////////
function dis_project_consult($pid) {
  global $display, $l_query_error;

  $project_q = run_query_project_detail($pid);
  $tasks_q = run_query_project_tasks($pid);
  $members_q = run_query_project_members($pid);
  $times = run_query_project_allocation_and_time($pid);

  if ($project_q->num_rows() != 1) {
    $display["msg"] .= display_err_msg($l_query_error);
  }

  $display["detailInfo"] = display_record_info($project_q);
  $block = html_project_consult($project_q, $tasks_q, $members_q, $times);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Project Consult (main infos, progress infos, members infos)  //
// Parameters :
//   - $project_q : DBO : information about the project
//   - $tasks_q   : DBO : list of tasks in the project 
//   - $members_q : DBO : list of members and related time infos
//   - $times     : array of hash with uid,task_id,allo,miss,used valued 
//      keys used : id
///////////////////////////////////////////////////////////////////////////////
function html_project_consult($project_q, $tasks_q, $members_q, $times) {
  global $path, $set_theme, $display, $action, $ico_company, $ico_deal;
  global $l_name, $l_tasktype, $l_company, $l_total, $l_datebegin, $l_dateend;
  global $l_deal, $l_project, $l_progress, $l_progressinfos;
  global $l_used, $l_missing, $l_sold, $l_estimated, $l_comment;
  global $l_allotted, $l_no_members, $l_progress_update, $l_project, $l_resume;
  global $l_alloestimated, $l_usedestimated, $l_progeval, $l_tasks;
  global $l_members, $l_task_list, $l_members_info, $l_archive, $l_yes, $l_no;
  global $l_alloproj, $l_usedproj, $l_no_tasks;

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
  $estimated = $project_q->f("project_estimatedtime");
  $datebegin = date_format($project_q->f("datebegin"), 1);
  $dateend = date_format($project_q->f("dateend"), 1);
  $comment = beautify_comment(nl2br($project_q->f("project_comment")));

  // Labels are different for sales and R&D projects
  if ($company > 0) {
    $text_time = "$l_sold / $l_estimated";
    $value_time = "$soldtime / $estimated";
  } else {
    $text_time = $l_estimated;
    $value_time = $estimated;
  }

  // Company name
  if ($company > 0) {
    if ($deal > 0) {
      $dis_deal = "
    </tr><tr>
      <td class=\"detailLabel\">$l_deal</td>
      <td class=\"detailText\">
        <a href=\"". url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;param_deal=$deal")."\">
        <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_deal\" alt=\"[details]\" /></a>
        $deal_label
      </td>";
    }
    $dis_comp = "
    <div class=\"detailHead\">$l_company</div>
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_company</td>
      <td class=\"detailText\">
        <a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=$company")."\">
        <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_company\" alt=\"[details]\" /></a>
        $company_name
      </td>
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


  //---------------------------------------------------------------------------
  // tasks or members not defined, we just display task or member list
  //---------------------------------------------------------------------------
  if (!($has_tasks) or !($has_members)) {
    $tab_style = "class=\"detail\"";
    $dis_progress = "
     <tr>
       <td class=\"detailLabel\">&nbsp;$l_sold / $l_estimated&nbsp;</td>
       <td class=\"detailText\" align=\"right\">$soldtime / $estimated</td>
       <td width=\"10%\">&nbsp;&nbsp;</td>
       <td class=\"detailLabel\">&nbsp;$l_allosold&nbsp;</td>
       <td class=\"detailText\" align=\"right\">0 %</td>
      </tr>";    

    // we display the task list
    if ($has_tasks) { 
      $dis_member .= "
       <tr>
        <td class=\"detailLabel\" colspan=\"2\">$l_task_list :</td>
       </tr>";

      while ($tasks_q->next_record()) {
	$tsk_name = $tasks_q->f("projecttask_label");
	$img = ($tasks_q->f("parent_id")) ? "<img border=\"0\" src=\"".C_IMAGE_PATH."/$set_theme/hierarchy.png\" alt=\"\" />" : "";
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

    if ($has_members) {
      $dis_member .= "
       <tr>
        <td class=\"detailLabel\" colspan=\"2\">$l_members_info :</td>
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

  //---------------------------------------------------------------------------
  // Display the detailled progress array
  //---------------------------------------------------------------------------
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

      if ($t_parent) {
	$array_child[$t_parent] = 1;
      }
      $array_parent[$t_id] = $t_parent;

      $cpt_t++; 
    }

    // list the members of the project
    while (list($key,$cell) = each($times)) {
      $m_id = $cell["uid"];
      $m_task = ($cell["task_id"] ? $cell["task_id"] : 0);
      $m_proj = ($cell["allo"] ? $cell["allo"] : 0);
      $m_miss = ($cell["miss"] ? $cell["miss"] : 0);
      $m_used = ($cell["used"] ? $cell["used"] : 0);

      // time (used/missing/projected) for each (task/member) couple
      $array_time["proj"][$m_task][$m_id] = $m_proj;
      $array_time["miss"][$m_task][$m_id] = $m_miss;
      $array_time["used"][$m_task][$m_id] = $m_used;

      // totals by parent tasks
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

      //used for evaluation of the project progress
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
        <img border=\"0\" src=\"".C_IMAGE_PATH."/$set_theme/hierarchy.png\" alt=\"\" />
        $t_label</td>\n";
      }

      // write a line (corresponding to a task)
      for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {

	$m_id = $array_mem[$cpt_e]["id"];
	$tm_proj = $array_time["proj"][$t_id][$m_id];
	$tm_miss = $array_time["miss"][$t_id][$m_id];
	$tm_used = $array_time["used"][$t_id][$m_id];

	// We write the cell only if not 0/0/0
	if ( ( ($tm_used != "0.0") && ($tm_used != ""))
	     || ( ($tm_miss != "0") && ($tm_miss != "") )
             || ( ($tm_proj != "0") && ($tm_proj != "") ) ) {
	  $tm_label = number_format($tm_used, 1) ."/". $tm_miss ."/". $tm_proj;
	} else {
	  $tm_label = "";
	}

	if ($t_haschild) {
	  $dis_member .= "<td class=\"projectPtime\">&nbsp;</td>\n";
	} else if ($t_parent) {
	  $dis_member .= "<td class=\"projectPtime\">$tm_label</td>\n";
	} else {
	  $dis_member .= "<td class=\"projectCtime\">$tm_label</td>\n";
	}
      }

      if ($t_parent) {
	// write the total for a parent task
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

    // display the last line (totals by members)
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
    $ratio_alloestimated = intval(($allotted/$estimated)*100);
    $ratio_usedestimated = intval(($usedtime/$estimated)*100);

    $ratio_progress = (($usedtime + $missingtime) == 0) ? 0 : intval(($usedtime/($usedtime + $missingtime))*100);
    $ratio_progeval = intval((($usedtime + $missingtime)/$estimated)*100);

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
    $dis_progress = "
    <tr>
     <td class=\"detailLabel\">&nbsp;$text_time&nbsp;</td>
     <td class=\"detailText\" align=\"right\">$value_time</td>
     <td>&nbsp;&nbsp;</td>
     <td class=\"detailLabel\">&nbsp;$l_alloestimated&nbsp;</td>
     <td class=\"detailText\" align=\"right\">$ratio_alloestimated %</td>
    </tr>
    <tr>
     <td class=\"detailLabel\">&nbsp;$l_allotted&nbsp;</td>
     <td class=\"detailText\" align=\"right\">$allotted</td>
     <td>&nbsp;&nbsp;</td>
     <td class=\"detailLabel\">&nbsp;$l_usedestimated&nbsp;</td>
     <td class=\"detailText\" align=\"right\">$ratio_usedestimated %</td>
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

  $display["link"] = html_project_links($project_q);
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
      <td class=\"detailLabel\">$l_tasktype</td>
      <td class=\"detailText\">$tt</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_datebegin</td>
      <td class=\"detailText\">$datebegin</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_dateend</td>
      <td class=\"detailText\">$dateend</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_resume</td>
      <td class=\"detailText\"># $l_tasks : $nb_tsk / # $l_members : $cpt_m</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_archive</td>
      <td class=\"detailText\">$arc_status</td>
    </tr>
    </table>
 	
    <div class=\"detailHead\">$l_progress</div>

    <table class=\"detail\">
      $dis_progress
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

    <div class=\"detailHead\">$l_comment</div>
    <table class=\"detail\">
    <tr>
      <td class=\"detailText\">$comment</td>
    </tr>
    </table>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Project Form
// Parameters:
//   - $action    : 
//   - $project_q : database object with project infos
//   - $project[] : default form values
//     keys used  : name, id, tt, soldtime
///////////////////////////////////////////////////////////////////////////////
function html_project_form($action, $project_q, $project) {
  global $l_company, $l_deal, $l_project, $l_member, $l_insert, $l_update;
  global $l_name, $l_tasktype, $l_task, $l_projtime, $l_soldtime, $l_archive;
  global $l_datebegin, $l_dateend, $c_undef, $l_undef;
  global $l_comment, $l_add_comment, $l_upd_comment;
  global $uid, $cg_prod, $c_php_isodate_format;
  global $ctt_sales, $ctt_research, $ctt_others;
  global $l_tt_sales, $l_tt_research, $l_tt_others;

  $usr_q = run_query_all_users_from_group($cg_prod, array($uid));

  if ($action == "detailupdate") {
    $pid = $project_q->f("project_id");
    $name = $project_q->f("project_name");
    $tt = $project_q->f("project_tasktype_id");
    $company = $project_q->f("project_company_id");
    $deal = $project_q->f("project_deal_id");
    $soldtime = $project_q->f("project_soldtime");
    $estimated = $project_q->f("project_estimatedtime");
    $arc_val = ($project_q->f("project_archive")) ? "checked" : "";
    $company_name = $project_q->f("company_name");
    $deal_label = $project_q->f("deal_label");
    $datebegin = isodate_format($project_q->f("datebegin"), 1);
    $dateend = isodate_format($project_q->f("dateend"), 1);
    $comment = $project_q->f("project_comment");
    $usercomment = $uid;
    $datecomment = isodate_format();
  } else {
    if ($action == "new") {
      $name = $project["deal_label"];
      // We pre-fill datebegin and dateend with current date
      $datebegin = date($c_php_isodate_format); 
      $dateend = $datebegin;
      $usercomment = $uid;
      $datecomment = isodate_format();
    }
  }

  // If parameters have been given, they supercede the default action value
  if (isset($project["id"])) { $pid = $project["id"]; }
  if (isset($project["name"])) { $name = stripslashes($project["name"]); }
  if (isset($project["tt"])) { $tt = $project["tt"]; }
  if (isset($project["company"])) { $company = $project["company"]; }
  if (isset($project["deal"])) { $deal = $project["deal"]; }
  if (isset($project["soldtime"])) { $soldtime = $project["soldtime"]; }
  if (isset($project["estimated"])) { $estimated = $project["estimated"]; }
  if (isset($project["company_name"])) { $company_name = stripslashes($project["company_name"]); }
  if (isset($project["deal_label"])) { $deal_label = $project["deal_label"]; }
  if (isset($project["tasklabel"])) { $task = $project["tasklabel"]; }
  if (isset($project["member"])) { $member = $project["member"]; }
  if (isset($project["datebegin"])) { $datebegin = $project["datebegin"]; }
  if (isset($project["dateend"])) { $dateend = $project["dateend"]; }
  if (isset($project["archive"])) { $arc_val = ($project["archive"] == 1 ? "checked" : ""); }
  if (isset($project["comment"])) { $comment = stripslashes($project["comment"]); }
  if (isset($project["add_comment"])) { $add_comment = stripslashes($project["add_comment"]); }
  if (isset($project["usercomment"])) { $usercomment = $project["usercomment"]; }
  if (isset($project["datecomment"])) { $datecomment = $project["datecomment"]; }


  if (($action == "new") || ($action == "insert")) {
    // member select
    $mb_q = run_query_all_users_from_group($cg_prod);
    $sel_member = "<select id=\"sel_member\" name=\"sel_member\">
      <option value=\"$c_undef\">$l_undef</option>\n";
    while ($mb_q->next_record()) {
	$mb_id = $mb_q->f("userobm_id");
	$mb_name = $mb_q->f("userobm_lastname")." ".$mb_q->f("userobm_firstname");
	$sel_member .= "
      <option value=\"$mb_id\"";
	if ($mb_id == $member) { $sel_member .= " selected=\"selected\""; }
	$sel_member .= ">$mb_name</option>";
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

  // We select the task list depending on task category (or if company filled)
  if ($company > 0) {
    $tts = get_global_tasktype($ctt_sales);
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

    $dis_sold = "
    </tr><tr>
      <td class=\"detailLabel\">$l_soldtime</td>
      <td class=\"detailForm\"><input type=\"text\" name=\"tf_soldtime\" size=\"10\" maxlength=\"8\" value=\"$soldtime\" /></td>";

  } else {
    $tts = get_global_tasktype();
    $dis_comp = "";
  }

  // TaskType select construction
  $task_list = array($ctt_sales=>$l_tt_sales,
		     $ctt_research=>$l_tt_research,
		     $ctt_others=>$l_tt_others);
  $type_prec = -1;
  $sel_tt = "
        <select id=\"sel_tt\" name=\"sel_tt\">
          <option value=\"$c_undef\">$l_undef</option>";
  if (is_array($tts)) {
    foreach ($tts as $id=>$one_tt) {
      $tt_label = $one_tt["label"];
      $internal = $one_tt["internal"];
      // put menu titles
      if ($internal != $type_prec) {
	$sel_tt .= "<option disabled=\"disabled\">".$task_list[$internal]." </option>";
	$type_prec = $internal;
      }
      $sel_tt .= "\n<option value=\"$id\"";
      if ($tt == $id) $sel_tt .= "selected =\"selected\"";
      $sel_tt .= ">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;$tt_label</option>\n";
    }
    $sel_tt .= "</select>";
  }

  // archive field
  if ($action == "detailupdate") {
    $dis_archive = "<tr>
      <td class=\"detailLabel\">$l_archive</td>
      <td class=\"detailForm\"><input type=\"checkbox\" name=\"cb_archive\" $arc_val /></td>
     </tr>";
  } else {
    $dis_archive = "";
  }

  // User comment select construction
  if ($usr_q->nf()>0) {
    $usr_q->seek(0);
  }
  $sel_usercomment = "<select name=\"sel_usercomment\">";
  while ($usr_q->next_record()) {
    $cid = $usr_q->f("userobm_id");
    $cname = $usr_q->f("userobm_lastname")." ".$usr_q->f("userobm_firstname");
    $sel_usercomment .= "<option value=\"$cname\"";
    // First time default to uid of connected user (uid) then set to name
    if ( ($usercomment == $cid) || ($usercomment == $cname) ) {
      $sel_usercomment .= " selected = \"selected\"";
    }
    $sel_usercomment .= ">$cname</option>\n";
  }
  $sel_usercomment .= "</select>";

  // target action
  if (($action == "detailupdate") || ($action == "update")) {
    $display["title"] = "<div class=\"title\">$l_project : $name</div>";

    $dis_comment = "
      </tr><tr>
        <td class=\"detailLabel\">$l_upd_comment</td>
        <td>&nbsp;</td>
      </tr><tr>
        <td class=\"detailForm\" colspan=\"2\"><textarea name=\"ta_com\" rows=\"6\" cols=\"78\">$comment</textarea></td>
    ";

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
  
  // --- HTML Template --------------------------------------------------------
  
  $block = "
    <form method=\"post\" name=\"f_entity\" 
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
    $dis_sold
    </tr><tr>
      <td class=\"detailLabel\">$l_projtime</td>
      <td class=\"detailForm\"><input type=\"text\" name=\"tf_estimated\" size=\"10\" maxlength=\"8\" value=\"$estimated\" /></td>
    $dis_task
    $dis_member
    </tr><tr>
      <td class=\"detailLabel\">$l_datebegin</td>
      <td class=\"detailForm\"><script>calendar('tf_datebegin','$datebegin')</script></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_dateend</td>
      <td class=\"detailForm\"><script>calendar('tf_dateend','$dateend')</script></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_archive</td>
      <td class=\"detailForm\"><input type=\"checkbox\" name=\"cb_archive\" $arc_val /></td>
    </tr>
    </table>

    <div class=\"detailHead\">$l_comment</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_add_comment</td>
      <td class=\"detailForm\"><script>calendar('tf_datecomment','$datecomment')</script>$sel_usercomment</td>
    </tr><tr>
      <td class=\"detailForm\" colspan=\"2\"><textarea name=\"ta_add_comment\" rows=\"6\" cols=\"78\">$add_comment</textarea></td>
      $dis_comment
    </tr>
    </table>

   <div class=\"detailButton\">
   <p class=\"detailButtons\">$dis_button</p>
   </div>
   </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the project delete validation screen
// Parameters:
//   - $p_id : project id
///////////////////////////////////////////////////////////////////////////////
function dis_can_delete_project($p_id) {
  global $l_can_delete, $l_back, $l_delete;
  global $display;

  $url = url_prepare("project_index.php");

  $dis_back = "<form name=\"form_back\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"param_project\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"param_project\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onclick=\"if (confirm_del(this.form)) return true; else return false;\" />
    </form>";

  $display["msg"] .= display_ok_msg($l_can_delete);

  $block .= "
    <div class=\"detailButton\">
      <p class=\"detailButtons\">$dis_delete</p>
    <p class=\"detailButtons\">$dis_back</p>
    </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Task Add Form
// Parameters:
//   - $ptask_q   : database object with project tasks list
//   - $project[] : default form values
//     keys used  : id
///////////////////////////////////////////////////////////////////////////////
function html_project_task_form($ptask_q, $project) {
  global $display, $c_undef, $l_undef;
  global $l_name, $l_parent, $l_task_new, $l_task_add, $l_project;
  global $l_task_update, $l_task_upd, $l_task;
  
  $pid = $project["id"];
  $pname = $project["name"];
  $label = stripslashes($project["tasklabel"]);
  $task = $project["task"];
  $ptask = $project["ptask"];

  if (($ptask_q != 0) && ($ptask_q->num_rows() != 0)) {
    $cpt_root = 0;
    // parent task select
    $sel_ptask = "
        <select id=\"sel_ptask\" name=\"sel_ptask\">
          <option value=\"$c_undef\">$l_undef</option>\n";
    while ($ptask_q->next_record()) {
      $parent = $ptask_q->f("parent_id");
      if ($parent == 0) {
	$cpt_root++;
	$ptask_id = $ptask_q->f("projecttask_id");
	$ptask_label = $ptask_q->f("projecttask_label");
	if ($ptask_id == $ptask) {
	  $selected = "selected";
	} else {
	  $selected = "";
	}
	$sel_ptask .= "<option value=\"$ptask_id\" $selected>$ptask_label</option>\n";
	$js_task .= "parent_pos[$ptask_id] = $cpt_root;\n";
      }
    }
    $sel_ptask .= "</select>";

    // task select
    $sel_task = "
      <select id=\"sel_task\" name=\"sel_task\"
      onchange=\"fill_parenttask(this.form.sel_ptask, this.form.sel_task, this.form.tf_tasklabel)\" >";
    $ptask_q->seek(0);
    while ($ptask_q->next_record()) {
      $parent = $ptask_q->f("parent_id");
      if ($parent == 0) {
	$prefix = "";
      } else {
	$prefix = "&nbsp;&nbsp;&nbsp;&nbsp;";
      }	
      $task_id = $ptask_q->f("projecttask_id");
      $task_label = $ptask_q->f("projecttask_label");
      if ($task_id == $task) {
	$selected = "selected";
      } else {
	$selected = "";
      }
      $sel_task .= "<option value=\"$task_id\" $selected>$prefix$task_label</option>\n";
      $js_task .= "parent_task[$task_id] = $parent;\n";
    }

    $sel_task .= "</select>";
    $ptask_q->seek(0);
  }

  $js_script = "
    <script type=\"text/javascript\">
      parent_pos = new Array();
      parent_task = new Array();
      parent_pos[0] = 0;
      $js_task
    </script>\n";

  $display["title"] = "<div class=\"title\">$l_project : $pname</div>";

  // --- HTML Template --------------------------------------------------------

  $block = "
   $js_script
   <form method=\"post\" name=\"f_entity\"
     onsubmit=\"if (check_taskadd(this)) return true; else return false;\"
     action=\"". url_prepare("project_index.php") ."\">
 
    <div class=\"detailHead\">$l_task_new</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailForm\"><input type=\"text\" name=\"tf_tasklabel\" size=\"30\" value=\"\" /></td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_parent</td>
      <td class=\"detailForm\">
      $sel_ptask
      </td>
    </tr>
    </table>

    <div class=\"detailButton\">
    <p class=\"detailButtons\">
     <input type=\"hidden\" name=\"param_project\" value=\"$pid\" />
     <input type=\"hidden\" name=\"action\" value=\"task_add\" />
     <input type=\"submit\" value=\"$l_task_add\" />
    </p>
    </div>
  </form>    

  <form method=\"post\" name=\"form_taskupd\"
     onsubmit=\"if (check_taskupd(this)) return true; else return false;\"
     action=\"". url_prepare("project_index.php") ."\">

    <p class=\"detailButtons\">&nbsp;</p>

    <div class=\"detailHead\">$l_task_update</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_task</td>
      <td class=\"detailForm\">
      $sel_task
      </td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_parent</td>
      <td class=\"detailForm\">
      $sel_ptask
      </td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailForm\"><input type=\"text\" name=\"tf_tasklabel\" size=\"30\" value=\"\" /></td>
    </tr>
    </table>

    <div class=\"detailButton\">
    <p class=\"detailButtons\">
     <input type=\"hidden\" name=\"param_project\" value=\"$pid\" />
     <input type=\"hidden\" name=\"action\" value=\"task_update\" />
     <input type=\"submit\" value=\"$l_task_upd\" />
    </p>
    </div>

   </form>
   ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Project Tasklist
// Parameters :
//   - $tasks_q     : DBO : list of tasks related to the deal
//   - $project[]   : project search criteria
//     keys used    : id
///////////////////////////////////////////////////////////////////////////////
function html_project_tasklist($tasks_q, $project) {
  global $set_theme;
  global $l_task_list, $l_task_del, $l_no_tasks;

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
           <img border=\"0\" src=\"".C_IMAGE_PATH."/$set_theme/hierarchy.png\" alt=\"\" />$t_label
          </td>
         </tr>";
      }
   }
    
    $dis_button  = "
      <input type=\"hidden\" name=\"param_project\" value=\"$pid\" />
      <input type=\"hidden\" name=\"action\" value=\"task_del\" />
      <input type=\"submit\" value=\"$l_task_del\" />";

  } else {
    
    $dis_task = "<tr><td class=\"detailLabel\">$l_no_tasks</td></tr>\n";
  }
  
  $block = "
    <p class=\"detailButtons\">&nbsp;</p>

    <div class=\"detailHead\">$l_task_list</div>

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
// HTML Display Project Member List
// Parameters :
//   - $tasks_q     : DBO : list of tasks related to the deal
//   - $members_q   : DBO : list of members
//   - $project[]   : project search criteria
//     keys used    : id
///////////////////////////////////////////////////////////////////////////////
function html_project_member_form($members_q, $project) {
  global $display;
  global $l_no_members, $l_members_info, $l_del_member_sel;
  global $l_project, $l_project_manager, $l_member;

  $pid = $project["id"];
  $pname = $project["name"];

  if (($members_q != 0) && ($members_q->num_rows() != 0)) {

    $class = "projectMemberlist";

    while ($members_q->next_record()) {

      $m_id = $members_q->f("member_id");
      $m_name = $members_q->f("member_lastname"). " " .$members_q->f("member_firstname");
      $m_manager = $members_q->f("member_manager");

      if ($m_manager) {
	$rd_manager = "
          <input type=\"radio\" name=\"rd_man$m_id\" value=\"1\" checked
            onchange=\"change_status($m_id, 1, f_entity)\" />$l_project_manager
          <input type=\"radio\" name=\"rd_man$m_id\" value=\"0\"
            onchange=\"change_status($m_id, 0, f_entity)\" />$l_member";
      } else {
	$rd_manager = "
          <input type=\"radio\" name=\"rd_man$m_id\" value=\"1\"
            onchange=\"change_status($m_id, 1, f_entity)\" />$l_project_manager
          <input type=\"radio\" name=\"rd_man$m_id\" value=\"0\" checked
            onchange=\"change_status($m_id, 0, f_entity)\" />$l_member";
      }

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
   <form name=\"f_entity\" method=\"post\"
         action=\"".url_prepare("project_index.php")."\">

    <div class=\"detailHead\">$l_members_info</div>

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
// HTML Display Project Member Time List
// Parameters :
//   - $tasks_q     : DBO : list of tasks related to the deal
//   - $members_q   : DBO : list of members
//   - $project[]   : project search criteria
//     keys used    : id
///////////////////////////////////////////////////////////////////////////////
function html_project_allocate_form($tasks_q, $members_q, $allo_q, $project) {
  global $display, $set_theme;
  global $l_memberstimeinfos, $l_validate;
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

  if (($allo_q != 0) && ($allo_q->num_rows() != 0)) {
    while ($allo_q->next_record()) {
	$m_id = $allo_q->f("userobm_id");
	$m_task = $allo_q->f("projectuser_projecttask_id");
	$m_proj = $allo_q->f("projected_time");
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
        <img border=\"0\" src=\"".C_IMAGE_PATH."/$set_theme/hierarchy.png\" alt=\"\" />$t_label</td>\n";

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
        onclick=\"if(check_allo_valid(this.form, $cpt_m)) return true; else return false;\" />";
   
   $span = $cpt_m*2 + 1;
   
   $display["title"] = "<div class=\"title\">$l_project : $pname</div>";

  // --- HTML Template --------------------------------------------------------

  $block = "
   <form name=\"form_allocate\" method=\"post\">
    <input type=\"hidden\" name=\"action\" value=\"allocate_update\" />

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
// HTML Display Project Member Time List
// Parameters :
//   - $tasks_q     : DBO : list of tasks related to the deal
//   - $members_q   : DBO : list of members
//   - $project[]   : project search criteria
//     keys used    : id
///////////////////////////////////////////////////////////////////////////////
function html_project_advance_form($tasks_q, $members_q, $allo_q, $project) {
  global $display, $set_theme;
  global $l_fill_advance_form, $l_validate;
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

  if (($allo_q != 0) && ($allo_q->num_rows() != 0)) {
    while ($allo_q->next_record()) {
      $m_id = $allo_q->f("userobm_id");
      $m_task = $allo_q->f("projectuser_projecttask_id");
      $array_time["proj"][$m_task][$m_id] = $allo_q->f("projected_time");
      $array_time["miss"][$m_task][$m_id] = $allo_q->f("missing_time");

      $m_proj = $allo_q->f("projected_time");
      $array_time[$m_task][$m_id] = $m_proj;
    } 
  }

  $dis_memhead = "<tr><td class=\"projectHead\">&nbsp;</td>";

  // diplay the members of the project
  for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {
    $m_id = $array_mem[$cpt_e]["id"];
    $m_name = $array_mem[$cpt_e]["name"];
    $dis_memhead .= "<td class=\"projectHead\" width=\"64\">$m_name</td>\n";
  }

  $dis_memhead .= "<tr>";

  // display the rest of the array (ie. tasknames - projected times)
  for ($cpt_d=0; $cpt_d<$cpt_t; $cpt_d++) {

    $t_id = $array_tsk[$cpt_d]["id"];
    $t_label = $array_tsk[$cpt_d]["label"];

    $t_parent = $array_child[$t_id];

    if ($array_parent["$t_id"]) {
      $img = "";
      $classTask = "projectPtask";
      $classInput = "projectPtime";
    } else {
      $img = "<img border=\"0\" src=\"".C_IMAGE_PATH."/$set_theme/hierarchy.png\" alt=\"\" />";
      $classTask = "projectCtask";
      $classInput = "projectCtime";
    }

    // Header column
    $dis_member .= "<tr>
      <td class=\"$classTask\">$img $t_label</td>";

    // Data columns
    for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {

      $m_id = $array_mem[$cpt_e]["id"];
      $tm_proj = $array_time["proj"][$t_id][$m_id];
      $tm_miss = $array_time["miss"][$t_id][$m_id];

      if ($t_parent) {
        $dis_member .= "<td class=\"projectPtime\">&nbsp;</td>\n";
      } else {
        $dis_member .= "
          <td class=\"$classInput\">
            <input type=\"text\" name=\"tf_projected[$m_id][$t_id]\" size=\"3\" maxlength=\"8\" value=\"$tm_proj\" />
            <input type=\"text\" name=\"tf_missing[$m_id][$t_id]\" size=\"3\" maxlength=\"8\" value=\"$tm_miss\" />
          </td>";
      }
    }

    $dis_member .= "</tr>\n";
  }
    
  $dis_button .= "
    <input type=\"submit\" value=\"$l_validate\"
      onclick=\"if(check_advance_valid(this.form, $cpt_m)) return true; else return false;\" />";
     
  $span = $cpt_m*2 + 1;
     
  $display["title"] = "<div class=\"title\">$l_project : $pname</div>";

  // --- HTML Template --------------------------------------------------------

  $block = "
  <form name=\"f_entity\" method=\"post\">
    <div class=\"detailHead\">$l_fill_advance_form</div>

    <table class=\"projectDetail\">
      $dis_memhead
      $dis_member
    </table>

    <input type=\"hidden\" name=\"action\" value=\"advance_update\" />
    <input type=\"hidden\" name=\"param_project\" value=\"$pid\" />
  
    <div class=\"detailButton\">
     <p class=\"detailButtons\">$dis_button</p>
    </div>

  </form>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Project dashboard
// Parameters:
//   - $project : project hash infos
///////////////////////////////////////////////////////////////////////////////
function dis_project_dashboard($project) {
  global $display;

  $pid = $project["id"];
  $project_q = run_query_project_detail($pid);
  $tasks_q = run_query_project_tasks($pid);
  $members_q = run_query_project_members($pid);
  $times = run_query_project_allocation_and_time($pid);

  if ($project_q->num_rows() != 1) {
    $display["msg"] .= display_err_msg($l_query_error);
  }

  $block = html_project_dashboard($project_q, $tasks_q, $members_q, $times);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Project Dashboard
// Parameters :
//   - $project_q : DBO : information about the project
//   - $tasks_q   : DBO : list of tasks in the project 
//   - $members_q : DBO : list of members and related time infos
//   - $times     : array of hash with uid,task_id,allo,miss,used valued 
//      keys used : id
///////////////////////////////////////////////////////////////////////////////
function html_project_dashboard($project_q, $tasks_q, $members_q, $times) {
  global $path, $set_theme, $display, $action;
  global $l_board_no_data, $l_total;
  global $l_project, $l_progress, $l_progressinfos;
  global $l_used, $l_missing;
  global $l_allotted, $l_progress_update, $l_project, $l_resume;
  global $l_alloestimated, $l_usedestimated, $l_progeval, $l_tasks;
  global $l_yes, $l_no;
  global $l_alloproj, $l_usedproj;

  $cpt_t = 0;
  $cpt_m = 0;

  // retrieve project related infos
  $pid = $project_q->f("project_id");
  $name = $project_q->f("project_name");
  $soldtime = $project_q->f("project_soldtime");
  $estimated = $project_q->f("project_estimatedtime");

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


  //---------------------------------------------------------------------------
  // tasks or members not defined, we just display task or member list
  //---------------------------------------------------------------------------
  if (!($has_tasks) or !($has_members)) {
    $display["msg"] = display_warn_msg($l_board_no_data);

  //---------------------------------------------------------------------------
  // Display the detailled progress array
  //---------------------------------------------------------------------------
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

      if ($t_parent) {
	$array_child[$t_parent] = 1;
      }
      $array_parent[$t_id] = $t_parent;

      $cpt_t++; 
    }

    // list the members of the project
    while (list($key,$cell) = each($times)) {
      $m_id = $cell["uid"];
      $m_task = ($cell["task_id"] ? $cell["task_id"] : 0);
      $m_proj = ($cell["allo"] ? $cell["allo"] : 0);
      $m_miss = ($cell["miss"] ? $cell["miss"] : 0);
      $m_used = ($cell["used"] ? $cell["used"] : 0);

      // time (used/missing/projected) for each (task/member) couple
      $array_time["proj"][$m_task][$m_id] = $m_proj;
      $array_time["miss"][$m_task][$m_id] = $m_miss;
      $array_time["used"][$m_task][$m_id] = $m_used;

      // totals by parent tasks
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

      //used for evaluation of the project progress
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
        <img border=\"0\" src=\"".C_IMAGE_PATH."/$set_theme/hierarchy.png\" alt=\"\" />
        $t_label</td>\n";
      }

      // write a line (corresponding to a task)
      for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {

	$m_id = $array_mem[$cpt_e]["id"];
	$tm_proj = $array_time["proj"][$t_id][$m_id];
	$tm_miss = $array_time["miss"][$t_id][$m_id];
	$tm_used = $array_time["used"][$t_id][$m_id];

	// We write the cell only if not 0/0/0
	if ( ( ($tm_used != "0.0") && ($tm_used != ""))
	     || ( ($tm_miss != "0") && ($tm_miss != "") )
             || ( ($tm_proj != "0") && ($tm_proj != "") ) ) {
	  $tm_label = number_format($tm_used, 1) ."/". $tm_miss ."/". $tm_proj;
	} else {
	  $tm_label = "";
	}

	if ($t_haschild) {
	  $dis_member .= "<td class=\"projectPtime\">&nbsp;</td>\n";
	} else if ($t_parent) {
	  $dis_member .= "<td class=\"projectPtime\">$tm_label</td>\n";
	} else {
	  $dis_member .= "<td class=\"projectCtime\">$tm_label</td>\n";
	}
      }

      if ($t_parent) {
	// write the total for a parent task
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

    // display the last line (totals by members)
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
    $ratio_alloestimated = intval(($allotted/$estimated)*100);
    $ratio_usedestimated = intval(($usedtime/$estimated)*100);

    $ratio_progress = (($usedtime + $missingtime) == 0) ? 0 : intval(($usedtime/($usedtime + $missingtime))*100);
    $ratio_progeval = intval((($usedtime + $missingtime)/$estimated)*100);

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
    $dis_progress = "
    <tr>
     <td class=\"detailLabel\">&nbsp;$text_time&nbsp;</td>
     <td class=\"detailText\" align=\"right\">$value_time</td>
     <td>&nbsp;&nbsp;</td>
     <td class=\"detailLabel\">&nbsp;$l_alloestimated&nbsp;</td>
     <td class=\"detailText\" align=\"right\">$ratio_alloestimated %</td>
    </tr>
    <tr>
     <td class=\"detailLabel\">&nbsp;$l_allotted&nbsp;</td>
     <td class=\"detailText\" align=\"right\">$allotted</td>
     <td>&nbsp;&nbsp;</td>
     <td class=\"detailLabel\">&nbsp;$l_usedestimated&nbsp;</td>
     <td class=\"detailText\" align=\"right\">$ratio_usedestimated %</td>
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

    <div class=\"detailHead\">$l_progress</div>

    <table class=\"detail\">
      $dis_progress
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

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Project Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_project_display_pref($prefs) {
  global $l_project_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs, "project");
  $dis_pref->pref_title = $l_project_display;

  $block .= "<td>";
  $block .= $dis_pref->display();
  $block .= "</td></tr></table>";

  $block .= $dis_pref->dis_pref_help();
  return $block;
}

?>