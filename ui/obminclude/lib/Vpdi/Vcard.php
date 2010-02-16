<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2010 OBM.org project members team                   |
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

/**
 * vCard class
 * 
 * This class implements vCard 3.0 and should not be used with vCard 2.1
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
 * @license GPL 2.0
 */
class Vpdi_Vcard extends Vpdi_Entity {

  protected $profile = 'VCARD';
  
  /**
   * Returns a Vpdi_Vcard_Name object wrapping the value of the N property
   * 
   * @access public
   * @return Vpdi_Vcard_Name
   */
  public function getName() {
    return Vpdi_Vcard_Name::decode($this->getProperty('N'), $this->getProperty('FN'));
  }
  
  /**
   * Sets the name properties, N and FN, by passing a Vpdi_Vcard_Name object
   * 
   * @access public
   * @param Vpdi_Vcard_Name $name
   * @return void
   */
  public function setName(Vpdi_Vcard_Name $name) {
    $this->addProperty($name->encode());
    $this->addProperty($name->encodeFn());
  }
  
  /**
   * Returns a DateTime object set to the value of the BDAY property
   * 
   * @access public
   * @return DateTime
   */
  public function getBday() {
    if (($bday = $this->getValue('BDAY')) === null) {
      return null;
    }
    return Vpdi::decodeDate($bday);
  }
  
  /**
   * Sets the birthday property by passing a DateTime object
   * 
   * @access public
   * @param DateTime $bday
   * @return void
   */
  public function setBday(DateTime $bday) {
    $this->addProperty($bday->format('Y-m-d'));
  }
  
  /**
   * Returns a Vpdi_Vcard_Address object wrapping the first ADR value of type $type
   * 
   * @access public
   * @param string $type optional specific type (ex: 'home', 'work', ...)
   * @return Vpdi_Vcard_Address
   */
  public function getAddress($type = null) {
    if (($adr = $this->getProperty('ADR')) === null) {
      return null;
    }
    return Vpdi_Vcard_Address::decode($adr);
  }
  
  /**
   * Returns an array of Vpdi_Vcard_Address objects wrapping the ADR values
   * 
   * @access public
   * @return array
   */
  public function getAddresses() {
    $addresses = array();
    foreach ($this->getPropertiesByName('ADR') as $property) {
      $addresses[] = Vpdi_Vcard_Address::decode($property);
    }
    return $addresses;
  }
  
  /**
   * Adds an ADR property by passing a Vpdi_Vcard_Address object
   * 
   * @access public
   * @param Vpdi_Vcard_Address $address
   * @return void
   */
  public function addAddress(Vpdi_Vcard_Address $address) {
    $this->addProperty($address->encode());
  }
  
  /**
   * Returns a Vpdi_Vcard_Phone object wrapping the first TEL value of type $type
   * 
   * @access public
   * @param string $type optional specific type (ex: 'home', 'work', ...)
   * @return Vpdi_Vcard_Phone
   */
  public function getPhone($type = null) {
    if (($tel = $this->getProperty('TEL')) === null) {
      return null;
    }
    return Vpdi_Vcard_Phone::decode($tel);
  }
  
  /**
   * Returns an array of Vpdi_Vcard_Phone objects wrapping the TEL values
   * 
   * @access public
   * @return array
   */
  public function getPhones() {
    $phones = array();
    foreach ($this->getPropertiesByName('TEL') as $property) {
      $phones[] = Vpdi_Vcard_Phone::decode($property);
    }
    return $phones;
  }
  
  /**
   * Adds a TEL property by passing a Vpdi_Vcard_Phone object
   * 
   * @access public
   * @param Vpdi_Vcard_Phone $phone
   * @return void
   */
  public function addPhone(Vpdi_Vcard_Phone $phone) {
    $this->addProperty($phone->encode());
  }
  
  /**
   * Returns a Vpdi_Vcard_Email object wrapping the first EMAIL value of type $type
   * 
   * @access public
   * @param string $type optional specific type (ex: 'home', 'work', ...)
   * @return Vpdi_Vcard_Email
   */
  public function getEmail($type = null) {
    if (($mail = $this->getProperty('EMAIL')) === null) {
      return null;
    }
    return Vpdi_Vcard_Email::decode($mail);
  }
  
