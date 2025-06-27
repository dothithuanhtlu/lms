# Test Submission Statistics API
# This script tests the submission statistics functionality

# Constants
$BASE_URL = "http://localhost:8080"
$LOGIN_URL = "$BASE_URL/auth/login"
$SUBMIT_URL = "$BASE_URL/api/submissions"
$STATISTICS_URL = "$BASE_URL/api/submissions/assignment"

# Headers
$headers = @{
    "Content-Type" = "application/json"
}

# Functions
function Login-User {
    param($email, $password)
    
    $loginData = @{
        email = $email
        password = $password
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri $LOGIN_URL -Method POST -Body $loginData -Headers $headers
        return $response.token
    }
    catch {
        Write-Host "Login failed for $email" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        return $null
    }
}

function Get-SubmissionStatistics {
    param($token, $assignmentId)
    
    $authHeaders = @{
        "Authorization" = "Bearer $token"
    }
    
    try {
        $response = Invoke-RestMethod -Uri "$STATISTICS_URL/$assignmentId/statistics" -Method GET -Headers $authHeaders
        return $response
    }
    catch {
        Write-Host "Failed to get submission statistics" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        return $null
    }
}

function Submit-Assignment {
    param($token, $assignmentId)
    
    $authHeaders = @{
        "Authorization" = "Bearer $token"
    }
    
    $submitData = @{
        assignmentId = $assignmentId
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri $SUBMIT_URL -Method POST -Body $submitData -Headers ($headers + $authHeaders)
        return $response
    }
    catch {
        Write-Host "Submission failed" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        return $null
    }
}

function Grade-Submission {
    param($token, $submissionId, $score, $feedback = "")
    
    $authHeaders = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    $gradeData = @{
        submissionId = $submissionId
        score = $score
        feedback = $feedback
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$SUBMIT_URL/$submissionId/grade" -Method PUT -Body $gradeData -Headers $authHeaders
        return $response
    }
    catch {
        Write-Host "Grading failed" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        return $null
    }
}

# Test scenarios
Write-Host "=== Testing Submission Statistics API ===" -ForegroundColor Green

# Test data - Update these based on your test data
$TEACHER_EMAIL = "teacher@example.com"
$TEACHER_PASSWORD = "password"
$STUDENT1_EMAIL = "student1@example.com"
$STUDENT1_PASSWORD = "password"
$STUDENT2_EMAIL = "student2@example.com"
$STUDENT2_PASSWORD = "password"
$ASSIGNMENT_ID = 1  # Update with actual assignment ID

Write-Host "`n1. Login as teacher" -ForegroundColor Yellow
$teacherToken = Login-User -email $TEACHER_EMAIL -password $TEACHER_PASSWORD
if (-not $teacherToken) {
    Write-Host "Cannot continue without teacher token" -ForegroundColor Red
    exit
}
Write-Host "Teacher logged in successfully" -ForegroundColor Green

Write-Host "`n2. Login as students" -ForegroundColor Yellow
$student1Token = Login-User -email $STUDENT1_EMAIL -password $STUDENT1_PASSWORD
$student2Token = Login-User -email $STUDENT2_EMAIL -password $STUDENT2_PASSWORD

if (-not $student1Token -or -not $student2Token) {
    Write-Host "Warning: Some student logins failed" -ForegroundColor Yellow
}

Write-Host "`n3. Get initial statistics (before submissions)" -ForegroundColor Yellow
$initialStats = Get-SubmissionStatistics -token $teacherToken -assignmentId $ASSIGNMENT_ID
if ($initialStats) {
    Write-Host "Initial Statistics:" -ForegroundColor Green
    Write-Host "  Total Students in Course: $($initialStats.totalStudentsInCourse)" -ForegroundColor White
    Write-Host "  Total Submissions: $($initialStats.totalSubmissions)" -ForegroundColor White
    Write-Host "  Students Not Submitted: $($initialStats.studentsNotSubmitted)" -ForegroundColor White
    Write-Host "  Submission Rate: $($initialStats.submissionRate)%" -ForegroundColor White
    Write-Host "  Grading Rate: $($initialStats.gradingRate)%" -ForegroundColor White
}

Write-Host "`n4. Create submissions for testing" -ForegroundColor Yellow

# Student 1 submits
if ($student1Token) {
    Write-Host "4.1. Student 1 submits assignment" -ForegroundColor Cyan
    $submission1 = Submit-Assignment -token $student1Token -assignmentId $ASSIGNMENT_ID
    if ($submission1) {
        Write-Host "Student 1 submission created with ID: $($submission1.id)" -ForegroundColor Green
        $submissionId1 = $submission1.id
        
        # Grade submission 1 with excellent score
        Start-Sleep -Seconds 1
        Write-Host "4.2. Teacher grades student 1 with excellent score (95)" -ForegroundColor Cyan
        $grade1 = Grade-Submission -token $teacherToken -submissionId $submissionId1 -score 95 -feedback "Excellent work!"
    }
}

