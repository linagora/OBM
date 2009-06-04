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
  require_once 'obminclude/of/report/command.php';
  require_once 'obminclude/of/report/sender/mailSender.php';
  require_once 'obminclude/of/report/sender/stdoutSender.php';
  require_once 'obminclude/of/report/sender/downloadSender.php';

  /**
   * UserReportCommand 
   * 
   * @uses Command
   * @package 
   * @version $id:$
   * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
   * @author Benoît Caudesaygues <benoit.caudesaygues@aliasource.fr> 
   * @license GPL 2.0
   */
  class ActiveUserReportCommand extends Command {
    protected $name = 'activeuser';
    const kind = 'user';

    protected function execute() {
      $this->kind = 'user';
      
      $this->sender = new stdoutSender;
      $this->sender->setNext(new downloadSender());

      //Workflow Filter
      $this->filters[] = new GenericFilter('archive','==','1');
      $this->filters[] = new GenericFilter('status','==','INIT');

      $this->formater = new GenericFormater;
      $this->formater->addField('id');
      $this->formater->addField('login');
      $this->formater->addField('lastname');
      $this->formater->addField('firstname');
      $this->formater->addField('domain_name');
    }

    public function getKind() {
      return self::kind;
    }
  }
?>
