<?php
/******************************************************************************
Copyright (C) 2011-2012 Linagora

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
 * @param $btn_class CSS class given to the button itself
 * @param $confirm_msg if set to a string, a JS confirm popup will be displayed
 * @access public
 * @return string
 */
function button_to($label, $url, $btn_class = false, $confirm_msg = false)
{
    if (is_string($confirm_msg)) {
      $confirm_msg = " onclick=\"return confirm('".phpStringToJsString($confirm_msg)."');\"";
    }
    if (is_string($btn_class)) {
      $btn_class = " class=\"$btn_class\"";
    }
    return "<form method=\"post\" action=\"$url\" class=\"buttonTo\">
        <div>
          <input type=\"submit\" value=\"$label\"{$btn_class}{$confirm_msg} />
        </div>
      </form>";
}

function wd_remove_accents($str, $charset='utf-8')
{
    $str = htmlentities($str, ENT_NOQUOTES, $charset);

    $str = preg_replace('#\&([A-za-z])(?:acute|cedil|circ|grave|ring|tilde|uml)\;#', '\1', $str);
    $str = preg_replace('#\&([A-za-z]{2})(?:lig)\;#', '\1', $str); // pour les ligatures e.g. '&oelig;'
    $str = preg_replace('#\&[^;]+\;#', '', $str); // supprime les autres caractères

    return $str;
}

// Case insensitive sort, correct acute letters sort
function wd_unaccent_compare_ci($a, $b)
{
    return strcmp(strtolower(wd_remove_accents($a)), strtolower(wd_remove_accents($b)));
}

function get_localized_countries_array() {
  $countries = include(dirname(__FILE__)."/../lib/Stato/i18n/data/countries/".SI18n::get_locale().".php");
  if (is_array($countries)) uasort($countries, 'wd_unaccent_compare_ci');
  return $countries;
}

function get_localized_country($code) {
  $countries = include(dirname(__FILE__)."/../lib/Stato/i18n/data/countries/".SI18n::get_locale().".php");
  return $countries[$code];
}
