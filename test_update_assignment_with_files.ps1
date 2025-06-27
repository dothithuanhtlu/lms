# Test Update Assignment With Files API
# PowerShell Script

Write-Host "🧪 Testing Update Assignment With Files API" -ForegroundColor Green

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

# Step 2: Get list of assignments to find one to update
Write-Host "`n2️⃣ Getting assignments list..." -ForegroundColor Yellow
try {
    $assignmentsResponse = Invoke-RestMethod -Uri "$baseUrl/assignments?page=0&size=10" -Method GET -Headers $headers
    $assignments = $assignmentsResponse.data.result
    
    if ($assignments.Count -eq 0) {
        Write-Host "❌ No assignments found to update" -ForegroundColor Red
        exit 1
    }
    
    # Use the first assignment for testing
    $assignmentToUpdate = $assignments[0]
    
    Write-Host "📋 Found assignment to update:" -ForegroundColor Cyan
    Write-Host "   ID: $($assignmentToUpdate.id)" -ForegroundColor White
    Write-Host "   Title: $($assignmentToUpdate.title)" -ForegroundColor White
    Write-Host "   Current Documents: $($assignmentToUpdate.documents.Count)" -ForegroundColor White
    
} catch {
    Write-Host "❌ Failed to get assignments: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 3: Create test files for update
Write-Host "`n3️⃣ Creating test files for update..." -ForegroundColor Yellow

$testFile1Path = "test_update_doc1.txt"
$testFile2Path = "test_update_doc2.pdf"

"Updated Assignment Document 1`nContent: Assignment requirements`nUpdated at: $(Get-Date)" | Out-File -FilePath $testFile1Path -Encoding UTF8
"Updated Assignment Document 2`nContent: Sample PDF content`nUpdated at: $(Get-Date)" | Out-File -FilePath $testFile2Path -Encoding UTF8

Write-Host "✅ Created test files: $testFile1Path, $testFile2Path" -ForegroundColor Green

# Step 4: Update assignment with files
Write-Host "`n4️⃣ Updating assignment with files..." -ForegroundColor Yellow

try {
    # Create multipart form data
    $boundary = [System.Guid]::NewGuid().ToString()
    
    # Read file contents
    $file1Content = Get-Content $testFile1Path -Raw
    $file2Content = Get-Content $testFile2Path -Raw
    
    # Build form data
    $formData = @"
--$boundary
Content-Disposition: form-data; name="title"

Updated Assignment Title - $(Get-Date -Format "yyyy-MM-dd HH:mm")
--$boundary
Content-Disposition: form-data; name="description"

This assignment has been updated with new files and description. Updated on $(Get-Date).
--$boundary
Content-Disposition: form-data; name="maxScore"

95
--$boundary
Content-Disposition: form-data; name="dueDate"

2025-08-15T23:59:59
--$boundary
Content-Disposition: form-data; name="allowLateSubmission"

true
--$boundary
Content-Disposition: form-data; name="isPublished"

true
--$boundary
Content-Disposition: form-data; name="documentsMetadata"

[{"title":"Updated Requirements","documentType":"DOCUMENT","isDownloadable":true},{"title":"Updated Sample","documentType":"REFERENCE","isDownloadable":true}]
--$boundary
Content-Disposition: form-data; name="files"; filename="test_update_doc1.txt"
Content-Type: text/plain

$file1Content
--$boundary
Content-Disposition: form-data; name="files"; filename="test_update_doc2.pdf"
Content-Type: application/pdf

$file2Content
--$boundary--
"@

    # Set headers for multipart request
    $updateHeaders = $headers.Clone()
    $updateHeaders["Content-Type"] = "multipart/form-data; boundary=$boundary"
    
    # Make the update request
    $updateResponse = Invoke-RestMethod -Uri "$baseUrl/assignments/$($assignmentToUpdate.id)/update-with-files" -Method PUT -Body $formData -Headers $updateHeaders
    
    Write-Host "✅ Assignment updated successfully!" -ForegroundColor Green
    Write-Host "   Updated ID: $($updateResponse.id)" -ForegroundColor White
    Write-Host "   New Title: $($updateResponse.title)" -ForegroundColor White
    Write-Host "   New Documents Count: $($updateResponse.documents.Count)" -ForegroundColor White
    Write-Host "   Max Score: $($updateResponse.maxScore)" -ForegroundColor White
    Write-Host "   Published: $($updateResponse.isPublished)" -ForegroundColor White
    
    if ($updateResponse.documents.Count -gt 0) {
        Write-Host "   📄 New Documents:" -ForegroundColor Cyan
        foreach ($doc in $updateResponse.documents) {
            Write-Host "      - $($doc.fileNameOriginal)" -ForegroundColor White
        }
    }
    
} catch {
    $statusCode = $null
    $errorMessage = $_.Exception.Message
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
    }
    
    Write-Host "❌ Failed to update assignment: $errorMessage" -ForegroundColor Red
    if ($statusCode) {
        Write-Host "   Status Code: $statusCode" -ForegroundColor Red
    }
}

# Step 5: Verify the update by fetching the assignment again
Write-Host "`n5️⃣ Verifying update..." -ForegroundColor Yellow
try {
    $verifyResponse = Invoke-RestMethod -Uri "$baseUrl/assignments/detail/$($assignmentToUpdate.id)" -Method GET -Headers $headers
    
    Write-Host "✅ Assignment details after update:" -ForegroundColor Green
    Write-Host "   ID: $($verifyResponse.id)" -ForegroundColor White
    Write-Host "   Title: $($verifyResponse.title)" -ForegroundColor White
    Write-Host "   Description: $($verifyResponse.description.Substring(0, [Math]::Min(50, $verifyResponse.description.Length)))..." -ForegroundColor White
    Write-Host "   Max Score: $($verifyResponse.maxScore)" -ForegroundColor White
    Write-Host "   Due Date: $($verifyResponse.dueDate)" -ForegroundColor White
    Write-Host "   Documents: $($verifyResponse.documents.Count)" -ForegroundColor White
    Write-Host "   Published: $($verifyResponse.isPublished)" -ForegroundColor White
    
} catch {
    Write-Host "⚠️ Could not verify update: $($_.Exception.Message)" -ForegroundColor Yellow
}

# Clean up test files
Write-Host "`n6️⃣ Cleaning up..." -ForegroundColor Yellow
if (Test-Path $testFile1Path) {
    Remove-Item $testFile1Path
}
if (Test-Path $testFile2Path) {
    Remove-Item $testFile2Path
}
Write-Host "✅ Test files cleaned up" -ForegroundColor Green

Write-Host "`n🎉 Update Assignment With Files test completed!" -ForegroundColor Green
Write-Host "📝 Notes:" -ForegroundColor Cyan
Write-Host "   - Old documents were replaced with new ones" -ForegroundColor White
Write-Host "   - Check Cloudinary to verify old files were deleted" -ForegroundColor White
Write-Host "   - Check application logs for detailed update process" -ForegroundColor White
