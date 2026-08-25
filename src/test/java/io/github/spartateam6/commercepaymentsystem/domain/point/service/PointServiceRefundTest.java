package io.github.spartateam6.commercepaymentsystem.domain.point.service;

import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import io.github.spartateam6.commercepaymentsystem.domain.member.service.MemberService;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import io.github.spartateam6.commercepaymentsystem.domain.point.entity.PointTransaction;
import io.github.spartateam6.commercepaymentsystem.domain.point.entity.PointTransactionType;
import io.github.spartateam6.commercepaymentsystem.domain.point.repository.PointTransactionRepository;
import io.github.spartateam6.commercepaymentsystem.domain.refund.entity.Refund;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundType;
import io.github.spartateam6.commercepaymentsystem.dummy.MemberFixture;
import io.github.spartateam6.commercepaymentsystem.dummy.PaymentFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PointServiceRefundTest {
    @InjectMocks PointService pointService;
    @Mock MemberService memberService;
    @Mock PointTransactionRepository pointTransactionRepository;

    @Test
    void 적립포인트가_부족해도_전액회수하여_음수잔액을_허용한다() {
        Member member = MemberFixture.members.get(0);
        ReflectionTestUtils.setField(member, "pointBalance", 0);
        Payment payment = PaymentFixture.createPayment();
        ReflectionTestUtils.setField(payment, "id", 10L);
        Refund refund = Refund.complete(payment, "request-1", "환불",
                0, 30_000, 300, RefundType.FULL);
        ReflectionTestUtils.setField(refund, "id", 20L);
        given(memberService.getMemberForUpdate(1L)).willReturn(member);

        pointService.applyRefundPoint(1L, payment, refund, 0, 300);

        assertEquals(-300, member.getPointBalance());
        ArgumentCaptor<PointTransaction> captor = ArgumentCaptor.forClass(PointTransaction.class);
        then(pointTransactionRepository).should().save(captor.capture());
        assertEquals(PointTransactionType.EARN_REVOKE, captor.getValue().getTransactionType());
        assertEquals(-300, captor.getValue().getAmount());
    }
}
