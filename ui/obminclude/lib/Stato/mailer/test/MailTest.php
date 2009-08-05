<?php

require_once dirname(__FILE__) . '/../../test/TestsHelper.php';

require_once 'mime/mime.php';
require_once 'mime/entity.php';
require_once 'mime/part.php';
require_once 'mime/multipart.php';
require_once 'mail.php';

require_once dirname(__FILE__).'/files/dummy_transport.php';

class SMailTest extends PHPUnit_Framework_TestCase
{
    public function setup()
    {
        $this->date = new DateTime('2009-02-13 15:47:25', new DateTimeZone('Europe/Paris'));
    }
    
    public function test_simple_message()
    {
        $message = <<<EOT
Date: Fri, 13 Feb 09 15:47:25 +0100
MIME-Version: 1.0
From: Foo Bar <foo.bar@dummy.com>
To: John Doe <john.doe@fake.net>
Subject: Stop these useless meetings...
Content-Type: text/plain; charset="UTF-8"
Content-Transfer-Encoding: 8bit

test
EOT;
        $mail = new SMail($this->date);
        $mail->set_from('foo.bar@dummy.com', 'Foo Bar');
        $mail->add_to('john.doe@fake.net', 'John Doe');
        $mail->set_subject('Stop these useless meetings...');
        $mail->set_text('test');
        $this->assertEquals($message, (string) $mail);
    }
    
    public function test_message_without_a_content_should_throw()
    {
        $mail = new SMail($this->date);
        $mail->add_to('john.doe@fake.net', 'John Doe');
        $this->setExpectedException('SMailException', 'No body specified');
        $mail->get_content();
    }
    
    public function test_recipients()
    {
        $message = <<<EOT
Date: Fri, 13 Feb 09 15:47:25 +0100
MIME-Version: 1.0
From: Foo Bar <foo.bar@dummy.com>
To: John Doe <john.doe@fake.net>
Cc: jane.doe@fake.net, =?UTF-8?Q?Rapha=C3=ABl=20Rougeron?= <not.real@ofcourse.net>
Bcc: bureaucratic.director@bigbrother.com
Subject: Stop these useless meetings...
Content-Type: text/plain; charset="UTF-8"
Content-Transfer-Encoding: 8bit

test
EOT;
        $mail = new SMail($this->date);
        $mail->set_from('foo.bar@dummy.com', 'Foo Bar');
        $mail->add_to('john.doe@fake.net', 'John Doe');
        $mail->add_cc('jane.doe@fake.net');
        $mail->add_cc('not.real@ofcourse.net', 'Raphaël Rougeron'); // is the above encoded name correct ? not sure...
        $mail->add_bcc('bureaucratic.director@bigbrother.com');
        $mail->set_subject('Stop these useless meetings...');
        $mail->set_text('test');
        $this->assertEquals($message, (string) $mail);
    }
    
    public function test_get_header_value()
    {
        $mail = new SMail($this->date);
        $mail->set_from('foo.bar@dummy.com', 'Foo Bar');
        $mail->add_to('john.doe@fake.net', 'John Doe');
        $mail->add_cc('jane.doe@fake.net');
        $mail->add_cc('not.real@ofcourse.net', 'Raphael Rougeron');
        $mail->set_subject('Stop these useless meetings...');
        $this->assertEquals('John Doe <john.doe@fake.net>', $mail->get_to());
        $this->assertEquals('Foo Bar <foo.bar@dummy.com>', $mail->get_from());
        $this->assertEquals('jane.doe@fake.net, Raphael Rougeron <not.real@ofcourse.net>', $mail->get_cc());
        $this->assertEquals('', $mail->get_bcc());
        $this->assertEquals('Stop these useless meetings...', $mail->get_subject());
    }
    
    public function test_message_without_a_to_should_throw()
    {
        $mail = new SMail($this->date);
        $this->setExpectedException('SMailException', 'To: recipient is not specified');
        $mail->get_to();
    }
    
