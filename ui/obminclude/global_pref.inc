<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : global_pref.inc                                             //
//     - Desc  : Global preference OBM include file                          //
// 2002-07-31 Mehdi Rande                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
$sess->setcookie();
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

if ($menu != "") {
  $lang_file = "$obminclude/lang/$set_lang/".strtolower($menu).".inc";
  if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  } else {
    include("$obminclude/lang/$set_lang_default/".strtolower($menu).".inc");
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




