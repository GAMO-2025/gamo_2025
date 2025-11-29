(function() {

    if (window.globalCallSocket && window.globalCallSocket.readyState === WebSocket.OPEN) {
        console.log('[Global Call Handler] Socket already connected, skipping');
        return;
    }

    console.log('[Global Call Handler] Initializing...');

    function connect() {
        const userId = window.currentUserId;

        if (!userId) {
            console.warn('[Global Call Handler] User ID not found');
            return;
        }

        console.log('[Global Call Handler] WebSocket 연결 시작 userId=' + userId);

        const socket = new WebSocket(`wss://peaceproject.site/ws/signaling?userId=${userId}`);
        window.globalCallSocket = socket;
        previousUserId = userId;

        socket.onopen = () => {
            console.log('[Global Call Handler] WebSocket 연결됨 (userId=' + userId + ')');
        };

        socket.onmessage = (event) => {
            const data = JSON.parse(event.data);
            console.log('[Global Call Handler] 메시지 수신:', data);

            const messageType = data.code || data.type;
            const payload = data.payload || {};

            switch (messageType) {
                case 'CALL_INCOMING':
                    const callerId = data.sender;
                    console.log('[Global Call Handler] 페이지 이동 전에 socket 저장');
                    // WebSocket을 닫지 말고 전역 변수에 유지
                    window.globalCallSocket = socket;
                    // 페이지 이동
                    window.location.href = `/call/loading?mode=incoming&targetId=${callerId}`;
                    break;

                case 'CALL_ACCEPTED':
                    console.log('[Global Call Handler] 통화 수락됨');
                    const sender = data.sender;
                    const receiver = data.receiver;
                    if (window.currentUserId == sender) {
                        window.location.href = `/call/videocall/${receiver}`;
                    } else if (window.currentUserId == receiver) {
                        window.location.href = `/call/videocall/${sender}`;
                    }
                    break;

                case 'CALL_REJECTED':
                    console.log('[Global Call Handler] 통화 거절됨');
                    window.location.href = `/call`;
                    break;

                case 'CALL_BUSY':
                    console.log('[Global Call Handler] 상대방 통화 중');
                    alert('상대방이 통화 중입니다.');
                    if (window.location.pathname.includes('/call/loading')) {
                        window.location.href = '/call';
                    }
                    break;

                case 'CALL_CANCELLED':
                    console.log('[Global Call Handler] 통화 취소됨');
                    window.location.href = '/call';
                    break;

                case 'OFFER':
                case 'ANSWER':
                case 'CANDIDATE':
                    if (window.handleWebRTCMessage) {
                        window.handleWebRTCMessage(data);
                    }
                    break;
                case 'END_CALL':
                    console.log('[Global Call Handler] 통화 종료');
                    if (window.handleCallEnd) {
                        window.handleCallEnd(payload);
                    }
                    break;

                default:
                    console.log('[Global Call Handler] 처리되지 않은 메시지:', messageType);
            }
        };

        socket.onerror = (error) => {
            console.error('[Global Call Handler] WebSocket 에러:', error);
        };

        socket.onclose = () => {
            console.log('[Global Call Handler] WebSocket 연결 종료 (userId=' + userId + ')');
            window.globalCallSocket = null;

            // 5초 후 재연결 시도
            setTimeout(() => {
                console.log('[Global Call Handler] 재연결 시도...');
                connect();
            }, 5000);
        };

    }

    // 초기 연결 + 주기적 확인
    connect();

    setInterval(() => {
        if (window.currentUserId !== previousUserId) {
            console.log('[Global Call Handler] userId changed, reconnecting...');
            connect();
        }
    }, 1000);
})();
