package io.github.ladium1.erp.product.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 제품 모델 마스터. 출력 (kW·ton) 과 옵션 (BEVEL, FMC 등) 은 같은 모델에서도
 * 계약마다 달라지는 사양이라 여기가 아닌 계약 필드로 관리한다.
 */
@Entity
@Getter
@Table(name = "products",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_products_supplier_model",
                columnNames = {"supplier_id", "model_name"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false,
            comment = "제품 카테고리 외래키")
    private ProductCategory category;

    @Column(name = "model_name", nullable = false,
            comment = "모델명")
    private String modelName;

    @Column(name = "supplier_id", nullable = false,
            comment = "공급사 참조 (supplier 모듈)")
    private Long supplierId;

    @Column(columnDefinition = "TEXT", comment = "비고")
    private String note;

    @Column(nullable = false,
            comment = "사용 여부 — 단종 / 취급 중단 모델 숨김용")
    private boolean active;

    @Builder
    Product(ProductCategory category, String modelName, Long supplierId, String note, boolean active) {
        this.category = category;
        this.modelName = modelName;
        this.supplierId = supplierId;
        this.note = note;
        this.active = active;
    }

    public void update(ProductCategory category, String modelName, Long supplierId, String note, boolean active) {
        this.category = category;
        this.modelName = modelName;
        this.supplierId = supplierId;
        this.note = note;
        this.active = active;
    }
}
