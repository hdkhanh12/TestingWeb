import React, { useState } from 'react';
import { useRouter } from 'next/router';
import styles from '../../styles/Login.module.css';

function TestdevLogin() {
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPassword(e.target.value);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setErrorMessage('');
  
    try {
      const response = await fetch('/api/auth/testdev/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username: 'testdev', password: password }),
        credentials: 'include', 
      });
      if (response.ok) {
        //const data = await response.json(); 
        //localStorage.setItem('token', data.token); 
        router.push('/testdev');
      } else {
        const errorData = await response.text(); 
        setErrorMessage(errorData || 'Sai mật khẩu, vui lòng thử lại');
      }
    } catch (error) {
      console.error('Login error:', error);
      setErrorMessage('Có lỗi xảy ra, vui lòng thử lại');
    } finally {
      setLoading(false);
    }
  };
  

  return (
    <div className={styles.loginContainer}>
      <h1 className={styles.heading}>Đăng nhập Testdev</h1>

      <form onSubmit={handleSubmit} className={styles.loginForm}>
        <div className={styles.formGroup}>
          <label htmlFor="password">Mật khẩu</label>
          <input
            type="password"
            id="password"
            value={password}
            onChange={handlePasswordChange}
            required
            className={styles.input}
          />
        </div>

        {errorMessage && <div className={styles.error}>{errorMessage}</div>}

        <button type="submit" className={styles.submitButton} disabled={loading}>
          {loading ? 'Đang đăng nhập...' : 'Đăng nhập'}
        </button>
      </form>
    </div>
  );
}

export default TestdevLogin;
