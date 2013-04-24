Feature: test login and logout

  Scenario: title contains OBM
    Given on login page
    Then title contains "OBM"

  Scenario: login fails because of unknown login
    Given on login page
    When user logs as:
      | login      | password | domain        |
      | doNotExist | neither  | Global Domain |
    Then invalid login page displayed
    
  Scenario: login as admin0
    Given on login page
    When user logs as:
      | login      | password | domain        |
      | admin0     | admin    | Global Domain |
    Then user is logged
    
   Scenario: access home page without login returns login page
    Given on login page
    When user logs as:
      | login      | password | domain        |
      |            |          |               |	
	Then empty login page is displayed

   Scenario: access home page with login does not return login page
    Given on login page
    When user logs as:
      | login      | password | domain        |
      | admin0     | admin    | Global Domain |
    Then empty login page is not displayed
    
   Scenario: logout
    Given on login page
    When user logs as:
      | login      | password | domain        |
      | admin0     | admin    | Global Domain |   	
   	And user logout
    Then empty login page is displayed
	
   Scenario: logout by Url
    Given on login page
    When user logs as:
      | login      | password | domain        |
      | admin0     | admin    | Global Domain |   	
    And user logout by url
    Then empty login page is displayed