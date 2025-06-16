package com.intarea.intarea.controller;

import com.intarea.intarea.domain.OrderCompany;
import com.intarea.intarea.domain.Orders;
import com.intarea.intarea.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CustomerController {

    private final OrderService orderService;

    /** 선택된 회사의 주문 내역 조회 */
    @GetMapping("/customers")
    public String customerByCompany(@RequestParam OrderCompany company,
                                    Model model) {
        List<Orders> orders = orderService.findOrdersByCompany(company);
        model.addAttribute("company", company);
        model.addAttribute("orders", orders);

        // 일별 합계
        Map<LocalDate, Long> dailyTotals = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getProcessDate(),
                        TreeMap::new,  // 날짜 오름차순 보장
                        Collectors.summingLong(Orders::getOrderQuantity)
                ));
        model.addAttribute("dailyTotals", dailyTotals);

        // 월별 합계
        Map<YearMonth, Long> monthlyTotals = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> YearMonth.from(o.getProcessDate()),
                        TreeMap::new,
                        Collectors.summingLong(Orders::getOrderQuantity)
                ));
        model.addAttribute("monthlyTotals", monthlyTotals);


        return "customer/customerList";
    }

}
