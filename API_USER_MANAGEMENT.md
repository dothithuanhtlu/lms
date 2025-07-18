# API Documentation - User Management

## Change Password API

### Endpoint: PUT /auth/change-password

**Description:** Allows authenticated users to change their password.

**Authentication:** Required (JWT token)

**Request Body:**
```json
{
  "oldPassword": "current_password",
  "newPassword": "new_password",
  "confirmPassword": "new_password"
}
```

**Validation Rules:**
- `oldPassword`: Not blank
- `newPassword`: Not blank, 6-100 characters
- `confirmPassword`: Not blank, must match newPassword

**Response:**
- **200 OK:** Password changed successfully
  ```json
  "Đã thay đổi mật khẩu thành công"
  ```

**Error Responses:**
- **400 Bad Request:** 
  - Invalid old password: "Mật khẩu cũ không chính xác"
  - Password mismatch: "Mật khẩu mới và xác nhận mật khẩu không khớp"
  - Same password: "Mật khẩu mới không được trùng với mật khẩu cũ"
  - Cannot identify user: "Không thể xác định user hiện tại"
- **404 Not Found:** User not found: "Không tìm thấy user với email: {email}"

**Required Permission:** `CHANGE_PASSWORD`

---

## Get User Details API

### Endpoint: GET /admin/user/{userCode}

**Description:** Retrieves detailed information about a user by their user code.

**Authentication:** Required (JWT token)

**Path Parameters:**
- `userCode`: String - The unique user code

**Response:**
- **200 OK:** User details found
  ```json
  {
    "userCode": "USER001",
    "email": "user@example.com",
    "fullName": "John Doe",
    "dateOfBirth": "1990-01-01",
    "gender": "MALE",
    "address": "123 Main St",
    "phone": "0123456789",
    "roleName": "Student"
  }
  ```

**Error Responses:**
- **404 Not Found:** User not found: "User code không tồn tại: {userCode}"

**Required Permission:** `VIEW_USER_DETAILS` (Admin access)

---

## Implementation Details

### Security Configuration

Both APIs are integrated with the dynamic permission system:

1. **Change Password API:**
   - Permission: `CHANGE_PASSWORD`
   - Available to: All authenticated users (ADMIN, TEACHER, STUDENT)
   - Endpoint pattern: `PUT /auth/change-password`

2. **Get User Details API:**
   - Permission: `VIEW_USER_DETAILS` (admin functionality)
   - Available to: ADMIN users only
   - Endpoint pattern: `GET /admin/user/{userCode}`

### Service Layer

**UserService.changePassword():**
- Validates current user authentication
- Verifies old password using BCrypt
- Ensures new password differs from old password
- Encrypts new password with BCrypt
- Updates user record with new password and timestamp

**UserService.getUserDetailsByUserCode():**
- Validates user code existence
- Retrieves comprehensive user information
- Returns AdminDTO with all user details

### Data Validation

**ChangePasswordRequest:**
- Jakarta Validation annotations
- Custom validation for password strength
- Confirmation password matching

**Security Features:**
- Password encryption using BCrypt
- JWT token validation
- Permission-based access control
- Audit trail with updatedAt timestamp

---

## Usage Examples

### Change Password Example

```bash
curl -X PUT "http://localhost:8080/auth/change-password" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "oldPassword": "oldpass123",
    "newPassword": "newpass456",
    "confirmPassword": "newpass456"
  }'
```

### Get User Details Example

```bash
curl -X GET "http://localhost:8080/admin/user/USER001" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Notes

1. **Password Policy:** Minimum 6 characters, maximum 100 characters
2. **Security:** All password operations use BCrypt hashing
3. **Audit:** Password changes are timestamped with `updatedAt`
4. **Authorization:** Dynamic permission checking via enum system
5. **Error Handling:** Comprehensive error messages in Vietnamese
6. **Validation:** Input validation at both DTO and service levels
