import React, { useState, useEffect } from "react";
import styles from "../styles/AddTestModal.module.css";
import { BACKEND_URL } from '../config';

interface TestSuite {
  id: string;
  name: string;
  description: string;
}

interface AddTestModalProps {
  closeModal: () => void;
  onTestAdded: () => void;
}

export const AddTestModal: React.FC<AddTestModalProps> = ({ closeModal, onTestAdded }) => {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [csrfToken, setCsrfToken] = useState<string | null>(null);

  const fetchCsrfToken = async () => {
    try {
      const response = await fetch('${BACKEND_URL}/api/auth/csrf', {
        method: 'GET',
        credentials: 'include',
      });
      if (!response.ok) {
        throw new Error(`Failed to fetch CSRF token: ${response.status}`);
      }
      const data = await response.json();
      console.log("Fetched CSRF token:", data.token);
      return data.token;
    } catch (error) {
      console.error("Error fetching CSRF token:", error);
      throw error;
    }
  };

  useEffect(() => {
    const loadCsrfToken = async () => {
      try {
        const token = await fetchCsrfToken();
        setCsrfToken(token);
      } catch (error) {
        alert("Không thể lấy CSRF token. Vui lòng thử lại.");
      }
    };
    loadCsrfToken();
  }, []);

  const handleAddTest = async () => {
    const testData = { name, description };
    try {
      if (!csrfToken) {
        console.error("CSRF token not available");
        alert("CSRF token không sẵn sàng. Vui lòng thử lại.");
        return;
      }
      console.log("CSRF token before POST:", csrfToken);
      console.log("Cookies before POST:", document.cookie);

      const response = await fetch("${BACKEND_URL}/api/testsuites", {
        method: "POST",
        credentials: 'include',
        headers: {
          "Content-Type": "application/json",
          "X-XSRF-TOKEN": csrfToken
        },
        body: JSON.stringify(testData),
      });

      console.log("POST response status:", response.status);
      if (!response.ok) {
        console.error("POST failed with status:", response.status);
        throw new Error(`HTTP error! Status: ${response.status}`);
      }

      const newTest = await response.json();
      console.log("Test added:", newTest);
      onTestAdded();
      closeModal();
    } catch (error) {
      console.error("Error adding test:", error);
      alert("Failed to add test: " + error);
    }
  };

  return (
    <div className={styles.modal}>
      <h2>Thêm Test Mới</h2>
      <input
        type="text"
        placeholder="Tên test"
        value={name}
        onChange={(e) => setName(e.target.value)}
        className={styles.input} // Dùng style từ EditTestPage
      />
      
      <button
        onClick={handleAddTest}
        disabled={!csrfToken}
        className={styles.button} // Dùng style từ EditTestPage
      >
        Thêm
      </button>
      <button
        onClick={closeModal}
        className={styles.button} // Dùng style từ EditTestPage
      >
        Hủy
      </button>
    </div>
  );
};