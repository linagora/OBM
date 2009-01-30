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
  global $l_upd_run;
  global $l_upd_need_domain_update;
  global $l_header_tools_upd,$l_js_validate_global_update;
  global $l_validate, $l_cancel, $l_error_permission;

  $domains = array();

  // Determine which domain to display, and which admin realm
  // For domain 0, only 'domain' realm and loop through all domains
  if ($obm['domain_global'] == true) {
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
    $has_domain_upd = check_tools_update_need_global_update($domain_id, $domain['global']);
    // Loop through all udpates 'realms' (user, delegation, domain)
    foreach ($admin_realm as $realm) {
      $has_update = false;
      $dis_realm_board = '';
      $dis_row_update = '';
      $dis_detail_title = $GLOBALS["l_upd_$realm"];
      if ($realm == 'delegation') {
        $dis_detail_title .= ' ('. $obm['delegation'] . ') ';
      }
      if ($realm == 'delegation' && $obm['delegation'] == '' && in_array('domain',$admin_realm)) {
        continue;
      }
      foreach ($entities as $entity=>$db_info) {
	      if ($cgp_show['module']["$entity"]) {
          $nb = run_query_tools_perform_scope($db_info,$realm,$domain_id, $domain['global']);
          if ( $nb['diff'] > 0 ) {
              $has_update = true;
            }
          $dis_row_update.= html_tools_update_entity_table($entity, $nb);
        }
      }
      if (($has_update) || ($has_domain_upd)) {
        set_update_state($domain_id);
  
        $update_type = 'incremental';
        if ($has_domain_upd) {
          $update_type = 'global';
        }
        // Domain 0 allow only "global" update
        if ($obm['domain_global'] === true) {
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
        
        } else {
          $dis_button = $l_upd_running;
        }

        $dis_realm_board = html_tools_javascript($domain_id)
            ."<a id=\"link_domain_$domain_id\">Voir le détail</a>
            <table id=\"table_domain_$domain_id\" class=\"spreadSheet\">
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

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// HTML Entity updates table
///////////////////////////////////////////////////////////////////////////////
function html_tools_update_entity_table($entity, $nb) {
  global $l_user, $l_group, $l_host, $l_mailshare;
  global $l_created, $l_updated, $l_deleted, $l_links;
  
  $block = '';
  $dis_entity = ${"l_$entity"};
  $class_cre = ($nb['created'] > 0 ? " class=\"highlight\"" : '');
  $class_upd = ($nb['updated'] > 0 ? " class=\"highlight\"" : '');
  $class_del = ($nb['deleted'] > 0 ? " class=\"highlight\"" : '');
  $class_lnk = ($nb['links'] > 0 ? " class=\"highlight\"" : '');
  
  $block .= "
    <thead><tr>
      <th colspan=\"4\">$dis_entity</th>
    </tr></thead>
    <tr>
      <td$class_cre>$l_created : <span id=\"count_{$entity}_created\">$nb[created]</span></td>
      <td$class_upd>$l_updated : <span id=\"count_{$entity}_updated\">$nb[updated]</td>
      <td$class_del>$l_deleted : <span id=\"count_{$entity}_deleted\">$nb[deleted]</td>
      <td$class_lnk>$l_links : $nb[links]</td>
    </tr>";
  $block .= '<tr>'
    .html_tools_update_detail_list($entity, 'created', $nb['created_entities'])
    .html_tools_update_detail_list($entity, 'updated', $nb['updated_entities'])
    .html_tools_update_detail_list($entity, 'deleted', $nb['deleted_entities'])
    .'<td></td>'
    .'</tr>';
  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// HTML Updates unordered list
///////////////////////////////////////////////////////////////////////////////
function html_tools_update_detail_list($entity, $state, $entities) {
  $block = '';
  if (!empty($entities)) {
    $block .= '<ul style="display:none;">';
    foreach ($entities as $en) {
      $block .= "<li id=\"item_{$entity}_$en[id]\">$en[repr]&nbsp;".html_tools_delete_button($entity, $state, $en['id'])."</li>";
    }
    $block .= '</ul>';
  }
  return "<td>$block</td>";
}

///////////////////////////////////////////////////////////////////////////////
// HTML Delete button
///////////////////////////////////////////////////////////////////////////////
function html_tools_delete_button($entity, $state, $id) {
  global $resources_path, $l_js_ask_confirm;

  $block = "
    <img id=\"spinner_{$entity}_{$id}\" src=\"$resources_path/themes/default/images/spinner.gif\" style=\"display: none;\" />
    <form method=\"post\" action=\"\" id=\"form_{$entity}_{$id}\" class=\"button-to\">
      <input type=\"image\" value=\"Delete\" src=\"$resources_path/themes/default/images/ico_trash.gif\"
             onclick=\"if (confirm('{$l_js_ask_confirm}')) { postUpdateToDelete('$entity', '$state', '$id'); }; return false;\"/>
    </form>";

//  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Javascript
///////////////////////////////////////////////////////////////////////////////
function html_tools_javascript($domain_id) {
  return "
    <script type=\"text/javascript\">
      window.addEvent('domready', function(){
        $('link_domain_$domain_id').addEvent('click', function(e){
          $('table_domain_$domain_id').getElements('ul').each(function(item, index){
            if (item.getStyle('display') == 'none') {
              item.setStyle('display', 'block');
            } else {
              item.setStyle('display', 'none');
            }
          });
        });
      });
      
      function postUpdateToDelete(entity, state, id) {
        var req = new Ajax('$path/tools/tools_index.php?action=cancel_update', {
          method: 'post',
          data: {entity: entity, state: state, id: id},
          onSuccess: function(){
            $('item_'+entity+'_'+id).remove();
            $('count_'+entity+'_'+state).firstChild.nodeValue--;
          }
        });
        $('form_'+entity+'_'+id).setStyle('display', 'none');
        $('spinner_'+entity+'_'+id).setStyle('display', 'inline');
        req.request();
      }
    </script>";
}

</script>
