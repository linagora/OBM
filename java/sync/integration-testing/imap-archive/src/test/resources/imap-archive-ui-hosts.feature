Feature: Mail archive - UI hosts

  Scenario: admin0 searches for host with 'IMAP Archive' flag when none
    Given admin0 connects to global.virt domain
    And one host without 'IMAP Archive' configured
    When admin0 goto hosts index
    And admin0 checks 'IMAP Archive' flag
    And admin0 clicks on search button
    Then no result is found

  Scenario: admin0 searches for host with 'IMAP Archive' flag when one defined
    Given admin0 connects to global.virt domain
    And one host with 'IMAP Archive' configured
    When admin0 goto hosts index
    And admin0 checks 'IMAP Archive' flag
    And admin0 clicks on search button
    Then one result is found

  Scenario: admin0 loads host creation page
    Given admin0 connects to global.virt domain
    When admin0 goto hosts index
    And admin0 clicks on new host button
    Then a checkbox labeled 'IMAP Archive' with id cb_imap_archive should be present

  Scenario: admin0 loads host consultation page
    Given admin0 connects to global.virt domain
    And one host configured
    When admin0 goto hosts index
    And admin0 clicks on search button
    And admin0 clicks on the first line in host results
    Then an element labeled 'IMAP Archive' should be present

  Scenario: admin0 loads host modification page
    Given admin0 connects to global.virt domain
    And one host configured
    When admin0 goto hosts index
    And admin0 clicks on search button
    And admin0 clicks on the first line in host results
    And admin0 clicks on modify button
    Then a checkbox labeled 'IMAP Archive' with id cb_imap_archive should be present

  Scenario: admin0 modifies a host
    Given admin0 connects to global.virt domain
    And one host without 'IMAP Archive' configured
    When admin0 goto hosts index
    And admin0 clicks on search button
    And admin0 clicks on the first line in host results
    And admin0 clicks on modify button
    And admin0 clicks on 'IMAP Archive' checkbox
    And admin0 validates
    And admin0 goto hosts index
    And admin0 checks 'IMAP Archive' flag
    And admin0 clicks on search button
    Then one result is found

  Scenario: admin0 creates a host
    Given admin0 connects to global.virt domain
    When admin0 goto hosts index
    And admin0 clicks on new host button
    And admin0 clicks on 'IMAP Archive' checkbox
    And admin0 validates
    And admin0 goto hosts index
    And admin0 checks 'IMAP Archive' flag
    And admin0 clicks on search button
    Then one result is found
    