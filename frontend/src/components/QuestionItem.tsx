import React from 'react';

interface QuestionItemProps {
    question: {
        id: string;
        text: string;
        type: string;
         options?: string[];
         answer?: string
    };
    onDelete: (id: string) => void;
}

const QuestionItem: React.FC<QuestionItemProps> = ({ question, onDelete }) => {
    return (
        <div className="question-item">
          <p>ID: {question.id}</p>
            <p>Text: {question.text}</p>
             <p>Type: {question.type}</p>
              {question.options && <p>Options: {question.options.join(', ')}</p>}
                {question.answer && <p>Answer: {question.answer}</p>}
               <button onClick={() => onDelete(question.id)}>Xóa</button>
        </div>
    );
};

export default QuestionItem;