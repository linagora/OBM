<?php

class Vpdi_InvalidEncodingException extends Exception {}
class Vpdi_UnexpectedEntityException extends Exception {}
class Vpdi_BeginEndMismatchException extends Exception {}

/**
 * OBM RFC 2425 parser
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 * @author Raphaël Rougeron <raphael.rougeron@aliasource.fr> 
 * @license GPL 2.0
 */
class Vpdi {
  
  const NAME_RE = '[-a-z0-9_/][-a-z0-9_/ ]*';
  
  const PARAM_VALUE_RE = ';?([^:]*)';
  
  private static $config = array(
    'type_values_as_a_parameter_list' => false,
    'always_encode_in_upper_case' => true
  );
  
  /**
   * Decodes a string into an array of Vpdi_Entity objects
   * 
   * For each entity found in the string, it will try to instantiate the
   * corresponding Vpdi_Entity child class or fall back on the Vpdi_Entity
   * class. If the optional $profile argument is provided and the entities found
   * do not correspond to it, an exception will be thrown.
   * 
   * @param string $string
   * @param string $profile entity profile expected (VCARD or VCALENDAR)
   * @access public
   * @return mixed
   */
  public static function decode($string, $profile = null) {
    $entities = self::expand(self::decodeFields($string));
    if ($profile !== null) {
      $expected_class = 'Vpdi_'.$profile;
      foreach ($entities as $e) {
        if (strcasecmp(get_class($e), $expected_class) != 0) {
          throw new Vpdi_UnexpectedEntityException($e->profile());
        }
      }
    }
    return $entities;
  }
  
  /**
   * Expands an unidimensional array of fields into a tree of entities
   * 
   * Since BEGIN/END delimited entities can be nested, whe must build a tree.
   * 
   * @param array $fields
   * @param string $break_profile
   * @access public
   * @return mixed
   */
  public static function expand($fields, $break_profile = null) {
    $stack = array();
    $current_profile = null;
    $current_entity = null;
    
    while (($f = array_shift($fields)) !== null) {
      if ($f->nameEquals('BEGIN')) {
        $profile = strtolower($f->value());
        if ($current_profile === null) {
          $current_entity  = self::instantiate($profile);
          $current_profile = $profile;
        } else {
          array_unshift($fields, $f);
          $children = self::expand(&$fields, $profile);
          if ($current_entity !== null) {
            $current_entity->addField($children[0]);
          } else {
            $stack[] = $children[0];
          }
        }
      } elseif ($f->nameEquals('END')) {
        $profile = strtolower($f->value());
        if ($profile == $current_profile) {
          $stack[] = $current_entity;
          $current_profile = null;
          $current_entity = null;
          if ($profile == $break_profile) break;
        } else {
          throw new Vpdi_BeginEndMismatchException($current_profile.' != '.$f->value());
        }
      } else {
        if ($current_entity !== null) {
          $current_entity->addField($f);
        } else {
          $stack[] = $f;
        }
      }
    }
    return $stack;
  }
  
  /**
   * Instantiates an entity object
   * 
   * It will first try to instantiate a specialized class corresponding
   * to the $profile argument (ex: $profile='VCARD' -> Vpdi_VCard object) 
   * of fall back to the generic Vpdi_Entity class.
   * 
   * @param string $profile
   * @access public
   * @return mixed
   */
  public static function instantiate($profile) {
    $entity_child_class = 'Vpdi_'.ucfirst($profile);
    if (class_exists($entity_child_class)) {
      return new $entity_child_class;
    }
    $entity = new Vpdi_Entity;
    $entity->setProfile($profile);
    return $entity;
  }
  
  /**
   * Decodes a string into an array of Vpdi_Field objects
   * 
   * @param string $string
   * @access public
   * @return array
   */
  public static function decodeFields($string) {
    $string = self::convertLineEndings($string);
    $string = self::unfoldLines($string);
    
    $fields = array();
    $lines = explode("\n", $string);
    foreach ($lines as $line) {
      if (empty($line)) {
        continue;
      }
      $fields[] = Vpdi_Field::decode($line);
    }
    return $fields;
  }
  
  /**
   * Encodes an array of Vpdi_Field objects into a string
   * 
   * @param array $fields
   * @access public
   * @return string
   */
  public static function encodeFields($fields) {
    $strings = array();
    foreach ($fields as $f) {
      $strings[] = $f->__toString();
    }
    return implode("\n", $strings);
  }
  
  /**
   * Transforms a single line into an array expressing the group, name, 
   * params and value of the field
   * 
   * @param string $line
   * @access public
   * @return array
   */
  public static function decodeLine($line) {
    $regex = '#^((?:'.self::NAME_RE.'\.)*)?('.self::NAME_RE.')'.self::PARAM_VALUE_RE.':(.*)$#i';
    $parts = array('group' => null, 'name' => '', 'params' => array(), 'value' => array());
    if (!preg_match($regex, $line, $match)) {
      throw new Vpdi_InvalidEncodingException($line);
    }
    if (strlen($match[1]) > 0) {
      $parts['group'] = substr($match[1], 0, -1); 
    }
    $parts['name'] = trim($match[2]);
    $parts['params'] = self::decodeParams($match[3]);
    $parts['value'] = trim($match[4]); // Should we trim or not ?
    
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
    return str_replace(array('\,', '\;', '\n'), array(',', ';', "\n"), $text);
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
   * @return string
   */
  public static function decodeDate($value) {
    $date = new DateTime($value);
    return $date;
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
      //$name = strtoupper($name);
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
