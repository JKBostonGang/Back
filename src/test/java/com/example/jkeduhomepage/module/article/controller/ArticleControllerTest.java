package com.example.jkeduhomepage.module.article.controller;

import com.example.jkeduhomepage.module.article.dto.ArticleRequestDTO;
import com.example.jkeduhomepage.module.article.entity.Article;
import com.example.jkeduhomepage.module.article.entity.UploadFile;
import com.example.jkeduhomepage.module.article.repository.ArticleRepository;
import com.example.jkeduhomepage.module.common.enums.Category;
import com.example.jkeduhomepage.module.common.enums.Role;
import com.example.jkeduhomepage.module.member.entity.Member;
import com.example.jkeduhomepage.module.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("dev")
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
class ArticleControllerTest {

    private final String URL = "/article";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ArticleRepository articleRepository;

    private Member SECURITY_MEMBER;

    private Article TEST_ARTICLE;

    private Article TEST_DELETE_ARTICLE;

    private Long SECURITY_MEMBER_ID;


    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext,
                      RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build();
    }

    @BeforeAll
    public void all(){
        saveMember();
        deleteArticle();
        saveArticle();
    }

    public void saveMember() {
        SECURITY_MEMBER=memberRepository.save(Member.builder()
                .loginId("aaaa")
                .email("")
                .phone("01091097122")
                .name("momo")
                .role(Role.ROLE_ADMIN)
                .password("1234")
                .build());

        SECURITY_MEMBER_ID=SECURITY_MEMBER.getId();

    }

    public void saveArticle(){
        List<UploadFile> uploadFileList=new ArrayList<>();

        UploadFile uploadFile =new UploadFile();
        uploadFile.setFileName("test");
        uploadFile.setCustomFileName("test");
        uploadFile.setUrl("test");
        uploadFileList.add(uploadFile);

        Article article =new Article();
        article.setTitle("????????? ??????");
        article.setMember(SECURITY_MEMBER);
        article.setCategory(Category.NOTICE);
        article.setContent("????????? ??????");
        article.setUploadFileList(uploadFileList);
        TEST_ARTICLE=articleRepository.save(article);
    }

    public void deleteArticle(){
        List<UploadFile> uploadFileList=new ArrayList<>();

        UploadFile uploadFile =new UploadFile();
        uploadFile.setFileName("test2");
        uploadFile.setCustomFileName("test2");
        uploadFile.setUrl("test2");
        uploadFileList.add(uploadFile);

        Article article =new Article();
        article.setTitle("????????? ??????2");
        article.setMember(SECURITY_MEMBER);
        article.setCategory(Category.NOTICE);
        article.setContent("????????? ??????2");
        article.setUploadFileList(uploadFileList);
        TEST_DELETE_ARTICLE=articleRepository.save(article);
    }


    @AfterTransaction
    public void accountCleanup() {
        memberRepository.deleteById(SECURITY_MEMBER_ID);
        articleRepository.deleteById(TEST_ARTICLE.getId());
    }

    // 1. ?????? ?????????

    @Test
    @WithUserDetails(value = "aaaa")
    @DisplayName("1.????????? ?????????")
    public void file_upload() throws Exception {
        //TODO : ????????? response ??????

        File filePath=new File("/Users/jjunmo/Desktop/Back/src/test/resources/static/test.jpeg");
        MockMultipartFile file = new MockMultipartFile("file", "test.jpeg", "image/jpeg",
                Files.readAllBytes(filePath.toPath()));

        mockMvc.perform(multipart(URL+"/file?category=notice")
                        .file(file)
                        .contentType(MediaType.IMAGE_JPEG)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("Image-Upload", // 1
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParts(partWithName("file").description("????????? ??????")),
                        queryParameters(parameterWithName("category").description("AWS S3 ?????? ??????????????????")),
                        responseFields(
                                getDescription("[].fileName","?????? ?????? ??????"),
                                getDescription("[].customFileName","???????????? ?????? ??????"),
                                getDescription("[].url","???????????? URL"),
                                getDescription("[].thumbnailImageUrl","????????? ????????? URL")
                        )

                ));
    }

    @Test
    @WithUserDetails(value = "aaaa")
    @DisplayName("?????? ?????????( image ?????? )")
    public void file_upload_noImage() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Upload Test :)".getBytes()
        );

        mockMvc.perform(multipart(URL+"/file?category=notice")
                        .file(file)
                        .accept(MediaType.ALL)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("File-Upload", // 1
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParts(partWithName("file").description("????????? ??????")),
                        queryParameters(parameterWithName("category").description("AWS S3 ?????? ??????????????????")),
                        responseFields(
                                getDescription("[].fileName","?????? ?????? ??????"),
                                getDescription("[].customFileName","???????????? ?????? ??????"),
                                getDescription("[].url","???????????? URL")
                        )

                ));
    }

    @Test
    @WithUserDetails(value = "aaaa")
    @DisplayName("2.????????? ??????")
    public void article_save_success() throws Exception {
        List<UploadFile> uploadFileList=new ArrayList<>();

        UploadFile uploadFile =new UploadFile();
        uploadFile.setFileName("test");
        uploadFile.setCustomFileName("test");
        uploadFile.setUrl("test");
        uploadFileList.add(uploadFile);

        ArticleRequestDTO articleRequestDTO=new ArticleRequestDTO();
        articleRequestDTO.setTitle("??????!!");
        articleRequestDTO.setContent("??????!!");
        articleRequestDTO.setUploadFileList(uploadFileList);


        String paramString = objectMapper.writeValueAsString(articleRequestDTO);

        mockMvc.perform(RestDocumentationRequestBuilders.post(URL+"/{category}","notice")
                        .content(paramString) // body
                        .accept(MediaType.ALL)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("Article-Save-Success", // 1
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("category").description("????????? ????????????")
                        ),
                        requestFields(
                                getDescription("title","????????? ??????"),
                                getDescription("content","????????? ??????"),
                                getDescription("uploadFileList[].fileName","???????????? ??????"),
                                getDescription("uploadFileList[].customFileName","????????? ????????? ?????? ??????"),
                                getDescription("uploadFileList[].url","???????????? ??????")),
                        responseFields(
                                getDescription("id","????????? ??????"),
                                getDescription("category","????????? ????????????"),
                                getDescription("title","????????? ??????"),
                                getDescription("content","????????? ??????"),
                                getDescription("uploadFileList[].id","DB??? ????????? ?????? PK"),
                                getDescription("uploadFileList[].fileName","DB??? ????????? ?????? ??????"),
                                getDescription("uploadFileList[].customFileName","DB??? ????????? ????????? ?????? ??????"),
                                getDescription("uploadFileList[].url","DB??? ????????? ???????????? ??????"))
                ));

    }

    @Test
    @WithUserDetails(value = "aaaa")
    @DisplayName("3.????????? ?????? ??????")
    public void article_save_fail_title() throws Exception {
        List<UploadFile> uploadFileList=new ArrayList<>();

        UploadFile uploadFile =new UploadFile();
        uploadFile.setFileName("test");
        uploadFile.setCustomFileName("test");
        uploadFile.setUrl("test");
        uploadFileList.add(uploadFile);

        ArticleRequestDTO articleRequestDTO=new ArticleRequestDTO();
        articleRequestDTO.setContent("??????!!");
        articleRequestDTO.setUploadFileList(uploadFileList);


        String paramString = objectMapper.writeValueAsString(articleRequestDTO);

        mockMvc.perform(RestDocumentationRequestBuilders.post(URL+"/{category}","notice")
                        .content(paramString) // body
                        .accept(MediaType.ALL)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(content().string("????????? ???????????????"))
                .andDo(document("Article-Save-Fail", // 1
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("category").description("????????? ????????????")
                        ),
                        requestFields(
                                getDescription("title","????????? ??????"),
                                getDescription("content","????????? ??????"),
                                getDescription("uploadFileList[].fileName","???????????? ??????"),
                                getDescription("uploadFileList[].customFileName","????????? ????????? ?????? ??????"),
                                getDescription("uploadFileList[].url","???????????? ??????"))
                ));
    }

    @Test
    @WithUserDetails(value = "aaaa")
    @DisplayName("4.????????? ????????? ??????")
    public void article_list() throws Exception {

        mockMvc.perform(RestDocumentationRequestBuilders.get(URL+"/{category}","notice")
                        .accept(MediaType.ALL)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("Article-List", // 1
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("category").description("????????? ????????????")
                        ),
                        responseFields(
                                getDescription("next","?????? ????????? ??????"),
                                getDescription("nowPage","?????? ????????? ??????"),
                                getDescription("articleResponseDTOList[].id","????????? ??????"),
                                getDescription("articleResponseDTOList[].title","????????? ??????"),
                                getDescription("articleResponseDTOList[].name","????????? ?????????"),
                                getDescription("articleResponseDTOList[].createdDate","????????? ?????????"))
                ));
    }

    @Test
    @WithUserDetails(value = "aaaa")
    @DisplayName("5.????????? ??????")
    public void get_article() throws Exception {

        mockMvc.perform(RestDocumentationRequestBuilders.get(URL+"/{category}/{id}","notice",1)
                        .accept(MediaType.ALL)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("Article-Get", // 1
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("category").description("????????? ????????????"),
                                parameterWithName("id").description("????????? ??????")
                        ),
                        responseFields(
                                getDescription("id","????????? ??????"),
                                getDescription("title","????????? ??????"),
                                getDescription("content","????????? ??????"),
                                getDescription("name","?????????"),
                                getDescription("createdDate","?????????"),
                                getDescription("uploadFileList[].id","DB??? ????????? ?????? PK"),
                                getDescription("uploadFileList[].fileName","DB??? ????????? ?????? ??????"),
                                getDescription("uploadFileList[].customFileName","DB??? ????????? ????????? ?????? ??????"),
                                getDescription("uploadFileList[].url","DB??? ????????? ???????????? ??????"),
                                getDescription("uploadFileList[].article","????????? ????????? ??????"))
                ));
    }

    @Test
    @WithUserDetails(value = "aaaa")
    @DisplayName("6.?????? ?????? ?????? ?????????")
    public void get_article_fail() throws Exception {

        mockMvc.perform(RestDocumentationRequestBuilders.get(URL+"/{category}/{id}","notice",1000)
                        .accept(MediaType.ALL)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("?????? ???????????? ???????????? ????????????."))
                .andDo(document("Article-Get-Fail", // 1
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("category").description("????????? ????????????"),
                                parameterWithName("id").description("????????? ??????"))
                ));
    }

    @Test
    @WithUserDetails(value = "aaaa")
    @DisplayName("7.????????? ??????")
    public void delete_article() throws Exception {

        mockMvc.perform(RestDocumentationRequestBuilders.delete(URL+"/{category}/{id}","notice",2)
                        .accept(MediaType.ALL)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("??? ?????? ??????"))
                .andDo(document("Article-Delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("category").description("????????? ????????????"),
                                parameterWithName("id").description("????????? ??????"))
                ));
    }


    private FieldDescriptor getDescription(String name, String description) {
        return fieldWithPath(name)
                .description(description);
    }

//    private ParameterDescriptor


}