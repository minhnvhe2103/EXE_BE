import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { useUi } from '../context/UiContext'
export default function AuthModal({ onClose }){
  const [mode, setMode] = useState('login')
  const { login, register } = useAuth()
  const { notify } = useUi()
  const [loading, setLoading] = useState(false)
  const close = ()=> onClose?.()
  const onLogin = async (e)=>{
    e.preventDefault(); setLoading(true)
    const email = e.target.elements.email.value; const password = e.target.elements.password.value
    try{ await login(email, password); notify('Đăng nhập thành công!'); close() }
    catch(err){ notify(`Đăng nhập thất bại: ${err.message}`) }
    finally{ setLoading(false) }
  }
  const onRegister = async (e)=>{
    e.preventDefault(); setLoading(true)
    const name = e.target.elements.name.value; const email = e.target.elements.email.value
    const phone = e.target.elements.phone.value; const password = e.target.elements.password.value
    const confirm = e.target.elements.confirm.value
    if (password !== confirm) { setLoading(false); return notify('Mật khẩu xác nhận không khớp') }
    try{ await register({ name, email, phone, password }); notify('Đăng ký thành công! Chào mừng bạn!'); close() }
    catch(err){ notify(`Đăng ký thất bại: ${err.message}`) }
    finally{ setLoading(false) }
  }
  return (
    <div className="modal-overlay active" onClick={close}>
      <div className="modal-content" onClick={(e)=>e.stopPropagation()}>
        <button className="modal-close" onClick={close}>&times;</button>
        {mode === 'login' ? (
          <div className="auth-form" id="loginForm">
            <h2>Đăng Nhập</h2>
            <form onSubmit={onLogin}>
              <div className="form-group"><input name="email" type="email" placeholder="Email" required /><i className="fas fa-envelope"/></div>
              <div className="form-group"><input name="password" type="password" placeholder="Mật khẩu" required /><i className="fas fa-lock"/></div>
              <button type="submit" className="auth-btn" disabled={loading}>{loading ? 'Đang xử lý...' : 'Đăng Nhập'}</button>
            </form>
            <p className="auth-switch">Chưa có tài khoản? <a href="#" onClick={(e)=>{e.preventDefault(); setMode('register')}}>Đăng ký ngay</a></p>
          </div>
        ) : (
          <div className="auth-form" id="registerForm">
            <h2>Đăng Ký</h2>
            <form onSubmit={onRegister}>
              <div className="form-group"><input name="name" type="text" placeholder="Họ và tên" required /><i className="fas fa-user"/></div>
              <div className="form-group"><input name="email" type="email" placeholder="Email" required /><i className="fas fa-envelope"/></div>
              <div className="form-group"><input name="phone" type="tel" placeholder="Số điện thoại" required /><i className="fas fa-phone"/></div>
              <div className="form-group"><input name="password" type="password" placeholder="Mật khẩu" required /><i className="fas fa-lock"/></div>
              <div className="form-group"><input name="confirm" type="password" placeholder="Xác nhận mật khẩu" required /><i className="fas fa-lock"/></div>
              <button type="submit" className="auth-btn" disabled={loading}>{loading ? 'Đang xử lý...' : 'Đăng Ký'}</button>
            </form>
            <p className="auth-switch">Đã có tài khoản? <a href="#" onClick={(e)=>{e.preventDefault(); setMode('login')}}>Đăng nhập</a></p>
          </div>
        )}
      </div>
    </div>
  )
}