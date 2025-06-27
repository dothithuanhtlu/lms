package vn.doan.lms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.doan.lms.domain.SubmissionDocument;

@Repository
public interface SubmissionDocumentRepository extends JpaRepository<SubmissionDocument, Long> {

    List<SubmissionDocument> findBySubmissionId(Long submissionId);

    void deleteBySubmissionId(Long submissionId);

    int countBySubmissionId(Long submissionId);
}
