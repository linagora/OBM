<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2009 OBM.org project members team                   |
 |                                                                         |
 | This program is free software; you can redistribute it and/or           |
 | modify it under the terms of the GNU General Public License             |
 | as published by the Free Software Foundation; version 2                 |
 | of the License.                                                         |
 |                                                                         |
 | This program is distributed in the hope that it will be useful,         |
 | but WITHOUT ANY WARRANTY; without even the implied warranty of          |
 | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
 | GNU General Public License for more details.                            |
 +-------------------------------------------------------------------------+
 | http://www.obm.org                                                      |
 +-------------------------------------------------------------------------+
*/
?>
<?php

require_once dirname(__FILE__).'/TestsHelper.php';

require_once 'vpdi/vpdi.php';
require_once 'vpdi/field.php';
require_once 'vpdi/entity.php';
require_once 'vpdi/vcard.php';

date_default_timezone_set('Europe/Paris');
 
class Vpdi_TestCase extends PHPUnit_Framework_TestCase {
  public function testRFC2425ParserBasicMethods() {
    $this->assertEquals(Vpdi::decodeTextList('aaa,bbb,ccc'), array('aaa', 'bbb', 'ccc'));
    $this->assertEquals(Vpdi::decodeTextList('a\,aa,bbb,ccc'), array('a,aa', 'bbb', 'ccc'));
    $this->assertEquals(Vpdi::decodeTextList('a\,aa,bb\,b,ccc'), array('a,aa', 'bb,b', 'ccc'));
    $this->assertEquals(Vpdi::decodeTextList('a\,aa,bb\,b,\,ccc'), array('a,aa', 'bb,b', ',ccc'));
    
    $this->assertEquals(Vpdi::encodeTextList(array('aaa', 'bbb', 'ccc')), 'aaa,bbb,ccc');
    $this->assertEquals(Vpdi::encodeTextList(array('a,aa', 'bbb', 'ccc')), 'a\,aa,bbb,ccc');
    $this->assertEquals(Vpdi::encodeTextList(array('a,aa', 'bb,b', 'ccc')), 'a\,aa,bb\,b,ccc');
    $this->assertEquals(Vpdi::encodeTextList(array('a,aa', 'bb,b', ',ccc')), 'a\,aa,bb\,b,\,ccc');
    
    $this->assertEquals('1996-04-15', Vpdi::decodeDate('1996-04-15')->format('Y-m-d'));
    $this->assertEquals('1953-10-15 23:10:00', Vpdi::decodeDate('1953-10-15T23:10:00Z')->format('Y-m-d H:i:s'));
    $this->assertEquals('1987-09-27 08:30:00', Vpdi::decodeDate('1987-09-27T08:30:00-06:00')->format('Y-m-d H:i:s'));
  }
  
  public function testRFC2425LineParser() {
    $this->assertEquals(Vpdi::decodeLine('BEGIN:VCARD'), 
      array('group' => null, 'name' => 'BEGIN', 'value' => 'VCARD', 'params' => array()));
    $this->assertEquals(Vpdi::decodeLine('FN:John Doe'), 
      array('group' => null, 'name' => 'FN', 'value' => 'John Doe', 'params' => array()));
    $this->assertEquals(Vpdi::decodeLine('ORG:Example.com Inc.;'), 
      array('group' => null, 'name' => 'ORG', 'value' => 'Example.com Inc.;', 'params' => array()));
    $this->assertEquals(Vpdi::decodeLine('N:Doe;John;;;'), 
      array('group' => null, 'name' => 'N', 'value' => 'Doe;John;;;', 'params' => array()));
    $this->assertEquals(Vpdi::decodeLine('TEL;type=CELL:+1 781 555 1212'), 
      array('group' => null, 'name' => 'TEL', 'value' => '+1 781 555 1212', 'params' => array('type' => 'CELL')));
    $this->assertEquals(Vpdi::decodeLine('TEL;type=work,voice,msg:+1 313 747-4454'), 
      array('group' => null, 'name' => 'TEL', 'value' => '+1 313 747-4454', 'params' => array('type' => array('work', 'voice', 'msg'))));
    $this->assertEquals(Vpdi::decodeLine('TEL;type=WORK;type=pref:+1 617 555 1212'), 
      array('group' => null, 'name' => 'TEL', 'value' => '+1 617 555 1212', 'params' => array('type' => array('WORK', 'pref'))));
    $this->assertEquals(Vpdi::decodeLine('item1.ADR;type=WORK:;;2 Example Avenue;Anytown;NY;01111;USA'), 
      array('group' => 'item1', 'name' => 'ADR', 'value' => ';;2 Example Avenue;Anytown;NY;01111;USA', 'params' => array('type' => 'WORK')));
  }
  
