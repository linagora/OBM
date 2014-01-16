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



include_once('obminclude/of/Vcalendar.php');

/**
 * Parse an ICS File into an Vcalendar
 *
 * @package
 * @version $Id:$
 * @copyright Copyright (c) 1997-2007 Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr>
 */
class Vcalendar_Reader_ICS {

  var $handle;

  var $attribute;

  var $cns;

  var $mails;

  var $frequency = array("secondly","minutely","hourly","daily","weekly","monthly","yearly");

  var $repeat = array("bysecond","byminute","byhour","byday","bymonthday","byyearday","byweekno","bymonth","bysetpos","wkst");

  var $weekDays = array('su' => 'sunday', 'mo' => 'monday', 'tu' => 'tuesday', 'we' => 'wednesday', 'th' => 'thursday' , 'fr' => 'friday', 'sa' => 'saturday');
  /**
   * Constructor
   *
   * @param string $file ICS file name or file descriptor
   * @access public
   * @return void
   */
  function Vcalendar_Reader_ICS($file) {
    if (is_string($file)) {
      $this->handle = fopen($file, 'r');
    } else {
      $this->handle = $file;
    }
    $this->attribute = array('name' => '', 'options' => array(), 'value' => array());
    $this->cns = array();
    $this->mails = array();
  }

  function & getDocument() {
    $document = new Vcalendar();
    $document->vcalendar = &$this->parseElement($document);
    $this->close;
    return $document;
  }

  /**
   * Read an ICS Attribute and store properties in an internal value.
   *
   * @access public
   * @return boolean True if a ICS line is read, False if eof or empty line
   */
  function getAttribute() {
    $indent = 0;
    $separator = '{0}';
    while(($line = fgets($this->handle)) && preg_match('/^\s'.$separator.'(.*)$/',$line,$match)) {
      $propertyLine .= trim($match[1],"\n\r");
      $offset = ftell($this->handle);
      $separator = '';
    }
    fseek($this->handle,$offset);

    if($propertyLine == '') {
      return false;
    }

    preg_match('/^([^;:]*);?([^:]*):(.*)$/',$propertyLine, $match);
    $this->attribute = array('name' => '', 'options' => array(), 'value' => array());

    $this->attribute['name'] = trim(strtolower($match[1]));
    $this->attribute['options'] = Vcalendar_Reader_ICS::parseAttributeOptions(explode(';',$match[2]));
    $this->attribute['value'] = $match[3];

    return true;
  }

  /**
   * Parse an array of OptionName=OptionValue and store it into
   * an hash map
   *
   * @param array $options
   * @access public
   * @return hasmap of options
   */
  function & parseAttributeOptions($options) {
    $property = array();
    foreach($options as $option) {
      list($optionName, $optionValue) = explode('=', $option);
      $optionName = strtolower($optionName);
      $property[$optionName] = $optionValue;
    }
    return $property;
  }

  /**
   * Parse an array of ValueName=ValueValue and store it into
   * an hash map
   *
   * @param array $values
   * @access public
   * @return hasmap of values
   */
  function & parseAttributeValues($values) {
    $property = array();
    foreach($values as $value) {
      list($valueName, $valueValue) = explode('=', $value);
      $valueName = strtolower($valueName);
      $property[$valueName] = $valueValue;
    }
    return $property;
  }
  /**
   * Return True if the current ICS Attribute is a new ICS
   * element attribute
   *
   * @access public
   * @return boolean
   */
  function isBeginLine() {
    return ($this->attribute['name'] == 'begin');
  }

  /**
   * Return True if the current ICS Attribute is the end of ICS
   * an element
   *
   * @access public
   * @return boolean
   */
  function isEndLine() {
    return ($this->attribute['name'] == 'end');
  }

  /**
   * Search for the next ICS element beginning
   *
   * @access public
   * @return boolean True if a new element is found
   */
  function & nextElement() {
    while((!$this->isBeginLine() && ($return = $this->getAttribute())) );
    return $return;
  }

