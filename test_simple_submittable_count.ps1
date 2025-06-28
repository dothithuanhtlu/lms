# Test API: Get Submittable Unsubmitted Assignment Count for Student
Write-Host "=== Testing Submittable Unsubmitted Assignment Count API ===" -ForegroundColor Cyan

# Configuration
$BASE_URL = "http://localhost:8080"
$STUDENT_ID = 1

# Teacher credentials
$TEACHER_USERNAME = "teacher01"
$TEACHER_PASSWORD = "123456"

Write-Host "`nThis API counts assignments that student can still submit:" -ForegroundColor Magenta
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
    Write-Host "Teacher login successful" -ForegroundColor Green

    # 2. Test API
    Write-Host "`n2. Getting Submittable Unsubmitted Assignment Count..." -ForegroundColor Yellow
    
    $headers = @{ "Authorization" = "Bearer $teacherToken" }
    
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/submissions/student/$STUDENT_ID/unsubmitted-count" -Method GET -Headers $headers
    
    Write-Host "API Response:" -ForegroundColor White
    Write-Host "  Student ID: $($response.studentId)" -ForegroundColor White
    Write-Host "  Submittable Count: $($response.unsubmittedCount)" -ForegroundColor White
    Write-Host "  Message: $($response.message)" -ForegroundColor White

    # 3. Test Error Case
    Write-Host "`n3. Testing Error Cases..." -ForegroundColor Yellow
    
    try {
        Write-Host "Testing with invalid student ID (999999)..." -ForegroundColor Gray
        $invalidResponse = Invoke-RestMethod -Uri "$BASE_URL/api/submissions/student/999999/unsubmitted-count" -Method GET -Headers $headers
        Write-Host "Unexpected success with invalid ID" -ForegroundColor Red
    } catch {
        Write-Host "Expected error for invalid student ID: $($_.Exception.Message)" -ForegroundColor Green
    }

    Write-Host "`nAPI Test Completed Successfully!" -ForegroundColor Green

} catch {
    Write-Host "Error occurred: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $streamReader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        $errorBody = $streamReader.ReadToEnd()
        Write-Host "Error details: $errorBody" -ForegroundColor Red
        $streamReader.Close()
    }
}

Write-Host "`n=== API Summary ===" -ForegroundColor Cyan
Write-Host "Purpose: Get count of assignments a student can still submit" -ForegroundColor Green
Write-Host "Logic: (NOT DUE YET) OR (OVERDUE AND ALLOWS LATE)" -ForegroundColor Yellow
Write-Host "Endpoint: GET /api/submissions/student/{studentId}/unsubmitted-count" -ForegroundColor Magenta

Write-Host "`nTest Completed!" -ForegroundColor Green
