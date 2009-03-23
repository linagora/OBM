<?php

require_once dirname(__FILE__) . '/../../tests/TestsHelper.php';

require_once 'i18n.php';
require_once 'backend/abstract.php';
require_once 'backend/xliff.php';

class Stato_I18n_XliffBackendTest extends Stato_I18n_YamlBackendTest
{
    public function setup()
    {
        Stato_I18n::addDataPath(dirname(__FILE__).'/data/xliff');
        $this->backend = new Stato_I18n_XliffBackend();
    }
}