  /**
   * Parse an ICS Element and his child
   *
   * @param ICS_Document $document ICS document
   * @access public
   * @return Vcalendar_Element Parsed element
   */
  function & parseElement(&$document) {
    $r = $this->nextElement();
    $element = $document->createElement($this->attribute['value']);
    while($this->getAttribute() && !$this->isEndLine()) {
      if($this->isBeginLine()) {
        $children[] = &$this->parseElement($document);
      } else {
        $properties[] = $this->attribute;
      }
    }
    if(is_array($children)) {
      foreach($children as $child) {
        $element->appendChild($child);
        unset($child);
      }
    }
    $element->setProperties($this->parseProperties($properties));
    return $element;
  }



  /**
   * setProperties
   *
   * @access public
   * @return void
   */
  function parseProperties($properties) {
    $parse = array();
    if(is_array($properties)) {
      foreach($properties as $property) {
        $parse[] =$this->parseProperty($property['name'],$property['value'], $property['options']);
      }
    }
    return $parse;
  }
  /**
   * set
   *
   * @param mixed $name
   * @param mixed $values
   * @param mixed $options
   * @access public
   * @return void
   */
  function parseProperty($name, $value, $options) {
    $methodName = 'parse'.str_replace(' ','',ucwords(str_replace('-',' ',$name)));
    if(method_exists($this, $methodName)) {
      return array('name' => $name, 'value' => $this->$methodName($value, $options));
    } else {
      return array('name' => $name, 'value' => $this->parseText($value, $options));
    }
  }

  /**
   * Ensures the ICS uid is a valid OBM event_ext_id.
   *
   * @param int $id
   * @access public
   * @return The id or a new one if $id is invalid
   */
  function parseUid($id, $options=array()) {
    if(trim($id) == '') {
      $id = genUniqueExtEventId();
    }
    return $id;
  }

  /**
   * parseDate
   *
   * @param mixed $value
   * @param mixed $options
   * @access public
   * @return void
   */
  function parseDate($value, $options=array()) {
    if(isset($options['TZID']) && !preg_match('/^[^Z]*Z$/',$value)) {
      $date = new Of_Date($value, $options['TZID']);
      if ($date->error() == Of_Date::ERR_INVALID_DATE) { // Bad timezone
        $date = new Of_Date($value, 'GMT');
      }
    } else {
      $date = new Of_Date($value, null, false);
    }

    return $date;
  }

  /**
   * @see setDate
   * @param mixed $values
   * @param mixed $options
   * @access public
   * @return void
   */
  function parseDtstart($value,$option) {
    return $this->parseDate($value,$option);
  }

  /**
   * setDtend
   *
   * @param mixed $value
   * @param mixed $options
   * @access public
   * @return void
   */
  function parseDtend($value,$option) {
    return $this->parseDate($value,$option);
  }

  function parseTrigger($value,$option) {
    return $this->parseDate($value,$option);
  }  

  function parseCategories($value, $options) {
    return explode(',',$this->parseText($value,$options));
  }

  function parseExdate($value, $options) {
    $exdates = explode(',',$value);
    foreach($exdates as $key => $date) {
      $exdates[$key] = $this->parseDate($date,$options);
    }
    return $exdates;
  }
  
  /**
   * setRrule
   *
   * @param mixed $value
   * @param mixed $options
   * @access public
   * @return void
   */
  function parseRrule($value,$options) {
    $value = Vcalendar_Reader_ICS::parseAttributeValues(explode(';',$value));
    $rrule = array();
    $kind = strtolower($value['freq']);
    if(!in_array($kind,$this->frequency))  {
      //TODO Unsupported item. Error handler!
      return NULL;
    }
    $rrule['kind'] = $kind;
    if(is_numeric($value['interval'])) {
      $interval = $value['interval'];
    } else {
      $interval = 1;
    }
    $rrule['interval'] = $interval;
    foreach($value as $name => $repeat) {
      $repeat = strtolower($repeat);
      if(in_array($name,$this->repeat)) {
        $keys = explode(',',$repeat);
        foreach($keys as $key) {
          $rrule[$name][] = $this->weekDays[$key];
        }
      }
    }
    if(!is_null($value['count'])) {
      $rrule['count'] = $value['count'];
    } elseif(!is_null($value['until'])) {
      $rrule['until'] = $this->parseDate($value['until']);
    }
    return $rrule;
  }

