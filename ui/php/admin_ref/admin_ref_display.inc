<?php
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
  global $l_name, $l_lang, $l_iso_3166, $l_phone;

  if ($ctry_q->num_rows() > 0) {

    // Country select construction
    $sel_ctry = "<select name=\"sel_country\" onChange=\"
      mytext = this.options[this.selectedIndex].text;

      // Get Iso
      pos = mytext.indexOf('-');
      iso = mytext.substr(0, pos);

      // Get Lang
      mytext = mytext.substr(pos+1);
      pos = mytext.indexOf('-');
      lang = mytext.substr(0,pos);

      // Get name
      mytext = mytext.substr(pos+1);
      pos = mytext.indexOf('(');
      myname = mytext.substr(0,pos-1);

      // Get phone
      mytext = mytext.substr(pos+1);
      pos = mytext.indexOf(')');
      phone = mytext.substr(0,pos);

      forms.form_ctry_update.tf_name.value=myname;
      forms.form_ctry_update.tf_iso.value=iso;
      forms.form_ctry_update.tf_lang.value=lang;
      forms.form_ctry_update.tf_phone.value=phone;
      forms.form_ctry_update.hd_old_iso.value=iso;
      forms.form_ctry_update.hd_old_lang.value=lang;\"
      size=\"20\">";
    while ($ctry_q->next_record()) {
      $iso = $ctry_q->f("country_iso3166");
      $lang = $ctry_q->f("country_lang");
      $name = $ctry_q->f("country_name");
      $phone = $ctry_q->f("country_phone");
      $sel_ctry .= "\n<option value=\"$iso-$lang\">$iso-$lang-$name ($phone)</option>";
    }
    $sel_ctry .= "</select>";

    $dis_update_delete = "
<!-- Country Check Delete Section -->

    <table>
    <tr>
      <td colspan=\"3\">
      <form name=\"form_ctry_delete\" method=\"post\"
        action=\"" . url_prepare("admin_ref_index.php") . "\">
      <table>
      <tr>
        <td class=\"adminLabel\">$l_ctry_exist</td>
        <td class=\"adminLabel\">$sel_ctry</td>
        <td>
          <input type=\"hidden\" name=\"action\" value=\"country_checklink\" />
          <input type=\"submit\" name=\"sub_ctry\" value=\"$l_ctry_checkdelete\"
            onclick=\"if (check_admin_ref_ctry_checkdel(this.form.sel_country)) return true;
            else return false;\" />
        </td>
      </tr>
      </table>
      </form>
      </td>
    </tr>

<!-- Country Update Section -->

    <tr>
      <td colspan=\"2\">
        <form name=\"form_ctry_update\" method=\"get\"
          action=\"" . url_prepare("admin_ref_index.php") . "\">
        <input type=\"hidden\" name=\"hd_old_iso\" value=\"\" />
        <input type=\"hidden\" name=\"hd_old_lang\" value=\"\" />
        <input type=\"hidden\" name=\"action\" value=\"country_update\" />

        <table>
        <tr>
          <td class=\"adminLabel\">$l_name : </td>
          <td><input type=\"text\" name=\"tf_name\" maxlength=\"64\" /></td>
          <td rowspan=\"4\">
          <input type=\"submit\" name=\"sub_ctry\" value=\"$l_ctry_update\"
            onclick=\"if (check_admin_ref_ctry_upd(this.form,document.form_ctry_delete.sel_country)) return true; else return false;\" />
          </td>
        </tr>
        <tr>
          <td class=\"adminLabel\">$l_iso_3166 : </td>
          <td>
            <input type=\"text\" name=\"tf_iso\" size=\"2\" maxlength=\"2\" />
	  </td>
        </tr>
        <tr>
          <td class=\"adminLabel\">$l_lang : </td>
          <td>
            <input type=\"text\" name=\"tf_lang\" size=\"2\" maxlength=\"2\" />
          </td>
        </tr>
        <tr>
          <td class=\"adminLabel\">$l_phone : </td>
          <td>
            <input type=\"text\" name=\"tf_phone\" size=\"5\" maxlength=\"5\" />
          </td>
        </tr>
        </table>

        </form>
      </td>
      <td>&nbsp;</td>
    </tr>
    </table>";
    
  } else {
    $dis_update_delete = $l_ctry_no;
  }

  // --- HTML Template --------------------------------------------------------

  $block = "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">$l_ctry_manage</td>
    </tr>
    <tr>
      <td>
      $dis_update_delete

      </td>
      <td>

