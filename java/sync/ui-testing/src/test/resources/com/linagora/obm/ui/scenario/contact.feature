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
    Then "John Doe" is 1 time(s) in contact list
    
   Scenario: create a existing contact and accept
    Given on create contact page
    When user creates a contact:
      | firstName | lastName | companyField |
      | John      | Doe      | Linagora     | 
    And user validate accepting existing popup
    Then "John Doe" is 2 time(s) in contact list
    
   Scenario: create a existing contact and cancel
    Given on create contact page
    When user creates a contact:
      | firstName | lastName | companyField |
      | John      | Doe      | Linagora     | 
    And user validate cancelling existing popup
    Then creation page still active with "Doe" as lastname