package com.intarea.intarea;

import com.intarea.intarea.domain.*;
import com.intarea.intarea.service.MaterialService;
import com.intarea.intarea.service.NoticeService;
import com.intarea.intarea.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

// yml을 create로 실행시 더미값을 넣기위한 클래스
// @Component 숨김처리로 작동 On/Off 가능.

// @Component
@AllArgsConstructor
public class BasicDataLoader implements CommandLineRunner {

    private final UserService userService;
    private final MaterialService materialService;
    private final NoticeService noticeService;

    @Override
    public void run(String... args) throws Exception {
        // String... args = 가변인자, 여러 개의 String값을 받을 수 있는 배열같은 기능.
        // ','로 여러 개 값 전달가능.

        // 기본 계졍 입력
        Users user1 = Users.builder()
                .loginId("test")
                .password("1111")
                .name("체험계정")
                .phone("010-1234-1111")
                .email("test@ezen.com")
                .grade(Grade.GUEST)
                .build();
        userService.save(user1);

        // 관리자 계졍 입력
        Users admin1 = Users.builder()
                .loginId("admin")
                .password("1253")
                .name("관리자 계정")
                .phone("010-1234-1253")
                .email("admin@ezen.com")
                .grade(Grade.MANAGER)
                .build();
        userService.save(admin1);

        // 직원 계졍 입력
        Users employee1 = Users.builder()
                .loginId("user1")
                .password("1112")
                .name("직원A")
                .phone("010-2025-0611")
                .email("user1@ezen.com")
                .grade(Grade.EMPLOYEE)
                .build();
        userService.save(employee1);

        // 기초품목 리스트
        List<MaterialName> materialNameList = List.of(
                MaterialName.PP,
                MaterialName.PEI,
                MaterialName.PMMA,
                MaterialName.PIGMENT,
                MaterialName.CACO3,
                MaterialName.UVS
        );

        // 기초품목값 입력 -> 리스트 활용
        List<Material> materialList = materialNameList.stream().map(materialName -> Material.builder()
                // stream() : list, set 등의 값을 함수형 방식으로 처리가능 =for문 대용.
                // map() : 스트림의 각요소를 다른 값(여기서는 객체)로 변환.
                .materialName(materialName)
                .quantity(100)
                .users(user1)
                .company(Company.EZEN)
                .dateTime(LocalDateTime.now())
                .build()).toList(); // 리스트에 개별 입력값 저장

        materialList.forEach(materialService::saveMaterial); // for문(+람다식)으로 리스트안의 개별값 서버에 저장.

        // 기본 공지사항 입력
        Notices firstNotice = Notices.builder()
                .title("첫 글입니다.")
                .content("첫 글입니다. 공지사항은 관리자만 등록할 수 있습니다.")
                .author(admin1)
                .createdAt(LocalDateTime.now())
                .build();

        noticeService.saveNotice(firstNotice);

    }

}
