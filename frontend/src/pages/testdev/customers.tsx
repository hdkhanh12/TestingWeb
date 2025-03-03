'use client';
import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/router';
import styles from '../../styles/TestResultsPage.module.css';

interface CustomerInfo {
  id: string;
  username: string;
  password: string;
  phoneNumber: string;
}

const CustomersPage: React.FC = () => {
  const [customers, setCustomers] = useState<CustomerInfo[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  const fetchCsrfToken = async () => {
    try {
      const response = await fetch('/api/auth/csrf', {
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

  const fetchCustomers = async () => {
    try {
      const csrfToken = await fetchCsrfToken();
      const response = await fetch('http://localhost:8080/api/auth/customers', {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': csrfToken
        }
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || `HTTP error! Status: ${response.status}`);
      }

      const data: CustomerInfo[] = await response.json();
      setCustomers(data);
    } catch (err: any) {
      console.error('Error fetching customers:', err);
      setError(err.message || 'Không thể tải danh sách khách hàng');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteCustomer = async (customerId: string) => {
    if (!window.confirm(`Bạn có chắc chắn muốn xóa khách hàng với ID: ${customerId}?`)) {
      return;
    }

    try {
      const csrfToken = await fetchCsrfToken();
      const response = await fetch(`http://localhost:8080/api/auth/customers/${customerId}`, {
        method: 'DELETE',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': csrfToken
        }
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || `HTTP error! Status: ${response.status}`);
      }

      // Cập nhật danh sách sau khi xóa
      setCustomers(customers.filter(customer => customer.id !== customerId));
      alert('Xóa khách hàng thành công!');
    } catch (err: any) {
      console.error('Error deleting customer:', err);
      alert('Xóa thất bại: ' + (err.message || 'Lỗi hệ thống'));
    }
  };

  useEffect(() => {
    fetchCustomers();
  }, []);

  if (loading) {
    return <div className={styles.container}><h1 className={styles.title}>Đang tải...</h1></div>;
  }

  if (error) {
    return <div className={styles.container}><h1 className={styles.title}>Lỗi: {error}</h1></div>;
  }

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Danh sách khách hàng</h1>
      <table className={styles.resultsTable}>
        <thead>
          <tr>
            <th>ID</th>
            <th>Tên đăng nhập</th>
            <th>Mật khẩu (mã hóa)</th>
            <th>Số điện thoại</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          {customers.map((customer) => (
            <tr key={customer.id}>
              <td>{customer.id}</td>
              <td>{customer.username}</td>
              <td>{customer.password}</td>
              <td>{customer.phoneNumber}</td>
              <td>
                <button
                  className={styles.deleteButton}
                  onClick={() => handleDeleteCustomer(customer.id)}
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

export default CustomersPage;