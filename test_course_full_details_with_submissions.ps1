# Test Course Full Details API with Assignment Submission Status

This script tests the enhanced `/admin/courses/{courseId}/full-details` endpoint that now includes assignment submission status for each student.

## API Endpoint
```
GET /admin/courses/{courseId}/full-details
```

## New Features Added

### Student Assignment Submission Status
Each student now includes an `assignmentSubmissions` array that shows:

- `assignmentId`: ID of the assignment
- `assignmentTitle`: Title of the assignment  
- `hasSubmitted`: Boolean indicating if student submitted
- `submissionDate`: When the assignment was submitted (if submitted)
- `score`: Score received (if graded)
- `status`: Current status ("SUBMITTED", "NOT_SUBMITTED", "LATE", "GRADED")

## PowerShell Test Script

```powershell
# Test Course Full Details API with Assignment Submission Status
param(
    [int]$CourseId = 1,
    [string]$BaseUrl = "http://localhost:8080"
)

$apiUrl = "$BaseUrl/admin/courses/$CourseId/full-details"

Write-Host "🧪 Testing Course Full Details API with Assignment Submission Status" -ForegroundColor Yellow
Write-Host "📋 Course ID: $CourseId" -ForegroundColor Cyan
Write-Host "🌐 API URL: $apiUrl" -ForegroundColor Cyan

Write-Host "`n🚀 Sending request..." -ForegroundColor Yellow

try {
    $response = Invoke-RestMethod -Uri $apiUrl -Method GET -ContentType "application/json"
    
    Write-Host "`n✅ SUCCESS! Course details retrieved successfully!" -ForegroundColor Green
    
    # Display basic course info
    Write-Host "`n📚 Course Information:" -ForegroundColor Green
    Write-Host "  - Course Name: $($response.courseName)" -ForegroundColor White
    Write-Host "  - Course Code: $($response.courseCode)" -ForegroundColor White
    Write-Host "  - Status: $($response.status)" -ForegroundColor White
    Write-Host "  - Current Students: $($response.currentStudents)" -ForegroundColor White
    Write-Host "  - Max Students: $($response.maxStudents)" -ForegroundColor White
    
    # Display assignments
    if ($response.assignments -and $response.assignments.Count -gt 0) {
        Write-Host "`n📝 Assignments:" -ForegroundColor Green
        foreach ($assignment in $response.assignments) {
            Write-Host "  - ID: $($assignment.id) | Title: $($assignment.title) | Submissions: $($assignment.totalSubmissions)" -ForegroundColor White
        }
    }
    
    # Display students with their assignment submission status
    if ($response.students -and $response.students.Count -gt 0) {
        Write-Host "`n👥 Students with Assignment Submission Status:" -ForegroundColor Green
        foreach ($student in $response.students) {
            Write-Host "`n  📍 Student: $($student.fullName) ($($student.userCode))" -ForegroundColor Cyan
            Write-Host "     Email: $($student.email)" -ForegroundColor White
            Write-Host "     Class: $($student.className)" -ForegroundColor White
            Write-Host "     Enrollment Status: $($student.enrollmentStatus)" -ForegroundColor White
            
            if ($student.assignmentSubmissions -and $student.assignmentSubmissions.Count -gt 0) {
                Write-Host "     Assignment Submissions:" -ForegroundColor Yellow
                foreach ($submission in $student.assignmentSubmissions) {
                    $statusColor = switch ($submission.status) {
                        "GRADED" { "Green" }
                        "SUBMITTED" { "Cyan" }
                        "LATE" { "Yellow" }
                        "NOT_SUBMITTED" { "Red" }
                        default { "White" }
                    }
                    
                    $submissionInfo = "       • $($submission.assignmentTitle): $($submission.status)"
                    if ($submission.hasSubmitted) {
                        $submissionInfo += " (Submitted: $($submission.submissionDate))"
                        if ($submission.score -ne $null) {
                            $submissionInfo += " [Score: $($submission.score)]"
                        }
                    }
                    Write-Host $submissionInfo -ForegroundColor $statusColor
                }
            } else {
                Write-Host "     No assignments found" -ForegroundColor Gray
            }
        }
    }
    
    Write-Host "`n📊 Summary:" -ForegroundColor Green
    Write-Host "  - Total Students: $($response.students.Count)" -ForegroundColor White
    Write-Host "  - Total Assignments: $($response.assignments.Count)" -ForegroundColor White
    Write-Host "  - Total Lessons: $($response.lessons.Count)" -ForegroundColor White
    
} catch {
    Write-Host "`n❌ ERROR! Failed to retrieve course details" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "Error Message: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            $reader.Close()
            
            Write-Host "Response Body:" -ForegroundColor Red
            $responseBody | Write-Host -ForegroundColor White
        } catch {
            Write-Host "Could not read response body" -ForegroundColor Red
        }
    }
}

Write-Host "`n📋 API Enhancement Summary:" -ForegroundColor Yellow
Write-Host "  - Added assignmentSubmissions field to each student" -ForegroundColor White
Write-Host "  - Shows submission status for each assignment per student" -ForegroundColor White
Write-Host "  - Includes submission date, score, and status" -ForegroundColor White
Write-Host "  - Status types: SUBMITTED, NOT_SUBMITTED, LATE, GRADED" -ForegroundColor White