  public function testRFC2425Sample1() {
    $sample = <<<EOF
cn:  
cn:Babs Jensen
cn:Barbara J Jensen
sn:Jensen
email:babs@umich.edu
phone:+1 313 747-4454
x-id:1234567890
EOF;
    $fields = Vpdi::decodeFields($sample);
    
    $this->assertEquals('', $fields[0]->value());
    $this->assertEquals('cn', $fields[0]->name());
    $this->assertEquals('Babs Jensen', $fields[1]->value());
    $this->assertEquals('cn', $fields[1]->name());
    $this->assertEquals('Barbara J Jensen', $fields[2]->value());
    $this->assertEquals('cn', $fields[2]->name());
    $this->assertEquals('Jensen', $fields[3]->value());
    $this->assertEquals('sn', $fields[3]->name());
    $this->assertEquals('babs@umich.edu', $fields[4]->value());
    $this->assertEquals('email', $fields[4]->name());
    $this->assertEquals('+1 313 747-4454', $fields[5]->value());
    $this->assertEquals('phone', $fields[5]->name());
    $this->assertEquals('1234567890', $fields[6]->value());
    $this->assertEquals('x-id', $fields[6]->name());
  }
  
  public function testVpdiExpand() {
$sample = <<<EOF
BEGIN:a
k1:v1
BEGIN:b
BEGIN:c
k2:v2
k3:v3
END:c
k4:v4
k5:v5
END:b
k6:v6
END:a
BEGIN:d
k7:v7
END:d
BEGIN:e
k8:v8
BEGIN:f
k9:v9
END:f
END:e
EOF;
    $tree = Vpdi::decode($sample);
    
    $this->assertEquals('k1', $tree[0][0]->name());
    $this->assertEquals('v1', $tree[0][0]->value());
    $this->assertEquals('k2', $tree[0][1][0][0]->name());
    $this->assertEquals('v2', $tree[0][1][0][0]->value());
    $this->assertEquals('k3', $tree[0][1][0][1]->name());
    $this->assertEquals('v3', $tree[0][1][0][1]->value());
    $this->assertEquals('k4', $tree[0][1][1]->name());
    $this->assertEquals('v4', $tree[0][1][1]->value());
    $this->assertEquals('k5', $tree[0][1][2]->name());
    $this->assertEquals('v5', $tree[0][1][2]->value());
    $this->assertEquals('k6', $tree[0][2]->name());
    $this->assertEquals('v6', $tree[0][2]->value());
    $this->assertEquals('k7', $tree[1][0]->name());
    $this->assertEquals('v7', $tree[1][0]->value());
    $this->assertEquals('k8', $tree[2][0]->name());
    $this->assertEquals('v8', $tree[2][0]->value());
    $this->assertEquals('k9', $tree[2][1][0]->name());
    $this->assertEquals('v9', $tree[2][1][0]->value());
    
    $this->assertEquals('Vpdi_Entity', get_class($tree[0]));
    $this->assertEquals('Vpdi_Entity', get_class($tree[0][1]));
    $this->assertEquals('Vpdi_Entity', get_class($tree[0][1][0]));
    $this->assertEquals('Vpdi_Entity', get_class($tree[1]));
    $this->assertEquals('Vpdi_Entity', get_class($tree[2]));
    $this->assertEquals('Vpdi_Entity', get_class($tree[2][1]));
    
    $this->assertEquals('A', $tree[0]->profile());
    $this->assertEquals('B', $tree[0][1]->profile());
    $this->assertEquals('C', $tree[0][1][0]->profile());
    $this->assertEquals('D', $tree[1]->profile());
    $this->assertEquals('E', $tree[2]->profile());
    $this->assertEquals('F', $tree[2][1]->profile());
  }
  
  public function testVpdiExpandFailure() {
$sample = <<<EOF
BEGIN:a
k1:v1
BEGIN:b
k2:v2
END:c
END:a
EOF;
    $this->setExpectedException('Vpdi_BeginEndMismatchException');
    $tree = Vpdi::decode($sample);
  }
  
