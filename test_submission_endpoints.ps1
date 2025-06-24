# Test Submission APIs
Write-Host "Testing Submission APIs..." -ForegroundColor Green

# Configuration
$baseUrl = "http://localhost:8080/api"
$token = "your-jwt-token-here"  # Replace with actual JWT token

# Headers
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# Test data
$assignmentId = 1
$submissionId = 1
$courseId = 1

Write-Host "`n=== Testing Submission Endpoints ===" -ForegroundColor Yellow

# 1. Test: Check if student has submitted assignment
Write-Host "`n1. Checking if student has submitted assignment..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/submissions/check/$assignmentId" -Method GET -Headers $headers
    Write-Host "Has submitted: $response" -ForegroundColor Green
} catch {
    Write-Host "Error checking submission status: $($_.Exception.Message)" -ForegroundColor Red
}

# 2. Test: Get student's own submissions
Write-Host "`n2. Getting student's own submissions..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/submissions/my-submissions?page=0&size=10" -Method GET -Headers $headers
    Write-Host "My submissions count: $($response.Count)" -ForegroundColor Green
    if ($response.Count -gt 0) {
        Write-Host "First submission ID: $($response[0].id)" -ForegroundColor Green
    }
} catch {
    Write-Host "Error getting my submissions: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. Test: Get submissions by assignment (for teachers)
Write-Host "`n3. Getting submissions by assignment..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/submissions/assignment/$assignmentId?page=0&size=10" -Method GET -Headers $headers
    Write-Host "Assignment submissions count: $($response.Count)" -ForegroundColor Green
} catch {
    Write-Host "Error getting assignment submissions: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. Test: Get submission by ID
Write-Host "`n4. Getting submission by ID..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/submissions/$submissionId" -Method GET -Headers $headers
    Write-Host "Submission found: ID=$($response.id), Status=$($response.status)" -ForegroundColor Green
} catch {
    Write-Host "Error getting submission by ID: $($_.Exception.Message)" -ForegroundColor Red
}

# 5. Test: Submit assignment (multipart form data)
Write-Host "`n5. Testing submit assignment..." -ForegroundColor Cyan
Write-Host "Note: This test requires multipart form data and is best tested with a frontend or Postman" -ForegroundColor Yellow

# Create a simple text file for testing
$testFile = "test-submission.txt"
"This is a test submission content." | Out-File -FilePath $testFile -Encoding UTF8

try {
    # PowerShell multipart form data is complex, showing structure for reference
    Write-Host "Multipart form data structure:" -ForegroundColor Yellow
    Write-Host "  - assignmentId: $assignmentId"
    Write-Host "  - content: 'Test submission content'"
    Write-Host "  - files: $testFile"
    
    # For actual testing, use Postman or frontend
    Write-Host "Use Postman or frontend to test file upload" -ForegroundColor Yellow
} catch {
    Write-Host "Error submitting assignment: $($_.Exception.Message)" -ForegroundColor Red
} finally {
    # Clean up test file
    if (Test-Path $testFile) {
        Remove-Item $testFile
    }
}

# 6. Test: Grade submission (for teachers)
Write-Host "`n6. Testing grade submission..." -ForegroundColor Cyan
$gradeData = @{
    score = 8.5
    feedback = "Good work! Minor improvements needed in algorithm optimization."
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/submissions/$submissionId/grade" -Method PUT -Headers $headers -Body $gradeData
    Write-Host "Submission graded successfully: Score=$($response.score)" -ForegroundColor Green
} catch {
    Write-Host "Error grading submission: $($_.Exception.Message)" -ForegroundColor Red
}

# 7. Test: Delete submission
Write-Host "`n7. Testing delete submission..." -ForegroundColor Cyan
Write-Host "Skipping delete test to preserve data. Use with caution." -ForegroundColor Yellow
# try {
#     Invoke-RestMethod -Uri "$baseUrl/submissions/$submissionId" -Method DELETE -Headers $headers
#     Write-Host "Submission deleted successfully" -ForegroundColor Green
# } catch {
#     Write-Host "Error deleting submission: $($_.Exception.Message)" -ForegroundColor Red
# }

Write-Host "`n=== Test Summary ===" -ForegroundColor Yellow
Write-Host "- Check submission status: GET /api/submissions/check/{assignmentId}"
Write-Host "- Get my submissions: GET /api/submissions/my-submissions"
Write-Host "- Get assignment submissions: GET /api/submissions/assignment/{assignmentId}"
Write-Host "- Get submission by ID: GET /api/submissions/{id}"
Write-Host "- Submit assignment: POST /api/submissions/submit (multipart/form-data)"
Write-Host "- Grade submission: PUT /api/submissions/{id}/grade"
Write-Host "- Delete submission: DELETE /api/submissions/{id}"

Write-Host "`nTesting completed!" -ForegroundColor Green
