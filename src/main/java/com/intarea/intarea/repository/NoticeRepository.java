package com.intarea.intarea.repository;

import com.intarea.intarea.domain.Notices;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notices,Long> {

    //공지사항에서 작성자와 제목으로 찾기
    Page<Notices> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrAuthor_NameContainingIgnoreCase(String title, String content, String authorName, Pageable pageable);


}
