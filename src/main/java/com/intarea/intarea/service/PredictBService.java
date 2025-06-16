package com.intarea.intarea.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intarea.intarea.domain.PredictionBOrderInput;
import com.intarea.intarea.domain.PredictionBOrderRecord;
import com.intarea.intarea.dto.*;
import com.intarea.intarea.repository.PredictionBOrderRecordRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Slf4j
public class PredictBService {

    private final PredictionBOrderRecordRepository predictionRecordRepository;

    // 예측을 위해 요청을 보낼 Flask API 엔드포인트 주소값을 상수 지정.
    private final String FLASK_URL = "http://localhost:5000/predictB";


    // 예측 요구값을 받아 예측 결과를 반환하는 메서드
    public PredictBResult getPrediction(PredictBRequest request) {

        // 1. Flask 에측 요청
        // 1-1. RestTemplates 생성 - 스프링에서 HTTP요청을 쉽게 보낼 수 있는 클래스(클라이언트 역할)
        RestTemplate restTemplate = new RestTemplate();

        // 1-2. 요청 헤더 설정 - Content-Type을 JSON으로 설정 -> Flask맞춤.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 1-3. 요청 엔티티 생성 - 실제로 보낼 요청 본문과 헤더를 모두 포함한 HttpEntity 생성.
        HttpEntity<PredictBRequest> entity = new HttpEntity<>(request, headers);

        // 1-4. Flask API 호출 - exchange()로 POST요청 보내기.
        ResponseEntity<PredictBResponse> response = restTemplate.exchange(
                FLASK_URL,
                HttpMethod.POST,
                entity,
                PredictBResponse.class // 결과값을 PredictResponse.class형태로 자동 변환.
        );

        // 모델의 예측 결과 - double 값으로.
        boolean prediction = response.getBody().getPrediction()>0.54;
        // JSON형태의 응답결과를 Java객체로 바꾸고, 그 안에 있는 prediction값을 꺼냄.
        // flask 확인 = return jsonify({'prediction':float(y_pred)})
        
        
        // 2. JSON 원본 저장 - request(요청값)를 JSON문자열로 직렬화(serialize)해 저장

        // 2-1 ObjectMapper 생성 - 자바 객체와 JSON 문자열간의 변환 담당
        ObjectMapper mapper = new ObjectMapper();
        String inputJson = null;
        try {
            inputJson = mapper.writeValueAsString(request); // request객체를 JSON문자열로.
        } catch (JsonProcessingException e){
            log.error("예측 처리 중 에러 발생", e); // log에 처리 중 에러 출력, 실무에선 log 자주 사용.
        }

        // 3. 예측 요청 정보와 결과를 DB에 저장

        PredictionBOrderInput orderInput = request.getOrders().get(0);

        // PredictionRecord 생성
        PredictionBOrderRecord record = new PredictionBOrderRecord();

        record.setInputData(inputJson);
        record.setPrediction(prediction);
        record.setPredictAt(LocalDateTime.now());
        record.setOrderInput(orderInput);

        orderInput.setRecord(record);

        // 5.저장
        predictionRecordRepository.save(record);

        // 6. 결과값 받아 반환
        return new PredictBResult(prediction);

    }
    
}
