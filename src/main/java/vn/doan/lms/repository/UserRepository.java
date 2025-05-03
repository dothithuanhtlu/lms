package vn.doan.lms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.doan.lms.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findOneByIdUser(String idUser);

    void deleteUserByIdUser(String idUser);

    boolean existsByIdUser(String idUser);
}
