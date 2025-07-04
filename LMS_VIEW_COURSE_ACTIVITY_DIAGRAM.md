# Activity Diagram: View Course Use Case

## Overview
Activity diagram cho use case "Xem khóa học" (View Course) với các actor: Admin, Teacher, Student.

## Activity Diagram - View Course Use Case

### Complete Activity Diagram
```plantuml
@startuml "View Course Activity Diagram"
!theme plain

title View Course Use Case - Activity Diagram

|#LightBlue|User|
start
:User truy cập hệ thống;
:User đăng nhập;

|#LightGreen|System|
:Xác thực user;
:Kiểm tra quyền truy cập;

if (User đã đăng nhập?) then (yes)
  |#LightBlue|User|
  :Chọn "Xem khóa học";
  
  |#LightGreen|System|
  :Lấy danh sách khóa học theo quyền;
  
  if (Có khóa học?) then (yes)
    :Hiển thị danh sách khóa học;
    
    |#LightBlue|User|
    :Chọn khóa học cụ thể;
    
    |#LightGreen|System|
    :Kiểm tra quyền xem khóa học;
    
    if (Có quyền xem?) then (yes)
      :Lấy thông tin chi tiết khóa học;
      :Lấy thông tin assignments;
      :Lấy thông tin lessons;
      :Lấy thông tin enrollments;
      :Lấy thông tin submissions (nếu có);
      
      if (User là Student?) then (yes)
        :Hiển thị thông tin cơ bản khóa học;
        :Hiển thị assignments và lessons;
        :Hiển thị submission status;
      elseif (User là Teacher?) then (yes)
        :Hiển thị thông tin chi tiết khóa học;
        :Hiển thị toàn bộ assignments và lessons;
        :Hiển thị danh sách students;
        :Hiển thị thống kê submissions;
      elseif (User là Admin?) then (yes)
        :Hiển thị thông tin quản trị khóa học;
        :Hiển thị toàn bộ thông tin chi tiết;
        :Hiển thị thống kê và báo cáo;
        :Hiển thị lịch sử hoạt động;
      endif
      
      :Hiển thị thông tin khóa học;
      
      |#LightBlue|User|
      :Xem thông tin khóa học;
      
      repeat
        :Thực hiện hành động khác;
        
        if (Refresh thông tin?) then (yes)
          |#LightGreen|System|
          :Cập nhật thông tin mới;
          :Hiển thị thông tin đã cập nhật;
        elseif (Xem assignment?) then (yes)
          |#LightGreen|System|
          :Chuyển đến chi tiết assignment;
        elseif (Xem lesson?) then (yes)
          |#LightGreen|System|
          :Chuyển đến chi tiết lesson;
        elseif (Xem students?) then (yes)
          |#LightGreen|System|
          :Hiển thị danh sách students;
        endif
        
        |#LightBlue|User|
        :Tiếp tục xem thông tin?;
      repeat while (Có) is (yes)
      ->no;
      
    else (no)
      :Hiển thị thông báo "Không có quyền truy cập";
    endif
  else (no)
    :Hiển thị thông báo "Không có khóa học";
  endif
else (no)
  :Chuyển đến trang đăng nhập;
endif

:Kết thúc;
stop

@enduml
```

### Simplified Activity Diagram by Actor

#### 1. Student View Course Activity
```plantuml
@startuml "Student View Course Activity"
!theme plain

title Student - View Course Activity

|#LightBlue|Student|
start
:Đăng nhập vào hệ thống;
:Truy cập "My Courses";

|#LightGreen|System|
:Lấy danh sách khóa học đã enroll;
:Hiển thị danh sách courses;

|#LightBlue|Student|
:Chọn khóa học để xem;

|#LightGreen|System|
:Kiểm tra enrollment status;
:Lấy thông tin course details;
:Lấy assignments của course;
:Lấy lessons của course;
:Lấy submission status;
:Hiển thị course overview;

|#LightBlue|Student|
:Xem thông tin khóa học;
:Xem assignments;
:Xem lessons;
:Xem submission status;

repeat
  :Thực hiện hành động;
  
  if (Xem assignment detail?) then (yes)
    |#LightGreen|System|
    :Chuyển đến assignment detail;
  elseif (Xem lesson detail?) then (yes)
    |#LightGreen|System|
    :Chuyển đến lesson detail;
  elseif (Submit assignment?) then (yes)
    |#LightGreen|System|
    :Chuyển đến submission form;
  endif
  
  |#LightBlue|Student|
  :Tiếp tục xem course?;
repeat while (Có) is (yes)
->no;

stop
@enduml
```

