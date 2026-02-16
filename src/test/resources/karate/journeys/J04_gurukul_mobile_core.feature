@journey @gurukul @mobile
Feature: Gurukul student/parent mobile core journey

  Scenario: mobile student session exercises core API surfaces
    * def studentSession = call read('classpath:karate/common/session.feature') { role: 'ROLE_STUDENT', namePrefix: 'Gurukul Student' }
    * def uniq = java.lang.System.currentTimeMillis() + ''

    Given url baseUrl
    And path '/api/notifications/register'
    And header Authorization = studentSession.authHeader
    And request
      """
      {
        "tokenValue": "fcm-token-#(uniq)",
        "platform": "FCM",
        "deviceType": "ANDROID",
        "deviceId": "device-#(uniq)",
        "appVersion": "1.0.0"
      }
      """
    When method post
    Then status 200
    And match response.success == true

    Given url baseUrl
    And path '/api/me'
    And header Authorization = studentSession.authHeader
    When method get
    Then status 200
    And match response.success == true
    And match response.data.role == 'ROLE_STUDENT'

    Given url baseUrl
    And path '/api/me/home'
    And header Authorization = studentSession.authHeader
    When method get
    Then status 400
    And match response.success == false

    Given url baseUrl
    And path '/api/attendance/summary'
    And header Authorization = studentSession.authHeader
    And param studentId = 999999
    When method get
    Then status 200
    And match response.studentId == 999999

    Given url baseUrl
    And path '/api/homework'
    And header Authorization = studentSession.authHeader
    When method get
    Then status 200
    And match response == '#array'

    Given url baseUrl
    And path '/api/notices'
    And header Authorization = studentSession.authHeader
    When method get
    Then status 200
    And match response.success == true

    Given url baseUrl
    And path '/api/finance/students', 999999, 'ledger'
    And header Authorization = studentSession.authHeader
    When method get
    Then status 400

    Given url baseUrl
    And path '/api/calendar'
    And header Authorization = studentSession.authHeader
    When method get
    Then status 200
    And match response.success == false

    Given url baseUrl
    And path '/api/notifications/unregister'
    And header Authorization = studentSession.authHeader
    And request { tokenValue: 'fcm-token-#(uniq)' }
    When method post
    Then status 200
    And match response.success == true
