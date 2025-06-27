# Test Assignment Info Update API

This script tests the `/api/assignments/update/{assignmentId}` endpoint which only updates assignment information fields (no files).

## API Endpoint
```
PUT /api/assignments/update/{assignmentId}
Content-Type: application/json
```

## Request Body Fields
Only these 6 fields are supported:

### Required:
- `title` (string, max 200 chars): Assignment title
- `dueDate` (string, ISO format): Due date and time

### Optional:
- `description` (string): Assignment description
- `maxScore` (number, min 0): Maximum score for the assignment
- `isPublished` (boolean): Whether the assignment is published
- `allowLateSubmission` (boolean): Whether late submissions are allowed

## PowerShell Test Script

```powershell
# Test Assignment Info Update
$assignmentId = 1  # Replace with actual assignment ID
$apiUrl = "http://localhost:8080/api/assignments/update/$assignmentId"

# Test data - only information fields
$requestBody = @{
    title = "Updated Assignment Title"
    dueDate = "2025-07-01T23:59:59"
    description = "Updated assignment description"
    maxScore = 100
    isPublished = $true
    allowLateSubmission = $false
} | ConvertTo-Json

Write-Host "Testing Assignment Info Update API..." -ForegroundColor Yellow
Write-Host "URL: $apiUrl" -ForegroundColor Cyan
Write-Host "Request Body:" -ForegroundColor Cyan
$requestBody

try {
    $response = Invoke-RestMethod -Uri $apiUrl -Method PUT -Body $requestBody -ContentType "application/json"
    
    Write-Host "`n✅ SUCCESS!" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 5
    
} catch {
    Write-Host "`n❌ ERROR!" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "Error Message: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body: $responseBody" -ForegroundColor Red
    }
}
```

## JavaScript/Fetch Example

```javascript
// Frontend JavaScript example
async function updateAssignmentInfo(assignmentId, updateData) {
    const response = await fetch(`/api/assignments/update/${assignmentId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            title: updateData.title,
            dueDate: updateData.dueDate,  // ISO format: "2025-07-01T23:59:59"
            description: updateData.description,      // optional
            maxScore: updateData.maxScore,           // optional
            isPublished: updateData.isPublished,     // optional
            allowLateSubmission: updateData.allowLateSubmission  // optional
        })
    });
    
    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return await response.json();
}

// Usage example
updateAssignmentInfo(1, {
    title: "Updated Assignment Title",
    dueDate: "2025-07-01T23:59:59",
    description: "Updated description",
    maxScore: 100,
    isPublished: true,
    allowLateSubmission: false
}).then(result => {
    console.log('Assignment updated:', result);
}).catch(error => {
    console.error('Error updating assignment:', error);
});
```

## Key Points

1. **No File Handling**: This endpoint only updates assignment information, it does not touch any files
2. **JSON Body**: Uses `@RequestBody` with JSON (not form data or multipart)
3. **Validation**: Title is required and cannot be blank, dueDate is required and must be valid ISO format
4. **Optional Fields**: description, maxScore, isPublished, and allowLateSubmission are optional
5. **No Course Change**: This endpoint does not allow changing the course assignment belongs to

## Different from File Update API

If you need to update files, use the separate endpoint:
```
PUT /api/assignments/{assignmentId}/update-with-files
Content-Type: multipart/form-data
```

That endpoint replaces ALL files and can also update the same info fields.
