<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : account_display.inc                                          //
//     - Desc : Account display file                                         //
// 2002-07-16 Pierre Baudracco (from N. Roman)                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// to see tables during dev :
$border=($set_debug>0)? 2 : 0;
$cellspacing=1;

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["account_bank"] = $l_bank;
$fieldnames["account_number"] = $l_number;
$fieldnames["account_label"] = $l_label;
$fieldnames["account_balance"] = $l_initial_balance;
$fieldnames["account_today"] = $l_balance;
$fieldnames["payment_label"] = $l_label;
$fieldnames["payment_number"] = $l_number;
$fieldnames["payment_date"] = $l_date;
$fieldnames["payment_expect_date"] = $l_expected_date;
$fieldnames["payment_amount"] = $l_amount;


///////////////////////////////////////////////////////////////////////////////
// Display Account Form              
// Parameters:
//  $obm_q_account : if updating, contains the account current info
//  $action        : action 
///////////////////////////////////////////////////////////////////////////////
function html_account_form($obm_q_account, $action) {
  // - Themes :
  global $set_theme,$col_label,$col_table,$col_textem,$col_ok,$ico_company,$ico_contact;
  // -- Labels
  global $l_label, $l_owner, $l_bank, $l_number;
  global $l_initial_balance, $l_comment;
  global $l_insert,$l_update, $l_delete;
  global $obm_q_company;
  global $border,$cellspacing;

  // if update mode and first time, values are taken from db
  if ($action == "detailupdate") {
    $id = $obm_q_account->f("account_id");
    $label = $obm_q_account->f("account_label");
    $number = $obm_q_account->f("account_number");
    $today = $obm_q_account->f("account_today");
    $bank = $obm_q_account->f("account_bank");
    $comment = nl2br($obm_q_account->f("account_comment"));
    // when we update, this field is only available if the account
    // is not connected to any payments
    $q_payments = run_query_search_payments ($id);
    if ($q_payments->nf() != 0) {
      $bal_state = " disabled ";
    }
  }

  // If parameters have been given, they supercede the default action value
  if (isset($account["id"])) { $id = $account["id"]; }
  if (isset($account["label"])) { $label = stripslashes($account["label"]); }
  if (isset($account["number"])) { $number = $account["number"]; }
  if (isset($account["balance"])) { $balance = $account["balance"]; }
  if (isset($account["bank"])) { $bank = stripslashes($account["bank"]); }
  if (isset($account["comment"])) { $comment = stripslashes($account["comment"]); }


  echo "
  <center>
  <form method=\"post\" onSubmit=\"if (check_cpte(this)) return true; else return false;\" action=\"\">
    <table border=$border cellspacing=$cellspacing>
    <tr>
      <td>
        <table border=$border cellspacing=$cellspacing>
        <tr>
          <td>
            <font color=\"#$col_label\">$l_label</font>
            <br />
            <input name=\"tf_label\" size=\"40\" value=\"$label\">
          </td>
          <td>
            <font color=\"#$col_label\">$l_number</font>
            <br />
            <input name=\"tf_number\" size=\"12\" value=\"$number\">
          </td>
          <td>
            <font color=\"#$col_label\">$l_initial_balance</font>
            <br />
            <input name=\"tf_balance\" size=\"20\" value=\"$balance\" $bal_state>
          </td>
        </tr>
        </table>
      </td>
    </tr>
    <tr>
      <td>
        <font color=\"#$col_label\">$l_bank</font>
        <br />
        <input name=\"tf_bank\" size=\"60\" value=\"$bank\">
      </td>
    </tr>
    <tr>
      <td>
        <font color=\"#$col_label\">$l_comment</font>
        <br />
        <textarea name=\"ta_comment\" rows=\"6\" cols=\"72\">
        $comment
        </textarea>
      </td>
    </tr>
    <tr>
      <td>
        <br />
        <center>";
  if ($action=="detailupdate"){//modifier/supprimer
    $url_delete = url_prepare("account_index.php?action=delete&amp;param_account=$id");
    $url_update = url_prepare("account_index.php?action=update&amp;param_account=$id");
    echo "
            <table border=\"$border\" cellspacing=\"$cell_spacing\"><tr><td>
             <input type=\"button\" value=\"$l_update\" ".
      "onClick=\"if (check_cpte (this.form)) {".
      "this.form.action='$url_update'; this.form.submit ();".
      "}\">
            </td><td>";
    //    echo "
    //            <form method=post name=form_account_delete onSubmit=\"if(valider_suppression()) return true; else return false;\" action=\"$url_delete\">";
    // deletion is impossible if there are payments connected to
    // this account :
    if (($action == "detailupdate") && ($q_payments->nf() != 0)) {
      echo "
             <input type=\"button\" value=\"$l_delete\" disabled>
             </td></tr>
            </table>"; 
    } else {
      echo "
             <input type=\"button\" value=\"$l_delete\" ".
	"onClick=\" if (valider_suppression ()) { ".
	" this.form.action='$url_delete'; this.form.submit () ; ".
	"}\">
             </td></tr>
            </table>"; 
    }
  } else{ // inserer : 
    $url_insert = url_prepare("account_index.php?action=insert");
    echo "
          <input type=\"button\" value=\"$l_insert\" ".
      " onClick=\"if (check_cpte (this.form)) {".
      "this.form.action='$url_insert';this.form.submit();".
      "}\">";
  }  
  echo "
         </center>
         </td>
        </tr>
          </td>
       </tr>
</table></form></center>";
}


