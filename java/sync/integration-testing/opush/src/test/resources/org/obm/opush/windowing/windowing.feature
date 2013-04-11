Feature: store and retrieve emails by chunks

  Scenario: user synchronize a very big mailbox by 25 elements
    Given user has 1000 elements in INBOX
    When user ask for the first 25 elements
    Then user get 25 elements
    And there is 975 elements left in store

  Scenario: user synchronize a very big mailbox by 25 elements then 10 elements
    Given user has 1000 elements in INBOX
    And user ask for the first 25 elements
    When user ask for the next 10 elements
    Then user get 10 elements
    And there is 965 elements left in store

  Scenario: user synchronize a very big mailbox with mixed window size
    Given user has 1000 elements in INBOX
    And user ask for the first 25 elements
    And user ask for the next 10 elements
    And user ask for the next 25 elements
    Then user get 25 elements
    And there is 940 elements left in store

  Scenario: user synchronize a very big mailbox by 25 elements then 55 elements
    Given user has 1000 elements in INBOX
    And user ask for the first 25 elements
    When user ask for the next 55 elements
    Then user get 55 elements
    And there is 920 elements left in store
    
  Scenario: user tries to synchronize an empty mailbox
    Given user has 0 elements in INBOX
    When user ask for the next 25 elements
    Then user get 0 elements
    And there is 0 elements left in store
    
  Scenario: user synchronize more elements than mailbox size
    Given user has 1000 elements in INBOX
    When user ask for the first 1005 elements
    Then user get 1000 elements
    And there is 0 elements left in store
    
  Scenario: user synchronize the whole mailbox at once
    Given user has 1000 elements in INBOX
    When user ask for the first 1000 elements
    Then user get 1000 elements
    And there is 0 elements left in store

  Scenario: user empties its mailbox 25 elements at a time
    Given user has 1000 elements in INBOX
    And user ask for the first 25 elements
    When user ask repeatedly for 25 elements
    Then user get 975 elements in 39 iterations
    And there is 0 elements left in store

  Scenario: user ask for first elements after that a big mailbox synchronization has been started
    Given user has 1000 elements in INBOX
    And user ask for the first 25 elements
    And user ask for the next 25 elements
    When user ask for the first 25 elements
    Then user get 25 elements
    And there is 975 elements left in store

  Scenario: user refill INBOX ask for first elements after that a big INBOX synchronization has been started
    Given user has 1000 elements in INBOX
    And user ask for the first 25 elements
    And user ask for the next 25 elements
    And user has 500 elements in INBOX
    When user ask for the first 25 elements
    Then user get 25 elements
    And there is 475 elements left in store