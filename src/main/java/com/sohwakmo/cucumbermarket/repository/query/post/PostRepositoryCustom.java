package com.sohwakmo.cucumbermarket.repository.query.post;

import com.sohwakmo.cucumbermarket.domain.Post;
import com.sohwakmo.cucumbermarket.dto.PostDetailDto;
import com.sohwakmo.cucumbermarket.dto.SearchPostDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PostRepositoryCustom {
    Post selectPostByImageSrc(String fileName);
    Page<SearchPostDto> selectPostBySearch(String searchString, String address,Pageable pageable);

    PostDetailDto selectDetailPostMember(Integer postNo);
}
