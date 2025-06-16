package com.intarea.intarea.service;

import com.intarea.intarea.domain.Notices;
import com.intarea.intarea.domain.Users;
import com.intarea.intarea.repository.NoticeRepository;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NoticeService {

    private final NoticeRepository noticeRepository;

    // 공지 저장
    public void saveNotice(Notices notices) {
        noticeRepository.save(notices);
    }

    // 공지 id로 찾기
    public Optional<Notices> get(Long id) {
        return noticeRepository.findById(id);
    }

    // 공지 수정 및 적용
    public void modify(Notices notices, String title, String content) {
        notices.setContent(content);
        notices.setTitle(title);
        notices.setModifiedAt(LocalDateTime.now());
        noticeRepository.save(notices);
    }

    // 공지 삭제
    public void delete(Notices notices) {
        noticeRepository.deleteById(notices.getId());
    }


    // 공지 검색 기능
    // JPA가 제공하는 Specification 인터페이스 사용하기, DB 검색을 유연하게 할 수 있고 복잡한 검색 조건도 처리할 수 있음.
    private Specification<Notices> search(String kw) {

        return (Root<Notices> q, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            query.distinct(true);

            // 공지-작성자 Join (Users)
            Join<Notices, Users> author = q.join("author", JoinType.LEFT);

            // 검색 조건 통합: 제목, 내용, 작성자 이름
            return cb.or(
                    cb.like(q.get("title"), "%" + kw + "%"),
                    cb.like(q.get("content"), "%" + kw + "%"),
                    cb.like(author.get("name"), "%" + kw + "%")
            );
        };
    }

    // 검색 기능 반영 공지사항 리스트 조회
    public Page<Notices> getList(Pageable pageable, String kw) {
        // 검색어가 없으면 전체 공지 리스트 반환.
        if (kw == null || kw.trim().isEmpty()) {
            return noticeRepository.findAll(pageable);
        }
        // 검색어가 있으면 해당검색어를 제목/내용/작성자로 포함하는 공지만 리스트 반환.
        else {
            return noticeRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrAuthor_NameContainingIgnoreCase(kw, kw, kw, pageable);
        }

    }


}
