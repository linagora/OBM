<?php

require_once dirname(__FILE__).'/TestsHelper.php';

require_once 'obmlib.inc';
require_once 'of_acl.php';

/**
 * OBM ACL Test
 * 
 * This test class uses fixture data automatically loaded in DB.
 * 
 * WARNING : if you need to change the fixture data, don't forget to set to NULL
 * all the fields which could break integrity constraints
 * (for example, domain_usercreate in Domain table)
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 * @author Raphaël Rougeron <raphael.rougeron@aliasource.fr> 
 * @license GPL 2.0
 */
class OBM_Acl_TestCase extends OBM_Database_TestCase {
  
  protected function getDataSet() {
    $csvDataSet = new OBM_Database_CsvDataSet(';');
    $csvDataSet->addEntityTable('Domain', 'domain', dirname(__FILE__).'/db_data/Domain.csv');
    $csvDataSet->addEntityTable('UserObm', 'user', dirname(__FILE__).'/db_data/UserObm.csv');
    $csvDataSet->addEntityTable('UGroup', 'group', dirname(__FILE__).'/db_data/UGroup.csv');
    $csvDataSet->addEntityTable('Cv', 'cv', dirname(__FILE__).'/db_data/Cv.csv');
    $csvDataSet->addTable('UserObmGroup', dirname(__FILE__).'/db_data/UserObmGroup.csv');
    return $csvDataSet;
  }
  
  public function testBasics() {
    OBM_Acl::initialize();
    $this->assertFalse(OBM_Acl::isAllowed(2, 'cv', 1, 'read'));
    OBM_Acl::allow(2, 'cv', 1, 'read');
    $this->assertTrue(OBM_Acl::isAllowed(2, 'cv', 1, 'read'));
    $this->assertFalse(OBM_Acl::isAllowed(2, 'cv', 1, 'write'));
    OBM_Acl::allow(2, 'cv', 1, 'write');
    $this->assertTrue(OBM_Acl::isAllowed(2, 'cv', 1, 'read'));
    $this->assertTrue(OBM_Acl::isAllowed(2, 'cv', 1, 'write'));
    $this->assertTrue(OBM_Acl::canRead(2, 'cv', 1));
    $this->assertTrue(OBM_Acl::canWrite(2, 'cv', 1));
    $this->assertFalse(OBM_Acl::canAdmin(2, 'cv', 1));
    
    $this->assertTrue(OBM_Acl::canWrite(2, 'cv', array(1, 2)));
    
    // special entities
    $this->addCalendar(2);
    $this->addCalendar(3);
    $this->assertTrue(OBM_Acl::isAllowed(2, 'calendar', 2, 'read'));
    $this->assertTrue(OBM_Acl::isAllowed(2, 'calendar', 2, 'write'));
    $this->assertTrue(OBM_Acl::canRead(2, 'calendar', 2));
    $this->assertTrue(OBM_Acl::canWrite(2, 'calendar', 2));
    $this->assertTrue(OBM_Acl::canAdmin(2, 'calendar', 2));
    
    $this->assertTrue(OBM_Acl::canWrite(2, 'calendar', array(2, 3)));
  }
  
  public function testGroupBasics() {
    OBM_Acl::initialize();
    $this->assertFalse(OBM_Acl::isAllowed(3, 'cv', 1, 'read'));
    OBM_Acl::allowGroup(4, 'cv', 1, 'read');
    $this->assertTrue(OBM_Acl::isAllowed(3, 'cv', 1, 'read'));
    $this->assertFalse(OBM_Acl::isAllowed(3, 'cv', 1, 'write'));
    OBM_Acl::allow(3, 'cv', 1, 'write');
    $this->assertTrue(OBM_Acl::isAllowed(3, 'cv', 1, 'read'));
    $this->assertTrue(OBM_Acl::isAllowed(3, 'cv', 1, 'write'));
  }
  
