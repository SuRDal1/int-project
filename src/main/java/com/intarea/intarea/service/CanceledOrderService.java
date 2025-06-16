package com.intarea.intarea.service;

import com.intarea.intarea.domain.*;
import com.intarea.intarea.repository.CanceledOrderRepository;
import com.intarea.intarea.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CanceledOrderService {

    private final OrderRepository orderRepository;
    private final MaterialService materialService;
    private final CanceledOrderRepository canceledOrderRepository;

    // 주문 삭제
    @Transactional
    public void deleteOrder(Long orderId) {

        //1.주문 조회
        Orders orders = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다, ID" + orderId));

        // 정확히 복구
        for ( OrderMaterial orderMaterial: orders.getOrderMaterialList()) {
            Material material = orderMaterial.getMaterial();
            log.info("복구 대상 Material ID: {}, 기존 수량: {}, 복구 수량: {}",
                    material.getId(), material.getQuantity(), orderMaterial.getDeductedQuantity());

            // 복구할 Material 꺼내기 - 차감했던 재고를 그대로 다시 가져옴
            material.setQuantity(material.getQuantity() + orderMaterial.getDeductedQuantity());

            // 예전에 차감했던 수량(deductedQuantity)만큼 더해서 원래대로 되돌림
            materialService.saveMaterial(material);
        }

        // 주문 삭제
        orderRepository.deleteById(orderId);

    }

    // 취소 주문 저장
    @Transactional
    public void cancelOrder(Long orderId, String cancelText, Users user) {
        
        Orders orders = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다, ID" + orderId));

        // 주문 삭제 및 원재료 복구
        deleteOrder(orderId);

        // 취소 이력 저장
        CanceledOrder cancel = CanceledOrder.builder()
                .productName(orders.getProductName())
                .orderQuantity(orders.getOrderQuantity())
                .canceledText(cancelText)
                .orderDate(orders.getOrderDate())
                .canceledDate(LocalDateTime.now())
                .users(user)
                .build();

        canceledOrderRepository.save(cancel);
    }

    // 취소 주문 리스트 확인
    public Page<CanceledOrder> findAllCancel(Pageable pageable) {
        return canceledOrderRepository.findAll(pageable);
    }


}
