package com.intarea.intarea.repository;

import com.intarea.intarea.domain.OrderCompany;
import com.intarea.intarea.domain.Orders;
import com.intarea.intarea.dto.DateTotalDto;
import com.intarea.intarea.dto.MonthTotalDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OrderRepository extends JpaRepository<Orders, Long > {
    List<Orders> findByProcessDate(LocalDate date);

    List<Orders> findByUsersId(Long userId);

    List<Orders> findByOrderCompany(OrderCompany orderCompany);

    @Query("""
                  SELECT new com.intarea.intarea.dto.DateTotalDto(
                  o.processDate,
                  SUM(o.orderQuantity)
                )
                FROM Orders o
                WHERE o.orderCompany = :company
                GROUP BY o.processDate
                ORDER BY o.processDate
                """)
    List<DateTotalDto> findDailyTotalsByCompany(@Param("company") OrderCompany company);

    @Query("""
            SELECT new com.intarea.intarea.dto.MonthTotalDto(
                YEAR(o.processDate),
                MONTH(o.processDate),
                SUM(o.orderQuantity)
              )
              FROM Orders o
              WHERE o.orderCompany = :company
              GROUP BY YEAR(o.processDate), MONTH(o.processDate)
              ORDER BY YEAR(o.processDate), MONTH(o.processDate)
            """)
    List<MonthTotalDto> findMonthlyTotalsByCompany(@Param("company") OrderCompany company);

}
