 # ***** BEGIN LICENSE BLOCK *****
 # 
 # Copyright (C) 2011-2014  Linagora
 #
 # This program is free software: you can redistribute it and/or modify it under
 # the terms of the GNU Affero General Public License as published by the Free
 # Software Foundation, either version 3 of the License, or (at your option) any
 # later version, provided you comply with the Additional Terms applicable for OBM
 # software by Linagora pursuant to Section 7 of the GNU Affero General Public
 # License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 # retain the displaying by the interactive user interfaces of the “OBM, Free
 # Communication by Linagora” Logo with the “You are using the Open Source and
 # free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 # by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 # links between OBM and obm.org, between Linagora and linagora.com, as well as
 # between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 # from infringing Linagora intellectual property rights over its trademarks and
 # commercial brands. Other Additional Terms apply, see
 # <http://www.linagora.com/licenses/> for more details.
 # 
 # This program is distributed in the hope that it will be useful, but WITHOUT ANY
 # WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 # PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 # 
 # You should have received a copy of the GNU Affero General Public License and
 # its applicable Additional Terms for OBM along with this program. If not, see
 # <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 # version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 # applicable to the OBM software.
 # 
 # ***** END LICENSE BLOCK ***** */
Feature: tests on users

   Scenario: create a user without name fails
    Given connected as "admin" with password "admin" on domain "obm.domain"
    Given on create user page
    When user creates a user without name 
    Then creation fails with "Le nom doit être correctement renseigné ! :" as message

   Scenario: create a user without email fails
     Given connected as "admin" with password "admin" on domain "obm.domain"
     Given on create user page
     When user creates a user without email 
     Then creation fails with "Vous devez saisir une adresse E-mail afin d'activer la messagerie !" as message

   Scenario: create an admin user
     Given connected as admin0
     Given on create user page
     When user creates a user "testAdminUser" with admin profile 
     Then creation succeeds

   Scenario: create a user
     Given connected as "admin" with password "admin" on domain "obm.domain"
     Given on create user page
     When user creates a user "testRegularUser"
     Then creation succeeds

   Scenario: create a user already existing
     Given connected as "admin" with password "admin" on domain "obm.domain"
     Given on create user page
     When user creates a user already existing 
     Then creation fails with "testuser : Le login est déjà attribué à un autre utilisateur !" as message

   Scenario: delete an admin user
     Given connected as admin0
     Given on delete user page
     When user deletes "testAdmin2"
     Then deletion succeeds
     And "testadmin2" is no longer in user list
     And "testadmin2" can t connect anymore with password "testadmin2" on domain "obm.domain"

   Scenario: delete a non admin user
     Given connected as "admin" with password "admin" on domain "obm.domain"
     Given on delete user page
     When user deletes "testuser2"
     Then deletion succeeds
     And "testuser2" is no longer in user list
     And "testuser2" can t connect anymore with password "testuser2" on domain "obm.domain"
