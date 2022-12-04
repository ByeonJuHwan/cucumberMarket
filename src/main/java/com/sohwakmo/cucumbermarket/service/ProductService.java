package com.sohwakmo.cucumbermarket.service;

import com.sohwakmo.cucumbermarket.domain.Member;
import com.sohwakmo.cucumbermarket.domain.Product;
import com.sohwakmo.cucumbermarket.dto.ProductCreateDto;
import com.sohwakmo.cucumbermarket.dto.ProductUpdateDto;
import com.sohwakmo.cucumbermarket.repository.MemberRepository;
import com.sohwakmo.cucumbermarket.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;


    public List<Product> read() { // 전체 상품 목록
        log.info("read()");

        return productRepository.findAll();
    }

    public Product read(Integer productNo) { // 상품 조회
        log.info("read(productNo = {})", productNo);

        return productRepository.findById(productNo).get();
    }

    @Transactional
    public Product update(Integer productNo) { // 상품 클릭 수 update
        log.info("update(productNo = {})", productNo);

        Product entity = productRepository.findById(productNo).get();
        log.info("entity = {}", entity);
        entity.update(entity.getClickCount()+1);
        log.info("entity = {}", entity);

        Member member = memberRepository.findById(entity.getMember().getMemberNo()).get();
        log.info("member = {}", member);

        return entity;
    }

    public List<Product> search(String keyword) {
        log.info("search(keyword = {})", keyword);

        List<Product> list = productRepository.findByTitleIgnoreCaseContainingOrContentIgnoreCaseContainingOrderByProductNoDesc(keyword, keyword);
        log.info("list = {}", list);

        return list;
    }


    public Product create(ProductCreateDto dto) { // 상품 등록
        log.info("create(dto={})", dto);

        Product entity = productRepository.save(dto.toEntity());

        return entity;
    }

    @Transactional
    public Integer update(ProductUpdateDto dto) { // 상품 업데이트
        log.info("update(dto={})", dto);

        Product entity = productRepository.findById(dto.getProductNo()).get();
        entity.update(dto.getTitle(), dto.getContent(), dto.getPrice(), dto.getCategory());

        return entity.getProductNo();
    }
}
