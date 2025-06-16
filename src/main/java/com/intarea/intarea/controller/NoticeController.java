package com.intarea.intarea.controller;

import com.intarea.intarea.domain.Notices;
import com.intarea.intarea.domain.Users;
import com.intarea.intarea.dto.NoticeForm;
import com.intarea.intarea.service.NoticeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeService noticeService;

    //공지사항 리스트 열기 + 검색기능
    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "15") int size,
                       @RequestParam(value = "kw", defaultValue = "") String kw,
                       Model model, HttpSession session) {

        // 로그인 체크
        Users loginMember = (Users) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/";
        }

        // 페이징 설정
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 검색어가 있으면 검색 결과, 없으면 전체 목록
        Page<Notices> paging = noticeService.getList(pageable, kw);

        // 모델 전달
        model.addAttribute("noticeList", paging.getContent());
        model.addAttribute("noticePage", paging);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paging.getTotalPages());
        model.addAttribute("kw", kw);

        // 사용자 정보 전달
        model.addAttribute("loginGrade", loginMember.getGrade());
        model.addAttribute("loginUserId", loginMember.getId());

        return "notice/noticeList";
    }



    // 공지사항 등록 폼 열기
    @GetMapping("/create")
    public String create(Model model,HttpSession session) {
        Users loginMember = (Users) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/";
        }
        model.addAttribute("noticeForm", new NoticeForm());
        return "notice/noticeForm";
    }

    // 공지사항 등록
    @PostMapping("/create")
    public String createPost(@Valid@ModelAttribute("noticeForm") NoticeForm noticeForm, HttpSession session,BindingResult bindingResult) {

        Users loginMember = (Users) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/";
        }

        if (bindingResult.hasErrors()) {
            return "notice/noticeForm";
        }

        Notices notices = new Notices();
        notices.setTitle(noticeForm.getTitle());
        notices.setContent(noticeForm.getContent());
        notices.setAuthor(loginMember);

        noticeService.saveNotice(notices);
        return "redirect:/notice";
    }

    // 공지사항 조회
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        Users loginMember = (Users) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/";
        }

        Notices notice = noticeService.get(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항이 존재하지 않습니다."));

        // 조회수 증가
        notice.setViews(notice.getViews() + 1);
        noticeService.saveNotice(notice);


        model.addAttribute("notice", notice);

        // 로그인 사용자 ID 추가
        model.addAttribute("loginUserId", loginMember.getId());
        return "notice/noticeDetail";
    }


    //공지사항 수정 폼 열기
    @GetMapping("/{id}/edit")
    public String updateForm(@PathVariable Long id, Model model, HttpSession session) {
        Users loginMember = (Users) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/";
        }

        Notices notice = noticeService.get(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항이 존재하지 않습니다."));

        // 작성자만 수정 가능
        if (!notice.getAuthor().getId().equals(loginMember.getId())) {
            return "redirect:/notice/" + id;
        }

        NoticeForm form = NoticeForm.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .build();

        model.addAttribute("noticeForm", form);
        // 로그인 사용자 ID 추가
        model.addAttribute("loginUserId", loginMember.getId());
        return "notice/noticeEditForm";
    }


    // 공지사항 수정 적용
    @PostMapping("/{id}/edit")
    public String updatePost(@PathVariable Long id,
                             @Valid @ModelAttribute("noticeForm") NoticeForm form,
                             BindingResult bindingResult,
                             HttpSession session) {

        Users loginMember = (Users) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/";
        }

        if (bindingResult.hasErrors()) {
            return "notice/noticeEditForm";
        }

        Notices notice = noticeService.get(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항이 존재하지 않습니다."));

        if (!notice.getAuthor().getId().equals(loginMember.getId())) {
            return "redirect:/notice/" + id;
        }

        noticeService.modify(notice, form.getTitle(), form.getContent());

        return "redirect:/notice";
    }

    // 공지사항 삭제
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {

        Users loginMember = (Users) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/";
        }

        Notices notice = noticeService.get(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항이 존재하지 않습니다."));

        // 작성자만 삭제 가능
        if (!notice.getAuthor().getId().equals(loginMember.getId())) {
            return "redirect:/notice";
        }

        noticeService.delete(notice);
        return "redirect:/notice";
    }


}
