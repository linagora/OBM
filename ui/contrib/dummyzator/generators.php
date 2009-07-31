<?php

/* run_query_domain_init_data() */
require("$obm_root/php/domain/domain_query.inc");
/* genUniqueExtEventId() calendar_*_repeatition() */
require("$obm_root/php/calendar/calendar_query.inc");

class DummyGenerators extends PDO {

  /* Constants */
  private static $REPEAT_KIND   = array('daily', 'weekly', 'monthlybyday', 'monthlybydate', 'yearly');
  private static $EVENT_CATEGORY; 
  private static $KIND;

  /* Data from the database */
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

    $dbtype = strtolower($settings['global']['dbtype']);
    $host   = $settings['global']['host'];
    $db     = $settings['global']['db'];
    $user   = $settings['global']['user'];
    $pass   = $settings['global']['password'];
    
    return parent::__construct("$dbtype:host=$host;dbname=$db", $user, $pass);
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
      echo "\nPlease edit conf.ini.sample and save it as conf.ini\n";
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
    $this->rights = array ( 'calendar' => array ( 
        'user' => array ( 
          'access'      => floatval($ratios['right.contact.user']['access']), 
          'read'        => floatval($ratios['right.contact.user']['read']),  
          'write'       => floatval($ratios['right.contact.user']['write']),  
          'admin'       => floatval($ratios['right.contact.user']['admin'])
        ),
        'group'         => array ( 
          'access'      => floatval($ratios['right.contact.group']['access']), 
          'read'        => floatval($ratios['right.contact.group']['read']),  
          'write'       => floatval($ratios['right.contact.group']['write']),  
          'admin'       => floatval($ratios['right.contact.group']['admin'])          
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
    $this->query("INSERT INTO Domain (
      domain_timeupdate,
      domain_timecreate,
      domain_userupdate,
      domain_usercreate,
      domain_label,
      domain_description,
      domain_name,
      domain_alias)
    VALUES (
      null,
      NOW(),
      null,
      1,
      '".$GLOBALS['obm']['domain_label']."',
      '',
      '".$GLOBALS['obm']['domain_name']."',
      '')");

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
  }

  /**
   * genDummyData 
   * 
   * @param mixed $usersCount 
   * @access public
   * @return void
   */
  public function genDummyData($usersCount) {
    
    $this->initialize();
    
    $groupsCount = ceil($usersCount * $this->groupRatio);

    print "Inserting $usersCount users... ";
    $firstId = $this->lastInsertId('UserObm','userobm_id') +1 ;
    $userIdSet = new IdIterator(array('start' => $firstId, 'len' => $usersCount));
    $firstId = $this->lastInsertId('Entity','entity_id') +1 ;
    $this->userEntityIdSet = new IdIterator(array('start' => $firstId, 'len' => $usersCount));
    $firstId = $this->lastInsertId('UGroup','group_id') +1 ;
    $userGidSet = new IdIterator(array('start' => $firstId, 'len' => $groupsCount, 'every' => 1/$this->groupRatio));

    $this->createUsers(clone $userIdSet, clone $userGidSet);
    print "done\n";

    $firstId = $this->lastInsertId('Entity','entity_id') +1 ;
    $this->groupEntityIdSet = new IdIterator( array('start' => $firstId, 'len'   => $groupsCount) );
    print "Inserting $groupsCount groups... ";
    $groupIdSet = new IdIterator(array('start' => $firstId, 'len' => $groupsCount));
    $this->createGroups(clone $groupIdSet);
    print "done\n";

    print "Randomly linking $usersCount users to $groupsCount groups... ";
    $this->createUserGroupLink(clone $userIdSet, clone $groupIdSet);
    print "done\n";

    /* Contacts */

    $contactPrivateCount = $usersCount * $this->contactPrivateRatio;
    $contactPublicCount = $usersCount * $this->contactPublicRatio;
    $contactCount = $contactPublicCount + $contactPrivateCount;
    $firstId = $this->lastInsertId('Entity','entity_id') +1 ;
    $this->contactEntityIdSet = new IdIterator( array('start' => $firstId, 'len'   => $contactCount) );
    print "Inserting $this->contactPrivateRatio privates contacts for each user (total $contactPrivateCount contacts)... ";
    $this->createAllUsersContacts(1, clone $userIdSet, $this->contactPrivateRatio);
    print "done\n";

    print "Inserting $this->contactPublicRatio publics contacts for each user (total $contactPublicCount contacts)... ";
    $this->createAllUsersContacts(0, clone $userIdSet, $this->contactPublicRatio);
    print "done\n";

    print "Randomly inserting informations for all the $contactCount contacts:\n";
    $this->createContactsExtInfos(clone $this->contactEntityIdSet);
    print "done\n";

    /* Resources */
    $resourcesCount = ceil($usersCount * $this->resourceRatio);
    $firstId = $this->lastInsertId('Entity','entity_id') +1 ;
    $this->resourceEntityIdSet = new IdIterator( array('start' => $firstId, 'len'   => $resourcesCount) );
    print "Inserting $resourcesCount resources... ";
    $this->createResources(clone $userIdSet, $resourcesCount);
    print "done\n";

    /* Events */
    $firstId = $this->lastInsertId('Entity','entity_id') +1 ;
    $this->calendarEntityIdSet = new IdIterator( array('start' => $firstId, 'len'   => $calendarsCount) );    
    print "Inserting $usersCount calendars... ";
    $this->createCalendars(clone $userIdSet);
    print "done\n";

    $eventCount = $usersCount * $this->eventRecurringRatio;
    print "Inserting $this->eventRecurringRatio repeating events "
      ."for each user (total $eventCount), with "
      .(int)($this->eventMeetingRatio * 100)."% meetings... ";
    $this->createEvents(clone $userIdSet, $this->eventRecurringRatio,true);
    print "done\n";

    $eventCount = $usersCount * $this->eventNormalRatio;
    print "Inserting $this->eventNormalRatio non-repeating events for each user (total $eventCount), with ".(int)($this->eventMeetingRatio * 100)."% meetings... ";
    $this->createEvents(clone $userIdSet, $this->eventNormalRatio,false);
    print "done\n";
  }
  
  /**
   * createAllUsersContacts 
   * 
   * @param mixed $isPrivate 
   * @param mixed $userIdSet 
   * @param mixed $contactCount 
   * @access public
   * @return void
   */
  public function createAllUsersContacts($isPrivate, $userIdSet, $contactCount) {
    while($uid = $userIdSet->next()) {
      $this->createContacts($isPrivate, $contactCount, $uid,new Of_Date(time()));
    }
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
    $staticsColumns = array
      ( 'userobm_domain_id'  => "'$this->domain'",
        'userobm_password'   => "'password'",
        'userobm_timeupdate' => "NOW()",
        'userobm_timecreate' => "NOW()",
        'userobm_perms'      => "'admin'",
        'userobm_userupdate' => "'1'",
        'userobm_usercreate' => "'1'",
        'userobm_kind'       => "''"
        );

    /* Dynamic stuff */
    $dynamicsColumns = array('userobm_id', 'userobm_login', 'userobm_uid', 'userobm_gid','userobm_lastname', 'userobm_firstname');

    /* Prepared statments */
    $users = $this->massInsertorStatment('UserObm', $staticsColumns, $dynamicsColumns);
    while(1) {
      $uid = $userIdSet->next();
      $gid = $userGidSet->next();
      if($gid === null || $uid === null) {
        break;
      }
      $changing = array ( 
        $uid, // id
        'u'.($uid-1), //login 
        1000 + $uid, //uid 
        1000 + $gid, //gid
        RandomData::getInstance()->getRandomLastname(), //lastname
        RandomData::getInstance()->getRandomFirstname(), //firstname
      );
      $this->executeOrDie($users, $changing);

      /* Add related entity */
      $entityId = $this->newEntity();
      $this->query("INSERT INTO UserEntity (userentity_user_id, userentity_entity_id) VALUES ('$uid', '$entityId')");
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
      'group_usercreate'  => "'1'"
    );
    $dynamics = array( 'group_id', 'group_name', 'group_desc', 'group_email' );

    $groups = $this->massInsertorStatment('UGroup', $staticsColumns, $dynamics);

    while(1) {
      $gid = $userGidSet->next();
      if($gid === null) {
        break;
      }
      /* Build dummy group infos */
      $changing = array( $gid, 'group'.$gid, RandomData::getInstance()->getRandomWord(60), 'group'.$gid.'@example.net' );
      $this->executeOrDie($groups, $changing);
      /* Add related entity */
      $entityId = $this->newEntity();
      $this->query("INSERT INTO GroupEntity (groupentity_group_id, groupentity_entity_id) VALUES ('$gid','$entityId')");
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
    /* For linking group to their users */
    $group_link  = $this->massInsertorStatment(
      'of_usergroup',
      array( /* No statics */ ),
      array( 'of_usergroup_group_id',
      'of_usergroup_user_id' )
    );

    $group_link2 = $this->massInsertorStatment(
      'UserObmGroup',
      array( /* No statics */ ),
      array( 'userobmgroup_group_id',
      'userobmgroup_userobm_id' )
    );

    $firstGid = $userGidSet->start();
    $lastGid = $userGidSet->end();
    while(1) {
      $uid = $userIdSet->next();
      if($uid === null) {
        break;
      }
      for($i = 0 ; $i < $this->groupUserRatio ; $i++) {
        $gid = rand($firstGid, $lastGid);
        /* Add its link to the user */
        $group_link->execute(array( $gid, $uid ));
        $group_link2->execute(array( $gid, $uid ));
      }
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
      'resource_delegation'   => "NULL"
    );

    $dynamicsColumns = array (
      'resource_usercreate', 
      'resource_name',
      'resource_description', 
      'resource_qty' 
    );

    $resources = $this->massInsertorStatment('Resource', $staticsColumns, $dynamicsColumns);
    $firstUid = $userIdSet->start();
    $lastUid   = $userIdSet->end()-1;
  
    while($resourcesCount) {
      $uid = rand($firstUid, $lastUid);
      $changing = array( 
        $uid,// userupdate
        RandomData::getInstance()->getRandomWord(8),// name        
        RandomData::getInstance()->getRandomText(254), // description
        rand(1,20)  // quantity
          );
      $this->executeOrDie($resources, $changing);
      $rid = $this->lastInsertId('Resource', 'resource_id');

      /* Add related entity */
      $entityId = $this->newEntity();
      $this->query("INSERT INTO ResourceEntity (resourceentity_resource_id, resourceentity_entity_id) VALUES ('$rid','$entityId')");
      
      $userEntityId = $this->userEntityIdSet->start() + ($uid - $userIdSet->start());
      /* Add related rights */
      $this->setRights('resource', $entityId, $userEntityId);

      $resourcesCount--;
    }
  }

  /**
   * createCalendars 
   * 
   * @param mixed $userIdSet 
   * @access public
   * @return void
   */
  public function createCalendars($userIdSet) {
    /* Fills CalendarEntity */
    $cals = $this->massInsertorStatment(
      'CalendarEntity',
      array( /* No statics */),
      array(
        'calendarentity_entity_id',
        'calendarentity_calendar_id'
      )
    );
                                        
    while(1) {
      $uid = $userIdSet->next();
      if($uid === null) {
        break;
      }
      /* Create related entity */
      $entityId = $this->newEntity();
      /* Create a new right */
      $this->setRights('calendar', $entityId);

      /* Create a new CalendarEntity */
      $cals->execute(array( $entityId, $uid ));
    }
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
  public function createContacts($isPrivate, $contactCount, $uid, $time) {
    /* Contact table */
    $staticsColumns = array (
      'contact_domain_id'           => "'$this->domain'",
      'contact_timeupdate'          => "NOW()",
      'contact_timecreate'          => "NOW()",
      'contact_usercreate'          => "'$uid'",
      'contact_userupdate'          => "'$uid'",
      'contact_datasource_id'       => "NULL",
      'contact_company_id'          => "NULL",
      'contact_company'             => "''",
      'contact_marketingmanager_id' => "NULL", 
      'contact_suffix'              => "''",
      'contact_manager'             => "''",
      'contact_assistant'           => "''",
      'contact_mailing_ok'          => "'1'",
      'contact_newsletter'          => "'1'",
      'contact_privacy'             => "'$isPrivate'",
      'contact_date'                => "'$this->today'",
      'contact_origin'              => "'obm-dummyzator'"
    );
    $dynamicsColumns = array( 
      'contact_kind_id', 
      'contact_lastname',
      'contact_firstname',
      'contact_middlename',
      'contact_aka',
      'contact_sound',
      'contact_comment'
    );
    $contacts = $this->massInsertorStatment('Contact', $staticsColumns, $dynamicsColumns);

    /* SynchedContact table */
    $staticsColumns = array ( 
      'synchedcontact_user_id'    => "'$uid'",
      'synchedcontact_timestamp'  => "'$time'" 
    );
    $dynamicsColumns = array( 'synchedcontact_contact_id' );
    $sctct = $this->massInsertorStatment('SynchedContact', $staticsColumns,$dynamicsColumns);
    
    while($contactCount) {
      $lname = RandomData::getInstance()->getRandomLastname();
      $changing = array( 
        DummyGenerators::$KIND[array_rand(DummyGenerators::$KIND)], // kind
        $lname,// lastname 
        RandomData::getInstance()->getRandomFirstname(),//firstname
        RandomData::getInstance()->getRandomFirstname(),//middlename
        RandomData::getInstance()->getRandomFirstname(),// aka
        metaphone($lname), //sound 
        RandomData::getInstance()->getRandomText() //comment
      );
      $this->executeOrDie($contacts, $changing);
      $cid = $this->lastInsertId('Contact','contact_id');

      $this->executeOrDie($sctct, array($cid));

      /* Add related entity */
      $entityId = $this->newEntity();
      $this->query("INSERT INTO ContactEntity (contactentity_contact_id, contactentity_entity_id) VALUES ('$cid', '$entityId')");
      if($isPrivate) $this->setRights('contact', $entityId);
      $contactCount--;
    }
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
    $baselabel = "WORK;X-OBM-Ref1";
    $staticsColumns = array ( 'address_country' => "'FR'" );
    $dynamicsColumns = array ( 
      'address_entity_id',
      'address_street',
      'address_zipcode',
      'address_town',
      'address_label' 
    );

    $address = $this->massInsertorStatment('Address', $staticsColumns, $dynamicsColumns);
    while($eid = $entityIdSet->next()) {
      $nb = rand(0, $randmax);
      $first = 1;
      while($nb--) {
        $changing = array
          ( $eid,                                   // entity_id
            rand(1,500)." ".RandomData::getInstance()->getRandomWord(3)
              ." ".RandomData::getInstance()->getRandomWord(20),              // street
            rand(1000,99999),                       // zipcode
            RandomData::getInstance()->getRandomWord(12),                     // town
            $first ? "PREF;$baselabel" : $baselabel // label
            );
        $this->executeOrDie($address, $changing);
        $first = 0;
      }
    }
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
    $baselabel = "INTERNET;X-OBM-Ref1";
    $dynamicsColumns = array ( 'email_entity_id', 'email_address', 'email_label' );

    $email = $this->massInsertorStatment('Email', array(), $dynamicsColumns);
    while($eid = $entityIdSet->next()) {
      $nb = rand(0, $randmax);
      $first = 1;
      while($nb--) {
        $changing = array
          ( $eid,                                   // entity_id
            RandomData::getInstance()->getRandomWord(8)."@".RandomData::getInstance()->getRandomWord(4)
              .".".RandomData::getInstance()->getRandomWord(2),               // email
            $first ? "PREF;$baselabel" : $baselabel // label
            );
        $this->executeOrDie($email, $changing);
        $first = 0;
      }
    }
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
    $baselabel = "";
    $dynamicsColumns = array( 'im_entity_id', 'im_protocol', 'im_label' );

    $im = $this->massInsertorStatment('IM', array(), $dynamicsColumns);
    while($eid = $entityIdSet->next()) {
      $nb = rand(0, $randmax);
      $first = 1;
      while($nb--) {
        $changing = array(
          $eid,                                   // entity_id
          RandomData::getInstance()->getRandomWord(8)."@jabber.".RandomData::getInstance()->getRandomWord(2),                   // im_address
          $first ? "PREF;$baselabel" : $baselabel // label
        );
        $this->executeOrDie($im, $changing);
        $first = 0;
      }
    }
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
    $baselabel = "URL;X-OBM-Ref1";
    $dynamicsColumns = array( 'website_entity_id', 'website_url', 'website_label' );

    $website = $this->massInsertorStatment('Website', array(), $dynamicsColumns);
    while($eid = $entityIdSet->next()) {
      $nb = rand(0, $randmax);
      $first = 1;
      while($nb--) {
        $changing = array( 
          $eid, // entity_id
          htmlspecialchars("www.".RandomData::getInstance()->getRandomWord(10).".org"), // url
          $first ? "PREF;$baselabel" : $baselabel // label
        );
        $this->executeOrDie($website, $changing);
        $first = 0;
      }
    }
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
    $baselabel = "HOME;VOICE;X-OBM-Ref1";
    $dynamicsColumns = array ( 'phone_entity_id', 'phone_number', 'phone_label' );
    $phone = $this->massInsertorStatment('Phone', array(), $dynamicsColumns);
    while($eid = $entityIdSet->next()) {
      $nb = rand(0, $randmax);
      $first = 1;
      while($nb--) {
        $changing = array ( 
          $eid, // entity_id
          rand(0000000000, 9999999999), // phone
          $first ? "PREF;$baselabel" : $baselabel // label
        );
        $this->executeOrDie($phone, $changing);
        $first = 0;
      }
    }
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
    $staticsColumns = array(
      'event_domain_id'             => "'$this->domain'",
      'event_timeupdate'            => "NOW()",
      'event_timecreate'            => "NOW()",
      'event_type'                  => "'VEVENT'",
      'event_origin'                => "'obm-dummyzator'",
      'event_timezone'              => "'Europe/Paris'",
      'event_opacity'               => "'OPAQUE'",
      'event_properties'            => "'<extended_desc></extended_desc>'"
    );
    $dynamicsColumns = array ( 
      'event_usercreate',
      'event_userupdate', 
      'event_ext_id', 
      'event_owner',
      'event_title',
      'event_location', 
      'event_category1_id',
      'event_priority',
      'event_privacy',
      'event_date', 
      'event_duration', 
      'event_allday',
      'event_description', 
      'event_repeatfrequence', 
      'event_endrepeat',
      'event_repeatkind',
      'event_repeatdays'
    );
    $events = $this->massInsertorStatment('Event', $staticsColumns, $dynamicsColumns);

    /* EventLink table */
    $avlb_states = array( 'ACCEPTED', 'NEEDS-ACTION', 'DECLINED' );
    $staticsColumns = array (
      'eventlink_timeupdate' => 'NOW()',
      'eventlink_timecreate' => 'NOW()',
      'eventlink_required'   => "'REQ'"
      );
    $dynamicsColumns = array (
      'eventlink_event_id',
      'eventlink_usercreate',
      'eventlink_entity_id',
      'eventlink_percent', 
      'eventlink_state' 
    );
    $evlink = $this->massInsertorStatment('EventLink', $staticsColumns, $dynamicsColumns);
    $date = new Of_Date(); 

    while(1) {
      $uid = $userIdSet->next();
      $euid = $this->userEntityIdSet->next();
      if($uid === null || $euid === null) {
        break;
      }
       
      for($i = 0 ; $i < $userEventCount ; $i++) {
        /* Generate the event properties */
        $allDay = intval(!rand(0,$this->eventAllDayRatio/1));
        $duration = rand(1,$this->eventData['maxEventDuration']) * 3600;
        $days = '0000000';
        $date->setTimestamp(strtotime(date('%Y-'.rand(1,12).'-'.rand(1,31).' '.rand(0,24).':'.rand(0,60).':00')));
        $end = clone $date;
        $end->addSecond($duration);
        if($isRecurring) {
          $repeatkind = DummyGenerators::$REPEAT_KIND[array_rand(DummyGenerators::$REPEAT_KIND)];

          switch ($repeatkind) {
          case 'daily'         :
            $end->addDay(rand(3, 20));
            $freq = rand(1,10);
            break;

          case 'weekly'        :
            $end->addWeek(rand(4, 8));
            $days = '';
            for($d = 0 ; $d < 7 ; $d++) {
              $days .= rand(0,1);
            }
            $freq = rand(1,3);
            break;

          case 'monthlybyday'  :
            $end->addMonth(rand(4, 8));
            $freq = rand(1,3);
            break;

          case 'monthlybydate' :
            $end->addMonth(rand(4, 8));
            $freq = rand(1,4);
            break;

          case 'yearly'        :
            $freq = rand(1,2);
            $end->addYear(rand(3, 4));
            break;
          }

        } else { /* Non repeating event */
          $repeatkind = 'none';
          $freq = 1;
        }

        /* Insert in Event table */
        $changing = array( 
          $uid, 
          $uid,
          "DUMMY-".RandomData::getInstance()->getRandomWord()."-OBM", // usercreate, userupdate, ext_id
          $uid,
          RandomData::getInstance()->getRandomWord(8),           // owner, title
          RandomData::getInstance()->getRandomWord(7),                 // location
          DummyGenerators::$EVENT_CATEGORY[array_rand(DummyGenerators::$EVENT_CATEGORY)], // category1
          rand(1,3), // priority 
          rand(0,1), //  privacy
          $date, // begin date
          $duration, // duration
          $allDay, // allday
          RandomData::getInstance()->getRandomText(),// description
          $freq,  // repeat frequence
          $end, // end repeat
          $repeatkind, // repeat kind
          $days // repeat days
        );
        $this->executeOrDie($events, $changing);

        /* Randomly insert some other participants in EventLink, and the creator */
        $eventId = $this->lastInsertId('Event','event_id');
        if($i % (1 / $this->eventMeetingRatio) == 0) {
          /* Note: with $nb_parts = rand(2, 5) we need at least 6 users
             (event creator + 5 others) available, otherwise we may fall
             in an infinite loop while looking for others users */
          $attendeeCount = rand(2, 5);
          $attendees = $this->pickUpRandomPeople($attendeeCount, $euid);
          foreach($attendees as $attendee) {
            $changing = array(
              $eventId, // event id,
              $uid, //user's event id
              $attendee, // event participant
              rand(0,100),// percent state
              $avlb_states[ array_rand($avlb_states) ] // state
            );
            $this->executeOrDie($evlink, $changing);
          }
        }
      }
    }
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

  public function newEntity() {
    $this->query('INSERT INTO Entity (entity_mailing) VALUES (TRUE)');
    $this->lastEntityId = $this->lastInsertId('Entity','entity_id');
    return $this->lastEntityId;
  }

  /**
   * Pick up $nb random people in contacts and users, and return
   * their entity ids in an array. If $eid is given, it's added
   * to the returned array.
   * Needs $this->userEntityIdSet and $this->contactEntityIdSet to be set.
   */
  public function pickUpRandomPeople($nb, $eid = null) {
    if($this->userEntityIdSet === null || $this->contactEntityIdSet === null) {
      return array();
    }

    $eids = array();
    if($eid !== null) {
      $eids[] = $eid;
    }

    while($nb) {
      if(rand(0,1) == 1) {
        do {$i =  $this->userEntityIdSet->seek(rand(0,($this->userEntityIdSet->getSize()-1)));} while(in_array($i, $eids));
        $eids[] = $i;
      } else {
        do {$i =  $this->contactEntityIdSet->seek(rand(0,($this->contactEntityIdSet->getSize()-1)));} while(in_array($i, $eids));
        $eids[] = $i;
      }
      $nb --;
    }
    return $eids;
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

  public function setRights($entity, $eid, $cid = null) {
    /* EntityRight table */
    $query = $this->massInsertorStatment(
      'EntityRight',
      array( /* No statics */),
      array( 
        'entityright_entity_id',
        'entityright_consumer_id',
        'entityright_access',
        'entityright_read',
        'entityright_write',
        'entityright_admin' 
      )
    );
    $eids = array();
    if($cid !== null) {
      $eids[$cid] = array('access' => 1, 'read' => 1, 'write' => 1, 'admin' => 1);
    }  
    // User rights 
    foreach($this->rights[$entity] as $kind => $kindRights) {
      foreach($kindRights as $right => $ratio) {
        $set =  $this->{$kind.'EntityIdSet'};
        if($ratio == 1) {
          $eids['NULL'][$right] = 1;
        } else {
          $nb = ceil($ratio * $this->{$entity.'EntityIdSet'}->getSize());
          while($nb) {
            do {$i =  $set->seek(rand(0,($set->getSize()-1)));} while(in_array($i, $eids));
            $eids[$i][$right] = 1;
            $nb --;
          }
        }
      }
    }
    foreach($eids as $cid => $rights) {
      $query->execute( array( $eid, $cid, (int)$rights['access'], (int)$rights['read'], (int)$rights['write'], (int)$rights['admin']));
    }
    
  }
}

