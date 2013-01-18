<?php
/******************************************************************************
Copyright (C) 2011-2012 Linagora

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
// OBM - File : right.inc                                                    //
//     - Desc : OBM Rights Objects and functions                             //
// 2005-08-08 Florent Goalabré                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

require_once 'of_acl.php';
OBM_Acl::initialize();

///////////////////////////////////////////////////////////////////////////////
// Display Rights admin panel
// Parameters:
//   - $entity    : entity to administer the right for
//   - $entity_id : entity id
///////////////////////////////////////////////////////////////////////////////
function of_right_dis_admin($entity, $entity_id, $hide_user=false) {
  global $display, $obm, $l_of_right_err_empty;

  if (!isset($entity_id) || ($entity_id == '')) {
    $display['msg'] .= display_err_msg($l_of_right_err_empty);
    return;
  }
  
  $display['features'] = html_entityadmin_bar($entity_id, $entity);
  
  $consumers = OBM_Acl::getEntityConsumers($entity, $entity_id);

  $consumers_to_display = null;
  if ($hide_user) {
      # Remove the current user, as there is no point to let the user change the
      # rights on their own account (in practice, they always have all the rights)
      # Should really be done by changing the SQL query, but this would mean too
      # many changes in of_acl.php, so we do it the naive way instead
      $consumers_to_display = array();
      foreach ($consumers as $consumer) {
          if ($consumer['id'] != $entity_id) {
              array_push($consumers_to_display, $consumer);
          }
      }
  }
  else {
      $consumers_to_display = $consumers;
  }

  $public_rights = OBM_Acl::getPublicRights($entity, $entity_id);

  $html = html_dis_right_admin($consumers_to_display, $public_rights,
      $entity_id, $entity);
  return $html;
}


///////////////////////////////////////////////////////////////////////////////
// User selection for management admin panel
// Parameters:
//   - $writable_user : Array of user on who you can manage the calandar
///////////////////////////////////////////////////////////////////////////////
function html_entityadmin_bar($entity_id, $entity) {
  global $l_entity_right, $l_validate, $path, $module, $obm;

  if ($module != '') {
    $uri = $path.'/'.$module.'/'.$module.'_index.php';
  }
  // Entity Selection
  $dis_sel = "<input type=\"text\" name=\"entity_label\" value=\"\" onfocus=\"\" onblur=\"\" id=\"entity\" autocomplete=\"off\" />";
  $block = "
    <div class=\"portlet\" id=\"rightManagerPortlet\">
    <h1>$l_entity_right</h1>
    <div>
    <form action=\"$uri\" method=\"get\" $check_sel_user>
    <fieldset class=\"detail\">
    $dis_sel
    </fieldset>
    <fieldset class=\"buttons\">
    <input type=\"hidden\" name=\"entity_id\" value=\"$entity_id\" id=\"entity_id\" />
    <input type=\"hidden\" name=\"action\" value=\"rights_admin\" />
    <input type=\"submit\" value=\"$l_validate\" />
    </fieldset>
    </form>
    <script type=\"text/javascript\">
      obm.initialize.chain(function () {
        new obm.AutoComplete.Search('$path/obm.php?action=ext_get_entities&entity=$entity&ajax=1', 'entity_id', 'entity', {mode: 'mono', locked: true});
      });
      </script>       
    </div>  
    </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Rights admin panel
// Parameters:
//   - $consumers : array of all consumers for the entity
//   - $entity_id : entity selected
///////////////////////////////////////////////////////////////////////////////
function html_dis_right_admin(&$consumers, $public_rights, $entity_id, $entity) {
  global $obm;
  global $l_authorize_list,$l_read_permission, $l_write_permission, $l_access_permission;
  global $l_right_validate ,$l_authorize,$l_deny, $ico_delete;
  global $l_public,$l_admin_permission, $module, $l_unselect_all, $l_select_all;
  global $path, $ico_add, $l_users, $l_groups, $l_header_right;
  global $cgp_default_right;

  $uid = $obm['uid'];
  $l_userSearch = phpStringToJsString("$l_users...");
  
  $GLOBALS['js']['vars']['labels']['cell_perms_access'] = $GLOBALS['l_toggle_public_access'];
  $GLOBALS['js']['vars']['labels']['cell_perms_read'] = $GLOBALS['l_toggle_public_read'];
  $GLOBALS['js']['vars']['labels']['cell_perms_write'] = $GLOBALS['l_toggle_public_write'];
  $GLOBALS['js']['vars']['labels']['cell_perms_admin'] = $GLOBALS['l_toggle_public_admin'];

  if ($public_rights['read'] == 1) {
    $disabled_read = 'disabled="true"';
    $public_read_checked = "checked=\"checked\"";
    $style_read = 'visibility: hidden';
  }
  if ($public_rights['write'] == 1) {
    $disabled_write = 'disabled="true"';
    $public_write_checked = "checked=\"checked\"";
    $disabled_read = 'disabled="true"';
    $public_read_checked = "checked=\"checked\"";
    $style_write = 'visibility: hidden';
    $style_read = 'visibility: hidden';
  }
  if ($public_rights['access'] == 1) {
    $disabled_access = 'disabled="true"';
    $public_access_checked = "checked=\"checked\"";
    $style_access = 'visibility: hidden';
  }

  foreach ($consumers as $one_consumer) {
    $id = $one_consumer['id'];
    $consumer = $one_consumer['consumer'];
    $label = $one_consumer['label'];
    $write = $one_consumer['write'];
    $read = $one_consumer['read'];
    $admin = $one_consumer['admin'];
    $access = $one_consumer['access'];
    
    if ($write == 1) {
      $checked_write = 'checked="checked"';
    } else {
      $checked_write = '';
    }
    if ($read == 1) {
      $checked_read = 'checked="checked"';
    } else {
      $checked_read = '';
    }
    if ($admin == 1) {
      $checked_admin = 'checked="checked"';
    } else {
      $checked_admin = '';
    }
    if ($access == 1) {
      $checked_access = 'checked="checked"';
    } else {
      $checked_access = '';
    }
    
    $sel_id = 'data-'.$consumer.'-'.$id;
    $tr_id = 'sel_accept_'.$consumer.'-'.$sel_id;

    if (($consumer=='user') && ($id==$uid))
      $disabled_admin='disabled="disabled"';
    else
      $disabled_admin='';
    ${'dis_'.$consumer} .= "
      <tr id=\"$tr_id\">
        <td>
          <a href=\"javascript: remove_element('$tr_id','perms');\"><img src=\"$ico_delete\"></a>
          $label
        </td>
        <td>
          <a href=\"#\" onclick=\"select_all_perms('$tr_id')\">$l_select_all</a>/<a href=\"#\" onclick=\"unselect_all_perms('$tr_id')\">$l_unselect_all</a>
        </td>
        <td class=\"cell_perms_access\">
          <input style=\"$style_access\" type=\"checkbox\" name=\"cb_accept_access[]\" value=\"$sel_id\" $checked_access $disabled_access/>
        </td>
        <td class=\"cell_perms_read\">
          <input style=\"$style_read\" type=\"checkbox\" name=\"cb_accept_read[]\" onclick=\"apply_checked_access(this, '$tr_id')\" onchange=\"apply_checked_access(this, '$tr_id')\" value=\"$sel_id\" $checked_read $disabled_read/>
        </td>
        <td class=\"cell_perms_write\">
          <input style=\"$style_write\" type=\"checkbox\" name=\"cb_accept_write[]\" onclick=\"apply_checked_read(this, '$tr_id')\" onchange=\"apply_checked_read(this, '$tr_id')\" value=\"$sel_id\" $checked_write $disabled_write/>
        </td>
        <td>
          <input type=\"checkbox\" name=\"cb_accept_admin[]\" onclick=\"apply_checked_admin(this, '$tr_id')\" value=\"$sel_id\" $checked_admin $disabled_admin />
        </td>
      </tr>
";
  }
  $publicClass = 'H';
  if ($cgp_default_right[$entity]['public']['read'] != -1 || ($read == 1 && $id == 0)){ 
	if($cgp_default_right[$entity]['public']['read'] == -2) {
		$block_consumer_read_public = "
		  <span id=\"cb_read_public\">
		  <input onclick=\"if(togglePrivacy(this,'cell_perms_read')) apply_checked_access(this, 'public_perms') ;\"
		  onchange=\"apply_checked_access(this, 'public_perms');\" 
		  name=\"cb_read_public\" value=\"1\" type=\"checkbox\" $public_read_checked disabled=\"disabled\" />
		  </span>
		  <input type=\"hidden\" name=\"cb_read_public\" value=\"1\" /> ";
		$publicClass = '';
	} else {
		$block_consumer_read_public = "
		  <span id=\"cb_read_public\">
		  <input onclick=\"if(togglePrivacy(this,'cell_perms_read')) apply_checked_access(this, 'public_perms');\"		
		  onchange=\"apply_checked_access(this, 'public_perms');\" 
		  name=\"cb_read_public\" value=\"1\" type=\"checkbox\" $public_read_checked />
		  </span>";
		$publicClass = '';
	}
  } else {
    $block_consumer_read_public = "&nbsp;";
  }
  if ($cgp_default_right[$entity]['public']['access'] != -1 || ($access == 1 && $id == 0)) { 
	if($cgp_default_right[$entity]['public']['access'] == -2) {
		$block_consumer_access_public = "
		  <span id=\"cb_access_public\">
		  <input onclick=\"togglePrivacy(this,'cell_perms_access');\"
		  name=\"cb_access_public\" value=\"1\" type=\"checkbox\" $public_access_checked disabled=\"disabled\" />
		  </span>
		  <input type=\"hidden\" name=\"cb_access_public\" value=\"1\" />";
		$publicClass = '';
	} else {
		$block_consumer_access_public = "
		  <span id=\"cb_access_public\">
		  <input onclick=\"togglePrivacy(this,'cell_perms_access');\"
		  name=\"cb_access_public\" value=\"1\" type=\"checkbox\" $public_access_checked />
		  </span>";
		$publicClass = '';
	}
  } else {
    $block_consumer_access_public = "&nbsp;";
  }
  if ($cgp_default_right[$entity]['public']['write'] != -1 || ($write == 1 && $id == 0)){ 
	if($cgp_default_right[$entity]['public']['write'] == -2) {
		$block_consumer_write_public = "
		  <span id=\"cb_write_public\">
		  <input onclick=\"if(togglePrivacy(this,'cell_perms_write')) apply_checked_read(this, 'public_perms');\"
		  name=\"cb_write_public\" value=\"1\" type=\"checkbox\" $public_write_checked disabled=\"disabled\" />
		  </span>
		  <input type=\"hidden\" name=\"cb_write_public\" value=\"1\" />";
		$publicClass = '';
	} else {
		$block_consumer_write_public = "
		  <span id=\"cb_write_public\">
		  <input onclick=\"if(togglePrivacy(this,'cell_perms_write')) apply_checked_read(this, 'public_perms');\"
		  name=\"cb_write_public\" value=\"1\" type=\"checkbox\" $public_write_checked />
		  </span>";
		$publicClass = '';
	}
  } else {
    $block_consumer_write_public = "&nbsp;";
  }
  $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_accept_user&amp;ext_container=table";
  $popup_user_add_button = "
    <a id=\"lnk_accept_read\" href=\"#\" onclick=\"return popup('$url');
  return false;\">
    <img src=\"$ico_add\" alt=\"[Add]\" />
    </a>";
    
  $url = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_accept_group&amp;ext_container=table&amp;filter=$GLOBALS[c_public]&amp;sel_privacy=".$GLOBALS['c_public'];
  $popup_group_add_button = "
    <a id=\"lnk_accept_admin\" href=\"#\" onclick=\"return popup('$url');\">
    <img src=\"$ico_add\" alt=\"[Add]\" />
    </a>";
    
  $block_public_perms = "
  <tbody class='$publicClass'>
    <tr id=\"public_perms\" class=\"pair\">
    <th>$l_public</th>
    <td>&nbsp;</td>
    <td>$block_consumer_access_public</td>
    <td>$block_consumer_read_public</td>
    <td>$block_consumer_write_public</td>
    <td>&nbsp;</td>
    </tr>
</tbody>
";
  	
  $block_users = "
  <tbody id='sel_user'>
    <tr>
      <th>$l_users</th>
      <th colspan=\"5\">
        <input type=\"text\" id=\"userSearch\" autocomplete=\"off\" />
        $popup_user_add_button
      </th>
    </tr>
  </tbody>
  <tbody id='sel_accept_user'>
    $dis_user
  </tbody>
";
  
  $block_groups = "
  <tbody id='sel_group'>
    <tr>
      <th>$l_groups</th>
      <th colspan=\"5\">
        <input type=\"text\" id=\"groupSearch\" autocomplete=\"off\" />
        $popup_group_add_button
      </th>
    </tr>
  </tbody>
  <tbody id='sel_accept_group'>
    $dis_group
  </tbody>";

  $block = "
  <script type=\"text/javascript\">
    
    function togglePrivacy(formElement, dataElements, force) {
      var elements = \$$('td.'+dataElements);
      if(formElement.checked) {
        if(obm.vars.labels[dataElements] && !force) {
          if(!confirm(obm.vars.labels[dataElements])) {
            formElement.checked = false;
            return false;
          }
        }
        for (var i=0; i<elements.length; i++) {
          var cb_field = elements[i].getElement('input');
          cb_field.setProperty('disabled', true);
          cb_field.setStyle('visibility', 'hidden');
        }
      } else {
        for (var i=0; i<elements.length; i++) {
          var cb_field = elements[i].getElement('input');
          cb_field.setProperty('disabled', false);
          cb_field.setStyle('visibility', 'visible');
        }
      }
      return true;
    }
    
    function select_all_perms(tr_id) {
      var tr_element = $(tr_id);
      
      var td_elements = tr_element.getChildren();
      for (var i=0; i<td_elements.length; i++) {
          var td_element = td_elements[i];
          var cb_element = td_element.getElement('input');
          if (cb_element !== null && cb_element.getProperty('type') == 'checkbox') {
            cb_element.setProperty('checked', true);
          }
      }
    }
    
    function unselect_all_perms(tr_id) {
      var tr_element = $(tr_id);
        
      var td_elements = tr_element.getChildren();
      for (var i=0; i<td_elements.length; i++) {
        var td_element = td_elements[i];
        var cb_element = td_element.getElement('input');
        if (cb_element !== null && cb_element.getProperty('type') == 'checkbox') {
          cb_element.setProperty('checked', false);
        }
      }
    }

    function apply_checked_admin(formElement, tr_id) {
      if (formElement.checked) {
        select_all_perms(tr_id)
      }
    }
    
    function apply_checked_read(formElement, tr_id) {
    
      if (tr_id != 'public_perms') {
        var td_elements = $(tr_id).getChildren();
        
        if (formElement.checked) {
          for (var i=0; i<td_elements.length; i++) {
            var td_element = td_elements[i];
            if (td_element.hasClass('cell_perms_read')) {
              var cb_read = td_element.getElement('input');
              if (cb_read !== false && cb_read.getProperty('type') == 'checkbox') {
                cb_read.checked = true;
		apply_checked_access(formElement, tr_id) ;
              }
            }
          }
        }
      } else if (formElement.checked) {
        var cb_public_read = $('cb_read_public').getElement('input');
        if (cb_public_read !== false && cb_public_read.getProperty('type') == 'checkbox') {
          cb_public_read.checked = true;
	  apply_checked_access(formElement, tr_id) ;
          togglePrivacy(cb_public_read,'cell_perms_read',true);
        }
      }
    }

    function apply_checked_access(formElement, tr_id) {
    
      if (tr_id != 'public_perms') {
        var td_elements = $(tr_id).getChildren();
        
        if (formElement.checked) {
          for (var i=0; i<td_elements.length; i++) {
            var td_element = td_elements[i];
            if (td_element.hasClass('cell_perms_access')) {
              var cb_access = td_element.getElement('input');
              if (cb_access !== false && cb_access.getProperty('type') == 'checkbox') {
                cb_access.checked = true;
              }
            }
          }
        }
      } else if (formElement.checked) {
        var cb_public_access = $('cb_access_public').getElement('input');
        if (cb_public_access !== false && cb_public_access.getProperty('type') == 'checkbox') {
          cb_public_access.checked = true;
          togglePrivacy(cb_public_access,'cell_perms_access',true);
        }
      }
    }
    
    function apply_backgroud() {
      var tbody_blocks = new Array($('sel_accept_user'), $('sel_accept_group'));
      
      for (var i=0; i < tbody_blocks.length; i++) {
        var tbody = tbody_blocks[i];
        var tr_elements = (tbody ? tbody.getChildren() : new Array());
        for (var j=0; j < tr_elements.length; j++) {
          if ( (j % 2) == 0) {
            tr_elements[j].addClass('pair');
          }
        }
      }
    }
  </script>

  <br />
  <form method=\"post\" name=\"f_mod_right\" action=\"$path/$module/${module}_index.php\">
    <table class=\"spreadSheet\">
      <colgroup class=\"highlight\"></colgroup>
      <thead>
        <tr>
          <th colspan=\"2\">$l_header_right</th>
          <th>$l_access_permission</th>
          <th>$l_read_permission</th>
          <th>$l_write_permission</th>
          <th>$l_admin_permission</th>
        </tr>
      </thead>
      $block_public_perms
      $block_users
      $block_groups
    </table>
    <fieldset class=\"buttons\">
      <input type=\"hidden\" name=\"entity_id\" value=\"$entity_id\" />
      <input type=\"hidden\" name=\"action\" value=\"rights_update\" />
      <input type=\"submit\" value=\"$l_right_validate\" />
    </fieldset>
  </form>

  <script type=\"text/javascript\">

    apply_backgroud();

    var resultValue = function (element, extension) {
      var accept_read = this.name[0];
      var accept_write = this.name[1];
      var accept_access = this.name[2];
      var accept_admin = this.name[3];
      var item_id = element.getProperty('id');
      var id = item_id.substr(('item_').length,item_id.length);
      var entity = this.selectedBox.id;
      var tr_id = entity +'-'+ id;
      var text = $(item_id+'_label').innerHTML;
      if (!$(tr_id)) {
        element.addClass(\"selected\");
        var result = new Element('tr');
        result.setProperties({'id': tr_id});
        result.injectInside(this.selectedBox);
        if (this.selectedBox.getChildren().length % 2 != 0) {
          result.addClass('pair');
        }
        new Element('td').adopt(
          new Element('a').setProperty('href', '#').adopt(
            new Element('img').setProperty('src', obm.vars.images.del)
          ).addEvent('mousedown',
            function() {
              var item = \$(item_id);
              if (item) { item.removeClass(\"selected\"); }
              remove_element(tr_id, entity);
            }.bind(this)
          )
        ).appendText(' ' + text).injectInside(result);

        var cb_read = $('cb_read_public');
        var accept_read_style = 'visible';
        var accept_read_disabled = false;
        if (cb_read!==null && cb_read.getElement('input').checked) {
          accept_read_disabled = true;
          accept_read_style = 'hidden';
        }

        var cb_write = $('cb_write_public');
        var accept_write_style = 'visible';
        var accept_write_disabled = false;
        if (cb_write!==null && cb_write.getElement('input').checked) {
          accept_write_disabled = true;
          accept_write_style = 'hidden';
        }

        var cb_access = $('cb_access_public');
        var accept_access_style = 'visible';
        var accept_access_disabled = false;
        if (cb_access!==null && cb_access.getElement('input').checked) {
          accept_access_disabled = true;
          accept_access_style = 'hidden';
        }

        new Element('td').adopt(
          new Element('a').setProperty('href', '#')
                          .setProperty('onclick', 'select_all_perms(\''+tr_id+'\')').appendText('$l_select_all')          
        ).appendText('/').adopt(
          new Element('a').setProperty('href', '#')
                          .setProperty('onclick', 'unselect_all_perms(\''+tr_id+'\')').appendText('$l_unselect_all')
        ).injectInside(result);

        new Element('td').addClass('cell_perms_access').adopt(
          new Element('input').setProperty('type', 'checkbox')
                              .setProperty('name', accept_access+'[]')
                              .setProperty('value', id)
                              .setProperty('disabled', accept_access_disabled)
        ).setStyle('visibility', accept_access_style).injectInside(result);

        new Element('td').addClass('cell_perms_read').adopt(
          new Element('input').setProperty('type', 'checkbox')
                              .setProperty('name', accept_read+'[]')
                              .setProperty('value', id)
                              .setProperty('disabled', accept_read_disabled)
			      .setProperty('onclick', 'apply_checked_access(this, \''+tr_id+'\')')
                              .setProperty('onchange', 'apply_checked_access(this, \''+tr_id+'\')')
        ).setStyle('visibility', accept_read_style).injectInside(result);

        new Element('td').addClass('cell_perms_write').adopt(
          new Element('input').setProperty('type', 'checkbox')
                              .setProperty('name', accept_write+'[]')
                              .setProperty('value', id)
                              .setProperty('disabled', accept_write_disabled)
                              .setProperty('onclick', 'apply_checked_read(this, \''+tr_id+'\')')
                              .setProperty('onchange', 'apply_checked_read(this, \''+tr_id+'\')')
        ).setStyle('visibility', accept_write_style).injectInside(result);

        new Element('td').adopt(
          new Element('input').setProperty('type', 'checkbox')
                              .setProperty('name', accept_admin+'[]')
                              .setProperty('onclick', 'apply_checked_admin(this, \''+tr_id+'\')')
                              .setProperty('value', id)
        ).injectInside(result);

      } else {
        this.inputField.blur();
        this.resetFunc();
        this.inputField.focus();
      }
      eval(this.options.selectfunction);
    }

    var sel_perms = new Array('cb_accept_read', 'cb_accept_write', 'cb_accept_access', 'cb_accept_admin');
    obm.initialize.chain(function () {
      new obm.AutoComplete.Search('$path/user/user_index.php?action=ext_search&ajax=1', 'sel_accept_user', 'userSearch', {name: sel_perms, resultValue: resultValue});
      new obm.AutoComplete.Search('$path/group/group_index.php?action=ext_search&ajax=1', 'sel_accept_group', 'groupSearch', {name: sel_perms, resultValue: resultValue});
    });
  </script>
";

  return $block;
}



///////////////////////////////////////////////////////////////////////////////
// Get the list of entities for the consumer given with the corresponding right
// Parameters:
//   - $entityu     : target entity (table name with capital)
//   - $user_id : user id
//   - $right       : right to get and select from : "read", "write" or "admin"
//   - $ids_set     : array of ids restricting the result
//   - $real_entity : real data entity (eg: userobm for entity calendar)
// Returns:
//   array ("ids"=>(id1, id2, id3), "entity"=>((id1,label1), (id2,label2)))
///////////////////////////////////////////////////////////////////////////////
function searchWritablesEntities($entityu, $user_id, $pattern = false, $limit = false) {
  global $cdg_sql, $obm;

  // GLOBAL - superman has all rights on entities

   
  $db = new DB_OBM;
  $ent_array = array();
  $id_array = array();

  $e_extra = "''";
  $entity = strtolower($entityu);
  //$query =  "SELECT ".OBM_Acl::getEntityColumns($entity)." FROM (".OBM_Acl::getAclSubselect('', $entityType, $entityId = null, $userId = null, $action = null).")" as AllowedEntity WHERE id IN (".OBM_Acl::getAclSubselect('', $entityType, $entityId = null, $userId = null, $action = null).")"
  //while ($db->next_record()) {
  //  $id = $db->f('id');
  //  if (is_numeric($id)) {
  //    $entities[$id] = $db->f('label');
  //  }
  //}
  //return OBM_Acl::getAllowedEntities($user_id, $entity, 'admin');
  if ($entity=='addressbook') {
    $table_entity = '(SELECT * FROM AddressBook LEFT JOIN UserObm ON userobm_id = owner)';
    $e_id = "e.id";
    $e_label = "#CONCAT(e.name,' [',userobm_lastname,' ',userobm_firstname,']')";
    $multidomain = ' AND domain_id = '.$GLOBALS['obm']['domain_id'];
  } elseif (OBM_Acl::isSpecialEntity($entityu)) {
    $table_entity = 'UserObm';
    $e_id = 'e.userobm_id';
    $e_label = "#CONCAT(userobm_lastname, ' ', userobm_firstname)";
    $e_extra = 'userobm_email';
    $u = get_user_info($user_id);
  } else {
    switch($entity) {
    case 'resource':
      $table_entity = 'Resource';
      break;
    case 'mailshare':
      $table_entity = 'MailShare';
      break;
    default :
      $table_entity = ucfirst($entityu);
    }
    $e_id = "e.${entity}_id";
    $e_label = "e.${entity}_name";
  }
  if(!$multidomain) $multidomain = sql_multidomain(strtolower($table_entity));
  if($limit) $sqllimit = 'LIMIT 24 OFFSET 0' ;
  if($pattern !== false) $where .= "AND $e_label #LIKE '%$pattern%'";

  if (!(Perm::get_module_rights($entity) & $GLOBALS['cright_write_admin']) == $GLOBALS['cright_write_admin']) {
    $where_filter_entity = " AND $e_id IN (".OBM_Acl::getAclSubselect($entity.'entity_'.$entity.'_id', $entity, null, $obm['uid'], 'admin').")";
  }    


  $query = "SELECT $e_id AS id, $e_label AS label, $e_extra as extra, '$entity' as entityright_entity FROM $table_entity AS e WHERE 1=1 $where $where_filter_entity $multidomain $sqllimit";
  // END - superman has all rights on entities

  display_debug_msg($query, $cdg_sql, 'of_right_entity_for_user()');
  $db->xquery($query);
  $ret_array = array();

  while($db->next_record()) {
    $id = $db->f('id');
    if ( ($ids_set == '') || ( (is_array($ids_set)) && (in_array($id, $ids_set)) ) ) {
      $label = $db->f('label');
      $extra = $db->f('extra');
      $id_array[] = $id;
      $ret_array[$id] = array('label' => $label, 'extra' => $extra);
    }
  }
  
  return $ret_array;
}

?>
