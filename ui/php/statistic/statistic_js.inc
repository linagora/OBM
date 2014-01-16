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
// OBM - File  : contact_js.inc                                              //
//     - Desc  : Contact javascript functions File                           //
// 2000-01-13 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$extra_js .= "
 
NS4 = (document.layers) ? 1 : 0;
IE4 = (document.all) ? 1 : 0;
W3C = (document.getElementById) ? 1 : 0;	
function show ( evt, name ) {
  if (IE4) {
    evt = window.event;  
  }

  var currentX,		
      currentY,	
      x,
      y,
      docWidth,
      docHeight,
      layerWidth,
      layerHeight,
      ele;	

  if ( W3C ) {
    ele = document.getElementById(name);
    currentX = evt.clientX,
    currentY = evt.clientY;
    docWidth = document.width;
    docHeight = document.height;
    layerWidth = ele.style.width;
    layerHeight = ele.style.height;
  } else if ( NS4 ) {
    ele = document.layers[name];
    currentX = evt.pageX,
    currentY = evt.pageY;
    docWidth = document.width;
    docHeight = document.height;
    layerWidth = ele.clip.width;
    layerHeight = ele.clip.height;

  } else {
    ele = document.all[name];
    currentX = evt.clientX,
    currentY = evt.clientY;
    docHeight = document.body.offsetHeight;
    docWidth = document.body.offsetWidth;
    layerWidth = 200;
    layerHeight = ele.offsetHeight;
  }

  if ( ( currentX) > docWidth ) {
    x = ( currentX - layerWidth );
  }
  else {
    x = currentX;
  }
   x = x - 196;
  y = currentY + 20;
  y = y - 150;
  if ( IE4 ) {
    x += document.body.scrollLeft;
    y += document.body.scrollTop;
  } else {
    x += window.pageXOffset;
    y += window.pageYOffset;
  }

  if ( NS4 ) {
    ele.right = parseInt ( x );
    ele.top = parseInt ( y );
    ele.visibility = \"show\";
  } else {  // IE4 & W3C
    ele.style.position = 'absolute'
    ele.style.left = x+'px';
    ele.style.top = y+'px';
    ele.style.display = \"block\";
  }
}

function hide ( name ) {
  if (W3C) {
    document.getElementById(name).style.display = \"none\";
  } else if (NS4) {
    document.layers[name].display = \"none\";
  } else {
    document.all[name].style.display = \"none\";
  }
}

";

?>