///////////////////////////////////////////////////////////////////////////////
// Account Search Form
// Parameters :
//  $p_action : current action in the application
//  $account hash table containing values for
//  name account and number.
///////////////////////////////////////////////////////////////////////////////
function html_account_search_form ($p_action, $account) {
  global $l_label_start, $l_bank, $l_number, $l_find;

  $label = $account["label"];
  $bank = $account["bank"];
  $number = $account["number"];

  $url = url_prepare("account_index.php?action=search");
  echo "
<center>
 <form method=\"post\" name=\"form_account\" ".
    " onSubmit=\"if (check_form(this) == false) return false ;".
    " else return true;\"".
    " action=\"$url\">";
  echo "
  <table border=0>
   <tr>";
  //--------------------------------------------------------------------------
  // account label
  echo "<td>
    <font size=\"-2\">".$l_label_start."</font><br>
    <input type=\"text\" name=\"tf_label\" size=\"20\" value=\"".$label."\"
   </td>";
  //--------------------------------------------------------------------------
  // bank name 
  echo "<td><font size=\"-2\">".$l_bank."</font><br>".
    "<input type=\"text\" name=\"tf_bank\" size=\"40\" value=\"".$bank.
    "\"</td>";
  //--------------------------------------------------------------------------
  // account number 
  echo "<td><font size=\"-2\">".$l_number."</font><br>".
    "<input type=\"text\" name=\"tf_number\" size=\"12\" value=\"".$number.
    "\"</td>";
  //--------------------------------------------------------------------------
  // bouton valider
  echo "<td valign=\"bottom\"> ";
  echo "<input name=\"submit\" type=\"submit\" value=\"$l_find\"></td>";
  echo "</tr></table></form><hr></center>";
}  

///////////////////////////////////////////////////////////////////////////////
// Display Search result (if it has to be done                               //
// if ($set_display != "no") or if it comes from the submit bouton           //
///////////////////////////////////////////////////////////////////////////////
// Parameters :
// $obm_q_accounts : list of accounts
// $obm_q_display_options : fields to display in the list of accounts
// $nb_accounts : number of accounts
//
// $account hash table containing :
// $label,
// $bank,
// $number values of search fields
function html_account_search_list ($obm_q_accounts, $obm_q_display_options, $nb_accounts, $account) {
  //-- Themes
  global $col_tableh,$col_textem,$col_table,$col_error;
  //-- Labels
  global $l_label,$l_bank,$l_number;
  global $l_found;
  global $perms_user,$l_perms_user;

  $label = urlencode($account["label"]);
  $bank = urlencode($account["bank"]);
  $number = urlencode($account["number"]);

  echo "<center><font color=\"#$col_textem\">$nb_accounts $l_found</font>
    <br /></center>";

  $url = url_prepare("account_index.php?action=search".
		    "&amp;tf_label=$label".
		    "&amp;tf_bank=$bank".
		    "&amp;tf_number=$number");
  $dis_account = new OBM_DISPLAY("DATA",$obm_q_display_options);
  $dis_account->data_set = $obm_q_accounts;
  $dis_account->data_url = $url;

  $url = url_prepare("account_index.php?action=search&amp;tf_label=$label&amp;tf_bank=$bank&amp;tf_number=$number");

  $dis_account->display ();
} // html_account_search_list ()


