package io.github.spartateam6.commercepaymentsystem.domain.paymentmethod.entity;

public enum SubscriptionInvoiceStatus {
    PENDING{
        @Override public boolean canTransitTo(SubscriptionInvoiceStatus target){
            return target == SUCCEEDED || target == FAILED;
        }
    },
    SUCCEEDED{
        @Override public boolean canTransitTo(SubscriptionInvoiceStatus target){
            return false;
        }
    },
    FAILED{
        @Override public boolean canTransitTo(SubscriptionInvoiceStatus target){
            return false;
        }
    };

    public abstract boolean canTransitTo(SubscriptionInvoiceStatus target);

}
