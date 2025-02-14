import React, { useState } from 'react';

const ImportQuestionsPage = () => {
  const [file, setFile] = useState<File | null>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
        console.log('file selected');
      setFile(e.target.files[0]);
       console.log('file:', e.target.files[0])
    }
  };

  const handleImport = async () => {
    if (!file) {
      alert('Xin hãy chọn file');
      return;
    }
     try{
       const formData = new FormData();
        formData.append('file', file);
        console.log('formData', formData)
       const response = await fetch('http://localhost:8080/api/questions/import', {
        method: 'POST',
         body: formData,
       });
      console.log('Response:', response);
       if(!response.ok){
          throw new Error(`HTTP error! Status: ${response.status}`);
       }
          alert('Nhập file thành công');
    } catch (error) {
      console.error('Error import question', error);
      alert('Nhập file thất bại');
   }

 };

  return (
    <div className="import-page">
        <h2>Import Questions</h2>
        <input type="file" accept=".json, .txt" onChange={handleFileChange} />
      <button onClick={handleImport}>Import</button>
    </div>
  );
};

export default ImportQuestionsPage;