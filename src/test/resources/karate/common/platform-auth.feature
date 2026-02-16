Feature: Platform auth helper for sanchalan-admin journeys

  Scenario: login as platform admin
    Given url baseUrl
    And path '/api/platform/v1/auth/login'
    And request
      """
      {
        "email": "#(platformEmail)",
        "password": "#(platformPassword)"
      }
      """
    When method post
    Then status 200
    And match response.accessToken == '#string'

    * def token = response.accessToken
    * def authHeader = 'Bearer ' + token
