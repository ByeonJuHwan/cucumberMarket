package com.sohwakmo.cucumbermarket.dto;

import com.sohwakmo.cucumbermarket.domain.Post;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SearchPostDto {
    private int postNo;
    private String title;
    private String writer;
    private int clickCount;
    private LocalDateTime createdTime;

    public SearchPostDto(int postNo, String title, String writer, int clickCount, LocalDateTime createdTime) {
        this.postNo = postNo;
        this.title = title;
        this.writer = writer;
        this.clickCount = clickCount;
        this.createdTime = createdTime;
    }

    public SearchPostDto(Post post) {
        this.postNo = post.getPostNo();
        this.title = post.getTitle();
        this.writer = post.getMember().getNickname();
        this.clickCount = post.getClickCount();
        this.createdTime = post.getCreatedTime();
    }
}
