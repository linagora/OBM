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
?>
<?php
/**
 * dis_webmail_content 
 * 
 * @access public
 * @return void
 */
function dis_webmail_content() {

  return '
  <div id="obm_webmail"/>
  <script language="javascript" src="/minig/fr.aliasource.webmail.WebmailUI.nocache.js">
  </script>      
  <!-- OPTIONAL: include this if you want history support -->
  <iframe src="javascript:\'\'" id="__gwt_historyFrame" style="width:0;height:0;border:0">
  </iframe>
  ';
}
?>
