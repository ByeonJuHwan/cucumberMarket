package com.sohwakmo.cucumbermarket.repository.query.post;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sohwakmo.cucumbermarket.domain.Post;
import com.sohwakmo.cucumbermarket.dto.SearchPostDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import javax.persistence.EntityManager;

import java.util.List;

import static com.sohwakmo.cucumbermarket.domain.QPost.*;

public class PostRepositoryCustomImpl implements PostRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public PostRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Post selectPostByImageSrc(String fileName) {
        return queryFactory.selectFrom(post)
                .where(post.imageName01.eq(fileName).or(post.imageName02.eq(fileName)))
                .fetchOne();
    }

    @Override
    public Page<SearchPostDto> selectPostBySearch(String searchString, Pageable pageable) {
        List<SearchPostDto> result = queryFactory
                .select(Projections.constructor(
                        SearchPostDto.class,
                        post.postNo,
                        post.title,
                        post.member.nickname.as("writer"),
                        post.clickCount,
                        post.createdTime
                )).from(post)
                .where(
                        post.title.upper().like(searchString)
                                .or(post.content.upper().like(searchString))
                                .or(post.member.nickname.upper().like(searchString))
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> count = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        post.title.upper().like(searchString)
                                .or(post.content.upper().like(searchString))
                                .or(post.member.nickname.upper().like(searchString))
                );
        return PageableExecutionUtils.getPage(result, pageable, count::fetchOne);
    }
}
