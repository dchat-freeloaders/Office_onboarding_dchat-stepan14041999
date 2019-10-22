package com.github.iauglov.persistence;

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
    @Column(length = 2048)
    private String text;

}
