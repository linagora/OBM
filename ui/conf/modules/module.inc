<?php
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


/*
This file let you define :
- hook functions called by obm
- 'custom' validation and replacement rules for some modules insert/update action. This rules will be parsed *before* obm standards rules.
The standards rules will be parsed anyway, this feature let you add rules, not replace the standards ones.
To activate this feature you must set the 'conf_modules' to true in obm_conf.inc
This file should be named after the module to check's name. For example to 
define custom rules for the user module, create a file user.inc in this
repository
*/

// Hook functions -------------------------------------------------------------

function hook_pre_check_contact_update_rights($params, $c='') {
  global $actions, $perm, $err, $cright_write_admin;

  echo "Hello Hook world";

  return true;
}

// Rules handling -------------------------------------------------------------

$rules = array (
  /* 
  'fieldname' must be the name of the key of the field to check in
  OBM params framework. To find the field key, you can activate param's debug
  in obm configuration or take a look at the html source and remove the prefix
  to the fieldname. For example : 'tf_lastname' field key will be 'lastname'
  */
  "fieldname" => array( 
    /* 
    Field max size, if this size is exceeded, obm will not throw an error,
    but will truncate the field. To define a error on max size exceeded, go to
    regexp section
    */
    "length" => 40,
    /*
    Set replacement regext. Replacement must be a associatve array, key is the
    search pattern, it can be a regexp put it always be enclosed by /(key)+/g,
    value is the replace string. The replacements will be execute one after the
    other, so 'a' => 'b', 'b' => 'c' is the same as 'a|b' => 'c'.
    */
    "replacement" => array (
      "'" => "-",
      "\s" => "",
      "_" => "."
    ), 
    /*
    Callback function. It can be a php or an userdefined function, but it's
    this is case sensitive. The function must accept only one (or zero) 
    argument. 'callback' can be a string or an array of string.
    */
    "callback" => "strtoupper", // "callback" => array('strtoupper','trim');
    /*
    Error message, this can be a string or an array, if only one string is
    defined, it will be the default error message, else the array must be
    an associative array with the key being the message id and the value the
    message content. The 'default' key will set the default error message.

    */
    "message" => "Error", // "message"=>array('default'=>'Error!','empty' => 'Empty!')
    /*
    Validation regexp, validation will be done after 'replacement', 'length'
    and 'callback' application. If field value does not match this regexp, it will
    throw an error. This can be an string or an array. If it's a string the error
    message will be the default one, else it must be an associative array with the
    key being the regexp id, and the value the regexp pattern. If an error is thrown
    the message will be the one with the same id than the regexp (if doesn't exist,
    the default on will be used). The regexp will be executed in the set order, if
    one throw an error the second one will not be tested.
    */
    "regexp" => "/^[ a-z0-9A-Z\-]*$/"// 'regexp'=>array('empty'=>'/^.+$/','valid'=>'/^[a-z]*$/')
  )
);

?>
