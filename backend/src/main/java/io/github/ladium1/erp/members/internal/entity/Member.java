package io.github.ladium1.erp.members.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
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

    @Column(nullable = false, unique = true,
            comment = "로그인 id")
    private String loginId;

    @Column(nullable = false,
            comment = "비밀번호")
    private String password;

    @Column(nullable = false,
            comment = "직원명")
    private String name;

    @Embedded
    @Column(comment = "거주지 주소")
    private Address address;

    @Column(comment = "입사일")
    private LocalDate joinDate;

    @Column(comment = "이메일 주소")
    private String email;

    @Column(comment = "연락처")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,
            comment = "재직 상태")
    private MemberStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id",
            comment = "역할 외래키")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id",
            comment = "부서 외래키")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id",
            comment = "직책 외래키")
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