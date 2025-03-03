import React from "react";
import TestSuiteManager from "../components/TestSuiteManager";
import Link from "next/link";
import styles from "../styles/TestdevPage.module.css"; // Import CSS

const TestdevPage = () => {
  return (
    <div className={styles.container}>
      <h1 className={styles.heading}>Trang Admin Quản Lý Test</h1>
      <TestSuiteManager />

      <Link href="/import-questions">
        <button className={styles.importButton}>Nhập ngân hàng câu hỏi</button>
      </Link>
    </div>
  );
};

export default TestdevPage;
