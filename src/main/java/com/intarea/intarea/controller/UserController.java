package com.intarea.intarea.controller;

import com.intarea.intarea.domain.*;
import com.intarea.intarea.dto.ProcessViewDto;
import com.intarea.intarea.dto.UpdateUserForm;
import com.intarea.intarea.dto.UserForm;
import com.intarea.intarea.repository.MaterialRepository;
import com.intarea.intarea.repository.OrderRepository;
import com.intarea.intarea.repository.ProcessARepository;
import com.intarea.intarea.repository.ProcessBRepository;
import com.intarea.intarea.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin")
public class UserController {

    private final OrderRepository orderRepository;
    private final MaterialRepository materialRepository;
    private final ProcessARepository processARepository;
    private final ProcessBRepository processBRepository;

    private final UserService userService;

    //회원등록 열기
    @GetMapping("/register")
    public String register(Model model) {

        model.addAttribute("userForm", new UserForm());

        return "user/createUser";

    }

    // 회원 등록 진행
    @PostMapping("/register")
    public String createPost(@Valid @ModelAttribute("userForm") UserForm userForm, BindingResult bindingResult) {
        // 공란 확인
        if (bindingResult.hasErrors()) {
            return "user/createUser";
        }

        // 중복 확인
        Users findUsers = userService.vailduplicateMember(userForm.getLoginId());

        if (findUsers != null) {
            bindingResult.reject("DuplicateMember", "아이디가 존재합니다.");
            return "user/createUser";
        }

        // 가입 실행
        Users users = Users.builder()
                        .loginId(userForm.getLoginId())
                        .password(userForm.getPassword())
                        .name(userForm.getName())
                        .phone(userForm.getPhone())
                        .grade(Grade.valueOf(userForm.getGrade()))
                        .build();

        userService.save(users);

        return "redirect:/admin/user/list";

    }


    //회원 리스트
    @GetMapping("/user/list")
    public String list(Model model, HttpSession session) {

        // 로그인 여부 확인
        Users loginMember = (Users) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if(loginMember==null) {
            return "redirect:/";
        }


        List<Users> usersList = userService.findAllUser();

        model.addAttribute("userList", usersList);

        return "user/userList";

    }

    // 회원 정보 수정 열기 (GET=수정 폼 호출)
    @GetMapping("/user/{userId}/update")
    public String update(@PathVariable("userId") Long userId, Model model) {

        Users users = userService.findOne(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        UpdateUserForm updateUserForm = new UpdateUserForm();
        updateUserForm.setId(users.getId());
        updateUserForm.setLoginId(users.getLoginId());
        updateUserForm.setPassword(users.getPassword());
        updateUserForm.setName(users.getName());
        updateUserForm.setEmail(users.getEmail());
        updateUserForm.setPhone(users.getPhone());
        updateUserForm.setGrade(users.getGrade().name());

        model.addAttribute("updateUserForm", updateUserForm);
        return "user/updateUser";
    }

    // 회원 정보 수정 (POST=수정 저장)
    @PostMapping("/user/{userId}/update")
    public String updatePost(@Valid @ModelAttribute("updateUserForm") UpdateUserForm updateUserForm,
                             BindingResult bindingResult) {
        // 누락값 여부 확인
        if (bindingResult.hasErrors()) {
            return "user/updateUser";
        }

        // 회원 정보 오류 확인
        if (updateUserForm.getId() == null) {
            bindingResult.reject("globalError", "잘못된 접근입니다. 회원 ID가 없습니다.");
            return "user/updateUser";
        }

        Users users = userService.findOne(updateUserForm.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        // 비밀번호 변경
        String newPassword = updateUserForm.getPassword();
        if (newPassword == null || newPassword.isBlank()) {
            newPassword = users.getPassword();
        }

        // loginId는 수정 불가능
        users.update(
                newPassword,
                updateUserForm.getPhone(),
                updateUserForm.getEmail(),
                Grade.valueOf(updateUserForm.getGrade()),
                updateUserForm.getName()  // 성명 수정가능.
        );

        userService.save(users);

        return "redirect:/admin/user/list";
    }


    // 회원 삭제
    @GetMapping("/user/{userId}/delete")
    public String delete(@PathVariable("userId") Long userId) {

        Users users = userService.findOne(userId).orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));
        userService.delete(users.getId());
        return "redirect:/user/list";
    }


    // 회원 업무 조회
    @GetMapping("/user/{userId}/tasks")
    public String getUserTasks(@PathVariable Long userId, Model model) {
        List<Orders> orders = orderRepository.findByUsersId(userId);
        List<Material>materials = materialRepository.findByUsersId(userId);

        // 두 공정을 공통 DTO 형태로 통일
        List<ProcessViewDto> allProcesses = new ArrayList<>();

        // 공정 A 처리
        Map<ProductName, List<ProcessA>> aGrouped = processARepository.findByUsersId(userId)
                .stream()
                .collect(Collectors.groupingBy(pa -> pa.getOrders().getProductName()));

        for (ProductName product : aGrouped.keySet()) {
            List<ProcessA> list = aGrouped.get(product);
            int total = list.size();
            int ok = (int) list.stream().filter(p -> p.getStatus() == Status.OK).count();
            int ng = (int) list.stream().filter(p -> p.getStatus() == Status.NG).count();
            LocalDateTime date = list.get(0).getProcessDateTime();

            allProcesses.add(new ProcessViewDto(date, product.getLabel(), "A", total, ok, ng));
        }

        // 공정 B 처리
        Map<ProductName, List<ProcessB>> bGrouped = processBRepository.findByUsersId(userId)
                .stream()
                .collect(Collectors.groupingBy(pb -> pb.getOrders().getProductName()));

        for (ProductName product : bGrouped.keySet()) {
            List<ProcessB> list = bGrouped.get(product);
            int total = list.size();
            int ok = (int) list.stream().filter(p -> p.getStatus() == Status.OK).count();
            int ng = (int) list.stream().filter(p -> p.getStatus() == Status.NG).count();
            LocalDateTime date = list.get(0).getProcessDateTime();

            allProcesses.add(new ProcessViewDto(date, product.getLabel(), "B", total, ok, ng));
        }


        // 날짜 기준 정렬
        allProcesses.sort(Comparator.comparing(ProcessViewDto::getDateTime));

        model.addAttribute("processes", allProcesses);


        Users user = userService.findOne(userId).orElseThrow();

        model.addAttribute("user",user);
        model.addAttribute("orders", orders);
        model.addAttribute("materials",materials);

        return "user/userTaskList";
    }

}
