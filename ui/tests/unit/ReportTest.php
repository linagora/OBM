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

require_once 'report/reportFactory.php';

class ReportTest extends OBM_Database_TestCase {

  protected function getDataSet() {
    $csvDataSet = new OBM_Database_CsvDataSet(';');
    $csvDataSet->addEntityTable('Domain', 'domain', dirname(__FILE__).'/db_data/Domain.csv');
    $csvDataSet->addEntityTable('UserObm', 'user', dirname(__FILE__).'/db_data/UserObm.csv');
    $csvDataSet->addEntityTable('UGroup', 'group', dirname(__FILE__).'/db_data/UGroup.csv');
    $csvDataSet->addTable('of_usergroup', dirname(__FILE__).'/db_data/of_usergroup.csv');
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
  }

  public function testStandardGroupReport() {
    $report = ReportFactory::getReport(array(),'group');
    $formater = new GenericFormater();
    $formater->addField('id');
    $formater->addField('name');
    $formater->addField('domain_name');
    $formater->addField('nb_user');
    $output = $report->format($formater);
    $this->assertEquals($output, "id\tname\tdomain_name\tnb_user\t\n"
                                ."1\tAdmin\tglobal.virt\t0\t\n"
                                ."2\tCommercial\tglobal.virt\t0\t\n"
                                ."3\tProduction\tglobal.virt\t0\t\n"
                                ."4\tDéveloppeur\tzz.com\t1\t\n");
    unset($report);
    $filter1 = new GenericFilter('nb_user','>','0');
    $report = ReportFactory::getReport(array($filter1),'group');
    $output = $report->format($formater);
    $this->assertEquals($output, "id\tname\tdomain_name\tnb_user\t\n"
                                ."4\tDéveloppeur\tzz.com\t1\t\n");
    unset($report);
    $filter2 = new GenericFilter('domain_name','=','global.virt');
    $report = ReportFactory::getReport(array($filter1,$filter2),'group');
    $output = $report->format($formater);
    $this->assertEquals($output, "id\tname\tdomain_name\tnb_user\t\n");
  }

  public function testStandardUserReport() {
    $report = ReportFactory::getReport(array(),'user');
    $formater = new GenericFormater();
    $formater->addField('id');
    $formater->addField('login');
    $formater->addField('lastname');
    $formater->addField('firstname');
    $formater->addField('domain_name');
    $output = $report->format($formater);
    $this->assertEquals($output, "id\tlogin\tlastname\tfirstname\tdomain_name\t\n"
                                ."1\tadmin0\tAdmin Lastname\tFirstname\tglobal.virt\t\n"
                                ."2\tadmin1\tAdmin\tdomainezz.com\tzz.com\t\n"
                                ."3\tuser1\tDoe\tJohn\tzz.com\t\n"
                                ."4\tediteur1\tDoe\tJane\tzz.com\t\n");
    unset($report);
    $filter1 = new GenericFilter('domain_id','!=','1');
    $report = ReportFactory::getReport(array($filter1),'user');
    $output = $report->format($formater);
    $this->assertEquals($output, "id\tlogin\tlastname\tfirstname\tdomain_name\t\n"
                                ."2\tadmin1\tAdmin\tdomainezz.com\tzz.com\t\n"
                                ."3\tuser1\tDoe\tJohn\tzz.com\t\n"
                                ."4\tediteur1\tDoe\tJane\tzz.com\t\n");
    unset($report);
    $filter2 = new GenericFilter('domain_name','=','global.virt');
    $report = ReportFactory::getReport(array($filter1,$filter2),'user');
    $output = $report->format($formater);
    $this->assertEquals($output, "id\tlogin\tlastname\tfirstname\tdomain_name\t\n");
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
