package com.intarea.intarea.repository;

import com.intarea.intarea.domain.Orders;
import com.intarea.intarea.domain.ProcessA;
import com.intarea.intarea.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProcessARepository extends JpaRepository<ProcessA, Long> {


    List<ProcessA> findByUsersId(Long userId);

    List<ProcessA> findAllByOrders_Id(Long ordersID);

    // 한 주문의 공정들 오름차순 정렬(순차 처리)
    Optional<ProcessA> findFirstByStatusOrderByOrdersIdAsc(Status status);

    // 공정 상태 완료 처리용 id와 status값 사용.
    long countByOrdersIdAndStatus(Long OrdersId, Status status);

    // 지정된 공정 범위에서 상태(WAITING, OK, NG)별로 카운팅하는 Query문 시행
    @Query("SELECT p.status, COUNT(p) FROM ProcessA p WHERE p.orders IN :orders GROUP BY p.status")
    List<Object[]> countByStatusInOrders(@Param("orders") List<Orders> orders);



    // 만약 날짜 연결 시각화가 필요할 시 사용 가능.
//    @Query("SELECT p FROM ProcessA p WHERE p.processDateTime BETWEEN :start AND :end ORDER BY p.lotNumber")
//    List<ProcessA> findAllByProcessDateTimeBetween(
//            @Param("start") LocalDateTime start,
//            @Param("end")LocalDateTime end);

}
