// CustomerModal.tsx
import React, { useState } from 'react';
import { useRouter } from 'next/router';

interface CustomerModalProps {
  closeModal: () => void;
}

export const CustomerModal: React.FC<CustomerModalProps> = ({ closeModal }) => {
  const [name, setName] = useState('');
  const router = useRouter();

  const handleStartTest = async () => {
    if (name) {
      try {
        const response = await fetch(`http://localhost:8080/api/customers?name=${name}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          }
        });

        if (!response.ok) {
          throw new Error(`HTTP error! Status: ${response.status}`);
        }

        const data = await response.json(); 

        if (data && data.id) {
          localStorage.setItem('customerId', data.id); // Lưu customerId vào localStorage
          router.push(`/customer/${data.id}`); 
        } else {
          console.error("Giá trị data không hợp lệ: ", data);
          alert('Không bắt đầu được bài thi vì giá trị id không hợp lệ');
        }
      } catch (error) {
        console.error('Không tạo được thí sinh', error);
        alert('Không bắt đầu bài thi được');
      }
    } else {
      alert('Xin hãy điền tên');
    }
  };

  return (
    <div className="modal">
      <div className="modal-content">
        <h2>Nhập tên của bạn</h2>
        <input
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="Nhập tên"
        />
        <div className="modal-buttons">
          <button onClick={handleStartTest}>Bắt đầu</button>
          <button onClick={closeModal}>Đóng</button>
        </div>
      </div>
    </div>
  );
};