package io.github.spartateam6.commercepaymentsystem.domain.paymentmethod.service;

import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import io.github.spartateam6.commercepaymentsystem.domain.member.service.MemberService;
import io.github.spartateam6.commercepaymentsystem.domain.paymentmethod.entity.PaymentMethod;
import io.github.spartateam6.commercepaymentsystem.domain.paymentmethod.repository.PaymentMethodRepository;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.hibernate.internal.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final MemberService memberService;

    @Transactional
    public PaymentMethodResponse register(Long memberId, PaymentMethodRegisterRequest request) {
        Optional<PaymentMethod> existing =
                paymentMethodRepository.findByMember_IdAndIssueId(memberId, request.issueId());
        if (existing.isPresent()) {
            return PaymentMethodResponse.from(existing.get());
        }
        Member member = memberService.getMember(memberId);
        PaymentMethod saved = paymentMethodRepository.save(
                PaymentMethod.builder()
                        .member(member)
                        .billingKey(request.billingkey())
                        .issueId(request.issueId())
                        .cardCompany(request.cardCompany())
                        .build()
        );
        return PaymentMethodResponse.from(saved);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public PaymentMethod getOwned(Long memberId, Long paymentMethodId) {
        PaymentMethod pm = paymentMethodRepository.findByid(paymentMethodId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));
        if(!pm.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        return pm;
    }



}
