package com.nghhieu27.mail.demo.repository;

import com.nghhieu27.mail.demo.entity.Email;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailRepository extends JpaRepository<Email, String> {
//    @Query("SELECT e FROM Email e WHERE " +
//            "LOWER(e.sub) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
//            "LOWER(e.body) LIKE LOWER(CONCAT('%', :keyword, '%'))")
//    Page<Email> searchBySubOrBody(String keyword, Pageable pageable);

    Optional<List<Email>> findByFrom(String from);

    @Query("SELECT e FROM Email e WHERE " +
            "(:keyword IS NULL OR LOWER(e.sub) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(e.body) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            //"(:folder IS NULL OR e.folder = :folder) AND " +
            "(:fromDate IS NULL OR e.date >= :fromDate) AND " +
            "(:toDate IS NULL OR e.date <= :toDate) AND " +
            "(:hasAttachment IS FALSE OR e.attachmentPath IS NOT NULL)")
    Page<Email> advancedSearch(
            @Param("keyword") String keyword,
            //@Param("folder") Folder folder,
            @Param("fromDate") Date fromDate,
            @Param("toDate") Date toDate,
            @Param("hasAttachment") boolean hasAttachment,
            Pageable pageable
    );
}