  /**
   *
   * @see setTimecreate
   * @param mixed $value
   * @param mixed $options
   * @access public
   * @return void
   */
  function parseCreated($value, $options) {
    return $this->parseDate($value,$options);
  }
  
  function parseDuration($value, $options) {
    if(preg_match('/\s*P((\d*)D)?(T((\d)*H)?((\d*)M)?((\d*)S)?)?((\d*)W)?$/',$value,$match)) {
      $duration = 86400 * $match[2] + 3600 * $match[5] + 60 * $match[7] + $match[9] +604800 * $match[11] ;
    } 
    return $duration;
  }
  /**
   * @see setTimeupdate
   * @param mixed $value
   * @param mixed $options
   * @access public
   * @return void
   */
  function parseLastModified($value, $options) {
    return $this->parseDate($value,$option);
  }

  function parseOrganizer($value, $options) {
    return $this->getAttendeeId($value, $options);
  }
  /**
   * @see setTimeupdate
   * @param mixed $value
   * @param mixed $options
   * @access public
   * @return void
   */
  function parseAttendee($value, $options) {
    switch(strtolower($options['cutype'])) {
      case 'resource' :
        $entity = 'resource';
        break;
      case 'group' :
        $entity = 'group';
        break;
      case 'individual' :
        $entity = 'user';
        break;
      case 'x-task' :
        $entity = 'task';
        break;
      case NULL :
        $entity = 'user';
        break;        
      default :
        //FIXME Unsupported Attendee
        return NULL;
    }
    switch(strtolower($options['partstat'])) {
      case 'accepted' :
        $state = 'ACCEPTED';
        break;
      case 'declined' :
        $state = 'DECLINED';
        break;
      case null :
        $state = null;
        break;
      default :
        $state = 'NEEDS-ACTION';
    }

    $attendee = $this->getAttendeeId($value, $options, $entity);
    if(!is_null($attendee)) {
      return array('entity' => $entity, 'id' => $attendee, 'state' => $state);
    

   }return NULL;
  }

  function getAttendeeId($attendee, $options, &$entity='user') {
    if(!is_null($options['x-obm-id'])) {
      if(Vcalendar_Utils::entityExist($options['x-obm-id'], $entity)) return $options['x-obm-id'];
    }
    if(preg_match('/^\s*mailto\s*:\s*(([^@]*)@([^\s]*))\s*$/i',$attendee, $match)) {
      $attendee = $this->getStandardMailto($match);
    } elseif (preg_match('/^\s*(CN|MAILTO)\s*[:=]\s*([^\/]*).*$/i',$attendee, $match)) {
      $attendee =  $this->getLotusCN($match);
    }
    if(!is_null($options['cn'])) {
      $attendee['cn'] = strtolower($options['cn']);
    }

    if(is_array($attendee)) {
      if(isset($attendee['mail'])) {
        if(isset($this->mails[$attendee['mail']])) {
          return $this->mails[$attendee['mail']][$entity];
        }
        if(isset($this->mails[$attendee['email']])) {
          return $this->mails[$attendee['email']][$entity];
        }
      }
      if(isset($attendee['cn']) && isset($this->cns[$attendee['cns']])) {
        return $this->cns[$entity][$attendee['cns']];
      }

      $id = $this->getOBMId($attendee, $entity);
      // If the entity is an unknown user, assume it's a contact
      if($id === null && $entity == 'user') {
        $entity = 'contact';
        $id = $this->getOBMId($attendee, $entity);
        if($id === null) {
          return $attendee; // No luck, keep this for future contact creation
        }
      }
      return $id;
    }
    return NULL;
  }

  function getStandardMailto($match) {
    if(!preg_match($GLOBALS['php_regexp_email'], $match[1] )) {
      return false;
    }
    return array('email' => strtolower($match[1]),
                 'mail' => strtolower($match[2]),
                 'domain' => strtolower($match[3]) );
  }

  function getLotusCN($match) {
    if(!empty($matches[2])) {
      return array('cn' => strtolower($mathches[2]));
    }
    return array();
  }

