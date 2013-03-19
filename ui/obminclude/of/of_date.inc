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



class Of_Date extends DateTime {

  // day formats
  const DAY            = 'd';              // d - 2 digit day of month, 01-31
  const DAY_SHORT      = 'j';              // j - 1,2 digit day of month, 1-31

  const DAY_SUFFIX     = 'S';              // S - english suffix day of month, st-th
  const DAYOFYEAR      = 'z';              // z - Number of day of year

  const WEEKDAY        = 'l';              // l - full day name - locale aware, Monday - Sunday
  const WEEKDAY_SHORT  = 'D';              // --- 3 letter day of week - locale aware, Mon-Sun
  const WEEKDAY_ICS    = 'WEEKDAY_ICS';    // --- 2 letters day name - locale aware, MO-SU
  const WEEKDAY_NARROW = 'X';              // --- 1 letter day name - locale aware, M-S
  const WEEKDAY_NAME   = 'D';              // D - abbreviated day name, 1-3 letters - locale aware, Mon-Sun

  const WEEKDAY_8601   = 'N';              // N - digit weekday ISO 8601, 1-7 1 = monday, 7=sunday
  const WEEKDAY_DIGIT  = 'w';              // w - weekday, 0-6 0=sunday, 6=saturday

  // week formats
  const WEEK           = 'W';              // W - number of week ISO8601, 1-53

  // month formats
  const MONTH          = 'm';              // m - 2 digit month, 01-12
  const MONTH_SHORT    = 'n';             // n - 1 digit month, no leading zeros, 1-12

  const MONTH_DAYS     = 't';              // t - Number of days this month

  const MONTH_NAME        = 'F';           // F - full month name - locale aware, January-December
  const MONTH_NAME_SHORT  = 'M';           // M - 3 letter monthname - locale aware, Jan-Dec
  const MONTH_NAME_NARROW = 'MONTH_NAME_NARROW'; // --- 1 letter month name - locale aware, J-D

  // year formats
  const YEAR           = 'Y';              // Y - 4 digit year
  const YEAR_SHORT     = 'y';              // y - 2 digit year, leading zeros 00-99

  const YEAR_8601      = 'o';              // o - number of year ISO8601

  const LEAPYEAR       = 'L';              // L - is leapyear ?, 0-1

  // time formats
  const MERIDIEM       = 'A';              // A - AM/PM - locale aware, AM/PM
  const MERIDIEM_LOWERCASE = 'a';          // a - am/pm - locale aware, am/pm
  const SWATCH         = 'B';              // B - Swatch Internet Time

  const HOUR           = 'H';              // H - 2 digit hour, leading zeros, 00-23
  const HOUR_SHORT     = 'G';              // G - 1 digit hour, no leading zero, 0-23

  const HOUR_AM        = 'h';              // h - 2 digit hour, leading zeros, 01-12 am/pm
  const HOUR_SHORT_AM  = 'g';              // g - 1 digit hour, no leading zero, 1-12 am/pm

  const MINUTE         = 'i';              // i - 2 digit minute, leading zeros, 00-59

  const SECOND         = 's';              // s - 2 digit second, leading zeros, 00-59

  // timezone formats
  const TIMEZONE_NAME  = 'e';              // e - timezone string
  const DAYLIGHT       = 'I';              // I - is Daylight saving time ?, 0-1
  const GMT_DIFF       = '0';              // O - GMT difference, -1200 +1200
  const GMT_DIFF_SEP   = 'P';              // P - seperated GMT diff, -12:00 +12:00
  const TIMEZONE       = 'T';              // T - timezone, EST, GMT, MDT
  const TIMEZONE_SECS  = 'Z';              // Z - timezone offset in seconds, -43200 +43200

  // date strings
  const ISO_8601       = 'c';              // DATE_ISO8601 c - ISO 8601 date string
  const RFC_2822       = 'r';              // DATE_RFC2822 r - RFC 2822 date string
  const TIMESTAMP      = 'U';              // U - unix timestamp
  const ICS_DATETIME   = 'Ymd\THis';       // ICS date time format
  const ICS_DATE       = 'Ymd';            // ICS date format

  // additional formats
  const DATABASE_DATE  = 'Y-m-d H:i:s';
  const ERA            = 'ERA';            // --- short name of era, locale aware,
  const ERA_NAME       = 'ERA_NAME';       // --- full name of era, locale aware,
  const DATES          = 'DATES';          // --- standard date, locale aware
  const DATE_FULL      = 'l d F Y';        // --- full date, locale aware
  const DATE_LONG      = 'd F Y';          // --- long date, locale aware
  const DATE_MEDIUM    = 'd.m.Y';          // --- medium date, locale aware
  const DATE_SHORT     = 'd.m.y';          // --- short date, locale aware
  const DATE_ISO       = 'Y-m-d';          // --- short date, locale aware
  const TIMES          = 'H:i:s';          // --- standard time, locale aware
  const TIMES_AM       = 'g:i:sa';          // --- standard time, locale aware
  const TIME_LONG      = 'H:i:s T';        // --- long time, locale aware
  const TIME_LONG_AM   = 'g:i:sa T';        // --- long time, locale aware
  const TIME_MEDIUM    = 'H:i:s';          // --- medium time, locale aware
  const TIME_MEDIUM_AM = 'g:i:sa';          // --- medium time, locale aware
  const TIME_SHORT     = 'H:i';            // --- short time, locale aware
  const TIME_SHORT_AM  = 'g:ia';            // --- short time, locale aware
  const ATOM           = 'c';              // --- DATE_ATOM
  const COOKIE         = 'l, d-M-y H:i:s e';// --- DATE_COOKIE
  const RFC_822        = 'D, d M y H:i:s O';// --- DATE_RFC822
  const RFC_850        = 'l, d-M-y H:i:s e';// --- DATE_RFC850
  const RFC_1036       = 'D, d M y H:i:s O';// --- DATE_RFC1036
  const RFC_1123       = 'D, d M y H:i:s O';// --- DATE_RFC1123
  const RSS            = 'D, d M Y H:i:s P';// --- DATE_RSS
  const W3C            = 'c';              // --- DATE_W3C
  const SOLR            = 'Y-m-d\TH:i:s\Z';              // --- DATE_W3C

  // OBM Format 
  const OBM_DATE_DAY            = 'l j F Y';
  const OBM_DATE_WEEK           = 'j F Y';
  const OBM_DATE_WEEK_LIST      = 'D j';
  const OBM_DATE_WEEK_LONG      = 'l j';
  const OBM_DATE_WEEK_JUMP      = 'j M';
  const OBM_DATE_MONTH          = 'F Y';
  const OBM_DATE_MONTH_LIST     = 'l j F';

  //OBM_ERROR AND WARNING
  const WARN_EMPTY_DATE         = 1;
  const ERR_INVALID_DATE        = 2;

  //DATE OCNSTANT
  const DAYDURATION             = 86400;
  
