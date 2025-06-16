package com.intarea.intarea.controller;

import com.intarea.intarea.domain.CanceledOrder;
import com.intarea.intarea.domain.Users;
import com.intarea.intarea.service.CanceledOrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/order")
public class CanceledOrderController {

   private final CanceledOrderService canceledOrderService;

    // 취소된 주문 목록 페이지 열기
    @GetMapping("/cancel")
    public String cancelList(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "15") int size,
                             Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("canceledDate").descending());
        Page<CanceledOrder> canceledOrderPage = canceledOrderService.findAllCancel(pageable);

        model.addAttribute("cancelList", canceledOrderPage.getContent());
        model.addAttribute("orderPage", canceledOrderPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", canceledOrderPage.getTotalPages());

        return "order/canceledOrderList";

    }


    // 주문 취소 처리 후 취소된 주문 목록 페이지로 이동
    @PostMapping("/cancel")
    public String cancelOrder(@RequestParam("orderId") Long orderId,
                              @RequestParam("cancelText") String cancelText,
                              HttpSession session) {

        Users loginMember = (Users) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/";
        }

        canceledOrderService.cancelOrder(orderId, cancelText, loginMember);
        return "redirect:/order/cancel";
    }



}


