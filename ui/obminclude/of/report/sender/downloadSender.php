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
 * Sender used to propose a download to the client
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2009 Aliasource - Groupe LINAGORA
 * @author Vincent Alquier <vincent.alquier@aliasource.fr> 
 * @license GPL 2.0
 */
class DownloadSender extends Sender {
  const context = 'web';

  /**
   * Propose a download to the client
   *
   * @param mixed $report report message
   * @param mixed $name report command name
   * @access protected
   * @return void
   */
  protected function sendMessage($report, $name) {
    header('Content-Description: File Transfer');
//    header('Content-Type: application/octet-stream');
    header('Content-Type: text/plain');
    header('Content-Disposition: attachment; filename=report.txt');
//    header('Content-Transfer-Encoding: binary');
    header('Expires: 0');
    header('Cache-Control: must-revalidate, post-check=0, pre-check=0');
    header('Pragma: public');
    header('Content-Length: '.strlen($report));
    ob_clean();
    flush();
    echo $report;
  }

}
