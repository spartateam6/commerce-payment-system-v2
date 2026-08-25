package io.github.spartateam6.commercepaymentsystem.domain.paymentmethod.entity;

public enum SubscriptionSatatus {
    ACTIVE{
        @Override public boolean canTransitTo(SubscriptionSatatus target) {
            return target == CANCELLED || target == PAUSED;
        }
    },
    PAUSED{
        @Override public boolean canTransitTo(SubscriptionSatatus target) {
            return target == ACTIVE || target == CANCELLED;
        }
    },
    CANCELLED{
        @Override public boolean canTransitTo(SubscriptionSatatus target) {
            return false;
        }
    };

    public abstract boolean canTransitTo(SubscriptionSatatus target);
}
