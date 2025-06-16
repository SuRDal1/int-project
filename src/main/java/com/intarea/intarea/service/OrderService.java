package com.intarea.intarea.service;

import com.intarea.intarea.domain.*;
import com.intarea.intarea.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final MaterialService materialService;


    // 주문 생성
    @Transactional
    public Long save(Orders orders) {
        // 사용자가 주문한 제품과 수량을 가져옴
        int orderQuantity = orders.getOrderQuantity(); // 주문수량
        ProductName productName = orders.getProductName();

        // 원재료 차감 로직 시작
        Map<MaterialName, Long> requiredMaterialStock = materialService.getRequiredMaterialStock(productName);
        log.info("requiredMaterialStock, 원자재 재고수량 {}", requiredMaterialStock);

        // 부족 재료 기록용 리스트
        List<String> insufficientMaterials = new ArrayList<>();

        // 사전 재고 확인 (부족한 재료 개별 체크)
        for (Map.Entry<MaterialName, Long> entry : requiredMaterialStock.entrySet()) {
            // 요구량
            MaterialName materialName = entry.getKey();
            long requiredOrderQuantity = orderQuantity;

            // 현재 총재고량
            List<Material> materials = materialService.findMaterialName(materialName);
            long totalAvailable = materials.stream().mapToLong(Material::getQuantity).sum();

            // 요구량보다 총량이 적은 재료는 리스트에 추가
            if (totalAvailable < requiredOrderQuantity) {
                insufficientMaterials.add(materialName.name());
            }
        }
        
        // 재료 부족시 에러 메세지 처리(앞선 리스트에서 꺼내 표시)
        if (!insufficientMaterials.isEmpty()) {
            String message =  "다음 재료가 부족합니다 : " + String.join(", ", insufficientMaterials);
            throw new IllegalStateException(message);
        }


        // 재료 소진 처리
        for (Map.Entry<MaterialName, Long> entry : requiredMaterialStock.entrySet()) {
            MaterialName materialName = entry.getKey();
            // 기본 1개씩 소진, 20개씩 주문하면 1*20
            long requiredOrderQuantity = orderQuantity; // 주문수량

            log.info("entry - 원재료이름 {}", entry.getKey());
            log.info("entry - 원자재 재고수량 {}", entry.getValue());
            log.info("orderQuantity 주문수량 {}", requiredOrderQuantity);

            // DB에서 해당 원재료 가져오기 (여러 개 가져오기)
            List<Material> materials = materialService.findMaterialName(materialName);

            if (materials.isEmpty()) {
                throw new IllegalStateException(materialName + "재료가 존재하지 않습니다");
            }

            // 필요한 만큼 재료 차감 (FIFO 방식)
            // 만약 red 10, red 50, 오더는 30 -> 10을 먼저 차감해 20. -> 다음 50에서 나머지 20 차감.
            for (Material material : materials) {
                if (requiredOrderQuantity <= 0) break;

                long materialQuantity = material.getQuantity(); // 남은재고수량 10

                // 남은 재고와 주문 수량을 비교하여 작은값을 가져온다
                long minQuantity = Math.min(materialQuantity, requiredOrderQuantity); // 작은 값 10

                material.setQuantity((int) (materialQuantity - minQuantity)); // 10-10

                log.info("차감된 원재료: {}, 차감량 {}, 남은 주문 필요량 {}", materialName, minQuantity, requiredOrderQuantity);
                materialService.saveMaterial(material);

                // 어떤 Material에서 얼마나 차감했는지 기록
                // Orders <-> OrderMaterial 양방향 연관관계 설정
                OrderMaterial orderMaterial = new OrderMaterial();
                orderMaterial.setMaterial(material);
                orderMaterial.setDeductedQuantity((int)minQuantity);
                orders.addJobOrderMaterial(orderMaterial);

                // 주문 수량 업데이트
                // 차감된 수량만큼 남은 주문 수량 갱신
                requiredOrderQuantity -= minQuantity;
            }

            if (requiredOrderQuantity > 0) {
                throw new IllegalStateException(materialName + "재료가 부족하여 차감할 수 없습니다");
            }
        }
        orderRepository.save(orders);
        return orders.getId();
    }


    // 주문 상태만 업데이트 (재고 차감x)
    @Transactional
    public void updateOrderState(Long orderId, OrderState state) {
        Orders orders = findOneOrder(orderId).orElseThrow(()-> new IllegalArgumentException("주문 없음"));
        orders.setOrderState(state);
    }


    // 단건조회
    public Optional<Orders> findOneOrder(Long orderId) {
        return orderRepository.findById(orderId);
    }

    // 전체주문조회
    public List<Orders> findOrders() {
        return orderRepository.findAll();
    }


    // 개별 재료들의 총합수 확인 메서드 (Map<개별 재료명, 총합수> 반환)
    public Map<ProductName, Long> getOrdersQuantities() {
        //DB에서 재료를 가져온다
        List<Orders> orders = orderRepository.findAll();
        // 재료 이름별로 그룹화하고 총합 계산
        Map<ProductName, Long> productNameLongMap = orders.stream().collect(Collectors.groupingBy(
                Orders::getProductName, // 재료 이름 기준으로 그룹화 -> 별도의 String이 아닌 ProductName 그대로 저장 = .getLabel()을 통한 표시 사용가능.
                Collectors.summingLong(Orders::getOrderQuantity) // 각 그룹의 총합계산
        ));
        return productNameLongMap;
    }

    // 공정예정일 기준으로 해당 주문 전부 찾기(관리자 홈페이지)
    public List<Orders> findOrdersByProcessDate(LocalDate date) {
        return orderRepository.findByProcessDate(date);
    }

    // 공정예정일이 오늘인 주문의 주문처 리스트로 뽑기(관리자 홈페이지)
    public List<OrderCompany> findTodayOrderCompanies() {
        
        // 오늘 일자
        LocalDate today = LocalDate.now();
        // 공정예정일이 오늘인 주문의 리스트
        List<Orders> todayOrders = orderRepository.findByProcessDate(today);

        // 공정예정일이 오늘인 주문들의 주문처를 리스트로
        return todayOrders.stream() // for문 유사처리
                .map(Orders::getOrderCompany) // map으로 찾는 대상 지정 및 처리
                .distinct() // 중복값 없음 처리 = 고유값
                .collect(Collectors.toList()); // List<OrderCompany> 형태로 지정
    }


    // 페이징
    public Page<Orders> findAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }


    // 회사별 차트용(레포트페이지 차트, 고객관리용)
    @Transactional(readOnly = true)
    public List<Orders> findOrdersByCompany(OrderCompany company) {
        return orderRepository.findByOrderCompany(company);
    }

    // 회사별 → 제품별 주문 수량 합계
    @Transactional(readOnly = true)
    public Map<OrderCompany, Map<ProductName, Long>> getOrderStatsByCompany() {
        return orderRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Orders::getOrderCompany,
                        Collectors.groupingBy(
                                Orders::getProductName,
                                Collectors.summingLong(Orders::getOrderQuantity)
                        )
                ));
    }

}

