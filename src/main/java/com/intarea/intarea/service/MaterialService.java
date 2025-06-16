package com.intarea.intarea.service;

import com.intarea.intarea.domain.Material;
import com.intarea.intarea.domain.MaterialName;
import com.intarea.intarea.domain.ProductName;
import com.intarea.intarea.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MaterialService {

    private final MaterialRepository materialRepository;

    //제료 저장
    @Transactional
    public void saveMaterial(Material material) {
        materialRepository.save(material);
    }

    // 재료 하나 조회
    public Optional<Material> findOne(Long materialId) {
        return materialRepository.findById(materialId);
    }


    //재료 이름으로 조회 (repository의 별도 지정 메서드 이용)
    public List<Material> findMaterialName(MaterialName materialName) {
        return materialRepository.findAllByMaterialName(materialName);
    }

    // 재료 삭제
    @Transactional
    public void deleteMaterial(Long materialId) {
        materialRepository.deleteById(materialId);
    }


    //개별 재료들의 총합수 확인 메서드 (Map<개별 재료명, 총합수> 반환)
    public Map<MaterialName, Long> getMaterialQuantities() {
        //DB에서 재료를 가져온다
        List<Material> materials = materialRepository.findAll();
        // 재료 이름별로 그룹화하고 총합 계산
        Map<MaterialName, Long> materialNameLongMap = materials.stream().collect(Collectors.groupingBy(
                Material::getMaterialName, // 재료 이름 기준으로 그룹화
                Collectors.summingLong(Material::getQuantity) // 각 그룹의 총합계산
        ));
        return materialNameLongMap;
    }

    // 각 제품의 대응 소진 재료 Map
    private static final Map<ProductName, List<MaterialName>> PRODUCT_NAME_LIST_MAP = Map.of(
            ProductName.FLOWER_POT, List.of(MaterialName.PP, MaterialName.CACO3, MaterialName.PIGMENT),
            ProductName.BASKET, List.of(MaterialName.PP, MaterialName.CACO3, MaterialName.PIGMENT),
            ProductName.VASE, List.of(MaterialName.PMMA, MaterialName.UVS, MaterialName.PIGMENT),
            ProductName.STORAGE_BOX, List.of(MaterialName.PP, MaterialName.CACO3, MaterialName.PIGMENT),
            ProductName.FRAME, List.of(MaterialName.PMMA, MaterialName.UVS),
            ProductName.LAMP_COVER, List.of(MaterialName.PEI, MaterialName.UVS, MaterialName.PIGMENT)
    );

    // 제품 요구 재료량 확인
    public Map<MaterialName, Long> getRequiredMaterialStock(ProductName productName) {
        // 전체 원재료 수량 가져오기
        Map<MaterialName, Long> materialQuantities = getMaterialQuantities();
        // 제품별 필요한 원재료 리스트 가져오기
        List<MaterialName> requiredMaterials = PRODUCT_NAME_LIST_MAP.getOrDefault(productName, List.of());
        log.info("원재료 리스트 {}", requiredMaterials);

        //필요한 원재료 수량만 추출
        Map<MaterialName, Long> materialNameLongMap = requiredMaterials.stream().collect(Collectors.toMap(
                materialName -> materialName,
                materialName -> materialQuantities.getOrDefault(materialName, 0L)
        ));
        return materialNameLongMap;
    }


    // 페이징
    //수량이 0보다 큰 재료만 가져오도록 설정
    public Page<Material> findAllMaterials(Pageable pageable) {
        return materialRepository.findAllByQuantityGreaterThan(0, pageable);
    }
}