  public function testSetRights() {
    OBM_Acl::initialize();
    OBM_Acl::setRights(2, 'cv', 1, array('access' => true, 'read' => true, 'write' => false, 'admin' => false));
    $this->assertTrue(OBM_Acl::canAccess(2, 'cv', 1));
    $this->assertTrue(OBM_Acl::canRead(2, 'cv', 1));
    $this->assertFalse(OBM_Acl::canWrite(2, 'cv', 1));
    $this->assertFalse(OBM_Acl::canAdmin(2, 'cv', 1));
    OBM_Acl::setRights(2, 'cv', 1, array('access' => 1, 'read' => 0, 'write' => true, 'admin' => false));
    $this->assertTrue(OBM_Acl::canAccess(2, 'cv', 1));
    $this->assertFalse(OBM_Acl::canRead(2, 'cv', 1));
    $this->assertTrue(OBM_Acl::canWrite(2, 'cv', 1));
    $this->assertFalse(OBM_Acl::canAdmin(2, 'cv', 1));
  }
  
  public function testGetRights() {
    $this->assertEquals(OBM_Acl::getRights(3, 'cv', 1), array(
      'access' => 0,
      'read'   => 0,
      'write'  => 0,
      'admin'  => 0
    ));
    OBM_Acl::allowGroup(4, 'cv', 1, 'read');
    OBM_Acl::allow(3, 'cv', 1, 'write');
    $this->assertEquals(OBM_Acl::getRights(3, 'cv', 1), array(
      'access' => 0,
      'read'   => 1,
      'write'  => 1,
      'admin'  => 0
    ));
  }
  
  public function testGetAllowedEntities() {
    OBM_Acl::allow(2, 'cv', 1, 'read');
    OBM_Acl::allow(2, 'cv', 2, 'read');
    OBM_Acl::allow(2, 'cv', 3, 'read');
    $this->assertEquals(OBM_Acl::getAllowedEntities(2, 'cv', 'read', null, 'title'), array(
        1 => 'CV Admin',
        2 => 'CV John Doe',
        3 => 'CV Jane Doe'
    ));
    $this->assertEquals(OBM_Acl::getAllowedEntities(2, 'cv', 'read', array(3,4), 'title'), array(
        3 => 'CV Jane Doe'
    ));
    $this->assertEquals(OBM_Acl::getAllowedEntities(2, 'cv', 'read', array(2,3,4), 'title'), array(
        2 => 'CV John Doe',
        3 => 'CV Jane Doe'
    ));
  }
  
  public function testGetAllowedSpecialEntities() {
    $this->addCalendar(2);
    $this->addCalendar(3);
    OBM_Acl::allow(4, 'calendar', 2, 'read');
    OBM_Acl::allow(4, 'calendar', 3, 'read');
    $this->assertEquals(OBM_Acl::getAllowedEntities(4, 'calendar', 'read'), array(
        2 => 'Admin domainezz.com',
        3 => 'Doe John',
        4 => 'Doe Jane'
    ));
  }
  
  public function testPublicRights() {
    OBM_Acl::initialize();
    $this->assertFalse(OBM_Acl::canAccess(2, 'cv', 1));
    $this->assertFalse(OBM_Acl::canRead(2, 'cv', 1));
    $this->assertFalse(OBM_Acl::canWrite(2, 'cv', 1));
    $this->assertFalse(OBM_Acl::canAdmin(2, 'cv', 1));
    OBM_Acl::setPublicRights('cv', 1, array('access' => 1, 'read' => 1, 'write' => 0));
    $this->assertTrue(OBM_Acl::canAccess(2, 'cv', 1));
    $this->assertTrue(OBM_Acl::canRead(2, 'cv', 1));
    $this->assertFalse(OBM_Acl::canWrite(2, 'cv', 1));
    $this->assertFalse(OBM_Acl::canAdmin(2, 'cv', 1));
    OBM_Acl::allow(2, 'cv', 1, 'admin');
    $this->assertTrue(OBM_Acl::canAccess(2, 'cv', 1));
    $this->assertTrue(OBM_Acl::canRead(2, 'cv', 1));
    $this->assertFalse(OBM_Acl::canWrite(2, 'cv', 1));
    $this->assertTrue(OBM_Acl::canAdmin(2, 'cv', 1));
    $this->assertEquals(OBM_Acl::getAllowedEntities(2, 'cv', 'read', null, 'title'), array(1 => 'CV Admin'));
    OBM_Acl::setPublicRights('cv', 1, array('access' => 1, 'read' => 1, 'write' => 0, 'admin' => 1));
    $this->assertTrue(OBM_Acl::canAccess(3, 'cv', 1));
    $this->assertTrue(OBM_Acl::canRead(3, 'cv', 1));
    $this->assertFalse(OBM_Acl::canWrite(3, 'cv', 1));
    $this->assertFalse(OBM_Acl::canAdmin(3, 'cv', 1));
    $this->assertEquals(OBM_Acl::getPublicRights('cv', 1), array(
      'access' => 1, 'read' => 1, 'write' => 0, 'admin' => 0
    ));
  }
  
