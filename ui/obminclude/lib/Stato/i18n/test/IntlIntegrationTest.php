<?php

require_once dirname(__FILE__) . '/../../test/TestsHelper.php';

require_once 'i18n.php';
require_once 'backend/abstract.php';
require_once 'backend/simple.php';

class SIntlIntegrationTest extends PHPUnit_Framework_TestCase
{
    public function setup()
    {
        if (!extension_loaded('intl'))
            $this->mark_test_skipped('The intl extension is not available');
    }
    
    public function test_set_locale_from_http_accept()
    {
        $http_accept = 'fr-fr,fr;q=0.8,en-us;q=0.6,en;q=0.4,de;q=0.2';
        SI18n::set_locale(Locale::acceptFromHttp($http_accept));
        $this->assertEquals('fr_FR', SI18n::get_locale());
    }
}

