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
