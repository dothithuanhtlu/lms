package vn.doan.lms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.doan.lms.domain.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findOneByNameRole(String nameRole);
}
