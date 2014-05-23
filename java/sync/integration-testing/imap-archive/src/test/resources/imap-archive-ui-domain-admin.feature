Feature: Mail archive - UI domain administrators

  Scenario: admin1 has a menu link
    Given admin1 connects to mydomain.org domain
    When admin1 clicks on 'Administration' menu
    Then a link to 'IMAP Archive' is displayed with href equals to "./imap_archive/imap_archive_index.php"

  Scenario: error annotation is displayed when the 'IMAP Archive' service is unreachable  
    Given admin1 connects to mydomain.org domain
    When admin1 clicks on 'Administration' menu
    And admin1 clicks on 'IMAP Archive' element
    Then the 'IMAP Archive' page is loaded
    And an error appears 'IMAP Archive server unreachable'