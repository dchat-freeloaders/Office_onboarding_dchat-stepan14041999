package com.github.iauglov.persistence;

import static java.time.Clock.systemUTC;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class InternalUser {

    @Id
    private Integer id;
    private LocalDateTime registeredAt;

    @PrePersist
    public void prePersist() {
        registeredAt = LocalDateTime.now(systemUTC());
    }

}