  //D => EE, l => EEEE, w => eee. 
  //n => M, F => MMMM, M => MMM,
  //For parse compliance this is not the case here 
  private static $PHP_TO_ISO = array(
      'd' => 'dd'  , 'D' => 'e'  , 'j' => 'd'   , 'l' => 'e', 'N' => 'e'   ,
      'w' => 'e' , 'z' => 'D'   , 'W' => 'w'   , 'F' => 'MM', 'm' => 'MM'  , 'M' => 'MM' ,
      'n' => 'MM'   , 't' => 'ddd' , 'L' => 'l'   , 'o' => 'YYYY', 'Y' => 'yyyy', 'y' => 'yy'  ,
      'a' => 'a'   , 'A' => 'a'   , 'B' => 'B'   , 'g' => 'h'   , 'G' => 'H'   , 'h' => 'hh'  ,
      'H' => 'HH'  , 'i' => 'mm'  , 's' => 'ss'  , 'e' => 'zzzz', 'I' => 'I'   , 'O' => 'Z'   ,
      'P' => 'ZZZZ', 'T' => 'z'   , 'c' => 'yyyy-MM-ddTHH:mm:ssZZZZ',
      'r' => 'r'   , 'U' => 'U');
  
  private static $ISO_TO_STRING = array(
      'd' => 'day', 'E' => 'weekday', 'e' => 'weekday', 'D' => 'day', 'w' =>  'week',
      'M' => 'month', 'n' => 'month', 'Y' => 'year', 'y' => 'year','g' => 'hour', 
      'G' => 'hour', 'a' => 'AM', 'h' => 'hour', 'H' => 'hour', 'm' => 'minute', 's' => 'second',
      'r' => 'date', 'U' => 'timestamp');     

  private static $STRING_TO_PHP = array(
      'day' => 'd', 'weekday' => 'N', 'week' => 'W', 'month' => 'm', 'weekday' => 'w',
      'year' => 'Y', 'hour' => 'H', 'minute' => 'i', 'second' => 's', 'timestamp' => 'U'
    );

  private static $PHP_TO_STRING = array(
      'd' => 'day', 'N' => 'weekday', 'W' => 'week', 'm' => 'month', 'w' => 'weekday',
      'Y' => 'year', 'H' => 'hour', 'i' => 'minute', 's' => 'second', 'U' => 'timestamp');

  private static $DAY_LIST = array('sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday');

  private static $SHORT_DAY_LIST = array('sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat');

  private static $MONTH_LIST = array(
      '01' => 'january', '02' => 'february', '03' => 'march', '04' => 'april', '05' => 'may', '06' => 'june',
      '07' => 'july', '08' => 'august', '09' => 'september', '10' => 'october', '11' => 'november',
      '11' => 'december');

  private static $SHORT_MONTH_LIST = array(
      '01' => 'jan', '02' => 'feb', '03' => 'mar', '04' => 'apr', '05' => 'may', '06' => 'jun', '07' => 'jul',
      '08' => 'aug', '09' => 'sep', '10' => 'oct', '11' => 'nov', '12' => 'dec');


  private static $_options = array(
    'outputdate' => 'd/m/Y',
    'inputdate' => 'd/m/Y',
    'timezone' => 'Europe/Paris'
  );
  
  private static $_localized_formats = array(
    'd M Y' => '%e %B %Y'
  );

  private static $_loaded = false;

  private static $_cache = array();

  private static $_today;

  private $_error;
  
  private $originalTimeZone;

  private $serialized; 

  public function __construct($time=null, $timezone=null, $parse=true) {
    self::loadOptions();
     if(is_null($time) || empty($time)) {
      $this->_error = self::WARN_EMPTY_DATE;
    }
    
    if(is_numeric($time) && $parse) {
      $time = ceil($time);
      $time = "@$time";
    }
    try {
      if(!is_null($timezone)) {
        $this->originalTimeZone = $timezone;
        $timezone = new DateTimeZone($timezone);
        parent::__construct($time, $timezone);
      } else {
        parent::__construct($time,new DateTimeZone(self::$_options['timezone']));
      }
      $this->setTimezone(new DateTimeZone(self::$_options['timezone']));
    } catch (Exception $e) {
      parent::__construct(date("Y-m-d"),new DateTimeZone(self::$_options['timezone']));
      $this->error =self::ERR_INVALID_DATE;
    }

  }

  /**
   * loadOptions 
   * 
   * @static
   * @access private
   * @return void
   */
  static private function loadOptions() {
    if(!self::$_loaded) {
      if(isset($_SESSION['set_date'])) self::setOption('outputdate',$_SESSION['set_date']);
      if(isset($_SESSION['set_date_upd'])) self::setOption('inputdate',$_SESSION['set_date_upd']);
      if(isset($_SESSION['set_timezone'])) self::setOption('timezone',$_SESSION['set_timezone']);
      if(isset($_SESSION['set_timeformat'])) self::setOption('timeformat',$_SESSION['set_timeformat']);
      self::$_loaded = true;
    }
  }

  /**
   * Set the default Options 
   * 
   * @param mixed $option 
   * @param mixed $value 
   * @static
   * @access public
   * @return void
   */
  static public function setOption($option, $value) {
    self::$_options[$option] = $value;
  }

  /**
   * get the default  
   * 
   * @param mixed $option 
   * @param mixed $value 
   * @static
   * @access public
   * @return void
   */
  static public function getOption($option) {
    self::loadOptions();
    return self::$_options[$option];
  }
  /**
   * setDefaultTimezone 
   * 
   * @access public
   * @return void
   */
  public function setDefaultTimezone() {
    $this->setTimezone(new DateTimeZone(self::$_options['timezone']));
  }

  /**
   * Return the current stored error 
   * 
   * @access public
   * @return void
   */
  public function error() {
    return $this->_error;
  }

  /**
   * Returns a date part or a date part.
   * If the timestamp is too large for integers, then the return value will be a string.
   * This function does not return the timestamp as an object.
   * Is locale aware
   *
   * @param string $part  Part to get
   * @return integer|string  UNIX timestamp
   */
  public function get($part=self::ISO_8601) {
    switch ($part) {
      case self:: WEEKDAY_NARROW :
        return substr($this->format(self::WEEKDAY_SHORT), 0, 1);
        break;
      case self:: WEEKDAY_ICS :
        return strtoupper(substr($this->format(self::WEEKDAY_SHORT), 0, 2));
        break;        
      default :
        if (array_key_exists($part, self::$_localized_formats)) {
          return strftime(self::$_localized_formats[$part], $this->getTimestamp());
        }
        return $this->format($part);
    }
  }

  /**
   * Returns a date part or a date part.
   * locale aware
   *  
   * @param mixed $part  Part to get
   * @access public
   * @return void
   */
  public function localize($part) {
    $parts = str_split($part);
    $string = '';
    foreach($parts as $digit) {
      switch ($digit) {
      case self::WEEKDAY :
        $string .= $GLOBALS['l_daysofweek'][$this->get(self::WEEKDAY_DIGIT)];
        break;
      case self::WEEKDAY_SHORT :
        $string .= $GLOBALS['l_daysofweekshort'][$this->get(self::WEEKDAY_DIGIT)];
        break;
      case self::WEEKDAY_NARROW :
        $string .= $GLOBALS['l_daysofweekfirst'][$this->get(self::WEEKDAY_DIGIT)];
        break;
      case self::WEEKDAY_NAME :
        $string .= $GLOBALS['l_daysofweek'][$this->get(self::WEEKDAY_DIGIT)];
        break;
      case self::MONTH_NAME :
        $string .= $GLOBALS['l_monthsofyear'][$this->get(self::MONTH_SHORT) - 1];
        break;
      case self::MONTH_NAME_SHORT :
        $string .= $GLOBALS['l_monthsofyearshort'][$this->get(self::MONTH_SHORT) - 1];
        break;
      case self::MONTH_NAME_NARROW :
        break;
      default :
        $string .= $this->get($digit);
      }
    }
    return $string ;
  }

