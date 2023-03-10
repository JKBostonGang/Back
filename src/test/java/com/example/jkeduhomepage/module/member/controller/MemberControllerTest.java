package com.example.jkeduhomepage.module.member.controller;

import com.example.jkeduhomepage.module.common.enums.Role;
import com.example.jkeduhomepage.module.common.enums.Status;
import com.example.jkeduhomepage.module.member.dto.MemberRequestDTO;
import com.example.jkeduhomepage.module.member.dto.MemberUpdateDTO;
import com.example.jkeduhomepage.module.member.entity.Member;
import com.example.jkeduhomepage.module.member.entity.MemberPhoneAuth;
import com.example.jkeduhomepage.module.member.repository.MemberRepository;
import com.example.jkeduhomepage.module.member.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("dev")
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
class MemberControllerTest {
    private final String URL = "/member";

    @Autowired
    private MemberService memberService;

    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static Member SECURITY_ADMIN_MEMBER;

    private static Member SECURITY_DELETE_MEMBER;

    private static Member SECURITY_MEMBER;

    private static Member RED_MEMBER;


    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext,
                      RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build();
    }


    @BeforeAll
    public void all() throws CoolsmsException {
        saveMember();
    }

    private void saveMember() throws CoolsmsException {
        MemberPhoneAuth memberPhoneAuth=memberService.certifiedPhone("memberPhone2");
        memberService.certifiedPhoneCheck("memberPhone2",memberPhoneAuth.getSmscode());

        MemberRequestDTO memberRequestDTO = new MemberRequestDTO();
        memberRequestDTO.setLoginId("memberTest");
        memberRequestDTO.setPassword("123456");
        memberRequestDTO.setEmail("aa@aa");
        memberRequestDTO.setName("momo");
        memberRequestDTO.setPhone("memberPhone2");
        memberService.save(memberRequestDTO);


        SECURITY_ADMIN_MEMBER=memberRepository.save(Member.builder()
                .loginId("adminMember")
                .email("abc@abc")
                .phone("123456789")
                .name("jkjk")
                .role(Role.ROLE_ADMIN)
                .status(Status.GREEN)
                .password(passwordEncoder.encode("4321"))
                .build());

        SECURITY_MEMBER=memberRepository.save(Member.builder()
                .loginId("userMember")
                .email("cba@cba")
                .phone("123456789")
                .name("jkjk")
                .role(Role.ROLE_USER)
                .status(Status.GREEN)
                .password(passwordEncoder.encode("4321"))
                .build());

        RED_MEMBER=memberRepository.save(Member.builder()
                .loginId("redMember")
                .email("red")
                .phone("221133")
                .name("redred")
                .password(passwordEncoder.encode("4321"))
                .status(Status.RED)
                .build());


        SECURITY_DELETE_MEMBER=memberRepository.save(Member.builder()
                .loginId("deleteMember")
                .email("abc@abc")
                .phone("123321")
                .name("momo")
                .role(Role.ROLE_ADMIN)
                .password(passwordEncoder.encode("1234"))
                .build());
    }

    @BeforeEach
    public void each() {
    }

    // 1. Member ?????? ??????
    // 2. Member ??????
    // 3. Member ????????? ??????
    // 4. Member ??????
    // 5. Member ?????? ?????? ( idx ?????? ?????? ??? )
    // 6. Member ??????
    // 7. Member ?????? ?????? ( idx ?????? ?????? ??? )
    // 8. Member ?????? ?????? ( body ??? ??? ?????? )
    // 9. Member ????????? ??????
    // 10. Member ????????? ??????

    @Test
    @DisplayName("1. Member ?????? ??????")
    public void member_save_fail() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        MemberRequestDTO memberRequestDTO = new MemberRequestDTO();
        memberRequestDTO.setEmail("test");
        memberRequestDTO.setPhone("010101");
        memberRequestDTO.setName("test");

        // Object -> json String
        String paramString = objectMapper.writeValueAsString(memberRequestDTO);

        mockMvc.perform(post(URL)
                        .content(paramString) // body
                        .accept(MediaType.ALL)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andDo(document("Member-Save-Fail", // 1
                        preprocessRequest(prettyPrint()),
                        requestFields(
                                getDescription("loginId", "?????? ????????? ?????????").type(JsonFieldType.STRING),
                                getDescription("password","?????? ????????????").type(JsonFieldType.STRING),
                                getDescription("email", "?????? ?????????").type(JsonFieldType.STRING),
                                getDescription("name","?????? ??????").type(JsonFieldType.STRING),
                                getDescription("phone", "?????? ????????????").type(JsonFieldType.STRING))
                ));

    }

    @Test
    @DisplayName("2. Member ?????? ??????")
    public void member_save() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        MemberPhoneAuth memberPhoneAuth=memberService.certifiedPhone("memberPhone");
        memberService.certifiedPhoneCheck("memberPhone",memberPhoneAuth.getSmscode());

        MemberRequestDTO memberRequestDTO = new MemberRequestDTO();
        memberRequestDTO.setLoginId("memeberLoginId");
        memberRequestDTO.setPassword("memberPassword");
        memberRequestDTO.setEmail("memberEmail");
        memberRequestDTO.setName("memberName");
        memberRequestDTO.setPhone("memberPhone");

        // Object -> json String
        String paramString = objectMapper.writeValueAsString(memberRequestDTO);

        mockMvc.perform(post(URL)
                        .content(paramString) // body
                        .accept(MediaType.ALL)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("??????????????? ?????????????????????."))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("Member-Save-Success", // 1
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                getDescription("loginId", "?????? ????????? ?????????").type(JsonFieldType.STRING),
                                getDescription("password","?????? ????????????").type(JsonFieldType.STRING),
                                getDescription("email", "?????? ?????????").type(JsonFieldType.STRING),
                                getDescription("name","?????? ??????").type(JsonFieldType.STRING),
                                getDescription("phone", "?????? ????????????").type(JsonFieldType.STRING))
                ));
    }

    @Test
    @WithUserDetails("adminMember")
    @DisplayName("3. Member ????????? ??????")
    public void member_list() throws Exception {

        mockMvc.perform(get(URL+"/management")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.ALL))
                .andDo(print())
                .andExpect(status().isOk()) // 7
                .andDo(document("Member-List", // 1
                        preprocessResponse(prettyPrint()), // 2
                        responseFields( // 3
                                subsectionWithPath("[]").description("???????????? ??????"),
                                getDescription("[].id", "?????? ????????????(PK)").type(JsonFieldType.NUMBER),// 5
                                getDescription("[].loginId", "?????? ????????? ?????????").type(JsonFieldType.STRING),
                                getDescription("[].email", "?????? ?????????").type(JsonFieldType.STRING),
                                getDescription("[].name","?????? ??????").type(JsonFieldType.STRING),
                                getDescription("[].phone", "?????? ????????????").type(JsonFieldType.STRING),
                                getDescription("[].status","?????? ??????").type(JsonFieldType.STRING),
                                getDescription("[].createdDate", "???????????? ??????").type(JsonFieldType.STRING),
                                getDescription("[].updatedDate","???????????? ??????").type(JsonFieldType.STRING))
                ));
    }

    @Test
    @DisplayName("4. member ?????? (id)")
    public void member_search() throws Exception {

        // When && Then
        // ??????
        mockMvc.perform(RestDocumentationRequestBuilders.get(URL + "/{id}", 1)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .characterEncoding("UTF-8")
                                .accept(MediaType.ALL)
                )
                .andExpect(jsonPath("loginId").value("memberTest"))
                .andExpect(jsonPath("email").value("aa@aa"))
                .andExpect(jsonPath("name").value("momo"))
                .andExpect(jsonPath("phone").value("memberPhone2"))
                .andExpect(jsonPath("status").value("RED"))
                .andExpect(jsonPath("createdDate").value(String.valueOf(LocalDate.now())))
                .andExpect(jsonPath("updatedDate").value(String.valueOf(LocalDate.now())))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("Member-Search",
                        preprocessRequest(),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                          parameterWithName("id").description("MemberId")
                        ),
                        responseFields(
                                getDescription("loginId", "?????? ????????? ?????????").type(JsonFieldType.STRING),
                                getDescription("email", "?????? ?????????").type(JsonFieldType.STRING),
                                getDescription("name","?????? ??????").type(JsonFieldType.STRING),
                                getDescription("phone", "?????? ????????????").type(JsonFieldType.STRING),
                                getDescription("status","?????? ??????").type(JsonFieldType.STRING),
                                getDescription("role","?????? ??????").type(JsonFieldType.STRING),
                                getDescription("createdDate", "???????????? ??????").type(JsonFieldType.STRING),
                                getDescription("updatedDate","???????????? ??????").type(JsonFieldType.STRING))
                ));
    }

    @Test
    @DisplayName("5. member ?????? ?????? ( id ?????? ?????? ??? )")
    public void member_fail() throws Exception {
        mockMvc.perform(RestDocumentationRequestBuilders.get(URL +"/{id}",1000)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.ALL)
                )
                .andExpect(content().string("????????? ??????????????????."))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(document("Member-Search-Fail" ,
                        preprocessRequest(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("MemberId"))
                ));
    }

    @Test
    @DisplayName("6. member ??????")
    public void member_update() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        MemberUpdateDTO memberUpdateDTO = new MemberUpdateDTO();
        memberUpdateDTO.setEmail("modifyEmail");
        memberUpdateDTO.setPassword("modifyPassword");

        // Object -> json String
        String paramString = objectMapper.writeValueAsString(memberUpdateDTO);

        mockMvc.perform(RestDocumentationRequestBuilders.put(URL + "/{id}",1)
                                .content(paramString)
                                .characterEncoding("UTF-8")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .accept(MediaType.ALL)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("??????????????? ?????? ???????????????."))
                .andDo(document("Member-Update-Success", // 1
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("MemberId")
                        ),
                        requestFields(
                                getDescription("email", "?????? ?????????").type(JsonFieldType.STRING),
                                getDescription("password","?????? ????????????").type(JsonFieldType.STRING)
                        )
                ));

    }

    @Test
    @DisplayName("7. member ?????? {id} ?????? ?????? ???")
    public void member_update_fail() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        MemberUpdateDTO memberUpdateDTO = new MemberUpdateDTO();
        memberUpdateDTO.setEmail("modifyEmail");
        memberUpdateDTO.setPassword("modifyPassword");

        // Object -> json String
        String paramString = objectMapper.writeValueAsString(memberUpdateDTO);

        mockMvc.perform(RestDocumentationRequestBuilders.put(URL + "/{id}",1000)
                                .content(paramString)
                                .characterEncoding("UTF-8")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .accept(MediaType.ALL)
                )
                .andExpect(content().string("?????? ????????? ????????????."))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(document("Member-Update-id-Fail",
                        preprocessRequest(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("MemberId")
                        ),
                        requestFields(
                                getDescription("email", "?????? ?????????").type(JsonFieldType.STRING),
                                getDescription("password","?????? ????????????").type(JsonFieldType.STRING))
                ));
    }

    @Test
    @DisplayName("8. member update ?????? (body ??? ?????? ???)")
    public void member_update_fail2() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        MemberUpdateDTO memberUpdateDTO = new MemberUpdateDTO();

        // Object -> json String
        String paramString = objectMapper.writeValueAsString(memberUpdateDTO);

        mockMvc.perform(RestDocumentationRequestBuilders.put(URL + "/{id}",1)
                                .content(paramString)
                                .characterEncoding("UTF-8")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .accept(MediaType.ALL)
                )
                .andExpect(content().string("???????????? ???????????????."))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(document("Member-Update-body-Fail",
                        preprocessRequest(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("MemberId")
                        ),
                        requestFields(
                                getDescription("email", "?????? ?????????").type(JsonFieldType.STRING),
                                getDescription("password","?????? ????????????").type(JsonFieldType.STRING))
                ));
    }

    @Test
    @DisplayName("9. Member ????????? ??????")
    public void member_login_fail() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        MemberRequestDTO memberRequestDTO = new MemberRequestDTO();
        memberRequestDTO.setLoginId("");
        memberRequestDTO.setPassword("");

        // Object -> json String
        String paramString = objectMapper.writeValueAsString(memberRequestDTO);

        mockMvc.perform(post(URL+"/login")
                        .content(paramString) // body
                        .accept(MediaType.ALL)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("???????????? ???????????????."))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andDo(document("Member-login-Fail", // 1
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                getDescription("loginId", "?????? ????????? ?????????").type(JsonFieldType.STRING),
                                getDescription("password","?????? ????????????").type(JsonFieldType.STRING))
                ));
    }

    @Test
    @DisplayName("10. Member ????????? ??????")
    public void member_login_Success() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        MemberRequestDTO memberRequestDTO = new MemberRequestDTO();
        memberRequestDTO.setLoginId("adminMember");
        memberRequestDTO.setPassword("4321");

        // Object -> json String
        String paramString = objectMapper.writeValueAsString(memberRequestDTO);

        mockMvc.perform(post(URL+"/login")
                        .content(paramString) // body
                        .accept(MediaType.ALL)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("Member-login-Success", // 1
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                getDescription("loginId", "?????? ????????? ?????????").type(JsonFieldType.STRING),
                                getDescription("password","?????? ????????????").type(JsonFieldType.STRING)),
                        responseFields(
                                getDescription("name","????????? ??????").type(JsonFieldType.STRING),
                                getDescription("accessToken","????????? AccessToken").type(JsonFieldType.STRING))
                ));
    }

    @Test
    @DisplayName("11. ???????????? ??????")
    public void phone_cert() throws Exception {

        mockMvc.perform(post(URL+"/cert?phone=01012341234")
                        .accept(MediaType.ALL)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().string("???????????? ?????? ??????."))
                .andExpect(status().isOk())
                .andDo(document("Phone-SMS-Success", // 1
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("phone").description("???????????? ??????"))
                ));
    }

    @Test
    @DisplayName("12. ???????????? ?????? ??????")
    public void phone_cert_fail() throws Exception {

        mockMvc.perform(post(URL+"/cert?phone=memberPhone2")
                        .accept(MediaType.ALL)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().string("?????? ???????????????."))
                .andExpect(status().isBadRequest())
                .andDo(document("Phone-SMS-Fail", // 1
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("phone").description("???????????? ??????"))
                ));
    }

    @Test
    @DisplayName("13.???????????? ??????")
    public void phone_cert_check() throws Exception {
        MemberPhoneAuth memberPhoneAuth=memberService.certifiedPhone("memberPhone4");

        mockMvc.perform(put(URL+"/cert?phone=memberPhone4&smscode="+memberPhoneAuth.getSmscode())
                        .accept(MediaType.ALL)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().string("?????? ?????? ???????????????."))
                .andExpect(status().isOk())
                .andDo(document("Phone-SMS-Check-Success", // 1
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("phone").description("???????????? ??????"),
                                parameterWithName("smscode").description("????????????"))
                ));
    }

    @Test
    @DisplayName("14.???????????? ????????????( ??????????????? ??????????????? ?????? ?????? ?????? )")
    public void phone_cert_check_fail_phone() throws Exception {

        mockMvc.perform(put(URL+"/cert?phone=01010101&smscode=0000")
                        .accept(MediaType.ALL)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().string("????????? ????????? ?????? ???????????????"))
                .andExpect(status().isNotFound())
                .andDo(document("Phone-SMS-Check-Fail-NoPhone", // 1
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("phone").description("???????????? ??????"),
                                parameterWithName("smscode").description("????????????"))
                ));
    }

    @Test
    @DisplayName("15.???????????? ????????????( ??????????????? ????????? )")
    public void phone_cert_check_fail_smscode() throws Exception {
        memberService.certifiedPhone("memberPhone3");

        mockMvc.perform(put(URL+"/cert?phone=memberPhone3&smscode=0000")
                        .accept(MediaType.ALL)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().string("??????????????? ???????????? ????????????."))
                .andExpect(status().isBadRequest())
                .andDo(document("Phone-SMS-Check-Fail-NoSMS", // 1
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("phone").description("???????????? ??????"),
                                parameterWithName("smscode").description("????????????"))
                ));
    }

    @Test
    @DisplayName(" Member ?????? ?????? ( ?????? ?????? ????????? )")
    public void member_save_fail_noSMS() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        MemberRequestDTO memberRequestDTO = new MemberRequestDTO();
        memberRequestDTO.setLoginId("memeberLoginId");
        memberRequestDTO.setPassword("memberPassword");
        memberRequestDTO.setEmail("memberEmail");
        memberRequestDTO.setName("memberName");
        memberRequestDTO.setPhone("memberPhone");

        // Object -> json String
        String paramString = objectMapper.writeValueAsString(memberRequestDTO);

        mockMvc.perform(post(URL)
                        .content(paramString) // body
                        .accept(MediaType.ALL)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("????????? ????????? ???????????????"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andDo(document("Member-Save-Fail-NoSMS", // 1
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                getDescription("loginId", "?????? ????????? ?????????").type(JsonFieldType.STRING),
                                getDescription("password","?????? ????????????").type(JsonFieldType.STRING),
                                getDescription("email", "?????? ?????????").type(JsonFieldType.STRING),
                                getDescription("name","?????? ??????").type(JsonFieldType.STRING),
                                getDescription("phone", "?????? ????????????").type(JsonFieldType.STRING))
                ));
    }

    @Test
    @WithUserDetails("memberTest")
    @DisplayName("Member ??? ??????")
    public void member_info() throws Exception {
        mockMvc.perform(get(URL)
                        .accept(MediaType.ALL)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("Member-My-Info", // 1
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                getDescription("loginId","????????? ?????????").type(JsonFieldType.STRING),
                                getDescription("name","??????").type(JsonFieldType.STRING),
                                getDescription("email","?????????").type(JsonFieldType.STRING),
                                getDescription("phone","????????????").type(JsonFieldType.STRING))
                ));
    }

    @Test
    @WithUserDetails("memberTest")
    @DisplayName("Member ???????????? ??????")
    public void member_password_change() throws Exception {
        String password ="{\"newPassword\" : \"123456\"}";

        mockMvc.perform(put(URL)
                        .content(password)
                        .accept(MediaType.ALL)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("??????????????? ?????? ???????????????."))
                .andDo(document("Member-Password-Change", // 1
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                getDescription("newPassword","????????? ????????????"))
                ));
    }


    @Test
    @WithUserDetails("memberTest")
    @DisplayName("Member ?????? ?????? ??????")
    public void member_delete_fail() throws Exception {


        String password ="{\"password\" : \"12312312\"}";

        mockMvc.perform(delete(URL)
                        .content(password)
                        .accept(MediaType.ALL)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("?????? ??????????????? ?????? ????????????."))
                .andDo(document("Member-Delete-Fail", // 1
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                getDescription("password","?????? Member ????????????"))
                ));
    }

    @Test
    @WithUserDetails("deleteMember")
    @DisplayName("Member ?????? ??????")
    public void member_delete() throws Exception {

        String password ="{\"password\" : \"1234\"}";

        mockMvc.perform(delete(URL)
                        .content(password)
                        .accept(MediaType.ALL)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("???????????? ??????"))
                .andDo(document("Member-Delete", // 1
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                getDescription("password","?????? Member ????????????"))
                ));
    }

    @Test
    @WithUserDetails("adminMember")
    @DisplayName("????????? ????????? Member ?????????")
    public void member_approval_list() throws Exception {

        mockMvc.perform(get(URL+"/approval")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.ALL))
                .andDo(print())
                .andExpect(status().isOk()) // 7
                .andDo(document("Member-Approval-List", // 1
                        preprocessResponse(prettyPrint()), // 2
                        responseFields( // 3
                                getDescription("[].id", "?????? ????????????(PK)").type(JsonFieldType.NUMBER),// 5
                                getDescription("[].name","?????? ??????").type(JsonFieldType.STRING),
                                getDescription("[].phone", "?????? ????????????").type(JsonFieldType.STRING),
                                getDescription("[].createdDate", "???????????? ???????????? ??????").type(JsonFieldType.STRING))
                ));
    }

    @Test
    @WithUserDetails("userMember")
    @DisplayName("????????? ????????? Member ????????? ?????? ??????")
    public void member_approval_list_Fail() throws Exception {

        mockMvc.perform(get(URL+"/approval")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.ALL))
                .andExpect(content().string("????????? ??????"))
                .andDo(print())
                .andExpect(status().isForbidden()) // 7
                .andDo(document("Member-Approval-List-Fail"));

    }

    @Test
    @WithUserDetails("adminMember")
    @DisplayName("???????????? ??????")
    public void member_approval() throws Exception {

        mockMvc.perform(RestDocumentationRequestBuilders.post(URL+"/approval/{id}",RED_MEMBER.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.ALL))
                .andExpect(content().string("??????????????? ?????????????????????."))
                .andDo(print())
                .andExpect(status().isOk()) // 7
                .andDo(document("Member-Approval", // 1
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("?????? ????????????"))// 2
                ));
    }

    @Test
    @WithUserDetails("userMember")
    @DisplayName("???????????? ?????? ??????")
    public void member_approval_fail() throws Exception {

        mockMvc.perform(RestDocumentationRequestBuilders.post(URL+"/approval/{id}",RED_MEMBER.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.ALL))
                .andExpect(content().string("????????? ??????"))
                .andDo(print())
                .andExpect(status().isForbidden()) // 7
                .andDo(document("Member-Approval-fail"));

    }





    private FieldDescriptor getDescription(String name, String description) {
        return fieldWithPath(name)
                .description(description);
    }

}
