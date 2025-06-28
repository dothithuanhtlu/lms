# Student Submittable Unsubmitted Assignment Count API - Implementation Summary

## Completed Implementation

### 🎯 **API Purpose**
API để lấy tổng số bài tập mà sinh viên **vẫn có thể nộp được**, tức là:
- Các bài tập **chưa hết hạn** (still due)
- Các bài tập **đã hết hạn nhưng được phép nộp muộn** (overdue but allows late submission)

### 🔧 **Technical Implementation**

#### 1. Interface Method
**File**: `ISubmissionService.java`
```java
/**
 * Get count of unsubmitted assignments that student can still submit
 * (assignments that are either not due yet OR overdue but allow late submission)
 */
long getUnsubmittedAssignmentCountByStudentId(Long studentId);
```

#### 2. Repository Query
**File**: `AssignmentRepository.java`
```java
@Query("SELECT COUNT(a) FROM Assignment a " +
       "JOIN a.course.enrollments e " +
       "WHERE e.student.id = :studentId " +
       "AND a.isPublished = true " +
       "AND NOT EXISTS (SELECT s FROM Submission s WHERE s.assignment.id = a.id AND s.student.id = :studentId) " +
       "AND (a.dueDate > :currentTime OR (a.dueDate <= :currentTime AND a.allowLateSubmission = true))")
long countUnsubmittedSubmittableAssignmentsByStudentId(@Param("studentId") Long studentId, 
                                                      @Param("currentTime") LocalDateTime currentTime);
```

#### 3. Service Implementation
**File**: `SubmissionService.java`
```java
@Override
public long getUnsubmittedAssignmentCountByStudentId(Long studentId) {
    // Validate student exists
    User student = userRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
    
    LocalDateTime now = LocalDateTime.now();
    long count = assignmentRepository.countUnsubmittedSubmittableAssignmentsByStudentId(studentId, now);
    
    return count;
}
```

#### 4. Controller Endpoint
**File**: `SubmissionController.java`
```java
@GetMapping("/student/{studentId}/unsubmitted-count")
public ResponseEntity<?> getUnsubmittedAssignmentCount(
        @PathVariable("studentId") Long studentId,
        Authentication authentication) {
    
    long count = submissionService.getUnsubmittedAssignmentCountByStudentId(studentId);
    
    return ResponseEntity.ok(Map.of(
        "studentId", studentId,
        "unsubmittedCount", count,
        "message", String.format("Student has %d assignments that can still be submitted", count)
    ));
}
```

### 📋 **Logic Criteria**

#### ✅ **Included Assignments**
1. **Student enrolled** in course
2. **Assignment published** (`isPublished = true`)
3. **Student hasn't submitted** (no submission record exists)
4. **Assignment submittable**:
   - **Due date in future** (`dueDate > currentTime`), OR
   - **Overdue but allows late submission** (`dueDate <= currentTime AND allowLateSubmission = true`)

#### ❌ **Excluded Assignments**
1. **Not published** assignments
2. **Already submitted** assignments
3. **Overdue assignments that don't allow late submission**

### 🔗 **API Details**

#### Endpoint
```
GET /api/submissions/student/{studentId}/unsubmitted-count
```

#### Response Format
```json
{
  "studentId": 123,
  "unsubmittedCount": 5,
  "message": "Student has 5 assignments that can still be submitted"
}
```

### 📊 **Example Scenarios**

#### Scenario 1: Mixed States
- Assignment A: Due tomorrow (not submitted) → **✅ COUNTED**
- Assignment B: Due yesterday, allows late (not submitted) → **✅ COUNTED**  
- Assignment C: Due yesterday, no late allowed (not submitted) → **❌ NOT COUNTED**
- Assignment D: Due next week (already submitted) → **❌ NOT COUNTED**

**Result**: `unsubmittedCount = 2`

#### Scenario 2: All Recoverable
- Assignment A: Due in 3 days (not submitted) → **✅ COUNTED**
- Assignment B: Due yesterday, allows late (not submitted) → **✅ COUNTED**
- Assignment C: Due last week, allows late (not submitted) → **✅ COUNTED**

**Result**: `unsubmittedCount = 3`

#### Scenario 3: No Recoverable
- Assignment A: Due yesterday, no late allowed → **❌ NOT COUNTED**
- Assignment B: Already submitted → **❌ NOT COUNTED**

**Result**: `unsubmittedCount = 0`

### 🧪 **Testing**

#### Files Created
- `test_simple_submittable_count.ps1` - Test script
- `STUDENT_SUBMITTABLE_UNSUBMITTED_COUNT_API.md` - Documentation

#### Compilation Status
```bash
./gradlew compileJava
# ✅ BUILD SUCCESSFUL
```

### 💡 **Use Cases**

#### For Students
- **Dashboard**: Show actionable pending work
- **Study Planning**: Focus on recoverable assignments
- **Time Management**: Prioritize what can still be done

#### For Teachers
- **Progress Monitoring**: See which students can still catch up
- **Intervention**: Identify recoverable situations
- **Course Management**: Track actionable vs. missed work

#### For Academic Advisors
- **Student Support**: Focus on recoverable assignments
- **Performance Tracking**: Monitor actionable workload
- **Intervention Planning**: Help students prioritize

### 🔄 **Key Differences from Original Request**

**Original**: "tổng bài tập chưa nộp của 1 sinh viên"
**Updated**: "bài tập chưa nộp nhưng nó phải còn hạn hoặc hết hạn nhưng được phép nộp muộn"

The implementation now focuses on **actionable** assignments rather than all unsubmitted ones, making it more useful for student planning and teacher intervention.

### ✅ **Status: COMPLETE**

The API successfully provides a count of assignments that students can still submit, helping with:
- Student productivity and planning
- Teacher intervention and support
- Academic advising and counseling
- Course management and tracking

**Endpoint**: `GET /api/submissions/student/{studentId}/unsubmitted-count`
**Logic**: `(NOT_DUE_YET) OR (OVERDUE_AND_ALLOWS_LATE)`
