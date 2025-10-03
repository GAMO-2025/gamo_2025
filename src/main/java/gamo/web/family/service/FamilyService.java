package gamo.web.family.service;

import gamo.web.family.repository.FamilyRepository;
import gamo.web.member.domain.Family;
import gamo.web.member.domain.Member;
import gamo.web.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FamilyService {

    @Autowired
    FamilyRepository familyRepository;
    MemberRepository memberRepository;

    public List<Member> allMember(Long id) { //가족 구성원 조회
        return familyRepository.findAll(id);
    }

    public void create(Member member) { //가족 생성
        //멤버의 가족아이디가 null인지 검사
        if(member.getFamily() == null)
            throw new IllegalStateException("이미 가입한 가족 그룹이 존재합니다");

        //family 테이블에 값 생성
        UUID uuid = UUID.randomUUID();
        Family family = Family.builder().familyCode(uuid.toString()).build();
        familyRepository.save(family);

        //멤버의 가족아이디 설정
        Member starter = memberRepository.findById(member.getId())
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 회원입니다."));
        starter.setFamily(family);
    }

    public void invite() {//가족 초대
        //링크생성
    }

    public void join() {// 가족 가입

    }

    public void cancel() {//가족 탈퇴

    }

    public void nickname() { //가족 닉네임 설정

    }
}


//가족 생성
//    public void create(Member member) {
//        //고유코드 생성 -> 가족 테이블에 엔티티 넣음 -> 멤버의 가족id(외래키)에 값 설정
//
//    }
//가족 생성
//가족 구성원 가입
//가족 초대
//가죽 구성원 조회
