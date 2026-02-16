@journey @guru @teacher
Feature: Guru teacher app daily journey

  Scenario: teacher handles timetable, attendance, homework and notices in one flow
    * def platform = call read('classpath:karate/common/platform-auth.feature')
    * def admin = call read('classpath:karate/common/session.feature') { role: 'ROLE_ADMIN', namePrefix: 'Setup Admin' }
    * def teacherSession = call read('classpath:karate/common/session.feature') { role: 'ROLE_TEACHER', namePrefix: 'Guru Teacher User' }
    * def uniq = java.lang.System.currentTimeMillis() + ''
    * def LocalDate = Java.type('java.time.LocalDate')
    * def today = LocalDate.now().toString()
    * def tomorrow = LocalDate.now().plusDays(1).toString()

    Given url baseUrl
    And path '/api/platform/v1/schools'
    And header Authorization = platform.authHeader
    And request { schoolCode: 'GURU-#(uniq)', name: 'Guru School #(uniq)', registrationNumber: 'GREG-#(uniq)', timezone: 'Asia/Kolkata', board: 'CBSE' }
    When method post
    Then status 200
    * def schoolId = response.id

    Given url baseUrl
    And path '/api/platform/v1/schools', schoolId, 'academic', 'classes'
    And header Authorization = platform.authHeader
    And request { name: 'Class 9-C' }
    When method post
    Then status 200
    * def classId = response.id

    Given url baseUrl
    And path '/api/academic/subjects'
    And header Authorization = admin.authHeader
    And request { name: 'English #(uniq)', code: 'ENG-#(uniq)' }
    When method post
    Then status 200
    * def subjectId = response.id

    Given url baseUrl
    And path '/api/academics/teachers'
    And header Authorization = admin.authHeader
    And request
      """
      {
        "name": "Teacher Guru #(uniq)",
        "email": "guru.teacher.#(uniq)@test.local",
        "phone": "9000000010",
        "qualification": "M.Ed",
        "specializationIds": [#(subjectId)]
      }
      """
    When method post
    Then status 200
    * def teacherId = response.id

    Given url baseUrl
    And path '/api/academics/students'
    And header Authorization = admin.authHeader
    And request
      """
      {
        "firstName": "Learner",
        "lastName": "#(uniq)",
        "rollNo": 17,
        "gender": "FEMALE",
        "guardianName": "Parent #(uniq)",
        "guardianMobile": "9000000011",
        "classId": #(classId)
      }
      """
    When method post
    Then status 200
    * def studentId = response.id

    Given url baseUrl
    And path '/api/academics/routine'
    And header Authorization = admin.authHeader
    And request
      """
      {
        "classId": #(classId),
        "subjectId": #(subjectId),
        "teacherId": #(teacherId),
        "dayOfWeek": "MONDAY",
        "period": 1,
        "startTime": "09:00",
        "endTime": "09:45"
      }
      """
    When method post
    Then status 200

    Given url baseUrl
    And path '/api/academics/routine'
    And header Authorization = teacherSession.authHeader
    And param classId = classId
    When method get
    Then status 200
    And match response == '#array'

    Given url baseUrl
    And path '/api/attendance/bulk'
    And header Authorization = teacherSession.authHeader
    And request
      """
      {
        "classId": #(classId),
        "date": "#(today)",
        "markedBy": "Teacher Mobile",
        "attendances": [
          {
            "studentId": #(studentId),
            "status": "PRESENT"
          }
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
        "title": "Read Chapter 1",
        "description": "Complete exercise 1-5",
        "dueDate": "#(tomorrow)"
      }
      """
    When method post
    Then status 200

    Given url baseUrl
    And path '/api/homework'
    And header Authorization = teacherSession.authHeader
    When method get
    Then status 200
    And match response == '#array'

    Given url baseUrl
    And path '/api/notices'
    And header Authorization = teacherSession.authHeader
    And request { title: 'Class Reminder #(uniq)', content: 'Bring notebook', priority: 'MEDIUM', targetRole: 'STUDENT', publishDate: '#(today)' }
    When method post
    Then status 200
    And match response.success == true

    Given url baseUrl
    And path '/api/notices'
    And header Authorization = teacherSession.authHeader
    And param onlyRecent = true
    When method get
    Then status 200
    And match response.success == true
