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
// Display Account specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_account(&$OD, $fieldname, $link_ok) {
  global $path, $set_theme,$col_balance_pos, $col_balance_neg;

  if ($fieldname == "account_label") {
    $res["url"] = "$path/account/account_index.php?action=detailconsult&amp;param_account=".$OD->data_set->f("account_id");
  }

  elseif ( ($fieldname == "account_today") 
	   || ($fieldname == "account_balance") ) {
    // balance column color :
    $balance = $OD->data_set->f($fieldname);
    if ($balance > 0) {
      $couleur = $col_balance_pos;
    } else {
      $couleur = $col_balance_neg;
    }
    $res["name"] = "<font color=\"#$couleur\">$balance</font>";
    $res["txt_name"] = $balance;
  }

  elseif ($fieldname == "account_pay_label") {
    $res["url"] = "$path/payment/payment_index.php?action=detailconsult&amp;param_pay_label=".$OD->data_set->f("account_pay_id");
  }

  elseif ($fieldname == "account_pay_amount") {
    // amount column color
    $amount = $OD->data_set->f($fieldname);
    if ($OD->data_set->f("payment_inout") == '+') {
      $couleur = $col_balance_pos;
    } else {
      $couleur = $col_balance_neg;
    }
    $res["name"] = "<font color=\"#$couleur\">$amount</font>";
    $res["txt_name"] = $amount;
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Account Search Form
// Parameters:
//   - $p_action : current action in the application
//   - $account  : hash table containing values for name account and number.
///////////////////////////////////////////////////////////////////////////////
function html_account_search_form ($p_action, $account) {
  global $l_label_start, $l_bank, $l_number, $l_find;

  $label = $account["label"];
  $bank = $account["bank"];
  $number = $account["number"];

  $url = url_prepare("account_index.php");

  $block .= "
  <form method=\"get\" name=\"f_search\"
    onsubmit=\"if (check_form(this) == false) return false; else return true;\"
    action=\"$url\">
  <div class=\"search\">
   <div class=\"searchLabel\">$l_label_start<br />
    <input name=\"tf_label\" size=\"20\" value=\"$label\" />
   </div>

   <div class=\"searchLabel\">$l_bank<br />
    <input name=\"tf_bank\" size=\"20\" value=\"$bank\" />
   </div>

   <div class=\"searchLabel\">$l_number<br />
    <input name=\"tf_number\" size=\"12\" value=\"$number\" />
   </div>

   <div class=\"searchLabel\">&nbsp;<br />
    <input name=\"action\" type=\"hidden\" value=\"search\" />
    <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
   </div>
  <div class='searchEnd'></div>
  </div>
  </form>";

  return $block;
}  


///////////////////////////////////////////////////////////////////////////////
// Display the Account search result
// Parameters:
//   - $account[] : account search criteria
//     keys used   : label, bank, number
///////////////////////////////////////////////////////////////////////////////
function dis_account_search_list($account) {
  global $l_no_found;
  global $auth, $new_order, $order_dir,$display;

  $prefs = get_display_pref($auth->auth["uid"], "account");
  $obm_q = run_query_search($account, $new_order, $order_dir);
  $nb_account = $obm_q->num_rows_total();
  if ($nb_account == 0) {
    $display["msg"] = display_info_msg($l_no_found);
  } else {
    return html_account_search_list($obm_q, $prefs, $nb_account, $account);
  }
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Search result
// Parameters:
//   - $acc_q      : list of accounts
//   - $prefs      : the fields which have to be displayed
//   - $nb_account : number of accounts
//   - $account[]  : accounts search criteria
//     keys used   : label, bank, number
///////////////////////////////////////////////////////////////////////////////
function html_account_search_list ($acc_q, $prefs, $nb_account, $account) {
  global $l_label, $l_bank, $l_number, $l_found,$display;

  $label = urlencode($account["label"]);
  $bank = urlencode($account["bank"]);
  $number = urlencode($account["number"]);

  $url = url_prepare("account_index.php?action=search&amp;tf_label=$label&amp;tf_bank=$bank&amp;tf_number=$number");

  $dis_account = new OBM_DISPLAY("DATA", $prefs, "account");
  $dis_account->data_set = $acc_q;
  $dis_account->data_url = $url;
  $dis_account->data_header = "both";

  $display["msg"] = display_info_msg("$nb_account $l_found");
  return $dis_account->display("dis_data_account");
}


///////////////////////////////////////////////////////////////////////////////
// Parameters :
// ------------
//  $obm_q_account : information about the account
//  $p_action : action in account_index.php that brought us here
///////////////////////////////////////////////////////////////////////////////
function html_account_consult($obm_q_account, $p_action){
  // -- Themes
  global $display, $set_theme,$col_label,$col_ok;
  // - Labels
  global $l_account, $l_update,$l_delete,$l_insert;
  global $col_balance_neg, $col_balance_pos;
  global $l_label,$l_comment,$l_bank, $l_number, $l_balance,$l_compute_balance;
  global $auth, $perms_user,$l_perms_user;
  global $border, $cellspacing;

  $label = $obm_q_account->f("account_label");
  $number = $obm_q_account->f("account_number");
  $today = $obm_q_account->f("account_today");
  $bank = $obm_q_account->f("account_bank");
  $comment = nl2br($obm_q_account->f("account_comment"));

  // $col_balance = ($obm_q_account->f("account_balance")>0) ? $col_balance_pos:$col_balance_neg;

  $display["title"] = "<div class=\"title\">$l_account : $label</div>";
  
  // number and label
  $block .= "
    <div class=\"detailHead\">$l_account</div>

    <table class=\"detail\">
     <tr>
      <td class=\"detailLabel\">$l_label</td>
      <td class=\"detailText\">$label</td>
     </tr>
     <tr>
      <td class=\"detailLabel\">$l_number</td>
      <td class=\"detailText\">$number</td>
     </tr>
     <tr>
      <td class=\"detailLabel\">$l_bank</td>
      <td class=\"detailText\">$bank</td>
     </tr>
     <tr>
      <td class=\"detailLabel\">$l_balance</td>
      <td class=\"detailText\">$today</td>
     </tr>
     <tr>
      <td class=\"detailLabel\">$l_comment</td>
      <td class=\"detailText\">$comment</td>
     </tr>
    </table>";

//   // we display buttons only if we need them :
//   if ($p_action == "detailconsult") {
//     // update/ compute balance of the account
//     $block .= "
//      <table border=$border cellspacing=$cellspacing width=\"100%\">
//       <tr><td align=\"center\" valign=\"top\">";
//     // if ($auth->auth["perm"] != $perms_user) {
//     $block .= "
//        <form method=\"post\" action=\"\">";
//     $url_upd = url_prepare("account_index.php?action=detailupdate".
// 			   "&amp;param_account=".
// 			   $obm_q_account->f("account_id"));
//     $url_comp = url_prepare("account_index.php?action=compute_balance".
// 			   "&amp;param_account=".
// 			   $obm_q_account->f("account_id"));
    
//     $block .= " 
//         <input type=\"button\" value=\"$l_update\" ".
//       "onClick=\"this.form.action='$url_upd' ; this.form.submit ();\">";

//     // compute the balance 
//     // useless ?
// //        <input type=hidden name=hd_balance value=\"".
// //      $obm_q_account->f("account_balance")."\">
//     $block .= "
//       </td><td align=\"center\" valign=\"top\">
//         <input type=\"button\" value=\"$l_compute_balance\" ".
//       "onClick=\"this.form.action='$url_comp' ; this.form.submit ();\">
//        </form>
//       </td></tr>
//      </table>";
//     //}
//     $block .= "
//     </td></tr>";
//   }
//   $block .= "
//    </table>
//   </center>";
  return $block;

}


///////////////////////////////////////////////////////////////////////////////
// Display Account Form              
// Parameters:
//  $obm_q_account : if updating, contains the account current info
//  $action        : action 
///////////////////////////////////////////////////////////////////////////////
function html_account_form($obm_q_account, $action) {
  // - Themes :
  global $set_theme,$col_label,$col_ok,$ico_company,$ico_contact;
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
    $balance = $obm_q_account->f("account_balance");
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

  if ($action=="detailupdate"){
    $action = ($q_payments->nf() != 0) ? "detailupdate_nodel" : $action;

    $dis_button = "
      <input type=\"hidden\" name=\"param_account\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"submit\" name=\"\" value=\"$l_update\" />
    ";
  } else {
    $dis_button = "
      <input type=\"hidden\" name=\"param_account\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" name=\"\" value=\"$l_insert\" />
    ";
  }

  //////////////////////////////////////

//   if ($action=="detailupdate"){//modifier/supprimer
//     $action = ($q_payments->nf() != 0)) ? "detailupdate_nodel" : $action;

//     $url_delete = url_prepare("account_index.php?action=delete&amp;param_account=$id");
//     $url_update = url_prepare("account_index.php?action=update&amp;param_account=$id");
//     $block .= "
//             <table border=\"$border\" cellspacing=\"$cell_spacing\"><tr><td>
//              ">
//             </td><td>";
//     //    $block .= "
//     //            <form method=post name=form_account_delete onSubmit=\"if(valider_suppression()) return true; else return false;\" action=\"$url_delete\">";
//     // deletion is impossible if there are payments connected to
//     // this account :
//     if (($action == "detailupdate") &&  {
//       $block .= "
//              <input type=\"button\" value=\"$l_delete\" disabled>
//              </td></tr>
//             </table>"; 
//     } else {
//       $block .= "
//              <input type=\"button\" value=\"$l_delete\" ".
// 	"onClick=\" if (valider_suppression ()) { ".
// 	" this.form.action='$url_delete'; this.form.submit () ; ".
// 	"}\">
//              </td></tr>
//             </table>"; 
//     }
//   } else{ // inserer : 
//     $url_insert = url_prepare("account_index.php?action=insert");
//     $block .= "
//           <input type=\"button\" value=\"$l_insert\" ".
//       " onClick=\"if (check_cpte (this.form)) {".
//       "this.form.action='$url_insert';this.form.submit();".
//       "}\">";
//   }  
////////////////////////////////////////

  $block .= "
   <form method=\"post\" name=\"f_entity\" action=\"account_index.php\"
         onsubmit=\"if (check_cpte(this)) return true; else return false;\" >
    <table class=\"detail\">
     <tr>
      <td class=\"detailLabel\">$l_label</td>
      <td class=\"detailForm\"><input name=\"tf_label\" size=\"40\" value=\"$label\"></td>
     <tr>
      <td class=\"detailLabel\">$l_number</td>
      <td class=\"detailForm\"><input name=\"tf_number\" size=\"12\" value=\"$number\"></td>
     </tr>
      <td class=\"detailLabel\">$l_initial_balance</td>
      <td class=\"detailForm\"><input name=\"tf_balance\" size=\"20\" value=\"$balance\" $bal_state></td>
     </tr>
     </tr>
      <td class=\"detailLabel\">$l_bank</td>
      <td class=\"detailForm\"><input name=\"tf_bank\" size=\"60\" value=\"$bank\"></td>
     </tr>
     </tr>
      <td class=\"detailLabel\">$l_comment</td>
      <td class=\"detailForm\"><textarea name=\"ta_comment\" rows=\"6\" cols=\"72\">$comment</textarea></td>
     </tr>
    </table>

    <div class=\"detailButton\">
     <p class=\"detailButtons\">
      $dis_button
     </p>
    </div>
   </form>
  ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the screen used to change the display parameters of accounts
// Parameters :
//   - $prefs   : Account Display preferences
//   - $prefs_p : Payment Display preferences
///////////////////////////////////////////////////////////////////////////
function dis_account_display_pref ($prefs, $prefs_p) {
  global $l_account_options, $l_payment_options;
  global $set_theme;

  $block .= "<center>\n";
 
 $block .= "<table><tr valign=\"top\">";
   $block .= "<td>";
 
  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs, "account");
  $dis_pref->pref_title = $l_account_options;
  $dis_pref->pref_dis_help = 0;
  $dis_pref->display();

  $block .= "</td><td>";

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs_p, "account", "payment");
  $dis_pref->pref_title = $l_payment_options;
  $dis_pref->pref_dis_help = 0;
  $block .= $dis_pref->display ();

  $block .=  "</td></tr></table>";
  $dis_pref->dis_pref_help ();

  $block .=  "</center>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// displays an error message when 
// an account connected to payments is being deleted 
///////////////////////////////////////////////////////////////////////////////
function html_impossible_deletion ($p_account_id, $q_related_payments) {
  global $l_impossible_deletion, $l_cancel;

  $block .= "
   <center>
    <font color=\"#$FF00FF\">".$q_related_payments->nf()." ".$l_impossible_deletion." : </font><br>";
  while ($q_related_payments->next_record()) {
    $block .= "
     <a href=\"payment_index.php?action=detailconsult&amp;param_payment=".$q_related_payments->f("payment_id")."\">".$q_related_payments->f("payment_label") . "</a><br>\n";
  }
  
  $block .= "
     <br><br>
     <form name=form_cancel_delete method=post action=\"".url_prepare("account_index.php?action=index")."\">
      <input type=submit name=\"sub_cancel_del\" value=\"$l_cancel\">
     </form>
     <center>";
  
  return $block;

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
  global $l_expected_payments, $l_payments, $l_totals,$display;

  // if $p_date == "", we use today :
  $p_date = ($p_date =="") ? date("Y-m-d") : $p_date;

  // let's compute balance now : 
  $data = account_compute_balance ($p_account["id"], $p_date);
  $q_payments = $data["payments"];
  $q_expected = $data["expected"];
  $nb = $q_expected->nf();
  $col_balance = ($data["balance"] > 0) ? $col_balance_pos:$col_balance_neg;
  $col_today   = ($data["today"] > 0) ? $col_balance_pos:$col_balance_neg;

  // where we go when user clicks on compute :
  $url = url_prepare("account_index.php?action=compute_balance&amp;param_account=".$p_account["id"]);

  // we display all account data :
  $q_account = run_query_detail ($p_account["id"]);
  html_account_consult ($q_account, "compute_balance");

  // the form allowing a date input :
  $block .= "<br><br>";
  $url = url_prepare("account_index.php?action=compute_balance&amp;param_account=".$p_account["id"]);
  $block .= "
<center>
 <table border=\"$border\" cellspacing=\"$cellspacing\">
  <form method=\"post\" action=\"$url\"
  onsubmit=\"return check_balance_form (this);\">
   <tr><td>
    <font size=-2 color=\"#$col_label\">$l_choose_balance_date</font><br>
      <script>calendar('tf_balance_date','$p_date')</script>
   </td><td>
    <input type=submit value=\"".$l_compute_balance."\">
   </td></tr>
  </form>
 </table>
</center>
 <br><br><hr>";
     
  // we display the result :
  $block .= "
<center>
 <table border=\"$border\" cellspacing=\"$cellspacing\" cellpadding=\"5\">
  <tr><td>
   &nbsp;";
  $block .= "
  </td><td>
   <font color=\"#$col_label\">$l_in_total</font>
  </td><td>
   <font color=\"#$col_label\">$l_out_total</font>
  </td></tr>";
  // first line : expected payments :
  $block .= "
  <tr><td>
   <font color=\"#$col_label\">$l_expected_payments</font>
  </td><td align=\"right\">
   <font color=\"#$col_balance_pos\">".$data["in_expected"]."</font>
  </td><td align=\"right\">
   <font color=\"#$col_balance_neg\">".$data["out_expected"]."</font>";
  // the payments already paid :
  $block .= "
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
  $block .= "
  <tr><td>
   <font color=\"#$col_label\">$l_totals</font>
  </td><td align=\"right\">
   <font color=\"#$col_balance_pos\">".$total_in ."</font>
  </td><td align=\"right\">
   <font color=\"#$col_balance_neg\">".$total_out."</font>
  </td></tr>";
  // balance :
  $block .= "
  <tr><td>
   <font color=\"#$col_label\">$l_total</font>
  </td><td colspan=\"2\" align=\"center\">
   <font color=\"#$col_balance\">".$data["balance"]."</font>
  </td></tr>
 </table>
</center><br><br>";

  /*  $block .= "
<center>
 <table border=\"$border\" cellspacing=\"$cellspacing\" cellpadding=\"5\">
  <tr><td colspan=2>
   <font color=\"#$col_label\"><b>". 
    $nb."</b> ".  
    $l_nb_expected_used ."</font><br>  
  </td></tr>";  
  // today balance 
  $block .= "
  <tr><td>
   <font color=\"#$col_label\">$l_old_balance</font>
  </td><td>
   <font color=\"#$col_today\">".$data["today"]."</font>
  </td></tr>";
  // total amount of incoming on the compute period
  $block .= "
  <tr><td>
   <font color=\"#$col_label\">$l_in_total</font>
  </td><td> 
   <font color=\"#$col_balance_pos\">".$data["in"]."</font>
  </td></tr>";
  // total amount of outgoing on the compute period
  $block .= "
  <tr><td>
   <font color=\"#$col_label\">$l_out_total</font>
  </td><td>
   <font color=\"#$col_balance_neg\">".$data["out"]."</font>
  </td></tr>";
  // the final balance we all are waiting for :
  $block .= "
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
			      "&amp;param_account=".$p_account["id"]);

    $dis_expect  = new OBM_DISPLAY ("DATA", $payments_options, "account");
    $dis_expect->data_set = $q_expected;
    $dis_expect->data_header = "top";
    $dis_expect->data_url = $url_expect;
    $dis_expect->data_order = false;

    $display["msg"] = display_ok_msg ($l_used_expected_payments);
    $block .= $dis_expect->display ();
  }
  // operations  detail display
  if ($q_payments->nf() != 0) { 
    $q_payments->seek(0);
    $url_done = url_prepare ("account_index.php?action=compute_balance".
			      "&amp;param_account=".$p_account["id"]);

    $dis_done  = new OBM_DISPLAY ("DATA", $payments_options, "account");
    $dis_done->data_set = $q_payments;
    $dis_done->data_header = "top";
    $dis_done->data_url = $url_done;
    $dis_done->data_order = false;

    $display["msg"] = display_ok_msg ("<br>".$l_used_payments);
    $block .= $dis_done->display (); 
  }
  return $block;

}

?>
