package io.github.ladium1.erp.member.internal.service;

import io.github.ladium1.erp.department.api.DepartmentApi;
import io.github.ladium1.erp.department.api.dto.DepartmentInfo;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.member.internal.dto.MemberProfileResponse;
import io.github.ladium1.erp.member.internal.entity.Member;
import io.github.ladium1.erp.member.internal.exception.MemberErrorCode;
import io.github.ladium1.erp.member.internal.mapper.MemberMapper;
import io.github.ladium1.erp.member.internal.repository.MemberRepository;
import io.github.ladium1.erp.position.api.PositionApi;
import io.github.ladium1.erp.position.api.dto.PositionInfo;
import io.github.ladium1.erp.role.api.RoleApi;
import io.github.ladium1.erp.role.api.dto.MenuPermission;
import io.github.ladium1.erp.role.api.dto.RoleInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final RoleApi roleApi;
    private final DepartmentApi departmentApi;
    private final PositionApi positionApi;

    public MemberProfileResponse getMyInfo(String loginId) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        RoleInfo roleInfo = roleApi.getById(member.getRoleId());

        List<MenuPermission> menuPermissions = roleApi.getMenuPermissionsByRoleId(member.getRoleId());

        DepartmentInfo departmentInfo = Optional.ofNullable(member.getDepartmentId())
                .map(departmentApi::getById)
                .orElse(null);

        PositionInfo positionInfo = Optional.ofNullable(member.getPositionId())
                .map(positionApi::getById)
                .orElse(null);

        return memberMapper.toProfileResponse(
                member,
                departmentInfo,
                positionInfo,
                roleInfo,
                menuPermissions
        );
    }

}
