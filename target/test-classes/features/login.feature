Feature: Login Module

  @smoke
  Scenario: Valid login with correct credentials
    Given I am on the login page
    When I enter username "tomsmith" and password "SuperSecretPassword!"
    And I click the login button
    Then I should see the message "You logged into a secure area!"

  @regression
  Scenario: Invalid login with wrong password
    Given I am on the login page
    When I enter username "tomsmith" and password "wrongpassword"
    And I click the login button
    Then I should see the message "Your password is invalid!"

  @edge-case
  Scenario: Login with empty credentials
    Given I am on the login page
    When I enter username "" and password ""
    And I click the login button
    Then I should see the message "Your username is invalid!"