<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>목표 관리</title>
</head>
<body>
    <h1>목표 생성하기</h1>
    <form id="goalForm" action="/api/v1/goals/create" method="post">
        <label for="userId">사용자 ID:</label>
        <input type="text" id="userId" name="userId" required ><br><br>

        <label for="name">목표 이름:</label>
        <input type="text" id="name" name="name" required><br><br>

        <label for="status">상태:</label>
        <select id="status" name="status">
            <option value="DRAFT">임시저장</option>
            <option value="ACTIVE">진행중</option>
            <option value="COMPLETE">완료</option>
        </select><br><br>

        <label for="startDate">시작일:</label>
        <input type="date" id="startDate" name="startDate" required><br><br>

        <label for="endDate">종료일:</label>
        <input type="date" id="endDate" name="endDate" required><br><br>

        <label for="locationName">장소명:</label>
        <input type="text" id="locationName" name="locationName" required><br><br>

        <label for="latitude">위도:</label>
        <input type="text" id="latitude" name="latitude" required><br><br>

        <label for="longitude">경도:</label>
        <input type="text" id="longitude" name="longitude" required><br><br>

        <label for="radius">인증 반경 (미터):</label>
        <input type="number" id="radius" name="radius" value="5" required><br><br>

        <h3>요일 선택</h3>
        <div>
            <label><input type="checkbox" name="days" value="MON"> 월요일</label>
            <label><input type="checkbox" name="days" value="TUE"> 화요일</label>
            <label><input type="checkbox" name="days" value="WED"> 수요일</label>
            <label><input type="checkbox" name="days" value="THU"> 목요일</label>
            <label><input type="checkbox" name="days" value="FRI"> 금요일</label>
            <label><input type="checkbox" name="days" value="SAT"> 토요일</label>
            <label><input type="checkbox" name="days" value="SUN"> 일요일</label>
        </div>

        <input type="submit" value="목표 생성">
    </form>

    <h2>전체 목표 목록</h2>
    <div id="goalListContainer"></div>
    <form id="goalListForm" action="/api/v1/goals/check" method="get">
        <label for="userId">사용자 ID:</label>
        <input type="text" id="userId" name="userId" required ><br><br>
        <input type="submit" value="목표 목록 조회">
    </form>

    <h3>목표 상세 조회</h3>

    <form id="goalCheckForm" method="post">
        <label for="goalId">목표 ID:</label>
        <input type="number" id="goalId" name="goalId" required><br><br>
        <input type="submit" value="목표 상세 조회">
    </form>
    

    <h3>목표 활성화</h3>
    <form id="goalActivateForm" action="/api/v1/goals/10/activate" method="post">
        <input type="hidden" name="status" value="ACTIVE">
        <input type="submit" value="목표 활성화">
    </form>

    <h3>목표 삭제</h3>
    <form id="goalDeleteForm" action="/api/v1/goals/13/delete" method="post">
        <input type="submit" value="목표 삭제">
    </form>


    <h3> 목표 1차 인증 </h3>
    <form id = "goalAc" action="/api/v1/goals/20/achieve" method="post">
        <label for="qwe1">userId:</label>
        <input type="text" id="userId" name="userId" required><br><br>

        <label for="QWE2">latitude:</label>
        <input type="text" id="latitude" name="latitude" required><br><br>

        <label for="QWE3">longitude:</label>
        <input type="text" id="longitude" name="longitude" required><br><br>
        <input type="submit" value="목표 1차 인증">
    </form>

    <h3>목표 완료</h3>
    <form id="goalComplete" action="/api/v1/goals/15/complete" method="post">
        <input type="submit" value="목표 완료">
    </form>
    

    <script>
        document.getElementById("goalCheckForm").onsubmit = function(event) {
            event.preventDefault(); // 기본 폼 전송 막기
    
            var goalId = document.getElementById("goalId").value; // 입력된 값 가져오기
            if (!goalId) {
                alert("목표 ID를 입력하세요.");
                return;
            }
    
            // 동적으로 URL을 설정하고 폼 전송
            this.action = "/api/v1/goals/check/" + goalId;
            this.submit();
        };
    </script>

</body>
</html>
