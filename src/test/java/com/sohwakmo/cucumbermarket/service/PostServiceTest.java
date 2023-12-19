package com.sohwakmo.cucumbermarket.service;

import com.sohwakmo.cucumbermarket.domain.Post;
import com.sohwakmo.cucumbermarket.repository.PostRepository;
import javassist.NotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.parameters.P;

import javax.transaction.Transactional;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    // 테스트 코드 수정
    @Test
    @DisplayName("이미지 삽입 테스트")
    @Transactional
    void testInsertImage() throws Exception {
        // given
        Integer postNo = 1;
        MockMultipartFile data = new MockMultipartFile("data", "test.jpg", "image/jpeg", "test image".getBytes());

        Post existingPost = Post.builder()
                .title("테스트")
                .content("테스트")
                .imageName01("oldImage")
                .imageUrl01("oldImage")
                .build();

        when(postRepository.findById(postNo)).thenReturn(Optional.of(existingPost));
//        when(postRepository.save(any(Post.class))).thenReturn(existingPost); // Assuming save returns the same post

        // when
        String result = postService.insertImage(postNo, data);

        // then
        assertEquals("2번이미지 삽입 완료", result); // 수정된 부분
    }

    @Test
    @DisplayName("게시물이 없는 경우 예외 테스트")
    @Transactional
    void testInsertImageWithNonExistingPost() {
        // given
        Integer nonExistingPostNo = 2;
        MockMultipartFile data = new MockMultipartFile("data", "test.jpg", "image/jpeg", "test image".getBytes());

        when(postRepository.findById(nonExistingPostNo)).thenReturn(Optional.empty());

        // when, then
        assertThrows(NotFoundException.class, () -> postService.insertImage(nonExistingPostNo, data));
    }

    @Test
    @DisplayName("양쪽에 전부 사진이 들어 있는경우 경고 메세지 확인")
    void fullImage() throws Exception {
        Integer postNo = 3;
        MockMultipartFile data = new MockMultipartFile("data", "test.jpg", "image/jpeg", "test image".getBytes());
        Post post = Post.builder()
                .title("테스트")
                .content("테스트")
                .imageName01("oldImage")
                .imageUrl01("oldImage")
                .imageName02("oldImage")
                .imageUrl02("oldImage")
                .build();

        // when
        when(postRepository.findById(postNo)).thenReturn(Optional.of(post));

        String errorMsg = postService.insertImage(postNo, data);
        assertThat(errorMsg).isEqualTo("사진은 2장까지 가능합니다!!");
    }
}