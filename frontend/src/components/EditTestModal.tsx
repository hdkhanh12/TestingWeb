import React, { useState, useEffect } from 'react';

interface EditTestModalProps {
    closeModal: () => void;
    test: {
      id: string;
      name: string;
      description: string;
    };
}

export const EditTestModal: React.FC<EditTestModalProps> = ({ closeModal, test }) => {
    const [name, setName] = useState(test.name);
    const [description, setDescription] = useState(test.description);
    
      const handleEditTest = () => {
         if (name && description) {
             alert('Chỉnh sửa thành công');
        } else {
             alert('Nhập tên và mô tả');
        }
    };

   useEffect(() => {
        setName(test.name);
        setDescription(test.description);
    }, [test]);
   
    return (
        <div className="modal">
            <div className="modal-content">
              <h2>Chỉnh sửa Test</h2>
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
                     <button onClick={handleEditTest}>Sửa</button>
                   <button onClick={closeModal}>Đóng</button>
                </div>
            </div>
        </div>
    );
};