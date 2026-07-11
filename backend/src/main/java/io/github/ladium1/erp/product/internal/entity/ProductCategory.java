package io.github.ladium1.erp.product.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 제품 카테고리 — 카탈로그 개편 때마다 분류가 바뀌므로 enum 이 아닌 사용자 관리 데이터.
 * 별도 메뉴 없이 제품 모델 관리 (PRODUCTS) 의 서브 기능으로 CRUD / 순서 변경.
 */
@Entity
@Getter
@Table(name = "product_categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true,
            comment = "카테고리명")
    private String name;

    @Column(nullable = false,
            comment = "노출 순서 — 카탈로그 분류 순서와 동일하게 유지")
    private int sortOrder;

    @Builder
    ProductCategory(String name, int sortOrder) {
        this.name = name;
        this.sortOrder = sortOrder;
    }

    public void update(String name) {
        this.name = name;
    }

    public void changeSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
