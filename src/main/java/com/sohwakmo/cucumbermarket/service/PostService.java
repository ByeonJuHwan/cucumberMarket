package com.sohwakmo.cucumbermarket.service;

import com.sohwakmo.cucumbermarket.domain.Member;
import com.sohwakmo.cucumbermarket.domain.Post;
import com.sohwakmo.cucumbermarket.domain.Reply;
import com.sohwakmo.cucumbermarket.dto.PostCreateDto;
import com.sohwakmo.cucumbermarket.dto.PostReadDto;
import com.sohwakmo.cucumbermarket.dto.PostUpdateDto;
import com.sohwakmo.cucumbermarket.repository.MemberRepository;
import com.sohwakmo.cucumbermarket.repository.PostRepository;

import com.sohwakmo.cucumbermarket.repository.ReplyRepository;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    private  final ReplyRepository replyRepository;

    public List<Post> readByClickCountDesc(){
        log.info("readByClickCountDesc()");
        return postRepository.findByOrderByClickCountDescPostNoDesc();
    }

    @Transactional(readOnly = true)
    /**
     * 내용, 제목으로 검색해서 결과를 페이지로 가져오기
     * @param searchText 검색 내용
     * @return 결과 페이지들을 리턴
     */
    public List<PostReadDto> searchPost(String searchText,String address){
        String[] memberAddressArr = address.split(" ");
        address = memberAddressArr[0];
        List<Post> postList =  postRepository.findByTitleIgnoreCaseContainingOrContentIgnoreCaseContainingOrMemberNicknameIgnoreCaseContainingOrderByPostNoDesc(searchText,searchText,searchText);
        List<PostReadDto> list = new ArrayList<>();
        for(Post p : postList){
            if(address.equals("전국")){
                Member member = memberRepository.findById(p.getMember().getMemberNo()).get();
                PostReadDto dto = PostReadDto.builder()
                        .postNo(p.getPostNo()).title(p.getTitle()).writer(member.getNickname()).createdTime(p.getCreatedTime()).clickCount(p.getClickCount())
                        .build();
                list.add(dto);
            }else {
                String memberAddress = p.getMember().getAddress();
                String[] memberDetailAddress = memberAddress.split(" ");
                if (memberDetailAddress[0].equals(address)) {
                    Member member = memberRepository.findById(p.getMember().getMemberNo()).get();
                    PostReadDto dto = PostReadDto.builder()
                            .postNo(p.getPostNo()).title(p.getTitle()).writer(member.getNickname()).createdTime(p.getCreatedTime()).clickCount(p.getClickCount())
                            .build();
                    list.add(dto);
                }
            }
        }
        return list;
    }

    /**
     * 메서드 searchPost() 로 받아온 list 를 리턴
     * @param searchText 검색어
     * @param memberAddress 로그인한 유저의 주소
     * @return 검색 결과가 1개라도 있으면 1 없으면 0
     */
    public int checkSearchResult(String searchText, String memberAddress) {
        List<PostReadDto> list = searchPost(searchText, memberAddress);
        return list.size() == 0 ? 0 : 1;
    }

    /**
     * modify 에서 detail 페이지로 다시 넘어오는경우 조회수가 2번 늘어나는 것을 방지하고자 -1 을해준다.
     * @param postNo 게시글 id
     * @param clickCount 조회수
     */
    public int setClickCount(Integer postNo, Integer clickCount) {
        return findPostByPostNo(postNo).getClickCount() - 1;
    }

    /**
     * 게시물 리시트를 Page 로 변환하는 작업
     * @param pageable
     * @param list 검색된 게시물 리스트
     * @return list -> Page 로 변환된 결과
     */
    public Page listToPage(Pageable pageable, List<PostReadDto> list) {
        final int start = (int)pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), list.size());
        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }

    /**
     * 변환 받은 페이지에서 시작페이지와 마지막 페이지를 계산해서 리턴
     *
     * @param page 구해야하는 페이지
     * @param list page 로 변경받기 전의 리스트
     * @return [0] 에는 시작 페이지, [1] 에는 마지막 페이지를 넣는다.
     */
    public List<Integer> setStartEndPage(Page page, List<PostReadDto> list) {
        List<Integer> pageList = new ArrayList<>();
        int startPage=1;
        int endPage=1;
        if(list.size()!=0) {
            if(page.getTotalPages()<11){
                endPage = page.getTotalPages();
            }else{
                if(page.getPageable().getPageNumber()<10){
                    endPage=10;
                }else{
                    startPage=(page.getPageable().getPageNumber()/10)*10+1;
                    endPage = Math.min(page.getTotalPages(), (page.getPageable().getPageNumber() / 10) * 10 + 10);
                }
            }
        }
        pageList.add(startPage);
        pageList.add(endPage);
        return pageList;
    }

    @Transactional
    public Post findPostByIdandUpdateClickCount(Integer id, Integer clickCount) {
         Post post = postRepository.findById(id).orElse(null);
         post = post.plusClickCount(clickCount + 1);
         return post;
    }

    /**
     * 글 작성시 post 객체를 생성해서 DB에 저장
     * @param dto
     * @param member
     * @param files
     */
    public void insertPost(PostCreateDto dto, Member member, List<MultipartFile> files) throws Exception {
        Post post = PostCreateDto.builder()
                .title(dto.getTitle()).content(dto.getContent()).clickCount(dto.getClickCount()).member(member).build().toEntity();
        createPost(post,files);
    }

    /**
     * 사진을 넣고 작성을한경우
     * @param post  제목, 내용
     * @param files 사진
     * @throws Exception 사진이 있냐 없냐 에따라 exception 발생
     */
    public void createPost(Post post, List<MultipartFile> files)throws Exception{
        if (files.stream().findFirst().isPresent()) {
            for (MultipartFile multipartFile : files) {
                String fileName = saveImage(multipartFile);
                post.saveImage(fileName);
            }
        }
        postRepository.save(post);
    }
    public void deletePost(Integer id) {
        postRepository.deleteById(id);
    }

    public Post findPostByPostNo(Integer postNo) {
        return postRepository.findById(postNo)
                .orElseThrow(() -> new EntityNotFoundException("게시물이 없습니다 : " + postNo));
    }

    @Transactional
    public Integer modifyPost(PostUpdateDto dto) {
        Post post = postRepository.findById(dto.getPostNo()).get();
        Post newPost = post.update(dto.getTitle(), dto.getContent());
        log.info(newPost.toString());
        return post.getPostNo();
    }