  /**
   * Get the date time formated with the outputdatetime option
   * 
   * @access public
   * @return void
   */
  public function getOutputDateTime() {
    if (self::$_options['timeformat'] == '12H') return $this->get(self::$_options['outputdate'].' '.self::TIME_SHORT_AM);
    return $this->get(self::$_options['outputdate'].' '.self::TIME_SHORT);
  }


  /**
   * Get the date time formated with the outputdatetime option
   * 
   * @access public
   * @return void
   */
  public function getOutputHour() {
    if (self::$_options['timeformat'] == '12H') return $this->format(self::HOUR_SHORT_AM.self::MERIDIEM_LOWERCASE);
    return $this->format(self::HOUR);
  }

  /**
  * Get the time formated with the outputdatetime option
  *
  * @access public
  * @return void
  */
  public function getOutputTime() {
  	if (self::$_options['timeformat'] == '12H') return $this->get(self::TIME_SHORT_AM);
  	return $this->get(self::TIME_SHORT);
  }
  
  /**
   * Get the date formated with the outputdate option
   * 
   * @access public
   * @return void
   */
  public function getOutputDate() {
    return $this->get(self::$_options['outputdate']);
  }


  /**
   * Get the date time formated with the inputdatetime option
   * 
   * @access public
   * @return void
   */
  public function getInputDateTime() {
    return $this->get(self::$_options['inputdate'].' '.self::TIME_MEDIUM);
  }

  /**
   * Get the date formated with the inputdate option
   * 
   * @access public
   * @return void
   */
  public function getInputDate() {
    return $this->get(self::$_options['inputdate']);
  }

  /**
   * Returns this object's iso date .
   * This function does not return the iso as an object.
   *
   * @return integer|string  UNIX iso
   */
  public function getURL() {
    return urlencode($this->format(self::ISO_8601));
  }

  /**
   * Returns the time format .
   *
   * @return string  Time format
   */
  public function getTimeFormat() {
    if (self::$_options['timeformat'] == '12H') return self::TIME_SHORT_AM;
    return self::TIME_SHORT;
  }

  public function getHourFormat(){
    if (self::$_options['timeformat'] == '12H') return self::HOUR_AM;
    return self::HOUR;
  }

  /**
   * Sets a new date or date part 
   *
   * @param  integer|string|array|Of_Date  $date  date to set
   * @param string $part  Part to set OPTIONAL
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */

  public function set($date, $part=null, $format=null) {
    
    return $this->_set($part, $date, $format);
  }

  /**
   * add a part to the current object
   *
   * @param  integer|string|array|Of_Date  $date  Date to add
   * @param string $part  Part to add OPTIONAL
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function add($date, $part=null, $format=null) {
    return $this->_add($part, $date, $format); 
  }

  /**
   * substracts a part to the current object 
   *
   * @param  integer|string|array|Of_Date  $date  Date to substract
   * @param string $part  Part to substract OPTIONAL
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function sub($date, $part=null, $format=null) {
    return $this->_sub($part, $date, $format); 
  }

  /**
   * Compares the current object part with the givent date
   *
   * @param  integer|string|array|Of_Date  $date  Date to compare 
   * @param string $part  Part to compare OPTIONAL
   * @param string $format Format of the input string OPTIONAL
   * @return integer  0 = equal, 1 = later, -1 = earlier
   */
  public function compare($date, $part=null, $format=null) {
    return $this->_compare($part, $date, $format);
  }  

  /**
   * Compares the current object part with the givent date
   *
   * @param  integer|string|array|Of_Date  $date  Date to compare 
   * @param string $part  Part to compare OPTIONAL
   * @param string $format Format of the input string OPTIONAL
   * @return integer  0 = equal, 1 = later, -1 = earlier
   */
  public function equals($date, $part=null, $format=null) {
    return $this->_compare($part, $date, $format) == 0;
  }  

  /**
   * difference between two date part
   *
   * @param  integer|string|array|Of_Date  $date  Date to diff 
   * @param string $part  Part to compare OPTIONAL
   * @param string $format Format of the input string OPTIONAL
   * @return integer  difference in seconds
   */
  public function diff($date, $part=null, $format=null) {
    return $this->_diff($part, $date, $format);
  }

  /**
   * Returns this object's internal UNIX timestamp .
   * If the timestamp is too large for integers, then the return value will be a string.
   * This function does not return the timestamp as an object.
   *
   * @return integer|string  UNIX timestamp
   */
  public function getTimestamp() {
    return $this->format(self::TIMESTAMP);
  }

  /**
   * Sets a new timestamp
   *
   * @param  integer|string|array|Of_Date  $timestamp  Timestamp to set
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */

  public function setTimestamp($timestamp, $format=null) {
    
    return $this->_set(self::TIMESTAMP, $timestamp, $format); 
  }

  /**
   * add a timestamp
   *
   * @param  integer|string|array|Of_Date  $timestamp  Timestamp to add
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function addTimestamp($timestamp, $format=null) {
    return $this->_add(self::TIMESTAMP, $timestamp, $format); 
  }

  /**
   * substracts a timestamp
   *
   * @param  integer|string|array|Of_Date  $timestamp  Timestamp to substract
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function subTimestamp($timestamp, $format=null) {
    return $this->_sub(self::TIMESTAMP, $timestamp, $format); 
  }

  /**
   * Compares two timestamps
   *
   * @param  integer|string|array|Of_Date  $timestamp  Timestamp to compare 
   * @param string $format Format of the input string OPTIONAL
   * @return integer  0 = equal, 1 = later, -1 = earlier
   */
  public function compareTimestamp($timestamp, $format=null) {
    return $this->_compare(self::TIMESTAMP, $timestamp, $format);
  }  

  /**
   * difference between two timestamps
   *
   * @param  integer|string|array|Of_Date  $timestamp  Timestamp to diff 
   * @param string $format Format of the input string OPTIONAL
   * @return integer  difference in seconds
   */
  public function diffTimestamp($timestamp, $format=null) {
    return $this->_diff(self::TIMESTAMP, $timestamp, $format);
  }  


  /**
   * Returns the full date formatted with the default user format.
   *
   * @return Of_Date
   */
  public function getDateIso() {
    return $this->format(self::DATE_ISO);
  }

