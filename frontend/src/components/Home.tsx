import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/router';  
import { FaUserCog } from 'react-icons/fa';
import styles from '../styles/Home.module.css';

interface TestResult {
  score: number;
  testName: string;
  timestamp: string;
}

function Home() {
  const [testResult, setTestResult] = useState<TestResult | null>(null);
  const router = useRouter(); 

  useEffect(() => {
    const result = localStorage.getItem('testResult');
    if (result) {
      setTestResult(JSON.parse(result));
      localStorage.removeItem('testResult');
    }
  }, []);

  const handleTestdevClick = () => {
    router.push('/testdev/login');  
  };

  const handleCustomerClick = () => {
    router.push('/customer/login');  
  };

  const handleCloseResult = () => {
    setTestResult(null);
  };

  return (
    <div className={styles.home}>
      {testResult && (
        <div className={styles.resultNotification}>
          <div className={styles.resultHeader}>
            <div>
              <h3 className={styles.resultTitle}>
                Kết quả bài thi: {testResult.testName}
              </h3>
              <p className={styles.score}>
                Điểm số: {testResult.score}
              </p>
              <p className={styles.timestamp}>
                Thời gian nộp: {new Date(testResult.timestamp).toLocaleString()}
              </p>
            </div>
            <button
              onClick={handleCloseResult}
              className={styles.closeButton}
            >
              ×
            </button>
          </div>
        </div>
      )}

      <h1 className={styles.heading}>
        Hệ thống thi trắc nghiệm
      </h1>

      {/* Icon Testdev ở góc trên bên phải */}
      <button
        onClick={handleTestdevClick}
        className={styles.testdevIcon}
        title="Testdev Login"
      >
        <FaUserCog size={30} />
      </button>

      <div className={styles.options}>
        <button
          onClick={handleCustomerClick}
          className={`${styles.button} ${styles.customerButton}`}
        >
          Nhấn vào đây để bắt đầu bài thi
        </button>
      </div>
    </div>
  );
}

export default Home;
