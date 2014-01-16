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
 * Class used to query the BackupEntity module of obm-satellite
 * call the restoreentity command
 */
class OBM_Satellite_RestoreEntity extends OBM_Satellite_Query {
  public static $restoreData = array('mailbox','calendar','contact','all');
  protected $data;

  /**
   * build the url to query from the given arguments
   * data to restore can be : mailbox, contact, calendar
   * none specified to restore all
   * @param  array     $args   (used keys: host, entity, login, realm, data)
   * @access protected
   * @return string
   **/
  protected function buildUrl($args) {
    extract($args);   //create $host, $entity, $login, $realm and $data
    $this->data = $data;
    $port = OBM_Satellite_ICredentials::$port;
    if (!empty($data) && $data!='all')
      return "https://{$host}:{$port}/restoreentity/{$entity}/{$login}@{$realm}/{$data}";
    //else
    return "https://{$host}:{$port}/restoreentity/{$entity}/{$login}@{$realm}";
  }

  /**
   * Allow to personalize http request options
   * @param  array     $options    editable array
   * @access protected
   **/
  protected function addHeaders(&$options) {
    $options[CURLOPT_POST] = TRUE;
  }

  /**
   * Prepare the query with the given data
   * @param  array     $data   (used keys: report, sendMail, email, backupFile)
   * @access protected
   * @return string    the query body or null if no body
   **/
  protected function buildBody($data) {
    $template =
    '<obmSatellite module="backupEntity">
      <options>
      </options>
    </obmSatellite>';
    $sxml = new SimpleXMLElement($template);

    //report
    if ($data['report']) {
      $report = $sxml->options->addChild('report');

      //sendMail
      $sendMail = $data['sendMail'] ? 'true' : 'false';
      $report->addAttribute('sendMail',$sendMail);

      //email
      $email = $data['email'];
      if (!empty($email) && !is_array($email)) {
        $email = array($email);
      }
      if (!empty($email)) {
        foreach ($data['email'] as $email) {
          $report->addChild('email',$email);
        }
      }
    }

    //file to restore
    if (isset($data['backupFile']))
      $sxml->addChild('backupFile',$data['backupFile']);

    return $sxml->asXML();
  }

  /**
   * Parse the response xml body to return it under an easily usable form
   * @param string $xml   The response body
   * @access protected
   * @return mixed
   **/
  protected function parseResponse($xml) {
    $sxml = new SimpleXMLElement($xml);
    $return = array();

    if (empty($this->data) || $this->data=='all' || $this->data=='calendar') {
      $calendar = $sxml->calendar;
      if ($calendar) {
        $return['calendar'] = $this->putToTmpFile(base64_decode((string)$calendar));
      }
    }

    if (empty($this->data) || $this->data=='all' || $this->data=='contact') {
      $privateContact = $sxml->privateContact;
      if ($privateContact) {
        $return['privateContact'] = array();
        foreach ($privateContact->addressBook as $addBook) {
          $addBookName = (string)$addBook['name'];
          $return['privateContact'][$addBookName] = $this->putToTmpFile(base64_decode((string)$addBook));
        }
      }
    }

    return $return;
  }

  /**
   * Put the given string into a new tempfile and return the file descriptor or false if string is empty
   * @param string $s
   * @access protected
   * @return int       file descriptor
   **/
  protected function putToTmpFile($s) {
    $strlen = strlen($s);
    if (!$strlen)
      return false;
    $temp = tmpfile();
    for ($length=0; $length<$strlen; $length+=fwrite($temp, substr($s,$length)));
    rewind($temp);
    return $temp;
  }

}

