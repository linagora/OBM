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

require_once 'report/report.php';

class ReportTest extends OBM_Database_TestCase {

  protected function getDataSet() {
    $csvDataSet = new OBM_Database_CsvDataSet(';');
    return $csvDataSet;    
  }

  public function testStaticReport() {
    $report = new Report();
    $report->addRecord(new Element('John', 'Doe', 'M')); 
    $report->addRecord(new Element('Janne', 'Doe', 'F')); 
    $report->addRecord(new Element('Julia', 'Doe', 'F')); 
    $formater = new GenericFormater();
    $formater->addField('name');
    $formater->addField('surname');
    $formater->addField('gender');
    $output = $report->format($formater);
    $this->assertEquals($output, "name\tsurname\tgender\t\nJohn\tDoe\tM\t\nJanne\tDoe\tF\t\nJulia\tDoe\tF\t\n");
    //$report->addFilter(new GenericFilter('gender','=','F'));
    //$output = $report->format($formater);
    //$this->assertEquals($output, "name\tsurname\tgender\t\nJanne\tDoe\tF\t\nJulia\tDoe\tF\t\n");
  }

  public function testStandardReport() {

  }

  public function testReportSender() {

  }

  public function testReportCommand() {

  }

  public function testContextualOutput() {

  }

  public function testStructureUserReport() {

  }
  //[...]
}

class Element {
  public $name;
  public $surname;
  public $gender;

  public function __construct($name, $surname, $gender) {
    $this->name = $name;
    $this->surname = $surname;
    $this->gender = $gender;
  }
}
