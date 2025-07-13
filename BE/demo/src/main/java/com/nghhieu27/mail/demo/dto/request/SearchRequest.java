package com.nghhieu27.mail.demo.dto.request;

import com.nghhieu27.mail.demo.enums.Folder;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRequest {
    String query;
    int page = 0;
    int size = 10;
    Date fromDate;
    Date toDate;
    boolean hasAttachment;
    @Enumerated
    Folder folder;
}
