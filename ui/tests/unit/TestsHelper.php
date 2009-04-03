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

require_once 'PHPUnit/Framework.php';
require_once 'PHPUnit/Extensions/Database/TestCase.php';
require_once 'PHPUnit/Extensions/Database/DataSet/CsvDataSet.php';
require_once 'PHPUnit/Extensions/Database/DataSet/ReplacementDataSet.php';

// It would be great to run these tests in E_STRICT mode, but that's not
// currently possible because of unitialized variables
// error_reporting( E_ALL | E_STRICT );

date_default_timezone_set('Europe/Paris');

// Prepend the OBM of/, phplib/ and tests/unit/ directories to the include_path
$path = array(
  dirname(__FILE__),
  dirname(__FILE__).'/../..',
  dirname(__FILE__).'/../../obminclude/of',
  dirname(__FILE__).'/../../obminclude/phplib',
  get_include_path()
);
set_include_path(implode(PATH_SEPARATOR, $path));

// Necessary global variable
$obminclude = dirname(__FILE__).'/../../obminclude';

// Get DB conf and globalize it
$db_conf = include('conf/db.php');
extract($db_conf);
$obmdb_dbtype = strtoupper($obmdb_dbtype);

// Fake function
function display_debug_msg() { }

/**
 * OBM Database TestCase base class
 * 
 * This abstract class takes care of DB connection.
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 * @author Raphaël Rougeron <raphael.rougeron@aliasource.fr> 
 * @license GPL 2.0
 */
abstract class OBM_Database_TestCase extends PHPUnit_Extensions_Database_TestCase {
  
  protected $pdo;
  protected $test_db;
  
  public function __construct($name = NULL, array $data = array(), $dataName = '') {
    parent::__construct($name, $data, $dataName);
    $db_conf = include('conf/db.php');
    extract($db_conf);
    $this->pdo = new PDO("{$obmdb_dbtype}:host={$obmdb_host};dbname={$obmdb_db}", $obmdb_user, $obmdb_password);
    $this->pdo->exec('TRUNCATE TABLE Entity');
    $this->test_db = $obmdb_db;
  }
  
  protected function getConnection() {
    return $this->createDefaultDBConnection($this->pdo, $this->test_db);
  }
  
  protected function getSetupOperation() {
    return new PHPUnit_Extensions_Database_Operation_Composite(array(
      new PHPUnit_Extensions_Database_Operation_Truncate(true),
      new OBM_Database_Operation_Entity_Insert()
    ));
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
    parent::addTable($tableName, $csvFile);
  }

  protected function createIterator($reverse = FALSE) {
    $innerIterator = parent::createIterator($reverse);
    return new PHPUnit_Extensions_Database_DataSet_ReplacementTableIterator($innerIterator, array('NULL' => NULL), array());
  }
}

class OBM_Database_Operation_Entity_Insert extends PHPUnit_Extensions_Database_Operation_Insert {
  
  public function execute(PHPUnit_Extensions_Database_DB_IDatabaseConnection $connection, 
                          PHPUnit_Extensions_Database_DataSet_IDataSet $dataSet) {
    $pdo = $connection->getConnection(); // we retrieve the PDO object
    $pdo->exec('TRUNCATE TABLE Entity');
    
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
                      VALUES ('.$entityId.', '.$i.')';
            $pdo->exec($query);
          }
        }
      }
    }
  }
}
