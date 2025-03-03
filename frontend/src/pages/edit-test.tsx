'use client';
import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/router';
import QuestionItem from '../components/QuestionItem';
import { AddQuestionModal } from '../components/AddQuestionModal';
import styles from '../styles/EditTestPage.module.css';


interface Question {
  id: string;
  text: string;
  type: string;
  options?: string[];
  answer?: string;
  packageId?: string;
  packageName?: string; // Thêm packageName
}

interface PackageOption {
  value: string; // packageId
  label: string; // packageName
}

const EditTestPage = () => {
  const [multichoicePercentage, setMultichoicePercentage] = useState(0);
  const [oralPercentage, setOralPercentage] = useState(0);
  const [scalePercentage, setScalePercentage] = useState(0);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [showAddModal, setShowAddModal] = useState(false);
  const [testName, setTestName] = useState('');
  const [testDescription, setTestDescription] = useState('');
  const [selectedPackage, setSelectedPackage] = useState<string>(''); // Lưu packageId
  const [packageOptions, setPackageOptions] = useState<PackageOption[]>([]);
  const router = useRouter();
  const { id } = router.query;
  const [testSuiteId, setTestSuiteId] = useState('');
  const [csrfToken, setCsrfToken] = useState<string | null>(null);

  const fetchCsrfToken = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/auth/csrf', {
        method: 'GET',
        credentials: 'include',
      });
      if (!response.ok) throw new Error(`Failed to fetch CSRF token: ${response.status}`);
      const data = await response.json();
      console.log("Fetched CSRF token:", data.token);
      setCsrfToken(data.token);
      return data.token;
    } catch (error) {
      console.error("Error fetching CSRF token:", error);
      throw error;
    }
  };

  useEffect(() => {
    const safeId = Array.isArray(id) ? (id.length > 0 ? String(id[0]) : '') : String(id ?? '');
    setTestSuiteId(safeId);
    if (safeId) {
      fetchTest();
      fetchPackages();
    }
  }, [id]);

  const fetchPackages = async () => {
    try {
      const csrfToken = await fetchCsrfToken();
      const response = await fetch('http://localhost:8080/api/questions', {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': csrfToken
        }
      });
      if (!response.ok) throw new Error(`HTTP error! Status: ${response.status}`);
      const data: Question[] = await response.json();

      // Nhóm câu hỏi theo packageId và lấy packageName
      const packageMap = data.reduce((acc, q) => {
        const pkgId = q.packageId;
        const pkgName = q.packageName;
        if (pkgId && !acc[pkgId]) {
          acc[pkgId] = { value: pkgId, label: pkgName || `Gói ${pkgId}` };
        }
        return acc;
      }, {} as Record<string, PackageOption>);

      const uniquePackages = Object.values(packageMap);
      setPackageOptions([{ value: '', label: 'Chọn gói' }, ...uniquePackages]);
      console.log("Package options:", uniquePackages); // Log để kiểm tra
    } catch (error) {
      console.error('Error fetching packages:', error);
      alert('Không thể tải danh sách gói');
    }
  };

  const fetchTest = async () => {
    if (!testSuiteId) return;
    try {
      const csrfToken = await fetchCsrfToken();
      const response = await fetch(`http://localhost:8080/api/tests/detail/${testSuiteId}`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': csrfToken || '',
        },
      });
      if (!response.ok) throw new Error(`HTTP error! Status: ${response.status}`);

      const data = await response.json();
      setTestName(data.name || '');
      setTestDescription(data.description || '');
      setMultichoicePercentage(data.multichoicePercentage || 0);
      setOralPercentage(data.oralPercentage || 0);
      setScalePercentage(data.scalePercentage || 0);
      setQuestions(data.questions || []);
    } catch (error) {
      console.error('Error fetching test suite:', error);
      alert('Không thể tải bài thi');
    }
  };

  const handleCreateTest = async () => {
    if (!selectedPackage) {
      alert('Vui lòng chọn một gói câu hỏi');
      return;
    }
    try {
      const csrfToken = await fetchCsrfToken();
      if (!csrfToken) {
        console.error("CSRF token not available");
        alert("CSRF token không sẵn sàng. Vui lòng thử lại.");
        return;
      }

      const url = new URL('http://localhost:8080/api/tests/createWithPercentage');
      const params = {
        id: testSuiteId,
        name: testName,
        description: testDescription,
        multichoicePercentage: (multichoicePercentage || 0).toString(),
        oralPercentage: (oralPercentage || 0).toString(),
        scalePercentage: (scalePercentage || 0).toString(),
        packageId: selectedPackage
      };
      url.search = new URLSearchParams(params).toString();

      console.log("CSRF token before POST:", csrfToken);
      console.log("Cookies before POST:", document.cookie);

      const response = await fetch(url, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': csrfToken
        }
      });

      console.log("POST response status:", response.status);
      if (!response.ok) {
        const errorText = await response.text();
        console.error("Error response:", errorText);
        throw new Error(`HTTP error! Status: ${response.status}`);
      }

      const data = await response.json();
      setQuestions(data.questions);
      alert('Tạo đề thi thành công');
    } catch (error) {
      console.error('Error creating test:', error);
      alert('Failed to create test: ' + error);
    }
  };

  const handleSaveTest = async () => {
    try {
      const csrfToken = await fetchCsrfToken();
      if (!csrfToken) {
        console.error("CSRF token not available");
        alert("CSRF token không sẵn sàng. Vui lòng thử lại.");
        return;
      }

      console.log("CSRF token before PUT:", csrfToken);
      console.log("Cookies before PUT:", document.cookie);

      const response = await fetch(`http://localhost:8080/api/testsuites/${testSuiteId}`, {
        method: 'PUT',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': csrfToken
        },
        body: JSON.stringify({
          id: testSuiteId,
          name: testName,
          description: testDescription,
          questions,
          multichoicePercentage,
          oralPercentage,
          scalePercentage,
        }),
      });

      console.log("PUT response status:", response.status);
      if (!response.ok) {
        const errorText = await response.text();
        console.error("Error response:", errorText);
        throw new Error(`HTTP error! Status: ${response.status}`);
      }

      alert('Lưu bài thi thành công');
      router.push('/testdev');
    } catch (error) {
      console.error('Error saving test suite:', error);
      alert('Không thể lưu bài thi: ' + error);
    }
  };

  const handleDeleteQuestion = (id: string) => {
    setQuestions(questions.filter((question) => question.id !== id));
  };

  const handleAddQuestionClick = () => {
    setShowAddModal(true);
  };

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Chỉnh sửa bài thi</h1>

      <div className={styles.card}>
        <label className={styles.label}>Tên bài thi:</label>
        <input
          type="text"
          value={testName}
          onChange={(e) => setTestName(e.target.value)}
          className={styles.input}
          placeholder="Nhập tên bài thi"
        />

        <label className={styles.label}>Mô tả:</label>
        <input
          type="text"
          value={testDescription}
          onChange={(e) => setTestDescription(e.target.value)}
          className={styles.input}
          placeholder="Nhập mô tả bài thi"
        />

        <label className={styles.label}>Chọn gói câu hỏi:</label>
        <select
          value={selectedPackage}
          onChange={(e) => setSelectedPackage(e.target.value)}
          className={styles.input}
        >
          {packageOptions.map(option => (
            <option key={option.value} value={option.value}>{option.label}</option>
          ))}
        </select>

        <div className={styles.percentageInputs}>
          <div>
            <label className={styles.label}>Multichoice (%):</label>
            <input
              type="number"
              value={multichoicePercentage}
              onChange={(e) => setMultichoicePercentage(Number(e.target.value) || 0)}
              className={styles.input}
            />
          </div>
          <div>
            <label className={styles.label}>Oral (%):</label>
            <input
              type="number"
              value={oralPercentage}
              onChange={(e) => setOralPercentage(Number(e.target.value) || 0)}
              className={styles.input}
            />
          </div>
          <div>
            <label className={styles.label}>Scale (%):</label>
            <input
              type="number"
              value={scalePercentage}
              onChange={(e) => setScalePercentage(Number(e.target.value) || 0)}
              className={styles.input}
            />
          </div>
        </div>

        <div className={styles.buttonGroup}>
          <button className={styles.button} onClick={handleCreateTest}>
            Tạo Đề
          </button>
          <button className={styles.button} onClick={handleSaveTest}>
            Lưu bài thi
          </button>
        </div>
      </div>

      <h2 className={styles.subTitle}>Danh sách câu hỏi</h2>
      <div className={styles.questionList}>
        {questions.length > 0 ? (
          questions.map((question) => (
            <QuestionItem key={question.id} question={question} onDelete={handleDeleteQuestion} />
          ))
        ) : (
          <p className={styles.empty}>Chưa có câu hỏi nào</p>
        )}
      </div>
    </div>
  );
};

export default EditTestPage;