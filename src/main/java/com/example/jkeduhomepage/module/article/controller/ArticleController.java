package com.example.jkeduhomepage.module.article.controller;

import com.example.jkeduhomepage.module.article.dto.ArticleRequestDTO;
import com.example.jkeduhomepage.module.article.entity.UploadFile;
import com.example.jkeduhomepage.module.article.service.ArticleService;
import com.example.jkeduhomepage.module.article.service.AwsS3Service;
import com.example.jkeduhomepage.module.common.enums.Category;
import com.example.jkeduhomepage.module.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/article")
public class ArticleController {

    private final AwsS3Service awsS3Service;
    private final ArticleService articleService;

    @PostMapping("/{category}")
    public ResponseEntity<Object> save(@PathVariable Category category, @RequestBody ArticleRequestDTO articleRequestDTO){
        Member member=articleService.isMemberCurrent();

        if(articleRequestDTO.getTitle().equals(""))
            return ResponseEntity.badRequest().body("제목을 작성하세요");
        if(articleRequestDTO.getContent().equals(""))
            return ResponseEntity.badRequest().body("내용을 입력하세요");

        return ResponseEntity.ok().body(articleService.saveArticle(category,member,articleRequestDTO));
    }

    @GetMapping("/{category}")
    public ResponseEntity<Object> categoryList(@PathVariable Category category, @PageableDefault Pageable pageable){
        articleService.isMemberCurrent();

        return ResponseEntity.ok().body(articleService.categoryList(category,pageable));
    }



    @PostMapping("/file")
    public ResponseEntity<List<UploadFile>> uploadFile(@RequestParam("category") String filePath, @RequestPart(value = "file")  List<MultipartFile> multipartFile) {
//        Member member=articleService.isMemberCurrent();
        if(filePath.equalsIgnoreCase("GALLERY")){
            throw new RuntimeException("썸넬 이미지 만들어야함");
        }

        return ResponseEntity.ok(awsS3Service.uploadFile(filePath,multipartFile));
    }

    @DeleteMapping("/file")
    public ResponseEntity<Object> deleteFile(@RequestParam("category") String filePath, @RequestParam String fileName) {
        awsS3Service.deleteFile(filePath,fileName);
        return ResponseEntity.ok("삭제 완료");
    }



}
