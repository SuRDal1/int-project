package com.intarea.intarea.repository;

import com.intarea.intarea.domain.PredictionBOrderRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PredictionBOrderRecordRepository extends JpaRepository<PredictionBOrderRecord, Long> {

}
