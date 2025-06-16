package com.intarea.intarea.controller;

import com.intarea.intarea.domain.Orders;
import com.intarea.intarea.domain.Users;
import com.intarea.intarea.dto.LoginForm;
import com.intarea.intarea.dto.ProcessDataDto;
import com.intarea.intarea.service.LoginService;
import com.intarea.intarea.service.OrderService;
import com.intarea.intarea.service.ProcessDataService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final OrderService orderService;
    private final ProcessDataService processDataService;


    // 홈 열기(로그인 여부 구분)
    @GetMapping("/")
    public String login( @RequestParam(required = false) String selectedCompany, Model model, HttpSession session) {

        // 로그인 여부 체크(비로그인시 로그인창으로)
        Users loginMember = (Users) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (loginMember == null) {
            model.addAttribute("loginForm", new LoginForm());
            return "login";
        }

        // 1. 공정예정일이 오늘인 주문 호출
        // 오늘 날짜 불러오기(최초 표시용으로 model넘김 처리)
        LocalDateTime today = LocalDateTime.now();
        model.addAttribute("today", today);

        // 공정예정일이 오늘인 주문 리스트
        List<Orders> todayOrders = orderService.findOrdersByProcessDate(LocalDate.now());


        // 2. script연계 주문처별 필터링 -> todayOrders의 값 필터링해 새로 적용
        if (selectedCompany != null && !selectedCompany.isEmpty()) {
            todayOrders = todayOrders.stream() // for문 유사적용
                    .filter(o -> o.getOrderCompany().name().equals(selectedCompany)) // 필터 적용
                    .collect(Collectors.toList()); // List<Orders>형태로 저장
        }

        // todayOrders까지 이 부분에서 model로 전송되어야 필터링 적용.
        model.addAttribute("orderList", todayOrders);
        model.addAttribute("orderCompanyList", orderService.findTodayOrderCompanies());
        model.addAttribute("selectedCompany", selectedCompany);

        // 3. 금일 주문별 공정 결과 수량 맵 생성
        Map<Long, Long> okMap = new HashMap<>();
        Map<Long, Long> ngMap = new HashMap<>();

        for (Orders order : todayOrders) {
            ProcessDataDto dto;
            if (order.getProductName().name().equals("LAMP_COVER")) {
                dto = processDataService.getProcessBData(order.getId());
            } else {
                dto = processDataService.getProcessAData(order.getId());
            }
            okMap.put(order.getId(), dto.getOkProduct());
            ngMap.put(order.getId(), dto.getNgProduct());
        }

        model.addAttribute("okMap", okMap);
        model.addAttribute("ngMap", ngMap);


        // 4. 금일 생산률 - 불량률 표시
        // Scheduler와 별개로 설정해두지 않으면 스케쥴러 동작시에만 progress바 작동
        // 기존 생성한 okMap과 ngMap의 값 Long처리후 sum()
        long totalOk = okMap.values().stream().mapToLong(Long::longValue).sum();
        long totalNg = ngMap.values().stream().mapToLong(Long::longValue).sum();
        long total = todayOrders.stream()
                .mapToLong(Orders::getOrderQuantity)
                .sum();

        // progress를 위한 퍼센트 처리
        int okPercent = total > 0 ? (int) ((double) totalOk / total * 100) : 0;
        int ngPercent = total > 0 ? (int) ((double) totalNg / total * 100) : 0;

        model.addAttribute("todayTotal",total);
        model.addAttribute("todayOk",totalOk);
        model.addAttribute("todayNg",totalNg);
        model.addAttribute("okPercent", okPercent);
        model.addAttribute("ngPercent", ngPercent);

        // 5. 최종처리 - 로그인되어있으면 '관리자 홈'으로
        return "admin";

    }


    // 로그인 처리
    @PostMapping("/")
    public String loginPost(@Valid@ModelAttribute("loginForm") LoginForm loginForm, BindingResult bindingResult, HttpServletRequest request, Model model) {

        if (bindingResult.hasErrors()) {
            return "login";
        }

        Users loginMember = loginService.login(loginForm.getLoginId(), loginForm.getPassword());

        if(loginMember == null) {
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 다릅니다");
            return "login";
        }

        HttpSession session = request.getSession();

        session.setAttribute(SessionConst.LOGIN_MEMBER,loginMember);
        log.info("로그인 정보 : {}", loginMember.getLoginId());

        model.addAttribute("loginMember", loginMember);

        // 로그인 완료후 팝업창 처리
        session.setAttribute("SHOW_POPUP", true);

        return "redirect:/";

    }

    // 로그아웃 처리
    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if(session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }
}
