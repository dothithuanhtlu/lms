# Test Assignment Creation APIs

Write-Host "=== Testing Assignment Creation APIs ===" -ForegroundColor Green

# Test 1: Basic Assignment Creation (without files)
Write-Host "`n1. Testing basic assignment creation..." -ForegroundColor Yellow

$basicAssignment = @{
    title = "Test Assignment - Basic"
    description = "This is a test assignment created without files"
    maxScore = 100.0
    dueDate = "2025-07-10T23:59:00"
    courseId = 7
    isPublished = $true
    allowLateSubmission = $true
} | ConvertTo-Json -Depth 3

try {
    $response1 = Invoke-RestMethod -Uri "http://localhost:8080/api/assignments/create" `
        -Method POST `
        -ContentType "application/json" `
        -Body $basicAssignment `
        -ErrorAction Stop
    
    Write-Host "✅ Basic assignment created successfully!" -ForegroundColor Green
    Write-Host "Assignment ID: $($response1.id)" -ForegroundColor Cyan
    Write-Host "Title: $($response1.title)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Failed to create basic assignment: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Assignment Creation with Files
Write-Host "`n2. Testing assignment creation with files..." -ForegroundColor Yellow

# Create a test file
$testContent = "This is a test assignment document."
$testFilePath = "test_assignment_doc.txt"
Set-Content -Path $testFilePath -Value $testContent

try {
    # Prepare multipart form data
    $form = @{
        title = "Test Assignment - With Files"
        description = "This is a test assignment created with files"
        maxScore = "80.0"
        dueDate = "2025-07-15T23:59:00"
        courseId = "7"
        isPublished = "true"
        allowLateSubmission = "true"
        fileDescriptions = "Assignment Instructions"
    }

    $response2 = Invoke-RestMethod -Uri "http://localhost:8080/api/assignments/create-with-files" `
        -Method POST `
        -Form $form `
        -InFile @{files = $testFilePath} `
        -ErrorAction Stop
    
    Write-Host "✅ Assignment with files created successfully!" -ForegroundColor Green
    Write-Host "Assignment ID: $($response2.id)" -ForegroundColor Cyan
    Write-Host "Title: $($response2.title)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Failed to create assignment with files: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.Exception.Response)" -ForegroundColor Red
}

# Clean up
if (Test-Path $testFilePath) {
    Remove-Item $testFilePath
}

Write-Host "`n=== Assignment Creation API Test Complete ===" -ForegroundColor Green
