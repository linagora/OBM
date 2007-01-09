<?php
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
  global $path, $popup_height, $popup_width, $l_select_template;
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
    $c_label = htmlentities($one_cat["label"]);
    $defaultodttemplate_list .= "
    <tr $class>
    <td>
    <input type=\"hidden\" id=\"tf_defaultodttemplate_$id\" value=\"$id\" />
    <a href=\"?action=defaultodttemplate_delete&amp;category=defaultodttemplate&amp;defaultodttemplate=$id\">
    <img src=\"$ico_delete\" alt=\"$l_template_delete\" title=\"$l_template_delete\" />
    </a></td>
    <td><input type=\"text\" id=\"tf_defaultodttemplate_label_$id\" value=\"$label\" /></td>
    <td><input type=\"hidden\" name=\"tf_defaultodttemplate_document_id_$id\" value=\"$document_id\" /></td>
    <td><input type=\"hidden\" name=\"tf_defaultodttemplate_document_name_$id\" value=\"$document_name\" /></td>
    <td>
    <a href=\"#\" onclick=\"return updateLine($id,'defaultodttemplate');\">
    <img src=\"$ico_ok\" alt=\"$l_template_update\" title=\"$l_template_update\" />
    </a>
    </td>
    </tr>";
  }



 
  $url_sel_document = "$path/document/document_index.php?action=ext_get_id_from_path&amp;popup=1&amp;ext_widget=form_defaultodttemplate_new.tf_defaultodttemplate_document_id&amp;";
  $url_sel_document .= "ext_widget_text=form_defaultodttemplate_new.tf_defaultodttemplate_document_name&amp;ext_disp_file=true&amp;ext_title=".urlencode($l_select_template);
  $ext_sel_document = "<a href=\"\" onclick=\"window.open('$url_sel_document','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">$l_select_template</a>";
 

  $showDetails_js = "
  
  <script type=\"text/javascript\">

  ///////////////////////////////////////////////////////////////////////////////
  // Show the selected template details in the update form fields
  ///////////////////////////////////////////////////////////////////////////////
  function showDetails(){
    var sel = document.getElementById('sel_defaultodttemplate_id');
    var id = sel.options[sel.selectedIndex].value;
    var label = sel.options[sel.selectedIndex].innerHTML;
    var documentId = document.getElementById('document_id_'+id).value;
    var documentName = document.getElementById('document_name_'+id).value;
    void(document.getElementById('tf_defaultodttemplate_label').value = label);
    void(document.getElementById('tf_defaultodttemplate_id').value = id);
    void(document.getElementById('defaultodttemplate_document').innerHTML = '<a href=$path/document/document_index.php?action=detailconsult&amp;document_id='+documentId+'>'+documentName+'</a>');
    return;
  }

  </script>";
  
  $block = "
  <div class=\"detail admin\" >
  <form id=\"form_defaultodttemplate_update\" action=\"?action=defaultodttemplate_update\" method=\"post\">
  <input type=\"hidden\" name=\"tf_defaultodttemplate_label\" value=\"\" />
  <input type=\"hidden\" name=\"defaultodttemplate\" value=\"\" />
  </form>    
  <h1>$l_defaultodttemplate_manage</h1>
  <div class=\"overflow\">
  <table>
  <thead>
  <tr>
  <td>&nbsp;</td>
  <td>$l_defaultodttemplate_label</td>
  <td>&nbsp;</td>        
  </tr> 
  <tr>
  <td>&nbsp;</td>
  <td>$l_defaultodttemplate_exist</td>
  <td>&nbsp;</td>        
  </tr> 
  </thead>
  <tbody>
  $defaultodttemplate_list
  </tbody>
  </table>
  </div>    
  
  
  
  
  <form id=\"form_defaultodttemplate_new\" action=\"\" method=\"post\"
  onsubmit=\"return check_category_new(this.tf_${cat}_label,'$jslabel')\">
  <table>
  <thead>
  <tr>
  <td colspan=\"3\">$l_c_new</td>
  </tr>
  <tbody>
  <tr>
  <td>$l_defaultodttemplate_document</td>
  </tr><tr>
  <td><input type=\"text\" name=\"tf_defaultodttemplate_document_name\" /></td></tr>
  <tr><td>$ext_sel_document</td></tr>
  <tr><td>$l_defaultodttemplate_label</td></tr>       
  <tr>
  <td><input type=\"text\" name=\"tf_defaultodttemplate_label\" value=\"\" /></td>    
  </tr>  
  </tbody>
  <tfoot>
  <tr>
  <td colspan=\"3\"><input type=\"submit\" value=\"$l_template_insert\" /></td>
  </tr>
  </tfoot>
  </table>
  <input type=\"hidden\" name=\"action\" value=\"defaultodttemplate_insert\" />
  <input type=\"hidden\" name=\"category\" value=\"defaultodttemplate\" />
  </form>
  $showDetails_js
  </div>



















