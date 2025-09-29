import { aiRequest } from "./http";
export async function sendChat({ message, history = [] }) {
  return aiRequest("/chat", { method: "POST", data: { message, history } });
}
