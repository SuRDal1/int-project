package com.intarea.intarea;

import com.intarea.intarea.domain.*;
import com.intarea.intarea.dto.*;
import com.intarea.intarea.repository.ProcessARepository;
import com.intarea.intarea.repository.ProcessBRepository;
import com.intarea.intarea.service.OrderService;
import com.intarea.intarea.service.PredictAService;
import com.intarea.intarea.service.PredictBService;
import com.intarea.intarea.service.ProcessDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessScheduler {

    private final SimpMessagingTemplate messagingTemplate;
    private final ProcessARepository processARepository;
    private final ProcessBRepository processBRepository;
    private final PredictAService predictAService;
    private final PredictBService predictBService;
    private final OrderService orderService;
    private final ProcessDataService processDataService;

    // 매초마다 현재 시각 반영
    @Scheduled(fixedRate = 1000)
    public void sendCurrentTime() {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        messagingTemplate.convertAndSend("/topic/time", currentTime);
    }

    // 공정A 하나씩 처리
    @Scheduled(fixedRate =  5000) // 5초
    public void processAOneByOne(){
        // 1. 스케줄러 준비
        log.info("스케줄러 호출 확인");

        Optional<ProcessA> orderByAsc = processARepository.findFirstByStatusOrderByOrdersIdAsc(Status.WAITING);

        if (orderByAsc.isEmpty()) return;

        ProcessA current = orderByAsc.get();

        log.info("가져온 공정 : 순서 = {}, 주문ID = {}", current.getStep(), current.getOrders().getId());

        // 2. 공정A 처리
        //제품이름
        ProductName productName = current.getOrders().getProductName();

        // 지정 범위 내 랜덤한 온도 (100.0 ~ 300.0도)
        double temp = ThreadLocalRandom.current().nextDouble(100.0, 300.0);

        // 지정 범위 내 랜덤한 압력 (1.0 ~ 25.0 KPa)
        double press = ThreadLocalRandom.current().nextDouble(1.0,25.0);


        // 모델에 값 입력해 품질 얻기
        PredictARequest request = new PredictARequest();
        PredictionAOrderInput orderInput = new PredictionAOrderInput();
        orderInput.setProductName(productName);
        orderInput.setTemp(temp);
        orderInput.setPress(press);

        ArrayList<PredictionAOrderInput> list = new ArrayList<>();
        list.add(orderInput);

        log.info("Java 입력 list :  {}", list);

        request.setOrders(list);

        PredictAResult result = predictAService.getPrediction(request);

        // 품질값 100이하로 제한 + 소숫점 한 자리 표시 처리
        double quality = result.getPrediction()>100? 100.0: result.getPrediction();

        //품질 기반으로 공정상태 결정(OK 또는 NG)
        // 기준치 99.0
        boolean isOk = quality>=99.0;

        // 가져온 공정 업뎃
        current.setTemp(temp);
        current.setPress(press);
        current.setProcessQuality(quality);
        current.setStatus(isOk ? Status.OK:Status.NG);
        current.setProcessDateTime(LocalDateTime.now());
        processARepository.save(current);

        // 3. 모든 제품의 공정 종료 확인 후 공정 종료 처리
        Long ordersId = current.getOrders().getId();

        // Status.WAITING = 공정대기상태인 공정A 개수 확인
        long finishedCount = processARepository.countByOrdersIdAndStatus(ordersId,Status.WAITING);

        // 남은 공정대기수가 0이면 공정 완료로 상태 변경
        if(finishedCount == 0){
            orderService.updateOrderState(current.getOrders().getId(), OrderState.COMPLETED);
        }

        // 4. 웹으로 공정 데이터 전송
        // 저장하는 공정A값 전송 준비
        ProcessADto dto = new ProcessADto(
                current.getLotNumber(),
                current.getStatus().name(),
                current.getTemp(),
                current.getPress(),
                current.getOrders().getProductName().getLabel());

        log.info("Sending to WebSocket : {}", current.getLotNumber());

        // 공정A 웹소켓으로 전송
        messagingTemplate.convertAndSend("/topic/processA",dto);


        // 5. 주문별 OK/NG 수량 계산 및 전송
        ProcessDataDto dataA = processDataService.getProcessAData(ordersId);

        Map<String, Object> orderStats = new HashMap<>();
        orderStats.put("orderId", ordersId);
        orderStats.put("ok", dataA.getOkProduct());
        orderStats.put("ng", dataA.getNgProduct());

        messagingTemplate.convertAndSend("/topic/orderStats/" + ordersId, orderStats);

        // 6. progress바용 데이터 전송
        updateAndSendProgress();
    }

    // 공정B 하나씩 처리
    @Scheduled(fixedRate =  5000)
    public void processBOneByOne(){
        log.info("스케줄러 호출 확인");

        Optional<ProcessB> orderByAsc = processBRepository.findFirstByStatusOrderByOrdersIdAsc(Status.WAITING);

        if (orderByAsc.isEmpty()) return;

        ProcessB current = orderByAsc.get();

        log.info("가져온 공정 : 순서 = {}, 주문ID = {}", current.getStep(), current.getOrders().getId());

        // 2. 공정B 처리
        //제품이름
        ProductName productName = current.getOrders().getProductName();

        // 랜덤한 상열(좌) 종료 온도 (215.3 ~ 220.1도)
        double lTemp = ThreadLocalRandom.current().nextDouble(215.3, 220.1);

        // 랜덤한 상열(우) 종료 온도 (216.6 ~ 220.6도)
        double rTemp = ThreadLocalRandom.current().nextDouble(216.6, 220.6);

        // 랜덤한 상열(중) 종료 온도 (상열온도(좌/우)의 평균 ~ 230.3도)
        double mTemp = ThreadLocalRandom.current().nextDouble((lTemp+rTemp)/2, 230.3);


        // 모델에 값 입력
        PredictBRequest request = new PredictBRequest();
        PredictionBOrderInput orderInput = new PredictionBOrderInput();
        orderInput.setProductName(productName);
        orderInput.setLeftHighTemp(lTemp);
        orderInput.setMidHighTemp(mTemp);
        orderInput.setRightHighTemp(rTemp);

        ArrayList<PredictionBOrderInput> list = new ArrayList<>();
        list.add(orderInput);

        log.info("Java 입력 list :  {}", list);

        request.setOrders(list);

        PredictBResult result = predictBService.getPrediction(request);

        // 모델 결과값 = 공정상태 결정(OK 또는 NG)
        boolean isOk = result.isPrediction();


        current.setLeftHighTemp(lTemp);
        current.setMidHighTemp(mTemp);
        current.setRightHighTemp(rTemp);
        current.setPass(isOk);
        current.setStatus(isOk ? Status.NG:Status.OK);
        current.setProcessDateTime(LocalDateTime.now());
        processBRepository.save(current);

        // 3. 모든 제품의 공정 종료 확인 후 공정 종료 처리
        Long ordersId = current.getOrders().getId();

        // Status.WAITING = 공정대기상태인 공정A 개수 확인
        long finishedCount = processBRepository.countByOrdersIdAndStatus(ordersId,Status.WAITING);

        // 남은 공정대기수가 0이면 공정 완료로 상태 변경
        if(finishedCount == 0){
            orderService.updateOrderState(current.getOrders().getId(), OrderState.COMPLETED);
        }

        // 4. 웹으로 공정 데이터 전송
        // 저장하는 공정B값 전송 준비
        ProcessBDto dto = new ProcessBDto(
                current.getLotNumber(),
                current.getStatus().name(),
                current.getLeftHighTemp(),
                current.getMidHighTemp(),
                current.getRightHighTemp(),
                current.getOrders().getProductName().getLabel());

        log.info("Sending to WebSocket : {}", current.getLotNumber());

        // 공정B 웹소켓으로 전송
        messagingTemplate.convertAndSend("/topic/processB",dto);


        // 5. 주문별 OK/NG 수량 계산 및 전송
        ProcessDataDto dataA = processDataService.getProcessBData(ordersId);

        Map<String, Object> orderStats = new HashMap<>();
        orderStats.put("orderId", ordersId);
        orderStats.put("ok", dataA.getOkProduct());
        orderStats.put("ng", dataA.getNgProduct());

        messagingTemplate.convertAndSend("/topic/orderStats/" + ordersId, orderStats);

        // 6. progress바용 데이터 전송
        updateAndSendProgress();
    }

    // progress바 실시간화
    public void updateAndSendProgress() {
        List<Orders> todayOrders = orderService.findOrdersByProcessDate(LocalDate.now());

        long totalOk = 0;
        long totalNg = 0;

        for (Orders order : todayOrders) {
            ProcessDataDto dto = (order.getProductName().name().equals("LAMP_COVER"))
                    ? processDataService.getProcessBData(order.getId())
                    : processDataService.getProcessAData(order.getId());

            totalOk += dto.getOkProduct();
            totalNg += dto.getNgProduct();
        }

        long total = todayOrders.stream()
                .mapToLong(Orders::getOrderQuantity)
                .sum();

        int okPercent = total > 0 ? (int) ((double) totalOk / total * 100) : 0;
        int ngPercent = total > 0 ? (int) ((double) totalNg / total * 100) : 0;

        Map<String, Object> progress = new HashMap<>();
        progress.put("total", total);
        progress.put("ok", totalOk);
        progress.put("ng", totalNg);
        progress.put("okPercent", okPercent);
        progress.put("ngPercent", ngPercent);

        messagingTemplate.convertAndSend("/topic/progress", progress);
    }

}
