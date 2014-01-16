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
// OBM - File : admin_ref_display.inc                                        //
//     - Desc : Referential data display file                                //
// 2003-12-05 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display the Country index screen
///////////////////////////////////////////////////////////////////////////////
function dis_admin_ref_country_index() {

  $ctry_q = run_query_global_country();
  $block = html_admin_ref_country_form($ctry_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Country section
// Parameters:
//   - $ctry_q : Country list database object
///////////////////////////////////////////////////////////////////////////////
function html_admin_ref_country_form($ctry_q) {
  global $l_ctry_manage, $l_ctry_exist, $l_ctry_no;
  global $l_ctry_checkdelete, $l_ctry_update, $l_ctry_new, $l_ctry_insert;
  global $l_name, $l_lang, $l_iso_3166, $l_phone,$ico_ok,$ico_delete,$l_c_new;

  $char_title = '=';
  $key = 0;
  while ($ctry_q->next_record()) {
    $iso = $ctry_q->f('country_iso3166');
    $lang = $ctry_q->f('country_lang');
    $name = $ctry_q->f('country_name');
    $phone = $ctry_q->f('country_phone');
    $key = "$iso-$lang";

    if ($key%2 == 0) {
      $class = "class=\"pair\"";
    } else {
      $class = '';
    }
    $c_label = htmlentities($one_cat['label'], ENT_COMPAT, 'UTF-8');
    $ctry_list .= "
    <tr $class>
    <td>
    <a
    href=\"?action=country_checklink&amp;category=country&amp;ctry_code=$iso&amp;ctry_lang=$lang&amp;ctry_name=$name&amp;ctry_phone=$phone\">
    <img src=\"$ico_delete\" alt=\"$l_ctry_checkdelete\" title=\"$l_ctry_checkdelete\" />
    </a></td>
    <td>
    <input type=\"hidden\" id=\"tf_ctry_old_code_$key\" value=\"$iso\" size=\"3\" />
    <input type=\"text\" id=\"tf_ctry_code_$key\" maxlength=\"2\" value=\"$iso\" size=\"3\" /></td>
    <td>
    <input type=\"hidden\" id=\"tf_ctry_old_lang_$key\" value=\"$lang\" size=\"3\" />
    <input type=\"text\" id=\"tf_ctry_lang_$key\" maxlength=\"2\" value=\"$lang\" size=\"3\" /></td>
    <td><input type=\"text\" id=\"tf_ctry_name_$key\" value=\"$name\" size=\"12\" /></td>
    <td><input type=\"text\" id=\"tf_ctry_phone_$key\" value=\"$phone\" size=\"6\" /></td>
    <td>
    <a  onclick=\"return updateLine('$key','country');\">
    <img src=\"$ico_ok\" alt=\"$l_ctry_update\" title=\"$l_ctry_update\" />
    </a>
    </td>
    </tr>";
  }

  // --- HTML Template --------------------------------------------------------
  $block = "
  <div class=\"detail admin\" >
  <form id=\"form_country_update\" action=\"?action=country_update\" method=\"post\">
  <input type=\"hidden\" name=\"tf_ctry_name\" value=\"\" />
  <input type=\"hidden\" name=\"tf_ctry_code\" value=\"\" />
  <input type=\"hidden\" name=\"tf_ctry_old_code\" value=\"\" />
  <input type=\"hidden\" name=\"tf_ctry_lang\" value=\"\" />
  <input type=\"hidden\" name=\"tf_ctry_old_lang\" value=\"\" />
  <input type=\"hidden\" name=\"tf_ctry_phone\" value=\"\" />
  <input type=\"hidden\" name=\"action\" value=\"country_update\" />
  </form>    
  <h1>$l_ctry_manage</h1>
  <div class=\"overflow\">
  <table>
  <thead>
  <tr>
  <td>&nbsp;</td>
  <td>$l_iso_3166</td>
  <td>$l_lang</td>
  <td>$l_name</td>
  <td>$l_phone</td>
  <td>&nbsp;</td>
  </tr>    
  </thead>
  <tbody>
  $ctry_list
  </tbody>
  </table>
  </div>    
  <form id=\"form_country_new\" action=\"\" method=\"post\" onsubmit=\"return check_admin_ref_ctry_new(this)\">
  <table>
  <thead>
  <tr>
  <td colspan=\"4\">$l_c_new</td>
  </tr>
  <tbody>   
  <tr>
  <td>$l_iso_3166</td>    
  <td>$l_lang</td>
  <td>$l_name</td>    
  <td>$l_phone</td>    
  </tr>       
  <tr>
  <td><input type=\"text\" name=\"tf_ctry_code\" value=\"\" size=\"3\" /></td>
  <td><input type=\"text\" name=\"tf_ctry_lang\" value=\"\" size=\"3\" /></td>
  <td><input type=\"text\" name=\"tf_ctry_name\" value=\"\" size=\"12\" /></td>    
  <td><input type=\"text\" name=\"tf_ctry_phone\" value=\"\" size=\"6\" /></td>    
  </tr>  
  </tbody>
  <tfoot>
  <tr>
  <td colspan=\"4\"><input type=\"submit\" value=\"$l_ctry_insert\" /></td>
  </tr>
  </tfoot>
  </table>
  <input type=\"hidden\" name=\"action\" value=\"country_insert\" />
  <input type=\"hidden\" name=\"category\" value=\"country\" />
  </form>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Country links
// Parameters:
//   - $ref : country hash info : keys used : name, iso, lang
///////////////////////////////////////////////////////////////////////////////
function dis_admin_ref_country_links($ref) {
  global $display, $l_back, $l_ctry_link_company, $l_ctry_link_company_no;
  global $l_ctry_link_contact, $l_ctry_link_contact_no;
  global $l_ctry_delete, $l_ctry_can_delete, $l_ctry_cant_delete;

  $delete_ok = true;
  $iso = $ref['ctry_code'];
  $lang = $ref['ctry_lang'];
  $name = get_admin_ref_country_name($iso, $lang);

  // Company Links
  $obm_q = run_query_admin_ref_country_company_links($iso);
  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $dis_link_comp_head = "<h1>$name($iso) : $l_ctry_link_company</h1>";

    while ($obm_q->next_record()) {
      $cid = $obm_q->f('company_id');
      $cname = $obm_q->f('company_name');
      $dis_link_comp .= "<li><a href=\"".url_prepare("company_index.php?action=detailconsult&amp;company_id=$cid"). "\">$cname</a></li>";
    }

  } else {
    $dis_link_comp_head = "<h1>$name($iso) : $l_ctry_link_company_no</h1>";
  }

  // Contacts Links
  $obm_q = run_query_admin_ref_country_contact_links($iso);
  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $dis_link_cont_head = "<h1>$name($iso) : $l_ctry_link_contact</h1>";

    while ($obm_q->next_record()) {
      $cid = $obm_q->f('contact_id');
      $cname = $obm_q->f('contact_lastname').' '.$obm_q->f('contact_firstname');
      $dis_link_cont .= "<li><a href=\""
      .url_prepare("contact_index.php?action=consult&amp;id=$cid")
      . "\">$cname</a></li>";
    }

  } else {
    $dis_link_cont_head = "<h1>$name($iso) : $l_ctry_link_contact_no</h1>";
  }

  $block .= "<div class=\"detail\">
    $dis_link_comp_head
    <ul>
    $dis_link_comp
    </ul>
    $dis_link_cont_head
    <ul>
    $dis_link_cont
    </ul>
    </div>";

  $dis_back = "<a href=\"$path/admin_ref/admin_ref_index.php?action=country\">$l_back</a>";

  if ($delete_ok == true) {
    $display['msg'] .= display_ok_msg($l_ctry_can_delete);
    $dis_del = "<a
    href=\"$path/admin_ref/admin_ref_index.php?action=country_delete&amp;ctry_code=$iso&amp;ctry_lang=$lang\">$l_ctry_delete</a>";
    $block .= "
      <div class=\"buttons\">
        $dis_del
        $dis_back
      </div>";
  } else {
    $display['msg'] .= display_warn_msg($l_ctry_cant_delete);
    $block .= "
      <div class=\"buttons\">
        $dis_back
      </div>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Data Source index screen
///////////////////////////////////////////////////////////////////////////////
function dis_admin_ref_datasource_index() {

  $dsrc_q = run_query_global_datasource();
  $block = html_admin_ref_datasource_form($dsrc_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Data Source section
// Parameters:
//   - $dsrc_q : Data Source list database object
///////////////////////////////////////////////////////////////////////////////
function html_admin_ref_datasource_form($dsrc_q) {
  global $l_dsrc_manage, $l_dsrc_exist, $l_dsrc_no;
  global $l_dsrc_checkdelete, $l_dsrc_update, $l_dsrc_new, $l_dsrc_insert,$ico_ok,$ico_delete;

  $char_title = '=';
  $key = 0;
  while ($dsrc_q->next_record()) {
      $id = $dsrc_q->f('datasource_id');
      $name = $dsrc_q->f('datasource_name');
    
    if ($key%2 == 0) {
      $class = "class=\"pair\"";
    } else {
      $class = '';
    }
    $dsrc_list .= "
    <tr $class>
    <td>
    <input type=\"hidden\" id=\"dsrc_$id\" value=\"$id\" />
    <a href=\"?action=datasource_checklink&amp;category=datasource&amp;dsrc=$id\">
    <img src=\"$ico_delete\" alt=\"$l_dsrc_checkdelete\" title=\"$l_dsrc_checkdelete\" />
    </a></td>
    <td><input type=\"text\" id=\"tf_dsrc_name_$id\" value=\"$name\" size=\"30\" /></td>
    <td>
    <a href=\"#\" onclick=\"return updateLine($id,'datasource');\">
    <img src=\"$ico_ok\" alt=\"$l_dsrc_update\" title=\"$l_dsrc_update\" />
    </a>
    </td>
    </tr>";
  }

  // --- HTML Template --------------------------------------------------------

  $block = "
  <div class=\"detail admin\" >
  <form id=\"form_datasource_update\" action=\"?action=datasource_update\" method=\"post\">
  <input type=\"hidden\" name=\"dsrc\" value=\"\" />
  <input type=\"hidden\" name=\"tf_dsrc_name\" value=\"\" />
  </form>    
  <h1>$l_dsrc_manage</h1>
  <div class=\"overflow\">
  <table>
  <thead>
  <tr>
  <td>&nbsp;</td>
  <td>$l_dsrc_exist</td>
  <td>&nbsp;</td>
  </tr>    
  </thead>
  <tbody>
  $dsrc_list
  </tbody>
  </table>
  </div>    
  <form id=\"form_datasource_new\" action=\"\" method=\"post\" onsubmit=\"return check_admin_ref_dsrc_new(this)\">
  <table>
  <thead>
  <tr>
  <td colspan=\"4\">$l_dsrc_new</td>
  </tr>
  <tbody>   
  <tr>
  <td><input type=\"text\" name=\"tf_dsrc_name\" value=\"\" size=\"30\" /></td>
  </tr>  
  </tbody>
  <tfoot>
  <tr>
  <td colspan=\"4\"><input type=\"submit\" value=\"$l_dsrc_insert\" /></td>
  </tr>
  </tfoot>
  </table>
  <input type=\"hidden\" name=\"action\" value=\"datasource_insert\" />
  <input type=\"hidden\" name=\"category\" value=\"datasource\" />
  </form>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Data Source links
// Parameters:
//   - $ref : datasource hash info : keys used : id, name
///////////////////////////////////////////////////////////////////////////////
function dis_admin_ref_datasource_links($ref) {
  global $path, $display, $l_back;
  global $l_dsrc_link_company, $l_dsrc_link_company_no;
  global $l_dsrc_link_contact, $l_dsrc_link_contact_no;
  global $l_dsrc_delete, $l_dsrc_can_delete, $l_dsrc_cant_delete;

  $delete_ok = true;
  $id = $ref['dsrc'];
  $name = get_admin_ref_datasource_name($id);

  // Company Links
  $obm_q = run_query_admin_ref_datasource_company_links($ref);
  $nb = $obm_q->num_rows();
  if ($nb > 0) {
    $delete_ok = false;
    $dis_link_comp_head = "<h1>$name : $l_dsrc_link_company ($nb)</h1>";

    while ($obm_q->next_record()) {
      $cid = $obm_q->f('company_id');
      $cname = $obm_q->f('company_name');
      $dis_link_comp .= "<li><a href=\"".url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$cid"). "\">$cname</a></li>";
    }
      } else {
    $dis_link_comp_head = "<h1>$name : $l_dsrc_link_company_no</h1>";
  }

  // Contacts Links
  $obm_q = run_query_admin_ref_datasource_contact_links($ref);
  $nb = $obm_q->num_rows();
  if ($nb > 0) {
    $delete_ok = false;
    $dis_link_cont_head = "<h1>$name : $l_dsrc_link_contact ($nb)</h1>";

    while ($obm_q->next_record()) {
      $cid = $obm_q->f('contact_id');
      $cname = $obm_q->f('contact_lastname').' '.$obm_q->f('contact_firstname');
      $dis_link_cont .= "<li><a href=\"".url_prepare("$path/contact/contact_index.php?action=consult&amp;id=$cid"). "\">$cname</a></li>";
    }

  } else {
    $dis_link_cont_head = "<h1>$name : $l_dsrc_link_contact_no</h1>";
  }

  $block .= "<div class=\"detail\">
    $dis_link_comp_head
    <ul>
    $dis_link_comp
    </ul>
    $dis_link_cont_head
    <ul>
    $dis_link_cont
    </ul>
    </div>";

  $dis_back = "<a href=\"$path/admin_ref/admin_ref_index.php?action=datasource\">$l_back</a>";

  if ($delete_ok == true) {
    $display['msg'] .= display_ok_msg($l_dsrc_can_delete);
    $dis_del = "<a
    href=\"$path/admin_ref/admin_ref_index.php?action=datasource_delete&amp;dsrc=$id&amp;dsrc_name=$name\">$l_dsrc_delete</a>";
    $block .= "
      <div class=\"buttons\">
        $dis_del
        $dis_back
      </div>";
  } else {
    $display['msg'] .= display_warn_msg($l_dsrc_cant_delete);
    $block .= "
      <div class=\"buttons\">
        $dis_back
      </div>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Task Type index screen
///////////////////////////////////////////////////////////////////////////////
function dis_admin_ref_tasktype_index() {

  $tts = get_global_tasktype();
  $block = html_admin_ref_tasktype_form($tts);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Task Type section
// Parameters:
//   - $tts : Task Type array
///////////////////////////////////////////////////////////////////////////////
function html_admin_ref_tasktype_form($tts) {
  global $l_code, $l_tt_manage, $l_tt_exist, $l_tt_no;
  global $l_tt, $l_tt_checkdelete, $l_tt_update, $l_tt_new, $l_tt_insert;
  global $ctt_sales, $ctt_research, $ctt_others;
  global $l_tt_sales, $l_tt_research, $l_tt_others,$ico_delete,$ico_ok;

  $tt_list = array($ctt_sales => $l_tt_sales,
		     $ctt_research => $l_tt_research,
		     $ctt_others => $l_tt_others);

  $type_prec = -1;
  if (is_array($tts)) {
    foreach ($tts as $id=>$one_tt) {
      $tt_label = $one_tt['label'];
      $tt_code = $one_tt['code'];
      $tt_internal = $one_tt['internal'];
      
      // put menu titles
      $tasktypelist[$tt_internal] .= "
    <tr>
    <td>
    <input type=\"hidden\" id=\"tasktype_internal_$tt_internal\" value=\"$tt_internal\" />
    <input type=\"hidden\" id=\"tasktype_$id\" value=\"$id\" />
    <a
    href=\"?action=tasktype_checklink&amp;category=tasktype&amp;tasktype_id=$id&amp;tasktype_internal=$tt_internal\">
    <img src=\"$ico_delete\" alt=\"$l_tt_delete\" title=\"$l_tt_delete\" />
    </a></td>
    <td><input type=\"text\" id=\"tf_tt_code_$id\" size=\"10\" maxlength=\"10\" value=\"$tt_code\" /></td>
    <td><input type=\"text\" id=\"tf_tt_label_$id\" value=\"$tt_label\" /></td>
    <td>
    <a href=\"#\" onclick=\"return updateLine($id,'tasktype');\">
    <img src=\"$ico_ok\" alt=\"$l_tt_update\" title=\"$l_tt_update\" />
    </a>
    </td>
    </tr>      
";
    } 
  }

  // --- HTML Template --------------------------------------------------------

  $block = "
    <form id=\"form_tasktype_update\" method=\"post\" action=\"?action=tasktype_update\">
    <input type=\"hidden\" name=\"tf_tt_code\" value=\"\" />
    <input type=\"hidden\" name=\"tf_tt_label\" value=\"\" />
    <input type=\"hidden\" name=\"tasktype_internal\" value=\"\" />
    <input type=\"hidden\" name=\"tasktype\" value=\"\" />
    </form>    
    <div class=\"detail admin\">
    <h1>$tt_list[$ctt_sales]</h1>
    <div class=\"overflow\">
    <table>
    <thead>
    <tr>
    <td>&nbsp;</td>
    <td>$l_code</td>
    <td>$l_tt_exist</td>
    <td>&nbsp;</td>
    </tr>
    </thead>
    <tbody>
    $tasktypelist[$ctt_sales]
    </tbody>
    </table>
    </div>

    <form id=\"form_tasktype1_new\" method=\"post\" action=\"\">
    <table>
    <thead>
    <tr>
    <td colspan=\"3\">$l_tt_new</td>
    </tr>
    </thead>
    <tbody>
    <tr>
      <td>&nbsp;</td>
      <td>$l_code</td>
      <td>$l_tt</td>
    </tr>
    <tr>
    <td><input type=\"hidden\" name=\"rd_internal\" value=\"0\"/></td>
    <td><input type=\"text\" name=\"tf_tt_code\" size=\"10\" maxlength=\"10\" /></td>
    <td><input type=\"text\" name=\"tf_tt_label\" /></td>
    </tr>
    </tbody>
    <tfoot>
    <tr>
    <td colspan=\"3\">
    <input type=\"hidden\" name=\"action\" value=\"tasktype_insert\" />
    <input type=\"submit\" name=\"sub_tt\" value=\"$l_tt_insert\" onclick=\"if (check_admin_ref_tt_new(this.form)) return true; else return false;\" />
    </td>
    </tr>
    </tfoot>
    </table>
    </form>
    </div>

   <div class=\"detail admin\">
    <h1>$tt_list[$ctt_others]</h1>
    <div class=\"overflow\">
    <table>
    <thead>
    <tr>
    <td>&nbsp;</td>
    <td>$l_code</td>
    <td>$l_tt_exist</td>
    <td>&nbsp;</td>
    </tr>
    </thead>
    <tbody>
    $tasktypelist[$ctt_others]
    </tbody>
    </table>
    </div>
    <form id=\"form_tasktype2_new\" method=\"post\" action=\"\">
    <table>
    <thead>
    <tr>
    <td colspan=\"3\">$l_tt_new</td>
    </tr>
    </thead>
    <tbody>
    <tr>
      <td>&nbsp;</td>
      <td>$l_code</td>
      <td>$l_tt</td>
    </tr>
    <tr>
    <td><input type=\"hidden\" name=\"rd_internal\" value=\"2\"/></td>
    <td><input type=\"text\" name=\"tf_tt_code\" size=\"10\" maxlength=\"10\" /></td>
    <td><input type=\"text\" name=\"tf_tt_label\" /></td>
    </tr>
    </tbody>
    <tfoot>
    <tr>
    <td colspan=\"3\">
    <input type=\"hidden\" name=\"action\" value=\"tasktype_insert\" />
    <input type=\"submit\" name=\"sub_tt\" value=\"$l_tt_insert\" onclick=\"if (check_admin_ref_tt_new(this.form)) return true; else return false;\" />
    </td>
    </tr>
    </tfoot>
    </table>
    </form>
    </div>

    <div class=\"detail admin\">
    <h1>$tt_list[$ctt_research]</h1>
    <div class=\"overflow\">
    <table>
    <thead>
    <tr>
    <td>&nbsp;</td>
    <td>$l_code</td>
    <td>$l_tt_exist</td>
    <td>&nbsp;</td>
    </tr>
    </thead>
    <tbody>
    $tasktypelist[$ctt_research]
    </tbody>
    </table>
    </div>
    <form id=\"form_tasktype3_new\" method=\"post\" action=\"\">
    <table>
    <thead>
    <tr>
    <td colspan=\"3\">$l_tt_new</td>
    </tr>
    </thead>
    <tbody>
    <tr>
      <td>&nbsp;</td>
      <td>$l_code</td>
      <td>$l_tt</td>
    </tr>
    <tr>
    <td><input type=\"hidden\" name=\"rd_internal\" value=\"1\"/></td>
    <td><input type=\"text\" name=\"tf_tt_code\" size=\"10\" maxlength=\"10\" /></td>
    <td><input type=\"text\" name=\"tf_tt_label\" /></td>
    </tr>
    </tbody>
    <tfoot>
    <tr>
    <td colspan=\"3\">
    <input type=\"hidden\" name=\"action\" value=\"tasktype_insert\" />
    <input type=\"submit\" name=\"sub_tt\" value=\"$l_tt_insert\" onclick=\"if (check_admin_ref_tt_new(this.form)) return true; else return false;\" />
    </td>
    </tr>
    </tfoot>
    </table>
    </form>
    </div>    
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Task Type links
// Parameters:
//   - $ref : tasktype hash info : keys used : id, name
///////////////////////////////////////////////////////////////////////////////
function dis_admin_ref_tasktype_links($ref) {
  global $path, $display, $l_back, $l_tt_link_deal, $l_tt_link_deal_no;
  global $l_tt_link_project, $l_tt_link_project_no, $l_tt_link_time, $l_tt_link_time_no;
  global $l_tt_delete, $l_tt_can_delete, $l_tt_cant_delete;

  $delete_ok = true;
  $id = $ref['tasktype_id'];
  $label = get_admin_ref_tasktype_label($id);

  // Deal Links
  $obm_q = run_query_admin_ref_tasktype_deal_links($ref);
  $nb_deal = $obm_q->num_rows();
  if ($nb_deal > 0) {
    $delete_ok = false;
    $dis_link_deal_head = "<h1>$label : $l_tt_link_deal ($nb_deal)</h1>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $did = $obm_q->f('deal_id');
      $dlabel = $obm_q->f('deal_label');
      $dis_link_deal .= "<li><a href=\""
      .url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;deal_id=$did")
      . "\">$dlabel</a></li>";
    }
    if ($cpt < $nb_deal) {
      $dis_link_deal .= "<li>...</li>";
    }

  } else {
    $dis_link_deal_head = "<h1>$label : $l_tt_link_deal_no</h1>";
  }

  // Projects Links
  $obm_q = run_query_admin_ref_tasktype_project_links($ref);
  $nb_proj = $obm_q->num_rows();
  if ($nb_proj > 0) {
    $delete_ok = false;
    $dis_link_proj_head = "<h1>$label : $l_tt_link_project ($nb_proj)</h1>";
    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $pid = $obm_q->f('project_id');
      $pname = $obm_q->f('project_name');
      $dis_link_proj .= "<a href=\"" .url_prepare("$path/project/project_index.php?action=detailconsult&amp;project_id=$pid") . "\">$pname</a>";
    }
    if ($cpt < $nb_proj) {
      $dis_link_proj .= "<li>...</li>
    </tr>";
    }

  } else {
    $dis_link_proj_head = "<h1>$label : $l_tt_link_project_no</h1>";
  }

  // Time Task Links
  $obm_q = run_query_admin_ref_tasktype_timetask_links($ref);
  $nb_time = $obm_q->num_rows();
  if ($nb_time > 0) {
    $delete_ok = false;
    $dis_link_time_head = "<h1>$label : $l_tt_link_time ($nb_time)</h1>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $tid = $obm_q->f('timetask_user_id');
      $tname = $obm_q->f('lastname') . ' ' . $obm_q->f('firstname');
      $tdate = of_date_format($obm_q->f('date'), 1);
      $dis_link_time .= "<li><a href=\""
      .url_prepare("$path/time/time_index.php?action=index&amp;sel_user_id=$tid&amp;wbegin=$tdate")
      . "\">$tname ($tdate)</a></li>";
    }
    if ($cpt < $nb_time) {
      $dis_link_time .= "<li>...</li>";
    }

  } else {
    $dis_link_time_head = "<h1>$label : $l_tt_link_time_no</h1>";
  }

  $block .= "<div class=\"detail\">
    $dis_link_deal_head
    <ul>
    $dis_link_deal
    </ul>
    $dis_link_proj_head
    <ul>
    $dis_link_proj
    </ul>
    $dis_link_time_head
    <ul>
    $dis_link_time
    </ul>
    </div>";

  $dis_back = "<a href=\"$path/admin_ref/admin_ref_index.php?action=tasktype\">$l_back</a>";

  if ($delete_ok == true) {
    $display['msg'] .= display_ok_msg($l_tt_can_delete);
    $dis_del = "<a href=\"$path/admin_ref/admin_ref_index.php?action=tasktype_delete&amp;tasktype=$id\">$l_tt_delete</a>";
    $block .= "
      <div class=\"buttons\">
        $dis_del
        $dis_back
      </div>";
  } else {
    $display['msg'] .= display_warn_msg($l_tt_cant_delete);
    $block .= "
      <div class=\"buttons\">
        $dis_back
      </div>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Task Type select list for external call
///////////////////////////////////////////////////////////////////////////////
function html_admin_ref_tasktype_select_list($ref) {
  global $l_close, $l_add;
  global $l_tt_no, $ctt_sales, $ctt_research, $ctt_others;
  global $l_tt_sales, $l_tt_research, $l_tt_others;

  $tt_list = array($ctt_sales => $l_tt_sales,
		     $ctt_research => $l_tt_research,
		     $ctt_others => $l_tt_others);

  $type_prec = -1;

  $tts = get_global_tasktype($ref['tt_target']);

  if (is_array($tts)) {

    $ext_action = $ref['ext_action'];
    $ext_url = $ref['ext_url'];
    $ext_id = $ref['ext_id'];
    $ext_target = $ref['ext_target'];
    $ext_widget = $ref['ext_widget'];
    $ext_element = $ref['ext_element'];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_element=$ext_element&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget";

    if ($ext_element != '') {
      $form_head = "
      <form onsubmit=\"of_select_fill_from_checkbox(this, 'tt'); window.close(); return false;\">";
    } elseif ($ext_widget != '') {
      $form_head = "
      <form onsubmit=\"fill_ext_form(this); return false;\">";
    } else {
      $form_head = "
      <form target=\"$ext_target\" method=\"post\" action=\"$ext_url\">";
    }

    $table_tt = "<table>";
    foreach ($tts as $id=>$one_tt) {
      $tt_label = $one_tt['label'];
      $internal = $one_tt['internal'];
      // put menu titles
      if ($internal != $type_prec) {
	$table_tt .= "\n<tr><td class=\"detailLabel\">".$tt_list[$internal]."</td></tr>";
	$type_prec = $internal;
      } else {
	$table_tt .= "\n<tr><td class=\"detailText\"><input type=\"checkbox\" name=\"$id\"><span id=\"data-tt-$id\" style=\"display:none;\">$tt_label</span>&nbsp;&nbsp;$tt_label</td></tr>";
      }
    }
    $table_tt .= "</table>";

    $block = "
<!-- Task Type Select list -->
$form_head
$table_tt

      <p class=\"detailButton\">
        <p class=\"detailButtons\">
        <input type=\"submit\" value=\"$l_add\" />
        <input type=\"hidden\" name=\"ext_id\" value=\"$ext_id\" />
        <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
        </p>
      </p>
      </form>

      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>
";
    
  } else {
    $block = $l_tt_no;
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Region index screen
///////////////////////////////////////////////////////////////////////////////
function dis_admin_ref_region_index() {

  $regions = of_category_get_ordered('', 'region');
  $block .= of_category_dis_admin_form('region', $regions);

  return $block;
}


?>
