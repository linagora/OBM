<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : JavaScript Embedded in PHP                               //
//     - File     : check_js.inc                                             //
// 1999-12-30 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// Fonctions JAVASCRIPT utilitaires et generiques (trim(),...)               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// trim(string) function : Removes the leading and trailing spaces and tabs of
// a string
// return the trimmed string
$extra_js .= "
function trim(str_data) {
  var str_trimmed = String(str_data);

  str_trimmed = str_trimmed.replace(/(^[ |\t]+)|([ |\t]+$)/g, '');

  return str_trimmed;
}

function check_zip(zip) {
  // allow only zip code that match $regexp
  return zip.match($js_regexp_zip);
}

function check_phone(phone) {
  // allow only phone numbers that match $regexp
  return phone.match($js_regexp_phone);
}

function check_fax(fax) {
  // allow only fax numbers that match $regexp
  return fax.match($js_regexp_fax);
}

function check_email(email) {
  // allow only valid email address that match $regexp
  return email.match($js_regexp_email);
}

function check_web(web) {
  // allow only valid web address
  return web.match($js_regexp_web);
}

function check_list(list) {
  // allow only valid list name
  return list.match($js_regexp_list_name) ;
}
function check_number(number){
  // field containing only digits :
  return number.match($js_regexp_number);
}

function check_money(field){
  // field containing a money amout
  // digits and eventually a '.'
  return field.match($js_regexp_money);
}
";



