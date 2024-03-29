package com.sohwakmo.cucumbermarket.controller;

import com.sohwakmo.cucumbermarket.domain.Post;
import com.sohwakmo.cucumbermarket.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/post/reply")
public class PostRegisterController {

    private final PostService postService;

    @PutMapping("/3{postNo}")
    public ResponseEntity<String>insertImage(@PathVariable Integer postNo, @RequestParam("file") @RequestBody MultipartFile data) throws Exception {
        return ResponseEntity.ok(postService.insertImage(postNo,data));
    }

    @PutMapping("/1{postNo}")
    public ResponseEntity<String>modifyImage01(@PathVariable Integer postNo,@RequestParam("file")@RequestBody MultipartFile data)throws Exception{
        String fileName = postService.modifyImage01(postNo,data);
        return ResponseEntity.ok(fileName);
    }

    @PutMapping("/2{postNo}")
    public ResponseEntity<String>modifyImage02(@PathVariable Integer postNo,@RequestParam("file")@RequestBody MultipartFile data)throws Exception{
        String fileName = postService.modifyImage02(postNo, data);
        return ResponseEntity.ok(fileName);
    }

    @DeleteMapping("/{postNo}")
    public ResponseEntity<String> deleteImage(@PathVariable Integer postNo, @RequestParam("imageSrc") String imageSrc) throws Exception{
        String result = postService.checkImageNumAndDeleteImage(postNo,imageSrc);
        return ResponseEntity.ok("성공");
    }
}
