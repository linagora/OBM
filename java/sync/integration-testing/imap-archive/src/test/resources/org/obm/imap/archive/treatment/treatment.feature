Feature: Mail archive - Treatment

  Scenario: archive treatment on INBOX containing 5 mails one month old
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera@mydomain.org" imap folder
    And this user has 5 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 5 mails should be archived in the "user/usera/arChive/2014/INBOX@mydomain.org" imap folder with subject "subject"

  Scenario: archive treatment on INBOX containing 4 mails older one month old and 1 mail one day old 
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera@mydomain.org" imap folder
    And this user has 4 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject"
    And this user has 1 mail at "2014-12-09T10:00:00Z" in this folder with subject "subject not archived"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 4 mails should be archived in the "user/usera/arChive/2014/INBOX@mydomain.org" imap folder with subject "subject"

  Scenario: archive treatment on INBOX containing 5 mails in 5 different years 
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera@mydomain.org" imap folder
    And this user has 1 mail at "2010-11-08T10:00:00Z" in this folder with subject "subject 2010"
    And this user has 1 mail at "2011-11-08T10:00:00Z" in this folder with subject "subject 2011"
    And this user has 1 mail at "2012-11-08T10:00:00Z" in this folder with subject "subject 2012"
    And this user has 1 mail at "2013-11-08T10:00:00Z" in this folder with subject "subject 2013"
    And this user has 1 mail at "2014-11-08T10:00:00Z" in this folder with subject "subject 2014"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 1 mail should be archived in the "user/usera/arChive/2010/INBOX@mydomain.org" imap folder with subject "subject 2010"
    And 1 mail should be archived in the "user/usera/arChive/2011/INBOX@mydomain.org" imap folder with subject "subject 2011"
    And 1 mail should be archived in the "user/usera/arChive/2012/INBOX@mydomain.org" imap folder with subject "subject 2012"
    And 1 mail should be archived in the "user/usera/arChive/2013/INBOX@mydomain.org" imap folder with subject "subject 2013"
    And 1 mail should be archived in the "user/usera/arChive/2014/INBOX@mydomain.org" imap folder with subject "subject 2014"

  Scenario: archive treatment on INBOX containing 5 mails in 5 different years with UIDs not ordered by date
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera@mydomain.org" imap folder
    And this user has 1 mail at "2013-11-08T10:00:00Z" in this folder with subject "subject 2013"
    And this user has 1 mail at "2011-11-08T10:00:00Z" in this folder with subject "subject 2011"
    And this user has 1 mail at "2012-11-08T10:00:00Z" in this folder with subject "subject 2012"
    And this user has 1 mail at "2014-11-08T10:00:00Z" in this folder with subject "subject 2014"
    And this user has 1 mail at "2010-11-08T10:00:00Z" in this folder with subject "subject 2010"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 1 mail should be archived in the "user/usera/arChive/2010/INBOX@mydomain.org" imap folder with subject "subject 2010"
    And 1 mail should be archived in the "user/usera/arChive/2011/INBOX@mydomain.org" imap folder with subject "subject 2011"
    And 1 mail should be archived in the "user/usera/arChive/2012/INBOX@mydomain.org" imap folder with subject "subject 2012"
    And 1 mail should be archived in the "user/usera/arChive/2013/INBOX@mydomain.org" imap folder with subject "subject 2013"
    And 1 mail should be archived in the "user/usera/arChive/2014/INBOX@mydomain.org" imap folder with subject "subject 2014"

  Scenario: archive treatment on a folder containing 5 mails one month old
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera/myFolder@mydomain.org" imap folder
    And this user has 5 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 5 mails should be archived in the "user/usera/arChive/2014/myFolder@mydomain.org" imap folder with subject "subject"

  Scenario: archive treatment on a folder containing 4 mails older one month old and 1 mail one day old 
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera/myFolder@mydomain.org" imap folder
    And this user has 4 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject"
    And this user has 1 mail at "2014-12-09T10:00:00Z" in this folder with subject "subject not archived"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 4 mails should be archived in the "user/usera/arChive/2014/myFolder@mydomain.org" imap folder with subject "subject"

  Scenario: archive treatment on a folder containing 5 mails in 5 different years 
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera/myFolder@mydomain.org" imap folder
    And this user has 1 mail at "2010-11-08T10:00:00Z" in this folder with subject "subject 2010"
    And this user has 1 mail at "2011-11-08T10:00:00Z" in this folder with subject "subject 2011"
    And this user has 1 mail at "2012-11-08T10:00:00Z" in this folder with subject "subject 2012"
    And this user has 1 mail at "2013-11-08T10:00:00Z" in this folder with subject "subject 2013"
    And this user has 1 mail at "2014-11-08T10:00:00Z" in this folder with subject "subject 2014"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 1 mail should be archived in the "user/usera/arChive/2010/myFolder@mydomain.org" imap folder with subject "subject 2010"
    And 1 mail should be archived in the "user/usera/arChive/2011/myFolder@mydomain.org" imap folder with subject "subject 2011"
    And 1 mail should be archived in the "user/usera/arChive/2012/myFolder@mydomain.org" imap folder with subject "subject 2012"
    And 1 mail should be archived in the "user/usera/arChive/2013/myFolder@mydomain.org" imap folder with subject "subject 2013"
    And 1 mail should be archived in the "user/usera/arChive/2014/myFolder@mydomain.org" imap folder with subject "subject 2014"

  Scenario: archive treatment on a folder containing 5 mails in 5 different years with UIDs not ordered by date
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera/myFolder@mydomain.org" imap folder
    And this user has 1 mail at "2013-11-08T10:00:00Z" in this folder with subject "subject 2013"
    And this user has 1 mail at "2011-11-08T10:00:00Z" in this folder with subject "subject 2011"
    And this user has 1 mail at "2012-11-08T10:00:00Z" in this folder with subject "subject 2012"
    And this user has 1 mail at "2014-11-08T10:00:00Z" in this folder with subject "subject 2014"
    And this user has 1 mail at "2010-11-08T10:00:00Z" in this folder with subject "subject 2010"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 1 mail should be archived in the "user/usera/arChive/2010/myFolder@mydomain.org" imap folder with subject "subject 2010"
    And 1 mail should be archived in the "user/usera/arChive/2011/myFolder@mydomain.org" imap folder with subject "subject 2011"
    And 1 mail should be archived in the "user/usera/arChive/2012/myFolder@mydomain.org" imap folder with subject "subject 2012"
    And 1 mail should be archived in the "user/usera/arChive/2013/myFolder@mydomain.org" imap folder with subject "subject 2013"
    And 1 mail should be archived in the "user/usera/arChive/2014/myFolder@mydomain.org" imap folder with subject "subject 2014"

  Scenario: archive treatment on a folder containing 1 mail on the epoch date
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera/myFolder@mydomain.org" imap folder
    And this user has 1 mail at "1970-01-01T00:00:00Z" in this folder with subject "subject epoch"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then 1 mail should be archived in the "user/usera/arChive/1970/myFolder@mydomain.org" imap folder with subject "subject epoch"

  Scenario: archive treatment on a folder containing 3 mails in the future
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera/myFolder@mydomain.org" imap folder
    And this user has 1 mail at "2015-01-01T10:00:00Z" in this folder with subject "subject 2015"
    And this user has 1 mail at "2020-02-01T10:00:00Z" in this folder with subject "subject 2020"
    And this user has 1 mail at "2300-03-01T10:00:00Z" in this folder with subject "subject 2300"
    And current date is "2014-12-10T15:07:00Z"
    When admin launches an immediate treatment
    Then this user imap folders should contain 3 mails

  Scenario: scheduler process an archive treatment on a folder containing 5 mails one month old
    Given configuration state is "ENABLE"
    And configuration repeat kind is set to "DAILY" at 10:30
    And a user "usera" with "user/usera/myFolder@mydomain.org" imap folder
    And this user has 5 mails at "2014-11-08T10:00:00Z" in this folder with subject "subject"
    And current date is "2014-12-10T10:28:00Z"
    When current date is "2014-12-10T10:29:58Z"
    Then an archive treatment has been processed by the scheduler after 1 second
    And 5 mails should be archived in the "user/usera/arChive/2014/myFolder@mydomain.org" imap folder with subject "subject"
