package io.smallrye.reactive.operators.multi;

import java.util.function.Predicate;

import org.reactivestreams.Subscriber;

import io.smallrye.reactive.Multi;
import io.smallrye.reactive.helpers.ParameterValidation;

/**
 * Filters out items from the upstream that do <strong>NOT</strong> pass the given filter.
 *
 * @param <T> the type of item
 */
public class MultiFilterOp<T> extends AbstractMultiWithUpstream<T, T> {

    private final Predicate<? super T> predicate;

    public MultiFilterOp(Multi<? extends T> upstream, Predicate<? super T> predicate) {
        super(upstream);
        this.predicate = ParameterValidation.nonNull(predicate, "predicate");
    }

    @Override
    public void subscribe(Subscriber<? super T> downstream) {
        upstream.subscribe(new MultiFilterSubscriber<>(downstream, predicate));
    }

    static final class MultiFilterSubscriber<T> extends MultiOperatorSubscriber<T, T> {

        private final Predicate<? super T> predicate;

        MultiFilterSubscriber(Subscriber<? super T> downstream, Predicate<? super T> predicate) {
            super(downstream);
            this.predicate = predicate;
        }

        @Override
        public void onNext(T t) {
            if (isDone()) {
                return;
            }

            boolean passed;
            try {
                passed = predicate.test(t);
            } catch (Throwable exception) {
                onError(exception);
                return;
            }

            if (passed) {
                downstream.onNext(t);
            } else {
                request(1);
            }
        }
    }
}
