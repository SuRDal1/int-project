package com.intarea.intarea.repository;

import com.intarea.intarea.domain.PredictionMaterialRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialPredictRepository extends JpaRepository<PredictionMaterialRecord, Long> {

}
