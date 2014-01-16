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



///////////////////////////////////////////////////////////////////////////////
// OBM - File : UserPattern.php                                              //
//     - Desc : User pattern model file                                      //
// 2010-01-26 - Vincent Alquier                                              //
///////////////////////////////////////////////////////////////////////////////

/**
 * Model class for user pattern
 * public interface :
 *   __construct($title, $description, $attributes)
 *   __clone()
 *   __get($key)
 *   get_id()
 *   get_domain_id()
 *   get_title()
 *   get_description()
 *   get_attributes()
 *   get_attribute($attribute)
 *   __set($key, $value)
 *   set_domain_id($domain_id)
 *   set_title($title)
 *   set_description($description)
 *   set_attributes($attributes)
 *   UserPattern::get($userpattern_id)
 *   UserPattern::all()
 *   save()
 *   applyTo(&$params, $attributes)
 **/
class UserPattern {
  protected $id;
  protected $domain_id;
  protected $title;
  protected $description;
  protected $attributes = array();

  static $allowed_attributes = array(
    'login'             => array( 'type'=>'string'  ),
    'commonname'        => array( 'type'=>'string'  ),
    'passwd'            => array( 'type'=>'password'),
    'hidden'            => array( 'type'=>'boolean' ),
    'profile'           => array( 'type'=>'string', 'validate_func'=>'validate_profile' ),
    'delegation'        => array( 'type'=>'string'  ),
    'delegation_target' => array( 'type'=>'string'  ),
    'title'             => array( 'type'=>'string'  ),
    'noexperie'         => array( 'type'=>'boolean' ),
    'datebegin'         => array( 'type'=>'date'    ),
    'dateexp'           => array( 'type'=>'date'    ),
    'phone'             => array( 'type'=>'string'  ),
    'phone2'            => array( 'type'=>'string'  ),
    'mobile'            => array( 'type'=>'string'  ),
    'fax'               => array( 'type'=>'string'  ),
    'fax2'              => array( 'type'=>'string'  ),
    'company'           => array( 'type'=>'string'  ),
    'direction'         => array( 'type'=>'string'  ),
    'service'           => array( 'type'=>'string'  ),
    'ad1'               => array( 'type'=>'string'  ),
    'ad2'               => array( 'type'=>'string'  ),
    'ad3'               => array( 'type'=>'string'  ),
    'zip'               => array( 'type'=>'string'  ),
    'town'              => array( 'type'=>'string'  ),
    'cdx'               => array( 'type'=>'string'  ),
    'desc'              => array( 'type'=>'string'  ),
    'web_perms'         => array( 'type'=>'boolean' ),
    'mail_perms'        => array( 'type'=>'boolean' ),
    'mail_server_id'    => array( 'type'=>'integer', 'validate_func'=>'validate_mail_server_id' ),
    'email'             => array( 'type'=>'string', 'validate_func'=>'validate_email' ),
    'mail_quota'        => array( 'type'=>'integer' ),
    'email_nomade'      => array( 'type'=>'string', 'validate_func'=>'validate_email' ),
    'nomade_perms'      => array( 'type'=>'boolean' ),
    'nomade_enable'     => array( 'type'=>'boolean' ),
    'nomade_local_copy' => array( 'type'=>'boolean' )
    //extra attributes are read from $GLOBALS['cgp_user']['user']['category'] and $GLOBALS['cgp_user']['user']['field']
  );

  static $allowed_keywords = array( '%kind%', '%lastname%', '%firstname%', '%login%', '%profile%', '%delegation%', '%delegation_target%', '%title%', '%phone%', '%phone2%', '%mobile%', '%fax%', '%fax2%', '%company%', '%direction%', '%service%', '%ad1%', '%ad2%', '%ad3%', '%zip%', '%town%', '%cdx%', '%desc%', '%today%', '%domain%' );
  //extra keywords are read from $GLOBALS['cgp_user']['user']['field']

  static $initialized = false;

