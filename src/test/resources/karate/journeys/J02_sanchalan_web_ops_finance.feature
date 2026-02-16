@journey @sanchalan @web
Feature: Sanchalan web school-ops and finance journey

  Scenario: school admin runs core operations from setup to fee collection
    * def platform = call read('classpath:karate/common/platform-auth.feature')
    * def admin = call read('classpath:karate/common/session.feature') { role: 'ROLE_ADMIN', namePrefix: 'Sanchalan Admin' }
    * def uniq = java.lang.System.currentTimeMillis() + ''
    * def LocalDate = Java.type('java.time.LocalDate')
    * def today = LocalDate.now().toString()

    Given url baseUrl
    And path '/api/platform/v1/schools'
    And header Authorization = platform.authHeader
    And request { schoolCode: 'WEB-#(uniq)', name: 'Web Ops #(uniq)', registrationNumber: 'WEBREG-#(uniq)', timezone: 'Asia/Kolkata', board: 'CBSE' }
    When method post
    Then status 200
    * def schoolId = response.id

    Given url baseUrl
    And path '/api/platform/v1/schools', schoolId, 'academic', 'classes'
    And header Authorization = platform.authHeader
    And request { name: 'Class 10-B' }
    When method post
    Then status 200
    * def classId = response.id

    Given url baseUrl
    And path '/api/academic/subjects'
    And header Authorization = admin.authHeader
    And request { name: 'Science #(uniq)', code: 'SCI-#(uniq)' }
    When method post
    Then status 200
    * def subjectId = response.id

    Given url baseUrl
    And path '/api/academics/teachers'
    And header Authorization = admin.authHeader
    And request
      """
      {
        "name": "Teacher #(uniq)",
        "email": "teacher.#(uniq)@test.local",
        "phone": "9000000001",
        "qualification": "B.Ed",
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
        "firstName": "Student",
        "lastName": "#(uniq)",
        "rollNo": 11,
        "gender": "MALE",
        "guardianName": "Guardian #(uniq)",
        "guardianMobile": "9000000002",
        "classId": #(classId)
      }
      """
    When method post
    Then status 200
    * def studentId = response.id

    Given url baseUrl
    And path '/api/attendance/bulk'
    And header Authorization = admin.authHeader
    And request
      """
      {
        "classId": #(classId),
        "date": "#(today)",
        "markedBy": "Sanchalan Admin",
        "attendances": [
          {
            "studentId": #(studentId),
            "status": "PRESENT",
            "remarks": "On time"
          }
        ]
      }
      """
    When method post
    Then status 200
    And match response.markedCount >= 1

    Given url baseUrl
    And path '/api/attendance/class', classId, 'date', today
    And header Authorization = admin.authHeader
    When method get
    Then status 200
    And match response.classId == classId

    Given url baseUrl
    And path '/api/notices'
    And header Authorization = admin.authHeader
    And request { title: 'Exam Notice #(uniq)', content: 'Prepare well', priority: 'HIGH', targetRole: 'ALL', publishDate: '#(today)' }
    When method post
    Then status 200
    And match response.success == true

    Given url baseUrl
    And path '/api/notices'
    And header Authorization = admin.authHeader
    When method get
    Then status 200
    And match response.success == true

    Given url baseUrl
    And path '/api/finance/categories'
    And header Authorization = admin.authHeader
    And request { name: 'Tuition-Web-#(uniq)', description: 'Core annual fee', isMandatory: true }
    When method post
    Then status 200
    * def categoryId = response.id

    Given url baseUrl
    And path '/api/finance/structures'
    And header Authorization = admin.authHeader
    And request
      """
      {
        "name": "Structure-Web-#(uniq)",
        "academicYear": "2026-27",
        "frequency": "MONTHLY",
        "gracePeriodDays": 7,
        "items": [
          { "categoryId": #(categoryId), "amount": 1200 }
        ]
      }
      """
    When method post
    Then status 200
    * def structureId = response.id

    Given url baseUrl
    And path '/api/finance/structures', structureId, 'assign'
    And header Authorization = admin.authHeader
    And param classId = classId
    When method post
    Then status 200

    Given url baseUrl
    And path '/api/finance/students', studentId, 'ledger'
    And header Authorization = admin.authHeader
    When method get
    Then status 200
    * def pendingBalance = response.pendingBalance
    * match pendingBalance == '#number'

    * def payAmount = pendingBalance > 0 ? pendingBalance : 1
    Given url baseUrl
    And path '/api/finance/transactions'
    And header Authorization = admin.authHeader
    And request { studentId: #(studentId), amount: #(payAmount), paymentMethod: 'UPI', transactionReference: 'TXN-#(uniq)' }
    When method post
    Then status 200
    And match response.status == 'SUCCESS'
