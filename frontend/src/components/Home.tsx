import React, { useState, useEffect } from 'react';
import { TestdevModal } from './TestdevModal';
import { CustomerModal } from './CustomerModal';
import styles from '../styles/Home.module.css';

interface TestResult {
  score: number;
  testName: string;
  timestamp: string;
}

function Home() {
  const [showTestdevModal, setShowTestdevModal] = useState(false);
  const [showCustomerModal, setShowCustomerModal] = useState(false);
  const [testResult, setTestResult] = useState<TestResult | null>(null);

  useEffect(() => {
    const result = localStorage.getItem('testResult');
    if (result) {
      setTestResult(JSON.parse(result));
      localStorage.removeItem('testResult');
    }
  }, []);

  const handleTestdevClick = () => {
    setShowTestdevModal(true);
  };

  const handleCustomerClick = () => {
    setShowCustomerModal(true);
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

      <div className={styles.options}>
        <button
          onClick={handleTestdevClick}
          className={`${styles.button} ${styles.testdevButton}`}
        >
          Testdev
        </button>
        
        <button
          onClick={handleCustomerClick}
          className={`${styles.button} ${styles.customerButton}`}
        >
          Customer
        </button>
      </div>

      {showTestdevModal && (
        <div className={styles.modalOverlay}>
          <TestdevModal closeModal={() => setShowTestdevModal(false)} />
        </div>
      )}
      
      {showCustomerModal && (
        <div className={styles.modalOverlay}>
          <CustomerModal closeModal={() => setShowCustomerModal(false)} />
        </div>
      )}
    </div>
  );
}

export default Home;