    public function test_html_message()
    {
        $message = <<<EOT
Date: Fri, 13 Feb 09 15:47:25 +0100
MIME-Version: 1.0
From: Foo Bar <foo.bar@dummy.com>
To: John Doe <john.doe@fake.net>
Content-Type: text/html; charset="UTF-8"
Content-Transfer-Encoding: 8bit

<b>test</b>
EOT;
        $mail = new SMail($this->date);
        $mail->set_from('foo.bar@dummy.com', 'Foo Bar');
        $mail->add_to('john.doe@fake.net', 'John Doe');
        $mail->set_html_text('<b>test</b>');
        $this->assertEquals($message, (string) $mail);
    }
    
    public function test_automatic_multipart_message()
    {
        $message = <<<EOT
Date: Fri, 13 Feb 09 15:47:25 +0100
MIME-Version: 1.0
To: John Doe <john.doe@fake.net>
Content-Type: multipart/alternative; boundary="c67476988f320ca04d61815bcfd14360"

This is a multi-part message in MIME format.
--c67476988f320ca04d61815bcfd14360
Content-Type: text/plain; charset="UTF-8"
Content-Transfer-Encoding: 8bit

test
--c67476988f320ca04d61815bcfd14360
Content-Type: text/html; charset="UTF-8"
Content-Transfer-Encoding: 8bit

<b>test</b>
--c67476988f320ca04d61815bcfd14360--
EOT;
        $mail = new SMail($this->date);
        $mail->add_to('john.doe@fake.net', 'John Doe');
        $mail->set_text('test');
        $mail->set_html_text('<b>test</b>');
        $mail->set_boundary('c67476988f320ca04d61815bcfd14360');
        $this->assertEquals($message, (string) $mail);
    }
    
    public function test_add_part()
    {
        $message = <<<EOT
Date: Fri, 13 Feb 09 15:47:25 +0100
MIME-Version: 1.0
To: John Doe <john.doe@fake.net>
Content-Type: multipart/alternative; boundary="c67476988f320ca04d61815bcfd14360"

This is a multi-part message in MIME format.
--c67476988f320ca04d61815bcfd14360
Content-Type: text/plain; charset="UTF-8"
Content-Transfer-Encoding: 8bit

test
--c67476988f320ca04d61815bcfd14360
Content-Type: text/calendar; charset="UTF-8"
Content-Transfer-Encoding: quoted-printable

BEGIN:VCALENDAR
VERSION:2.0
CALSCALE:GREGORIAN
METHOD:REQUEST
BEGIN:VEVENT
DTSTART:20090218T140000Z
DTEND:20090218T153000Z
DTSTAMP:20090217T160043Z
ORGANIZER:mailto:john@doe.net
SEQUENCE:0
STATUS:CONFIRMED
SUMMARY:test
END:VEVENT
END:VCALENDAR
--c67476988f320ca04d61815bcfd14360--
EOT;
        $mail = new SMail($this->date);
        $mail->add_to('john.doe@fake.net', 'John Doe');
        $mail->set_text('test');
        $mail->add_part(fopen(dirname(__FILE__).'/files/dummy.ics', 'r'), 'text/calendar', 'quoted-printable');
        $mail->set_boundary('c67476988f320ca04d61815bcfd14360');
        $this->assertEquals($message, (string) $mail);
    }
    
    public function test_add_attachment()
    {
        $message = <<<EOT
Date: Fri, 13 Feb 09 15:47:25 +0100
MIME-Version: 1.0
To: John Doe <john.doe@fake.net>
Content-Type: multipart/mixed; boundary="c67476988f320ca04d61815bcfd14360"

This is a multi-part message in MIME format.
--c67476988f320ca04d61815bcfd14360
Content-Type: text/plain; charset="UTF-8"
Content-Transfer-Encoding: 8bit

test
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
        $mail = new SMail($this->date);
        $mail->add_to('john.doe@fake.net', 'John Doe');
        $mail->set_text('test');
        $mail->add_attachment(file_get_contents(dirname(__FILE__).'/files/image.png'), 'hello.png', 'image/png');
        $mail->set_boundary('c67476988f320ca04d61815bcfd14360');
        $this->assertEquals($message, (string) $mail);
    }
    
