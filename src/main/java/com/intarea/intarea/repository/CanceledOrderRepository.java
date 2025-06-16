package com.intarea.intarea.repository;

import com.intarea.intarea.domain.CanceledOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CanceledOrderRepository extends JpaRepository<CanceledOrder, Long> {

}