  function getOBMId($attendee, $entity) {
    global $cdg_sql;

    $db = new DB_OBM;
    $db_type = $obm_q->type;
    if(!is_null($attendee['cn'])) {
      $this->cns[$entity][$attendee['cn']] = NULL;
      $cn = "OR cn = '".addslashes($attendee['cn'])."'";
    }
    if(!is_null($attendee['mail'])) {
      $this->mails[$entity][$attendee['mail']] = NULL;
      $mail = "OR mail #LIKE '%".addslashes($attendee['mail'])."%' ";
    }
    if(!$mail && !$cn) {
      return NULL;
    }
    $entityTable = $this->buildEntityQuery($entity, $db);
    if(is_null($entityTable)) {
      return NULL;
    }
    $query = 'SELECT id, mail, cn
              FROM ('.$entityTable.') as Entity WHERE (1 = 0 '.$cn.' '.$mail.') 
              AND domain_id '.sql_parse_id($GLOBALS['obm']['domain_id'], true).'
              GROUP BY id, mail, cn';
    display_debug_msg($query, $cdg_sql, 'getOBMId');
    $db->xquery($query);
    while($db->next_record()) {
      if((!is_null($attendee['cn']) && strtolower($db->f('cn')) == $attendee['cn']) ||
         preg_match_all('/^('.($attendee['mail']).'|'.$attendee['email'].')\r?$/mi',$db->f('mail'),$results)) { 
        $this->cns[strtolower($db->f('cn'))][$entity] = $db->f('id');
        $emails = get_entity_email($db->f('mail'),null,true,null);
        foreach($emails as $email) {
          $this->mails[strtolower($email)][$entity] = $db->f('id');
        } 
        return  $db->f('id');
      }
    }
    return NULL;
  }

  function buildEntityQuery($entity, &$db) {
    switch ($entity) {
      case 'group' :
        return "Select group_domain_id as domain_id, group_name as cn, group_id as id, group_email as mail, 'group' as kind FROM UGroup";
      case 'task' :
        return "SELECT ".$GLOBALS['obm']['domain_id']." as domain_id, projecttask_id as id, projecttask_label as cn, projecttask_label as mail, 'task' as kind FROM ProjectTask";
        break;
      case 'resource' :
        return "SELECT resource_domain_id as domain_id, resource_name AS cn, resource_name AS mail, resource_id AS id, 'resource' AS kind FROM Resource";
        break;
      case 'user' :
        $c_id = "userobm_id";
        $concat[0]["type"] = "field";
        $concat[0]["value"] = "userobm_firstname";        
        $concat[1]["type"] = "string";
        $concat[1]["value"] = " ";
        $concat[2]["type"] = "field";
        $concat[2]["value"] = "userobm_lastname";
        $label = sql_string_concat($db->type, $concat);
        return "SELECT userobm_domain_id as domain_id, $label as cn, userobm_id as id, userobm_email as mail, 'user' as kind FROM UserObm";
        break;
      case 'contact' :
        $concat = array ( array( 'type' => 'field',  'value' => 'contact_firstname' ),
                          array( 'type' => 'string', 'value' => ' ' ),
                          array( 'type' => 'field',  'value' => 'contact_middlename' ),
                          array( 'type' => 'string', 'value' => ' ' ),
                          array( 'type' => 'field',  'value' => 'contact_lastname' ));
        $cn = sql_string_concat($db->type, $concat);
        return "SELECT contact_id AS id, contact_domain_id AS domain_id, $cn AS cn, email_address AS mail, 'user' AS kind
                FROM Contact
                INNER JOIN ContactEntity ON contactentity_contact_id = contact_id
                INNER JOIN Email ON email_entity_id = contactentity_entity_id";
        break;
      default:
        return NULL;
    }
  }

  function parseText($text, $options) {
    if(strtolower($options['encoding']) == 'quoted-printable') {
      $text = quoted_printable_decode($text);
    }
    $text = htmlspecialchars($text);
    $text = stripcslashes($text);
    return $text;    
  }

  function parseXObmAlert($duration, $options) {
    $alert = array();
    if(!is_null($options['x-obm-id'])) {
      if(Vcalendar_Utils::entityExist($options['x-obm-id'], 'user')) $alert['user'] = $options['x-obm-id'];
    }
    $alert['duration'] = $duration;
    return $alert;
  }
}
?>

