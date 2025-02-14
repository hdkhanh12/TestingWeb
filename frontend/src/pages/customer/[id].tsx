import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/router';
import TestItem from '../../components/TestItem';

interface CustomerPageProps {
  customerId: string;
}

interface Customer {
  id: string;
  name: string;
}
interface Test {
    id: string;
    name: string;
    description: string;
  }

const CustomerPage: React.FC<CustomerPageProps> = () => {
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [tests, setTests] = useState<Test[]>([]);
  const router = useRouter();
  const { id } = router.query; // Lấy customer ID từ URL
  console.log("Customer ID:", router.query.id);

  useEffect(() => {
    const fetchCustomerAndTests = async () => {
      try {
        // Lấy thông tin customer
        const customerResponse = await fetch(`http://localhost:8080/api/customers/${id}`); 
        console.log(customerResponse);
        if (!customerResponse.ok) {
          throw new Error(`HTTP error! Status: ${customerResponse.status}`);
        }
        const customerData = await customerResponse.json();
        setCustomer(customerData);

        // Lấy danh sách các test
        const testsResponse = await fetch('http://localhost:8080/api/testsuites ');
        if (!testsResponse.ok) {
          throw new Error(`HTTP error! Status: ${testsResponse.status}`);
        }
        const testsData = await testsResponse.json();
        setTests(testsData);
      } catch (error) {
        console.error('Error fetching data:', error);
        alert('Failed to load data');
      }
    };

    fetchCustomerAndTests();
  }, [id]);

  if (!customer) {
    return <div>Loading...</div>;
  }

  return (
    <div>
      <h1>Chào mừng đến với bài kiểm tra, {customer?.name}!</h1>
      <h2>Chọn bài kiểm tra:</h2>
      {tests.map(test => (
        <TestItem key={test.id} test={test} />
      ))}
    </div>
  );
};

export default CustomerPage;