  /**
   * Standard user pattern constructor
   * @param  string $title
   * @param  string $description
   * @param  array  $attributes
   * @access public
   **/
  public function __construct($title, $description = null, $attributes = array()) {
    $this->domain_id = $GLOBALS['obm']['domain_id'];
    $this->title = trim($title);
    $this->set_description($description);
    $this->set_attributes($attributes);
  }

  /**
   * Clone function
   * @access public
   **/
  public function __clone() {
    $this->id = null;
    $this->domain_id = $GLOBALS['obm']['domain_id'];
  }

  /**
   * Standard getter
   * @param  string $key
   * @access public
   * @return mixed
   **/
  public function __get($key) {
    $getter = "get_{$key}";
    if (method_exists($this,$getter)) {
      return $this->$getter();
    }
    //else
    return $this->get_attribute($key);
  }

  /**
   * id getter
   * @access public
   * @return int
   **/
  public function get_id() {
    return $this->id;
  }

  /**
   * domain_id getter
   * @access public
   * @return int
   **/
  public function get_domain_id() {
    return $this->domain_id;
  }

  /**
   * title getter
   * @access public
   * @return int
   **/
  public function get_title() {
    return $this->title;
  }

  /**
   * description getter
   * @access public
   * @return int
   **/
  public function get_description() {
    return $this->description;
  }

  /**
   * attributes getter
   * @access public
   * @return mixed
   **/
  public function get_attributes() {
    return $this->attributes;
  }

  /**
   * attribute getter
   * @param  string $attribute
   * @access public
   * @return mixed
   **/
  public function get_attribute($attribute) {
    return $this->attributes[$attribute];
  }

  /**
   * Standard setter
   * @param  string $key
   * @param  string $value
   * @access public
   **/
  public function __set($key, $value) {
    $setter = "set_{$key}";
    if (method_exists($this,$setter)) {
      $this->$setter($value);
    } else {
      $this->set_attributes(array($key=>$value));
      //will only work if $key is present in self::$allowed_attributes
    }
  }

  /**
   * domain_id setter
   * @param  int    $domain_id
   * @access public
   **/
  public function set_domain_id($domain_id) {
    $this->domain_id = $domain_id;
  }

  /**
   * title setter
   * @param  string $title
   * @access public
   **/
  public function set_title($title) {
    $this->title = trim($title);
  }

  /**
   * description setter
   * @param  string $description
   * @access public
   **/
  public function set_description($description = null) {
    $description = trim($description);
    $this->description = ($description!=='' ? $description : null);
  }

  /**
   * attributes setter
   * @param  array  $attributes
   * @access public
   **/
  public function set_attributes($attributes = array()) {
    $return = true;
    $attributes = array_intersect_key($attributes,self::$allowed_attributes);
    foreach ($attributes as $key => $value) {
      $validation_func = self::$allowed_attributes[$key]['validate_func'];
      if (empty($validation_func)) {
        $attribute_type = self::$allowed_attributes[$key]['type'];
        $validation_func = "validate_{$attribute_type}";
      }
      if ($this->$validation_func($value, $key)) {
        $this->attributes[$key] = $value;
      } else {
        $GLOBALS['err']['fields'][] = $key;
        $GLOBALS['err']['msg'] = $GLOBALS['l_invalid_field'];
        $return = false;
      }
    }
    if ($return) {
      $return = $this->check_circular_dependencies();
    }
    return $return;
  }

  /**
   * allow to check a string attribute before to set it
   * @param  string $attribute
   * @access protected
   **/
  protected function check_circular_dependencies() {
    $return = true;
    $level = 0;
    $dependencies = array();
    // search for direct dependencies
    foreach ($this->attributes as $key => $attributes) {
      if (preg_match_all('/%(.*?)%/',$attributes,$matches,PREG_PATTERN_ORDER)) {
        foreach ($matches[1] as $keyword) {
          $dependencies[$key][$keyword] = 1;
          $level = 1;
        }
      }
    }
    // search for indirect dependencies
    for ($i=1;$i<=$level;$i++) {
      foreach ($dependencies as $key => $depends_on) {
        foreach ($depends_on as $keyword => $useless) {
          if (is_array($dependencies[$keyword])) {
            foreach ($dependencies[$keyword] as $depend => $count) {
              if (!$dependencies[$key][$depend]) {
                $dependencies[$key][$depend] = $count+1;
                if ($count+1 > $level) $level = $level+1;
              }
            }
          }
        }
      }
    }
    // search for any field depending on itself
    foreach ($dependencies as $field => $depends_on) {
      if ($depends_on[$field]) {
        $GLOBALS['err']['fields'][] = $field;
        $GLOBALS['err']['msg'] = $GLOBALS['l_circular_dependencies'];
        $return = false;
      }
    }
    return $return;
  }

