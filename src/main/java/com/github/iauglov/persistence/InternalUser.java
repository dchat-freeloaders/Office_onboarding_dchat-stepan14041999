package com.github.iauglov.persistence;

import static java.time.Clock.systemUTC;
import java.time.LocalDateTime;
import java.util.Objects;
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
    private String name;
    private LocalDateTime registeredAt;

    @PrePersist
    public void prePersist() {
        registeredAt = LocalDateTime.now(systemUTC());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InternalUser that = (InternalUser) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(registeredAt, that.registeredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, registeredAt);
    }
}
