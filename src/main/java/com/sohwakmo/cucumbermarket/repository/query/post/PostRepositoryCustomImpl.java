package com.sohwakmo.cucumbermarket.repository.query.post;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sohwakmo.cucumbermarket.domain.Post;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;

import static com.sohwakmo.cucumbermarket.domain.QPost.*;

@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom{

    private final EntityManager em;

    JPAQueryFactory queryFactory;
    @Override
    public Post selectPostByImageSrc(String fileName) {
        queryFactory = new JPAQueryFactory(em);
        return queryFactory.selectFrom(post)
                .where(post.imageName01.eq(fileName).or(post.imageName02.eq(fileName)))
                .fetchOne();
    }
}
