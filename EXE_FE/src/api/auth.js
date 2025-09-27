import { request, setToken } from "./http";
export async function loginApi({ email, password }) {
  const res = await request("/auth/login", {
    method: "POST",
    data: { email, password },
  });
  if (res?.token) setToken(res.token);
  return res;
}
export async function registerApi({ name, email, phone, password }) {
  const res = await request("/auth/register", {
    method: "POST",
    data: { name, email, phone, password },
  });
  if (res?.token) setToken(res.token);
  return res;
}