  /**
   * allow to check a string attribute before to set it
   * @param  string $value
   * @param  string $attribute
   * @access protected
   * @return boolean
   **/
  protected function validate_string(&$value,$attribute) {
    if ($value=='') {
      $value = null;
      return true;
    }
    if (preg_match_all('/%(.*?)%/',$value,$matches,PREG_PATTERN_ORDER)) {
      $keywords = $matches[0];  //all keywords found in the pattern
      $keywords = array_diff($keywords, self::$allowed_keywords);
      array_filter($keywords,create_function('$attr','return preg_match(\'/^%today([-+]\d*){0,1}%$/\',$attr);'));
      if (!empty($keywords)) {
        return false;
      }
    }

    return true;
  }

  /**
   * allow to check a password attribute before to set it
   * @param  string $value
   * @param  string $attribute
   * @access protected
   * @return boolean
   **/
  protected function validate_password(&$value,$attribute) {
    if ($value==='%random%')
      return true;
    if ($value=='') {
      $value = null;
      return true;
    }
    return false;
  }

  /**
   * allow to check an integer attribute before to set it
   * @param  string $value
   * @param  int    $attribute
   * @access protected
   * @return boolean
   **/
  protected function validate_integer(&$value,$attribute) {
    $value = trim($value);
    if (is_null($value) || $value=='')
      $value = null;
    else
      $value = intval($value);
    return true;
  }

  /**
   * allow to check a boolean attribute before to set it
   * @param  string $value
   * @param  int    $attribute
   * @access protected
   * @return boolean
   **/
  protected function validate_boolean(&$value,$attribute) {
    $value = intval($value);
    return true;
  }

  /**
   * allow to check a date attribute before to set it
   * @param  string $value
   * @param  string $attribute
   * @access protected
   * @return boolean
   **/
  protected function validate_date(&$value,$attribute) {
    $value = preg_replace('/\s/','',trim($value));
    if ($value=='') {
      $value = null;
      return true;
    }
    if (preg_match('/^%today([-+]\d*){0,1}%$/',$value)) {
      return true;
    }
    if ($value = of_isodate_convert($value,true,true)) {
      return true;
    }
    $value = null;
    return false;
  }

  /**
   * allow to check any category attribute before to set it
   * @param  string $value
   * @param  string $attribute
   * @access protected
   * @return boolean
   **/
  protected function validate_category(&$value,$attribute) {
    //FIXME: would be better to check...
    return true;
  }

  /**
   * allow to check the profile field attribute before to set it
   * @param  string $value
   * @param  string $attribute
   * @access protected
   * @return boolean
   **/
  protected function validate_profile(&$value,&$attribute) {
    $profiles = array_keys(get_all_profiles(false));
    if (in_array($value,$profiles))
      return true;
    $value = null;
    return false;
  }

  /**
   * allow to check the mail_server_id field attribute before to set it
   * @param  string $value
   * @param  string $attribute
   * @access protected
   * @return boolean
   **/
  protected function validate_mail_server_id(&$value,$attribute) {
    //FIXME: would be better to check...
    return true;
  }

  /**
   * allow to check the email field attribute before to set it
   * @param  string $value
   * @param  string $attribute
   * @access protected
   * @return boolean
   **/
  protected function validate_email(&$value,$attribute) {
    if (is_array($value)) {
      $mails = array();
      foreach ($value as $email) {
        $email = trim($email);
        if (!empty($email)) {
          $mails[] = $email;
        }
      }
      $value = implode("\r\n",$mails);
    }
    return true;
  }

