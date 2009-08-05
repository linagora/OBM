<?php

require_once dirname(__FILE__) . '/../../test/TestsHelper.php';

require_once 'mime/mime.php';
require_once 'mime/entity.php';
require_once 'mime/part.php';
require_once 'mime/multipart.php';
require_once 'mail.php';
require_once 'mailer.php';

require_once dirname(__FILE__).'/files/user_mailer.php';
require_once dirname(__FILE__).'/files/dummy_transport.php';

class SMailerTest extends PHPUnit_Framework_TestCase
{
    public function setup()
    {
        SMailer::set_template_root(dirname(__FILE__).'/files');
        SMailer::set_default_transport(new SDummyTransport());
        $this->user = new stdClass;
        $this->user->name = 'John Doe';
        $this->user->mail = 'john.doe@fake.net';
        $this->mailer = new UserMailer();
    }
    
    public function test_render_plain_message()
    {
        $mail = $this->mailer->prepare_welcome_message($this->user);
        $this->assertEquals('Welcome John Doe', $mail->get_content());
    }
    
    public function test_prepare_with_camelcase_method()
    {
        $mail = $this->mailer->prepareWelcomeMessage($this->user);
        $this->assertEquals('Welcome John Doe', $mail->get_content());
    }
    
    public function test_render_html_message()
    {
        $mail = $this->mailer->prepare_greetings_message($this->user);
        $this->assertEquals('Greetings <b>John Doe</b>', $mail->get_content());
    }
    
    public function test_render_missing_template_should_throw()
    {
        $this->setExpectedException('SMailException');
        $mail = $this->mailer->prepare_forgot_password_message($this->user);
    }
    
    public function test_render_body_when_template_root_not_set_should_throw()
    {
        $this->setExpectedException('SMailException', 'Template root not set');
        SMailer::set_template_root(null);
        $mail = $this->mailer->prepare_greetings_message($this->user);
    }
    
    public function test_send()
    {
        $message = <<<EOT
Date: Fri, 13 Feb 09 15:47:25 +0100
MIME-Version: 1.0
To: John Doe <john.doe@fake.net>
Content-Type: text/plain; charset="UTF-8"
Content-Transfer-Encoding: 8bit

test
EOT;
        $this->assertEquals($message, $this->mailer->send_test_message());
    }
    
    public function test_text_shortcuts()
    {
        $message = <<<EOT
Date: Fri, 13 Feb 09 15:47:25 +0100
MIME-Version: 1.0
From: notifications@dummysite.com
To: john.doe@fake.net
Subject: Welcome to our site
Content-Type: multipart/alternative; boundary="c67476988f320ca04d61815bcfd14360"

This is a multi-part message in MIME format.
--c67476988f320ca04d61815bcfd14360
Content-Type: text/plain; charset="UTF-8"
Content-Transfer-Encoding: 8bit

Welcome John Doe
--c67476988f320ca04d61815bcfd14360
Content-Type: text/html; charset="UTF-8"
Content-Transfer-Encoding: 8bit

Welcome <b>John Doe</b>
--c67476988f320ca04d61815bcfd14360--
EOT;
        $mail = $this->mailer->prepare_signup_notification($this->user);
        $mail->set_boundary('c67476988f320ca04d61815bcfd14360');
        $this->assertEquals($message, $mail->__toString());
    }
    
    public function test_part_shortcuts()
    {
        $message = <<<EOT
Date: Fri, 13 Feb 09 15:47:25 +0100
MIME-Version: 1.0
From: notifications@dummysite.com
To: john.doe@fake.net
Subject: Welcome to our site
Content-Type: multipart/mixed; boundary="c67476988f320ca04d61815bcfd14360"

This is a multi-part message in MIME format.
--c67476988f320ca04d61815bcfd14360
Content-Type: text/x-vcard; charset="UTF-8"
Content-Transfer-Encoding: 8bit

BEGIN:VCARD
END:VCARD

--c67476988f320ca04d61815bcfd14360
Content-Type: image/png; name="hello.png"
Content-Transfer-Encoding: base64
Content-Disposition: attachment; filename="hello.png"

iVBORw0KGgoAAAANSUhEUgAAAAYAAAAFCAYAAABmWJ3mAAAAAXNSR0IArs4c6QAAAAZiS0dE
AP8A/wD/oL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAIBJREFUCNc9yK8OQWEcx+HPiyCb
JtjciKaZILgARTRXwfQTXIVGM03xZwLZtHN2zpiZ2fu+v6/miY+b7EYyRcwMJMo4us0+lWgB
WcQJEPRaA9qNDqxuS12Ks8bboTb3tSQpRC+e34ckKf9k/0z2U5WS04y3f1Gr1jEZi8Oca3rk
B3WXTGfs7Y8kAAAAAElFTkSuQmCC
--c67476988f320ca04d61815bcfd14360--
EOT;
        $mail = $this->mailer->prepare_contact_notification($this->user);
        $mail->set_boundary('c67476988f320ca04d61815bcfd14360');
        $this->assertEquals($message, $mail->__toString());
    }
}
