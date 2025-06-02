package vn.doan.lms.domain;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "semesters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Semester {

    @Id
    private String id;

    @NotBlank(message = "Name mustn't be empty")
    private String name;

    @NotNull(message = "StartDate mustn't be empty")
    private LocalDate startDate;

    @NotNull(message = "EndDate mustn't be empty")
    private LocalDate endDate;

    public boolean isActive() {
        LocalDate now = LocalDate.now();
        return !startDate.isAfter(now) && !endDate.isBefore(now);
    }
}
