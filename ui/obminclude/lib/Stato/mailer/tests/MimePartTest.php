<?php

require_once dirname(__FILE__) . '/../../tests/TestsHelper.php';

require_once 'mime/mime.php';
require_once 'mime/entity.php';
require_once 'mime/part.php';

class Stato_MimePartTest extends PHPUnit_Framework_TestCase
{
    public function testTextPlainPart()
    {
        $part = <<<EOT
Content-Type: text/plain; charset="UTF-8"
Content-Transfer-Encoding: 8bit

test
EOT;
        $this->assertEquals($part, (string) new Stato_MimePart('test'));
    }
    
    public function testSetContentType()
    {
        
    }
}