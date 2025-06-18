package vn.doan.lms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.doan.lms.domain.AssignmentDocument;

@Repository
public interface AssignmentDocumentRepository extends JpaRepository<AssignmentDocument, Long> {
    // This interface can be extended with custom query methods if needed
}
