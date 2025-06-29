# Test Student Course Details API with Lessons
# This script tests the /admin/student/courses/{courseId}/details endpoint

param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$CourseId = 7,
    [string]$Username = "SV001", # Student username
    [string]$Password = "123456"
)

Write-Host "Testing Student Course Details API with Lessons Support" -ForegroundColor Green
Write-Host "Base URL: $BaseUrl" -ForegroundColor Yellow
Write-Host "Course ID: $CourseId" -ForegroundColor Yellow
Write-Host "Student Username: $Username" -ForegroundColor Yellow

# Step 1: Login to get JWT token
Write-Host "`n=== Step 1: Student Login ===" -ForegroundColor Cyan
$loginUrl = "$BaseUrl/login"
$loginBody = @{
    username = $Username
    password = $Password
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri $loginUrl -Method POST -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.result.access_token
    Write-Host "✅ Login successful" -ForegroundColor Green
    Write-Host "Token: $($token.Substring(0, 50))..." -ForegroundColor Gray
} catch {
    Write-Host "❌ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 2: Get Student Course Details
Write-Host "`n=== Step 2: Get Student Course Details with Lessons ===" -ForegroundColor Cyan
$apiUrl = "$BaseUrl/admin/student/courses/$CourseId/details"

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

try {
    $response = Invoke-RestMethod -Uri $apiUrl -Method GET -Headers $headers
    
    Write-Host "✅ API call successful" -ForegroundColor Green
    Write-Host "`n📊 Course Details:" -ForegroundColor White
    Write-Host "Course ID: $($response.id)" -ForegroundColor Yellow
    Write-Host "Course Code: $($response.courseCode)" -ForegroundColor Yellow
    Write-Host "Course Name: $($response.courseName)" -ForegroundColor Yellow
    Write-Host "Description: $($response.description)" -ForegroundColor Yellow
    Write-Host "Status: $($response.status)" -ForegroundColor Yellow
    Write-Host "Start Date: $($response.startDate)" -ForegroundColor Yellow
    Write-Host "End Date: $($response.endDate)" -ForegroundColor Yellow
    
    Write-Host "`n👨‍🏫 Teacher Info:" -ForegroundColor White
    Write-Host "Teacher: $($response.teacher.fullName) ($($response.teacher.userCode))" -ForegroundColor Yellow
    Write-Host "Email: $($response.teacher.email)" -ForegroundColor Yellow
    
    Write-Host "`n👨‍🎓 Student Info:" -ForegroundColor White
    Write-Host "Student: $($response.studentInfo.fullName) ($($response.studentInfo.userCode))" -ForegroundColor Yellow
    Write-Host "Class: $($response.studentInfo.className)" -ForegroundColor Yellow
    Write-Host "Enrollment Status: $($response.studentInfo.enrollmentStatus)" -ForegroundColor Yellow
    Write-Host "Midterm Score: $($response.studentInfo.midtermScore)" -ForegroundColor Yellow
    Write-Host "Final Score: $($response.studentInfo.finalScore)" -ForegroundColor Yellow
    
    # Display Lessons Information
    if ($response.lessons -and $response.lessons.Count -gt 0) {
        Write-Host "`n📚 Lessons ($($response.lessons.Count) total):" -ForegroundColor White
        foreach ($lesson in $response.lessons) {
            Write-Host "  📖 Lesson ID: $($lesson.id)" -ForegroundColor Cyan
            Write-Host "     Title: $($lesson.title)" -ForegroundColor Yellow
            Write-Host "     Description: $($lesson.description)" -ForegroundColor Yellow
            Write-Host "     Published: $($lesson.isPublished)" -ForegroundColor Yellow
            Write-Host "     Created: $($lesson.createdAt)" -ForegroundColor Gray
            Write-Host "     Updated: $($lesson.updatedAt)" -ForegroundColor Gray
            
            if ($lesson.documents -and $lesson.documents.Count -gt 0) {
                Write-Host "     📎 Documents ($($lesson.documents.Count)):" -ForegroundColor White
                foreach ($doc in $lesson.documents) {
                    Write-Host "       - $($doc.fileName) [$($doc.fileType)]" -ForegroundColor Green
                    Write-Host "         URL: $($doc.fileUrl)" -ForegroundColor Gray
                    Write-Host "         Uploaded: $($doc.uploadedAt)" -ForegroundColor Gray
                }
            } else {
                Write-Host "     📎 No documents" -ForegroundColor Gray
            }
            Write-Host ""
        }
    } else {
        Write-Host "`n📚 No lessons found or lessons not published" -ForegroundColor Yellow
    }
    
    # Display Assignments Information
    if ($response.assignments -and $response.assignments.Count -gt 0) {
        Write-Host "`n📝 Assignments ($($response.assignments.Count) total):" -ForegroundColor White
        foreach ($assignment in $response.assignments) {
            Write-Host "  📋 Assignment: $($assignment.title)" -ForegroundColor Cyan
            Write-Host "     Max Score: $($assignment.maxScore)" -ForegroundColor Yellow
            Write-Host "     Due Date: $($assignment.dueDate)" -ForegroundColor Yellow
            Write-Host "     Status: $($assignment.submission.status)" -ForegroundColor Yellow
            Write-Host "     Submitted: $($assignment.submission.hasSubmitted)" -ForegroundColor Yellow
            if ($assignment.submission.score) {
                Write-Host "     Score: $($assignment.submission.score)" -ForegroundColor Green
            }
            Write-Host ""
        }
    } else {
        Write-Host "`n📝 No assignments found" -ForegroundColor Yellow
    }
    
    # Display Statistics
    Write-Host "`n📊 Course Statistics:" -ForegroundColor White
    Write-Host "Total Assignments: $($response.statistics.totalAssignments)" -ForegroundColor Yellow
    Write-Host "Submitted Assignments: $($response.statistics.submittedAssignments)" -ForegroundColor Yellow
    Write-Host "Graded Assignments: $($response.statistics.gradedAssignments)" -ForegroundColor Yellow
    Write-Host "Late Submissions: $($response.statistics.lateSubmissions)" -ForegroundColor Yellow
    Write-Host "Average Score: $($response.statistics.averageScore)" -ForegroundColor Yellow
    Write-Host "Completion Rate: $($response.statistics.completionRate)%" -ForegroundColor Yellow
    
    # Save response to file for detailed inspection
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $outputFile = "student_course_details_with_lessons_$timestamp.json"
    $response | ConvertTo-Json -Depth 10 | Out-File -FilePath $outputFile -Encoding UTF8
    Write-Host "`n💾 Full response saved to: $outputFile" -ForegroundColor Green
    
} catch {
    Write-Host "❌ API call failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        Write-Host "Status Description: $($_.Exception.Response.StatusDescription)" -ForegroundColor Red
    }
    exit 1
}

Write-Host "`n✅ Test completed successfully!" -ForegroundColor Green
