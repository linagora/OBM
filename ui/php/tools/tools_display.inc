<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : tools_display.inc                                            //
//     - Desc : Tools Display functions File                                 //
// 2002-09-26 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// HTML Display Update tool
///////////////////////////////////////////////////////////////////////////////
function html_tools_update_index() {
  global $l_upd, $l_upd_needed, $l_upd_not_needed, $l_upd_not_allowed;
  global $l_upd_run, $l_upd_cancel;
  global $l_validate, $l_cancel;

  // Si une mise a jour est autorisee (non deja en cours)
  if (check_tools_update_context()) {
    $update_state = get_update_state();

    if (! $update_state) {
      $dis_update = "<td class=\"detailLabel\">$l_upd_not_needed</td>";
    } else {
      $dis_update = "<td class=\"detailLabel\">$l_upd_needed</td>";
      $dis_form = "
    <table>
    <tr>
      <form method=\"post\" action=\"tools_index.php\">
      <input type=\"hidden\" name=\"action\" value=\"update_update\">
      <td class=\"detailText\"><input type=\"hidden\" name=\"update\" value=\"1\">$l_upd_run</td>
      <td class=\"detailForm\"><input type=\"submit\" name=\"submit\" value=\"$l_validate\"></td>
    </form>
    </tr>
    <tr><td>&nbsp;</td></tr>
    <tr><td>&nbsp;</td></tr>
    <tr>
      <form method=\"post\" action=\"tools_index.php\">
      <input type=\"hidden\" name=\"action\" value=\"update_cancel\">
      <td class=\"detailText\"><input type=\"hidden\" name=\"update\" value=\"1\">$l_upd_cancel</td>
      <td class=\"detailForm\"><input type=\"submit\" name=\"submit\" value=\"$l_cancel\"></td>
    </tr>
    </table>
    </form>";
    }
  } else {
    $dis_update = "<td class=\"detailText\">$l_upd_not_allowed</td>";
  }


  $block = "
    <div class=\"detail extra\">
    <h1>$l_upd</h1>
    <table class=\"detail\">
    <tr>
      $dis_update
    </tr>
    </table>
    <p>
    </div>
    $dis_form
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Update detail for an admin
///////////////////////////////////////////////////////////////////////////////
function dis_tools_update_detail() {
  global $display, $cgp_show, $profiles, $obm;
  global $l_upd, $l_upd_needed, $l_upd_not_needed, $l_upd_not_allowed;
  global $l_upd_run, $l_upd_cancel, $l_created, $l_updated, $l_deleted;
  global $l_user, $l_group, $l_host, $l_mailshare;
  global $l_header_tools_upd;
  global $l_header_tools_upd_admin, $l_header_tools_upd_domain;
  global $l_validate, $l_cancel, $l_error_permission;

  $update_state = get_update_state();

  // No update, display this and return
  if (! $update_state) {
    $block = "
    <div class=\"detail extra\">
    <h1>$l_upd</h1>
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_upd_not_needed</td>
    </tr>
    </table>
    <p>
    </div>";

    return $block;
  }

  if (! isset($profiles[$obm['profile']]['conf']['admin_realm'])) {
    $display["msg"] = display_warn_msg("$l_error_permission");
    return $block;
  }

  $entities = array("user" => array("UserObm", "userobm"),
		    "group" => array("UGroup", "group"),
		    "host" => array("Host", "host"),
		    "mailshare" => array("MailShare", "mailshare"));

  // Loop through all udpates "realms" (own updates, target updates, domain)
  foreach ($profiles[$obm['profile']]['conf']['admin_realm'] as $realm) {
    if ($realm == 'target') {
      $target = $profiles[$obm['profile']]['conf']['admin_target'];
    }

    $has_update = false;
    $dis_row_update = "";
    $detail_title = "l_upd_$realm";
    global $$detail_title;
    $dis_detail_title = $$detail_title;

    foreach ($entities as $entity=>$db_info) {
      $table = $db_info[0];
      $field = $db_info[1];
      $dis_entity = ${"l_$entity"};
      if ($cgp_show["module"]["$entity"]) {
	$nb = get_update_numbers($table, $field, $realm, $target);
	if ($nb['move'] > 0) {
	  $has_update = true;
	}
	$class_cre = ($nb['created'] > 0 ? " class=\"highlight\"" : "");
	$class_upd = ($nb['updated'] > 0 ? " class=\"highlight\"" : "");
	$class_del = ($nb['deleted'] > 0 ? " class=\"highlight\"" : "");
	$dis_row_update .= "
      <tr>
        <td>$dis_entity</td>
        <td$class_cre>$nb[created]</td>
        <td$class_upd>$nb[updated]</td>
        <td$class_del>$nb[deleted]</td>
      </tr>";
      }

    }

    if ($has_update) {
  
      // Si une mise a jour est autorisee (non deja en cours)
      if (check_tools_update_context()) {
	$dis_button = "
      <form method=\"post\" action=\"tools/tools_index.php\">
      <fieldset class=\"buttons\">
      <input type=\"hidden\" name=\"action\" value=\"update_update\" />
      <input type=\"submit\" value=\"$l_validate\" />
      </fieldset>
      </form>";
	
	//button
      } else {
	$dis_button = "<td class=\"detailText\">$l_upd_not_allowed</td>";
      }
    } else {
      $dis_button = "<td class=\"detailText\">$l_upd_not_needed</td>";
    }

    $dis_realm .= "
    <div class=\"detail extra\">
    <h1>$dis_detail_title</h1>

    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th>&nbsp;</th>
      <th>$l_created</th>
      <th>$l_updated</th>
      <th>$l_deleted</th>
    </tr>
    $dis_row_update
    </table>

    <p>
    </div>
    $dis_button
";
  }

  $display["title"] = "<h1 class=\"title\">$l_header_tools_upd</h1>";

  $block = "
    $dis_realm
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Halt tool
///////////////////////////////////////////////////////////////////////////////
function html_tools_halt_index() {
  global $l_halt, $l_halt_aliamin, $l_validate;

  $block = "
    <form method=\"post\" action=\"tools_index.php\">
      <input type=\"hidden\" name=\"action\" value=\"halt_halt\">

    <div class=\"detailHead\">$l_halt</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_halt_aliamin</td>
      <td class=\"detailForm\"><input type=\"submit\" value=\"$l_validate\"></td>
    </tr>
    </table>
    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Remote access tool
///////////////////////////////////////////////////////////////////////////////
function html_tools_remote_index() {
  global $l_remote_access, $l_remote_allowed, $l_remote_not_allowed;
  global $l_remote_allow, $l_remote_not_allow, $l_validate;

  $remote_access = get_tools_remote_access();
  if (! $remote_access) {
    $dis_remote = "$l_remote_not_allowed";
    $remote_access = "1";
    $remote_label = $l_remote_allow;
  } else {
    $dis_remote = "$l_remote_allowed";
    $remote_access = "0";
    $remote_label = $l_remote_not_allow;    
  }

  $block = "
    <div class=\"detailHead\">$l_remote_access</div>

    <table class=\"detail\">
    <tr>
    <td class=\"detailLabel\">
      $dis_remote
    </tr>
    </table>
    <p />

    <form method=\"post\" action=\"tools_index.php\">
      <input type=\"hidden\" name=\"action\" value=\"remote_update\" />
      <table class=\"detail\">
      <tr>
        <td class=\"detailLabel\"><input type=\"hidden\" name=\"remote_access\" value=\"$remote_access\" />$remote_label : </td>
        <td class=\"detailForm\"><input type=\"submit\" name=\"submit\" value=\"$l_validate\" /></td>
      </tr>
      </table>
    </form>
";

  return $block;
}
</script>
