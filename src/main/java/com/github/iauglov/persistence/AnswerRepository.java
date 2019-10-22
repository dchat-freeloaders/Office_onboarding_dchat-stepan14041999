package com.github.iauglov.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {

    Optional<Answer> findByQuestionId(Integer questionId);

}
