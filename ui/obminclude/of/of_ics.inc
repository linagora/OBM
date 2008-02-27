<?php

/**
 * Utilities helping ICS format performing 
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */
class ICalendar_Utils {

  /**
   * Parse an ICS File into a ICS_Document
   * 
   * @param string $file ICS file name
   * @access public
   * @return ICalendar_Document ICS Document resulting from parsing $file
   */
  function parseICS($file) {
    $reader = new ICalendar_ICS_Reader($file);
    $document = new ICalendar_Document();
    $document->vcalendar = &$reader->parseElement($document);
    $reader->setObmUsers($document);
//    $reader->setObmFlags($document); 
    $reader->close;
    return $document;
  }
  
  /**
   * Retrieve users id from ICS Properties 
   * 
   * @param array $users contained ics properties
   * @access public
   * @return array $users with an additionnal x-obm-id property
   */
  function getUidFromICSProperty($users) {
    $db = new DB_OBM;
    foreach($users as $id => $user) {
      if(!empty($user['cn'])) {
        $cns[] = "'$user[cn]'";
      }
      if(($mail = ICalendar_Utils::parseMailto($id))) {
        $mails[] = "'$mail'";
      } elseif($cn = ICalendar_Utils::parseLotusCN($id)) {
        $cns[] = "'$cn'";
      }
    }
    $concat = array (array ('type' => 'field','value' => 'userobm_firstname'),
                     array ('type' => 'string','value' => ' '),
                     array ('type' => 'field','value' => 'userobm_lastname') 
                    );
    $user_label = sql_string_concat($db->type,$concat);    

    $query = 'SELECT userobm_id as id, userobm_email as mail, '.$user_label.' as cn
              FROM UserObm WHERE userobm_email IN ('.implode(',',$mails).') OR '.$user_label.' IN ('.implode(',',$cns).')
              GROUP BY id';
    echo $query;
    $db->query($query);
    while($db->next_record()) {
      var_dump($db->Record);
    }
  }


  /**
   * Parse a MAILTO:xxx@xx.fr into xxx@xx.fr
   * or xxx depending on obm configuration
   * 
   * @param mixed $mailto 
   * @access public
   * @return mail
   */
  function parseMailto($mailto) {
    if(preg_match('/^\s*mailto\s*:\s*(([^@]*)@([^\s]*))\s*$/i',$mailto, $match)) {
      if(!preg_match($GLOBALS['php_regexp_email'], $match[1] )) {
        return false;  
      }
      if($GLOBALS['cgp_use']['service']['mail']) {
        return $match[2];
      } else {
        return $match[1];
      }
    }
    return false;
  }

  /**
   * Parse a lotus CN MAILTO:A B/X-Y@Y 
   * into A B
   * @param mixed $lotusCN 
   * @access public
   * @return CN
   */
  function parseLotusCN($lotusCN) {
    if (preg_match('/^\s*(CN|MAILTO)\s*[:=]\s*([^\/]*).*$/i',$lotusCN, $matches)) {
      if(!empty($matches[2])) {
        return $mathches[2];
      }
    }
    return false;
  }
}


/**
 * Parse an ICS File into an ICalendar_Document
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */
class ICalendar_ICS_Reader {

  var $handler;

  var $attribute;

  /**
   * Constructor 
   * 
   * @param string $file ICS file name 
   * @access public
   * @return void
   */
  function ICalendar_ICS_Reader($file) {
    $this->handle = fopen($file, 'r');
    $this->attribute = array('name' => '', 'options' => array(), 'value' => array());
  }

