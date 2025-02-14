import React, { useState } from 'react';

interface AddQuestionModalProps {
   closeModal: () => void;
   onQuestionAdded: (newQuestion: any) => void;
}

export const AddQuestionModal: React.FC<AddQuestionModalProps> = ({ closeModal, onQuestionAdded }) => {
 const [id, setId] = useState('');

 const handleAddQuestion = async () => {
  if(id) {
     try {
          const response = await fetch(`http://localhost:8080/api/questions/${id}`);
           if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
             alert('Thêm câu hỏi thành công');
             const newQuestion = {id: Math.random().toString(36).substring(2, 15), Text};
           onQuestionAdded(newQuestion)
        } catch(error) {
          console.error('Lỗi', error);
          alert('Không thêm được câu hỏi');
        }
  } else {
      alert('Vui lòng nhập id')
  }

};

return (
    <div className="modal">
        <div className="modal-content">
            <h2>Thêm câu hỏi</h2>
            <input
                type="text"
                 value={id}
               onChange={(e) => setId(e.target.value)}
                placeholder="Nhập id câu hỏi"
             />
            <div className="modal-buttons">
               <button onClick={handleAddQuestion}>Thêm</button>
                <button onClick={closeModal}>Đóng</button>
            </div>
        </div>
    </div>
);
};