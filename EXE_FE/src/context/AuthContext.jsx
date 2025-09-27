import { createContext, useContext, useEffect, useState } from 'react'
import { loginApi, registerApi } from '../api/auth'
import { setToken } from '../api/http'
const AuthContext = createContext()
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem('gaosach:user')
    return raw ? JSON.parse(raw) : null
  })
  const [points, setPoints] = useState(() => {
    const raw = localStorage.getItem('gaosach:points')
    return raw ? Number(raw) : 1250
  })
  useEffect(() => { localStorage.setItem('gaosach:user', JSON.stringify(user)) }, [user])
  useEffect(() => { localStorage.setItem('gaosach:points', String(points)) }, [points])
  const login = async (email, password) => {
    const { user: u, token } = await loginApi({ email, password })
    setUser(u); if (token) setToken(token); setPoints(p => p + 10)
  }
  const register = async ({ name, email, phone, password }) => {
    const { user: u, token } = await registerApi({ name, email, phone, password })
    setUser(u); if (token) setToken(token); setPoints(p => p + 100)
  }
  const logout = () => { setToken(null); setUser(null) }
  return (
    <AuthContext.Provider value={{ user, setUser, points, setPoints, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}
export const useAuth = () => useContext(AuthContext)