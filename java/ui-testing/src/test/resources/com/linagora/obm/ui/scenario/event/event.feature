Feature: tests on event creation

  Scenario: Successfully creates a simple event
    Given on calendar page
    When I create a meeting "Simple Event Test" at 01/01/2014 from 12:00 to 14:00
    Then I am redirected to my calendar
    And I am informed that the event has been inserted
    And event "Simple Event Test" is inserted in the calendar at 01/01/2014 from 12:00 to 14:00
    
  Scenario: Create a simple event without title fails 
    Given on calendar page
    When I create a meeting "" at 02/01/2014 from 12:00 to 14:00
    Then the red message "Données invalides : La date de fin doit être supérieure à la date de début" is displayed
    
  Scenario: Create a simple event with wrong date fails 
    Given on calendar page
    When I create a meeting "" at 02/01/2014 from 12:00 to 11:00
    Then a popup with the message "Veuillez remplir le champ [ Titre ]" is displayed    
    
  Scenario: Successfully creates a daily recurrent event
    Given on calendar page
    When I create a meeting "Daily Event Test" at 03/01/2014 from 12:00 to 14:00 every day
    Then I am redirected to my calendar
    And I am informed that the event has been inserted
    And event "Daily Event Test" is inserted in the calendar
    And event "Daily Event Test" appears every day, first is 03/01/2014 from 12:00 to 14:00
    
  Scenario: Successfully creates a weekly recurrent event
    Given on calendar page
    When I create a meeting "Weekly Event Test" at 04/01/2014 from 12:00 to 14:00 every week
    Then I am redirected to my calendar
    And I am informed that the event has been inserted
    And event "Weekly Event Test" is inserted in the calendar
    And event "Weekly Event Test" appears every week on saturday, first is 04/01/2014 from 12:00 to 14:00
    
  Scenario: Successfully creates a monthly recurrent event
    Given on calendar page
    When I create a meeting "Monthly Event Test" at 05/01/2014 from 12:00 to 14:00 every month
    Then I am redirected to my calendar
    And I am informed that the event has been inserted
    And event "Monthly Event Test" is inserted in the calendar
    And event "Monthly Event Test" appears every month at 05, first is 05/01/2014 from 12:00 to 14:00
    
  Scenario: Successfully creates a yearly recurrent event
    Given on calendar page
    When I create a meeting "Yearly Event Test" at 06/01/2014 from 12:00 to 14:00 every year
    Then I am redirected to my calendar
    And I am informed that the event has been inserted
    And event "Monthly Event Test" is inserted in the calendar
    And event "Monthly Event Test" appears every year at 06/01 from 12:00 to 14:00
    
  Scenario: Successfully creates a allday simple event
    Given on calendar page
    When I create a allday "allday Event Test" at 07/01/2014
    Then I am redirected to my calendar
    And I am informed that the event has been inserted
    And allday event "allday Event Test" is inserted in the calendar at 07/01/2014
 	
    
    