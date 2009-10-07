<?php

require_once dirname(__FILE__) . '/../../tests/TestsHelper.php';

require_once 'mime/mime.php';
require_once 'mime/entity.php';
require_once 'mime/part.php';
require_once 'mime/multipart.php';
require_once 'mail.php';

require_once dirname(__FILE__).'/files/dummy_transport.php';

class Stato_MailTest extends PHPUnit_Framework_TestCase
{
    public function setup()
    {
        $this->date = new DateTime('2009-02-13 15:47:25', new DateTimeZone('Europe/Paris'));
    }
    
    public function testSimpleMessage()
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
        $mail = new Stato_Mail($this->date);
        $mail->setFrom('foo.bar@dummy.com', 'Foo Bar');
        $mail->addTo('john.doe@fake.net', 'John Doe');
        $mail->setSubject('Stop these useless meetings...');
        $mail->setText('test');
        $this->assertEquals($message, (string) $mail);
    }
    
    public function testMessageWithoutAContentShouldThrow()
    {
        $mail = new Stato_Mail($this->date);
        $mail->addTo('john.doe@fake.net', 'John Doe');
        $this->setExpectedException('Stato_MailException', 'No body specified');
        $mail->getContent();
    }
    
    public function testRecipients()
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
        $mail = new Stato_Mail($this->date);
        $mail->setFrom('foo.bar@dummy.com', 'Foo Bar');
        $mail->addTo('john.doe@fake.net', 'John Doe');
        $mail->addCc('jane.doe@fake.net');
        $mail->addCc('not.real@ofcourse.net', 'Raphaël Rougeron'); // is the above encoded name correct ? not sure...
        $mail->addBcc('bureaucratic.director@bigbrother.com');
        $mail->setSubject('Stop these useless meetings...');
        $mail->setText('test');
        $this->assertEquals($message, (string) $mail);
    }
    
    public function testGetHeaderValue()
    {
        $mail = new Stato_Mail($this->date);
        $mail->setFrom('foo.bar@dummy.com', 'Foo Bar');
        $mail->addTo('john.doe@fake.net', 'John Doe');
        $mail->addCc('jane.doe@fake.net');
        $mail->addCc('not.real@ofcourse.net', 'Raphael Rougeron');
        $mail->setSubject('Stop these useless meetings...');
        $this->assertEquals('John Doe <john.doe@fake.net>', $mail->getTo());
        $this->assertEquals('Foo Bar <foo.bar@dummy.com>', $mail->getFrom());
        $this->assertEquals('jane.doe@fake.net, Raphael Rougeron <not.real@ofcourse.net>', $mail->getCc());
        $this->assertEquals('', $mail->getBcc());
        $this->assertEquals('Stop these useless meetings...', $mail->getSubject());
    }
    
    public function testMessageWithoutAToShouldThrow()
    {
        $mail = new Stato_Mail($this->date);
        $this->setExpectedException('Stato_MailException', 'To: recipient is not specified');
        $mail->getTo();
    }
    
    public function testHtmlMessage()
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
        $mail = new Stato_Mail($this->date);
        $mail->setFrom('foo.bar@dummy.com', 'Foo Bar');
        $mail->addTo('john.doe@fake.net', 'John Doe');
        $mail->setHtmlText('<b>test</b>');
        $this->assertEquals($message, (string) $mail);
    }
    
    public function testAutomaticMultipartMessage()
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
        $mail = new Stato_Mail($this->date);
        $mail->addTo('john.doe@fake.net', 'John Doe');
        $mail->setText('test');
        $mail->setHtmlText('<b>test</b>');
        $mail->setBoundary('c67476988f320ca04d61815bcfd14360');
        $this->assertEquals($message, (string) $mail);
    }
    
    public function testAddPart()
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
        $mail = new Stato_Mail($this->date);
        $mail->addTo('john.doe@fake.net', 'John Doe');
        $mail->setText('test');
        $mail->addPart(fopen(dirname(__FILE__).'/files/dummy.ics', 'r'), 'text/calendar', 'quoted-printable');
        $mail->setBoundary('c67476988f320ca04d61815bcfd14360');
        $this->assertEquals($message, (string) $mail);
    }
    
    public function testAddAttachment()
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
        $mail = new Stato_Mail($this->date);
        $mail->addTo('john.doe@fake.net', 'John Doe');
        $mail->setText('test');
        $mail->addAttachment(file_get_contents(dirname(__FILE__).'/files/image.png'), 'hello.png', 'image/png');
        $mail->setBoundary('c67476988f320ca04d61815bcfd14360');
        $this->assertEquals($message, (string) $mail);
    }
    
    public function testAddAttachmentToMultipartMessage()
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
        $mp = new Stato_MimeMultipart();
        $mp->setBoundary('c67476988f320ca04d61815bcfd14360');
        $mp->addPart(new Stato_MimePart('test'));
        $mp->addPart(new Stato_MimePart('<b>test</b>', 'text/html'));
        $mail = new Stato_Mail($this->date);
        $mail->addTo('john.doe@fake.net', 'John Doe');
        $mail->setContent($mp);
        $mail->addAttachment(file_get_contents(dirname(__FILE__).'/files/image.png'), 'hello.png', 'image/png');
        $mail->setBoundary('c67476988f320ca04d61815bcfd14361');
        $this->assertEquals($message, (string) $mail);
    }
    
    public function testAddEmbeddedImage()
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
        $mail = new Stato_Mail($this->date);
        $mail->addTo('john.doe@fake.net', 'John Doe');
        $mail->setText('test');
        $mail->addEmbeddedImage(file_get_contents(dirname(__FILE__).'/files/image.png'), 'hello', 'hello.png', 'image/png');
        $mail->setBoundary('c67476988f320ca04d61815bcfd14360');
        $this->assertEquals($message, (string) $mail);
    }
    
    public function testSend()
    {
        $message = <<<EOT
Date: Fri, 13 Feb 09 15:47:25 +0100
MIME-Version: 1.0
To: John Doe <john.doe@fake.net>
Content-Type: text/plain; charset="UTF-8"
Content-Transfer-Encoding: 8bit

test
EOT;
        $mail = new Stato_Mail($this->date);
        $mail->addTo('john.doe@fake.net', 'John Doe');
        $mail->setText('test');
        $this->assertEquals($message, $mail->send(new Stato_DummyTransport()));
    }
}
