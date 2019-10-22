package com.github.iauglov.persistence;

import com.github.iauglov.persistence.UserGuide.PK;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGuideRepository extends JpaRepository<UserGuide, PK> {
}
