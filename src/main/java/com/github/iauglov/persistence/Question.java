package com.github.iauglov.persistence;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import static lombok.AccessLevel.NONE;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "questions")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Question {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Setter(NONE)
    private Integer id;
    private String text;
    @ManyToOne
    private Answer answer;
    @ManyToOne
    private Guide guide;

}
