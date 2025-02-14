import React from 'react';
import { useRouter } from "next/router";

interface TestItemProps {
    test: {
      id: string;
      name: string;
      description: string;
    };
  }

  const TestItem: React.FC<TestItemProps> = ({ test }) => {
      const router = useRouter();

      const handleTestClick = () => {
        router.push(`/tests/${test.id}`); // Chuyển đến trang test
      };

   
  return (
    <div onClick={handleTestClick} style={{ 
      cursor: 'pointer',
      border: '1px solid #ccc', // Thêm border
      padding: '10px', // Thêm padding
      margin: '10px 0', // Thêm margin
      display: 'flex', // Sử dụng flexbox
      alignItems: 'center' // Căn giữa theo chiều dọc
    }}>
      <h3 style={{ marginRight: '10px' }}>{test.name}</h3> {/* Thêm margin right */}
      <p>{test.description}</p>
    </div>
  );
};

  export default TestItem;