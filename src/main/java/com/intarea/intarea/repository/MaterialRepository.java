package com.intarea.intarea.repository;

import com.intarea.intarea.domain.Material;
import com.intarea.intarea.domain.MaterialName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    //재료이름으로 찾기
    //입고 순서로 정렬하여 리스트를 가져온다. 재료의 동일한 이름은 여러개일 수 있다
    List<Material> findAllByMaterialName(MaterialName materialName);

    Material findTopByOrderByDateTimeDesc();

    //재료가 0개 남았을 때 리스트에서 없어지게 처리하기 위함
    Page<Material> findAllByQuantityGreaterThan(int quantity, Pageable pageable);

    List<Material> findByUsersId(Long userId);
}
