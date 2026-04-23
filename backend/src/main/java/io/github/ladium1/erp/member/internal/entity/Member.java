package io.github.ladium1.erp.member.internal.entity;

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

    @Column(name = "role_id",
            comment = "역할 식별자")
    private Long roleId;

    @Column(name = "department_id",
            comment = "부서 식별자")
    private Long departmentId;

    @Column(name = "position_id",
            comment = "직책 식별자")
    private Long positionId;

    @Builder
    Member(String loginId,
           String password,
           String name,
           String email,
           String phone,
           Address address,
           LocalDate joinDate,
           MemberStatus status,
           Long roleId,
           Long departmentId,
           Long positionId) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.joinDate = joinDate;
        this.status = status;
        this.roleId = roleId;
        this.departmentId = departmentId;
        this.positionId = positionId;
    }

    public void update(String name,
                       String email,
                       String phone,
                       Address address,
                       LocalDate joinDate,
                       MemberStatus status,
                       Long roleId,
                       Long departmentId,
                       Long positionId) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.joinDate = joinDate;
        this.status = status;
        this.roleId = roleId;
        this.departmentId = departmentId;
        this.positionId = positionId;
    }
}