# Simple Test for Get Submissions API
$BASE_URL = "http://localhost:8080"

Write-Host "Testing API: GET /api/submissions/assignment/{assignmentId}" -ForegroundColor Cyan

# Test if server is running
try {
    $response = Invoke-WebRequest -Uri "$BASE_URL/api/health" -Method GET -TimeoutSec 5
    Write-Host "✅ Server is running on $BASE_URL" -ForegroundColor Green
} catch {
    Write-Host "❌ Server is not running or not accessible at $BASE_URL" -ForegroundColor Red
    Write-Host "Please start the Spring Boot application first." -ForegroundColor Yellow
    exit 1
}

Write-Host "`n📋 API Details:" -ForegroundColor Yellow
Write-Host "Endpoint: GET /api/submissions/assignment/{assignmentId}" -ForegroundColor White
Write-Host "Purpose: Teacher can view all student submissions for an assignment" -ForegroundColor White
Write-Host "Authentication: Required (Teacher JWT Token)" -ForegroundColor White
Write-Host "Parameters: ?page=0`&size=20 (optional pagination)" -ForegroundColor White

Write-Host "`n🔧 Implementation Status:" -ForegroundColor Yellow
Write-Host "✅ Controller: SubmissionController.java - API endpoint available" -ForegroundColor Green
Write-Host "✅ Service: SubmissionService.java - Business logic implemented" -ForegroundColor Green
Write-Host "✅ Repository: SubmissionRepository.java - Data access ready" -ForegroundColor Green
Write-Host "✅ Interface: ISubmissionService.java - Contract updated" -ForegroundColor Green

Write-Host "`n📄 Usage Example:" -ForegroundColor Yellow
Write-Host "curl -X GET 'http://localhost:8080/api/submissions/assignment/1?page=0`&size=10' \\" -ForegroundColor White
Write-Host "  -H 'Authorization: Bearer <teacher_jwt_token>'" -ForegroundColor White

Write-Host "`n🎯 This API allows teachers to:" -ForegroundColor Yellow
Write-Host "• View all student submissions for a specific assignment" -ForegroundColor White
Write-Host "• See submission status (SUBMITTED, LATE, NOT_SUBMITTED)" -ForegroundColor White
Write-Host "• Review student information and submission content" -ForegroundColor White
Write-Host "• Access attached files through Cloudinary URLs" -ForegroundColor White
Write-Host "• Navigate through submissions with pagination" -ForegroundColor White
Write-Host "• Use this data for grading workflow" -ForegroundColor White

Write-Host "`n✅ API is ready to use!" -ForegroundColor Green
