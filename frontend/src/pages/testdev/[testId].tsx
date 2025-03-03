import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/router';
import QuestionItem from '../../components/QuestionItem';
import Select from 'react-select';
import styles from '../../styles/EditTestPage.module.css'; 

interface TestPageProps {
    testId: string;
}

interface SelectOption {
    value: string;
    label: string;
}

const TestPage: React.FC<TestPageProps> = () => {
    const [test, setTest] = useState<any>(null);
    const router = useRouter();
    const { testId } = router.query;
    console.log("Test ID:", testId);
    const [availableQuestions, setAvailableQuestions] = useState<SelectOption[]>([]);
    const [selectedQuestion, setSelectedQuestion] = useState<SelectOption | null>(null);

    const fetchCsrfToken = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/auth/csrf', {
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
        if (!router.isReady || !testId) return;

        const fetchTest = async () => {
            try {
                const csrfToken = await fetchCsrfToken();
                const response = await fetch(`http://localhost:8080/api/tests/${testId}`, {
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
                const data = await response.json();
                console.log("Data - api/tests/id:", data);
                setTest(data);
            } catch (error) {
                console.error('Error fetching test:', error);
                alert('Failed to fetch test');
            }
        };

        const fetchQuestions = async () => {
            try {
                const csrfToken = await fetchCsrfToken();
                const response = await fetch(`http://localhost:8080/api/questions`, {
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
                const data = await response.json();
                setAvailableQuestions(data.map((question: { id: any; text: any; }) => ({
                    value: question.id,
                    label: question.text,
                })));
                console.log("List questions: ", data);
            } catch (error) {
                console.error('Error fetching list question:', error);
                alert('Failed to fetch list question');
            }
        };

        fetchTest();
        fetchQuestions();
    }, [testId, router.isReady]);

    if (!testId || !test) {
        return <div>Loading...</div>;
    }

    const handleAddQuestion = async () => {
        if (selectedQuestion) {
            try {
                const csrfToken = await fetchCsrfToken();
                const response = await fetch(`http://localhost:8080/api/tests/${testId}/questions/${selectedQuestion.value}`, {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-XSRF-TOKEN': csrfToken
                    },
                });

                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }

                const data = await response.json();
                const responseQuestion = await fetch(`http://localhost:8080/api/questions/${selectedQuestion.value}`, {
                    method: 'GET',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-XSRF-TOKEN': csrfToken
                    }
                });
                if (!responseQuestion.ok) {
                    throw new Error(`HTTP error! Status: ${responseQuestion.status}`);
                }
                const questionData = await responseQuestion.json();
                setTest((prevTest: { questions: any[]; }) => {
                    if (prevTest && prevTest.questions) {
                        const questionExists = prevTest.questions.some((q) => q.id === questionData.id);
                        if (questionExists) {
                            return prevTest;
                        }
                        return { ...prevTest, questions: [...prevTest.questions, questionData] };
                    }
                    return { ...prevTest, questions: [questionData] };
                });
                setSelectedQuestion(null);
                alert('Thêm câu hỏi thành công');
            } catch (error) {
                console.error('Error adding question:', error);
                alert('Failed to add question');
            }
        } else {
            alert('Xin hãy lựa chọn câu hỏi');
        }
    };

    const handleDeleteQuestion = async (questionId: string) => {
        try {
            const csrfToken = await fetchCsrfToken();
            const response = await fetch(`http://localhost:8080/api/tests/${testId}/questions/${questionId}`, {
                method: 'DELETE',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'X-XSRF-TOKEN': csrfToken
                },
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            setTest((prevTest: { questions: any[]; }) => {
                const updatedQuestions = prevTest.questions.filter((question) => question.id !== questionId);
                return { ...prevTest, questions: updatedQuestions };
            });
            setSelectedQuestion(null);
            alert('Xóa câu hỏi thành công');
        } catch (error) {
            console.error('Error deleting question:', error);
            alert('Failed to delete question');
        }
    };

    const handleSaveTest = async () => {
        try {
            const csrfToken = await fetchCsrfToken();
            const response = await fetch(`http://localhost:8080/api/tests/${testId}`, {
                method: 'PUT',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'X-XSRF-TOKEN': csrfToken
                },
                body: JSON.stringify(test)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            alert('Lưu bài thi thành công');
            router.push('/testdev');
        } catch (error) {
            console.error('Error saving test:', error);
            alert('Failed to save test');
        }
    };

    return (
        <div className={styles.container}>
          <h1 className={styles.title}>{test.name}</h1>
          <p className={styles.empty}>{test.description}</p> 
          <div className={styles.card}>
            <h2 className={styles.label}>Questions</h2>
            <div className={styles.percentageInputs}> 
              <Select
                options={availableQuestions}
                value={selectedQuestion}
                onChange={(selectedOption: SelectOption | null) => setSelectedQuestion(selectedOption)}
                placeholder="Lựa chọn câu hỏi"
                className={styles.input} 
                styles={{
                  container: (base) => ({ ...base, width: '100%' }),
                  control: (base) => ({ ...base, borderRadius: '6px', borderColor: '#d1d5db' })
                }}
              />
              <button className={styles.button} onClick={handleAddQuestion}>
                Add Question
              </button>
            </div>
            <div className={styles.questionList}>
              {test.questions && test.questions.length > 0 ? (
                test.questions.map((question: any) => (
                  <QuestionItem
                    key={question.id}
                    question={question}
                    onDelete={handleDeleteQuestion}
                  />
                ))
              ) : (
                <p className={styles.empty}>Chưa có câu hỏi nào</p>
              )}
            </div>
            <div className={styles.buttonGroup}>
              <button className={styles.button} onClick={handleSaveTest}>
                Save
              </button>
            </div>
          </div>
        </div>
      );
};

export default TestPage;