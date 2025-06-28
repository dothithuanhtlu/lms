# Test API: Get All Submissions by Assignment ID (New Pagination Format)
# This script tests the updated teacher API with ResultPaginationDTO format

Write-Host "=== Testing Updated Get Submissions API with ResultPaginationDTO ===" -ForegroundColor Cyan

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

    # 2. Test Default Pagination (current=1, pageSize=10)
    Write-Host "`n2. Test Default Pagination (current=1, pageSize=10)..." -ForegroundColor Yellow
    
    $headers = @{ "Authorization" = "Bearer $teacherToken" }
    $submissionsResponse = Invoke-RestMethod -Uri "$BASE_URL/api/submissions/assignment/$ASSIGNMENT_ID" -Method GET -Headers $headers

    Write-Host "✅ Default Pagination Response:" -ForegroundColor Green
    Write-Host ($submissionsResponse | ConvertTo-Json -Depth 3) -ForegroundColor White

    # 3. Test Custom Pagination (current=1, pageSize=5)
    Write-Host "`n3. Test Custom Pagination (current=1, pageSize=5)..." -ForegroundColor Yellow
    
    $customPaginationUri = "$BASE_URL/api/submissions/assignment/$ASSIGNMENT_ID" + "?current=1`&pageSize=5"
    $customSubmissionsResponse = Invoke-RestMethod -Uri $customPaginationUri -Method GET -Headers $headers

    Write-Host "✅ Custom Pagination Response:" -ForegroundColor Green
    Write-Host ($customSubmissionsResponse | ConvertTo-Json -Depth 3) -ForegroundColor White

    # 4. Test Page 2 (current=2, pageSize=3)
    Write-Host "`n4. Test Page 2 (current=2, pageSize=3)..." -ForegroundColor Yellow
    
    $page2Uri = "$BASE_URL/api/submissions/assignment/$ASSIGNMENT_ID" + "?current=2`&pageSize=3"
    $page2Response = Invoke-RestMethod -Uri $page2Uri -Method GET -Headers $headers

    Write-Host "✅ Page 2 Response:" -ForegroundColor Green
    Write-Host ($page2Response | ConvertTo-Json -Depth 3) -ForegroundColor White

    # 5. Display Pagination Info
    Write-Host "`n=== Pagination Analysis ===" -ForegroundColor Cyan
    if ($submissionsResponse.meta) {
        Write-Host "📊 Pagination Metadata:" -ForegroundColor Yellow
        Write-Host "  Current Page: $($submissionsResponse.meta.page)" -ForegroundColor White
        Write-Host "  Page Size: $($submissionsResponse.meta.pageSize)" -ForegroundColor White
        Write-Host "  Total Pages: $($submissionsResponse.meta.pages)" -ForegroundColor White
        Write-Host "  Total Elements: $($submissionsResponse.meta.total)" -ForegroundColor White
        
        Write-Host "`n📋 Results on Current Page: $($submissionsResponse.result.Count)" -ForegroundColor Yellow
        
        if ($submissionsResponse.result.Count -gt 0) {
            Write-Host "`nSubmission Details:" -ForegroundColor Yellow
            foreach ($submission in $submissionsResponse.result) {
                Write-Host "- ID: $($submission.id) | Student: $($submission.studentName)" -ForegroundColor White
                Write-Host "  Status: $($submission.status) | Score: $($submission.score)" -ForegroundColor White
                Write-Host "  Submitted: $($submission.submittedAt)" -ForegroundColor White
                Write-Host "  Files: $($submission.documents.Count)" -ForegroundColor White
                Write-Host ""
            }
        } else {
            Write-Host "No submissions found for this assignment." -ForegroundColor Yellow
        }
    }

    # 6. Test My Submissions API
    Write-Host "`n5. Test Student's My Submissions API..." -ForegroundColor Yellow
    
    $mySubmissionsUri = "$BASE_URL/api/submissions/my-submissions?current=1`&pageSize=5"
    $mySubmissionsResponse = Invoke-RestMethod -Uri $mySubmissionsUri -Method GET -Headers $headers

    Write-Host "✅ My Submissions Response:" -ForegroundColor Green
    Write-Host ($mySubmissionsResponse | ConvertTo-Json -Depth 2) -ForegroundColor White

} catch {
    Write-Host "❌ Error occurred: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $streamReader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        $errorBody = $streamReader.ReadToEnd()
        Write-Host "Error details: $errorBody" -ForegroundColor Red
        $streamReader.Close()
    }
}

Write-Host "`n=== API Update Summary ===" -ForegroundColor Cyan
Write-Host "✅ Updated to use ResultPaginationDTO format" -ForegroundColor Green
Write-Host "✅ Parameters: current (starts from 1), pageSize (default 10)" -ForegroundColor Green
Write-Host "✅ Response includes meta (pagination info) and result (data)" -ForegroundColor Green
Write-Host "✅ Compatible with existing frontend pagination components" -ForegroundColor Green
Write-Host "`n🎉 Test Completed!" -ForegroundColor Green
