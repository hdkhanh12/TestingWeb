'use client';
import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/router';
import styles from '../../styles/CustomerPage.module.css';

interface Customer {
  id: string;
  name: string;
  testAttempts: number; 
}

interface Test {
  id: string;
  name: string;
  description: string;
}

const CustomerPage: React.FC = () => {
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [tests, setTests] = useState<Test[]>([]);
  const [loading, setLoading] = useState(true);
  const [csrfToken, setCsrfToken] = useState<string | null>(null);
  const router = useRouter();
  const { id } = router.query;

  const fetchCsrfToken = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/auth/csrf', {
        method: 'GET',
        credentials: 'include',
      });
      if (!response.ok) throw new Error(`Failed to fetch CSRF token: ${response.status}`);
      const data = await response.json();
      setCsrfToken(data.token);
      return data.token;
    } catch (error) {
      console.error('Failed to fetch CSRF token:', error);
      throw error;
    }
  };

  useEffect(() => {
    if (!id) {
      const storedId = localStorage.getItem('customerId');
      if (storedId) {
        router.push(`/customer/${storedId}`);
      } else {
        console.log('Không tìm thấy ID, vui lòng đăng nhập lại.');
      }
      return;
    }

    const fetchCustomerAndTests = async () => {
      const token = await fetchCsrfToken();
      if (!token) {
        console.error('Không tìm thấy CSRF token.');
        alert('Không thể xác thực, vui lòng thử lại.');
        return;
      }

      try {
        // Fetch thông tin khách hàng
        const customerResponse = await fetch(`http://localhost:8080/api/customers/${id}`, {
          method: 'GET',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': token,
          },
        });

        if (!customerResponse.ok) {
          throw new Error(`Lỗi khi tải thông tin khách hàng: ${customerResponse.status}`);
        }

        const customerData = await customerResponse.json();
        console.log('Customer data:', customerData);
        setCustomer(customerData);
        localStorage.setItem('customerId', customerData.id); // Lưu customerId

        // Fetch danh sách bài kiểm tra gốc từ TESTDEV
        const testsResponse = await fetch(`http://localhost:8080/api/testsuites`, {
          method: 'GET',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': token,
          },
        });

        if (!testsResponse.ok) {
          console.error('Fetch tests failed with status:', testsResponse.status);
          const errorText = await testsResponse.text();
          throw new Error(errorText || `Lỗi khi tải danh sách bài kiểm tra: ${testsResponse.status}`);
        }

        const testsData = await testsResponse.json();
        console.log('Tests data:', testsData);
        if (!testsData || testsData.length === 0) {
          console.warn('No tests found from API');
          setTests([]);
        } else {
          setTests(testsData);
        }
      } catch (error: any) {
        console.error('Lỗi khi fetch dữ liệu:', error.message);
        if (error.message.includes('403')) {
          alert('Bạn không có quyền truy cập. Vui lòng đăng nhập lại.');
        } else {
          alert('Đã xảy ra lỗi, vui lòng thử lại.');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchCustomerAndTests();
  }, [id, router]);

  const handleTestClick = (testId: string) => {
    router.push(`/tests/${testId}`);
  };

  const handleRechargeClick = () => {
    router.push(`/customer/recharge`);
  };

  const handleLogout = async () => {
    try {
      const token = await fetchCsrfToken();
      const response = await fetch("http://localhost:8080/api/auth/logout", {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
          "X-XSRF-TOKEN": token,
        },
      });

      if (!response.ok) {
        throw new Error(`Logout failed with status: ${response.status}`);
      }

      localStorage.removeItem("customerId"); // Xóa customerId khỏi localStorage
      router.push("/"); // Chuyển về trang chủ
    } catch (error) {
      console.error("Error logging out:", error);
      alert("Đăng xuất thất bại, vui lòng thử lại.");
    }
  };

  const handleViewResults = () => {
    const customerId = localStorage.getItem('customerId');
    if (customerId) {
      router.push(`/customer/results/${customerId}`);
    } else {
      alert('Không tìm thấy Id khách hàng, vui lòng đăng nhập lại.');
      router.push('/');
    }
  };

  if (loading) {
    return <div className={styles.loading}>Loading...</div>;
  }

  return (
    <div className={styles.container}>
      <h1 className={styles.welcome}>Chào mừng, {customer?.name}!</h1>
      <p className={styles.testAttempts}>Bạn còn {customer?.testAttempts} lần test</p>
      <h2 className={styles.subtitle}>Chọn bài kiểm tra:</h2>
      {tests.length > 0 ? (
        <ul className={styles.testList}>
          {tests.map((test) => (
            <li key={test.id} className={styles.testItem} onClick={() => handleTestClick(test.id)}>
              <span className={styles.testName}>{test.name}</span>
              <p className={styles.testDescription}>{test.description || 'Không có mô tả'}</p>
            </li>
          ))}
        </ul>
      ) : (
        <p>Không có bài kiểm tra nào hiện tại.</p>
      )}
      <button className={styles.rechargeButton} onClick={handleRechargeClick}>
        Nạp tiền
      </button>
      <button className={styles.resultsButton} onClick={handleViewResults}>
          Xem kết quả thi
      </button>
      <button className={styles.logoutButton} onClick={handleLogout}>
        Đăng xuất
      </button>
    </div>
  );
};

export default CustomerPage;