  /**
   * Used to retrieve user pattern from the database
   * @param  int    $userpattern_id
   * @access public
   * @return UserPattern
   **/
  static public function get($userpattern_id) {
    global $cdg_sql, $err;

    $obm_q = new DB_OBM;
    $db_type = $obm_q->type;
    $timecreate = sql_date_format($db_type, "timecreate", "timecreate");
    $timeupdate = sql_date_format($db_type, "timeupdate", "timeupdate");

    // WHERE construction
    $where = array();

    $id = sql_parse_id($userpattern_id,true);
    $where[]= "id $id";

    //multidomain
    if (!$GLOBALS['obm']['domain_global']) {
      $domain_id = sql_parse_id($GLOBALS['obm']['domain_id'], true);
      $where[] = "((domain_id $domain_id) OR (domain_id IN (SELECT d.domain_id FROM Domain d WHERE d.domain_global=TRUE)))";
    }

    if (!empty($where))
      $whereq = 'WHERE '.implode(' AND ',$where);

    // Querying
    $query = "SELECT
        id,
        domain_id,
        $timecreate,
        $timeupdate,
        title,
        description
      FROM userpattern
      $whereq";
    display_debug_msg($query, $cdg_sql, "UserPattern::get()");
    $obm_q->query($query);

    if (!$obm_q->next_record()) {
      $err['msg'] = $GLOBALS['l_id_error'];
      return false;
    }
    //else
    $pattern = new UserPattern($obm_q->f('title'),$obm_q->f('description'));
    $pattern->id = $obm_q->f('id');
    $pattern->domain_id = $obm_q->f('domain_id');
    $pattern->loadAttributes();
    return $pattern;
  }

  /**
   * Return a list of all user patterns
   * @param  int    $domain_id  get all patterns for this domain (only if we are in the global domain)
   * @access public
   * @return array              list of id => title
   **/
  static public function all($domain_id = null) {
    global $cdg_sql;

    $obm_q = new DB_OBM;

    //multidomain
    if (!$GLOBALS['obm']['domain_global']) {
      $domain_id = sql_parse_id($GLOBALS['obm']['domain_id'], true);
      $where = "(domain_id $domain_id)";
    } else {
      $domain_id = sql_parse_id($domain_id, true);
      $where = "(domain_id $domain_id)";
    }
    if (!empty($where))
      $whereq = "WHERE $where";

    // Querying
    $query = "SELECT id, title FROM userpattern $whereq";
    display_debug_msg($query, $cdg_sql, "UserPattern::all()");
    $obm_q->query($query);

    $return = array();
    while ($obm_q->next_record()) {
      $id = $obm_q->f('id');
      $return[$id] = $obm_q->f('title');
    }

    return $return;
  }

  static public function getDomainPatternsAndGlobalPatterns($domain_id) {
    global $cdg_sql;

    $obm_q = new DB_OBM;

    // Querying
    $query = "SELECT id, title
        FROM userpattern u
        WHERE u.domain_id = ".sql_parse_id($domain_id)."
        OR u.domain_id IN (SELECT d.domain_id FROM Domain d WHERE d.domain_global=TRUE)";
    display_debug_msg($query, $cdg_sql, "UserPattern::getDomainPatternsAndGlobalPatterns()");
    $obm_q->query($query);

    $return = array();
    while ($obm_q->next_record()) {
      $id = $obm_q->f('id');
      $return[$id] = $obm_q->f('title');
    }

    return $return;
  }

  /**
   * Used to store user pattern into the database
   * @access public
   **/
  public function save() {
    if (empty($this->title)) {
      $GLOBALS['err']['msg'] = $GLOBALS['l_no_title_error'];
      return false;
    }
    // else
    if ($this->id) {
      $this->update();
    } else {
      $this->insert();
    }
    return true;
  }

  /**
   * Used to store user pattern into the database
   * @access public
   **/
  public function delete() {
    global $cdg_sql;
    if ($this->id) {
      $id = sql_parse_id($this->id,true);
      $obm_q = new DB_OBM;
      $query = "DELETE FROM userpattern WHERE id $id";
      display_debug_msg($query, $cdg_sql, 'UserPattern::delete()');
      $obm_q->query($query);
    }
    return true;
  }

