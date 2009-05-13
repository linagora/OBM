<?php

/* run_query_domain_init_data() */
require("$obm_root/php/domain/domain_query.inc");
/* genUniqueExtEventId() */
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
  var $nb_normal_events_per_user;
  var $nb_reccur_events_per_user;
  var $nb_ext_contact_infos;
  var $today_date;

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
      ("$dbtype:host=$host;dbname=$db", $user, $pass,
       array(PDO::ATTR_PERSISTENT => true));
  }

  public function do_inits() {
    /* Define scales */
    $this->nb_groups_ratio           = 1/5;
    $this->user_per_groups           = 2;
    $this->priv_contact_per_user     = 50;
    $this->pub_contact_per_user      = 10;
    $this->nb_normal_events_per_user = 100;
    $this->nb_reccur_events_per_user = 100;
    $this->nb_ext_contact_infos = array
    /* 'Email' => 2 will create rand(0,2) mails per contact */
      ('Email' => 2, 'Website' => 1, 'IM' => 1, 'Address' => 2, 'Phone' => 3);
    $today_date = strftime("%Y-%m-%d", time()-(2*3600));

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
      'foobar',
      '',
      'foo.bar',
      '')");
    $this->domain_id = $this->lastInsertId();

    /* Fill it with initial data from the global domain */
    run_query_domain_init_data($this->domain_id);

    /* Keep infos we will need */
    $res = $this->query("
SELECT kind_id FROM Kind
WHERE kind_domain_id = '$this->domain_id'");
    $kinds = $res->fetchAll(PDO::FETCH_COLUMN);
    $this->available_kinds = array_values($kinds);

    $res = $this->query("
SELECT eventcategory1_id FROM EventCategory1
WHERE eventcategory1_domain_id = '$this->domain_id'");
    $cats = $res->fetchAll(PDO::FETCH_COLUMN);
    $this->available_event_cats = array_values($cats);
  }

  public function genDummyData($nb_users)
  {
    $this->do_inits();

    $nb_groups = (int)($nb_users * $this->nb_groups_ratio);

    print "Inserting $nb_users users... ";
    $users_ids      = new IntIterator(array('start' => 2, 'len' => $nb_users));
    $usergroups_ids = new IntIterator(array('start' => 1, 'len' => $nb_groups,
                                            'every' => 1/$this->nb_groups_ratio));
    $this->createUsers(clone $users_ids, clone $usergroups_ids);
    print "done\n";
    $last_user_entity_id = $this->last_entity_id;


    print "Inserting $nb_groups groups... ";
    $groups_ids     = new IntIterator(array('start' => 1, 'len' => $nb_groups));
    $this->createGroups(clone $groups_ids);
    print "done\n";

    print "Randomly linking $nb_users users to $nb_groups groups... ";
    $this->createUserGroupLink(clone $users_ids, clone $groups_ids);
    print "done\n";

    /* Contacts */
    // we guess the next entities ids
    $next_entity_id = $this->last_entity_id + 1;

    $nb_priv_contacts = $nb_users * $this->priv_contact_per_user;
    print "Inserting $this->priv_contact_per_user privates contacts for each "
      ."user (total $nb_priv_contacts contacts)... ";
    $contacts_ids = new IntIterator(array('start' => 1,
                                          'len' => $nb_priv_contacts));
    $this->createAllUsersContacts(0, clone $users_ids, $contacts_ids);
    print "done\n";

    $nb_pub_contacts = $nb_users * $this->pub_contact_per_user;
    print "Inserting $this->pub_contact_per_user publics contacts for each "
      ."user (total $nb_pub_contacts contacts)... ";
    $contacts_ids = new IntIterator(array('start' => $contacts_ids->end()+1,
                                          'len'   => $nb_pub_contacts));
    $this->createAllUsersContacts(1, clone $users_ids, $contacts_ids);
    print "done\n";

    $extinfos_eids = new IntIterator
     ( array('start' => $next_entity_id,
             'end'   => $this->last_entity_id) );
    $total_contacts = $nb_pub_contacts + $nb_priv_contacts;
    print "Randomly inserting informations for all the "
      ."$total_contacts contacts:\n";
    $this->createContactsExtInfos($extinfos_eids);
    print "done\n";

    print "Inserting $nb_users calendars... ";
    $this->createCalendars(clone $users_ids);
    print "done\n";

    $nb_events = $nb_users * $this->nb_normal_events_per_user;
    $users_eids = new IntIterator
      ( array('start' => $last_user_entity_id - $nb_users,
              'end'   => $last_user_entity_id) );
    print "Inserting $this->nb_normal_events_per_user events for each user "
      ."(total $nb_events)... ";
    $this->createEvents(clone $users_ids, clone $users_eids,
                        $this->nb_normal_events_per_user);
    print "done\n";
  }
  
  public function createAllUsersContacts($is_public, $uid_iter, $ctct_iter)
  {
    while($uid = $uid_iter->nextInt()) {
      $this->createContacts($is_public, $ctct_iter, $uid);
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
        ( $uid, "u$uid",                          // sql id, login
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
  
  public function createContacts($is_public, $ctct_iter, $uid)
  {
    /* Static for each record */
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
        'contact_privacy'             => "'$is_public'",
        'contact_date'                => "'$this->today_date'",
        'contact_origin'              => "'obm-dummyzator'"
        );
    /* Dynamic stuff */
    $dyna_cols = array
      ( 'contact_id', 'contact_kind_id', 'contact_lastname',
        'contact_firstname', 'contact_middlename', 'contact_aka',
        'contact_sound', 'contact_comment' );

    $contacts = $this->massInsertorStatment('Contact', $statics, $dyna_cols);
    while($cid = $ctct_iter->nextInt()) {
      $lname = 'C'.randomAlphaStr(8);
      $changing = array
        ( $cid,                                                 // sql id
          $this->available_kinds[array_rand($this->available_kinds)], // kind
          $lname, randomAlphaStr(8),                  // lastname, firstname
          randomAlphaStr(4), randomAlphaStr(5),       // middlename, aka
          metaphone($lname), 'COM'.randomAlphaStr(15) // sound, comment
          );
      $this->execute_or_die($contacts, $changing);

      /* Add related entity */
      $entity_id = $this->newEntity();
      $this->query("INSERT INTO ContactEntity
       (contactentity_contact_id, contactentity_entity_id)
VALUES (                  '$cid',            '$entity_id')");
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

  public function createEvents($user_iter, $user_entity_iter, $events_per_user)
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
        'event_repeatkind'            => "'none'",
        'event_repeatfrequence'       => "'1'",
        'event_repeatdays'            => "'0000000'",
        'event_endrepeat'             => "'$this->today_date'",
        'event_properties'            => "'<extended_desc></extended_desc>'"
        );
    $dyna_cols = array
      ( 'event_usercreate', 'event_userupdate', 'event_ext_id', 'event_owner',
        'event_title', 'event_location', 'event_category1_id', 'event_priority',
        'event_privacy', 'event_date', 'event_duration', 'event_allday',
        'event_description' );
    $events = $this->massInsertorStatment('Event', $statics, $dyna_cols);

    /* EventLink table */
    $statics = array
      ( 'eventlink_timeupdate' => 'NOW()',
        'eventlink_timecreate' => 'NOW()',
        'eventlink_state'      => "'ACCEPTED'",
        'eventlink_required'   => "'REQ'"
      );
    $dyna_cols = array
      ( 'eventlink_event_id', 'eventlink_usercreate',
        'eventlink_entity_id', 'eventlink_percent' );
    $evlink = $this->massInsertorStatment('EventLink', $statics, $dyna_cols);

    while(1) {
      $uid = $user_iter->nextInt();
      $euid = $user_entity_iter->nextInt();
      if($uid === null || $euid === null) {
        break;
      }
              
      $date = new Of_Date(time());
      // Begin one month before today
      $date->setHour(0)->setMinute(0)->setSecond(0)->subMonth(1);
      for($i = 0 ; $i < $events_per_user ; $i++) {
        $allday = !rand(0,15);
        if($allday) {
          $duration = 3600 * 24;
          $date->setHour(0)->setMinute(0)->addDay(1);
        } else {
          $duration = rand(1,20) * 900; // Random from 1/4h to 5h
        }

        /* Insert in Event table */
        $changing = array
          ( $uid, $uid, genUniqueExtEventId(), // usercreate, userupdate, ext_id
            $uid, randomAlphaStr(8),           // owner, title
            randomAlphaStr(7),                 // location
            $this->available_event_cats
              [array_rand($this->available_event_cats)], // category1
            rand(1,3), rand(0,1),              // priority, privacy
            $date, $duration, $allday,         // begin date, duration, allday
            randomAlphaStr(20)                 // description
            );
        $this->execute_or_die($events, $changing);

        /* Insert in EventLink table the event creator as participant */
        $changing = array
          ( $this->lastInsertId(), $uid, // event id, user's event id
            $euid,rand(0,100) // event id, user's event entity id, percent state
            );
        $this->execute_or_die($evlink, $changing);

        /* Make the time to forward */
        $date->addSecond($duration);
        $date->addSecond(rand(0,40) * 900); // Random from 0h to 10h
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
    $this->last_entity_id = $this->lastInsertId();
    return $this->last_entity_id;
  }
}

