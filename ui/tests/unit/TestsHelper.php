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



require_once 'PHPUnit/Framework.php';
require_once 'PHPUnit/Extensions/Database/TestCase.php';
require_once 'PHPUnit/Extensions/Database/DataSet/CsvDataSet.php';
require_once 'PHPUnit/Extensions/Database/DataSet/ReplacementDataSet.php';
require_once 'PHPUnit/Extensions/SeleniumTestCase.php';

// It would be great to run these tests in E_STRICT mode, but that's not
// currently possible because of unitialized variables
// error_reporting( E_ALL | E_STRICT );

date_default_timezone_set('Europe/Paris');

// Prepend the OBM of/, phplib/ and tests/unit/ directories to the include_path
$path = array(
  dirname(__FILE__),
  dirname(__FILE__).'/../..',
  dirname(__FILE__).'/../../lib',
  dirname(__FILE__).'/../../app',
  dirname(__FILE__).'/../../app/default/models',
  dirname(__FILE__).'/../../obminclude/of',
  dirname(__FILE__).'/../../obminclude/phplib',
  get_include_path()
);
set_include_path(implode(PATH_SEPARATOR, $path));

// Necessary global variable
$obminclude = dirname(__FILE__).'/../../obminclude';

// Autoloading
require_once 'OBM/Autoloader.php';
require_once 'OBM/Autoloader/Standard.php';
$autoloader = OBM_Autoloader::getInstance();
$autoloader->autoloaders['OBM'] =  new OBM_Autoloader_Standard();
$autoloader->autoloaders['Stato'] =  new OBM_Autoloader_Standard();
$autoloader->autoloaders['*'] = new OBM_Autoloader_Resource();

// Get DB conf and globalize it
$db_conf = include('conf/db.php');
extract($db_conf);
$obmdb_dbtype = strtoupper($obmdb_dbtype);

// Fake function
function display_debug_msg() { }

class OBM_FixtureLoader {
  
  protected $pdo;
  protected $test_db;
  
  public function __construct() {
    $db_conf = include('conf/db.php');
    extract($db_conf);
    $this->pdo = new PDO("{$obmdb_dbtype}:host={$obmdb_host};dbname={$obmdb_db}", $obmdb_user, $obmdb_password);
    $truncate = new PHPUnit_Extensions_Database_Operation_Truncate(true);
    $truncate->setCascade(true);
    $dataset = new OBM_Database_CsvDataSet(';');
    $dataset->addTable('Entity');
    $truncate->execute($this->getDatabaseTester()->getConnection(), $dataset);
    $this->test_db = $obmdb_db;
  }
  
  public function setUp($dataset) {
    $this->getDatabaseTester()->setSetUpOperation($this->getSetUpOperation());
    $this->getDatabaseTester()->setDataSet($dataset);
    $this->getDatabaseTester()->onSetUp();
  }

  public function tearDown($dataset) {
    $this->getDatabaseTester()->setTearDownOperation($this->getTearDownOperation());
    $this->getDatabaseTester()->setDataSet($dataset);
    $this->getDatabaseTester()->onTearDown();
    $this->databaseTester = NULL;
  }
  
  public function getPDOConnection() {
    return $this->pdo;
  }
  
  protected function getDatabaseTester() {
    if (empty($this->databaseTester)) {
        $this->databaseTester = new PHPUnit_Extensions_Database_DefaultTester($this->getConnection());
    }
    return $this->databaseTester;
  }
  
  protected function getConnection() {
    return $this->createDefaultDBConnection($this->pdo, $this->test_db);
  }
  
  protected function createDefaultDBConnection(PDO $connection, $schema) {
    return new PHPUnit_Extensions_Database_DB_DefaultDatabaseConnection($connection, $schema);
  }
  
  protected function getSetupOperation() {
    $truncate = new PHPUnit_Extensions_Database_Operation_Truncate(true);
    $truncate->setCascade(true);
    return new PHPUnit_Extensions_Database_Operation_Composite(array(
      $truncate,
      new OBM_Database_Operation_Entity_Insert()
    ));
  }
  
  protected function getTearDownOperation() {
    return PHPUnit_Extensions_Database_Operation_Factory::NONE();
  }
}

/**
 * OBM Database TestCase base class
 * 
 * This abstract class takes care of DB fixtures loading.
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2008 Groupe LINAGORA
 * @author Raphaël Rougeron <raphael.rougeron@aliasource.fr> 
 */
abstract class OBM_Database_TestCase extends PHPUnit_Framework_TestCase {
  protected $fixtureLoader;
  
