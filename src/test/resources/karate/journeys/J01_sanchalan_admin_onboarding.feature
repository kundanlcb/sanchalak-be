@journey @sanchalan-admin @platform
Feature: Sanchalan Admin onboarding journey

  Scenario: platform team onboards a new school end-to-end
    * def platform = call read('classpath:karate/common/platform-auth.feature')
    * def uniq = java.lang.System.currentTimeMillis() + ''

    Given url baseUrl
    And path '/api/platform/v1/schools'
    And header Authorization = platform.authHeader
    And request
      """
      {
        "schoolCode": "SCH-#(uniq)",
        "name": "Journey School #(uniq)",
        "registrationNumber": "REG-#(uniq)",
        "timezone": "Asia/Kolkata",
        "board": "CBSE",
        "contactInfo": {
          "contactPerson": "Ops Manager",
          "contactNumber": "9999999999",
          "contactEmail": "ops#(uniq)@school.test",
          "address": "Main Road",
          "city": "Indore",
          "state": "MP",
          "postalCode": "452001",
          "country": "India"
        }
      }
      """
    When method post
    Then status 200
    * def schoolId = response.id
    * match schoolId == '#string'

    Given url baseUrl
    And path '/api/platform/v1/schools', schoolId, 'status-transition'
    And header Authorization = platform.authHeader
    And request 'ACTIVE'
    When method post
    Then status 200
    And match response.status == 'ACTIVE'

    Given url baseUrl
    And path '/api/platform/v1/schools', schoolId, 'academic', 'years'
    And header Authorization = platform.authHeader
    And request { name: '2026-27', startDate: '2026-04-01', endDate: '2027-03-31', current: true }
    When method post
    Then status 200

    Given url baseUrl
    And path '/api/platform/v1/schools', schoolId, 'academic', 'classes'
    And header Authorization = platform.authHeader
    And request { name: 'Class 8-A' }
    When method post
    Then status 200

    Given url baseUrl
    And path '/api/platform/v1/schools', schoolId, 'academic', 'subjects'
    And header Authorization = platform.authHeader
    And request { name: 'Mathematics', code: 'MATH-8' }
    When method post
    Then status 200

    Given url baseUrl
    And path '/api/platform/v1/schools', schoolId, 'operations'
    And header Authorization = platform.authHeader
    And request { attendanceEnabled: true, noticesEnabled: true, routineEnabled: true, saturdayIsWorking: false }
    When method post
    Then status 200

    Given url baseUrl
    And path '/api/platform/v1/schools', schoolId, 'bootstrap-admin'
    And header Authorization = platform.authHeader
    And request
      """
      {
        "name": "School Admin #(uniq)",
        "email": "school.admin.#(uniq)@test.local",
        "password": "Test@123456",
        "mobileNumber": "9876543210"
      }
      """
    When method post
    Then status 200

    Given url baseUrl
    And path '/api/platform/v1/subscriptions/plans'
    And header Authorization = platform.authHeader
    And request
      """
      {
        "name": "Starter-#(uniq)",
        "price": 19999,
        "durationMonths": 12,
        "maxStudents": 1500,
        "features": "[\"attendance\",\"fees\",\"notices\"]"
      }
      """
    When method post
    Then status 200
    * def planId = response.id
    * match planId == '#string'

    Given url baseUrl
    And path '/api/platform/v1/subscriptions/assign', schoolId
    And header Authorization = platform.authHeader
    And param planId = planId
    When method post
    Then status 200
    And match response.status == 'ACTIVE'

    Given url baseUrl
    And path '/api/platform/v1/schools', schoolId, 'finance', 'categories'
    And header Authorization = platform.authHeader
    And request { name: 'Tuition-#(uniq)', description: 'Annual tuition fee', isMandatory: true }
    When method post
    Then status 200

    Given url baseUrl
    And path '/api/platform/v1/schools', schoolId, 'finance', 'structures'
    And header Authorization = platform.authHeader
    And request { name: 'Default-#(uniq)', academicYear: '2026-27', frequency: 'MONTHLY', gracePeriodDays: 7 }
    When method post
    Then status 200

    * def csvData = 'first_name,last_name,roll_no,gender,guardian_name,guardian_mobile,class_name\nJohn,Doe,1,MALE,Parent One,9876500000,Class 8-A'
    Given url baseUrl
    And path '/api/platform/v1/schools', schoolId, 'imports'
    And header Authorization = platform.authHeader
    And param type = 'STUDENT'
    And multipart file file = { value: '#(csvData)', filename: 'students.csv', contentType: 'text/csv' }
    When method post
    Then status 200
    And match ['PENDING','PROCESSING','COMPLETED'] contains response.status

    Given url baseUrl
    And path '/api/platform/v1/schools', schoolId, 'onboarding-status'
    And header Authorization = platform.authHeader
    When method get
    Then status 200
    And match response.profileComplete == true
    And match response.academicYearCreated == true
    And match response.adminUserInvited == true
    And match response.subscriptionActive == true
