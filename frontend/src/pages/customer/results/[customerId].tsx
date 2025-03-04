'use client';
import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/router';
import styles from '../../../styles/ResultsPage.module.css';
import { BACKEND_URL } from '../../../config';

interface TestResult {
  id: string;
  testId: string;
  customerId: string;
  score: number;
  totalQuestions: number;
  timestamp: string;
}

const CustomerResultsPage: React.FC = () => {
  const [results, setResults] = useState<TestResult[]>([]);
  const [loading, setLoading] = useState(true);
  const router = useRouter();
  const { customerId } = router.query;

  const fetchCsrfToken = async () => {
    try {
      const response = await fetch('${BACKEND_URL}/api/auth/csrf', {
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

  useEffect(() => {
    if (!customerId) return;

    const fetchResults = async () => {
      try {
        const csrfToken = await fetchCsrfToken();
        const response = await fetch(`${BACKEND_URL}/api/customers/${customerId}/results`, {
          method: 'GET',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': csrfToken,
          },
        });

        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(errorText || `HTTP error! Status: ${response.status}`);
        }

        const resultsData = await response.json();
        console.log('Results data:', resultsData);
        setResults(resultsData);
      } catch (error) {
        console.error('Error fetching results:', error);
        alert('Không thể tải kết quả thi');
      } finally {
        setLoading(false);
      }
    };

    fetchResults();
  }, [customerId]);

  if (loading) return <div className={styles.loading}>Loading...</div>;

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Kết quả thi của bạn</h1>
      {results.length > 0 ? (
        <table className={styles.resultsTable}>
          <thead>
            <tr>
              <th>ID Bài thi</th>
              <th>Điểm số</th>
              <th>Tổng số câu hỏi</th>
              <th>Thời gian nộp</th>
            </tr>
          </thead>
          <tbody>
            {results.map((result) => (
              <tr key={result.id}>
                <td>{result.testId}</td>
                <td>{result.score}</td>
                <td>{result.totalQuestions}</td>
                <td>{new Date(result.timestamp).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : (
        <p>Chưa có kết quả thi nào.</p>
      )}
      <button className={styles.backButton} onClick={() => router.push(`/customer/${customerId}`)}>
        Quay lại
      </button>
    </div>
  );
};

export default CustomerResultsPage;