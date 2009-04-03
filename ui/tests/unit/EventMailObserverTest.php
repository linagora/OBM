<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2009 OBM.org project members team                   |
 |                                                                         |
 | This program is free software; you can redistribute it and/or           |
 | modify it under the terms of the GNU General Public License             |
 | as published by the Free Software Foundation; version 2                 |
 | of the License.                                                         |
 |                                                                         |
 | This program is distributed in the hope that it will be useful,         |
 | but WITHOUT ANY WARRANTY; without even the implied warranty of          |
 | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
 | GNU General Public License for more details.                            |
 +-------------------------------------------------------------------------+
 | http://www.obm.org                                                      |
 +-------------------------------------------------------------------------+
 */
?>
<?php
require_once dirname(__FILE__).'/TestsHelper.php';
require_once 'php/calendar/calendar_query.inc';
require_once 'of_date.inc';
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
    Stato_Mailer::setDefaultTransport(new Stato_StaticTransport());
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
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[0]['to']);
    $this->assertContains("Message automatique envoyé par OBM
------------------------------------------------------------------
NOUVEAU RENDEZ-VOUS !
------------------------------------------------------------------

Vous êtes invité à participer à un rendez-vous

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

Une ressource dont vous êtes responsable à été réservée

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
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[0]['to']);
    $this->assertContains("Message automatique envoyé par OBM
------------------------------------------------------------------
RENDEZ-VOUS ANNULÉ !
------------------------------------------------------------------

Le rendez-vous suivant à été annulé

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

La réservation suivante à été annulée

du     : 24/03/2009 08:00
au     : 24/03/2009 09:00
sujet  : Title
lieu   : Location
auteur : domainezz.com Admin
",$mailData[1]['content']);    

  }  

  public function testModifyEventMail() {
    $event = OBM_EventFactory::getInstance()->getById(1);
    $event->add('user',4);
    $event->add('user',3);
    $event->del('user',2);
    $event->location = 'New Location';
    OBM_EventFactory::getInstance()->store($event,OBM_EventFactory::getInstance()->getById(1));
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('New event on OBM: Title',$mailData[0]['subject']);
    $this->assertEquals("Jane Doe <editeur1@zz.com>", $mailData[0]['to']);
    $this->assertContains("Message automatique envoyé par OBM
------------------------------------------------------------------
NOUVEAU RENDEZ-VOUS !
------------------------------------------------------------------

Vous êtes invité à participer à un rendez-vous

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
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[2]['to']);
    $this->assertContains("Message automatique envoyé par OBM
------------------------------------------------------------------
RENDEZ-VOUS ANNULÉ !
------------------------------------------------------------------

Le rendez-vous suivant à été annulé

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
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[3]['to']);
    $this->assertContains("Message automatique envoyé par OBM
------------------------------------------------------------------
RENDEZ-VOUS MODIFIÉ !
------------------------------------------------------------------

Le rendez-vous Title, initialement prévu du 24/03/2009 08:00 au 24/03/2009 09:00, (lieu : Location),
a été modifié et se déroulera du 24/03/2009 08:00 au 24/03/2009 09:00, (lieu : New Location 2).

:: Pour plus de détails : 
/calendar/calendar_index.php?action=detailconsult&calendar_id=1

:: Pour accepter les modifications :
/calendar/calendar_index.php?action=update_decision&calendar_id=1&entity_kind=user&rd_decision_event=ACCEPTED

:: Pour refuser les modifications : 
/calendar/calendar_index.php?action=update_decision&calendar_id=1&entity_kind=user&rd_decision_event=DECLINED
",$mailData[3]['content']);
    $this->assertEquals('Event cancelled on OBM: Title',$mailData[4]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[4]['to']);
    $this->assertContains("Message automatique envoyé par OBM
------------------------------------------------------------------
RESERVATION DE RESSOURCE ANNULEE !
------------------------------------------------------------------

La réservation suivante à été annulée

du     : 24/03/2009 08:00
au     : 24/03/2009 09:00
sujet  : Title
lieu   : Location
auteur : domainezz.com Admin
",$mailData[4]['content']);    


  }

  public function testModifyPartStatMail() {
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

    $user->set('state', 'DELINED');
    OBM_EventFactory::getInstance()->store($event,OBM_EventFactory::getInstance()->getById(1));
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('Participation updated on OBM: Title',$mailData[1]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[1]['to']);
    $this->assertContains("DELINED",$mailData[1]['content']);  

    $user->set('state', 'ACCEPTED');
    OBM_EventFactory::getInstance()->store($event,OBM_EventFactory::getInstance()->getById(1));
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertNull($mailData[2]);

    $res->set('state', 'DELINED');
    OBM_EventFactory::getInstance()->store($event,OBM_EventFactory::getInstance()->getById(1));
    $mailData = Stato_StaticTransport::getMailQ();
    $this->assertEquals('Resource participation updated on OBM: Title',$mailData[2]['subject']);
    $this->assertEquals("domainezz.com Admin <admin1@zz.com>", $mailData[2]['to']);
    $this->assertContains("DELINED",$mailData[2]['content']);  

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

class Stato_StaticTransport implements Stato_IMailTransport {

  private static $mailQ;

  public function __construct() {
    self::$mailQ = array();
  }

  public function send(Stato_Mail $mail) {
    self::$mailQ[] = array('to' => $mail->getTo(), 'subject' => $mail->getSubject(), 'content' => $mail->getContent());
  }

  public static function getMailQ() {
    return self::$mailQ;
  }
}

