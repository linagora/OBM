<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_ref_display.inc                                        //
//     - Desc : Referential data display file                                //
// 2003-12-05 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display the index screen
///////////////////////////////////////////////////////////////////////////////
function dis_ref_index() {

  $block = dis_datasource_index();

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Data Source index screen
///////////////////////////////////////////////////////////////////////////////
function dis_datasource_index() {

  $dsrc_q = run_query_datasource();
  $block = html_datasource_form($dsrc_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Data Source section                                              //
// Parameters:
//   - $dsrc_q : Data Source list database object
///////////////////////////////////////////////////////////////////////////////
function html_datasource_form($dsrc_q) {
  global $l_dsrc_manage, $l_dsrc_exist, $l_dsrc_no;
  global $l_dsrc_checkdelete, $l_dsrc_update, $l_dsrc_new, $l_dsrc_insert;

  if ($dsrc_q->num_rows() > 0) {
    $sel_dsrc = "<select name=\"sel_dsrc\" onChange=\"
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
            onclick=\"if (check_dsrc_checkdel(this.form.sel_dsrc)) return true;
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
        <input type=\"hidden\" name=\"sel_dsrc\" value=\"\" />
        <input type=\"hidden\" name=\"action\" value=\"datasource_update\" />
        <input type=\"text\" name=\"tf_name\" />
        <input type=\"submit\" name=\"sub_dsrc\" value=\"$l_dsrc_update\"
          onclick=\"if (check_dsrc_upd(this.form,document.form_dsrc_delete.sel_dsrc)) return true; else return false;\" />
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
              onclick=\"if (check_dsrc_new(this.form)) return true; else return false;\" />
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
// Display: Data Source links                                                //
// Parameters:
//   - $ref : datasource hash info : keys used : id, name
///////////////////////////////////////////////////////////////////////////////
function dis_datasource_links($ref) {
  global $display, $l_back, $l_dsrc_link_company, $l_dsrc_link_company_no;
  global $l_dsrc_link_contact, $l_dsrc_link_contact_no;
  global $l_dsrc_delete, $l_dsrc_can_delete, $l_dsrc_cant_delete;

  $delete_ok = true;
  $id = $ref["datasource"];
  $name = get_datasource_name($id);

  // Company Links
  $obm_q = run_query_datasource_company_links($id);
  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $dis_link_comp = "
    <tr>
      <td class=\"detailHead\">$name : $l_dsrc_link_company</td>
    </tr>";

    while ($obm_q->next_record()) {
      $cid = $obm_q->f("company_id");
      $cname = $obm_q->f("company_name");
      $dis_link_comp .= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("company_index.php?action=detailconsult&amp;param_company=$cid") . "\">$cname</a>
      </td>
    </tr>";
    }

  } else {
    $dis_link_comp = "
    <tr>
      <td class=\"detailHead\">$l_dsrc_link_company_no</td>
    </tr>";
  }

  // Contacts Links
  $obm_q = run_query_datasource_contact_links($id);
  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $dis_link_cont = "
    <tr>
      <td class=\"detailHead\">$name : $l_dsrc_link_contact</td>
    </tr>";

    while ($obm_q->next_record()) {
      $cid = $obm_q->f("contact_id");
      $cname = $obm_q->f("contact_lastname")." ".$obm_q->f("contact_firstname");
      $dis_link_cont .= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("contact_index.php?action=detailconsult&amp;param_contact=$cid") . "\">$cname</a>
      </td>
    </tr>";
    }

  } else {
    $dis_link_cont = "
    <tr>
      <td class=\"detailHead\">$l_dsrc_link_contact_no</td>
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
    $dis_del = "<form name=\"form_act_delete\".
          method=\"post\" action=\"" . url_prepare("admin_ref_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"datasource_delete\" />
        <input type=\"hidden\" name=\"sel_dsrc\" value=\"$id\" />
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
// Display the Country index screen
///////////////////////////////////////////////////////////////////////////////
function dis_country_index() {

  $ctry_q = run_query_country();
  $block = html_country_form($ctry_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Country section
// Parameters:
//   - $ctry_q : Country list database object
///////////////////////////////////////////////////////////////////////////////
function html_country_form($ctry_q) {
  global $l_ctry_manage, $l_ctry_exist, $l_ctry_no;
  global $l_ctry_checkdelete, $l_ctry_update, $l_ctry_new, $l_ctry_insert;
  global $l_name, $l_lang, $l_iso_3166, $l_phone;

  if ($ctry_q->num_rows() > 0) {

    // Country select construction
    $sel_ctry = "<select name=\"sel_ctry\" onChange=\"
      forms.form_ctry_update.tf_name.value=this.options[this.selectedIndex].text;\" size=\"20\">";
    while ($ctry_q->next_record()) {
      $id = $ctry_q->f("country_id");
      $iso = $ctry_q->f("country_iso3166");
      $lang = $ctry_q->f("country_lang");
      $name = $ctry_q->f("country_name");
      $phone = $ctry_q->f("country_phone");
      $sel_ctry .= "\n<option value=\"$id\">$iso-$lang-$name ($phone)</option>";
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
            onclick=\"if (check_ctry_checkdel(this.form.sel_ctry)) return true;
            else return false;\" />
        </td>
      </tr>
      </table>
      </form>
      </td>
    </tr>

<!-- Country Update Section -->

    <tr>
      <td colspan=\"2\" class=\"adminLabel\">
        <form name=\"form_ctry_update\" method=\"get\"
          action=\"" . url_prepare("admin_ref_index.php") . "\">
        <input type=\"hidden\" name=\"sel_ctry\" value=\"\" />
        <input type=\"hidden\" name=\"action\" value=\"country_update\" />

        <table>
        <tr>
          <td class=\"adminLabel\">$l_name : </td>
          <td><input type=\"text\" name=\"tf_name\" /></td>
          <td rowspan=\"3\">
          <input type=\"submit\" name=\"sub_ctry\" value=\"$l_ctry_update\"
            onclick=\"if (check_ctry_upd(this.form,document.form_ctry_delete.sel_ctry)) return true; else return false;\" />
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
            <input type=\"text\" name=\"tf_phone\" size=\"3\" maxlength=\"3\" />
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
      <td colspan=\"5\" class=\"adminHead\">
        $l_ctry_manage
      </td>
    </tr><tr>
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
        </tr><tr>
          <td class=\"adminLabel\">
            <input type=\"hidden\" name=\"action\" value=\"country_insert\" />
            <input type=\"submit\" name=\"sub_ctry\" value=\"$l_ctry_insert\"
              onclick=\"if (check_ctry_new(this.form)) return true; else return false;\" />
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
// Display: Country links                                                //
// Parameters:
//   - $ref : country hash info : keys used : id, name
///////////////////////////////////////////////////////////////////////////////
function dis_country_links($ref) {
  global $display, $l_back, $l_ctry_link_company, $l_ctry_link_company_no;
  global $l_ctry_link_contact, $l_ctry_link_contact_no;
  global $l_ctry_delete, $l_ctry_can_delete, $l_ctry_cant_delete;

  $delete_ok = true;
  $id = $ref["country"];
  $name = get_country_name($id);

  // Company Links
  $obm_q = run_query_country_company_links($id);
  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $dis_link_comp = "
    <tr>
      <td class=\"detailHead\">$name : $l_ctry_link_company</td>
    </tr>";

    while ($obm_q->next_record()) {
      $cid = $obm_q->f("company_id");
      $cname = $obm_q->f("company_name");
      $dis_link_comp .= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("company_index.php?action=detailconsult&amp;param_company=$cid") . "\">$cname</a>
      </td>
    </tr>";
    }

  } else {
    $dis_link_comp = "
    <tr>
      <td class=\"detailHead\">$l_ctry_link_company_no</td>
    </tr>";
  }

  // Contacts Links
  $obm_q = run_query_country_contact_links($id);
  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $dis_link_cont = "
    <tr>
      <td class=\"detailHead\">$name : $l_ctry_link_contact</td>
    </tr>";

    while ($obm_q->next_record()) {
      $cid = $obm_q->f("contact_id");
      $cname = $obm_q->f("contact_lastname")." ".$obm_q->f("contact_firstname");
      $dis_link_cont .= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("contact_index.php?action=detailconsult&amp;param_contact=$cid") . "\">$cname</a>
      </td>
    </tr>";
    }

  } else {
    $dis_link_cont = "
    <tr>
      <td class=\"detailHead\">$l_ctry_link_contact_no</td>
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
    $dis_del = "<form name=\"form_act_delete\".
          method=\"post\" action=\"" . url_prepare("admin_ref_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"country_delete\" />
        <input type=\"hidden\" name=\"sel_ctry\" value=\"$id\" />
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
