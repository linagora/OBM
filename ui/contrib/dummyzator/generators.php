<?php

/* run_query_domain_init_data() */
require("$obm_root/php/domain/domain_query.inc");
/* genUniqueExtEventId() calendar_*_repeatition() */
require("$obm_root/php/calendar/calendar_query.inc");

class DummyGenerators extends PDO
{
  /* Data from the database */
  var $domain_id;
  var $available_kinds;
  var $available_event_cats;
  var $last_entity_id;

  /* Scales definitions */
  var $nb_groups_ratio;
  var $user_per_groups;
  var $priv_contact_per_user;
  var $pub_contact_per_user;
  var $resource_users_ratio;
  var $nb_normal_events_per_user;
  var $nb_reccur_events_per_user;
  var $nb_ext_contact_infos;
  var $meeting_events_ratio;
  var $rights;

  /* Our stuff */
  var $today_date;
  var $ev_manager;
  var $available_repeatkind;
  var $users_eids;
  var $contacts_eids;

  public function __construct($obmpath = '.')
  {
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
    
    return parent::__construct
      ("$dbtype:host=$host;dbname=$db", $user, $pass
       );
  }

  public function do_inits() {
    /* read scales from conf.ini*/
    if(file_exists($GLOBALS['confFile'])) {
      $ratios = parse_ini_file($GLOBALS['confFile'],true);
    } else {
      echo "\nPlease edit conf.ini.sample and save it as conf.ini\n";
      exit(1);
    }
    $this->nb_groups_ratio           = floatval($ratios['entity.group']['ratio']);
    $this->user_per_groups           = floatval($ratios['entity.group.user']['ratio']);
    $this->priv_contact_per_user     = floatval($ratios['entity.contact.private']['ratio']);
    $this->pub_contact_per_user      = floatval($ratios['entity.contact.public']['ratio']);
    $this->resource_users_ratio      = floatval($ratios['entity.resource']['ratio']);
    $this->nb_normal_events_per_user = floatval($ratios['entity.event.normal']['ratio']);
    $this->nb_reccur_events_per_user = floatval($ratios['entity.event.recurring']['ratio']);
    $this->meeting_events_ratio      = floatval($ratios['entity.event.meeting']['ratio']);
    /* 'Email' => 2 will create rand(0,2) mails per contact */
    $this->nb_ext_contact_infos = array (
      'Email'   => floatval($ratios['entity.contact.coords']['email']),
      'Website' => floatval($ratios['entity.contact.coords']['website']),
      'IM'      => floatval($ratios['entity.contact.coords']['address']),
      'Address' => floatval($ratios['entity.contact.coords']['phone']),
      'Phone'   => floatval($ratios['entity.contact.coords']['im'])
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
    $this->today_date = strftime("%Y-%m-%d", time()-(2*3600));
    $this->ev_manager = array( /* user_id => new EventManager(time()), ... */ );
    $this->available_repeatkind = array('daily', 'weekly', 'monthlybyday', 'monthlybydate', 'yearly');

    /* Set debug by default */
    $this->query("UPDATE UserObmPref
SET userobmpref_value = '30'
WHERE userobmpref_option = 'set_debug'
AND userobmpref_user_id IS NULL");

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
    $this->domain_id = $this->lastInsertId('Domain','domain_id');
    /* Add related entity */
    $entity_id = $this->newEntity();
    $this->query("INSERT INTO DomainEntity (domainentity_domain_id, domainentity_entity_id)
    VALUES ( ".$this->domain_id.", $entity_id)");
    /* Fill it with initial data from the global domain */
    run_query_domain_init_data($this->domain_id);

    /* Keep infos we will need */
    $res = $this->query("
SELECT kind_id FROM Kind
WHERE kind_domain_id = '$this->domain_id'");
    $this->available_kinds = $res->fetchAll(PDO::FETCH_COLUMN);
    unset($res); // Next queries would fail otherwise

    $res = $this->query("
SELECT eventcategory1_id FROM EventCategory1
WHERE eventcategory1_domain_id = '$this->domain_id'");
    $this->available_event_cats = $res->fetchAll(PDO::FETCH_COLUMN);
  }

  public function genDummyData($nb_users)
  {
    $this->do_inits();

    $nb_groups = ceil($nb_users * $this->nb_groups_ratio);

    print "Inserting $nb_users users... ";
    $start = $this->lastInsertId('UserObm','userobm_id') +1 ;
    $users_ids      = new IntIterator(array('start' => $start, 'len' => $nb_users));
    $start = $this->lastInsertId('UGroup','group_id') +1 ;
    $usergroups_ids = new IntIterator(array('start' => $start, 'len' => $nb_groups,
                                            'every' => 1/$this->nb_groups_ratio));
    $this->createUsers(clone $users_ids, clone $usergroups_ids);
    print "done\n";
    $last_user_entity_id = $this->last_entity_id;


    print "Inserting $nb_groups groups... ";
    $groups_ids     = new IntIterator(array('start' => $start, 'len' => $nb_groups));
    $this->createGroups(clone $groups_ids);
    print "done\n";

    print "Randomly linking $nb_users users to $nb_groups groups... ";
    $this->createUserGroupLink(clone $users_ids, clone $groups_ids);
    print "done\n";

    /* Contacts */
    // we guess the next entities ids
    $next_entity_id = $this->last_entity_id + 1;

    $nb_priv_contacts = $nb_users * $this->priv_contact_per_user;
    $nb_pub_contacts = $nb_users * $this->pub_contact_per_user;
    $total_contacts = $nb_pub_contacts + $nb_priv_contacts;
    print "Inserting $this->priv_contact_per_user privates contacts for each "
      ."user (total $nb_priv_contacts contacts)... ";
    $this->createAllUsersContacts(1, clone $users_ids, $this->priv_contact_per_user);
    print "done\n";

    print "Inserting $this->pub_contact_per_user publics contacts for each "
      ."user (total $nb_pub_contacts contacts)... ";
    $this->createAllUsersContacts(0, clone $users_ids, $this->pub_contact_per_user);
    print "done\n";

    $this->contacts_eids = new IntIterator
     ( array('start' => $next_entity_id,
             'end'   => $this->last_entity_id) );
    print "Randomly inserting informations for all the "
      ."$total_contacts contacts:\n";
    $this->createContactsExtInfos(clone $this->contacts_eids);
    print "done\n";

    /* Resources */
    $nb_resources = ceil($nb_users * $this->resource_users_ratio);
    print "Inserting $nb_resources resources... ";
    $this->createResources(clone $users_ids, $nb_resources);
    print "done\n";

    /* Events */
    print "Inserting $nb_users calendars... ";
    $this->createCalendars(clone $users_ids);
    print "done\n";

    $this->users_eids = new IntIterator
      ( array('start' => $last_user_entity_id - $nb_users + 1,
              'end'   => $last_user_entity_id + 1) );
    $nb_events = $nb_users * $this->nb_reccur_events_per_user;
    print "Inserting $this->nb_reccur_events_per_user repeating events "
      ."for each user (total $nb_events), with "
      .(int)($this->meeting_events_ratio * 100)."% meetings... ";
    $this->createEvents(clone $users_ids, $this->nb_reccur_events_per_user,true);
    print "done\n";

    $nb_events = $nb_users * $this->nb_normal_events_per_user;
    print "Inserting $this->nb_normal_events_per_user non-repeating events "
      ."for each user (total $nb_events), with "
      .(int)($this->meeting_events_ratio * 100)."% meetings... ";
    $this->createEvents(clone $users_ids, $this->nb_normal_events_per_user,false);
    print "done\n";
  }
  
  public function createAllUsersContacts($is_private, $uid_iter, $nb_contacts)
  {
    while($uid = $uid_iter->nextInt()) {
      $this->createContacts($is_private, $nb_contacts, $uid,new Of_Date(time()));
    }
  }

  public function createContactsExtInfos($entity_iter)
  {
    foreach($this->nb_ext_contact_infos as $info => $randmax) {
      print "  $info... ";
      $func = "create$info";
      $this->$func(clone $entity_iter, $randmax);
      print "done\n";
    }
  }

  public function createUsers($uid_iter, $gid_iter)
  {
    /* Static for each record */
    $statics = array
      ( 'userobm_domain_id'  => "'$this->domain_id'",
        'userobm_password'   => "'password'",
        'userobm_timeupdate' => "NOW()",
        'userobm_timecreate' => "NOW()",
        'userobm_perms'      => "'user'",
        'userobm_userupdate' => "'1'",
        'userobm_usercreate' => "'1'",
        'userobm_kind'       => "''"
        );

    /* Dynamic stuff */
    $dyna_cols = array
      ( 'userobm_id', 'userobm_login', 'userobm_uid', 'userobm_gid',
        'userobm_lastname', 'userobm_firstname'
       );

    /* Prepared statments */
    $users = $this->massInsertorStatment('UserObm', $statics, $dyna_cols);
    while(1) {
      $uid = $uid_iter->nextInt();
      $gid = $gid_iter->nextInt();
      if($gid === null || $uid === null) {
        break;
      }
      $changing = array
        ( $uid, 'u'.($uid-1),                     // sql id, login
          1000 + $uid, 1000 + $gid,               // uid, gid
          'U'.randomAlphaStr(), randomAlphaStr(), // lastname, firstname
          );
      $this->execute_or_die($users, $changing);

      /* Add related entity */
      $entity_id = $this->newEntity();
      $this->query("INSERT INTO UserEntity
       (userentity_user_id, userentity_entity_id)
VALUES (            '$uid',         '$entity_id')");
    }
  }

  public function createGroups($gid_iter)
  {
    /* For filling some dummy groups in UGroup */
    $statics = array
      ('group_domain_id'   => "'".$this->domain_id."'",
       'group_timeupdate'  => 'NOW()',
       'group_timecreate'  => 'NOW()',
       'group_userupdate'  => "'1'",
       'group_usercreate'  => "'1'"
       );
    $dynamics = array
      ( 'group_id', 'group_name', 'group_desc', 'group_email' );

    $groups = $this->massInsertorStatment('UGroup', $statics, $dynamics);

    while(1) {
      $gid = $gid_iter->nextInt();
      if($gid === null) {
        break;
      }

      /* Build dummy group infos */
      $changing = array
        ( $gid, 'group'.$gid, randomAlphaStr(60), 'group'.$gid.'@example.net' );
      $this->execute_or_die($groups, $changing);

      /* Add related entity */
      $entity_id = $this->newEntity();
      $this->query("INSERT INTO GroupEntity
       (groupentity_group_id, groupentity_entity_id)
VALUES (              '$gid',          '$entity_id')");
    }
  }

  public function createUserGroupLink($uid_iter, $gid_iter)
  {
    /* For linking group to their users */
    $group_link  = $this->massInsertorStatment('of_usergroup',
                                            array( /* No statics */ ),
                                            array( 'of_usergroup_group_id',
                                                   'of_usergroup_user_id' ));
    $group_link2 = $this->massInsertorStatment('UserObmGroup',
                                            array( /* No statics */ ),
                                            array( 'userobmgroup_group_id',
                                                   'userobmgroup_userobm_id' ));

    $gid_start = $gid_iter->start();
    $gid_end   = $gid_iter->end();
    while(1) {
      $uid = $uid_iter->nextInt();
      if($uid === null) {
        break;
      }

      for($i = 0 ; $i < $this->user_per_groups ; $i++) {
        $gid = rand($gid_start, $gid_end);

        /* Add its link to the user */
        $group_link->execute(array( $gid, $uid ));
        $group_link2->execute(array( $gid, $uid ));
      }
    }
  }

  public function createResources($users_iter, $nb_resources) {
    /* Resource table */
    $statics = array
      ( 'resource_domain_id'    => "'$this->domain_id'",
        'resource_timeupdate'   => "NOW()",
        'resource_timecreate'   => "NOW()",
        'resource_delegation'   => "NULL"
        );

    $dyna_cols = array
      ( 'resource_usercreate', 'resource_name',
        'resource_description', 'resource_qty' );

    /* EntityRight table */
    $entr = $this->massInsertorStatment('EntityRight',
                       array( /* No statics */),
                       array( 'entityright_entity_id', 'entityright_consumer_id',
                              'entityright_access', 'entityright_read',
                              'entityright_write', 'entityright_admin' ));

    $resources = $this->massInsertorStatment('Resource', $statics, $dyna_cols);

    $uid_start = $users_iter->start();
    $uid_end   = $users_iter->end()-1;
  
    while($nb_resources) {
      $uid = rand($uid_start, $uid_end);
      $changing = array
        ( $uid, randomAlphaStr(8),        // userupdate, name
          randomAlphaStr(30), rand(1,20)  // description, quantity
          );
      $this->execute_or_die($resources, $changing);
      $rid = $this->lastInsertId('Resource', 'resource_id');

      /* Add related entity */
      $entity_id = $this->newEntity();
      $this->query("INSERT INTO ResourceEntity
       (resourceentity_resource_id, resourceentity_entity_id)
VALUES (                    '$rid',             '$entity_id')");

      /* Add related rights */
      $defaults = array
        ( $entity_id, $uid, rand(0,1), rand(0,1), rand(0,1), rand(0,1) );
      $this->execute_or_die($entr, $defaults);


      $nb_resources--;
    }
  }

  public function createCalendars($uid_iter)
  {
    /* Fills EntityRigtht */
    $rights = $this->massInsertorStatment('EntityRight',
                       array( 'entityright_access' => "'1'"),
                       array( 'entityright_entity_id', 'entityright_read',
                              'entityright_write', 'entityright_admin' ));
    /* Fills CalendarEntity */
    $cals = $this->massInsertorStatment('CalendarEntity',
                       array( /* No statics */),
                       array( 'calendarentity_entity_id',
                              'calendarentity_calendar_id' ));
                                        
    while(1) {
      $uid = $uid_iter->nextInt();
      if($uid === null) {
        break;
      }

      /* Create related entity */
      $entity_id = $this->newEntity();
      /* Create a new right */
      $rights->execute(array
        ( $entity_id, (int)rand(0,1), (int)rand(0,1), (int)rand(0,1) ));

      /* Create a new CalendarEntity */
      $cals->execute(array( $entity_id, $uid ));
    }
  }
  
  public function createContacts($is_private, $nb_contacts, $uid, $time)
  {
    /* Contact table */
    $statics = array
      ( 'contact_domain_id'           => "'$this->domain_id'",
        'contact_timeupdate'          => "NOW()",
        'contact_timecreate'          => "NOW()",
        'contact_usercreate'          => "'$uid'",
        'contact_userupdate'          => "'$uid'",
        'contact_datasource_id'       => "NULL",
        'contact_company_id'          => "NULL",
        'contact_company'             => "''",
        'contact_marketingmanager_id' => "'$uid'", // good ?
        'contact_suffix'              => "''",
        'contact_manager'             => "''",
        'contact_assistant'           => "''",
        'contact_mailing_ok'          => "'1'",
        'contact_newsletter'          => "'1'",
        'contact_privacy'             => "'$is_private'",
        'contact_date'                => "'$this->today_date'",
        'contact_origin'              => "'obm-dummyzator'"
        );
    $dyna_cols = array
      ( 'contact_kind_id', 'contact_lastname',
        'contact_firstname', 'contact_middlename', 'contact_aka',
        'contact_sound', 'contact_comment' );
    $contacts = $this->massInsertorStatment('Contact', $statics, $dyna_cols);

    /* SynchedContact table */
    $statics = array
      ( 'synchedcontact_user_id'    => "'$uid'",
        'synchedcontact_timestamp'  => "'$time'" );
    $dyna_cols = array
      ( 'synchedcontact_contact_id' );
    $sctct = $this->massInsertorStatment('SynchedContact', $statics,$dyna_cols);
    
    while($nb_contacts) {
      $lname = 'C'.randomAlphaStr(8);
      $changing = array
        ( $this->available_kinds[array_rand($this->available_kinds)], // kind
          $lname, randomAlphaStr(8),                  // lastname, firstname
          randomAlphaStr(4), randomAlphaStr(5),       // middlename, aka
          metaphone($lname), 'COM'.randomAlphaStr(15) // sound, comment
          );
      $this->execute_or_die($contacts, $changing);
      $cid = $this->lastInsertId('Contact','contact_id');

      $this->execute_or_die($sctct, array($cid));

      /* Add related entity */
      $entity_id = $this->newEntity();
      $this->query("INSERT INTO ContactEntity
       (contactentity_contact_id, contactentity_entity_id)
VALUES (                  '$cid',            '$entity_id')");

      $nb_contacts--;
    }
  }

  public function createAddress($entity_iter, $randmax)
  {
    $baselabel = "WORK;X-OBM-Ref1";
    $statics = array
      ( 'address_country' => "'FR'" );
    $dyna_cols = array
      ( 'address_entity_id', 'address_street',
        'address_zipcode', 'address_town', 'address_label' );

    $address = $this->massInsertorStatment('Address', $statics, $dyna_cols);
    while($eid = $entity_iter->nextInt()) {
      $nb = rand(0, $randmax);
      $first = 1;
      while($nb--) {
        $changing = array
          ( $eid,                                   // entity_id
            rand(1,500)." ".randomAlphaStr(3)
              ." ".randomAlphaStr(20),              // street
            rand(1000,99999),                       // zipcode
            randomAlphaStr(12),                     // town
            $first ? "PREF;$baselabel" : $baselabel // label
            );
        $this->execute_or_die($address, $changing);
        $first = 0;
      }
    }
  }

  public function createEmail($entity_iter, $randmax)
  {
    $baselabel = "INTERNET;X-OBM-Ref1";
    $dyna_cols = array
      ( 'email_entity_id', 'email_address', 'email_label' );

    $email = $this->massInsertorStatment('Email', array(), $dyna_cols);
    while($eid = $entity_iter->nextInt()) {
      $nb = rand(0, $randmax);
      $first = 1;
      while($nb--) {
        $changing = array
          ( $eid,                                   // entity_id
            randomAlphaStr(8)."@".randomAlphaStr(4)
              .".".randomAlphaStr(2),               // email
            $first ? "PREF;$baselabel" : $baselabel // label
            );
        $this->execute_or_die($email, $changing);
        $first = 0;
      }
    }
  }

  public function createIM($entity_iter, $randmax)
  {
    $baselabel = "";
    $dyna_cols = array
      ( 'im_entity_id', 'im_protocol', 'im_label' );

    $im = $this->massInsertorStatment('IM', array(), $dyna_cols);
    while($eid = $entity_iter->nextInt()) {
      $nb = rand(0, $randmax);
      $first = 1;
      while($nb--) {
        $changing = array
          ( $eid,                                   // entity_id
            randomAlphaStr(8)."@jabber."
              .randomAlphaStr(2),                   // im_address
            $first ? "PREF;$baselabel" : $baselabel // label
            );
        $this->execute_or_die($im, $changing);
        $first = 0;
      }
    }
  }

  public function createWebsite($entity_iter, $randmax)
  {
    $baselabel = "URL;X-OBM-Ref1";
    $dyna_cols = array
      ( 'website_entity_id', 'website_url', 'website_label' );

    $website = $this->massInsertorStatment('Website', array(), $dyna_cols);
    while($eid = $entity_iter->nextInt()) {
      $nb = rand(0, $randmax);
      $first = 1;
      while($nb--) {
        $changing = array
          ( $eid,                                               // entity_id
            htmlspecialchars("www.".randomAlphaStr(10).".org"), // url
            $first ? "PREF;$baselabel" : $baselabel // label
            );
        $this->execute_or_die($website, $changing);
        $first = 0;
      }
    }
  }

  public function createPhone($entity_iter, $randmax)
  {
    $baselabel = "HOME;VOICE;X-OBM-Ref1";
    $dyna_cols = array
      ( 'phone_entity_id', 'phone_number', 'phone_label' );

    $phone = $this->massInsertorStatment('Phone', array(), $dyna_cols);
    while($eid = $entity_iter->nextInt()) {
      $nb = rand(0, $randmax);
      $first = 1;
      while($nb--) {
        $changing = array
          ( $eid,                         // entity_id
            rand(0000000000, 9999999999), // phone
            $first ? "PREF;$baselabel" : $baselabel // label
            );
        $this->execute_or_die($phone, $changing);
        $first = 0;
      }
    }
  }

  public function createEvents($user_iter, $events_per_user, $want_repeat)
  {
    /* Event table */
    $statics = array
      ( 'event_domain_id'             => "'$this->domain_id'",
        'event_timeupdate'            => "NOW()",
        'event_timecreate'            => "NOW()",
        'event_type'                  => "'VEVENT'",
        'event_origin'                => "'obm-dummyzator'",
        'event_timezone'              => "'Europe/Paris'",
        'event_opacity'               => "'OPAQUE'",
        'event_properties'            => "'<extended_desc></extended_desc>'"
        );
    $dyna_cols = array
      ( 'event_usercreate', 'event_userupdate', 'event_ext_id', 'event_owner',
        'event_title', 'event_location', 'event_category1_id', 'event_priority',
        'event_privacy', 'event_date', 'event_duration', 'event_allday',
        'event_description', 'event_repeatfrequence', 'event_endrepeat',
        'event_repeatkind', 'event_repeatdays' );
    $events = $this->massInsertorStatment('Event', $statics, $dyna_cols);

    /* EventLink table */
    $avlb_states = array( 'ACCEPTED', 'NEEDS-ACTION', 'DECLINED' );
    $statics = array
      ( 'eventlink_timeupdate' => 'NOW()',
        'eventlink_timecreate' => 'NOW()',
        'eventlink_required'   => "'REQ'"
      );
    $dyna_cols = array
      ( 'eventlink_event_id', 'eventlink_usercreate',
        'eventlink_entity_id', 'eventlink_percent', 'eventlink_state' );
    $evlink = $this->massInsertorStatment('EventLink', $statics, $dyna_cols);

    while(1) {
      $uid = $user_iter->nextInt();
      $euid = $this->users_eids->nextInt();
      if($uid === null || $euid === null) {
        break;
      }
      
      /* For adding repeating events, we go randomly go from 0h to 24h
         through 5 steps. For example with 100 events to insert, we make
         100/5 = 20 repeting events, each at the same hour of the day, the
         repeathour. Then we go back to the beginning date, inc the repeathour
         with a pause time, and insert another 20 repeating events, and so on. */
      if($want_repeat) {
        $repeat_hour = 0;
        $max_repeat_event_time  = 3; // 
        $max_repeat_event_pause = 2; // hours
        $repeat_steps = 5;
        /* Ensure all the steps will hold in one day  */
        assert($max_repeat_event_time * $repeat_steps + $max_repeat_event_pause * ($repeat_steps-1) < 24);
      }

      if(!isset($this->ev_manager[$uid])) {
        $this->ev_manager[$uid] = new EventManager(time());
      }
      $evgen = $this->ev_manager[$uid];
      $evgen->gotoInitialDate();

      for($i = 0 ; $i < $events_per_user ; $i++) {
        /* Generate the event properties */
        if($want_repeat) {
          $allday = 0;
          $duration = rand(1,$max_repeat_event_time) * 3600;
          $repeatkind = $this->available_repeatkind
              [array_rand($this->available_repeatkind)];

          if(($i % ($events_per_user/$repeat_steps)) == 0) {
            /* Increase the repeathour if needed */
            $evgen->gotoInitialDate();
            if($i > 0) {
              $repeat_hour += $max_repeat_event_time;
              $repeat_hour += rand(0,$max_repeat_event_pause);
            }
          } else {
            /* Goto next repeat session free time */
            $evgen->date = clone $end;
          }
          $evgen->date->addDay(rand(1,10)); // little break between 2 events session
          $end = clone $evgen->date;
          $end->addSecond($duration);

          switch ($repeatkind) {
          case 'daily'         :
            $end->addDay(rand(3, 20));
            $freq = rand(1,10);
            break;

          case 'weekly'        :
            $end->addWeek(rand(1, 5));
            $days = '';
            for($d = 0 ; $d < 7 ; $d++) {
              $days .= rand(0,1);
            }
            $freq = rand(1,6);
            break;

          case 'monthlybyday'  :
            $end->addMonth(rand(1, 3));
            $freq = rand(1,2);
            break;

          case 'monthlybydate' :
            $end->addMonth(rand(1, 3));
            $freq = rand(1,2);
            break;

          case 'yearly'        :
            $freq = rand(1,2);
            $end->addYear(rand(1, 2));
            break;
          }
          assert(
            $evgen->addRepeatEvent($allday, $repeat_hour, $duration, $end, $repeatkind, $freq, $days) === true
          );

        } else { /* Non repeating event */
          $days = '0000000';
          $repeatkind = 'none';
          $freq = 1;
          $allday = intval(!rand(0,15));
          $duration = rand(1,5) * 3600;

          /* Make the time to forward */
          $evgen->gotoNextFreeTime();
          $evgen->date->addHour(rand(0,48)); // little break between 2 events

          assert($evgen->addEvent($allday, $duration) === true);

          $end = clone $evgen->date;
          $end->addSecond($duration); // $duration may have been changed
        }

        /* Insert in Event table */
        $changing = array
          ( $uid, $uid, genUniqueExtEventId(), // usercreate, userupdate, ext_id
            $uid, randomAlphaStr(8),           // owner, title
            randomAlphaStr(7),                 // location
            $this->available_event_cats
              [array_rand($this->available_event_cats)], // category1
            rand(1,3), rand(0,1),              // priority, privacy
            $evgen->date, $duration,           // begin date, duration
            $allday, randomAlphaStr(20),       // allday, description
            $freq,                             // repeat frequence
            $end,                              // end repeat
            $repeatkind,                       // repeat kind
            $days                              // repeat days
            );
        $this->execute_or_die($events, $changing);

        /* Randomly insert some other participants in EventLink, and the creator */
        $last_id = $this->lastInsertId('Event','event_id');
        if($i % (1 / $this->meeting_events_ratio) == 0) {
          /* Note: with $nb_parts = rand(2, 5) we need at least 6 users
             (event creator + 5 others) available, otherwise we may fall
             in an infinite loop while looking for others users */
          $nb_parts = rand(2, 5);
          $parts = $this->pickUpRandomPeople($nb_parts, $euid);
          foreach($parts as $part) {
            $changing = array
              ( $last_id, $uid,   // event id, user's event id
                $part,rand(0,100),// event participant entity id, percent state
                $avlb_states[ array_rand($avlb_states) ] // state
                );
            $this->execute_or_die($evlink, $changing);
          }
        }
      }
    }
  }

  public function execute_or_die($sth, $changing)
  {
    $res = $sth->execute($changing);
    if($res === false) {
      $err = $sth->errorinfo();
      print "\nFailed to execute SQL prepared request:\n$err[2]\n";
      debug_print_backtrace();
      exit(1);
    }
  }

  public function query_or_die($query)
  {
    $res = $this->query($query);
    if($res === false) {
      $err = $this->errorinfo();
      print "\nFailed to execute SQL request:\nQuery: $query\nError: $err[2]\n";
      debug_print_backtrace();
      exit(1);
    }
    return $res;
  }

  public function massInsertorStatment($table, $statics, $dynamic_cols)
  {
    // Prepare the query
    $statics_str_vars = implode(",\n    ", array_keys($statics));
    $statics_str_vals = implode(",\n    ", array_values($statics));
    if(count($statics)) {
      $statics_str_vars .= ",\n    ";
      $statics_str_vals .= ",\n    ";
    }

    $dynamics_str_vars = implode(",\n    ", $dynamic_cols);
    $dynamics_str_vals = '';
    for($i = 0 ; $i < count($dynamic_cols)-1 ; $i++) {
        $dynamics_str_vals .= '?, ';
    }
    if(count($dynamic_cols)) {
      $dynamics_str_vals .= "?\n";
    }

    $query = "INSERT INTO $table (
    $statics_str_vars$dynamics_str_vars
  ) VALUES (
    $statics_str_vals$dynamics_str_vals
)";
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
    $this->last_entity_id = $this->lastInsertId('Entity','entity_id');
    return $this->last_entity_id;
  }

  /**
   * Pick up $nb random people in contacts and users, and return
   * their entity ids in an array. If $eid is given, it's added
   * to the returned array.
   * Needs $this->users_eids and $this->contacts_eids to be set.
   */
  public function pickUpRandomPeople($nb, $eid = null) {
    if($this->users_eids === null || $this->contacts_eids === null) {
      return array();
    }

    $eids = array();
    if($eid !== null) {
      $eids[] = $eid;
    }

    /* Build a list of the available people */
    $participants = array( 'user' => array(), 'contact' => array() );
    while($eid = $this->users_eids->nextInt()) {
      $participants['user'][] = $eid;
    }
    $this->users_eids->gotoStart();
    while($eid = $this->contacts_eids->nextInt()) {
      $participants['contact'][] = $eid;
    }
    $this->contacts_eids->gotoStart();

    while($nb) {
      $who = array_rand($participants);
      do {
        $i = $participants[$who][ array_rand($participants[$who]) ];
      } while(in_array($i, $eids));
      $eids[] = $i;

      $nb--;
    }
    return $eids;
  }

  public function lastInsertId($table, $field) {
    $res = $this->query_or_die("SELECT MAX($field) FROM $table");
    return $res->fetchColumn();
  }
}

