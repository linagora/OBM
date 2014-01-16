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



require_once dirname(__FILE__).'/TestsHelper.php';
require_once 'php/calendar/calendar_mailer.php';
require_once 'php/calendar/event_observer.php';
require_once 'php/calendar/calendar_mailer.php';
require_once 'php/calendar/event_observer.php';
require_once 'of_date.inc';
require_once 'of_acl.php';
require_once 'obmlib.inc';

class EventMailObserverTest extends OBM_Database_TestCase {
  
  protected function getDataSet() {
    $csvDataSet = new OBM_Database_CsvDataSet(';');
    $csvDataSet->addTable('Entity');    
    $csvDataSet->addTable('Domain');
    $csvDataSet->addTable('DomainEntity');
    $csvDataSet->addTable('UserObm');
    $csvDataSet->addTable('UserEntity');
    $csvDataSet->addTable('UserObmPref');
    $csvDataSet->addTable('UGroup');
    $csvDataSet->addTable('GroupEntity');
    $csvDataSet->addTable('Resource');
    $csvDataSet->addTable('ResourceEntity');
    $csvDataSet->addTable('EventCategory1');
    $csvDataSet->addTable('Event');
    $csvDataSet->addTable('EventLink');
    $csvDataSet->addTable('EventException');
    $csvDataSet->addTable('of_usergroup');
    $csvDataSet->addTable('EntityRight');
    return $csvDataSet;
  }

  public function setup() {
    parent::setup();
    $GLOBALS['obm']['uid'] = 3;
    $this->mailObserver = new OBM_EventMailObserver();
    OBM_EventFactory::getInstance()->attach($this->mailObserver);
    SMailer::set_default_transport(new Stato_StaticTransport());
    OBM_Acl::initialize();
  }

  public function tearDown() {
    OBM_EventFactory::getInstance()->detach($this->mailObserver);
  }

