package com.intarea.intarea.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intarea.intarea.domain.*;
import com.intarea.intarea.dto.MaterialForm;
import com.intarea.intarea.dto.MaterialPredictRequest;
import com.intarea.intarea.dto.MaterialPredictResult;
import com.intarea.intarea.service.MaterialPredictService;
import com.intarea.intarea.service.MaterialService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;


@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/material")
public class MaterialController {

    private final MaterialService materialService;
    private final MaterialPredictService materialPredictService;

    // 재료 입력 폼 열기(뷰 영역)
    @GetMapping
    public String materialNew(@RequestParam(value = "materialName", required = false) String materialName, Model model, HttpSession session) {

        // 로그인 여부 체크
        Users loginMember = (Users) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if(loginMember==null) {
            return "redirect:/";
        }

        // 부족재료 선택해 넘어오면 선택된 상태가 적용되도록!
        MaterialForm form = new MaterialForm();
        if (materialName != null && !materialName.isBlank()) {
            form.setMaterialName(MaterialName.valueOf(materialName));
        }


        model.addAttribute("materialForm", form);


        // 라디오 버튼 작동 위해 리스트도 넘기기
        List<MaterialName> materialNameList = Arrays.asList(MaterialName.values());
        List<Company> companyList = Arrays.asList(Company.values());

        model.addAttribute("material", materialNameList);
        model.addAttribute("company", companyList);

        return "material/materialForm";
    }


    // 재료 저장
    @PostMapping
    public String materialNewPost(@Valid MaterialForm materialForm, BindingResult bindingResult, @ModelAttribute("loginMember") Users loginMember, Model model) {

        // 누락값 검증
        if(bindingResult.hasErrors()) {
            // model로 리스트도 다시 넘겨주어야 라디오 버튼 정상작동
            List<MaterialName> materialNameList = Arrays.asList(MaterialName.values());
            List<Company> companyList = Arrays.asList(Company.values());

            model.addAttribute("material", materialNameList);
            model.addAttribute("company", companyList);

            return "material/materialForm";
        }

        // 재료 저장
        Material material = Material.builder()
                .materialName(materialForm.getMaterialName())
                .quantity(materialForm.getQuantity())
                .company(materialForm.getCompany())
                .users(loginMember)
                .dateTime(LocalDateTime.now())
                .build();

        materialService.saveMaterial(material);

        // 재료 입력 멤버 확인용
        log.info("현재 로그인 멤버 : {}", loginMember);

        return "redirect:/material/list";
    }