  /**
   * Sets a new date for the date object. Format defines how to parse the date string.
   * If no format is given, the standardformat of this locale is used.
   * Also a complete date with time can be given, but only the date is used for setting.
   * For example: MMMM.yy HH:mm-> May.07 22:11 => 01.May.07 00:00
   *
   * @param  string|integer|array|Of_Date  $date    Date to set
   * @param  string                          $format  OPTIONAL Date format for parsing
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date  new date
   */
  public function setDateIso($date, $format=null) {
    return $this->_set(self::DATE_ISO, $date, $format);
  } 

  /**
   * Subtracts a date from the existing date object. Format defines how to parse the date string.
   * If no format is given, the standardformat of this locale is used.
   * For example: MM.dd.YYYY -> 10 -> -10 months
   *
   * @param  string|integer|array|Of_Date  $date    Date to sub
   * @param  string                          $format  OPTIONAL Date format for parsing input
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date  new date
   */
  public function subDateIso($date, $format=null) {
    return $this->_sub(self::DATE_ISO, $date, $format);
  }    

  /**
   * Add a date from the existing date object. Format defines how to parse the date string.
   * If no format is given, the standardformat of this locale is used.
   *
   * @param  string|integer|array|Of_Date  $date    Date to sub
   * @param  string                          $format  OPTIONAL Date format for parsing input
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date  new date
   */
  public function addDateIso($date, $format=null) {
    return $this->_add(self::DATE_ISO, $date, $format);
  }    

  /**
   * Compare two date
   * Format defines how to parse the date string.
   * If no format is given, the standardformat of this locale is used.
   *
   * @param  string|integer|array|Of_Date  $date    Date to compare 
   * @param  string                          $format  OPTIONAL Date format for parsing input
   * @param string $format Format of the input string OPTIONAL
   * @return integer  0 = equal, 1 = later, -1 = earlier
   */
  public function compareDateIso($date, $format=null) {
    return $this->_compare(self::DATE_ISO, $date, $format);
  }        

  /**
   * Difference between two date
   * Format defines how to parse the date string.
   * If no format is given, the standardformat of this locale is used.
   *
   * @param  string|integer|array|Of_Date  $date    Date to compare 
   * @param  string                          $format  OPTIONAL Date format for parsing input
   * @param string $format Format of the input string OPTIONAL
   * @return integer|string  UNIX timestamp
   */
  public function diffDateIso($date, $format=null) {
    return $this->_diff(self::DATE_ISO, $date, $format);
  }         

  /**
   * Returns this object's iso date .
   * This function does not return the iso as an object.
   *
   * @return integer|string  UNIX iso
   */
  public function getIso() {
    return $this->format(self::ISO_8601);
  }

  /**
   * Sets a new iso date
   *
   * @param  integer|string|array|Of_Date  $iso  Iso date to set
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */

  public function setIso($iso, $format=self::ISO_8601) {
    return $this->_set(self::ISO_8601, $iso, $format); 
  }

  /**
   * add a iso date
   *
   * @param  integer|string|array|Of_Date  $iso  Iso date to add
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function addIso($iso, $format=self::ISO_8601) {
    return $this->_add(self::ISO_8601, $iso, $format); 
  }

  /**
   * substracts an iso date
   *
   * @param  integer|string|array|Of_Date  $iso  Iso date to substract
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function subIso($iso, $format=self::ISO_8601) {
    return $this->_sub(self::ISO_8601, $iso, $format); 
  }

  /**
   * Compares two iso dates
   *
   * @param  integer|string|array|Of_Date  $iso  Iso date to compare 
   * @param string $format Format of the input string OPTIONAL
   * @return integer  0 = equal, 1 = later, -1 = earlier
   */
  public function compareIso($iso, $format=self::ISO_8601) {
    return $this->_compare(self::ISO_8601, $iso, $format);
  }  

  /**
   * difference between two iso dates
   *
   * @param  integer|string|array|Of_Date  $iso  Iso date to diff 
   * @param string $format Format of the input string OPTIONAL
   * @return integer  difference in seconds
   */
  public function diffIso($iso, $format=self::ISO_8601) {
    return $this->_diff(self::ISO_8601, $iso, $format);
  }  


  /**
   * Returns this object's internal year .
   * If the year is too large for integers, then the return value will be a string.
   * This function does not return the year as an object.
   *
   * @return integer|string  UNIX year
   */
  public function getYear() {
    return $this->format(self::YEAR);
  }

  /**
   * Sets a new year
   *
   * @param  integer|string|array|Of_Date  $year  Year to set
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */

  public function setYear($year, $format=null) {
    return $this->_set(self::YEAR, $year, $format); 
  }

  /**
   * add a year
   *
   * @param  integer|string|array|Of_Date  $year  Year to add
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function addYear($year, $format=null) {
    return $this->_add(self::YEAR, $year, $format); 
  }

  /**
   * substracts an year
   *
   * @param  integer|string|array|Of_Date  $year  Year to substract
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function subYear($year, $format=null) {
    return $this->_sub(self::YEAR, $year, $format); 
  }

  /**
   * Compares two years
   *
   * @param  integer|string|array|Of_Date  $year  Year to compare 
   * @param string $format Format of the input string OPTIONAL
   * @return integer  0 = equal, 1 = later, -1 = earlier
   */
  public function compareYear($year, $format=null) {
    return $this->_compare(self::YEAR, $year, $format);
  }  

  /**
   * difference between two years
   *
   * @param  integer|string|array|Of_Date  $year  Year to diff 
   * @param string $format Format of the input string OPTIONAL
   * @return integer  difference in years 
   */
  public function diffYear($year, $format=null) {
    return $this->_diff(self::YEAR, $year, $format);
  }  

  /**
   * Returns this object's internal month .
   * If the month is too large for integers, then the return value will be a string.
   * This function does not return the month as an object.
   *
   * @return integer|string  UNIX month
   */
  public function getMonth() {
    return $this->format(self::MONTH);
  }

  /**
   * Sets a new month
   *
   * @param  integer|string|array|Of_Date  $month  Month to set
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */

  public function setMonth($month, $format=null) {
    return $this->_set(self::MONTH, $month, $format); 
  }

  /**
   * add a month
   *
   * @param  integer|string|array|Of_Date  $month  Month to add
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function addMonth($month, $format=null) {
    return $this->_add(self::MONTH, $month, $format); 
  }

  /**
   * substracts an month
   *
   * @param  integer|string|array|Of_Date  $month  Month to substract
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function subMonth($month, $format=null) {
    return $this->_sub(self::MONTH, $month, $format); 
  }

  /**
   * Compares two months
   *
   * @param  integer|string|array|Of_Date  $month  Month to compare 
   * @param string $format Format of the input string OPTIONAL
   * @return integer  0 = equal, 1 = later, -1 = earlier
   */
  public function compareMonth($month, $format=null) {
    return $this->_compare(self::MONTH, $month, $format);
  }  

  /**
   * difference between two months
   *
   * @param  integer|string|array|Of_Date  $month  Month to diff 
   * @param string $format Format of the input string OPTIONAL
   * @return integer  difference in months 
   */
  public function diffMonth($month, $format=null) {
    return $this->_diff(self::MONTH, $month, $format);
  }    

