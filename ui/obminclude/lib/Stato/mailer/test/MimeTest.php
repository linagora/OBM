<?php

require_once dirname(__FILE__) . '/../../test/TestsHelper.php';

require_once 'mime/mime.php';

class SMimeTest extends PHPUnit_Framework_TestCase
{
    public function test_is_printable()
    {
        $this->assertTrue(SMime::is_printable('simple text'));
    }
    
    public function test_is_not_printable()
    {
        $this->assertFalse(SMime::is_printable('not so simple text éà&ç'));
    }
    
    public function test_base64_encode()
    {
        $str = 'not so simple text éà&ç';
        $this->assertEquals($str, base64_decode(SMime::encode_base64($str)));
    }
    
    public function test_base64_encode_image_resource()
    {
        $encoded = <<<EOT
iVBORw0KGgoAAAANSUhEUgAAAAYAAAAFCAYAAABmWJ3mAAAAAXNSR0IArs4c6QAAAAZiS0dE
AP8A/wD/oL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAIBJREFUCNc9yK8OQWEcx+HPiyCb
JtjciKaZILgARTRXwfQTXIVGM03xZwLZtHN2zpiZ2fu+v6/miY+b7EYyRcwMJMo4us0+lWgB
WcQJEPRaA9qNDqxuS12Ks8bboTb3tSQpRC+e34ckKf9k/0z2U5WS04y3f1Gr1jEZi8Oca3rk
B3WXTGfs7Y8kAAAAAElFTkSuQmCC
EOT;
        $this->assertEquals($encoded, SMime::encode(fopen(dirname(__FILE__).'/files/image.png', 'r'), SMime::BASE64));
    }
    
    public function test_base64_encode_image_content()
    {
        $encoded = <<<EOT
iVBORw0KGgoAAAANSUhEUgAAAAYAAAAFCAYAAABmWJ3mAAAAAXNSR0IArs4c6QAAAAZiS0dE
AP8A/wD/oL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAIBJREFUCNc9yK8OQWEcx+HPiyCb
JtjciKaZILgARTRXwfQTXIVGM03xZwLZtHN2zpiZ2fu+v6/miY+b7EYyRcwMJMo4us0+lWgB
WcQJEPRaA9qNDqxuS12Ks8bboTb3tSQpRC+e34ckKf9k/0z2U5WS04y3f1Gr1jEZi8Oca3rk
B3WXTGfs7Y8kAAAAAElFTkSuQmCC
EOT;
        $this->assertEquals($encoded, SMime::encode(file_get_contents(dirname(__FILE__).'/files/image.png'), SMime::BASE64));
    }
    
    public function test_encode_quoted_printable()
    {
        $html = file_get_contents(dirname(__FILE__).'/files/dummy.html');
        $this->assertEquals($html, quoted_printable_decode(SMime::encode_quoted_printable($html)));
    }
    
    public function test_encode_quoted_printable_stream()
    {
        $html = file_get_contents(dirname(__FILE__).'/files/dummy.html');
        $this->assertEquals($html, quoted_printable_decode(
            SMime::encode_stream(fopen(dirname(__FILE__).'/files/dummy.html', 'r'), SMime::QUOTED_PRINTABLE)));
    }
    
    public function test8bit_encoding()
    {
        $this->assertEquals('test', SMime::encode('test', '8bit'));
    }
    
    public function test8bit_stream_encoding()
    {
        $html = file_get_contents(dirname(__FILE__).'/files/dummy.html');
        $this->assertEquals($html, quoted_printable_decode(
            SMime::encode(fopen(dirname(__FILE__).'/files/dummy.html', 'r'), '8bit')));
    }
    
    public function test_not_supported_encoding_should_throw()
    {
        $this->setExpectedException('SMimeException');
        SMime::encode('test', 'dummy');
    }
    
    public function test_not_supported_encoding_should_throw_with_streams_too()
    {
        $this->setExpectedException('SMimeException');
        SMime::encode_stream('test', 'dummy');
    }
}