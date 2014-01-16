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



/* run_query_domain_init_data() */
require("$obm_root/php/domain/domain_query.inc");
/* genUniqueExtEventId() calendar_*_repeatition() */
require("$obm_root/php/calendar/calendar_query.inc");

class DummyGenerators extends PDO {

  /* Constants */
  private static $REPEAT_KIND   = array("'daily'", "'weekly'", "'monthlybyday'", "'monthlybydate'", "'yearly'");
  private static $PERMS = array("'user'", "'editor'", "'admin'");
  private static $BOOLEAN = array('TRUE', 'FALSE') ;
  private static $REPEAT_DAYS = array("'0010110'","'1010001'","'0111110'","'1000001'","'0110110'");
  private static $PARTS = array( "'ACCEPTED'", "'NEEDS-ACTION'", "'DECLINED'" );
  private static $EVENT_CATEGORY; 
  private static $KIND;

  /* Data from the database */
  public static $_dbtype;
  var $domain_id;
  var $available_kinds;
  var $available_event_cats;
  var $lastEntityId;

  /* Scales definitions */
  var $groupRatio;
  var $groupUserRatio;
  var $contactPrivateRatio;
  var $contactPublicRatio;
  var $resourceRatio;
  var $eventNormalRatio;
  var $eventRecurringRatio;
  var $contactData;
  var $eventMeetingRatio;
  var $rights;

  /* Our stuff */
  var $domain;
  var $today;
  var $eventManagerSet;
  var $available_repeatkind;
  var $userEntityIdSet;
  var $contactEntityIdSet;

  /**
   * __construct 
   * 
   * @param string $obmpath 
   * @access public
   * @return void
   */
  public function __construct($obmpath = '.') {
    $ini = "$obmpath/conf/obm_conf.ini";
    if (!$settings = parse_ini_file($ini, TRUE)) {
      print "Unable to open $ini\n";
      exit(1);
    }

    $this->_dbtype = strtolower($settings['global']['dbtype']);
    define('DBTYPE',$this->_dbtype);
    $host   = $settings['global']['host'];
    $db     = $settings['global']['db'];
    $user   = $settings['global']['user'];
    $pass   = $settings['global']['password'];
    return parent::__construct($this->_dbtype.":host=$host;dbname=$db", $user, $pass);
  }

