'use client';
import React, { useState } from 'react';
import styles from '../styles/ImportQuestionsPage.module.css';
import { BACKEND_URL } from '../config';

const ImportQuestionsPage = () => {
  const [file, setFile] = useState<File | null>(null);
  const [packageName, setPackageName] = useState<string>('');

  const fetchCsrfToken = async () => {
    try {
      const response = await fetch('${BACKEND_URL}/api/auth/csrf', {
        method: 'GET',
        credentials: 'include',
      });
      if (!response.ok) throw new Error(`Failed to fetch CSRF token: ${response.status}`);
      const data = await response.json();
      console.log("Fetched CSRF token:", data.token);
      return data.token;
    } catch (error) {
      console.error("Error fetching CSRF token:", error);
      throw error;
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      console.log('File selected:', e.target.files[0]);
      setFile(e.target.files[0]);
    }
  };

  const handlePackageNameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPackageName(e.target.value);
  };

  const handleImport = async () => {
    if (!file) {
      alert('Xin hãy chọn file');
      return;
    }
    if (!packageName) {
      alert('Xin hãy nhập tên gói');
      return;
    }

    try {
      const csrfToken = await fetchCsrfToken();
      const formData = new FormData();
      formData.append('file', file);
      formData.append('packageName', packageName);
      console.log('FormData:', formData);

      const response = await fetch('${BACKEND_URL}/api/questions/import/package', {
        method: 'POST',
        credentials: 'include',
        headers: {
          'X-XSRF-TOKEN': csrfToken
        },
        body: formData,
      });

      console.log('Response:', response);

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Error response:', errorText);
        throw new Error(`HTTP error! Status: ${response.status}`);
      }

      const data = await response.json();
      const importedPackageName = data.packageName || packageName; // Lấy packageName từ response
      alert(`Nhập file thành công vào gói: ${importedPackageName}`);
      setFile(null);
      setPackageName('');
    } catch (error) {
      console.error('Error importing questions:', error);
      alert('Nhập file thất bại: ' + error);
    }
  };

  return (
    <div className={styles.container}>
      <h2 className={styles.title}>Import Questions</h2>
      <input
        type="text"
        value={packageName}
        onChange={handlePackageNameChange}
        placeholder="Nhập tên gói"
        className={styles.packageInput}
      />
      <input
        type="file"
        accept=".json, .txt"
        id="fileInput"
        className={styles.fileInput}
        onChange={handleFileChange}
      />
      <label htmlFor="fileInput" className={styles.fileLabel}>
        Chọn file
      </label>
      {file && <p className={styles.fileName}>📂 {file.name}</p>}
      <button className={styles.importButton} onClick={handleImport}>
        Import
      </button>
    </div>
  );
};

export default ImportQuestionsPage;