  /**
   * Returns this object's internal day .
   * If the day is too large for integers, then the return value will be a string.
   * This function does not return the day as an object.
   *
   * @return integer|string  UNIX day
   */
  public function getDay() {
    return $this->format(self::DAY);
  }

  /**
   * Sets a new day
   *
   * @param  integer|string|array|Of_Date  $day  Day to set
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */

  public function setDay($day, $format=null) {
    return $this->_set(self::DAY, $day, $format); 
  }

  /**
   * add a day
   *
   * @param  integer|string|array|Of_Date  $day  Day to add
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function addDay($day, $format=null) {
    return $this->_add(self::DAY, $day, $format); 
  }

  /**
   * substracts an day
   *
   * @param  integer|string|array|Of_Date  $day  Day to substract
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function subDay($day, $format=null) {
    return $this->_sub(self::DAY, $day, $format); 
  }

  /**
   * Compares two days
   *
   * @param  integer|string|array|Of_Date  $day  Day to compare 
   * @param string $format Format of the input string OPTIONAL
   * @return integer  0 = equal, 1 = later, -1 = earlier
   */
  public function compareDay($day, $format=null) {
    return $this->_compare(self::DAY, $day, $format);
  }

  /**
   * difference between two days
   *
   * @param  integer|string|array|Of_Date  $day  Day to diff 
   * @param string $format Format of the input string OPTIONAL
   * @return integer  difference in days 
   */
  public function diffDay($day, $format=null) {
    return $this->_diff(self::DAY, $day, $format);
  }


  /**
   * Returns this object's internal weekday .
   * If the weekday is too large for integers, then the return value will be a string.
   * This function does not return the weekday as an object.
   *
   * @return integer|string  UNIX weekday
   */
  public function getWeekday() {
    return $this->format(self::WEEKDAY_DIGIT);
  }

  /**
   * Sets a new weekday
   *
   * @param  integer|string|array|Of_Date  $weekday  Weekday to set
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */

  public function setWeekday($weekday, $format=null) {
    return $this->_set(self::WEEKDAY_DIGIT, $weekday, $format); 
  }

  /**
   * add a weekday
   *
   * @param  integer|string|array|Of_Date  $weekday  Weekday to add
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function addWeekday($weekday, $format=null) {
    return $this->_add(self::WEEKDAY_DIGIT, $weekday, $format);
  }

  /**
   * substracts an weekday
   *
   * @param  integer|string|array|Of_Date  $weekday  Weekday to substract
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function subWeekday($weekday, $format=null) {
    return $this->_sub(self::WEEKDAY_DIGIT, $weekday, $format);
  }

  /**
   * Compares two weekdays
   *
   * @param  integer|string|array|Of_Date  $weekday  Weekday to compare 
   * @param string $format Format of the input string OPTIONAL
   * @return integer  0 = equal, 1 = later, -1 = earlier
   */
  public function compareWeekday($weekday, $format=null) {
    return $this->_compare(self::WEEKDAY_DIGIT, $weekday, $format);
  }

  /**
   * difference between two weekdays
   *
   * @param  integer|string|array|Of_Date  $weekday  Weekday to diff 
   * @param string $format Format of the input string OPTIONAL
   * @return integer  difference in weekdays 
   */
  public function diffWeekday($weekday, $format=null) {
    return $this->_diff(self::WEEKDAY_DIGIT, $weekday, $format);
  }


  /**
   * Returns this object's internal dayofyear .
   * If the dayofyear is too large for integers, then the return value will be a string.
   * This function does not return the dayofyear as an object.
   *
   * @return integer|string  UNIX dayofyear
   */
  public function getDayOfYear() {
    return $this->format(self::DAYOFYEAR);
  }

  /**
   * Sets a new dayofyear
   *
   * @param  integer|string|array|Of_Date  $dayofyear  DayOfYear to set
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */

  public function setDayOfYear($dayofyear, $format=null) {
    return $this->_set(self::DAYOFYEAR, $dayofyear, $format);
  }

  /**
   * add a dayofyear
   *
   * @param  integer|string|array|Of_Date  $dayofyear  DayOfYear to add
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function addDayOfYear($dayofyear, $format=null) {
    return $this->_add(self::DAYOFYEAR, $dayofyear, $format);
  }

  /**
   * substracts an dayofyear
   *
   * @param  integer|string|array|Of_Date  $dayofyear  DayOfYear to substract
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function subDayOfYear($dayofyear, $format=null) {
    return $this->_sub(self::DAYOFYEAR, $dayofyear, $format);
  }

  /**
   * Compares two dayofyears
   *
   * @param  integer|string|array|Of_Date  $dayofyear  DayOfYear to compare 
   * @param string $format Format of the input string OPTIONAL
   * @return integer  0 = equal, 1 = later, -1 = earlier
   */
  public function compareDayOfYear($dayofyear, $format=null) {
    return $this->_compare(self::DAYOFYEAR, $dayofyear, $format);
  }

  /**
   * difference between two dayofyears
   *
   * @param  integer|string|array|Of_Date  $dayofyear  DayOfYear to diff 
   * @param string $format Format of the input string OPTIONAL
   * @return integer  difference in dayofyears 
   */
  public function diffDayOfYear($dayofyear, $format=null) {
    return $this->_diff(self::DAYOFYEAR, $dayofyear, $format);
  }


  /**
   * Returns this object's internal hour .
   * If the hour is too large for integers, then the return value will be a string.
   * This function does not return the hour as an object.
   *
   * @return integer|string  UNIX hour
   */
  public function getHour() {
    return $this->format(self::HOUR);
  }

  /**
   * Sets a new hour
   *
   * @param  integer|string|array|Of_Date  $hour  Hour to set
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */

  public function setHour($hour, $format=null) {
    if($format==null) $format = $this->getHourFormat();
    return $this->_set(self::HOUR, $hour, $format);
  }

  /**
   * add a hour
   *
   * @param  integer|string|array|Of_Date  $hour  Hour to add
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function addHour($hour, $format=null) {
    return $this->_add(self::HOUR, $hour, $format);
  }

  /**
   * substracts an hour
   *
   * @param  integer|string|array|Of_Date  $hour  Hour to substract
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function subHour($hour, $format=null) {
    return $this->_sub(self::HOUR, $hour, $format);
  }

  /**
   * Compares two hours
   *
   * @param  integer|string|array|Of_Date  $hour  Hour to compare 
   * @param string $format Format of the input string OPTIONAL
   * @return integer  0 = equal, 1 = later, -1 = earlier
   */
  public function compareHour($hour, $format=null) {
    return $this->_compare(self::HOUR, $hour, $format);
  }

  /**
   * difference between two hours
   *
   * @param  integer|string|array|Of_Date  $hour  Hour to diff 
   * @param string $format Format of the input string OPTIONAL
   * @return integer  difference in hours 
   */
  public function diffHour($hour, $format=null) {
    return $this->_diff(self::HOUR, $hour, $format);
  }

  /**
   * Returns this object's internal minute .
   * If the minute is too large for integers, then the return value will be a string.
   * This function does not return the minute as an object.
   *
   * @return integer|string  UNIX minute
   */
  public function getMinute() {
    return $this->format(self::MINUTE);
  }

