package vn.doan.lms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.doan.lms.domain.Answer;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
}
