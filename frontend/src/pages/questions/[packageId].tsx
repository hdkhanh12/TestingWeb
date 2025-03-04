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

const QuestionsByPackagePage = () => {
  const [questions, setQuestions] = useState<Question[]>([]);
  const [packageName, setPackageName] = useState<string>(''); // Thêm để hiển thị tên gói
  const [selectedQuestions, setSelectedQuestions] = useState<Set<string>>(new Set());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();
  const { packageId } = router.query; // packageId là UUID từ route

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
    if (!router.isReady || !packageId) return;

    const fetchQuestions = async () => {
      try {
        const csrfToken = await fetchCsrfToken();
        const url = packageId === 'no-package'
          ? '${BACKEND_URL}/api/questions'
          : `${BACKEND_URL}/api/questions/package/${packageId}`; // Dùng packageId
        const response = await fetch(url, {
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
        let data: Question[] = await response.json();
        if (packageId === 'no-package') {
          data = data.filter(q => !q.packageId);
        } else if (data.length > 0) {
          setPackageName(data[0].packageName || packageId as string); // Lấy packageName từ câu hỏi đầu tiên
        }
        setQuestions(data);
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchQuestions();
  }, [router.isReady, packageId]);

  const handleSelectQuestion = (id: string) => {
    setSelectedQuestions(prev => {
      const newSet = new Set(prev);
      if (newSet.has(id)) {
        newSet.delete(id);
      } else {
        newSet.add(id);
      }
      return newSet;
    });
  };

  const handleDeleteSelected = async () => {
    if (selectedQuestions.size === 0) {
      alert('Vui lòng chọn ít nhất một câu hỏi để xóa');
      return;
    }
    if (window.confirm(`Bạn có chắc chắn muốn xóa ${selectedQuestions.size} câu hỏi?`)) {
      try {
        const csrfToken = await fetchCsrfToken();
        const deletePromises = Array.from(selectedQuestions).map(id =>
          fetch(`${BACKEND_URL}/api/questions/${id}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: {
              'Content-Type': 'application/json',
              'X-XSRF-TOKEN': csrfToken
            }
          }).then(res => {
            if (!res.ok) throw new Error(`Failed to delete question ${id}`);
            return id;
          })
        );

        await Promise.all(deletePromises);
        setQuestions(prev => prev.filter(q => !selectedQuestions.has(q.id)));
        setSelectedQuestions(new Set());
        alert('Đã xóa các câu hỏi được chọn');
      } catch (error) {
        console.error('Error deleting selected questions:', error);
        alert('Không thể xóa câu hỏi');
      }
    }
  };

  const handleDeleteQuestion = async (id: string) => {
    if (window.confirm('Bạn có chắc chắn muốn xóa câu hỏi này?')) {
      try {
        const csrfToken = await fetchCsrfToken();
        const response = await fetch(`${BACKEND_URL}/api/questions/${id}`, {
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

        setQuestions(prev => prev.filter(q => q.id !== id));
        alert('Question deleted successfully!');
      } catch (error) {
        console.error('Error deleting question:', error);
        alert('Failed to delete question');
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
      <h1 className={styles.title}>Câu hỏi trong gói {packageName || (packageId === 'no-package' ? 'Không có gói' : packageId)}</h1>
      <button
        className={styles.deleteButton}
        onClick={handleDeleteSelected}
        style={{ marginBottom: '20px' }}
      >
        Xóa các câu hỏi được chọn ({selectedQuestions.size})
      </button>
      <table className={styles.questionsTable}>
        <thead>
          <tr>
            <th>Chọn</th>
            <th>ID</th>
            <th>Nội dung</th>
            <th>Loại</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          {questions.map((question) => (
            <tr key={question.id}>
              <td>
                <input
                  type="checkbox"
                  checked={selectedQuestions.has(question.id)}
                  onChange={() => handleSelectQuestion(question.id)}
                />
              </td>
              <td>{question.id}</td>
              <td>{question.text}</td>
              <td>{question.type}</td>
              <td>
                <button
                  className={styles.deleteButton}
                  onClick={() => handleDeleteQuestion(question.id)}
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

export default QuestionsByPackagePage;