<!-- New Country Section -->

        <form name=\"form_ctry_new\" method=\"get\"
          action=\"" . url_prepare("admin_ref_index.php") . "\">
        <table>
        <tr>
          <td class=\"adminLabel\">$l_ctry_new :
            <input name=\"tf_name\" /></td>
        </tr>
        <tr>
          <td class=\"adminLabel\">$l_iso_3166 : </td>
          <td><input type=\"text\" name=\"tf_iso\" size=\"2\" maxlength=\"2\" /></td>
        </tr>
        <tr>
          <td class=\"adminLabel\">$l_lang : </td>
          <td><input type=\"text\" name=\"tf_lang\" size=\"2\" maxlength=\"2\" /></td>
        </tr>
        <tr>
          <td class=\"adminLabel\">$l_phone : </td>
          <td><input type=\"text\" name=\"tf_phone\" size=\"5\" maxlength=\"5\" /></td>
        </tr>
        <tr>
          <td class=\"adminLabel\">
            <input type=\"hidden\" name=\"action\" value=\"country_insert\" />
            <input type=\"submit\" name=\"sub_ctry\" value=\"$l_ctry_insert\"
              onclick=\"if (check_admin_ref_ctry_new(this.form)) return true; else return false;\" />
          </td>
        </tr>
        </table>
        </form>
      </td>
    </tr>
    </table>";

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
  $iso = $ref["iso"];
  $lang = $ref["lang"];
  $name = get_admin_ref_country_name($iso, $lang);

  // Company Links
  $obm_q = run_query_admin_ref_country_company_links($iso);
  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $dis_link_comp = "
    <tr>
      <td class=\"detailHead\">$name($iso) : $l_ctry_link_company</td>
    </tr>";

    while ($obm_q->next_record()) {
      $cid = $obm_q->f("company_id");
      $cname = $obm_q->f("company_name");
      $dis_link_comp .= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("company_index.php?action=detailconsult&amp;company_id=$cid") . "\">$cname</a>
      </td>
    </tr>";
    }

  } else {
    $dis_link_comp = "
    <tr>
      <td class=\"detailHead\">$name($iso) : $l_ctry_link_company_no</td>
    </tr>";
  }

  // Contacts Links
  $obm_q = run_query_admin_ref_country_contact_links($iso);
  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $dis_link_cont = "
    <tr>
      <td class=\"detailHead\">$name($iso) : $l_ctry_link_contact</td>
    </tr>";

    while ($obm_q->next_record()) {
      $cid = $obm_q->f("contact_id");
      $cname = $obm_q->f("contact_lastname")." ".$obm_q->f("contact_firstname");
      $dis_link_cont .= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("contact_index.php?action=detailconsult&amp;contact_id=$cid") . "\">$cname</a>
      </td>
    </tr>";
    }

  } else {
    $dis_link_cont = "
    <tr>
      <td class=\"detailHead\">$name($iso) : $l_ctry_link_contact_no</td>
    </tr>";
  }

  $block .= "<table class=\"detail\">
    $dis_link_comp
    $dis_link_cont
    </table>";

  $dis_back = "<form name=\"form_back\" method=\"get\"
          action=\"" .url_prepare("admin_ref_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"country\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>";

  if ($delete_ok == true) {
    $display["msg"] .= display_ok_msg($l_ctry_can_delete);
    $dis_del = "<form name=\"form_ctry_delete\".
          method=\"post\" action=\"" . url_prepare("admin_ref_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"country_delete\" />
        <input type=\"hidden\" name=\"tf_iso\" value=\"$iso\" />
        <input type=\"hidden\" name=\"tf_lang\" value=\"$lang\" />
        <input type=\"submit\" name=\"sub_ctry\" value=\"$l_ctry_delete\" />
        </form>";
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_del</p>
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  } else {
    $display["msg"] .= display_warn_msg($l_ctry_cant_delete);
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_back</p>
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
  global $l_dsrc_checkdelete, $l_dsrc_update, $l_dsrc_new, $l_dsrc_insert;

  if ($dsrc_q->num_rows() > 0) {
    $sel_dsrc = "<select name=\"sel_datasource_id\" onChange=\"
      forms.form_dsrc_update.tf_name.value=this.options[this.selectedIndex].text;\" size=\"8\">";
    while ($dsrc_q->next_record()) {
      $id = $dsrc_q->f("datasource_id");
      $name = $dsrc_q->f("datasource_name");
      $sel_dsrc .= "\n<option value=\"$id\">$name</option>";
    }
    $sel_dsrc .= "</select>";

    $dis_update_delete = "
<!-- Data Source Check Delete Section -->

    <table>
    <tr>
      <td colspan=\"3\">
      <form name=\"form_dsrc_delete\" method=\"post\"
        action=\"" . url_prepare("admin_ref_index.php") . "\">
      <table>
      <tr>
        <td class=\"adminLabel\">$l_dsrc_exist</td>
        <td class=\"adminLabel\">$sel_dsrc</td>
        <td>
          <input type=\"hidden\" name=\"action\" value=\"datasource_checklink\" />
          <input type=\"submit\" name=\"sub_dsrc\" value=\"$l_dsrc_checkdelete\"
            onclick=\"if (check_admin_ref_dsrc_checkdel(this.form.sel_datasource_id)) return true;
            else return false;\" />
        </td>
      </tr>
      </table>
      </form>
      </td>
    </tr>

<!-- Data Source Update Section -->

    <tr>
      <td colspan=\"2\" class=\"adminLabel\">
        <form name=\"form_dsrc_update\" method=\"get\"
          action=\"" . url_prepare("admin_ref_index.php") . "\">
        <input type=\"hidden\" name=\"sel_datasource_id\" value=\"\" />
        <input type=\"hidden\" name=\"action\" value=\"datasource_update\" />
        <input type=\"text\" name=\"tf_name\" />
        <input type=\"submit\" name=\"sub_dsrc\" value=\"$l_dsrc_update\"
          onclick=\"if (check_admin_ref_dsrc_upd(this.form,document.form_dsrc_delete.sel_datasource_id)) return true; else return false;\" />
        </form>
      </td>
      <td>&nbsp;</td>
    </tr>
    </table>";
    
  } else {
    $dis_update_delete = $l_dsrc_no;
  }

  // --- HTML Template --------------------------------------------------------

  $block = "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">
        $l_dsrc_manage
      </td>
    </tr><tr>
      <td>
      $dis_update_delete

      </td>
      <td>

<!-- New Data Source Section -->

        <form name=\"form_dsrc_new\" method=\"get\"
          action=\"" . url_prepare("admin_ref_index.php") . "\">
        <table>
        <tr>
          <td class=\"adminLabel\">$l_dsrc_new :
            <input name=\"tf_name\" /></td>
        </tr><tr>
          <td class=\"adminLabel\">
            <input type=\"hidden\" name=\"action\" value=\"datasource_insert\" />
            <input type=\"submit\" name=\"sub_dsrc\" value=\"$l_dsrc_insert\"
              onclick=\"if (check_admin_ref_dsrc_new(this.form)) return true; else return false;\" />
          </td>
        </tr>
        </table>
        </form>
      </td>
    </tr>
    </table>";

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
  $id = $ref["datasource_id"];
  $name = get_admin_ref_datasource_name($id);

  // Company Links
  $obm_q = run_query_admin_ref_datasource_company_links($ref);
  $nb = $obm_q->num_rows();
  if ($nb > 0) {
    $delete_ok = false;
    $dis_link_comp = "
    <tr>
      <td class=\"detailHead\">$name : $l_dsrc_link_company ($nb)</td>
    </tr>";

    while ($obm_q->next_record()) {
      $cid = $obm_q->f("company_id");
      $cname = $obm_q->f("company_name");
      $dis_link_comp .= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$cid") . "\">$cname</a>
      </td>
    </tr>";
    }

  } else {
    $dis_link_comp = "
    <tr>
      <td class=\"detailHead\">$name : $l_dsrc_link_company_no</td>
    </tr>";
  }

  // Contacts Links
  $obm_q = run_query_admin_ref_datasource_contact_links($ref);
  $nb = $obm_q->num_rows();
  if ($nb > 0) {
    $delete_ok = false;
    $dis_link_cont = "
    <tr>
      <td class=\"detailHead\">$name : $l_dsrc_link_contact ($nb)</td>
    </tr>";

    while ($obm_q->next_record()) {
      $cid = $obm_q->f("contact_id");
      $cname = $obm_q->f("contact_lastname")." ".$obm_q->f("contact_firstname");
      $dis_link_cont .= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;contact_id=$cid") . "\">$cname</a>
      </td>
    </tr>";
    }

  } else {
    $dis_link_cont = "
    <tr>
      <td class=\"detailHead\">$name : $l_dsrc_link_contact_no</td>
    </tr>";
  }

  $block .= "<table class=\"detail\">
    $dis_link_comp
    $dis_link_cont
    </table>";

  $dis_back = "<form name=\"form_back\" method=\"get\"
          action=\"" .url_prepare("admin_ref_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"datasource\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>";

  if ($delete_ok == true) {
    $display["msg"] .= display_ok_msg($l_dsrc_can_delete);
    $dis_del = "<form name=\"form_dsrc_delete\".
          method=\"post\" action=\"" . url_prepare("admin_ref_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"datasource_delete\" />
        <input type=\"hidden\" name=\"sel_datasource_id\" value=\"$id\" />
        <input type=\"submit\" name=\"sub_dsrc\" value=\"$l_dsrc_delete\" />
        </form>";
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_del</p>
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  } else {
    $display["msg"] .= display_warn_msg($l_dsrc_cant_delete);
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_back</p>
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
  global $l_tt_manage, $l_tt_exist, $l_tt_no;
  global $l_tt_checkdelete, $l_tt_update, $l_tt_new, $l_tt_insert;
  global $ctt_sales, $ctt_research, $ctt_others;
  global $l_tt_sales, $l_tt_research, $l_tt_others;

  $tt_list = array($ctt_sales => $l_tt_sales,
		     $ctt_research => $l_tt_research,
		     $ctt_others => $l_tt_others);

  $type_prec = -1;
  $sel_tt = "<select name=\"sel_tasktype_id\" onChange=\"
      forms.form_tt_update.tf_label.value=this.options[this.selectedIndex].text;\" size=\"16\">";
  if (is_array($tts)) {
    foreach ($tts as $id=>$one_tt) {
      $tt_label = $one_tt["label"];
      $internal = $one_tt["internal"];
      // put menu titles
      if ($internal != $type_prec) {
	$sel_tt .= "<option disabled=\"disabled\">".$tt_list[$internal]."</option>";
	$type_prec = $internal;
      }
      $sel_tt .= "\n<option value=\"$id\"";
      if ($tt == $id) $sel_tt .= "selected =\"selected\"";
      $sel_tt .= ">&nbsp;&nbsp;$tt_label</option>\n";
    }
    $sel_tt .= "</select>";

    $dis_update_delete = "
<!-- Task Type Check Delete Section -->

    <table>
    <tr>
      <td colspan=\"3\">
      <form name=\"form_tt_delete\" method=\"post\"
        action=\"" . url_prepare("admin_ref_index.php") . "\">
      <table>
      <tr>
        <td class=\"adminLabel\">$l_tt_exist</td>
        <td class=\"adminLabel\">$sel_tt</td>
        <td>
          <input type=\"hidden\" name=\"action\" value=\"tasktype_checklink\" />
          <input type=\"submit\" name=\"sub_tt\" value=\"$l_tt_checkdelete\"
            onclick=\"if (check_admin_ref_tt_checkdel(this.form.sel_tasktype_id)) return true;
            else return false;\" />
        </td>
      </tr>
      </table>
      </form>
      </td>
    </tr>

<!-- Task Type Update Section -->

    <tr>
      <td colspan=\"2\" class=\"adminLabel\">
        <form name=\"form_tt_update\" method=\"get\"
          action=\"" . url_prepare("admin_ref_index.php") . "\">
        <input type=\"hidden\" name=\"sel_tasktype_id\" value=\"\" />
        <input type=\"hidden\" name=\"action\" value=\"tasktype_update\" />
        <input type=\"text\" name=\"tf_label\" />
        <input type=\"submit\" name=\"sub_tt\" value=\"$l_tt_update\"
          onclick=\"if (check_admin_ref_tt_upd(this.form,document.form_tt_delete.sel_tasktype_id)) return true; else return false;\" />
        </form>
      </td>
      <td>&nbsp;</td>
    </tr>
    </table>";
    
  } else {
    $dis_update_delete = $l_tt_no;
  }

  // --- HTML Template --------------------------------------------------------

  $block = "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">
        $l_tt_manage
      </td>
    </tr><tr>
      <td>
      $dis_update_delete

      </td>
      <td>

<!-- New Task Type Section -->

        <form name=\"form_tt_new\" method=\"get\"
          action=\"" . url_prepare("admin_ref_index.php") . "\">
        <table>
        <tr>
          <td class=\"adminLabel\">$l_tt_new :
            <input name=\"tf_label\" /></td>
        </tr>
        <tr>
          <td colspan=\"2\">
            <input type=\"radio\" name=\"rd_internal\" value=\"0\" checked=\"checked\" />$l_tt_sales<br />
            <input type=\"radio\" name=\"rd_internal\" value=\"1\" />$l_tt_research<br />
            <input type=\"radio\" name=\"rd_internal\" value=\"2\" />$l_tt_others
          </td>
        </tr>
        <tr>
          <td class=\"adminLabel\">
            <input type=\"hidden\" name=\"action\" value=\"tasktype_insert\" />
            <input type=\"submit\" name=\"sub_tt\" value=\"$l_tt_insert\"
              onclick=\"if (check_admin_ref_tt_new(this.form)) return true; else return false;\" />
          </td>
        </tr>
        </table>
        </form>
      </td>
    </tr>
    </table>";

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
  $id = $ref["tasktype_id"];
  $label = get_admin_ref_tasktype_label($id);

  // Deal Links
  $obm_q = run_query_admin_ref_tasktype_deal_links($ref);
  $nb_deal = $obm_q->num_rows();
  if ($nb_deal > 0) {
    $delete_ok = false;
    $dis_link_deal = "
    <tr>
      <td class=\"detailHead\">$label : $l_tt_link_deal ($nb_deal)</td>
    </tr>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $did = $obm_q->f("deal_id");
      $dlabel = $obm_q->f("deal_label");
      $dis_link_deal .= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;deal_id=$did") . "\">$dlabel</a>
      </td>
    </tr>";
    }
    if ($cpt < $nb_deal) {
      $dis_link_deal .= "
    <tr>
      <td class=\"detailLabel\">...</td>
    </tr>";
    }

  } else {
    $dis_link_deal = "
    <tr>
      <td class=\"detailHead\">$label : $l_tt_link_deal_no</td>
    </tr>";
  }

  // Projects Links
  $obm_q = run_query_admin_ref_tasktype_project_links($ref);
  $nb_proj = $obm_q->num_rows();
  if ($nb_proj > 0) {
    $delete_ok = false;
    $dis_link_proj = "
    <tr>
      <td class=\"detailHead\">$label : $l_tt_link_project ($nb_proj)</td>
    </tr>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $pid = $obm_q->f("project_id");
      $pname = $obm_q->f("project_name");
      $dis_link_proj .= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("$path/project/project_index.php?action=detailconsult&amp;project_id=$pid") . "\">$pname</a>
      </td>
    </tr>";
    }
    if ($cpt < $nb_proj) {
      $dis_link_proj .= "
    <tr>
      <td class=\"detailLabel\">...</td>
    </tr>";
    }

  } else {
    $dis_link_proj = "
    <tr>
      <td class=\"detailHead\">$label : $l_tt_link_project_no</td>
    </tr>";
  }

  // Time Task Links
  $obm_q = run_query_admin_ref_tasktype_timetask_links($ref);
  $nb_time = $obm_q->num_rows();
  if ($nb_time > 0) {
    $delete_ok = false;
    $dis_link_time = "
    <tr>
      <td class=\"detailHead\">$label : $l_tt_link_time ($nb_time)</td>
    </tr>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $tid = $obm_q->f("timetask_user_id");
      $tname = $obm_q->f("lastname") . " " . $obm_q->f("firstname");
      $tdate = date_format($obm_q->f("date"), 1);
      $dis_link_time .= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("$path/time/time_index.php?action=index&amp;sel_user_id=$tid&amp;wbegin=$tdate") . "\">$tname ($tdate)</a>
      </td>
    </tr>";
    }
    if ($cpt < $nb_time) {
      $dis_link_time .= "
    <tr>
      <td class=\"detailLabel\">...</td>
    </tr>";
    }

  } else {
    $dis_link_time = "
    <tr>
      <td class=\"detailHead\">$label : $l_tt_link_time_no</td>
    </tr>";
  }

  $block .= "<table class=\"detail\">
    $dis_link_deal
    $dis_link_proj
    $dis_link_time
    </table>";

  $dis_back = "<form name=\"form_back\" method=\"get\"
          action=\"" .url_prepare("admin_ref_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"tasktype\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>";

  if ($delete_ok == true) {
    $display["msg"] .= display_ok_msg($l_tt_can_delete);
    $dis_del = "<form name=\"form_act_delete\".
          method=\"post\" action=\"" . url_prepare("admin_ref_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"tasktype_delete\" />
        <input type=\"hidden\" name=\"sel_tasktype_id\" value=\"$id\" />
        <input type=\"submit\" name=\"sub_tt\" value=\"$l_tt_delete\" />
        </form>";
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_del</p>
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  } else {
    $display["msg"] .= display_warn_msg($l_tt_cant_delete);
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_back</p>
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

  $tts = get_global_tasktype($ref["tt_target"]);

  if (is_array($tts)) {

    $ext_action = $ref["ext_action"];
    $ext_url = $ref["ext_url"];
    $ext_id = $ref["ext_id"];
    $ext_target = $ref["ext_target"];
    $ext_widget = $ref["ext_widget"];
    $ext_element = $ref["ext_element"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_element=$ext_element&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget";

    if ($ext_element != "") {
      $form_head = "
      <form onsubmit=\"of_extmod_fill_ext_element(this, 'tt'); window.close(); return false;\">";
    } elseif ($ext_widget != "") {
      $form_head = "
      <form onsubmit=\"fill_ext_form(this); return false;\">";
    } else {
      $form_head = "
      <form target=\"$ext_target\" method=\"post\" action=\"$ext_url\">";
    }

    $table_tt = "<table>";
    foreach ($tts as $id=>$one_tt) {
      $tt_label = $one_tt["label"];
      $internal = $one_tt["internal"];
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

  $regions = of_category_get_ordered("", "region");
  $block .= "<hr />";
  $block .= of_category_dis_admin_form("", "region", $regions);

  return $block;
}



?>