package com.nghhieu27.mail.demo.repository;

import com.nghhieu27.mail.demo.entity.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepository extends JpaRepository<Email, String> {
}
