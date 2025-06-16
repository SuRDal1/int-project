package com.intarea.intarea.repository;

import com.intarea.intarea.domain.Orders;
import com.intarea.intarea.domain.ProcessB;
import com.intarea.intarea.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProcessBRepository extends JpaRepository<ProcessB, Long> {


    List<ProcessB> findByUsersId(Long userId);

    List<ProcessB>findAllByOrders_Id(Long orderId);

    // 한 주문의 공정들 오름차순 정렬(순차 처리)
    Optional<ProcessB> findFirstByStatusOrderByOrdersIdAsc(Status status);

    // 공정 상태 완료 처리용 id와 status값 사용.
    long countByOrdersIdAndStatus(Long OrdersId, Status status);

    // 지정된 공정 범위에서 상태(WAITING, OK, NG)별로 카운팅하는 Query문 시행
    @Query("SELECT p.status, COUNT(p) FROM ProcessB p WHERE p.orders IN :orders GROUP BY p.status")
    List<Object[]> countByStatusInOrders(@Param("orders") List<Orders> orders);

}
