# Test API: Get Submittable Unsubmitted Assignment Count for Student
# This script tests the API to get count of assignments that student can still submit
# (assignments that are either not due yet OR overdue but allow late submission)

Write-Host "=== Testing Submittable Unsubmitted Assignment Count API ===" -ForegroundColor Cyan

# Configuration
$BASE_URL = "http://localhost:8080"
$STUDENT_ID = 1  # Change this to a valid student ID

# Teacher credentials (for accessing student data)
$TEACHER_USERNAME = "teacher01"
$TEACHER_PASSWORD = "123456"

# Student credentials (for testing self-access)
$STUDENT_USERNAME = "student01"
$STUDENT_PASSWORD = "123456"

Write-Host "`n🎯 This API counts assignments that student can still submit:" -ForegroundColor Magenta
Write-Host "   - Assignments not yet due" -ForegroundColor White
Write-Host "   - Assignments overdue but allow late submission" -ForegroundColor White
Write-Host "   - Excludes assignments that are overdue and don't allow late submission" -ForegroundColor White

try {
    # 1. Teacher Login
    Write-Host "`n1. Teacher Login..." -ForegroundColor Yellow
    $loginRequest = @{
        username = $TEACHER_USERNAME
        password = $TEACHER_PASSWORD
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri "$BASE_URL/api/auth/login" -Method POST -Body $loginRequest -ContentType "application/json"
    $teacherToken = $loginResponse.token
    Write-Host "✅ Teacher login successful" -ForegroundColor Green

    # 2. Test API with Teacher Token
    Write-Host "`n2. Getting Submittable Unsubmitted Assignment Count (Teacher Access)..." -ForegroundColor Yellow
    
    $headers = @{ "Authorization" = "Bearer $teacherToken" }
    
    $unsubmittedResponse = Invoke-RestMethod -Uri "$BASE_URL/api/submissions/student/$STUDENT_ID/unsubmitted-count" -Method GET -Headers $headers
    
    Write-Host "📊 Submittable Unsubmitted Assignment Count Response:" -ForegroundColor White
    Write-Host "  Student ID: $($unsubmittedResponse.studentId)" -ForegroundColor White
    Write-Host "  Submittable Unsubmitted Count: $($unsubmittedResponse.unsubmittedCount)" -ForegroundColor White
    Write-Host "  Message: $($unsubmittedResponse.message)" -ForegroundColor White

    # 3. Student Login (for self-access test)
    Write-Host "`n3. Student Login..." -ForegroundColor Yellow
    $studentLoginRequest = @{
        username = $STUDENT_USERNAME
        password = $STUDENT_PASSWORD
    } | ConvertTo-Json

    $studentLoginResponse = Invoke-RestMethod -Uri "$BASE_URL/api/auth/login" -Method POST -Body $studentLoginRequest -ContentType "application/json"
    $studentToken = $studentLoginResponse.token
    Write-Host "✅ Student login successful" -ForegroundColor Green

    # 4. Test API with Student Token (should work for their own data)
    Write-Host "`n4. Getting Submittable Unsubmitted Assignment Count (Student Self-Access)..." -ForegroundColor Yellow
    
    $studentHeaders = @{ "Authorization" = "Bearer $studentToken" }
    
    try {
        $studentUnsubmittedResponse = Invoke-RestMethod -Uri "$BASE_URL/api/submissions/student/$STUDENT_ID/unsubmitted-count" -Method GET -Headers $studentHeaders
        
        Write-Host "📊 Student Self-Access Response:" -ForegroundColor White
        Write-Host "  Student ID: $($studentUnsubmittedResponse.studentId)" -ForegroundColor White
        Write-Host "  Unsubmitted Count: $($studentUnsubmittedResponse.unsubmittedCount)" -ForegroundColor White
        Write-Host "  Message: $($studentUnsubmittedResponse.message)" -ForegroundColor White
    } catch {
        Write-Host "⚠️ Student access may be restricted: $($_.Exception.Message)" -ForegroundColor Yellow
    }

    # 5. Get Student Details (if available)
    Write-Host "`n5. Getting Student Information..." -ForegroundColor Yellow
    
    try {
        $studentDetailsResponse = Invoke-RestMethod -Uri "$BASE_URL/api/users/$STUDENT_ID" -Method GET -Headers $headers
        Write-Host "👤 Student Details:" -ForegroundColor White
        Write-Host "  Name: $($studentDetailsResponse.fullName)" -ForegroundColor White
        Write-Host "  User Code: $($studentDetailsResponse.userCode)" -ForegroundColor White
        Write-Host "  Email: $($studentDetailsResponse.email)" -ForegroundColor White
    } catch {
        Write-Host "⚠️ Could not get student details: $($_.Exception.Message)" -ForegroundColor Yellow
    }

    # 6. Get Student's Submitted Assignments (for comparison)
    Write-Host "`n6. Getting Student's Submitted Assignments..." -ForegroundColor Yellow
    
    try {
        $submissionsResponse = Invoke-RestMethod -Uri "$BASE_URL/api/submissions/my-submissions?pageSize=100" -Method GET -Headers $studentHeaders
        Write-Host "📝 Student's Submissions:" -ForegroundColor White
        Write-Host "  Total Submitted: $($submissionsResponse.meta.total)" -ForegroundColor White
        
        if ($submissionsResponse.result.Count -gt 0) {
            Write-Host "  Recent Submissions:" -ForegroundColor Gray
            foreach ($submission in $submissionsResponse.result | Select-Object -First 5) {
                Write-Host "    - Assignment: $($submission.assignmentTitle) | Score: $($submission.score)" -ForegroundColor Gray
            }
        }
    } catch {
        Write-Host "⚠️ Could not get student submissions: $($_.Exception.Message)" -ForegroundColor Yellow
    }

    # 7. Test Error Cases
    Write-Host "`n7. Testing Error Cases..." -ForegroundColor Yellow
    
    # Test with invalid student ID
    try {
        Write-Host "Testing with invalid student ID (999999)..." -ForegroundColor Gray
        $invalidResponse = Invoke-RestMethod -Uri "$BASE_URL/api/submissions/student/999999/unsubmitted-count" -Method GET -Headers $headers
        Write-Host "Unexpected success with invalid ID" -ForegroundColor Yellow
    } catch {
        Write-Host "✅ Expected error for invalid student ID: $($_.Exception.Message)" -ForegroundColor Green
    }

} catch {
    Write-Host "❌ Error occurred: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $streamReader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        $errorBody = $streamReader.ReadToEnd()
        Write-Host "Error details: $errorBody" -ForegroundColor Red
        $streamReader.Close()
    }
}

