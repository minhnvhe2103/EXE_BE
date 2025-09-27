import { useState } from "react";
import ChatWidget from "./ChatWidget";

export default function ChatBubble() {
  const [open, setOpen] = useState(false);

  return (
    <>
      {/* Bong bóng Chat AI */}
      <button
        className="chat-bubble"
        aria-label="Mở chat AI"
        onClick={() => setOpen(true)}
        style={{
          right: "20px",
          bottom: "20px",
          backgroundColor: "#4caf50",
        }}
      >
        <i className="fas fa-message" />
      </button>

      {/* Bong bóng Zalo */}
      <a
        href="https://zalo.me/yourZaloID" // đổi thành link zalo thật
        target="_blank"
        rel="noopener noreferrer"
        className="chat-bubble"
        aria-label="Liên hệ Zalo"
        style={{
          right: "20px",
          bottom: "80px",
          backgroundColor: "white", // nền trắng để icon rõ hơn
        }}
      >
        <img
          src="/images/zalo.png"
          alt="Zalo"
          style={{ width: "28px", height: "28px" }}
        />
      </a>

      {/* Bong bóng Facebook */}
      <a
        href="https://www.facebook.com/profile.php?id=61580996324879" // đổi thành link facebook thật
        target="_blank"
        rel="noopener noreferrer"
        className="chat-bubble"
        aria-label="Liên hệ Facebook"
        style={{
          right: "20px",
          bottom: "140px",
          backgroundColor: "white", // nền trắng để icon rõ
        }}
      >
        <img
          src="/images/fb.png"
          alt="Facebook"
          style={{ width: "28px", height: "28px" }}
        />
      </a>

      {open && <ChatWidget onClose={() => setOpen(false)} />}
    </>
  );
}