//////////////////////////////////////////////////////////////////////////
// Display the screen used to change the display parameters of accounts
//////////////////////////////////////////////////////////////////////////
// Parameters :
// ------------
//    $obm_q_options_display : list of fields to display or not
///////////////////////////////////////////////////////////////////////////
function dis_account_display_pref ($account_options, $payment_options) {
  // Label
   global $l_account_options, $l_payment_options;
  // Image
  global $set_theme ;
  echo "<center>\n";
 
 echo "<table><tr valign=\"top\">";
   echo "<td>";
 
  $dis_pref = new OBM_DISPLAY("PREFERENCES",$account_options) ;
  $dis_pref->display_entity = "account" ; // not used but must be set
  $dis_pref->pref_title = $l_account_options;
  $dis_pref->pref_dis_help = 0 ;
  $dis_pref->display() ;

  echo "</td><td>" ;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $payment_options);
  $dis_pref->display_entity = "payment";
  $dis_pref->pref_title = $l_payment_options;
  $dis_pref->pref_dis_help = 0;
  $dis_pref->display ();

  echo  "</td></tr></table>";
  $dis_pref->dis_pref_help ();

  echo  "</center>" ;
}
///////////////////////////////////////////////////////////////////////////////
// Parameters :
// ------------
//  $obm_q_account : information about the account
//  $p_action : action in account_index.php that brought us here
///////////////////////////////////////////////////////////////////////////////
function html_account_consult($obm_q_account, $p_action){
  // -- Themes
  global $set_theme,$col_label,$col_table,$col_textem,$col_ok;
  // - Labels
  global $l_update,$l_delete,$l_insert;
  global $col_balance_neg, $col_balance_pos;
  global $l_label,$l_comment,$l_bank, $l_number;
  global $l_balance, $l_compute_balance;
  global $auth, $perms_user,$l_perms_user;
  global $border, $cellspacing;


  $label = $obm_q_account->f("account_label");
  $number = $obm_q_account->f("account_number");
  $today = $obm_q_account->f("account_today");
  $bank = $obm_q_account->f("account_bank");
  $comment = nl2br($obm_q_account->f("account_comment"));

  $col_balance = ($obm_q_account->f("account_balance")>0) ? $col_balance_pos:$col_balance_neg;

  
  // number and label
  echo "
  <center>
  <table border=$border cellspacing=$cellspacing>
  <tr>
    <td>
      <table border=$border cellspacing=$cellspacing width=\"100%\">
      <tr>
        <td width=\"60%\">
          <font color=\"#$col_label\">$l_label</font>
          <br />$label
        </td>
        <td width=\"20%\">
          <font color=\"#$col_label\">$l_number</font>
          <br />$number
        </td>
        <td width=\"20%\">
         <font color=\"#$col_label\">$l_balance</font><br>
         <font color=\"#$col_balance\">$today</font>
        </td>
      </tr>
      </table>
    </td>
  </tr>
  <tr>
    <td>
      <font color=\"#$col_label\">$l_bank</font>
      <br />$bank
    </td>
  </tr>
  <tr>
    <td>
      <font color=\"#$col_label\">$l_comment</font>
      <br />$comment
    </td>
  </tr>";

  // we display buttons only if we need them :
  if ($p_action == "detailconsult") {
    echo "<br><br><br>";

    echo "
    <tr><td>";  
    // update/ compute balance of the account
    echo "
     <table border=$border cellspacing=$cellspacing width=\"100%\">
      <tr><td align=\"center\" valign=\"top\">";
    // if ($auth->auth["perm"] != $perms_user) {
    echo "
       <form method=\"post\" action=\"\">";
    $url_upd = url_prepare("account_index.php?action=detailupdate".
			   "&amp;param_account=".
			   $obm_q_account->f("account_id"));
    $url_comp = url_prepare("account_index.php?action=compute_balance".
			   "&amp;param_account=".
			   $obm_q_account->f("account_id"));
    
    echo " 
        <input type=\"button\" value=\"$l_update\" ".
      "onClick=\"this.form.action='$url_upd' ; this.form.submit ();\">";

    // compute the balance 
    // useless ?
//        <input type=hidden name=hd_balance value=\"".
//      $obm_q_account->f("account_balance")."\">
    echo "
      </td><td align=\"center\" valign=\"top\">
        <input type=\"button\" value=\"$l_compute_balance\" ".
      "onClick=\"this.form.action='$url_comp' ; this.form.submit ();\">
       </form>
      </td></tr>
     </table>";
    //}
    echo "
    </td></tr>";
  }
  echo "
   </table>
  </center>";
}

