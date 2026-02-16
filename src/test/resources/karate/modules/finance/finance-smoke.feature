@smoke @regression @finance
Feature: Finance smoke checks

  Scenario: finance structures list requires elevated role
    * def session = call read('classpath:karate/common/auth.feature')
    Given url baseUrl
    And path '/api/finance/structures'
    And header Authorization = session.authHeader
    When method get
    Then status 403

  Scenario: ledger endpoint responds with validation error for unknown student
    * def session = call read('classpath:karate/common/auth.feature')
    Given url baseUrl
    And path '/api/finance/students', 999999, 'ledger'
    And header Authorization = session.authHeader
    When method get
    Then status 400
