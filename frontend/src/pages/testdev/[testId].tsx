import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/router';
import QuestionItem from '../../components/QuestionItem';
import Select from 'react-select';

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
    const [availableQuestions, setAvailableQuestions] = useState([]);
   //const [selectedQuestion, setSelectedQuestion] = useState(null);
    const [selectedQuestion, setSelectedQuestion] = useState<SelectOption | null>(null);

    useEffect(() => {

        if (!router.isReady) return;

        const fetchTest = async () => {
        
                try {
                    const response = await fetch(`http://localhost:8080/api/tests/${testId}`); // Lấy thông tin test
                    if (!response.ok) {
                        throw new Error(`HTTP error! Status: ${response.status}`);
                    }
                    const data = await response.json();
                    console.log("Data - api/test/id:", data);
                    //console.log("Data from backend: ", data);
                    setTest(data);
                } catch (error) {
                    console.error('Error fetching test:', error);
                    alert('Failed to fetch test');
                }
            
        };
        
        const fetchQuestions = async () => {
       
            try {
                const response = await fetch(`http://localhost:8080/api/questions`); // API lay list question
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                const data = await response.json();
                 setAvailableQuestions(data.map((question: { id: any; text: any; }) => ({
                        value: question.id,
                        label: question.text,
                })));
                console.log("list question: ", availableQuestions);
            } catch (error) {
                console.error('Error fetching list question:', error);
                alert('Failed to fetch list question');
            }
         };

        //fetchTest();
        //fetchQuestions();
        if (testId) {
            fetchTest();
            fetchQuestions();
        }
    }, [testId]);

    if (!testId || !test) {
        return <div>Loading...</div>;
    }

    const handleAddQuestion = async () => {
        if (selectedQuestion) {
            try {
                 const response = await fetch(`http://localhost:8080/api/tests/${testId}/questions/${selectedQuestion.value}`, {
                     method: 'POST',
                     headers: {
                         'Content-Type': 'application/json',
                     },
                 });

                 if (!response.ok) {
                     throw new Error(`HTTP error! Status: ${response.status}`);
                 }

                 const data = await response.json();
                //Khi đã call API thanh công thì phải làm 2 bước là
                //1. set lại Question
                  const responseQuestion = await fetch(`http://localhost:8080/api/questions/${selectedQuestion.value}`); // API lay list question
                   if (!responseQuestion.ok) {
                           throw new Error(`HTTP error! Status: ${response.status}`);
                      }
                      const questionData = await responseQuestion.json();
                      setTest((prevTest: { questions: any[]; }) => {
                         if (prevTest && prevTest.questions) {
                             // Kiểm tra xem câu hỏi đã tồn tại trong danh sách hay chưa
                                const questionExists = prevTest.questions.some((q) => q.id === questionData.id);
                                 if (questionExists) {
                                        // Nếu câu hỏi đã tồn tại, không thêm mới
                                       return prevTest;
                                  }
                             // Nếu câu hỏi chưa tồn tại, thêm vào danh sách
                            return { ...prevTest, questions: [...prevTest.questions, questionData] };
                         }
                       return { ...prevTest, questions: [questionData] };
                   });
                    //2. Selected lại
                 setSelectedQuestion(null)
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
                  const response = await fetch(`http://localhost:8080/api/tests/${testId}/questions/${questionId}`, {
                       method: 'DELETE',
                       headers: {
                            'Content-Type': 'application/json',
                       },
                 });
  
                 if (!response.ok) {
                       throw new Error(`HTTP error! Status: ${response.status}`);
                 }
  
                 const data = await response.json();
               setTest((prevTest: { questions: any[]; }) => {
                    // Clone lại state trước để tránh sửa đổi trực tiếp
                       const updatedQuestions = prevTest.questions.filter((question) => question.id !== questionId);
                       return { ...prevTest, questions: updatedQuestions };
               })
                  setSelectedQuestion(null)
                  alert('xóa câu hỏi thành công');
             } catch (error) {
                  console.error('Error adding question:', error);
                  alert('Failed to delete question');
              }
    }

    const handleSaveTest = async () => {
        try {
          const response = await fetch(`http://localhost:8080/api/tests/${testId}`, {
               method: 'PUT',
               headers: {
                    'Content-Type': 'application/json',
               },
               body: JSON.stringify(test)
         });

         if (!response.ok) {
               throw new Error(`HTTP error! Status: ${response.status}`);
         }

         const data = await response.json();

        alert('Lưu bài thi thành công');
        router.push('/testdev');
     } catch (error) {
          console.error('Error adding question:', error);
          alert('Failed to saved question');
      }
  }


    return (
        <div>
        <h1>{test.name}</h1>
        <p>{test.description}</p>
        <h2>Questions</h2>
         <Select
                    options={availableQuestions}
                    value={selectedQuestion}
                    onChange={(selectedOption: SelectOption | null) => setSelectedQuestion(selectedOption)}
                    placeholder="Lựa chọn câu hỏi"
                />
         <button onClick={handleAddQuestion}>Add Question</button>
        {test.questions && test.questions.map((question: any) => (
            <QuestionItem key={question.id} question={question} onDelete={handleDeleteQuestion} />
        ))}
         <button onClick={handleSaveTest}>Save</button>
    </div>
    );
};

export default TestPage;