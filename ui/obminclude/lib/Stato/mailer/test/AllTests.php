<?php

require_once dirname(__FILE__) . '/../../test/TestsHelper.php';

require_once 'MimeTest.php';
require_once 'MimeEntityTest.php';
require_once 'MimePartTest.php';
require_once 'MimeAttachmentTest.php';
require_once 'MimeMultipartTest.php';
require_once 'MailTest.php';
require_once 'SmtpTest.php';
require_once 'MailerTest.php';

class SMailer_AllTests
{
    public static function suite()
    {
        $suite = new PHPUnit_Framework_TestSuite('Stato mailer');
        $suite->addTestSuite('SMimeTest');
        $suite->addTestSuite('SMimeEntityTest');
        $suite->addTestSuite('SMimePartTest');
        $suite->addTestSuite('SMimeAttachmentTest');
        $suite->addTestSuite('SMimeMultipartTest');
        $suite->addTestSuite('SMailTest');
        /*try {
            $conf = STestEnv::get_config('mailer', 'smtp');
        } catch (STestsConfigFileNotFound $e) {
            $suite->mark_test_suite_skipped('You need a TestConfig.php file to run mailer SMTP tests !');
            return $suite;
        } catch (STestConfigNotFound $e) {
            $suite->mark_test_suite_skipped('Your TestConfig.php file does not appear to contain SMTP tests params !');
            return $suite;
        }
        $suite->addTestSuite('SSmtpTest');*/
        $suite->addTestSuite('SMailerTest');
        return $suite;
    }
}
