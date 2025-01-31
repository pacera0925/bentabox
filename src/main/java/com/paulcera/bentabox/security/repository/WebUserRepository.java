package com.paulcera.bentabox.security.repository;

import com.paulcera.bentabox.security.model.WebUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebUserRepository extends JpaRepository<WebUser, Integer> {

    Optional<WebUser> findByUsername(String username);

}
