package com.intarea.intarea.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intarea.intarea.domain.PredictMaterialOrderInput;
import com.intarea.intarea.domain.PredictionMaterialRecord;
import com.intarea.intarea.dto.MaterialPredictRequest;
import com.intarea.intarea.dto.MaterialPredictResponse;
import com.intarea.intarea.dto.MaterialPredictResult;
import com.intarea.intarea.repository.MaterialPredictRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class MaterialPredictService {

    private final MaterialPredictRepository materialPredictRepository;

    // Flask API 주소 – 3일 뒤 예측 모델
    private final String FLASK_URLD3 = "http://localhost:8000/predictD3";

    // Flask API 주소 – 5일 뒤 예측 모델
    private final String FLASK_URLD5 = "http://localhost:8000/predictD5";


    public MaterialPredictResult getPrediction(MaterialPredictRequest request) {

        // 1. RestTemplate 생성 - 외부 HTTP 요청을 보낼 때 사용되는 클래스
        RestTemplate restTemplate = new RestTemplate();

        // 2. 요청 헤더 설정 - Content-Type을 application/json으로 설정 (Flask는 JSON 형식의 요청을 받음)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 3. 요청 본문과 헤더를 합쳐 HttpEntity 생성 (Flask로 보낼 데이터)
        HttpEntity<MaterialPredictRequest> entity = new HttpEntity<>(request, headers);

        // 4. 예측 타입 확인 - D3 또는 D5를 기준으로 예측 날짜 결정
        String predictType = request.getPredictType(); // D3: 3일 후, D5: 5일 후 예측
        String url;         // Flask 호출 주소
        int daysToAdd;      // 예측일 수

        if ("D3".equalsIgnoreCase(predictType)) {
            url = FLASK_URLD3;
            daysToAdd = 3;
        } else if ("D5".equalsIgnoreCase(predictType)) {
            url = FLASK_URLD5;
            daysToAdd = 5;
        } else {
            // 예외 처리 – 지원하지 않는 타입일 경우
            throw new IllegalArgumentException("지원하지 않는 예측 타입입니다: " + predictType);
        }

        // 5. Flask API 호출 – 예측 결과 받아오기
        ResponseEntity<MaterialPredictResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                MaterialPredictResponse.class
        );

        // 6. Flask에서 받은 예측 결과 (json의 prediction 값 추출)
        double prediction = response.getBody().getPrediction();

        // 7. 요청 객체(request)를 JSON 문자열로 변환 – DB에 저장하기 위함
        ObjectMapper mapper = new ObjectMapper();
        String inputJson = null;
        try {
            inputJson = mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.error("예측 처리 중 JSON 직렬화 에러 발생", e);
        }

        // 8. 예측 결과를 저장할 엔티티(PredictionMaterialRecord) 생성
        PredictionMaterialRecord record = new PredictionMaterialRecord();
        record.setInputData(inputJson);               // 입력 데이터 JSON 저장
        record.setPrediction(prediction);             // 예측 결과 저장
        record.setPredictAt(LocalDateTime.now());     // 예측 처리 시각 저장

        // 9. 입력된 orders 리스트를 개별 엔티티(PredictMaterialOrderInput)로 변환하여 record에 연결
        List<PredictMaterialOrderInput> orderInputList = request.getOrders().stream().map(order -> {
            PredictMaterialOrderInput input = new PredictMaterialOrderInput();
            input.setDate(order.getDate());
            input.setQty(order.getQty());
            input.setMaterialName(order.getMaterialName());
            input.setRecord(record);
            return input;
        }).toList();

        // 10. 양방향 매핑 설정
        record.setOrderInputs(orderInputList);

        // 11. 예측 결과 및 입력 정보 저장
        materialPredictRepository.save(record);

        // 12. 입력된 날짜 중 가장 최신 날짜 추출
        LocalDate maxDate = request.getOrders().stream()
                .map(order -> LocalDate.parse(order.getDate()))
                .max(Comparator.naturalOrder())
                .orElse(LocalDate.now());

        // 13. 예측 날짜 계산 – 최신 날짜 기준으로 3일 또는 5일 뒤
        LocalDate predictedDate = maxDate.plusDays(daysToAdd);

        // 14. 예측 결과와 예측 날짜를 포함한 응답 객체 반환
        return new MaterialPredictResult(prediction, predictedDate);
    }
}
