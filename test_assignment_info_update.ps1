# Test Assignment Info Update API
# This script tests the /api/assignments/update/{assignmentId} endpoint

param(
    [int]$AssignmentId = 1,
    [string]$BaseUrl = "http://localhost:8080"
)

$apiUrl = "$BaseUrl/api/assignments/update/$AssignmentId"

# Test data - only information fields (no files)
$requestBody = @{
    title = "Updated Assignment Title - $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
    dueDate = "2025-07-01T23:59:59"
    description = "This is an updated assignment description with timestamp: $(Get-Date)"
    maxScore = 100
    isPublished = $true
    allowLateSubmission = $false
} | ConvertTo-Json

Write-Host "🧪 Testing Assignment Info Update API" -ForegroundColor Yellow
Write-Host "📋 Assignment ID: $AssignmentId" -ForegroundColor Cyan
Write-Host "🌐 API URL: $apiUrl" -ForegroundColor Cyan
Write-Host "📄 Request Body:" -ForegroundColor Cyan
Write-Host $requestBody -ForegroundColor White

Write-Host "`n🚀 Sending request..." -ForegroundColor Yellow

try {
    $response = Invoke-RestMethod -Uri $apiUrl -Method PUT -Body $requestBody -ContentType "application/json"
    
    Write-Host "`n✅ SUCCESS! Assignment info updated successfully!" -ForegroundColor Green
    Write-Host "📊 Response Data:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 5 | Write-Host -ForegroundColor White
    
    Write-Host "`n📝 Updated Fields:" -ForegroundColor Green
    Write-Host "  - Title: $($response.title)" -ForegroundColor White
    Write-Host "  - Due Date: $($response.dueDate)" -ForegroundColor White
    Write-Host "  - Description: $($response.description)" -ForegroundColor White
    Write-Host "  - Max Score: $($response.maxScore)" -ForegroundColor White
    Write-Host "  - Is Published: $($response.isPublished)" -ForegroundColor White
    Write-Host "  - Allow Late Submission: $($response.allowLateSubmission)" -ForegroundColor White
    
} catch {
    Write-Host "`n❌ ERROR! Failed to update assignment info" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "Error Message: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            $reader.Close()
            
            Write-Host "Response Body:" -ForegroundColor Red
            $responseBody | Write-Host -ForegroundColor White
            
            # Try to parse as JSON for better formatting
            try {
                $jsonError = $responseBody | ConvertFrom-Json
                Write-Host "`n🔍 Error Details:" -ForegroundColor Red
                Write-Host "  - Error Type: $($jsonError.error)" -ForegroundColor White
                Write-Host "  - Message: $($jsonError.message)" -ForegroundColor White
            } catch {
                # Not JSON, just display as is
            }
        } catch {
            Write-Host "Could not read response body" -ForegroundColor Red
        }
    }
}

Write-Host "`n📋 API Summary:" -ForegroundColor Yellow
Write-Host "  - Endpoint: PUT /api/assignments/update/{assignmentId}" -ForegroundColor White
Write-Host "  - Content-Type: application/json" -ForegroundColor White
Write-Host "  - Updates: Only assignment info (title, dueDate, description, maxScore, isPublished, allowLateSubmission)" -ForegroundColor White
Write-Host "  - No file handling in this endpoint" -ForegroundColor White
