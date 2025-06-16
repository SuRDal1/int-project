package com.intarea.intarea.repository;

import com.intarea.intarea.domain.PredictionAOrderRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PredictionAOrderRecordRepository extends JpaRepository<PredictionAOrderRecord, Long> {

}