    public function test_add_attachment_to_multipart_message()
    {
        $message = <<<EOT
Date: Fri, 13 Feb 09 15:47:25 +0100
MIME-Version: 1.0
To: John Doe <john.doe@fake.net>
Content-Type: multipart/mixed; boundary="c67476988f320ca04d61815bcfd14361"

This is a multi-part message in MIME format.
--c67476988f320ca04d61815bcfd14361
Content-Type: multipart/alternative; boundary="c67476988f320ca04d61815bcfd14360"

This is a multi-part message in MIME format.
--c67476988f320ca04d61815bcfd14360
Content-Type: text/plain; charset="UTF-8"
Content-Transfer-Encoding: 8bit

test
--c67476988f320ca04d61815bcfd14360
Content-Type: text/html; charset="UTF-8"
Content-Transfer-Encoding: 8bit

<b>test</b>
--c67476988f320ca04d61815bcfd14360--
--c67476988f320ca04d61815bcfd14361
Content-Type: image/png; name="hello.png"
Content-Transfer-Encoding: base64
Content-Disposition: attachment; filename="hello.png"

iVBORw0KGgoAAAANSUhEUgAAAAYAAAAFCAYAAABmWJ3mAAAAAXNSR0IArs4c6QAAAAZiS0dE
AP8A/wD/oL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAIBJREFUCNc9yK8OQWEcx+HPiyCb
JtjciKaZILgARTRXwfQTXIVGM03xZwLZtHN2zpiZ2fu+v6/miY+b7EYyRcwMJMo4us0+lWgB
WcQJEPRaA9qNDqxuS12Ks8bboTb3tSQpRC+e34ckKf9k/0z2U5WS04y3f1Gr1jEZi8Oca3rk
B3WXTGfs7Y8kAAAAAElFTkSuQmCC
--c67476988f320ca04d61815bcfd14361--
EOT;
        $mp = new SMimeMultipart();
        $mp->set_boundary('c67476988f320ca04d61815bcfd14360');
        $mp->add_part(new SMimePart('test'));
        $mp->add_part(new SMimePart('<b>test</b>', 'text/html'));
        $mail = new SMail($this->date);
        $mail->add_to('john.doe@fake.net', 'John Doe');
        $mail->set_content($mp);
        $mail->add_attachment(file_get_contents(dirname(__FILE__).'/files/image.png'), 'hello.png', 'image/png');
        $mail->set_boundary('c67476988f320ca04d61815bcfd14361');
        $this->assertEquals($message, (string) $mail);
    }
    
    public function test_add_embedded_image()
    {
        $message = <<<EOT
Date: Fri, 13 Feb 09 15:47:25 +0100
MIME-Version: 1.0
To: John Doe <john.doe@fake.net>
Content-Type: multipart/related; boundary="c67476988f320ca04d61815bcfd14360"

This is a multi-part message in MIME format.
--c67476988f320ca04d61815bcfd14360
Content-Type: text/plain; charset="UTF-8"
Content-Transfer-Encoding: 8bit

test
--c67476988f320ca04d61815bcfd14360
Content-Type: image/png; name="hello.png"
Content-Transfer-Encoding: base64
Content-ID: <hello>

iVBORw0KGgoAAAANSUhEUgAAAAYAAAAFCAYAAABmWJ3mAAAAAXNSR0IArs4c6QAAAAZiS0dE
AP8A/wD/oL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAIBJREFUCNc9yK8OQWEcx+HPiyCb
JtjciKaZILgARTRXwfQTXIVGM03xZwLZtHN2zpiZ2fu+v6/miY+b7EYyRcwMJMo4us0+lWgB
WcQJEPRaA9qNDqxuS12Ks8bboTb3tSQpRC+e34ckKf9k/0z2U5WS04y3f1Gr1jEZi8Oca3rk
B3WXTGfs7Y8kAAAAAElFTkSuQmCC
--c67476988f320ca04d61815bcfd14360--
EOT;
        $mail = new SMail($this->date);
        $mail->add_to('john.doe@fake.net', 'John Doe');
        $mail->set_text('test');
        $mail->add_embedded_image(file_get_contents(dirname(__FILE__).'/files/image.png'), 'hello', 'hello.png', 'image/png');
        $mail->set_boundary('c67476988f320ca04d61815bcfd14360');
        $this->assertEquals($message, (string) $mail);
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
        $mail = new SMail($this->date);
        $mail->add_to('john.doe@fake.net', 'John Doe');
        $mail->set_text('test');
        $this->assertEquals($message, $mail->send(new SDummyTransport()));
    }
}
