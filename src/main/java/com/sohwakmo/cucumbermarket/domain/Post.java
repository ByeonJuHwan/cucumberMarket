package com.sohwakmo.cucumbermarket.domain;


import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import javax.persistence.*;

import static lombok.AccessLevel.*;

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
    @ToString.Exclude
    private Member member;

    @ColumnDefault("0")
    private Integer clickCount;

    private String imageUrl01;

    private String imageName01;

    private String imageUrl02;

    private String imageName02;

    @Builder
    public Post(String title, String content, Member member, Integer clickCount, String imageUrl01, String imageName01, String imageUrl02, String imageName02) {
        this.title = title;
        this.content = content;
        this.member = member;
        this.clickCount = clickCount;
        this.imageUrl01 = imageUrl01;
        this.imageName01 = imageName01;
        this.imageUrl02 = imageUrl02;
        this.imageName02 = imageName02;
    }

    @Builder
    public Post(Integer postNo, String title, String content, Member member, Integer clickCount, String imageUrl01, String imageName01, String imageUrl02, String imageName02) {
        this.postNo = postNo;
        this.title = title;
        this.content = content;
        this.member = member;
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
        this.clickCount = clickCount;
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
}
