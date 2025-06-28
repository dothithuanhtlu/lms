# Test Script: Monitoring Automatic Grading System
# This script tests and monitors the automatic grading system
# Note: Manual API has been removed - system now auto-grades automatically every 30 minutes

Write-Host "=== Testing Automatic Grading System Monitoring ===" -ForegroundColor Cyan

# Configuration
$BASE_URL = "http://localhost:8080"
$ASSIGNMENT_ID = 1  # Change this to a valid assignment ID that is overdue

# Teacher credentials
$TEACHER_USERNAME = "teacher01"
$TEACHER_PASSWORD = "123456"

Write-Host "`n🤖 IMPORTANT: Auto-grading is now fully automatic!" -ForegroundColor Magenta
Write-Host "   - No manual API calls needed" -ForegroundColor White
Write-Host "   - System automatically processes overdue assignments every 30 minutes" -ForegroundColor White
Write-Host "   - This script only monitors the current state" -ForegroundColor White

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

    # 2. Get Assignment Details First
    Write-Host "`n2. Get Assignment Details..." -ForegroundColor Yellow
    
    $headers = @{ "Authorization" = "Bearer $teacherToken" }
    
    try {
        $assignmentResponse = Invoke-RestMethod -Uri "$BASE_URL/api/assignments/detail/$ASSIGNMENT_ID" -Method GET -Headers $headers
        Write-Host "📋 Assignment: $($assignmentResponse.title)" -ForegroundColor White
        Write-Host "📅 Due Date: $($assignmentResponse.dueDate)" -ForegroundColor White
        Write-Host "⏰ Allow Late: $($assignmentResponse.allowLateSubmission)" -ForegroundColor White
        Write-Host "📊 Max Score: $($assignmentResponse.maxScore)" -ForegroundColor White
    } catch {
        Write-Host "⚠️ Could not get assignment details: $($_.Exception.Message)" -ForegroundColor Yellow
    }

    # 3. Get Current Submissions Before Auto-Grade
    Write-Host "`n3. Get Current Submissions..." -ForegroundColor Yellow
    
    $submissionsResponse = Invoke-RestMethod -Uri "$BASE_URL/api/submissions/assignment/$ASSIGNMENT_ID" -Method GET -Headers $headers
    Write-Host "📊 Current submissions: $($submissionsResponse.meta.total)" -ForegroundColor White

    # 4. Get Current Statistics
    Write-Host "`n4. Get Current Statistics..." -ForegroundColor Yellow
    
    try {
        $statsResponse = Invoke-RestMethod -Uri "$BASE_URL/api/submissions/assignment/$ASSIGNMENT_ID/statistics" -Method GET -Headers $headers
        Write-Host "📈 Current Statistics:" -ForegroundColor White
        Write-Host "  Total Students: $($statsResponse.totalStudentsInCourse)" -ForegroundColor White
        Write-Host "  Total Submissions: $($statsResponse.totalSubmissions)" -ForegroundColor White
        Write-Host "  Students Not Submitted: $($statsResponse.studentsNotSubmitted)" -ForegroundColor White
        Write-Host "  Graded Submissions: $($statsResponse.gradedSubmissions)" -ForegroundColor White
        Write-Host "  Average Score: $($statsResponse.averageScore)" -ForegroundColor White
    } catch {
        Write-Host "⚠️ Could not get statistics: $($_.Exception.Message)" -ForegroundColor Yellow
    }

    # 5. Check for Auto-Graded Submissions
    Write-Host "`n5. Checking for Auto-Graded Submissions..." -ForegroundColor Yellow
    
    $submissionsResponse = Invoke-RestMethod -Uri "$BASE_URL/api/submissions/assignment/$ASSIGNMENT_ID" -Method GET -Headers $headers
    
    $autoGradedCount = 0
    if ($submissionsResponse.result.Count -gt 0) {
        Write-Host "`nAuto-Graded Submissions Found:" -ForegroundColor Yellow
        foreach ($submission in $submissionsResponse.result) {
            if ($submission.feedback -like "*Automatic grade*") {
                $autoGradedCount++
                Write-Host "  👤 Student: $($submission.studentName) - Score: $($submission.score) - Auto-graded" -ForegroundColor Cyan
            }
        }
    }
    
    if ($autoGradedCount -eq 0) {
        Write-Host "ℹ️ No auto-graded submissions found yet" -ForegroundColor Blue
        Write-Host "   (Auto-grading happens every 30 minutes for overdue assignments)" -ForegroundColor White
    } else {
        Write-Host "✅ Found $autoGradedCount auto-graded submissions" -ForegroundColor Green
    }
                Write-Host "- Student: $($submission.studentName)" -ForegroundColor White
                Write-Host "  Score: $($submission.score)" -ForegroundColor Red
                Write-Host "  Feedback: $($submission.feedback)" -ForegroundColor White
                Write-Host "  Graded At: $($submission.gradedAt)" -ForegroundColor White
                Write-Host ""
            }
        }
    }

    # 8. Test Error Cases
    # 6. Monitor System Status
    Write-Host "`n6. System Monitoring Information..." -ForegroundColor Yellow
    
    Write-Host "🔄 Auto-Grading Schedule:" -ForegroundColor Cyan
    Write-Host "  • Runs automatically every 30 minutes" -ForegroundColor White
    Write-Host "  • Processes all eligible assignments system-wide" -ForegroundColor White
    Write-Host "  • No manual intervention required" -ForegroundColor White
    
    Write-Host "`n📋 Eligibility Criteria for Auto-Grading:" -ForegroundColor Cyan
    Write-Host "  • Assignment has due date" -ForegroundColor White
    Write-Host "  • Assignment is overdue (past due date)" -ForegroundColor White
    Write-Host "  • Assignment does NOT allow late submission" -ForegroundColor White
    Write-Host "  • Assignment is published" -ForegroundColor White

} catch {
    Write-Host "❌ Error occurred: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $streamReader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        $errorBody = $streamReader.ReadToEnd()
        Write-Host "Error details: $errorBody" -ForegroundColor Red
        $streamReader.Close()
    }
}

Write-Host "`n=== Automatic Grading System Summary ===" -ForegroundColor Cyan
Write-Host "🤖 System: Fully automated - no manual API calls needed" -ForegroundColor Green
Write-Host "⏰ Schedule: Every 30 minutes via Spring @Scheduled" -ForegroundColor Green
Write-Host "🎯 Purpose: Automatically assign 0 score to students who didn't submit overdue assignments" -ForegroundColor Green
Write-Host "📋 Process:" -ForegroundColor Yellow
Write-Host "  • System automatically scans for expired assignments" -ForegroundColor White
Write-Host "  • Finds all students enrolled in courses with expired assignments" -ForegroundColor White
Write-Host "  • Creates zero-score submissions for non-submitters" -ForegroundColor White
Write-Host "  • Sets automatic feedback message" -ForegroundColor White
Write-Host "  • Marks as graded with current timestamp" -ForegroundColor White
Write-Host "📊 Monitoring: Check submissions and statistics to see auto-graded results" -ForegroundColor Yellow

Write-Host "`n🎉 Monitoring Test Completed!" -ForegroundColor Green
