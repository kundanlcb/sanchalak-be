@smoke @regression @auth
Feature: Authentication smoke checks

  Scenario: user can sign up and sign in
    * def session = call read('classpath:karate/common/auth.feature')
    * match session.token == '#string'
    * match session.authHeader contains 'Bearer '

  Scenario: OTP request rejects unknown mobile cleanly
    Given url baseUrl
    And path '/api/auth/otp/request'
    And request { mobileNumber: '9990001112' }
    When method post
    Then status 400
