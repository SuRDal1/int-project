package com.intarea.intarea.controller;

import com.intarea.intarea.domain.*;
import com.intarea.intarea.dto.*;
import com.intarea.intarea.service.OrderService;
import com.intarea.intarea.service.PredictBService;
import com.intarea.intarea.service.ProcessBService;
import com.intarea.intarea.service.ProcessDataService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("process/b")
public class ProcessBController {

    private final ProcessBService processBService;
    private final OrderService orderService;
    private final PredictBService predictBService;
    private final ProcessDataService processDataService;

    // 공정 시작
    @PostMapping("/{ordersId}")
    public String createProcessB(@PathVariable("ordersId")Long ordersId, HttpSession session){
        Orders orders = orderService.findOneOrder(ordersId).orElseThrow(()->new IllegalArgumentException("해당 작업오더를 찾을 수 없습니다."));

        // 상태변경 저장
        orders.setOrderState(OrderState.IN_PROGRESS);
        orderService.updateOrderState(orders.getId(),orders.getOrderState());

        // 공정 담당자 정보
        Users users = (Users) session.getAttribute(SessionConst.LOGIN_MEMBER);

        // 공정 생성 및 저장
        processBService.processSave(orders, users);

        return "redirect:/";
    }

    // 공정보기 페이지 열기
    @GetMapping("/{ordersId}")
    public String ProcessBList(@PathVariable("ordersId")Long ordersId, Model model){

        ProcessDataDto processBData = processDataService.getProcessBData(ordersId);

        model.addAttribute("processBList", processBData.getProcessBList());
        model.addAttribute("ordersId",ordersId);
        model.addAttribute("okProduct", processBData.getOkProduct()); // 공정별 ok
        model.addAttribute("ngProduct", processBData.getNgProduct()); // 공정별 ng
        model.addAttribute("totalOrder", processBData.getTotalOrder());//주문수
        return "process/processBList";
    }


    // 공정B 예측 페이지 열기
    @GetMapping("/predict")
    public String openPredict(Model model, HttpSession session){

        // 로그인 여부 체크
        Users loginMember = (Users) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (loginMember == null) {
            return "redirect:/";
        }

        // 모델 입력값 리스트로 설정
        PredictBRequest request = new PredictBRequest();

        ArrayList<PredictionBOrderInput> list = new ArrayList<>();
        list.add(new PredictionBOrderInput());

        log.info("list{}",list);

        request.setOrders(list);
        model.addAttribute("request",request);
        return "predict/processBPredictForm";
    }

    // 공정B 예측 결과 불러오기
    @PostMapping("/predict")
    public String makePredict(@Valid @ModelAttribute("request") PredictBRequest request, BindingResult bindingResult, Model model){

        if(bindingResult.hasErrors()){
            return "predict/processBPredictForm";
        }

        PredictBResult result = predictBService.getPrediction(request);

        log.info("예측결과는? : {}", request.getOrders());
        model.addAttribute("prediction", result.isPrediction());
        return "predict/processBPredictForm";
    }

}
