package com.github.iauglov.persistence;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.Table;
import static lombok.AccessLevel.NONE;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "guides")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Guide {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Setter(NONE)
    private Integer id;
    private Long delay;
    private String title;
    @Column(length = 2048)
    private String text;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Guide guide = (Guide) o;
        return Objects.equals(id, guide.id) &&
                Objects.equals(delay, guide.delay) &&
                Objects.equals(title, guide.title) &&
                Objects.equals(text, guide.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, delay, title, text);
    }
}