  /**
   * Insert the user pattern into the database
   * @access protected
   **/
  protected function insert() {
    global $cdg_sql;

    $domain_id = sql_parse_id($this->domain_id);
    $title = addslashes($this->title);
    $description = addslashes($this->description);
    $usercreate = sql_parse_id($GLOBALS['obm']['uid']);

    $obm_q = new DB_OBM;
    $query = "INSERT INTO userpattern (domain_id, title, description, timecreate, usercreate)
      VALUES ($domain_id,'$title','$description',NOW(),$usercreate)";
    display_debug_msg($query, $cdg_sql, 'UserPattern::insert()');
    $obm_q->query($query);

    $this->id = $obm_q->lastid();

    $this->storeAttributes();
  }

  /**
   * Update the user pattern into the database
   * @access protected
   **/
  protected function update() {
    global $cdg_sql;

    $id = sql_parse_id($this->id,true);
    $domain_id = sql_parse_id($this->domain_id);
    $title = addslashes($this->title);
    $description = addslashes($this->description);
    $userupdate = sql_parse_id($GLOBALS['obm']['uid']);

    $obm_q = new DB_OBM;
    $query = "UPDATE userpattern SET
      domain_id = $domain_id,
      title = '$title',
      description = '$description',
      timeupdate = NOW(),
      userupdate = $userupdate
      WHERE id $id";
    display_debug_msg($query, $cdg_sql, 'UserPattern::update()');
    $obm_q->query($query);

    $this->purgeAttributes();
    $this->storeAttributes();
  }

  /**
   * Load user pattern attributes from the database
   * @access protected
   **/
  protected function loadAttributes() {
    global $cdg_sql;

    $obm_q = new DB_OBM;
    $id = sql_parse_id($this->id,true);
    $query = "SELECT attribute, value
      FROM userpattern_property
      WHERE userpattern_id $id";
    display_debug_msg($query, $cdg_sql, "UserPattern::loadAttributes()");
    $obm_q->query($query);

    $this->attributes = array();
    while ($obm_q->next_record()) {
      $this->attributes[$obm_q->f('attribute')] = $obm_q->f('value');
    }
  }

  /**
   * Store user pattern attributes into the database
   * @access protected
   **/
  protected function storeAttributes() {
    global $cdg_sql;

    $obm_q = new DB_OBM;
    $id = sql_parse_id($this->id);
    foreach ($this->attributes as $attribute => $value) {
      if (!is_null($value)) {
        $value = addslashes($value);
        $query = "INSERT INTO userpattern_property (userpattern_id, attribute, value)
          VALUES ($id, '$attribute', '$value')";
        display_debug_msg($query, $cdg_sql, "UserPattern::storeAttributes()");
        $obm_q->query($query);
      }
    }
  }

  /**
   * Delete all attributes stored into the database
   * @access protected
   **/
  protected function purgeAttributes() {
    global $cdg_sql;

    $obm_q = new DB_OBM;
    $id = sql_parse_id($this->id,true);
    $query = "DELETE FROM userpattern_property WHERE userpattern_id $id";
    display_debug_msg($query, $cdg_sql, "UserPattern::purgeAttributes()");
    $obm_q->query($query);
  }

  /**
   * Apply the template to the given parameters, do not override parameters
   * @param  array $params       parameters, updated with generated
   * @param  array $attributes   generate only the given user attributes... and dependecies (optional)
   * @access public
   **/
  public function applyTo(&$params, $attributes=null) {
    if (is_null($attributes) || !is_array($attributes)) {
      $attributes = array_keys($this->attributes);
    }
    foreach ($params as $attr => $value) {
      if (empty($value) && ($value!=='0')) {
        $params[$attr] = null;
        unset($params[$attr]);
      }
    }

    // We assume there are no circular dependencies as set_attributes function must prevent it !
    foreach ($attributes as $attr) {
      if (!isset($params[$attr]))
        $this->generate($params, $attr);
    }

    if ($params['mail_perms']) {
      $params['email'] = explode("\r\n",$params['email']);
    }
    $params['email_nomade'] = explode("\r\n",$params['email_nomade']);
  }

