'use client';
import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/router';
import styles from '../styles/TestResultsPage.module.css';
import { BACKEND_URL } from '../config';

interface TestResult {
    id: string;
    testId: string;
    customerId: string;
    customerName: string;
    score: number;
    totalQuestions: number;
}

const TestResultsPage = () => {
    const [results, setResults] = useState<TestResult[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const router = useRouter();

    const fetchCsrfToken = async () => {
        try {
            const response = await fetch('${BACKEND_URL}/api/auth/csrf', {
                method: 'GET',
                credentials: 'include',
            });
            if (!response.ok) throw new Error(`Failed to fetch CSRF token: ${response.status}`);
            const data = await response.json();
            console.log("Fetched CSRF token:", data.token);
            return data.token;
        } catch (error) {
            console.error("Error fetching CSRF token:", error);
            throw error;
        }
    };

    useEffect(() => {
        const fetchTestResults = async () => {
            try {
                const csrfToken = await fetchCsrfToken();
                const response = await fetch('/api/tests/results', {
                    method: 'GET',
                    credentials: 'include', // Gửi JSESSIONID
                    headers: {
                        'Content-Type': 'application/json',
                        'X-XSRF-TOKEN': csrfToken // Thêm CSRF token
                    }
                });
                if (!response.ok) {
                    if (response.status === 403) {
                        router.push("/");
                        return;
                    }
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                const data: TestResult[] = await response.json();
                setResults(data);
            } catch (err: any) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchTestResults();
    }, [router]);

    const handleDeleteResult = async (resultId: string) => {
        if (!confirm(`Bạn có chắc muốn xóa kết quả thi với ID ${resultId}?`)) return;

        try {
            const csrfToken = await fetchCsrfToken();
            const response = await fetch(`/api/tests/results/${resultId}`, {
                method: 'DELETE',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'X-XSRF-TOKEN': csrfToken
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            // Cập nhật danh sách kết quả sau khi xóa
            setResults(results.filter(result => result.id !== resultId));
            alert('Xóa kết quả thi thành công');
        } catch (error) {
            console.error('Error deleting test result:', error);
            alert('Không thể xóa kết quả thi: ' + error);
        }
    };

    if (loading) {
        return <div className={styles.container}><h1 className={styles.title}>Loading...</h1></div>;
    }

    if (error) {
        return <div className={styles.container}><h1 className={styles.title}>Error: {error}</h1></div>;
    }

    return (
        <div className={styles.container}>
            <h1 className={styles.title}>Kết quả thi</h1>
            <table className={styles.resultsTable}>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Test ID</th>
                        <th>Customer ID</th>
                        <th>Customer Name</th>
                        <th>Điểm</th>
                        <th>Tổng số câu</th>
                        <th>Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    {results.map((result) => (
                        <tr key={result.id}>
                            <td>{result.id}</td>
                            <td>{result.testId}</td>
                            <td>{result.customerId}</td>
                            <td>{result.customerName}</td>
                            <td>{result.score}</td>
                            <td>{result.totalQuestions}</td>
                            <td>
                                <button 
                                    onClick={() => handleDeleteResult(result.id)}
                                    className={styles.deleteButton}
                                >
                                    Xóa
                                </button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default TestResultsPage;