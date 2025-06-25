# Test Delete Assignment API with Cloudinary cleanup
# PowerShell Script

Write-Host "🧪 Testing Delete Assignment API with Cloudinary cleanup" -ForegroundColor Green

$baseUrl = "http://localhost:8080/v1/api"

# Step 1: Login to get JWT token
Write-Host "`n1️⃣ Logging in as teacher..." -ForegroundColor Yellow
$loginData = @{
    username = "teacher1@example.com"
    password = "password123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body $loginData -ContentType "application/json"
    $token = $loginResponse.data.access_token
    $headers = @{ Authorization = "Bearer $token" }
    Write-Host "✅ Login successful" -ForegroundColor Green
} catch {
    Write-Host "❌ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 2: Get list of assignments to find one to delete
Write-Host "`n2️⃣ Getting assignments list..." -ForegroundColor Yellow
try {
    $assignmentsResponse = Invoke-RestMethod -Uri "$baseUrl/assignments?page=0&size=10" -Method GET -Headers $headers
    $assignments = $assignmentsResponse.data.result
    
    if ($assignments.Count -eq 0) {
        Write-Host "❌ No assignments found to delete" -ForegroundColor Red
        exit 1
    }
    
    # Find an assignment with files if possible
    $assignmentToDelete = $assignments | Where-Object { $_.documents.Count -gt 0 } | Select-Object -First 1
    if (-not $assignmentToDelete) {
        $assignmentToDelete = $assignments[0]
    }
    
    Write-Host "📋 Found assignment to delete:" -ForegroundColor Cyan
    Write-Host "   ID: $($assignmentToDelete.id)" -ForegroundColor White
    Write-Host "   Title: $($assignmentToDelete.title)" -ForegroundColor White
    Write-Host "   Documents: $($assignmentToDelete.documents.Count)" -ForegroundColor White
    
} catch {
    Write-Host "❌ Failed to get assignments: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 3: Test deletion
Write-Host "`n3️⃣ Deleting assignment..." -ForegroundColor Yellow
Write-Host "   Assignment ID: $($assignmentToDelete.id)" -ForegroundColor White
Write-Host "   Title: $($assignmentToDelete.title)" -ForegroundColor White

try {
    $deleteResponse = Invoke-RestMethod -Uri "$baseUrl/assignments/delete/$($assignmentToDelete.id)" -Method DELETE -Headers $headers
    Write-Host "✅ Assignment deleted successfully" -ForegroundColor Green
    
    if ($deleteResponse.message) {
        Write-Host "   Response: $($deleteResponse.message)" -ForegroundColor White
    }
    
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $errorMessage = $_.Exception.Message
    
    if ($statusCode -eq 404) {
        Write-Host "⚠️ Assignment not found (may have been already deleted)" -ForegroundColor Yellow
    } else {
        Write-Host "❌ Failed to delete assignment: $errorMessage" -ForegroundColor Red
        Write-Host "   Status Code: $statusCode" -ForegroundColor Red
    }
}

# Step 4: Verify deletion
Write-Host "`n4️⃣ Verifying deletion..." -ForegroundColor Yellow
try {
    $verifyResponse = Invoke-RestMethod -Uri "$baseUrl/assignments/detail/$($assignmentToDelete.id)" -Method GET -Headers $headers
    Write-Host "❌ Assignment still exists - deletion may have failed" -ForegroundColor Red
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 404) {
        Write-Host "✅ Assignment successfully deleted from database" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Unexpected error during verification: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

Write-Host "`n🎉 Delete Assignment test completed!" -ForegroundColor Green
Write-Host "📝 Notes:" -ForegroundColor Cyan
Write-Host "   - Check application logs for Cloudinary deletion details" -ForegroundColor White
Write-Host "   - Verify that assignment documents were removed from Cloudinary" -ForegroundColor White
Write-Host "   - Verify that submission files (if any) were also removed" -ForegroundColor White
