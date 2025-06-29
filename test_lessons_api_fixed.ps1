# PowerShell script to test LMS Lessons API
# Author: Generated for LMS Testing
# Description: Tests lesson data retrieval endpoints

Write-Host "=== LMS Lessons API Test Script ===" -ForegroundColor Green
Write-Host "Testing lesson data retrieval..." -ForegroundColor Yellow

# API base URL
$baseUrl = "http://localhost:8080"

# Test endpoints
$endpoints = @(
    @{
        name = "Get Course Full Details (includes lessons)"
        url = "$baseUrl/admin/courses/1/full-details"
        method = "GET"
    },
    @{
        name = "Get Student Course Details (includes lessons)"  
        url = "$baseUrl/admin/student/courses/1/details"
        method = "GET"
    },
    @{
        name = "Get Lesson by ID"
        url = "$baseUrl/api/lessons/1"
        method = "GET"
    },
    @{
        name = "Get Lesson Documents"
        url = "$baseUrl/api/lessons/1/documents"
        method = "GET"
    }
)

function Test-ApiEndpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$Method = "GET"
    )
    
    Write-Host "`n--- Testing: $Name ---" -ForegroundColor Cyan
    Write-Host "URL: $Url" -ForegroundColor Gray
    
    try {
        $response = Invoke-RestMethod -Uri $Url -Method $Method -ContentType "application/json" -ErrorAction Stop
        
        if ($response) {
            Write-Host "SUCCESS: Response received" -ForegroundColor Green
            
            # Check if response contains lesson data
            if ($response.lessons -or $response.lessonInfos -or $response.title) {
                Write-Host "Lesson data found in response" -ForegroundColor Green
                
                # Display lesson count if available
                if ($response.lessons) {
                    Write-Host "   Lessons count: $($response.lessons.Count)" -ForegroundColor Blue
                } elseif ($response.lessonInfos) {
                    Write-Host "   Lesson infos count: $($response.lessonInfos.Count)" -ForegroundColor Blue
                } elseif ($response.title) {
                    Write-Host "   Individual lesson: $($response.title)" -ForegroundColor Blue
                }
            } else {
                Write-Host "No lesson data detected in response" -ForegroundColor Yellow
            }
            
            # Show sample of response structure
            Write-Host "   Response structure:" -ForegroundColor Gray
            $response | ConvertTo-Json -Depth 2 -Compress | Write-Host
            
        } else {
            Write-Host "Empty response received" -ForegroundColor Yellow
        }
        
    } catch {
        $statusCode = $_.Exception.Response.StatusCode
        $statusDescription = $_.Exception.Response.StatusDescription
        
        if ($statusCode -eq 401) {
            Write-Host "AUTHENTICATION REQUIRED: This endpoint requires login" -ForegroundColor Yellow
        } elseif ($statusCode -eq 404) {
            Write-Host "NOT FOUND: Endpoint or resource not found" -ForegroundColor Red
        } elseif ($statusCode -eq 500) {
            Write-Host "SERVER ERROR: Internal server error" -ForegroundColor Red
        } else {
            Write-Host "ERROR: $statusCode - $statusDescription" -ForegroundColor Red
        }
        
        Write-Host "   Error details: $($_.Exception.Message)" -ForegroundColor Gray
    }
}

# Test if server is running
Write-Host "`nChecking if LMS server is running..." -ForegroundColor Yellow

try {
    $healthCheck = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method GET -ErrorAction SilentlyContinue
    Write-Host "Server is running" -ForegroundColor Green
} catch {
    try {
        # Try a simple endpoint
        $response = Invoke-WebRequest -Uri $baseUrl -Method GET -ErrorAction SilentlyContinue
        Write-Host "Server is responding" -ForegroundColor Green
    } catch {
        Write-Host "Server appears to be down. Please start the application first." -ForegroundColor Red
        Write-Host "   Run: ./gradlew bootRun" -ForegroundColor Gray
        exit 1
    }
}

# Run tests for each endpoint
foreach ($endpoint in $endpoints) {
    Test-ApiEndpoint -Name $endpoint.name -Url $endpoint.url -Method $endpoint.method
    Start-Sleep -Milliseconds 500  # Small delay between requests
}

# Summary
Write-Host "`n=== Test Summary ===" -ForegroundColor Green
Write-Host "API testing completed" -ForegroundColor Green
Write-Host "Check the results above for lesson data verification" -ForegroundColor Yellow
Write-Host "Note: Some endpoints may require authentication" -ForegroundColor Gray

# Additional information
Write-Host "`nAvailable Lesson Endpoints:" -ForegroundColor Blue
Write-Host "   GET /api/lessons/{lessonId} - Get specific lesson" -ForegroundColor Gray
Write-Host "   GET /api/lessons/{lessonId}/documents - Get lesson documents" -ForegroundColor Gray
Write-Host "   GET /admin/courses/{courseId}/full-details - Get course with lessons" -ForegroundColor Gray
Write-Host "   POST /api/lessons/create - Create new lesson (requires auth)" -ForegroundColor Gray
Write-Host "   PUT /api/lessons/{lessonId} - Update lesson (requires auth)" -ForegroundColor Gray

Write-Host "`nTo restart the application, run:" -ForegroundColor Yellow
Write-Host "   ./gradlew bootRun" -ForegroundColor White