  /**
   * Generate an attribute and it's potential dependencies
   * @param  array  $generated    array of all generated attributes
   * @param  string $attribute    the name of the attribute to generate
   * @access protected
   **/
  protected function generate(&$generated, $attribute) {
    $meta_attribute = self::$allowed_attributes[$attribute];
    if (!empty($meta_attribute) && !empty($this->attributes[$attribute])) {
      $type = $meta_attribute['type'];
      $generate_func = "generate_{$type}";
      $this->$generate_func($generated, $attribute);
    } else {
      $generated[$attribute] = '';
    }
  }

  /**
   * Generate a string attribute and it's potential dependencies
   * @param  array  $params       parameters
   * @param  array  $generated    array of all generated attributes
   * @param  string $attribute    the name of the attribute to generate
   * @access protected
   **/
  protected function generate_string(&$generated, $attribute) {
    $pattern = $this->attributes[$attribute];
    $matches = array();
    if (preg_match_all('/%(.*?)%/',$pattern,$matches,PREG_PATTERN_ORDER)) {
      $search = $matches[0];  //all keywords found in the pattern
      $replacement = array(); //values to replace keywords with
      $matches = $matches[1]; //keywords without both %
      foreach ($matches as $i => $keyword) {
        if (!isset($generated[$keyword])) {
          if (isset($this->attributes[$keyword])) {
            $this->generate($generated, $keyword);
          } else {
            $this->generate_extra($generated,$keyword);
          }
        }
        $replacement[$i] = isset($generated[$keyword]) ? $generated[$keyword] : '';
      }
      $generated[$attribute] = str_replace($search,$replacement,$pattern);
    } else {
      $generated[$attribute] = $pattern;
    }
  }

  /**
   * Generate a password attribute
   * @param  array  $generated    array of all generated attributes
   * @param  string $attribute    the name of the attribute to generate
   * @access protected
   **/
  protected function generate_password(&$generated, $attribute) {
    $pattern = $this->attributes[$attribute];
    //if ($pattern=='%random%')
    $length = 10;
    $i = 0;
    $charset = Array(
      'abcdefghjkmnpqrstuvwxyz',
      'ABCDEFGHJKLMNPQRSTUVWXYZ',
      '0123456789',
      '[-&~#{(\[|`_^@)\]=+}$%>,?;.:!\/])'
    );
    $rules = Array();
    $ruleIndex = rand(0,5);
    $rules[$ruleIndex] = 2;
    $ruleIndex = rand(0,3) + 5;
    $rules[$ruleIndex] = 0;
    $ruleIndex = rand(0,3) + 8;
    $rules[$ruleIndex] = 1;

    while($i <= $length) {
      $charsetIndex = 0;
      if($rules[$i]) {
        $charsetIndex = $rules[$i];
      } else {
        $charsetIndex = rand(0,4);
      }
      $generated[$attribute] .= $charset[$charsetIndex][rand(0,strlen($charset[$charsetIndex])-1)];
      $i++;
    }
  }

  /**
   * Generate a integer attribute and it's potential dependencies
   * @param  array  $generated    array of all generated attributes
   * @param  string $attribute    the name of the attribute to generate
   * @access protected
   **/
  protected function generate_integer(&$generated, $attribute) {
    $pattern = $this->attributes[$attribute];
    $generated[$attribute] = $pattern;
  }

  /**
   * Generate a boolean attribute and it's potential dependencies
   * @param  array  $generated    array of all generated attributes
   * @param  string $attribute    the name of the attribute to generate
   * @access protected
   **/
  protected function generate_boolean(&$generated, $attribute) {
    $pattern = $this->attributes[$attribute];
    $generated[$attribute] = $pattern;
  }

  /**
   * Generate a date attribute and it's potential dependencies
   * @param  array  $generated    array of all generated attributes
   * @param  string $attribute    the name of the attribute to generate
   * @access protected
   **/
  protected function generate_date(&$generated, $attribute) {
    $pattern = $this->attributes[$attribute];
    if ($pattern[0]=='%') {
      $inner_attr = substr($pattern, 1, -1);
      $this->generate_extra($generated,$inner_attr);
      $generated[$attribute] = $generated[$inner_attr];
    } else {
      $generated[$attribute] = of_date_upd_format($pattern,true);
    }
  }

