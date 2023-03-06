package com.sohwakmo.cucumbermarket.service;

import com.sohwakmo.cucumbermarket.domain.ChatRoom;
import com.sohwakmo.cucumbermarket.domain.Member;
import com.sohwakmo.cucumbermarket.domain.Message;
import com.sohwakmo.cucumbermarket.repository.ChatRoomRepository;
import com.sohwakmo.cucumbermarket.repository.MemberRepository;
import com.sohwakmo.cucumbermarket.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private  final MessageRepository messageRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public List<ChatRoom> getAllChatList(Integer memberNo) {
        Member member = memberRepository.findById(memberNo).get();
        log.info(member.toString());
        List<ChatRoom> list = chatRoomRepository.findByRoomIdOrMemberMemberNo(member.getNickname(),memberNo);
        
        // 사용자의 이미지 설정(프로필 사진이 변경 되었을 수도 있어서 애초에 채팅방을 생성할때 넣어주면 안됨)
        for(ChatRoom c : list){
            Member chatRoomMember = memberRepository.findByNickname(c.getRoomId()).orElse(null);
            c.setUserImage(chatRoomMember.getUserImgUrl());
        }
        
        return list;
    }
    
    @Transactional
    public ChatRoom saveAndGetChatRoom(String roomId, Integer memberNo,String nickname) {
        Member member1 = memberRepository.findByNickname(roomId).orElse(null);
        Member member2 = memberRepository.findById(memberNo).orElse(null);

        ChatRoom chatRoom = chatRoomRepository.findByRoomIdAndMemberMemberNo(roomId, memberNo);
        if(chatRoom == null){
            chatRoom = chatRoomRepository.findByRoomIdAndMemberMemberNo(member2.getNickname(), member1.getMemberNo());
            if(chatRoom == null){ // 여기서는 채팅방을 만들어야함.
                ChatRoom newChatRoom = new ChatRoom(roomId, member2, "nobody", 0);
                chatRoomRepository.save(newChatRoom);
                return newChatRoom;
            }
        }// 채팅방을 찾거나 새로만듬

        Message message = messageRepository.findByMessageNumAndRoomIdOrderByIdDesc(chatRoom.getMember().getMemberNo(), chatRoom.getRoomId()).stream().findAny().get();
        if (!message.getWriter().equals(nickname)) {
            chatRoom.setUnReadMessages(0);
        }

        if (chatRoom.getLeavedUser().equals(member2.getNickname())){ // 한번 채팅방을 나갔다가 다시 들어온 경우.
            chatRoom.setLeavedUser("nobody");
        }
        return chatRoom;
    }



    @Transactional
    public void saveMessage(Message message) {
        Message message1 = messageRepository.save(message);
        log.info(message1.toString());
        Member member1 = memberRepository.findByNickname(message.getRoomId()).orElse(null);
        Member member2 = memberRepository.findById(message.getMessageNum()).orElse(null);

        ChatRoom chatRoom = chatRoomRepository.findByRoomIdAndMemberMemberNo(member1.getNickname(), member2.getMemberNo());
        if(chatRoom == null){
            chatRoom = chatRoomRepository.findByRoomIdAndMemberMemberNo(member2.getNickname(), member1.getMemberNo());
        }
        chatRoom.setUnReadMessages(chatRoom.getUnReadMessages()+1);
        chatRoom.setLeavedUser("nobody");
    }

    @Transactional
    public List<Message> getAllMessages(ChatRoom chatRoom) {
        List<Message> message = messageRepository.findByRoomIdAndMessageNumOrderById(chatRoom.getRoomId(),chatRoom.getMember().getMemberNo());
        if(message==null){
            Message newMessage = new Message();
            newMessage.setRoomId(chatRoom.getRoomId());
            messageRepository.save(newMessage);
            log.info("massageSave={}",newMessage);
        }
        List<Message> messageList = messageRepository.findByRoomIdAndMessageNumOrderById(chatRoom.getRoomId(), chatRoom.getMember().getMemberNo()).stream().toList();
        for(Message m : messageList){
            String sendTime = m.getSendTime();
            if (sendTime.length() == 24) {
                sendTime = sendTime.substring(sendTime.length() - 10, sendTime.length() - 3);
                m.setSendTime(sendTime);
            }else if(sendTime.length()==25){
                sendTime = sendTime.substring(sendTime.length() - 11, sendTime.length() - 3);
                m.setSendTime(sendTime);
            }
        }
        log.info("messageList={}",messageList);
        return messageList;
    }

    public String getLoginedName(Integer memberNo) {
        Member member = memberRepository.findById(memberNo).orElse(null);
        return member.getNickname();
    }

    /**
     * 가장 최근에 보낸 메세지를 가져온다. desc 정렬로 가장 위에 있는 메세지가 가장 최근에 보낸 메세지.
     * @param memberNo
     * @return 가장 최근에 보낸 메세지를 리턴한다.
     */
    public String getRecentMessage(String roomId,Integer memberNo) {
        List<Message> messages = messageRepository.findByMessageNumAndRoomIdOrderByIdDesc(memberNo,roomId);
        if(messages.size()!=0){
            String recentMessage = String.valueOf(messages.get(0).getMessage());
            if(recentMessage.length()>11){
                recentMessage = recentMessage.substring(0, 11)+"...";
            }
            return recentMessage;
        }else{
            return null;
        }
    }

    @Transactional
    /**
     * 채팅방에 누가 제일 마지막에 들어갔는지 세팅한다.
     * @param roomId 채팅방이름
     * @param nickname 컬럼에 채워질 이름
     */
    public void setLastCheckUser(ChatRoom chatRoom,String nickname) {
        chatRoom.setLastEnterName(nickname);
    }


    @Transactional
    public void deleteChatRoom(String roomId, String nickname,Integer memberNo) {
        Member member = memberRepository.findByNickname(nickname).get();
        Member loginUser = memberRepository.findById(memberNo).get();
        ChatRoom chatRoom = chatRoomRepository.findByRoomIdAndMemberMemberNo(roomId, member.getMemberNo());
        
        if(!chatRoom.getLeavedUser().equals("nobody")){ // nobody가 아니면 즉, 한명이라도 채팅방을 나가면 이제 채팅방을 삭제한다.
            chatRoomRepository.delete(chatRoom);
            List<Message> deleteMessageList1 = messageRepository.findByMessageNumAndRoomIdOrderByIdDesc(loginUser.getMemberNo(), roomId);
            List<Message> deleteMessageList2 = messageRepository.findByMessageNumAndRoomIdOrderByIdDesc(member.getMemberNo(), roomId);
            messageRepository.deleteAllInBatch(deleteMessageList1);
            messageRepository.deleteAllInBatch(deleteMessageList2);
        }else{ // 한쪽만 나갔을 경우 나간사람 닉네임을 컬럼에 저장하고 안읽은 메세지도 0으로 읽음 처리 해준다.
            chatRoom.setLeavedUser(loginUser.getNickname());
            chatRoom.setUnReadMessages(0);
        }
    }

    /**
     * 현재 로그인 되어있는 유저의 정보를 회원번호로 가져온다.
     * @param memberNo 회원번호
     * @return 로그인 되어있는 유저의 객체
     */
    public Member getLoginedMember(Integer memberNo) {
        return memberRepository.findById(memberNo).get();
    }

    @Transactional
    public void setMessages(ChatRoom c,String memberNickname) {
        c.setMessage(getRecentMessage(c.getRoomId(),c.getMember().getMemberNo()));
    }


}
