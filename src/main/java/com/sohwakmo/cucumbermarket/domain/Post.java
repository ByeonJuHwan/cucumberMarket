package com.sohwakmo.cucumbermarket.domain;


import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.util.StringUtils;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.*;
import static org.springframework.util.StringUtils.*;

@Entity(name = "POST")
@NoArgsConstructor(access = PROTECTED)
@Getter
@SequenceGenerator(name = "POSTS_SEQ_GEN",sequenceName = "POST_SEQ", allocationSize = 1)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "POSTS_SEQ_GEN")
    private Integer postNo; // Primary Key(고유키)

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberNo")
    @ToString.Exclude
    private Member member;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reply> replies = new ArrayList<>();

    @ColumnDefault("0")
    private Integer clickCount;

    private String imageUrl01;

    private String imageName01;

    private String imageUrl02;

    private String imageName02;


    @Builder
    public Post(Integer postNo, String title, String content, Member member, List<Reply> replies, Integer clickCount, String imageUrl01, String imageName01, String imageUrl02, String imageName02) {
        this.postNo = postNo;
        this.title = title;
        this.content = content;
        this.member = member;
        this.replies = (replies != null) ? replies : new ArrayList<>();
        this.clickCount = clickCount;
        this.imageUrl01 = imageUrl01;
        this.imageName01 = imageName01;
        this.imageUrl02 = imageUrl02;
        this.imageName02 = imageName02;
    }

    public Post update(String title, String content) {
        this.title = title;
        this.content = content;
        return this;
    }

    public Post plusClickCount(Integer clickCount){
        this.clickCount = clickCount + 1;
        return this;
    }

    public void saveImage01NameAndUrl(String fileName) {
        this.imageName01 = fileName;
        this.imageUrl01 = "/files/" + fileName;
    }
    public void saveImage02NameAndUrl(String fileName) {
        this.imageName02 = fileName;
        this.imageUrl02 = "/files/" + fileName;
    }

    /**
     * 1번 이미지란이 비어있으면 1번이미지에
     * 아니면 2번 이미지로 저장
     */
    public String saveImage(String fileName) {
        if (isUrlBothFull()) return "사진은 2장까지 가능합니다!!";
        if (isImage01Empty()) {
            this.imageName01 = fileName;
            this.imageUrl01 = "/files/" + fileName;
            return "1번이미지 삽입 완료";
        }else{
            this.imageName02 = fileName;
            this.imageUrl02 = "/files/" + fileName;
            return "2번이미지 삽입 완료";
        }
    }

    /**
     * 사진 url 이 전부 다 꽉차있는경우
     * @return
     */
    private boolean isUrlBothFull() {
        return hasText(imageUrl01) && hasText(imageUrl02);
    }

    /**
     * 게시판 객체의 이미지 파일 경로, 파일 이름 란이 비어있는지 확인
     * @return
     */
    private boolean isImage01Empty() {
        return !hasText(imageName01);
    }
}
