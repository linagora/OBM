<?php

require_once dirname(__FILE__) . '/../../test/TestsHelper.php';

require_once 'transports/smtp.php';

class SSmtpTest extends PHPUnit_Framework_TestCase
{
    public function setup()
    {
        $conf = STestEnv::get_config('mailer', 'smtp');
        $this->smtp = new SSmtpTransport($conf['host'], $conf);
    }
    
    public function test_send()
    {
        $mail = new SMail();
        $mail->set_from('root@localhost');
        $mail->add_to('root@localhost', 'John Doe');
        $mail->set_text('test');
        $mail->set_html_text('<b>test</b>');
        $this->assertTrue($mail->send($this->smtp));
    }
}