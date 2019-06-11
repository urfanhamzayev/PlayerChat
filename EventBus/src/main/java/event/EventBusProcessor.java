package event;


import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EventBusProcessor {

    private  static EventBus eventBus;

    static class Player implements EventBus.Listener {

        int idx = 0;
        final String name;

        Player(String name) {
            this.name = name;
        }

        private final Predicate<EventBus.Listener> selfExcludePredicate = (listener) ->
                listener.equals(Player.this);

        @Override
        public void onMessage(String message) {

            if (idx>10)
                return;
            message=message+idx;
            idx++;

            System.out.println(name + " " + message);

            eventBus.post(message, selfExcludePredicate);
        }
    }

    private static Player firstPlayer;
    private static Player secondPlayer;
    private static ExecutorService executor;

    public static void main(String[] args) throws InterruptedException {
        firstPlayer = new Player("initiator");
        secondPlayer = new Player("second");

        executor = Executors.newFixedThreadPool(2);

        eventBus = new EventBus(executor);
        // eventBus = new EventBus(Executors.newSingleThreadExecutor());
        eventBus.register(firstPlayer);
        eventBus.register(secondPlayer);
        eventBus.post("Hello");

        executor.awaitTermination(1, TimeUnit.SECONDS);

        eventBus.unregister(firstPlayer);
        eventBus.unregister(secondPlayer);
    }




    static class EventBus {


        interface Listener {

            void onMessage(String message);
        }


        EventBus(Executor executor) {
            this.dispatcher = new EventBus.PerThreadQueuedUnicastDispatcher();
            this.executor = executor;
        }

        class Subscriber {
            private final EventBus bus;
            private final EventBus.Listener target;
            private final Executor executor;

            Subscriber(EventBus bus, EventBus.Listener target) {
                this.bus = bus;
                this.target = target;
                this.executor = bus.getExecutor();
            }

            final void dispatchEvent(final String event) {
                synchronized (this) {
                    executor.execute(
                            () -> {
                                try {
                                    //  System.out.println("dispatch event {}. :  "+ event);
                                    target.onMessage(event);
                                } catch (Exception e) {
                                    System.out.println("Cannot dispatch event {}." + event+ e.getCause());


                                }
                            });
                }
            }

            Object getTarget() {
                return target;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                EventBus.Subscriber that = (EventBus.Subscriber) o;
                return bus.equals(that.bus) &&
                        target.equals(that.target) &&
                        executor.equals(that.executor);
            }

            @Override
            public int hashCode() {
                return Objects.hash(bus, target, executor);
            }
        }

        static abstract class Dispatcher {
            abstract void dispatch(String event, Iterator<EventBus.Subscriber> subscribers);
        }


        private static final class PerThreadQueuedUnicastDispatcher extends EventBus.Dispatcher {

            private final ThreadLocal<Queue<EventBus.PerThreadQueuedUnicastDispatcher.Event>> queue =
                    ThreadLocal.withInitial(ArrayDeque::new);

            private final ThreadLocal<Boolean> dispatching = ThreadLocal.withInitial(() -> false);

            @Override
            void dispatch(String event, Iterator<EventBus.Subscriber> subscribers) {
                Queue<EventBus.PerThreadQueuedUnicastDispatcher.Event> queueForThread = queue.get();
                queueForThread.offer(new EventBus.PerThreadQueuedUnicastDispatcher.Event(event, subscribers));

                if (!dispatching.get()) {
                    dispatching.set(true);
                    try {
                        EventBus.PerThreadQueuedUnicastDispatcher.Event nextEvent;
                        while ((nextEvent = queueForThread.poll()) != null) {
                            if (nextEvent.subscribers.hasNext()) {
                                nextEvent.subscribers.next().dispatchEvent(nextEvent.event);
                            }
                        }
                    } finally {
                        dispatching.remove();
                        queue.remove();
                    }
                }
            }


            private static final class Event {
                private final String event;
                private final Iterator<EventBus.Subscriber> subscribers;

                private Event(String event, Iterator<EventBus.Subscriber> subscribers) {
                    this.event = event;
                    this.subscribers = subscribers;
                }
            }
        }


        private final EventBus.Dispatcher dispatcher;
        private final Executor executor;
        private final CopyOnWriteArraySet<EventBus.Subscriber> subscribers = new CopyOnWriteArraySet<>();

        Executor getExecutor() {
            return executor;
        }

        void post(String message) {
            post(message, null);
        }

        void post(String message, Predicate<EventBus.Listener> excludes) {
            Predicate<EventBus.Listener> predicate = (null == excludes) ? (s) -> true : excludes.negate();

            Set<EventBus.Subscriber> subscribers = this.subscribers.stream()
                    .filter(subscriber -> predicate.test(subscriber.target))
                    .collect(Collectors.toSet());

            dispatcher.dispatch(message, subscribers.iterator());
        }

        void register(EventBus.Listener listener) {
            subscribers.add(new EventBus.Subscriber(this, listener));
        }


        void unregister(EventBus.Listener listener) {
            Set<EventBus.Subscriber> subscribersToRemove =
                    subscribers.stream()
                            .filter((s) -> listener.equals(s.getTarget()))
                            .collect(Collectors.toSet());

            subscribers.removeAll(subscribersToRemove);
        }
    }
}