<?php

require_once dirname(__FILE__) . '/../../tests/TestsHelper.php';

require_once 'MimeTest.php';
require_once 'MimeEntityTest.php';
require_once 'MimePartTest.php';
require_once 'MimeAttachmentTest.php';
require_once 'MimeMultipartTest.php';
require_once 'MailTest.php';
require_once 'SmtpTest.php';
require_once 'MailerTest.php';

class Stato_Mailer_AllTests
{
    public static function suite()
    {
        $suite = new PHPUnit_Framework_TestSuite('Stato mailer');
        $suite->addTestSuite('Stato_MimeTest');
        $suite->addTestSuite('Stato_MimeEntityTest');
        $suite->addTestSuite('Stato_MimePartTest');
        $suite->addTestSuite('Stato_MimeAttachmentTest');
        $suite->addTestSuite('Stato_MimeMultipartTest');
        $suite->addTestSuite('Stato_MailTest');
        try {
            $conf = Stato_TestEnv::getConfig('mailer', 'smtp');
        } catch (Stato_TestsConfigFileNotFound $e) {
            $suite->markTestSuiteSkipped('You need a TestConfig.php file to run mailer SMTP tests !');
            return $suite;
        } catch (Stato_TestConfigNotFound $e) {
            $suite->markTestSuiteSkipped('Your TestConfig.php file does not appear to contain SMTP tests params !');
            return $suite;
        }
        $suite->addTestSuite('Stato_SmtpTest');
        $suite->addTestSuite('Stato_MailerTest');
        return $suite;
    }
}
