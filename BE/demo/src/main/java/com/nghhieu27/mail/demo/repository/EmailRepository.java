package com.nghhieu27.mail.demo.repository;

import com.nghhieu27.mail.demo.entity.Email;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepository extends JpaRepository<Email, String> {
    @Query("SELECT e FROM Email e WHERE " +
            "LOWER(e.sub) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.body) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Email> searchBySubOrBody(String keyword, Pageable pageable);
}