  public function testNewEventMail() {
    $data = array ( 
      'id' => 1,
      'title' => 'Title' ,
      'owner' => '2' ,
      'location' => 'Location' ,
      'category1' => '9' ,
      'privacy' => 1,
      'date_begin' => new Of_Date('2009-03-24 08:00:00'),
      'date_end' => new Of_Date('2009-03-24 09:00:00'),
      'priority' => '2' ,
      'color' => '#cc3333' ,
      'repeat_kind' => 'daily' ,
      'repeatfrequency' => '1' ,
      'repeat_end' => new Of_Date('2009-04-30'),
      'date_exception' => array (0 => new Of_Date('2009-03-25 08:00:00')),
      'description' => 'Description' ,
      'duration' => 3600,
      'repeat_days' => '0000000', 
      'user' => array (0 => 2),
      'resource' => array(0 => 1),
      'contact' => array ()
    );
    $event = OBM_EventFactory::getInstance()->create($data);
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('New event on OBM: Title',$mailData[0]['subject']);
    $this->assertEquals("Jane Doe <editeur1@zz.com>", $mailData[0]['to']);
    $this->assertContains("Message automatique envoyé par OBM
------------------------------------------------------------------
NOUVEAU RENDEZ-VOUS !
------------------------------------------------------------------

Vous êtes invité à participer à ce rendez-vous

du     : 24/03/2009 08:00
au     : 24/03/2009 09:00
sujet  : Title
lieu   : Location
auteur : domainezz.com Admin
",$mailData[0]['content']);
    $this->assertEquals('New resource reservation on OBM: Title',$mailData[1]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[1]['to']);
    $this->assertContains("Message automatique envoyé par OBM
------------------------------------------------------------------
NOUVELLE RESERVATION DE RESSOURCE !
------------------------------------------------------------------

Une ressource dont vous êtes responsable a été réservée

du     : 24/03/2009 08:00
au     : 24/03/2009 09:00
sujet  : Title
lieu   : Location
auteur : domainezz.com Admin
",$mailData[1]['content']);
  }

  public function testDeleteEventMail() {
    $event = OBM_EventFactory::getInstance()->getById(1);
    OBM_EventFactory::getInstance()->delete($event);
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('Event cancelled on OBM: Title',$mailData[0]['subject']);
    $this->assertEquals("Jane Doe <editeur1@zz.com>", $mailData[0]['to']);
    $this->assertContains("Message automatique envoyé par OBM
------------------------------------------------------------------
RENDEZ-VOUS ANNULÉ !
------------------------------------------------------------------

Le rendez-vous suivant a été annulé

du     : 24/03/2009 08:00
au     : 24/03/2009 09:00
sujet  : Title
lieu   : Location
auteur : domainezz.com Admin
",$mailData[0]['content']);    
    $this->assertEquals('Event cancelled on OBM: Title',$mailData[1]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[1]['to']);
    $this->assertContains("Message automatique envoyé par OBM
------------------------------------------------------------------
RESERVATION DE RESSOURCE ANNULEE !
------------------------------------------------------------------

La réservation suivante a été annulée

du     : 24/03/2009 08:00
au     : 24/03/2009 09:00
sujet  : Title
lieu   : Location
auteur : domainezz.com Admin
",$mailData[1]['content']);    

  }  

  public function testModifyEventMail() {
    $event = OBM_EventFactory::getInstance()->getById(1);
    $event->add('user',2);
    $event->add('user',3);
    $event->del('user',4);
    $event->location = 'New Location';
    OBM_EventFactory::getInstance()->store($event,OBM_EventFactory::getInstance()->getById(1));
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('New event on OBM: Title',$mailData[0]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[0]['to']);
    $this->assertContains("Message automatique envoyé par OBM
------------------------------------------------------------------
NOUVEAU RENDEZ-VOUS !
------------------------------------------------------------------

Vous êtes invité à participer à ce rendez-vous

du     : 24/03/2009 08:00
au     : 24/03/2009 09:00
sujet  : Title
lieu   : New Location
auteur : domainezz.com Admin
",$mailData[0]['content']);

    $this->assertEquals('Resource reservation updated on OBM: Title',$mailData[1]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[1]['to']);
    $this->assertContains("Message automatique envoyé par OBM
------------------------------------------------------------------
MODIFICATION D'UNE RESERVATION DE RESSOURCE !
------------------------------------------------------------------

Le rendez-vous Title, initialement prévu du 24/03/2009 08:00 au 24/03/2009 09:00, (lieu : Location),
a été modifié et se déroulera du 24/03/2009 08:00 au 24/03/2009 09:00, (lieu : New Location).

:: Pour plus de détails : 
/calendar/calendar_index.php?action=detailconsult&calendar_id=1

:: Pour accepter les modifications :
/calendar/calendar_index.php?action=update_decision&calendar_id=1&entity_kind=user&rd_decision_event=ACCEPTED

:: Pour refuser les modifications : 
/calendar/calendar_index.php?action=update_decision&calendar_id=1&entity_kind=user&rd_decision_event=DECLINED
",$mailData[1]['content']);

    $this->assertEquals('Event cancelled on OBM: Title',$mailData[2]['subject']);
    $this->assertEquals("Jane Doe <editeur1@zz.com>", $mailData[2]['to']);
    $this->assertContains("Message automatique envoyé par OBM
------------------------------------------------------------------
RENDEZ-VOUS ANNULÉ !
------------------------------------------------------------------

Le rendez-vous suivant a été annulé

du     : 24/03/2009 08:00
au     : 24/03/2009 09:00
sujet  : Title
lieu   : Location
auteur : domainezz.com Admin
",$mailData[2]['content']); 

    $event = OBM_EventFactory::getInstance()->getById(1);
    $event->location = 'New Location 2';
    $event->del('resource', 1);
    OBM_EventFactory::getInstance()->store($event,OBM_EventFactory::getInstance()->getById(1));
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('Event updated on OBM: Title',$mailData[3]['subject']);
    $this->assertEquals("Jane Doe <editeur1@zz.com>", $mailData[3]['to']);
    $this->assertContains("Message automatique envoyé par OBM
------------------------------------------------------------------
RENDEZ-VOUS MODIFIÉ !
------------------------------------------------------------------

Le rendez-vous Title, initialement prévu du 24/03/2009 08:00 au 24/03/2009 09:00, (lieu : Location),
a été modifié et se déroulera du 24/03/2009 08:00 au 24/03/2009 09:00, (lieu : New Location 2).

:: Pour plus de détails : 
calendar/calendar_index.php?action=detailconsult&calendar_id=1

:: Pour accepter les modifications :
calendar/calendar_index.php?action=update_decision&calendar_id=1&entity_kind=user&rd_decision_event=ACCEPTED

:: Pour refuser les modifications : 
calendar/calendar_index.php?action=update_decision&calendar_id=1&entity_kind=user&rd_decision_event=DECLINED
",$mailData[3]['content']);
    $this->assertEquals('Event cancelled on OBM: Title',$mailData[4]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[4]['to']);
    $this->assertContains("Message automatique envoyé par OBM
------------------------------------------------------------------
RESERVATION DE RESSOURCE ANNULEE !
------------------------------------------------------------------

La réservation suivante a été annulée

du     : 24/03/2009 08:00
au     : 24/03/2009 09:00
sujet  : Title
lieu   : Location
auteur : domainezz.com Admin
",$mailData[4]['content']);    


  }

  public function testModifyParticipationStateMail() {
    $event = OBM_EventFactory::getInstance()->getById(1);
    $users = $event->get('user');
    $resources = $event->get('resource');
    $user = $users[0];
    $res = $resources[0];

    $user->set('state', 'NEEDS-ACTION');
    OBM_EventFactory::getInstance()->store($event,OBM_EventFactory::getInstance()->getById(1));
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('Participation updated on OBM: Title',$mailData[0]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[0]['to']);
    $this->assertContains("NEEDS-ACTION",$mailData[0]['content']);

    $user->set('state', 'DECLINED');
    OBM_EventFactory::getInstance()->store($event,OBM_EventFactory::getInstance()->getById(1));
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('Participation updated on OBM: Title',$mailData[1]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[1]['to']);
    $this->assertContains("DECLINED",$mailData[1]['content']);  

    $user->set('state', 'ACCEPTED');
    OBM_EventFactory::getInstance()->store($event,OBM_EventFactory::getInstance()->getById(1));
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertNull($mailData[2]);

    $res->set('state', 'DECLINED');
    OBM_EventFactory::getInstance()->store($event,OBM_EventFactory::getInstance()->getById(1));
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('Resource participation updated on OBM: Title',$mailData[2]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[2]['to']);
    $this->assertContains("DECLINED",$mailData[2]['content']);  

    $res->set('state', 'NEEDS-ACTION');
    OBM_EventFactory::getInstance()->store($event,OBM_EventFactory::getInstance()->getById(1));
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('Resource participation updated on OBM: Title',$mailData[3]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[3]['to']);
    $this->assertContains("NEEDS-ACTION",$mailData[3]['content']);  

    $res->set('state', 'ACCEPTED');
    OBM_EventFactory::getInstance()->store($event,OBM_EventFactory::getInstance()->getById(1));
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertNull($mailData[4]);
  }

}

class Stato_StaticTransport implements SIMailTransport {

  private static $mailQ;

  public function __construct() {
    self::$mailQ = array();
  }

  public function send(SMail $mail) {
    self::$mailQ[] = array('to' => $mail->get_to(), 'subject' => $mail->get_subject(), 'content' => $mail->get_content());
  }

  public static function getMailQ() {
    return self::$mailQ;
  }
}

