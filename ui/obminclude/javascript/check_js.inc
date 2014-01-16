<script language="php">
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
