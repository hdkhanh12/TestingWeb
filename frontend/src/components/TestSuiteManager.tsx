import React, { useState, useEffect, useCallback } from "react";
import { AddTestModal } from "./AddTestModal";
import { EditTestModal } from "./EditTestModal";
import Link from "next/link";
import { useRouter } from "next/router";
import styles from "../styles/TestSuiteManager.module.css";
import { BACKEND_URL } from '../config';

interface TestSuite {
  id: string;
  name: string;
  description: string;
  startDate?: string;
  endDate?: string;
  status?: string;
  testIds?: string[];
}

const TestSuiteManager = () => {
  const [testSuites, setTestSuites] = useState<TestSuite[]>([]);
  const [showAddModal, setShowAddModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [selectedTest, setSelectedTest] = useState<TestSuite | null>(null);
  const router = useRouter();

  const getCsrfToken = () => {
    return document.cookie
      .split('; ')
      .find(row => row.startsWith('XSRF-TOKEN='))
      ?.split('=')[1] ?? '';
  };

  const fetchTestSuites = useCallback(async () => {
    try {
      const csrfToken = getCsrfToken();
      const response = await fetch('${BACKEND_URL}/api/testsuites', {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': csrfToken
        }
      });
      console.log('Response status:', response.status);
      console.log('Cookies:', document.cookie);
      
      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }
      const data = await response.json();
      setTestSuites(data);
    } catch (error) {
      console.error("Error fetching test suites:", error);
      alert("Failed to fetch test suites");
    }
  }, [setTestSuites]);

  useEffect(() => {
    fetchTestSuites();
  }, [fetchTestSuites]);

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

const handleDeleteTestClick = async (testId: string) => {
    try {
        const csrfToken = await fetchCsrfToken();
        if (!csrfToken) {
            console.error("CSRF token not available");
            alert("CSRF token không sẵn sàng. Vui lòng thử lại.");
            return;
        }

        console.log("CSRF token before DELETE:", csrfToken);
        console.log("Cookies before DELETE:", document.cookie);

        const response = await fetch(`${BACKEND_URL}/api/testsuites/${testId}`, {
            method: "DELETE",
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': csrfToken
            }
        });

        console.log("DELETE response status:", response.status);
        if (!response.ok) {
            const errorText = await response.text();
            console.error('Delete failed with status:', response.status, "Response:", errorText);
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        const updatedTestSuites = testSuites.filter((test) => test.id !== testId);
        setTestSuites(updatedTestSuites);
        alert("Xóa test thành công");
    } catch (error) {
        console.error("Error deleting test suite:", error);
        alert("Failed to delete test suite: " + error);
    }
};
  
  

  const handleLogout = async () => {
    try {
      const response = await fetch('/api/auth/logout', {
        method: 'POST',
        credentials: 'include',
      });
      if (response.ok) {
        localStorage.removeItem('token');
        window.location.href = "/";
      } else {
        console.error("Logout thất bại");
      }
    } catch (error) {
      console.error("Lỗi khi logout:", error);
    }
  };

  return (
    <div className={styles.container}>
      <h2 className={styles.heading}>Danh sách Test</h2>
      <div className={styles.grid}>
        {testSuites.map((test) => (
          <div key={test.id} className={styles.card}>
            <h3>{test.name}</h3>
            <p>{test.description}</p>
            <div className={styles.buttonGroup}>
              <Link href={`/edit-test?id=${test.id}`}>
                <button className={`${styles.button} ${styles.editButton}`}>
                  Sửa
                </button>
              </Link>
              <Link href={`/testdev/${test.id}`}>
                <button className={`${styles.button} ${styles.viewButton}`}>
                  Xem
                </button>
              </Link>
              <button
                onClick={() => handleDeleteTestClick(test.id)}
                className={`${styles.button} ${styles.deleteButton}`}
              >
                Xóa
              </button>
            </div>
          </div>
        ))}
      </div>
      <div className={styles.mainButtons}>
        <button onClick={() => setShowAddModal(true)} className={styles.mainButton}>
          Thêm Test
        </button>
        <button onClick={() => router.push("/test-results")} className={styles.mainButton}>
          Xem tất cả điểm
        </button>
        <button onClick={() => router.push("/testdev/customers")} className={styles.mainButton}>
          Xem danh sách người dùng
        </button>
        <button onClick={() => router.push("/questions")} className={styles.mainButton}>
          Xem câu hỏi
        </button>
        <button onClick={handleLogout} className={styles.mainButton}>
          Trang chủ
        </button>
      </div>
      {showAddModal && (
        <AddTestModal
          closeModal={() => setShowAddModal(false)}
          onTestAdded={fetchTestSuites}
        />
      )}
      {showEditModal && selectedTest && (
        <EditTestModal
          closeModal={() => setShowEditModal(false)}
          test={selectedTest}
        />
      )}
    </div>
  );
};

export default TestSuiteManager;