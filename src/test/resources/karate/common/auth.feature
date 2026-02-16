Feature: Backward-compatible auth helper for smoke tests

  Scenario: create and login student user
    * def session = call read('classpath:karate/common/session.feature') { role: 'ROLE_STUDENT', namePrefix: 'Karate Student' }
    * def token = session.token
    * def authHeader = session.authHeader
