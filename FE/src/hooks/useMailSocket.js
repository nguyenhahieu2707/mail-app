import { Client } from '@stomp/stompjs';
import { useEffect } from 'react';

export default function useMailSocket(onNewMail) {
  useEffect(() => {
    const token = localStorage.getItem("accessToken"); // ⚠️ Sửa dòng này
    if (!token) {
      console.warn("⚠️ No JWT token found in localStorage, cannot connect WebSocket");
      return;
    }

    const socketUrl = `${window.location.origin.replace(/^http/, 'ws')}/ws-mail?token=${token}`;

    const stompClient = new Client({
      brokerURL: socketUrl,
      reconnectDelay: 5000,
      onConnect: () => {
        console.log("✅ WebSocket connected with token");

        stompClient.subscribe('/user/queue/mail', (message) => {
          console.log("📩 Received new mail:", message.body);
          const mail = JSON.parse(message.body);
          onNewMail(mail);
          onMessage(payload);
        });
      },
      onStompError: (frame) => {
        console.error("❌ STOMP error:", frame.headers['message']);
      },
    });

    stompClient.activate();

    return () => {
      stompClient.deactivate();
    };
  }, [onNewMail]);
}
