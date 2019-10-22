package com.github.iauglov.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GuideRepository extends JpaRepository<Guide, Integer> {

//    List<Guide> findAllByStartsWithIsBefore(LocalDateTime now);

}
