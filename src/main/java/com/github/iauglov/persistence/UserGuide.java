package com.github.iauglov.persistence;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_guides")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserGuide {

    @EmbeddedId
    private PK pk;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Embeddable
    public static class PK implements Serializable {
        @OneToOne
        private InternalUser user;
        @OneToOne
        private Guide guide;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PK pk = (PK) o;
            return Objects.equals(user, pk.user) &&
                    Objects.equals(guide, pk.guide);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user, guide);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserGuide userGuide = (UserGuide) o;
        return Objects.equals(pk, userGuide.pk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pk);
    }
}
