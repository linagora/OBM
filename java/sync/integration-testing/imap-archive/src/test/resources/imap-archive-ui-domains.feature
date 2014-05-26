Feature: Mail archive - UI domains

  Scenario: admin0 loads domain creation page
    Given admin0 connects to global.virt domain
    When admin0 goto domains index
    And admin0 clicks on new domain button
    Then a selectbox labeled 'IMAP Archive' and named sel_imap_archive should be present

  Scenario: admin0 loads domain consultation page
    Given admin0 connects to global.virt domain
    And one domain configured
    When admin0 goto domains index
    And admin0 clicks on search button
    And admin0 clicks on the first line in domain results
    Then an element labeled 'IMAP Archive' should be present

  Scenario: admin0 loads domain modification page
    Given admin0 connects to global.virt domain
    And one domain configured
    When admin0 goto domains index
    And admin0 clicks on search button
    And admin0 clicks on the first line in domain results
    And admin0 clicks on modify button
    Then a selectbox labeled 'IMAP Archive' and named sel_imap_archive should be present

  Scenario: admin0 creates a domain
    Given admin0 connects to global.virt domain
    When admin0 goto domains index
    And admin0 clicks on new domain button
    Then a selectbox labeled 'IMAP Archive' and named sel_imap_archive should be present

  Scenario: admin0 creates a domain and checks the result
    Given admin0 connects to global.virt domain
    And a host named 'myhost' configured for IMAP Archive server 
    When admin0 goto domains index
    And admin0 clicks on new domain button
    And admin0 writes 'mydomain' in 'tf_label' field
    And admin0 writes 'mydomain.org' in 'tf_domain_name' field
    And admin0 selects 'myhost' in sel_imap_archive selectbox
    And admin0 validates
    Then admin0 goto domains index
    And admin0 clicks on search button
    And admin0 clicks the line with label 'mydomain'
    And 'IMAP archive server' table contains only one line with 'myhost' as host

  Scenario: admin0 creates a domain and checks the result when multiple hosts
    Given admin0 connects to global.virt domain
    And a host named 'myhost' configured for IMAP Archive server 
    And a host named 'myhost2' configured for IMAP Archive server 
    When admin0 goto domains index
    And admin0 clicks on new domain button
    And admin0 writes 'mydomain' in 'tf_label' field
    And admin0 writes 'mydomain.org' in 'tf_domain_name' field
    And admin0 selects 'myhost2' in sel_imap_archive selectbox
    And admin0 validates
    Then admin0 goto domains index
    And admin0 clicks on search button
    And admin0 clicks the line with label 'mydomain'
    And 'IMAP archive server' table contains only one line with 'myhost2' as host
    