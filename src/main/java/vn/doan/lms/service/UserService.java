package vn.doan.lms.service;

import java.util.List;

import org.springframework.stereotype.Service;

import vn.doan.lms.domain.User;
import vn.doan.lms.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    public User getUserByIdUser(String idUser) {
        return this.userRepository.findOneByIdUser(idUser);
    }

    public User saveUser(User user) {
        return this.userRepository.save(user);
    }

    public void deleteUser(String idUser) {
        this.userRepository.deleteUserByIdUser(idUser);
    }

    public boolean isExistIdUser(String idUser) {
        return this.userRepository.existsByIdUser(idUser);
    }

}