  /**
   * Returns an array of Vpdi_Vcard_Email objects wrapping the EMAIL values
   * 
   * @access public
   * @return array
   */
  public function getEmails() {
    $phones = array();
    foreach ($this->getPropertiesByName('EMAIL') as $property) {
      $phones[] = Vpdi_Vcard_Email::decode($property);
    }
    return $phones;
  }
  
  /**
   * Adds an EMAIL property by passing a Vpdi_Vcard_Email object
   * 
   * @access public
   * @param Vpdi_Vcard_Email $email
   * @return void
   */
  public function addEmail(Vpdi_Vcard_Email $email) {
    $this->addProperty($email->encode());
  }
  
  /**
   * Returns a Vpdi_Vcard_Impp object wrapping the first IMPP value of type $type
   * 
   * @access public
   * @param string $type optional specific type (ex: 'home', 'work', ...)
   * @return Vpdi_Vcard_Impp
   */
  public function getImpp($type = null) {
    if (($impp = $this->getProperty('IMPP')) === null) {
      return null;
    }
    return Vpdi_Vcard_Impp::decode($impp);
  }
  
  /**
   * Returns an array of Vpdi_Vcard_Impp objects wrapping the IMPP values
   * 
   * @access public
   * @return array
   */
  public function getImpps() {
    $ims = array();
    foreach ($this->getPropertiesByName('IMPP') as $property) {
      $ims[] = Vpdi_Vcard_Impp::decode($property);
    }
    return $ims;
  }
  
  /**
   * Adds an IMPP property by passing a Vpdi_Vcard_Impp object
   * 
   * @access public
   * @param Vpdi_Vcard_Impp $im
   * @return void
   */
  public function addImpp(Vpdi_Vcard_Impp $im) {
    $this->addProperty($im->encode());
  }
  
  /**
   * Returns the raw value for a specific $name
   * 
   * If no property exists, null is returned.
   * 
   * If multiple properties exist, the method will prefer properties with values 
   * and secondly properties with a type=pref. If multiple properties subsist after
   * this sort, then the first property will be returned.
   * 
   * @param string $name the name of the property
   * @param string $type optional specific type (ex: 'home', 'work', ...)
   * @access public
   * @return mixed
   */
  public function getRawValue($name, $type = null) {
    $property = $this->getProperty($name, $type);
    if ($property === null) {
      return null;
    }
    return $property->rawValue();
  }
  
  /**
   * Returns the value for a specific $name
   * 
   * If no property exists, null is returned.
   * 
   * If multiple properties exist, the method will prefer properties with values 
   * and secondly properties with a type=pref. If multiple properties subsist after
   * this sort, then the first property will be returned.
   * 
   * @param string $name the name of the property
   * @param string $type optional specific type (ex: 'home', 'work', ...)
   * @access public
   * @return mixed
   */
  public function getValue($name, $type = null) {
    $property = $this->getProperty($name, $type);
    if ($property === null) {
      return null;
    }
    return $this->decodeProperty($property);
  }
  
  /**
   * Returns the property for a specific $name
   * 
   * If no property exists, null is returned.
   * 
   * If multiple properties exist, the method will prefer properties with values 
   * and secondly properties with a type=pref. If multiple properties subsist after
   * this sort, then the first property will be returned.
   * 
   * @param string $name the name of the property
   * @param string $type optional specific type (ex: 'home', 'work', ...)
   * @access public
   * @return mixed
   */
  public function getProperty($name, $type = null) {
    if ($type === null) {
      $properties = $this->getPropertiesByName($name);
    } else {
      $properties = array();
      foreach ($this->getPropertiesByName($name) as $p) {
        if ($p->typeEquals($type)) {
          $properties[] = $p;
        }
      }
    }
    
    // TODO : gestion de type=PREF
    
    if (count($properties) == 0) {
      return null;
    }
    return $properties[0];
  }
}
