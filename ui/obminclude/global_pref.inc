<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : global_pref.inc                                             //
//     - Desc  : Global preference OBM include file                          //
// 2002-07-31 Mehdi Rande                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

if ($set_lang == "") $set_lang = $set_lang_default;
if ($set_theme == "") $set_theme = $set_theme_default;
if ($set_display == "") $set_display = $set_display_default;
if ($set_rows == "") $set_rows = $set_rows_default;
if ($set_debug == "") $set_debug = $set_debug_default;

if ($last_company=="") $last_company=$last_company_default;
if ($last_deal=="") $last_deal=$last_deal_default;
if ($last_contact=="") $last_contact=$last_contact_default;
if ($last_account=="") $last_account=$last_account_default;
if ($last_invoice=="") $last_invoice=$last_invoice_default;
if ($last_payment=="") $last_payment=$last_payment_default;
if ($last_contract=="") $last_contract=$last_contract_default;
if ($last_incident=="") $last_incident=$last_incident_default;

///////////////////////////////////////////////////////////////////////////////
// Inclusion of the correct Theme.                                           //
///////////////////////////////////////////////////////////////////////////////

if ($menu == "")
  $path = "";
else 
  $path = "../";

$theme_file = "$obminclude/themes/$set_theme/theme.inc";
if (file_exists("$path/../$theme_file")) {
  include("$theme_file");
} else {
  include ("$obminclude/themes/$set_theme_default/theme.inc");
}

///////////////////////////////////////////////////////////////////////////////
// Inclusion of the correct Language File, depending from which module this  //
// page is called and the selected language.                                 //
///////////////////////////////////////////////////////////////////////////////

$lang_file = "$obminclude/lang/$set_lang/global.inc";
if (file_exists("$path/../".$lang_file)) 
{
  include("$lang_file");
}

if ($menu == "ADMIN") {
  $lang_file = "$obminclude/lang/$set_lang/admin.inc";
  if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  } else {
    include("$obminclude/lang/$set_lang_default/admin.inc");
  }
}
else if ($menu == "COMPANY") {
  $lang_file = "$obminclude/lang/$set_lang/company.inc";
  if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  } else {
    include("$obminclude/lang/$set_lang_default/company.inc");
  }
}
else if ($menu == "CONTACT") {
  $lang_file = "$obminclude/lang/$set_lang/contact.inc";
  if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  } else {
    include("$obminclude/lang/$set_lang_default/contact.inc");
  }
}
else if ($menu == "DEAL") {
  $lang_file = "$obminclude/lang/$set_lang/deal.inc";
  if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  } else {
    include("$obminclude/lang/$set_lang_default/deal.inc");
  }
}
else if ($menu == "LIST") {
  $lang_file = "$obminclude/lang/$set_lang/list.inc";
  if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  } else {
    include("$obminclude/lang/$set_lang_default/deal.inc");
  }
}
else if ($menu == "USER") {
  $lang_file = "$obminclude/lang/$set_lang/user.inc";
  if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  } else {
    include("$obminclude/lang/$set_lang_default/user.inc");
  }
}
else if ($menu == "TIME") {
  $lang_file = "$obminclude/lang/$set_lang/time.inc";
  if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  } else {
    include("$obminclude/lang/$set_lang_default/time.inc");
  }
}
else if ($menu == "COMPUTER") {
  $lang_file = "$obminclude/lang/$set_lang/computer.inc";
  if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  } else {
    include("$obminclude/lang/$set_lang_default/computer.inc");
  }
}
else if ($menu == "SETTINGS") {
  $lang_file = "$obminclude/lang/$set_lang/settings.inc";
   if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  } else {
    include("$obminclude/lang/$set_lang_default/settings.inc");
  }
}
else if ($menu == "AGENDA") {
  $lang_file = "$obminclude/lang/$set_lang/agenda.inc";
   if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  } else {
    include("$obminclude/lang/$set_lang_default/agenda.inc");
  }
}
else if ($menu == "ACCOUNT") {
  $lang_file = "$obminclude/lang/$set_lang/account.inc";
   if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  } else {
    include("$obminclude/lang/$set_lang_default/account.inc");
  }
}
else if ($menu == "INVOICE") {
  $lang_file = "$obminclude/lang/$set_lang/invoice.inc";
   if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  } else {
    include("$obminclude/lang/$set_lang_default/invoice.inc");
  }
}
else if ($menu == "PAYMENT") {
  $lang_file = "$obminclude/lang/$set_lang/payment.inc";
   if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  } else {
    include("$obminclude/lang/$set_lang_default/payment.inc");
  }
}
else if ($menu == "CONTRACT") {
  $lang_file = "$obminclude/lang/$set_lang/contract.inc";
   if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  } else {
    include("$obminclude/lang/$set_lang_default/contract.inc");
  }
}
else if ($menu == "INCIDENT") {
  $lang_file = "$obminclude/lang/$set_lang/incident.inc";
   if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  } else {
    include("$obminclude/lang/$set_lang_default/incident.inc");
  }

}


///////////////////////////////////////////////////////////////////////////////
// Inclusion of the correct Menu (function generate_menu($menu) in menu.inc) //
///////////////////////////////////////////////////////////////////////////////
$menu_include_file = "$obminclude/themes/$set_theme/menu.inc";
include("$menu_include_file");


// List Export Formats (use theme info so after theme inclusion)
// Format 0 : HTML Stickers
$c_export_format[0]["template"] = false;
$c_export_format[0]["url"] = "addresses.php?fmt=htm&amp;form=label";
$c_export_format[0]["icon"] = $ico_html;
// Format 1 : HTML Table
$c_export_format[1]["template"] = false;
$c_export_format[1]["url"] = "addresses.php?fmt=htm&amp;form=table";
$c_export_format[1]["icon"] = $ico_html;
// Format 2 : RTF Stickers
$c_export_format[2]["template"] = false;
$c_export_format[2]["url"] = "addresses.php?fmt=rtf&amp;form=label";
$c_export_format[2]["icon"] = $ico_rtf;
// Format 3 : Excel data
$c_export_format[3]["template"] = true;
$c_export_format[3]["url"] = "addresses.php?fmt=xls&amp;form=table";
$c_export_format[3]["icon"] = $ico_excel;




