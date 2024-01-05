package com.sohwakmo.cucumbermarket.dto;


import com.sohwakmo.cucumbermarket.domain.Post;
import lombok.Getter;


import java.time.LocalDateTime;

@Getter
public class PostDetailDto {
    private String userImgUrl;
    private Integer memberNo;
    private Integer postNo;

    private String title;
    private String nickname;
    private LocalDateTime createdTime;
    private int clickCount;
    private String imageUrl01;
    private String imageUrl02;
    private String content;

    public PostDetailDto(Post post) {
        this.userImgUrl = post.getMember().getUserImgUrl();
        this.memberNo = post.getMember().getMemberNo();
        this.postNo = post.getPostNo();
        this.title = post.getTitle();
        this.nickname = post.getMember().getNickname();
        this.createdTime = post.getCreatedTime();
        this.clickCount = post.getClickCount();
        this.imageUrl01 = post.getImageUrl01();
        this.imageUrl02 = post.getImageUrl02();
        this.content = post.getContent();
    }
}
