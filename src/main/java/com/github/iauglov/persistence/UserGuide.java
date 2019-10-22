package com.github.iauglov.persistence;

import java.io.Serializable;
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
    }
}
