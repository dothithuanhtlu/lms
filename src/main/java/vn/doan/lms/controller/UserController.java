package vn.doan.lms.controller;

import org.springframework.web.bind.annotation.RestController;

import vn.doan.lms.domain.User;
import vn.doan.lms.service.UserService;
import vn.doan.lms.util.error.IdValidationException;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(this.userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") String idUser) {
        return ResponseEntity.ok(this.userService.getUserByIdUser(idUser));
    }

    @PostMapping("/users")
    public ResponseEntity<User> postMethodName(@RequestBody User user) throws IdValidationException {
        if (this.userService.isExistIdUser(user.getIdUser())) {
            throw new IdValidationException("Id already exist!");
        }
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        User newUser = this.userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteMethodName(@PathVariable("id") String idUser) throws IdValidationException {
        if (!this.userService.isExistIdUser(idUser)) {
            throw new IdValidationException("Id not exist!");
        }
        this.userService.deleteUser(idUser);
        return ResponseEntity.ok("Delete successed!");
    }
    // @PutMapping("/users/{id}")
    // public ResponseEntity<User> putMethodName(@PathVariable String id,
    // @RequestBody String entity) {
    // //TODO: process PUT request

    // return null;
    // }
}
