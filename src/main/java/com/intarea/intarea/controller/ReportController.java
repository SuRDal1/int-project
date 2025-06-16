package com.intarea.intarea.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intarea.intarea.domain.*;
import com.intarea.intarea.dto.ProcessDataDto;
import com.intarea.intarea.service.OrderService;
import com.intarea.intarea.service.ProcessDataService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {

    private final OrderService orderService;
    private final ProcessDataService processDataService;
    private final ObjectMapper objectMapper; // JSON 변환


    // 레포트 페이지 열기
    @GetMapping
    public String report(Model model, HttpSession session) throws JsonProcessingException {

        // 로그인 여부 체크
        Users loginMember = (Users) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (loginMember == null) {
            return "redirect:/";
        }

        // 1. 전체 제품 상태별 차트용 chartData 생성 로직
        Map<Status, Long> statusCountMap = processDataService.getAllStatusSum();

        Map<String, Object> chartDataA = new HashMap<>();

        // 1-1. labels 추출
        List<String> labels = List.of("총합", "정상", "불량", "작업전");

        // 1-2. 데이터 값 추출
        List<Long> data = new ArrayList<>();
        long total = Arrays.stream(Status.values())
                .mapToLong(status -> statusCountMap.getOrDefault(status, 0L))
                .sum();
        data.add(total); // 총합값

        log.info("{}", data);

        data.addAll(Arrays.stream(Status.values())
                .map(status -> statusCountMap.getOrDefault(status, 0L))
                .collect(Collectors.toList())); // 개별 상태값

        // 1-3. 색상 지정 (Chart.js 개별 바 색상)
        List<String> backgroundColors = Arrays.asList(
                "#ff8a44", // 총합
                "#4caf50", // OK
                "#f44336", // NG
                "#f39c12" // WAITING
        );

        // 1-4. datasets 구성
        Map<String, Object> dataset = new HashMap<>();
        dataset.put("label", "공정 상태 분포");
        dataset.put("data", data);
        dataset.put("backgroundColor", backgroundColors);

        chartDataA.put("labels", labels);
        chartDataA.put("datasets", Collections.singletonList(dataset));

        // 1-5. JSON 변환 + 모델에 추가
        String chartJsonA = objectMapper.writeValueAsString(chartDataA);
        model.addAttribute("chartDataA", chartJsonA);


        // 2. 제품별 차트용 chartData 생성 로직
        Map<String, Map<String, Integer>> allStats = processDataService.getAllProductStats();

        Set<String> productNames = new HashSet<>();
        productNames.addAll(allStats.get("okA").keySet());
        productNames.addAll(allStats.get("okB").keySet());

        Map<String, Object> chartDataB = new HashMap<>();
        for (String product : productNames) {
            Map<String, Object> perProduct = new HashMap<>();

            // 공정B일 때와 아닐 때 기준 분리.
            if (product.equals("LAMP_COVER")){
                perProduct.put("ok", allStats.get("okB").getOrDefault(product, 0));
                perProduct.put("ng", allStats.get("ngB").getOrDefault(product, 0));
                perProduct.put("total", allStats.get("total").getOrDefault(product, 0));
                perProduct.put("process","B");
            } else {
                perProduct.put("ok", allStats.get("okA").getOrDefault(product,0));
                perProduct.put("ng", allStats.get("ngA").getOrDefault(product,0));
                perProduct.put("total", allStats.get("total").getOrDefault(product, 0));
                perProduct.put("process", "A");
            }

            chartDataB.put(product, perProduct);
        }

        String chartJsonB = objectMapper.writeValueAsString(chartDataB);
        model.addAttribute("chartDataB", chartJsonB);


        // 3. 주문별 차트용 chartData 생성 로직

        List<Orders> orders = orderService.findOrders();
        model.addAttribute("orders",orders);

        Map<Long, Map<String, Object>> chartDataC = new HashMap<>();
        for(Orders order : orders){
            Long orderId = order.getId();

            ProcessDataDto processAData = processDataService.getProcessAData(orderId);
            ProcessDataDto processBData = processDataService.getProcessBData(orderId);

            long okValue = processAData.getOkProduct() + processBData.getOkProduct();
            long ngValue = processAData.getNgProduct() +processBData.getNgProduct();
            long totalValue = okValue +ngValue;

            Map<String, Object>perOrder = new HashMap<>();
            perOrder.put("total", totalValue);
            perOrder.put("ok", okValue);
            perOrder.put("ng",ngValue);

            // 제품명 추가
            ProductName productName = order.getProductName();
            perOrder.put("productName", productName != null ? productName.name() : "확인 불가");
            perOrder.put("productLabel", productName != null ? productName.getLabel() : "확인 불가");

            chartDataC.put(orderId, perOrder);
        }

        String chartJsonC = objectMapper.writeValueAsString(chartDataC);
        model.addAttribute("chartDataC", chartJsonC);


        // 4. 회사별 차트용 chartData 생성 로직 추가 시작
        // 4-1. 회사 Enum 목록
        model.addAttribute("orderCompanies", OrderCompany.values());

        // 4-2. 회사별→제품별 주문 통계 집계
        Map<OrderCompany, Map<ProductName, Long>> stats =
                orderService.getOrderStatsByCompany();

        // 4-3. 회사별 차트용 JSON 직렬화
        Map<String, String> chartDataD = new HashMap<>();
        for (OrderCompany comp : OrderCompany.values()) {
            Map<String, Object> dataMap = new HashMap<>();
            List<String> complabels = new ArrayList<>();
            List<Long> values = new ArrayList<>();

            for (ProductName p : ProductName.values()) {
                complabels.add(p.getLabel());
                values.add(stats.getOrDefault(comp, Collections.emptyMap())
                        .getOrDefault(p, 0L));
            }

            dataMap.put("labels", complabels);
            dataMap.put("datasets", List.of(
                    Map.of("label", comp.getLabel() + " 주문량", "data", values)
            ));

            String json = objectMapper.writeValueAsString(dataMap);
            chartDataD.put(comp.name(), json);

        }
        model.addAttribute("chartDataD", chartDataD);


        // 5. 최종처리 : 로그인되어있으면 '관리자 홈'으로
        return "report/report";

    }
}
