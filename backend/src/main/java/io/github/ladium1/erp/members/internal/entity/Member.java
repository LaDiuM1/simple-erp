package io.github.ladium1.erp.members.internal.entity;

import io.github.ladium1.erp.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 로그인 id
    @Column(nullable = false, unique = true)
    private String loginId;
    // 비밀번호 (BCrypt)
    @Column(nullable = false)
    private String password;
    // 직원명
    @Column(nullable = false)
    private String name;
    // 거주지 주소
    @Embedded
    private Address address;
    // 입사일
    private LocalDate joinDate;
    // 이메일 주소
    private String email;
    // 연락처
    private String phone;
    // 재직 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;
    // 역할(시스템 권한)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;
    // 부서
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
    // 직책
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    @Builder
    Member(String loginId,
           String password,
           String name,
           String email,
           String phone,
           Address address,
           LocalDate joinDate,
           MemberStatus status,
           Role role,
           Department department,
           Position position) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.joinDate = joinDate;
        this.status = status;
        this.role = role;
        this.department = department;
        this.position = position;
    }
}