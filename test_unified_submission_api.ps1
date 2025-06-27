# Test Unified Submit/Update Submission API

Write-Host "=== Testing Unified Submit/Update Submission API ===" -ForegroundColor Green

# Create test files
Write-Host "`nCreating test files..." -ForegroundColor Yellow
$testFile1 = "test_submission_v1.txt"
$testFile2 = "test_submission_v2.txt"
Set-Content -Path $testFile1 -Value "This is version 1 of the submission document."
Set-Content -Path $testFile2 -Value "This is version 2 of the updated submission document."

# Test 1: First submission (should create new)
Write-Host "`n1. Testing first submission (create)..." -ForegroundColor Yellow

try {
    $form1 = @{
        assignmentId = "21"
        documentsMetadata = '{"documents":[{"description":"First submission","displayOrder":1}]}'
    }

    $response1 = Invoke-RestMethod -Uri "http://localhost:8080/api/submissions/submit-or-update" `
        -Method POST `
        -Form $form1 `
        -InFile @{files = $testFile1} `
        -ErrorAction Stop
    
    Write-Host "✅ First submission successful!" -ForegroundColor Green
    Write-Host "Submission ID: $($response1.id)" -ForegroundColor Cyan
    Write-Host "Status: $($response1.status)" -ForegroundColor Cyan
    Write-Host "Document Count: $($response1.documents.Count)" -ForegroundColor Cyan
    
    $submissionId = $response1.id
    
} catch {
    Write-Host "❌ First submission failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.Exception.Response)" -ForegroundColor Red
}

# Wait a moment
Start-Sleep -Seconds 2

# Test 2: Update submission (should update existing)
Write-Host "`n2. Testing update submission (replace files)..." -ForegroundColor Yellow

try {
    $form2 = @{
        assignmentId = "21"
        documentsMetadata = '{"documents":[{"description":"Updated submission","displayOrder":1}]}'
    }

    $response2 = Invoke-RestMethod -Uri "http://localhost:8080/api/submissions/submit-or-update" `
        -Method POST `
        -Form $form2 `
        -InFile @{files = $testFile2} `
        -ErrorAction Stop
    
    Write-Host "✅ Update submission successful!" -ForegroundColor Green
    Write-Host "Submission ID: $($response2.id)" -ForegroundColor Cyan
    Write-Host "Status: $($response2.status)" -ForegroundColor Cyan
    Write-Host "Document Count: $($response2.documents.Count)" -ForegroundColor Cyan
    Write-Host "Updated At: $($response2.submittedAt)" -ForegroundColor Cyan
    
    # Verify it's the same submission ID
    if ($response2.id -eq $submissionId) {
        Write-Host "✅ Confirmed: Same submission was updated (not new one created)" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Warning: Different submission ID - may have created new instead of updating" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "❌ Update submission failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.Exception.Response)" -ForegroundColor Red
}

# Test 3: Check submission status via student course details
Write-Host "`n3. Checking submission status..." -ForegroundColor Yellow

try {
    $courseDetails = Invoke-RestMethod -Uri "http://localhost:8080/api/student/courses/7/details" `
        -Method GET `
        -ErrorAction Stop
    
    $assignment = $courseDetails.data.assignments | Where-Object { $_.id -eq 21 }
    if ($assignment) {
        Write-Host "✅ Assignment found in course details" -ForegroundColor Green
        Write-Host "Has Submitted: $($assignment.submission.hasSubmitted)" -ForegroundColor Cyan
        Write-Host "Submission Status: $($assignment.submission.status)" -ForegroundColor Cyan
        Write-Host "Document Count: $($assignment.submission.documentCount)" -ForegroundColor Cyan
        Write-Host "Can Edit: $($assignment.submission.canEdit)" -ForegroundColor Cyan
    }
    
} catch {
    Write-Host "❌ Failed to get course details: $($_.Exception.Message)" -ForegroundColor Red
}

# Clean up
if (Test-Path $testFile1) { Remove-Item $testFile1 }
if (Test-Path $testFile2) { Remove-Item $testFile2 }

Write-Host "`n=== Test Complete ===" -ForegroundColor Green
Write-Host "New API endpoint: POST /api/submissions/submit-or-update" -ForegroundColor Cyan
Write-Host "- Creates new submission if none exists" -ForegroundColor White
Write-Host "- Updates existing submission (replaces all files) if already exists" -ForegroundColor White
Write-Host "- Single endpoint for both operations!" -ForegroundColor White
