package vn.doan.lms.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.doan.lms.domain.AssignmentComment;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentCommentDTO {

    private Long id;
    private String content;
    private String createdBy;
    private String createdAt;

    public AssignmentCommentDTO(AssignmentComment assignmentComment) {
        this.id = assignmentComment.getId();
        this.content = assignmentComment.getContent();
        this.createdBy = assignmentComment.getUser().getFullName();
        this.createdAt = assignmentComment.getCreatedAt().toString(); // Assuming createdAt is a LocalDateTime

    }
}
