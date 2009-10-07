<?php

require_once dirname(__FILE__) . '/../../tests/TestsHelper.php';

require_once 'I18nTest.php';
require_once 'IntlIntegrationTest.php';
require_once 'YamlBackendTest.php';
require_once 'XliffBackendTest.php';

class Stato_I18n_AllTests
{
    public static function suite()
    {
        $suite = new PHPUnit_Framework_TestSuite('Stato i18n');
        $suite->addTestSuite('Stato_I18nTest');
        $suite->addTestSuite('Stato_I18n_IntlIntegrationTest');
        $suite->addTestSuite('Stato_I18n_YamlBackendTest');
        $suite->addTestSuite('Stato_I18n_XliffBackendTest');
        return $suite;
    }
}