<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2010 OBM.org project members team                   |
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

/*
 * Class used to query the BackupEntity module of obm-satellite
 * call the availablebackup command
 */
class OBM_Satellite_AvailableBackup extends OBM_Satellite_Query {

  /**
   * build the url to query from the given arguments
   * @param  array     $args   (used keys: host, entity, login, realm)
   * @access protected
   * @return string
   **/
  protected function buildUrl($args) {
    extract($args);   //create $host, $entity, $login and $realm
    $port = OBM_Satellite_ICredentials::$port;
    return "https://{$host}:{$port}/availablebackup/{$entity}/{$login}@{$realm}";
  }

  /**
   * Allow to personalize http request options
   * @param  array     $options    editable array
   * @access protected
   **/
  protected function addHeaders(&$options) {
    $options[CURLOPT_HTTPGET] = TRUE;
  }

  /**
   * Prepare the query with the given data
   * @param  array     $data   (used keys: report, sendMail, email, calendar, privateContact)
   * @access protected
   * @return string    the query body or null if no body
   **/
  protected function buildBody($data) {
    return null;
  }

  /**
   * Parse the response xml body to return it under an easily usable form
   * @param string $xml   The response body
   * @access protected
   * @return mixed
   **/
  protected function parseResponse($xml) {
    $sxml = new SimpleXMLElement($xml);
    $files = array();
    foreach ($sxml->backupFile as $file) {
      $filename = (string)$file;
      preg_match('/(\\d{4})(\\d{2})(\\d{2})-(\\d{2})(\\d{2})(\\d{2})/',substr($filename,-22,15),$d);
      $datetime = mktime($d[4],$d[5],$d[6],$d[2],$d[3],$d[1]);
      $files[] = array( 'filename' => $filename, 'datetime' => $datetime );
    }
    return $files;
  }

}

