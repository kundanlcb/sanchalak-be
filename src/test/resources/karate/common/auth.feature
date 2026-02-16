Feature: Auth helper for Karate tests

  Scenario: create and login student user
    * def uniq = java.lang.System.currentTimeMillis() + ''
    * def email = 'karate.' + uniq + '@example.com'
    * def password = defaultPassword

    Given url baseUrl
    And path '/api/auth/signup'
    And request
      """
      {
        "name": "Karate Student",
        "email": "#(email)",
        "password": "#(password)",
        "role": "ROLE_STUDENT"
      }
      """
    When method post
    Then status 201
    And match response.success == true

    Given url baseUrl
    And path '/api/auth/signin'
    And request { email: '#(email)', password: '#(password)' }
    When method post
    Then status 200
    And match response.accessToken == '#string'
    And match response.tokenType == 'Bearer'

    * def token = response.accessToken
    * def authHeader = 'Bearer ' + token
