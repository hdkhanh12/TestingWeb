'use client';
import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/router';
import styles from '../../styles/AdminQuestionsPage.module.css';
import { BACKEND_URL } from '../../config';

interface Question {
  id: string;
  text: string;
  type: string;
  options?: string[];
  answer?: string;
  packageId?: string;
  packageName?: string;
}

interface PackageSummary {
  packageName: string;
  packageId: string;
  questionCount: number;
}

const QuestionsPage = () => {
  const [packages, setPackages] = useState<PackageSummary[]>([]);
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
    const fetchPackages = async () => {
      try {
        const csrfToken = await fetchCsrfToken();
        const response = await fetch('${BACKEND_URL}/api/questions', {
          method: 'GET',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': csrfToken
          }
        });
        if (!response.ok) {
          throw new Error(`HTTP error! Status: ${response.status}`);
        }
        const data: Question[] = await response.json();

        // Nhóm câu hỏi theo packageId và lấy packageName
        const packageMap = data.reduce((acc, q) => {
          const pkgId = q.packageId || 'no-package';
          const pkgName = q.packageName || 'Không có gói';
          if (!acc[pkgId]) {
            acc[pkgId] = { packageName: pkgName, packageId: pkgId, questionCount: 0 };
          }
          acc[pkgId].questionCount += 1;
          return acc;
        }, {} as Record<string, PackageSummary>);

        const packageList = Object.values(packageMap);
        console.log("Package list:", packageList); // Log để kiểm tra dữ liệu
        setPackages(packageList);
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchPackages();
  }, []);

  const handleViewPackage = (packageId: string) => {
    const routeId = packageId === 'no-package' ? 'no-package' : packageId;
    router.push(`/questions/${routeId}`);
  };

  const handleDeletePackage = async (packageId: string) => {
    if (packageId === 'no-package') {
      alert('Không thể xóa các câu hỏi không thuộc gói');
      return;
    }
    const packageName = packages.find(pkg => pkg.packageId === packageId)?.packageName || packageId;
    if (window.confirm(`Bạn có chắc chắn muốn xóa gói ${packageName}?`)) {
      try {
        const csrfToken = await fetchCsrfToken();
        const url = packageId === 'no-package'
          ? '${BACKEND_URL}/api/questions/no-package'
          : `${BACKEND_URL}/api/questions/package/${packageId}`;
        const response = await fetch(url, {
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

        setPackages(prev => prev.filter(pkg => pkg.packageId !== packageId));
        alert('Đã xóa gói thành công');
      } catch (error) {
        console.error('Error deleting package:', error);
        alert('Không thể xóa gói: ' + error);
      }
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
      <h1 className={styles.title}>Quản lý gói câu hỏi</h1>
      <table className={styles.questionsTable}>
        <thead>
          <tr>
            <th>Gói (Package Name)</th>
            <th>Số câu hỏi</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          {packages.map((pkg) => (
            <tr key={pkg.packageId}>
              <td>{pkg.packageName}</td>
              <td>{pkg.questionCount}</td>
              <td>
                <button
                  onClick={() => handleViewPackage(pkg.packageId)}
                  style={{
                    backgroundColor: '#007bff',
                    color: 'white',
                    border: 'none',
                    padding: '8px 12px',
                    cursor: 'pointer',
                    borderRadius: '5px',
                    marginRight: '10px'
                  }}
                >
                  Xem
                </button>
                <button
                  className={styles.deleteButton}
                  onClick={() => handleDeletePackage(pkg.packageId)}
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

export default QuestionsPage;