<!--
  
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">
        $l_defaultodttemplate_manage
      </td>
    </tr><tr>
      <td>

<!-- Default Odt Template Delete Section -->

        <table>
        <tr>
          <td colspan=\"3\">
            <form name=\"form_defaultodttemplate_delete\" method=\"post\"  action=\"{$entity}_index.php\" >
            <table>
            <tr>
              <td class=\"adminLabel\">$l_defaultodttemplate_exist</td>
              <td class=\"adminLabel\">$sel_defaultodttemplate</td>
              <td>
              <input type=\"hidden\" name=\"action\" value=\"defaultodttemplate_delete\" />
              $hidden
              <input type=\"submit\" value=\"$l_template_delete\" />
              </td>
            </tr>
            </table>
            </form>
          </td>
        </tr>

<!-- Default Odt Template Update Section -->
        <tr>
    <form name=\"form_defaultodttemplate_update\" method=\"post\" action=\"{$entity}_index.php\" 
          <td class=\"adminLabel\">$l_defaultodttemplate_document :</td>
            <td><span id=\"defaultodttemplate_document\"></td>
        </tr><tr>
        <tr>
          <td class=\"adminLabel\">$l_defaultodttemplate_label :</td>
            <td><input type=\"text\" id=\"tf_defaultodttemplate_label\" name=\"tf_defaultodttemplate_label\" value=\"\" /></td>
        </tr><tr> 
        <tr>
          <td colspan=\"2\" class=\"adminLabel\">
            <input type=\"hidden\" id=\"tf_defaultodttemplate_id\" name=\"tf_defaultodttemplate_id\" value=\"\" />
            <input type=\"hidden\" name=\"action\" value=\"defaultodttemplate_update\" />
            <input type=\"submit\" value=\"$l_template_update\" />
            </form>
          </td>
          <td>&nbsp;</td>
        </tr>
        </table>
      </td>
      <td>

<!-- New Default Odt Template Section -->

        <form name=\"form_defaultodttemplate_new\" method=\"post\" action=\"{$entity}_index.php\" >
        <table>
        <tr>
          <td class=\"adminLabel\">$l_defaultodttemplate_document :</td>
            <td><input type=\"text\" name=\"tf_defaultodttemplate_document_name\" /> $ext_sel_document </td>
        </tr><tr>
        <tr>
          <td class=\"adminLabel\">$l_defaultodttemplate_label :</td>
            <td> <input type=\"text\" name=\"tf_defaultodttemplate_label\" /></td>
        </tr><tr> 
          <td class=\"adminLabel\" colspan=\"2\">
            <input type=\"hidden\" name=\"tf_defaultodttemplate_document_id\" value=\"\" />
            <input type=\"hidden\" name=\"action\" value=\"defaultodttemplate_insert\" />
            <input type=\"submit\"value=\"$l_template_insert\" />
          </td>
        </tr>
        </table>
        </form>
      </td>
    </tr>
    </table>
    $showDetails_js-->
  ";
  
  return $block;
}


?>
