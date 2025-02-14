
'use client';
import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/router';
import styles from '../styles/TestResultsPage.module.css';

interface TestResult {
    id: string;
    testId: string;
    customerId: string;
    customerName: string; //tên customer
    score: number;
    totalQuestions: number;
}
const TestResultsPage = () => {
    console.log("TestResultsPage: Component rendered"); // Đầu component
    const [results, setResults] = useState<TestResult[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const router = useRouter();


    useEffect(() => {
        console.log("TestResultsPage: useEffect called"); // Trong useEffect
        const fetchTestResults = async () => {
            console.log("TestResultsPage: fetchTestResults called"); // Trong fetchTestResults
            try {
                // URL dựa trên việc có testId hay không
                const url = '/api/tests/results';

                const response = await fetch(url);
                console.log("TestResultsPage: Response:", response); // Sau fetch
                if (!response.ok) {
                    if (response.status === 403) {
                        router.push("/")
                        return;
                    }
                throw new Error(`HTTP error! Status: ${response.status}`);
                }
                const data: TestResult[] = await response.json();
                console.log("TestResultsPage: Data fetched:", data); // Sau khi lấy data
                setResults(data);
            } catch (err:any) {
                console.error("TestResultsPage: Error fetching results:", err); // Trong catch
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchTestResults();
    }, [router]); // Thêm testId vào dependency array

    if (loading) {
        console.log("TestResultsPage: Loading..."); // Trong loading
        return <div>Loading...</div>;
    }

    if (error) {
        console.log("TestResultsPage: Error:", error); // Trong error
        return <div>Error: {error}</div>;
    }
    console.log("TestResultPage: results", results)

    return (
        <div className={styles.container}>
             <h1>Kết quả thi</h1>
             <table className={styles.table}>
               <thead>
                 <tr>
                   <th>ID</th>
                   <th>Test ID</th>
                   <th>Customer ID</th>
                   <th>Customer Name</th>
                   <th>Điểm</th>
                   <th>Tổng số câu</th>
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
                   </tr>
                 ))}
               </tbody>
             </table>
           </div>
         );
    };
    

export default TestResultsPage;