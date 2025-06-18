package com.intarea.intarea.controller;

import com.intarea.intarea.domain.*;
import com.intarea.intarea.dto.OrderForm;
import com.intarea.intarea.dto.ProcessDataDto;
import com.intarea.intarea.service.MaterialService;
import com.intarea.intarea.service.OrderService;
import com.intarea.intarea.service.ProcessDataService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final MaterialService materialService;
    private final ProcessDataService processDataService;

    //제품 주문 폼 열기
    @GetMapping
    private String order(Model model, HttpSession session) {

        // 로그인 여부 확인
        Users loginMember = (Users) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if(loginMember==null) {
            return "redirect:/";
        }

        //현재 재료 총 재고량 확인
        Map<MaterialName, Long> materialQuantities = materialService.getMaterialQuantities();
        log.info("총 재료 : {}", materialQuantities);

        // 모델로 넘기기
        model.addAttribute("orderForm", new OrderForm());

        // ?
        Boolean clicked = (Boolean) session.getAttribute("buttonClicked");
        model.addAttribute("disabled", clicked != null && clicked);

        // 제품명 + 주문처명 리스트화
        List<ProductName> productNameList = Arrays.asList(ProductName.values());
        List<OrderCompany> orderCompanyList = Arrays.asList(OrderCompany.values());

        model.addAttribute("productName", productNameList);
        model.addAttribute("orderCompany", orderCompanyList);

        // 장바구니 데이터 전달
        List<OrderForm> cart = (List<OrderForm>) session.getAttribute("cart");
        model.addAttribute("cart", cart != null ? cart : new ArrayList<>());


        return "order/orderForm";

    }

    //제품 주문 발주
    @PostMapping
    public String orderPost(@Valid OrderForm orderForm, BindingResult bindingResult, @ModelAttribute("loginMember") Users loginMember, Model model) {

        List<ProductName> productNameList = Arrays.asList(ProductName.values());

        // 누락 검증
        if(bindingResult.hasErrors()) {
            model.addAttribute("productName", productNameList);
            return "order/orderForm";
        }

        // 선택된 제품의 이미지 URL 추가
        if (orderForm.getProductName() != null) {
            String selectedImageUrl = getImageUrlForProduct(orderForm.getProductName());
            model.addAttribute("selectedProductImageUrl", selectedImageUrl);
        }

        // 주문 실행(재료 부족시 에러 처리 포함)
        try {
            Orders orders = Orders.builder()
                    .productName(orderForm.getProductName())
                    .orderQuantity(orderForm.getOrderQuantity())
                    .orderDate(LocalDateTime.now())
                    .processDate(orderForm.getProcessDate())
                    .users(loginMember)
                    .orderState(OrderState.PENDING)
                    .orderCompany(orderForm.getOrderCompany())
                    .build();

            orderService.save(orders);
        } catch (IllegalStateException e) {
            bindingResult.reject("globalError", e.getMessage());
            model.addAttribute("productName", productNameList);

            //에러 메세지가 떠도 그 사진에 멈춰있게 하기 위해
            if (orderForm.getProductName() != null) {
                String selectedImageUrl = getImageUrlForProduct(orderForm.getProductName());
                model.addAttribute("selectedProductImageUrl", selectedImageUrl);
            }

            return "order/orderForm";
        }
        // 주문 정상 완료 후 리스트로 이동
        return "redirect:/order/list";
    }


    // 장바구니 담기
    @PostMapping("/cart/add")
    public String addToCart(@ModelAttribute OrderForm orderForm, HttpSession session, RedirectAttributes redirectAttributes) {

        if (orderForm.getOrderCompany() == null || orderForm.getProductName() == null || orderForm.getProcessDate() == null) {
            redirectAttributes.addFlashAttribute("materialError", "입력값이 부족합니다.");
            return "redirect:/order";
        }

        List<OrderForm> cart = (List<OrderForm>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
        }

        boolean merged = false;

        for (OrderForm existing : cart) {
            // 제품명, 공정예정일, 주문처가 모두 같으면 수량만 증가
            if (existing.getProductName() == orderForm.getProductName()
                    && existing.getProcessDate().equals(orderForm.getProcessDate())
                    && existing.getOrderCompany() == orderForm.getOrderCompany()) {

                int updatedQuantity = existing.getOrderQuantity() + orderForm.getOrderQuantity();
                existing.setOrderQuantity(updatedQuantity);
                merged = true;
                break;
            }
        }

        if (!merged) {
            cart.add(orderForm);
        }

        session.setAttribute("cart", cart);
        return "redirect:/order#shoppingCart";
    }

    // 카트 내역 삭제
    @PostMapping("/cart/delete")
    public String deleteFromCart(@RequestParam("index") int index, HttpSession session) {
        List<OrderForm> cart = (List<OrderForm>) session.getAttribute("cart");
        if (cart != null && index >= 0 && index < cart.size()) {
            cart.remove(index);
            session.setAttribute("cart", cart);
        }
        return "redirect:/order#shoppingCart";
    }


    // 최종 주문 전송
    @PostMapping("/cart/checkout")
    public String checkoutCart(HttpSession session,
                               @SessionAttribute(SessionConst.LOGIN_MEMBER) Users loginMember,
                               RedirectAttributes redirectAttributes) {

        List<OrderForm> cart = (List<OrderForm>) session.getAttribute("cart");

        if (cart != null && !cart.isEmpty()) {
            // 성공/실패 주문 저장 List
            List<OrderForm> failOrders = new ArrayList<>();
            List<OrderForm> successOrders = new ArrayList<>();

            for (OrderForm orderForm : cart) {
                try {
                    Orders orders = Orders.builder()
                            .productName(orderForm.getProductName())
                            .orderQuantity(orderForm.getOrderQuantity())
                            .orderDate(LocalDateTime.now())
                            .processDate(orderForm.getProcessDate())
                            .users(loginMember)
                            .orderState(OrderState.PENDING)
                            .orderCompany(orderForm.getOrderCompany())
                            .build();
                    orderService.save(orders);
                    successOrders.add(orderForm);

                } catch (IllegalStateException e) {
                    failOrders.add(orderForm);
                    log.error("주문 처리 실패 : {} {}개, 수량을 다시 확인하세요.", orderForm.getProductName().getLabel(), orderForm.getOrderQuantity());
                }

                if (!failOrders.isEmpty()) {
                    session.setAttribute("cart", failOrders);
                    redirectAttributes.addFlashAttribute("materialError", "일부 주문이 재료 부족으로 실패했습니다.");
                    return "redirect:/order";
                } else {
                    session.removeAttribute("cart"); // 전부 성공했으면 장바구니 비움
                }
            }
        }
        return "redirect:/";
    }

    //제품 리스트 열기
    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "15") int size,
                       Model model, HttpSession session) {

        // 로그인 여부 검사
        Users loginMember = (Users) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if(loginMember==null) {
            return "redirect:/";
        }

        // 1. Page 나눔 처리 + 전체 제품 리스트
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").ascending());
        Page<Orders> ordersPage = orderService.findAllOrders(pageable);

        model.addAttribute("orderList", ordersPage.getContent());
        model.addAttribute("orderPage", ordersPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordersPage.getTotalPages());


        // 2. 개별 제품 총량 확인 및 알림 기능용 Map
        // 개별 제품들의 총합 불러오기
        Map<ProductName, Long> productNameLongMap = orderService.getOrdersQuantities();

        // Model로 Map 넘기기
        model.addAttribute("productNameLongMap", productNameLongMap);


        // 3. 주문별 공정 결과 수량 맵 생성
        Map<Long, Long> okMap = new HashMap<>();
        Map<Long, Long> ngMap = new HashMap<>();

        for (Orders order : ordersPage.getContent()) {
            ProcessDataDto dto;
            if (order.getProductName().name().equals("LAMP_COVER")) {
                dto = processDataService.getProcessBData(order.getId());
            } else {
                dto = processDataService.getProcessAData(order.getId());
            }
            okMap.put(order.getId(), dto.getOkProduct());
            ngMap.put(order.getId(), dto.getNgProduct());
        }

        model.addAttribute("okMap", okMap);
        model.addAttribute("ngMap", ngMap);


        return "order/orderList";
    }

    // ✅ 공정 예정일 기반 그룹화 API
    @GetMapping("/groupedByCompany")
    @ResponseBody
    public Map<String, List<Map<String, Object>>> getGroupedOrdersByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Orders> orders = orderService.findOrdersByProcessDate(date);

        return orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getOrderCompany().getLabel(),
                        Collectors.mapping(o -> Map.of(
                                "orderId", o.getId(),
                                "productName", o.getProductName().getLabel(),
                                "orderQuantity", o.getOrderQuantity(),
                                "orderDate", o.getOrderDate().toString(),
                                "processDate", o.getProcessDate().toString(),
                                "managerName", o.getUsers().getName(),
                                "orderState", o.getOrderState().name()
                        ), Collectors.toList())
                ));
    }


    // 주문 정보 수정 열기
    @GetMapping("/update/{orderId}")
    public String update(@PathVariable("orderId") Long orderId, Model model) {

        Orders orders = orderService.findOneOrder(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 제품을 찾을 수 없습니다."));

        OrderForm orderForm = OrderForm.builder()
                .id(orders.getId())
                .productName(orders.getProductName())
                .orderQuantity(orders.getOrderQuantity())
                .build();

        model.addAttribute("orderForm", orderForm);

        return "order/updateOrder";
    }

    // 주문 정보 수정 적용
    @PostMapping("/update/{orderId}")
    public String updatePost(@PathVariable("orderId") Long orderId,
                             @Valid @ModelAttribute("orderForm") OrderForm orderForm,
                             BindingResult bindingResult,
                             Model model) {

        if (bindingResult.hasErrors()) {
            Orders orders = orderService.findOneOrder(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 제품을 찾을 수 없습니다."));

            orderForm.setId(orders.getId());
            orderForm.setProductName(orders.getProductName());
            orderForm.setOrderQuantity(orders.getOrderQuantity());
            model.addAttribute("orderForm", orderForm);
            return "order/updateOrder";
        }

        Orders orders = orderService.findOneOrder(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 제품을 찾을 수 없습니다."));

        //실질적 업데이트
        orders.update(
                String.valueOf(orderForm.getProductName()),
                orderForm.getOrderQuantity(),
                orderForm.getOrderCompany(),
                orderForm.getProcessDate()
        );

        orderService.save(orders);  // 저장

        return "redirect:/order/list";
    }



    //에러 메세지가 떠도 그 사진에 멈춰있게 하기 위해
    private String getImageUrlForProduct(ProductName productName) {
        switch (productName) {
            case FLOWER_POT: return "/image/fPot.jpg";
            case BASKET: return "/image/basket.gif";
            case VASE: return "/image/vase.jpg";
            case STORAGE_BOX: return "/image/sBox.jpg";
            case FRAME: return "/image/frame.jpg";
            case LAMP_COVER: return "/image/lCover.jpg";
            default: return "/image/productHere.png";
        }
    }

}