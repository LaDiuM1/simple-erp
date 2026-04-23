package io.github.ladium1.erp.member.internal.service;

import io.github.ladium1.erp.department.api.DepartmentApi;
import io.github.ladium1.erp.department.api.dto.DepartmentInfo;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.member.api.MemberApi;
import io.github.ladium1.erp.member.internal.dto.MemberCreateRequest;
import io.github.ladium1.erp.member.internal.dto.MemberDetailResponse;
import io.github.ladium1.erp.member.internal.dto.MemberProfileResponse;
import io.github.ladium1.erp.member.internal.dto.MemberSearchCondition;
import io.github.ladium1.erp.member.internal.dto.MemberSummaryResponse;
import io.github.ladium1.erp.member.internal.dto.MemberUpdateRequest;
import io.github.ladium1.erp.member.internal.entity.Address;
import io.github.ladium1.erp.member.internal.entity.Member;
import io.github.ladium1.erp.member.internal.excel.MemberExcelExporter;
import io.github.ladium1.erp.member.internal.exception.MemberErrorCode;
import io.github.ladium1.erp.member.internal.mapper.MemberMapper;
import io.github.ladium1.erp.member.internal.repository.MemberRepository;
import io.github.ladium1.erp.position.api.PositionApi;
import io.github.ladium1.erp.position.api.dto.PositionInfo;
import io.github.ladium1.erp.role.api.RoleApi;
import io.github.ladium1.erp.role.api.dto.MenuPermission;
import io.github.ladium1.erp.role.api.dto.RoleInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService implements MemberApi {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final RoleApi roleApi;
    private final DepartmentApi departmentApi;
    private final PositionApi positionApi;
    private final PasswordEncoder passwordEncoder;
    private final MemberExcelExporter memberExcelExporter;

    @Override
    public Long getRoleIdByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId)
                .map(Member::getRoleId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

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

        return memberMapper.toProfileResponse(member, departmentInfo, positionInfo, roleInfo, menuPermissions);
    }

    public PageResponse<MemberSummaryResponse> search(MemberSearchCondition condition, Pageable pageable) {
        Page<Member> page = memberRepository.search(condition, pageable);
        ReferenceCache refs = loadReferences(page.getContent());
        return PageResponse.of(page.map(member -> toSummary(member, refs)));
    }

    public byte[] exportExcel(MemberSearchCondition condition, Sort sort) {
        List<Member> members = memberRepository.searchAll(condition, sort);
        ReferenceCache refs = loadReferences(members);
        List<MemberSummaryResponse> rows = members.stream()
                .map(member -> toSummary(member, refs))
                .toList();
        return memberExcelExporter.export(rows);
    }

    private MemberSummaryResponse toSummary(Member member, ReferenceCache refs) {
        return memberMapper.toSummaryResponse(
                member,
                refs.departmentName(member.getDepartmentId()),
                refs.positionName(member.getPositionId()),
                refs.roleName(member.getRoleId())
        );
    }

    public MemberDetailResponse getDetail(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        DepartmentInfo departmentInfo = Optional.ofNullable(member.getDepartmentId())
                .map(departmentApi::getById)
                .orElse(null);
        PositionInfo positionInfo = Optional.ofNullable(member.getPositionId())
                .map(positionApi::getById)
                .orElse(null);
        RoleInfo roleInfo = roleApi.getById(member.getRoleId());

        return memberMapper.toDetailResponse(member, departmentInfo, positionInfo, roleInfo);
    }

    @Transactional
    public Long create(MemberCreateRequest request) {
        if (memberRepository.existsByLoginId(request.loginId())) {
            throw new BusinessException(MemberErrorCode.DUPLICATE_LOGIN_ID);
        }
        validateReferences(request.roleId(), request.departmentId(), request.positionId());

        Member member = Member.builder()
                .loginId(request.loginId())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .address(toAddress(request.zipCode(), request.roadAddress(), request.detailAddress()))
                .joinDate(request.joinDate())
                .status(request.status())
                .roleId(request.roleId())
                .departmentId(request.departmentId())
                .positionId(request.positionId())
                .build();

        return memberRepository.save(member).getId();
    }

    @Transactional
    public void update(Long id, MemberUpdateRequest request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
        validateReferences(request.roleId(), request.departmentId(), request.positionId());

        member.update(
                request.name(),
                request.email(),
                request.phone(),
                toAddress(request.zipCode(), request.roadAddress(), request.detailAddress()),
                request.joinDate(),
                request.status(),
                request.roleId(),
                request.departmentId(),
                request.positionId()
        );
    }

    @Transactional
    public void delete(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
        }
        memberRepository.deleteById(id);
    }

    private Address toAddress(String zipCode, String roadAddress, String detailAddress) {
        if (zipCode == null && roadAddress == null && detailAddress == null) {
            return null;
        }
        return Address.builder()
                .zipCode(zipCode)
                .roadAddress(roadAddress)
                .detailAddress(detailAddress)
                .build();
    }

    private void validateReferences(Long roleId, Long departmentId, Long positionId) {
        roleApi.getById(roleId);
        if (departmentId != null) {
            departmentApi.getById(departmentId);
        }
        if (positionId != null) {
            positionApi.getById(positionId);
        }
    }

    private ReferenceCache loadReferences(List<Member> members) {
        List<Long> deptIds = distinctIds(members.stream().map(Member::getDepartmentId));
        List<Long> posIds = distinctIds(members.stream().map(Member::getPositionId));
        List<Long> roleIds = distinctIds(members.stream().map(Member::getRoleId));

        Map<Long, String> deptNames = departmentApi.findByIds(deptIds).stream()
                .collect(toMap(DepartmentInfo::id, DepartmentInfo::name));
        Map<Long, String> posNames = positionApi.findByIds(posIds).stream()
                .collect(toMap(PositionInfo::id, PositionInfo::name));
        Map<Long, String> roleNames = roleApi.findByIds(roleIds).stream()
                .collect(toMap(RoleInfo::id, RoleInfo::name));

        return new ReferenceCache(deptNames, posNames, roleNames);
    }

    private static List<Long> distinctIds(Stream<Long> ids) {
        return ids.filter(java.util.Objects::nonNull).distinct().toList();
    }

    private record ReferenceCache(Map<Long, String> departmentNames,
                                  Map<Long, String> positionNames,
                                  Map<Long, String> roleNames) {

        String departmentName(Long id) {
            return id == null ? null : departmentNames.get(id);
        }

        String positionName(Long id) {
            return id == null ? null : positionNames.get(id);
        }

        String roleName(Long id) {
            return id == null ? null : roleNames.get(id);
        }
    }
}
