package com.github.iauglov.persistence;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import static lombok.AccessLevel.NONE;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name= "answers")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Answer {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Setter(NONE)
    private Integer id;
    private String text;
    @OneToOne
    private Question question;

}
