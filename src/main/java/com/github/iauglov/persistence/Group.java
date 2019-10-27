package com.github.iauglov.persistence;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "groups")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Group {

    @Id
    private Integer id;
    private Long hash;

}
