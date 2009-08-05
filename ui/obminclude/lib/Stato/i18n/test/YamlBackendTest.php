<?php

require_once dirname(__FILE__) . '/../../test/TestsHelper.php';

require_once 'i18n.php';
require_once 'backend/abstract.php';
require_once 'backend/yaml.php';

class SYamlBackendTest extends SSimpleBackendTest
{
    public function setup()
    {
        if (!extension_loaded('syck'))
            $this->mark_test_skipped('The Syck extension is not available');
             
        $this->backend = new SYamlBackend(dirname(__FILE__).'/data/yaml');
    }
    
    public function teardown()
    {
        @unlink(dirname(__FILE__) . '/tmp/klingon.yml');
        @unlink(dirname(__FILE__) . '/tmp/fr.yml');
    }
    
    public function test_save()
    {
        $this->backend->store('klingon', 'The Klingon culture is a very ancient one, though there is no record of its roots.', 
                                         'tIQqu\' tlhIngan Segh tIgh je, \'ach mungDaj qonlu\'be\'.');
        $this->backend->save('klingon', dirname(__FILE__) . '/tmp');
        
        $backend = new SYamlBackend(dirname(__FILE__) . '/tmp');
        $this->assertEquals('tIQqu\' tlhIngan Segh tIgh je, \'ach mungDaj qonlu\'be\'.',
            $backend->translate('klingon', 'The Klingon culture is a very ancient one, though there is no record of its roots.'));
    }
    
    public function test_save_with_existent_translations()
    {
        
    }
}