<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : deal_display.php                                             //
//     - Desc : Deal Display File                                            //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display Deal index page                                                   //
// Parameters:
//   - $deal[] : hash with deal and parent deal values
///////////////////////////////////////////////////////////////////////////////
function dis_deal_index($deal="") {
  $usr_q = run_query_userobm();
  html_parentdeal_search_form($deal, $usr_q);
  html_deal_search_form($deal, run_query_dealtype(), run_query_deal_tasktype(), run_query_dealstatus(), $usr_q);
}


///////////////////////////////////////////////////////////////////////////////
// Display Deal search form                                                  //
// Parameters : 
//   - $deal[]        : default form values
//     keys used      : parent, label, company_name, archive, kind, cat, state,
//                      manager, dateafter, datebefore
//   - $obm_q_type    : DB result object (deal kind list)
//   - $obm_q_cat     : DB result object (deal categories list)
//   - $obm_q_state   : DB result object (deal status list)
//   - $obm_q_manager : DB result object (userobm list)
///////////////////////////////////////////////////////////////////////////////
function html_deal_search_form($deal, $obm_q_type, $obm_q_cat, $obm_q_state, $obm_q_manager) {
  global $l_deal, $l_label_start, $l_company, $l_kind, $l_category, $l_state;
  global $l_after, $l_before, $l_manager, $l_show_archive;
  global $l_all, $l_all_f, $l_find; 
  global $col_textem, $sess, $c_all;

  // --- Var preparation ------------------------------------------------------

  $parent = $deal["parent"];
  $label = stripslashes($deal["label"]);
  $name = stripslashes($deal["company_name"]);
  $archive = ($deal["archive"] == "1" ? "checked" : "");
  $kind = $deal["kind"];
  $cat = $deal["cat"];
  $state = $deal["state"];
  $manager = $deal["manager"];
  $dateafter = stripslashes($deal["dateafter"]);
  $datebefore = stripslashes($deal["datebefore"]);
  $company_id = $deal["company"];

  // Kind select construction
  $sel_kind = "<SELECT name=sel_kind>
     <OPTION value=\"$c_all\">$l_all";
  while($obm_q_type->next_record()) {
    $id = $obm_q_type->f("dealtype_id");
    $klabel = $obm_q_type->f("dealtype_label");
    $sel_kind .= "<OPTION value=\"$id\"";
    if ($kind == $id) $sel_kind .= " selected";
    $sel_kind .= ">$klabel";
  }
  $sel_kind .= "</SELECT>";


  //TaskType select construction
  $sel_cat = "<SELECT name=sel_cat>
              <OPTION value=\"$c_all\">$l_all";
  while($obm_q_cat->next_record()) {
    //put menu titles
    $id = $obm_q_cat->f("tasktype_id");
    $tt_label = $obm_q_cat->f("tasktype_label");
    $sel_cat .= "\n<OPTION value=\"$id\"";
    if ($cat == $id) $sel_cat .= " selected";
    $sel_cat .= ">$tt_label</OPTION>\n";
  }
  $sel_cat .= "</SELECT>";

  // State select construction
  $sel_state = "<SELECT name=sel_state>
     <OPTION value=\"$c_all\">$l_all";
  while($obm_q_state->next_record()) {
    $id = $obm_q_state->f("dealstatus_id");
    $slabel = $obm_q_state->f("dealstatus_label");
    $sel_state .= "<OPTION value=\"$id\"";
    if ($state == $id) $sel_state .= " selected";
    $sel_state .= ">$slabel";
  }
  $sel_state .= "</SELECT>";

  // Manager select construction
  if ($obm_q_manager->num_rows() > 0) {
    $obm_q_manager->seek(0);
  }
  $sel_manager = "<select name=sel_manager>
     <option value=\"$c_all\">$l_all";
  while($obm_q_manager->next_record()) {
    $id = $obm_q_manager->f("userobm_id");
    $lname = $obm_q_manager->f("userobm_lastname");
    $sel_manager .= "<option value=\"$id\"";
    if ($manager == $id) $sel_manager .= " selected";
    $sel_manager .= ">$lname";
  }
  $sel_manager .= "</select>";

  // --- HTML Template --------------------------------------------------------

  echo "<center>
    <form method=get name=form_deal
    onSubmit=\"if (check_search_form(this) == false) return false; else return true;\"
    action=\"" . $sess->url("deal_index.php") . "\">

    <table border=0>
    <tr>
      <td align=left><font color=\"#$col_textem\">$l_deal</font></td>
      <td>&nbsp;&nbsp;&nbsp;</td>
<!-- Label -->
      <td><font size=-2>$l_label_start</font><br>
      <input name=tf_label size=16 maxlength=128 value=\"$label\"></td>
      <td>&nbsp;&nbsp;</td>
<!-- Name -->
      <td><font size=-2>$l_company</font><br>
      <input name=tf_company_name size=12 maxlength=16 value=\"$name\"></td>
      <td>&nbsp;&nbsp;</td>
<!-- Archive -->
      <td align=center><font size=-2>$l_show_archive</font><br>
      <input type=checkbox name=cb_archive value=1 $archive>
      </td>
      <td>&nbsp;&nbsp;</td>
<!-- Alarm date after -->
      <td><font size=-2>$l_after</font><br>
      <input name=tf_dateafter size=10 maxlength=10 value=\"$dateafter\"></td>
<!-- Alarm date before -->
      <td><font size=-2>$l_before</font><br>
      <input name=tf_datebefore size=10 maxlength=10 value=\"$datebefore\"></td>
      <td>&nbsp;&nbsp;</td>
<!-- Search -->
      <td valign=bottom> 
      <input name=action type=hidden value=search>
      <input name=param_parent type=hidden value=\"$parent\">
      <input name=param_company type=hidden value=\"$company_id\">
      <input name=submit type=submit value=\"$l_find\">
      </td>
    </tr>
    </table>

    <table border=0>
    <tr>
<!-- Kind -->
      <td><font size=-2>&nbsp;&nbsp;$l_kind</font><br>
      $sel_kind
      </td>
<!-- Categories -->
      <td><font size=-2>&nbsp;&nbsp;$l_category</font><br>
      $sel_cat
      </td>
<!-- State -->
      <td><font size=-2>&nbsp;&nbsp;$l_state</font><br>
      $sel_state
      </td>
<!-- Manager -->
      <td><font size=-2>&nbsp;&nbsp;$l_manager</font><br>
      $sel_manager
      </td>
    </tr>
    </table>
  </form>
  <hr>";
}


///////////////////////////////////////////////////////////////////////////////
// Display Parent Deal search form                                           //
// Parameters : 
//   - $deal[]         : default form values
//     keys used       plabel, parchive, pmanager
//   - $obm_q_pmanager : DB result object (userobm list)
///////////////////////////////////////////////////////////////////////////////
function html_parentdeal_search_form($deal, $obm_q_pmanager) {
  global $l_parentdeal, $l_label_start,$l_all,$l_all_f,$l_manager;
  global $l_find,$l_show_archive; 
  global $col_textem, $sess;

  // --- Var preparation ------------------------------------------------------

  $plabel = stripslashes($deal["plabel"]);
  $parchive = ($deal["parchive"] == "1" ? "checked" : "");
  $pmanager = $deal["pmanager"];

  // Manager select construction
  if ($obm_q_pmanager->num_rows() > 0) {
    $obm_q_pmanager->seek(0);
  }
  $sel_pmanager = "<select name=sel_pmanager>
     <option value=\"$c_all\">$l_all";
  while($obm_q_pmanager->next_record()) {
    $id = $obm_q_pmanager->f("userobm_id");
    $lname = $obm_q_pmanager->f("userobm_lastname");
    $sel_pmanager .= "<option value=\"$id\"";
    if ($pmanager == $id) $sel_pmanager .= " selected";
    $sel_pmanager .= ">$lname";
  }
  $sel_pmanager .= "</select>";

  // --- HTML Template --------------------------------------------------------

  echo "<CENTER><FORM METHOD=GET NAME=\"form_search_parentdeal\"
    ACTION=\"" . $sess->url("deal_index.php") . "\">
    <TABLE BORDER=0>
    <TR>
      <TD align=left><font color=\"#$col_textem\">$l_parentdeal</font></TD>
      <TD>&nbsp;&nbsp;&nbsp;</TD>
<!-- Label -->
      <TD><FONT SIZE=-2>$l_label_start</FONT><BR>
      <INPUT TYPE=text NAME=tf_plabel size=20 value=\"$plabel\"></TD>
      <TD>&nbsp;&nbsp;</TD>
<!-- Manager -->
      <TD><FONT SIZE=-2>&nbsp;&nbsp;$l_manager</FONT><BR>
      $sel_pmanager
      </TD>
      <TD>&nbsp;&nbsp;</TD>
<!-- Archive -->
      <TD ALIGN=center><FONT SIZE=-2>$l_show_archive</FONT><BR>
      <INPUT TYPE=checkbox NAME=cb_parchive VALUE=\"1\" $parchive>
      </TD>
      <TD>&nbsp;&nbsp;</TD>
<!-- Search -->
      <TD VALIGN=bottom> 
      <INPUT NAME=action TYPE=hidden VALUE=parent_search>
      <INPUT NAME=submit TYPE=submit VALUE=\"$l_find\">
      </TD>
    </TR>
    </TABLE>
  </FORM>
  <HR>";
}