Write-Host "`n=== Submittable Unsubmitted Assignment Count API Summary ===" -ForegroundColor Cyan
Write-Host "🎯 Purpose: Get count of assignments a student can still submit" -ForegroundColor Green
Write-Host "📋 Features:" -ForegroundColor Yellow
Write-Host "  • Only counts published assignments from enrolled courses" -ForegroundColor White
Write-Host "  • Excludes assignments already submitted by the student" -ForegroundColor White
Write-Host "  • Includes assignments not yet due" -ForegroundColor White
Write-Host "  • Includes assignments overdue but allow late submission" -ForegroundColor White
Write-Host "  • Excludes assignments overdue that don't allow late submission" -ForegroundColor White
Write-Host "  • Returns count with student ID and descriptive message" -ForegroundColor White
Write-Host "⚡ Use Cases:" -ForegroundColor Yellow
Write-Host "  • Student dashboard showing actionable pending work" -ForegroundColor White
Write-Host "  • Teacher monitoring which assignments students can still complete" -ForegroundColor White
Write-Host "  • Academic advisors focusing on recoverable assignments" -ForegroundColor White
Write-Host "  • Study planning and time management tools" -ForegroundColor White

Write-Host "`n🔗 API Endpoint: GET /api/submissions/student/{studentId}/unsubmitted-count" -ForegroundColor Magenta
Write-Host "✨ Logic: (NOT DUE YET) OR (OVERDUE AND ALLOWS LATE)" -ForegroundColor Yellow

Write-Host "`n🎉 Test Completed!" -ForegroundColor Green
