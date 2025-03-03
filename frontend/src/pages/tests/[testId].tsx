'use client';
import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/router';
import styles from '../../styles/CustomerTestPage.module.css';

interface TestPageProps {
  testId: string;
}

interface Question {
  id: string;
  text: string;
  type: string;
  options?: string[];
  answer?: string;
}

interface Answer {
  questionId: string;
  selectedOption: string;
}

interface Test {
  id: string;
  name: string;
  description: string;
  questions: Question[];
}

const CustomerTestPage: React.FC<TestPageProps> = () => {
  const [test, setTest] = useState<Test | null>(null);
  const router = useRouter();
  const { testId } = router.query;
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [selectedOption, setSelectedOption] = useState<string | null>(null);
  const [answers, setAnswers] = useState<Answer[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [timeLeft, setTimeLeft] = useState(20);
  const [showResult, setShowResult] = useState(false);
  const [result, setResult] = useState<{ score: number; totalQuestions: number } | null>(null);
  const [timerId, setTimerId] = useState<NodeJS.Timeout | null>(null);
  const [submitted, setSubmitted] = useState(false);

  const fetchCsrfToken = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/auth/csrf', {
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
    const fetchTest = async () => {
      if (testId) {
        try {
          const csrfToken = await fetchCsrfToken();
          const response = await fetch(`http://localhost:8080/api/tests/${testId}`, {
            method: 'GET',
            credentials: 'include', // Gửi Cookie (JSESSIONID)
            headers: {
              'Content-Type': 'application/json',
              'X-XSRF-TOKEN': csrfToken, // Gửi CSRF token
            },
          });
          if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
          }
          const data: Test = await response.json();
          setTest(data);
        } catch (error) {
          console.error('Error fetching test:', error);
          alert('Failed to fetch test');
        }
      }
    };

    fetchTest();
  }, [testId]);

  useEffect(() => {
    if (!test) return;

    if (timeLeft > 0) {
      const id = setTimeout(() => {
        setTimeLeft(prevTimeLeft => prevTimeLeft - 1);
      }, 1000);
      setTimerId(id);

      return () => clearTimeout(id);
    } else {
      handleSkipQuestion();
    }
  }, [timeLeft, test, currentQuestionIndex]);

  if (!test || !test.questions) {
    return <div>Loading...</div>;
  }

  const currentQuestion = test.questions[currentQuestionIndex];

  const handleOptionClick = (option: string) => {
    setSelectedOption(option);

    setAnswers(prev => {
      const newAnswers = [...prev];
      const existingAnswerIndex = newAnswers.findIndex(
        a => a.questionId === currentQuestion.id
      );

      if (existingAnswerIndex !== -1) {
        newAnswers[existingAnswerIndex].selectedOption = option;
      } else {
        newAnswers.push({
          questionId: currentQuestion.id,
          selectedOption: option
        });
      }

      return newAnswers;
    });
  };

  const handleSkipQuestion = () => {
    setSelectedOption(null);

    if (currentQuestionIndex < test.questions.length - 1) {
      setCurrentQuestionIndex(prevIndex => prevIndex + 1);
      setTimeLeft(20);
    } else if (!isSubmitting) {
      confirmSubmit();
    }
    if (timerId) {
      clearTimeout(timerId);
    }
  };

  const handleSubmitTest = async () => {
    if (!test) return;

    setIsSubmitting(true);
    try {
      const customerId = localStorage.getItem('customerId');
      if (!customerId) {
        alert("Không tìm thấy Id");
        router.push("/");
        return;
      }
      const testId = test.id;
      const csrfToken = await fetchCsrfToken(); // Lấy token cho submit
      const response = await fetch(`http://localhost:8080/api/tests/${testId}/submit`, {
        method: 'POST',
        credentials: 'include', // Gửi Cookie
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': csrfToken, // Gửi CSRF token
        },
        body: JSON.stringify({
          testId: testId,
          customerId: customerId,
          answers: answers,
        }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }

      const resultData = await response.json();
      setResult(resultData);
      setShowResult(true);
      setSubmitted(true);
      if (timerId) {
        clearTimeout(timerId);
      }
    } catch (error) {
      console.error('Error submitting test:', error);
      alert('Failed to submit test');
    } finally {
      setIsSubmitting(false);
    }
  };

  const confirmSubmit = () => {
    if (!submitted) {
      const confirmed = window.confirm('Bạn có chắc chắn muốn nộp bài?');
      if (confirmed) {
        handleSubmitTest();
      }
    }
  };

  const handleReturnToCustomerPage = () => {
    const customerId = localStorage.getItem('customerId');
    if (customerId) {
      // Reset trạng thái
      setTest(null);
      setCurrentQuestionIndex(0);
      setSelectedOption(null);
      setAnswers([]);
      setIsSubmitting(false);
      setTimeLeft(20);
      setShowResult(false);
      setResult(null);
      setSubmitted(false);
      if (timerId) {
        clearTimeout(timerId);
      }
      // Chuyển hướng và thay thế lịch sử
      router.replace(`/customer/${customerId}`);
    } else {
      console.error('Customer ID not found in localStorage');
      router.replace('/'); // Nếu không có customerId, về trang chủ
    }
  };

  if (showResult && result) {
    return (
      <div className={styles.testPage}>
        <h2>Kết quả bài thi</h2>
        <p>Bạn đã trả lời đúng {result.score} / {result.totalQuestions} câu hỏi.</p>
        <button onClick={handleReturnToCustomerPage}>Về trang Customer</button>
      </div>
    );
  }

  return (
    <div className={styles.testPage}>
      <div className={styles.timer}>
        Timer: {timeLeft}s
      </div>
      <h2>Câu hỏi {currentQuestionIndex + 1}/{test.questions.length}</h2>
      <p>{currentQuestion.text}</p>
      <div className={styles.options}>
        {currentQuestion.options && currentQuestion.options.map((option) => (
          <div
            key={option}
            className={`${styles.option} ${selectedOption === option ? styles.selected : ''}`}
            onClick={() => handleOptionClick(option)}
          >
            {option}
          </div>
        ))}
      </div>
      <div className={styles.navigation}>
        <button onClick={handleSkipQuestion} disabled={isSubmitting}>
          Skip
        </button>
        {currentQuestionIndex === test.questions.length -1 &&  <button onClick={confirmSubmit} disabled={isSubmitting} className={styles.submitButton}>
            {isSubmitting ? 'Đang nộp...' : 'Nộp bài'}
          </button>}
      </div>
      <div className={styles.progress}>
        {currentQuestionIndex + 1}/{test.questions.length} câu hỏi
      </div>
    </div>
  );
};


export default CustomerTestPage;