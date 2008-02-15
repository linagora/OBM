<?php

/**
 * ICalendar_Utils 
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */
class ICalendar_Utils {

  /**
   * parse 
   * 
   * @param mixed $file 
   * @access public
   * @return void
   */
  function parseICS($file) {
    $reader = new ICalendar_ICS_Reader($file);
    $document = new ICalendar_Document();
//    $handle = fopen($file, 'r');
    //    while(($line = fgets($handle)) && strcmp(trim($line),'BEGIN:VCALENDAR') !== 0);
    while($reader->nextElement()){
       
    }
    
    ICalendar_Utils::parseElement($reader, $document->vcalendar);
    //    fclose($handle);
    $reader->close;
    return $document;
  }

  /**
   * parseElement 
   * 
   * @param mixed $handle 
   * @param mixed $element 
   * @access public
   * @return void
   */
  function parseElement(&$reader,&$element) {
    while($reader->nextElement()){

    }
 
    while(($line = fgets($handle)) && preg_match('/^END:.*$/',$line) === 0) {
      if(preg_match('/^BEGIN:(.*)$/',$line,$match) !== 0) {
        $document = &$element->getDocument();
        $child = &$document->createElement(trim($match[1]));
        $element->appendChild($child);
        ICalendar_Utils::parseElement($handle,$child);
        unset($child);
      } else {
        $property = ICalendar_Utils::parseProperty($handle, $line);
        $element->setProperty($property['name'],$property['value'],$property['options']);
      }
    }
  }

  /**
   * parseProperty 
   * 
   * @param mixed $handle 
   * @param mixed $line 
   * @access public
   * @return void
   */
  function parseProperty(&$handle, $line) {
    $propertyLine = '';
    $indent = 0;
    $properties = array('name','options','values');
    do {
      $offset = ftell($handle);
      $propertyLine .= trim($line);
      $line = fgets($handle);
      $indent++;
    } while(preg_match('/^\s{'.$indent.'}.*$/',$line) !== 0);
    fseek($handle,$offset);

    preg_match('/^([^;:]*);?([^:]*):(.*)$/',$propertyLine, $match);
    $property['name'] = strtolower($match[1]);

    $property['options'] = array();
    $options = explode(';',$match[2]);
    foreach($options as $option) {
      list($optionName, $optionValue) = explode('=', $option);
      $optionName = strtolower($optionName);
      $property['options'][$optionName] = $optionValue;
    }

    $property['value'] = array();
    $values = explode(';',$match[3]);
    foreach($values as $value) {
      if(strpos('=',$value) !== FALSE) {
        list($valueName, $valueValue) = explode('=', $value);
        $valueName = strtolower($valueName);
        $property['value'][$valueName] = $valueValue;
      } else {
        $property['value'][] = $value;
      }
    }
    if(count($property['value']) == 1) {
      $property['value'] = array_shift($property['value']);
    }

    return $property;
  }
}


class ICalendar_ICS_Reader {

  var $handler;

  var $attribute;

  function ICalendar_ICS_Reader($file) {
    $this->handle = fopen($file, 'r');
  }

  function getAttribute() {
    $propertyLine = '';
    $indent = 0;
    while(($line = fgets($this->handle)) && preg_match('/^\s{'.$indent.'}.*$/',$line)) {
      $propertyLine .= trim($line);
      $offset = ftell($this->handle);
      $indent++;
    }
    fseek($handle,$offset);
    
    preg_match('/^([^;:]*);?([^:]*):(.*)$/',$propertyLine, $match);   
    $this->attribute = array('name' => '', 'options' => array(), => 'value' => array());
    
    $property['name'] = trim(strtolower($match[1]));
    
    $options = explode(';',$match[2]);
    foreach($options as $option) {
      list($optionName, $optionValue) = explode('=', $option);
      $optionName = strtolower($optionName);
      $this->attribute['options'][$optionName] = $optionValue;
    }

    $property['value'] = array();
    $values = explode(';',$match[3]);
    foreach($values as $value) {
      if(strpos('=',$value) !== FALSE) {
        list($valueName, $valueValue) = explode('=', $value);
        $valueName = strtolower($valueName);
        $this->attribute['value'][$valueName] = $valueValue;
      } else {
        $property['value'][] = $value;
      }
    }
    if(count($property['value']) == 1) {
      $this->attribute['value'] = array_shift($this->attribute['value']);
    }    

  }

  function isBeginLine() {
    return ($this->attribute['name'] == 'begin');
  }

  function isEndLine() {
    return ($this->attribute['name'] == 'end');
  }
  
  function nextElement() {
    while(($return = $this->getAttribute()) && !$this->isBeginLine());
    return $return;
  }

  function parseElement() {

    while(($line = fgets($this->handle)) && $this->isEndLine()) {

      if(preg_match('/^BEGIN:(.*)$/',$line,$match) !== 0) {
        $document = &$element->getDocument();
        $child = &$document->createElement(trim($match[1]));
        $element->appendChild($child);
        ICalendar_Utils::parseElement($handle,$child);
        unset($child);
      } else {
        $property = ICalendar_Utils::parseProperty($handle, $line);
        $element->setProperty($property['name'],$property['value'],$property['options']);
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
 * ICalendar_Document 
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */
class ICalendar_Document {

  /**
   * vcalendar 
   * 
   * @var mixed
   * @access public
   */
  var $vcalendar;

  /**
   * ICalendar_Document 
   * 
   * @access public
   * @return void
   */
  function ICalendar_Document () {
    $this->vcalendar = new ICalendar_Element($this,'vcalendar');
  }

  /**
   * createElement 
   * 
   * @param mixed $name 
   * @access public
   * @return void
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
   * getVevents 
   * 
   * @access public
   * @return void
   */
  function getVevents() {
    return $this->vcalendar->getElementByName('vevent');
  }
}
/**
 * ICalendar_Element 
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
      if(isset($this->name)) {
        if(isset($this->name['value'])) {
          $this->name[] =  $this->name;
        }
        $this->name[] = array('value' => $value, 'options' => $options);
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

  function parseDate($value, $options) {
    preg_match('/.*(\d{4})(\d{2})(\d{2})(T(\d{2})(\d{2})(\d{2})){0,1}/', $value, $match) ;
    list($all, $year, $month, $day, $time, $hour, $minute, $second) = $match;
    $date = gmmktime($hour, $minute, $second, $month, $day, $year);
    if($options['TZID']) {
      //FIXME : Only php 5 handle timezone...
    }
    return $date;
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
    $this->dtend = array('value' => ICalendar_Element::parseDate($value),
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
    //TODO: DTEND
    //TODO: User ids
  }
  
}


?>