  /**
   * initialize 
   * 
   * @access public
   * @return void
   */
  public function initialize() {
    /* read scales from conf.ini*/
    if(file_exists($GLOBALS['confFile'])) {
      $ratios = parse_ini_file($GLOBALS['confFile'],true);
    } else {
      print "\nPlease edit conf.ini.sample and save it as conf.ini\n";
      exit(1);
    }
    $this->groupRatio           = floatval($ratios['entity.group']['ratio']);
    $this->groupUserRatio       = floatval($ratios['entity.group.user']['ratio']);
    $this->contactPrivateRatio  = floatval($ratios['entity.contact.private']['ratio']);
    $this->contactPublicRatio   = floatval($ratios['entity.contact.public']['ratio']);
    $this->resourceRatio        = floatval($ratios['entity.resource']['ratio']);
    $this->eventNormalRatio     = floatval($ratios['entity.event.normal']['ratio']);
    $this->eventRecurringRatio  = floatval($ratios['entity.event.recurring']['ratio']);
    $this->eventMeetingRatio    = floatval($ratios['entity.event.meeting']['ratio']);
    $this->eventMeetingUserRatio= floatval($ratios['entity.event.meeting.user']['ratio']);
    $this->eventAllDayRatio     = floatval($ratios['entity.event.allday']['ratio']);
    /* 'Email' => 2 will create rand(0,2) mails per contact */
    $this->contactData = array (
      'Email'   => floatval($ratios['entity.contact.data']['email']),
      'Website' => floatval($ratios['entity.contact.data']['website']),
      'IM'      => floatval($ratios['entity.contact.data']['address']),
      'Address' => floatval($ratios['entity.contact.data']['phone']),
      'Phone'   => floatval($ratios['entity.contact.data']['im'])
    );
    $this->eventData = array (
      'maxEventDuration' => floatval($ratios['entity.event.data']['duration'])
    );
    /* The propabilities for a user/group to get each right
       (floats between 0 and 1) */
    $this->rights = array ( 
      'calendar' => array ( 
        'user' => array ( 
          'access'      => floatval($ratios['right.calendar.user']['access']), 
          'read'        => floatval($ratios['right.calendar.user']['read']),  
          'write'       => floatval($ratios['right.calendar.user']['write']),  
          'admin'       => floatval($ratios['right.calendar.user']['admin'])
        ),
        'group'         => array ( 
          'access'      => floatval($ratios['right.calendar.group']['access']), 
          'read'        => floatval($ratios['right.calendar.group']['read']),  
          'write'       => floatval($ratios['right.calendar.group']['write']),  
          'admin'       => floatval($ratios['right.calendar.group']['admin'])          
        ),
      ),
      'resource' => array ( 
        'user' => array(
          'access'      => floatval($ratios['right.resource.user']['access']), 
          'read'        => floatval($ratios['right.resource.user']['read']),  
          'write'       => floatval($ratios['right.resource.user']['write']),  
          'admin'       => floatval($ratios['right.resource.user']['admin'])
          ),
          'group' => array (
            'access'      => floatval($ratios['right.resource.group']['access']), 
            'read'        => floatval($ratios['right.resource.group']['read']),  
            'write'       => floatval($ratios['right.resource.group']['write']),  
            'admin'       => floatval($ratios['right.resource.group']['admin'])          
        )
      ),
      'contact' => array ( 
        'user' => array(
          'access'      => floatval($ratios['right.contact.user']['access']), 
          'read'        => floatval($ratios['right.contact.user']['read']),  
          'write'       => floatval($ratios['right.contact.user']['write']),  
          'admin'       => floatval($ratios['right.contact.user']['admin'])
        ),
        'group' => array (
          'access'      => floatval($ratios['right.contact.group']['access']), 
          'read'        => floatval($ratios['right.contact.group']['read']),  
          'write'       => floatval($ratios['right.contact.group']['write']),  
          'admin'       => floatval($ratios['right.contact.group']['admin'])        
        )
      )
    );
    /* Our stuff */
    $this->today = date('Y-m-d');

    /* Add a domain */
    $this->query("INSERT INTO Domain (domain_timeupdate,domain_timecreate,domain_userupdate,domain_usercreate,domain_label, domain_description, domain_name, domain_alias)
    VALUES (null, NOW(), null,  1, '".$GLOBALS['obm']['domain_label']."', '', '".$GLOBALS['obm']['domain_name']."', '')");

    $this->domain = $this->lastInsertId('Domain','domain_id');
    /* Add related entity */
    $entityId = $this->newEntity();
    $this->query("INSERT INTO DomainEntity (domainentity_domain_id, domainentity_entity_id) VALUES ( ".$this->domain.", $entityId)");
    /* Fill it with initial data from the global domain */
    run_query_domain_init_data($this->domain);


    /* Keep infos we will need */
    $res = $this->query("SELECT kind_id FROM Kind WHERE kind_domain_id = '$this->domain'");
    DummyGenerators::$KIND = $res->fetchAll(PDO::FETCH_COLUMN);
    unset($res); // Next queries would fail otherwise

    $res = $this->query("SELECT eventcategory1_id FROM EventCategory1 WHERE eventcategory1_domain_id = '$this->domain'");
    DummyGenerators::$EVENT_CATEGORY = $res->fetchAll(PDO::FETCH_COLUMN);
    $this->setup();
  }

  /**
   * genDummyData 
   * 
   * @param mixed $usersCount 
   * @access public
   * @return void
   */
  public function genDummyData($usersCount) {
    
    print "Initializing data...";
    $microstart = microtime(true);
    $this->initialize();
    $groupsCount = ceil($usersCount * $this->groupRatio);
    $calendarsCount = $usersCount; 
    $contactPrivateCount = $usersCount * $this->contactPrivateRatio;
    $contactPublicCount = $usersCount * $this->contactPublicRatio;
    $contactCount = $contactPublicCount + $contactPrivateCount;
    $resourcesCount = ceil($usersCount * $this->resourceRatio);
    $eventRecurringCount = $usersCount * $this->eventRecurringRatio;
    $eventNormalCount = $usersCount * $this->eventNormalRatio;
    $this->limit = 500000;


    $count=0;
    $counters = array($calendarsCount, $usersCount , $groupsCount , ($contactCount * array_sum($this->contactData)) , $resourcesCount , $eventNormalCount , $eventRecurringCount );
    $max = max($counters) + 1;
    $max = min(array($max, $this->limit));
    $this->query('INSERT INTO ids (mailing) VALUES (true)');
    while($count <= $max) {
      $this->query('INSERT INTO ids (mailing) SELECT true FROM ids');
      $count = $this->lastInsertId('ids', 'id');
    }

    RandomData::getInstance(); 
    $microinit = microtime(true) - $microstart;
    print "\nDone in ".round($microinit,4)."s\n";
    $microstart = microtime(true);

    print "Inserting $usersCount users... ";
    $microtime = microtime(true);
    $firstId = $this->lastInsertId('UserObm','userobm_id') +1 ;
    $userIdSet = new IdIterator(array('start' => $firstId, 'len' => $usersCount));
    $firstId = $this->lastInsertId('Entity','entity_id') +1 ;
    $this->userEntityIdSet = new IdIterator(array('start' => $firstId, 'len' => $usersCount));
    $firstId = $this->lastInsertId('UGroup','group_id') +1 ;
    $userGidSet = new IdIterator(array('start' => $firstId, 'len' => $groupsCount, 'every' => 1/$this->groupRatio));

    $this->createUsers(clone $userIdSet, clone $userGidSet);
    $microtime = microtime(true) - $microtime;
    print "\nDone in ".round($microtime,4)."s (".round($microtime/$usersCount, 4).")\n";

    $microtime = microtime(true);
    $firstId = $this->lastInsertId('Entity','entity_id') +1 ;
    $this->groupEntityIdSet = new IdIterator( array('start' => $firstId, 'len'   => $groupsCount) );
    print "Inserting $groupsCount groups... ";
    $firstId = $this->lastInsertId('UGroup','group_id') +1 ;
    $groupIdSet = new IdIterator(array('start' => $firstId, 'len' => $groupsCount));
    $this->createGroups(clone $groupIdSet);
    $microtime = microtime(true) - $microtime;
    print "\nDone in ".round($microtime,4)."s (".round($microtime/$groupsCount, 4).")\n";

    $microtime = microtime(true);
    print "Randomly linking $usersCount users to $groupsCount groups... ";
    $this->createUserGroupLink(clone $userIdSet, clone $groupIdSet);
    $microtime = microtime(true) - $microtime;
    print "\nDone in ".round($microtime,4)."s (".round($microtime/$groupsCount, 4).")\n";

    /* Contacts */

    $microtime = microtime(true);
    $firstId = $this->lastInsertId('Entity','entity_id') +1 ;
    $this->contactEntityIdSet = new IdIterator( array('start' => $firstId, 'len'   => $contactCount) );
    $this->contactPrivateEntityIdSet = new IdIterator( array('start' => $firstId, 'len'   => $contactPrivateCount) );
    print "Inserting $this->contactPrivateRatio privates contacts for each user (total $contactPrivateCount contacts)... ";
    print "\n And \n";
    print "Inserting $this->contactPublicRatio publics contacts for each user (total $contactPublicCount contacts)... ";
    $this->createContacts($contactPrivateCount, $contactPublicCount);
    $microtime = microtime(true) - $microtime;
    print "\nDone in ".round($microtime,4)."s (".round($microtime/$contactCount, 4).")\n";

    $microtime = microtime(true);
    print "Randomly inserting informations for all the $contactCount contacts:\n";
    $this->createContactsExtInfos(clone $this->contactEntityIdSet);
    $microtime = microtime(true) - $microtime;
    print "\nDone in ".round($microtime,4)."s (".round($microtime/($contactCount * array_sum($this->contactData)),4).")\n";

    /* Resources */
    $microtime = microtime(true);
    $firstId = $this->lastInsertId('Entity','entity_id') +1 ;
    $this->resourceEntityIdSet = new IdIterator( array('start' => $firstId, 'len'   => $resourcesCount) );
    print "Inserting $resourcesCount resources... ";
    $this->createResources(clone $userIdSet, $resourcesCount);
    $microtime = microtime(true) - $microtime;
    print "\nDone in ".round($microtime,4)."s (".round($microtime/$resourcesCount, 4).")\n";

    /* Events */
    $microtime = microtime(true);
    $firstId = $this->lastInsertId('Entity','entity_id') +1 ;
    $this->calendarEntityIdSet = new IdIterator( array('start' => $firstId, 'len'   => $calendarsCount) );    
    print "Inserting $usersCount calendars... ";
    $this->createCalendars(clone $userIdSet);
    $microtime = microtime(true) - $microtime;
    print "\nDone in ".round($microtime,4)."s (".round($microtime/$usersCount, 4).")\n";

    $microtime = microtime(true);
    $firstId = $this->lastInsertId('Event','event_id') +1 ;
    $eventIdsSet = new IdIterator( array('start' => $firstId, 'len'   => ($eventNormalCount + $eventRecurringCount) ));
    print "Inserting $this->eventRecurringRatio repeating events "
      ."for each user (total $eventRecurringCount), with "
      .(int)($this->eventMeetingRatio * 100)."% meetings... ";
    print "\n And \n";
    print "Inserting $this->eventNormalRatio non-repeating events for each user (total $eventNormalCount)... ";
    $this->createEvents(clone $userIdSet, $this->eventRecurringRatio,true);
    $microtime = microtime(true) - $microtime;
    print "\nDone in ".round($microtime,4)."s (".round($microtime/($eventNormalCount + $eventRecurringCount),4).")\n";


    $ratio = $eventIdsSet->getSize() * $this->eventMeetingRatio * 6;
    $microtime = microtime(true);
    print "Randomly creating $ratio links between users to events and linking all owner to their events...";
    $this->createEventLinks($eventIdsSet);
    $microtime = microtime(true) - $microtime;
    print "\nDone in ".round($microtime,4)."s (".round($microtime/($eventNormalCount + $eventRecurringCount),4).")\n";

    print "Now unsetting all tmp data and updating sequence \n";
    $this->tearDown();
    $microtime = microtime(true) - $microstart;
    print "Script execution time : ".round($microtime + $microinit, 4)."s (".round($microinit, 4)."s for init, ".round($microtime/$usersCount, 4)." per user, ".round($microtime/(array_sum($counters)+$contactCount*array_sum($this->contactData)), 4)." per entity)\n";
  }
  

  /**
   * createContactsExtInfos 
   * 
   * @param mixed $entityIdSet 
   * @access public
   * @return void
   */
  public function createContactsExtInfos($entityIdSet) {
    foreach($this->contactData as $info => $randmax) {
      print "  $info... ";
      $func = "create$info";
      $this->$func(clone $entityIdSet, $randmax);
      print "done\n";
    }
  }

  /**
   * createUsers 
   * 
   * @param mixed $userIdSet 
   * @param mixed $userGidSet 
   * @access public
   * @return void
   */
  public function createUsers($userIdSet, $userGidSet) {
    /* Static for each record */
    $diffIds = $userGidSet->start() - $userIdSet->start();
    if($diffIds > 0) $diffIds = '+'. $diffIds;
    $staticsColumns = array(
      'userobm_domain_id'  => "'$this->domain'",
      'userobm_password'   => "'password'",
      'userobm_timeupdate' => "NOW()",
      'userobm_timecreate' => "NOW()",
      'userobm_perms'      => $this->random(DummyGenerators::$PERMS),
      'userobm_userupdate' => "'1'",
      'userobm_usercreate' => "'1'",
      'userobm_kind'       => "NULL",
      'userobm_id' => '%id', 
      'userobm_login' => $this->concat("'u'","id"), 
      'userobm_uid' => '%id + 1000',
      'userobm_email' => $this->concat("'u'","id"),
      'userobm_gid' => '%id '.$diffIds,
      'userobm_lastname' => RandomData::getInstance()->getRandomLastname(), //lastname
      'userobm_firstname' => RandomData::getInstance()->getRandomFirstname() //firstname      
    );

    /* Prepared statments */
    $this->massiveInsert('UserObm', $staticsColumns, $userIdSet->start(), $userIdSet->getSize());
    $this->massiveInsert('Entity', array('entity_id' => '%id', 'entity_mailing' => 'true'), $this->userEntityIdSet->start(), $this->userEntityIdSet->getSize());
    $diffIds = $this->userEntityIdSet->start() - $userIdSet->start();
    if($diffIds > 0) $diffIds = '+'. $diffIds;
    $start = $userIdSet->start();
    $size = $userIdSet->getSize();
    while($size > 0) {
      $this->query("INSERT INTO UserEntity (userentity_user_id, userentity_entity_id) 
        SELECT userobm_id, userobm_id $diffIds FROM UserObm WHERE userobm_domain_id = $this->domain AND userobm_id >= $start LIMIT ".$this->limit);
      $start += $this->limit;
      $size -= $this->limit;
    }
  }

  /**
   * createGroups 
   * 
   * @param mixed $userGidSet 
   * @access public
   * @return void
   */
  public function createGroups($userGidSet) {
    /* For filling some dummy groups in UGroup */
    $staticsColumns = array (
      'group_domain_id'   => "'".$this->domain."'",
      'group_timeupdate'  => 'NOW()',
      'group_timecreate'  => 'NOW()',
      'group_userupdate'  => "'1'",
      'group_usercreate'  => "'1'",
      'group_id' => '%id',
      'group_gid' => 'group_id',
      'group_name' => $this->concat("'group'",'%id'),
      'group_desc' => RandomData::getInstance()->getRandomWord(60),
      'group_email' => $this->concat("'group'",'%id')
    );

    $this->massiveInsert('UGroup', $staticsColumns, $userGidSet->start(), $userGidSet->getSize());
    $this->massiveInsert('Entity', array('entity_id' => '%id', 'entity_mailing' => 'true'), $this->groupEntityIdSet->start(), $this->groupEntityIdSet->getSize());
    $diffIds = $this->groupEntityIdSet->start() - $userGidSet->start();
    if($diffIds > 0) $diffIds = '+'. $diffIds;
    $start = $userGidSet->start();
    $size = $userGidSet->getSize();
    while($size > 0) {
      $this->query("INSERT INTO GroupEntity (groupentity_group_id, groupentity_entity_id) 
        SELECT group_id, group_id $diffIds FROM UGroup WHERE group_domain_id = $this->domain AND group_id >= $start LIMIT ".$this->limit);
      $start += $this->limit;
      $size -= $this->limit;
    }
      
  }

  /**
   * createUserGroupLink 
   * 
   * @param mixed $userIdSet 
   * @param mixed $userGidSet 
   * @access public
   * @return void
   */
  public function createUserGroupLink($userIdSet, $userGidSet) {
    $query = implode(',', array_fill(0,$this->groupUserRatio, $this->random($userIdSet->start(), $userIdSet->last())));
    $this->query("INSERT INTO UserObmGroup (userobmgroup_group_id,userobmgroup_userobm_id) SELECT group_id, userobm_id FROM UGroup, UserObm WHERE userobm_id IN ($query)");
    $size = $this->groupUserRatio * $userGidSet->getSize();
    $start = 0; 
    while($size > 0) {
    $this->query('INSERT INTO of_usergroup (of_usergroup_group_id, of_usergroup_user_id ) 
      SELECT userobmgroup_group_id, userobmgroup_userobm_id FROM UserObmGroup INNER JOIN UserOBM ON userobmgroup_userobm_id = userobm_id 
      WHERE userobm_domain_id = '.$this->domain.' LIMIT '.$this->limit.' OFFSET '.$start);
      $start += $this->limit;
      $size -= $this->limit;
    }
    
  }

  /**
   * createResources 
   * 
   * @param mixed $userIdSet 
   * @param mixed $resourcesCount 
   * @access public
   * @return void
   */
  public function createResources($userIdSet, $resourcesCount) {
    /* Resource table */
    $staticsColumns = array( 
      'resource_domain_id'    => "'$this->domain'",
      'resource_timeupdate'   => "NOW()",
      'resource_timecreate'   => "NOW()",
      'resource_delegation'   => "NULL",
      'resource_usercreate'   => $this->random($userIdSet->start(), $userIdSet->last()), 
      'resource_name'         => RandomData::getInstance()->getRandomWord(8),
      'resource_description'  => RandomData::getInstance()->getRandomText(254), 
      'resource_qty'          => $this->random(0,20)
    );
    $id = $this->lastInsertId('Resource', 'resource_id') + 1;
    $this->massiveInsert('Resource', $staticsColumns, $id, $resourcesCount)."\n";
    $this->massiveInsert('Entity', array('entity_id' => '%id', 'entity_mailing' => 'true'), $this->resourceEntityIdSet->start(), $this->resourceEntityIdSet->getSize());
    $diffIds = $this->resourceEntityIdSet->start() - $id;
    if($diffIds > 0) $diffIds = '+'. $diffIds;
    $start = $id;
    $size = $this->resourceEntityIdSet->getSize();
    while($size > 0) {
      $this->query("INSERT INTO ResourceEntity (resourceentity_resource_id, resourceentity_entity_id) 
        SELECT resource_id, resource_id $diffIds FROM Resource WHERE resource_domain_id = $this->domain AND resource_id >= $start LIMIT ".$this->limit);
      $start += $this->limit;
      $size -= $this->limit;
    }    
    $this->setRights('resource');
  }

  /**
   * createCalendars 
   * 
   * @param mixed $userIdSet 
   * @access public
   * @return void
   */
  public function createCalendars($userIdSet) {

    $diffIds = $this->calendarEntityIdSet->start() - $userIdSet->start();
    $this->massiveInsert('Entity', array('entity_id' => '%id', 'entity_mailing' => 'true'), $this->calendarEntityIdSet->start(), $this->calendarEntityIdSet->getSize())."\n";
    if($diffIds > 0) $diffIds = '+'. $diffIds;
    $start = $userIdSet->start();
    $size = $this->calendarEntityIdSet->getSize();
    while($size > 0) {
      $this->query("INSERT INTO CalendarEntity (calendarentity_calendar_id, calendarentity_entity_id) 
        SELECT userobm_id, userobm_id $diffIds FROM UserObm WHERE userobm_domain_id = $this->domain AND userobm_id >= $start LIMIT ".$this->limit);
      $start += $this->limit;
      $size -= $this->limit;
    }        
  
    $this->setRights('calendar');
  }
  
  /**
   * createContacts 
   * 
   * @param mixed $isPrivate 
   * @param mixed $contactCount 
   * @param mixed $uid 
   * @param mixed $time 
   * @access public
   * @return void
   */
  public function createContacts($contactPrivateCount, $contactPublicCount) {
    /* Contact table */
    $staticsColumns = array (
      'contact_domain_id'           => "'$this->domain'",
      'contact_timeupdate'          => "NOW()",
      'contact_timecreate'          => "NOW()",
      'contact_usercreate'          => "userobm_id",
      'contact_userupdate'          => "userobm_id",
      'contact_datasource_id'       => "NULL",
      'contact_company_id'          => "NULL",
      'contact_company'             => "''",
      'contact_marketingmanager_id' => "NULL", 
      'contact_suffix'              => "''",
      'contact_manager'             => "''",
      'contact_assistant'           => "''",
      'contact_mailing_ok'          => "'1'",
      'contact_newsletter'          => "'1'",
      'contact_privacy'             => "1",
      'contact_date'                => "'$this->today'",
      'contact_origin'              => "'obm-dummyzator'",
      'contact_kind_id'             => $this->random(DummyGenerators::$KIND), 
      'contact_lastname'            => RandomData::getInstance()->getRandomLastname(),
      'contact_firstname'           => RandomData::getInstance()->getRandomFirstname(),
      'contact_middlename'          => RandomData::getInstance()->getRandomFirstname(),
      'contact_aka'                 => RandomData::getInstance()->getRandomFirstname(),
      'contact_sound'               => "0",
      'contact_comment'             => RandomData::getInstance()->getRandomText()
    );

    $id = $this->lastInsertId('Contact', 'contact_id') + 1;
    $this->massiveInsert('Contact', $staticsColumns, 1, $this->contactPrivateRatio, array('UserObm'));
    if($isPrivate) {
      $this->query('INSERT INTO SynchedContact (synchedcontact_user_id, synchedcontact_timestamp, synchedcontact_contact_id) 
        SELECT contact_userupdate, NOW(), contact_id FROM Contact WHERE contact_domain_id = '.$this->domain);
    }
    $staticsColumns['contact_privacy'] = '0';
    $this->massiveInsert('Contact', $staticsColumns, 1, $this->contactPublicRatio, array('UserObm'));
    $this->massiveInsert('Entity', array('entity_id' => '%id', 'entity_mailing' => 'true'), $this->contactEntityIdSet->start(), $this->contactEntityIdSet->getSize())."\n";
    $diffIds = $this->contactEntityIdSet->start() - $id;
    if($diffIds > 0) $diffIds = '+'. $diffIds;
    $start = $id;
    $size = $this->contactEntityIdSet->getSize();
    while($size > 0) {
      $this->query("INSERT INTO ContactEntity (contactentity_contact_id, contactentity_entity_id) 
        SELECT contact_id, contact_id $diffIds FROM Contact WHERE contact_domain_id = $this->domain AND contact_id >= $start LIMIT ".$this->limit);
      $start += $this->limit;
      $size -= $this->limit;
    }        

    $this->setRights('contact');
    $this->query('DELETE FROM EntityRight WHERE entityright_entity_id IN (SELECT contactentity_entity_id FROM ContactEntity INNER JOIN Contact ON contact_id = contactentity_contact_id WHERE contact_privacy = 0)');
    
  }

  /**
   * createAddress 
   * 
   * @param mixed $entityIdSet 
   * @param mixed $randmax 
   * @access public
   * @return void
   */
  public function createAddress($entityIdSet, $randmax) {
    $staticsColumns = array ( 
      'address_country' => "'FR'",
      'address_entity_id' => 'contactentity_entity_id',
      'address_street' => RandomData::getInstance()->getRandomText(64),
      'address_zipcode' => $this->random(1000,99999),
      'address_town' => RandomData::getInstance()->getRandomWord(12),
      'address_label' => "'WORK;X-OBM-Ref1'"
    );
    
    if($randmax > 0) {
      $this->massiveInsert('Address', $staticsColumns, 1, 1, array('ContactEntity'), false, array('contactentity_entity_id >= '.$this->contactEntityIdSet->start(), 'contactentity_entity_id <= '.$this->contactEntityIdSet->last()));
      $randmax --;
    }
    $staticsColumns['address_label'] = "'WORK;X-OBM-Ref2'";
    $this->massiveInsert('Address', $staticsColumns, 1 , $randmax, array('ContactEntity'), false, array('contactentity_entity_id >= '.$this->contactEntityIdSet->start(), 'contactentity_entity_id <= '.$this->contactEntityIdSet->last()));

  }

  /**
   * createEmail 
   * 
   * @param mixed $entityIdSet 
   * @param mixed $randmax 
   * @access public
   * @return void
   */
  public function createEmail($entityIdSet, $randmax)  {
    $staticsColumns = array ( 
      'email_entity_id' => 'contactentity_entity_id', 
      'email_address' => RandomData::getInstance()->getRandomText(64), 
      'email_label' => "'INTERNET;X-OBM-Ref1'"
    );
    
    if($randmax > 0) {
      $this->massiveInsert('Email', $staticsColumns, 1, 1, array('ContactEntity'), false, array('contactentity_entity_id >= '.$this->contactEntityIdSet->start(), 'contactentity_entity_id <= '.$this->contactEntityIdSet->last()));
      $randmax --;
    }
    $staticsColumns['email_label'] = "'INTERNET;X-OBM-Ref2'";
    $this->massiveInsert('Email', $staticsColumns, 1 , $randmax, array('ContactEntity'), false, array('contactentity_entity_id >= '.$this->contactEntityIdSet->start(), 'contactentity_entity_id <= '.$this->contactEntityIdSet->last()));
    
  }

  /**
   * createIM 
   * 
   * @param mixed $entityIdSet 
   * @param mixed $randmax 
   * @access public
   * @return void
   */
  public function createIM($entityIdSet, $randmax)  {
    $staticsColumns = array( 
      'im_entity_id' => 'contactentity_entity_id', 
      'im_address' => $this->concat(RandomData::getInstance()->getRandomWord(8),"'@jabber.fr'") ,
      'im_protocol' => "'XMPP'", 
      'im_label'  => "'XMPP;X-OBM-Ref1'" 
    );
    if($randmax > 0) {
      $this->massiveInsert('IM', $staticsColumns, 1, 1, array('ContactEntity'), false, array('contactentity_entity_id >= '.$this->contactEntityIdSet->start(), 'contactentity_entity_id <= '.$this->contactEntityIdSet->last()));
      $randmax --;
    }
    $staticsColumns['im_label'] = "'XMPP;X-OBM-Ref2'";
    $this->massiveInsert('IM', $staticsColumns, 1 , $randmax, array('ContactEntity'), false, array('contactentity_entity_id >= '.$this->contactEntityIdSet->start(), 'contactentity_entity_id <= '.$this->contactEntityIdSet->last()));

  }

  /**
   * createWebsite 
   * 
   * @param mixed $entityIdSet 
   * @param mixed $randmax 
   * @access public
   * @return void
   */
  public function createWebsite($entityIdSet, $randmax)  {
    $staticsColumns = array( 
      'website_entity_id' => 'contactentity_entity_id',
      'website_url' => $this->concat("'http://www.'",RandomData::getInstance()->getRandomWord(10),"'.org'"), 
      'website_label' => "'URL;X-OBM-Ref1'"
    );
    if($randmax > 0) {
      $this->massiveInsert('Website', $staticsColumns, 1, 1, array('ContactEntity'), false, array('contactentity_entity_id >= '.$this->contactEntityIdSet->start(), 'contactentity_entity_id <= '.$this->contactEntityIdSet->last()));
      $randmax --;
    }
    $staticsColumns['website_label'] = "'URL;X-OBM-Ref1'";
    $this->massiveInsert('Website', $staticsColumns, 1 , $randmax, array('ContactEntity'), false, array('contactentity_entity_id >= '.$this->contactEntityIdSet->start(), 'contactentity_entity_id <= '.$this->contactEntityIdSet->last()));

  }

  /**
   * createPhone 
   * 
   * @param mixed $entityIdSet 
   * @param mixed $randmax 
   * @access public
   * @return void
   */
  public function createPhone($entityIdSet, $randmax)  {
    $staticsColumns = array ( 
      'phone_entity_id' => 'contactentity_entity_id',
      'phone_number' => $this->random(0, 9999999999),
      'phone_label' => "'HOME;VOICE;X-OBM-Ref1'"
    );
    if($randmax > 0) {
      $this->massiveInsert('Phone', $staticsColumns, 1, 1, array('ContactEntity'), false, array('contactentity_entity_id >= '.$this->contactEntityIdSet->start(), 'contactentity_entity_id <= '.$this->contactEntityIdSet->last()));
      $randmax --;
    }
    $staticsColumns['phone_label'] = "'HOME;VOICE;X-OBM-Ref1'";
    $this->massiveInsert('Phone', $staticsColumns, 1 , $randmax, array('ContactEntity'), false, array('contactentity_entity_id >= '.$this->contactEntityIdSet->start(), 'contactentity_entity_id <= '.$this->contactEntityIdSet->last()));

  }

  /**
   * createEvents 
   * 
   * @param mixed $userIdSet 
   * @param mixed $userEventCount 
   * @param mixed $isRecurring 
   * @access public
   * @return void
   */
  public function createEvents($userIdSet, $userEventCount, $isRecurring) {
    /* Event table */
    $baseDate = date('Y-06-15 12:00:00');
    $staticsColumns = array(
      'event_domain_id'                 => "'$this->domain'",
      'event_timeupdate'                => "NOW()",
      'event_timecreate'                => "NOW()",
      'event_type'                      => "'VEVENT'",
      'event_origin'                    => "'obm-dummyzator'",
      'event_timezone'                  => "'Europe/Paris'",
      'event_opacity'                   => "'OPAQUE'",
      'event_properties'                => "'<extended_desc></extended_desc>'",
      'event_usercreate'                => 'userobm_id',
      'event_userupdate'                => 'userobm_id', 
      'event_ext_id'                    => "%id", 
      'event_owner'                     => 'userobm_id',
      'event_title'                     => RandomData::getInstance()->getRandomWord(),
      'event_location'                  => RandomData::getInstance()->getRandomWord(), 
      'event_category1_id'              => $this->random(DummyGenerators::$EVENT_CATEGORY),
      'event_priority'                  => $this->random(1,3),
      'event_privacy'                   => $this->random(0,1),
      //FIXME => Random
      'event_date'                      => $this->randomDate($baseDate), 
      'event_duration'                  => $this->random(900, $this->eventData['maxEventDuration'] * 3600), 
      'event_allday'                    => $this->random(DummyGenerators::$BOOLEAN),
      'event_description'               => RandomData::getInstance()->getRandomText(), 
      'event_repeatfrequence'           => 1, 
      'event_endrepeat'                 => "NULL",
      'event_repeatkind'                => "'none'",
      'event_repeatdays'                => "'0000000'"
    );
    $this->massiveInsert('Event', $staticsColumns, 1, $this->eventNormalRatio, array('UserObm'));
    $staticsColumns['event_repeatfrequence'] = $this->random(1,3);
    $staticsColumns['event_repeatkind'] = $this->random(DummyGenerators::$REPEAT_KIND);
    $staticsColumns['event_repeatdays'] = $this->random(DummyGenerators::$REPEAT_DAYS);
    //FIXME => Random
    $staticsColumns['event_endrepeat'] = "'".date('Y-12-31 00:00:00')."'";
    $this->massiveInsert('Event', $staticsColumns, 1, $this->eventRecurringRatio, array('UserObm'));
  }
  /**
   * createEvents 
   * 
   * @param mixed $userIdSet 
   * @param mixed $userEventCount 
   * @param mixed $isRecurring 
   * @access public
   * @return void
   */
  public function createEventLinks($eventIdsSet) {
    $staticsColumns = array(
      'tmpevent_id'=> $this->random($this->userEntityIdSet->start(), $this->userEntityIdSet->last()),
      'tmpentity_id'=> $this->random($eventIdsSet->start(), $eventIdsSet->last())
    );
    $start = 0;
    $total = $eventIdsSet->getSize() * $this->eventMeetingRatio * 6 ;
    $this->massiveInsert('TmpEventLink', $staticsColumns, 1, $total);
    exit();
    $size = $eventIdsSet->getSize();
    $start = 0;
    while($size > 0) {
      $this->query("INSERT INTO TmpEventLink (tmpevent_id, tmpentity_id) SELECT event_id, userentity_entity_id
        FROM Event 
        INNER JOIN UserEntity ON userentity_user_id =  event_owner WHERE  event_domain_id = ".$this->domain." LIMIT ".$this->limit." OFFSET $start");    

      $start += $this->limit;
      $size -= $this->limit;
    }
    $start = 0;
    $size = $total;
    while($size > 0) {    
      $this->query("INSERT INTO EventLink (eventlink_timeupdate, eventlink_timecreate, eventlink_required, eventlink_event_id, eventlink_usercreate,  eventlink_percent, eventlink_state, eventlink_entity_id)
        SELECT NOW(), NOW(), 'REQ', tmpevent_id, NULL, NULL, ".$this->random(DummyGenerators::$PARTS).", tmpentity_id 
        FROM TmpEventLink GROUP BY tmpentity_id, tmpevent_id LIMIT ".$this->limit." OFFSET $start");
      $start += $this->limit;
      $size -= $this->limit;
    }
    
    $this->query("DELETE FROM TmpEventLink");    
  }

  /**
   * executeOrDie 
   * 
   * @param mixed $sth 
   * @param mixed $changing 
   * @access public
   * @return void
   */
  public function executeOrDie($sth, $changing){
    $res = $sth->execute($changing);
    if($res === false) {
      $err = $sth->errorinfo();
      print "\nFailed to execute SQL prepared request:\n$err[2]\n";
      debug_print_backtrace();
      exit(1);
    }
  }

  /**
   * queryOrDie 
   * 
   * @param mixed $query 
   * @access public
   * @return void
   */
  public function queryOrDie($query) {
    $res = $this->query($query);
    if($res === false) {
      $err = $this->errorinfo();
      print "\nFailed to execute SQL request:\nQuery: $query\nError: $err[2]\n";
      debug_print_backtrace();
      exit(1);
    }
    return $res;
  }



  /**
   * massiveInsert 
   * 
   * @param mixed $table 
   * @param mixed $staticsColumns 
   * @param mixed $start 
   * @param mixed $size 
   * @param mixed $joins 
   * @param mixed $multidomain 
   * @param array $conditions 
   * @access public
   * @return void
   */
  public function massiveInsert($table, $staticsColumns, $start, $size, $joins = NULL, $multidomain=true, $conditions = array()) {
    if($size <= 0) return false;
    $before = $start - 1;
    $staticsColumnsNames = implode(",", array_keys($staticsColumns));
    $staticsColumnsValues = implode(",", $staticsColumns);
    if(is_array($joins) ) {
      foreach($joins as $join) {
        $select .= "$coma $join";
        $coma = ',';
        if($multidomain) $where .= " AND ".strtolower($join)."_domain_id = ".$this->domain;
      }
    }
    if(!empty($conditions)) $condition = 'AND '.implode(' AND ', $conditions);
    
    if($size > 1) {
      if($select) $select = ','.$select;
      while($size > 0) {
        $rows = min(array($size, $this->limit));
        $query = str_replace("%id","(id + $before)","INSERT INTO $table ($staticsColumnsNames $dynamicsColmunsNames)
          SELECT $staticsColumnsValues $dynamicsColumnsValues FROM ids $select WHERE id <= $rows $where $condition");
        $this->query($query);
        $size -= $this->limit;
        $before += $this->limit;
      } 
    } else {
      if($select) $select = ','.$select;
      $query = str_replace("%id","($start)","INSERT INTO $table ($staticsColumnsNames $dynamicsColmunsNames)
        SELECT $staticsColumnsValues $dynamicsColumnsValues FROM ids $select
        WHERE id = 1 $where $condition");
      $this->query($query);
    }
    return $query;
  }

  /**
   * massInsertorStatment 
   * 
   * @param mixed $table 
   * @param mixed $staticsColumns 
   * @param mixed $dynamicsColumns 
   * @access public
   * @return void
   */
  public function massInsertorStatment($table, $staticsColumns, $dynamicsColumns) {
    // Prepare the query
    $staticsColumnsNames = implode(",\n    ", array_keys($staticsColumns));
    $staticsColumnsValues = implode(",\n    ", array_values($staticsColumns));
    if(count($staticsColumns)) {
      $staticsColumnsNames .= ",\n    ";
      $staticsColumnsValues .= ",\n    ";
    }
    $dynamicsNames = implode(",\n    ", $dynamicsColumns);
    $dynamicsValues = '';
    for($i = 0 ; $i < count($dynamicsColumns)-1 ; $i++) {
        $dynamicsValues .= '?, ';
    }
    if(count($dynamicsColumns)) {
      $dynamicsValues .= "?\n";
    }

    $query = "INSERT INTO $table ($staticsColumnsNames$dynamicsNames) VALUES ( $staticsColumnsValues$dynamicsValues)";
    $sth = $this->prepare($query, array(PDO::ATTR_CURSOR => PDO::CURSOR_FWDONLY));
    if($sth === false) {
      $err = $this->errorinfo();
      print "\nFailed to prepare SQL request:\n$query\n$err[2]\n";
      debug_print_backtrace();
      exit(1);
    }
    return $sth;
  }


  /**
   * newEntity 
   * 
   * @access public
   * @return void
   */
  public function newEntity() {
    $this->query('INSERT INTO Entity (entity_mailing) VALUES (TRUE)');
    $this->lastEntityId = $this->lastInsertId('Entity','entity_id');
    return $this->lastEntityId;
  }

  /**
   * lastInsertId 
   * 
   * @param mixed $table 
   * @param mixed $field 
   * @access public
   * @return void
   */
  public function lastInsertId($table, $field) {
    $res = $this->queryOrDie("SELECT MAX($field) FROM $table");
    return $res->fetchColumn();
  }


  /**
   * setRights 
   * 
   * @param mixed $entity 
   * @access public
   * @return void
   */
  public function setRights($entity) {

    foreach($this->rights[$entity] as $consumer => $rights) {
      $public = array_keys($rights, 1);
      if(!empty($public)) {
        foreach($public as $right) {
          $keys[] = "entityright_$right";
          $values[] = 1;
          unset($rights[$right]);
        }
        $query = "INSERT INTO TmpEntityRight (entityright_entity_id, entityright_consumer_id, ".implode(',',$keys).") 
          SELECT ${entity}entity_entity_id, NULL, ".implode(',', $values)." FROM ".ucfirst($entity)."Entity 
          WHERE ".$entity."entity_entity_id >= ".$this->{$entity.'EntityIdSet'}->start()." 
          AND ".$entity."entity_entity_id <=". $this->{$entity.'EntityIdSet'}->last();
        $this->query($query);
      }
      $max = max($rights);
      if(!empty($rights) && $max != 0) {
        $keys = array();
        $values = array();
        $staticsColumns = array(
          'entityright_entity_id'=> $this->random($this->{$entity.'EntityIdSet'}->start(), $this->{$entity.'EntityIdSet'}->last()),
          'entityright_consumer_id'=> $this->random($this->{$consumer.'EntityIdSet'}->start(), $this->{$consumer.'EntityIdSet'}->last())
        );
        foreach($rights as $right => $value) {
          if($value == $max) {
            $staticsColumns["entityright_$right"] = 1;
          } elseif($value == 0) {
            $staticsColumns["entityright_$right"] = 0;
          } else {
            $ratio = ($max/$value);
            $staticsColumns["entityright_$right"] = 'floor('.$this->random(1,$ratio).'/'.$ratio.')';
          }
        }
        $total = ($this->{$entity.'EntityIdSet'}->getSize())* ($max) * 6 ;
        $this->massiveInsert('TmpEntityRight', $staticsColumns, 1, $total);
      } 
    }
    $query = "INSERT INTO EntityRight (entityright_entity_id, entityright_consumer_id, entityright_access, entityright_read, entityright_write, entityright_admin)
      SELECT entityright_entity_id, entityright_consumer_id, MAX(entityright_access), MAX(entityright_read), MAX(entityright_write), MAX(entityright_admin) FROM TmpEntityRight GROUP BY entityright_entity_id, entityright_consumer_id";
    $this->query($query);    
    $query = "DELETE FROM TmpEntityRight";
    $this->query($query);    
  }

  public static function substr($string, $from, $for) {
    if(DBTYPE == 'pgsql') {
      return "SUBSTRING($string FROM $from FOR $for)";
    } else {
      return "SUBSTR($string FROM $from FOR $for)";
    }
  }

  /**
   * concat 
   * 
   * @access public
   * @return void
   */
  public static function concat() {
    $strings = func_get_args();
    if(DBTYPE == 'pgsql') {
      return implode(' || ', $strings);
    } else {
      return "CONCAT(".implode(',', $strings).")";
    }
  }
  /**
   * random 
   * 
   * @access public
   * @return void
   */
  public static function random() {
    $scale = func_get_args();
    if(count($scale) > 0 && is_array($scale[0])) {
      $scale = $scale[0];
      foreach($scale as $index => $value) $query .= " WHEN $index THEN $value " ;
      return  "CASE ".self::random(0, count($scale) - 1).$query." END";
    }elseif(count($scale) > 0) {
      $scale[1] = $scale[1] - $scale[0] + 1;
      return "(floor(".self::random()."* $scale[1]) + $scale[0])";
    }
    if(DBTYPE == 'pgsql') {
      return "random()";
    } else {
      return "rand()";
    }
  }
  /**
   * randomDate 
   * 
   * @access public
   * @return void
   */
  public static function randomDate($date) {
    if(DBTYPE == 'pgsql') {
      $sqlDate = "timestamp '$date'"; 
      $sqlDate .=" + ".self::random(-5,6)." * INTERVAL '1 MONTH'";
      $sqlDate .=" + ".self::random(-15,13)." * INTERVAL '1 DAY'";
      $sqlDate .=" + ".self::random(-8,8)." * INTERVAL '1 HOUR'";
      $sqlDate .=" + ".self::random(0,3)." * INTERVAL '15 MINUTE'";
      return $sqlDate;
    } else {
      $sqlDate = "'$date'"; 
      $sqlDate .=" + INTERVAL ".self::random(-5,6)." * 1 MONTH";
      $sqlDate .=" + INTERVAL ".self::random(-15,13)." * 1 DAY";
      $sqlDate .=" + INTERVAL ".self::random(-8,8)." * 1 HOUR";
      $sqlDate .=" + INTERVAL ".self::random(0,3)." * 15 MINUTE";
      return $sqlDate;
    }
  }

  public function tearDown() {
    $this->query('DROP TABLE TmpEntityRight');
    $this->query('DROP TABLE TmpEventLink');
    $this->query('DROP TABLE ids');
    $this->query('DROP TABLE firstnames');
    $this->query('DROP TABLE texts');
    $this->query('DROP TABLE lastnames');
    $this->query('DROP TABLE words');

    if($this->_dbtype == 'pgsql') {
      $this->query("ALTER TABLE Account ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE AccountEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ActiveUserObm ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Address ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE CV ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE CalendarEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Campaign ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE CampaignDisabledEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE CampaignEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE CampaignMailContent ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE CampaignMailTarget ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE CampaignPushTarget ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE CampaignTarget ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Category ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE CategoryLink ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Company ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE CompanyActivity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE CompanyEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE CompanyNafCode ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE CompanyType ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Contact ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ContactEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ContactFunction ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ContactList ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Contract ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ContractEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ContractPriority ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ContractStatus ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ContractType ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Country ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE CvEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE DataSource ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Deal ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE DealCompany ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE DealCompanyRole ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE DealEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE DealStatus ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE DealType ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE DefaultOdtTemplate ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Deleted ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE DeletedContact ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE DeletedEvent ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE DeletedUser ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE DisplayPref ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Document ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE DocumentEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE DocumentLink ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE DocumentMimeType ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Domain ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE DomainEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE DomainProperty ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE DomainPropertyValue ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Email ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Entity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE EntityRight ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Event ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE EventAlert ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE EventCategory1 ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE EventEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE EventException ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE EventLink ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE GroupEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE GroupGroup ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Host ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE HostEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE IM ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Import ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ImportEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Incident ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE IncidentEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE IncidentPriority ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE IncidentResolutionType ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE IncidentStatus ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Invoice ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE InvoiceEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Kind ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Lead ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE LeadEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE LeadSource ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE LeadStatus ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE List ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ListEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE MailShare ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE MailboxEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE MailshareEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE OGroup ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE OGroupLink ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ObmBookmark ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ObmBookmarkProperty ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ObmInfo ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ObmSession ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ObmbookmarkEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE OgroupEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE OrganizationalChart ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE OrganizationalchartEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_Domain ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_DomainEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_EntityRight ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_GroupEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_Host ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_HostEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_MailShare ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_MailboxEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_MailshareEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_Service ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_ServiceProperty ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_UGroup ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_UserEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_UserObm ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_of_usergroup ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ParentDeal ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ParentdealEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Payment ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE PaymentEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE PaymentInvoice ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE PaymentKind ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Phone ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Profile ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ProfileEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ProfileModule ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ProfileProperty ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ProfileSection ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Project ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ProjectCV ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ProjectClosing ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ProjectEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ProjectRefTask ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ProjectTask ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ProjectUser ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Publication ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE PublicationEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE PublicationType ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE RGroup ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Region ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Resource ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ResourceEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ResourceGroup ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ResourceItem ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ResourceType ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ResourcegroupEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE SSOTicket ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Service ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE ServiceProperty ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Stats ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Subscription ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE SubscriptionEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE SubscriptionReception ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE SynchedContact ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE TaskEvent ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE TaskType ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE TimeTask ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE UGroup ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Updated ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Updatedlinks ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE UserEntity ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE UserObm ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE UserObmGroup ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE UserObmPref ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE UserObm_SessionLog ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE UserSystem ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE Website ENABLE TRIGGER ALL");
      $this->query("ALTER TABLE of_usergroup ENABLE TRIGGER ALL");      
      $this->query("SELECT setval('campaignmailtarget_campaignmailtarget_id_seq',1,false)");
      $this->query("SELECT setval('campaignmailtarget_campaignmailtarget_id_seq',MAX(campaignmailtarget_id)) FROM campaignmailtarget");
      $this->query("SELECT setval('account_account_id_seq',1,false)");
      $this->query("SELECT setval('account_account_id_seq',MAX(account_id)) FROM account");
      $this->query("SELECT setval('address_address_id_seq',1,false)");
      $this->query("SELECT setval('address_address_id_seq',MAX(address_id)) FROM address");
      $this->query("SELECT setval('campaign_campaign_id_seq',1,false)");
      $this->query("SELECT setval('campaign_campaign_id_seq',MAX(campaign_id)) FROM campaign");
      $this->query("SELECT setval('campaignmailcontent_campaignmailcontent_id_seq',1,false)");
      $this->query("SELECT setval('campaignmailcontent_campaignmailcontent_id_seq',MAX(campaignmailcontent_id)) FROM campaignmailcontent");
      $this->query("SELECT setval('campaignpushtarget_campaignpushtarget_id_seq',1,false)");
      $this->query("SELECT setval('campaignpushtarget_campaignpushtarget_id_seq',MAX(campaignpushtarget_id)) FROM campaignpushtarget");
      $this->query("SELECT setval('campaigntarget_campaigntarget_id_seq',1,false)");
      $this->query("SELECT setval('campaigntarget_campaigntarget_id_seq',MAX(campaigntarget_id)) FROM campaigntarget");
      $this->query("SELECT setval('category_category_id_seq',1,false)");
      $this->query("SELECT setval('category_category_id_seq',MAX(category_id)) FROM category");
      $this->query("SELECT setval('company_company_id_seq',1,false)");
      $this->query("SELECT setval('company_company_id_seq',MAX(company_id)) FROM company");
      $this->query("SELECT setval('companyactivity_companyactivity_id_seq',1,false)");
      $this->query("SELECT setval('companyactivity_companyactivity_id_seq',MAX(companyactivity_id)) FROM companyactivity");
      $this->query("SELECT setval('companynafcode_companynafcode_id_seq',1,false)");
      $this->query("SELECT setval('companynafcode_companynafcode_id_seq',MAX(companynafcode_id)) FROM companynafcode");
      $this->query("SELECT setval('companytype_companytype_id_seq',1,false)");
      $this->query("SELECT setval('companytype_companytype_id_seq',MAX(companytype_id)) FROM companytype");
      $this->query("SELECT setval('contact_contact_id_seq',1,false)");
      $this->query("SELECT setval('contact_contact_id_seq',MAX(contact_id)) FROM contact");
      $this->query("SELECT setval('contactfunction_contactfunction_id_seq',1,false)");
      $this->query("SELECT setval('contactfunction_contactfunction_id_seq',MAX(contactfunction_id)) FROM contactfunction");
      $this->query("SELECT setval('contract_contract_id_seq',1,false)");
      $this->query("SELECT setval('contract_contract_id_seq',MAX(contract_id)) FROM contract");
      $this->query("SELECT setval('contractpriority_contractpriority_id_seq',1,false)");
      $this->query("SELECT setval('contractpriority_contractpriority_id_seq',MAX(contractpriority_id)) FROM contractpriority");
      $this->query("SELECT setval('contractstatus_contractstatus_id_seq',1,false)");
      $this->query("SELECT setval('contractstatus_contractstatus_id_seq',MAX(contractstatus_id)) FROM contractstatus");
      $this->query("SELECT setval('contracttype_contracttype_id_seq',1,false)");
      $this->query("SELECT setval('contracttype_contracttype_id_seq',MAX(contracttype_id)) FROM contracttype");
      $this->query("SELECT setval('cv_cv_id_seq',1,false)");
      $this->query("SELECT setval('cv_cv_id_seq',MAX(cv_id)) FROM cv");
      $this->query("SELECT setval('datasource_datasource_id_seq',1,false)");
      $this->query("SELECT setval('datasource_datasource_id_seq',MAX(datasource_id)) FROM datasource");
      $this->query("SELECT setval('deal_deal_id_seq',1,false)");
      $this->query("SELECT setval('deal_deal_id_seq',MAX(deal_id)) FROM deal");
      $this->query("SELECT setval('dealcompany_dealcompany_id_seq',1,false)");
      $this->query("SELECT setval('dealcompany_dealcompany_id_seq',MAX(dealcompany_id)) FROM dealcompany");
      $this->query("SELECT setval('dealcompanyrole_dealcompanyrole_id_seq',1,false)");
      $this->query("SELECT setval('dealcompanyrole_dealcompanyrole_id_seq',MAX(dealcompanyrole_id)) FROM dealcompanyrole");
      $this->query("SELECT setval('dealstatus_dealstatus_id_seq',1,false)");
      $this->query("SELECT setval('dealstatus_dealstatus_id_seq',MAX(dealstatus_id)) FROM dealstatus");
      $this->query("SELECT setval('dealtype_dealtype_id_seq',1,false)");
      $this->query("SELECT setval('dealtype_dealtype_id_seq',MAX(dealtype_id)) FROM dealtype");
      $this->query("SELECT setval('defaultodttemplate_defaultodttemplate_id_seq',1,false)");
      $this->query("SELECT setval('defaultodttemplate_defaultodttemplate_id_seq',MAX(defaultodttemplate_id)) FROM defaultodttemplate");
      $this->query("SELECT setval('deleted_deleted_id_seq',1,false)");
      $this->query("SELECT setval('deleted_deleted_id_seq',MAX(deleted_id)) FROM deleted");
      $this->query("SELECT setval('deletedevent_deletedevent_id_seq',1,false)");
      $this->query("SELECT setval('deletedevent_deletedevent_id_seq',MAX(deletedevent_id)) FROM deletedevent");
      $this->query("SELECT setval('displaypref_display_id_seq',1,false)");
      $this->query("SELECT setval('displaypref_display_id_seq',MAX(display_id)) FROM displaypref");
      $this->query("SELECT setval('document_document_id_seq',1,false)");
      $this->query("SELECT setval('document_document_id_seq',MAX(document_id)) FROM document");
      $this->query("SELECT setval('documentmimetype_documentmimetype_id_seq',1,false)");
      $this->query("SELECT setval('documentmimetype_documentmimetype_id_seq',MAX(documentmimetype_id)) FROM documentmimetype");
      $this->query("SELECT setval('domain_domain_id_seq',1,false)");
      $this->query("SELECT setval('domain_domain_id_seq',MAX(domain_id)) FROM domain");
      $this->query("SELECT setval('email_email_id_seq',1,false)");
      $this->query("SELECT setval('email_email_id_seq',MAX(email_id)) FROM email");
      $this->query("SELECT setval('entity_entity_id_seq',1,false)");
      $this->query("SELECT setval('entity_entity_id_seq',MAX(entity_id)) FROM entity");
      $this->query("SELECT setval('entityright_entityright_id_seq',1,false)");
      $this->query("SELECT setval('entityright_entityright_id_seq',MAX(entityright_id)) FROM entityright");
      $this->query("SELECT setval('event_event_id_seq',1,false)");
      $this->query("SELECT setval('event_event_id_seq',MAX(event_id)) FROM event");
      $this->query("SELECT setval('eventcategory1_eventcategory1_id_seq',1,false)");
      $this->query("SELECT setval('eventcategory1_eventcategory1_id_seq',MAX(eventcategory1_id)) FROM eventcategory1");
      $this->query("SELECT setval('host_host_id_seq',1,false)");
      $this->query("SELECT setval('host_host_id_seq',MAX(host_id)) FROM host");
      $this->query("SELECT setval('im_im_id_seq',1,false)");
      $this->query("SELECT setval('im_im_id_seq',MAX(im_id)) FROM im");
      $this->query("SELECT setval('import_import_id_seq',1,false)");
      $this->query("SELECT setval('import_import_id_seq',MAX(import_id)) FROM import");
      $this->query("SELECT setval('incident_incident_id_seq',1,false)");
      $this->query("SELECT setval('incident_incident_id_seq',MAX(incident_id)) FROM incident");
      $this->query("SELECT setval('incidentpriority_incidentpriority_id_seq',1,false)");
      $this->query("SELECT setval('incidentpriority_incidentpriority_id_seq',MAX(incidentpriority_id)) FROM incidentpriority");
      $this->query("SELECT setval('incidentresolutiontype_incidentresolutiontype_id_seq',1,false)");
      $this->query("SELECT setval('incidentresolutiontype_incidentresolutiontype_id_seq',MAX(incidentresolutiontype_id)) FROM incidentresolutiontype");
      $this->query("SELECT setval('incidentstatus_incidentstatus_id_seq',1,false)");
      $this->query("SELECT setval('incidentstatus_incidentstatus_id_seq',MAX(incidentstatus_id)) FROM incidentstatus");
      $this->query("SELECT setval('invoice_invoice_id_seq',1,false)");
      $this->query("SELECT setval('invoice_invoice_id_seq',MAX(invoice_id)) FROM invoice");
      $this->query("SELECT setval('kind_kind_id_seq',1,false)");
      $this->query("SELECT setval('kind_kind_id_seq',MAX(kind_id)) FROM kind");
      $this->query("SELECT setval('lead_lead_id_seq',1,false)");
      $this->query("SELECT setval('lead_lead_id_seq',MAX(lead_id)) FROM lead");
      $this->query("SELECT setval('leadsource_leadsource_id_seq',1,false)");
      $this->query("SELECT setval('leadsource_leadsource_id_seq',MAX(leadsource_id)) FROM leadsource");
      $this->query("SELECT setval('leadstatus_leadstatus_id_seq',1,false)");
      $this->query("SELECT setval('leadstatus_leadstatus_id_seq',MAX(leadstatus_id)) FROM leadstatus");
      $this->query("SELECT setval('list_list_id_seq',1,false)");
      $this->query("SELECT setval('list_list_id_seq',MAX(list_id)) FROM list");
      $this->query("SELECT setval('mailshare_mailshare_id_seq',1,false)");
      $this->query("SELECT setval('mailshare_mailshare_id_seq',MAX(mailshare_id)) FROM mailshare");
      $this->query("SELECT setval('obmbookmark_obmbookmark_id_seq',1,false)");
      $this->query("SELECT setval('obmbookmark_obmbookmark_id_seq',MAX(obmbookmark_id)) FROM obmbookmark");
      $this->query("SELECT setval('obmbookmarkproperty_obmbookmarkproperty_id_seq',1,false)");
      $this->query("SELECT setval('obmbookmarkproperty_obmbookmarkproperty_id_seq',MAX(obmbookmarkproperty_id)) FROM obmbookmarkproperty");
      $this->query("SELECT setval('ogroup_ogroup_id_seq',1,false)");
      $this->query("SELECT setval('ogroup_ogroup_id_seq',MAX(ogroup_id)) FROM ogroup");
      $this->query("SELECT setval('ogrouplink_ogrouplink_id_seq',1,false)");
      $this->query("SELECT setval('ogrouplink_ogrouplink_id_seq',MAX(ogrouplink_id)) FROM ogrouplink");
      $this->query("SELECT setval('organizationalchart_organizationalchart_id_seq',1,false)");
      $this->query("SELECT setval('organizationalchart_organizationalchart_id_seq',MAX(organizationalchart_id)) FROM organizationalchart");
      $this->query("SELECT setval('parentdeal_parentdeal_id_seq',1,false)");
      $this->query("SELECT setval('parentdeal_parentdeal_id_seq',MAX(parentdeal_id)) FROM parentdeal");
      $this->query("SELECT setval('payment_payment_id_seq',1,false)");
      $this->query("SELECT setval('payment_payment_id_seq',MAX(payment_id)) FROM payment");
      $this->query("SELECT setval('paymentkind_paymentkind_id_seq',1,false)");
      $this->query("SELECT setval('paymentkind_paymentkind_id_seq',MAX(paymentkind_id)) FROM paymentkind");
      $this->query("SELECT setval('phone_phone_id_seq',1,false)");
      $this->query("SELECT setval('phone_phone_id_seq',MAX(phone_id)) FROM phone");
      $this->query("SELECT setval('profile_profile_id_seq',1,false)");
      $this->query("SELECT setval('profile_profile_id_seq',MAX(profile_id)) FROM profile");
      $this->query("SELECT setval('profilemodule_profilemodule_id_seq',1,false)");
      $this->query("SELECT setval('profilemodule_profilemodule_id_seq',MAX(profilemodule_id)) FROM profilemodule");
      $this->query("SELECT setval('profileproperty_profileproperty_id_seq',1,false)");
      $this->query("SELECT setval('profileproperty_profileproperty_id_seq',MAX(profileproperty_id)) FROM profileproperty");
      $this->query("SELECT setval('profilesection_profilesection_id_seq',1,false)");
      $this->query("SELECT setval('profilesection_profilesection_id_seq',MAX(profilesection_id)) FROM profilesection");
      $this->query("SELECT setval('project_project_id_seq',1,false)");
      $this->query("SELECT setval('project_project_id_seq',MAX(project_id)) FROM project");
      $this->query("SELECT setval('projectclosing_projectclosing_id_seq',1,false)");
      $this->query("SELECT setval('projectclosing_projectclosing_id_seq',MAX(projectclosing_id)) FROM projectclosing");
      $this->query("SELECT setval('projectreftask_projectreftask_id_seq',1,false)");
      $this->query("SELECT setval('projectreftask_projectreftask_id_seq',MAX(projectreftask_id)) FROM projectreftask");
      $this->query("SELECT setval('projecttask_projecttask_id_seq',1,false)");
      $this->query("SELECT setval('projecttask_projecttask_id_seq',MAX(projecttask_id)) FROM projecttask");
      $this->query("SELECT setval('projectuser_projectuser_id_seq',1,false)");
      $this->query("SELECT setval('projectuser_projectuser_id_seq',MAX(projectuser_id)) FROM projectuser");
      $this->query("SELECT setval('publication_publication_id_seq',1,false)");
      $this->query("SELECT setval('publication_publication_id_seq',MAX(publication_id)) FROM publication");
      $this->query("SELECT setval('publicationtype_publicationtype_id_seq',1,false)");
      $this->query("SELECT setval('publicationtype_publicationtype_id_seq',MAX(publicationtype_id)) FROM publicationtype");
      $this->query("SELECT setval('region_region_id_seq',1,false)");
      $this->query("SELECT setval('region_region_id_seq',MAX(region_id)) FROM region");
      $this->query("SELECT setval('resource_resource_id_seq',1,false)");
      $this->query("SELECT setval('resource_resource_id_seq',MAX(resource_id)) FROM resource");
      $this->query("SELECT setval('resourceitem_resourceitem_id_seq',1,false)");
      $this->query("SELECT setval('resourceitem_resourceitem_id_seq',MAX(resourceitem_id)) FROM resourceitem");
      $this->query("SELECT setval('resourcetype_resourcetype_id_seq',1,false)");
      $this->query("SELECT setval('resourcetype_resourcetype_id_seq',MAX(resourcetype_id)) FROM resourcetype");
      $this->query("SELECT setval('rgroup_rgroup_id_seq',1,false)");
      $this->query("SELECT setval('rgroup_rgroup_id_seq',MAX(rgroup_id)) FROM rgroup");
      $this->query("SELECT setval('service_service_id_seq',1,false)");
      $this->query("SELECT setval('service_service_id_seq',MAX(service_id)) FROM service");
      $this->query("SELECT setval('serviceproperty_serviceproperty_id_seq',1,false)");
      $this->query("SELECT setval('serviceproperty_serviceproperty_id_seq',MAX(serviceproperty_id)) FROM serviceproperty");
      $this->query("SELECT setval('subscription_subscription_id_seq',1,false)");
      $this->query("SELECT setval('subscription_subscription_id_seq',MAX(subscription_id)) FROM subscription");
      $this->query("SELECT setval('subscriptionreception_subscriptionreception_id_seq',1,false)");
      $this->query("SELECT setval('subscriptionreception_subscriptionreception_id_seq',MAX(subscriptionreception_id)) FROM subscriptionreception");
      $this->query("SELECT setval('tasktype_tasktype_id_seq',1,false)");
      $this->query("SELECT setval('tasktype_tasktype_id_seq',MAX(tasktype_id)) FROM tasktype");
      $this->query("SELECT setval('timetask_timetask_id_seq',1,false)");
      $this->query("SELECT setval('timetask_timetask_id_seq',MAX(timetask_id)) FROM timetask");
      $this->query("SELECT setval('ugroup_group_id_seq',1,false)");
      $this->query("SELECT setval('ugroup_group_id_seq',MAX(group_id)) FROM ugroup");
      $this->query("SELECT setval('updated_updated_id_seq',1,false)");
      $this->query("SELECT setval('updated_updated_id_seq',MAX(updated_id)) FROM updated");
      $this->query("SELECT setval('updatedlinks_updatedlinks_id_seq',1,false)");
      $this->query("SELECT setval('updatedlinks_updatedlinks_id_seq',MAX(updatedlinks_id)) FROM updatedlinks");
      $this->query("SELECT setval('userobm_userobm_id_seq',1,false)");
      $this->query("SELECT setval('userobm_userobm_id_seq',MAX(userobm_id)) FROM userobm");
      $this->query("SELECT setval('userobmpref_userobmpref_id_seq',1,false)");
      $this->query("SELECT setval('userobmpref_userobmpref_id_seq',MAX(userobmpref_id)) FROM userobmpref");
      $this->query("SELECT setval('usersystem_usersystem_id_seq',1,false)");
      $this->query("SELECT setval('usersystem_usersystem_id_seq',MAX(usersystem_id)) FROM usersystem");
      $this->query("SELECT setval('website_website_id_seq',1,false)");
      $this->query("SELECT setval('website_website_id_seq',MAX(website_id)) FROM website");    
    } else {
      $this->query("SET foreign_key_checks = 1");
    }
  }

  function setup() {
    if($this->_dbtype == 'pgsql') {
      $this->query("ALTER TABLE Account DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE AccountEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ActiveUserObm DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Address DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE CV DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE CalendarEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Campaign DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE CampaignDisabledEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE CampaignEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE CampaignMailContent DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE CampaignMailTarget DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE CampaignPushTarget DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE CampaignTarget DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Category DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE CategoryLink DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Company DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE CompanyActivity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE CompanyEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE CompanyNafCode DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE CompanyType DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Contact DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ContactEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ContactFunction DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ContactList DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Contract DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ContractEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ContractPriority DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ContractStatus DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ContractType DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Country DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE CvEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE DataSource DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Deal DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE DealCompany DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE DealCompanyRole DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE DealEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE DealStatus DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE DealType DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE DefaultOdtTemplate DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Deleted DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE DeletedContact DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE DeletedEvent DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE DeletedUser DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE DisplayPref DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Document DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE DocumentEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE DocumentLink DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE DocumentMimeType DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Domain DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE DomainEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE DomainProperty DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE DomainPropertyValue DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Email DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Entity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE EntityRight DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Event DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE EventAlert DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE EventCategory1 DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE EventEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE EventException DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE EventLink DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE GroupEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE GroupGroup DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Host DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE HostEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE IM DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Import DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ImportEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Incident DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE IncidentEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE IncidentPriority DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE IncidentResolutionType DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE IncidentStatus DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Invoice DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE InvoiceEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Kind DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Lead DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE LeadEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE LeadSource DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE LeadStatus DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE List DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ListEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE MailShare DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE MailboxEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE MailshareEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE OGroup DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE OGroupLink DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ObmBookmark DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ObmBookmarkProperty DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ObmInfo DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ObmSession DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ObmbookmarkEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE OgroupEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE OrganizationalChart DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE OrganizationalchartEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_Domain DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_DomainEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_EntityRight DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_GroupEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_Host DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_HostEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_MailShare DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_MailboxEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_MailshareEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_Service DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_ServiceProperty DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_UGroup DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_UserEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_UserObm DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE P_of_usergroup DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ParentDeal DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ParentdealEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Payment DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE PaymentEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE PaymentInvoice DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE PaymentKind DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Phone DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Profile DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ProfileEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ProfileModule DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ProfileProperty DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ProfileSection DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Project DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ProjectCV DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ProjectClosing DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ProjectEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ProjectRefTask DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ProjectTask DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ProjectUser DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Publication DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE PublicationEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE PublicationType DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE RGroup DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Region DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Resource DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ResourceEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ResourceGroup DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ResourceItem DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ResourceType DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ResourcegroupEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE SSOTicket DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Service DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE ServiceProperty DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Stats DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Subscription DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE SubscriptionEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE SubscriptionReception DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE SynchedContact DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE TaskEvent DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE TaskType DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE TimeTask DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE UGroup DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Updated DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Updatedlinks DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE UserEntity DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE UserObm DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE UserObmGroup DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE UserObmPref DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE UserObm_SessionLog DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE UserSystem DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE Website DISABLE TRIGGER ALL");
      $this->query("ALTER TABLE of_usergroup DISABLE TRIGGER ALL");      
      foreach(DummyGenerators::$PARTS as $index => $part) DummyGenerators::$PARTS[$index] = "CAST($part as vpartstat)";
      $this->query('CREATE TABLE ids (id serial, mailing boolean, PRIMARY KEY (id))');
      $this->query("CREATE TABLE lastnames (id serial, data1 text,data2 text,data3 text , PRIMARY KEY (id))");
      $this->query("CREATE TABLE firstnames (id serial, data1 text,data2 text,data3 text , PRIMARY KEY (id))");
      $this->query("CREATE TABLE texts (id serial, data1 text,data2 text,data3 text , PRIMARY KEY (id))");
      $this->query("CREATE TABLE words (id serial, data1 text,data2 text,data3 text , PRIMARY KEY (id))");      
    } else {
      $this->query("SET foreign_key_checks = 0");
      $this->query('CREATE TABLE ids (id int(8) auto_increment, mailing boolean, PRIMARY KEY (id))');
      $this->query("CREATE TABLE lastnames (id int(8) auto_increment, data1 text,data2 text,data3 text , PRIMARY KEY (id))");
      $this->query("CREATE TABLE firstnames (id int(8) auto_increment, data1 text,data2 text,data3 text , PRIMARY KEY (id))");
      $this->query("CREATE TABLE texts (id int(8) auto_increment, data1 text,data2 text,data3 text , PRIMARY KEY (id))");
      $this->query("CREATE TABLE words (id int(8) auto_increment, data1 text,data2 text,data3 text , PRIMARY KEY (id))");      
    }

    $query = "CREATE TABLE TmpEntityRight (LIKE EntityRight)";
    $this->query($query);
    $query = "ALTER TABLE TmpEntityRight DROP COLUMN entityright_id";
    $this->query($query);
    $query = "ALTER TABLE TmpEntityRight ALTER COLUMN entityright_access SET DEFAULT 0";
    $this->query($query);
    $query = "ALTER TABLE TmpEntityRight ALTER COLUMN entityright_read SET DEFAULT 0";
    $this->query($query);
    $query = "ALTER TABLE TmpEntityRight ALTER COLUMN entityright_write SET DEFAULT 0";
    $this->query($query);
    $query = "ALTER TABLE TmpEntityRight ALTER COLUMN entityright_admin SET DEFAULT 0";
    $this->query($query);
    $query  = "CREATE TABLE TmpEventLink (tmpevent_id integer, tmpentity_id integer)";
    $this->query($query);
    $query = "CREATE INDEX tmpevent_id_key ON TmpEventLink (tmpevent_id)";
    $this->query($query);
    $query = "CREATE INDEX tmpentity_id_key ON TmpEventLink (tmpentity_id)";
    $this->query($query);    

  }
}

