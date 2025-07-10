package vn.doan.lms.util;

import lombok.extern.slf4j.Slf4j;
import vn.doan.lms.domain.enums.Permission;
import vn.doan.lms.domain.enums.PermissionRole;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class để xử lý phân quyền động
 * Không sử dụng @Service để tránh circular dependency với SecurityUtil
 */
@Slf4j
public class PermissionUtil {

    /**
     * Lấy danh sách quyền từ role name sử dụng enum PermissionRole
     * 
     * @param roleName Tên role (Admin, Teacher, Student)
     * @return Danh sách quyền theo format Spring Security (ROLE_PERMISSION_NAME)
     */
    public static List<String> getPermissionsByRole(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            log.warn("Role name is null or empty, returning empty permissions list");
            return new ArrayList<>();
        }

        try {
            // Lấy danh sách Permission từ enum PermissionRole
            List<Permission> permissions = PermissionRole.getPermissionsForRole(roleName.trim().toUpperCase());

            if (permissions.isEmpty()) {
                log.warn("No permissions found for role: {}", roleName);
                return new ArrayList<>();
            }

            // Chuyển đổi thành format Spring Security: ROLE_PERMISSION_NAME
            List<String> authorities = permissions.stream()
                    .map(permission -> "ROLE_" + permission.getName())
                    .toList();

            log.debug("Generated {} permissions for role {}: {}", authorities.size(), roleName, authorities);
            return authorities;

        } catch (Exception e) {
            log.error("Error getting permissions for role: {}", roleName, e);
            return new ArrayList<>();
        }
    }

    /**
     * Lấy danh sách tên quyền (không có prefix ROLE_) từ role name
     * 
     * @param roleName Tên role
     * @return Danh sách tên quyền
     */
    public static List<String> getPermissionNames(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return PermissionRole.getPermissionsForRole(roleName.trim().toUpperCase())
                    .stream()
                    .map(Permission::getName)
                    .toList();
        } catch (Exception e) {
            log.error("Error getting permission names for role: {}", roleName, e);
            return new ArrayList<>();
        }
    }

    /**
     * Lấy danh sách mô tả quyền từ role name
     * 
     * @param roleName Tên role
     * @return Danh sách mô tả quyền
     */
    public static List<String> getPermissionDescriptions(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return PermissionRole.getPermissionDescriptions(roleName.trim().toUpperCase());
        } catch (Exception e) {
            log.error("Error getting permission descriptions for role: {}", roleName, e);
            return new ArrayList<>();
        }
    }

    /**
     * Kiểm tra xem role có quyền cụ thể không
     * 
     * @param roleName       Tên role
     * @param permissionName Tên quyền cần kiểm tra
     * @return true nếu có quyền
     */
    public static boolean hasPermission(String roleName, String permissionName) {
        if (roleName == null || permissionName == null) {
            return false;
        }

        try {
            return PermissionRole.getPermissionsForRole(roleName.trim().toUpperCase())
                    .stream()
                    .anyMatch(permission -> permission.getName().equals(permissionName));
        } catch (Exception e) {
            log.error("Error checking permission {} for role: {}", permissionName, roleName, e);
            return false;
        }
    }
}
