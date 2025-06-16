package com.intarea.intarea.controller;

import com.intarea.intarea.domain.*;
import com.intarea.intarea.dto.PredictARequest;
import com.intarea.intarea.dto.PredictAResult;
import com.intarea.intarea.dto.ProcessDataDto;
import com.intarea.intarea.service.OrderService;
import com.intarea.intarea.service.PredictAService;
import com.intarea.intarea.service.ProcessAService;
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
@RequestMapping("process/a")
public class ProcessAController {

    private final ProcessAService processAService;
    private final OrderService orderService;
    private final PredictAService predictAService;
    private final ProcessDataService processDataService;

    // 공정 시작
    @PostMapping("/{ordersId}")
    public String createProcessA(@PathVariable("ordersId")Long ordersId,  HttpSession session){

        Orders orders = orderService.findOneOrder(ordersId).orElseThrow(()->new IllegalArgumentException("해당 작업오더를 찾을 수 없습니다."));

        // 상태변경 저장
        orders.setOrderState(OrderState.IN_PROGRESS);
        orderService.updateOrderState(orders.getId(),orders.getOrderState());

        // 공정 담당자 정보
        Users users = (Users) session.getAttribute(SessionConst.LOGIN_MEMBER);

        // 공정 생성 및 저장
        processAService.processSave(orders, users);

        return "redirect:/";
    }

    // 공정보기 페이지 열기
    @GetMapping("/{ordersId}")
    public String ProcessAList(@PathVariable("ordersId") Long ordersId, Model model){
        ProcessDataDto processAData = processDataService.getProcessAData(ordersId);

        model.addAttribute("processAList", processAData.getProcessAList());
        model.addAttribute("ordersId",ordersId);
        model.addAttribute("okProduct", processAData.getOkProduct()); // 공정별 ok
        model.addAttribute("ngProduct", processAData.getNgProduct()); // 공정별 ng
        model.addAttribute("totalOrder", processAData.getTotalOrder());//주문수

        return "process/processAList";
    }


    // 공정A 예측 페이지 열기
    @GetMapping("/predict")
    public String openPredict(Model model, HttpSession session){

        // 로그인 여부 체크
        Users loginMember = (Users) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (loginMember == null) {
            return "redirect:/";
        }

        // 모델 입력값 리스트로 설정
        PredictARequest request = new PredictARequest();

        ArrayList<PredictionAOrderInput> list = new ArrayList<>();
        list.add(new PredictionAOrderInput());

        log.info("list{}",list);

        request.setOrders(list);
        model.addAttribute("request",request);
        return "predict/processAPredictForm";
    }

    // 공정A 예측 결과 불러오기
    @PostMapping("/predict")
    public String makePredict(@Valid @ModelAttribute("request") PredictARequest request, BindingResult bindingResult, Model model){

        if(bindingResult.hasErrors()){
            return "predict/processAPredictForm";
        }

        PredictAResult result = predictAService.getPrediction(request);

        log.info("예측결과는? : {}", request.getOrders());
        model.addAttribute("prediction", result.getPrediction());
        return "predict/processAPredictForm";
    }

}