  public function __construct($name = NULL, array $data = array(), $dataName = '') {
    parent::__construct($name, $data, $dataName);
    $this->fixtureLoader = new OBM_FixtureLoader();
  }
  
  protected abstract function getDataSet();
  
  protected function getConnection() {
    return $this->fixtureLoader->getPDOConnection();
  }
  
  protected function setUp() {
    $this->fixtureLoader->setUp($this->getDataSet());
  }
  
  protected function tearDown() {
    $this->fixtureLoader->tearDown($this->getDataSet());
  }
}

/**
 * OBM Selenium TestCase base class
 * 
 * This abstract class takes care of DB fixtures loading.
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2008 Groupe LINAGORA
 * @author Raphaël Rougeron <raphael.rougeron@aliasource.fr> 
 */
abstract class OBM_Selenium_TestCase extends PHPUnit_Extensions_SeleniumTestCase {
  protected $fixtureLoader;
  protected $dataset;
  
  public function __construct($name = NULL, array $data = array(), $dataName = '', array $browser = array()) {
    parent::__construct($name, $data, $dataName, $browser);
    $this->fixtureLoader = new OBM_FixtureLoader();
    $this->dataset = new OBM_Database_CsvDataSet(';');
  }
  
  protected function setUp() {
    $this->declareFixtures();
    $this->fixtureLoader->setUp($this->dataset);
    
    $this->setBrowser('*firefox');
    $this->setBrowserUrl('http://obm/');
  }
  
  protected function tearDown() {
    $this->fixtureLoader->tearDown($this->dataset);
  }
  
  protected function declareFixtures() {
    
  }
  
  protected function addFixture($tableName) {
    $this->dataset->addTable($tableName, dirname(__FILE__)."/db_data/$tableName.csv");
  }
  
  protected function addEntityFixture($tableName, $entityName = null) {
    if (is_null($entityName)) $entityName = strtolower($tableName);
    $this->dataset->addEntityTable($tableName, $entityName, dirname(__FILE__)."/db_data/$tableName.csv");
  }
  
  protected function getConnection() {
    return $this->fixtureLoader->getPDOConnection();
  }
}

class OBM_Database_CsvDataSet extends PHPUnit_Extensions_Database_DataSet_CsvDataSet {
  protected $entityTables = array();
  
  public function addEntityTable($tableName, $entityName, $csvFile) {
    $this->addTable($tableName, $csvFile);
    $this->entityTables[$tableName] = $entityName;
  }
  
  public function isEntityTable($tableName) {
    return isset($this->entityTables[$tableName]);
  }
  
  public function getEntityName($tableName) {
    return $this->entityTables[$tableName];
  }

  public function addTable($tableName, $csvFile=null) {
    if($csvFile == null) {
      $csvFile = dirname(__FILE__).'/db_data/'.$tableName.'.csv';
    }
    parent::addTable(strtolower($tableName), $csvFile);
  }

  protected function createIterator($reverse = FALSE) {
    $innerIterator = parent::createIterator($reverse);
    return new PHPUnit_Extensions_Database_DataSet_ReplacementTableIterator($innerIterator, array('NULL' => NULL), array());
  }
}

class OBM_Database_Operation_Entity_Insert extends PHPUnit_Extensions_Database_Operation_Insert {
  
  public function execute(PHPUnit_Extensions_Database_DB_IDatabaseConnection $connection, 
                          PHPUnit_Extensions_Database_DataSet_IDataSet $dataSet) {
    $truncate = new PHPUnit_Extensions_Database_Operation_Truncate(true);
    $truncate->setCascade(true);
    $dataset = new OBM_Database_CsvDataSet(';');
    $dataset->addTable('Entity');
    $truncate->execute($connection, $dataset);
    
    $pdo = $connection->getConnection(); // we retrieve the PDO object
    parent::execute($connection, $dataSet);
    
    $dsIterator = $dataSet->getIterator();
    foreach ($dsIterator as $table) {
      $tableName = $table->getTableMetaData()->getTableName();
      if ($dataSet->isEntityTable($tableName)) {
        $entities = $dataSet->getEntityName($tableName);
        if(!is_array($entities)) $entities = array($entities);
        foreach($entities as $entity) {
          for ($i = 1; $i <= $table->getRowCount(); $i++) {
            $pdo->exec('INSERT INTO Entity (entity_mailing) VALUES (TRUE)');
            $entityId = $pdo->lastInsertId();
            $query = 'INSERT INTO '.ucfirst($entity).'Entity ('.$entity.'entity_entity_id, '.$entity.'entity_'.$entity.'_id )
                      SELECT MAX(entity_id), '.$i.' FROM Entity';
            $pdo->exec($query);
          }
        }
      }
    }
  }
}
