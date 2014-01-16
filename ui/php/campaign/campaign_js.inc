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



///////////////////////////////////////////////////////////////////////////////
// OBM - File : php/campaign/campaign_js.php
//     - Desc : campaign JS File
// 2008-02-11 Christophe Liou Kee On
///////////////////////////////////////////////////////////////////////////////
// $Id:  $ //
///////////////////////////////////////////////////////////////////////////////

require("obminclude/javascript/check_js.inc");

  $extra_js .= "

var fx_slides = [];

function init_fx_slide (nameid) {
  //~ fx_slides[nameid] = new Fx.Slide(\$('slide_detail_' + nameid), {duration: 50,wait:false});
  //~ fx_slides[nameid].hide();
  myslide = new Fx.Slide(\$('slide_detail_' + nameid), {duration: 50,wait:false});
  myslide.hide();
  \$('slide_detail_' + nameid).setStyle('display','block');
  \$('slide_link_' + nameid).addEvent('click', function(e){
    myslide.toggle();
  });
}

";

// to avoid a javascript error when ext_widget is not defined
if ($params["ext_widget"] != "") {
  $extra_js .= "

/*
function check_campaign_get_id(valeur,text) {
    if ((valeur < 1) || (valeur == null)) {
      alert (\"$l_j_select_campaign\");
      return false;
    } else {
      window.opener.document.$params[ext_widget].value=valeur;
      window.opener.document.$params[ext_widget_text].value=text;
      if ('$params[restriction_calendar]' == 'calendar') {
        window.opener.location=('../calendar/calendar_index.php?date=$params[date]&campaign_id='+valeur+'&new_campaign=1');
      }
      window.close();
      return true;
    }
}
*/
";
}

if ($params["ext_element"] != "") {
  require_once("$obminclude/of/of_select.inc");
}

?>