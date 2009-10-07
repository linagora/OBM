<script language="php">
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

function check_number(number){
  // field containing only digits :
  return number.match($js_regexp_number);
}

function check_money(field){
  // field containing a money amout
  // digits and eventually a '.'
  return field.match($js_regexp_money);
}


///////////////////////////////////////////////////////////////////////////////
// Get an Element by its id
// Parameters:
//   - doc  :
//   - name : element id (or name)
///////////////////////////////////////////////////////////////////////////////
function get_elem(doc, name) {
  return doc.getElementById(name);
}


///////////////////////////////////////////////////////////////////////////////
// Remove an element line from the list form
///////////////////////////////////////////////////////////////////////////////
function remove_element(elemId,parentId) {
  content = $(elemId);
  container = $(parentId);
  try {
    container.removeChild(content);
  } catch (e) {
    try {
      content.getParent().removeChild(content);
    } catch (e) {
      // what to do if element does not exists
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Remove an element line from the list form
///////////////////////////////////////////////////////////////////////////////
function remove_all_element(parentId) {
  target = window.document;
  container = target.getElementById(parentId);
  while (container.firstChild) {
    container.removeChild(container.firstChild);
  }
}

";
</script>
