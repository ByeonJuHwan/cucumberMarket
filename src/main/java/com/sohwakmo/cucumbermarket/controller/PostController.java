package com.sohwakmo.cucumbermarket.controller;

import com.sohwakmo.cucumbermarket.domain.Member;
import com.sohwakmo.cucumbermarket.domain.Post;
import com.sohwakmo.cucumbermarket.dto.*;
import com.sohwakmo.cucumbermarket.service.MemberService;
import com.sohwakmo.cucumbermarket.service.PostService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class    PostController {

    private final PostService postService;
    private final MemberService memberService;

    @GetMapping("/posts")
    public String list(Model model, @PageableDefault(sort = "postNo", direction = Sort.Direction.DESC) Pageable pageable,
                       @RequestParam(required = false,defaultValue = "")String searchText,
                       @RequestParam(required = false,defaultValue = "전국") String address){
        Page<SearchPostDto> page = postService.searchPost(searchText, address, pageable);
        int endPage = page.getTotalPages();

        model.addAttribute("address", address);
        model.addAttribute("searchText", searchText);
        model.addAttribute("list", page);
        model.addAttribute("startPage", page.getPageable().first().getPageNumber() + 1);
        model.addAttribute("endPage", endPage == 0 ? endPage+ 1 : endPage);

        return "/post/list";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/detail")
    public String detail(Model model, @RequestParam Integer postNo,@RequestParam(required = false, defaultValue = "-1")Integer clickCount){
        if(clickCount==-1) clickCount = postService.setClickCount(postNo, clickCount);  // modify 에서 넘어올경우 파라미터 초기화
        PostDetailDto post = postService.findPostByIdAndUpdateClickCount(postNo,clickCount); // detail page 로 올경우 조회수도 같이 증가.

        model.addAttribute("post", post);
//        model.addAttribute("nickname", post.getMember().getNickname());
//        model.addAttribute("member", post.getMember());

        return "/post/detail";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/create")
    public String create(){
        return "/post/create";
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/create")
    public String create(PostCreateDto dto, Integer memberNo, @RequestParam(value = "files", required = false) List<MultipartFile> files,RedirectAttributes attrs) throws Exception {
        // 매너온도 +1.5
        Member member = memberService.updateGrade(memberNo);

        // dto 로 넘어온 내용들을 가지고 게시물생성
        postService.insertPost(dto, member, files);

        attrs.addAttribute("address", member.getAddress());

        return "redirect:/api/posts";
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/delete")
    public String delete(Integer id, Integer memberNo){
        //매너온도 -1.5
        memberService.minusGrade(memberNo);
        // 관련 댓글 및 게시물 삭제
        postService.deletePost(id);

        return "redirect:/api/posts";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/modify")
    public String modify(Model model, Integer id){
        Post post = postService.findPostByPostNo(id);

        model.addAttribute("post", post);

        return "/post/modify";
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/modify")
    public String modify(PostUpdateDto dto,RedirectAttributes attrs, @RequestParam("files") List<MultipartFile> files){
        Integer postNo = postService.modifyPost(dto);

        attrs.addAttribute("postNo",postNo);

        return "redirect:/api/detail";
    }
}