///////////////////////////////////////////////////////////////////////////////
// Display the Deal search result                                            //
// Parameters:
//   - $deal[]    : deal search criteria
//     keys used  : company_id, name, company
///////////////////////////////////////////////////////////////////////////////
function dis_deal_search_list($deal) {
  global $l_no_found;
  global $auth, $new_order, $order_dir;

  $obm_q = run_query_search($deal, 0, $new_order, $order_dir);
  $nb_deal = $obm_q->num_rows();
  if ($nb_deal == 0) {
    display_warn_msg($l_no_found);
  } else {
    $pref_q = run_query_display_pref($auth->auth["uid"], "deal", 0);
    html_deal_search_list($obm_q, $pref_q, $nb_deal, $deal);
  }
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Deal Search result                                       //
// Parameters:
//   - $obm_q_deals : list of deals
//   - $pref_q      : fields to display in the list of deals 
//   - $nb_deal     : nb deals found
//   - $deal[]      : deal search criteria
//     keys used    : parent, label, company_name, archive, kind, cat, state,
//                    manager, dateafter, datebefore
///////////////////////////////////////////////////////////////////////////////
function html_deal_search_list($obm_q_deals, $pref_q, $nb_deal, $deal) {
  //-- Themes
  global $col_tableh,$col_textem,$col_table,$col_error;
  //-- Labels
  global $l_archive_first,$l_label,$l_company,$l_kind,$l_category,$l_state;
  global $l_date_alarm, $l_archive_update, $l_found;
  global $perms_user,$l_perms_user;
  global $auth, $sess;

  $parent = $deal["parent"];
  $label = stripslashes($deal["label"]);
  $name = stripslashes($deal["company_name"]);
  $archive = $deal["archive"];
  $kind = $deal["kind"];
  $cat = $deal["cat"];
  $state = $deal["state"];
  $manager = $deal["manager"];
  $dateafter = stripslashes($deal["dateafter"]);
  $datebefore = stripslashes($deal["datebefore"]);

  $obm_q_deals->seek($first_row);
  
  $dis_deals_list=new OBM_DISPLAY("DATA", $pref_q);
  $dis_deals_list->data_set = $obm_q_deals;
  $dis_deals_list->data_url = $url;
  $dis_deals_list->data_header = "both";

  // --- HTML Template --------------------------------------------------------

  echo "<FONT color=\"#$col_textem\">$nb_deal $l_found</FONT><BR>";

  $url = $sess->url("deal_index.php?action=search&amp;tf_label=$label&amp;tf_company_name=$name&amp;cb_archive=$archive&amp;sel_kind=$kind&amp;sel_state=$state&amp;sel_cat=$cat&amp;sel_manager=$manager&amp;tf_dateafter=$dateafter&amp;tf_datebefore=$datebefore&amp;param_parent=$parent");
  $dis_deals_list->display();

}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Contact Consult                                              //
// Parameters :
//   -  $deal_q    : DBO : information about the deal
//   -  $con_q     : DBO : list of contact from the company 
//   -  $comp_id   : id of the company concerned by the deal
///////////////////////////////////////////////////////////////////////////////
function html_deal_consult($deal_q,$con_q,$comp_id, $q_invoices, $invoices_options) {
  // -- Themes
  global $set_theme,$col_label,$col_table,$col_textem,$col_ok,$ico_company,$ico_contact, $col_link,$ico_contact_list;
  // - Labels
  global $l_update,$l_delete,$l_insert;
  global $l_label,$l_date_init,$l_kind,$l_category,$l_marketing_manager,$l_technical_manager,$l_proposition,$l_amount,$l_contacts,$l_date_alarm,$l_state,$l_last_update,$l_archive,$l_comment,$l_at,$l_number,$l_visibility,$l_public,$l_private,$l_affect_deal,$l_parentdeal,$l_todo;
  global $l_invoices_connected,$l_no_invoices_connected;
  global $l_create_invoice, $l_archived_invoices_connected;
  global $l_see_archived_invoices, $incl_arch;
  global $perms_user,$l_perms_user, $perms_admin,$l_contract_list;
  global $auth, $sess, $c_yes, $c_no;

  $id = $deal_q->f("deal_id");
  $pid = $deal_q->f("deal_parentdeal_id");
  $usercreate = $deal_q->f("deal_usercreate");
  $label = $deal_q->f("deal_label");
  $num = $deal_q->f("deal_number");
  $datebegin = $deal_q->f("datebegin");
  $kind = $deal_q->f("dealtype_label");
  $cat = $deal_q->f("tasktype_label");
  $lvis = ($deal_q->f("deal_visibility") == 0 ? $l_public : $l_private);
  $market = $deal_q->f("lname1") . " " . $deal_q->f("fname1");
  $tech = $deal_q->f("lname2") . " " . $deal_q->f("fname2");
  $datepro = ($deal_q->f("deal_dateproposal") != "0000-00-00" ? $deal_q->f("deal_dateproposal") : "");
  $amount = $deal_q->f("deal_amount");
  $con1 = $deal_q->f("deal_contact1_id");
  $con2 = $deal_q->f("deal_contact2_id");
  $datealarm = $deal_q->f("datealarm");
  $status = $deal_q->f("dealstatus_label");
  $dateu = timestamp_to_date($deal_q->f("datemodif"));
  $timeu = timestamp_to_time($deal_q->f("datemodif"));
  $archive = ($deal_q->f("deal_archive") == 1 ? $c_yes : $c_no);
  $todo = $deal_q->f("deal_todo");
  $com = $deal_q->f("deal_comment");
  $comp = $deal_q->f("company_name");
  $ad1 = $deal_q->f("company_address1");
  $zip = $deal_q->f("company_zipcode");
  $town = $deal_q->f("company_town");

  $nb_invoices = $q_invoices->num_rows ();
  echo "<CENTER>
    <TABLE border=0 width=100%><TR><TD valign=top width=130>";
//////////////////
//LIENS EXTERNES//
//////////////////
  // Parent deal info (if exists) : link and bind
  if ($pid > 0) {
    $plabel = get_parent_label($pid);
    echo "<a href=\"". $sess->url("deal_index.php?action=parent_detailconsult&amp;param_parent=$pid")." \">$l_parentdeal</a><br>$plabel<br>";
  }
  if ($auth->auth["perm"] != $perms_user) {
    echo "<a href=\"".$sess->url("deal_index.php?action=affect&amp;param_deal=$id")
           . "\">$l_affect_deal</a><br><br>";
  }
  echo "
  <A HREF=\"".$sess->url("../contract/contract_index.php?action=search&amp;param_deal=".$id)."\">
  <img BORDER=0  SRC=\"/images/$set_theme/$ico_contact_list\"></A>
  <A HREF=\"".$sess->url("../contract/contract_index.php?action=search&amp;param_deal=".$id)."\">
  $l_contract_list</A><BR><BR>";
  // to create an invoice connected to this deal
  if ($auth->auth["perm"] == $perms_admin) {
    echo "<a href=\"".$sess->url("../treso/invoice_index.php?action=new&amp;deal_linked=".$id)."\"> $l_create_invoice </a><br><br>";
    // this is displayed close to links so we use that color too
    echo "<font color=\"#$col_link\">".
      $nb_invoices." ".$l_invoices_connected.
      "</font>";
    // how many archived invoices connected :
    $nb_arch_invoices = run_query_nb_archived_invoices ($id);
    echo "<br><br><font color=\"#$col_link\">".
      $nb_arch_invoices." ".$l_archived_invoices_connected.
      "</font>";
  }else { /* echo "et merde..."; */ }
//////////////////////
//FIN LIENS EXTERNES//
//////////////////////

  echo "</TD><TD>
    <TABLE border=0><TR><TD>
      <TABLE border=0><TR><TD valign=top align=left>
        <TABLE border=0>
        <TR>
          <TD><FONT COLOR=\"#$col_label\">$l_label</FONT></TD>
          <TD>$label</TD>";
   
  if ($usercreate == $auth->auth["uid"]) {   
   echo "<TD><FONT COLOR=\"#$col_label\">$l_visibility</FONT></TD>
     <TD>$lvis</TD>";
  }

  echo "</TR><TR>
          <TD><FONT COLOR=\"#$col_label\">$l_number</FONT></TD>
          <TD>$num</TD>
        </TR><TR>
          <TD><FONT COLOR=\"#$col_label\">$l_date_init</FONT></TD>
          <TD>$datebegin</TD>
        </TR><TR>
          <TD><FONT COLOR=\"#$col_label\">$l_kind</FONT></TD>
          <TD>$kind</TD>
        </TR><TR>
          <TD><FONT COLOR=\"#$col_label\">$l_category</FONT></TD>
          <TD>$cat</TD>
        </TR>
        </TABLE>

        <table border=0>
        <tr>
          <td><font color=\"#$col_label\">$l_marketing_manager</font></td>
          <td>$market</td>
        </tr><tr>
          <td><font color=\"#$col_label\">$l_technical_manager</font></td>
          <td>$tech</td>
        </tr>
        </table>

        <br>
        <TABLE border=2 bgcolor=\"#$col_table\"><TR><TD>
          <TABLE>
          <TR>
            <TD>$l_proposition :</TD>
            <TD>Date <BR>$datepro</TD>
            <TD>$l_amount<BR>$amount</TD>
          </TR>
          </TABLE>
        </TD>
        </TR>
        </TABLE>
<!-- Company and contacts Panel -->
      </TD><TD valign=top>
        <TABLE bgcolor=\"#$col_label\">
        <TR>
          <TD bgcolor=\"#$col_table\" colspan=2>
          <FONT color=\"#$col_textem\">$comp</FONT>&nbsp;&nbsp;&nbsp;&nbsp;
          <A HREF=\"". $sess->url("../company/company_index.php?action=detailconsult&amp;param_company=$comp_id")."\"><IMG ALIGN=MIDDLE SRC=\"/images/$set_theme/$ico_company\"></A>
          <BR>$ad1
          <BR>$zip $town</TD>
        </TR><TR>
          <TD valign=center colspan=2>$l_contacts</TD>
        </TR><TR>";

    // Contact 1
    while($con_q->next_record()) {
      if ($con_q->f("contact_id") == $con1)
        echo "<TD>".$con_q->f("contact_lastname") . " " .
        $con_q->f("contact_firstname") . " - " . $con_q->f("contact_phone") .
        "</TD>";
    }
    echo "<TD><A HREF=\"".
      $sess->url("../contact/contact_index.php?action=detailconsult&amp;param_contact=$con1") . "\"><IMG ALIGN=middle
        SRC=\"/images/$set_theme/$ico_contact\"></A></TD>
        </TR><TR>";

    // Contact 2
    if ($con_q->nf()>0) {  
      $con_q->seek(0);
    }

    while($con_q->next_record()) {
      if ($con_q->f("contact_id") == $con2) 
        echo "<TD>".$con_q->f("contact_lastname")." ". $con_q->f("contact_firstname") .  " - " . $con_q->f("contact_phone")."</TD>";
    }
    echo "<TD><A HREF=\"".
      $sess->url("../contact/contact_index.php?action=detailconsult&amp;param_contact=$con2") . "\"><IMG ALIGN=middle
          SRC=\"/images/$set_theme/$ico_contact\"></A></TD>
        </TR>
        </TABLE>
        <TABLE width=100%>
        <TR>
          <TD><FONT color=\"#$col_ok\">$l_date_alarm</FONT>&nbsp;&nbsp;</TD>
          <TD>$datealarm</TD>
        </TR><TR>
          <TD><FONT color=\"#$col_label\">$l_state</FONT>&nbsp;&nbsp;</TD>
          <TD>$status</TD> 
        </TR><TR>
          <TD colspan=2>
          <FONT color=\"#$col_label\">$l_last_update </FONT> $dateu
          <FONT color=\"#$col_label\"> $l_at </FONT> $timeu
          </TD>
        </TR><TR>
          <TD><FONT color=\"#$col_label\">$l_archive</FONT>&nbsp;&nbsp;</TD>
          <TD>$archive</TD>
        </TR>
        </TABLE>
      </TD>
      </TR>
      </TABLE>
    </TD>
    </TR><TR>
      <TD>
      <TABLE width=100%>
        <TR>
          <TD valign=top><FONT COLOR=\"#$col_label\">$l_todo</FONT>
          <BR>$todo<br></TD>
        </TR><TR>
          <TD valign=top><FONT COLOR=\"#$col_label\">$l_comment</FONT>
          <BR>" . nl2br($com) . "</TD>
          <TD align=right>";

  if ($auth->auth["perm"] != $perms_user) {
    echo "<FORM METHOD=GET NAME=form_deal_update ACTION=\"".
      $sess->url("deal_index.php")."\">";
    echo "
      <INPUT TYPE=hidden NAME=action VALUE=\"detailupdate\">
      <INPUT TYPE=hidden NAME=param_deal VALUE=\"$id\">
      <INPUT TYPE=submit value=\"$l_update\">
      </FORM>";
  }

  echo "&nbsp;</TD>
        </TR>
        </TABLE>
      </TD>
      </TR>
      </TABLE>
    </TD>
    </TR>
    </TABLE>
  </CENTER>";

  // we display invoices list
  echo "<br><br><br>";
  if ($nb_invoices == 0) {
    display_ok_msg ($l_no_invoices_connected);
  } else {
    display_ok_msg ($nb_invoices." ".$l_invoices_connected);

    echo "</CENTER>";
    echo "<br><br>";
    
    $url = $sess->url("deal_index.php?".
		      "action=detailconsult".
		      "&amp;param_deal=".
		      $deal_q->f("deal_id").
		      "&amp;incl_arch=".$incl_arch);
    
    $dis_invoice_list = new OBM_DISPLAY("DATA", $invoices_options);
    $dis_invoice_list->display_entity = "invoice";
    $dis_invoice_list->display_module = "deal";
    $dis_invoice_list->data_set = $q_invoices;
    $dis_invoice_list->data_header = "top";
    $dis_invoice_list->data_url = $url;

    $dis_invoice_list->display();
    echo "<br>";
    // and a button to allow displaying archived invoices :
    $url = $sess->url ("deal_index.php?action=detailconsult".
		       "&amp;param_deal=".$id.
		       "&amp;incl_arch=1");
    echo "
      <center>
       <form method=\"post\" action=\"$url\">
	<input type=\"submit\" value=\"$l_see_archived_invoices\">
       </form>
      </center>";
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display Deal Form                                                         //
// Parameters :
//   - $action    : action called
//   - $deal_q    : DBO : information about the deal (null for new deal)
//   - $kind_q    : DBO : deal kind list
//   - $cat_q     : DBO : deal category list
//   - $usr_q     : DBO : list of userobm
//   - $comp_q    : DBO : deal company infos (name, ad1, zip, town when new)
//   - $con_q     : DBO : list of contact from the company 
//   - $sta_q     : DBO : list of deal status
//   - $comp_id   : id of the company concerned by the deal
//   - deal[]     : default or transmitted values
//     keys used  : id, usercreate, num, label
///////////////////////////////////////////////////////////////////////////////
function html_deal_form($action,$deal_q,$kind_q,$cat_q,$usr_q,$comp_q,$con_q,$sta_q,$comp_id,$contr_q,$deal) {
  // - Themes
  global $set_theme,$col_label,$col_table,$col_textem,$col_ok,$ico_company,$ico_contact;
  // -- Labels
  global $c_php_isodate_format, $l_update,$l_delete,$l_insert, $l_none;
  global $l_label,$l_date_init,$l_kind,$l_category,$l_marketing_manager,$l_technical_manager,$l_proposition,$l_amount,$l_contacts,$l_date_alarm,$l_state,$l_last_update,$l_archive,$l_comment,$l_at,$l_number,$l_private,$l_todo;
  global $action, $sess, $auth,$c_all,$l_all,$l_add_contrat;
  
  // if update mode and first time, values are taken from db
  if ($action == "detailupdate") {
    $id = $deal_q->f("deal_id");
    $pid = $deal_q->f("deal_parentdeal_id");
    $usercreate = $deal_q->f("deal_usercreate");
    $label = $deal_q->f("deal_label");
    $num = $deal_q->f("deal_number");
    $datebegin = $deal_q->f("datebegin");
    $kind = $deal_q->f("deal_type_id");
    $cat = $deal_q->f("deal_tasktype_id");
    $vis = ($deal_q->f("deal_visibility") == 0 ? "0" : "1");
    $market = $deal_q->f("deal_marketingmanager_id");
    $tech = $deal_q->f("deal_technicalmanager_id");
    $dateprop = ($deal_q->f("deal_dateproposal") != "0000-00-00" ? $deal_q->f("deal_dateproposal") : "");
    $amount = $deal_q->f("deal_amount");
    $con1 = $deal_q->f("deal_contact1_id");
    $con2 = $deal_q->f("deal_contact2_id");
    $datealarm = $deal_q->f("datealarm");
    $status = $deal_q->f("deal_status_id");
    $dateu = timestamp_to_date($deal_q->f("datemodif")); // ???? timeupdate
    $timeu = timestamp_to_time($deal_q->f("datemodif"));
    $archive = ($deal_q->f("deal_archive") == 1 ? " checked" : "");
    $todo = $deal_q->f("deal_todo");
    $comment = $deal_q->f("deal_comment");
    $comp = $deal_q->f("company_name");
    $ad1 = $deal_q->f("company_address1");
    $zip = $deal_q->f("company_zipcode");
    $town = $deal_q->f("company_town");
  } else {
    // Values are taken from parameters
    $id = $deal["id"];
    $pid = $deal["parent"];
    $usercreate = $deal["usercreate"];
    $num = stripslashes($deal["num"]);
    $label = stripslashes($deal["label"]);
    $vis = $deal["vis"];
    $datebegin = stripslashes($deal["datebegin"]);
    $kind = $deal["kind"]; 
    $cat = $deal["cat"];
    $market = $deal["market"];
    $tech = $deal["tech"];
    $dateprop = stripslashes($deal["dateprop"]);
    $amount = stripslashes($deal["amount"]);
    $con1 = $deal["contact1"];
    $con2 = $deal["contact2"];
    $datealarm = stripslashes($deal["datealarm"]);
    $status = $deal["state"];
    $timeupdate = $deal["timeupdate"];
    $dateu = timestamp_to_date($timeupdate);
    $timeu = timestamp_to_time($timeupdate);
    $archive = ($deal["archive"] == 1 ? "checked" : "");
    $todo = stripslashes($deal["todo"]);
    $comment = stripslashes($deal["com"]);
    if ($action == "new") {
      $comp = $comp_q->f("company_name");
      $ad1 = $comp_q->f("company_address1");
      $zip = $comp_q->f("company_zipcode");
      $town = $comp_q->f("company_town");
      $market = $comp_q->f("company_marketingmanager_id");
      // We pre-fill datebegin and datealarm with current date
      $today = getdate();
      $datebegin = $today["year"]."-".$today["mon"]."-".$today["mday"];
      $datebegin = date($c_php_isodate_format); 
      $datealarm = $datebegin;
    } else {
      $comp = stripslashes($deal["company_name"]);
      $ad1 = $deal["company_ad1"];
      $zip = $deal["company_zip"];
      $town = $deal["company_town"];
    }
  }  

  // Kind select construction
  $sel_kind = "<select name=sel_kind>
    <option value=\"0\">$l_none</option>";
  while ($kind_q->next_record()) {
    $kid = $kind_q->f("dealtype_id");
    $klabel = $kind_q->f("dealtype_label");
    $sel_kind .= "\n<option value=\"$kid\"";
    if ($kind == $kid) $sel_kind .= " selected"; 
    $sel_kind .= ">$klabel</option>";
  }
  $sel_kind .= "</select>";

  // Category select construction
  $sel_cat = "<select name=sel_cat>
    <option value=\"0\">$l_none</option>";
  while($cat_q->next_record()) {
    $cid = $cat_q->f("tasktype_id");
    $tt_label = $cat_q->f("tasktype_label");
    $sel_cat .= "\n<option value=\"$cid\"";
    if ($cat == $cid) $sel_cat .= " selected"; 
    $sel_cat .= ">$tt_label</option>\n";
  }
  $sel_cat .= "</select>";
 
  // Marketing manager select construction
  $sel_market = "<select name=sel_market>
    <option value=\"0\">$l_none</option>";
  while ($usr_q->next_record()) {
    $cid = $usr_q->f("userobm_id");
    $cname = $usr_q->f("userobm_lastname") . " " .
             $usr_q->f("userobm_firstname");
    $sel_market .= "<option value=\"$cid\"";
    if ($market == $cid) $sel_market .= " selected"; 
    $sel_market .= ">$cname</option>\n";
  }
  $sel_market .= "</select>";

  // Technical manager select construction
  if ($usr_q->nf()>0) {
    $usr_q->seek(0);
  }
  $sel_tech = "<select name=sel_tech>
    <option value=\"0\">$l_none</option>";
  while ($usr_q->next_record()) {
    $cid = $usr_q->f("userobm_id");
    $cname = $usr_q->f("userobm_lastname") . " " .
             $usr_q->f("userobm_firstname");
    $sel_tech .= "<option value=\"$cid\"";
    if ($tech == $cid) $sel_tech .= " selected"; 
    $sel_tech .= ">$cname</option>\n";
  }
  $sel_tech .= "</select>";

  // Contact 1 select construction
  $sel_con1 = "<SELECT name=sel_contact1>";
  while ($con_q->next_record()) {
    $cid = $con_q->f("contact_id");
    $cinfo = $con_q->f("contact_lastname") . " " .
             $con_q->f("contact_firstname") . " - " .
             $con_q->f("contact_phone");
    $sel_con1 .= "<OPTION value=\"$cid\"";
    if ($con1 == $cid) $sel_con1 .= " selected"; 
    $sel_con1 .= ">$cinfo</OPTION>\n";
  }
  $sel_con1 .= "</SELECT>";

  // Contact 2 select construction
  if ($con_q->nf()>0) {
    $con_q->seek(0);
  }
  $sel_con2 = "<SELECT name=sel_contact2>";
  while ($con_q->next_record()) {
    $cid = $con_q->f("contact_id");
    $cinfo = $con_q->f("contact_lastname") . " " .
             $con_q->f("contact_firstname") . " - " .
             $con_q->f("contact_phone");
    $sel_con2 .= "<OPTION value=\"$cid\"";
    if ($con2 == $cid) $sel_con2 .= " selected"; 
    $sel_con2 .= ">$cinfo</OPTION>\n";
  }
  $sel_con2 .= "</SELECT>";

  // State select construction
  $sel_state = "<SELECT name=sel_state>";
  while ($sta_q->next_record()) {
    $sid = $sta_q->f("dealstatus_id");
    $slabel = $sta_q->f("dealstatus_label");
    $sel_state .= "<OPTION value=\"$sid\"";
    if ($status == $sid) $sel_state .= " selected"; 
    $sel_state .= ">$slabel</OPTION>\n";
  }
  $sel_state .= "</SELECT>";
  
  if($contr_q == 0) 
    $ch_add_contr = "<INPUT type=checkbox name=ch_contrat>$l_add_contrat";


  echo "<CENTER>
    <FORM METHOD=POST NAME=form_deal_update 
      onSubmit=\"if (check_form(this)) return true; else return false;\"
      ACTION=\"".$sess->url("deal_index.php")."\">

    <TABLE><TR><TD>
      <TABLE>
      <TR>
        <TD><FONT COLOR=\"#$col_label\">$l_label</FONT><BR>
        <INPUT NAME=tf_label TYPE=text size=40 value=\"$label\">
        </TD>";

  if ( ($action=="new") ||
       ( (($action=="detailupdate") || ($action=="update")) && 
         ($usercreate == $auth->auth["uid"]) ) ) {
    echo "<TD>
        <TABLE border=2 bgcolor=\"#$col_table\">
          <TR><TD>&nbsp; $l_private &nbsp;
          <INPUT NAME=cb_vis TYPE=checkbox value=\"1\" ";
    if ($vis==1) echo " checked";
    echo "></TD></TR>
        </TABLE>
        </TD>";
  }

  echo "</TR>
      </TABLE>

      <TABLE>
      <TR>
        <TD><FONT COLOR=\"#$col_label\">$l_number</FONT><BR>
          <INPUT NAME=tf_num size=40 maxlength=32 value=\"$num\"></TD>
        <TD><FONT COLOR=\"#$col_label\">$l_date_init</FONT><BR>
          <INPUT NAME=tf_datebegin TYPE=text size=10 value=\"$datebegin\"></TD>
      </TR>
      </TABLE>

      <TABLE>
      <TR>
        <TD><FONT COLOR=\"#$col_label\">$l_kind</FONT><BR>
        $sel_kind
        </TD>
        <TD><FONT COLOR=\"#$col_label\">$l_category</FONT><BR>
        $sel_cat
        </TD>
      </TR>
      </TABLE>

      <TABLE>
      <TR>
        <TD><FONT COLOR=\"#$col_label\">$l_marketing_manager</FONT><BR>
        $sel_market
        </TD>
        <TD><FONT COLOR=\"#$col_label\">$l_technical_manager</FONT><BR>
        $sel_tech
        </TD>
      </TR>
      </TABLE>

      <TABLE border=2 bgcolor=\"#$col_table\">
      <TR>
        <TD>
        <TABLE>
        <TR>
          <TD>$l_proposition</TD>
          <TD>Date <BR>
            <INPUT NAME=tf_dateprop TYPE=text size=10 value=\"$dateprop\"></TD>
          <TD>$l_amount<BR>
            <INPUT NAME=tf_amount TYPE=text size=10 value=\"$amount\"></TD>
        </TR>
        </TABLE>
        </TD>
      </TR>
      </TABLE>

  <!-- Company and contact block -->
  
    </TD>
    <TD valign=top>
      <TABLE bgcolor=\"#$col_label\">
      <TR>
        <TD bgcolor=\"#$col_table\">
        <FONT color=\"#$col_textem\">$comp</FONT>&nbsp;
        <A HREF=\"" . $sess->url("../company/company_index.php?action=detailconsult&amp;param_company=$comp_id") . "\"><IMG ALIGN=MIDDLE
          SRC=\"/images/$set_theme/$ico_company\"></A>
        <BR>$ad1
        <BR>$zip $town
        </TD>
      </TR><TR>
        <TD valign=center>$l_contacts<BR>
        $sel_con1";

  if ($action == "detailupdate") {
    echo "<A HREF=\"" . $sess->url("../contact/contact_index.php?action=detailconsult&amp;param_contact=$con1")."\"><IMG ALIGN=middle SRC=\"/images/$set_theme/$ico_contact\"></A>";
  }
  echo "<BR>
      $sel_con2";

  if ($action == "detailupdate") {
    echo "<A HREF=\"".$sess->url("../contact/contact_index.php?action=detailconsult&amp;param_contact=$con2")."\"><img ALIGN=middle SRC=\"/images/$set_theme/$ico_contact\"></A>";
  }
  echo " </TD>
      </TR>
      </TABLE>
    <P>
      <TABLE>
      <TR>
        <TD><FONT color=\"#$col_ok\">$l_date_alarm</FONT><BR>
        <INPUT NAME=tf_datealarm TYPE=text SIZE=10 value=\"$datealarm\">
        </TD>
        <TD><FONT color=\"#$col_label\">$l_state</FONT><BR>
        $sel_state
        </TD>
      </TR>
      <TR>
        <TD colspan=2>
          <FONT color=\"#$col_label\">$l_last_update </FONT>
          $dateu
          <FONT color=\"#$col_label\"> $l_at </FONT>
          $timeu
        </TD>
      </TR>
      <TR>
        <TD>
          <INPUT TYPE=checkbox NAME=cb_archive VALUE=\"1\" $archive>$l_archive
        </TD>
      </TR>
      </TABLE>

      </TD>
    </TR>
    </TABLE>

    <TABLE>
    <TR>
      <TD><FONT COLOR=\"#$col_label\">$l_todo</FONT><BR>
      <INPUT type=text name=tf_todo size=60 maxlength=128 value=\"$todo\">
      </TD>
      <TD><FONT COLOR=\"#$col_label\" ><br>$ch_add_contr</FONT></TD>
    </TR>
    <TR>
      <TD colspan = 2><FONT COLOR=\"#$col_label\">$l_comment</FONT><BR>
        <TEXTAREA NAME=ta_com ROWS=6 COLS=72>$comment</TEXTAREA>
      </TD>
      <TD align=center>";

  if (($action == "detailupdate") || ($action == "update")) {
    echo "<INPUT TYPE=hidden NAME=param_company VALUE=\"$comp_id\">
        <INPUT TYPE=hidden NAME=param_parent VALUE=\"$pid\">
        <INPUT TYPE=hidden NAME=tf_company_name VALUE=\"$comp\">
        <INPUT TYPE=hidden NAME=hd_company_ad1 VALUE=\"$ad1\">
        <INPUT TYPE=hidden NAME=hd_company_zip VALUE=\"$zip\">
        <INPUT TYPE=hidden NAME=hd_company_town VALUE=\"$town\">
        <INPUT TYPE=hidden NAME=action VALUE=\"update\">
        <INPUT TYPE=hidden NAME=param_deal VALUE=\"$id\">
        <INPUT TYPE=submit value=\"$l_update\">
      </FORM>
      <BR>
      <FORM METHOD=POST NAME=form_deal_delete
      onSubmit=\"if (valider_suppression()) return true; else return false;\"
      ACTION=\"" . $sess->url("deal_index.php") . "\">
        <INPUT TYPE=hidden NAME=action VALUE=delete>
        <INPUT TYPE=hidden NAME=param_deal VALUE=\"$id\">
        <INPUT TYPE=submit value=\"$l_delete\">";
  } else {
    echo "<INPUT TYPE=hidden NAME=param_company VALUE=\"$comp_id\">
      <INPUT TYPE=hidden name=action value=insert>
      <INPUT TYPE=hidden name=param_parent value=\"$pid\">
      <INPUT TYPE=hidden NAME=tf_company_name VALUE=\"$comp\">
      <INPUT TYPE=hidden NAME=hd_company_ad1 VALUE=\"$ad1\">
      <INPUT TYPE=hidden NAME=hd_company_zip VALUE=\"$zip\">
      <INPUT TYPE=hidden NAME=hd_company_town VALUE=\"$town\">
      <INPUT TYPE=submit value=\"$l_insert\">";
  }

  echo "</FORM>
      </TD> 
    </TR>
    </TABLE>
  </CENTER>";
  
}


///////////////////////////////////////////////////////////////////////////////
// Form used to affect a deal to a parentdeal chosen in the list of  
// the existing parentdeals.
// Parameters :
//   - $parent_q  : the list of the existing parentdeals
//   - $p_deal_id : the id of the deal we want to affect to a parentdeal
///////////////////////////////////////////////////////////////////////////////
function html_deal_affect($parent_q, $p_deal_id) {
    global $l_affect,$l_deal_select_parentdeal;  
    global $sess;

    $sel_parent = "<SELECT NAME=\"sel_parent\" size=10>";
    while ($parent_q->next_record()) {
	$sel_parent .= "<OPTION value=\"".$parent_q->f("parentdeal_id")."\">".$parent_q->f("parentdeal_label")."</OPTION>";
    }
    $sel_parent .= "</SELECT>";

    echo "<CENTER>
      <FORM NAME=\"form_affect_deal\" METHOD=GET
        ACTION=\"" . $sess->url("deal_index.php") . "\">
      $l_deal_select_parentdeal
      <br><br>
      $sel_parent
      <BR><BR> 
      <INPUT TYPE=hidden NAME=action value=affect_update>
      <INPUT TYPE=hidden NAME=\"param_deal\" value=\"$p_deal_id\">
      <INPUT TYPE=submit NAME=\"sub_affect_deal\" value=\"$l_affect\">
      </FORM>
    </CENTER>";
}
         
	  
///////////////////////////////////////////////////////////////////////////////
// Deal Administration Forms
// Parametres:
//   - $kind_q   : DBO : Deal kind list
//   - $cat_q    : DBO : Deal TaskType list
//   - $status_q : DBO : Deal Status list
///////////////////////////////////////////////////////////////////////////////
function html_deal_admin_form($kind_q, $cat_q, $status_q) {
  global $col_a_table, $col_a_tableh, $col_textem;
  // Labels : 
  global $l_kind_manage,$l_kind_new,$l_kind_exist;
  global $l_status_manage,$l_status_exist,$l_status_new;
  global $l_category_manage,$l_category_exist,$l_category_new;
  global $l_category_label,$l_category_minilabel,$l_label,$l_minus,$l_plus,$l_order;
  // Actions : 
  global $l_kind_checkdelete, $l_kind_update, $l_kind_insert;
  global $l_cat_checkdelete,$l_cat_update,$l_cat_insert;
  global $l_status_checkdelete,$l_status_update,$l_status_insert;
  // Messages : 
  global $l_kind_insert_ok, $l_kind_insert_error, $l_kind_update_ok;
  global $l_kind_update_error, $l_kind_delete_error, $l_kind_delete_ok;
  global $l_status_insert_ok, $l_status_insert_error, $l_status_update_ok;
  global $l_status_update_error, $l_status_delete_error, $l_status_delete_ok;
  global $l_cat_insert_ok, $l_cat_insert_error, $l_cat_update_ok;
  global $l_cat_update_error, $l_cat_delete_error;
  global $sess;

  // Kind Select construction
  $sel_kind = "<SELECT NAME=sel_kind SIZE=4>";
   while ($kind_q->next_record()) {
     $id = $kind_q->f("dealtype_id");
     $inout = $kind_q->f("dealtype_inout");
     $label = $kind_q->f("dealtype_label");
     $sel_kind .= "<OPTION value=\"$id\">$inout $label</OPTION>\n";
  }
  $sel_kind .= "</SELECT>";

  // Status Select construction
  $sel_sta = "<SELECT NAME=sel_state SIZE=4>";
   while ($status_q->next_record()) {
     $id = $status_q->f("dealstatus_id");
     $order = $status_q->f("dealstatus_order");
     $label = $status_q->f("dealstatus_label");
     $sel_sta .= "<OPTION value=\"$id\">$order-$label</OPTION>\n";
  }
  $sel_sta .= "</SELECT>";

  // Category Select construction
  $sel_cat = "<SELECT NAME=sel_cat SIZE=4 >";
   while ($cat_q->next_record()) {
     $id = $cat_q->f("tasktype_id");
     $mlabel = $cat_q->f("tasktype_label");
     $sel_cat .= "<OPTION value=\"$id\">$mlabel</OPTION>\n";
  }
  $sel_cat .= "</SELECT>";

  echo "<CENTER>
    <FORM name=form_kind_delete
      METHOD=post action=\"" . $sess->url("deal_index.php") . "\">
    <TABLE WIDTH=90% border=1 bgcolor=\"#$col_a_table\">
    <TR>
      <TD colspan=5 BGCOLOR=\"#$col_a_tableh\">
        <I><FONT COLOR=\"#$col_textem\">$l_kind_manage
        </FONT></I></TD>
    </TR>
    <TR>
      <TD>

<!-- Kind Delete Section -->

      <TABLE border=0>
      <TR>
        <TD>$l_kind_exist</TD>
        <TD>$sel_kind</TD>
        <TD><INPUT TYPE=hidden name=action value=kind_checklink>
        <INPUT TYPE=submit name=sub_type value=\"$l_kind_checkdelete\"
          onClick=\"if (check_kind_checkdel(this.form)) return true; else return false;\">
        </TD>
      </TR>
      </FORM>

<!-- Kind Update Section -->

      <FORM name=form_kind_update
        METHOD=post action=\"" . $sess->url("deal_index.php") ."\">
      <TR>
        <TD colspan=2 align=center>
          <INPUT TYPE=hidden name=sel_kind value=\"\">
          <INPUT TYPE=hidden name=action value=kind_update>
          <TABLE border=0>
          <TR>
            <TD><INPUT TYPE=text name=tf_kind></TD>
          </TR><TR>
            <TD align=center>
              <INPUT type=radio name=rd_kind_inout value=- checked>$l_minus
              <INPUT type=radio name=rd_kind_inout value=+>$l_plus
            </TD>
          </TR>
          </TABLE>
        </TD>
        <TD>
          <INPUT TYPE=submit name=sub_type value=\"$l_kind_update\"
            onClick=\"if (check_kind_upd(this.form, document.form_kind_delete)) return true; else return false;\">
        </TD>
        <TD valign=center>
        </TD>
      </TR>
      </TABLE>
      </TD>
      <TD>
      </FORM>

<!-- New Kind Section -->

      <FORM name=form_kind_new
        METHOD=post action=\"" . $sess->url("deal_index.php") . "\">
      <TABLE border=0>
      <TR>
        <TD align=right>$l_kind_new : <INPUT type=text name=tf_kind>
        </TD>
        <TD valign=center>
          <INPUT type=radio name=rd_kind_inout value=- checked>$l_minus<br>
          <INPUT type=radio name=rd_kind_inout value=+>$l_plus
        </TD>
      </TR>
      <TR>
        <TD align=center>
          <INPUT TYPE=hidden name=action value=kind_insert>
          <INPUT type=submit name=sub_type value=\"$l_kind_insert\"
            onClick=\"if (check_kind_new(this.form)) return true; " .
            "else return false;\">
        </TD>
      </TR>
      </TABLE>
      </TD>
    </TR>
    </TABLE>
    </FORM>
    </CENTER>

<!-- --------------- STATUS SECTION --------------- -->

    <br>
    <CENTER>
    <FORM name=form_status_delete
      METHOD=post action=\"" . $sess->url("deal_index.php")."\">
    <TABLE WIDTH=90% border=1 bgcolor=\"#$col_a_table\">
    <TR>
      <TD colspan=5 BGCOLOR=\"#$col_a_tableh\">
        <I><FONT COLOR=\"#$col_textem\">$l_status_manage</FONT></I></TD>
    </TR>
    <TR>
      <TD>

<!-- Status Delete Section -->

      <TABLE border=0>
      <TR>
        <TD>$l_status_exist</TD>
        <TD>$sel_sta</TD>
        <TD><INPUT TYPE=hidden name=action value=status_checklink>
          <INPUT TYPE=submit name=sub_status value=\"$l_status_checkdelete\"
          onClick=\"if (check_status_checkdel(this.form)) return true; else return false;\">
        </TD>
      </TR>
      </FORM>

<!-- Status Update Section -->

      <FORM name=form_status_update
        METHOD=post action=\"" . $sess->url("deal_index.php") . "\">
      <TR>
        <TD colspan=2 align=center>
          <INPUT TYPE=hidden name=sel_state value=\"\">
          <INPUT TYPE=hidden name=action value=status_update>
          <TABLE border=0>
          <TR>
            <TD align=right>$l_label : </TD>
            <TD align=left><INPUT TYPE=text name=tf_status></TD>
          </TR>
            <TD align=right>$l_order : </TD>
            <TD align=left>
	      <INPUT TYPE=text name=tf_order size=3 maxlength=2>
	    </TD>
          </TR>
          </TABLE>
        </TD>
        <TD>
          <INPUT TYPE=submit name=sub_status value=\"$l_status_update\"
            onClick=\"if (check_status_upd(this.form, document.form_status_delete)) return true; else return false;\">
        </TD>
      </TR>
      </TABLE>
      </TD>
      <TD>
      </FORM>

<!-- New Status Section -->

      <FORM name=form_status_new
        METHOD=post action=\"" . $sess->url("deal_index.php") . "\">
      <TABLE>
      <TR>
        <TD align=right>
	<TABLE border=0>
        <TR>
          <TD align=center>$l_status_new : </TD>
          <TD align=left><INPUT type=text name=tf_status></TD>
        </TR>
        <TR>
          <TD align=right>$l_order : </TD>
          <TD align=left>
            <INPUT type=text name=tf_order size=3 maxlength=2>
	  </TD>
        </TR>
        </TABLE>
	</TD>
      </TR>
      <TR>
        <TD align=center>
          <INPUT TYPE=hidden name=action value=status_insert>
          <INPUT type=submit name=sub_status value=\"$l_status_insert\"
            onClick=\"if (check_status_new(this.form)) return true; " .
            "else return false;\">
        </TD>
      </TR>
      </TABLE>
      </TD>
    </TR>
    </TABLE>
    </FORM>
    </CENTER>

<!-- --------------- CATEGORY SECTION --------------- -->

    <br>
    <CENTER>
    <FORM name=form_category_delete
      METHOD=post action=\"" . $sess->url("deal_index.php") . "\">
    <TABLE WIDTH=90% border=1 bgcolor=\"#$col_a_table\">
    <TR>
      <TD colspan=5 BGCOLOR=\"#$col_a_tableh\">
        <I><FONT COLOR=\"#$col_textem\">$l_category_manage</FONT></I></TD>
    </TR>
    <TR>
      <TD>

<!-- Category Delete Section -->

      <TABLE border=0>
      <TR>
        <TD>$l_category_exist</TD>
        <TD>$sel_cat</TD>
        <TD><INPUT TYPE=hidden name=action value=cat_checklink>
          <INPUT TYPE=submit name=sub_cat value=\"$l_cat_checkdelete\"
          onClick=\"if (check_cat_checkdel(this.form)) return true; else return false;\">
        </TD>
      </TR>
      </FORM>

<!-- Category Update Section -->

      <FORM name=form_category_update
        METHOD=post action=\"" . $sess->url("deal_index.php") . "\">
      <TR>
        <TD colspan=2 align=center>
          <INPUT TYPE=hidden name=sel_cat value=\"\">
          <INPUT TYPE=hidden name=action value=cat_update>
	  <TABLE border=0>
          <TR>
            <TD align=right>$l_category_label : </TD>
            <TD align=left>
              <INPUT TYPE=text name=tf_cat>
            </TD>
          </TR>
          </TABLE>
        </TD>
        <TD><INPUT TYPE=submit name=sub_category value=\"$l_cat_update\"
          onClick=\"if (check_category_upd(this.form, document.form_category_delete)) return true; else return false;\">
        </TD>
      </TR>
      </TABLE>
      </TD>
      <TD>
      </FORM>

<!-- New Category Section -->

      <FORM name=form_category_new
        METHOD=post action=\"" . $sess->url("deal_index.php") . "\">
      <TABLE>
      <TR>
        <TD align=right>
        <TABLE border=0>
        <TR>
          <TD align=center>$l_category_new : </TD>
          <TD align=left>
	    <INPUT type=text name=tf_cat>
          </TD>
        </TR>
        </TABLE>
	</TD>
      </TR>
      <TR>
        <TD align=center>
          <INPUT TYPE=hidden name=action value=cat_insert>
          <INPUT type=submit name=sub_category value=\"$l_cat_insert\"
            onClick=\"if (check_category_new(this.form)) return true; " .
            "else return false;\">
        </TD>
      </TR>
      </TABLE>
      </TD>
    </TR>
    </TABLE>
    </FORM>
    </CENTER>";
}


///////////////////////////////////////////////////////////////////////////////
// Displays the kind links                                                   //
// Parameters:
//   - $id : kind id
///////////////////////////////////////////////////////////////////////////////
function dis_kind_links($id) {
  global $l_kind_link_deal, $l_kind_can_delete, $l_kind_cant_delete, $l_back;
  global $l_kind_delete;
  global $sess;

  $label = get_kind_label($id);
  $obm_q = run_query_kind_links($id);

  if ($obm_q->num_rows() > 0) {
    echo "$label : $l_kind_link_deal";
    while ($obm_q->next_record()) {
      $did = $obm_q->f("deal_id");
      $dlabel = $obm_q->f("deal_label");
      echo "<BR><A HREF=\"" .$sess->url("deal_index.php?action=detailconsult&amp;param_deal=$did") . "\">$dlabel</A>\n";
    }
    echo "<P>$label : $l_kind_cant_delete";
  } else {
    echo "$label : $l_kind_can_delete
      <TABLE><TR><TD>
        <FORM name=\"form_kind_delete\".
          METHOD=post action=\"" . $sess->url("deal_index.php") . "\">
        <INPUT TYPE=hidden name=action value=\"kind_delete\">
        <INPUT TYPE=hidden name=sel_kind value=\"$id\">
        <INPUT TYPE=submit name=sub_kind value=\"$l_kind_delete\">
        </FORM>
      </TD><TD>
        <FORM NAME=form_back METHOD=POST
     ACTION=\"" .$sess->url("deal_index.php") . "\">
     <INPUT TYPE=hidden NAME=action VALUE=admin>
     <INPUT TYPE=submit VALUE=\"$l_back\">
     </FORM>";
    echo "</TR></TABLE>";
  }
}


///////////////////////////////////////////////////////////////////////////////
// Displays the stabtus links                                                //
// Parameters:
//   - $id : status id
///////////////////////////////////////////////////////////////////////////////
function dis_status_links($id) {
  global $l_status_link_deal, $l_status_can_delete, $l_status_cant_delete;
  global $l_status_delete;
  global $sess, $l_back;

  $label = get_status_label($id);
  $obm_q = run_query_status_links($id);

  if ($obm_q->num_rows() > 0) {
    echo "$label : $l_status_link_deal";
    while ($obm_q->next_record()) {
      $did = $obm_q->f("deal_id");
      $dlabel = $obm_q->f("deal_label");
      echo "<BR><A HREF=\"" .$sess->url("deal_index.php?action=detailconsult&amp;param_deal=$did") . "\">$dlabel</A>\n";
    }
    echo "<P>$label : $l_status_cant_delete";
  } else {
    echo "$label : $l_status_can_delete
      <TABLE><TR><TD>
        <FORM name=\"form_kind_delete\".
          METHOD=post action=\"" . $sess->url("deal_index.php") . "\">
        <INPUT TYPE=hidden name=action value=\"status_delete\">
        <INPUT TYPE=hidden name=sel_state value=\"$id\">
        <INPUT TYPE=submit name=sub_status value=\"$l_status_delete\">
        </FORM>
      </TD><TD>
        <FORM NAME=form_back METHOD=POST
     ACTION=\"" .$sess->url("deal_index.php") . "\">
     <INPUT TYPE=hidden NAME=action VALUE=admin>
     <INPUT TYPE=submit VALUE=\"$l_back\">
     </FORM>";
    echo "</TR></TABLE>";
  }
}


///////////////////////////////////////////////////////////////////////////////
// Displays the category links                                               //
// Parameters:
//   - $id : category id
///////////////////////////////////////////////////////////////////////////////
function dis_cat_links($id) {
  global $l_cat_link_deal, $l_cat_can_delete, $l_cat_cant_delete;
  global $l_cat_delete;
  global $sess, $l_back;

  $label = get_cat_label($id);
  $obm_q = run_query_cat_links($id);

  if ($obm_q->num_rows() > 0) {
    echo "$label $l_cat_link_deal";
    while ($obm_q->next_record()) {
      $cid = $obm_q->f("deal_id");
      $clabel = $obm_q->f("deal_label");
      echo "<BR><A HREF=\"" .$sess->url("deal_index.php?action=detailconsult&amp;param_deal=$cid") . "\">$clabel</A>\n";
    }
    echo "<P>$label : $l_cat_cant_delete";
  } else {
    echo "$label : $l_cat_can_delete
      <TABLE><TR><TD>
        <FORM name=\"form_cat_delete\".
          METHOD=post action=\"" . $sess->url("deal_index.php") . "\">
        <INPUT TYPE=hidden name=action value=\"cat_delete\">
        <INPUT TYPE=hidden name=sel_cat value=\"$id\">
        <INPUT TYPE=submit name=sub_cat value=\"$l_cat_delete\">
        </FORM>
      </TD><TD>
        <FORM NAME=form_back METHOD=POST
     ACTION=\"" .$sess->url("deal_index.php") . "\">
     <INPUT TYPE=hidden NAME=action VALUE=admin>
     <INPUT TYPE=submit VALUE=\"$l_back\">
     </FORM>";
    echo "</TR></TABLE>";
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display the Deal Display preference screen
// Parameters:
//   - $pref_q        : DBO : Deal Field list to display
//   - $pref_parent_q : DBO : ParentDeal Field list to display
//   - $pref_invoice  : DBO : Invoice Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_deal_display_pref($pref_q, $pref_parent_q, $pref_invoice) {
  global $l_deal_display, $l_parentdeal_display, $l_invoice_display;
 
  $dis_pref = new OBM_DISPLAY("PREFERENCES", $pref_q);
  $dis_pref->display_entity = "deal";
  $dis_pref->pref_title = $l_deal_display;

  echo "<center>
    <table><tr><td valign=top>";
  $dis_pref->display();
  echo "</td>";

  $dis_pref->display_pref = $pref_parent_q;
  $dis_pref->display_module = "deal";
  $dis_pref->display_entity = "parentdeal";
  $dis_pref->pref_title = $l_parentdeal_display;

  echo "<td valign=top>";
  $dis_pref->display();
  echo "</td><td valign=\"top\">";
  $dis_pref->display_pref = $pref_invoice;
  $dis_pref->display_entity = "invoice";
  $dis_pref->pref_title = $l_invoice_display;
  
  $dis_pref->display();
  
  echo "</td></tr></table>";
  $dis_pref->dis_pref_help();
  echo "</center>";

}


///////////////////////////////////////////////////////////////////////////////
// -- ParentDeal's Section                                                   //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display the Parent Deal search result                                     //
// Parameters:
//   - $deal[]    : deal search criteria
//     keys used  : company_id, name, company
///////////////////////////////////////////////////////////////////////////////
function dis_parentdeal_search_list($deal) {
  global $l_no_found_parent;
  global $auth, $new_order, $order_dir;

  $obm_q = run_query_search_parentdeal($deal, $new_order, $order_dir);
  
  $nb_parentdeals = $obm_q->num_rows();
  if ($obm_q->num_rows() == 0) {
    display_warn_msg($l_no_found_parent);
  } else {
    $pref_q = run_query_display_pref($auth->auth["uid"], "parentdeal");
    html_parentdeal_search_list($obm_q,$pref_q,$nb_parentdeals, $deal);
  }
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Parent Deal Search result                                //
// Parameters:
//   - $obm_q        : list of parent deals
//   - $option_dis_q : fields to display in the list of deals 
//   - $nb_pdeal     : nb parent deals found
//   - $deal[]       : parent deal search criteria
//     keys used     : plabel, pmanager, parchive
// Display the results of the parentdeal search
///////////////////////////////////////////////////////////////////////////////
function html_parentdeal_search_list($obm_q, $option_dis_q, $nb_parentdeals, $deal) {
  global $l_found_parent,$col_textem; 
  global $sess;

  $plabel = stripslashes($deal["plabel"]);
  $pmanager = $deal["pmanager"];
  $parchive = $deal["parchive"];

  echo "<FONT color=\"#$col_textem\">$nb_parentdeals $l_found_parent</font>";

  $url = $sess->url("deal_index.php?action=parent_search&amp;tf_plabel=$plabel&amp;sel_pmanager=$pmanager&amp;cb_parchive=$parchive");

  $obm_dis_list_parentdeal = new OBM_DISPLAY("DATA",$option_dis_q);
  $obm_dis_list_parentdeal->data_set = $obm_q;
  $obm_dis_list_parentdeal->data_url = $url;
  $obm_dis_list_parentdeal->data_header = "both";
  $obm_dis_list_parentdeal->data_idfield = "parentdeal_id"; 
  $obm_dis_list_parentdeal->data_set->seek($first_row);
  echo "<br>";
  $obm_dis_list_parentdeal->display();

}


///////////////////////////////////////////////////////////////////////////////
// Display Parent Deal Form                                                  //
// Parameters :
//   - $action    : action called
//   - $deal_q    : DBO : information about the parent deal (null for new deal)
//   - $usr_q     : DBO : OBM user list
//   - deal[]     : default or transmitted values
//     keys used  : parent_id, plabel, pmarket, ptech, pcom
///////////////////////////////////////////////////////////////////////////////
function html_parentdeal_form($action, $deal_q, $usr_q, $deal) {
  // -- Themes
  global $col_label,$col_table,$col_textem,$col_ok,$ico_company,$ico_contact;
  // -- Labels
  global $l_update_parent,$l_delete_parent,$l_insert_parent,$l_label,$l_marketing_manager,$l_technical_manager,$l_comment;
  global $sess,$auth;

  // if update mode and first time, values are taken from db
  if (($action == "parent_detailupdate") || ($action == "parent_delete")) {
    $parent = $deal_q->f("parentdeal_id");
    $plabel = $deal_q->f("parentdeal_label");
    $pmarket = $deal_q->f("parentdeal_marketingmanager_id");
    $ptech = $deal_q->f("parentdeal_technicalmanager_id");
    $pcomment = $deal_q->f("parentdeal_comment");
  } else {
    // Values are taken from parameters
    $parent = $deal["parent"];
    $plabel = stripslashes($deal["plabel"]);
    $pmarket = $deal["pmarket"];
    $ptech = $deal["ptech"];
    $pcomment = stripslashes($deal["pcom"]);
  }  

  // Marketing manager select
  $sel_market = "<select name=\"sel_pmarket\">";
  while($usr_q->next_record()) {
    $sel_market .= "<option value=\"" . $usr_q->f("userobm_id") . "\"";
    if ($usr_q->f("userobm_id") == $pmarket) $sel_market .= " selected";
    $sel_market .= ">" . $usr_q->f("userobm_lastname") . " " . $usr_q->f("userobm_firstname");
  }
  $sel_market .= "</select>";

  // Technical manager select
  if ($usr_q->nf()>0) {
    $usr_q->seek(0);
  }
  $sel_tech = "<select name=\"sel_ptech\">";
  while($usr_q->next_record()) {
    $sel_tech .= "<option value=\"" . $usr_q->f("userobm_id") . "\"";
    if ($usr_q->f("userobm_id") == $ptech) $sel_tech .= " selected";
    $sel_tech .= ">" . $usr_q->f("userobm_lastname") . " " . $usr_q->f("userobm_firstname");
  }
  $sel_tech .= "</select>";


  echo "<center>
    <form method=post name=form_parentdeal_update
      onSubmit=\"if (check_parentdeal(this)) return true; else return false;\"
      action=\"" . $sess->url("deal_index.php") . "\">

    <table>
    <tr>
      <td align=center>
      <table>
      <tr>
        <td><font color=\"#$col_label\">$l_label</font><br>
          <input name=tf_plabel type=text size=40 value=\"$plabel\">
        </td>
      </tr>
      </table>
      </td>
    </tr>
    <tr>
      <td align=center>
      <table> 
      <tr>
        <td><font color=\"#$col_label\">$l_marketing_manager</font><br>
        $sel_market
        </td><td><font color=\"#$col_label\">$l_technical_manager</font><br>
        $sel_tech
        </td>
      </tr>
      </table>
      </td>
    </tr>
    <tr>
      <td align=center>
      <table>
      <tr>
        <td><font color=\"#$col_label\">$l_comment</font><br>
          <textarea name=\"ta_pcom\" rows=8 cols=72>$pcomment</textarea></td>
        <td align=right>";

  if (($action == "parent_detailupdate") || ($action == "parent_update")) {
    echo "<input type=hidden name=action value=\"parent_update\">
        <input type=hidden name=param_parent value=\"$parent\">
        <input type=submit value=\"$l_update_parent\">
      </form>
      <form method=post name=form_parentdeal_delete
      onSubmit=\"if (valider_suppression()) return true; else return false;\"
      action=\"" . $sess->url("deal_index.php") . "\">
       <input type=hidden name=action value=\"parent_delete\">
       <input type=hidden name=param_parent value=\"$parent\">
       <input type=submit value=\"$l_delete_parent\">";
  } else {
    echo "<input type=hidden name=action value=\"parent_insert\">
      <input type=submit value=\"$l_insert_parent\">";
  }

  echo "</td>
      </tr>
      </table>
      </td>
    </tr>
    </table>
    </form>
  </center>";
}


///////////////////////////////////////////////////////////////////////////////
// Display Parent Deal Detail                                                //
// Parameters :
//   - $parent_q : DBO : information about the parent deal
//   - $deal_q   : DBO : parentdeal deal list
//   - deal[]    : default or transmitted values
//     keys used : forwarded to html_deal_search_form and $url for OBM_DISPLAY
//   - $pref_q   : display preferences for the deal list
//   - $nbdeals  : number of deals in the list
//     //---> used by the deal search form :
//   - $kind_q   : list of deal types available
//   - $cat_q    : list of deal categories available
//   - $state_q  : list of deal status available 
//   - $usr_q    : DBO : list of userobm
///////////////////////////////////////////////////////////////////////////////
function html_parentdeal_consult($parent_q,$deal_q,$deal,$pref_q,$nb_deals,$kind_q,$cat_q,$state_q,$usr_q) {
  global $col_textem,$col_label,$col_ok; 
  global $path,$sess,$auth,$perms_user;
  global $l_found,$l_no_found,$l_parentdeal_deal_new,$l_parentdeal_deals,$l_update_parent,$l_label,$l_technical_manager,$l_marketing_manager,$l_date_proposal,$l_date_alarm,$l_amount_in,$l_amount_out,$l_comment,$l_state;
  global $l_invoices_connected,$l_no_invoices_connected;
  global $popup_width, $popup_height, $l_deal_select_company;

  $parent = $parent_q->f("parentdeal_id");
  $plabel = $parent_q->f("parentdeal_label");
  $pmarket = $parent_q->f("parentdeal_marketingmanager_id");
  $ptech = $parent_q->f("parentdeal_technicalmanager_id");
  $pcomment = $parent_q->f("parentdeal_comment");

  $pmarket_name = $parent_q->f("lname1") . " " . $parent_q->f("fname1");
  $ptech_name = $parent_q->f("lname2") . " " . $parent_q->f("fname2");
  $p_status = get_parent_status($parent);
  $p_dateproposal = get_parent_dateproposal($parent);
  $p_datealarm = get_parent_datealarm($parent);
  $p_expense = get_parent_expense($parent);
  $p_income = get_parent_income($parent);

  // If allowed, display the update button and the new deal
  if ($auth->auth["perm"] != $perms_user) {
    $f_update = "<FORM NAME=\"form_consult_parent\"
      ACTION=\"" . $sess->url("deal_index.php") . "\" METHOD=GET>
      <input type=hidden name=action value=\"parent_detailupdate\">
      <input type=hidden name=param_parent value=\"$parent\">
      <input type=submit name=sub_detailupdate value=\"$l_update_parent\">
      </FORM>";

    $ret_url = urlencode("$path/deal/deal_index.php?action=new&amp;param_parent=$parent&amp;sel_market=$pmarket&amp;sel_tech=$ptech&amp;param_company=");
    $title = urlencode($l_deal_select_company);
    $win_url = $sess->url("../company/company_index.php?action=ext_get_id_url&amp;popup=1&amp;title=$title&amp;url=$ret_url");
    $f_new = "<FORM NAME=\"form_deal_new\"
      ACTION=\"" . $sess->url("deal_index.php") . "\" METHOD=GET
      onsubmit=\"window.open('$win_url','Company','height=$popup_height,width=$popup_width'); return false;\">

      <input type=submit name=detailupdate value=\"$l_parentdeal_deal_new\">
      </FORM>";
  } else {
    $f_update = "";
    $f_new = "";
  }

  echo "<CENTER><TABLE border=0 width=98%>
    <TR>
      <TD align=center>
        <TABLE BORDER=0>
        <TR>
          <TD><FONT COLOR=\"#$col_label\">$l_label : </FONT></TD>
          <TD>$plabel</TD>
        </TR>
        <TR>
          <TD><FONT COLOR=\"#$col_label\">$l_marketing_manager</FONT></TD>
          <TD>$pmarket_name</TD>
        </TR>
        <TR>
          <TD><FONT COLOR=\"#$col_label\">$l_technical_manager</FONT></TD>
          <TD>$ptech_name</TD>
        </TR>
        <TR>
          <TD><FONT COLOR=\"#$col_label\">$l_date_proposal : </FONT></TD>
          <TD>$p_dateproposal</TD>
        </TR>
        </TABLE>
      </TD>
      <TD align=center>
        <TABLE border=0>
        <TR>
          <TD><FONT COLOR=\"#$col_label\">$l_state : </FONT></TD>
          <TD>$p_status</TD>
        </TR>
        <TR>
          <TD><FONT COLOR=\"#$col_ok\">$l_date_alarm : </FONT></TD>
          <TD>$p_datealarm</TD>
        </TR>
        <TR>
          <TD><FONT COLOR=\"#$col_label\">$l_amount_out : </FONT></TD>
          <TD>$p_expense</TD>
        </TR>
        <TR>
          <TD><FONT COLOR=\"#$col_label\">$l_amount_in : </FONT></TD>
          <TD>$p_income</TD>
        </TR>
        </TABLE>
      </TD>
      <TD align=center>
        $f_update $f_new
      </TD>
    </TR>
    <TR>
      <TD colspan=3>
        <FONT COLOR=\"#$col_label\">$l_comment : <br></FONT>" . 
        nl2br($pcomment) . "
      </TD>
    </TR>
    </TABLE>
    </CENTER>

    <br>
    <font color=\"#$col_textem\"><b> $l_parentdeal_deals : </b></font><br>
    <hr>";

  //----- DEAL SEARCH FORM 
  html_deal_search_form($deal, $kind_q, $cat_q, $state_q, $usr_q);

  //-----> DEAL LIST
  // If no record found, display it and stop
  if ($nb_deals == 0) {
    echo "<font color=\"#$col_ok\">$l_no_found</font><br>";
    return;
  }

  echo "<font color=\"#$col_textem\">$nb_deals $l_found</font>";

  $label = urlencode(stripslashes($deal["label"]));
  $name = urlencode(stripslashes($deal["company_name"]));
  $archive = $deal["archive"];
  $kind = $deal["kind"];
  $cat = $deal["cat"];
  $state = $deal["state"];
  $manager = $deal["manager"];
  $dateafter = stripslashes($deal["dateafter"]);
  $datebefore = stripslashes($deal["datebefore"]);

  $url = $sess->url("deal_index.php?action=search&amp;tf_label=$label&amp;tf_company_name=$name&amp;cb_archive=$archive&amp;sel_kind=$kind&amp;sel_state=$state&amp;sel_cat=$cat&amp;sel_manager=$manager&amp;tf_dateafter=$dateafter&amp;tf_datebefore=$datebefore&amp;param_parent=$parent");

  // Display the list of deals attached to the parentdeal
  $dis_list_deal = new OBM_DISPLAY("DATA", $pref_q);
  $dis_list_deal->data_set = $deal_q;
  $dis_list_deal->data_url = $url;
  $dis_list_deal->data_header = "both";
  $dis_list_deal->data_idfield = "deal_id"; 
  $dis_list_deal->data_set->seek($first_row);

  echo "<br>";
  $dis_list_deal->display();
  echo "<br>";      
}

///////////////////////////////////////////////////////////////////////////////
// Display Deal Select Form                                               //
// Parameters:
//   - $deal_q : deal database result
//   - $title  : Title to be displayed
//   - $url    : URL to call with parameter deal
///////////////////////////////////////////////////////////////////////////////
function html_select_deal($deal_q, $title, $url="") {
  global $l_select_deal;
  global $sess;

  if ($url != "") {
    $jsfunc = "check_get_id_url(this, '$url')";
  } else {
    $jsfunc = "check_get_id(this)";
  }

  echo "<CENTER>
    <FORM METHOD=GET NAME=form_contr
          onSubmit=\"if ($jsfunc) return true; else return false;\"
          ACTION=\"\">";

  echo "$title
    <P><font size=2 face=Courier>
    <SELECT name=\"param_deal\" size=10 style=\"font-family:fixed,courier\">";

  while($deal_q->next_record()) {
    $id = $deal_q->f("deal_id");
    $name = $deal_q->f("deal_label");
    echo "\n<OPTION value=\"$id\">$name ";
  }

  echo "</SELECT></font>
    <P>
    <INPUT TYPE=\"hidden\" name=\"action\" value=\"new\">
    <INPUT TYPE=submit value=\"$l_select_deal\">
    </FORM>
    </CENTER>";
}
 

</SCRIPT>