  public function testGetEntityRoles() {
    OBM_Acl::allow(1, 'cv', 1, 'admin');
    OBM_Acl::allow(2, 'cv', 1, 'read');
    OBM_Acl::allow(3, 'cv', 1, 'write');
    $users = OBM_Acl::getEntityUsers('cv', 1);
    $this->assertEquals($users[1], array('id' => 1, 'label' => 'Admin Lastname Firstname', 
      'access' => 0, 'read' => 0, 'write' => 0, 'admin' => 1
    ));
    $this->assertEquals($users[2], array('id' => 2, 'label' => 'Admin domainezz.com', 
      'access' => 0, 'read' => 1, 'write' => 0, 'admin' => 0
    ));
    $this->assertEquals($users[3], array('id' => 3, 'label' => 'Doe John', 
      'access' => 0, 'read' => 0, 'write' => 1, 'admin' => 0
    ));
    OBM_Acl::allowGroup(4, 'cv', 1, 'read');
    $users = OBM_Acl::getEntityUsers('cv', 1);
    $this->assertEquals($users[1], array('id' => 1, 'label' => 'Admin Lastname Firstname', 
      'access' => 0, 'read' => 0, 'write' => 0, 'admin' => 1
    ));
    $this->assertEquals($users[2], array('id' => 2, 'label' => 'Admin domainezz.com', 
      'access' => 0, 'read' => 1, 'write' => 0, 'admin' => 0
    ));
    $this->assertEquals($users[3], array('id' => 3, 'label' => 'Doe John', 
      'access' => 0, 'read' => 1, 'write' => 1, 'admin' => 0
    ));
    $consumers = OBM_Acl::getEntityConsumers('cv', 1);
    $this->assertEquals($consumers[0], array('id' => 4, 'label' => 'DÃ©veloppeur', 'consumer' => 'group',
      'access' => 0, 'read' => 1, 'write' => 0, 'admin' => 0
    ));
    $this->assertEquals($consumers[1], array('id' => 2, 'label' => 'Admin domainezz.com', 'consumer' => 'user',
      'access' => 0, 'read' => 1, 'write' => 0, 'admin' => 0
    ));
    $this->assertEquals($consumers[2], array('id' => 1, 'label' => 'Admin Lastname Firstname', 'consumer' => 'user',
      'access' => 0, 'read' => 0, 'write' => 0, 'admin' => 1
    ));
    $this->assertEquals($consumers[3], array('id' => 3, 'label' => 'Doe John', 'consumer' => 'user',
      'access' => 0, 'read' => 0, 'write' => 1, 'admin' => 0
    ));
  }
  
  public function testAclUtils() {
    $params = array(
      'access_public' => 1,
      'read_public' => 1,
      'accept_admin' => array(
        0 => 'data-user-23',
        1 => 'data-user-112'
      ),
      'accept_write' => array(
        0 => 'data-user-23',
        1 => 'data-user-112'
      )
    );
    $this->assertEquals(OBM_Acl_Utils::parseRightsParams($params), array(
      'user' => array(
        23 => array('admin' => 1, 'write' => 1),
        112 => array('admin' => 1, 'write' => 1)
      ),
      'group' => array(),
      'public' => array('access' => 1, 'read' => 1)
    ));
    
    $entities = array(
      1 => 'CV Admin',
      2 => 'CV John Doe',
      3 => 'CV Jane Doe'
    );
    $this->assertEquals(OBM_Acl_Utils::expandEntitiesArray($entities), array(
      'ids' => array(1,2,3),
      'entity' => array(
        array('id' => 1, 'label' => 'CV Admin'),
        array('id' => 2, 'label' => 'CV John Doe'),
        array('id' => 3, 'label' => 'CV Jane Doe')
    )));
  }
  
  private function addCalendar($userId) {
    $this->pdo->exec('INSERT INTO Entity (entity_mailing) VALUES (TRUE)');
    $entityId = $this->pdo->lastInsertId();
    $query = "INSERT INTO CalendarEntity (calendarentity_entity_id, calendarentity_calendar_id)
              VALUES ($entityId, $userId)";
    $this->pdo->exec($query);
    return $entityId;
  }
}
 
