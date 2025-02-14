import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/router';
import QuestionItem from '../components/QuestionItem';
import { AddQuestionModal } from '../components/AddQuestionModal';

interface Question {
  id: string;
  text: string;
  type: string;
  options?: string[];
  answer?: string;
}

const EditTestPage = () => {
  const [multichoicePercentage, setMultichoicePercentage] = useState(0);
  const [oralPercentage, setOralPercentage] = useState(0);
  const [scalePercentage, setScalePercentage] = useState(0);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [showAddModal, setShowAddModal] = useState(false);
  const [testName, setTestName] = useState('');
  const [testDescription, setTestDescription] = useState('');
  const router = useRouter();
  const { id } = router.query;
  const [testSuiteId, setTestSuiteId] = useState('');

  const fetchTest = async () => {
    if (testSuiteId) {
      try {
        const response = await fetch(`http://localhost:8080/api/tests/detail/${testSuiteId}`);
        if (!response.ok) {
          throw new Error(`HTTP error! Status: ${response.status}`);
        }
        const data = await response.json();
        console.log("Data from backend: ", data);

        setTestName(data.name || '');
        setTestDescription(data.description || '');
        setMultichoicePercentage(data.multichoicePercentage || 0);
        setOralPercentage(data.oralPercentage || 0);
        setScalePercentage(data.scalePercentage || 0);
        setQuestions(data.questions || []);
      } catch (error) {
        console.error('Error fetching test suite:', error);
        alert('Failed to fetch test suite');
      }
    }
  };

  useEffect(() => {
    const safeId = Array.isArray(id) ? (id.length > 0 ? String(id[0]) : "") : String(id ?? "");
    setTestSuiteId(safeId);
    if (safeId) {
      fetchTest();
    }
  }, [id]);

  const handleCreateTest = async () => {
    try {
      const url = new URL('http://localhost:8080/api/tests/createWithPercentage');
      const params = {
        id: testSuiteId,
        name: testName,
        description: testDescription,
        multichoicePercentage: (multichoicePercentage || 0).toString(),
        oralPercentage: (oralPercentage || 0).toString(),
        scalePercentage: (scalePercentage || 0).toString()
      };
      url.search = new URLSearchParams(params).toString();

      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        }
      });

      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }

      const data = await response.json();
      setQuestions(data.questions); 

      alert('Create test success');
    } catch (error) {
      console.error('Error fetching test suites:', error);
      alert('Failed to fetch test suites');
    }
  };

    const handleSaveTest = async () => {
    try {
      const url = `http://localhost:8080/api/testsuites/${testSuiteId}`;
      const response = await fetch(url, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            id:testSuiteId,
          name: testName,
          description: testDescription,
          questions:questions,
          multichoicePercentage: (multichoicePercentage || 0).toString(),
          oralPercentage: (oralPercentage || 0).toString(),
          scalePercentage: (scalePercentage || 0).toString()
        }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }

      const data = await response.json();
      console.log("Save successful: ", data);
      alert('Lưu thành công')
      router.push('/testdev');
    } catch (error) {
      console.error('Error saving test suite:', error);
      alert('Failed to save test suite');
    }
  };
  const handleDeleteQuestion = (id: string) => {
    setQuestions(questions.filter((question) => question.id !== id));
};
    

     const handleAddQuestionClick = () => {
        setShowAddModal(true);
    };

    
  const handleQuestionAdded = async (newQuestion: any) => {
    try {
         const response = await fetch('http://localhost:8080/api/questions/' + newQuestion.id);
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
        const data = await response.json();
        setQuestions(data.questions);
   } catch(error) {
    console.error('Error fetching test suites:', error);
        alert('Failed to fetch test suites');
    }
    setShowAddModal(false);
   }
        const fetchTestSuites = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/testsuites');
      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }
      const data = await response.json();
      console.log("Test Suites: " , data);

    } catch (error) {
      console.error('Error fetching test suites:', error);
      alert('Failed to fetch test suites');
    }
  };

  return (
    <div className="test-edit">
      <h1>Chỉnh sửa đề</h1>
      <label>Đề thi:
        <input
          type="text"
          value={testName}
          onChange={(e) => setTestName(e.target.value)}
          placeholder="Nhập tên test"
        />
      </label>
      <label>
        Mô tả đề
        <input
          type="text"
          value={testDescription}
          onChange={(e) => setTestDescription(e.target.value)}
          placeholder="Nhập mô tả"
        />
      </label>
      <div>
        <label>
          Multichoice %:
          <input
            type="number"
            value={multichoicePercentage}
            onChange={(e) => {
              const parsedValue = parseInt(e.target.value);
              setMultichoicePercentage(isNaN(parsedValue) ? 0 : parsedValue);
            }}
            placeholder="Multichoice %"
          />
        </label>
        <label>
          Oral %:
          <input
            type="number"
            value={oralPercentage}
            onChange={(e) => {
              const parsedValue = parseInt(e.target.value);
              setOralPercentage(isNaN(parsedValue) ? 0 : parsedValue);
            }}
            placeholder="Oral %"
          />
        </label>
        <label>
          Scale %:
          <input
            type="number"
            value={scalePercentage}
            onChange={(e) => {
              const parsedValue = parseInt(e.target.value);
              setScalePercentage(isNaN(parsedValue) ? 0 : parsedValue);
            }}
            placeholder="Scale %"
          />
        </label>
        <button onClick={handleCreateTest}>Tạo Đề</button>
          <button onClick={handleSaveTest}>Lưu</button> {/* Thêm nút "Lưu" */}
      </div>
        <h2>Danh sách câu hỏi</h2>
          {questions?.map((question) => (
            <QuestionItem key={question.id} question={question} onDelete={handleDeleteQuestion} />
          ))}
        
    </div>
  );
};

export default EditTestPage;