package vn.doan.lms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.doan.lms.domain.QuestionOption;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {

    List<QuestionOption> findByQuestionIdOrderByOptionOrderAsc(Long questionId);

    List<QuestionOption> findByQuestionIdAndIsCorrectTrue(Long questionId);

    long countByQuestionId(Long questionId);

    boolean existsByQuestionIdAndOptionOrder(Long questionId, Integer optionOrder);
}
