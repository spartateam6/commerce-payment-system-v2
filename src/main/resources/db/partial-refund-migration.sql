-- Issue #4 부분 환불 배포 전 MySQL에 1회 적용한다.
-- 운영 프로필은 ddl-auto=validate이므로 애플리케이션보다 먼저 실행해야 한다.

ALTER TABLE payment
    ADD COLUMN point_used_amount INT NOT NULL DEFAULT 0,
    ADD COLUMN earned_point_amount INT NOT NULL DEFAULT 0;

UPDATE payment p
JOIN orders o ON o.order_id = p.order_id
SET p.point_used_amount = o.point_used_amount,
    p.earned_point_amount = FLOOR(p.pg_amount / 100);

ALTER TABLE order_items
    ADD COLUMN refunded_quantity INT NOT NULL DEFAULT 0;

ALTER TABLE refund
    DROP INDEX uk_refund_payment,
    ADD COLUMN request_key VARCHAR(100),
    ADD COLUMN total_refund_amount INT NOT NULL DEFAULT 0,
    ADD COLUMN earned_point_revoke_amount INT NOT NULL DEFAULT 0,
    ADD COLUMN refund_type VARCHAR(20) NOT NULL DEFAULT 'FULL',
    ADD COLUMN gateway_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN pg_cancel_requested_at DATETIME(6),
    ADD COLUMN pg_cancel_completed_at DATETIME(6),
    ADD COLUMN retry_count INT NOT NULL DEFAULT 0;

UPDATE refund
SET request_key = CONCAT('legacy-refund-', id),
    total_refund_amount = point_refund_amount + pg_refund_amount,
    gateway_status = CASE WHEN pg_refund_amount = 0 THEN 'NOT_REQUIRED' ELSE 'SUCCEEDED' END;

ALTER TABLE refund
    MODIFY request_key VARCHAR(100) NOT NULL,
    ADD CONSTRAINT uk_refund_request_key UNIQUE (request_key);

CREATE TABLE refund_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    refund_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price_snapshot INT NOT NULL,
    total_refund_amount INT NOT NULL,
    point_refund_amount INT NOT NULL,
    pg_refund_amount INT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_refund_item_refund_order_item UNIQUE (refund_id, order_item_id),
    CONSTRAINT fk_refund_item_refund FOREIGN KEY (refund_id) REFERENCES refund (id),
    CONSTRAINT fk_refund_item_order_item FOREIGN KEY (order_item_id) REFERENCES order_items (order_item_id)
);

ALTER TABLE point_transactions
    DROP INDEX uk_point_transactions_member_payment_type,
    ADD COLUMN refund_id BIGINT,
    ADD CONSTRAINT uk_point_transactions_refund_type UNIQUE (refund_id, transaction_type),
    ADD CONSTRAINT fk_point_transactions_refund FOREIGN KEY (refund_id) REFERENCES refund (id);
