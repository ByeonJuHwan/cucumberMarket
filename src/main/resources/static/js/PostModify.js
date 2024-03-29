const modifyButton = document.querySelector("#modifyButton");
modifyButton.addEventListener("click", function () {
  const title = document.querySelector("#title").value;
  const postContent = document.querySelector("#postContent").value;

  if (title == "" && postContent == "") {
    alert("제목과 내용을 입력해주세요.");
    return;
  } else if (title == "") {
    alert("제목을 입력해주세요.");
    return;
  } else if (postContent == "") {
    alert("내용을 입력해주세요.");
    return;
  }

  const check = confirm("수정하시겠습니까?");

  if (check) {
    // 사용자가 '확인'을 선택했을 때
    form.action = "/api/modify"; // 제출 요청 주소
    form.method = "post"; // 제출 요청 방식
    form.submit(); // 서버로 제출
  }
});

const image1 = document.querySelector("#image1");
image1.addEventListener("click", function () {
  let deleteModifyImage01Nav = document.querySelector(
    "#deleteModifyImage01Nav"
  );
  if (deleteModifyImage01Nav.style.display == "none") {
    deleteModifyImage01Nav.style.display = "block";
  } else {
    deleteModifyImage01Nav.style.display = "none";
  }
});

const image2 = document.querySelector("#image2");
image2.addEventListener("click", function () {
  let deleteModifyImage02Nav = document.querySelector(
    "#deleteModifyImage02Nav"
  );
  if (deleteModifyImage02Nav.style.display == "none") {
    deleteModifyImage02Nav.style.display = "block";
  } else {
    deleteModifyImage02Nav.style.display = "none";
  }
});

const deleteImage01Button = document.querySelector("#deleteImage01Button");
deleteImage01Button.addEventListener("click", function () {
  const postNo = document.querySelector("#postNo").value;
  let imageSrc = image1.src;
//  alert(imageSrc);
  imageSrc = imageSrc.substring(imageSrc.lastIndexOf('/') + 1);
//alert(imageSrc);
  const data = { imageSrc: imageSrc};

  axios
      .delete(`/post/reply/${postNo}?imageSrc=${imageSrc}`, {
         headers: {
            "Content-Type": `application/json`,
         },
      })
      .then((response) => {
        alert("사진이 삭제되었습니다.");
        location.reload();
      })
      .catch((error) => {
        console.log(error);
      });
  })

const deleteImage02Button = document.querySelector("#deleteImage02Button");
deleteImage02Button.addEventListener("click", function () {
  const postNo = document.querySelector("#postNo").value;
  let imageSrc = image2.src;
  imageSrc = imageSrc.substr(32);

  const data = { imageSrc: imageSrc};

  axios
    .delete("/post/reply/" + postNo, data, {
       headers: {
          "Content-Type": `application/json`,
       },
    })
    .then((response) => {
      alert("사진이 삭제되었습니다.");
      location.reload();
    })
    .catch((error) => {
      console.log(error);
    });
});

const modifyFile01 = document.querySelector("#modifyFile01");
modifyFile01.addEventListener("change", function () {
  const postNo = document.querySelector("#postNo").value;

  let file = modifyFile01.files[0];

  const data = { file: file };

  axios
    .put("/post/reply/1" + postNo, data, {
      headers: {
        "Content-Type": `multipart/form-data`,
      },
    })
    .then((response) => {
      image1.src = "http://localhost:8889/" + response.data;
      location.reload();
    })
    .catch((error) => {
      //handle error
    });
});

const modifyFile02 = document.querySelector("#modifyFile02");
modifyFile02.addEventListener("change", function () {
  const postNo = document.querySelector("#postNo").value;

  let file = modifyFile02.files[0];

  const data = { file: file };

  axios
    .put("/post/reply/2" + postNo, data, {
      headers: {
        "Content-Type": `multipart/form-data`,
      },
    })
    .then((response) => {
      image1.src = "http://localhost:8889/" + response.data;
      location.reload();
    })
    .catch((error) => {
      //handle error
    });
});

const chooseFile = document.querySelector("#chooseFile");
chooseFile.addEventListener("change", function () {
  const postNo = document.querySelector("#postNo").value;

  let files = chooseFile.files;
  if (files.length > 2) {
    alert("사진은 2장이상 추가 못합니다!");
    return;
  }

  let file = chooseFile.files[0];
  const data = { file: file };

  axios
    .put("/post/reply/3" + postNo, data, {
      headers: {
        "Content-Type": `multipart/form-data`,
      },
    })
    .then((response) => {
      if (response.data == "사진은 2장까지 가능합니다!!") {
        alert(response.data);
        return;
      }
      alert(response.data);
      location.reload();
    })
    .catch((error) => {
    });
});
