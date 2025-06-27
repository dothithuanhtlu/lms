# 🗄️ HƯỚNG DẪN XÓA COLUMN GRADED_BY

## ❌ **LỖI HIỆN TẠI**
```
Error Code: 1828. Cannot drop column 'graded_by': needed in a foreign key constraint 'FKmjfva3nkq8vlatkp2mk3678hh'
```

## ✅ **GIẢI PHÁP - THỰC HIỆN THEO THỨ TỰ**

### **Bước 1: Drop Foreign Key Constraint trước**
```sql
ALTER TABLE submissions DROP FOREIGN KEY FKmjfva3nkq8vlatkp2mk3678hh;
```

### **Bước 2: Drop Column sau**
```sql
ALTER TABLE submissions DROP COLUMN graded_by;
```

### **Bước 3: Kiểm tra kết quả**
```sql
DESCRIBE submissions;
```

---

## 🛠️ **CÁCH THỰC HIỆN**

### **Option 1: MySQL Workbench**
1. Mở MySQL Workbench
2. Connect vào database
3. Chạy lần lượt 3 câu lệnh SQL ở trên

### **Option 2: Command Line**
```bash
mysql -u your_username -p your_database_name

# Sau khi nhập password, chạy:
ALTER TABLE submissions DROP FOREIGN KEY FKmjfva3nkq8vlatkp2mk3678hh;
ALTER TABLE submissions DROP COLUMN graded_by;
DESCRIBE submissions;
```

### **Option 3: phpMyAdmin (nếu có)**
1. Vào phpMyAdmin
2. Chọn database
3. Chọn table `submissions`
4. Vào tab SQL
5. Chạy 2 câu lệnh ALTER TABLE

---

## 🔍 **KIỂM TRA KẾT QUẢ**

Sau khi chạy xong, table `submissions` sẽ có cấu trúc:
```
+---------------+--------------+------+-----+---------+----------------+
| Field         | Type         | Null | Key | Default | Extra          |
+---------------+--------------+------+-----+---------+----------------+
| id            | bigint       | NO   | PRI | NULL    | auto_increment |
| file_path     | varchar(255) | YES  |     | NULL    |                |
| submitted_at  | datetime(6)  | YES  |     | NULL    |                |
| graded_at     | datetime(6)  | YES  |     | NULL    |                |
| score         | float        | YES  |     | NULL    |                |
| feedback      | text         | YES  |     | NULL    |                |
| status        | varchar(255) | NO   |     | NULL    |                |
| is_late       | bit(1)       | YES  |     | NULL    |                |
| assignment_id | bigint       | NO   | MUL | NULL    |                |
| student_id    | bigint       | NO   | MUL | NULL    |                |
+---------------+--------------+------+-----+---------+----------------+
```

**Chú ý:** `graded_by` column sẽ KHÔNG còn trong danh sách!

---

## 🔙 **ROLLBACK (nếu cần)**

Nếu muốn phục hồi lại:
```sql
-- Thêm lại column
ALTER TABLE submissions ADD COLUMN graded_by BIGINT;

-- Thêm lại foreign key
ALTER TABLE submissions ADD CONSTRAINT FKmjfva3nkq8vlatkp2mk3678hh 
    FOREIGN KEY (graded_by) REFERENCES users(id);

-- Cập nhật data cho các submission đã grade
UPDATE submissions s 
SET graded_by = (
    SELECT c.teacher_id 
    FROM assignments a 
    JOIN courses c ON a.course_id = c.id 
    WHERE a.id = s.assignment_id
) 
WHERE s.graded_at IS NOT NULL;
```

---

## ⚠️ **LƯU Ý QUAN TRỌNG**

1. **Backup trước khi thực hiện:**
   ```bash
   mysqldump -u username -p database_name submissions > submissions_backup.sql
   ```

2. **Kiểm tra ứng dụng sau khi drop:**
   - Code đã compile thành công ✅
   - Test lại các API grading ✅
   - Đảm bảo không có lỗi runtime ✅

3. **Constraint name có thể khác:**
   - Nếu `FKmjfva3nkq8vlatkp2mk3678hh` không đúng
   - Chạy lệnh này để tìm tên constraint:
   ```sql
   SHOW CREATE TABLE submissions;
   ```

---

## 🎯 **THỰC HIỆN NGAY**

**Copy và paste 2 lệnh này vào MySQL:**
```sql
ALTER TABLE submissions DROP FOREIGN KEY FKmjfva3nkq8vlatkp2mk3678hh;
ALTER TABLE submissions DROP COLUMN graded_by;
```

Sau đó chạy test để đảm bảo application hoạt động bình thường! 🚀
