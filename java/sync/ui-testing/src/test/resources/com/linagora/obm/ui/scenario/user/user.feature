Feature: tests on users

  Scenario: create a user without name fails
    Given on create user page
    When user creates a user without name 
    Then creation fails with "Le nom doit être correctement renseigné ! :" as message

   Scenario: create a user without email fails
     Given on create user page
     When user creates a user without email 
     Then creation fails with "Vous devez saisir une adresse E-mail afin d'activer la messagerie !" as message

   Scenario: create an admin user
     Given on create user page
     When user creates a user with admin profile 
     Then creation succeeds

   Scenario: create a user
     Given on create user page
     When user creates a user 
     Then creation succeeds

   Scenario: create a user already existing
     Given on create user page
     When user creates a user already existing 
     Then creation fails with "testuser : Le login est déjà attribué à un autre utilisateur !" as message