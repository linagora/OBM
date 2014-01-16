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
// OBM - File : of_defaultodttemplate.inc                                    //
//     - Desc : OBM Framework Default Odt Templates Handling                 //
// 2006-06-28 Vincent Coulette                                           //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Run Odt Default Templates detail query execution
// Params :
//  - $entity : entity name
// Returns :
//  - Database object
///////////////////////////////////////////////////////////////////////////////
function of_get_defaultodttemplates($entity){
  global $cdg_sql;
  
  $query = "SELECT *, document_path, document_name, document_title
            FROM DefaultOdtTemplate
            JOIN Document ON document_id = DefaultOdtTemplate_document_id
            WHERE defaultodttemplate_entity = '$entity'";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Intégration dans l'OF ??????
// Display the cv administration forms
// Params :
//  - $entity : entity name
//  - $defaults_q : database object defaults templates of the module
// Returns :
//  - HTML code
///////////////////////////////////////////////////////////////////////////////
function of_defaultodttemplates_dis_admin_form($entity, $defaults_q) {
  global $path, $l_select_template;
  global $l_defaultodttemplate_manage, $l_defaultodttemplate_exist;
  global $l_defaultodttemplate_document, $l_defaultodttemplate_label;
  global $l_template_delete, $l_template_update, $l_template_insert,$l_c_new,$ico_ok,$ico_delete;
  
 
  $char_title = '=';
  $key = 0;
  while ($defaults_q->next_record()) {
      $id = $defaults_q->f("defaultodttemplate_id");
      $label = $defaults_q->f("defaultodttemplate_label");
      $document_id = $defaults_q->f("defaultodttemplate_document_id");
      $document_name = $defaults_q->f("document_title");
    
    if ($key%2 == 0) {
      $class = "class=\"pair\"";
    } else {
      $class = "";
    }
    $c_label = htmlentities($one_cat["label"], ENT_COMPAT, 'UTF-8');
    $defaultodttemplate_list .= "
    <tr $class>
    <td>
    <input type=\"hidden\" id=\"defaultodttemplate_$id\" value=\"$id\" />
    <a href=\"?action=defaultodttemplate_delete&amp;category=defaultodttemplate&amp;defaultodttemplate=$id\">
    <img src=\"$ico_delete\" alt=\"$l_template_delete\" title=\"$l_template_delete\" />
    </a></td>
    <td><input type=\"text\" id=\"tf_defaultodttemplate_label_$id\" value=\"$label\" /></td>
    <td>
    <a href=\"#\" onclick=\"return updateLine($id,'defaultodttemplate');\">
    <img src=\"$ico_ok\" alt=\"$l_template_update\" title=\"$l_template_update\" />
    </a>
    </td>
    </tr>";
  }

 
  $url_sel_document = "$path/document/document_index.php?action=ext_get_id_from_path&amp;popup=1&amp;ext_widget=form_defaultodttemplate_new.tf_defaultodttemplate_document_id&amp;";
  $url_sel_document .= "ext_widget_text=form_defaultodttemplate_new.tf_defaultodttemplate_document_name&amp;ext_disp_file=true&amp;ext_title=".urlencode($l_select_template);
  $ext_sel_document = "<a href=\"\" onclick=\"popup('$url_sel_document',''); return false;\">$l_select_template</a>";
 
  
  $block = "
  <div class=\"detail admin\" >
  <form id=\"form_defaultodttemplate_update\" action=\"?action=defaultodttemplate_update\" method=\"post\">
  <input type=\"hidden\" name=\"tf_defaultodttemplate_document_id\" value=\"\" />
  <input type=\"hidden\" name=\"tf_defaultodttemplate_document_name\" value=\"\" />
  <input type=\"hidden\" name=\"tf_defaultodttemplate_label\" value=\"\" />
  <input type=\"hidden\" name=\"defaultodttemplate\" value=\"\" />
  </form>    
  <h1>$l_defaultodttemplate_manage</h1>
  <div class=\"overflow\">
  <table>
  <thead>
  <tr>
  <td colspan=\"6\">$l_defaultodttemplate_exist</td>
  </tr>
  <tr>
  <td colspan=\"6\">$l_defaultodttemplate_label</td>
  </tr> 
  </thead>
  <tbody>
  $defaultodttemplate_list
  </tbody>
  </table>
  </div>    
  
  
  
  <form name=\"form_defaultodttemplate_new\" method=\"post\" action=\"{$entity}_index.php\" >
  <table>
  <thead>
  <tr>
  <td colspan=\"5\">$l_c_new</td>
  </tr>
  </thead>
  <tbody>
  <tr>
  <td>$l_defaultodttemplate_document</td>
  <td>$l_defaultodttemplate_label</td>
  </tr>
  <tr>
  <td><input type=\"text\" name=\"tf_defaultodttemplate_document_name\" /><br />$ext_sel_document </td>
  <td><input type=\"text\" name=\"tf_defaultodttemplate_label\" /></td>
  </tr>
  </tbody>
  <tfoot>
  <tr>
  <td colspan=\"5\">
  <input type=\"submit\"value=\"$l_template_insert\" />
  </td>
  </tr>
  </tfoot>
  </table>
  <input type=\"hidden\" name=\"tf_defaultodttemplate_document_id\" value=\"\" />
  <input type=\"hidden\" name=\"action\" value=\"defaultodttemplate_insert\" />
  </form>
 </div>



  ";
  
  return $block;
}


?>
