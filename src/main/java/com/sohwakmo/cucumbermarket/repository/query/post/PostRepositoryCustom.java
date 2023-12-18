package com.sohwakmo.cucumbermarket.repository.query.post;

import com.sohwakmo.cucumbermarket.domain.Post;

public interface PostRepositoryCustom {
    Post selectPostByImageSrc(String fileName);
}
