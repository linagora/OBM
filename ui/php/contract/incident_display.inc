<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : incident_display.php                                         //
//     - Desc : Incident File                                                //
// 2002-03-14 : Mehdi Rande                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Display Company search Form                                               //
// Parameters:
//   - $usr_q      : DB result object (userobm list)
//   - $incident[] : default form values
//     keys used  : status, label incident, priority, label contract
///////////////////////////////////////////////////////////////////////////////
function html_incident_search_form ($usr_q, $incident) {
  global $l_incident_label,$l_contract_label,$l_incident_priority,$l_incident_status, $l_find,$l_incident_owner;
  global $l_redhot,$l_hot,$l_normal,$l_low,$l_open,$l_call,$l_waitcall,$l_paused,$l_closed,$sess,$c_all,$l_all;
  global $l_date_after,$l_date_before,$l_archive;

  $lincident = stripslashes($incident["lincident"]);
  $lcontract = stripslashes($incident["lcontract"]);
  $priority = $incident["priority"];
  $status = $incident["status"];
  $owner = $incident["owner"];
  $dateafter = $incident["date_after"];
  $datebefore = $incident["date_before"];
  $archive = ($incident["archive"] == "1" ? "checked" : "");

  $dis_sel_owner="<select name=\"sel_owner\">
    <option value=\"$c_all\">$l_all</option>";
  while($usr_q->next_record()) {
    $id = $usr_q->f("userobm_id");
   $dis_sel_owner.="\n<option value=\"$id\"";
   if ($id == $owner) 
     $dis_sel_owner.=" selected";
   $dis_sel_owner.=">". $usr_q->f("userobm_lastname") . " " . $usr_q->f("userobm_firstname")."</option>";
  }
  $dis_sel_owner.="</select>";

  $dis_sel_prio="<select name=\"sel_priority\">
    <option value=\"$c_all\">$l_all</option>
    <option value=\"REDHOT\"";
  if ($priority == "REDHOT") 
    $dis_sel_prio.= " selected";
  $dis_sel_prio.=">$l_redhot</option>
    <option value=\"HOT\"";
  if ($priority == "HOT") 
    $dis_sel_prio.=" selected";
  $dis_sel_prio.=">$l_hot</option>
    <option value=\"NORMAL\"";
  if ($priority == "NORMAL") 
    $dis_sel_prio.=" selected";
  $dis_sel_prio.= ">$l_normal</option>
    <option value=\"LOW\"";
  if ($priority == "LOW") 
    $dis_sel_prio.=" selected";
  $dis_sel_prio.=">$l_low</option>
  </select>";

  $dis_sel_stat="<SELECT name=\"sel_status\">\n<OPTION value=\"$c_all\">$l_all</OPTION>\n<OPTION value=\"OPEN\"";
  if ($status == "OPEN") 
   $dis_sel_stat.=" selected";
  $dis_sel_stat.= ">$l_open</OPTION>\n<OPTION value=\"CALL\"";
  if ($status == "CALL") 
   $dis_sel_stat.=" selected";
  $dis_sel_stat.= ">$l_call</OPTION>\n<OPTION value=\"WAITCALL\"";
  if ($status == "WAITCALL") 
   $dis_sel_stat.= " selected";
  $dis_sel_stat.=">$l_waitcall</OPTION>\n<OPTION value=\"PAUSED\"";
  if ($status == "PAUSED") 
   $dis_sel_stat.=" selected";
  $dis_sel_stat.=">$l_paused</OPTION>\n<OPTION value=\"CLOSED\"";
  if ($status == "CLOSED") 
   $dis_sel_stat.=" selected";
  $dis_sel_stat.=">$l_closed</OPTION>\n</SELECT>";
 
  echo "<CENTER>
     <FORM METHOD=GET NAME=f_incident ACTION=\"" .
     $sess->url("incident_index.php") . " \" onSubmit=\"if (verifier_inc(this)) return true; else return false;\">
     <TABLE BORDER=0><TR><TD>
      <TABLE BORDER=0><TR>
       <TD><FONT SIZE=-2>$l_incident_label</FONT><BR>
         <INPUT TYPE=text NAME=tf_lincident size=20 value=\"$lincident\"></TD>
       <TD>&nbsp;&nbsp;</TD>
       <TD align=left><FONT SIZE=-2> $l_contract_label</FONT><BR>
         <INPUT TYPE=text NAME=tf_lcontract size=20 value=\"$lcontract\"></TD>
       <TD align=\"center\"><FONT  SIZE=-2>$l_archive</FONT><BR>
  	<INPUT NAME=cb_archive TYPE=checkbox size=10 value=\"1\" ".$archive." >
       </TD>
       <TD><FONT  SIZE=-2>$l_date_after</FONT><BR>
  	<INPUT NAME=tf_dateafter TYPE=text size=10 value=\"$dateafter\">
       </TD> 
       <TD><FONT  SIZE=-2>$l_date_before</FONT><BR>
  	<INPUT NAME=tf_datebefore TYPE=text size=10 value=\"$datebefore\">
       </TD>
       </TR>
      </TABLE>
       </TD></TR><TR><TD align = center>
      <TABLE BORDER=0>     	
       <TR>
       <TD ><FONT SIZE=-2>$l_incident_priority</FONT><BR>
       ".$dis_sel_prio."
       </TD>
       <TD>&nbsp;&nbsp;</TD>
       <TD align=left><FONT SIZE=-2> $l_incident_status </FONT><BR>
       ".$dis_sel_stat."
       </TD>
       <TD>
       <FONT  SIZE=-2>$l_incident_owner</FONT><BR>
       ".$dis_sel_owner."
       </TD>
      </TR>
      </TABLE>
	</TD>      
	<TD VALIGN=bottom COLSPAN=\"3\" ALIGN=\"center\">&nbsp;&nbsp;
         <INPUT NAME=\"action\" TYPE=hidden VALUE=\"search\">
         <INPUT NAME=\"submit\" TYPE=submit VALUE=\"$l_find\">&nbsp;
       </TD>
     </TR>
     </TABLE>
     </FORM><HR>
   </CENTER>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the Incident search result                                        //
// Parameters:
//   - $incident[] : incident search criteria
//     keys used  : status, label incident, priority, label contract
///////////////////////////////////////////////////////////////////////////////
function dis_incident_search_list($incident) {
  global $l_no_found;
  global $auth, $new_order, $order_dir;

  $pref_q = run_query_display_pref($auth->auth["uid"], "incident");
  $obm_q = run_query_search($incident, $new_order, $order_dir);
  $nb_incident = $obm_q->num_rows();
  if ($nb_incident == 0) {
    display_warn_msg($l_no_found);
  } else {
    html_incident_search_list($obm_q,$pref_q,$nb_incident,$incident);
  }
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Search result                                                //
// Parameters : 
//   - $obm_q_comp    : list of incidents
//   - $obm_q_display : the fields which have to be displayed
//   - $nb_incident    : nb incidents to return by the query search
//   - $company[]     : incidents search criteria
//     keys used      : status, label incident, priority, label contract
///////////////////////////////////////////////////////////////////////////////
function html_incident_search_list($obm_q_comp,$obm_q_display,$nb_incident,$incident) {
  //-- Themes
  global $set_theme,$col_textem,$col_table,$col_tableh;
  //-- Labels
  global $l_found, $l_no_found;
  //-- Authentication
  global $perms_user,$l_perms_user, $auth, $sess;

  $lincident = $incident["lincident"];
  $lcontract = $incident["lcontract"];
  $priority = $incident["priority"];
  $status = $incident["status"];
  $param_contract = $incident["contract_id"];
  $owner = $incident["owner"];
  $archive = $incident["archive"];
  $dateafter = $incident["date_after"];
  $datebefore = $incident["date_before"];

  echo "<FONT color=\"#$col_textem\">";
  echo "$nb_incident $l_found";
  echo "</FONT><BR>";

  $url = $sess->url("incident_index.php?action=search&amp;tf_lincident=$lincident&amp;tf_lcontract=$lcontract&amp;sel_priority=$priority&amp;sel_status=$status&amp;param_contract=$param_contract&amp;tf_dateafter=$dateafter&amp;tf_datebefore=$datebefore&amp;sel_owner=$owner&amp;cb_archive=$archive");

  $obm_q_comp->seek($first_row);

  $dis_incidents_list=new OBM_DISPLAY("DATA",$obm_q_display);
  $dis_incidents_list->data_set = $obm_q_comp;
  $dis_incidents_list->data_url = $url;
  $dis_incidents_list->data_header = "both";

  $dis_incidents_list->display();
 
}


///////////////////////////////////////////////////////////////////////////////
// Display new incident : Contract Form                                      //
// Parameters:
//   - $contr_q : contract database result
///////////////////////////////////////////////////////////////////////////////
function html_new_incident_contract($contr_q) {
  global $l_incident_select_contract,$l_select_contract;
  global $sess;

  $dis_param_contract =" <SELECT name=\"param_contract\" size=10>";
    while($contr_q->next_record()) {
    $id = $contr_q->f("contract_id");
    $label = $contr_q->f("contract_label");
    $dis_param_contract .= "\n<OPTION value=\"$id\">$label";
  }
  $dis_param_contract .= " \n</SELECT>";

  echo "<CENTER>
    <FORM METHOD=POST NAME=form_contr
          onSubmit=\"if (verifie(this)) return true; else return false;\"
          ACTION=\"".$sess->url("incident_index.php")."\">";

  echo $l_incident_select_contract;
  echo "<P>
    $dis_param_contract
    <P>
    <INPUT TYPE=\"hidden\" name=\"action\" value=\"new\">
    <INPUT TYPE=submit value=\"$l_select_contract\">
    </FORM>
    </CENTER>";
}


///////////////////////////////////////////////////////////////////////////////
// Display Incident Form                                                     //
// Parameters:
//   - $action    : action called
//   - $inc_q     : incident database result 
//   - $contr_q   : contract database result 
//   - $usr_q     : userobm database result 
//   - $incident[]: default values
//     keys used  : kind, lname, fname, ad1, ad2, zip, town, cdx, ctry, func
///////////////////////////////////////////////////////////////////////////////
function html_incident_form($action, $inc_q, $contr_q, $usr_q, $incident) {
  global $col_table, $col_label, $col_textem;
  global $l_incident_label,$l_contract_label,$l_incident_prority,$l_incident_status, $l_find;
  global $l_redhot,$l_hot,$l_normal,$l_low,$l_open,$l_call,$l_waitcall,$l_paused,$l_closed;
  global $l_incident_logger,$l_incident_owner,$l_incident_priority,$l_incident_status;
  global $l_contract_label, $l_contract_num, $l_update, $l_checkdelete, $l_insert, $l_contract_date_init;
  global $l_contract_date_expir,$l_incident_description,$l_incident_solution,$l_incident_date, $l_archive_new;
  global $sess, $auth,$popup_height,$popup_width, $set_theme, $ico_mail, $l_incident_select_contract,$l_contract_label ;

  // if update mode and first time values are taken from database
  if ($action == "detailupdate") {
    $param_contract = $inc_q->f("incident_contract_id");
    $lincident = $inc_q->f("incident_label");
    $owner =  $inc_q->f("incident_owner");
    $logger =  $inc_q->f("incident_logger");
    $desc =  $inc_q->f("incident_description");
    $solu =  $inc_q->f("incident_resolution");
    $priority =  $inc_q->f("incident_priority");
    $status =  $inc_q->f("incident_state");
    $date =  $inc_q->f("date");
    $archive = ($inc_q->f("incident_archive") == "1" ? "checked" : "");
    $param_incident = $inc_q->f("incident_id");
    $c_name = $contr_q->f("contract_label");
    $c_id = $param_contract;
  } else {
    $param_contract = $incident["contract_id"];
    $lincident = $incident["lincident"];
    $owner =  $incident["owner"];
    $logger =  $incident["logger"];
    $desc =  $incident["description"];
    $solu =  $incident["solution"];
    $priority =  $incident["priority"];
    $status =  $incident["status"];
    $date =  $incident["date"];
    $archive = ($incident["archive"] == "1" ? "checked" : "");
  }

  $dis_sel_owner="<select name=\"sel_owner\">";
  while($usr_q->next_record()) {
    $dis_sel_owner.="\n<option value=".$usr_q->f("userobm_id");
    if ($usr_q->f("userobm_id") == $owner) 
      $dis_sel_owner.=" selected";
    $dis_sel_owner.=">". $usr_q->f("userobm_lastname") . " " . $usr_q->f("userobm_firstname")."</option>";
  }
  $dis_sel_owner.="</select>";

  $usr_q->seek();

  $dis_sel_logger="<select name=\"sel_logger\">";
  while($usr_q->next_record()) {
    $dis_sel_logger.="\n<option value=".$usr_q->f("userobm_id");
    if ($usr_q->f("userobm_id") == $logger) 
      $dis_sel_logger.=" selected";
    $dis_sel_logger.=">". $usr_q->f("userobm_lastname") . " " . $usr_q->f("userobm_firstname")."</option>";
  }
  $dis_sel_owner.="</select>";

  $dis_sel_prio="<SELECT name=\"sel_priority\" >\n<OPTION value=\"REDHOT\"";
  if ($priority == "REDHOT") 
   $dis_sel_prio.= " selected";
  $dis_sel_prio.=">$l_redhot</OPTION>\n<OPTION value=\"HOT\"";
  if ($priority == "HOT") 
   $dis_sel_prio.=" selected";
  $dis_sel_prio.=">$l_hot</OPTION>\n<OPTION value=\"NORMAL\"";
  if ($priority == "NORMAL") 
   $dis_sel_prio.=" selected";
  $dis_sel_prio.= ">$l_normal</OPTION>\n<OPTION value=\"LOW\"";
  if ($priority == "LOW") 
   $dis_sel_prio.=" selected";
  $dis_sel_prio.=">$l_low</OPTION>\n</SELECT>";

  $dis_sel_stat="<SELECT name=\"sel_status\" OnChange=\"auto_archive(this.form)\">\n<OPTION value=\"OPEN\"";
  if ($status == "OPEN") 
   $dis_sel_stat.=" selected";
  $dis_sel_stat.= ">$l_open</OPTION>\n<OPTION value=\"CALL\"";
  if ($status == "CALL") 
   $dis_sel_stat.=" selected";
  $dis_sel_stat.= ">$l_call</OPTION>\n<OPTION value=\"WAITCALL\"";
  if ($status == "WAITCALL") 
   $dis_sel_stat.= " selected";
  $dis_sel_stat.=">$l_waitcall</OPTION>\n<OPTION value=\"PAUSED\"";
  if ($status == "PAUSED") 
   $dis_sel_stat.=" selected";
  $dis_sel_stat.=">$l_paused</OPTION>\n<OPTION value=\"CLOSED\"";
  if ($status == "CLOSED") 
   $dis_sel_stat.=" selected";
  $dis_sel_stat.=">$l_closed</OPTION>\n</SELECT>";

  // UPDATE 
  if (($action=="detailupdate") || ($action=="update")) {
    $dis_submit = "<INPUT TYPE=hidden NAME=action VALUE=update>\n";
    $dis_submit .=   "<INPUT TYPE=hidden NAME=param_incident VALUE=\"$param_incident\">\n";
    $dis_submit .=   "<INPUT TYPE=submit value=\"$l_update\">\n";
    $dis_submit .=   "</FORM></TD>\n";

  // CHECK DELETE
    $dis_submit .=     "<TD valign=top>\n";
    $dis_submit .=     "<FORM METHOD=GET NAME=f_del_con onSubmit=\"if (confirm_del()) return true; else return false;\" ";
    $dis_submit .=     "ACTION=\"" . $sess->url("incident_index.php") . "\">\n";
    $dis_submit .=     "<INPUT TYPE=hidden NAME=action VALUE=delete>\n";
    $dis_submit .=     "<INPUT TYPE=hidden NAME=param_incident VALUE=\"$param_incident\">\n";
    $dis_submit .=     "<INPUT TYPE=submit value=\"$l_checkdelete\">\n";

  // INSERT
  } elseif (($action=="new") || ($action=="insert")) {
    $dis_submit .="<INPUT TYPE=\"hidden\" NAME=\"action\" VALUE=\"insert\">\n";
    $dis_submit .="<INPUT TYPE=submit value=\"$l_insert\">\n";
  }

  echo "<CENTER>
    <TABLE BORDER=0>
    <FORM METHOD=GET NAME=form_mod_incident onSubmit=\"if (check_incident(this)) return true; else return false;\" ACTION=\"".$sess->url("incident_index.php")."\">
    <TR><TD>

  <TABLE BORDER=0>
  <TR><TD>
    <TABLE BORDER=0>
    <TR>
    <TD><FONT SIZE=-2>$l_incident_label</FONT></TD>
    <TD><INPUT NAME=tf_lincident size=30 value=\"$lincident\"></TD>
    </TR>
    <tr>
    <td><font size=-2>$l_incident_logger</font></td>
    <td>$dis_sel_logger</td>
    </tr><tr>
    <td><font size=-2>$l_incident_owner</font></td>
    <td>$dis_sel_owner</td>
    </tr>
    <TR>
     <TD><FONT SIZE=-2>$l_archive_new</FONT></TD>
     <TD><INPUT NAME=cb_archive type=\"checkbox\"size=30 value=\"1\" $archive ></TD>
    </TR>
   </TABLE>
  </TD>
  <TD>&nbsp;&nbsp;</TD>
  <TD valign=top>
";
/*
$contr_q->next_record();
echo "
  <TABLE bgcolor=\"#$col_label\">
    <TR>
    <TD bgcolor=\"#$col_table\">$l_contract_label&nbsp;&nbsp;:&nbsp;&nbsp;</TD>
    <TD><FONT color=\"#$col_textem\">".$contr_q->f("contract_label")."</FONT></TD>
    </TR><TR>
    <TD bgcolor=\"#$col_table\">$l_contract_num &nbsp;&nbsp;:&nbsp;&nbsp;</TD>
    <TD><FONT color=\"#$col_textem\">".$contr_q->f("contract_numero")."</FONT></TD>
    </TR><TR>
    <TD bgcolor=\"#$col_table\">$l_contract_date_init &nbsp;&nbsp;:&nbsp;&nbsp;</TD>
    <TD><FONT color=\"#$col_textem\">".$contr_q->f("contract_debut")."</FONT></TD>
    </TR><TR>
    <TD bgcolor=\"#$col_table\">$l_contract_date_expir &nbsp;&nbsp;:&nbsp;&nbsp;</TD>
    <TD><FONT color=\"#$col_textem\">".$contr_q->f("contract_expiration")."</FONT></TD>
    </TR>
  </TABLE>
*/

echo "
    <TABLE BORDER=0>
    <TR>
    <TD><FONT SIZE=-2>$l_contract_label&nbsp;&nbsp;&nbsp;&nbsp; </FONT></TD>
    <TD>
<A HREF=\"".
      $sess->url("../contract/contract_index.php?action=detailconsult&amp;param_contract=$c_id") . "\">$c_name";
  echo "</A><INPUT TYPE=hidden NAME=param_contract value=\"$param_contract\">
      <INPUT TYPE=hidden NAME=contract_new_id value=\"$c_new_id\">
      <a href=\"\" onclick=\"window.open('../contract/contract_index.php?action=ext_get_id&amp;popup=1&amp;title=".urlencode($l_incident_select_contract)."','contract','height=$popup_height,width=$popup_width'); return false;\">
      <img src=\"/images/$set_theme/$ico_mail\"></a>
      <INPUT TYPE=text NAME=contract_name value=\"$c_new_name\" style=\"border:none\" READONLY onfocus=\"this.blur();\">
    </TD></TR>
    </TABLE>
</TD></TR>
<TR><TD colspan=3 align=\"center\">
  <TABLE BORDER=0>
   <TR>
    <TD><FONT SIZE=-2>$l_incident_priority</FONT> </TD>
    <TD>
    ".$dis_sel_prio."
    </TD>
    <TD><FONT SIZE=-2>$l_incident_status</FONT> </TD>
    <TD>
    ".$dis_sel_stat."
   </TD>
   <TD><FONT SIZE=-2>$l_incident_date&nbsp;&nbsp;&nbsp;</FONT></TD>
   <TD><INPUT NAME=tf_date size=12 value=\"$date\"></TD>
  </TABLE>
  <TABLE BORDER=0>
  <TR>
  <TD><FONT SIZE=-2>$l_incident_description</FONT><BR>
   <TEXTAREA NAME=ta_desc ROWS=6 COLS=60>$desc</TEXTAREA></TD>
  </TR>
  <TR>
  <TD><FONT SIZE=-2>$l_incident_solution</FONT><BR>
   <TEXTAREA NAME=ta_solu ROWS=6 COLS=60>$solu</TEXTAREA></TD>
  </TR>
  </TABLE>
  </TD></TR>
  <TR><TD align=middle colspan=3>";
  echo "<CENTER><TABLE BORDER=0>
  <TR><TD valign=top align=\"center\">
   ".$dis_submit."
    </TD></TR>
    </TABLE>
    </CENTER>
    </FORM>
    </TD></TR></TABLE>
    </CENTER>";

}


///////////////////////////////////////////////////////////////////////////////
// Display Incident Consult                                                  //
// Parameters:
//   - $inc_q : DBO : incident result
//   - $con_q : DBO : contract result
//      $obm_q_company : list of companies
//      $contact_q : list of contact from the company 
///////////////////////////////////////////////////////////////////////////////
function html_incident_consult($inc_q,$con_q,$obm_q_company,$contact_q) {
  // -- Themes
  global $set_theme,$col_label,$col_table,$col_textem,$col_ok,$ico_company,$ico_contact;
  // - Labels
  global $l_incident_label,$l_incident_prority,$l_incident_status;
  global $l_incident_logger,$l_incident_owner,$l_incident_priority,$l_incident_status;
  global $l_update,$l_incident_description,$l_incident_solution,$l_incident_date,$l_contract_num;
  global $l_to, $l_from,$l_manager,$l_client_manager,$l_update,$l_archive_consult;
  global $perms_user,$l_perms_user;
  global $auth, $sess;

  $label = $inc_q->f("incident_label");
  $pri = $inc_q->f("incident_priority");
  $state = $inc_q->f("incident_state");
  $date = $inc_q->f("date");
  $inc_id = $inc_q->f("incident_id");
  $inc_desc = nl2br($inc_q->f("incident_description"));
  $inc_reso = nl2br($inc_q->f("incident_resolution"));
  $con_id = $con_q->f("contract_id");
  $comp_id = $con_q->f("contract_company_id");
  $con_label = $con_q->f("contract_label");
  $con_num = $con_q->f("contract_numero");
  $con_debut = $con_q->f("contract_datebegin");
  $con_exp = $con_q->f("contract_dateexp");
  $tech = $con_q->f("contract_techmanager_id");
  $market = $con_q->f("contract_marketmanager_id");
  $con_cli = $con_q->f("contract_contact1_id");
  $con_cli2 = $con_q->f("contract_contact2_id");
  $obm_q_company->next_record();
  $comp_name = $obm_q_company->f("company_name");
  $comp_ad1 = $obm_q_company->f("company_address1");
  $comp_zip = $obm_q_company->f("company_zipcode");
  $comp_town = $obm_q_company->f("company_town");

  $logger = $inc_q->f("lname1") . " " . $inc_q->f("fname1");
  $owner = $inc_q->f("lname2") . " " . $inc_q->f("fname2");
  $dis_market = $con_q->f("lname1") . " " . $con_q->f("fname1");
  $dis_tech = $con_q->f("lname2") . " " . $con_q->f("fname2");
  $dis_con1 = $con_q->f("lname_c1") . " " . $con_q->f("fname_c1");
  $dis_con2 = $con_q->f("lname_c2") . " " . $con_q->f("fname_c2");

  if ($inc_q->f("incident_archive") == 1) {
    $dis_lab_archive ="</TD></TR><TR><TD>$l_archive_consult";
  }

  while($contact_q->next_record()) {
    if ($contact_q->f("contact_id") == $con_cli) 
      $dis_lab_cli1 = "<TD>".$contact_q->f("contact_lastname")." ". $contact_q->f("contact_firstname") . " - " . $contact_q->f("contact_phone")."</TD>";
    }

 if ($contact_q->nf()>0) {
   $contact_q->seek(0);
 }
 
 while($contact_q->next_record()) {
   if ($contact_q->f("contact_id") == $con_cli2) 
     $dis_lab_cli2 = "<TD>".$contact_q->f("contact_lastname")." ". $contact_q->f("contact_firstname") . " - " . $contact_q->f("contact_phone")."</TD>";
    }

  if ($auth->auth["perm"] != $perms_user) {
    $dis_update = "<FORM METHOD=GET NAME=form_contract_update ACTION=\"".$sess->url("incident_index.php") . "\">
      <INPUT TYPE=hidden NAME=action VALUE=\"detailupdate\">
      <INPUT TYPE=hidden NAME=param_incident VALUE=\"$inc_id\">
      <INPUT TYPE=hidden NAME=param_contract VALUE=\"$con_id\">
      <INPUT TYPE=submit value=\"$l_update\">
      </FORM>";
  }

  echo "<center>
  <table border=0>
  <tr>
    <td valign=top></td>
    <td>
      <table border=0>
      <tr>
        <td>
          <table border=0>
          <tr>
            <td valign=top align=left>
              <table>
              <tr>
                <td><font COLOR=\"#$col_label\">$l_incident_label</font></td>
                <td>$label</td>
              </tr><tr>
                <td><font COLOR=\"#$col_label\">$l_incident_priority</font></td>
                <td>$pri</td>
              </tr><tr>
                <td><font COLOR=\"#$col_label\">$l_incident_status</font></td>
                <td>$state</td>
              </tr><tr>
                <td><font COLOR=\"#$col_label\">$l_incident_date</font></td>
                <td>$date</td>
              </tr>
              </table>
              <table border=0>
              <tr>
                <td><font COLOR=\"#$col_label\">$l_incident_logger</font></td>
                <td>$logger $dis_lab_archive</td>
              </tr><tr>
                <td><font COLOR=\"#$col_label\">$l_incident_owner</font></td>
                <td>$owner</td>
              </tr>
              </table>
<!--  Pave Contract -->
            </td>
            <td valign=top>
              <table bgcolor=\"#$col_label\">
              <tr>
                <td bgcolor=\"#$col_table\" colspan=2>
                  <A HREF=\"".$sess->url("contract_index.php?action=detailconsult&amp;param_contract=$con_id") . "\"><font color=\"#$col_textem\">$con_label</font></a>
                  <br>$l_contract_num : $con_num
                  <br>$l_from $con_debut $l_to $con_exp
                </td>
              </tr><tr>
                <td valign=center colspan=2>$l_manager</td>
              </tr><tr>
                <td>$dis_market</td>
              </tr><tr>
                <td>$dis_tech</td>
              </tr>
              </table>
            </td>
<!-- Compan,contact -->
            <td valign=top>
              <table bgcolor=\"#$col_label\">
              <tr>
                <td bgcolor=\"#$col_table\" colspan=2>
                  <a href=\"".$sess->url("../company/company_index.php?action=detailconsult&amp;param_company=$p_company_id")."\"><img align=middle border=1 src=\"/images/$set_theme/$ico_company\"></a>
                  <font color=\"#$col_textem\">$comp_name</font>
                  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                  <br>$comp_ad1
                  <br>$comp_zip $comp_town
                </td>
              </tr><tr>
                <td valign=center colspan=2>$l_client_manager</td>
              </tr><tr>
                <td><a href=\"".
                  $sess->url("../contact/contact_index.php?action=detailconsult&amp;param_contact=$con_cli")."\"><img align=middle src=\"/images/$set_theme/$ico_contact\"></a> $dis_con1
                </td>
              </tr><tr>
                <td><a href=\"".
                  $sess->url("../contact/contact_index.php?action=detailconsult&amp;param_contact=$con_cli2")."\"><img align=middle src=\"/images/$set_theme/$ico_contact\"></a> $dis_con2
                </td>
              </tr>
              </table>
            </td>
          </tr>
          </table>
        </td>
      </tr>
      <tr>
        <td>
          <table width=100% border=1>
          <tr>
            <td valign=top>
              <font COLOR=\"#$col_label\">$l_incident_description</font>
              <br>$inc_desc
              <br>
            </td>
            <td valign=top>
              <font COLOR=\"#$col_label\">$l_incident_solution</font>
              <br>$inc_reso
            </td>
          </tr><tr>
            <td align=center colspan=2>
              <br>$dis_update &nbsp;</td>
          </tr>
          </table>
        </td>
      </tr>
      </table>
    </td>
  </tr>
  </table>
  </center>";

}


///////////////////////////////////////////////////////////////////////////////
// Display the Incidnet Display preference screen                            //
// Parameters:
//   - $display_pref_q : DBO : Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_incident_display_pref($display_pref_q) {
  global $l_incident_display;
 
  $dis_pref = new OBM_DISPLAY("PREFERENCES", $display_pref_q);
  $dis_pref->display_entity = "incident";
  $dis_pref->pref_title = $l_incident_display;
  $dis_pref->pref_dis_help = 1;

  echo "<center>";
  $dis_pref->display();
  echo "<br></center>";
}


