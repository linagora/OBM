Feature: tests on contacts

  Scenario: create a contact without name fails
    Given on create contact page
    When user creates a contact without lastname 
    Then creation fails

  Scenario: create a contact succeeds
    Given on create contact page
    When user creates a contact:
      | firstName | lastName | companyField |
      | John      | Doe      | Linagora     | 
    And user validate
    Then "John Doe" is 1 times in "Mes contacts" address book
    
   Scenario: create a existing contact and accept
    Given on create contact page
    When user creates a contact:
      | firstName | lastName | companyField |
      | John      | Doe      | Linagora     | 
    And user validate accepting existing popup
    Then "John Doe" is 2 times in "Mes contacts" address book
    
   Scenario: create a existing contact and cancel
    Given on create contact page
    When user creates a contact:
      | firstName | lastName | companyField |
      | John      | Doe      | Linagora     | 
    And user validate cancelling existing popup
    Then creation page still active with "Doe" as lastname

   Scenario: delete an existing contact in My Contacts
    Given on contacts page
    And address book "Mes contacts" is selected
    When user deletes contact "Contact1 Test"
    And user accepts confirmation popup
    Then "Contact1 Test" is 0 times in "Mes contacts" address book
    And "Contact1 Test" is 1 times in "Archive" address book

   Scenario: delete an existing contact in My Addressbook
    Given on contacts page
    And address book "My Addressbook" is selected
    When user deletes contact "Contact2 Test"
    And user accepts confirmation popup
    Then "Contact2 Test" is 0 times in "Mes contacts" address book
    And "Contact2 Test" is 1 times in "Archive" address book

   Scenario: cannot delete an existing contact in an address book I have no WRITE rights on
    Given on contacts page
    And address book "Mes contacts (admin" is selected
    When user selects contact "AdminContact1 Test"
    Then there is no delete button
