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
  global $l_upd, $l_upd_needed, $l_upd_not_needed, $l_upd_running;
  global $l_upd_run, $l_upd_cancel;
  global $l_validate, $l_cancel;

  // Si une mise a jour est autorisee (non deja en cours)
  if (check_tools_update_context_ok($params)) {
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
    $dis_update = "<td class=\"detailText\">$l_upd_running</td>";
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
  global $l_upd, $l_upd_needed, $l_upd_not_needed, $l_upd_running;
  global $l_upd_run, $l_upd_cancel, $l_created, $l_updated, $l_deleted;
  global $l_upd_need_domain_update, $l_user, $l_group, $l_host, $l_mailshare;
  global $l_header_tools_upd;
  global $l_validate, $l_cancel, $l_error_permission;

  // If user has no update realm ! XXXX default to domain ??
  if (! isset($profiles[$obm['profile']]['properties']['admin_realm'])) {
    $display["msg"] = display_warn_msg("$l_error_permission");
    return $block;
  }

  $update_ok = check_tools_update_context_ok($params);

  $has_domain_upd = check_tools_update_domain_updated($obm['domain_id']);

  $entities = array('user' => array('UserObm', 'userobm'),
		    'group' => array('UGroup', 'group'),
		    'host' => array('Host', 'host'),
		    'mailshare' => array('MailShare', 'mailshare'));

  // Loop through all udpates 'realms' (user, delegation, domain)
  foreach ($profiles[$obm['profile']]['properties']['admin_realm'] as $realm) {

    $has_update = false;
    $dis_realm_board = '';
    $dis_row_update = '';
    $detail_title = "l_upd_$realm";
    global $$detail_title;
    $dis_detail_title = $$detail_title;
    if ($realm == 'delegation') {
      $dis_detail_title .= ' ('. $obm['delegation'] . ') ';
    }

    foreach ($entities as $entity=>$db_info) {
      $table = $db_info[0];
      $field = $db_info[1];
      $dis_entity = ${"l_$entity"};
      if ($cgp_show['module']["$entity"]) {
	$nb = get_update_entity_numbers($table, $field, $realm);
	if ($nb['move'] > 0) {
	  $has_update = true;
	}
	$class_cre = ($nb['created'] > 0 ? " class=\"highlight\"" : '');
	$class_upd = ($nb['updated'] > 0 ? " class=\"highlight\"" : '');
	$class_del = ($nb['deleted'] > 0 ? " class=\"highlight\"" : '');
	$dis_row_update .= "
      <tr>
        <td>$dis_entity</td>
        <td$class_cre>$nb[created]</td>
        <td$class_upd>$nb[updated]</td>
        <td$class_del>$nb[deleted]</td>
      </tr>";
      }
    }

    if (($has_update) || ($has_domain_upd)) {
      set_update_state();
  
      $update_type = 'incremental';
      if ($has_domain_upd) {
	$update_type = 'global';
      }

      // If table Domain has been updated allow only for domain update
      if (($realm != 'domain') && ($has_domain_upd)) {
	$dis_button = $l_upd_need_domain_update;

      } else if ($update_ok) {
	$dis_button = "
        <form method=\"post\" action=\"tools_index.php\">
        <fieldset class=\"buttons\">
        <input type=\"hidden\" name=\"action\" value=\"update_update\" />
        <input type=\"hidden\" name=\"realm\" value=\"$realm\" />
        <input type=\"hidden\" name=\"update_type\" value=\"$update_type\" />
        <input type=\"submit\" name=\"submit\" value=\"$l_validate\" />
        </fieldset>
        </form>";
	
	//button
      } else {
	$dis_button = $l_upd_running;
      }

      $dis_realm_board = "<table class=\"spreadSheet\">
      <thead>
      <tr>
        <th>&nbsp;</th>
        <th>$l_created</th>
        <th>$l_updated</th>
        <th>$l_deleted</th>
      </tr>
      $dis_row_update
      </table>";

    } else {
      set_update_state(0);
      $dis_button = "$l_upd_not_needed";
    }

    $dis_realm .= "
    <div class=\"detail extra\">
    <h1>$dis_detail_title</h1>
    $dis_realm_board

    <p>
    </div>
    $dis_button";
  }

  $display["title"] = "<h1 class=\"title\">$l_header_tools_upd</h1>";

  $block = "
    $dis_realm";

  return $block;
}


/**
 * Display Update validation
 *
 * @param array parameters hash
 * @return XHTML display
 */
function dis_tools_update_validation($params) {
  global $display, $cgp_show, $profiles, $obm;
  global $l_upd, $l_upd_not_needed;
  global $l_created, $l_updated, $l_deleted;
  global $l_user, $l_group, $l_host, $l_mailshare;
  global $l_header_tools_upd, $l_error_permission;

  $realm = $params['realm'];
  $uid = $obm['uid'];
  $domain_id = $obm['domain_id'];
  $delegation = $obm['delegation'];

  $entities = array('user' => array('UserObm', 'userobm'),
		    'group' => array('UGroup', 'group'),
		    'host' => array('Host', 'host'),
		    'mailshare' => array('MailShare', 'mailshare'));

  $has_update = false;
  $dis_row_update = '';
  $detail_title = "l_upd_$realm";
  global $$detail_title;
  $dis_detail_title = $$detail_title;
  if ($realm == 'delegation') {
    $dis_detail_title .= ' ('. $obm['delegation'] . ') ';
  }

  foreach ($entities as $entity=>$db_info) {
    $table = $db_info[0];
    $field = $db_info[1];
    $dis_entity = ${"l_$entity"};
    if ($cgp_show['module']["$entity"]) {
      $nb = get_update_entity_numbers($table, $field, $realm);
      if ($nb['move'] > 0) {
	$has_update = true;
      }
      $class_cre = ($nb['created'] > 0 ? " class=\"highlight\"" : '');
      $class_upd = ($nb['updated'] > 0 ? " class=\"highlight\"" : '');
      $class_del = ($nb['deleted'] > 0 ? " class=\"highlight\"" : '');
      $dis_row_update .= "
      <tr>
        <td>$dis_entity</td>
        <td$class_cre>$nb[created]</td>
        <td$class_upd>$nb[updated]</td>
        <td$class_del>$nb[deleted]</td>
      </tr>";

      $ret = validate_update_entity($entity, $table, $field, $realm);
    }
  }

  if ($has_update) {
    
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
";
  }

  $display['title'] = "<h1 class=\"title\">$l_header_tools_upd</h1>";

  $block = "
    $dis_realm";

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
