<SCRIPT language="php">
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
      forms.form_dsrc_update.tf_dsrc.value=this.options[this.selectedIndex].text;\" size=\"8\">";
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


