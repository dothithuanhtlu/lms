# Test Grade Submission API
# This script tests the grading functionality for submissions

# Constants
$BASE_URL = "http://localhost:8080"
$LOGIN_URL = "$BASE_URL/auth/login"
$SUBMIT_URL = "$BASE_URL/api/submissions"
$GRADE_URL = "$BASE_URL/api/submissions"

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

function Submit-Assignment {
    param($token, $assignmentId, $filePaths = @())
    
    $authHeaders = @{
        "Authorization" = "Bearer $token"
    }
    
    if ($filePaths.Count -eq 0) {
        # Submit without files
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
    else {
        # Submit with files (using multipart/form-data)
        try {
            $form = @{
                assignmentId = $assignmentId
            }
            
            for ($i = 0; $i -lt $filePaths.Count; $i++) {
                $form["files"] = Get-Item $filePaths[$i]
            }
            
            $response = Invoke-RestMethod -Uri $SUBMIT_URL -Method POST -Form $form -Headers @{"Authorization" = "Bearer $token"}
            return $response
        }
        catch {
            Write-Host "Submission with files failed" -ForegroundColor Red
            Write-Host $_.Exception.Message -ForegroundColor Red
            return $null
        }
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
        $response = Invoke-RestMethod -Uri "$GRADE_URL/$submissionId/grade" -Method PUT -Body $gradeData -Headers $authHeaders
        return $response
    }
    catch {
        Write-Host "Grading failed" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        return $null
    }
}

function Get-Submission {
    param($token, $submissionId)
    
    $authHeaders = @{
        "Authorization" = "Bearer $token"
    }
    
    try {
        $response = Invoke-RestMethod -Uri "$GRADE_URL/$submissionId" -Method GET -Headers $authHeaders
        return $response
    }
    catch {
        Write-Host "Failed to get submission" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        return $null
    }
}

function Get-AssignmentSubmissions {
    param($token, $assignmentId)
    
    $authHeaders = @{
        "Authorization" = "Bearer $token"
    }
    
    try {
        $response = Invoke-RestMethod -Uri "$SUBMIT_URL/assignment/$assignmentId" -Method GET -Headers $authHeaders
        return $response
    }
    catch {
        Write-Host "Failed to get assignment submissions" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        return $null
    }
}

# Test scenarios
Write-Host "=== Testing Grade Submission API ===" -ForegroundColor Green

# Test data - Update these based on your test data
$TEACHER_EMAIL = "teacher@example.com"
$TEACHER_PASSWORD = "password"
$STUDENT_EMAIL = "student@example.com"
$STUDENT_PASSWORD = "password"
$ASSIGNMENT_ID = 1  # Update with actual assignment ID

Write-Host "`n1. Login as teacher" -ForegroundColor Yellow
$teacherToken = Login-User -email $TEACHER_EMAIL -password $TEACHER_PASSWORD
if (-not $teacherToken) {
    Write-Host "Cannot continue without teacher token" -ForegroundColor Red
    exit
}
Write-Host "Teacher logged in successfully" -ForegroundColor Green

Write-Host "`n2. Login as student" -ForegroundColor Yellow
$studentToken = Login-User -email $STUDENT_EMAIL -password $STUDENT_PASSWORD
if (-not $studentToken) {
    Write-Host "Cannot continue without student token" -ForegroundColor Red
    exit
}
Write-Host "Student logged in successfully" -ForegroundColor Green

Write-Host "`n3. Student submits assignment" -ForegroundColor Yellow
$submission = Submit-Assignment -token $studentToken -assignmentId $ASSIGNMENT_ID
if (-not $submission) {
    Write-Host "Cannot continue without submission" -ForegroundColor Red
    exit
}
Write-Host "Submission created with ID: $($submission.id)" -ForegroundColor Green
$submissionId = $submission.id

Write-Host "`n4. Teacher grades the submission" -ForegroundColor Yellow

# Test case 1: Valid grade
Write-Host "4.1. Testing valid grade (85.5 points)" -ForegroundColor Cyan
$gradeResult = Grade-Submission -token $teacherToken -submissionId $submissionId -score 85.5 -feedback "Good work! Well structured code."
if ($gradeResult) {
    Write-Host "Grading successful!" -ForegroundColor Green
    Write-Host "Score: $($gradeResult.score)" -ForegroundColor Green
    Write-Host "Feedback: $($gradeResult.feedback)" -ForegroundColor Green
    Write-Host "Status: $($gradeResult.status)" -ForegroundColor Green
    Write-Host "Graded At: $($gradeResult.gradedAt)" -ForegroundColor Green
}

# Test case 2: Invalid grade (exceeding max score)
Write-Host "`n4.2. Testing invalid grade (exceeding max score)" -ForegroundColor Cyan
$invalidGrade = Grade-Submission -token $teacherToken -submissionId $submissionId -score 999 -feedback "This should fail"

# Test case 3: Negative score
Write-Host "`n4.3. Testing negative score" -ForegroundColor Cyan
$negativeGrade = Grade-Submission -token $teacherToken -submissionId $submissionId -score -5 -feedback "This should fail"

Write-Host "`n5. Get updated submission details" -ForegroundColor Yellow
$updatedSubmission = Get-Submission -token $teacherToken -submissionId $submissionId
if ($updatedSubmission) {
    Write-Host "Submission Details:" -ForegroundColor Green
    Write-Host "ID: $($updatedSubmission.id)" -ForegroundColor White
    Write-Host "Student: $($updatedSubmission.studentName)" -ForegroundColor White
    Write-Host "Assignment: $($updatedSubmission.assignmentTitle)" -ForegroundColor White
    Write-Host "Score: $($updatedSubmission.score)/$($updatedSubmission.assignmentMaxScore)" -ForegroundColor White
    Write-Host "Status: $($updatedSubmission.status)" -ForegroundColor White
    Write-Host "Is Late: $($updatedSubmission.isLate)" -ForegroundColor White
    Write-Host "Submitted At: $($updatedSubmission.submittedAt)" -ForegroundColor White
    Write-Host "Graded At: $($updatedSubmission.gradedAt)" -ForegroundColor White
    Write-Host "Feedback: $($updatedSubmission.feedback)" -ForegroundColor White
}

Write-Host "`n6. Get all submissions for assignment (teacher view)" -ForegroundColor Yellow
$allSubmissions = Get-AssignmentSubmissions -token $teacherToken -assignmentId $ASSIGNMENT_ID
if ($allSubmissions) {
    Write-Host "Assignment has $($allSubmissions.count) submissions:" -ForegroundColor Green
    foreach ($sub in $allSubmissions) {
        Write-Host "  - ID: $($sub.id), Student: $($sub.studentName), Score: $($sub.score), Status: $($sub.status)" -ForegroundColor White
    }
}

Write-Host "`n7. Test re-grading submission" -ForegroundColor Yellow
$regrade = Grade-Submission -token $teacherToken -submissionId $submissionId -score 92.0 -feedback "Updated grade after review."
if ($regrade) {
    Write-Host "Re-grading successful!" -ForegroundColor Green
    Write-Host "New Score: $($regrade.score)" -ForegroundColor Green
    Write-Host "New Feedback: $($regrade.feedback)" -ForegroundColor Green
}

Write-Host "`n8. Test student trying to grade (should fail)" -ForegroundColor Yellow
$studentGrade = Grade-Submission -token $studentToken -submissionId $submissionId -score 100 -feedback "Student trying to grade"

Write-Host "`n=== Grade Submission Test Complete ===" -ForegroundColor Green
