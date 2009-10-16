<?php

require_once(dirname(__FILE__).'/../lib/Stato/i18n/i18n.php');
if($GLOBALS['module']) {
  SI18n::add_data_path(dirname(__FILE__).'/../../conf/locale/'.$GLOBALS['module']);
  SI18n::add_data_path(dirname(__FILE__).'/../../locale/'.$GLOBALS['module']);
}
SI18n::add_data_path(dirname(__FILE__).'/../../conf/locale');
SI18n::add_data_path(dirname(__FILE__).'/../../locale');
