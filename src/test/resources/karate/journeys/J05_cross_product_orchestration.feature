@journey @cross-product @sanchalan @guru @gurukul @sanchalan-admin
Feature: Cross-product orchestration journey

  Scenario: platform onboarding drives downstream web, teacher and mobile usage
    * def platform = call read('classpath:karate/common/platform-auth.feature')
    * def webAdmin = call read('classpath:karate/common/session.feature') { role: 'ROLE_ADMIN', namePrefix: 'Cross Web Admin' }
    * def teacherSession = call read('classpath:karate/common/session.feature') { role: 'ROLE_TEACHER', namePrefix: 'Cross Teacher' }
    * def studentSession = call read('classpath:karate/common/session.feature') { role: 'ROLE_STUDENT', namePrefix: 'Cross Student' }
    * def uniq = java.lang.System.currentTimeMillis() + ''
    * def LocalDate = Java.type('java.time.LocalDate')
    * def today = LocalDate.now().toString()
    * def tomorrow = LocalDate.now().plusDays(1).toString()

    # sanchalan-admin: school onboarding base
    Given url baseUrl
    And path '/api/platform/v1/schools'
    And header Authorization = platform.authHeader
    And request { schoolCode: 'XPROD-#(uniq)', name: 'Cross Product #(uniq)', registrationNumber: 'XREG-#(uniq)', timezone: 'Asia/Kolkata', board: 'CBSE' }
    When method post
    Then status 200
    * def schoolId = response.id

    Given url baseUrl
    And path '/api/platform/v1/schools', schoolId, 'academic', 'classes'
    And header Authorization = platform.authHeader
    And request { name: 'Class 7-A' }
    When method post
    Then status 200
    * def classId = response.id

    # sanchalan: school admin setup
    Given url baseUrl
    And path '/api/academic/subjects'
    And header Authorization = webAdmin.authHeader
    And request { name: 'Math Cross #(uniq)', code: 'MC-#(uniq)' }
    When method post
    Then status 200
    * def subjectId = response.id

    Given url baseUrl
    And path '/api/academics/teachers'
    And header Authorization = webAdmin.authHeader
    And request
      """
      {
        "name": "Cross Teacher #(uniq)",
        "email": "cross.teacher.#(uniq)@test.local",
        "phone": "9000000020",
        "specializationIds": [#(subjectId)]
      }
      """
    When method post
    Then status 200
    * def teacherId = response.id

    Given url baseUrl
    And path '/api/academics/students'
    And header Authorization = webAdmin.authHeader
    And request
      """
      {
        "firstName": "Cross",
        "lastName": "Learner",
        "rollNo": 21,
        "gender": "MALE",
        "guardianName": "Cross Parent",
        "guardianMobile": "9000000021",
        "classId": #(classId)
      }
      """
    When method post
    Then status 200
    * def studentId = response.id

    # guru: teacher publishes homework and attendance
    Given url baseUrl
    And path '/api/attendance/bulk'
    And header Authorization = teacherSession.authHeader
    And request
      """
      {
        "classId": #(classId),
        "date": "#(today)",
        "markedBy": "Cross Teacher",
        "attendances": [
          { "studentId": #(studentId), "status": "PRESENT" }
        ]
      }
      """
    When method post
    Then status 200

    Given url baseUrl
    And path '/api/homework'
    And header Authorization = teacherSession.authHeader
    And request
      """
      {
        "classId": #(classId),
        "subjectId": #(subjectId),
        "teacherId": #(teacherId),
        "title": "Cross Homework",
        "description": "Solve 5 problems",
        "dueDate": "#(tomorrow)"
      }
      """
    When method post
    Then status 200

    Given url baseUrl
    And path '/api/notices'
    And header Authorization = webAdmin.authHeader
    And request { title: 'Cross Notice #(uniq)', content: 'Class update', priority: 'HIGH', targetRole: 'ALL', publishDate: '#(today)' }
    When method post
    Then status 200
    And match response.success == true

    # gurukul: student consumes exposed mobile-relevant APIs
    Given url baseUrl
    And path '/api/attendance/summary'
    And header Authorization = studentSession.authHeader
    And param studentId = studentId
    When method get
    Then status 200
    And match response.studentId == studentId

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