///////////////////////////////////////////////////////////////////////////////
// displays an error message when 
// an account connected to payments is being deleted 
///////////////////////////////////////////////////////////////////////////////
function html_impossible_deletion ($p_account_id, $q_related_payments) {
  global $col_error ,$l_impossible_deletion, $l_cancel;

  echo "
   <center>
    <font color=\"#$col_error\">".$q_related_payments->nf()." ".$l_impossible_deletion." : </font><br>";
  while ($q_related_payments->next_record()) {
    echo "
     <a href=\"payment_index.php?action=detailconsult&amp;param_payment=".$q_related_payments->f("payment_id")."\">".$q_related_payments->f("payment_label") . "</a><br>\n";
  }
  
  echo "
     <br><br>
     <form name=form_cancel_delete method=post action=\"".url_prepare("account_index.php?action=index")."\">
      <input type=submit name=\"sub_cancel_del\" value=\"$l_cancel\">
     </form>
   <center>";
}

//////////////////////////////////////////////////////////////////////////////
// compute an account balance at a given date
// $p_account : account to work on
// $payments_options : display options for the list of payments used 
// $p_date date of the compute. if none, we assume today
//////////////////////////////////////////////////////////////////////////////
function html_compute_balance ($p_account, $payments_options, $p_date) {
  global $border, $cellspacing;
  global $col_label, $col_balance_neg, $col_balance_pos;
  global $l_old_balance, $l_in_total, $l_out_total, $l_total;
  global $l_compute_balance, $l_choose_balance_date;
  global $l_nb_expected_used, $l_used_payments, $l_used_expected_payments;
  global $l_expected_payments, $l_payments, $l_totals;

  // if $p_date == "", we use today :
  $p_date = ($p_date =="") ? date("Y-m-d") : $p_date;

  // let's compute balance now : 
  $data = account_compute_balance ($p_account["account"], $p_date);
  $q_payments = $data["payments"];
  $q_expected = $data["expected"];
  $nb = $q_expected->nf();
  $col_balance = ($data["balance"] > 0) ? $col_balance_pos:$col_balance_neg;
  $col_today   = ($data["today"] > 0) ? $col_balance_pos:$col_balance_neg;

  // where we go when user clicks on compute :
  $url = url_prepare("account_index.php?action=compute_balance&amp;param_account=".$p_account["account"]);

  // we display all account data :
  $q_account = run_query_detail ($p_account["account"]);
  $q_account->next_record ();
  html_account_consult ($q_account, "compute_balance");

  // the form allowing a date input :
  echo "<br><br>";
  $url = url_prepare("account_index.php?action=compute_balance&amp;param_account=".$p_account["account"]);
  echo "
<center>
 <table border=\"$border\" cellspacing=\"$cellspacing\">
  <form method=\"post\" action=\"$url\"
  onSubmit=\"return check_balance_form (this);\">
   <tr><td>
    <font size=-2 color=\"#$col_label\">$l_choose_balance_date</font><br>
    <input type=text name=\"tf_balance_date\" value=\"".$p_date."\">
   </td><td>
    <input type=submit value=\"".$l_compute_balance."\">
   </td></tr>
  </form>
 </table>
</center>
 <br><br><hr>";
     
  // we display the result :
  echo "
<center>
 <table border=\"$border\" cellspacing=\"$cellspacing\" cellpadding=\"5\">
  <tr><td>
   &nbsp;";
  echo "
  </td><td>
   <font color=\"#$col_label\">$l_in_total</font>
  </td><td>
   <font color=\"#$col_label\">$l_out_total</font>
  </td></tr>";
  // first line : expected payments :
  echo "
  <tr><td>
   <font color=\"#$col_label\">$l_expected_payments</font>
  </td><td align=\"right\">
   <font color=\"#$col_balance_pos\">".$data["in_expected"]."</font>
  </td><td align=\"right\">
   <font color=\"#$col_balance_neg\">".$data["out_expected"]."</font>";
  // the payments already paid :
  echo "
  <tr><td>
   <font color=\"#$col_label\">$l_payments</font>
  </td><td align=\"right\">
   <font color=\"#$col_balance_pos\">".$data["in_real"]."</font>
  </td><td align=\"right\">
   <font color=\"#$col_balance_neg\">".$data["out_real"]."</font>
  </td></tr>";
  // total :
  $total_in = $data["in_real"]+$data["in_expected"];
  $total_out = $data["out_real"]+$data["out_expected"];
  echo "
  <tr><td>
   <font color=\"#$col_label\">$l_totals</font>
  </td><td align=\"right\">
   <font color=\"#$col_balance_pos\">".$total_in ."</font>
  </td><td align=\"right\">
   <font color=\"#$col_balance_neg\">".$total_out."</font>
  </td></tr>";
  // balance :
  echo "
  <tr><td>
   <font color=\"#$col_label\">$l_total</font>
  </td><td colspan=\"2\" align=\"center\">
   <font color=\"#$col_balance\">".$data["balance"]."</font>
  </td></tr>
 </table>
</center><br><br>";

  /*  echo "
<center>
 <table border=\"$border\" cellspacing=\"$cellspacing\" cellpadding=\"5\">
  <tr><td colspan=2>
   <font color=\"#$col_label\"><b>". 
    $nb."</b> ".  
    $l_nb_expected_used ."</font><br>  
  </td></tr>";  
  // today balance 
  echo "
  <tr><td>
   <font color=\"#$col_label\">$l_old_balance</font>
  </td><td>
   <font color=\"#$col_today\">".$data["today"]."</font>
  </td></tr>";
  // total amount of incoming on the compute period
  echo "
  <tr><td>
   <font color=\"#$col_label\">$l_in_total</font>
  </td><td> 
   <font color=\"#$col_balance_pos\">".$data["in"]."</font>
  </td></tr>";
  // total amount of outgoing on the compute period
  echo "
  <tr><td>
   <font color=\"#$col_label\">$l_out_total</font>
  </td><td>
   <font color=\"#$col_balance_neg\">".$data["out"]."</font>
  </td></tr>";
  // the final balance we all are waiting for :
  echo "
  <tr><td>
   <font color=\"#$col_label\">$l_total</font>
  </td><td>
   <font color=\"#$col_balance\">".$data["balance"]."</font>
  </td></tr>
 </table>
</center>
<br><br>";
  */
  // expected operations detail display :
  if ($q_expected->nf() != 0) { 
    $q_expected->seek(0);
    $url_expect = url_prepare ("account_index.php?action=compute_balance".
			      "&amp;param_account=".$p_account["account"]);

    $dis_expect  = new OBM_DISPLAY ("DATA", $payments_options);
    $dis_expect->data_set = $q_expected;
    $dis_expect->data_header = "top";
    $dis_expect->data_url = $url_expect;
    $dis_expect->data_order = false;

    display_ok_msg ($l_used_expected_payments);
    $dis_expect->display ();
  }
  // operations  detail display
  if ($q_payments->nf() != 0) { 
    $q_payments->seek(0);
    $url_done = url_prepare ("account_index.php?action=compute_balance".
			      "&amp;param_account=".$p_account["account"]);

    $dis_done  = new OBM_DISPLAY ("DATA", $payments_options);
    $dis_done->data_set = $q_payments;
    $dis_done->data_header = "top";
    $dis_done->data_url = $url_done;
    $dis_done->data_order = false;

    display_ok_msg ("<br>".$l_used_payments);
    $dis_done->display (); 
  }
}

</SCRIPT>
