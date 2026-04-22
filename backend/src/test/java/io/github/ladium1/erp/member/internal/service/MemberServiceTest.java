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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock private MemberRepository memberRepository;
    @Mock private MemberMapper memberMapper;
    @Mock private RoleApi roleApi;
    @Mock private DepartmentApi departmentApi;
    @Mock private PositionApi positionApi;

    private final String TEST_ID = "testUser";

    @Test
    @DisplayName("내 정보 조회 성공")
    void get_my_info_success() {
        // given
        String testName = "테스트이름";

        Long testRoleId = 10L;
        String testRoleCode = "TEST_ROLE";
        String testRoleName = "테스트권한명";

        Long testDeptId = 20L;
        String testDepartment = "테스트부서";

        Long testPosId = 30L;
        String testPosition = "테스트직책";

        Long testMenuId = 1L;
        String testMenuCode = "TEST_MENU";

        Member mockMember = Member.builder()
                .loginId(TEST_ID)
                .name(testName)
                .roleId(testRoleId)
                .departmentId(testDeptId)
                .positionId(testPosId)
                .build();

        RoleInfo mockRoleInfo = RoleInfo.builder()
                .id(testRoleId)
                .code(testRoleCode)
                .name(testRoleName)
                .build();

        MenuPermission mockPermission = MenuPermission.builder()
                .menuId(testMenuId)
                .menuCode(testMenuCode)
                .canRead(true)
                .canWrite(true)
                .build();
        List<MenuPermission> mockPermissions = List.of(mockPermission);

        DepartmentInfo mockDeptInfo = DepartmentInfo.builder()
                .id(testDeptId)
                .name(testDepartment)
                .build();

        PositionInfo mockPosInfo = PositionInfo.builder()
                .id(testPosId)
                .name(testPosition)
                .build();

        MemberProfileResponse expectedResponse = MemberProfileResponse.builder()
                .loginId(TEST_ID)
                .name(testName)
                .departmentName(testDepartment)
                .positionName(testPosition)
                .roleName(testRoleName)
                .roleCode(testRoleCode)
                .menuPermissions(mockPermissions)
                .build();

        given(memberRepository.findByLoginId(TEST_ID)).willReturn(Optional.of(mockMember));
        given(roleApi.getById(testRoleId)).willReturn(mockRoleInfo);
        given(roleApi.getMenuPermissionsByRoleId(testRoleId)).willReturn(mockPermissions);
        given(departmentApi.getById(testDeptId)).willReturn(mockDeptInfo);
        given(positionApi.getById(testPosId)).willReturn(mockPosInfo);
        given(memberMapper.toProfileResponse(mockMember, mockDeptInfo, mockPosInfo, mockRoleInfo, mockPermissions))
                .willReturn(expectedResponse);

        // when
        MemberProfileResponse actualResponse = memberService.getMyInfo(TEST_ID);

        // then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.loginId()).isEqualTo(TEST_ID);
        assertThat(actualResponse.name()).isEqualTo(testName);
        assertThat(actualResponse.departmentName()).isEqualTo(testDepartment);
        assertThat(actualResponse.roleCode()).isEqualTo(testRoleCode);

        verify(memberMapper).toProfileResponse(mockMember, mockDeptInfo, mockPosInfo, mockRoleInfo, mockPermissions);
    }

    @Test
    @DisplayName("직원 정보 없음")
    void get_my_info_fail_member_not_found() {
        // given
        given(memberRepository.findByLoginId(TEST_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.getMyInfo(TEST_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
    }
}