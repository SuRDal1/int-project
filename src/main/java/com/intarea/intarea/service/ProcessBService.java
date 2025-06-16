package com.intarea.intarea.service;

import com.intarea.intarea.domain.*;
import com.intarea.intarea.repository.ProcessBRepository;
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
public class ProcessBService {
    private final ProcessBRepository processBRepository;

    //공정 시행 및 저장
    public void processSave(Orders orders, Users users){
        //날짜 포맷 지정
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        //현재 날짜를 YYYYMMDD형식으로 변환
        String currentDate = LocalDateTime.now().format(dateTimeFormatter);

        //주문수량
        int orderQuantity = orders.getOrderQuantity();

        //주문처명
        OrderCompany orderCompany = orders.getOrderCompany();

        //OrdersId
        Long ordersId = orders.getId();

        for(int i=1; i<=orderQuantity; i++){

            String processStep = String.format("%01d",i);

            //lotNumber만들기 제품이름_오더아이디_제품순서_생성일
            String lotNumber = orderCompany+"_"+"LC"+"_"+ordersId+"_"+processStep+"_"+currentDate;


            //ProcessB 객체 생성 및 저장
            ProcessB processB = ProcessB.builder()
                    .lotNumber(lotNumber)
                    .step(i)
                    .status(Status.WAITING)
                    .processDateTime(LocalDateTime.now())
                    .orders(orders)
                    .users(users)
                    .build();

            processBRepository.save(processB);

        }

    }

    // 전체공정조회
    public List<ProcessB> findAllProcessB(){
        return processBRepository.findAll();
    }


    // jobOrderId로 조회
    public List<ProcessB>findAllOrders(Long ordersId){
        return processBRepository.findAllByOrders_Id(ordersId);
    }

}
