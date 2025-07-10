import { Client } from '@stomp/stompjs';
import { useEffect } from 'react'; // ✅ BẮT BUỘC


export default function useMailSocket(onNewMail) {
  useEffect(() => {
    console.log("🧩 Initializing WebSocket...");

    const stompClient = new Client({
      brokerURL: 'ws://localhost:8080/ws-mail',
      reconnectDelay: 5000,
      onConnect: () => {
        console.log("✅ WebSocket connected");

        stompClient.subscribe('/topic/mail', (message) => {
          console.log("📩 Received new mail:", message.body);
          const mail = JSON.parse(message.body);
          onNewMail(mail);
        });
      },
      onStompError: (frame) => {
        console.error("❌ Broker error:", frame.headers['message']);
      },
      debug: (str) => console.log("DEBUG:", str),
    });

    stompClient.activate();

    return () => {
      stompClient.deactivate();
    };
  }, [onNewMail]);
}
