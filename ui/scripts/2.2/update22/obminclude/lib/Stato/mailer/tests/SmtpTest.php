<?php

require_once dirname(__FILE__) . '/../../tests/TestsHelper.php';

require_once 'transports/smtp.php';

class Stato_SmtpTest extends PHPUnit_Framework_TestCase
{
    public function setup()
    {
        $conf = Stato_TestEnv::getConfig('mailer', 'smtp');
        $this->smtp = new Stato_SmtpTransport($conf['host'], $conf);
    }
    
    public function testSend()
    {
        $mail = new Stato_Mail();
        $mail->setFrom('root@localhost');
        $mail->addTo('root@localhost', 'John Doe');
        $mail->setText('test');
        $mail->setHtmlText('<b>test</b>');
        $this->assertTrue($mail->send($this->smtp));
    }
}