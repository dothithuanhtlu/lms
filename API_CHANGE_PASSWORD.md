# API Change Password Documentation

## 1. User Self Change Password

### Endpoint: PUT /auth/change-password

**Description:** Allows authenticated users to change their own password.

**Authentication:** Required (JWT token)

**Request Body:**
```json
{
  "oldPassword": "current_password",
  "newPassword": "new_password"
}
```

**Response:**
- **200 OK:** Password changed successfully
  ```json
  "Đã thay đổi mật khẩu thành công"
  ```

**Required Permission:** `CHANGE_PASSWORD`
**Available to:** All authenticated users (ADMIN, TEACHER, STUDENT)

---

## 2. Admin Change User Password

### Endpoint: PUT /admin/user/{userCode}/change-password

**Description:** Allows admin to change password for any user by their user code.

**Authentication:** Required (JWT token with admin role)

**Path Parameters:**
- `userCode`: String - The unique user code of the target user

**Request Body:**
```json
{
  "oldPassword": "user_current_password",
  "newPassword": "new_password"
}
```

**Response:**
- **200 OK:** Password changed successfully
  ```json
  "Đã thay đổi mật khẩu thành công cho user: USER001"
  ```

**Required Permission:** `ADMIN_CHANGE_USER_PASSWORD`
**Available to:** ADMIN users only

---

## Common Error Responses

**400 Bad Request:**
- Invalid old password: "Mật khẩu cũ không chính xác"
- Same password: "Mật khẩu mới không được trùng với mật khẩu cũ"

**404 Not Found:**
- User not found: "User code không tồn tại: {userCode}"
- Current user not found: "Không tìm thấy user với email: {email}"

**403 Forbidden:**
- Insufficient permissions

---

## Implementation Details

### Security Features:
1. **Password Validation:** BCrypt password matching
2. **Permission Control:** Dynamic permission checking
3. **User Validation:** Existence check before password change
4. **Audit Trail:** Updated timestamp on password change

### Business Logic:
1. **Self Password Change:** User can only change their own password
2. **Admin Password Change:** Admin can change any user's password
3. **Password Requirements:** New password must differ from old password
4. **Validation:** Only old password and new password required

### Database Updates:
- Password field encrypted with BCrypt
- UpdatedAt timestamp updated
- Audit trail maintained

---

## Usage Examples

### User Self Change Password:
```bash
curl -X PUT "http://localhost:8080/auth/change-password" \
  -H "Authorization: Bearer USER_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "oldPassword": "oldpass123",
    "newPassword": "newpass456"
  }'
```

### Admin Change User Password:
```bash
curl -X PUT "http://localhost:8080/admin/user/USER001/change-password" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "oldPassword": "user_old_pass",
    "newPassword": "user_new_pass"
  }'
```

---

## Permission Configuration

### Permissions Added:
1. `CHANGE_PASSWORD` - For user self password change
2. `ADMIN_CHANGE_USER_PASSWORD` - For admin changing user passwords

### Role Assignments:
- **ADMIN:** Both permissions
- **TEACHER:** CHANGE_PASSWORD only
- **STUDENT:** CHANGE_PASSWORD only

### Endpoint Patterns:
- `/auth/change-password` - Self change password
- `/admin/user/{userCode}/change-password` - Admin change user password