  public function testVpdiDecodeFailure() {
$sample = <<<EOF
BEGIN:a
k1:v1
END:a
EOF;
    $this->setExpectedException('Vpdi_UnexpectedEntityException');
    $tree = Vpdi::decode($sample, 'VCARD');
  }
  
  public function testRFC2425Sample2() {
    $sample = <<<EOF
BEGIN:VCARD
source:ldap://cn=bjorn%20Jensen, o=university%20of%20Michigan, c=US
name:Bjorn Jensen
fn:Bj=F8rn Jensen
n:Jensen;Bj=F8rn
email;TYPE=internet:bjorn@umich.edu
tel;TYPE=work,voice,msg:+1 313 747-4454
key;TYPE=x509;ENCODING=B:dGhpcyBjb3VsZCBiZSAKbXkgY2VydGlmaWNhdGUK
END:VCARD
EOF;
    $cards = Vpdi::decode($sample);
    $card = $cards[0];
    
    Vpdi::setConfig('always_encode_in_upper_case', false);
    $this->assertEquals($sample, $card->__toString());
    Vpdi::setConfig('always_encode_in_upper_case', true);
    
    $this->assertEquals('bjorn@umich.edu', $card->getValue('email'));
    $this->assertEquals('bjorn@umich.edu', $card->getValue('eMaiL'));
    $this->assertEquals('+1 313 747-4454', $card->getValue('tel'));
    $this->assertEquals('+1 313 747-4454', $card->getValue('tel', 'voice'));
    $this->assertEquals('+1 313 747-4454', $card->getValue('tEl', 'vOicE'));
    
    $tel_entries = $card->getFieldsByName('tel');
    $this->assertEquals(null, $tel_entries[0]->encoding());
    $key_entries = $card->getFieldsByName('key');
    $this->assertEquals('B', $key_entries[0]->encoding());
    
    $this->assertEquals('dGhpcyBjb3VsZCBiZSAKbXkgY2VydGlmaWNhdGUK', $card->getRawValue('key'));
    $this->assertEquals("this could be \nmy certificate\n", $card->getValue('key'));
    
    $this->assertEquals('Bj=F8rn Jensen', $card->name->fullname);
    $this->assertEquals('Bj=F8rn Jensen', $card->getValue('FN'));
    $this->assertEquals('Jensen', $card->name->family);
    $this->assertEquals('Bj=F8rn', $card->name->given);
    $this->assertEquals('', $card->name->prefixes);
    $this->assertEquals('+1 313 747-4454', $card->tel);
    $this->assertEquals('+1 313 747-4454', $card->phone->value);
    $this->assertEquals(array('work'), $card->phone->location);
    $this->assertEquals(array('voice', 'msg'), $card->phone->capability);
    $this->assertEquals('bjorn@umich.edu', $card->email->value);
  }
  
