<?php

require_once dirname(__FILE__) . '/../../tests/TestsHelper.php';

require_once 'mime/mime.php';

class Stato_MimeTest extends PHPUnit_Framework_TestCase
{
    public function testIsPrintable()
    {
        $this->assertTrue(Stato_Mime::isPrintable('simple text'));
    }
    
    public function testIsNotPrintable()
    {
        $this->assertFalse(Stato_Mime::isPrintable('not so simple text éà&ç'));
    }
    
    public function testBase64Encode()
    {
        $str = 'not so simple text éà&ç';
        $this->assertEquals($str, base64_decode(Stato_Mime::encodeBase64($str)));
    }
    
    public function testBase64EncodeImageResource()
    {
        $encoded = <<<EOT
iVBORw0KGgoAAAANSUhEUgAAAAYAAAAFCAYAAABmWJ3mAAAAAXNSR0IArs4c6QAAAAZiS0dE
AP8A/wD/oL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAIBJREFUCNc9yK8OQWEcx+HPiyCb
JtjciKaZILgARTRXwfQTXIVGM03xZwLZtHN2zpiZ2fu+v6/miY+b7EYyRcwMJMo4us0+lWgB
WcQJEPRaA9qNDqxuS12Ks8bboTb3tSQpRC+e34ckKf9k/0z2U5WS04y3f1Gr1jEZi8Oca3rk
B3WXTGfs7Y8kAAAAAElFTkSuQmCC
EOT;
        $this->assertEquals($encoded, Stato_Mime::encode(fopen(dirname(__FILE__).'/files/image.png', 'r'), Stato_Mime::BASE64));
    }
    
    public function testBase64EncodeImageContent()
    {
        $encoded = <<<EOT
iVBORw0KGgoAAAANSUhEUgAAAAYAAAAFCAYAAABmWJ3mAAAAAXNSR0IArs4c6QAAAAZiS0dE
AP8A/wD/oL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAIBJREFUCNc9yK8OQWEcx+HPiyCb
JtjciKaZILgARTRXwfQTXIVGM03xZwLZtHN2zpiZ2fu+v6/miY+b7EYyRcwMJMo4us0+lWgB
WcQJEPRaA9qNDqxuS12Ks8bboTb3tSQpRC+e34ckKf9k/0z2U5WS04y3f1Gr1jEZi8Oca3rk
B3WXTGfs7Y8kAAAAAElFTkSuQmCC
EOT;
        $this->assertEquals($encoded, Stato_Mime::encode(file_get_contents(dirname(__FILE__).'/files/image.png'), Stato_Mime::BASE64));
    }
    
    public function testEncodeQuotedPrintable()
    {
        $html = file_get_contents(dirname(__FILE__).'/files/dummy.html');
        $this->assertEquals($html, quoted_printable_decode(Stato_Mime::encodeQuotedPrintable($html)));
    }
    
    public function testEncodeQuotedPrintableStream()
    {
        $html = file_get_contents(dirname(__FILE__).'/files/dummy.html');
        $this->assertEquals($html, quoted_printable_decode(
            Stato_Mime::encodeStream(fopen(dirname(__FILE__).'/files/dummy.html', 'r'), Stato_Mime::QUOTED_PRINTABLE)));
    }
    
    public function test8bitEncoding()
    {
        $this->assertEquals('test', Stato_Mime::encode('test', '8bit'));
    }
    
    public function test8bitStreamEncoding()
    {
        $html = file_get_contents(dirname(__FILE__).'/files/dummy.html');
        $this->assertEquals($html, quoted_printable_decode(
            Stato_Mime::encode(fopen(dirname(__FILE__).'/files/dummy.html', 'r'), '8bit')));
    }
    
    public function testNotSupportedEncodingShouldThrow()
    {
        $this->setExpectedException('Stato_MimeException');
        Stato_Mime::encode('test', 'dummy');
    }
    
    public function testNotSupportedEncodingShouldThrowWithStreamsToo()
    {
        $this->setExpectedException('Stato_MimeException');
        Stato_Mime::encodeStream('test', 'dummy');
    }
}