  /**
   * Sets a new minute
   *
   * @param  integer|string|array|Of_Date  $minute  Minute to set
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */

  public function setMinute($minute, $format=null) {
    return $this->_set(self::MINUTE, $minute, $format);
  }

  /**
   * add a minute
   *
   * @param  integer|string|array|Of_Date  $minute  Minute to add
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function addMinute($minute, $format=null) {
    return $this->_add(self::MINUTE, $minute, $format);
  }

  /**
   * substracts an minute
   *
   * @param  integer|string|array|Of_Date  $minute  Minute to substract
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function subMinute($minute, $format=null) {
    return $this->_sub(self::MINUTE, $minute, $format);
  }

  /**
   * Compares two minutes
   *
   * @param  integer|string|array|Of_Date  $minute  Minute to compare 
   * @param string $format Format of the input string OPTIONAL
   * @return integer  0 = equal, 1 = later, -1 = earlier
   */
  public function compareMinute($minute, $format=null) {
    return $this->_compare(self::MINUTE, $minute, $format);
  }

  /**
   * difference between two minutes
   *
   * @param  integer|string|array|Of_Date  $minute  Minute to diff 
   * @param string $format Format of the input string OPTIONAL
   * @return integer  difference in minutes 
   */
  public function diffMinute($minute, $format=null) {
    return $this->_diff(self::MINUTE, $minute, $format);
  }


  /**
   * Returns this object's internal second .
   * If the second is too large for integers, then the return value will be a string.
   * This function does not return the second as an object.
   *
   * @return integer|string  UNIX second
   */
  public function getSecond() {
    return $this->format(self::SECOND);
  }

  /**
   * Sets a new second
   *
   * @param  integer|string|array|Of_Date  $second  Second to set
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */

  public function setSecond($second, $format=null) {
    return $this->_set(self::SECOND, $second, $format);
  }

  /**
   * add a second
   *
   * @param  integer|string|array|Of_Date  $second  Second to add
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function addSecond($second, $format=null) {
    return $this->_add(self::SECOND, $second, $format);
  }

  /**
  * add seconds, after having set the timezone to UTC
  * then set back the original timezone
  **/
  public function addSecondsInUTC($seconds) {
    $tz = $this->getTimezone()->getName();
    $this->setTimezone(new DateTimeZone("GMT"));
    $this = $this->addSecond($seconds);
    $this->setTimezone(new DateTimeZone($tz));
  }

  /**
   * substracts an second
   *
   * @param  integer|string|array|Of_Date  $second  Second to substract
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function subSecond($second, $format=null) {
    return $this->_sub(self::SECOND, $second, $format);
  }

  /**
   * Compares two seconds
   *
   * @param  integer|string|array|Of_Date  $second  Second to compare 
   * @param string $format Format of the input string OPTIONAL
   * @return integer  0 = equal, 1 = later, -1 = earlier
   */
  public function compareSecond($second, $format=null) {
    return $this->_compare(self::SECOND, $second, $format);
  }

  /**
   * difference between two seconds
   *
   * @param  integer|string|array|Of_Date  $second  Second to diff 
   * @param string $format Format of the input string OPTIONAL
   * @return integer  difference in seconds 
   */
  public function diffSecond($second, $format=null) {
    return $this->_diff(self::SECOND, $second, $format);
  }


  /**
   * Returns this object's internal week .
   * If the week is too large for integers, then the return value will be a string.
   * This function does not return the week as an object.
   *
   * @return integer|string  UNIX week
   */
  public function getWeek() {
    return $this->format(self::WEEK);
  }

  /**
   * Sets a new week
   *
   * @param  integer|string|array|Of_Date  $week  Week to set
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */

  public function setWeek($week, $format=null) {
    return $this->_set(self::WEEK, $week, $format);
  }

  /**
   * add a week
   *
   * @param  integer|string|array|Of_Date  $week  Week to add
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function addWeek($week, $format=null) {
    return $this->_add(self::WEEK, $week, $format);
  }

  /**
   * substracts an week
   *
   * @param  integer|string|array|Of_Date  $week  Week to substract
   * @param string $format Format of the input string OPTIONAL
   * @return Of_Date
   */  

  public function subWeek($week, $format=null) {
    return $this->_sub(self::WEEK, $week, $format);
  }

  /**
   * Compares two weeks
   *
   * @param  integer|string|array|Of_Date  $week  Week to compare 
   * @param string $format Format of the input string OPTIONAL
   * @return integer  0 = equal, 1 = later, -1 = earlier
   */
  public function compareWeek($week, $format=null) {
    return $this->_compare(self::WEEK, $week, $format);
  }

  /**
   * difference between two weeks
   *
   * @param  integer|string|array|Of_Date  $week  Week to diff 
   * @param string $format Format of the input string OPTIONAL
   * @return integer  difference in weeks 
   */
  public function diffWeek($week, $format=null) {
    return $this->_diff(self::WEEK, $week, $format);
  }

  /**
   * Set a part of a date with the given date coresponding
   * part. If part is null all given parts of the given date
   * will be set in current object.
   * 
   * @param string $parts  part to set
   * @param integer|string|array|Of_Date $time to set
   * @param string $format format of the $date param
   * @return of_Date
   */   
  private function _set($parts,$date, $format) {
    if(strpos(self::TIMESTAMP, $parts) !== FALSE) {
      $parts = self::ISO_8601;
    }
    $data = self::_parse($parts, $date, $format);
    if((isset($data['day']) && $data['day'] !== FALSE) ||
      (isset($data['month']) && $data['month'] !== FALSE) ||
      (isset($data['year']) && $data['year'] !== FALSE)) {
      $day = (isset($data['day']) && $data['day'] !== FALSE)?$data['day']:$this->format(self::DAY);
      $month = (isset($data['month']) && $data['month'] !== FALSE)?$data['month']:$this->format(self::MONTH_SHORT);
      $year = (isset($data['year']) && $data['year'] !== FALSE)?$data['year']:$this->format(self::YEAR);
      $this->setDate($year,$month, $day);
    }
    if((isset($data['hour']) && $data['hour'] !== FALSE) ||
      (isset($data['minute']) && $data['minute'] !== FALSE) ||
      (isset($data['second']) && $data['second'] !== FALSE)) {
      $hour = (isset($data['hour']) && $data['hour'] !== FALSE)?$data['hour']:$this->format(self::HOUR);
      $minute = (isset($data['minute']) && $data['minute'] !== FALSE)?$data['minute']:$this->format(self::MINUTE);
      $second = (isset($data['second']) && $data['second'] !== FALSE)?$data['second']:$this->format(self::SECOND);
      $this->setTime($hour,$minute, $second);
    }
    if((isset($data['week']) && $data['week'] !== FALSE) ||
      (isset($data['weekday']) && $data['weekday'] !== FALSE)) {
      $week = (isset($data['week']) && $data['week'] !== FALSE)?$data['week']:$this->format(self::WEEK);
      $weekday = (isset($data['weekday']) && $data['weekday'] !== FALSE)?$data['weekday']:$this->format(self::WEEKDAY_DIGIT);
      $weekday = ($weekday == 0)?7:$weekday;
      $year = $this->format('Y');
      if($week == 1 && $this->format('m') == '12') $year ++;
      elseif($week >= 52 && $this->format('m') == 1) $year --;
      $this->setISODate($year, $week, $weekday);
    }  
    return $this;
  }