  public function testW3CSample() {
    $sample = <<<EOF
BEGIN:VCARD
VERSION:3.0
N:Doe;John;;;
FN:John Doe
ORG:Example.com Inc.;
TITLE:Imaginary test person
EMAIL;TYPE=INTERNET;TYPE=WORK;TYPE=pref:johnDoe@example.org
TEL;TYPE=WORK;TYPE=pref:+1 617 555 1212
TEL;TYPE=CELL:+1 781 555 1212
TEL;TYPE=HOME:+1 202 555 1212
TEL;TYPE=WORK:+1 (617) 555-1234
item1.ADR;TYPE=WORK:;;2 Example Avenue;Anytown;NY;01111;USA
item1.X-ABADR:us
item2.ADR;TYPE=HOME;TYPE=pref:;;3 Acacia Avenue;Newtown;MA;02222;USA
item2.X-ABADR:us
NOTE:John Doe has a long and varied history\, being documented on more police files that anyone else. Reports of his death are alas numerous.
item3.URL;TYPE=pref:http\://www.example/com/doe
item3.X-ABLabel:_$!<HomePage>!\$_
item4.URL:http\://www.example.com/Joe/foaf.df
item4.X-ABLABEL:FOAF
item5.X-ABRELATEDNAMES;TYPE=pref:Jane Doe
item5.X-ABLabel:_$!<Friend>!\$_
CATEGORIES:Work,Test group
X-ABUID:5AD380FD-B2DE-4261-BA99-DE1D1DB52FBE\:ABPerson
END:VCARD
EOF;
    $cards = Vpdi::decode($sample);
    $card = $cards[0];
    
    Vpdi::setConfig('type_values_as_a_parameter_list', true);
    Vpdi::setConfig('always_encode_in_upper_case', false);
    $this->assertEquals($sample, $card->__toString());
    Vpdi::setConfig('always_encode_in_upper_case', true);
    
    $this->assertEquals('+1 781 555 1212', $card->getValue('tel', 'cell'));
    $this->assertEquals('+1 202 555 1212', $card->getValue('tel', 'home'));
    $this->assertEquals('+1 617 555 1212', $card->getValue('tel', 'work'));
    
    $this->assertEquals('John Doe', $card->name->fullname);
    $this->assertEquals('Doe', $card->name->family);
    $this->assertEquals('John', $card->name->given);
    $this->assertEquals('', $card->name->prefixes);
    
    $this->assertEquals('', $card->addresses[0]->pobox);
    $this->assertEquals('', $card->addresses[0]->extended);
    $this->assertEquals('2 Example Avenue', $card->addresses[0]->street);
    $this->assertEquals('Anytown', $card->addresses[0]->locality);
    $this->assertEquals('NY', $card->addresses[0]->region);
    $this->assertEquals('01111', $card->addresses[0]->postalcode);
    $this->assertEquals('USA', $card->addresses[0]->country);
    $this->assertFalse($card->addresses[0]->preferred);
    $this->assertEquals(array('work'), $card->addresses[0]->delivery);
    $this->assertEquals(array('work'), $card->addresses[0]->location);
    $this->assertEquals('+1 617 555 1212', $card->phone->value);
    $this->assertEquals('+1 617 555 1212', $card->getPhone('work')->value);
    $this->assertEquals('johnDoe@example.org', $card->email->value);
    $this->assertEquals(array('work'), $card->email->location);
    $this->assertEquals('johnDoe@example.org', $card->emails[0]->value);
    $this->assertEquals(array('work'), $card->emails[0]->location);
  }
  
  public function testDates() {
    $sample = <<<EOF
BEGIN:VCARD
VERSION:3.0
N:Doe;John;;;
FN:John Doe
BDAY:1996-04-15
END:VCARD
EOF;
    $cards = Vpdi::decode($sample);
    $card = $cards[0];
    $this->assertEquals('15/04/1996', $card->bday->format('d/m/Y'));
  }
  
  public function testBasicVCardCreation() {
    $sample = <<<EOF
BEGIN:VCARD
N:Rougeron;Raphaël;;;
FN:Rougeron Raphaël
EMAIL;TYPE=home;TYPE=pref:raphael@myhost.com
TEL;TYPE=home:+336666666
END:VCARD
EOF;
    
    $card = new Vpdi_VCard();
    $card[] = new Vpdi_Field('n', 'Rougeron;Raphaël;;;');
    $card[] = new Vpdi_Field('fn', 'Rougeron Raphaël');
    $card[] = new Vpdi_Field('email', 'raphael@myhost.com', array('type' => array('home', 'pref')));
    $card[] = new Vpdi_Field('tel', '+336666666', array('type' => 'home'));
    
    $this->assertEquals($sample, $card->__toString());
  }
  
  public function testAdvancedVCardCreation() {
    $sample = <<<EOF
BEGIN:VCARD
N:Rougeron;Raphaël;;;
FN:Rougeron Raphaël
EMAIL;TYPE=home;TYPE=pref:raphael@myhost.com
TEL;TYPE=home:+336666666
ADR;TYPE=work:;;4 rue Giotto;Toulouse;Haute-Garonne;31000;FRANCE
END:VCARD
EOF;
    
    $name = new Vpdi_VCard_Name();
    $name->family = 'Rougeron';
    $name->given  = 'Raphaël';
    
    $phone = new Vpdi_VCard_Phone('+336666666');
    $phone->location[] = 'home';
    
    $email = new Vpdi_VCard_Email('raphael@myhost.com');
    $email->location[] = 'home';
    $email->preferred = true;
    
    $add = new Vpdi_VCard_Address();
    $add->street = '4 rue Giotto';
    $add->locality = 'Toulouse';
    $add->region = 'Haute-Garonne';
    $add->postalcode = '31000';
    $add->country = 'FRANCE';
    $add->location[] = 'work';
    
    $card = new Vpdi_VCard();
    $card->setName($name);
    $card->addEmail($email);
    $card->addPhone($phone);
    $card->addAddress($add);
    
    $this->assertEquals($sample, $card->__toString());
  }
}
