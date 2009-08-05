<?php

require_once dirname(__FILE__) . '/../../test/TestsHelper.php';

require_once 'i18n.php';
require_once 'backend/abstract.php';
require_once 'backend/xliff.php';

class SXliffBackendTest extends SSimpleBackendTest
{
    public function setup()
    {
        $this->backend = new SXliffBackend(dirname(__FILE__).'/data/xliff');
    }
    
    public function teardown()
    {
        @unlink(dirname(__FILE__) . '/tmp/klingon.xml');
        @unlink(dirname(__FILE__) . '/tmp/fr.xml');
    }
    
    public function test_save()
    {
        $this->backend->store('klingon', 'The Klingon culture is a very ancient one, though there is no record of its roots.', 
                                         'tIQqu\' tlhIngan Segh tIgh je, \'ach mungDaj qonlu\'be\'.');
        $this->backend->save('klingon', dirname(__FILE__).'/tmp');
        
        $backend = new SXliffBackend(dirname(__FILE__).'/tmp');
        $this->assertEquals('tIQqu\' tlhIngan Segh tIgh je, \'ach mungDaj qonlu\'be\'.',
            $backend->translate('klingon', 'The Klingon culture is a very ancient one, though there is no record of its roots.'));
    }
    
    public function test_save_with_existent_translations()
    {
        $this->backend->store('fr', 'hello world', 'bonjour le monde', 'foo_controller.php:10');
        $this->backend->save('fr', dirname(__FILE__).'/tmp');
        $backend = new SXliffBackend(dirname(__FILE__).'/tmp');
        $this->assertEquals('bonjour le monde', $backend->translate('fr', 'hello world'));
        $this->assertEquals('Stato est un cadre de travail PHP5.', $backend->translate('fr', 'Stato is a PHP5 framework.'));
        $this->assertEquals('2 messages', $backend->translate_and_pluralize('fr', 'inbox', 2));
    }
}