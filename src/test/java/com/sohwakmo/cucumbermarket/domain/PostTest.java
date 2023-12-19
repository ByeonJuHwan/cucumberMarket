package com.sohwakmo.cucumbermarket.domain;

import com.sohwakmo.cucumbermarket.repository.PostRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PostTest {

    @Autowired
    PostRepository postRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("이미지저장 공통 메서드 리펙토링 - 1번 이미지가 없는 경우")
    void saveImage() {
        Post post = postRepository.save(Post.builder().title("테스트").content("테스트").build());
        post.saveImage("fileName");

        assertThat(post.getImageUrl01()).isEqualTo("/files/" + "fileName");
        assertThat(post.getImageName01()).isEqualTo("fileName");
        assertThat(post.getImageUrl02()).isEqualTo(null);
        assertThat(post.getImageName02()).isEqualTo(null);
    }

    @Test
    @DisplayName("이미지저장 공통 메서드 리펙토링 - 1번 이미지가 있는 경우")
    void saveImage2() {
        Post post = postRepository.save(Post.builder().title("테스트").content("테스트").imageName01("test").imageUrl01("test").build());
        post.saveImage("fileName");

        em.flush();
        em.clear();

        assertThat(post.getImageUrl02()).isEqualTo("/files/" + "fileName");
        assertThat(post.getImageName02()).isEqualTo("fileName");
    }

}