    // 재료 리스트 열기
    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "15") int size,
                       Model model , HttpSession session) {

        // 로그인 여부 체크
        Users loginMember = (Users) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (loginMember == null) {
            return "redirect:/";
        }


        // 2. 개별 재료 총량 확인 및 알림 기능용 Map
        // 개별 재료들의 총합 불러오기
        Map<MaterialName, Long> materialQuantityMap = materialService.getMaterialQuantities();

        // Model로 Map 넘기기
        model.addAttribute("materialQuantityMap", materialQuantityMap);


        //3. 현재 총 개수가 50개보다 적은 재료 찾아 List화 (부족분 알림 기능용)
        List<MaterialName> lowStockList = materialQuantityMap.entrySet().stream()
                .filter(entry -> entry.getValue() <= 50)
                .map(Map.Entry::getKey)
                .toList();

        // Model로 넘기기
        model.addAttribute("lowStockList", lowStockList);

        // 3. 페이징 처리
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateTime").ascending());
        Page<Material> materialPage = materialService.findAllMaterials(pageable);

        model.addAttribute("materialList", materialPage.getContent());
        model.addAttribute("materialPage", materialPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", materialPage.getTotalPages());

        // 페이지 열기
        return "material/materialList";
    }



    // 재료 정보 수정 열기
    @GetMapping("/{materialId}/update")
    public String update(@PathVariable("materialId") Long materialId, Model model) {
        
        // 개별 재료 조회
        Material material = materialService.findOne(materialId)
                .orElseThrow(() -> new IllegalArgumentException("해당 제품을 찾을 수 없습니다."));

        // 웹페이지 표시용 materialForm 생성
        MaterialForm materialForm = MaterialForm.builder()
                .id(material.getId())
                .materialName(material.getMaterialName())
                .quantity(material.getQuantity())
                .company(material.getCompany())
                .build();

        // model로 넘기기
        model.addAttribute("materialForm", materialForm);

        return "material/updateMaterial";
    }

    // 재료 정보 수정 적용
    @PostMapping("/{materialId}/update")
    public String updatePost(@PathVariable("materialId") Long materialId,
                             @Valid @ModelAttribute("materialForm") MaterialForm materialForm,
                             BindingResult bindingResult,
                             Model model) {

        // 입력 오류 검증(일반적으로 볼 경우는 드묾)
        if (bindingResult.hasErrors()) {
            Material material = materialService.findOne(materialId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 제품을 찾을 수 없습니다."));

            materialForm.setId(material.getId());
            materialForm.setMaterialName(material.getMaterialName());
            materialForm.setQuantity(material.getQuantity());
            materialForm.setCompany(material.getCompany());
            model.addAttribute("materialForm", materialForm);

            return "material/updateMaterial";
        }

        // 제료 정보 업뎃
        Material material = materialService.findOne(materialId)
                .orElseThrow(() -> new IllegalArgumentException("해당 제품을 찾을 수 없습니다."));

        material.toBuilder()
                .materialName(materialForm.getMaterialName())
                .quantity(materialForm.getQuantity())
                .company(materialForm.getCompany())
                .build();

        material.update(String.valueOf(materialForm.getMaterialName()), materialForm.getQuantity(), materialForm.getCompany());
        materialService.saveMaterial(material);

        return "redirect:/material/list";
    }

    
    // 재료 정보 삭제
    @GetMapping("/{materialId}/delete")
    public String delete(@PathVariable("materialId") Long materialId) {

        Material material = materialService.findOne(materialId).orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));
        materialService.deleteMaterial(material.getId());

        return "redirect:/material/list?deleted=true";
    }


    // 재료 발주 예상 페이지 열기
    @GetMapping("/predict")
    public String predictForm(Model model, HttpSession session) {

        // 로그인 여부 체크
        Users loginMember = (Users) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (loginMember == null) {
            return "redirect:/";
        }

        // 3일치 데이터를 모델에 입력 필요.
        MaterialPredictRequest request = new MaterialPredictRequest();
        //초깃값(3일후) 설정
        request.setPredictType("D3");

        List<PredictMaterialOrderInput> orders = new ArrayList<>();
        orders.add(new PredictMaterialOrderInput());
        orders.add(new PredictMaterialOrderInput());
        orders.add(new PredictMaterialOrderInput());
        request.setOrders(orders);

        model.addAttribute("request", request);
        return "/predict/materialPredictForm";
    }

    // 재료 발주 예측 flask로 전송후 반환값 출력
    @PostMapping("/predict")
    public String predict(@Valid @ModelAttribute("request") MaterialPredictRequest request,
                          BindingResult bindingResult,
                          Model model) {

        if (bindingResult.hasErrors()) {
            return "/predict/materialPredictForm"; // 입력폼 다시 보여줌
        }

        MaterialPredictResult result = materialPredictService.getPrediction(request);

        // 날짜 라벨과 수량 데이터를 구성
        List<String> chartLabels = new ArrayList<>();
        List<Double> chartData = new ArrayList<>();

        // 기존 입력값 찾아 차트용 라벨, 데이터로 설정
        for (PredictMaterialOrderInput order : request.getOrders()) {
            chartLabels.add(order.getDate().toString());
            chartData.add(order.getQty().doubleValue());
        }

        // 예측값 추가
        chartLabels.add(result.getPredictDate().toString());
        chartData.add(result.getPrediction());

        // 모델에 데이터 전달
        model.addAttribute("prediction", result.getPrediction());
        model.addAttribute("predictedDate", result.getPredictDate());
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartData", chartData);
        model.addAttribute("selectedType", request.getPredictType());  // 선택된 타입

        // JS에서 사용할 원본 order JSON 문자열 전달
        try {
            ObjectMapper mapper = new ObjectMapper();
            String ordersJson = mapper.writeValueAsString(request.getOrders());

            model.addAttribute("ordersJson", ordersJson);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return "/predict/materialPredictResult";
    }


    @PostMapping("/predict/api")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> predictApi(@RequestBody MaterialPredictRequest request) {

        MaterialPredictResult result = materialPredictService.getPrediction(request);

        List<String> chartLabels = new ArrayList<>();
        List<Double> chartData = new ArrayList<>();

        for (PredictMaterialOrderInput order : request.getOrders()) {
            chartLabels.add(order.getDate().toString());
            chartData.add(order.getQty().doubleValue());
        }

        chartLabels.add(result.getPredictDate().toString());
        chartData.add(result.getPrediction());

        Map<String, Object> response = new HashMap<>();
        response.put("prediction", result.getPrediction());
        response.put("predictedDate", result.getPredictDate().toString());
        response.put("chartLabels", chartLabels);
        response.put("chartData", chartData);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
