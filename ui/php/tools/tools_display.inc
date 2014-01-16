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
  global $l_upd_need_domain_update, $l_progress;
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
        $dis_detail_title .= ' ('. $obm['delegation_target'] . ') ';
      }
      if ($realm == 'delegation' && $obm['delegation_target'] == '' && in_array('domain',$admin_realm)) {
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

        $dis_realm_board = html_tools_javascript($domain_id, $realm)
            ."<a href='' onclick='return false;' id=\"link_domain_".$realm."_$domain_id\">$GLOBALS[l_show_details]<img src='$GLOBALS[ico_add]' alt='$GLOBALS[l_show_details]' /></a>
            <br /><br />
            <table id=\"table_domain_".$realm."_$domain_id\" class=\"spreadSheet\">
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
      $block .= "$dis_domain";
    }

  }
  $running = check_tools_update_running();
  if(!$running) {
    $steps = run_query_tools_get_steps($GLOBALS['params']['update_type'],$GLOBALS['params']['domain_id']);
    $progress = run_query_tools_get_progress();
    $progress = 0;
    if($steps > 0) $percent = round(($progress/$steps) * 100);
    else $percent = 100;
    $div_state = "
      <div class=\"detail extra\"> 
      <h1>$l_progress</h1>
      <div id='scopeProgressContainer'>
      <div id='scopeProgressBar' style='width:$percent%'>&nbsp;</div>
      <span id='scopeProgressPercent'>$percent%</span>
      </div>
      </div>
      <script type='text/javascript'>
        obm.initialize.chain(function () {
          var steps = $steps;
          var request = new Request.JSON({
            url : 'tools_index.php',
            secure : false,
            onComplete : function(response) {
              var progress = response.steps;
              var completed = response.completed;
              if(steps == 0 || completed == 1) {
                window.location.href = 'tools_index.php';
              }
              var percent = Math.round((progress / steps) * 100);

              if(percent > 100) percent = 100;
              $('scopeProgressPercent').set('html',percent + '%');
              $('scopeProgressBar').setStyle('width', percent + '%');
            }
          });
          request.post.periodical(3000, request, {action:'progress'});
        });
      </script>
    ";
  }

  return $div_state.$block;
}

/**
 * json_tools_update_progress 
 * 
 * @param mixed $domain_id 
 * @param mixed $realm 
 * @access public
 * @return void
 */
function json_tools_update_progress ($domain_id, $realm) {
  global $entities;

  $complete = intval(check_tools_update_running());
  $steps = run_query_tools_get_progress();

  $GLOBALS['display']['json'] = "{steps:$steps, completed: $complete}" ;
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
    <tbody>
    <tr>
      <td$class_cre>$l_created : <span id=\"count_{$entity}_created\">$nb[created]</span></td>
      <td$class_upd>$l_updated : <span id=\"count_{$entity}_updated\">$nb[updated]</td>
      <td$class_del>$l_deleted : <span id=\"count_{$entity}_deleted\">$nb[deleted]</td>
      <td$class_lnk>$l_links : $nb[links]</td>
    </tr>
    </tbody>
      ";
  $block .= html_tools_update_detail_list($entity, $nb);
  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// HTML Updates unordered list
///////////////////////////////////////////////////////////////////////////////
function html_tools_update_detail_list($entity, $entities) {
  $block = '';
  $cpt = count($entities['created_entities']) | count($entities['updated_entities']) | count($entities['deleted_entities']) ;
  if($cpt > 0) {
    for($i=0; ($i < $cpt && $i < 100); $i++) {
      if($i%2 == 0) {
        $klass = 'pair';
      } else {
        $klass = '';
      }
      $created = (isset($entities['created_entities'][$i]))?$entities['created_entities'][$i]['repr'].' '.html_tools_delete_button($entity, 'created', $entities['created_entities'][$i]['id']):'';
      $updated = (isset($entities['updated_entities'][$i]))?$entities['updated_entities'][$i]['repr'].' '.html_tools_delete_button($entity, 'updated', $entities['updated_entities'][$i]['id']):'';
      $deleted = (isset($entities['deleted_entities'][$i]))?$entities['deleted_entities'][$i]['repr'].' '.html_tools_delete_button($entity, 'deleted', $entities['deleted_entities'][$i]['id']):'';
      $block .= "<tr class='$klass'><td>$created</td><td>$updated</td><td>$deleted</td><td></td><tr>";
    }
    if($i < $cpt) {
      $block .= "<tr class='$klass'><td>...</td><td>...</td><td>...</td><td></td><tr>";
    }
    $block = '
      <tbody style="display:none;" class="detail">
      <tr><th colspan="4">'.$GLOBALS['l_details'].'Details</th></tr>
      '.$block.'
      </tbody>';
  }
  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// HTML Delete button
///////////////////////////////////////////////////////////////////////////////
function html_tools_delete_button($entity, $state, $id) {
  global $resources_path, $l_js_ask_confirm, $ico_undo, $ico_spinner;

  //$block = "
  //  <img id=\"spinner_{$entity}_{$id}\" src=\"$ico_spinner\" style=\"display: none;\" />
  //    <img src=\"$ico_undo\"
  //    onclick=\"if (confirm('".phpStringToJsString($GLOBALS['l_js_'.$state.'_'.$entity.'_confirm'])."')) { postUpdateToDelete('$entity', '$state', '$id'); }; return false;\"/>
  //  ";

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Javascript
///////////////////////////////////////////////////////////////////////////////
function html_tools_javascript($domain_id, $realm) {
  return "
    <script type=\"text/javascript\">
      obm.initialize.chain(function () {
        $('link_domain_".$realm."_$domain_id').addEvent('click', function(e){
          $('table_domain_".$realm."_$domain_id').getElements('tbody.detail').each(function(item, index){
            if (item.getStyle('display') == 'none') {
              item.setStyle('display', '');
            } else {
              item.setStyle('display', 'none');
            }
          });
        });
      });
      
      function postUpdateToDelete(entity, state, id) {
        new Request.JSON({
          url : 'tools_index.php',
          secure : false,
          onComplete : function(response) {
            if(response.error != 0) {
              showErrorMessage(response.message);
            } else {
              showOkMessage(response.message);
              $('item_'+entity+'_'+id).dispose();
              $('count_'+entity+'_'+state).firstChild.nodeValue--;
            }
          }
        }).post({entity: entity, state: state, id: id, action: 'cancel_update'});        

        $('form_'+entity+'_'+id).setStyle('display', 'none');
        $('spinner_'+entity+'_'+id).setStyle('display', 'inline');
        req.request();
      }
    </script>";
}

