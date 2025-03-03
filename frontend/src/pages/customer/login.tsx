'use client';
import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/router';
import styles from '../../styles/CustomerLogin.module.css';

const CustomerAuth: React.FC = () => {
  const [isRegistering, setIsRegistering] = useState(false);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [phoneNumber, setPhoneNumber] = useState(''); // Thêm số điện thoại
  const [errorMessage, setErrorMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const [isConfirmPasswordVisible, setIsConfirmPasswordVisible] = useState(false);
  const router = useRouter();

  const [csrfToken, setCsrfToken] = useState<string | null>(null);

  useEffect(() => {
    const fetchCsrfToken = async () => {
      try {
        const response = await fetch('/api/auth/csrf', { credentials: 'include' });
        if (response.ok) {
          const data = await response.json();
          setCsrfToken(data.token);
        } else {
          console.error('Không thể lấy CSRF token.');
        }
      } catch (error) {
        console.error('Lỗi khi lấy CSRF token:', error);
      }
    };
    fetchCsrfToken();
  }, []);

  const getCsrfTokenFromCookie = () => {
    return document.cookie
      .split('; ')
      .find(row => row.startsWith('XSRF-TOKEN='))
      ?.split('=')[1];
  };

  const fetchWithCSRF = async (url: string, options: RequestInit = {}) => {
    const csrfToken = getCsrfTokenFromCookie();
    if (!csrfToken) {
      console.error('CSRF token không tồn tại.');
      setErrorMessage('Không thể xác thực. Vui lòng thử lại.');
      return;
    }

    return fetch(url, {
      ...options,
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        'X-XSRF-TOKEN': csrfToken,
        ...options.headers,
      },
    });
  };

  const handleLogin = async () => {
    setLoading(true);
    setErrorMessage('');

    try {
      const response = await fetchWithCSRF('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password }),
      });

      if (response?.ok) {
        const data = await response.json();
        if (!data.customerId) throw new Error('Không tìm thấy ID khách hàng.');
        router.push(`/customer/${data.customerId}`);
      } else {
        const errorData = await response?.text();
        setErrorMessage(errorData || 'Sai tài khoản hoặc mật khẩu.');
      }
    } catch (error) {
      console.error('Lỗi khi đăng nhập:', error);
      setErrorMessage('Lỗi hệ thống, vui lòng thử lại sau.');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async () => {
    if (password !== confirmPassword) {
      setErrorMessage('Mật khẩu xác nhận không khớp.');
      return;
    }

    // Validate phoneNumber client-side
    const phonePattern = /^0\d{9}$/;
    if (!phonePattern.test(phoneNumber)) {
      setErrorMessage('Số điện thoại phải bắt đầu bằng 0 và có đúng 10 chữ số.');
      return;
    }

    setLoading(true);
    setErrorMessage('');

    try {
      const response = await fetchWithCSRF('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify({ username, password, phoneNumber }),
      });

      if (!response) {
        throw new Error('Không thể kết nối đến server do lỗi CSRF token.');
      }

      if (response.ok) {
        const data = await response.json();
        if (!data.customerId) throw new Error('Không tìm thấy ID khách hàng.');
        router.push(`/customer/${data.customerId}`);
      } else {
        const errorData = await response.text();
        if (errorData.includes("Tài khoản đã tồn tại")) {
          setErrorMessage('Tài khoản đã tồn tại.');
        } else if (errorData.includes("Số điện thoại phải bắt đầu bằng 0")) {
          setErrorMessage('Số điện thoại không hợp lệ.');
        } else if (errorData.includes("Số điện thoại đã được sử dụng")) {
          setErrorMessage('Số điện thoại đã được sử dụng. Vui lòng nhập số khác.');
        } else {
          setErrorMessage(errorData || 'Đăng ký thất bại.');
        }
      }
    } catch (error) {
      console.error('Lỗi khi đăng ký:', error);
      setErrorMessage('Lỗi hệ thống, vui lòng thử lại sau.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>{isRegistering ? 'Tạo tài khoản' : 'Đăng nhập'}</h1>

      <div className={styles.formGroup}>
        <label>Tên đăng nhập</label>
        <input
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          className={styles.input}
          required
        />
      </div>

      <div className={styles.formGroup}>
        <label>Mật khẩu</label>
        <div className={styles.passwordWrapper}>
          <input
            type={isPasswordVisible ? 'text' : 'password'}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className={styles.input}
            required
          />
          <button
            type="button"
            className={styles.togglePasswordButton}
            onClick={() => setIsPasswordVisible(!isPasswordVisible)}
          >
            {isPasswordVisible ? 'Ẩn' : 'Hiển thị'}
          </button>
        </div>
      </div>

      {isRegistering && (
        <>
          <div className={styles.formGroup}>
            <label>Xác nhận mật khẩu</label>
            <div className={styles.passwordWrapper}>
              <input
                type={isConfirmPasswordVisible ? 'text' : 'password'}
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className={styles.input}
                required
              />
              <button
                type="button"
                className={styles.togglePasswordButton}
                onClick={() => setIsConfirmPasswordVisible(!isConfirmPasswordVisible)}
              >
                {isConfirmPasswordVisible ? 'Ẩn' : 'Hiển thị'}
              </button>
            </div>
          </div>

          <div className={styles.formGroup}>
            <label>Số điện thoại</label>
            <input
              type="text"
              value={phoneNumber}
              onChange={(e) => setPhoneNumber(e.target.value)}
              className={styles.input}
              placeholder="Nhập số điện thoại (0xxxxxxxxx)"
              required
            />
          </div>
        </>
      )}

      {errorMessage && <div className={styles.error}>{errorMessage}</div>}

      <button
        onClick={isRegistering ? handleRegister : handleLogin}
        className={styles.submitButton}
        disabled={loading}
      >
        {loading ? 'Đang xử lý...' : isRegistering ? 'Tạo tài khoản' : 'Đăng nhập'}
      </button>

      <div className={styles.options}>
        {!isRegistering && (
          <button onClick={() => alert('Chức năng chưa hỗ trợ!')} className={styles.linkButton}>
            Quên mật khẩu?
          </button>
        )}
        <button
          onClick={() => {
            setIsRegistering(!isRegistering);
            setErrorMessage('');
            setPhoneNumber(''); // Reset phoneNumber khi chuyển form
          }}
          className={styles.linkButton}
        >
          {isRegistering ? 'Quay lại đăng nhập' : 'Tạo tài khoản'}
        </button>
      </div>
    </div>
  );
};

export default CustomerAuth;