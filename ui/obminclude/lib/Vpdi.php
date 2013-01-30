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



class Vpdi_InvalidEncodingException extends Exception {}
class Vpdi_UnexpectedEntityException extends Exception {}
class Vpdi_InvalidVcardEntityException extends Exception {}
class Vpdi_BeginEndMismatchException extends Exception {}
class Vpdi_UnencodableException extends Exception {}

/**
 * RFC 2425 parser
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
 */
class Vpdi {
  
  const VCARD = 'VCARD';
  
  /**
   * Property name regex : iana-token or x-name, i.e. alphanum chars + '-'
   * '_' and '/' have been added because they are used by some clients.
   */
  const NAME_RE = '[-a-z0-9_/][-a-z0-9_/]*';
  
  /**
   * Parameter regex : it is a simplified regex, it does not comply fully to the RFC
   */
  const PARAM_RE = ';?([^:]*)';
  
  /**
   * QSAFE-CHAR = WSP / %x21 / %x23-7E / NON-ASCII
   * Any character except CTLs, DQUOTE
   */
  const QSAFECHAR_RE = '[\s\x21\x23-\x7e\x80-\xff]';
  
  /**
   * SAFE-CHAR = WSP / %x21 / %x23-2B / %x2D-39 / %x3C-7E / NON-ASCII
   * Any character except CTLs, DQUOTE, ";", ":", ","
   */
  const SAFECHAR_RE = '[\s\x21\x23-\x2b\x2d-\x39\x3c-\x7e\x80-\xff]';
  
  private static $config = array(
    'type_values_as_a_parameter_list' => false,
    'always_encode_in_upper_case' => true
  );
  
  private static $component_classes = array(
    'vcard' => 'Vpdi_Vcard',
    'vcalendar' => 'Vpdi_Icalendar',
    'vevent' => 'Vpdi_Icalendar_Vevent',
    'vfreebusy' => 'Vpdi_Icalendar_Vfreebusy'
  );
  
  /**
   * Returns the first entity from string
   * 
   * @param string $string
   * @access public
   * @return mixed
   */
  public static function decodeOne($string, $expected_profile = null) {
    try {
      $entities = self::decode($string, $expected_profile);
      return $entities[0];
    } catch (Exception $e) {
      return false;
    }
  }
  
  /**
   * Decodes a string into an array of Vpdi_Entity objects
   * 
   * @param string $string
   * @access public
   * @return mixed
   */
  public static function decode($string, $expected_profile = null) {
    $entities = self::expand(self::decodeProperties($string));
    if (!is_null($expected_profile)) {
      self::checkProfile($entities, $expected_profile);
      self::checkAllEntitiesAreValid($entities);
    }
    return $entities;
  }
  
  /**
   * Splits an array into an array of all the properties at the outer level and
   * an array of all the inner entities
   * 
   * @param array $lines
   * @access public
   * @return array an array(<properties>, <entities>)
   */
  public static function split($lines) {
    $properties = array();
    $entities = array();
    foreach ($lines as $line) {
      if ($line instanceof Vpdi_Property) {
        $properties[] = $line;
      } else {
        $entities[] = $line;
      }
    }
    return array($properties, $entities);
  }
  
  /**
   * Expands an unidimensional array of properties into a tree of entities
   * 
   * Since BEGIN/END delimited entities can be nested, whe must build a tree.
   * 
   * @param array $properties
   * @param string $break_profile
   * @access public
   * @return mixed
   */
  public static function expand($properties, $break_profile = null) {
    $stack = array();
    $current_profile = null;
    $current_entity = null;
    
    while (($p = array_shift($properties)) !== null) {
      if ($p->nameEquals('BEGIN')) {
        $profile = strtolower($p->value());
        if ($current_profile === null) {
          $current_entity  = self::instantiate($profile);
          $current_profile = $profile;
        } else {
          array_unshift($properties, $p);
          $children = self::expand(&$properties, $profile);
          if ($current_entity !== null) {
            $current_entity->addProperty($children[0]);
          } else {
            $stack[] = $children[0];
          }
        }
      } elseif ($p->nameEquals('END')) {
        $profile = strtolower($p->value());
        if ($profile == $current_profile) {
          $stack[] = $current_entity;
          $current_profile = null;
          $current_entity = null;
          if ($profile == $break_profile) break;
        } else {
          throw new Vpdi_BeginEndMismatchException($current_profile.' != '.$p->value());
        }
      } else {
        if ($current_entity !== null) {
          $current_entity->addProperty($p);
        } else {
          $stack[] = $p;
        }
      }
    }
    return $stack;
  }
  
  /**
   * Instantiates an entity object
   * 
   * @param string $profile
   * @access public
   * @return mixed
   */
  public static function instantiate($profile) {
    if (array_key_exists($profile, self::$component_classes)) {
      $class = self::$component_classes[$profile];
      $entity = new $class;
    } else {
      $entity = new Vpdi_Entity;
      $entity->setProfile($profile);
    }
    return $entity;
  }
  
