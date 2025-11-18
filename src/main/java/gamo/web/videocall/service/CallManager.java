package gamo.web.videocall.service;

import gamo.web.videocall.domain.CallType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class CallManager {

    private final Map<String, CallSession> activeCalls = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> disconnectTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private static final long CALL_TIMEOUT_SECONDS = 90;
    private static final long DISCONNECT_TIMEOUT_SECONDS = 10;

    public enum Status {
        AVAILABLE,
        RINGING,
        REJECTED,
        MISSED,
        IN_CALL,
        ENDED,
        CANCELLED
    }

    public static class CallSession {
        private final String callerId;
        private final String receiverId;
        private final LocalDateTime requestTime;
        private volatile LocalDateTime connectedTime;
        private volatile Status currentStatus;
        private volatile boolean wasRejected;
        private volatile boolean wasCancelled;
        private volatile boolean wasTimedOut;

        private final ReentrantLock lock = new ReentrantLock();
        private ScheduledFuture<?> timeoutTask;

        public CallSession(String callerId, String receiverId) {
            this.callerId = callerId;
            this.receiverId = receiverId;
            this.requestTime = LocalDateTime.now();
            this.currentStatus = Status.RINGING;
            this.wasRejected = false;
            this.wasCancelled = false;
            this.wasTimedOut = false;
        }

        public void setTimeoutTask(ScheduledFuture<?> task) {
            this.timeoutTask = task;
        }

        public void cancelTimeout() {
            if (timeoutTask != null && !timeoutTask.isDone()) {
                timeoutTask.cancel(false);
                log.debug("[CallSession-cancelTimeout] Timeout cancelled for {} <-> {}", callerId, receiverId);
            }
        }

        public void updateStatus(Status status) {
            lock.lock();
            try {
                Status oldStatus = this.currentStatus;
                this.currentStatus = status;
                log.debug("[CallSession-updateStatus] Status changed: {} -> {} (caller={}, receiver={})",
                        oldStatus, status, callerId, receiverId);
            } finally {
                lock.unlock();
            }
        }

        public void markConnected() {
            lock.lock();
            try {
                if (this.connectedTime != null) {
                    log.warn("[CallSession-markConnected] Already connected at {}, ignoring duplicate call",
                            this.connectedTime);
                    return;
                }
                this.connectedTime = LocalDateTime.now();
                Status oldStatus = this.currentStatus;
                this.currentStatus = Status.IN_CALL;
                cancelTimeout();
                log.info("[CallSession-markConnected] CONNECTED: caller={}, receiver={}, connectedTime={}, status: {} -> {}",
                        callerId, receiverId, this.connectedTime, oldStatus, Status.IN_CALL);
            } finally {
                lock.unlock();
            }
        }

        public void markTimedOut() {
            lock.lock();
            try {
                this.wasTimedOut = true;
                Status oldStatus = this.currentStatus;
                this.currentStatus = Status.MISSED;
                log.warn("[CallSession-markTimedOut] TIMED OUT after {}s: caller={}, receiver={}, status: {} -> {}",
                        CALL_TIMEOUT_SECONDS, callerId, receiverId, oldStatus, Status.MISSED);
            } finally {
                lock.unlock();
            }
        }

        public void markRejected() {
            lock.lock();
            try {
                this.wasRejected = true;
                Status oldStatus = this.currentStatus;
                this.currentStatus = Status.REJECTED;
                log.info("[CallSession-markRejected] REJECTED: caller={}, receiver={}, status: {} -> {}",
                        callerId, receiverId, oldStatus, Status.REJECTED);
            } finally {
                lock.unlock();
            }
        }

        public void markCancelled() {
            lock.lock();
            try {
                this.wasCancelled = true;
                Status oldStatus = this.currentStatus;
                this.currentStatus = Status.CANCELLED;
                log.info("[CallSession-markCancelled] CANCELLED: caller={}, receiver={}, status: {} -> {}",
                        callerId, receiverId, oldStatus, Status.CANCELLED);
            } finally {
                lock.unlock();
            }
        }

        public void markEnded() {
            lock.lock();
            try {
                Status oldStatus = this.currentStatus;
                this.currentStatus = Status.ENDED;
                log.info("[CallSession-markEnded] ENDED: caller={}, receiver={}, status: {} -> {}",
                        callerId, receiverId, oldStatus, Status.ENDED);
            } finally {
                lock.unlock();
            }
        }

        public Duration getCallDuration() {
            if (connectedTime == null) {
                return Duration.ZERO;
            }
            Duration duration = Duration.between(connectedTime, LocalDateTime.now());
            log.debug("[CallSession-getCallDuration] caller={}, receiver={}, connectedTime={}, duration={}s",
                    callerId, receiverId, connectedTime, duration.getSeconds());
            return duration;
        }

        public String getCallerId() { return callerId; }
        public String getReceiverId() { return receiverId; }
        public LocalDateTime getRequestTime() { return requestTime; }
        public LocalDateTime getConnectedTime() { return connectedTime; }
        public Status getCurrentStatus() { return currentStatus; }
        public boolean wasRejected() { return wasRejected; }
        public boolean wasCancelled() { return wasCancelled; }
        public boolean wasTimedOut() { return wasTimedOut; }

        public String getPartner(String userId) {
            if (callerId.equals(userId)) return receiverId;
            if (receiverId.equals(userId)) return callerId;
            return null;
        }
    }

    public synchronized void startCall(String callerId, String receiverId) {
        if (activeCalls.containsKey(callerId) || activeCalls.containsKey(receiverId)) {
            log.warn("[CallManager-startCall] BLOCKED: User already in call - caller={}, receiver={}",
                    callerId, receiverId);
            return;
        }

        CallSession session = new CallSession(callerId, receiverId);
        activeCalls.put(callerId, session);
        activeCalls.put(receiverId, session);

        ScheduledFuture<?> timeoutTask = scheduler.schedule(() -> {
            handleCallTimeout(callerId);
        }, CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        session.setTimeoutTask(timeoutTask);

        log.info("[CallManager-startCall] CREATED SESSION: caller={}, receiver={}, requestTime={}, status=RINGING",
                callerId, receiverId, session.getRequestTime());
        log.info("[CallManager-startCall] TIMEOUT SCHEDULED: timeout will occur in {}s", CALL_TIMEOUT_SECONDS);
    }

    private void handleCallTimeout(String callerId) {
        CallSession session = activeCalls.get(callerId);
        if (session != null && session.getCurrentStatus() == Status.RINGING) {
            log.warn("[CallManager-handleCallTimeout] TIMEOUT TRIGGERED: caller={}, receiver={}, waitTime={}s",
                    callerId, session.getReceiverId(), CALL_TIMEOUT_SECONDS);
            session.markTimedOut();
        } else if (session != null) {
            log.debug("[CallManager-handleCallTimeout] TIMEOUT IGNORED: call already progressed, status={}",
                    session.getCurrentStatus());
        }
    }

    public void acceptCall(String userId) {
        CallSession session = activeCalls.get(userId);
        if (session == null) {
            log.warn("[CallManager-acceptCall] FAILED: No session found for userId={}", userId);
            return;
        }

        String partner = session.getPartner(userId);
        if (session.getConnectedTime() != null) {
            log.warn("[CallManager-acceptCall] DUPLICATE: Call already connected - userId={}, partner={}, connectedTime={}",
                    userId, partner, session.getConnectedTime());
            return;
        }

        session.markConnected();
        log.info("[CallManager-acceptCall] SUCCESS: userId={}, partner={}, both users now IN_CALL",
                userId, partner);
    }

    public void rejectCall(String userId) {
        CallSession session = activeCalls.get(userId);
        if (session == null) {
            log.warn("[CallManager-rejectCall] FAILED: No session found for userId={}", userId);
            return;
        }

        String partner = session.getPartner(userId);
        session.markRejected();
        log.info("[CallManager-rejectCall] SUCCESS: userId={} rejected call from {}", userId, partner);
    }

    public void cancelCall(String userId) {
        CallSession session = activeCalls.get(userId);
        if (session == null) {
            log.warn("[CallManager-cancelCall] FAILED: No session found for userId={}", userId);
            return;
        }

        String partner = session.getPartner(userId);
        session.markCancelled();
        log.info("[CallManager-cancelCall] SUCCESS: userId={} cancelled call to {}", userId, partner);
    }

    public synchronized void endCall(String userId) {
        CallSession session = activeCalls.get(userId);
        if (session == null) {
            log.warn("[CallManager-endCall] FAILED: No session found for userId={}", userId);
            return;
        }

        String callerId = session.getCallerId();
        String receiverId = session.getReceiverId();
        Status statusBeforeEnd = session.getCurrentStatus();

        session.markEnded();
        activeCalls.remove(callerId);
        activeCalls.remove(receiverId);

        log.info("[CallManager-endCall] SESSION REMOVED: caller={}, receiver={}, lastStatus={}, endTime={}",
                callerId, receiverId, statusBeforeEnd, LocalDateTime.now());
        log.info("[CallManager-endCall] ACTIVE SESSIONS NOW: {}", activeCalls.size());
    }

    public void markDisconnected(String userId) {
        CallSession session = activeCalls.get(userId);
        if (session == null) {
            return;
        }

        ScheduledFuture<?> existingTask = disconnectTasks.get(userId);
        if (existingTask != null && !existingTask.isDone()) {
            existingTask.cancel(false);
        }

        ScheduledFuture<?> task = scheduler.schedule(() -> {
            if (activeCalls.containsKey(userId)) {
                endCall(userId);
                log.info("[CallManager-markDisconnected] Session ended after {}s disconnection: userId={}",
                        DISCONNECT_TIMEOUT_SECONDS, userId);
            }
        }, DISCONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        disconnectTasks.put(userId, task);
        log.info("[CallManager-markDisconnected] Disconnect marked, will end in {}s if no reconnect: userId={}",
                DISCONNECT_TIMEOUT_SECONDS, userId);
    }

    public void cancelDisconnectTimeout(String userId) {
        ScheduledFuture<?> task = disconnectTasks.remove(userId);
        if (task != null && !task.isDone()) {
            task.cancel(false);
            log.debug("[CallManager-cancelDisconnectTimeout] Reconnected, timeout cancelled: userId={}", userId);
        }
    }
    public Status getStatus(String userId) {
        CallSession session = activeCalls.get(userId);
        if (session == null) {
            log.debug("[CallManager-getStatus] No active session for userId={}, returning AVAILABLE", userId);
            return Status.AVAILABLE;
        }
        log.debug("[CallManager-getStatus] userId={}, status={}", userId, session.getCurrentStatus());
        return session.getCurrentStatus();
    }

    public String getPartner(String userId) {
        CallSession session = activeCalls.get(userId);
        if (session == null) {
            log.debug("[CallManager-getPartner] No active session for userId={}", userId);
            return null;
        }
        String partner = session.getPartner(userId);
        log.debug("[CallManager-getPartner] userId={}, partner={}", userId, partner);
        return partner;
    }

    public CallSession getCallSession(String userId) {
        CallSession session = activeCalls.get(userId);
        if (session == null) {
            log.debug("[CallManager-getCallSession] No session found for userId={}", userId);
            return null;
        }
        log.debug("[CallManager-getCallSession] userId={}, status={}, connected={}, callDuration={}s",
                userId, session.getCurrentStatus(), session.getConnectedTime() != null,
                session.getCallDuration().getSeconds());
        return session;
    }

    public CallType determineCallType(String userId) {
        CallSession session = activeCalls.get(userId);

        if (session == null) {
            log.warn("[CallManager-determineCallType] MISSED (no session): userId={}", userId);
            return CallType.MISSED;
        }

        String partner = session.getPartner(userId);
        Status currentStatus = session.getCurrentStatus();

        if (session.wasRejected()) {
            log.info("[CallManager-determineCallType] REJECTED: userId={}, partner={}, status={}",
                    userId, partner, currentStatus);
            return CallType.REJECTED;
        }

        if (session.wasCancelled()) {
            log.info("[CallManager-determineCallType] CANCELLED: userId={}, partner={}, status={}",
                    userId, partner, currentStatus);
            return CallType.CANCELLED;
        }

        if (session.wasTimedOut()) {
            log.info("[CallManager-determineCallType] MISSED (timeout): userId={}, partner={}, waitTime={}s",
                    userId, partner, CALL_TIMEOUT_SECONDS);
            return CallType.MISSED;
        }

        if (session.getConnectedTime() != null) {
            Duration duration = session.getCallDuration();
            long seconds = duration.getSeconds();

            log.info("[CallManager-determineCallType] CALL CONNECTED: userId={}, partner={}, connectedTime={}, duration={}s",
                    userId, partner, session.getConnectedTime(), seconds);

            if (seconds >= 5) {
                log.info("[CallManager-determineCallType] COMPLETED: userId={}, partner={}, duration={}s (threshold=5s)",
                        userId, partner, seconds);
                return CallType.COMPLETED;
            } else {
                log.warn("[CallManager-determineCallType] MISSED (too short): userId={}, partner={}, duration={}s (< 5s threshold)",
                        userId, partner, seconds);
                return CallType.MISSED;
            }
        }

        log.warn("[CallManager-determineCallType] MISSED (never connected): userId={}, partner={}, requestTime={}, status={}",
                userId, partner, session.getRequestTime(), currentStatus);
        return CallType.MISSED;
    }

    public CallSessionInfo getCallInfo(String userId) {
        CallSession session = activeCalls.get(userId);
        if (session == null) {
            log.debug("[CallManager-getCallInfo] No session info for userId={}", userId);
            return null;
        }

        CallSessionInfo info = new CallSessionInfo(
                session.getCallerId(),
                session.getReceiverId(),
                session.getCurrentStatus(),
                session.getRequestTime(),
                session.getConnectedTime()
        );

        log.debug("[CallManager-getCallInfo] CallSessionInfo - caller={}, receiver={}, status={}, connected={}",
                info.callerId(), info.receiverId(), info.status(), info.connectedTime() != null);
        return info;
    }

    public record CallSessionInfo(
            String callerId,
            String receiverId,
            Status status,
            LocalDateTime requestTime,
            LocalDateTime connectedTime
    ) {}
}