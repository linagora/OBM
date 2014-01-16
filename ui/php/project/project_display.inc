<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



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
$fieldnames['project_name'] = $l_name;
$fieldnames['project_shortname'] = $l_shortname;
$fieldnames['project_company'] = $l_company;
$fieldnames['project_type'] = $l_type;
$fieldnames['project_tasktype'] = $l_tasktype;
$fieldnames['project_code'] = $l_code;
$fieldnames['project_soldtime'] = $l_soldtime;
$fieldnames['project_estimatedtime'] = $l_estimatedtime;
$fieldnames['project_datebegin'] = $l_datebegin;
$fieldnames['project_dateend'] = $l_dateend;
$fieldnames['project_archive'] = $l_archive_first;


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
  global $path,  $ico_mail, $ico_web;
  
  if ($fieldname == 'project_name') {
    if ($OD->display_ext == 'get_id') {
      $res['url'] = 'javascript:check_get_id('.$OD->data_set->f('project_id').",'".addslashes($OD->data_set->f('project_name'))."');";
    } else if ($OD->display_ext == 'get_id_url') {
      $res['url'] = "javascript:check_get_id_url('$ext_url',".$OD->data_set->f('project_id').');';
    } else if ($OD->display_ext == 'get_id_cv') {
      $res['url'] = 'javascript:check_get_id_cv('.$OD->data_set->f('project_id').",'".addslashes($OD->data_set->f('project_name'))."');";
    } else {
      $res['url'] = "$path/project/project_index.php?action=detailconsult&amp;project_id=".$OD->data_set->f('project_id');
    }
  }

  else if (($fieldname == 'project_company') && $link_ok) {
    $res['url'] = "$path/company/company_index.php?action=detailconsult&amp;company_id=".$OD->data_set->f('project_company_id');
  }

  else if ($fieldname == 'project_archive') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = '&nbsp;';
      $res['txt_name'] = ' ';
    }
  }

  else if ($fieldname == 'project_code') {
    $res['align'] = 'center';
    $res['name'] = get_project_code($OD->data_set);
  }

  else if ($fieldname == 'project_datebegin') {
    $date = $OD->data_set->f($fieldname);
    $res['name'] = of_date_format($date);
  }

  else if ($fieldname == 'project_dateend') {
    $date = $OD->data_set->f($fieldname);
    $res['name'] = of_date_format($date);
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

  $types = get_global_deal_types();
  $tts = get_global_tasktype('project');
  $member_q = run_query_project_members();

  $block = html_project_search_form ($types, $tts, $member_q, $project);
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Html Project search Form
// Parameters:
//   - $types     : types array
//   - $tts       : tasktype array
//   - $member_q  : database object with project member list
//   - $project[] : default form values
//     keys used  : name, company_name, tt, manager, member,
///////////////////////////////////////////////////////////////////////////////
function html_project_search_form ($types, $tts, $member_q, $project) {
  global $obm, $display, $c_all;
  global $l_name, $l_company, $l_type, $l_tasktype, $l_member, $l_date;
  global $l_all, $l_find, $l_archive, $l_select_project, $l_shortname;
  global $ctt_sales, $ctt_research, $ctt_others;
  global $l_tt_sales, $l_tt_research, $l_tt_others;

  $popup = $project['popup'];
  $name = $project['name'];
  $shortname = $project['shortname'];
  $company_name = $project['company_name'];
  $type = $project['type_id'];
  $tt = $project['tt'];
  $member = $project['member'];
  $archive = $project['archive'];
  $date = of_date_upd_format($project['datebegin'],true);
  if (($project['action'] != 'search') && (! isset($project['delegation']))) { 
    $delegation = $obm['delegation'];
  }
  if ($project['delegation'] != '') {
    $delegation = stripslashes($project['delegation']);
  }
  $company_id = $project['company_id'];
  $deal_id = $project['deal_id'];

  // Type select construction
  $sel_type = "<select name=\"sel_type_id\">
     <option value=\"$c_all\">$l_all</option>";  
  foreach($types as $t_id => $one_type) {
    $tlabel = $one_type['label'];
    $sel_type .= "<option value=\"$t_id\"";
    if ($type == $t_id) $sel_type .= " selected = \"selected\"";
    $sel_type .= ">$tlabel</option>";
  }
  $sel_type .= '</select>';

  $task_list = array($ctt_sales => $l_tt_sales,
		     $ctt_research => $l_tt_research,
		     $ctt_others => $l_tt_others);

  // TaskType select construction
  $type_prec = -1;
  $sel_tt = "<select name=\"sel_tt\">
    <option value=\"$c_all\">$l_all</option>";
  if (is_array($tts)) {
    foreach ($tts as $id=>$one_tt) {
      $tt_label = $one_tt['label'];
      $internal = $one_tt['internal'];
      // put menu titles
      if ($internal != $type_prec) {
        if($type_prec != '') {
          $sel_tt .= "</optgroup>";
        }
        $sel_tt .= "<optgroup label=\"".$task_list[$internal]."\">";        
	$type_prec = $internal;
      }
      $sel_tt .= "\n<option value=\"$id\"";
      if ($tt == $id) $sel_tt .= "selected =\"selected\"";
      $sel_tt .= ">$tt_label</option>\n";
    }
    $sel_tt .= "</optgroup>";
    $sel_tt .= "</select>";
  }

  // member select
  $sel_member = "<select id=\"sel_member\" name=\"sel_member\">
    <option value=\"$c_all\">$l_all</option>\n";
  while ($member_q->next_record()) {
    $mb_id = $member_q->f('member_id');
    $sel_member .= "<option value=\"$mb_id\"";
    if ($mb_id == $member) { $sel_member .= " selected=\"selected\""; }
    $sel_member .= ">". $member_q->f('member_lastname').' '.$member_q->f('member_firstname') . "</option>\n";
  }
  $sel_member .= '</select>';

  $checked = ($archive) ? 'checked' : '';
  $cb_archive = "<input type=\"checkbox\" name=\"cba_archive\" value=\"1\" $checked />";

  $dis_delegation = of_delegation_dis_search_form($delegation);

  $url = url_prepare('project_index.php');

  if ($popup) {
    $ext_action = $project['ext_action'];
    $ext_url = $project['ext_url'];
    $ext_id = $project['ext_id'];
    $ext_title = ($project['ext_title'] ? $project['ext_title'] : "$l_select_project");
    $ext_target = $project['ext_target'];
    $ext_widget = $project['ext_widget'];
    $ext_widget_text = $project['ext_widget_text'];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
            <input name=\"ext_widget\" type=\"hidden\" value=\"$ext_widget\">
            <input name=\"ext_widget_text\" type=\"hidden\" value=\"$ext_widget_text\">
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
    $display['title'] = $ext_title;
  }

  // --- HTML Template --------------------------------------------------------

  $block = "
    <form method=\"get\" class=\"search\" name=\"f_search\" id=\"f_search\" action=\"$url\">

      <label>$l_name<br />
        <input type=\"text\" name=\"tf_name\" size=\"20\" value=\"$name\" />
      </label>
      <label>$l_shortname<br />
        <input type=\"text\" name=\"tf_shortname\" size=\"16\" value=\"$shortname\" />
      </label>
      <label>$l_company<br />
        <input type=\"text\" name=\"tf_company_name\" id=\"tf_company_name\" size=\"20\" value=\"$company_name\" />
      </label>
      $dis_delegation
      <label>$l_type<br />
        $sel_type
      </label>
      <label>$l_tasktype<br />
        $sel_tt
      </label>
      <label>$l_member<br />
        $sel_member
      </label>
      <label>$l_date<br />
      <input type=\"text\" name=\"tf_datebegin\" value=\"$date\" class=\"datePicker\"/>
      </label>
      <label>$l_archive<br />
        $cb_archive
      </label>
      <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
        <input name=\"company_id\" type=\"hidden\" value=\"$company_id\" />
        <input name=\"deal_id\" type=\"hidden\" value=\"$deal_id\" />
        <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
        <input name=\"popup\" type=\"hidden\" value=\"$popup\" />      
       $ext
      </label>
      <p class=\"CL\"></p>

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
  global $display, $obm, $l_found, $l_no_found;

  $prefs = get_display_pref($obm['uid'], 'project', 0);
  $obm_q = run_query_project_search($project);
  $nb_project = $obm_q->num_rows_total();

  if ($nb_project == 0) {
    $display['msg'] .= display_warn_msg($l_no_found);
  } else {
    $display['msg'] .= display_info_msg($nb_project.' '.$l_found);
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

  $popup = $project['popup'];
  $name = $project['name'];
  $shortname = $project['shortname'];
  $company_name = $project['company_name'];
  $tt = $project['tt'];
  $manager = $project['manager'];
  $member = $project['member'];
  $archive = $project['archive'];
  $company_id = $project['company_id'];
  $type = $project['type_id'];
  $delegation = $project['delegation'];

  if ($popup) {
    $ext_action = $project['ext_action'];
    $ext_url = $project['ext_url'];
    $ext_id = $project['ext_id'];
    $ext_target = $project['ext_target'];
    $ext_widget = $project['ext_widget'];
    $ext_widget_text = $project['ext_widget_text'];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text&amp;popup=1";
  }

  $url = url_prepare("project_index.php?action=search&amp;tf_name=$name&amp;tf_shortname=$shortname&amp;sel_type_id=$type&amp;tf_company_name=$company_name&amp;sel_tt=$tt&amp;sel_member=$member&amp;sel_manager=$manager&amp;delegation=$delegation&amp;cba_archive=$archive&amp;company_id=$company_id$url_ext");

  $proj_d = new OBM_DISPLAY('DATA', $prefs, 'project');
  if ($popup) {
    $proj_d->display_link = false;
    if ($ext_action == 'ext_get_id_cv') {
      $proj_d->display_ext = 'get_id_cv';
    }
    else {
      if ($ext_url != '') {
        $proj_d->display_ext = 'get_id_url';
      } else if ( ($ext_widget != '') && ($ext_widget_text != '') ) { 
        $proj_d->display_ext = 'get_id';
      }
    }
   $display_popup_end = "
      <p>
      <a href=\"#\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  $proj_d->data_set = $proj_q;
  $proj_d->data_url = $url;
  $proj_d->data_header = 'both';
  $block = $proj_d->display('dis_data_project');
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the planning for a month
// Parameters : 
//   - $obm_q    : tasks list to display 
//   - $params   : project hash
///////////////////////////////////////////////////////////////////////////////
function dis_project_planning($params) {
  global $display, $l_project;

  if (!isset ($params['date'])) {
    $params['date'] = date('Ymd'); 
  }
  
  $obm_q = run_query_project_month_planning($params);
  $block = html_project_nav_date($params);
  $block .= html_project_planning($obm_q, $params);

  $p = get_project_info($params['project_id']);
  $title = $p['name'];
  $shortname = $p['shortname'];
  if ($shortname != '') {
    $title .= " ($shortname)";
  }
  $display['title'] = $title;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Time navigation by date
// Parameters:
//   - $time : parameter hash
///////////////////////////////////////////////////////////////////////////////
function html_project_nav_date($project) {
  global $cdg_param, $l_monthsofyear, $action;
  global $ico_previous,$ico_next;

  $id = $project['project_id'];

  $tot_width = 200 ;

  // This should work for january also (month 0 = december) 
  $month = substr($project['date'],4,2);
  $year = substr($project['date'],0,4);
  $d_prev = date('Ymd', mktime(0,0,0,$month-1,1,$year));
  $d_next = date('Ymd', mktime(0,0,0,$month+1,1,$year));
  $url_prev = url_prepare("project_index.php?action=$action&amp;project_id=$id&amp;date=$d_prev");
  $url_next = url_prepare("project_index.php?action=$action&amp;project_id=$id&amp;date=$d_next");
  $title = $l_monthsofyear[$month-1] . " $year";

  $block = "
  <h1>
    <a href=\"$url_prev\"><img src=\"$ico_previous\" alt=\"[Previous Month]\" /></a>     
    $title
    <a href=\"$url_next\"><img src=\"$ico_next\" alt=\"[Next Month]\" /></a>     
  </h1>     
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML the planning for a month
// Parameters : 
//   - $obm_q    : tasks list to display 
//   - $time[] : time hash
///////////////////////////////////////////////////////////////////////////////
function html_project_planning($obm_q, $params) {
  global $c_working_days, $c_week_first_day, $c_day_fraction, $l_fill, $l_total;
  global $l_daysofweekfirst;

  $col_nwday = 'A0A0A0';
  $p_ids = $params['project_ids'];
  $year = substr($params['date'],0,4);
  $month = substr($params['date'],4,2);

  // first day of the month in second
  $first_day_sec = mktime(0,0,0,$month, 1, $year);
  $last_day_sec = mktime(23,0,0,$month+1, 0, $year);
  $day_in_sec = $first_day_sec;
  $dow = date('w', $day_in_sec);
  $obm_dow = ($dow - $c_week_first_day + 7) % 7;

  // Fill the day array -------------------------------------------------------
  $day = 1;
  while ($day_in_sec < $last_day_sec) {
    $fday = ($day>9) ? "$day" : "0$day";
    $days["$fday"]['dow'] = $dow;
    $days["$fday"]['obm_dow'] = $obm_dow;
    $day++;

    $day_in_sec = strtotime("+1 day", $day_in_sec);
    $dow = date('w', $day_in_sec);
    $obm_dow = ($dow - $c_week_first_day + 7) % 7;
  }
  $nb_days = $day - 1;
  $planning = array();
  $task_days = array();
  $line = array();
  $css = array();
  while ($obm_q->next_record())  {
    $day = date('d', $obm_q->f('date'));
    $project_id = $obm_q->f('project_id');
    $project_label = $obm_q->f('project_label');
    $task_id = $obm_q->f('id');
    $task_label = $obm_q->f('label');
    if (mb_strlen($task_label) > 15) {
      $dis_task_label = substr($task_label,0,15) . '..';
    } else {
      $dis_task_label = $task_label;
    }
    $u_id = $obm_q->f('userobm_id');

    $projects[$project_id] = $project_label;

    $tasks[$task_id] = "<abbr title=\"$task_label\">$dis_task_label</abbr>";
    $users[$u_id] = $obm_q->f('userobm_firstname').' '. $obm_q->f('userobm_lastname');


    $planning[$u_id][$task_id]["$day"] = $obm_q->f('timetask_length')/$c_day_fraction;
    $planning[$u_id][$task_id]['length'] += $obm_q->f('timetask_length')/$c_day_fraction;
  }

  while (list($day,$val) = each($days)) {
    // Display the month planning
    $lday = $l_daysofweekfirst[$val['dow']];
    $line0 .= "<th>$lday</th>\n";
    if (! $c_working_days[$val['dow']]) {
      $line1 .= "<td style=\"background-color: #$col_nwday;\">$day</td>\n";
    } else {
      $line1 .= "<td>$day</td>\n";
    }

    // Fill the day cell for each user
    foreach($planning as $u_id => $user_planning) {
      foreach($user_planning as $task_id => $work) {
        if(isset($work[$day])) {
          $length = $work[$day]*100;
          $line[$u_id][$task_id] .= "<td title=\"$length%\" ><div style=\"width:$length%;\">&nbsp;</div></td>";
        } else {
	  if (! $c_working_days[$days[$day]['dow']]) {
	    $line[$u_id][$task_id] .= "<td style=\"background-color: #$col_nwday;\"></td>";
	  } else {
	    $line[$u_id][$task_id] .= "<td></td>";
	  }
        }
      }
    }
  }

  // Fill the displayed rows
  foreach($planning as $u_id => $user_planning) {
    foreach($user_planning as $task_id => $work) {
      $line[$u_id][$task_id] = "<tr class=\"t$task_id\">
        <th>".$tasks[$task_id]."</th>".$line[$u_id][$task_id]."
        <th></th><td>".$work['length'].'</td></tr>';
      $color = randomColor();
      $css[$project_id] .= ".t$task_id th {color:$color;} \n.t$task_id div {background-color:$color;}";
      $total_length += $work['length'];
    }
  }

  foreach($line as $u_id => $l) {
    $d_l .= '<tr><th>'.$users[$u_id]."</th><th colspan=\"".($nb_days+2)."\"></th></tr>".
    implode($l);
  }
  $css = implode("\n",$css);

  // Total line
  if ($total_length > 0) {
    $total_span = $nb_days+1;
    $dis_total = "<tr><th>&nbsp;</th><td colspan=\"$total_span\">$l_total</td><td>$total_length</td></tr>";
  }

  $block = "
  <style>
    $css
  </style>
  <div class=\"timeCenter\">
  <table class=\"planning\">
  <colgroup>
  </colgroup>
  <colgroup class=\"main\" span=\"$nb_days\">
  </colgroup> 
  <colgroup class=\"main\" span=\"2\">
  </colgroup>    
  <thead>
  <tr><th></th>$line0</tr>
  <tr><th></th>$line1</tr>
  </thead>
  $d_l
  $dis_total
  </table>
  </div>
";

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
  global  $ico_document, $ico_add, $ico_invoice;
  global $l_new, $l_module_invoice, $l_module_document;
  global $l_document_add, $l_project;
  global $path, $cgp_show, $perm;

  $id = $p_q->f('project_id');
  $name = $p_q->f('project_name');
  $uname = urlencode($name);
  $cid = $p_q->f('project_company_id');
  $cname = $p_q->f('company_name');
  $ucname = urlencode($cname);
  $did = $p_q->f('project_deal_id');
  $dlabel = $p_q->f('deal_label');
  $udlabel = urlencode($dlabel);

  // Document
  if ($perm->check_module_rights('document')) {
    $url_doc = url_prepare("$path/document/document_index.php?action=search&amp;entity_id=$id&amp;entity=project&amp;private=0");
    $url_doc_new = url_prepare("$path/document/document_index.php?action=new&amp;entity_id=$id&amp;entity=project");
    $url_doc_add = "$path/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_action=document_add&amp;ext_url=".urlencode($path."/project/project_index.php")."&amp;ext_id=$id&amp;ext_target=$l_project";
    $nb_document = run_query_global_document_nb ($id, 'project');
    $block_doc = "
 <div  class=\"portlet\" id=\"projectDocument\">
 <h1>$l_module_document</h1>
  <ul>
    <li><a href=\"$url_doc\"><img src=\"$ico_document\" /></a>
        <a href=\"$url_doc\">$l_module_document ($nb_document)</a></li>
    <li><a href=\"$url_doc_new\"><img src=\"$ico_add\" /></a>
        <a href=\"$url_doc_new\">$l_new</a></li>
    <li><a href=\"#\" 
	 onclick=\"popup('$url_doc_add',''); return false;
	 \"><img src=\"$ico_add\" /></a>
	<a href=\"#\" onclick=\"window.name='$l_project'; popup('$url_doc_add',''); return false;\">
	 $l_document_add</a></li>
  </ul>
  </div>";
  }

  // Invoice
  if ($perm->check_module_rights('invoice')) {
    $url_inv = url_prepare("$path/invoice/invoice_index.php?action=search&amp;project_id=$id");
    $url_inv_tot = url_prepare("$path/invoice/invoice_index.php?action=search&amp;project_id=$id&amp;cba_archive=1");
    $url_inv_new = url_prepare("$path/invoice/invoice_index.php?action=new&amp;project_id=$id&amp;project_name=$uname&amp;company_id=$cid&amp;company_name=$ucname&amp;deal_id=$did&amp;deal_label=$udlabel");
    $nb_inv_tot = get_global_linked_invoice_nb($id, 'project', 1);
    $nb_inv = get_global_linked_invoice_nb($id, 'project');
    $block_invoice = "
 <div  class=\"portlet\" id=\"projectInvoice\"> 
  <h1>$l_module_invoice</h1>
  <ul>
    <li><a href=\"$url_inv\"><img src=\"$ico_invoice\" alt=\"\" /></a>
        $l_module_invoice ( <a href=\"$url_inv\">$nb_inv</a> /
        <a href=\"$url_inv_tot\">$nb_inv_tot</a> )</li>
    <li><a href=\"$url_inv_new\"><img src=\"$ico_add\" alt=\"\" /></a>
        <a href=\"$url_inv_new\">$l_new</a></li>
  </ul>
  </div>";
  }

  // Links Template  
  $block = "
  $block_doc
  $block_invoice";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Project detail page
// Parameters:
//   - $pid : project id
///////////////////////////////////////////////////////////////////////////////
function dis_project_consult($pid) {
  global $display, $l_err_reference;

  $project_q = run_query_project_detail($pid);
  $tasks_q = run_query_project_tasks($pid);
  $members_q = run_query_project_members($pid);
  $times = run_query_project_allocation_and_time($pid);

  if ($project_q->num_rows() != 1) {
    $display['msg'] .= display_err_msg($l_err_reference);
  }

  $display['detailInfo'] = display_record_info($project_q);
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
  global $path,  $display, $action, $ico_company, $ico_deal;
  global $l_name, $l_tasktype, $l_company, $l_total, $l_datebegin, $l_dateend;
  global $l_deal, $l_project, $l_progress, $l_progressinfos,$l_shortname;
  global $l_type, $l_used, $l_missing, $l_sold, $l_estimated, $l_comment;
  global $l_allotted, $l_no_members, $l_progress_update, $l_project, $l_resume;
  global $l_alloestimated, $l_usedestimated, $l_progeval, $l_tasks, $l_code;
  global $l_members, $l_task_list, $l_members_info, $l_archive, $l_yes, $l_no;
  global $l_alloproj, $l_usedproj, $l_no_tasks,$ico_hierarchy,$l_allosold;
  global $l_ref, $l_project_date, $l_project_duration, $l_project_desc, $l_project_tech;

  $cpt_t = 0;
  $cpt_m = 0;

  // retrieve project related infos
  $pid = $project_q->f('project_id');
  $name = $project_q->f('project_name');
  $shortname = $project_q->f('project_shortname');
  $type = $project_q->f('type_label');
  $tt = $project_q->f('project_tasktype_label');
  $company = $project_q->f('project_company_id');
  $company_name = $project_q->f('company_name');
  $deal = $project_q->f('project_deal_id');
  $deal_label = $project_q->f('deal_label');
  $arc_status = ($project_q->f('project_archive')) ? "$l_yes" : "$l_no";
  $soldtime = $project_q->f('project_soldtime');
  $estimated = $project_q->f('project_estimatedtime');
  $datebegin = of_date_format($project_q->f('datebegin'), 1);
  $dateend = of_date_format($project_q->f('dateend'), 1);
  $reference_date = $project_q->f('project_reference_date');
  $reference_duration = $project_q->f('project_reference_duration');
  $reference_desc = nl2br($project_q->f('project_reference_desc'));
  $reference_tech = nl2br($project_q->f('project_reference_tech'));
  $comment = beautify_comment(nl2br($project_q->f('project_comment')));

  // Labels are different for sales and R&D projects
  if ($company > 0) {
    $text_time = "$l_sold / $l_estimated";
    $value_time = "$soldtime / $estimated";
  } else {
    $text_time = $l_estimated;
    $value_time = $estimated;
  }

  // Project references
  $code = get_project_code($project_q);
  $dis_code = "
    <tr>
      <th>$l_code</th>
      <td>
        $code
      </td>
    </tr>";
  if ($deal > 0) {
    $dis_deal = "
    <tr>
      <th>$l_deal</th>
      <td>
        <a href=\"". url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;deal_id=$deal")."\">
        <img src=\"$ico_deal\" alt=\"[details]\" /></a>
        $deal_label
      </td>
    </tr>";
  }
  if ($company > 0) {
    $company_link = "<a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$company")."\">
        <img src=\"$ico_company\" alt=\"[details]\" /></a>";
  }
  $dis_comp = "
    <div class=\"detail infos\">
    <h1>$l_company</h1>
    <table>
    <tr>
      <th>$l_company</th>
      <td>
        $company_link
        $company_name
      </td>
    </tr>
    $dis_deal
    $dis_code
    </table>
    </div>";

  if (($members_q != 0) && ($members_q->num_rows() != 0)){
    while ($members_q->next_record()) {
      $array_mem[$cpt_m]['id'] = $members_q->f('member_id');
      $array_mem[$cpt_m]['name'] = $members_q->f('member_lastname');
      $array_mem[$cpt_m]['firstname'] = $members_q->f('member_firstname');
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
    $dis_progress = "
     <tr>
       <th>$l_sold / $l_estimated</th>
       <td>$soldtime / $estimated</td>
       <th>$l_allosold</th>
       <td>0 %</td>
      </tr>";    

    // we display the task list
    if ($has_tasks) { 
      $dis_member .= "
       <tr>
        <th colspan=\"2\">$l_task_list :</th>
       </tr>";

      while ($tasks_q->next_record()) {
	$tsk_name = $tasks_q->f('projecttask_label');
	$img = ($tasks_q->f('parent_id')) ? "<img border=\"0\" src=\"$ico_hierarchy\" alt=\"\" />" : "";
	$dis_member .= "
       <tr>
        <th>&nbsp;</th>
        <td>$img $tsk_name</td>
       </tr>";
	}
    } else {
      $dis_member .= "
        <tr>
         <th colspan=\"2\">$l_no_tasks</th>
        </tr>";
    }
      
    if ($has_members) {
      $dis_member .= "
       <tr>
        <th colspan=\"2\">$l_members_info :</th>
       </tr>";
      
      while (list($key,$val) = each($array_mem)) {
	$mem_name = $val['firstname'] .' '. $val['name'];
	$dis_member .= "
       <tr>
        <th>&nbsp;</th>
        <td>- $mem_name</td>
       </tr>";
      }

    } else {
      $dis_member .= "
       <tr>
        <th colspan=\"2\">$l_no_members</th>
       </tr>";
    }

  //---------------------------------------------------------------------------
  // Display the detailled progress array
  //---------------------------------------------------------------------------
  } else {
    
    $allotted = 0;
    $usedtime = 0;
    $missingtime = 0;

    while ($tasks_q->next_record()) {
      $t_id = $tasks_q->f('projecttask_id');
      $t_parent = $tasks_q->f('parent_id');

      $array_tsk[$cpt_t]['id'] = $t_id;
      $array_tsk[$cpt_t]['label'] = $tasks_q->f('projecttask_label');
      $array_tsk[$cpt_t]['parent'] = ($t_parent == 0) ? 1 : 0;

      if ($t_parent) {
	$array_child[$t_parent] = 1;
      }
      $array_parent[$t_id] = $t_parent;

      $cpt_t++; 
    }

    // list the members of the project
    while (list($key,$cell) = each($times)) {
      $m_id = $cell['uid'];
      $m_task = ($cell['task_id'] ? $cell['task_id'] : 0);
      $m_proj = ($cell['allo'] ? $cell['allo'] : 0);
      $m_miss = ($cell['miss'] ? $cell['miss'] : 0);
      $m_used = ($cell['used'] ? $cell['used'] : 0);

      // time (used/missing/projected) for each (task/member) couple
      $array_time['proj'][$m_task][$m_id] = $m_proj;
      $array_time['miss'][$m_task][$m_id] = $m_miss;
      $array_time['used'][$m_task][$m_id] = $m_used;

      // totals by parent tasks
      if ($array_parent[$m_task] == 0) {
	$array_partot['proj'][$m_task] += $m_proj;
	$array_partot['miss'][$m_task] += $m_miss;
	$array_partot['used'][$m_task] += $m_used;
      } else {
	$m_ptask = $array_parent[$m_task];
	$array_partot['proj'][$m_ptask] += $m_proj;
	$array_partot['miss'][$m_ptask] += $m_miss;
	$array_partot['used'][$m_ptask] += $m_used;
      }

      //totals by members
      $array_memtot['proj'][$m_id] += $m_proj;
      $array_memtot['miss'][$m_id] += $m_miss;
      $array_memtot['used'][$m_id] += $m_used;

      //complete total
      $timetot['proj'] += $m_proj;
      $timetot['miss'] += $m_miss;
      $timetot['used'] += $m_used;

      //used for evaluation of the project progress
      $allotted += $m_proj;
      $missingtime += $m_miss;
      $usedtime += $m_used;
    }

    $dis_memhead = "<th>&nbsp;</th>\n";

    // diplay the members of the project
    for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {
      $m_id = $array_mem[$cpt_e]['id'];
      $m_name = $array_mem[$cpt_e]['name'];
      $dis_memhead .= "<th>$m_name</th>\n";
    }

    $dis_memhead .= "<th>$l_total</th>\n";

    // display the rest of the array (ie. tasknames - projected times)
    for ($cpt_d=0; $cpt_d<$cpt_t; $cpt_d++) {

      $t_id = $array_tsk[$cpt_d]['id'];
      $t_label = $array_tsk[$cpt_d]['label'];
      $t_parent = $array_tsk[$cpt_d]['parent'];

      $t_haschild = $array_child[$t_id];
      if($cpt_d % 2 == 0) {
        $class = "class=\"pair\"";
      } else {
        $class = '';
      }
      if ($t_parent) {
	$dis_member .= "<tr $class><th>$t_label</th>";
      } else {
	$dis_member .= "<tr $class><td>
        <img border=\"0\" src=\"$ico_hierarchy\" alt=\"\" />
        $t_label</td>";
      }

      // write a line (corresponding to a task)
      for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {

	$m_id = $array_mem[$cpt_e]['id'];
	$tm_proj = $array_time['proj'][$t_id][$m_id];
	$tm_miss = $array_time['miss'][$t_id][$m_id];
	$tm_used = $array_time['used'][$t_id][$m_id];

	// We write the cell only if not 0/0/0
	if ( ( ($tm_used != '0.0') && ($tm_used != ''))
	     || ( ($tm_miss != '0') && ($tm_miss != '') )
             || ( ($tm_proj != '0') && ($tm_proj != '') ) ) {
	  $tm_label = number_format($tm_used, 1) .'/'. $tm_miss .'/'. $tm_proj;
	} else {
	  $tm_label = '';
	}

	if ($t_haschild) {
	  $dis_member .= "<td>&nbsp;</td>\n";
	} else if ($t_parent) {
	  $dis_member .= "<td>$tm_label</td>\n";
	} else {
	  $dis_member .= "<td>$tm_label</td>\n";
	}
      } 

      if ($t_parent) {
	// write the total for a parent task
	$tm_proj = $array_partot['proj'][$t_id];
	$tm_miss = $array_partot['miss'][$t_id];
	$tm_used = $array_partot['used'][$t_id];

	if (!(isset($tm_miss))) $tm_miss = 0;
	if (!(isset($tm_proj))) $tm_proj = 0;

        $class= '';
        if (($tm_used + $tm_miss) > (1.10 * $tm_proj))
	  $class = 'error';
        elseif (($tm_used + $tm_miss) < (0.90 * $tm_proj))
	  $class = 'ok';        
	$tm_label = number_format($tm_used, 1) .'/'. number_format($tm_miss,1) .'/'. number_format($tm_proj,1);
	$dis_member .= "<td class=\"$class\">$tm_label</td>\n";

      } else {
	$dis_member .= "<td>&nbsp;</td>";
      }

      $dis_member .= "</tr>\n";
    }

    // display the last line (totals by members)
    $dis_member .= "<tr class=\"highlight\">\n <th>$l_total</th>\n";

    for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {
      $m_id = $array_mem[$cpt_e]['id'];

      $tm_proj = $array_memtot['proj'][$m_id];
      $tm_miss = $array_memtot['miss'][$m_id];
      $tm_used = $array_memtot['used'][$m_id];
      $tm_label = number_format($tm_used, 1) .'/'. number_format($tm_miss,1) .'/'. number_format($tm_proj,1);

      $dis_member .= "<td >$tm_label</td>\n";
    }

    //last cell (total of totals)
    $tm_proj = $timetot['proj'];
    $tm_miss = $timetot['miss'];
    $tm_used = $timetot['used'];

    if (!(isset($tm_miss))) $tm_miss = 0;
    if (!(isset($tm_proj))) $tm_proj = 0;

    $tm_label = number_format($tm_used, 1) .'/'. $tm_miss .'/'. $tm_proj;
    $dis_member .= "<td >$tm_label</td>\n";
    $dis_member .= "</tr>\n";

    if ($estimated != 0) {
      // ratios
      $ratio_alloestimated = intval(($allotted/$estimated)*100);
      $ratio_usedestimated = intval(($usedtime/$estimated)*100);
      $ratio_progress = (($usedtime + $missingtime) == 0) ? 0 : intval(($usedtime/($usedtime + $missingtime))*100);
      $ratio_progeval = intval((($usedtime + $missingtime)/$estimated)*100);
    }


    if ($ratio_progeval < 70)
      $symbol_progeval = '++';
    else if ($ratio_progeval < 85)
      $symbol_progeval = '+';
    else if ($ratio_progeval > 140)
      $symbol_progeval = '--';
    else if ($ratio_progeval > 120)
      $symbol_progeval = '-';
    else
      $symbol_progeval = '=';

    $span = $cpt_m + 2;

    //advance array
    $dis_progress = "
    <tr>
     <th>$text_time :</th>
     <td>$value_time</td>
     <th>$l_alloestimated :</th>
     <td>$ratio_alloestimated %</td>
    </tr>
    <tr>
     <th>$l_allotted :</th>
     <td>$allotted</td>
     <th>$l_usedestimated :</th>
     <td>$ratio_usedestimated %</td>
    </tr>
    <tr>
     <th>$l_used :</th>
     <td>$usedtime</td>
     <th>$l_progress :</th>
     <td>$ratio_progress %</td>
    </tr>
    <tr>
     <th>$l_missing :</th>
     <td>$missingtime</td>
     <th>$l_progeval :</th>
     <td>$symbol_progeval</td>
    </tr>";

  } 
  
  // Reference
  $dis_reference = "
  <h1>$l_ref</h1>
  <table>
  <tr>
    <th> $l_project_date :</th>
    <td>$reference_date</td>
  </tr>
  <tr>
    <th> $l_project_duration :</th>
    <td>$reference_duration</td>
  </tr>

  </table>";

  // --- HTML Template --------------------------------------------------------

  $display['link'] = html_project_links($project_q);
  $display['title'] = $name;

  $block = "
    
   <div class=\"detail infos\">
    <h1>$l_project</h1>

    <table>
    <tr>
      <th>$l_name :</th>
      <td>$name</td>
    </tr>
    <tr>
      <th>$l_shortname :</th>
      <td>$shortname</td>
    </tr>      
    <tr>
      <th>$l_type :</th>
      <td>$type</td>
    </tr>
    <tr>
      <th>$l_tasktype :</th>
      <td>$tt</td>
    </tr>
    <tr>
      <th>$l_datebegin :</th>
      <td>$datebegin</td>
    </tr><tr>
      <th>$l_dateend :</th>
      <td>$dateend</td>
    </tr><tr>
      <th>$l_resume :</th>
      <td># $l_tasks : $nb_tsk / # $l_members : $cpt_m</td>
    </tr>
    <tr>
      <th>$l_archive :</th>
      <td>$arc_status</td>
    </tr>
    </table>
    </div>

    $dis_comp

   <div class=\"detail infos\">
    $dis_reference
   </div>
 
   <div class=\"detail extra\">
    <h1>$l_progress</h1>
    <table>
      $dis_progress
    </table>
    </div>

    <div class=\"detail extra\">
    <h1>$l_progressinfos</h1>
    <table class=\"spreadSheet\">
      <colgroup span=\"".($span - 1)."\" />
      <colgroup class=\"highlight\" />
    <thead>
    <tr>
      $dis_memhead
    </tr>
    </thead>
    <tbody>
    $dis_member
    </tbody>
    </table>
    </div>
   

   <div class=\"detail extra\">  
   <h1> $l_project_desc</h1>
   $reference_desc
   </div>

  <div class=\"detail extra\">
  <h1> $l_project_tech</h1>
  $reference_tech
  </div>
  
  <div class=\"detail extra\">
  <h1>$l_comment</h1>
  $comment
  </div>
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
  global $l_project_date,$l_project_duration,$l_project_tech,$l_project_desc;
  global $l_type, $l_datebegin, $l_dateend, $c_undef, $l_undef,$l_tasks;
  global $l_comment, $l_add_comment, $l_upd_comment,$ico_project;
  global $display, $obm, $c_php_isodate_format,$l_shortname, $l_header_new;
  global $ctt_sales, $ctt_research, $ctt_others,$l_other;
  global $l_tt_sales, $l_tt_research, $l_tt_others,$path;

  // Hook : Pre
  if (function_exists('hook_pre_dis_project_form')) {
    $project = hook_pre_dis_project_form($project);
  }

  $users_p = of_usergroup_get_group_users($obm['group_prod'], true, array($obm['uid']), 'userobm_lastname');

  if ($action == 'detailupdate') {
    $pid = $project_q->f('project_id');
    $name = $project_q->f('project_name');
    $type = $project_q->f('project_type_id');
    $tt = $project_q->f('project_tasktype_id');
    $company = $project_q->f('project_company_id');
    $deal = $project_q->f('project_deal_id');
    $soldtime = $project_q->f('project_soldtime');
    $estimated = $project_q->f('project_estimatedtime');
    $arc_val = ($project_q->f('project_archive')) ? 'checked' : '';
    $company_name = $project_q->f('company_name');
    $deal_label = $project_q->f('deal_label');
    $datebegin = of_date_upd_format($project_q->f('datebegin'), 1);
    $dateend = of_date_upd_format($project_q->f('dateend'), 1);
    $reference_date =  $project_q->f('project_reference_date');
    $reference_duration =  $project_q->f('project_reference_duration');
    $reference_desc =  $project_q->f('project_reference_desc');
    $reference_tech =  $project_q->f('project_reference_tech');
    $comment = $project_q->f('project_comment');
    $shortname = $project_q->f('project_shortname');
    $usercomment = $obm['uid'];
    $datecomment = of_date_upd_format();
    $dis_title = $name;
  } else {
    if ($action == 'new') {
      $name = $project['deal_label'];
      // We pre-fill datebegin and dateend with current date
      $datebegin = of_date_upd_format(); 
      $dateend = $datebegin;
      $usercomment = $obm['uid'];
      $datecomment = of_date_upd_format();
    }
  }

  // If parameters have been given, they supercede the default action value
  if (isset($project['project_id'])) { $pid = $project['project_id']; }
  if (isset($project['name'])) { $name = stripslashes($project['name']); }
  if (isset($project['type_id'])) { $type = $project['type_id']; }
  if (isset($project['tt'])) { $tt = $project['tt']; }
  if (isset($project['company_id'])) { $company = $project['company_id']; }
  if (isset($project['deal_id'])) { $deal = $project['deal_id']; }
  if (isset($project['soldtime'])) { $soldtime = $project['soldtime']; }
  if (isset($project['estimated'])) { $estimated = $project['estimated']; }
  if (isset($project['company_name'])) { $company_name = stripslashes($project['company_name']); }
  if (isset($project['deal_label'])) { $deal_label = $project['deal_label']; }
  if (isset($project['tasklabel'])) { $task = $project['tasklabel']; }
  if (isset($project['member'])) { $member = $project['member']; }
  if (isset($project['datebegin'])) { $datebegin = of_date_upd_format($project['datebegin']); }
  if (isset($project['dateend'])) { $dateend = of_date_upd_format($project['dateend']); }
  if (isset($project['archive'])) { $arc_val = ($project['archive'] == 1 ? 'checked' : ''); }
  if (isset($project['reference_date'])) { $reference_date = stripslashes($project['reference_date']); }
  if (isset($project['reference_duration'])) { $reference_duration = stripslashes($project['reference_duration']); }
  if (isset($project['reference_desc'])) { $reference_desc = stripslashes($project['reference_desc']); }
  if (isset($project['reference_tech'])) { $reference_tech = stripslashes($project['reference_tech']); }
  if (isset($project['comment'])) { $comment = stripslashes($project['comment']); }
  if (isset($project['add_comment'])) { $add_comment = stripslashes($project['add_comment']); }
  if (isset($project['usercomment'])) { $usercomment = $project['usercomment']; }
  if (isset($project['datecomment'])) { $datecomment = $project['datecomment']; }
  if (isset($project['shortname'])) { $shortname = $project['shortname']; }


  if (($action == 'new') || ($action == 'insert')) {
    // member select
    $mbs = of_usergroup_get_group_users($obm['group_prod'], true, array(), 'userobm_lastname');
    $sel_member = "<select id=\"sel_member\" name=\"sel_member\">
      <option value=\"$c_undef\">$l_undef</option>\n";
    if (is_array($mbs)) {
      foreach ($mbs as $mb_id => $one_user) {
        $mb_name = $one_user['lastname'].' '.$one_user['firstname'];
          $sel_member .= "
        <option value=\"$mb_id\"";
          if ($mb_id == $member) { $sel_member .= " selected=\"selected\""; }
          $sel_member .= ">$mb_name</option>";
      }
    }
    $sel_member .= '</select>';

    $dis_member = "
  </tr><tr>
    <th>$l_member</th>
    <td>$sel_member</td>";

    $url = "$path/project/project_index.php?action=ext_get_reftask_ids&amp;popup=1&amp;ext_element=sel_task_id";
  // task
  $dis_task = "
  </tr><tr>
    <th>$l_task</th>
    <td><input name=\"tf_tasklabel\" size=\"40\" maxlength=\"128\" value=\"$task\" /></td>
  </tr><tr>
  <th>$l_tasks
    <a href=\"javascript: return false;\" 
      onclick=\"popup('$url','');
      return false;\">
       <img src=\"$ico_project\" alt=\"[Add]\" />
      </a>  
  </th>
    <td id=\"sel_task_id\">$dis_sel_tasks</td>";
  }

  // Task list depend on task category (eg : if company not filled)
  if (! ($company > 0)) {
    $tts = get_global_tasktype($ctt_research);
  } else {
    $tts = get_global_tasktype();
    if ($deal > 0) {
      $dis_deal = "
    </tr><tr>
      <th>$l_deal</th>
      <td>$deal_label</td>";
    }
    $dis_comp = "
     <fieldset class=\"detail infos\">
    <legend>$l_company</legend>
    <table>
    <tr>
      <th>$l_company</th>
      <td>$company_name</td>
    $dis_deal
    </tr>
    </table>
    </fieldset>";

    $dis_sold = "
    <tr>
      <th>$l_soldtime</th>
      <td><input type=\"text\" name=\"tf_soldtime\" size=\"10\" maxlength=\"8\" value=\"$soldtime\" /></td>
    </tr>";
  }

  $types = get_global_deal_types();

  // Type select construction
  $sel_type = "<select name=\"sel_type_id\">
     <option value=\"$c_undef\">$l_undef</option>";  
  if (is_array($types) && count ($types) > 0) {
    foreach($types as $t_id => $one_type) {
      $tlabel = $one_type['label'];
      $sel_type .= "<option value=\"$t_id\"";
      if ($type == $t_id) $sel_type .= " selected = \"selected\"";
      $sel_type .= ">$tlabel</option>";
    }
  }
  $sel_type .= '</select>';

  // TaskType select construction
  $task_list = array($ctt_sales=>$l_tt_sales,
		     $ctt_research=>$l_tt_research,
		     $ctt_others=>$l_tt_others);
  $type_ddec = -1;
  $sel_tt = "
        <select id=\"sel_tt\" name=\"sel_tt\">
          <option value=\"$c_undef\">$l_undef</option>";
  if (is_array($tts)) {
    foreach ($tts as $id=>$one_tt) {
      $tt_label = $one_tt['label'];
      $internal = $one_tt['internal'];
      // put menu titles
      if ($internal != $type_prec) {
        if($type_prec != '') {
          $sel_tt .= '</optgroup>';
        }
        $sel_tt .= "<optgroup label=\"".$task_list[$internal]."\">";
	$type_prec = $internal;
      }
      $sel_tt .= "\n<option value=\"$id\"";
      if ($tt == $id) $sel_tt .= "selected =\"selected\"";
      $sel_tt .= ">$tt_label</option>\n";
    }
    $sel_tt .= '</optgroup>';
    $sel_tt .= '</select>';
  }

  // User comment select construction
  $sel_usercomment = "<select name=\"sel_usercomment\">";
  if (is_array($users_p)) {
    foreach ($users_p as $cid => $one_user) {
      $cname = $one_user['lastname'].' '.$one_user['firstname'];
      $sel_usercomment .= "<option value=\"$cname\"";
      // First time default to uid of connected user (uid) then set to name
      if ( ($usercomment == $cid) || ($usercomment == $cname) ) {
	$sel_usercomment .= " selected = \"selected\"";
      }
      $sel_usercomment .= ">$cname</option>\n";
    }
  }
  $sel_usercomment .= '</select>';

  // target action
  if (($action == 'detailupdate') || ($action == 'update')) {
    $dis_comment = "
        $l_upd_comment
        <textarea name=\"ta_comment\" rows=\"6\" cols=\"78\">$comment</textarea>";

    $dis_action = "
      <input type=\"hidden\" name=\"action\" value=\"update\" />";
    $text_button = $l_update;
  } else {
    $dis_action = "
      <input type=\"hidden\" name=\"action\" value=\"insert\" />";
    $text_button = $l_insert;
  }

  $dis_button = "$dis_action
      <input type=\"hidden\" name=\"project_id\" value=\"$pid\" />
      <input type=\"hidden\" name=\"company_id\" value=\"$company\" />
      <input type=\"hidden\" name=\"deal_id\" value=\"$deal\" />
      <input type=\"submit\" value=\"$text_button\" />";
  
  $display['title'] = $dis_title;
  
  // --- HTML Template --------------------------------------------------------
  
  $block = "
    <form method=\"post\" name=\"f_entity\" 
      onsubmit=\"if (check_project(this)) return true; else return false;\"
      action=\"".url_prepare("project_index.php")."\">
   
    <fieldset class=\"detail infos\">
    <legend>$l_project</legend>

    <table>
    <tr>
      <th>$l_name</th>
      <td><input type=\"text\" name=\"tf_name\" size=\"40\" value=\"$name\" /></td>
    </tr><tr>
      <th>$l_shortname</th>
      <td><input type=\"text\" name=\"tf_shortname\" size=\"10\" maxlength=\"10\" value=\"$shortname\" /></td>
    </tr>
    <tr>
      <th>$l_type</th>
      <td>$sel_type</td>
    </tr>
    <tr>
      <th>$l_tasktype</th>
      <td>$sel_tt</td>
    </tr>
    $dis_sold
    </tr><tr>
      <th>$l_projtime</th>
      <td><input type=\"text\" name=\"tf_estimated\" size=\"10\" maxlength=\"8\" value=\"$estimated\" /></td>
    $dis_task
    $dis_member
    </tr>
    <tr>
      <th>$l_datebegin</th>
      <td><input type=\"text\" name=\"tf_datebegin\" value=\"$datebegin\" class=\"datePicker\"/></td>
    </tr><tr>
      <th>$l_dateend</th>
      <td><input type=\"text\" name=\"tf_dateend\" value=\"$dateend\" class=\"datePicker\"/></td>
    </tr><tr>
      <th><label for=\"cba_archive\">$l_archive</label></th>
      <td><input type=\"checkbox\" id=\"cba_archive\" name=\"cba_archive\" value=\"1\" $arc_val /></td>
    </tr>
    </table>
    </fieldset>
    $dis_comp
    <fieldset class=\"detail infos\">
    <legend>$l_project_date</legend>

    <table>
    <tr>
      <th> $l_project_date </th>
      <td><input type=\"text\" name=\"tf_reference_date\" size=\"32\" maxlength=\"32\"  value=\"$reference_date\" /></td>
    </tr>
    <tr>
      <th> $l_project_duration </th>
      <td><input type=\"text\" name=\"tf_reference_duration\" size=\"16\" maxlength=\"16\" value=\"$reference_duration\"  /></td>
    </tr>
    </table>
    </fieldset>

    <fieldset class=\"detail extra\">
      <legend> $l_project_desc </legend>
      <textarea name=\"ta_reference_desc\" rows=\"4\" cols=\"80\" >$reference_desc</textarea>
    </fieldset>

    <fieldset class=\"detail extra\">
      <legend> $l_project_tech </legend>
      <textarea name=\"ta_reference_tech\" rows=\"4\" cols=\"80\" >$reference_tech</textarea>
    </fieldset>

    <fieldset class=\"detail extra\">
    <legend>$l_comment</legend>

    $l_add_comment
    <input type=\"text\" name=\"tf_datecomment\" value=\"$datecomment\" class=\"datePicker\"/>
    $sel_usercomment
    <textarea name=\"ta_add_comment\" rows=\"6\" cols=\"78\">$add_comment</textarea>$dis_comment
    </fieldset>

   <fieldset class=\"buttons\">
   $dis_button
   </fieldset>

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

  $url = url_prepare('project_index.php');

  $dis_back = "<form name=\"form_back\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"project_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"project_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onclick=\"if (confirm_del(this.form)) return true; else return false;\" />
    </form>";

  $display['msg'] .= display_ok_msg($l_can_delete);

  $block .= "
    <div class=\"buttons\">
    $dis_delete
    $dis_back
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
  global $display, $c_undef, $l_undef,$ico_project, $path;
  global $l_name, $l_parent, $l_task_new, $l_task_add, $l_project;
  global $l_task_update, $l_task_upd, $l_task,$l_tasks;
  
  $pid = $project['project_id'];
  $pname = $project['name'];
  $label = stripslashes($project['tasklabel']);
  $task = $project['task'];
  $ptask = $project['ptask'];

  if (($ptask_q != 0) && ($ptask_q->num_rows() != 0)) {
    $cpt_root = 0;
    // parent task select
    $sel_ptask = "
        <select id=\"sel_ptask\" name=\"sel_ptask\">
          <option value=\"$c_undef\">$l_undef</option>\n";
    while ($ptask_q->next_record()) {
      $parent = $ptask_q->f('parent_id');
      if ($parent == 0) {
	$cpt_root++;
	$ptask_id = $ptask_q->f('projecttask_id');
	$ptask_label = $ptask_q->f('projecttask_label');
	if ($ptask_id == $ptask) {
	  $selected = 'selected';
	} else {
	  $selected = '';
	}
	$sel_ptask .= "<option value=\"$ptask_id\" $selected>$ptask_label</option>\n";
      }
    }
    $sel_ptask .= '</select>';

    // task select
    $sel_task = "
      <select id=\"sel_task\" name=\"sel_task\"
      onchange=\"fill_parenttask(this.form.sel_ptask, this.form.sel_task, this.form.tf_tasklabel)\" >";
    $ptask_q->seek(0);
    while ($ptask_q->next_record()) {
      $parent = $ptask_q->f('parent_id');
      if ($parent == 0) {
	$prefix = '';
      } else {
	$prefix = '&nbsp;&nbsp;&nbsp;&nbsp;';
      }	
      $task_id = $ptask_q->f('projecttask_id');
      $task_label = $ptask_q->f('projecttask_label');
      if ($task_id == $task) {
	$selected = 'selected';
      } else {
	$selected = '';
      }
      $sel_task .= "<option value=\"$task_id\" $selected>$prefix$task_label</option>\n";
    }

    $sel_task .= '</select>';
    $ptask_q->seek(0);
  }

  $display['title'] = $pname;

  // --- HTML Template --------------------------------------------------------


    $url = "$path/project/project_index.php?action=ext_get_reftask_ids&amp;popup=1&amp;ext_element=sel_task_id";

  $block = "
   $js_script
   <form method=\"post\" name=\"f_entity\"
     onsubmit=\"if (check_taskadd(this)) return true; else return false;\"
     action=\"".url_prepare('project_index.php')."\">
    
    <fieldset class=\"detail infos\"> 
    <legend>$l_task_new</legend>

    <table>
    <tr>
      <th>$l_name</th>
      <td><input type=\"text\" name=\"tf_tasklabel\" size=\"30\" value=\"\" /></td>
    </tr><tr>
    <th>$l_tasks
      <a href=\"javascript: return false;\" 
        onclick=\"popup('$url','');
        return false;\">
         <img src=\"$ico_project\" alt=\"[Add]\" />
        </a>  
    </th>
    <td id=\"sel_task_id\">$dis_sel_tasks</td>      
    </tr><tr>
      <th>$l_parent</th>
      <td>
      $sel_ptask
      </td>
    </tr>
    </table>
    <fieldset class=\"buttons\">
     <input type=\"hidden\" name=\"project_id\" value=\"$pid\" />
     <input type=\"hidden\" name=\"action\" value=\"task_add\" />
     <input type=\"submit\" value=\"$l_task_add\" />
    </fieldset>    
    </fieldset>

  </form>    

  <form method=\"post\" name=\"form_taskupd\"
     onsubmit=\"if (check_taskupd(this)) return true; else return false;\"
     action=\"".url_prepare('project_index.php')."\">

   <fieldset class=\"detail infos\">  
   <legend>$l_task_update</legend>
   <table>
    <tr>
      <th>$l_task</th>
      <td>
      $sel_task
      </td>
    </tr>
    <tr>
      <th>$l_parent</th>
      <td>
      $sel_ptask
      </td>
    </tr>
    <tr>
      <th>$l_name</th>
      <td><input type=\"text\" name=\"tf_tasklabel\" size=\"30\" value=\"\" /></td>
    </tr>
    </table>

    <fieldset class=\"buttons\">
     <input type=\"hidden\" name=\"project_id\" value=\"$pid\" />
     <input type=\"hidden\" name=\"action\" value=\"task_update\" />
     <input type=\"submit\" value=\"$l_task_upd\" />
    </fieldset>    
    </fieldset>

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
  global $ico_hierarchy;
  global $l_task_list, $l_task_del, $l_no_tasks;

  $pid = $project['project_id'];

  if (($tasks_q != 0) && ($tasks_q->num_rows() != 0)) {

    while ($tasks_q->next_record()) {
      $t_id = $tasks_q->f('projecttask_id');
      $t_label = $tasks_q->f('projecttask_label');
      $t_parent = $tasks_q->f('parent_id');
      $t_rank = $tasks_q->f('projecttask_rank');

      if (($t_parent == 0) || ($t_parent == '')) {
	$dis_task .= "
      <tr>
        <td>
        <input type=\"checkbox\" id=\"data-task-$t_id\" name=\"data-task-$t_id\" />
        </td>
        <td colspan=\"2\"><label for=\"data-task-$t_id\">$t_label</label></td>
       </tr>";
      } else {
	$dis_task .= "
      <tr>
        <td>
        <input type=\"checkbox\" id=\"data-task-$t_id\" name=\"data-task-$t_id\" />
        </td>
        <td colspan=\"2\">
        <label for=\"data-task-$t_id\"><img border=\"0\" src=\"$ico_hierarchy\" alt=\"\" />$t_label</label>
        </td>
      </tr>";
      }
   }
    
    $dis_button = "
      <input type=\"hidden\" name=\"project_id\" value=\"$pid\" />
      <input type=\"hidden\" name=\"action\" value=\"task_del\" />
      <input type=\"submit\" value=\"$l_task_del\" />";

  } else {
    
    $dis_task = "<tr><td class=\"detailLabel\">$l_no_tasks</td></tr>\n";
  }
  
  $block = "
    <form name=\"form_tasklist\" method=\"post\">
    <fieldset class=\"detail extra\">
    <legend>$l_task_list</legend>

     <table>
      $dis_task
     </table>
     </fieldset>

    <fieldset class=\"buttons\">
    $dis_button
    </fieldset>     
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

  $pid = $project['project_id'];
  $pname = $project['name'];

  if (($members_q != 0) && ($members_q->num_rows() != 0)) {

    while ($members_q->next_record()) {

      $m_id = $members_q->f('member_id');
      $m_name = $members_q->f('member_lastname'). ' ' .$members_q->f('member_firstname');
      $m_manager = $members_q->f('member_manager');

      if ($m_manager) {
	$rd_manager = "
          <label><input type=\"radio\" name=\"rd_man$m_id\" value=\"1\" checked onchange=\"change_status($m_id, 1, f_entity)\" />$l_project_manager</label>
          <label><input type=\"radio\" name=\"rd_man$m_id\" value=\"0\" onchange=\"change_status($m_id, 0, f_entity)\" />$l_member</label>";
      } else {
	$rd_manager = "
          <label><input type=\"radio\" name=\"rd_man$m_id\" value=\"1\" onchange=\"change_status($m_id, 1, f_entity)\" />$l_project_manager</label>
          <label><input type=\"radio\" name=\"rd_man$m_id\" value=\"0\" checked onchange=\"change_status($m_id, 0, f_entity)\"/>$l_member</label>";
      }

      $dis_member .= "
      <tr>
      <td>
       <label><input type=\"checkbox\" name=\"data-user-$m_id\"/>$m_name</label>
      </td>
      <td>$rd_manager</td>
      </tr>";
    }

    $dis_button = "
      <input type=\"hidden\" name=\"project_id\" value=\"$pid\" />
      <input type=\"hidden\" name=\"user_id\" value=\"\" />
      <input type=\"hidden\" name=\"user_status\" value=\"\" />
      <input type=\"hidden\" name=\"action\" value=\"member_del\" />
      <input type=\"submit\" value=\"$l_del_member_sel\"
             onclick=\"submit_memberdel(this.form)\" />";
    $span = 3;

  } else {

    $dis_member = "
      <tr>
       <th>$l_no_members</th>
      </tr>";

    $span = 1;
  }

  $display['title'] = $pname;

  // --- HTML Template --------------------------------------------------------

  $block = "
   <form name=\"f_entity\" method=\"post\"
         action=\"".url_prepare('project_index.php')."\">
    
   <div class=\"detail extra\"> 
    <h1>$l_members_info</h1>

    <table>
      $dis_member
    </table>
    </div>

    <div class=\"buttons\">
      $dis_button
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
  global $display ;
  global $l_memberstimeinfos, $l_validate;
  global $l_no_members, $l_project,$ico_hierarchy;

  $cpt_t=0;
  $cpt_m=0;
  $pid = $project['project_id'];
  $pname = $project['name'];

  // task list
  while ($tasks_q->next_record()) {
    $pt_id = $tasks_q->f('projecttask_id');
    $par_id = $tasks_q->f('parent_id');
    $parent = $tasks_q->f('parent_group');

    $array_tsk[$cpt_t]['id'] = $pt_id;
    $array_tsk[$cpt_t]['label'] = $tasks_q->f('projecttask_label');

    $array_child[$par_id] = ($par_id) ? 1 : 0;
    $array_parent[$pt_id] = ($pt_id == $parent) ? 1 : 0;

    $cpt_t++;
  }

  // list the members of the project
  while ($members_q->next_record()) {
    $array_mem[$cpt_m]['id'] = $members_q->f('member_id');
    $array_mem[$cpt_m]['name'] = $members_q->f('member_lastname');  
    $cpt_m++;
  }

  if (($allo_q != 0) && ($allo_q->num_rows() != 0)) {
    while ($allo_q->next_record()) {
	$m_id = $allo_q->f('userobm_id');
	$m_task = $allo_q->f('projectuser_projecttask_id');
	$m_proj = $allo_q->f('projected_time');
	$array_time[$m_task][$m_id] = $m_proj;
    } 
  }

  $dis_memhead = '<tr><th></th>';

  // diplay the members of the project
  for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {
    $m_id = $array_mem[$cpt_e]['id'];
    $m_name = $array_mem[$cpt_e]['name'];
    $dis_memhead .= "<th>$m_name</th>\n";
  }

  $dis_memhead .= '</tr>';

  // display the rest of the array (ie. tasknames - projected times)
  for ($cpt_d=0; $cpt_d<$cpt_t; $cpt_d++) {

    $t_id = $array_tsk[$cpt_d]['id'];
    $t_label = $array_tsk[$cpt_d]['label'];

    $t_parent = $array_child[$t_id];

    if ($array_parent[$t_id])
	$dis_member .= "<tr><th>$t_label</th>\n";
    else
    	$dis_member .= "<tr><td>
        <img border=\"0\" src=\"$ico_hierarchy\" alt=\"\" />$t_label</td>\n";

    //ecrit la ligne pour un tache sans enfants
    for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {

	$m_id = $array_mem[$cpt_e]['id'];
	$tm_proj = $array_time[$t_id][$m_id];

	if ($t_parent) {
	  $dis_member .= "<td></td>\n";
	} else if ($array_parent[$t_id]) {
	  $dis_member .= "
          <td>
           <input type=\"text\" name=\"tf_projected[$m_id][$t_id]\" size=\"5\" maxlength=\"8\" value=\"$tm_proj\" />
	    </td>";
	} else {
	  $dis_member .= "
          <td>
           <input type=\"text\" name=\"tf_projected[$m_id][$t_id]\" size=\"5\" maxlength=\"8\" value=\"$tm_proj\" />
	  </td>";
	}
    }

    $dis_member .= "</tr>\n";
  }
  
  $dis_button .= "
     <input type=\"submit\" value=\"$l_validate\" />";
   
   $span = $cpt_m*2 + 1;
   
   $display['title'] = $pname;

  // --- HTML Template --------------------------------------------------------

  $block = "
   <form action=\"\" id=\"formAllocate\" name=\"form_allocate\" method=\"post\">

    <fieldset class=\"detail extra\">
    <legend>$l_memberstimeinfos</legend>

    <table class=\"spreadSheet\">
    <thead>
    $dis_memhead
    </thead>
    <tbody>
    $dis_member
    </tbody>
    </table>
    </fieldset>
  
   <fieldset class=\"buttons\">
    <input type=\"hidden\" name=\"project_id\" value=\"$pid\" />
    <input type=\"hidden\" name=\"action\" value=\"allocate_update\" />
    $dis_button
   </fieldset>

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
  global $display, $ico_hierarchy;
  global $l_fill_advance_form, $l_validate;
  global $l_no_members, $l_project;

  $cpt_t=0;
  $cpt_m=0;
  $pid = $project['project_id'];
  $pname = $project['name'];

  // task list
  while ($tasks_q->next_record()) {
    $pt_id = $tasks_q->f('projecttask_id');
    $par_id = $tasks_q->f('parent_id');
    $parent = $tasks_q->f('parent_group');

    $array_tsk[$cpt_t]['id'] = $pt_id;
    $array_tsk[$cpt_t]['label'] = $tasks_q->f('projecttask_label');

    $array_child[$par_id] = ($par_id) ? 1 : 0;
    $array_parent[$pt_id] = ($pt_id == $parent) ? 1 : 0;

    $cpt_t++;
  }

  // list the members of the project
  while ($members_q->next_record()) {
    $array_mem[$cpt_m]['id'] = $members_q->f('member_id');
    $array_mem[$cpt_m]['name'] = $members_q->f('member_lastname');  
    $cpt_m++;
  }

  if (($allo_q != 0) && ($allo_q->num_rows() != 0)) {
    while ($allo_q->next_record()) {
      $m_id = $allo_q->f('userobm_id');
      $m_task = $allo_q->f('projectuser_projecttask_id');
      $array_time['proj'][$m_task][$m_id] = $allo_q->f('projected_time');
      $array_time['miss'][$m_task][$m_id] = $allo_q->f('missing_time');

      $m_proj = $allo_q->f('projected_time');
      $array_time[$m_task][$m_id] = $m_proj;
    } 
  }

  $dis_memhead = "<tr><th>&nbsp;</th>";

  // diplay the members of the project
  for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {
    $m_id = $array_mem[$cpt_e]['id'];
    $m_name = $array_mem[$cpt_e]['name'];
    $dis_memhead .= "<th>$m_name</th>\n";
  }

  $dis_memhead .= '<tr>';

  // display the rest of the array (ie. tasknames - projected times)
  for ($cpt_d=0; $cpt_d<$cpt_t; $cpt_d++) {

    $t_id = $array_tsk[$cpt_d]['id'];
    $t_label = $array_tsk[$cpt_d]['label'];

    $t_parent = $array_child[$t_id];

    if ($array_parent["$t_id"]) {
      $dis_member .= "<tr><th> $t_label</th>";
    } else {
      $dis_member .= "<tr><td><img border=\"0\" src=\"$ico_hierarchy\" alt=\"\" /> $t_label</td>";
    }



    // Data columns
    for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {

      $m_id = $array_mem[$cpt_e]['id'];
      $tm_proj = $array_time['proj'][$t_id][$m_id];
      $tm_miss = $array_time['miss'][$t_id][$m_id];

      if ($t_parent) {
        $dis_member .= "<th>&nbsp;</th>";
      } else {
        $dis_member .= "
          <td>
            <input type=\"text\" name=\"tf_projected[$m_id][$t_id]\" size=\"3\" maxlength=\"8\" value=\"$tm_proj\" />
            <input type=\"text\" name=\"tf_missing[$m_id][$t_id]\" size=\"3\" maxlength=\"8\" value=\"$tm_miss\" />
          </td>";
      }
    }

    $dis_member .= "</tr>\n";
  }
    
  $dis_button .= "
    <input type=\"submit\" value=\"$l_validate\" />";
     
  $span = $cpt_m*2 + 1;
     
  $display['title'] = $pname;

  // --- HTML Template --------------------------------------------------------

  $block = "
  <form id=\"formAdvance\" name=\"f_entity\" method=\"post\">
  <fieldset class=\"detail extra\">
    <legend>$l_fill_advance_form</legend>

    <table class=\"spreadSheet\">
    <thead>
    $dis_memhead
    </thead>
    <tbody>
    $dis_member
    </tbody>
    </table>

    <input type=\"hidden\" name=\"action\" value=\"advance_update\" />
    <input type=\"hidden\" name=\"project_id\" value=\"$pid\" />
  </fieldset>
  <fieldset class=\"buttons\"> 
    $dis_button
  </fieldset>
  </form>
";

  return $block;
}


/**
 * Display one Project closings
 * @param $project : project hash infos
 **/
function dis_project_closing($params) {
  global $display, $l_header_closing;
  global $l_closing, $l_closing_no, $l_used, $l_missing, $l_comment;

  $clos = get_project_closing($params['project_id']);

  if (count($clos) > 0) {
    $dis_title = $l_header_closing;
    $block_clo = "
      <table class=\"spreadSheet\">
      <thead>
      <tr>
        <th>$l_date</th>
        <th>$l_used</th>
        <th>$l_missing</th>
        <th>$l_comment</th>
      </tr>
      </thead>
      <tbody>";

    $k = 0;
    foreach ($clos as $c_id => $clo) {
      $c_date = of_date_format($clo['date']);
      $c_used = $clo['used'];
      $c_remaining = $clo['remaining'];
      $c_comment = $clo['comment'];
      $k ++;
      ($k % 2 == 0) ? $class = 'pair' : $class = '';

      $block_clo .= "
      <tr class=\"$class\">
        <td>$c_date</td>
        <td>$c_used</td>
<!--        <td><a href=\"$path/invoice/invoice_index.php?action=detailconsult&amp;invoice_id=$i_id\">$i_number</a></td> -->
        <td>$c_remaining</td>
        <td>$c_comment</td>
      </tr>";
    }
  } else {
    $dis_title = $l_closing_no;
  }

  $block = "
<div class=\"detail extra\">
  <h1>$dis_title</h1>
  $block_clo
</div>
";

  return $block;
}


/**
 * Display Project Closing Form
 *
 * @param mixed $action action called
 * @param mixed $params : default values
 * @return string XHTML contact form
 */
function dis_project_closing_form($action, $params) {

  $p_id = $params['project_id'];
  $clo_id = $params['closing_id'];
  if ($clo_id > 0) {
    $clo = get_project_closing($p_id, $clo_id);
  } else {
    $clo = '';
  }

  $block = html_project_closing_form($action, $clo, $params);

  return $block;
}


/**
 * HTML Display Contact Form
 * @param $action    : action called
 * @param mixed $clo : closing data array or ''
 * @param $params[] : default values
 **/
function html_project_closing_form($action, $clo, $params) {
  global $display, $path, $obm, $l_header_closing, $l_header_new;
  global $l_closing, $l_closing_no, $l_date, $l_used, $l_missing, $l_comment;
  global $l_closing_update, $l_checkdelete, $l_closing_insert;

  $uid = $obm['uid'];
  $project_id = $params['project_id'];

  // if update mode and first time values are taken from database
  if (is_array($clo) && (count($clo) > 0)) {
    foreach ($clo as $clo_id => $one_clo) {
      $used = $one_clo['used'];
      $remaining = $one_clo['remaining'];
      $comment = $one_clo['comment'];
      $date = $one_clo['date'];
      $date = of_date_upd_format($date, true);
    }
    // New form and first time
  } elseif ($action == 'closing_new') {
    $dis_title = $l_header_new;
    $date = of_date_upd_format();
  }

  // If parameters have been given, they supercede the default action value
  if (isset($params['used'])) { $used = $params['used']; }
  if (isset($params['remaining'])) { $remaining = $params['remaining']; }
  if (isset($params['comment'])) { $comment = stripslashes($params['comment']); }
  if (isset($params['date'])) { $date = $params['date']; }

  // UPDATE 
  if (($action == 'closing_detailupdate') || ($action == 'closing_update')) {
    $dis_button = "
      <input type=\"hidden\" name=\"action\" value=\"closing_update\" />
      <input type=\"hidden\" name=\"closing_id\" value=\"$clo_id\" />
      <input type=\"hidden\" name=\"hd_usercreate\" value=\"$usercreate\" />
      <input type=\"submit\" value=\"$l_update\" />";

  // INSERT
  } elseif (($action == 'closing_new') || ($action == 'closing_insert')) {
    $dis_button = "
      <input type=\"hidden\" name=\"action\" value=\"closing_insert\" />
      <input type=\"submit\" value=\"$l_closing_insert\" />";
  }

  $display['title'] = $dis_title;

  // --- HTML Template --------------------------------------------------------
  $block = "$script";

  $block .= "
    <form method=\"get\" name=\"f_entity\" action=\"".url_prepare('project_index.php')."\">

    <fieldset class=\"detail infos\">
    <legend>$l_closing</legend>

    <table>
    <tr>
      <th>$l_date</th>
      <td>
        <input type=\"text\" name=\"tf_date\" value=\"$date\" class=\"datePicker\"/>
      </td>
    </tr>
    <tr>
      <th>$l_used</th>
      <td><input type=\"text\" name=\"tf_used\" size=\"30\" maxlength=\"64\" value=\"$used\" /></td>
    </tr>
    <tr>
      <th>$l_missing</th>
      <td><input type=\"text\" name=\"tf_remaining\" size=\"30\" maxlength=\"64\" value=\"$remaining\" /></td>
    </tr>
    </table>
    </fieldset>
   
    <fieldset class=\"detail extra\">
    <legend>$l_comment</legend>
      <textarea name=\"ta_comment\" rows=\"6\" cols=\"78\">$comment</textarea>
    </fieldset>

    <fieldset class=\"buttons\">
    $dis_button
    <input type=\"hidden\" name=\"project_id\" value=\"$project_id\" />
    </fieldset>
    </form>";

  return $block;
}


/**
 * Display Project dashboard
 * @param $project : project hash infos
 **/
function dis_project_dashboard($project) {
  global $display, $l_err_reference;

  $pid = $project['project_id'];
  $project_q = run_query_project_detail($pid);
  $tasks_q = run_query_project_tasks($pid);
  $members_q = run_query_project_members($pid);
  $times = run_query_project_allocation_and_time($pid);

  if ($project_q->num_rows() != 1) {
    $display['msg'] .= display_err_msg($l_err_reference);
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
  global $path,  $display, $action;
  global $l_board_no_data, $l_total,$l_sold ,$l_estimated;
  global $l_project, $l_progress, $l_progressinfos;
  global $l_used, $l_missing,$ico_hierarchy;
  global $l_allotted, $l_progress_update, $l_project, $l_resume;
  global $l_alloestimated, $l_usedestimated, $l_progeval, $l_tasks;
  global $l_yes, $l_no;
  global $l_alloproj, $l_usedproj;

  $cpt_t = 0;
  $cpt_m = 0;

  // retrieve project related infos
  $pid = $project_q->f('project_id');
  $name = $project_q->f('project_name');
  $soldtime = $project_q->f('project_soldtime');
  $estimated = $project_q->f('project_estimatedtime');

  if (($members_q != 0) && ($members_q->num_rows() != 0)){
    while ($members_q->next_record()) {
      $array_mem[$cpt_m]['id'] = $members_q->f('member_id');
      $array_mem[$cpt_m]['name'] = $members_q->f('member_lastname');
      $array_mem[$cpt_m]['firstname'] = $members_q->f('member_firstname');
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
    $display['msg'] = display_warn_msg($l_board_no_data);

  //---------------------------------------------------------------------------
  // Display the detailled progress array
  //---------------------------------------------------------------------------
  } else {
    
    $allotted = 0;
    $usedtime = 0;
    $missingtime = 0;

    while ($tasks_q->next_record()) {
      $t_id = $tasks_q->f('projecttask_id');
      $t_parent = $tasks_q->f('parent_id');

      $array_tsk[$cpt_t]['id'] = $t_id;
      $array_tsk[$cpt_t]['label'] = $tasks_q->f('projecttask_label');
      $array_tsk[$cpt_t]['parent'] = ($t_parent == 0) ? 1 : 0;

      if ($t_parent) {
	$array_child[$t_parent] = 1;
      }
      $array_parent[$t_id] = $t_parent;

      $cpt_t++; 
    }

    // list the members of the project
    while (list($key,$cell) = each($times)) {
      $m_id = $cell['uid'];
      $m_task = ($cell['task_id'] ? $cell['task_id'] : 0);
      $m_proj = ($cell['allo'] ? $cell['allo'] : 0);
      $m_miss = ($cell['miss'] ? $cell['miss'] : 0);
      $m_used = ($cell['used'] ? $cell['used'] : 0);

      // time (used/missing/projected) for each (task/member) couple
      $array_time['proj'][$m_task][$m_id] = $m_proj;
      $array_time['miss'][$m_task][$m_id] = $m_miss;
      $array_time['used'][$m_task][$m_id] = $m_used;

      // totals by parent tasks
      if ($array_parent[$m_task] == 0) {
	$array_partot['proj'][$m_task] += $m_proj;
	$array_partot['miss'][$m_task] += $m_miss;
	$array_partot['used'][$m_task] += $m_used;
      } else {
	$m_ptask = $array_parent[$m_task];
	$array_partot['proj'][$m_ptask] += $m_proj;
	$array_partot['miss'][$m_ptask] += $m_miss;
	$array_partot['used'][$m_ptask] += $m_used;
      }

      //totals by members
      $array_memtot['proj'][$m_id] += $m_proj;
      $array_memtot['miss'][$m_id] += $m_miss;
      $array_memtot['used'][$m_id] += $m_used;

      //complete total
      $timetot['proj'] += $m_proj;
      $timetot['miss'] += $m_miss;
      $timetot['used'] += $m_used;

      //used for evaluation of the project progress
      $allotted += $m_proj;
      $missingtime += $m_miss;
      $usedtime += $m_used;
    }

    $dis_memhead = "<th>&nbsp;</th>\n";

    // diplay the members of the project
    for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {
      $m_id = $array_mem[$cpt_e]['id'];
      $m_name = $array_mem[$cpt_e]['name'];
      $dis_memhead .= "<th>$m_name</th>\n";
    }

    $dis_memhead .= "<th>$l_total</th>\n";

    // display the rest of the array (ie. tasknames - projected times)
    for ($cpt_d=0; $cpt_d<$cpt_t; $cpt_d++) {

      $t_id = $array_tsk[$cpt_d]['id'];
      $t_label = $array_tsk[$cpt_d]['label'];
      $t_parent = $array_tsk[$cpt_d]['parent'];

      $t_haschild = $array_child[$t_id];

      if ($t_parent) {
	$dis_member .= "<tr><th>$t_label</th>\n";
      } else {
	$dis_member .= "<tr><td>
        <img src=\"$ico_hierarchy\" alt=\"\" />
        $t_label</td>\n";
      }

      // write a line (corresponding to a task)
      for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {

	$m_id = $array_mem[$cpt_e]['id'];
	$tm_proj = $array_time['proj'][$t_id][$m_id];
	$tm_miss = $array_time['miss'][$t_id][$m_id];
	$tm_used = $array_time['used'][$t_id][$m_id];

	// We write the cell only if not 0/0/0
	if ( ( ($tm_used != '0.0') && ($tm_used != ''))
	     || ( ($tm_miss != '0') && ($tm_miss != '') )
             || ( ($tm_proj != '0') && ($tm_proj != '') ) ) {
	  $tm_label = number_format($tm_used, 1) .'/'. $tm_miss .'/'. $tm_proj;
	} else {
	  $tm_label = '';
	}

	if ($t_haschild) {
	  $dis_member .= "<td></td>\n";
	} else if ($t_parent) {
	  $dis_member .= "<td>$tm_label</td>\n";
	} else {
	  $dis_member .= "<td>$tm_label</td>\n";
	}
      }

      if ($t_parent) {
	// write the total for a parent task
	$tm_proj = $array_partot['proj'][$t_id];
	$tm_miss = $array_partot['miss'][$t_id];
	$tm_used = $array_partot['used'][$t_id];

	if (!(isset($tm_miss))) $tm_miss = 0;
	if (!(isset($tm_proj))) $tm_proj = 0;

	$class = '';
	if (($tm_used + $tm_miss) > (1.10 * $tm_proj))
          $class = 'error';
        elseif (($tm_used + $tm_miss) < (0.90 * $tm_proj))
          $class = 'ok';

	$tm_label = number_format($tm_used, 1) .'/'. number_format($tm_miss,1) .'/'. number_format($tm_proj,1);
	$dis_member .= "<td class=\"$class\">$tm_label</td>\n";

      } else {
	$dis_member .= "<td>&nbsp;</td>";
      }

      $dis_member .= "</tr>\n";
    }

    // display the last line (totals by members)
    $dis_member .= "<tr class=\"highlight\">\n <td>$l_total</td>\n";

    for ($cpt_e=0; $cpt_e<$cpt_m; $cpt_e++) {
      $m_id = $array_mem[$cpt_e]['id'];

      $tm_proj = $array_memtot['proj'][$m_id];
      $tm_miss = $array_memtot['miss'][$m_id];
      $tm_used = $array_memtot['used'][$m_id];
      $tm_label = number_format($tm_used, 1) .'/'. number_format($tm_miss,1) .'/'. number_format($tm_proj,1);

      $dis_member .= "<td>$tm_label</td>\n";
    }

    //last cell (total of totals)
    $tm_proj = $timetot['proj'];
    $tm_miss = $timetot['miss'];
    $tm_used = $timetot['used'];

    if (!(isset($tm_miss))) $tm_miss = 0;
    if (!(isset($tm_proj))) $tm_proj = 0;

    $tm_label = number_format($tm_used, 1) .'/'. $tm_miss .'/'. $tm_proj;
    $dis_member .= "<td>$tm_label</td>\n";
    $dis_member .= "</tr>\n";
  // Labels are different for sales and R&D projects
    if ($company > 0) {
      $text_time = "$l_sold / $l_estimated";
      $value_time = "$soldtime / $estimated";
    } else {
      $text_time = $l_estimated;
      $value_time = $estimated;
    }
    // ratios
    $ratio_alloestimated = intval(($allotted/$estimated)*100);
    $ratio_usedestimated = intval(($usedtime/$estimated)*100);

    $ratio_progress = (($usedtime + $missingtime) == 0) ? 0 : intval(($usedtime/($usedtime + $missingtime))*100);
    $ratio_progeval = intval((($usedtime + $missingtime)/$estimated)*100);

    if ($ratio_progeval < 70)
      $symbol_progeval = '++';
    else if ($ratio_progeval < 85)
      $symbol_progeval = '+';
    else if ($ratio_progeval > 140)
      $symbol_progeval = '--';
    else if ($ratio_progeval > 120)
      $symbol_progeval = '-';
    else
      $symbol_progeval = '=';

    $span = $cpt_m + 2;

    //advance array
    $dis_progress = "
    <tr>
     <th>$text_time :</th>
     <td>$value_time</td>
     <th>$l_alloestimated :</th>
     <td>$ratio_alloestimated %</td>
    </tr>
    <tr>
     <th>$l_allotted :</th>
     <td>$allotted</td>
     <th>$l_usedestimated :</th>
     <td>$ratio_usedestimated %</td>
    </tr>
    <tr>
     <th>$l_used :</th>
     <td>$usedtime</td>
     <th>$l_progress :</th>
     <td>$ratio_progress %</td>
    </tr>
    <tr>
     <th>$l_missing</th>
     <td>$missingtime</td>
     <th>$l_progeval :</th>
     <td>$symbol_progeval</td>
    </tr>";

  } 

  // --- HTML Template --------------------------------------------------------

  $display['title'] = $name;

  $block = "
    $dis_comp
    <div class=\"detail extra\">
    <h1>$l_progress</h1>
    <table>
      $dis_progress
    </table>
    </div>
    <div class=\"detail extra\">
    <h1>$l_progressinfos</h1>
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      $dis_memhead
    </tr>
    </thead>
    <tbody>
    $dis_member
    </tbody>
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

  $dis_pref = new OBM_DISPLAY('PREFERENCES', $prefs, 'project');
  $dis_pref->pref_title = $l_project_display;

  $block .= '<td>';
  $block .= $dis_pref->display();
  $block .= '</td></tr></table>';

  $block .= $dis_pref->dis_pref_help();
  return $block;
}
///////////////////////////////////////////////////////////////////////////////
// Display the task selector form, for external call
// Parameters:
//   - $params: project parameters hashtable
///////////////////////////////////////////////////////////////////////////////
function display_project_task_selector($project) {
  $project_q = run_query_project_tasks_by_users($project);
  $tasktype_q = run_query_project_tasktype();
  $block = html_project_task_selector($project,$project_q,$tasktype_q);
  return $block;
}
///////////////////////////////////////////////////////////////////////////////
// Display the task selector form, for external call
// Parameters:
//   - $params: project parameters hashtable
///////////////////////////////////////////////////////////////////////////////
function html_project_task_selector($project,$project_q,$tasktype_q) {
  global $c_undef, $l_undef, $extra_js,$l_project;
  global $ctt_sales, $ctt_research, $ctt_others,$l_header_tasktype;
  global $l_tt_sales, $l_tt_research, $l_tt_others,$l_task;

  $extra_js .= "
        projects = new Object(); 
        tasks = new Object();";

  // TaskType select construction
  $task_list = array($ctt_sales=>$l_tt_sales,
		     $ctt_research=>$l_tt_research,
		     $ctt_others=>$l_tt_others);

  $sel_tt = "
    <select name=\"sel_tasktype\"
      onchange=\"fill_project( this.value,this.form.sel_project,this.form.sel_projecttask) \">
      <option value=\"$c_undef\">$l_undef</option>";
  for ($i=0; $i<2; $i++) {
    while($tasktype_q->next_record()) {
      $tt_id = $tasktype_q->f('tasktype_id');
      $tt_label = $tasktype_q->f('tasktype_label');
      $tt_internal = $tasktype_q->f('tasktype_internal');
      //put menu titles
      if ($tt_internal != $type_prec) {
        if($type_prec != '') {
          $sel_tt .= '</optgroup>';
        }
        $sel_tt .= "<optgroup label=\"".$task_list[$tt_internal]."\">";
	$type_prec = $tt_internal;
      }
      $extra_js .= "
        projects[$tt_id] = new Object();";    
      $sel_tt .= "\n<option value=\"$tt_id\"";
      if ($is_tt == $tt_id) $sel_tt .= " selected=\"selected\""; 
      $sel_tt .= ">$tt_label</option>\n";
    }
  }
  $sel_tt .= "</optgroup>";
  $sel_tt .= "</select>";

  // Project select construction
  $sel_proj = "
    <select name=\"sel_project\"
      onchange=\"fill_projecttask(this.value,this.form.sel_projecttask);return true;\">
    <option value=\"$c_undef\">$l_undef</option>
    </select>";

  // ProjectTask select construction
  $sel_projtask = "
    <select name=\"sel_projecttask\">
    <option value=\"$c_undef\">$l_undef</option>
    </select>";

  while($project_q->next_record()) {
    $tasktype_id = $project_q->f('project_tasktype_id');
    $project_id = $project_q->f('project_id');
    $project_name = addslashes($project_q->f('project_name'));
    $company_name = $project_q->f('company_name');
    $task_id = $project_q->f('projecttask_id');
    $task_label = addslashes($project_q->f('projecttask_label'));

    if($old_project_id != $project_id) {
      $old_project_id = $project_id;
      $extra_js .= "
        projects[$tasktype_id][$project_id] = '$project_name';
        tasks[$project_id] = new Object();";
    }
    
    $extra_js .= "
        tasks[$project_id][$task_id] = '$task_label';";
  }

  $block .= "
    <form method=\"post\" name=\"f_add_task\" action=\"$url\"  
    onsubmit=\"check_get_task_id(this.sel_project.value,this.sel_projecttask.value,this.sel_tasktype.value);return false\">
    <table class=\"detail\" width=\"100%\">
    <tr>
      <td class=\"detailLabel\">$l_header_tasktype :</td>
      <td class=\"detailForm\" colspan=\"4\">$sel_tt</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_project :</td>
      <td class=\"detailForm\">$sel_proj</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_task :</td>    
      <td class=\"detailForm\" colspan=\"3\">$sel_projtask</td>
    </tr>
    <tr>
      <td colspan=\"2\" class=\"detailLabel\"><input type=\"submit\"></td>
    </tr>
    </table>    
  </form>
  "; 

  return $block; 
}
///////////////////////////////////////////////////////////////////////////////
// Display the task selector form, for external call
// Parameters:
//   - $params: project parameters hashtable
///////////////////////////////////////////////////////////////////////////////
function display_project_reftask_selector($project) {
  $reftask_q = run_query_project_reftasks();
  $block = html_project_reftask_selector($project,$reftask_q);
  return $block;
}
///////////////////////////////////////////////////////////////////////////////
// Display the task selector form, for external call
// Parameters:
//   - $params: project parameters hashtable
///////////////////////////////////////////////////////////////////////////////
function html_project_reftask_selector($project,$projectreftask_q) {
  global $c_undef, $l_undef, $extra_js,$l_project,$l_add;
  global $ctt_sales, $ctt_research, $ctt_others,$l_header_tasktype;
  global $l_tt_sales, $l_tt_research, $l_tt_others,$l_task;

  $extra_js .= "
        tasks = new Object();";

  // TaskType select construction
  $task_list = array($ctt_sales=>$l_tt_sales,
		     $ctt_research=>$l_tt_research,
		     $ctt_others=>$l_tt_others);

  $sel_tt = "
    <select name=\"sel_tasktype\"
      onchange=\"fill_projectreftask( this.value,this.form.elements[1]) \">
      <option value=\"$c_undef\">$l_undef</option>";
  for ($i=0; $i<2; $i++) {
    while($projectreftask_q->next_record()) {
      $tt_id = $projectreftask_q->f('tasktype_id');
      $tt_label = $projectreftask_q->f('tasktype_label');
      $tt_internal = $projectreftask_q->f('tasktype_internal');
      $projectreftask_id = $projectreftask_q->f('projectreftask_id');
      $projectreftask_name = addslashes($projectreftask_q->f('projectreftask_label'));
      
      //put menu titles
      if ($tt_internal != $type_prec) {
        if($type_prec != '') {
          $sel_tt .= '</optgroup>';
        }
        $sel_tt .= "<optgroup label=\"".$task_list[$tt_internal]."\">";
	$type_prec = $tt_internal;
      }
      if ($tt_id != $old_tt_id) {
        $extra_js .= "
          tasks[$tt_id] = new Object();"; 
        $sel_tt .= "\n<option value=\"$tt_id\">$tt_label</option>\n";   
        $old_tt_id =  $tt_id;
      }
      $extra_js .= "
        tasks[$tt_id][$projectreftask_id] = '$projectreftask_name';";


    }
  }
  $sel_tt .= "</optgroup>";
  $sel_tt .= "</select>";

  // ProjectTask select construction

  $block .= "
    <form method=\"post\" name=\"f_add_task\" action=\"$url\"  
    onsubmit=\"of_select_fill_from_checkbox(this,'task');window.close();return false\">
    <fieldset>
    <table class=\"detail\" width=\"100%\">
    <tr>
      <td class=\"detailLabel\">$l_header_tasktype :</td>
      <td class=\"detailForm\" colspan=\"4\">$sel_tt</td>
    </tr>
    </table>  
    </fieldset>
    <fieldset>
    <legend>$l_task</legend>
    <table  class=\"detail\" width=\"100%\" id=\"task_checkbox\">
    </table>  
    <table  class=\"detail\" width=\"100%\">
      <tr>
      <td colspan=\"2\" class=\"detailLabel\"><input type=\"submit\" value=\"$l_add\"></td>
      </tr>
      </table>
    </fieldset>
  </form>
  "; 

  return $block; 
}
///////////////////////////////////////////////////////////////////////////////
// Display the project administration index
///////////////////////////////////////////////////////////////////////////////
function dis_project_admin_index() {

  $reftask_q = run_query_project_reftasks();
  $tasktype_q = run_query_project_tasktype();  
  if($tasktype_q->nf() > 0) {
    $block .= dis_project_reftask_form($reftask_q,$tasktype_q);
  }

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display: Admin Category section
// Parameters:
//   - $entity : target entity
//   - $catu   : target category (string) can have Uppercase eg: CompanyRole
//   - $cats   : Category array
///////////////////////////////////////////////////////////////////////////////
function dis_project_reftask_form($reftask_q,$tasktype_q) {
  global $l_c_manage, $l_, $l_label,$l_tasktype,$l_delete_reftask;
  global $l_c_checkdelete, $l_c_update, $l_c_upd, $l_c_new, $l_c_insert;
  global $l_c_j_create, $l_c_j_insert, $l_c_j_update,$l_reftask_manage;
  global $ctt_sales, $ctt_research, $ctt_others,$l_header_tasktype,$l_reftask;
  global $l_tt_sales, $l_tt_research,$l_tt_others,$l_task,$extra_js,$ico_ok,$ico_delete;

  // TaskType select construction
  $task_list = array($ctt_sales=>$l_tt_sales,
		     $ctt_research=>$l_tt_research,
                     $ctt_others=>$l_tt_others);
  while($reftask_q->next_record()) {
    $tt_id = $reftask_q->f('tasktype_id');
    $tt_label = $reftask_q->f('tasktype_label');
    $projectreftask_id = $reftask_q->f('projectreftask_id');
    $projectreftask_name = addslashes($reftask_q->f('projectreftask_label'));
    if($tt_id != $old_tt_id) {
      if($old_tt_id != '') {
        $reftask_list .= '</tr>';
      }
      $reftask_list .= "<thead><tr><td colspan=\"3\">$tt_label</td></tr></thead>";
      $old_tt_id = $tt_id;
    }
    $reftask_list .= "
    <tr>
    <td>
    <input type=\"hidden\" id=\"reftask_$projectreftask_id\" value=\"$projectreftask_id\" />
    <input type=\"hidden\" id=\"tasktype_$projectreftask_id\" value=\"$tt_id\" />
    <a href=\"?action=reftask_delete&amp;category=reftask&amp;reftask=$projectreftask_id\">
    <img src=\"$ico_delete\" alt=\"$l_reftask_delete\" title=\"$l_reftask_delete\" />
    </a></td>
    <td><input type=\"text\" id=\"tf_reftask_label_$projectreftask_id\" value=\"$projectreftask_name\" /></td>
    <td>
    <a href=\"#\" onclick=\"return updateLine($projectreftask_id,'reftask');\">
    <img src=\"$ico_ok\" alt=\"$l_c_j_update\" title=\"$l_c_j_update\" />
    </a>
    </td>
    </tr>";
  }
  $reftask_list .= '</tr>';
  
  
  
  
  $sel_tasktype = "<select size=\"8\" name=\"sel_tasktype\">";
  $selected = "selected='selected'";
  while($tasktype_q->next_record()) {
    $tt_internal = $tasktype_q->f('tasktype_internal');
    $tt_label = $tasktype_q->f('tasktype_label');
    $tt_id = $tasktype_q->f('tasktype_id');

    if ($tt_internal != $type_prec) {
      if($type_prec != '') {
        $sel_tasktype .= '</optgroup>';
      }
      $sel_tasktype .= "<optgroup label=\"".$task_list[$tt_internal]."\">";      
      $type_prec = $tt_internal;
    }    
    $sel_tasktype .= "\n<option $selected value=\"$tt_id\">$tt_label</option>";
    $selected = ''; 

  }
  $sel_tasktype .= '</optgroup>';
  $sel_tasktype .= '</select>';

  $block = "
    <div class=\"detail admin\">
    <h1>$l_reftask_manage</h1>
    <form id=\"form_reftask_update\" method=\"post\" action=\"?action=reftask_update\">
    <input type=\"hidden\" name=\"tf_reftask_label\" value=\"\" />
    <input type=\"hidden\" name=\"reftask\" value=\"\" />
    <input type=\"hidden\" name=\"tasktype\" value=\"\" />
    </form>
    <table>
    <thead>
    <tr>
    <td colspan=\"3\">&nbsp;</td>
    </tr>
    <tr>
    <td colspan=\"3\">$l_reftask</td>
    </tr>
    </thead>
    <tbody>
    $reftask_list
    </tbody>
    </table>
    <form name=\"form_reftask_new\" method=\"post\" action=\"\" onsubmit=\"return (check_task_new(this) && check_tasktype(this.sel_tasktype))\">
    <table>
    <thead>
    <tr>
    <td colspan=\"3\">$l_c_new</td>
    </tr>
    </thead>
    <tbody>
    <tr>
    <td>$l_tasktype <br /> $sel_tasktype</td>
    </tr>
    <tr>
    <td>$l_label <br /> <input type=\"text\" name=\"tf_reftask_label\" /></td>
    </tr>
    </tbody>
    <tfoot>
    <tr>
    <td>
    <input type=\"submit\" name=\"sub_reftask\" value=\"$l_c_insert\" />
    </td>
    </tr>
    </table>
    <input type=\"hidden\" name=\"action\" value=\"reftask_insert\" />
    </form>
    </div>
";

  return $block;
}
?>
