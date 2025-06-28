# Test API: Get All Submissions by Assignment ID (Teacher View)
# This script tests the teacher's ability to view all student submissions for a specific assignment

# Test API: GET /api/submissions/assignment/{assignmentId}
Write-Host "=== Testing Get All Submissions by Assignment ID ===" -ForegroundColor Cyan

# Configuration
$BASE_URL = "http://localhost:8080"
$ASSIGNMENT_ID = 1  # Change this to a valid assignment ID

# Teacher credentials (adjust as needed)
$TEACHER_USERNAME = "teacher01"
$TEACHER_PASSWORD = "123456"

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

    # 2. Get All Submissions for Assignment (Default pagination)
    Write-Host "`n2. Get All Submissions for Assignment ID: $ASSIGNMENT_ID (Page 0, Size 20)..." -ForegroundColor Yellow
    
    $headers = @{ "Authorization" = "Bearer $teacherToken" }
    $submissionsResponse = Invoke-RestMethod -Uri "$BASE_URL/api/submissions/assignment/$ASSIGNMENT_ID" -Method GET -Headers $headers

    Write-Host "✅ API Response:" -ForegroundColor Green
    Write-Host ($submissionsResponse | ConvertTo-Json -Depth 3) -ForegroundColor White

    # 3. Get All Submissions with Custom Pagination
    Write-Host "`n3. Get All Submissions with Custom Pagination (Page 0, Size 5)..." -ForegroundColor Yellow
    
    $customPaginationUri = "$BASE_URL/api/submissions/assignment/$ASSIGNMENT_ID" + "?page=0&size=5"
    $customSubmissionsResponse = Invoke-RestMethod -Uri $customPaginationUri -Method GET -Headers $headers

    Write-Host "✅ Custom Pagination Response:" -ForegroundColor Green
    Write-Host ($customSubmissionsResponse | ConvertTo-Json -Depth 3) -ForegroundColor White

    # 4. Display Summary
    Write-Host "`n=== Summary ===" -ForegroundColor Cyan
    Write-Host "Total submissions found: $($submissionsResponse.Count)" -ForegroundColor Yellow
    
    if ($submissionsResponse.Count -gt 0) {
        Write-Host "`nSubmission Details:" -ForegroundColor Yellow
        foreach ($submission in $submissionsResponse) {
            Write-Host "- Student: $($submission.studentName)" -ForegroundColor White
            Write-Host "  Status: $($submission.status)" -ForegroundColor White
            Write-Host "  Score: $($submission.score)" -ForegroundColor White
            Write-Host "  Submitted At: $($submission.submittedAt)" -ForegroundColor White
            Write-Host "  Files: $($submission.documents.Count)" -ForegroundColor White
            Write-Host ""
        }
    } else {
        Write-Host "No submissions found for this assignment." -ForegroundColor Yellow
    }

    # 5. Test with Invalid Assignment ID
    Write-Host "`n4. Test with Invalid Assignment ID (999999)..." -ForegroundColor Yellow
    
    try {
        $invalidResponse = Invoke-RestMethod -Uri "$BASE_URL/api/submissions/assignment/999999" -Method GET -Headers $headers
        Write-Host "✅ Response for invalid assignment ID:" -ForegroundColor Green
        Write-Host ($invalidResponse | ConvertTo-Json -Depth 2) -ForegroundColor White
    } catch {
        Write-Host "❌ Error with invalid assignment ID (Expected): $($_.Exception.Message)" -ForegroundColor Red
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

Write-Host "`n=== Test Completed ===" -ForegroundColor Cyan