  /**
   * Add a part of a date to the current object part. 
   * If part is null all given parts of the given date
   * will be add to current object.
   * 
   * @param string $part part to set
   * @param integer|string|array|Of_Date $time to set
   * @param string $format format of the $date param
   * @return of_Date
   */   
  private function _add($parts,$date, $format) { 
    $data = self::_parse($parts, $date, $format);
    $str = '';
    foreach($data as $part => $value) {
      // Needed because of 'next week' behavior modification (bug ?) in, at
      // least, PHP 5.2.[8|9|10]
      if($part == 'weekday') {
        $part = self::$DAY_LIST[$value];
        $value = 'next';
      } else {
        $value = '+'.$value;
      }
      if ($part == 'timestamp') {
        $part = 'second';
      } 
      $str .= " $value $part";
    }
    $this->custom($str);
    return $this;
  }
  /**
   * Substract a part of a date to the current object part. 
   * If part is null all given parts of the given date
   * will be subracted to current object.
   * 
   * @param string $part  part to set
   * @param integer|string|array|Of_Date $time to set
   * @param string $format format of the $date param
   * @return of_Date
   */   
  private function _sub($parts,$date, $format) { 
    $data = self::_parse($parts, $date, $format);
    $str = '';
    foreach($data as $part => $value) {
      if($part == 'weekday') {
        $part = self::$DAY_LIST[$value];
        $value = 1;
      } elseif ($part == 'timestamp') {
        $part = 'second';
      }
      $str .= " -$value $part";
    }
    $this->custom($str);
    return $this;    
  }

  /**
   * Compare a part of a date to the current object part. 
   * If part is null all given parts of the given date
   * will be compare.
   * 
   * @param string $part  part to set
   * @param integer|string|array|Of_Date $time to set
   * @param string $format format of the $date param
   * @return of_Date
   */   
  private function _compare($parts,$date, $format) { 
    $data = self::_parse($parts, $date, $format);

    if(isset($data['timestamp'])) {
      if((float)$data['timestamp'] > (float)$this->format(self::TIMESTAMP)) return -1;
      elseif((float)$data['timestamp'] < (float)$this->format(self::TIMESTAMP)) return 1;
      return 0;
    } 
    if(isset($data['year'])) {
      if((float)$data['year'] > (float)$this->format(self::YEAR)) return -1;
      elseif((float)$data['year'] < (float)$this->format(self::YEAR)) return 1;
    } 
    if(isset($data['month'])) {
      if((float)$data['month'] > (float)$this->format(self::MONTH)) return -1;
      elseif((float)$data['month'] < (float)$this->format(self::MONTH)) return 1;
    } 
    if(isset($data['dayofyear'])) {
      if((float)$data['dayofyear'] > (float)$this->format(self::DAYOFYEAR)) return -1;
      elseif((float)$data['dayofyear'] < (float)$this->format(self::DAYOFYEAR)) return 1;
    } 
    if(isset($data['week'])) {
      if((float)$data['week'] > (float)$this->format(self::WEEK)) return -1;
      elseif((float)$data['week'] < (float)$this->format(self::WEEK)) return 1;
    } 
    if(isset($data['day'])) {
      if((float)$data['day'] > (float)$this->format(self::DAY)) return -1;
      elseif((float)$data['day'] < (float)$this->format(self::DAY)) return 1;
    } 
    if(isset($data['weekday'])) {
      if((float)$data['weekday'] > (float)$this->format(self::WEEKDAY_DIGIT)) return -1;
      elseif((float)$data['weekday'] < (float)$this->format(self::WEEKDAY_DIGIT)) return 1;
    } 
    if(isset($data['hour'])) {
      if((float)$data['hour'] > (float)$this->format(self::HOUR)) return -1;
      elseif((float)$data['hour'] < (float)$this->format(self::HOUR)) return 1;
    } 
    if(isset($data['minute'])) {
      if((float)$data['minute'] > (float)$this->format(self::MINUTE)) return -1;
      elseif((float)$data['minute'] < (float)$this->format(self::MINUTE)) return 1;
    } 
    if(isset($data['second'])) {
      if((float)$data['second'] > (float)$this->format(self::SECOND)) return -1;
      elseif((float)$data['second'] < (float)$this->format(self::SECOND)) return 1;
    }
    return 0;    
  }

  /**
   * Compare a part of a date to the current object part. 
   * and return an array containing the difference between both
   * date
   * If part is null all given parts of the given date
   * will be compare.
   * 
   * @param string $part  part to set
   * @param integer|string|array|Of_Date $time to set
   * @param string $format format of the $date param
   * @return of_Date
   */   
  private function _diff($parts,$date, $format) {
    $data = self::_parse($parts, $date, $format);
    $diff = array();
    if(isset($data['timestamp'])) {
      $diff['timestamp'] = $this->format(self::TIMESTAMP) - $data['timestamp'] ;
    }     
    if(isset($data['year'])) {
      $diff['year'] = $this->format(self::YEAR) - $data['year'];
    } 
    if(isset($data['month'])) {
      $diff['month'] = $this->format(self::MONTH) - $data['month'];
    } 
    if(isset($data['dayofyear'])) {
      $diff['dayofyear'] =  $this->format(self::DAYOFYEAR) - $data['dayofyear'];
    } 
    if(isset($data['week'])) {
      $diff['week'] =  $this->format(self::WEEK) - $data['week'];
    } 
    if(isset($data['day'])) {
      $diff['day'] =   $this->format(self::DAY) - $data['day'];
    } 
    if(isset($data['weekday'])) {
      $diff['weekday'] = ($this->format(self::WEEKDAY_DIGIT - $data['weekday']) + 7) % 7 ;
    } 
    if(isset($data['hour'])) {
      $diff['hour'] =   $this->format(self::HOUR) - $data['hour'];
    } 
    if(isset($data['minute'])) {
      $diff['minute'] =   $this->format(self::MINUTE) - $data['minute'];
    } 
    if(isset($data['second'])) {
      $diff['second'] =   $this->format(self::SECOND) - $data['second'];
    }
    if(count($diff) == 1) {
      return array_pop($diff);
    } else {
      return $diff;
    }
  }

