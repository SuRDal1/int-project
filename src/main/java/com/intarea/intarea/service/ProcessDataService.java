package com.intarea.intarea.service;

import com.intarea.intarea.domain.Orders;
import com.intarea.intarea.domain.ProcessA;
import com.intarea.intarea.domain.ProcessB;
import com.intarea.intarea.domain.Status;
import com.intarea.intarea.dto.ProcessDataDto;
import com.intarea.intarea.repository.ProcessARepository;
import com.intarea.intarea.repository.ProcessBRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessDataService {

    private final OrderService orderService;
    private final ProcessARepository processARepository;
    private final ProcessBRepository processBRepository;
    private final ProcessAService processAService;
    private final ProcessBService processBService;

    // 공정A용 로직
    public ProcessDataDto getProcessAData(Long ordersId){
        List<ProcessA> processAList = processAService.findAllOrders(ordersId);
        List<ProcessB> processBList = new ArrayList<>();


        //차트를 위한 추가
        //LotNumber 별로 모든 공정 상태를 set<String>으로 저장
        Map<String, Set<Status>>lotStatusMap =
                processAList.stream()
                        .collect(Collectors.groupingBy(ProcessA::getLotNumber,
                                Collectors.mapping(ProcessA::getStatus, Collectors.toSet())));
        log.info("lot별 ? {}",lotStatusMap);

        //모든 상태가 "OK"인 LotNumber만 선택하여 개수 계산
        //Set<String>의 크기가 1이고, 그 값이 "OK"라면 해당 LotNumber는 완제품
        long okProduct = lotStatusMap.values().stream()
                .filter(statuses -> statuses.contains(Status.OK)).count();
        log.info("lot별 모두 ok인 완제품 개수 {}", okProduct);

        //모든 상태가 "OK"인 LotNumber만 선택하여 개수 계산
        //Set<String>의 크기가 1이고, 그 값이 "OK"라면 해당 LotNumber는 완제품
        long ngProduct = lotStatusMap.values().stream()
                .filter(statuses -> statuses.contains(Status.NG)).count();
        log.info("lot별 모두 ng인 완제품 개수 {}", ngProduct);

        //공정 전체 개수, 주문수
        int totalOrder = (processAList.size()); //주문수
        log.info("주문수 {}", totalOrder);

        //DTO에 담아서 반환
        return new ProcessDataDto(processAList, processBList, okProduct ,ngProduct,totalOrder);
    }


    // 공정 B용 로직
    public ProcessDataDto getProcessBData(Long ordersId){
        List<ProcessB> processBList = processBService.findAllOrders(ordersId);
        List<ProcessA> processAList = new ArrayList<>();

        //차트를 위한 추가

        //lotNumber별 모든 공정이 "OK"인지를 확인하여 공정 개수 계산 - 로트번호별 상태값을 가져온다
        //LotNumber 별로 모든 공정 상태를 set<String>으로 저장
        Map<String, Set<Status>>lotStatusMap =
                processBList.stream()
                        .collect(Collectors.groupingBy(ProcessB::getLotNumber,
                                Collectors.mapping(ProcessB::getStatus, Collectors.toSet())));
        log.info("lot별 ? {}",lotStatusMap);

        //모든 상태가 "OK"인 LotNumber만 선택하여 개수 계산
        //Set<String>의 크기가 1이고, 그 값이 "OK"라면 해당 LotNumber는 완제품
        long okProduct = lotStatusMap.values().stream()
                .filter(statuses -> statuses.contains(Status.OK)).count();
        log.info("lot별 모두 ok인 완제품 개수 {}", okProduct);

        //모든 상태가 "OK"인 LotNumber만 선택하여 개수 계산
        //Set<String>의 크기가 1이고, 그 값이 "OK"라면 해당 LotNumber는 완제품
        long ngProduct = lotStatusMap.values().stream()
                .filter(statuses -> statuses.contains(Status.NG)).count();
        log.info("lot별 모두 ng인 완제품 개수 {}", ngProduct);

        //공정 전체 개수, 주문수
        int totalOrder = (processBList.size()); //주문수
        log.info("주문수 {}", totalOrder);

        //DTO에 담아서 반환
        return new ProcessDataDto(processAList,processBList, okProduct ,ngProduct,totalOrder);

    }


    // 전체 제품 상태별 차트용 메서드
    public Map<Status, Long> getAllStatusSum(){

        List<Orders> allOrders = orderService.findOrders();

        // 전체 주문에 대한 각 공정 상태별 카운팅
        List<Object[]> processAResults = processARepository.countByStatusInOrders(allOrders);
        List<Object[]> processBResults = processBRepository.countByStatusInOrders(allOrders);

        // 상태별 카운트를 가진 statusCountMap 생성 =  EnumMap<>(Status.class)으로 각 ENUM별로 지정.
        Map<Status, Long> statusCountMap = new EnumMap<>(Status.class);

        // 공정A
        for (Object[] row : processAResults) {
            Status status = (Status) row[0];
            Long count = (Long) row[1];
            statusCountMap.merge(status, count, Long::sum);
        }

        // 공정B
        for (Object[] row : processBResults) {
            Status status = (Status) row[0];
            Long count = (Long) row[1];
            statusCountMap.merge(status, count, Long::sum);
        }

        // 체크
        for (Status status : Status.values()) {
            log.info("{}: {}" , status, statusCountMap.getOrDefault(status, 0L));
        }

        return statusCountMap;

    }


    // 제품별 bar차트용 메서드
    public Map<String, Map<String, Integer>> getAllProductStats(){
        Map<String, Integer> okByProductA = new HashMap<>();
        Map<String, Integer> ngByProductA = new HashMap<>();
        for (ProcessA p: processAService.findAllProcessA()) {
            String name = p.getOrders().getProductName().name();
            if (p.getStatus() == Status.OK) {
                okByProductA.merge(name, 1, Integer::sum);
            } else if (p.getStatus() == Status.NG) {
                ngByProductA.merge(name, 1, Integer::sum);
            }
        }

        Map<String, Integer> okByProductB = new HashMap<>();
        Map<String, Integer> ngByProductB = new HashMap<>();
        for (ProcessB p: processBService.findAllProcessB()){
            String name = p.getOrders().getProductName().name();
            if (p.getStatus() == Status.OK) {
                okByProductB.merge(name, 1, Integer::sum);
            }else if (p.getStatus() == Status.NG) {
                ngByProductB.merge(name, 1, Integer::sum);
            }
        }

        Map<String, Integer> totalProduct = new HashMap<>();
        for (ProcessA p: processAService.findAllProcessA()) {
            String name = p.getOrders().getProductName().name();
            totalProduct.merge(name, 1, Integer::sum);
        }
        for (ProcessB p: processBService.findAllProcessB()){
            String name = p.getOrders().getProductName().name();
            totalProduct.merge(name, 1, Integer::sum);
        }

        Map<String, Map<String, Integer>> result = new HashMap<>();
        result.put("total",totalProduct);
        result.put("okA", okByProductA);
        result.put("ngA", ngByProductA);
        result.put("okB", okByProductB);
        result.put("ngB", ngByProductB);

        return result;
    }

}