#### 2. Teacher View Course Activity
```plantuml
@startuml "Teacher View Course Activity"
!theme plain

title Teacher - View Course Activity

|#LightBlue|Teacher|
start
:Đăng nhập vào hệ thống;
:Truy cập "My Courses";

|#LightGreen|System|
:Lấy danh sách courses của teacher;
:Hiển thị danh sách courses;

|#LightBlue|Teacher|
:Chọn khóa học để quản lý;

|#LightGreen|System|
:Kiểm tra quyền teacher;
:Lấy chi tiết course;
:Lấy danh sách students;
:Lấy assignments và lessons;
:Lấy thống kê submissions;
:Hiển thị course management view;

|#LightBlue|Teacher|
:Xem thông tin chi tiết course;
:Xem danh sách students;
:Xem assignments và lessons;
:Xem thống kê submissions;

repeat
  :Thực hiện hành động quản lý;
  
  if (Quản lý assignments?) then (yes)
    |#LightGreen|System|
    :Chuyển đến assignment management;
  elseif (Quản lý lessons?) then (yes)
    |#LightGreen|System|
    :Chuyển đến lesson management;
  elseif (Xem submissions?) then (yes)
    |#LightGreen|System|
    :Hiển thị submissions của students;
  elseif (Chấm điểm?) then (yes)
    |#LightGreen|System|
    :Chuyển đến grading interface;
  endif
  
  |#LightBlue|Teacher|
  :Tiếp tục quản lý course?;
repeat while (Có) is (yes)
->no;

stop
@enduml
```

#### 3. Admin View Course Activity
```plantuml
@startuml "Admin View Course Activity"
!theme plain

title Admin - View Course Activity

|#LightBlue|Admin|
start
:Đăng nhập với quyền Admin;
:Truy cập "Course Management";

|#LightGreen|System|
:Lấy toàn bộ danh sách courses;
:Hiển thị courses với thống kê;

|#LightBlue|Admin|
:Chọn course để quản trị;

|#LightGreen|System|
:Kiểm tra quyền admin;
:Lấy chi tiết đầy đủ course;
:Lấy thông tin teachers và students;
:Lấy thống kê hoạt động;
:Lấy lịch sử thay đổi;
:Hiển thị admin course view;

|#LightBlue|Admin|
:Xem thông tin quản trị course;
:Xem thống kê chi tiết;
:Xem báo cáo hoạt động;
:Xem lịch sử thay đổi;

repeat
  :Thực hiện hành động quản trị;
  
  if (Quản lý enrollments?) then (yes)
    |#LightGreen|System|
    :Chuyển đến enrollment management;
  elseif (Xem system logs?) then (yes)
    |#LightGreen|System|
    :Hiển thị system activity logs;
  elseif (Tạo báo cáo?) then (yes)
    |#LightGreen|System|
    :Tạo và xuất báo cáo;
  elseif (Cấu hình course?) then (yes)
    |#LightGreen|System|
    :Chuyển đến course configuration;
  endif
  
  |#LightBlue|Admin|
  :Tiếp tục quản trị course?;
repeat while (Có) is (yes)
->no;

stop
@enduml
```

### Business Rules & Constraints

1. **Authentication Required**: Tất cả users phải đăng nhập trước khi xem course
2. **Authorization Check**: Kiểm tra quyền truy cập dựa trên role
3. **Data Filtering**: Dữ liệu hiển thị được filter theo quyền của user
4. **Performance**: Lazy loading cho data lớn
5. **Error Handling**: Xử lý lỗi khi không có quyền hoặc course không tồn tại

### Data Access Patterns

- **Student**: Chỉ xem courses đã enroll, limited information
- **Teacher**: Xem courses được assign, full management view
- **Admin**: Xem tất cả courses, full administrative view

### Security Considerations

- Authentication validation
- Role-based access control
- Data filtering by permissions
- Audit trail for admin actions

## Usage Notes

1. **PlantUML**: Copy từng section riêng biệt để tránh lỗi "Request header too large"
2. **Draw.io**: Có thể import qua PlantUML plugin hoặc vẽ manual
3. **Customization**: Có thể điều chỉnh swim lanes và colors theo yêu cầu