  /**
   * Parse a string into an date array with only $parts par set
   *
   * @param  string  $parts   Part string in PHP's date format
   * @param  integer|string|array|Of_Date  $date    Date string
   * @param  string  $format  Date format
   * @return array           Format string in ISO format
   */
  private function _parse($parts,$date, $format) {
    if(is_numeric($date)) {
      if($parts == null) $parts = self::TIMESTAMP;
      switch($parts) {
        case self::DAY : 
        case self::WEEK : 
        case self::WEEKDAY_DIGIT : 
        case self::MONTH : 
        case self::YEAR : 
        case self::DAYOFYEAR : 
        case self::HOUR : 
        case self::MINUTE : 
        case self::SECOND : 
        case self::TIMESTAMP :
          return array(self::$PHP_TO_STRING[$parts] => $date);
          break;
        default :
          $time = new of_Date("@$date");
          if(isset(self::$PHP_TO_STRING[$parts])) {
            return array(self::$PHP_TO_STRING[$parts] => $time->format($parts));
          // FIXME : Optimization purpose, should not be done this way!
          } elseif($parts == 'Y-m-d') {
            return array('year' => $time->format(self::YEAR),'month' => $time->format(self::MONTH), 'day' => $time->format(self::DAY));            
          }
          $data = $time->toArray();
      }
    } elseif(is_object($date)) {
      if(isset(self::$PHP_TO_STRING[$parts])) {
        return array(self::$PHP_TO_STRING[$parts] => $date->format($parts));
      // FIXME : Optimization purpose, should not be done this way!
      } elseif($parts == 'Y-m-d') {
        return array('year' => $date->format(self::YEAR),'month' => $date->format(self::MONTH), 'day' => $date->format(self::DAY));
      } elseif($parts == null) {
        return array('timestamp' => $date->format(self::TIMESTAMP));
      } else {
        $data = $date->toArray();
      }      
    } else {
      $data = self::_parseDate($date, $format);
    } 

    if(!is_null($parts)) {
      if(isset(self::$PHP_TO_STRING[$parts])) {
        $result = array(self::$PHP_TO_STRING[$parts] => $data[self::$PHP_TO_STRING[$parts]]);
      // FIXME : Optimization purpose, should not be done this way!
      } elseif($parts == 'Y-m-d') {
        $result = array('year' => $data['year'], 'month' => $data['month'], 'day' => $data['day']);
      } else {
        $parts_array =  str_split(strtr($parts, self::$PHP_TO_ISO));
        $filter = array();
        foreach($parts_array as $part) {
          if(isset(self::$ISO_TO_STRING[$part]) && isset($data[self::$ISO_TO_STRING[$part]]))
            $filter[self::$ISO_TO_STRING[$part]] = '';
        }         
        $result = array_intersect_key($data, $filter);
      }
    } else {
      $result = $data;
    }
    return $result;
  }

  /**
   * Parse a date into an date array.
   * There is untested case : 'r', 'j'....
   *
   * @param  string  $date    Date string
   * @param  string  $format  Format string in PHP's date format
   * @return string           Format string in ISO format
   */

  private static function _parseDate($date, $format) {
    if(is_null($format) || empty($format))  {
      if(in_array($date, self::$DAY_LIST)) {
        return array('weekday' => array_search($date, self::$DAY_LIST));
      } elseif (in_array($date, self::$SHORT_DAY_LIST)) {
        return array('weekday' => array_search($date, self::$SHORT_DAY_LIST));
      } elseif(isset($date, self::$MONTH_LIST)) {
        return array('month' => array_search($date, self::$MONTH_LIST));
      } elseif (in_array($date, self::$SHORT_MONTH_LIST)) {
        return array('month' => array_search($date, self::$SHORT_MONTH_LIST));
      } else {
        $format = self::ISO_8601;
      }      
    }
    $format = str_split(strtr($format, self::$PHP_TO_ISO));
    if(in_array('e',$format)) {
      $data = array_flip(self::$DAY_LIST);
      $date = strtr($date, $data);
      $data = array_flip(self::$SHORT_DAY_LIST);
      $date = strtr($date, $data);
    } 
    if(in_array('m',$format)) {
      $data = array_flip(self::$MONTH_LIST);
      $date = strtr($date, $data);
      $data = array_flip(self::$SHORT_MONTH_LIST);
      $date = strtr($date, $data);
    }     
    foreach($format as $key => $part) {
      if(isset(self::$ISO_TO_STRING[$part]))
        $result[self::$ISO_TO_STRING[$part]] .= $date[$key]; 
    }
    foreach($result as $datePart => $value) {
      switch ($datePart) {
        case 'hour' :
          if(strtoupper($result['AM']) === 'AM' && $value == '12') {
            $value = 0;
          } elseif(strtoupper($result['AM']) === 'PM' && $value != '12') {
            $value += 12;
          }
          $dateArray[$datePart] = $value;
          break;
        default :
         $dateArray[$datePart] = $value;
         break;
      }
    }
    return $dateArray;
  }


  /**
   * eval 
   * 
   * @param mixed $string 
   * @access public
   * @return void
   */
  public function custom($string) {
    $this->modify($string);
    //Seems to clear an internal cache... magical power inside
    @$this->modify('');
    return $this;
  }

  /**
   * Convert a Of_Date into an array 
   * 
   * @access public
   * @return void
   */
  public function toArray() {
    $array = date_parse($this->getIso());
    $array['timestamp'] = $this->format(self::TIMESTAMP);
    $array['weekday'] = $this->format(self::WEEKDAY_DIGIT);
    $array['week'] = $this->format(self::WEEK);
    return $array;
  }

  /**
   * __toString 
   * 
   * @access public
   * @return void
   */
  public function __toString() {
    return $this->toString();
  }

  /**
   * toString 
   * 
   * @access public
   * @return void
   */
  public function toString() {
    $this->setTimezone(new DateTimeZone('GMT'));
    $date = $this->get(self::DATABASE_DATE);
    $this->setTimezone(new DateTimeZone(self::$_options['timezone']));
    return $date;
  }

  /**
   * get a today Of_Date 
   * 
   * @static
   * @access public
   * @return Of_Date
   */
  public static function today() {
    if(!self::$_today) {
      self::$_today = new Of_Date();
    }
    return clone self::$_today;
  }

  /**
   * Is current object is today
   * 
   * @access public
   * @return boolean
   */
  public function isToday() {
    return ($this->format('Y-m-d') == self::today()->format('Y-m-d'));
  }

  public static function cmp($d1, $d2) {
    return $d1->compare($d2);
  }

  public static function cmpDate($d1, $d2) {
    return $d1->compareDateIso($d2);
  }
  
  public function setOriginalTimeZone ($timezone) {
    $this->originalTimeZone = $timezone;
  }
  
  public function getOriginalTimeZone () {
    return $this->originalTimeZone;
  }
  
  public static function secondToString($seconds) {

    if(!is_numeric($seconds)) {
      return null;
    }
    $units = array(
      $GLOBALS["l_week"]   => 7*24*3600,
      $GLOBALS["l_day"]    =>   24*3600,
      $GLOBALS["l_hour"]   =>      3600,
      $GLOBALS["l_minute"] =>        60,
      $GLOBALS["l_second"] =>         1
    );    
    $string = "";
    foreach ( $units as $name => $divisor ) {
      if ( $quot = intval($seconds / $divisor) ) {
        $string .= "$quot ".strtolower($name);
        $string .= (abs($quot) > 1 ? "s" : "") . ", ";
        $seconds -= $quot * $divisor;
      }
    }
    return substr($string, 0, -2);     
  }

  public function __sleep() {
    $this->serialized = $this->get(Of_Date::ISO_8601);
    return array('serialized', 'originalTimeZone', '_error');
  }

  public function __wakeup() {
    parent::__construct($this->serialized);
  }
}
