Feature: Generic session helper for app users

  Scenario: create and login user for a role
    * def role = karate.get('role') || 'ROLE_STUDENT'
    * def namePrefix = karate.get('namePrefix') || 'Karate User'
    * def uniq = java.lang.System.currentTimeMillis() + ''
    * def email = 'karate.' + role + '.' + uniq + '@example.com'
    * def password = defaultPassword

    Given url baseUrl
    And path '/api/auth/signup'
    And request
      """
      {
        "name": "#(namePrefix)",
        "email": "#(email)",
        "password": "#(password)",
        "role": "#(role)"
      }
      """
    When method post
    Then status 201

    Given url baseUrl
    And path '/api/auth/signin'
    And request { email: '#(email)', password: '#(password)' }
    When method post
    Then status 200
    And match response.accessToken == '#string'

    * def token = response.accessToken
    * def authHeader = 'Bearer ' + token
    * def userEmail = email
    * def userPassword = password
