@smoke @regression @attendance
Feature: Attendance smoke checks

  Scenario: attendance summary requires authentication
    Given url baseUrl
    And path '/api/attendance/summary'
    And param studentId = 999999
    When method get
    Then status 401

  Scenario: authenticated user can access attendance summary endpoint
    * def session = call read('classpath:karate/common/auth.feature')
    Given url baseUrl
    And path '/api/attendance/summary'
    And param studentId = 999999
    And header Authorization = session.authHeader
    When method get
    Then status 200
    And match response.studentId == 999999
    And match response.totalDays == '#number'
    And match response.presentDays == '#number'
