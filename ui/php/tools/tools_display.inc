<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : tools_display.inc                                            //
//     - Desc : Tools Display functions File                                 //
// 2002-09-26 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display Update detail for an admin
///////////////////////////////////////////////////////////////////////////////
function dis_tools_update_detail() {
  global $display, $cgp_show, $profiles, $obm, $entities;
  global $l_upd, $l_upd_needed, $l_upd_not_needed, $l_upd_running;
  global $l_upd_run, $l_created, $l_updated, $l_deleted, $l_links;
  global $l_upd_need_domain_update, $l_user, $l_group, $l_host, $l_mailshare;
  global $l_header_tools_upd,$l_js_validate_global_update;
  global $l_validate, $l_cancel, $l_error_permission;

  $domains = array();

  // Determin which domain to display, and which admin realm
  // For domain 0, only 'domain' realm and loop through all domains
  if ($obm['domain_id'] == '0') {
    $admin_realm = array('domain');
    $domains = of_domain_get_list();

    // For others domains, display all realms and only the user's domain
  } else {
    $all_domains = of_domain_get_list();
    $domain_id = $obm['domain_id'];
    $domains[$domain_id] = $all_domains[$domain_id];
    // If user has no update realm, default to domain (backward compatibility)
    if (isset($profiles[$obm['profile']]['properties']['admin_realm'])) {
      $admin_realm = $profiles[$obm['profile']]['properties']['admin_realm'];
    } else {
      $admin_realm = array('domain');
    }
  }

  $update_ok = check_tools_update_context_ok($params);

  // Loop through all domains
  foreach ($domains as $domain_id => $domain) {
    $has_domain_upd = check_tools_update_need_global_update($domain_id);

    // Get all entities prod info (same for all realms)
    foreach ($entities as $entity=>$db_info) {
      $table = $db_info[0];
      $field = $db_info[1];
      $exclude_fields = $db_info[2];
      $dis_entity = ${"l_$entity"};
      if ($cgp_show['module']["$entity"]) {
	$prod[$entity] = get_update_entity_infos('prod', $domain_id, $table, $field, $realm, $exclude_fields);
      }
    }

    // Loop through all udpates 'realms' (user, delegation, domain)
    foreach ($admin_realm as $realm) {
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
	$exclude_fields = $db_info[2];
	$dis_entity = ${"l_$entity"};
	if ($cgp_show['module']["$entity"]) {
	  // Debug perf info
	  //  $da = date('i:s:u');
	  //  $f = exec("ps auxwk-size | grep apache | head -1 | cut -c 25-32", $tmp, $ret);
	  //	  display_debug_msg("<br>$da : mem=$f, guen", $cdg_exe, 'Perf');
	  //  echo "<br>$da : mem=$f, guen ".memory_get_usage().' '.memory_get_usage(true);

	  $nb = get_update_entity_numbers($prod[$entity], $domain_id, $table, $field, $realm, $exclude_fields);

	  // Debug perf info
	  //  $da = date('i:s:u');
	  //  $f = exec("ps auxwk-size | grep apache | head -1 | cut -c 25-32", $tmp, $ret);
	  //  display_debug_msg("<br>$da : mem=$f, guen after", $cdg_exe, 'Perf');
	  //  echo "<br>$da : mem=$f, guen after".memory_get_usage()." ".memory_get_usage(true);
	  $links = get_update_link_entity($domain_id, $entity, $realm);
	  $nb_links = count($links);
	  if (($nb['move'] > 0) || ($nb_links > 0)) {
	    $has_update = true;
	  }
	  $class_cre = ($nb['created'] > 0 ? " class=\"highlight\"" : '');
	  $class_upd = ($nb['updated'] > 0 ? " class=\"highlight\"" : '');
	  $class_del = ($nb['deleted'] > 0 ? " class=\"highlight\"" : '');
	  $class_lnk = ($nb_links > 0 ? " class=\"highlight\"" : '');
	  $dis_row_update .= "
      <tr>
        <td>$dis_entity</td>
        <td$class_cre>$nb[created]</td>
        <td$class_upd>$nb[updated]</td>
        <td$class_del>$nb[deleted]</td>
        <td$class_lnk>$nb_links</td>
      </tr>";
	}
      }

      if (($has_update) || ($has_domain_upd)) {
	set_update_state($domain_id);
  
	$update_type = 'incremental';
	if ($has_domain_upd) {
	  $update_type = 'global';
	}
	// Domain 0 allow only "global" update
	if ($obm['domain_id'] == '0') {
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
        <input type=\"hidden\" name=\"domain_id\" value=\"$domain_id\" />
        <input type=\"hidden\" name=\"realm\" value=\"$realm\" />
        <input type=\"hidden\" name=\"update_type\" value=\"$update_type\" />
        <input type=\"submit\" name=\"submit\" value=\"$l_validate\"/>
        </fieldset>
        </form>";
	
	// button
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
        <th>$l_links</th>
      </tr>
      $dis_row_update
      </table>";

      } else {
	set_update_state($domain_id, 0);
	$dis_button = "$l_upd_not_needed";
      }

      $dis_domain = "
      <div class=\"detail extra\">
      <h1>$domain[label] : $dis_detail_title</h1>
      $dis_realm_board

      <p>
      </div>
      $dis_button";
    }

    $block .= "
    $dis_domain";
  }

  $display['title'] = "<h1 class=\"title\">$l_header_tools_upd</h1>";

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

</script>