  /**
   * Generate a category attribute and it's potential dependencies
   * @param  array  $generated    array of all generated attributes
   * @param  string $attribute    the name of the attribute to generate
   * @access protected
   **/
  protected function generate_category(&$generated, $attribute) {
    $pattern = $this->attributes[$attribute];
    $generated[$attribute] = $pattern;
  }

  /**
   * Generate an attribute and it's potential dependencies
   * @param  array  $generated    array of all generated attributes
   * @param  string $attribute    the name of the attribute to generate
   * @access protected
   **/
  protected function generate_extra(&$generated, $attribute) {
    if ($attribute=='domain') {
      $generated[$attribute] = $GLOBALS['obm']['domain_name'];
    } else {
      if (substr($attribute,0,5)=='today') {
        $date = new Of_Date();
        $add = intval(substr($attribute,5));
        $date->addDay($add);
        $generated[$attribute] = $date->getInputDate();
      }
    }
  }

  /**
   * Load extra attributes and keywords from $GLOBALS['cgp_user']['user']['field'] and $GLOBALS['cgp_user']['user']['category']
   * @access public
   **/
  public static function init() {
    if (self::$initialized)
      return;
    self::$initialized = true;

    $extra_fields = $GLOBALS['cgp_user']['user']['field'];
    if (is_array($extra_fields)) {
      foreach ($extra_fields as $fieldname => $properties) {
        if (!isset(self::$allowed_attributes[$fieldname])) {
          $type = ($properties['type']=='text' || $properties['type']=='textarea') ? 'string' : $properties['type'];
          if (!in_array($type, array('boolean','date','integer','password','string'))) {
            $type = 'string';
          }
          self::$allowed_attributes[$fieldname] = array( 'type' => $type );
          if (($type=='string') && !in_array("%{$fieldname}%",self::$allowed_keywords)) {
            self::$allowed_keywords[] = "%{$fieldname}%";
          }
        }
      }
    }

    $extra_categories = $GLOBALS['cgp_user']['user']['category'];
    if (is_array($extra_categories)) {
      foreach ($extra_categories as $fieldname => $properties) {
        if (!isset(self::$allowed_attributes[$fieldname])) {
          self::$allowed_attributes[$fieldname] = array( 'type' => 'category' );
          //categories can't be used as keywords
        }
      }
    }
  }

  /**
   * Get keywords list
   * @param  string  $attribute    the name of the attribute to generate
   * @param  string  $pattern      optional filter pattern
   * @param  boolean $field_empty  false is the field if not empty
   * @access public
   * @return array
   **/
  public static function keywordsFor($attribute, $pattern='', $field_empty=true) {
    $attr = self::$allowed_attributes[$attribute];
    if (empty($attr))
      return array();
    //else
    $type = $attr['type'];
    return self::keywords($type, $pattern, $field_empty);
  }

  /**
   * Get keywords list
   * @param  string  $type           the type of the attribute to generate
   * @param  string  $pattern        optional filter pattern
   * @param  boolean $field_empty    false is the field if not empty
   * @access public
   * @return array
   **/
  public static function keywords($type, $pattern='', $field_empty=true) {

    if ($type=='string') {
      $keywords = self::$allowed_keywords;
      $keywords[] = '%today+';
      $keywords[] = '%today-';

    } elseif ($type=='password') {
      if ($field_empty) {
        $keywords = array('%random%');
      }

    } elseif ($type=='date') {
      if ($field_empty) {
        $keywords = array('%today%','%today+','%today-');
      }

    } else {
      return array();
    }

    if (!empty($pattern)) {
      $len = strlen($pattern);
      $keywords = array_filter($keywords,create_function('$keyword',"return substr(\$keyword,0,$len)=='$pattern';"));
    }

    sort($keywords);
    return $keywords;
  }
}

UserPattern::init();

