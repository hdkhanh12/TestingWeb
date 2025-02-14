import React, { useState } from 'react';
import { useRouter } from 'next/router';

interface TestdevModalProps {
    closeModal: () => void;
}

export const TestdevModal: React.FC<TestdevModalProps> = ({ closeModal }) => {
    const [key, setKey] = useState('');
    const router = useRouter();

    const handleAuthenticate = () => {
        if (key === 'password') {
          router.push('/testdev');
        } else {
            alert('Sai key xác thực');
            router.push('/');
        }
    };

    return (
        <div className="modal">
            <div className="modal-content">
                <h2>Nhập Key xác thực Testdev</h2>
                <input
                    type="text"
                    value={key}
                    onChange={(e) => setKey(e.target.value)}
                    placeholder="Nhập key"
                />
                <div className="modal-buttons">
                    <button onClick={handleAuthenticate}>Xác thực</button>
                    <button onClick={closeModal}>Đóng</button>
                </div>
            </div>
        </div>
    );
};