package io.github.ladium1.erp.menus.internal.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "menus")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 메뉴명(도메인명)
    @Column(nullable = false)
    private String name;
    // 접근 관리 주소, 최상위 카테고리 시 null
    @Column(nullable = true)
    private String url;
    // 메뉴 순서(동일 레벨 기준)
    private int sortOrder;
    // 계층 레벨, 자동 계산
    private int level;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Menu parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Menu> children = new ArrayList<>();

    // 계층 레벨 자동 계산
    @PrePersist
    @PreUpdate
    private void calculateLevel() {
        // 부모가 없으면 1레벨, 있으면 부모 레벨 + 1
        this.level = (this.parent == null) ? 1 : this.parent.getLevel() + 1;
    }

}