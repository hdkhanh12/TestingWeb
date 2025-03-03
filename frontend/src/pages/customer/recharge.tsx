'use client';
import React, { useState } from 'react';
import { useRouter } from 'next/router';
import styles from '../../styles/RechargePage.module.css';

const RechargePage: React.FC = () => {
  const [step, setStep] = useState(1); // 1: QR, 2: Nhập nội dung
  const [transactionContent, setTransactionContent] = useState('');
  const [amount, setAmount] = useState(5); // Số lần mặc định
  const [isSubmitting, setIsSubmitting] = useState(false);
  const router = useRouter();

  const fetchCsrfToken = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/auth/csrf', {
        method: 'GET',
        credentials: 'include',
      });
      if (!response.ok) throw new Error(`Failed to fetch CSRF token: ${response.status}`);
      const data = await response.json();
      return data.token;
    } catch (error) {
      console.error('Error fetching CSRF token:', error);
      throw error;
    }
  };

  const handleNextStep = () => {
    setStep(2);
  };

  const handleRecharge = async () => {
    setIsSubmitting(true);
    try {
      const customerId = localStorage.getItem('customerId');
      if (!customerId) {
        alert("Không tìm thấy Id khách hàng");
        router.push("/");
        return;
      }
      const csrfToken = await fetchCsrfToken();
      const response = await fetch(`http://localhost:8080/api/customers/${customerId}/recharge`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': csrfToken,
        },
        body: JSON.stringify({ amount }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`HTTP error! Status: ${response.status} - ${errorText}`);
      }

      const result = await response.json();
      alert(result.message);
      router.push(`/customer/${customerId}`);
    } catch (error) {
      console.error('Error recharging:', error);
      alert('Không thể nạp tiền: ' + error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Nạp tiền</h1>
      {step === 1 ? (
        <div className={styles.step}>
          <h2>Bước 1: Quét mã QR</h2>
          <p>Vui lòng quét mã QR dưới đây để nạp tiền:</p>
          {/* Giả lập mã QR bằng hình ảnh tĩnh */}
          <img src="/qr-code.png" alt="QR Code" className={styles.qrCode} />
          <p>Số lần nạp: {amount}</p>
          <input
            type="number"
            value={amount}
            onChange={(e) => setAmount(Math.max(1, parseInt(e.target.value) || 1))}
            className={styles.amountInput}
            min="1"
          />
          <button onClick={handleNextStep} className={styles.nextButton}>
            Tiếp theo
          </button>
        </div>
      ) : (
        <div className={styles.step}>
          <h2>Bước 2: Nhập nội dung chuyển tiền</h2>
          <p>Vui lòng nhập nội dung chuyển tiền bạn đã sử dụng:</p>
          <input
            type="text"
            value={transactionContent}
            onChange={(e) => setTransactionContent(e.target.value)}
            placeholder="Nhập nội dung chuyển tiền"
            className={styles.contentInput}
          />
          <button
            onClick={handleRecharge}
            disabled={isSubmitting || !transactionContent}
            className={styles.submitButton}
          >
            {isSubmitting ? 'Đang nạp...' : 'Xác nhận nạp tiền'}
          </button>
        </div>
      )}
    </div>
  );
};

export default RechargePage;