  /**
   * Checks that the decoded entities have the expected profile
   * 
   * @param array $entities
   * @param string $profile
   * @access public
   * @return void
   */
  public static function checkProfile(array $entities, $expected_profile) {
    foreach ($entities as $entity) {
      if ($entity->profile() != strtoupper($expected_profile)) {
        throw new Vpdi_UnexpectedEntityException($entity->profile());
      }
    }
  }
  
  public static function checkAllEntitiesAreValid($entities) {
    foreach ($entities as $entity) {
      if (!$entity->isValid()) {
        throw new Vpdi_InvalidVcardEntityException($entity);
      }
    }
  }
  /**
   * Decodes a string into an array of Vpdi_Property objects
   * 
   * @param string $string
   * @access public
   * @return array
   */
  public static function decodeProperties($string) {
    $string = self::convertLineEndings($string);
    $string = self::unfoldLines($string);
    
    $properties = array();
    $lines = explode("\n", $string);
    foreach ($lines as $line) {
      if (empty($line)) {
        continue;
      }
      $properties[] = Vpdi_Property::decode($line);
    }
    return $properties;
  }
  
  /**
   * Encodes an array of Vpdi_Property objects into a string
   * 
   * @param array $properties
   * @access public
   * @return string
   */
  public static function encodeProperties($properties) {
    $strings = array();
    foreach ($properties as $p) {
      $strings[] = $p->__toString();
    }
    return implode("\n", $strings);
  }
  
  /**
   * Transforms a single line into an array expressing the group, name, 
   * params and value of the property
   * 
   * @param string $line
   * @access public
   * @return array
   */
  public static function decodeLine($line) {
    $regex = '#^((?:'.self::NAME_RE.'\.)*)?('.self::NAME_RE.')'.self::PARAM_RE.':(.*)$#i';
    $parts = array('group' => null, 'name' => '', 'params' => array(), 'value' => array());
    if (!preg_match($regex, $line, $match)) {
      throw new Vpdi_InvalidEncodingException($line);
    }
    if (strlen($match[1]) > 0) {
      $parts['group'] = substr($match[1], 0, -1); 
    }
    $parts['name'] = trim($match[2]);
    $parts['params'] = self::decodeParams($match[3]);
    $parts['value'] = trim($match[4]); // TODO : Should we trim or not ?
    
    return $parts;
  }
  
  /**
   * Converts RFC 2425 text into a string
   * 
   * Unescapes ',' and ';' chars and transforms '\n' into newlines
   * 
   * @param string $text
   * @access public
   * @return string
   */
  public static function decodeText($text) {
    return str_replace(array('\,', '\;', '\r\n', '\n'), array(',', ';', "\n", "\n"), $text);
  }
  
  /**
   * Decodes a separated list into an array of strings
   * 
   * Takes care of escaped $sep chars
   * 
   * @param string $text
   * @param string $sep optional separator
   * @access public
   * @return string
   */
  public static function decodeTextList($text, $sep = ',') {
    $values = preg_split("/(?<!\\\\)({$sep})/", $text);
    foreach ($values as $k => $v) {
      $values[$k] = self::decodeText($v);
    }
    return $values;
  }
  
  /**
   * Encodes a string into a RFC 2425 text
   * 
   * Escapes ',' and ';' chars and newlines
   * 
   * @param string $text
   * @access public
   * @return string
   */
  public static function encodeText($text) {
    return str_replace("\n", '\n', addcslashes($text, ',;'));
  }
  
  /**
   * Encodes a parameter text
   * 
   * If the text contains non-SAFECHAR chars, an exception will be thrown.
   * 
   * @param string $value
   * @access public
   * @return string
   * @throws Vpdi_UnencodableException
   */
  public static function encodeParamText($text) {
    if (preg_match('#\A'.self::SAFECHAR_RE.'*\z#', $text)) {
      return $text;
    }
    throw new Vpdi_UnencodableException("Parameter text: $text");
  }
  
  /**
   * Encodes a parameter value
   * 
   * A parameter value is a parameter text, or a quoted string so :
   * - if the value contains ";", ":" or ",", if will be quoted.
   * - if the value contains non-QSAFECHAR chars, an exception will be thrown.
   * 
   * @param string $value
   * @access public
   * @return string
   * @throws Vpdi_UnencodableException
   */
  public static function encodeParamValue($value) {
    if (preg_match('#\A'.self::SAFECHAR_RE.'*\z#', $value)) {
      return $value;
    } elseif (preg_match('#\A'.self::QSAFECHAR_RE.'*\z#', $value)) {
      return '"'.$value.'"';
    }
    throw new Vpdi_UnencodableException("Parameter value: $value");
  }
  
