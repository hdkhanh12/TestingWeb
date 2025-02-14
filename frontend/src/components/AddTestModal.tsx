import React, { useState } from 'react';
interface AddTestModalProps {
    closeModal: () => void;
     onTestAdded: () => void;
}

export const AddTestModal: React.FC<AddTestModalProps> = ({ closeModal, onTestAdded }) => {
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');

     const handleAddTest = async () => {
         if (name && description) {
          try {
                const response = await fetch('http://localhost:8080/api/testsuites', {
                   method: 'POST',
                    headers: {
                      'Content-Type': 'application/json',
                    },
                     body: JSON.stringify({name, description})
                 });
                console.log('handleAddTest response:', response);
               if(!response.ok) {
                   throw new Error(`HTTP error! Status: ${response.status}`);
               }
             alert('Thêm test thành công');
               
           closeModal();
           onTestAdded();
        } catch (error) {
                console.error('Lỗi fetching test: ', error);
                alert('Fetching test thất bại');
        }
         } else {
            alert('Nhập tên và mô tả');
         }
    };
    return (
        <div className="modal">
            <div className="modal-content">
              <h2>Thêm Test</h2>
                <input
                   type="text"
                   value={name}
                   onChange={(e) => setName(e.target.value)}
                   placeholder="Nhập tên test"
                 />
                <input
                   type="text"
                   value={description}
                   onChange={(e) => setDescription(e.target.value)}
                    placeholder="Nhập mô tả"
                />
                 <div className="modal-buttons">
                   <button onClick={handleAddTest}>Thêm</button>
                    <button onClick={closeModal}>Đóng</button>
                </div>
            </div>
        </div>
    );
};