  /**
   * Read an ICS Attribute and store properties in an internal value. 
   * 
   * @access public
   * @return boolean True if a ICS line is read, False if eof or empty line
   */
  function getAttribute() {
    $propertyLine = '';
    $indent = 0;
    while(($line = fgets($this->handle)) && preg_match('/^\s{'.$indent.'}.*$/',$line)) {
      $propertyLine .= trim($line);
      $offset = ftell($this->handle);
      $indent++;
    }
    fseek($this->handle,$offset);
    
    if($propertyLine == '') {
      return false;
    } 

    preg_match('/^([^;:]*);?([^:]*):(.*)$/',$propertyLine, $match);   
    $this->attribute = array('name' => '', 'options' => array(), 'value' => array());
    
    $this->attribute['name'] = trim(strtolower($match[1]));
    $this->attribute['options'] = ICalendar_ICS_Reader::parseAttributeOptions(explode(';',$match[2]));
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
  function parseAttributeOptions($options) {
    $property = array();
    foreach($options as $option) {
      list($optionName, $optionValue) = explode('=', $option);
      $optionName = strtolower($optionName);
      $property[$optionName] = $optionValue;
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
  function nextElement() {
    while((!$this->isBeginLine() && ($return = $this->getAttribute())) );
    return $return;
  }

  /**
   * Parse an ICS Element and his child 
   * 
   * @param ICS_Document $document ICS document
   * @access public
   * @return ICalendar_Element Parsed element
   */
  function parseElement(&$document) {

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
    $element->setProperties($properties);
    return $element;
  }

  /**
   * Find all reference to user into the document
   * and try to map them with an obm id. 
   * 
   * @param ICS_Document $document 
   * @access public
   * @return void
   */
  function setObmUsers(&$document) {
    $users = $this->getICSIds($document->vcalendar);
    $obmIds = ICalendar_Utils::getUidFromICSProperty($users);
    //$this->setObmIds($users);
  }

  /**
   * Find all reference to users into a element 
   * 
   * @param ICalendar_Element $element 
   * @access public
   * @return array hashmap containing all reference to users 
   */
  function getICSIds(&$element) {
    $users = array();
    if(($organizer = $element->getValue('organizer')) && !$element->getOption('organizer','x-obm-id')) {
      $users[$organizer] = $element->getOptions('organizer');
    }
    if(($attendees = $element->getValue('attendee'))) {
      if(!is_array($attendees) && !$element->getOption('attendee','x-obm-id')) {
        $users[$attendees] = $element->getOptions('attendee');
      } else {
        foreach($attendees as $key => $attendee) {
          if(!$element->getOption('attendee','x-obm-id',$key)) {
            $users[$attendee] = $element->getOptions('attendee',$key);
          }
        }
      }
    }
    if(is_array($element->children)) {
      for($i=0;$i < count($element->children); $i++) {
        $users = array_merge($users, $this->getICSIds($element->children[$i]));
      }
    }
    return $users;
  }

  /**
   * Add an x-obm-id to all users reference in a element. 
   * 
   * @param ICalendar_Element $element 
   * @access public
   * @return void
   */
  function setICSIds(&$element, &$users) {
    $users = array();
    if(($organizer = $element->getValue('organizer')) && !$element->getOption('organizer','x-obm-id')) {
      $element->addOption('organizer','x-obm-id',$users[$organizer]['x-obm-id']);
    }
    if(($attendees = $element->getValue('attendee'))) {
      if(!is_array($attendees) && !$element->getOption('attendee','x-obm-id')) {
        $element->addOption('organizer','x-obm-id',$users[$attendees]['x-obm-id']);
      } else {
        foreach($attendees as $key => $attendee) {
          if(!$element->getOption('attendee','x-obm-id',$key)) {
            $element->addOption('organizer','x-obm-id',$users[$attendees]['x-obm-id'],$key);
          }
        }
      }
    }
    if(is_array($element->children)) {
      for($i=0;$i < count($element->children); $i++) {
        $this->setICSIds($element->children[$i]);
      }
    }
  }  
}

class ICalendar_ICS_Writer {
  
}

class ICalendar_OBM_Reader {

}

class ICalendar_OBM_Writer {

}

/**
 * OBM Icalendar Document  
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */
class ICalendar_Document {

  var $vcalendar;

  /**
   * Constructor
   * Create a vcalendar Element. //FIXME necessaire?
   * 
   * @access public
   * @return void
   */
  function ICalendar_Document () {
    $this->vcalendar = new ICalendar_Element($this,'vcalendar');
  }

  /**
   * Create a new ICS element 
   * 
   * @param string $name kind of element
   * @access public
   * @return ICalendar_Element
   */
  function createElement($name) {
    if(class_exists('ICalendar_'.ucfirst($name))) {
      $class = 'ICalendar_'.ucfirst($name);
      return new $class($this);
    } else {
      return new ICalendar_Element($this, strtolower($name));
    }
  }

  /**
   * Get all vevent in the document 
   * 
   * @access public
   * @return array of ICalendar_Element
   */
  function getVevents() {
    return $this->vcalendar->getElementByName('vevent');
  }
}
/**
 * ICalendar Element 
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */
class ICalendar_Element {
  
  /**
   * properties 
   * 
   * @var array
   * @access public
   */
  var $properties = array();

  /**
   * document 
   * 
   * @var mixed
   * @access public
   */
  var $document = NULL;

  /**
   * name 
   * 
   * @var mixed
   * @access public
   */
  var $name;

  /**
   * children 
   * 
   * @var array
   * @access public
   */
  var $children = array();

  /**
   * ICalendar_Element 
   * 
   * @param mixed $document 
   * @param mixed $name 
   * @access public
   * @return void
   */
  function ICalendar_Element(&$document, $name) {
    $this->document = &$document;
    $this->name = $name;
  }

  /**
   * setProperties 
   * 
   * @access public
   * @return void
   */
  function setProperties($properties) {
    if(is_array($properties)) {
      foreach($properties as $property) {
        $this->setProperty($property['name'],$property['value'], $property['options']);
      }
    }
  }

  /**
   * setProperty 
   * 
   * @param mixed $name 
   * @param mixed $values 
   * @param mixed $options 
   * @access public
   * @return void
   */
  function setProperty($name, $value, $options) {
    $methodName = 'set'.str_replace(' ','',ucwords(str_replace('-',' ',$name)));
    if(method_exists($this, $methodName)) {
      $this->$methodName($value, $options);
    } else {
      if(isset($this->$name)) {
        if(array_key_exists('value',$this->$name)) {
          $this->$name =  array($this->$name);
        }
        array_push($this->$name,array('value' => $value, 'options' => $options));
      } else {
        $this->$name = array('value' => $value, 'options' => $options);
      }
    }    
  }

  /**
   * getDocument 
   * 
   * @access public
   * @return void
   */
  function getDocument() {
    return $this->document;
  }


  /**
   * appendChild 
   * 
   * @param mixed $child 
   * @access public
   * @return void
   */
  function appendChild(&$child) {
    $this->children[] = &$child;
  }

  /**
   * getElementByName 
   * 
   * @param mixed $name 
   * @param mixed $recursive 
   * @access public
   * @return void
   */
  function getElementByName($name, $recursive=false) {
    $elements = array();
    foreach($this->children as $child) {
      if($child->name == $name) {
        array_push($elements,$child);
      }
      if($recursive) {
        $return = $child->getElementbyName($name, $recursive);
        $elements = array_merge($elements,$return);
      }
    }
    return $elements;
  }

  /**
   * parseDate 
   * 
   * @param mixed $value 
   * @param mixed $options 
   * @access public
   * @return void
   */
  function parseDate($value, $options) {
    preg_match('/.*(\d{4})(\d{2})(\d{2})(T(\d{2})(\d{2})(\d{2})){0,1}/', $value, $match) ;
    list($all, $year, $month, $day, $time, $hour, $minute, $second) = $match;
    $date = gmmktime($hour, $minute, $second, $month, $day, $year);
    if($options['TZID']) {
      //FIXME : Only php 5 handle timezone...
    }
    return $date;
  }  
  
  //FIXME Doit retourner un tableau ou une valeur. 
  /**
   * getValue 
   * 
   * @param mixed $propertyName 
   * @access public
   * @return void
   */
  function getValue($propertyName,$key=NULL) {
    if(isset($this->$propertyName)) {
      if(is_null($key)) {
        if(array_key_exists('value',$this->$propertyName)) {
          return $this->{$propertyName}['value'];
        } else {
          $values = array();
          foreach($this->$propertyName as $property) {
            $values[] = $property['value'];
          }
          return $values;
        }
      } else {
        if(array_key_exists($this->{$propertyName}, $key)) {
          return $this->{$propertyName}[$key]['value'];
        }
      }
    }
    return false;
  }
  //FIXME Doit retourner un tableau ou une valeur. 
  /**
   * getOption 
   * 
   * @param mixed $propertyName 
   * @param mixed $optionName 
   * @access public
   * @return void
   */
  function getOption($propertyName,$optionName,$key=NULL) {
    if(isset($this->$propertyName)) {
      if(is_null($key)) {
        if(array_key_exists('options',$this->$propertyName)) {
          if(isset($this->{$propertyName}['options'][$optionName])) {
            return $this->{$propertyName}['options'][$optionName];
          }
        } else {
          $values = array();
          foreach($this->$propertyName as $property) {
            if(isset($property['options'][$optionName])) {
              $values[] = $property['options'][$optionName];
              $isset = true;
            } else {
              $values[] = NULL;
            }
          }
          if($isset) {
            return $values;
          }
        }
      } else {
        return $this->{$propertyName}[$key]['options'][$optionName];
      }
    }
    return false;
  }  
  
  /**
   * getOption 
   * 
   * @param mixed $propertyName 
   * @param mixed $optionName 
   * @param mixed $key 
   * @access public
   * @return void
   */
  function getOptions($propertyName,$key=NULL) {
    if(isset($this->$propertyName)) {
      if(is_null($key)) {
        return $this->{$propertyName}['options'];
      } else {
        return $this->{$propertyName}[$key]['options'];
      }
    }
    return false;
  }  

  /**
   * addOption 
   * 
   * @param mixed $propertyName 
   * @param mixed $optionName 
   * @param mixed $value 
   * @param mixed $key 
   * @access public
   * @return void
   */
  function addOption($propertyName,$optionName,$value,$key = NULL) {

  } 
  /**
   * postCreate 
   * 
   * @access public
   * @return void
   */
  function postCreate() {
    // NOTHING TO DO
  }
}


/**
 * ICalendar_Vevent 
 * 
 * @uses ICalendar
 * @uses _Element
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */
class ICalendar_Vevent extends ICalendar_Element {


  /**
   * ICalendar_Vevent 
   * 
   * @param mixed $document 
   * @access public
   * @return void
   */
  function ICalendar_Vevent(&$document) {
    $this->document = &$document;
    $this->name = vevent;    
  }

  /**
   * @see setDate 
   * @param mixed $values 
   * @param mixed $options 
   * @access public
   * @return void
   */
  function setDtstart($value,$options) {
    $this->dtstart = array('value' => ICalendar_Element::parseDate($value,$option),
                           'options' => $options);
  }

  /**
   * setDtend 
   * 
   * @param mixed $value 
   * @param mixed $options 
   * @access public
   * @return void
   */
  function setDtend($value,$options) {
    $this->dtend = array('value' => ICalendar_Element::parseDate($value,$options),
                         'options' => $options);

  }

  /**
   * setRrule
   * 
   * @param mixed $value 
   * @param mixed $options 
   * @access public
   * @return void
   */
  function setRrule($value,$options) {
    $kind = strtolower($value['freq']);
    if($value['interval']) {
      $this->frequency = $value['interval'];
    } else {
      $this->frequency = 1;
    }
  }

  /**
   *  
   * @see setTimecreate
   * @param mixed $value 
   * @param mixed $options 
   * @access public
   * @return void
   */
  function setCreated($value, $options) {
    $this->created  = array('value' => ICalendar_Element::parseDate($value,$option),
                            'options' => $options);    
  }
  
  /**
   * @see setTimeupdate
   * @param mixed $value 
   * @param mixed $options 
   * @access public
   * @return void
   */
  function setLastModified($value, $options) {
    $this->lastMofified = array('value' => ICalendar_Element::parseDate($value,$option),
                                'options' => $options);    
  }  

  /**
   * postCreate 
   * 
   * @access public
   * @return void
   */
  function postCreate() {
    if($this->dtend) {
      $this->setDuration(($this->dtend - $this->dtstart), array());
    }
  }
  
}


?>