# Student 2 submits  
if ($student2Token) {
    Write-Host "4.3. Student 2 submits assignment" -ForegroundColor Cyan
    $submission2 = Submit-Assignment -token $student2Token -assignmentId $ASSIGNMENT_ID
    if ($submission2) {
        Write-Host "Student 2 submission created with ID: $($submission2.id)" -ForegroundColor Green
        $submissionId2 = $submission2.id
        
        # Grade submission 2 with average score
        Start-Sleep -Seconds 1
        Write-Host "4.4. Teacher grades student 2 with average score (75)" -ForegroundColor Cyan
        $grade2 = Grade-Submission -token $teacherToken -submissionId $submissionId2 -score 75 -feedback "Good work, can improve."
    }
}

Write-Host "`n5. Get updated statistics (after submissions and grading)" -ForegroundColor Yellow
$updatedStats = Get-SubmissionStatistics -token $teacherToken -assignmentId $ASSIGNMENT_ID
if ($updatedStats) {
    Write-Host "Updated Statistics:" -ForegroundColor Green
    Write-Host "  Total Students in Course: $($updatedStats.totalStudentsInCourse)" -ForegroundColor White
    Write-Host "  Total Submissions: $($updatedStats.totalSubmissions)" -ForegroundColor White
    Write-Host "  Graded Submissions: $($updatedStats.gradedSubmissions)" -ForegroundColor White
    Write-Host "  Ungraded Submissions: $($updatedStats.ungradedSubmissions)" -ForegroundColor White
    Write-Host "  Late Submissions: $($updatedStats.lateSubmissions)" -ForegroundColor White
    Write-Host "  Students Not Submitted: $($updatedStats.studentsNotSubmitted)" -ForegroundColor White
    Write-Host "" -ForegroundColor White
    Write-Host "  Submission Rate: $($updatedStats.submissionRate)%" -ForegroundColor White
    Write-Host "  Grading Rate: $($updatedStats.gradingRate)%" -ForegroundColor White
    Write-Host "  Late Rate: $($updatedStats.lateRate)%" -ForegroundColor White
    Write-Host "" -ForegroundColor White
    Write-Host "  Average Score: $($updatedStats.averageScore)" -ForegroundColor White
    Write-Host "  Highest Score: $($updatedStats.highestScore)" -ForegroundColor White
    Write-Host "  Lowest Score: $($updatedStats.lowestScore)" -ForegroundColor White
    Write-Host "  Assignment Max Score: $($updatedStats.assignmentMaxScore)" -ForegroundColor White
    Write-Host "" -ForegroundColor White
    Write-Host "  Grade Distribution:" -ForegroundColor White
    Write-Host "    - Excellent (>=90%): $($updatedStats.excellentGrades)" -ForegroundColor White
    Write-Host "    - Good (80-89%): $($updatedStats.goodGrades)" -ForegroundColor White
    Write-Host "    - Average (70-79%): $($updatedStats.averageGrades)" -ForegroundColor White
    Write-Host "    - Below Average (<70%): $($updatedStats.belowAverageGrades)" -ForegroundColor White
}

Write-Host "`n6. Test statistics API with invalid assignment ID" -ForegroundColor Yellow
$invalidStats = Get-SubmissionStatistics -token $teacherToken -assignmentId 99999

Write-Host "`n7. Test statistics API without authentication" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$STATISTICS_URL/$ASSIGNMENT_ID/statistics" -Method GET
    Write-Host "WARNING: API should require authentication!" -ForegroundColor Red
}
catch {
    Write-Host "Good: API correctly requires authentication" -ForegroundColor Green
}

Write-Host "`n=== Submission Statistics Test Complete ===" -ForegroundColor Green

# Summary
Write-Host "`n=== Test Summary ===" -ForegroundColor Blue
if ($initialStats -and $updatedStats) {
    $submissionIncrease = $updatedStats.totalSubmissions - $initialStats.totalSubmissions
    $gradingIncrease = $updatedStats.gradedSubmissions - $initialStats.gradedSubmissions
    
    Write-Host "Submissions added during test: $submissionIncrease" -ForegroundColor White
    Write-Host "Gradings added during test: $gradingIncrease" -ForegroundColor White
    Write-Host "Final submission rate: $($updatedStats.submissionRate)%" -ForegroundColor White
    Write-Host "Final grading rate: $($updatedStats.gradingRate)%" -ForegroundColor White
}
