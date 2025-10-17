package edu.trincoll.service.report;

import edu.trincoll.repository.MemberRepository;
import org.springframework.stereotype.Component;

/**
 * MemberReportGenerator generates a report of total members.
 * Implements the Strategy pattern for report generation.
 */
@Component
public class MemberReportGenerator implements ReportGenerator {
    private final MemberRepository memberRepository;

    public MemberReportGenerator(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public String generateReport() {
        long totalMembers = memberRepository.count();
        return "Total members: " + totalMembers;
    }
}
