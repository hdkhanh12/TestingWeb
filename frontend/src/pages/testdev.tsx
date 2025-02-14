import React from 'react';
 import TestSuiteManager from '../components/TestSuiteManager';
 import Link from 'next/link';

 const TestdevPage = () => {
   return (
     <div>
       <h1>Trang admin quản lý test</h1>
       <TestSuiteManager />
        <Link href="/import-questions">
            <button>Nhập ngân hàng câu hỏi</button>
         </Link>
     </div>
   );
 };

 export default TestdevPage;