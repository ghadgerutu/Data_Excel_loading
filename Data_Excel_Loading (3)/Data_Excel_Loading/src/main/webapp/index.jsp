<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Upload Page</title>
<style>
    body {
        font-family: Arial, sans-serif;
        background-color: #f4f4f4;
        display: flex;
        justify-content: center;
        align-items: center;
        height: 100vh;
        margin: 0;
    }
    .upload-container {
        background: white;
        padding: 20px;
        border-radius: 8px;
        box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        text-align: center;
    }
    input[type="file"] {
        padding: 10px;
        border: 1px solid #ccc;
        border-radius: 5px;
        display: block;
        margin: 10px auto;
    }
    button {
        background-color: #28a745;
        color: white;
        border: none;
        padding: 10px 15px;
        border-radius: 5px;
        cursor: pointer;
        font-size: 16px;
    }
    button:hover {
        background-color: #218838;
    }
</style>
</head>
<body>

<div class="upload-container">
    <h2>Upload Your File</h2>
    <form action="UploadData" method="post" enctype="multipart/form-data">
        <input type="file" name="fileUpload">
        <br>
        <button type="submit">Upload</button>
    </form>
</div>

</body>
</html>
