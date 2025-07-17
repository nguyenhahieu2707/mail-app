package com.nghhieu27.mail.demo.repository;

import com.nghhieu27.mail.demo.entity.UserIMAP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserImapRepository extends JpaRepository<UserIMAP, String> {

    Optional<UserIMAP> findByEmail(String email);
}
