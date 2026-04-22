package io.github.ladium1.erp.member.internal.web;

import io.github.ladium1.erp.member.internal.dto.MemberProfileResponse;
import io.github.ladium1.erp.member.internal.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Test
    @DisplayName("내 정보 조회 성공")
    void get_my_info_success() throws Exception {
        // given
        String testId = "테스트아이디";
        String testName = "테스트이름";
        String testDepartment = "테스트부서";
        String testPosition = "테스트직책";
        String testRoleName = "테스트권한명";
        String testRoleCode = "TEST_ROLE";

        MemberProfileResponse mockResponse = MemberProfileResponse.builder()
                .id(1L)
                .loginId(testId)
                .name(testName)
                .departmentName(testDepartment)
                .positionName(testPosition)
                .roleName(testRoleName)
                .roleCode(testRoleCode)
                .menuPermissions(List.of())
                .build();

        given(memberService.getMyInfo(any())).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/members/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.loginId").value(testId))
                .andExpect(jsonPath("$.data.name").value(testName))
                .andExpect(jsonPath("$.data.departmentName").value(testDepartment))
                .andExpect(jsonPath("$.data.roleCode").value(testRoleCode));
    }
}