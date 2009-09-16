<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2009 OBM.org project members team                   |
 |                                                                         |
 | This program is free software; you can redistribute it and/or           |
 | modify it under the terms of the GNU General Public License             |
 | as published by the Free Software Foundation; version 2                 |
 | of the License.                                                         |
 |                                                                         |
 | This program is distributed in the hope that it will be useful,         |
 | but WITHOUT ANY WARRANTY; without even the implied warranty of          |
 | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
 | GNU General Public License for more details.                            |
 +-------------------------------------------------------------------------+
 | http://www.obm.org                                                      |
 +-------------------------------------------------------------------------+
*/

/**
 * Generates a form containing a single button that submits to the $url...
 * 
 * ... because THOU SHALT NOT USE LINKS FOR ACTIONS THAT TRIGGER DATA CHANGES !!!
 * 
 * The generated form element has a class name of buttonTo to allow 
 * styling of the form itself and its children.
 * 
 * @param $label button label
 * @param $url form action url
 * @param $confirm_msg if set to a string, a JS confirm popup will be displayed
 * @access public
 * @return string
 */
function button_to($label, $url, $confirm_msg = false)
{
    if (is_string($confirm_msg)) {
      $confirm_msg = "onclick=\"return confirm('".phpStringToJsString($confirm_msg)."');\"";
    }
    return "<form method=\"post\" action=\"$url\" class=\"buttonTo\">
        <div>
          <input type=\"submit\" value=\"$label\"$confirm_msg />
        </div>
      </form>";
}