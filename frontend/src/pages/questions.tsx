// pages/admin/questions.tsx
import React, { useState, useEffect } from 'react';

interface Question {
  id: string;
  text: string;
  type: string;
  options?: string[]; // Tùy thuộc vào cấu trúc câu hỏi của bạn
  answer?: string;  // Tùy thuộc vào cấu trúc câu hỏi của bạn
}

const AdminQuestionsPage = () => {
  const [questions, setQuestions] = useState<Question[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchQuestions = async () => {
      try {
        const response = await fetch('/api/questions'); // Gọi API GET /api/questions
        if (!response.ok) {
          throw new Error(`HTTP error! Status: ${response.status}`);
        }
        const data: Question[] = await response.json();
        setQuestions(data);
      } catch (err:any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchQuestions();
  }, []);

  const handleDeleteQuestion = async (id: string) => {
    if (window.confirm('Bạn có chắc chắn muốn xóa câu hỏi này?')) { // Xác nhận trước khi xóa
      try {
        const response = await fetch(`/api/questions/${id}`, { method: 'DELETE' }); // Gọi API DELETE /api/questions/{id}
        if (!response.ok) {
          throw new Error(`HTTP error! Status: ${response.status}`);
        }

        // Cập nhật state sau khi xóa thành công:
        setQuestions(prevQuestions => prevQuestions.filter(q => q.id !== id));

        alert('Question deleted successfully!');
      } catch (error) {
        console.error('Error deleting question:', error);
        alert('Failed to delete question');
      }
    }
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error: {error}</div>;
  }

  return (
    <div>
      <h1>Quản lý câu hỏi</h1>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Nội dung</th>
            <th>Loại</th>
            {/* Thêm các cột khác (tùy thuộc vào cấu trúc câu hỏi) */}
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          {questions.map((question) => (
            <tr key={question.id}>
              <td>{question.id}</td>
              <td>{question.text}</td>
              <td>{question.type}</td>
              {/* Thêm các cột khác (tùy thuộc vào cấu trúc câu hỏi) */}
              <td>
                <button onClick={() => handleDeleteQuestion(question.id)}>Xóa</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default AdminQuestionsPage;
