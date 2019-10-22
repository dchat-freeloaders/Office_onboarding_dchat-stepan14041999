package com.github.iauglov.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Integer> {

    List<Question> findAllByGuideId(Integer guideId);

    List<Question> findAllByAnswerId(Integer answerId);

}
