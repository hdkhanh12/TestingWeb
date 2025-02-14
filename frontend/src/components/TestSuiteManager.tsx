import React, { useState, useEffect, useCallback } from 'react';
import { AddTestModal } from './AddTestModal';
import { EditTestModal } from './EditTestModal';
import Link from 'next/link';
import { useRouter } from 'next/router'; // Import useRouter

interface TestSuite {
    id: string;
    name: string;
    description: string;
     startDate?: string;
    endDate?: string;
    status?: string;
    testIds?: string[];
}

const TestSuiteManager = () => {
    const [testSuites, setTestSuites] = useState<TestSuite[]>([]);
    const [showAddModal, setShowAddModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [selectedTest, setSelectedTest] = useState<TestSuite | null>(null);
    const router = useRouter(); // Sử dụng useRouter


     const fetchTestSuites = useCallback(async () => {
        try {
            const response = await fetch('http://localhost:8080/api/testsuites'); // Thay bằng URL backend của bạn
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            const data = await response.json();
            console.log("Test Suites Data:", data);
            setTestSuites(data);
        } catch (error) {
            console.error('Error fetching test suites:', error);
            alert('Failed to fetch test suites');
        }
    },[setTestSuites]);


    useEffect(() => {
         fetchTestSuites();
    }, [fetchTestSuites]);

    const handleAddTestClick = () => {
        setShowAddModal(true);
    };

    const handleEditTestClick = (test: TestSuite) => {
        setSelectedTest(test);
        setShowEditModal(true);
    };

    const handleDeleteTestClick = async (testId: string) => {
        try {
            const response = await fetch(`http://localhost:8080/api/testsuites/${testId}`, {
                method: 'DELETE',
            });
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            const updatedTestSuites = testSuites.filter(test => test.id !== testId)
            setTestSuites(updatedTestSuites)
            alert('Xóa test thành công')
        } catch (error) {
            console.error('Error deleting test suite:', error);
            alert('Failed to delete test suite');
        }
    };
    

    const handleViewAllResultsClick = () => {
        console.log("handleViewAllResultsClick called");
        router.push('/test-results');
    };

    const backtoHomeClick = () => {
        console.log("backtoHomeClick called");
        window.location.href = '/'; // Tải lại trang chính
    };
    

    const viewQuestionsClick = () => {
        console.log("viewQuestionsClick called");
        router.push('/questions');
    };

    return (
        <div>
            <h2>Danh sách Test</h2>
            <ul>
                {testSuites.map((test) => (
                    <li key={test.id}>
                        {test.name} - {test.description}
                        <Link href={`/edit-test?id=${test.id}`}>
                         <button >Sửa</button>
                          </Link>
                          <Link href={`/testdev/${test.id}`}>
                            <button onClick={() => {
                            console.log("Clicking view button");
                            console.log("Current test:", test);
                            console.log("Test ID being passed:", test.id);
                            }}>Xem</button>
                        </Link>
                        <button onClick={() => handleDeleteTestClick(test.id)}>Xóa</button>
                    </li>
                ))}
            </ul>
            <button onClick={handleAddTestClick}>Thêm Test</button>
            <button onClick={handleViewAllResultsClick}>Xem tất cả điểm</button> 
            <button onClick={viewQuestionsClick}>Xem danh sách câu hỏi</button> 
            <button onClick={backtoHomeClick}>Quay về trang chủ</button> 
            {showAddModal && <AddTestModal closeModal={() => setShowAddModal(false)} onTestAdded={fetchTestSuites} />}
            {showEditModal && selectedTest ? <EditTestModal closeModal={() => setShowEditModal(false)} test={selectedTest} /> : null}
        </div>
    );
};

export default TestSuiteManager;