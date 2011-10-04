<?php

require_once dirname(__FILE__) . '/../../test/TestsHelper.php';

require_once 'mime/mime.php';
require_once 'mime/entity.php';
require_once 'mime/part.php';
require_once 'mime/attachment.php';
require_once 'mime/multipart.php';

class SMimeMultipartTest extends PHPUnit_Framework_TestCase
{
    public function test_multipart_alternative()
    {
        $str = <<<EOT
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
        $mp = new SMimeMultipart();
        $mp->set_boundary('c67476988f320ca04d61815bcfd14360');
        $mp->add_part(new SMimePart('test'));
        $mp->add_part(new SMimePart('<b>test</b>', 'text/html'));
        $this->assertEquals($str, (string) $mp);
    }
    
    public function test_multipart_tree()
    {
        $str = <<<EOT
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
        $mp1 = new SMimeMultipart();
        $mp1->set_boundary('c67476988f320ca04d61815bcfd14360');
        $mp1->add_part(new SMimePart('test'));
        $mp1->add_part(new SMimePart('<b>test</b>', 'text/html'));
        $mp2 = new SMimeMultipart(SMimeMultipart::MIXED);
        $mp2->set_boundary('c67476988f320ca04d61815bcfd14361');
        $mp2->add_part($mp1);
        $mp2->add_part(new SMimeAttachment(fopen(dirname(__FILE__).'/files/image.png', 'r'), 'hello.png', 'image/png'));
        $this->assertEquals($str, (string) $mp2);
    }
}