  /**
   * Encodes an array of strings into a separated list
   * 
   * @param string $text
   * @param string $sep optional separator
   * @access public
   * @return string
   */
  public static function encodeTextList($values, $sep = ',') {
    $new_values = array();
    foreach ($values as $k => $v) {
      $new_values[$k] = self::encodeText($v);
    }
    return implode($sep, $new_values);
  }
  
  /**
   * Decodes a date into a DateTime object
   * 
   * @param string $value
   * @access public
   * @return DateTime
   */
  public static function decodeDate($value) {
    $date = new Of_Date($value, null, false);
    return $date;
  }
  
  /**
   * Decodes a datetime into a DateTime object
   * 
   * @param string $value
   * @access public
   * @return DateTime
   */
  public static function decodeDateTime($value) {
    return self::decodeDate($value);
  }
  
  /**
   * Encodes a datetime with UTC time, Z being the UTC designator
   * 
   * @param DateTime $value
   * @access public
   * @return string
   */
  public static function encodeDateTime(DateTime $value) {
    return gmdate('Ymd\THis\Z', $value->format('U'));
  }
  
  /**
   * Decodes a TZID into a DateTimeZone object
   * 
   * @param string $tzid
   * @access public
   * @return DateTimeZone
   */
  public static function decodeTimezone($tzid) {
    $tz = new DateTimeZone(str_replace('-', '/', $tzid));
    return $tz;
  }
  
  /**
   * Encodes a DateTimeZone object into a string
   * 
   * @param DateTimeZone $tz
   * @access public
   * @return string
   */
  public static function encodeTimezone(DateTimeZone $tz) {
    return str_replace('/', '-', $tz->getName());
  }
  
  /**
   * Decodes a freebusy period into a tuple of DateTime objects
   * 
   * @param string $period
   * @access public
   * @return array
   */
  public static function decodePeriod($period) {
    list($start, $end) = explode('/', $period);
    return array(self::decodeDateTime($start), self::decodeDateTime($end));
  }
  
  /**
   * Encodes a tuple of DateTime objects into a freebusy period
   * 
   * @param DateTime $start
   * @param DateTime $end
   * @access public
   * @return string
   */
  public static function encodePeriod(DateTime $start, DateTime $end) {
    return self::encodeDateTime($start).'/'.self::encodeDateTime($end);
  }
  
  /**
   * Decodes a boolean value into a boolean
   * 
   * @param string $bool
   * @access public
   * @return boolean
   */
  public static function decodeBoolean($bool) {
    return strtoupper($bool) == 'TRUE';
  }
  
  /**
   * Encodes a boolean into a string
   * 
   * @param boolean $bool
   * @access public
   * @return string
   */
  public static function encodeBoolean($bool) {
    return ($bool === true || strtoupper($bool) == 'TRUE') ? 'TRUE' : 'FALSE';
  }
  
  /**
   * Sets the value of a configuration parameter
   * 
   * @param string $key
   * @param string $value
   * @access public
   * @return void
   */
  public static function setConfig($key, $value) {
    if (!isset(self::$config[$key])) {
      throw new Exception("Unknown configuration parameter: $key");
    }
    self::$config[$key] = $value;
  }
  
  /**
   * Gets the value of a configuration parameter
   * 
   * @param string $key
   * @access public
   * @return mixed
   */
  public static function getConfig($key) {
    if (!isset(self::$config[$key])) {
      throw new Exception("Unknown configuration parameter: $key");
    }
    return self::$config[$key];
  }
  
  /**
   * Transforms a string of parameters into a multidimensional array
   * 
   * Supports both types of multi-valued parameters
   * (i.e. type=work,voice,msg and type=WORK;type=pref)
   * 
   * @param string $string string of parameters
   * @access private
   * @return array
   */
  private static function decodeParams($string) {
    if (empty($string)) return array();
    $list = explode(';', $string);
    $params = array();
    foreach($list as $param) {
      list($name, $value) = explode('=', $param);
      if (strpos($value, ',') !== false) {
        $params[$name] = explode(',', $value);
      } else {
        if (isset($params[$name])) {
          if (is_array($params[$name])) {
            $params[$name][] = $value;
          } else {
            $params[$name] = array($params[$name], $value);
          }
        } else {
          $params[$name] = $value;
        }
      }
    }
    return $params;
  }
  
  /**
   * Converts all line endings to UNIX standard
   * 
   * @param string $text
   * @access private
   * @return string
   */
  private static function convertLineEndings($text) {
    return str_replace("\r", "\n", str_replace("\r\n", "\n", $text));
  }
  
  /**
   * Unfolds continued lines (i.e that start with a whitespace or \t)
   * 
   * @param string $text
   * @access private
   * @return string
   */
  private static function unfoldLines($text) {
    return preg_replace("/(\n)([ |\t])/i", "", $text);
  }
}
