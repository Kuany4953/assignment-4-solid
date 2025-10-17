package edu.trincoll.service.member;

import edu.trincoll.model.Member;
import edu.trincoll.repository.MemberRepository;
import org.springframework.stereotype.Service;

/**
 * MemberService handles all member-related operations.
 * Demonstrates Single Responsibility Principle (SRP) by separating member concerns
 * from book operations and notifications.
 */
@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * Increment the checkout count for a member
     */
    public void incrementCheckoutCount(Member member) {
        member.setBooksCheckedOut(member.getBooksCheckedOut() + 1);
        memberRepository.save(member);
    }

    /**
     * Decrement the checkout count for a member
     */
    public void decrementCheckoutCount(Member member) {
        member.setBooksCheckedOut(member.getBooksCheckedOut() - 1);
        memberRepository.save(member);
    }

    /**
     * Get a member by email
     */
    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
    }

    /**
     * Save a member
     */
    public Member saveMember(Member member) {
        return memberRepository.save(member);
    }
}
