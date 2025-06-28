package vn.doan.lms.repository;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.doan.lms.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
        List<User> findByDepartmentIdAndRole_NameRole(Long departmentId, String roleName);

        User findOneByUserCode(String userCode);

        void deleteUserByUserCode(String userCode);

        List<User> findAllByDepartmentId(Long departmentId);

        User findByUserCodeAndRefreshToken(String userCode, String refreshToken);

        boolean existsByPhone(String phone);

        boolean existsByUserCode(String userCode);

        boolean existsByEmail(String email);

        long countByRole_NameRole(String roleName);

        Optional<User> findByEmail(String email);

        @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
                        "FROM User u " +
                        "JOIN u.classRoom c " +
                        "JOIN c.major m " +
                        "JOIN m.department d " +
                        "WHERE u.userCode = :userCode AND d.id = :departmentId")
        boolean existsByUserCodeAndDepartmentId(@Param("userCode") String userCode,
                        @Param("departmentId") Long departmentId);

        List<User> findAllByRoleId(Long roleId);

        long countByClassRoomId(Long classRoomId);

        @Query(value = "CALL update_stu(:p_class_id, :p_full_name, :p_email, :p_date_of_birth, :p_address, :p_gender, :p_stu_code, :p_role_id, :p_result)", nativeQuery = true)
        void updateStudent(
                        @Param("p_class_id") Long classId,
                        @Param("p_full_name") String fullName,
                        @Param("p_email") String email,
                        @Param("p_date_of_birth") Date dateOfBirth,
                        @Param("p_address") String address,
                        @Param("p_gender") String gender,
                        @Param("p_stu_code") String stuCode,
                        @Param("p_role_id") Integer roleId,
                        @Param("p_result") Integer result);

        /**
         * Creates a new student using a stored procedure.
         *
         * @param classId     the ID of the class the student belongs to
         * @param fullName    the full name of the student
         * @param email       the email address of the student
         * @param dateOfBirth the date of birth of the student
         * @param address     the address of the student
         * @param gender      the gender of the student
         * @param stuCode     the unique student code
         * @param result      the academic result of the student
         */
        @Query(value = "CALL create_stu(:p_class_id, :p_user_code, :p_password, :p_email, :p_full_name, :p_address, :p_gender, :p_phone, :p_date_of_birth, :p_role_id, :p_create_at, :p_create_by, :p_result)", nativeQuery = true)
        void createStudent(
                        @Param("p_class_id") Long classId,
                        @Param("p_user_code") String userCode,
                        @Param("p_password") String password,
                        @Param("p_email") String email,
                        @Param("p_full_name") String fullName,
                        @Param("p_address") String address,
                        @Param("p_gender") String gender,
                        @Param("p_phone") String phone,
                        @Param("p_date_of_birth") Date dateOfBirth,
                        @Param("p_role_id") Long roleId,
                        @Param("p_create_at") Instant createAt,
                        @Param("p_create_by") String createBy,
                        @Param("p_result") Integer result);

        /**
         * Count students enrolled in a specific course
         * 
         * @param courseId the ID of the course
         * @return the number of students enrolled in the course
         */
        @Query("SELECT COUNT(DISTINCT u) FROM User u " +
                        "JOIN Enrollment e ON u.id = e.student.id " +
                        "WHERE e.course.id = :courseId AND u.role.nameRole = 'STUDENT'")
        Long countStudentsByCourseId(@Param("courseId") Long courseId);

        /**
         * Get all students enrolled in a specific course
         * 
         * @param courseId the ID of the course
         * @return list of students enrolled in the course
         */
        @Query("SELECT DISTINCT u FROM User u " +
                        "JOIN Enrollment e ON u.id = e.student.id " +
                        "WHERE e.course.id = :courseId AND u.role.nameRole = 'STUDENT'")
        List<User> findStudentsByCourseId(@Param("courseId") Long courseId);
}
