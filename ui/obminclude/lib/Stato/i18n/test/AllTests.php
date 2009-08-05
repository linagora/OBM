<?php

require_once dirname(__FILE__) . '/../../test/TestsHelper.php';

require_once 'I18nTest.php';
require_once 'IntlIntegrationTest.php';
require_once 'SimpleBackendTest.php';
require_once 'YamlBackendTest.php';
require_once 'XliffBackendTest.php';

class SI18nAllTests
{
    public static function suite()
    {
        $suite = new PHPUnit_Framework_TestSuite('Stato i18n');
        $suite->addTestSuite('SI18nTest');
        $suite->addTestSuite('SIntlIntegrationTest');
        $suite->addTestSuite('SSimpleBackendTest');
        $suite->addTestSuite('SYamlBackendTest');
        $suite->addTestSuite('SXliffBackendTest');
        return $suite;
    }
}