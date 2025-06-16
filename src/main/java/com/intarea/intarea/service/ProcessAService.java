package com.intarea.intarea.service;

import com.intarea.intarea.domain.*;
import com.intarea.intarea.repository.ProcessARepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessAService {
    private final ProcessARepository processARepository;

    //공정 시행 및 저장
    public void processSave(Orders orders, Users users){

        //날짜 포맷 지정
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        //현재 날짜를 YYYYMMDD형식으로 변환
        String currentDate = LocalDateTime.now().format(dateTimeFormatter);

        //주문수량
        int orderQuantity = orders.getOrderQuantity();

        //제품이름
        ProductName productName = orders.getProductName();

        //주문처명
        OrderCompany orderCompany = orders.getOrderCompany();

        // 공정 횟수 반복
        for(int i=1; i<=orderQuantity; i++){

            String processStep = String.format("%01d",i);

            //lotNumber만들기 주문자_제품이름_오더아이디_제품순서_생성일
            String lotNumber = orderCompany+"_"+(productName.ordinal()+1)+"_"+processStep+"_"+currentDate;

            //ProcessA 객체 생성 및 저장
            ProcessA processA = ProcessA.builder()
                    .lotNumber(lotNumber)
                    .step(i)
                    .status(Status.WAITING)
                    .processDateTime(LocalDateTime.now())
                    .orders(orders)
                    .users(users)
                    .build();

            processARepository.save(processA);

        }
    }


    // 전체공정조회
    public List<ProcessA> findAllProcessA(){
        return processARepository.findAll();
    }


    //jobOrderId로 조회
    public List<ProcessA>findAllOrders(Long ordersId){
        return processARepository.findAllByOrders_Id(ordersId);
    }


}