// ---------------------RestController 에서 온 api

    @Transactional
    public String insertImage(Integer postNo, MultipartFile data)throws Exception {
        String fileName = saveImage(data);
        Post post = postRepository.findById(postNo)
                .orElseThrow(() -> new EntityNotFoundException("게시물이 없습니다 : " + postNo));
        return post.saveImage(fileName);
    }

    @Transactional
    public String modifyImage01(Integer postNo, MultipartFile data)throws Exception {
        String fileName = saveImage(data);
        Post post = postRepository.findById(postNo)
                .orElseThrow(() -> new EntityNotFoundException("게시물이 없습니다 : " + postNo));
        extractImage(post.getImageName01());
        post.saveImage01NameAndUrl(fileName);
        return "files/"+fileName;
    }

    @Transactional
    public String modifyImage02(Integer postNo, MultipartFile data)throws Exception {
        String fileName = saveImage(data);
        Post post = postRepository.findById(postNo)
                .orElseThrow(() -> new EntityNotFoundException("게시물이 없습니다 : " + postNo));
        extractImage(post.getImageName02());
        post.saveImage02NameAndUrl(fileName);
        return "files/"+fileName;
    }

    /**
     * 사진 삭제를 누를경우 사진 삭제를 바로바로 해준다.
     * @param imageSrc
     * @return 몇번 사진을 삭제했는지 알려준다.
     * @throws Exception
     */
    @Transactional
    public String checkImageNumAndDeleteImage(Integer postNo,String imageSrc) throws Exception{
        Post post = postRepository.findById(postNo)
                .orElseThrow(() -> new EntityNotFoundException("게시물이 없습니다 : " + postNo));
        if (post.getImageName01().equals(imageSrc)) {
            extractImage(imageSrc);
            post.saveImage01NameAndUrl("");
        }else{
            extractImage(imageSrc);
            post.saveImage02NameAndUrl("");
        }
        return null;
    }
    private  String saveImage(MultipartFile files) throws IOException {
        String projectPath = System.getProperty("user.dir") + "/src/main/resources/static/files";

        String fileName = files.getOriginalFilename();

        File saveFile = new File(projectPath, fileName);

        files.transferTo(saveFile);
        return fileName;
    }

    /**
     * static  폴더 안에 있는 사진 경로를 찾아내서 삭제
     * @param imageSrc 전달받은 이미지 경로
     * @throws IOException
     */
    private void extractImage(String imageSrc) throws IOException {
        // 경로는 능동적으로 변경
        Path filePath = Paths.get(System.getProperty("user.dir")+"/src/main/resources/static/files/" + imageSrc);
        Files.delete(filePath);
    }
}


